 package me.justshare.storage;
 
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * User: filippo@diotalevi.com
  * Date: Nov 17, 2010
  */
 public class Utils {
 
     public static String guessContentType(String name) {
        if (name == null)
            return "description";
        else if (name.endsWith(".jpg") || name.endsWith(".jpeg"))
             return "image/jpeg";
         else if (name.endsWith(".png"))
             return "image/x-png";
         else if (name.endsWith(".gif"))
             return "image/gif";
         else
             return "application/octet-stream";
     }
 
 
     public static InputStream scaleImageIfNecessary(InputStream is, int maxwidth) throws IOException {
         BufferedImage sourceImage = ImageIO.read(is);
 
         int origWidth = sourceImage.getWidth();
 
         if (origWidth > maxwidth)
         {
             float scale = (float) maxwidth / (float) origWidth;
             int targetWidth = (int)(sourceImage.getWidth() * scale);
             int targetHeight = (int)(sourceImage.getHeight() * scale);
 
             BufferedImage scaledImage = new BufferedImage( targetWidth,
                     targetHeight,
                     BufferedImage.TYPE_INT_RGB );
             Graphics2D g2d = scaledImage.createGraphics();
             g2d.setRenderingHint( RenderingHints.KEY_INTERPOLATION,
                     RenderingHints.VALUE_INTERPOLATION_BILINEAR );
 
             g2d.drawImage( sourceImage, 0, 0, targetWidth, targetHeight, null );
 
             File tempFile = File.createTempFile("justshareme", "image");
             ImageIO.write( scaledImage, "jpeg", tempFile);
 
             return new FileInputStream(tempFile);
         }
         else
             return is;
 
     }
 
 }
