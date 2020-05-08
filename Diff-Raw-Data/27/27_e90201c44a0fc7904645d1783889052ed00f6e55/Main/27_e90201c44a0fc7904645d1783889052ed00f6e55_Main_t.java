 package smail.cli;
 
 import com.netflix.astyanax.Keyspace;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.SwingUtilities;
 import smail.cli.astyanax.Astyanax;
 import smail.cli.astyanax.Schema;
 import smail.cli.gui.LoginFrame;
 
 // @author lyubentodorov
 // @licence - MIT
 // Available at http://lyuben.herokuapp.com/casstor/ 
 // Source at https://github.com/lyubent/CassTor/ 
 //
 public class Main {    
     
     public static void main(String[] args) {
         
         // We want good threading!
         SwingUtilities.invokeLater(new Runnable() {
             
             @Override
             public void run(){
                 try {
                     //Schema.buildSchema(Astyanax.getKeyspaceContext());
                    LoginFrame loginFrame = new LoginFrame();
                     
                     //run tests
                    //new smail.cli.test.TestSuite().runTests();
                     
                 } catch (Exception ex) {
                     Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
     }
 }
