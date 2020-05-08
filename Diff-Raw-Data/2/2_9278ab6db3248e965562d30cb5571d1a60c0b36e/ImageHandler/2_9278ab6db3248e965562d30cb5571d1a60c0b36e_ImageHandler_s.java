 package models;
 
 import play.Logger;
 import play.libs.F;
 
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Class for handling speaker images, filenames related to image and storing them to disk.
  * This class uses ImageFetcher and ImageResizer internally to handle the dirty work.
  *
  * @author Knut Haugen <knuthaug@gmail.com>
  */
 public class ImageHandler {
 
     private File dir;
 
     public ImageHandler(String basedir) {
         dir = new File(basedir);
     }
 
     public HashMap<String, ImageInfo> handleImage(String basename, int year, String imageUrl, Map<String, Integer> sizes) {
        Logger.info("Handling image files for basename=%s year=%s, format=%s", basename, year);
         HashMap<String, ImageInfo> info = new HashMap<String, ImageInfo>();
         
         
         for(String sizeName : sizes.keySet()) {
             info.put(sizeName, handleFile(basename, year, imageUrl, sizeName, sizes.get(sizeName)));
         }
 
         return info;
     }
 
     private ImageInfo handleFile(String basename, int year, String imageUrl, String sizeName, int size) {
         F.Tuple<BufferedImage, String> image = ImageFetcher.fetch(imageUrl);
         String format = image._2;
 
         File file = getFileName(basename, year, sizeName, format);
         Logger.info("Checking for size %s (wide=%s)", sizeName, size);
 
         if(file.exists()) {
             Logger.info("File %s exists. Skipping to next size", file.getAbsoluteFile());
             return readFile(file, year);
         }
 
         File previousFile = getFileName(basename, year - 1, sizeName, format);
 
         if(previousFile.exists()) {
             Logger.info("File exists for basename, but for previous year. Updating it");
             previousFile.delete();
         }
 
         File nextFile = getFileName(basename, year + 1, sizeName, format);
 
         if(nextFile.exists()) {
             Logger.info("File exists for basename, but for more a recent year. Skipping this");
             return readFile(nextFile, year);
         }
 
 
         //must be a weak ref, or else we run out of memory quickly when fetching and scaling images
         WeakReference<BufferedImage> weakImage =  new WeakReference<BufferedImage>(ImageResizer.resize(image._1, size));
 
         ImageInfo info = new models.ImageWriter().write(weakImage.get(), image._2, file, year);
         //even with a weak ref, we need to gc to avoid messing too much with the memory settings
         // of the jvm running the test
         System.gc();
         return info;
 
     }
 
     private ImageInfo readFile(File file, int year) {
 
         try {
             Logger.info("trying to read file %s", file.getPath());
             BufferedImage image = ImageIO.read(file);
 
             if(image != null) {
                 return new ImageInfo(file.getPath(), image.getWidth(), image.getHeight(), year);
             }
         } catch (IOException e) {
             Logger.error("Caught exception %s when reading back image from path %s", e, file.getAbsolutePath());
         }
         return new ImageInfo(file.getPath(), 0, 0, year);
     }
 
     private File getFileName(String basename, int year, String size, String format) {
         return new File(dir + "/" + basename + "_" + year + "_" + size + "." + format.toLowerCase());
     }
 }
