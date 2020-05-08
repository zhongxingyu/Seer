 package com.java.server.core;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.sql.Connection;
 import java.util.GregorianCalendar;
 
 import com.java.server.controllers.ApplicationController;
 import com.java.server.database.Database;
 import com.java.server.util.logging.Log;
 
 public class Server{
 	private static int port = 8080;
 	static GregorianCalendar calendar = new GregorianCalendar();
 	public static void main(String[] args){
 		try {
 			Connection conn = Database.getInstance().connect();
 			Log.writeToFile("Connecting to db...");
 			ApplicationController ac = new ApplicationController();
 			Log.writeToFile("Launching application controller..");
			ac.addRoute("POST","/user_tbl/doLogOn/", "HTTP/1.1");
			ac.addRoute("POST", "/user_tbl/", "HTTP/1.1");
			Log.writeToFile("Routes added... ");
 			ServerSocket sock = new ServerSocket(port);
 			while(true){
 				Socket s = sock.accept();
 				Log.writeToFile("Socket accept");
 				new Thread(new SocketData(s)).start();
 			}
 		}
 		catch(Exception ex){
 			Log.writeToFile("Exception occured " + ex.toString());
 		}
 	}
 	
 	static class SocketData implements Runnable{
 		private Socket s;
 		private InputStream is;
 		private OutputStream os;
 		
 		private SocketData(Socket s) throws IOException{
 			this.s = s;
 			this.is = s.getInputStream();
 			this.os = s.getOutputStream();
 		}
 		
 		@Override
 		public void run() {
 			try {
 				readInputHeaders();
 				writeResponse("<html><body><h1>Welcome to server, man</h1><h2>This is server for chatting in varous chat rooms</h2>" +
 						"<h3>Enjoy</h3></body></html>");
 			}
 			catch(Exception ex){
 				Log.writeToFile("Exception occurred in method run() " + ex.toString());
 			}
 			finally {
 				try {
 					s.close();
 				}
 				catch(Exception ex){
 					Log.writeToFile("Exception occured while trying to close socket " + ex.toString());
 				}
 			}
 		}
 		
 		private void readInputHeaders(){
 			try {
 				BufferedReader br = new BufferedReader(new InputStreamReader(is));
 				while(true){
 					String s = br.readLine();
 					if(s == null || s.trim().length() == 0) {
 						break;
 					}
 				}
 			}
 			catch(Exception ex){
 				Log.writeToFile("Exception occured in method readInputHeaders() " + ex.toString());
 			}
 		}
 		
 		private void writeResponse(String s){
 			try {
 				String response = "HTTP/1.1 200 OK\r\n" +
 						"Server: Myserver, "+calendar.getTime()+"\r\n" +
 						"Content-Type: text/html\r\n" +
 						"Content-Length: " + s.length() + "\r\n" +
 						"Connection: close\r\n\r\n";
 				String result = response + s;
 				os.write(result.getBytes());
 				os.flush();
 				Log.writeToFile(response);
 			}
 			catch(Exception ex){
 				Log.writeToFile("Exception occured in method writeResponse() " + ex.toString());
 			}
 		}
 	}
 }
