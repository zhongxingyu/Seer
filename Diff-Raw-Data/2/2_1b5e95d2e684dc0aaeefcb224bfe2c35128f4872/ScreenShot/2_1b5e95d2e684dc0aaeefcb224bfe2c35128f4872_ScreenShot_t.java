 package org.netbeans.javafx.preview;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Window;
 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 import javax.swing.SwingUtilities;
 
 /**
  *
  * @author Adam
  */
 public class ScreenShot {
 
     public static void screenShot(final Window w) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 if (w.getComponents().length > 0) try {
                    w.addNotify();
                    w.validate();
                     Component c = w.getComponents()[0];
                     Dimension bounds = c.getSize();
                     BufferedImage bi = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
                     Graphics g = bi.getGraphics();
                     c.print(g);
                     ImageIO.write(bi, "PNG", SilentPremain.out); //NOI18N
                 } catch (Exception ex) {
                     ex.printStackTrace();
                     SilentPremain.out.close();
                     System.exit(1);
                 }
                 System.exit(0);
             }
         });
     }
 
 }
