 package bombe;
 
 import spielfeld.Objekte;
 import spielfeld.Spielfeld;
 import spielfeld.Spielflaeche;
 
 //Bombe hat folgende Eigenschaften
 //int: intBombeX, intBombeY
 //Methoden: getX(), getY(), setX(int), setY(int), explodieren(int)
 
 public class Bombe extends Objekte {
 	int intBombeX, intBombeY;
 	BombType bomb;
 
 	public Bombe(int xPos, int yPos, int width, int height, BombType bomb) {
 		super(xPos, yPos, width, height, bomb.picPath, bomb.type);
 		this.bomb = bomb;
 		intBombeX = xPos;
 		intBombeY = yPos;
 	}
 
 	public void run() {// explodieren der bombe als thread.
 		int radius = bomb.radius;
 
 		boolean boolMauer = true;
 		int nachUnten = 0, nachOben = 0, nachRechts = 0, nachLinks = 0;
 		// zunaechst warten bis die Bombe explodieren soll.
 		warten(2000);
 
 		// in alle 4 richtungen explodieren bis zu einer mauer, kiste oder
 		// rechts
 		for (int i = 0; (i < radius) && (boolMauer); i++) {
			if ((Spielflaeche.play.equalsMauer(intBombeX + i, intBombeY) == false)) {// keine
 				// Mauer
 				if (Spielflaeche.play.equalsKiste(intBombeX + i, intBombeY, 2) == false) {// keine
 					// Kiste
 					nachRechts++;
 					Spielflaeche.play.fill(intBombeX + i, intBombeY, 3,
 							Spielfeld.Explosion);// explosion
 					// anzeigen
 					// Animation von Bombe + Bomberman stirbt + kiste geht
 					// kaputt
 				} else {// wird eine Kiste getroffen soll diese zerst�rt
 						// werden
 					// und es soll nicht weiter explodieren
 
 					boolMauer = false;
 					Spielflaeche.play.fill(intBombeX + i, intBombeY, 3,
 							Spielfeld.Explosion);
 					nachRechts++;
 					// nachRechts++;
 
 				}
 			} else {
 				// Mauer=> explosion in diese richtung h�rt auf.
 				boolMauer = false;
 
 			}
 
 		}
 		boolMauer = true;
 		// links
 		for (int i = 0; (i < radius) && (boolMauer); i++) {
 			if ((Spielflaeche.play.equalsMauer(intBombeX - i, intBombeY) == false)) {// keine
 				// Mauer
 				if (Spielflaeche.play.equalsKiste(intBombeX - i, intBombeY, 2) == false) {// keine
 					// Kiste
 					nachLinks++;
 					Spielflaeche.play.fill(intBombeX - i, intBombeY, 3,
 							Spielfeld.Explosion);
 					// explosion
 					// anzeigen
 					// Animation von Bombe + Bomberman stirbt + kiste geht
 					// kaputt
 				} else {// wird eine Kiste getroffen soll diese zerst�rt
 						// werden
 					// und es soll nicht weiter explodieren
 
 					boolMauer = false;
 					Spielflaeche.play.fill(intBombeX - i, intBombeY, 3,
 							Spielfeld.Explosion);
 					nachLinks++;
 
 				}
 			} else {
 				// Mauer=> explosion in diese richtung h�rt auf.
 				boolMauer = false;
 
 			}
 
 		}
 		boolMauer = true;
 
 		// oben
 		for (int i = 0; (i < radius) && (boolMauer); i++) {
			if ((Spielflaeche.play.equalsMauer(intBombeX, intBombeY + i) == false)) {// keine
																						// Mauer
 				if (Spielflaeche.play.equalsKiste(intBombeX, intBombeY + i, 2) == false) {// keine
 					// Kiste
 					nachOben++;
 					Spielflaeche.play.fill(intBombeX, intBombeY + i, 3,
 							Spielfeld.Explosion);// explosion
 					// anzeigen
 					// Animation von Bombe + Bomberman stirbt + kiste geht
 					// kaputt
 				} else {// wird eine Kiste getroffen soll diese zerst�rt
 						// werden
 					// und es soll nicht weiter explodieren
 
 					boolMauer = false;
 					Spielflaeche.play.fill(intBombeX, intBombeY + i, 3,
 							Spielfeld.Explosion);
 					nachOben++;
 				}
 			} else {
 				// Mauer=> explosion in diese richtung h�rt auf.
 				boolMauer = false;
 			}
 
 		}
 		boolMauer = true;
 
 		// unten
 		for (int i = 0; (i < radius) && (boolMauer); i++) {
 
			if ((Spielflaeche.play.equalsMauer(intBombeX, intBombeY - i) == false)) {// keine
 				// Mauer
 				if (Spielflaeche.play.equalsKiste(intBombeX, intBombeY - i, 2) == false) {// keine
 					// Kiste
 					nachUnten++;
 					Spielflaeche.play.fill(intBombeX, intBombeY - i, 3,
 							Spielfeld.Explosion);// explosion
 					// anzeigen
 					// Animation von Bombe + Bomberman stirbt + kiste geht
 					// kaputt
 				} else {// wird eine Kiste getroffen soll diese zerst�rt
 						// werden
 					// und es soll nicht weiter explodieren
 
 					boolMauer = false;
 					Spielflaeche.play.fill(intBombeX, intBombeY - i, 3,
 							Spielfeld.Explosion);
 					nachUnten++;
 
 				}
 			} else {
 				// Mauer=> explosion in diese richtung h�rt auf.
 				boolMauer = false;
 
 			}
 
 		}
 
 		boolMauer = true;
 		warten(750);
 		// explosionsauswirkung nach UNTEN
 		for (int i = 0; i < nachUnten; i++) {
 			/*
 			 * Erst wird an jeder Stelle das Array geleert.
 			 * Explosion,Bombe,zerstörbare Kiste werden zerstört mit destroy()
 			 * If: Wenn An der Stelle auf Ebene 1 nichts lag, zeichne Gras an
 			 * die Stelle else: Wenn Auf Ebene 1 ein Objekt liegt dann schiebe
 			 * das in die 3. Dimension und zeichne es. analog für die anderen
 			 * Explosionsabfragen weiter unten.
 			 */
 			Spielflaeche.play.destroy(intBombeX, intBombeY - i, 3);
 			Spielflaeche.play.destroy(intBombeX, intBombeY - i, 4);
 			Spielflaeche.play.destroy(intBombeX, intBombeY - i, 2);
 			if (Spielflaeche.play.getObj(intBombeX, intBombeY - i, 1) == null) {
 				Spielflaeche.play.fill(intBombeX, intBombeY - i, 3,
 						Spielfeld.Gras);
 			} else {
 				Spielflaeche.play.fill(intBombeX, intBombeY - i, 3,
 						Spielflaeche.play.getObj(intBombeX, intBombeY - i, 1));
 			}
 
 		}
 		// explosionsauswirkung nach OBEN
 		for (int i = 0; i < nachOben; i++) {
 
 			Spielflaeche.play.destroy(intBombeX, intBombeY + i, 3);
 			Spielflaeche.play.destroy(intBombeX, intBombeY + i, 4);
 			Spielflaeche.play.destroy(intBombeX, intBombeY + i, 2);
 
 			if (Spielflaeche.play.getObj(intBombeX, intBombeY + i, 1) == null) {
 				Spielflaeche.play.fill(intBombeX, intBombeY + i, 3,
 						Spielfeld.Gras);
 			} else {
 				Spielflaeche.play.fill(intBombeX, intBombeY + i, 3,
 						Spielflaeche.play.getObj(intBombeX, intBombeY + i, 1));
 			}
 
 		}
 		// explosionsauswirkung nach LINKS
 		for (int i = 0; i < nachLinks; i++) {
 			Spielflaeche.play.destroy(intBombeX - i, intBombeY, 4);
 			Spielflaeche.play.destroy(intBombeX - i, intBombeY, 2);
 			Spielflaeche.play.destroy(intBombeX - i, intBombeY, 3);
 
 			if (Spielflaeche.play.getObj(intBombeX - i, intBombeY, 1) == null) {
 				Spielflaeche.play.fill(intBombeX - i, intBombeY, 3,
 						Spielfeld.Gras);
 			} else {
 				Spielflaeche.play.fill(intBombeX - i, intBombeY, 3,
 						Spielflaeche.play.getObj(intBombeX - i, intBombeY, 1));
 			}
 		}
 		// explosionsauswirkung nach RECHTS
 		for (int i = 0; i < nachRechts; i++) {
 			Spielflaeche.play.destroy(intBombeX + i, intBombeY, 4);
 			Spielflaeche.play.destroy(intBombeX + i, intBombeY, 2);
 			Spielflaeche.play.destroy(intBombeX + i, intBombeY, 3);
 
 			if (Spielflaeche.play.getObj(intBombeX + i, intBombeY, 1) == null) {
 				Spielflaeche.play.fill(intBombeX + i, intBombeY, 3,
 						Spielfeld.Gras);
 			} else {
 				Spielflaeche.play.fill(intBombeX + i, intBombeY, 3,
 						Spielflaeche.play.getObj(intBombeX + i, intBombeY, 1));
 			}
 
 		}
 		Spielflaeche.bman.bombeLiegt = false;
 		Spielflaeche.bman.setBombPlanted(false);
 	}// bombeexplodieren
 }
