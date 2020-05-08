 package com.gamalinda.java.jframe;
 
 import com.gamalinda.java.util.Log;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class FeaturesWindow implements ActionListener {
     private static final String TAG = FeaturesWindow.class.getSimpleName();
 
     private static JFrame mainFrame;
     private static JMenuBar menuBar;
     private static JMenu fileMenu;
     private static JMenu featuresMenu;
 
    //Desktop screen dimensions
     private static double SCREEN_WIDTH;
     private static double SCREEN_HEIGHT;
 
     //Menu Item Names/Action
     private static final String EXIT = "Exit";
     private static final String WRITE_TO_SCREEN = "Write to Screen";
 
     private FeaturesWindow() {
     }
 
     public static FeaturesWindow buildWindow(String windowTitle, int width, int height) {
         FeaturesWindow featuresWindow = new FeaturesWindow();
         featuresWindow.mainFrame = new JFrame(windowTitle);
         featuresWindow.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         featuresWindow.mainFrame.setSize(width, height);
 
         return featuresWindow;
     }
 
     public void showMenuBar() {
         menuBar = new JMenuBar(); //The Menu Bar on Windows
         fileMenu = new JMenu("File"); //The Menu Bar Menu item
         featuresMenu = new JMenu("Features");
 
         menuBar.add(fileMenu); //Add the menus to the menu bar
         showFileMenuItems();
 
         menuBar.add(featuresMenu);
         showFeaturesMenuItems();
 
         mainFrame.getContentPane().add(BorderLayout.NORTH, menuBar);
     }
 
     private void showFileMenuItems() {
         JMenuItem exitMenuItem = new JMenuItem(EXIT); //Menu item
        exitMenuItem.addActionListener(this);
         fileMenu.add(exitMenuItem); //Adding a menu item to a menu
     }
 
     private void showFeaturesMenuItems() {
         JMenuItem writeToScreenItem = new JMenuItem(WRITE_TO_SCREEN);
         writeToScreenItem.addActionListener(this);
 
         featuresMenu.add(writeToScreenItem);
     }
 
     public void showWindow() {
         mainFrame.setVisible(true);
     }
 
     public void run() {
 
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(EXIT)) {
            Log.d(TAG, "exitApp()"); //Should log first before exit app because it won't be reached when exit
            exitApp();
        } else if (e.getActionCommand().equals(WRITE_TO_SCREEN)) {
             writeOnScreen();
            Log.d(TAG, "writeOnScreen()");
         }
     }
 
    private void exitApp() {
        System.exit(0);
    }

     private void writeOnScreen() {
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
 }
