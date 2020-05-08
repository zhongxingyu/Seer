 import javax.swing.*;
 import java.awt.*;
 
 //this file contain program window
 public class Windows extends JFrame
 {
    int size_x = Automatika.fenetre_x ;
    int size_y = Automatika.fenetre_y ;

     Windows()
     {
         setTitle("Automatika");
         setSize(size_x, size_y);
         setLocationRelativeTo(null);
         setResizable(false);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setVisible(true);
     }
 }
 
 
 
 
 
 
 
 
 
