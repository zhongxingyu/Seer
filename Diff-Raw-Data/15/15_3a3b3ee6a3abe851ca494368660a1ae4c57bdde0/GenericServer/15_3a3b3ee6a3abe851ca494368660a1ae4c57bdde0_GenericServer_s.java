 package edu.drexel.GOP;
 
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.io.DataOutputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.HashSet;
 
 public class GenericServer extends Thread implements PacketAcceptor {
 	private ServerSocket server;
 	private Socket client;
 	private DataOutputStream writer;
 	private DataInputStream reader;
 	private AtomicBoolean sentinel;
 	private HashSet<PacketAcceptor> connections;
 	
 	public GenericServer(int port) {
 		super();
 		sentinel = new AtomicBoolean(true);
 		connections = new HashSet<PacketAcceptor>();
 		try {
 			server = new ServerSocket(port);
 		}
 		catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.exit(-1);
 		}
 	}
 	public void run() {
 		while (sentinel.get()) {
 			int input;
 			try {
 				client = server.accept();
 				
 				System.out.println("Accepted connection from "+client.getLocalAddress()+":"+client.getLocalPort());
 				
 				writer = new DataOutputStream(client.getOutputStream());
 				reader = new DataInputStream(client.getInputStream());
 				
 				while ((input = reader.read()) >= 0) {
 					for (PacketAcceptor connection : connections) {
 						connection.accept(input);
 					}
 				}
 				
 				writer.close();
 				reader.close();
 				client.close();
 			}
 			catch (IOException e) {
				System.err.println(e.getMessage());
 			}
 		}
 	}
 	public void addPacketAcceptor(PacketAcceptor connection) {
 		connections.add(connection);
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
 	public void close() {
 		sentinel.set(false);
 		try {
 			server.close();
 		}
 		catch (IOException e) {
 			System.err.println(e.getMessage());
 		}
 	}
 }
