 import java.awt.*;
 import javax.imageio.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.util.List;
 
 public class Util {
     public static final Random random = new Random();
 
     public static int min(int... args) {
         if (args.length < 2)
             throw new RuntimeException("Too few arguments to Util.min");
 
         int min = args[0];
         for (int arg: args)
             if (arg < min)
                 min = arg;
 
         return min;
     }
 
     public static Image loadImage(String filename) {
         try {
             return ImageIO.read(resource("resources/img/" + filename));
         }
         catch (Exception e) {
             System.err.println("in Util.loadImage: " + e);
             return null;
         }
     }
 
     public static URL resource(String filename) {
         return Util.class.getResource(filename);
     }
 
     public static InputStream stream(String filename) {
         try {
             return Util.class.getResource(filename).openStream();
         }
         catch (IOException e) {
             System.err.println("in Util.stream: " + e);
             return null;
         }
     }
 
     public static Color alpha(Color color, double alpha) {
         int r = color.getRed();
         int g = color.getGreen();
         int b = color.getBlue();
 
         return new Color(r, g, b, (int)(255*alpha));
     }
     public static Color randomColor() {
         return Color.getHSBColor(random.nextInt(360) / 360.0f, 0.75f, 0.75f);
     }
     public static int randInt(int maxExclusive){
         return random.nextInt(maxExclusive);
     }
 
     public static void sleep() {
         try {
            Thread.sleep(50);
         }
         catch (InterruptedException e) {
             System.err.println("INTERRUPTED WHILE SLEEPING");
         }
     }
 }
