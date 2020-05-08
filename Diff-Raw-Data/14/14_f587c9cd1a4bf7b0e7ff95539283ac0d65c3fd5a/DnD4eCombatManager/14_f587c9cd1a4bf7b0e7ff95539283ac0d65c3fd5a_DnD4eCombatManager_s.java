 package cm;
 
 import javax.swing.JFrame;
 
 import cm.view.MainFrame;
 
 /**
  * Dungeons & Dragons 4th Edition Combat Manager, ported to Java.
  * 
  * @author Matthew Rinehart &lt;gomamon2k at yahoo.com&gt;
  */
 public class DnD4eCombatManager {
 	/**
 	 * Application version.
 	 */
	public static String VERSION = "1.0RC1";
 
 	/**
 	 * Invokes the main frame.
 	 * 
 	 * @param args
 	 *            unused
 	 * @since 1.0
 	 */
 	public static void main(String[] args) {
 		MainFrame frame = new MainFrame();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 }
