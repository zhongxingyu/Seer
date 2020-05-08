 import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
 
 /**
  * A starter class for a Director-pattern Swing application program.
  */
 
 public class go {
 
     /**
      * Runs the program by creating a Director and passing it the
      * command-line arguments
      */
 
     public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
         JFrame window = new JFrame("Coral Simulation");
         new Simulation(window);
         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //window.setLocationRelativeTo(null);
         window.pack();
         window.setVisible(true);
     }
 }
