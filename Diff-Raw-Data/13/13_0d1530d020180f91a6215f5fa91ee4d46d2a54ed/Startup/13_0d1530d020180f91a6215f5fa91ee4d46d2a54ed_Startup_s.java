 package lab3;
 
 /**
  * The responsibility of this class is to just start the program, which
  * means starting the main GUI window. There's nothing wrong with this class.
  *
  * @author      Jim Lombardo, WCTC Instructor
  * @version     1.00
  */
 public class Startup {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // Don't worry if you don't understand this code. It
         // will be explained later.
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 (new MainGUI()).setVisible(true);
             }
         });
     }
 
 }
