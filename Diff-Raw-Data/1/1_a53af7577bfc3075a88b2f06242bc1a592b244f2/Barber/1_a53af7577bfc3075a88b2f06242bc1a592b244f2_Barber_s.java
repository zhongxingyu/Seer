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
 
 	public void run(){
 		while(running){
 			try {
 				gui.barberIsSleeping(barberid);
 				sleep((long) (Globals.barberSleep*Math.random()));
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			Customer customer;
 			try {
 				customer = customerQueue.takeCustomerFromQueue();
 			} catch (Exception e) {
 				e.printStackTrace();
 				gui.println("No customers in queue.");
 				continue;
 			}
 			gui.barberIsAwake(barberid);
 			gui.fillBarberChair(barberid, customer);
 			try {
 				sleep((long) (Globals.barberWork*Math.random()));
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			gui.emptyBarberChair(barberid);
 			
 		}
 	}
 }
 
