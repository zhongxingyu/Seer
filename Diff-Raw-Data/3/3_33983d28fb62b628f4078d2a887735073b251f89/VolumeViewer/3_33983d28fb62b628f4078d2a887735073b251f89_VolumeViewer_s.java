 package de.sofd.viskit.test.image3D.jogl;
 
 import java.awt.BorderLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.*;
 
import javax.media.opengl.*;
 import javax.swing.JFrame;
 
 import org.apache.log4j.*;
 
 import vtk.*;
 
 import com.sun.opengl.util.Animator;
 
import de.sofd.viskit.image3D.jogl.*;
 import de.sofd.viskit.image3D.jogl.view.*;
 import de.sofd.viskit.image3D.vtk.*;
 import de.sofd.viskit.image3D.vtk.util.*;
 
 @SuppressWarnings("serial")
 public class VolumeViewer extends JFrame 
 {
     static final Logger logger = Logger.getLogger(VolumeViewer.class);
     
     protected static Animator animator;
     
     public VolumeViewer() throws IOException
     {
         super("Volume Viewer");
         
         vtkImageData imageData = DicomReader.readImageData("D:/dicom/serie3");
         imageData.Update();
         int dim[] =  imageData.GetDimensions();
         
         vtkImageGaussianSmooth smooth = new vtkImageGaussianSmooth();
         smooth.SetInput(imageData);
         smooth.Update();
         vtkImageData imageData2 = smooth.GetOutput();
         
         logger.debug("image dimension : " + dim[0] + " " + dim[1] + " " + dim[2] + " " + imageData.GetPointData().GetScalars().GetSize());
         
         VolumeView volumeView = new VolumeView(imageData); 
         getContentPane().setLayout(new BorderLayout());
         this.add(volumeView, BorderLayout.CENTER);
         
         setSize(500, 500);
         setLocationRelativeTo(null);
         
         animator = new Animator(volumeView);
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
               // Run this on another thread than the AWT event queue to
               // make sure the call to Animator.stop() completes before
               // exiting
               new Thread(new Runnable() {
                   public void run() {
                     animator.stop();
                     System.exit(0);
                   }
                 }).start();
             }
           });
     }
     
     public static void main(String args[])
     {
         try {
             VTK.init();
             
             VolumeViewer volumeViewer = new VolumeViewer();
             
             volumeViewer.setVisible(true);
             animator.start();
         } catch (IOException e) {
             logger.error(e);
             e.printStackTrace();
         } catch (Exception e) {
             logger.error(e);
             e.printStackTrace();
         } 
         
     }
 }
