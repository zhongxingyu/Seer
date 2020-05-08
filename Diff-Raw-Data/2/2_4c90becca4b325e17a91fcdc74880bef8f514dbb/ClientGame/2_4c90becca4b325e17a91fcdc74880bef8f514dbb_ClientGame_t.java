 package client;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 
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
 	public final ClientModel model;
 	public final ClientView view;
 	public final ClientController controller;
 	private volatile boolean running;
 	private ObjectInputStream in;
 
 	
 	
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
 		
 		this.running = true;
 		this.model = new ClientModel(conn);
 		this.view = new ClientView(this.model);
 		this.controller = new ClientController(this.model, this.view);
 		
 		model.addObserver(this.controller);
 		model.bet.addObserver(controller);
         model.changeState(State.READY);
         
         try {
			this.in =  new ObjectInputStream(conn.socket.getInputStream());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Execution start of this thread
 	 */
 	
 	public void run() {
 		
 		Object token = null;
 		Command task = null;
 		while(running) {
 			
 				try {
 					token = in.readObject();
 				} catch (IOException e) {
 					e.printStackTrace();
 					System.out.println("Connection closed");
 					break;
 				} catch (ClassNotFoundException e) {
 					continue;
 				}
 				System.out.println("Received token");
 				System.out.println(token);
 				if(token == null) {
 					System.out.println("token is null, discarding it");
 					continue;
 				} else {
 					task = (Command) token;
 					task.execute(this);
 					System.out.println("Executed command");
 				} 	
 		}
 		
 			
 	}
 	
 	
 	/**
 	 * Toggles the thread to stop execution
 	 */
 	
 	public void stop(){
 		this.running = false;
 	}
 }
