 package screencapture;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.Serializable;
import javax.imageio.ImageIO;
 
 /**
  *
  * @author JJ_2011
  */
 public class PicNode implements Serializable
 {    
     private BufferedImage img;
     private String fileName;
     
     public PicNode(BufferedImage img, String fileName)
     {
         this.img = img;
         this.fileName = fileName;
     }
     
     public synchronized BufferedImage getImage()
     {
         return this.img;
     }
     
     public synchronized String getFilePath()
     {
         return this.fileName;
     }
     
    /**
     * Converts the BufferedImage into bytes.
     * @return Returns the bytes of a buffered Image.
     */
    public byte[] getImageBytes()
     {
         byte[] buff = null;
         
         try
         {
             ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ImageIO.write(img, "jpg", bos);
             buff = bos.toByteArray();
            bos.flush();
            bos.close();
         }
         catch(Exception ex)
         {
            System.out.println("Problem Serializeing PicNode");
             ex.printStackTrace();
         }
         return buff;
     }
 }
