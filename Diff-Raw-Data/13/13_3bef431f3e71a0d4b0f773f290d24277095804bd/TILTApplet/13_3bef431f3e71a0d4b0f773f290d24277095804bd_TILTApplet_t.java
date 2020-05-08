 package tilt.applet;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import javax.swing.JApplet;
 import java.awt.BorderLayout;
 import java.awt.image.BufferedImage;
 import java.net.URL;
 import javax.imageio.ImageIO;
 import tilt.TiltGui;
 /**
  * Applet implementation of TILT application
  * @author desmond
  */
 public class TILTApplet extends JApplet
 {
     TiltGui gui;
     public void init() 
     {
         //Execute a job on the event-dispatching thread:
         //creating this applet's GUI.
         try 
         {
             javax.swing.SwingUtilities.invokeAndWait(new Runnable() 
             {
                 public void run()
                 {
                     try
                     {
                         String host = getParameter("host");
                         if ( host == null )
                             host = "localhost";
                         URL url1 = new URL("http://"+host+"/images/frontispiece3.jpg");
                         URL url2 = new URL("http://"+host+"/texts/preface.txt");
                         URLReader reader = new URLReader( url2 );
                         byte[] textBytes = reader.read();
                         String text=new String( textBytes, "UTF-8" );
                         BufferedImage img = ImageIO.read( url1 );
                         createGUI( text, img );
                     }
                     catch (Exception e) 
                     {
                         e.printStackTrace( System.out );
                     }
                 }
             });
         }
         catch ( Exception e )
         {
             e.printStackTrace( System.out);
         }
     }
     /**
      * Gui design is identical to commandline app
      * @param text the text of the file to display
      * @param img the first image to display
      * @throws Exception 
      */
     private void createGUI( String text, BufferedImage img ) throws Exception
     {
         gui = new TiltGui( text, img );
         getContentPane().add(gui, BorderLayout.CENTER);
     }
     public void setSize( int width, int height )
     {
         if ( gui != null )
         {
             gui.setSize( width, height );
         }
         else
             super.setSize( width, height );
     }
     public int getWidth()
     {
         if ( gui != null )
             return gui.getPreferredSize().width;
         else
             return super.getWidth();
     }
     public int getHeight()
     {
         if ( gui != null )
             return gui.getPreferredSize().height;
         else
             return super.getHeight();
     }
     public void start()
     {
     }
     public void stop()
     {
     }
     public void destroy()
     {
     }
 }
