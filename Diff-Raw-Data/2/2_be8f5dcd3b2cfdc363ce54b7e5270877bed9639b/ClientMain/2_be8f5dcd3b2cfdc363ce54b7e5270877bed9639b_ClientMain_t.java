 package org.jboss.devconf2013.lab.byteman.client;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.charset.Charset;
 
 public class ClientMain {
 
 	private Socket socket;
 	int sentBytes = 0;
 
 	private void openSocket(int port) throws UnknownHostException, IOException {
 		socket = new Socket("localhost", port);
 	}
 
 	private void sendMessage(String message) throws IOException {
 		byte[] data = message.getBytes(Charset.defaultCharset());
 		sentBytes = data.length;
 		socket.getOutputStream().write(data);
 		System.out.printf("Sent message: %s", message);
 	}
 
 	private String receiveResponse() throws IOException, InterruptedException {
 		final byte[] data = new byte[sentBytes];
 		Thread t = new Thread(new Runnable() {
 
 			public void run() {
 				try {
 					socket.getInputStream().read(data);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 
 		});
		t.start();
 		t.join();
 		return new String(data, Charset.defaultCharset());
 	}
 
 	private void close() throws IOException {
 		socket.close();
 	}
 
 	public static void main(String[] args) throws IOException, InterruptedException {
 		int port = 0;
 		String message = "";
 		try {
 			port = Integer.valueOf(args[0]);
 			message = args[1];
 		} catch (Exception e) {
 			System.out.println("Usage: ClientMain <port> <message>");
 		}
 		ClientMain cm = new ClientMain();
 		cm.openSocket(port);
 		cm.sendMessage(message + "\n");
 		System.out.printf("Response: %s\n", cm.receiveResponse());
 		cm.close();
 	}
 
 }
