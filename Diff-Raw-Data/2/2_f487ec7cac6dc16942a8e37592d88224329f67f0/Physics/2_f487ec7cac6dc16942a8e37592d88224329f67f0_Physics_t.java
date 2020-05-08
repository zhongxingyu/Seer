 import javax.swing.*;
 import java.awt.*;
 
public class Physics
 {
     public static void main(String[] Args) throws Exception
     {
         Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
         GUI gui = new GUI(d);
         
         JFrame frame = new JFrame("Physics");
         frame.setSize(d);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setVisible(true);
         frame.getContentPane().add(gui);
     } 
 }
