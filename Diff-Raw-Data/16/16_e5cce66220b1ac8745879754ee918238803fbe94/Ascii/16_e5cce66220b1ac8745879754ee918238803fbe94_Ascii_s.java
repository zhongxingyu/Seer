 import java.util.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.image.*;
 import java.io.*;
 import javax.imageio.*;
 
 
 /**
  * Helper class for ASCII Art challenge.
  * Contains functions for evaluating solutions as well as drawing them.
  */
 
 // Usage: ascii imagefile basefile
 public class Ascii {
     static Font font;
     static {
         try {
             font = Font.createFont(Font.TRUETYPE_FONT, new File("font.ttf")).deriveFont(20.0f);
         } catch(Exception e) {
             e.printStackTrace();
         }
     }
     public static void main(String[] args) throws IOException {
         Scanner s = new Scanner(new File(args[0]));
         ArrayList<Letter> letters = new ArrayList<Letter>();
         while(s.hasNext()) {
             char letter = s.next().charAt(0);
             double x = s.nextDouble();
             double y = s.nextDouble();
             double rot = s.nextDouble();
             double size = s.nextDouble();
             float r = s.nextFloat();
             float g = s.nextFloat();
             float b = s.nextFloat();
             float a = s.nextFloat();
             letters.add(new Letter(x, y, rot, size, new Color(r,g,b,a), letter));
         }
         BufferedImage img = ImageIO.read(new File(args[1]));
         System.out.println((int)cost(img, letters));
         drawLetters(letters, img.getWidth(), img.getHeight());
     }
 
     /** Creates a window and displays letters on it.
      */
     static void drawLetters(Collection<Letter> letters, int width, int height) {
         Window frame = new Window();
         frame.setResizable(false);
         frame.letters = letters;
         frame.setSize(width, height);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setVisible(true);
     }
     static class Window extends JFrame {
         Collection<Letter> letters;
         public void paint(Graphics g) {
             draw((Graphics2D)g, letters, getWidth(), getHeight());
         }
     }
 
     /** Calculates cost of a solution consisting of letters.
      */
     static double cost(BufferedImage img, Collection<Letter> letters) {
         int height = img.getHeight(), width = img.getWidth();
         BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
         draw(tmp.createGraphics(), letters, width, height);
 
         double res=0;
         for(int i=0; i<height; ++i) {
             for(int j=0; j<width; ++j) {
                 res += diffRGB(img.getRGB(j,i), tmp.getRGB(j,i));
             }
         }
         return res;
     }
 
     /** Calculates approximate cost of a solution consisting of letters.
      */
     static double approxCost(BufferedImage img, Collection<Letter> letters) {
         int height = img.getHeight(), width = img.getWidth();
         BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
         draw(tmp.createGraphics(), letters, width, height);
 
         double res=0;
         for(int i=0; i<height; i+=7) {
             for(int j=0; j<width; j+=9) {
                 res += diffRGB(img.getRGB(j,i), tmp.getRGB(j,i));
             }
         }
        return res;
     }
     /** Calculates difference between two colors.
      */
     static double diffRGB(int a, int b) {
         double r=0;
         for(int i=0; i<3; ++i, a>>>=8, b>>>=8) {
             int d = (a&0xff) - (b&0xff);
             r += d*d;
         }
         return Math.sqrt(r);
     }
 
     /** Draws letters on a given graphics context.
      */
     static void draw(Graphics2D g, Collection<Letter> letters, int width, int height) {
         g.setColor(Color.white);
         g.fillRect(0,0,width,height);
         //              g.setFont(new Font("monospace", Font.BOLD, 20));
         g.setFont(font);
         g.setColor(Color.black);
         for(Letter i : letters) {
             g.setTransform(i.transform());
             g.setColor(i.color);
             g.drawString(""+i.letter, 0, 0);
         }
     }
 }
