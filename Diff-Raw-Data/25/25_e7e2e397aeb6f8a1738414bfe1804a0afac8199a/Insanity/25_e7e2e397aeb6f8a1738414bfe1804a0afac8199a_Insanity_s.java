package insanity;

 import java.awt.Dimension;
 import java.awt.Toolkit;
 import javax.swing.JFrame;
 
 public class Insanity 
 {
     public static void main(String[] args) throws Exception
     {
         Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
         GUI gui = new GUI(d);
         
         JFrame frame = new JFrame("Insane Mouse");
         frame.setSize(d);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setVisible(true);
         frame.getContentPane().add(gui);
     }
 }
