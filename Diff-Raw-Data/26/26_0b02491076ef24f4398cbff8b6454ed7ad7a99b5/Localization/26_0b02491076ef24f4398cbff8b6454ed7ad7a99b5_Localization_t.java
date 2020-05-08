 /**
  * Localization.java
  * @author jmd
  * 
  * Main class for localization. Herp derp.
  */
  
 import javax.imageio.ImageIO;
 import java.awt.image.ColorConvertOp;
 import java.awt.color.ColorSpace;
 import java.io.File;
 import java.io.IOException;
  import java.awt.image.BufferedImage;
  import java.util.Arrays;               // for testing
  
  public class Localization {
     
     public static int[][] getMap(String filename) {
         int[][] map;
         BufferedImage img = null;
         try {
             img = ImageIO.read(new File(filename));
         } catch (IOException e) {
             e.printStackTrace();
         }
         
//         BufferedImage img_gs = null;
//         ColorConvertOp conv = 
//             new ColorConvertOp(img.getColorModel().getColorSpace(),
//                 ColorSpace.getInstance(ColorSpace.CS_GRAY),
//                 null);
//         img_gs = conv.filter(img,null);
        
         map = new int[img.getWidth()][img.getHeight()];
         
        int pixel;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                pixel = img.getRGB(x,y);
                map[x][y] = pixel == -1 ? 255 : 0;
            }
//            map[y] = img.getRGB(0,y,map[y].length,1,null,0,1);
         }
         
         return map;
     }
     
     public static void main(String[] args) {
         int[][] map = getMap(args[0]);
         System.out.println(Arrays.deepToString(map));
        
         // copypasta initalization stuff from other jawns
     }
  }
