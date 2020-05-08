 package client;
 
import pw.svn.server.ChatServer;
 
 public class Demo {
 	public static void main(String[] args) {
		new ChatServer("localhost", 8000);
 	}
 }
