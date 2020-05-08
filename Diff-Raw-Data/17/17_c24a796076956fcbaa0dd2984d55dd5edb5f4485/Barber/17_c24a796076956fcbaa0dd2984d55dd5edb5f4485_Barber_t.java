 package no.gombos.tdt4186.exercise2;
 /**
  * This class implements the barber's part of the
  * Barbershop thread synchronization example.
  */
public class Barber extends Thread {
 	
	private Gui gui;
	private CustomerQueue customerQueue;
	private boolean running;
	private int barberid;
 	
 	/**
 	 * Creates a new barber.
 	 * @param queue		The customer queue.
 	 * @param gui		The GUI.
 	 * @param pos		The position of this barber's chair
 	 */
 	public Barber(CustomerQueue queue, Gui gui, int pos) { 
		this.customerQueue = queue;
		this.barberid = pos;
 		this.gui = gui;
 	}
 
 	/**
 	 * Starts the barber running as a separate thread.
 	 */
 	public void startThread() {
 		running = true;
 		start();
 	}
 
 	/**
 	 * Stops the barber thread.
 	 */
 	public void stopThread() {
 		running = false;
 	}
 
 	// Add more methods as needed
 }
 
