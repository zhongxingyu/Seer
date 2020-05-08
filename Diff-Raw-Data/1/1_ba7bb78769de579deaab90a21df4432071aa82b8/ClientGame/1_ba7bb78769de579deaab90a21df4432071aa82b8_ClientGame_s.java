 package client;
 
 import commands.Command;
 import poker.GUI.ClientView;
 
 
 /**
  * The ClientGame thread that holds all client side game
  * information and executes commands received from the server 
  * 
  * @author Aleksey
  *
  */
 public class ClientGame implements Runnable {
 
 	/**
 	 * ClientModel: the model
 	 * ClientView: the User interface
 	 * ClientController: the controller logic
 	 * running: switch to turn off the thread
 	 * taskList: a queue for server commands
 	 */
 	private ClientModel model;
 	private ClientView view;
 	private ClientController controller;
 	private volatile boolean running;
 	private TaskQueue taskList;
 	
 	
 	/**
 	 * Constructs a client game from a connection to the server and
 	 * and a taskQueue that will receive commands
 	 * 
 	 * @param conn
 	 * 	The connection to the server with a {@link Command} inputstream
 	 * @param queue
 	 *  The queue holding commands received from the server. Is being 
 	 *  listened on by this thread
 	 */
 	
 	public ClientGame(Conn conn, TaskQueue queue) {
 		
 		this.taskList = queue;
 		this.running = true;
 		this.model = new ClientModel(conn);
 		this.view = new ClientView(model);
 		this.controller = new ClientController(model, view);
 		
 		model.addObserver(this.controller);
 		view.setVisible(true);
 		
 	}
 	
 	/**
 	 * Execution start of this thread
 	 */
 	
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
 		
 		Command task = taskList.getNextTask();
 		task.execute(model, controller);
 		}
 	}
 	
 	/**
 	 * Toggles the thread to stop execution
 	 */
 	
 	public void stop(){
 		this.running = false;
 	}
 }
