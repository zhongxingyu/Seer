 package us.physion.ovation.interfaces;
 import java.awt.EventQueue;
 import javax.swing.SwingUtilities;
 import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
 public class EventQueueUtilities
 {
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
 
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
	    executorService.submit(r);
 	} else {
 	    r.run();
 	}
     }
 }
