 package edu.drexel.GOP;
 
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.io.DataOutputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.HashSet;
 
 public class GenericClient extends Thread implements PacketAcceptor {
 	private Socket socket;
 	private DataOutputStream writer;
 	private DataInputStream reader;
 	private AtomicBoolean sentinel;
 	private HashSet<PacketAcceptor> connections;
 	
 	public GenericClient(String hostName, int port) {
 		sentinel = new AtomicBoolean(true);
 		connections = new HashSet<PacketAcceptor>();
 		try {
 			socket = new Socket(hostName, port);
 			
 			System.out.println("Successfully connected to "+hostName+":"+port);
 			
 			writer = new DataOutputStream(socket.getOutputStream());
 			reader = new DataInputStream(socket.getInputStream());
 			
 		}
 		catch (UnknownHostException e) {
 			System.err.println(e.getMessage());
 		}
 		catch (IOException e) {
 			System.err.println(e.getMessage());
 		}
 	}
 	public GenericClient(int port) {
 		this("localhost", port);
 	}
 	public void run() {
 		while(sentinel.get()) {
 			int input;
 			try {
 				while ((input = reader.read()) >= 0) {
 					for (PacketAcceptor connection : connections) {
 						connection.accept(input);
 					}
 				}
 			}
 			catch (IOException e) {
				System.err.println(e.getMessage());
 			}
 		}
 	}
 	public void accept(int packet) {
 		System.out.print((char)packet);
 		try {
 			writer.write(packet);
 		}
 		catch (IOException e) {
			System.err.println(e.getMessage());
 		}		
 	}
 	public void addPacketAcceptor(PacketAcceptor connection) {
 		connections.add(connection);
 	}
 	public void close() {
 		sentinel.set(false);
 		try {
 			reader.close();
 			writer.close();
 			socket.close();
 		}
 		catch (IOException e) {
 			System.err.println(e.getMessage());
 		}
 	}
 }
