 package net.ishchenko.omfp;
 
 import com.sun.pdfview.PDFFile;
 import com.sun.pdfview.PDFPage;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.Rectangle2D;
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.channels.FileChannel;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Max
  * Date: 10.04.2010
  * Time: 1:00:28
  */
 public class PreviewPanel extends JPanel {
 
     private int pageCounter;
     private PDFFile pdffile;
     private Image pageImage;
     private Image deviceImage;
 
     private int pageX;
     private int pageY;
     private int pageWidth;
     private int pageHeight;
 
     private final Object lock = new Object();
 
    private static final String HINT = "Use your mouse wheel, or arrow buttons to scroll the document";
     private Rectangle2D hintBounds;
 
     public PreviewPanel(File documentFile, File deviceFile) throws IOException {
 
         FileChannel channel = new RandomAccessFile(documentFile, "r").getChannel();
         pdffile = new PDFFile(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
 
         if (deviceFile != null) {
             deviceImage = ImageIO.read(deviceFile);
             parseCoordinateFromDevice(deviceFile);
             setPreferredSize(new Dimension(deviceImage.getWidth(null), deviceImage.getHeight(null)));
         } else {
             deviceImage = createImage(1, 1);
             PDFPage firstPage = pdffile.getPage(1, true);
             pageWidth = Math.round(firstPage.getWidth() * 1.5f);
             pageHeight = Math.round(firstPage.getHeight() * 1.5f);
             setPreferredSize(new Dimension(pageWidth, pageHeight));
         }
 
         addListeners();
 
         paginate(1);
 
     }
 
     private void parseCoordinateFromDevice(File deviceFile) {
         String deviceName = deviceFile.getName();
         String coordinates = deviceName.substring(deviceName.indexOf("--") + 2, deviceName.indexOf("."));
         for (String dimension : coordinates.split("-")) {
             if (dimension.startsWith("x")) {
                 pageX = Integer.parseInt(dimension.substring(1));
             } else if (dimension.startsWith("y")) {
                 pageY = Integer.parseInt(dimension.substring(1));
             } else if (dimension.startsWith("w")) {
                 pageWidth = Integer.parseInt(dimension.substring(1));
             } else if (dimension.startsWith("h")) {
                 pageHeight = Integer.parseInt(dimension.substring(1));
             }
         }
     }
 
     @Override
     protected void paintComponent(Graphics g) {
 
         if (deviceImage != null) {
             g.drawImage(deviceImage, 0, 0, null);
         }
         g.drawImage(pageImage, pageX, pageY, null);
 
         if (hintBounds == null) {
             hintBounds = g.getFontMetrics().getStringBounds(HINT, g);
         }
 
         g.setColor(Color.WHITE);
         g.fillRect(10, 3, (int) hintBounds.getWidth() + 20, (int) hintBounds.getHeight() + 10);
         g.setColor(Color.BLACK);
         g.drawRect(10, 3, (int) hintBounds.getWidth() + 20, (int) hintBounds.getHeight() + 10);
         g.drawString(HINT, 20, 20);
 
     }
 
     private void addListeners() {
 
         addMouseWheelListener(new MouseWheelListener() {
             public void mouseWheelMoved(MouseWheelEvent e) {
                 if (e.getWheelRotation() == -1) {
                     if (pageCounter > 1) {
                         paginate(-1);
                     }
                 } else {
                     if (pageCounter < pdffile.getNumPages()) {
                         paginate(1);
                     }
                 }
             }
         });
 
         getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "paginate_up");
         getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "paginate_up");
         getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "paginate_down");
         getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "paginate_down");
 
         getActionMap().put("paginate_up", new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 if (pageCounter > 1) {
                     paginate(-1);
                 }
             }
         });
 
         getActionMap().put("paginate_down", new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 paginate(1);
             }
         });
 
     }
 
 
     /**
      * Is invoked from EDT only
      */
     private void paginate(final int increment) {
 
         if (!SwingUtilities.isEventDispatchThread()) {
             throw new IllegalStateException();
         }
 
         pageCounter += increment;
         final int counter = pageCounter;
 
         //kekeke
         new Thread(new Runnable() {
             public void run() {
                 synchronized (lock) {
                     pageImage = pdffile.getPage(counter, true).getImage(pageWidth, pageHeight, null, null, true, true);
                 }
                 repaint(pageX, pageY, pageWidth, pageHeight);
             }
         }).start();
     }
 
 }
