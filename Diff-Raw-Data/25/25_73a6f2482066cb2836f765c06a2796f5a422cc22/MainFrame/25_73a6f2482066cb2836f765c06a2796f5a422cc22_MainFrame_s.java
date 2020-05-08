 package screens.controls;
 
 import javax.swing.*;
 import java.awt.*;
 
 public class MainFrame {
     private final JFrame frame;
 
     public MainFrame() {
         frame = new JFrame("Mafia Game");
        frame.setBounds(100, 100, 600, 600);
         frame.setVisible(true);
         frame.setBackground(Color.BLACK);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     }
 
     public ImagePanel createImagePanel(String imageFilePath) {
         frame.getContentPane().removeAll();
         ImagePanel imagePanel = new ImagePanel(new ImageIcon(imageFilePath).getImage());
         frame.getContentPane().add(imagePanel);
        frame.pack();
         return imagePanel;
     }
 }
