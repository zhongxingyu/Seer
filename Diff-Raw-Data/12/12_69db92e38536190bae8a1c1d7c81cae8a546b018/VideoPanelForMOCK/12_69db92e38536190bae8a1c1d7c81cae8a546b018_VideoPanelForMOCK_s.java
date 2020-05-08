 /*
  * VideoPanel.java
  * 
  * Created on 21.05.2011, 18:42:10
  */
 
 package at.ppmrob.examples.mock;
 
 import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
 import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
 import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
 import static com.googlecode.javacv.cpp.opencv_core.cvLine;
 import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
 import static com.googlecode.javacv.cpp.opencv_imgproc.CV_HOUGH_PROBABILISTIC;
 import static com.googlecode.javacv.cpp.opencv_imgproc.cvCanny;
 import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
 import static com.googlecode.javacv.cpp.opencv_imgproc.cvHoughLines2;
 
 import at.ppmrob.examples.FeatureDetection;
 import at.ppmrob.examples.MyCircle;
 import at.ppmrob.examples.MyLine;
 
 import com.codeminders.ardrone.ARDrone;
 import com.codeminders.ardrone.DroneVideoListener;
 
 import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
 import com.googlecode.javacv.cpp.avcodec.LPDIRECT3DSURFACE9;
 import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
 import com.googlecode.javacv.cpp.opencv_core.CvPoint;
 import com.googlecode.javacv.cpp.opencv_core.CvScalar;
 import com.googlecode.javacv.cpp.opencv_core.CvSeq;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.util.Vector;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * 
  * @author normenhansen
  */
 @SuppressWarnings("serial")
 public class VideoPanelForMOCK extends javax.swing.JPanel implements IDroneVideoListenerMOCK
 {
 
     private AtomicReference<BufferedImage> image          = new AtomicReference<BufferedImage>();
 //    private AtomicReference<IplImage> image          = new AtomicReference<IplImage>();
     private AtomicBoolean                  preserveAspect = new AtomicBoolean(true);
     private BufferedImage                  noConnection   = new BufferedImage(320,240, BufferedImage.TYPE_INT_RGB);//320, 240, BufferedImage.TYPE_INT_RGB);
 
     private FeatureDetection featureDetection = new FeatureDetection();
     /** Creates new form VideoPanel */
     public VideoPanelForMOCK()
     {
         initComponents();
         Graphics2D g2d = (Graphics2D) noConnection.getGraphics();
         Font f = g2d.getFont().deriveFont(24.0f);
         g2d.setFont(f);
         g2d.drawString("No video connection", 40, 110);
         image.set(noConnection);
     }
 
     public void setDrone(ARDroneMOCK drone)
     {
         drone.addImageListener(this);
     }
 
     public void setPreserveAspect(boolean preserve)
     {
         preserveAspect.set(preserve);
     }
 
     @Override
     public void frameReceived(int startX, int startY, int w, int h, IplImage im , /*int[] rgbArray,*/ int offset, int scansize)
     {
 //        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
 //        im.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
         image.set(im.getBufferedImage());
         repaint();
     }
 
     @Override
     public void paintComponent(Graphics g)
     {
         Graphics2D g2d = (Graphics2D) g;
         g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         int width = getWidth();
         int height = getHeight();
         drawDroneImage(g2d, width, height);
     }
 
     private void drawDroneImage(Graphics2D g2d, int width, int height)
     {
         BufferedImage im = image.get();
         if(im == null)
         {
             return;
         }
         int xPos = 0;
         int yPos = 0;
         if(preserveAspect.get())
         {
             g2d.setColor(Color.BLACK);
             g2d.fill3DRect(0, 0, width, height, false);
             float widthUnit = ((float) width / 4.0f);
             float heightAspect = (float) height / widthUnit;
             float heightUnit = ((float) height / 3.0f);
             float widthAspect = (float) width / heightUnit;
 
             if(widthAspect > 4)
             {
                 xPos = (int) (width - (heightUnit * 4)) / 2;
                 width = (int) (heightUnit * 4);
             } else if(heightAspect > 3)
             {
                 yPos = (int) (height - (widthUnit * 3)) / 2;
                 height = (int) (widthUnit * 3);
             }
         }
        
 
       
         if(im != null)
         {
         	int w = im.getWidth();
         	int h = im.getHeight();
         	IplImage iplImg = IplImage.createFrom(im);
         	Vector<MyLine> detectedLines = featureDetection.detectLines(iplImg, w, h);
         	Vector<MyCircle> detectedCircles = featureDetection.detectCircles(iplImg, w, h);
         	
         	IplImage imgWithCircles = featureDetection.drawCircles(iplImg, detectedCircles);
         	IplImage imgWithLinesAndCircles = featureDetection.drawLines(imgWithCircles, detectedLines);
             
         	
         	g2d.drawImage(imgWithLinesAndCircles.getBufferedImage(), xPos, yPos, width, height, null);
         }
     }
 
  
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed"
     // desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents()
     {
 
         setLayout(new java.awt.GridLayout(4, 6));
     }// </editor-fold>//GEN-END:initComponents
      // Variables declaration - do not modify//GEN-BEGIN:variables
      // End of variables declaration//GEN-END:variables
 	
 }
