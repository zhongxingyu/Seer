 import java.awt.*;
 import javax.imageio.*;
 import java.io.*;
 import java.util.*;
 
 public class Util {
     public static final Random random = new Random();
 
     public static Image loadImage(String filename) {
         try {
            return ImageIO.read(new File("resources/img/" + filename));
         }
         catch (Exception e) {
             System.err.println(e);
             return null;
         }
     }
 
     public static Color randomColor() {
         return Color.getHSBColor(random.nextInt(360) / 360.0f, 0.75f, 0.50f);
     }
 }
