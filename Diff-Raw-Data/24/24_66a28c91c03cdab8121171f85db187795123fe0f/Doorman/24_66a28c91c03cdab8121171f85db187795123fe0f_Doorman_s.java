 
 
 /**
  * This class implements the doorman's part of the
  * Barbershop thread synchronization example.
  */
 public class Doorman extends Thread{
 	
 	private CustomerQueue queue = null;
 	private Gui gui = null;
	private boolean throwingInPeople = false;
 	
 	/**
 	 * Creates a new doorman.
 	 * @param queue		The customer queue.
 	 * @param gui		A reference to the GUI interface.
 	 */
 	public Doorman(CustomerQueue queue, Gui gui) { 
 		this.queue = queue;
 		this.gui = gui;
 	}
 
 	/**
 	 * Starts the doorman running as a separate thread.
 	 */
 	public void startThread() {
 		throwingInPeople = true;
 		start();
 	}
 
 	/**
 	 * Stops the doorman thread.
 	 */
 	public void stopThread() {
 		throwingInPeople = false;
 	}
 
 	public void throwingInPeople() {
 		try {
 			sleep(Globals.doormanSleep);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		queue.addCustomer(new Customer());
 	}
 }
