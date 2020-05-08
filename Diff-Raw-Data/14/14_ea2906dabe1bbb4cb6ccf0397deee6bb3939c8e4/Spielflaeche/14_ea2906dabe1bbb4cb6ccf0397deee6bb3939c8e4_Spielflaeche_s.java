 package spielfeld;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 
 import javax.swing.JPanel;
 
 /* *** Update ***/
 /* neue Version mit dynamischer Vergroeßerung der Bilder */
 
 public class Spielflaeche extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public void paint(Graphics g) {
 
 		// auslesen einer Bilddatei, wichtig: vorher muss sie in Source Code in
 		// Eclipse importiert werden!
 		// src > import...>file system>pfad suchen > Datei importieren
		super.paint(g);
 		Image gras = Toolkit.getDefaultToolkit().getImage(
 				getClass().getResource("/ressources/grafics/gras.jpg"));
 		Image mauer = Toolkit.getDefaultToolkit().getImage(
 				getClass().getResource("/ressources/grafics/brick.jpg"));
 
 		Spielfeld play = new Spielfeld(20, 20); // intern wird das spielfeld als
 												// array implementiert
 
 		for (int x = 0; x < 20; x++) { // befuellt das Array mit einem neutralen
 										// Element 1, hier: Gras
 			for (int y = 0; y < 20; y++) {
				play.fill(x, y, 1);
 			}
 		}
 		for (int y = 0; y < 20; y++) {
 			play.fill(0, y, 2);
 		} // Raender mit Mauer befuellen
 		for (int y = 0; y < 20; y++) {
 			play.fill(19, y, 2);
 		}
 		for (int x = 0; x < 20; x++) {
 			play.fill(x, 0, 2);
 		}
 		for (int x = 0; x < 20; x++) {
 			play.fill(x, 19, 2);
 		}
 
 		for (int x = 0; x < 20; x++) {
 			for (int y = 0; y < 20; y++) {
 				// wichtig: Register Zahlen befinden sich in der Klasse
 				// Spielfeld.java
 
				if (play.getObj(x, y) == 1) { // zeichnet die Objekte 1
 					g.drawImage(gras, x * (getWidth() / 20), y
 							* (getHeight() / 20), getWidth() / 20 + 1,
 							(getHeight() / 20) + 1, null); // hier wird das bild
															// "bg.jpg"
 															// gezeichnet, also
 															// Grasflaeche
 
 				}
 				if (play.getObj(x, y) == 2) {
 					g.drawImage(mauer, x * (getWidth() / 20), y
 							* (getHeight() / 20), getWidth() / 20 + 19,
 							(getHeight() / 20) + 19, null);
 				} // +19 um die Groeße an Frame anzupassen, /20 weil es 20
 					// Bilder sind die sich den Frame teilen
 
 			}
 		}

 	}
 
 }
