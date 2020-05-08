 package vooga.rts.gui;
 
 import java.awt.DisplayMode;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import javax.swing.JFrame;
 import javax.swing.RepaintManager;
 
 public class Window {
     
     private Canvas myCanvas;
     private JFrame myFrame;
     
     private boolean myFullscreen = false;
     
     private GraphicsDevice myGraphics;
     
     private DisplayMode myPrevDispMode;
 
     public Window () {
         myFrame = new JFrame();
         myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         myFrame.setUndecorated(true);
         myFrame.setVisible(true);
         myFrame.setIgnoreRepaint(true);
        //myFrame.createBufferStrategy(2);        
         myCanvas = new Canvas(myFrame.getBufferStrategy());
         myFrame.add(myCanvas);        
     }
     
     public void setFullscreen(boolean fullscreen) {        
         myGraphics = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
         DisplayMode displayMode = myGraphics.getDisplayMode();
         myPrevDispMode = myGraphics.getDisplayMode();
         
         if (myFullscreen == fullscreen) {
             return;
         }
         myFullscreen = fullscreen;
         if (myFullscreen) {            
             myFrame.setVisible(false);
             myFrame.dispose();
             myFrame.setUndecorated(true);
             try {
                 if (myGraphics.isFullScreenSupported()) {
                     myGraphics.setFullScreenWindow(myFrame);
                     
                 }
                 else {
                     System.out.println("Fail");
                 }
             } finally {
                 myGraphics.setDisplayMode(displayMode);
                 myFrame.setResizable(false);
                 myFrame.setAlwaysOnTop(false);
                 myFrame.setVisible(true);
             }
         }
         else
         {
             
             myFrame.setVisible(false);
             myFrame.dispose();
             myFrame.setUndecorated(false);
             try {
                 if (myGraphics.isFullScreenSupported()) {
                     myGraphics.setFullScreenWindow(null);
                     
                 }
                 else {
                     System.out.println("Fail");
                 }
             } finally {
                 myFrame.setLocationRelativeTo(null);                
                 myGraphics.setDisplayMode(myPrevDispMode);
                 myFrame.setResizable(true);                
                 myFrame.setVisible(true);
             }
         }
         myFrame.repaint();
     }
     
     public Canvas getCanvas() {
         return myCanvas;
     }
 }
