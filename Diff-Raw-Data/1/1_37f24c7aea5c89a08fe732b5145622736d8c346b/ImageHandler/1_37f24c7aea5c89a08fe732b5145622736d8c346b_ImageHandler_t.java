 package mihai.camera;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import lcm.lcm.LCM;
 import mihai.lcmtypes.image_path_t;
 import april.jcam.ImageSource;
 import april.jcam.ImageSourceFormat;
 import april.util.TimeUtil;
 
 public class ImageHandler
 {
     private ImageSource isrc;
     private ImageSourceFormat ifmt;
     private LCM lcm;
     
     public ImageHandler(String url, String dir, int fps, boolean loRes, boolean color16)
     {
         String camera = url;
         if(url.contains("//"))
         {
             camera = url.substring(url.indexOf("//") + 2);
         }
         if(url.contains("\\"))
         {
             camera = url.substring(url.indexOf("\\") + 2);
         }
         
         dir = dir + (!dir.endsWith(File.separator) ? File.separator : "") + camera; 
         new File(dir).mkdirs();
         
         try
         {
             lcm = LCM.getSingleton();
             isrc = ImageSource.make(url);
             isrc.setFormat(Integer.parseInt("" + (loRes ? 1 : 0) + (color16 ? 1 : 0), 2));
            isrc.setFeatureValue(14, 1); // frame-rate-manual, idx=14
             isrc.setFeatureValue(15, fps); // frame-rate, idx=15
             ifmt = isrc.getCurrentFormat();
         } catch (IOException e)
         {
             e.printStackTrace();
         }
         
         (new Camera(dir, camera)).start();
     }
     
     class Camera extends Thread
     {
         String dir;
         String camera;
         
         public Camera(String dir, String camera)
         {
             this.dir = dir;
             this.camera = camera;
         }
         
         public void run()
         {
             int saveCounter = 0;
             isrc.start();
             
             while (true)
             {
                 byte imageBuffer[] = isrc.getFrame();
                 if (imageBuffer == null)
                 {
                     System.out.println("Err: null frame");
                     continue;
                 }
                 
                 try
                 {
                     //SAVE FILE
                     new FileOutputStream(new File(dir+File.separator+"IMG" + saveCounter)).write(imageBuffer);
                     
                     image_path_t img = new image_path_t();
                     img.img_path = dir+File.separator+"IMG" + saveCounter++;
                     img.format = ifmt.format;
                     img.width = ifmt.width;
                     img.height = ifmt.height;
                     img.utime = TimeUtil.utime();
                     img.id = -1;
                     lcm.publish("rec" + camera, img);
                 } catch (FileNotFoundException e)
                 {
                     e.printStackTrace();
                 } catch (IOException e)
                 {
                     e.printStackTrace();
                 }
             }
         }
     }
 }
