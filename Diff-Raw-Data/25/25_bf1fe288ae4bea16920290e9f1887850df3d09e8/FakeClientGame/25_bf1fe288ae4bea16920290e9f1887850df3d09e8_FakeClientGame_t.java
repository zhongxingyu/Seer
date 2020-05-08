 package tests;
 
 import poker.GUI.ClientView;
 import client.ClientController;
 import client.ClientGame;
 import client.Conn;
 import client.TaskQueue;
 
 public class FakeClientGame extends ClientGame {
 
 	private ClientController controller;
 	private ClientView view;
 	
 	public FakeClientGame(Conn conn, TaskQueue queue) {
		super(null);
 		this.controller = new EmptyController(null, null);
 		this.view = null;
 	}
 	
 }
