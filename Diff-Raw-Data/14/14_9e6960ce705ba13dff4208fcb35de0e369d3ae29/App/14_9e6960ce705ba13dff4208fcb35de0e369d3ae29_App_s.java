 package br.ufrj.dcc.compgraf.im;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import org.apache.log4j.Logger;
 
 import br.ufrj.dcc.compgraf.im.ui.FileChooser;
 
 public class App {
   private static Logger log = Logger.getLogger(App.class);
 
   public static void main(String[] args)
   {
     File selectedFile = FileChooser.chooseFile("/export/home/andromeda/flavio/Desktop/SIGA/");
     
     try {
       BufferedImage image = ImageIO.read(selectedFile);
       
       int newImageX = 80, newImageY = 80;
       
       BufferedImage newImage = new BufferedImage(newImageX, newImageY, BufferedImage.TYPE_INT_RGB);
       
       for (int x = 20, newx = 0; x < 100; x++, newx++)
       {
         for (int y = 0, newy = 0; y < 80; y++, newy++)
         {
           log.debug("Original: " + x + "x" + y + " - Novo: " + newx + "x" + newy);
           newImage.setRGB(newx, newy, image.getRGB(x, y));
         }
       }
       
      ImageIO.write(newImage, "jpeg", new File("dest.jpg"));
       
     } catch (IOException e) {
       log.error(e, e);
     }
   }
 }
