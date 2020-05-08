 package no.gombos.tdt4186.exercise2;
 
 /**
  * This class implements the doorman's part of the
  * Barbershop thread synchronization example.
  */
 public class Doorman extends Thread{
 	private Gui gui;
 	private CustomerQueue customerQueue;
 	private boolean running;
 	
 	/**
 	 * Creates a new doorman.
 	 * @param queue		The customer queue.
 	 * @param gui		A reference to the GUI interface.
 	 */
 	public Doorman(CustomerQueue queue, Gui gui) { 
 		this.gui = gui;
 		this.customerQueue = queue;
 	}
 
 	/**
 	 * Starts the doorman running as a separate thread.
 	 */
 	public void startThread() {
 		running = true;
 		start();
 	}
 
 	/**
 	 * Stops the doorman thread.
 	 */
 	public void stopThread() {
 		running = false;
 	}
 	
 	public void run(){
 		while(running){
 			try {
 				sleep((long)(Globals.doormanSleep*Math.random()));
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
				e.printStackTrace();
 			}
 			try {
 				customerQueue.addCustomerToQueue(new Customer());
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
				e.printStackTrace();
 				gui.println("Queue is full.");
 			}
 		}
 	}
 
 	// Add more methods as needed
 }
