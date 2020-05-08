 /*
  * Part of Fresco software under GPL licence
  * http://www.gnu.org/licenses/gpl-3.0.txt
  */
 package fresco;
 
 import fresco.swing.CAppWindow;
 
 /**
  *
  * @author gimli
  */
 public class Main {
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String[] args) {
 		CData.mainFrame = new CAppWindow("Fresco");
 		CData.mainFrame.setVisible(true);
 	}
 }
