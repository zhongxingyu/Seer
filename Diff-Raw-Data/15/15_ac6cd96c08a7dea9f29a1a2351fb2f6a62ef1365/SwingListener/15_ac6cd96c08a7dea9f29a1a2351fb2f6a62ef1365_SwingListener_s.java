 package org.jtrace.swing;
 
 import java.awt.image.BufferedImage;
 
 import javax.swing.SwingUtilities;
 
 import org.jtrace.TracerListener;
 import org.jtrace.ViewPlane;
 import org.jtrace.primitives.ColorRGB;
 
 public class SwingListener implements TracerListener {
 
   private TracerPanel panel;
   
   private Refresher refresherThread;
   
   private int refreshInterval;
   private int paintedPixels;
   
   public SwingListener(TracerPanel panel) {
     this(panel, 50);
   }
   
   public SwingListener(TracerPanel panel, int refreshInterval) {
     this.panel = panel;
     this.refreshInterval = refreshInterval;
   }
   
   @Override
   public void start(ViewPlane viewPlane) {
     BufferedImage buffer = (BufferedImage) panel.createImage(viewPlane.getHres(), viewPlane.getVres());
     
     panel.getDrawablePanel().setImage(buffer);
     
     refresherThread = new Refresher();
     refresherThread.start();
   }
 
   @Override
   public void afterTrace(ColorRGB color, int x, int y) {
    getImage().setRGB(x, y, color.toInt());
     
     paintedPixels++;
     panel.pixelsPainted(paintedPixels);
   }
 
   @Override
   public void finish() {
     paintedPixels = 0;
     
     SwingUtilities.invokeLater(new Runnable() {
       @Override
       public void run() {
         panel.resetProgressBar();
         panel.getDrawablePanel().revalidate(); // configuring the scroll bars
       }
     });
     
     refresherThread.terminate();
   }
   
   private BufferedImage getImage() {
     return panel.getDrawablePanel().getImage();
   }
   
   private class Refresher extends Thread {
     private boolean stop = false;
     
     public Refresher() {
       super("JTrace-Refresher");
       setDaemon(true);
     }
     
     @Override
     public void run() {
       while (true) {
         refresh();
         
         if (stop) {
           return;
         }
         
         try {
           Thread.sleep(refreshInterval);
         } catch (InterruptedException e) {
         }
       }
     }
 
     private void refresh() {
       try {
         SwingUtilities.invokeAndWait(new Runnable() {
           @Override
           public void run() {
             panel.repaint();
           }
         });
       } catch (Exception e) {
       }
     }
     
     public void terminate() {
       stop = true;
     }
   }
 
 }
