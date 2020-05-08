 package client;
 
 import java.util.Scanner;
 
 public class ServerListener implements Runnable {
 
 	private Scanner in;
 	private TaskQueue que;
 	
 	public ServerListener(Scanner in, TaskQueue que) {
 		this.in = in;
 		this.que = que;
 	}
 	
 	public void run() {
 		
 		while(true) {
			String token = in.next();
			que.addTask(token);
 		}
 	}
 }
