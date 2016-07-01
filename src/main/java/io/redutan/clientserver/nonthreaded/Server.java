package io.redutan.clientserver.nonthreaded;

import io.redutan.clientserver.MessageUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author myeongju.jung
 */
public class Server implements Runnable {
    ServerSocket serverSocket;
    volatile boolean keepProcessing = true;

    public Server(int port, int millisecondsTimeout) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(millisecondsTimeout);
    }

    @Override
    public void run() {
        System.out.println("Server Starting");

        while (keepProcessing) {
            try {
                System.out.println("accepting client");
                Socket socket = serverSocket.accept();
                System.out.println("got client");
//                processSync(socket);  // 동기호출
                process(socket);    // 비동기 호출
            } catch (Exception e) {
                handle(e);
            }
        }
    }

    private void handle(Exception e) {
        if ((!(e instanceof SocketException))) {
            e.printStackTrace();
        }
    }

    void process(Socket socket) {
        if (socket == null) {
            return;
        }

        Runnable clientHandler = new Runnable() {
            @Override
            public void run() {
                processSync(socket);
            }
        };
        Thread clientConnection = new Thread(clientHandler);
        clientConnection.start();
    }

    void processSync(Socket socket) {
        try {
            System.out.println("Server: getting message");
            String message = MessageUtils.getMessage(socket);
            System.out.println("Server: got message: " + message);
            Thread.sleep(1000);
            System.out.println("Server: sending reploy: " + message);
            MessageUtils.sendMessage(socket, "Processed: " + message);
            System.out.println("Server: sent");
            closeIgnoringException(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeIgnoringException(Closeable socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {

            }
        }
    }

    public void stopProcessing() {
        keepProcessing = false;
        closeIgnoringException(serverSocket);
    }
}
