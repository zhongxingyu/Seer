 package gui;
 
 import fap_java.Game;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 import javax.swing.UIManager;
 
 public class Fapplication {
     private static JFrame frame;
    private static Game worldMap = new Game(0);
     public Fapplication() {
         frame = new TheFrame();
         worldMap = new Game(0);
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension frameSize = frame.getSize();
         if (frameSize.height > screenSize.height) {
             frameSize.height = screenSize.height;
         }
         if (frameSize.width > screenSize.width) {
             frameSize.width = screenSize.width;
         }
         frame.setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );
         frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
         frame.setVisible(true);
     }
 
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             e.printStackTrace();
         }
         new Fapplication();
     }
 
     public static void setFrame(JFrame frame) {
         Fapplication.frame = frame;
     }
 
     public static JFrame getFrame() {
         return frame;
     }
 
     public static void setWorldMap(Game worldMap) {
         Fapplication.worldMap = worldMap;
     }
 
     public static Game getWorldMap() {
         return worldMap;
     }
 }
