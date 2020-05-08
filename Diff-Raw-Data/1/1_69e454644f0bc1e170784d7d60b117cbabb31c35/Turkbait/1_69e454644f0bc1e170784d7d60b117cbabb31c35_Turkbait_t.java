 package turkbait;
 
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 
 import java.awt.Robot;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
 import javax.imageio.ImageIO;
 
 public class Turkbait {
     public static final int X = 540;
     public static final int Y = 50;
     public static final int W = 600;
     public static final int H = 400;
 
     private static Robot robot;
     private static int mouseX, mouseY;
 
     public static void main(String[] args) {
         /* Create the robot. */
         try {
             robot = new Robot();
         } catch (java.awt.AWTException e) {
             System.out.println("Could not create Robot.");
             System.exit(1);
             return;
         }
        robot.setAutoDelay(100);
 
         while (true) {
             robot.delay(1500);
             cast();
 
             BufferedImage screen = getArea();
             save(screen, "out.png");
             if (!find(screen)) {
                 System.out.println("error: could not find bobber!");
             }
 
             BufferedImage start = getBobber();
             for (int i = 0; i < 30; i++) {
                 BufferedImage now = getBobber();
                 double diff = diff(start, now);
                 System.out.println(diff);
                 if (diff > 900) {
                     System.out.println("BITE!");
                     break;
                 }
                 robot.delay(500);
             }
             robot.delay(500);
             robot.mousePress(InputEvent.BUTTON3_MASK);
             robot.mouseRelease(InputEvent.BUTTON3_MASK);
         }
     }
 
     private static double diff(BufferedImage a, BufferedImage b) {
         double sum = 0;
         for (int x = 0; x < a.getWidth(); x++) {
             for (int y = 0; y < b.getHeight(); y++) {
                 int rgbA = a.getRGB(x, y);
                 int rgbB = b.getRGB(x, y);
                 int ra = (rgbA >> 16) & 0xFF;
                 int ga = (rgbA >>  8) & 0xFF;
                 int ba = (rgbA >>  0) & 0xFF;
                 int rb = (rgbB >> 16) & 0xFF;
                 int gb = (rgbB >>  8) & 0xFF;
                 int bb = (rgbB >>  0) & 0xFF;
                 sum += Math.pow(ra - rb, 2);
                 sum += Math.pow(ga - gb, 2);
                 sum += Math.pow(ba - bb, 2);
             }
         }
         return sum / (a.getWidth() * b.getHeight());
     }
 
     private static boolean find(BufferedImage screen) {
         for (int x = 0; x < screen.getWidth(); x++) {
             for (int y = 0; y < screen.getHeight(); y++) {
                 int rgb = screen.getRGB(x, y);
                 int r = (rgb >> 16) & 0xFF;
                 int g = (rgb >>  8) & 0xFF;
                 int b = (rgb >>  0) & 0xFF;
                 if (r > 50 && b < 50 && g < 50) {
                     mouseX = X + x - 10;
                     mouseY = Y + y;
                     robot.mouseMove(mouseX, mouseY);
                     return true;
                 }
             }
         }
         return false;
     }
 
     private static void save(BufferedImage image, String name) {
         try {
             ImageIO.write(image, "PNG", new File(name));
         } catch (java.io.IOException e) {
             System.out.println("warning: failed to save image");
         }
     }
 
     private static BufferedImage getArea() {
         return robot.createScreenCapture(new Rectangle(X, Y, W, H));
     }
 
     private static BufferedImage getBobber() {
         Rectangle r = new Rectangle(mouseX - 25, mouseY - 25, 50, 50);
         BufferedImage bob = robot.createScreenCapture(r);
         return bob;
     }
 
     private static BufferedImage read(String name) {
         try {
             return ImageIO.read(Turkbait.class.getResource(name));
         } catch (java.io.IOException e) {
             System.out.println("Failed to read image.");
             System.exit(1);
         }
         return null;
     }
 
     private static void cast() {
         System.out.println("Casting ...");
         robot.keyPress(KeyEvent.VK_J);
         robot.keyRelease(KeyEvent.VK_J);
         robot.delay(2500);
     }
 }
