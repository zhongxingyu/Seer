 package edu.umich.mihai.camera;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import lcm.lcm.LCM;
 import april.jcam.ImageSourceFormat;
 import edu.umich.mihai.lcmtypes.image_path_t;
 
 /**
  * 
  * Saves images from the queue to a specified output directory and publishes the path via an LCM message 
  * 
  * @author Mihai Bulic
  *
  */
 public class ImageSaver extends Thread implements ImageReader.Listener
 {
     private LCM lcm = LCM.getSingleton();
 
     private String url;
     private HashMap<String, Integer> urls = new HashMap<String, Integer>();
     
     private boolean run = true;
     private Object lock = new Object();
     private boolean imageReady = false;
     private byte[] imageBuffer;
     private double timeStamp = 0;
     private int width = 0;
     private int height = 0;
     private String format = "";
     private String outputDir = "";
     private int saveCounter = 0;
 
     public ImageSaver(ImageReader ir, String url, String outputDir)
     {
         setUrls();
         if(urls.get(url) == null) return;
         this.url = url;
 
         this.outputDir = outputDir + "cam" + urls.get(url);
         File dir = new File(this.outputDir);
         dir.mkdirs();
         
         ir.addListener(this);
         ir.start();
     }
     
     public void run()
     {
         if(url == null) return;
         
         while (run)
         {
             synchronized(lock)
             {
                 try
                 {
                     while(!imageReady)
                     {
                         lock.wait();
                     }
                 } catch (InterruptedException e)
                 {
                     e.printStackTrace();
                 }
                 
                 image_path_t imagePath = new image_path_t();
     
                 try
                 {
                     imagePath.img_path = saveImage(imageBuffer);
                     imagePath.width = width;
                     imagePath.height = height;
                     imagePath.format = format;
                     imagePath.utime = (long)timeStamp;
                 } catch (NullPointerException e)
                 {
                     e.printStackTrace();
                 } catch (IOException e)
                 {
                     e.printStackTrace();
                 }
     
                 lcm.publish("cam" + urls.get(url), imagePath);
                 imageReady = false;
             }
         }
     }
 
     private String saveImage(byte[] image) throws NullPointerException, IOException
     {
         String filepath = outputDir + File.separator + "IMG" + saveCounter;
 
         if (image == null)
         {
             throw new NullPointerException();
         }
 
         new FileOutputStream(new File(filepath)).write(image);
 
         saveCounter++;
 
         return filepath;
     }
     
     private void setUrls()
     {
         urls.put("dc1394://b09d01008b51b8", 0);
         urls.put("dc1394://b09d01008b51ab", 1);
         urls.put("dc1394://b09d01008b51b9", 2);
         urls.put("dc1394://b09d01009a46a8", 3);
         urls.put("dc1394://b09d01009a46b6", 4);
         urls.put("dc1394://b09d01009a46bd", 5);
         urls.put("dc1394://b09d01008c3f62", 10);
         urls.put("dc1394://b09d01008c3f6a", 11); // has J on it
         urls.put("dc1394://b09d01008e366c", 12); // unmarked
     }
     
     /**
      * Stops the reader in a safe way
      */
     public void kill()
     {
         run = false;
     }
 
     @Override
     public void handleImage(byte[] im, ImageSourceFormat ifmt, double time)
     {
         synchronized(lock)
         {
             imageBuffer = im;
             width = ifmt.width;
             height = ifmt.height;
             format = ifmt.format;
             timeStamp = time;
             imageReady = true;
             lock.notify();
         }
     }
 }
