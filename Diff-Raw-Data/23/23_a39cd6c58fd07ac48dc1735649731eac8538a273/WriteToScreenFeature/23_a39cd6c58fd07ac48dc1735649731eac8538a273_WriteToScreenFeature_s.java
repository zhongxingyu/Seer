 package com.gamalinda.java.jwindow;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 public class WriteToScreenFeature implements KeyListener {
     private static final String TAG = WriteToScreenFeature.class.getSimpleName();
 
     //Desktop screen dimensions
     private static double SCREEN_WIDTH;
     private static double SCREEN_HEIGHT;
 
     public void execute() {
        JWindow w = new JWindow(); //A frameless window
        w.add(new JLabel("Hello World!"));
 
         getScreenDimensions();
         int centerHorizontal = (int) (SCREEN_WIDTH / 2);
         int centerVertical = (int) (SCREEN_HEIGHT / 2);
 
        w.setLocation(centerHorizontal, centerVertical);
        w.pack(); //Causes this Window to be sized to fit the preferred size and layouts of its subcomponents.
        w.setVisible(true);
     }
 
     private void getScreenDimensions() {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         SCREEN_WIDTH = screenSize.getWidth();
         SCREEN_HEIGHT = screenSize.getHeight();
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
 
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
 
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
     }
 }
