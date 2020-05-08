 package screencapture;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JLabel;
 
 public class MainThread extends Thread 
 {
    private boolean running;
     // Create Location to hold data
     private ConcurrentLinkedQueue<PicNode> data;
    private JLabel lblQueueSize;
     // Threads
     private PictureTakerThread pt;
     private PictureWriterThread pw;
     
     public MainThread()
     {
        this.lblQueueSize = null;
         this.running = false;
         // Initalize location to store data.
         this.data = new ConcurrentLinkedQueue<PicNode>();
         // Initalize Worker Threads
         pt = new PictureTakerThread(data);
         pt.setName("PictureTakerThread");
         pw = new PictureWriterThread(data);
         pw.setName("PictureWriterThread");
         // Set this threads name
         this.setName("Thread Manager");
     }
     
     @Override
     public void run()
     {
         // Set the running to true
         this.running = true;
        // Start Worker Threads
         pt.start();
         pw.start();
     }
     
     /**
      * Returns if the thread is running or not.
      */
     public synchronized boolean isRunning()
     {
         return this.running;
     }
     
     /**
     * Forces the thread to stop
      */
     public synchronized void kill()
     {
        this.running = false;
         try 
         {
             // Kill the PictureTakerThread
             pt.kill();
             // Join the PictureTakerThread
             pt.join();
             // Kill the PictureWriterThread
             pw.kill();
             // Join the PictureWriterThread.
             pw.join();
             // TODO: DEBUG prints out main thread is done.
             System.out.println("Main Thread is done.");
         } 
         catch (InterruptedException ex) 
         { 
             System.out.println("Unable to join thread.");
         }
     }
    /**
     * Sets lblQueueSize to the label in gui.
     */
 }
