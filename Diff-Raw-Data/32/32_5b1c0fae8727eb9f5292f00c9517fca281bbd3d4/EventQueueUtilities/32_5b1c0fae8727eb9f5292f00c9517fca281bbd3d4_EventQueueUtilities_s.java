 package us.physion.ovation.interfaces;
 import java.awt.EventQueue;
 import javax.swing.SwingUtilities;
 import java.lang.reflect.InvocationTargetException;
 
 public class EventQueueUtilities
 {
 
     public static void runOnEDT(Runnable r) {
 	if (EventQueue.isDispatchThread()) {
 	    r.run();
 	} else {
 	    SwingUtilities.invokeLater(r);
 	}
     }
     
     public static void runAndWaitOnEDT(Runnable r) throws InterruptedException {
 	if (EventQueue.isDispatchThread()) {
 	    r.run();
 	} else {
 	    try{
 	    SwingUtilities.invokeAndWait(r);
 	    } catch (InvocationTargetException e)
 		{
 		    e.printStackTrace(); //TODO: handle this better
 		}
 	}
     }
     
     public static void runOffEDT(Runnable r) {
 	if (EventQueue.isDispatchThread()) {
	    Thread t = new Thread(r);//TODO: executor service
	    t.start();
 	} else {
 	    r.run();
 	}
     }
 }
