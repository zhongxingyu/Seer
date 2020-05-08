 /* Copywright Kevin Y. Chen */
 
 package game;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 final public class Bmtron
 {
     static JFrame frame;
 
     static void menu()
     {
         frame.dispose();
 
         frame = new JFrame();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
         frame.setSize(size.width / 3, size.height);
         frame.setLocation(size.width / 3, 0);
         frame.setResizable(false);
         JButton button = new JButton("START GAME");
         button.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 start();
             }
         });
         frame.add(button);
         frame.setVisible(true);
     }
 
     static void start()
     {
         frame.dispose();
 
         frame = new JFrame("BM-TRON!");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(size.width, size.height);
         frame.setResizable(false);
         MyPanel panel = new MyPanel();
        frame.add(panel);
         frame.setVisible(true);
         panel.reset();
     }
 
    public static void main(String ... kevy)
     {
         frame = new JFrame();
         menu();
     }
 }
