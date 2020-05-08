 package edu.iit.swyne.crawler.client;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 public class SwyneCrawlerServerClient implements Runnable {
 
 	private String host;
 	private int port;
 	private String command = "You never set a command silly!";
 
 	public SwyneCrawlerServerClient(String host, int port) {
 		this.host = host;
 		this.port = port;
 	}
 
 	public void setCommand(String command) {
 		this.command = command;
 	}
 
 	public void run() {
 		Socket socket = null;
 		PrintWriter out = null;
 		
 		try {
 			Thread.sleep(2000);
 			socket = new Socket(this.host, this.port);
 			out = new PrintWriter(socket.getOutputStream(), true);
 			
 			out.println(this.command);
 			socket.shutdownOutput();
 			socket.shutdownInput();
 			
 		} catch (UnknownHostException e) {
 			System.err.println("ERROR: "+e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.err.println("ERROR: "+e.getMessage());
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
 			Thread.sleep(8000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
