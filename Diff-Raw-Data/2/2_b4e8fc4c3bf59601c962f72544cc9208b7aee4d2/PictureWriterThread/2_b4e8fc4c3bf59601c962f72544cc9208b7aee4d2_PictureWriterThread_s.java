 package screencapture;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import javax.imageio.ImageIO;
 
 public class PictureWriterThread extends Thread
 {
     private ConcurrentLinkedQueue<PicNode> data; 
     private volatile boolean running;
     
     /**
      * Default Constructor
      * Creates a new ConcurrentLinkedQueue to hold data taken from the PictureTakerThread.
      * Sets running to true; This makes our loop in run tick.
      */
     public PictureWriterThread(ConcurrentLinkedQueue<PicNode> data)
     {
         this.data = data;
         this.running = true;
     }
     
     /**
      * Main operation of the PictureWriterThread.
      * Converts the data from the PictureTakerThread and turns them into images.
      */
     @Override
     public void run()
     {
         // TODO: DEBUG prints out that the PictureWriterThread has started.
         System.out.println("PictureWriter has started.");
         // Sets a node that is going to be reused many times to null.
         PicNode pn = null;
         
         try
         {
             // TODO: DEBUG variable holds timer information.
             long before = 0;
             while(running)
             {
                 write(pn, before);
                 sleep(0);
             }
             // Write out everything left in the buffer.
             while(!data.isEmpty())
             {
                 write(pn, before);
                 sleep(0);
             }
             // TODO: DEBUG prints out that the PictureWriterThread has ended.
             System.out.println("PictureWriterThread has ended.");
         }
         catch(IOException ex)
         { 
            System.out.println("Unable to create Binary stream.");
         }
     }
     
     /**
      * Creates a .jpg image of the next file in the ConcurrentLinkedQueue data.
      * @param pn A PictureNode used to hold the data removed from the ConcurrentLinkedQueue.
      */
     private synchronized void write(PicNode pn, long before) throws IOException
     {
         if(data != null && !data.isEmpty())
         {
             // Debug Timing
             before = System.currentTimeMillis();
             pn = data.remove();
             ImageIO.write(pn.IMAGE, "jpg", new File(pn.FILE_NAME));
             System.out.println("Write DT: " + (System.currentTimeMillis() - before));
         }
     }
     
     /**
      * Ends the main loop in the run method.
      */
     public synchronized void kill()
     {
         this.running = false;
     }
     
     /**
      * @return If there is still data in the ConcurrentLinkedQueue return true. 
      */
     public synchronized boolean hasData()
     {
         return (this.data.size() > 0 ? true : false);
     }
     
     /**
      * Method to sleep the thread.
      * @param millis Contains the amount of milli seconds we want to sleep for.
      */
     private void sleep(int millis)
     {
         try
         {
             Thread.sleep(millis);
         }
         catch(Exception ex)
         {
             System.out.println("Error Sleeping. Thread may have been interupted.");
         }
     }
 }
