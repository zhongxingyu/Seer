 import javax.swing.JFrame;
 
 /**
  * A starter class for a Director-pattern Swing application program.
  */
 
 public class go {
 
     /**
      * Runs the program by creating a Director and passing it the
      * command-line arguments
      */
 
     public static void main(String args[]) {
         JFrame window = new JFrame("Coral Simulation");
         new Simulation(window);
         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //window.setLocationRelativeTo(null);
         window.pack();
         window.setVisible(true);
     }
 }
