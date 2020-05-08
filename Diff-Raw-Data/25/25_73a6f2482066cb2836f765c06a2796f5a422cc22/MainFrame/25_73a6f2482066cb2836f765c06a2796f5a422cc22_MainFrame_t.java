 package screens.controls;
 
 import javax.swing.*;
 import java.awt.*;
 
 public class MainFrame {
     private final JFrame frame;
 
     public MainFrame() {
         frame = new JFrame("Mafia Game");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds(0,0,screenSize.width, screenSize.height);
         frame.setVisible(true);
        frame.setResizable(false);
         frame.setBackground(Color.BLACK);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     }
 
     public ImagePanel createImagePanel(String imageFilePath) {
         frame.getContentPane().removeAll();
         ImagePanel imagePanel = new ImagePanel(new ImageIcon(imageFilePath).getImage());
         frame.getContentPane().add(imagePanel);
         return imagePanel;
     }
 }
