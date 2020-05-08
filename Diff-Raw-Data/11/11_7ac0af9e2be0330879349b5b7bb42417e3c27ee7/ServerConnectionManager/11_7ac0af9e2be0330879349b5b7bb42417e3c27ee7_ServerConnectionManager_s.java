 package com.voracious.dragons.server;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.voracious.dragons.common.ConnectionManager;
 
 public class ServerConnectionManager implements Runnable {
	public static final String default_hostname = "127.0.0.1";
 	public static final int default_port = 35580;
 	public static final int CHANNEL_CONNECT_SLEEP = 300;
 	
 	private Logger logger = Logger.getLogger(ServerConnectionManager.class);
 	
 	private ConnectionManager cm;
 	private ServerSocketChannel ssc;
 	private List<User> users;
 	
 	public ServerConnectionManager(){
 		users = new ArrayList<User>();
 		bindAddresses(default_hostname, default_port);
 		cm = new ConnectionManager();
 		new Thread(cm).start();
 	}
 	
 	public void bindAddresses(String hostname, int port){
 		try {
 			ssc = ServerSocketChannel.open();
 			ssc.configureBlocking(false);
 			ssc.bind(new InetSocketAddress(InetAddress.getByName(hostname), port));
 		} catch (IOException e) {
 			logger.error("Failed to bind ports", e);
 		}
 	}
 	
 	public void acceptNewConnections(){
 		SocketChannel clientChannel;
 		try {
 			while((clientChannel = ssc.accept()) != null){
 				clientChannel.configureBlocking(false);
 				cm.sendMessage(clientChannel, "Hello!");
 				
 				clientChannel.register(cm.getReadSelector(), SelectionKey.OP_READ, new ByteArrayOutputStream());
 				
 				User newUser = new User(clientChannel);
 				users.add(newUser);
 				
 				logger.info("Client connected: " + clientChannel.getRemoteAddress().toString());
 				
 				authenticate(newUser);
 			}
 		} catch (IOException e) {
 			logger.error("Could not accept connection", e);
 		}
 	}
 	
 	public void authenticate(User user){
 		
 	}
 
 	@Override
 	public void run() {
 		while(true){
 			acceptNewConnections();
 			try {
 				Thread.sleep(CHANNEL_CONNECT_SLEEP);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
