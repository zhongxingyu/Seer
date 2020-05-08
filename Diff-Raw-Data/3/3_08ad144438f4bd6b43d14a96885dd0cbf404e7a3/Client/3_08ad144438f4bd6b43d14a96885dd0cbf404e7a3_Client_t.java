 package edu.buffalo.util;
 
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.Socket;
 
 public class Client {
 	private Socket socket;
 	private String host; // because we can connect to any host
 
 	public Client(String host) {
 		this.host = host;
 	}
 
 	public Client() {
 
 	}
 
 	/**
 	 * 
 	 * @param text
 	 * @param port
 	 */
 	public void sendTcp(String text, int port) {
 		String response = null;
 		try {
 
 			socket = new Socket(host, port);
 			socket.setSoTimeout(500);
 			ObjectOutputStream os = new ObjectOutputStream(
 					socket.getOutputStream());
 			os.writeObject(text);
 
 			ObjectInputStream inpstr = new ObjectInputStream(
 					socket.getInputStream());
 			response = (String) inpstr.readObject();
 
 		} catch (ClassNotFoundException cx) {
 			cx.printStackTrace();
 			// why this kolaveri?
 
 		} catch (InterruptedIOException timeout) {
 			timeout.printStackTrace();
 
 		} catch (IOException ex) {
 			// there can be so many reasons why this error coming up.
 			// let treat this as timeout
 			ex.printStackTrace();
 
 		} finally {
 			System.out.println("RESPONSE:" + response);
 
 		}
 	}
 
 	/**
 	 * sends a udp message at given address
 	 * 
 	 * @param ipAddress
 	 * @param port
 	 * @param message
 	 */
 	public void sendUdp(String ipAddress, int port, String message) {
 
 		try {
			InetAddress address = InetAddress.getByName(ipAddress);
 			DatagramSocket socket = new DatagramSocket(port);
 			DatagramPacket packet = new DatagramPacket(message.getBytes(),
 					message.length(), address, port);
 			socket.send(packet);
 			socket.close();
 
 		} catch (Exception ex) {
 
 			ex.printStackTrace();
 			// TODO print meaningful stuff
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 	public String connect(String ip, int port) {
 
 		try {
 			
 			this.socket = new Socket(ip, port);
 			
 			
 			// socket.setSoTimeout(500);
 			Connection connection = new Connection();
 			connection.localPort = this.socket.getLocalPort();
 			connection.remotePort = this.socket.getPort();
 			connection.ip = this.socket.getInetAddress().getHostAddress();
 			connection.hostname = this.socket.getInetAddress().getHostName();
 			connection.connectionId = ServerConnections.connectionCounter++;
 			
 			ServerConnections.activeConnection.add(connection);
 			
 			Runnable connectionHandler = new ConnectionHandler(this.socket);
 			new Thread(connectionHandler).start();
 			
 			connection.handler = (ConnectionHandler)connectionHandler;
 			
 			return "connected to " + connection.ip;
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		return "failed to connect";
 	}
 
 }
