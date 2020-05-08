 package screencapture;
 
 import java.awt.AWTException;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 public class PictureTakerThread extends Thread
 {
     private ConcurrentLinkedQueue<PicNode> data; 
     private volatile boolean running;
     
     /**
      * Default Constructor
      * Creates a new ConcurrentLinkedQueue to hold data taken from the PictureTakerThread.
      * Sets running to true; This makes our loop in run tick.
      */
     public PictureTakerThread(ConcurrentLinkedQueue<PicNode> data)
     {
         this.data = data;
         this.running = true;
     }
     
     /**
      * Takes pictures of the desktop screen.
      */
     @Override
     public void run()
     {
         try 
         {
             // TODO: DEBUG prints out that the PictureTakerThread has started.
             System.out.println("PictureTaker has started.");
             Robot robot = new Robot();
             Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
             BufferedImage bufferedImage;
             int counter = 0;
             while(running)
             {    
                 // Create a buffered image of the screen.
                 bufferedImage = robot.createScreenCapture(captureSize);
                 // Add the image data to the ConcurrentLinkedQueue
                 data.add(new PicNode(bufferedImage, counter + ".jpg"));
                 // Increase the image counter.
                 counter++; 
                 // Give the cpu some time.
                 sleep(0);
             }
             // TODO: DEBUG prints out that the PictureTakerThread has ended.
             System.out.println("PictureTakerThread has ended.");
         }
         catch(AWTException ex)
         { 
             System.out.println("AWT-EXCEPTION");
         }    
     }
     
     /**
      * Ends the main loop within the run method.
      */
     public synchronized void kill()
     {
         this.running = false;
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
