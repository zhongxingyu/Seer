 package client;
 
 import poker.GUI.ClientView;
 
 public class ClientGame implements Runnable {
 
 	private Conn conn;
 	private ClientModel model;
 	private ClientView view;
 	private ClientController controller;
 	private volatile boolean running;
 	private TaskQueue taskList;
 	
 	
 	public ClientGame(Conn conn, TaskQueue queue) {
 		
 		this.taskList = queue;
 		this.running = true;
 		this.conn = conn;
 		this.model = new ClientModel();
 		this.view = new ClientView(model);
 		this.controller = new ClientController(model, view);
 		
 		view.setVisible(true);
 		
 	}
 	
 	public void run() {
 		
 		while(running) {
 			synchronized (this.taskList) {
 				if (this.taskList.isEmpty()) {
 					try {
 						taskList.wait();
 					} catch (InterruptedException e) {
 
 						e.printStackTrace();
 					}
 				}
 			}
 			
 		// something goes here?? like
 		// parsing commands and executing them
 		}
 	}
 	
 	public void stop(){
 		this.running = false;
 	}
 	
 	public void ParseTask() {
		// need to implement message passing between client and server
 	}
 }
