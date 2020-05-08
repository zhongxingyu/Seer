 package org.chriswood.plugin.DirtBlock;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.logging.*;
 
 public class DirtBlockTalk extends Thread {
 	private static final Logger LOG = Logger.getLogger("DirtBlock");
 	private boolean running;
 	private final String host;
 	private final int port;
 	private ServerSocket server;
 	private DirtBlockHolder stats;
 	
 	public DirtBlockTalk(DirtBlockHolder stats, String host, int port) throws Exception {
 		this.host = host;
 		this.port = port;
 		this.stats = stats;
 		this.running = true;
 		initialize();
 	}
 	
 	private void initialize() throws Exception {
 		try{
 			server = new ServerSocket();
 			server.bind(new InetSocketAddress(host, port));
 		}catch(Exception e) {
 			LOG.log(Level.SEVERE, "[DirtBlock] Error initializing CraftStats+. Make sure your host and port are set correctly.", e);
 		}
 	}
 	
 	protected void shutdown(){
 		running = false;
 		if(server == null)
 			return;
 		try{
 			server.close();
 		}catch(Exception e) {
 			LOG.log(Level.WARNING, "[DirtBlock] Could not shutdown cleanly!");
 		}
 	}
 	
 	@Override
 	public void run() {
 		while(running) {
 			try {
 				Socket socket = server.accept();
 				socket.setSoTimeout(5000);
 				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 				
 				String line = in.readLine();
				if(!line.equals("GoForIt")) {
					LOG.log(Level.WARNING, "[DirtBlock] Bad response...");
					throw new Exception("[DirtBlock] Error with CraftStats response code. Contact Us.");
				}else {
 					out.write(stats.toJson());
 					out.newLine();
 					out.flush();
 				}
 				
 				//System.out.println("Data Upload..." + stats.toJson());
 				//Clear data after upload
				stats.clearData();
 				in.close();
 				out.close();
 				socket.close();
 				
 			} catch(SocketException e) {
				//LOG.log(Level.WARNING, "[DirtBlock] Server Reloaded? Protocol error. ", e.getStackTrace());
 			} catch(IOException e) {
 				LOG.log(Level.WARNING, "[DirtBlock] Error reading/writing. ", e.getStackTrace());
 			}catch(Exception e) {
 				LOG.log(Level.WARNING, "Error, some exception caught.", e);
 			} 
 		}
 	}
 }
