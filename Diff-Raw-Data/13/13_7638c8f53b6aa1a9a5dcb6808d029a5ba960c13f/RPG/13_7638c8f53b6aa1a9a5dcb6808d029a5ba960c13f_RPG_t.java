 package rpg;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import javax.swing.JDialog;
 
 /**
  * @author GoddardMorse & Mcat12
  */
 public class RPG {
     static String[] village = {
         "Rest at the inn                 ",
         "Level up at the training grounds",
         "Get a new weapon at the shop    "
     };
     static String[] inventory = {
         " ", " ", " ", " ", " "
     };
     static int[] inventorypower = {
         0, 0, 0, 0, 0
     };
     static int on = 1;
     static Object[] shop = {
         "Greatsword", "Potion", 
     };
     static int[] shoppower = {
         5, 10
     };
     static int [] shopgold = {
         50, 100
     };
     static int level = 1;
     static int exp = 0;
     static int hp;
     static int gold;
     static String whatclass;
     static int strength;
     static int magic;
     static String name;
     static String arguments = "-nothing";
     static boolean submitted = false;
     static int initiative;
     static int maxhp;
     static chooseclass v = new chooseclass();
     public static RPGFrame frame = new RPGFrame();
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         if (args.length > 0) {
             arguments = args[0];
         }
         if (arguments.equals("-rick")) {
             try {
                 java.awt.Desktop.getDesktop().browse(java.net.URI.create("http://dafk.net/what"));
             } catch (IOException ex) {
                 Logger.getLogger(RPG.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         JOptionPane.showMessageDialog(null, "Please build your character.");
         v.setVisible(true);
     }
     public static void updatestats() {
         frame.setthelabel (name +
                 "\nLevel: " + level +
                 "\nExp: " + exp +
                 "\nHP: " + hp + 
                 "\nStrength: " + strength + 
                 "\nMagic: " + magic +
                 "\nInitiative: " + initiative +
                 "\nGold:" + gold);
     }
 
 }
