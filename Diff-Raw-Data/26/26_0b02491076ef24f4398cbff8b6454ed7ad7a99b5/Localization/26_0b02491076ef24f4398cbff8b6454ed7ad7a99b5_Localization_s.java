 /**
  * Localization.java
  * @author jmd
  * 
  * Main class for localization. Herp derp.
  */
  
 import java.imageio.ImageIO;
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
         
         map = new int[img.getWidth()][img.getHeight()];
         
        for (int y = 0; y < map.length; y++) {
            map[y] = img.getRGB(0,y,map[y].length,1,null,0,1);
         }
         
         return map;
     }
     
     public static void main(String[] args) {
         int[][] map = getMap(args[0]);
         System.out.println(Arrays.deepToString(map));
         // copypasta initalization stuff from other jawns
     }
  }
