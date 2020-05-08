 package com.candou.ic.navigation.jcwx.downloader;
 
 import java.awt.color.CMMException;
 import java.io.File;
 
 import org.apache.log4j.Logger;
 
 import com.candou.util.ImageUtil;
 
 public class ImageDownloader {
     private static Logger log = Logger.getLogger(ImageDownloader.class);
 
     public static String downloader(String imageUrl, String dbPath, String savePath) {
         String fileName = ImageUtil.getFileName(imageUrl);
 
        File dir = new File(savePath + ImageUtil.getDirName(imageUrl));
         if (!dir.exists()) {
             dir.mkdirs();
         }
 
         File targetFile = new File(dir.getAbsolutePath() + File.separator + fileName);
 
         try {
             if (!targetFile.exists()) {
                 int retry = 0;
                 do {
                     ImageUtil.remoteImage(imageUrl, targetFile);
                 } while (++retry <= 5 && !targetFile.exists());
 
                 if (targetFile.exists()) {
                     log.info("save to " + targetFile);
                 } else {
                     log.warn("download failed");
                 }
             }
         } catch (CMMException e) {
             log.error("image format error: " + e.getMessage());
             return null;
         }
 
         return dbPath + ImageUtil.getDirName(imageUrl) + File.separator + fileName;
     }
 
 }
