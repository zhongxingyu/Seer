 package cs437.som.demo;
 
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 /**
 * A simple frame for displaying a square image, scaled to 400x400 in a window.
 */
 public class ImageFrame extends JFrame {
     /** The height and width dimension to which the image will be scaled */
     private static final int IMAGE_DIMENSION = 400;
 
     /** The height and width the frame will be */
     private static final int FRAME_DIMENSION = 420;
 
     /** Image cache */
     Image image;
 
     /**
      * Display an image in a new frame.
      *
      * @param title The window title to use.
      * @param image The image to display.
      */
     public ImageFrame(String title, BufferedImage image) {
         super(title);
         this.image = image.getScaledInstance(IMAGE_DIMENSION, IMAGE_DIMENSION,
                 Image.SCALE_DEFAULT);
         setSize(FRAME_DIMENSION, FRAME_DIMENSION);
         setLocation(0, 0);
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         setVisible(true);
     }
 
     /**
      * Display an image in a new frame.
      *
      * @param title The window title to use.
      * @param image The image to display.
      * @param x The x location of the window's upper left corner.
      * @param y The y location of the window's upper left corner.
      */
     public ImageFrame(String title, BufferedImage image, int x, int y) {
         super(title);
         this.image = image.getScaledInstance(IMAGE_DIMENSION, IMAGE_DIMENSION,
                 Image.SCALE_DEFAULT);
         setSize(FRAME_DIMENSION, FRAME_DIMENSION);
         setLocation(x, y);
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         setVisible(true);
     }
 
     @Override
     public void paint(Graphics g) {
         super.paint(g);
         g.drawImage(image, 0, 0, IMAGE_DIMENSION, IMAGE_DIMENSION, this);
     }
 
     @Override
     public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
         return (infoflags & ALLBITS) != 0;
     }
 
     @Override public String toString() { return "ImageFrame"; }
 }
