 package no.ntnu.tdt4186.oving2;
 
 import java.util.LinkedList;
 
 /**
  * This class implements a queue of customers as a circular buffer.
  */
 public class CustomerQueue {
	private LinkedList<Customer> queue;
 	private int queueLength;
 	private Gui gui;
 	private int[] chairs;
 	private int paid;
 	private int served;
 	
 	/**
 	 * Creates a new customer queue.
 	 * @param queueLength	The maximum length of the queue.
 	 * @param gui			A reference to the GUI interface.
 	 */
     public CustomerQueue(int queueLength, Gui gui) {
     	this.queueLength = queueLength;
     	this.gui = gui;
     	this.paid = 0;
     	this.served = 0;
    	this.queue = new LinkedList<Customer>();
     	this.chairs = new int[queueLength];
 	}
     
     public boolean isFull(){
    	return (queueLength == queue.size());
     }
     
     public boolean isEmpty(){
    	return(queue.size() == 0);
     }
    
    
    public synchronized void addCustomer(Customer c) {
 	   gui.println("Queue: Add customer");
 	   while (isFull()){
 		    try {
 				wait();
 			} catch (InterruptedException e) {
 				// Silence is gold
 			}
 	   	}
		queue.add(c);
 	    for (int i = 0; i < chairs.length; i++) {
			if(chairs[i] == 0) {
 				chairs[i] = c.getCustomerID();
 				gui.fillLoungeChair(i, c);
 				break;
 			}
 		}
 	    
 	    
 	    notify();
 	   
    }
 
    public synchronized Customer getFromQueueCustomer() {
 	   while(isEmpty()) {
 		   try {
 			   wait();
 		   } catch (InterruptedException e) {
 			   // Silence is gold
 		   }
 	   }
	   Customer c = queue.removeFirst();
 	   for (int i = 0; i < chairs.length; i++) {
 		   if(chairs[i] == c.getCustomerID()){
 			   chairs[i] = 0;
 			   gui.emptyLoungeChair(i);
 			   this.served++;
 		   }
 		   break;
 	   }
 	   notify();
 	   return c; 
    }
    
    public synchronized void payForHaircut(){
 	   this.paid++;
    }
 }
 
