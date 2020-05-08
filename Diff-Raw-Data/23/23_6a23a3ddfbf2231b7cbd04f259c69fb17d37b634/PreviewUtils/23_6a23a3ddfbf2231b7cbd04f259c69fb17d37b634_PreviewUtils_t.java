 package com.easytag.utils;
 
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.apache.commons.io.FileUtils;
 
 /**
  *
  * @author Vitaly
  */
 public class PreviewUtils {
     private final static int PREVIEW_WIDTH = 280;
     private final static int PREVIEW_HEIGHT = 180;   
     private final static int DEFAULT_WIDTH = 848;
     
     public final static String  POSTFIX = "prev280x180"; 
     public final static String  DEFAULT_POSTFIX = "_default";
     
     public static final Logger log = LogManager.getLogger(PreviewUtils.class.getName());
      
     public static BufferedImage scale(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth, double fHeight) {
         BufferedImage dbi = null;
         if(sbi != null) {
             //int a = sbi.get
             dbi = new BufferedImage(dWidth, dHeight, imageType);
             Graphics2D g = dbi.createGraphics();
             AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
             g.drawRenderedImage(sbi, at);
         }
         return dbi;
     } 
     
     public static void makePreview(String srcFile, String distFile) throws IOException {
         File imgF = new File(srcFile);        
         BufferedImage sbi = ImageIO.read(imgF);  
         int w = sbi.getWidth();
         int h = sbi.getHeight();
         BufferedImage scImg;
         double coef;
         if (h >= w) {
             coef = (double)PREVIEW_WIDTH / w;
             scImg = scale(sbi, sbi.getType(),
                 PREVIEW_WIDTH, PREVIEW_HEIGHT, coef, coef);
         } else {            
             int width;
             coef = (double)PREVIEW_HEIGHT/ h;
             width = (int)Math.round(w * coef);
             int offset = (width - PREVIEW_WIDTH) / 2;
             if (offset > 0) {
                 scImg = scale(sbi, sbi.getType(),
                     width, PREVIEW_HEIGHT, coef, coef);
                 log.info("Offset: " + offset);
                 scImg = scImg.getSubimage(offset, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT);
             } else {
                 coef = (double)PREVIEW_WIDTH / w;
                 scImg = scale(sbi, sbi.getType(),
                     PREVIEW_WIDTH, PREVIEW_HEIGHT, coef, coef);
             }
         }
         
         File outputfile = new File(distFile);
         ImageIO.write(scImg, "jpg", outputfile);              
     }
     
     public static void makeDefaultView(String srcFile, String distFile) throws IOException {
         File imgF = new File(srcFile);        
         BufferedImage sbi = ImageIO.read(imgF);  
         int w = sbi.getWidth();
         int h = sbi.getHeight();
         BufferedImage scImg;
         double coef;
         coef = (double)DEFAULT_WIDTH / w;
         
         if (coef > 1){            
             FileUtils.copyFile(imgF, new File(distFile));
         } else {
             int height = (int)Math.round(h * coef);
             scImg = scale(sbi, sbi.getType(),
                 DEFAULT_WIDTH, height, coef, coef);
             File outputfile = new File(distFile);
             ImageIO.write(scImg, "jpg", outputfile);            
         }          
     }
 }
