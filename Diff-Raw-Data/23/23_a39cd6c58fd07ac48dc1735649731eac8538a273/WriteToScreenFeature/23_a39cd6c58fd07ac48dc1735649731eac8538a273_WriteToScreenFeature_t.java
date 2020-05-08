 package com.gamalinda.java.jwindow;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 public class WriteToScreenFeature implements KeyListener {
     private static final String TAG = WriteToScreenFeature.class.getSimpleName();
 
    private static final int ESCAPE_KEY_CODE = 27;

     //Desktop screen dimensions
     private static double SCREEN_WIDTH;
     private static double SCREEN_HEIGHT;
 
    JFrame frame;

     public void execute() {
        frame = new JFrame(); //A frameless window
        frame.add(new JLabel("Hello World!"));
        frame.setUndecorated(true);
        frame.addKeyListener(this);
 
         getScreenDimensions();
         int centerHorizontal = (int) (SCREEN_WIDTH / 2);
         int centerVertical = (int) (SCREEN_HEIGHT / 2);
 
        frame.setLocation(centerHorizontal, centerVertical);
        frame.pack(); //Causes this Window to be sized to fit the preferred size and layouts of its subcomponents.
        frame.setVisible(true);
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
        if (e.getKeyCode() == ESCAPE_KEY_CODE) {
            frame.dispose();
        }
     }
 }
