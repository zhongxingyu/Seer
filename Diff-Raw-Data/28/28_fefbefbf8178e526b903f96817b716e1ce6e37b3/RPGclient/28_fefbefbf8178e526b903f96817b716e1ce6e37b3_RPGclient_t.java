 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.cakemix;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
 import java.io.File;
 import javax.swing.JFrame;
 import org.lwjgl.LWJGLUtil;
 import org.lwjgl.opengl.Display;
 
 /**
  *
  * @author cakemix
  */
 public class RPGclient {
 
     /**
      * @param args the command line arguments
      */
     private Game game;
 
     public RPGclient() {
 
         // create the frame to hold the window
         JFrame frame = new JFrame();
 
         // Create a new canvas and set its size.
         Canvas canvas = new Canvas();
         // Must be 640*480 to match the size of an Env3D window
         canvas.setSize(800, 600);
         // This is the magic!  The setParent method attaches the
         // opengl window to the awt canvas.
         try {
             Display.setParent(canvas);
         } catch (Exception e) {
         }
         frame.add(canvas, BorderLayout.CENTER);
 
         frame.pack();
         frame.setVisible(true);
 
        final Game game = new Game();
         
        frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                     game.isRunning = false;
                     //System.exit(1);
                    }
        });
        
        game.start();
        //new Game().start();
 
     }
 
     public static void main(String[] args) {
         // load lwjgl native librarys
         System.setProperty("org.lwjgl.librarypath",
                 new File(new File(System.getProperty("user.dir"), "native"),
                 LWJGLUtil.getPlatformName()).getAbsolutePath());
         System.setProperty("net.java.games.input.librarypath",
                 System.getProperty("org.lwjgl.librarypath"));
         
         // create an arg[] check to see if its requesting
         // client or server mode
         
         // run the client
         new RPGclient();
     }
 }
