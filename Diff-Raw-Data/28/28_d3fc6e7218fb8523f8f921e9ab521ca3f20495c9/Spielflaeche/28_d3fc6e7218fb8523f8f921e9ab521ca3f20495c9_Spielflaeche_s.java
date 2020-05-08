 package spielfeld;
 
 import java.awt.Graphics;
 import java.awt.Image;
 
 import javax.swing.JPanel;
 
 import spielfigur.Spielfigur;
 
 /* *** Update ***/
 /* neue Version mit dynamischer Vergroeßerung der Bilder */
 
 public class Spielflaeche extends JPanel {
 
 	public static Spielfeld play;
 	public static Spielfigur bman;
 
 	private static final long serialVersionUID = 1L;
 
 	public void paint(Graphics g) {
 		play = new Spielfeld(20, 20); // intern wird das spielfeld als
 										// array implementiert und direkt
 										// befuellt mit 0
 		int arrayWidth = getWidth() / 20;
 		int arrayHeight = getHeight() / 20;
 		Image gras = play.loadImg("/ressources/grafics/gras.gif");
 		Image mauer = play.loadImg("/ressources/grafics/brick.jpg");
 		Image exit = play.loadImg("/ressources/grafics/ausgang.jpg");
		Image bomb = play.loadImg("/ressources/grafics/bombe.gif");
		Image man = play.loadImg("/ressources/grafics/brick.jpg");
 
 		play.Randfuellen(); // Befuellt die Raender mit Mauer
 		play.fill(18, 18, 1); // Ausgang
		play.fill(2, 2, 4); // Testbombe
		play.fill(6, 6, 10); // Testfigur
 
 		for (int x = 0; x < 20; x++) {
 			for (int y = 0; y < 20; y++) {
 
 				if (play.equalsGras(x, y)) {
 
 					g.drawImage(gras, x * arrayWidth, y * arrayHeight,
 							arrayWidth, arrayHeight, null);
 					// zeichnet
 					// Gras
 
 				}
 				if (play.equalsMauer(x, y)) {
 
 					g.drawImage(mauer, x * arrayWidth, y * arrayHeight,
 							arrayWidth, arrayHeight, null); // zeichnet
 					// Mauer
 
 				}
 				if (play.equalsExit(x, y)) {
 					g.drawImage(exit, x * arrayWidth, y * arrayHeight,
 							arrayWidth, arrayHeight, null); // zeichnet
 					// Ausgang
 				}
 				if (play.equalsBomb(x, y)) {
 					g.drawImage(bomb, x * arrayWidth, y * arrayHeight,
 							arrayWidth, arrayHeight, null);// Zeichnet
					// Bombe

					if (play.equalsMan(x, y)) {
						g.drawImage(man, x * arrayWidth, y * arrayHeight,
								arrayWidth, arrayHeight, null);// Zeichnet
						// Spielfigur
					}
 
 				}
 			}
 		}
 		repaint();
 	}
 }
