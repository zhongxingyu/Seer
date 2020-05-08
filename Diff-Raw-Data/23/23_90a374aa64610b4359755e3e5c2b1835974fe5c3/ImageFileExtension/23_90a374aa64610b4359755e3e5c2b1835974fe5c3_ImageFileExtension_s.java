 package edu.wpi.first.smartdashboard.camera;
 
 import edu.wpi.first.smartdashboard.gui.DashboardFrame;
 import edu.wpi.first.smartdashboard.gui.DashboardPrefs;
 import edu.wpi.first.smartdashboard.gui.StaticWidget;
 import edu.wpi.first.smartdashboard.properties.IPAddressProperty;
 import edu.wpi.first.smartdashboard.properties.Property;
 import edu.wpi.first.smartdashboard.properties.StringProperty;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import javax.swing.SwingUtilities;
 import edu.wpi.first.wpijavacv.WPICamera;
 import edu.wpi.first.wpijavacv.WPIColorImage;
 import edu.wpi.first.wpijavacv.WPIGrayscaleImage;
 import edu.wpi.first.wpijavacv.WPIImage;
 import java.io.File;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author Ed Venator
  */
 public class ImageFileExtension extends StaticWidget {
 
     public static final String NAME = "Image";
 
     public class GCThread extends Thread {
 
         boolean destroyed = false;
 
         @Override
         public void run() {
             while (!destroyed) {
                 try {
                     Thread.sleep(10000);
                 } catch (InterruptedException ex) {
                 }
                 System.gc();
             }
         }
 
         public void destroy() {
             destroyed = true;
             interrupt();
         }
     }
 
     public class BGThread extends Thread {
 
         boolean destroyed = false;
         Runnable draw = new Runnable() {
 
             public void run() {
                 DashboardFrame.getInstance().getPanel().repaint(getBounds());
             }
         };
 
         public BGThread() {
             super("Camera Background");
         }
 
         @Override
         public void run() {
             WPIImage image;
             while (!destroyed) {
                 if (computerImage == null) {
                     loadImage(fileNameProperty.getSaveValue());
                 }
                image = computerImage;
                drawnImage = processImage((WPIColorImage) image).getBufferedImage();
                 SwingUtilities.invokeLater(draw);
             }
         }
 
         @Override
         public void destroy() {
             destroyed = true;
         }
     }
     WPIImage computerImage;
     private boolean resized = false;
     private BufferedImage drawnImage;
     private BGThread bgThread = new BGThread();
     private GCThread gcThread = new GCThread();
    public final StringProperty fileNameProperty = new StringProperty(this, "FileName", "C:/Users/Edward/FIRST-2012/TestImages/VisionTargetTest2.bmp");
 
     @Override
     public void init() {
         setPreferredSize(new Dimension(100, 100));
         bgThread.start();
         gcThread.start();
         revalidate();
         DashboardFrame.getInstance().getPanel().repaint(getBounds());
     }
 
     @Override
     public void propertyChanged(Property property) {
         if (property == fileNameProperty) {
             loadImage(fileNameProperty.getSaveValue());
         }
 
     }
 
     private void loadImage(String fileName) {
 
         BufferedImage img = null;
         try {
             System.out.println("Loading image: " + fileName);
             img = ImageIO.read(new File(fileName));
             System.out.println("Image is: " + img.getWidth() + "x" + img.getHeight());
         } catch (Exception ignore) {
             System.err.println("***ERROR**** Failed to load image: " + fileName);
            // ignore.printStackTrace(System.err);
            System.exit(1);
         }
        // Create a WPIColorImage instance to process
        computerImage = new WPIColorImage(img);
     }
 
     @Override
     public void disconnect() {
         bgThread.destroy();
         gcThread.destroy();
         super.disconnect();
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         if (drawnImage != null) {
             if (!resized) {
                 setPreferredSize(new Dimension(drawnImage.getWidth(), drawnImage.getHeight()));
                 revalidate();
             }
             int width = getBounds().width;
             int height = getBounds().height;
             double scale = Math.min((double) width / (double) drawnImage.getWidth(), (double) height / (double) drawnImage.getHeight());
             g.drawImage(drawnImage, (int) (width - (scale * drawnImage.getWidth())) / 2, (int) (height - (scale * drawnImage.getHeight())) / 2,
                     (int) ((width + scale * drawnImage.getWidth()) / 2), (int) (height + scale * drawnImage.getHeight()) / 2,
                     0, 0, drawnImage.getWidth(), drawnImage.getHeight(), null);
         } else {
             g.setColor(Color.PINK);
             g.fillRect(0, 0, getBounds().width, getBounds().height);
             g.setColor(Color.BLACK);
             g.drawString("NO FILE", 10, 10);
         }
     }
 
     public WPIImage processImage(WPIColorImage rawImage) {
         return rawImage;
     }
 
     public WPIImage processImage(WPIGrayscaleImage rawImage) {
         return rawImage;
     }
 }
