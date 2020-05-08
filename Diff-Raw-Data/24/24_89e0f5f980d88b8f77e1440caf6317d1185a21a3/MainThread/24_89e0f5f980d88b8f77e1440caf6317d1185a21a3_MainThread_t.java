 package screencapture;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 public class MainThread extends Thread 
 {
    private volatile boolean running;
     // Create Location to hold data
     private ConcurrentLinkedQueue<PicNode> data;
     // Threads
     private PictureTakerThread pt;
     private PictureWriterThread pw;
     
     public MainThread()
     {
        // Set The running status to false
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
        // Start the worker threads
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
     * Forces the thread and all worker threads to stop.
      */
     public synchronized void kill()
     {
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
            // Set the status of the thread
            this.running = false;
             // TODO: DEBUG prints out main thread is done.
             System.out.println("Main Thread is done.");
         } 
         catch (InterruptedException ex) 
         { 
             System.out.println("Unable to join thread.");
         }
     }
 }
