 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JLabel;
 
 /**
  * 
  * @author Andrej Morlang
  * 
  */
 public class Zeit extends JLabel {
 	private static final long serialVersionUID = 1L;
 	Timer timer = new Timer(); // Timer erstellen
 	int bomb_x, bomb_y;
 	public int spielzeit;	   //Zeit pro durchlauf
 	int n = MapLoader.get_n();
 	private static int restZeit = 0;
 
 	/* METHODEN: */
 
 	// timer_starten-Methode
 	/**
 	 * Initialisiert den Bombentimer
 	 * 
 	 * @param x
 	 *            Zeit bis zur Detonation der Bombe
 	 * @param logo
 	 *            String der Bomben-Aktion ("Bombe" fuer Legen der Bombe, "Detonation")
 	 */
 	public void timer_starten(int x, String logo, int spieler, int bomben_radius) { 	// x = Timer-Verzoegerung, logo = Bomben-/Detonations-Icon
 		if (Menue.get_game().bomb[bomb_x][bomb_y].liegt == true) {
 			if (logo.equals("Bombe")) {
 				timer.schedule(new Bombe(spieler, bomben_radius), x); 	// Zeit bis zur Detonation um x
 				// Millisek. verzoegern
 			}
 
 			else if (logo.equals("Detonation")) {
 				timer.schedule(new Detonation(bomben_radius), x); 	// Zeit bis zum Ende der
 				// Detonation um x Millisek.
 				// verzoegern
 			}
 
 		}
 
 	}
 
 	/* INTERNE KLASSEN: */
 
 	/**
 	 * @author Andrej Morlang
 	 */
 	public class Bombe extends TimerTask {
 		private int Spieler;
 		private int bomben_radius;
 
 		/* Konstruktor: */
 		public Bombe(int spieler, int bomben_radius) {
 			Spieler = spieler;
 			this.bomben_radius = bomben_radius;
 		}
 
 		/* METHODEN: */
 
 		// run-Methode:
 		/**
		 * startet den TimerTask-Thread Bombe (Ueberprueft gleichzeitig, ob das Spiel waehrend des Countdown neugestartet wurde
 		 */
 		public void run() {
			if (Menue.get_game().bomb[bomb_x][bomb_y].liegt == true) { // falls das Spiel nicht waehrend des Timers neugestartet wurde
 
 				Menue.get_game().bomb[bomb_x][bomb_y].bombe_detonieren(Spieler,
 						bomben_radius); // Detonation der Bombe				

				timer.cancel(); // Timer terminieren
 			}
 
 		}
 
 	}
 
 	/**
 	 * Timer zum aendern der Schwierigkeit
 	 * 
 	 * @author Andrej Morlang
 	 */
 	public void laufzeit(final int spielzeit) {
 		timer.schedule(new Schwierigkeit(), spielzeit * 1000);  // 1000 steht hier fuer (Zeitlich) 1 sekunde
 
 	}
 
 	public class Schwierigkeit extends TimerTask { 					// beendet das Speil
 
 		public void run() {											// nach ablauf der Zeit.. 
 			Menue.abfrage_neustarten(); 							// .. erscheint das Fenster zum Neustart
 			timer.cancel();
 		}
 	}
 
 	/**
 	 * @author Andrej Morlang
 	 */
 	public class Detonation extends TimerTask {
 		private int radius;
 
 		/* Konstruktor: */
 		public Detonation(int radius) {
 			this.radius = radius;
 		}
 
 		/* METHODEN: */
 
 		// run-Methode:
 		/**
 		 * startet den TimerTask-Thread Detonation nach Ablauf der Zeit x (timer_starten-Methode), welcher die Detonationsschritte vollzieht
 		 */
		public void run() {
			timer.cancel(); // Timer terminieren	
 
 			// Verschwinden der Explosion:
 			for (int x = -radius, y = -radius; x <= radius; x++, y++) {
 				// horizontal:
 				if (bomb_x + x > 0 && bomb_x + x < n) {	// falls die Detonation nicht ueber den Spielfeldrand hinaus geht
 					if (Map.get_map()[bomb_x + x][bomb_y] == 6) {		// falls das Zielfeld der Detonation ein Explosions-Feld ist...
 						Map.get_map()[bomb_x + x][bomb_y] = 2; 			// ...dann wandele das Feld in ein Weg-Feld um
 					}
 
 					else if (Map.get_map()[bomb_x + x][bomb_y] == 13) { 	// oder falls das Zielfeld der Detonation ein Explosions-/Ausgangs-Feld ist...
 						Map.get_map()[bomb_x + x][bomb_y] = 7;			// ...dann wandele das Feld in ein Ausgangs-Feld um
 					}
 
 					else if (Map.get_map()[bomb_x + x][bomb_y] == 11) {	// oder falls das Zielfeld der Detonation ein Explosions-/Bomben-Item-Feld ist...
 						Map.get_map()[bomb_x + x][bomb_y] = 12;			// ...dann wandele das Feld in ein Bomben-Item-Feld um
 					}
 
 					else if (Map.get_map()[bomb_x + x][bomb_y] == 14) {	// oder falls das Zielfeld der Detonation ein Explosions-/Bomben-Item-Feld ist...
 						Map.get_map()[bomb_x + x][bomb_y] = 15;			// ...dann wandele das Feld in ein Bomben-Item-Feld um
 					}
 
 				}
 
 				// vertikal:
 				if (bomb_y + y > 0 && bomb_y + y < n) {	// falls die Detonation nicht ueber den Spielfeldrand hinaus geht
 					if (Map.get_map()[bomb_x][bomb_y + y] == 6) {		// falls das Zielfeld der Detonation ein Explosions-Feld ist...
 						Map.get_map()[bomb_x][bomb_y + y] = 2;			// ...dann wandele das Feld in ein Weg-Feld um
 					}
 
 					else if (Map.get_map()[bomb_x][bomb_y + y] == 13) {	// oder falls das Zielfeld der Detonation ein Explosions-/Ausgangs-Feld ist...
 						Map.get_map()[bomb_x][bomb_y + y] = 7;			// ...dann wandele das Feld in ein Ausgangs-Feld um
 					}
 
 					else if (Map.get_map()[bomb_x][bomb_y + y] == 11) {	// oder falls das Zielfeld der Detonation ein Explosions-/Bomben-Item-Feld ist...
 						Map.get_map()[bomb_x][bomb_y + y] = 12;			// ...dann wandele das Feld in ein Bomben-Item-Feld um
 					}
 
 					else if (Map.get_map()[bomb_x][bomb_y + y] == 14) {	// oder falls das Zielfeld der Detonation ein Explosions-/Bomben-Item-Feld ist...
 						Map.get_map()[bomb_x][bomb_y + y] = 15;			// ...dann wandele das Feld in ein Bomben-Item-Feld um
 					}
 
 				}
 
 			}
 
 			Menue.get_game().removeAll();
 			Menue.get_game().refresh();
 
 			Menue.get_game().bomb[bomb_x][bomb_y].liegt = false;
 
 			timer.cancel(); // Timer terminieren
 		}
 
 	}
 
 	/* METHODEN: */
 
 	/* setter & getter: */
 
 	// set_bomb_x-Methode:
 	/**
 	 * @param bomb_x
 	 *            setzt x-Koordinate der Bombe
 	 */
 	public void set_bomb_x(int bomb_x) {
 		this.bomb_x = bomb_x;
 	}
 
 	// set_bomb_y-Methode:
 	/**
 	 * @param bomb_y
 	 *            setzt y-Koordinate der Bombe
 	 */
 	public void set_bomb_y(int bomb_y) {
 		this.bomb_y = bomb_y;
 	}
 
 	public static void set_restZeit(int zeit) {
 		restZeit = zeit;
 	}
 
 	public static int get_restZeit() {
 		return restZeit;
 	}
 
 }
