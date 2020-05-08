 package com.ece.superkids.images;
 
 import com.ece.superkids.FileManager;
 import com.ece.superkids.FileManagerImpl;
 import org.apache.commons.io.FileUtils;
 
 import java.io.*;
 import java.util.Properties;
 
 /**
  * The <code>FileImageManager</code> saves custom images on the local file system
  * in the user home directory.
  *
  * @author Ben Duong
  */
 public class FileImageManager implements ImageManager {
 
     FileManager fileManager = FileManagerImpl.getInstance();
 
     @Override
     public String saveImage(final String filePath, final String fileName) {
         File imageDirectory = fileManager.getImagesDirectory();
         String ext = filePath.substring(filePath.lastIndexOf("."));
         String key="";
 
         File newImage = new File(imageDirectory.getPath() + File.separator + fileName + ext);
         int counter = 0;
         while (newImage.exists()) {
             newImage = new File(imageDirectory.getPath() + File.separator + fileName + "-" + ++counter + ext);
         }
         Properties props = new Properties();
         try {
             props.load(new FileInputStream(fileManager.getImagePathsFile()));
            if (counter > 0) {
                key = fileName + counter;

            } else {
                key = fileName;
             }
 
             props.put(key, newImage.getPath());
 
             FileWriter writer = new FileWriter(fileManager.getImagePathsFile());
             props.store(writer, "Custom Images");
             writer.close();
 
             FileUtils.copyFile(new File(filePath), newImage);
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return key;
     }
 
     @Override
     public boolean deleteImage(final String key) {
         try{
             Properties props = new Properties();
             props.load(new FileInputStream(fileManager.getImagePathsFile()));
             String imagePath = props.getProperty(key);
             if(imagePath == null) {
                 throw new FileNotFoundException("Custom image does not exist");
             }
             new File(imagePath).delete();
 
             props.remove(key);
             props.store(new FileWriter(fileManager.getImagePathsFile()), "Custom Images");
             return true;
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         return false;
     }
 }
