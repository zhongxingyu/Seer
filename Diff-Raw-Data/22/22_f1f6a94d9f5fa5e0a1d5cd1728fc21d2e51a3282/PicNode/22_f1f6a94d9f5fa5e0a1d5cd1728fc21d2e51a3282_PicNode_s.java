 package screencapture;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
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
     
    public byte[] getBytes()
     {
         byte[] buff = null;
         
         try
         {
             ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            out.close();
             buff = bos.toByteArray();
         }
         catch(Exception ex)
         {
            System.out.println("Problem Serializeing Login");
             ex.printStackTrace();
         }
         return buff;
     }
 }
