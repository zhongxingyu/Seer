 import javax.swing.JLabel;
 
 /**
  * zustaendig fuer die Koordinierung der Bombenplatzierung und Stellen des Timers
  * 
  * @author Andrej Morlang
  * 
  */
 
 public class Bombe extends JLabel {
 
 	/* Deklaration & Initialisierung: */
 	private static final long serialVersionUID = 1L;
 	public int x = 0; // x-Koordinate (horizontale Position)
 	public int y = 0; // y-Koordinate (vertikale Position)
 	Zeit timer = new Zeit(); // Timer erstellen
 	boolean liegt = false, detoniert = false;
 
 	// Pro Richtung kann die Bombe maximal einen Treffer verursachen:
 	boolean treffer_oben = false, treffer_rechts = false,
 			treffer_unten = false, treffer_links = false;
 	boolean hulk_getroffen = false;
 
 	int n = MapLoader.get_n();
 
 	/* Konstruktoren: */
 	// Konstruktor 1:
 	/**
 	 * @param x
 	 *            uebernimmt x-Koord. der Bombe
 	 * @param y
 	 *            uebernimmt y-Koord. der Bombe
 	 */
 	
 	public Bombe(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	// Konstruktor 2:
 	public Bombe() {
 	}
 
 	/* METHODEN: */
 
 	// aktivieren-Methode:
 	/**
 	 * Aktiviert den Timer unfuegt ihn an die {@code Map Menue.game()}-Variable
 	 */
 	public void aktivieren(int Spieler, int bomben_radius) {
 		liegt = true;
 
 		timer.set_bomb_x(x);
 		timer.set_bomb_y(y);
 		Menue.get_game().add(timer); // Timer hinzufuegen
 		timer.timer_starten(3000, "Bombe", Spieler, bomben_radius); // Timer
 																	// fuer
 																	// Bombe
 																	// starten
 	}
 
 	// bombe_detonieren-Methode:
 	/**
 	 * Ueberpruefung der Legalitaet der Detonationsausbreitung; Umwandlung der
 	 * Felder im Map-Array; Ueberpruefung ob Spieler von Explosion betroffen ist
 	 */
 	public void bombe_detonieren(int Spieler, int bomben_radius) {
 		Menue.sound.playExplosion();
 //		if (Spieler == 0) {
 //			Menue.get_bot(1).set_max_bomben(Menue.get_bot(1).get_max_bomben() + 1);
 //		}
 		
 		Menue.get_hulk(Spieler).set_max_bomben(
 				Menue.get_hulk(Spieler).get_max_bomben() + 1); // Spieler darf
 																// wieder eine
 																// Bombe mehr
 																// legen
 		
 
 		/* Ausbreitung der Detonation nach rechts und unten: */
 		for (int x = 0, y = 0; x <= bomben_radius; x++, y++) {
 			// horizontal:
 			if (this.x + x > 0 && this.x + x < n) { // falls die Detonation
 													// nicht ueber den
 													// Spielfeldrand hinaus geht
 				if (Map.get_map()[this.x + x][this.y] == 4) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Mauer-Feld
 																// ist...
 					if (treffer_rechts == false) {
 						treffer_rechts = true;
 					}
 				}
 
 				if (Map.get_map()[this.x + x][this.y] == 3) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Block-Feld
 																// ist...
 					if (treffer_rechts == false) {
 						treffer_rechts = true;
 						Map.get_map()[this.x + x][this.y] = 6; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x + x][this.y] == 8) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Ausgangs-Feld
 																	// ist...
 					if (treffer_rechts == false) {
 						treffer_rechts = true;
 						Map.get_map()[this.x + x][this.y] = 13; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Ausgangs-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x + x][this.y] == 0) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Bomben-Item-Feld
 																	// ist...
 					if (treffer_rechts == false) {
 						treffer_rechts = true;
 						Map.get_map()[this.x + x][this.y] = 11; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Bomben-Item-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x + x][this.y] == 9) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Bomben-Item-Feld
 																	// ist...
 					if (treffer_rechts == false) {
 						treffer_rechts = true;
 						Map.get_map()[this.x + x][this.y] = 14; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Bomben-Item-Feld
 																// um
 					}
 
 				}
 				
 				else if (Map.get_map()[this.x + x][this.y] == 15 || Map.get_map()[this.x + x][this.y] == 12){
 					if (treffer_rechts == false) {						// wandele Item in explosion um
 						treffer_rechts = true;
 						Map.get_map()[this.x + x][this.y] = 6;
 					}
 				}	
 
 
 
 
 
 
 				if (Map.get_map()[this.x + x][this.y] == 2) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Weg-Feld...
 					if (treffer_rechts == false) {
 						Map.get_map()[this.x + x][this.y] = 6; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-Feld
 																// um
 					}
 
 				}
 
 			}
 
 			// vertikal:
 			if (this.y + y > 0 && this.y + y < n) { // falls die Detonation
 													// nicht ueber den
 													// Spielfeldrand hinaus geht
 				if (Map.get_map()[this.x][this.y + y] == 4) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Mauer-Feld
 																// ist...
 					if (treffer_unten == false) {
 						treffer_unten = true;
 					}
 				}
 
 				if (Map.get_map()[this.x][this.y + y] == 3) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Block-Feld
 																// ist...
 					if (treffer_unten == false) {
 						treffer_unten = true;
 						Map.get_map()[this.x][this.y + y] = 6; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x][this.y + y] == 8) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Ausgangs-Feld
 																	// ist...
 					if (treffer_unten == false) {
 						treffer_unten = true;
 						Map.get_map()[this.x][this.y + y] = 13; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Ausgangs-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x][this.y + y] == 0) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Bomben-Item-Feld
 																	// ist...
 					if (treffer_unten == false) {
 						treffer_unten = true;
 						Map.get_map()[this.x][this.y + y] = 11; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Bomben-Item-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x][this.y + y] == 9) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Bomben-Item-Feld
 																	// ist...
 					if (treffer_unten == false) {
 						treffer_unten = true;
 						Map.get_map()[this.x][this.y + y] = 14; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Bomben-Item-Feld
 																// um
 					}
 
 				}
 				
 				
 				else if (Map.get_map()[this.x][this.y + y] == 15 || Map.get_map()[this.x][this.y + y] == 12){
 					if (treffer_unten == false) {							
 						treffer_unten = true;
 						Map.get_map()[this.x][this.y + y] = 6;				// wandele Item in explosion um
 					}
 				}
 
 				if (Map.get_map()[this.x][this.y + y] == 2) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Weg-Feld...
 					if (treffer_unten == false) {
 						Map.get_map()[this.x][this.y + y] = 6; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-Feld
 																// um
 					}
 
 				}
 
 			}
 
 			/* Treffen von Detonation & Bombe: */
 			// dieselbe Bombe:
 			if (x == 0 && y == 0) { // falls das Zielfeld der Detonation die
 									// Bombe selbst ist...
 				Map.get_map()[this.x][this.y] = 6; // ...dann wandele das Feld
 													// in ein Explosions-Feld um
 			}
 
 			// andere Bombe:
 			else { // sonst (falls das Zielfeld der Detonation eine andere Bombe
 					// ist)...
 				// horizontales Treffen von Explosion & Bombe:
 				if (this.x + x > 0 && this.x + x < n) { // falls die Detonation
 														// nicht ueber den
 														// Spielfeldrand hinaus
 														// geht
 					if (Map.get_map()[this.x + x][this.y] == 5) { // falls das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Bombe-Feld
 																	// ist...
 						if (treffer_rechts == false) {
 							Menue.get_game().bomb[this.x + x][this.y]
 									.bombe_detonieren(Spieler, bomben_radius); // ...lass
 																				// auch
 																				// die
 																				// andere
 																				// Bombe
 																				// detonieren
 							Menue.get_game().bomb[this.x + x][this.y].detoniert = true;
 						}
 						
 					}
 
 				}
 
 				if (this.y + y > 0 && this.y + y < n) { // falls die Detonation
 														// nicht ueber den
 														// Spielfeldrand hinaus
 														// geht
 					// vertikales Treffen von Explosion & Bombe:
 					if (Map.get_map()[this.x][this.y + y] == 5) { // falls das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Bombe-Feld
 																	// ist...
 						if (treffer_unten == false) {
 							Menue.get_game().bomb[this.x][this.y + y]
 									.bombe_detonieren(Spieler, bomben_radius); // ...lass
 																				// auch
 																				// die
 																				// andere
 																				// Bombe
 																				// detonieren
 							Menue.get_game().bomb[this.x][this.y + y].detoniert = true;
 						}
 						
 					}
 
 				}
 
 			}
 
 			/* Treffen von Detonation & Hulk: */
 			// horizontal:
 			if (this.x + x > 0 && this.x + x < n) { // falls die Detonation
 													// nicht ueber den
 													// Spielfeldrand hinaus geht
 				if (Menue.get_hulk(1).get_x() == this.x + x
 						&& Menue.get_hulk(1).get_y() == this.y) { 	// falls die
 																	// Detonation
 							 										// die erste...	
 					if (treffer_rechts == false && hulk_getroffen == false) {
 						hulk_getroffen = true;
 						Menue.sound.playTod();
 						System.out.println("Game over"); 	// Test
 						System.out.println(); 				// Test
 						
 						Menue.abfrage_neustarten();
 					}
 					
 				}		
 				
 				if (Menue.getMultiplayer()) {							// ...oder
 					if (Menue.get_hulk(2).get_x() == this.x + x			// zweite
 							&& Menue.get_hulk(2).get_y() == this.y) {	// Spielfigur
 																		// trifft
 						hulk_getroffen = true;
 						Menue.sound.playTod();
 						System.out.println("Game over"); 	// Test
 						System.out.println(); 				// Test
 						
 						Menue.abfrage_neustarten();
 					}
 					
 				}
 				
 //				if (Menue.get_bot(1).get_x() == this.x + x && Menue.get_bot(1).get_y() == this.y + y){
 //					Menue.botStop();
 //					Menue.sound.playTod();
 //					System.out.println("Gewonnen!"); // Test
 //					System.out.println(); // Test
 //				}
 
 			}
 
 			// vertikal:
 			if (this.y + y > 0 && this.y + y < n) { // falls die Detonation
 													// nicht ueber den
 													// Spielfeldrand hinaus geht
 				if (Menue.get_hulk(1).get_x() == this.x
 						&& Menue.get_hulk(1).get_y() == this.y + y) { 	// falls die
 																		// Detonation
 																		// die
 																		// erste...
 
 					if (treffer_unten == false && hulk_getroffen == false) {
 						hulk_getroffen = true;
 						Menue.sound.playTod();
 						System.out.println("Game over"); // Test
 						System.out.println(); // Test
 						
 						Menue.abfrage_neustarten();
 					}
 
 				}
 				
 				if (Menue.getMultiplayer()) {								// ...oder
 					if (Menue.get_hulk(2).get_x() == this.x					// zweite
 							&& Menue.get_hulk(2).get_y() == this.y + y) {	// Spielfigur
 																			// trifft
 						hulk_getroffen = true;
 						Menue.sound.playTod();
 						System.out.println("Game over"); 	// Test
 						System.out.println(); 				// Test
 						
 						Menue.abfrage_neustarten();
 					}
 					
 				}
 
 			}
 
 		}
 
 		/* Ausbreitung der Detonation nach links und oben: */
 		for (int x = 0, y = 0; x >= -bomben_radius; x--, y--) {
 			// horizontal:
 			if (this.x + x > 0 && this.x + x < n) { // falls die Detonation
 													// nicht ueber den
 													// Spielfeldrand hinaus geht
 				if (Map.get_map()[this.x + x][this.y] == 4) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Mauer-Feld
 																// ist...
 					if (treffer_links == false) {
 						treffer_links = true;
 					}
 
 				}
 
 				if (Map.get_map()[this.x + x][this.y] == 3) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Block-Feld
 																// ist...
 					if (treffer_links == false) {
 						treffer_links = true;
 						Map.get_map()[this.x + x][this.y] = 6; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x + x][this.y] == 8) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Ausgangs-Feld
 																	// ist...
 					if (treffer_links == false) {
 						treffer_links = true;
 						Map.get_map()[this.x + x][this.y] = 13; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Ausgangs-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x + x][this.y] == 0) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Bomben-Item-Feld
 																	// ist...
 					if (treffer_links == false) {
 						treffer_links = true;
 						Map.get_map()[this.x + x][this.y] = 11; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Bomben-Item-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x + x][this.y] == 9) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Bomben-Item-Feld
 																	// ist...
 					if (treffer_links == false) {
 						treffer_links = true;
 						Map.get_map()[this.x + x][this.y] = 14; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Bomben-Item-Feld
 																// um
 					}
 
 				}
				
				else if (Map.get_map()[this.x + x][this.y] == 15 || Map.get_map()[this.x + x][this.y] == 12){
					if (treffer_links == false) {						// wandele Item in explosion um
						treffer_links = true;
						Map.get_map()[this.x + x][this.y] = 6;
						
					}
				}
 
 				if (Map.get_map()[this.x + x][this.y] == 2) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Weg-Feld...
 					if (treffer_links == false) {
 						Map.get_map()[this.x + x][this.y] = 6; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-Feld
 																// um
 					}
 
 				}
 
 			}
 
 			// vertikal:
 			if (this.y + y > 0 && this.y + y < n) { // falls die Detonation
 													// nicht ueber den
 													// Spielfeldrand hinaus geht
 				if (Map.get_map()[this.x][this.y + y] == 4) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Mauer-Feld
 																// ist...
 					if (treffer_oben == false) {
 						treffer_oben = true;
 					}
 
 				}
 
 				if (Map.get_map()[this.x][this.y + y] == 3) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Block-Feld
 																// ist...
 					if (treffer_oben == false) {
 						treffer_oben = true;
 						Map.get_map()[this.x][this.y + y] = 6; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x][this.y + y] == 8) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Ausgangs-Feld
 																	// ist...
 					if (treffer_oben == false) {
 						treffer_oben = true;
 						Map.get_map()[this.x][this.y + y] = 13; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Ausgangs-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x][this.y + y] == 0) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Bomben-Item-Feld
 																	// ist...
 					if (treffer_oben == false) {
 						treffer_oben = true;
 						Map.get_map()[this.x][this.y + y] = 11; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Bomben-Item-Feld
 																// um
 					}
 
 				}
 
 				else if (Map.get_map()[this.x][this.y + y] == 9) { // oder falls
 																	// das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Block-/Bomben-Item-Feld
 																	// ist...
 					if (treffer_oben == false) {
 						treffer_oben = true;
 						Map.get_map()[this.x][this.y + y] = 14; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-/Bomben-Item-Feld
 																// um
 					}
 
 				}
				else if (Map.get_map()[this.x][this.y + y] == 15 || Map.get_map()[this.x][this.y + y] == 12){
					if (treffer_oben == false) {							
						treffer_oben = true;
						Map.get_map()[this.x][this.y + y] = 6;	
					}		
				}	
 
 				if (Map.get_map()[this.x][this.y + y] == 2) { // falls das
 																// Zielfeld der
 																// Detonation
 																// ein
 																// Weg-Feld...
 					if (treffer_oben == false) {
 						Map.get_map()[this.x][this.y + y] = 6; // ...dann
 																// wandele das
 																// Feld in ein
 																// Explosions-Feld
 																// um
 					}
 
 				}
 
 			}
 
 			/* Treffen von Detonation & Bombe: */
 			// dieselbe Bombe:
 			if (x == 0 && y == 0) { // falls das Zielfeld der Detonation die
 									// Bombe selbst ist...
 				Map.get_map()[this.x][this.y] = 6; // ...dann wandele das Feld
 													// in ein Explosions-Feld um
 			}
 
 			// andere Bombe:
 			else { // sonst (falls das Zielfeld der Detonation eine andere Bombe
 					// ist)...
 				// horizontales Treffen von Explosion & Bombe:
 				if (this.x + x > 0 && this.x + x < n) { // falls die Detonation
 														// nicht ueber den
 														// Spielfeldrand hinaus
 														// geht
 					if (Map.get_map()[this.x + x][this.y] == 5) { // falls das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Bombe-Feld
 																	// ist...
 						if (treffer_links == false) {
 							Menue.get_game().bomb[this.x + x][this.y]
 									.bombe_detonieren(Spieler, bomben_radius); // ...lass
 																				// auch
 																				// die
 																				// andere
 																				// Bombe
 																				// detonieren
 							Menue.get_game().bomb[this.x + x][this.y].detoniert = true;
 						}
 						
 					}
 					
 				}
				
 
				
 				// vertikales Treffen von Explosion & Bombe:
 				if (this.y + y > 0 && this.y + y < n) { // falls die Detonation
 														// nicht ueber den
 														// Spielfeldrand hinaus
 														// geht
 					if (Map.get_map()[this.x][this.y + y] == 5) { // falls das
 																	// Zielfeld
 																	// der
 																	// Detonation
 																	// ein
 																	// Bombe-Feld
 																	// ist
 						if (treffer_oben == false) {
 							Menue.get_game().bomb[this.x][this.y + y]
 									.bombe_detonieren(Spieler, bomben_radius); // ...lass
 																				// auch
 																				// die
 																				// andere
 																				// Bombe
 																				// detonieren
 							Menue.get_game().bomb[this.x][this.y + y].detoniert = true;
 						}
 						
 					}
 					
 				}
 				
 			}
 
 			/* Treffen von Detonation & Hulk: */
 			// horizontal:
 			if (this.x + x > 0 && this.x + x < n) { // falls die Detonation
 													// nicht ueber den
 													// Spielfeldrand hinaus geht
 				if (Menue.get_hulk(1).get_x() == this.x + x
 						&& Menue.get_hulk(1).get_y() == this.y) { 	// falls die
 																	// Detonation
 																	// die erste...
 
 					if (treffer_links == false && hulk_getroffen == false) {
 						hulk_getroffen = true;
 						System.out.println("Game over"); 	// Test
 						System.out.println(); 				// Test
 						Menue.sound.playTod();
 						Menue.abfrage_neustarten();
 					}
 
 				}
 				
 				if (Menue.getMultiplayer()) {							// ...oder
 					if (Menue.get_hulk(2).get_x() == this.x + x			// zweite
 							&& Menue.get_hulk(2).get_y() == this.y) {	// Spielfigur
 																		// trifft
 						hulk_getroffen = true;
 						Menue.sound.playTod();
 						System.out.println("Game over"); 	// Test
 						System.out.println(); 				// Test
 						
 						Menue.abfrage_neustarten();
 					}
 					
 				}
 
 			}
 
 			// vertikal:
 			if (this.y + y > 0 && this.y + y < n) { // falls die Detonation
 													// nicht ueber den
 													// Spielfeldrand hinaus geht
 				if (Menue.get_hulk(1).get_x() == this.x
 						&& Menue.get_hulk(1).get_y() == this.y + y) { 	// falls die
 																		// Detonation
 																		// die
 																		// erste...
 
 					if (treffer_oben == false && hulk_getroffen == false) {
 						hulk_getroffen = true;
 						System.out.println("Game over"); // Test
 						System.out.println(); // Test
 						Menue.sound.playTod();
 						Menue.abfrage_neustarten();						
 					}
 
 				}
 				
 				if (Menue.getMultiplayer()) {								// ...oder
 					if (Menue.get_hulk(2).get_x() == this.x					// zweite
 							&& Menue.get_hulk(2).get_y() == this.y + y) {	// Spielfigur
 																			// trifft
 						hulk_getroffen = true;
 						Menue.sound.playTod();
 						System.out.println("Game over"); 	// Test
 						System.out.println(); 				// Test
 						
 						Menue.abfrage_neustarten();
 					}
 					
 				}
 
 			}
 
 		}
 
 		// Treffer zuruecksetzen:
 		treffer_oben 	= false;
 		treffer_rechts 	= false;
 		treffer_unten 	= false;
 		treffer_links	= false;
 
 		Zeit ende_explosion = new Zeit();
 		ende_explosion.set_bomb_x(x);
 		ende_explosion.set_bomb_y(y); 	// Timer fuer Dauer der Explosion
 										// erstellen
 		Menue.get_game().add(ende_explosion); 	// Timer fuer Dauer der Explosion
 												// hinzufuegen
 
 		if (liegt == true) {	// falls das Spiel nicht waehrend des Timers
 								// neugestartet wurde
 			ende_explosion.timer_starten(1000, "Detonation", Spieler,
 					bomben_radius); // Timer fuer Dauer der Explosion starten
 		}
 
 		else { // sonst
 			Map.set_map(MapLoader.laden(MapLoader.get_level())); 	// Spielfeld
 																	// neu
 																	// einlesen
 		}
 
 		// Spielfeld grafisch reinitialisieren:
 		Menue.get_game().removeAll();
 		Menue.get_game().refresh();
 	}
 	
 	
 	/* setter & getter: */
 	
 	// get_x-Methode:
 	/**
 	 * @return Bombenposition x-Koord.
 	 */
 	public int get_x() {
 		return x;
 	}
 
 	// set_x-Methode:
 	/**
 	 * @param x
 	 *            Bombenposition x-Koord. setzen
 	 */
 	public void set_x(int x) {
 		this.x = x;
 	}
 
 	// get_y-Methode:
 	/**
 	 * @return Bombenposition y-Koord.
 	 */
 	public int get_y() {
 		return y;
 	}
 
 	// set_y-Methode:
 	/**
 	 * @param y
 	 *            Bombenposition y-Koord. setzen
 	 */
 	public void set_y(int y) {
 		this.y = y;
 	}
 
 }
