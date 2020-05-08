 package domain;
 import java.util.ArrayList;
 
 import valueobjects.Territory;
 
 
 /**
  * 
  * @author Timur
  * 
  */
 public class BattleSystem {
 
 	ArrayList<Integer> ang = new ArrayList<Integer>();
 	ArrayList<Integer> def = new ArrayList<Integer>();
 
 	// Anzahl angreifender Soldaten
 	int angAnzahl;
 	int defAnzahl;
 
 	// die angreifenden Wuerfel mit ihren wertigkeiten absteigen sortiert
 	int angEins = 0;
 	int angZwei = 0;
 	// die verteidigenden Wuerfel mit ihren wertigkeiten absteigen sortiert
 	int defEins = 0;
 	int defZwei = 0;
 
 	// Angriffzuege
 	int angriffZug;
 	int maxAngriffZug;
 
 	// Ziell und Quellland
 	Territory zielLand;
 	Territory quellLand;
 
 	// Angriffe wieviele Angriffe werden in einem Angriff getßtigt
 	int angriffe = 0;
 
 	// Konstruktur kriegt die Anzahl der Anfreifenden bzw. Verteidigenden Einheiten
 	// Kontruktur bekommt außerdem das Ziel und Quellland als Objekte
 	
 	public BattleSystem(int angAnzahl, int defAnzahl, Territory zielLand, Territory quellLand) {
 		this.angAnzahl = angAnzahl;
 		this.defAnzahl = defAnzahl;
 		this.zielLand = zielLand;
 		this.quellLand = quellLand;
 		fight();
 	}
 
 	/**
 	 * Zufßllige berechnung der Wßrfel wertigkeiten
 	 * 
 	 * @param anzahl Anzahl der zu wuerfelnden Wuerfel
 	 *
 	 * @return ArrayList als Integer der gewuerfelten zahlen
 	 */
	// TODO static methods are not allowed
 	public static ArrayList getDice(int anzahl) {
 		// varriert
 		ArrayList<Integer> dice = new ArrayList<Integer>();
 		for (int i = 0; i <= anzahl - 1; i++) {
 			dice.add(i, (int) ((Math.random()) * 6 + 1));
 		}
 		return dice;
 	}
 
 	/**
 	 * Berechnet die grueßte Zahl in einer Liste
 	 * 
 	 * @param wuerfel
 	 *            Eine Liste aus gewuerfelten Zahlen
 	 * @return Groeßte Zahl aus den Wuerfelergebnissen eines Spielers
 	 */
 	public int getGroessteZahl(ArrayList<Integer> wuerfel) {
 		int maximum = wuerfel.get(0);
 		boolean in = false;
 		int i = 1;
 		while (i < wuerfel.size()) {
 			if (wuerfel.get(i) > maximum) {
 				maximum = wuerfel.get(i);
 				wuerfel.remove(i);
 				in = true;
 			}
 			i++;
 		}
 		if (in == false) {
 			wuerfel.remove(0);
 		}
 		return maximum;
 	}
 
 	public void fight() {
 		/*
 		 * Benutzereingabe muss ueberprueft werden mit Einheiten Anzahl (angGesamt
 		 * und defGesamt) auf dem Angriff bzw Zielland angAnzahl darf nur 2 oder
 		 * 3 sein wenn eine Einheit auf dem Quellland bleibt bei Defense land
 		 * muss min. eine Einheite vorhanden sein heisst die Zahl darf nur 1
 		 * oder 2 sein.
 		 */
 
 		// hier werden die wuerfel Anzahl festgelegt je nach anzahl der Einheiten
 		// auf dem Land
 		ang = getDice(angAnzahl);
 		def = getDice(defAnzahl);
 
 		/*
 		 * generiert die groeßten Zahlen im Integer fuer den Angriff es gilt: fuer
 		 * den Angriff: angAnzahl = 2 dann nur eine Zahl zum kaempfen angAnzahl =
 		 * 3 dann zwei Zahlen zum kaempfen
 		 * 
 		 * fuer die Defensive: defAnzahl = 1 dann eine Zahl zum verteidigen
 		 * defAnzahl = 2 dann zwei Zahlen zum verteidigen
 		 */
 
 		// Kampfgeschehen ausrechnen der Verluste
 		if (angAnzahl == 1) {
 			angEins = getGroessteZahl(ang);
 			angriffZug++;
 			System.out.println("------ RUNDE (" + angriffZug + ") -------- \r\n");
 			
 			// 1 gegen 1
 			if (defAnzahl == 1) {
 				defEins = getGroessteZahl(def);
 
 				if (angEins > defEins) {
 					eventMsgOffensive();
 				} else {
 					// Quellland verliert Einheit
 					eventMsgDefensive();
 				}
 			}
 
 			// 1 gegen 2
 			if (defAnzahl == 2) {
 				defEins = getGroessteZahl(def);
 				defZwei = getGroessteZahl(def);
 
 				if (angEins > defEins) {
 					// Zielland verliert Einheit
 					eventMsgOffensive();
 				} else {
 					// Quellland verliert Einheit
 					eventMsgDefensive();
 				}
 			}
 		}
 
 		if (angAnzahl == 2) { // mit Abfrage wegen einem Soldat auf dem
 			// Quellland
 			angEins = getGroessteZahl(ang);
 			angZwei = getGroessteZahl(ang);
 			angriffZug++;
 			
 			System.out.println("------ RUNDE (" + angriffZug + ") -------- \r\n");
 			// 2 gegen 1
 			if (defAnzahl == 1) {
 				defEins = getGroessteZahl(def);
 
 				if (angEins > defEins) {
 					// Zielland verliert Einheit
 					eventMsgOffensive();
 				} else {
 					// Quellland verliert Einheit
 					eventMsgDefensive();
 				}
 			}
 			// 2 gegen 2
 			if (defAnzahl == 2) {
 				defEins = getGroessteZahl(def);
 				defZwei = getGroessteZahl(def);
 
 				// ersten Wuerfel treten gegeneinander an
 				if (angEins > defEins) {
 					// Zielland verliert Einheit
 					eventMsgOffensive();
 				} else {
 					// Quellland verliert Einheit
 					eventMsgDefensive();
 				}
 
 				// zweiten Wuerfel treten gegen einander an
 				if (angZwei > defZwei) {
 					// Zielland verliert Einheit
 					eventMsgOffensive();
 				} else {
 					// Quellland verliert Einheit
 					eventMsgDefensive();
 				}
 			}
 		} else {
 			// System.out.println("Sei muessen min. 1 Soldaten auf dem Angreifenden Land lassen. Geben sie eine neue Anzahl an!");
 			// TODO : neu eingabe der anzahl der Soldaten FUNKTION
 		}
 
 		// Angriff 3
 		if (angAnzahl == 3) {
 			angEins = getGroessteZahl(ang);
 			angZwei = getGroessteZahl(ang);
 			angriffZug++;
 			
 			System.out.println("------ RUNDE (" + angriffZug + ") -------- \r\n");
 
 			// 3 gegen 1
 			if (defAnzahl == 1) {
 				defEins = getGroessteZahl(def);
 				if (angEins > defEins) {
 					// Zielland verliert Einheit
 					eventMsgOffensive();
 				} else {
 					// Quellland verliert Einheit
 					eventMsgDefensive();
 				}
 			}
 
 			// 3 gegen 2
 			if (defAnzahl == 2) {
 				defEins = getGroessteZahl(def);
 				defZwei = getGroessteZahl(def);
 				// ersten Wuerfel treten gegeneinander an
 				if (angEins > defEins) {
 					// Zielland verliert Einheit
 					eventMsgOffensive();
 				} else {
 					// Quellland verliert Einheit
 					eventMsgDefensive();
 				}
 
 				// zweiten Wuerfel treten gegen einander an
 				if (angZwei > defZwei) {
 					// Zielland verliert Einheit
 					eventMsgOffensive();
 				} else {
 					// Quellland verliert Einheit
 					eventMsgDefensive();
 				}
 			}
 		}
 	}
 
 	// Kampfgeschehen wird ausgegeben und die Einheiten werden auf den
 	// jeweiligen Laendern reduziert um 1
 	public void eventMsgOffensive() {
 		angriffe++;
 		// Runter setzten der Einheiten
 		zielLand.setAnzahlEinheiten(zielLand.getAnzahlEinheiten() - 1);
 
 		if (angriffe == 1) {
 			System.out.println("Angriff1: " + angEins + " schlaegt Defensive1: "
 											+ defEins);
 
 			System.out.println(zielLand.getName() + "("
 					+ zielLand.getBesitzer().getName()
 					+ ") verliert 1 Einheit und " + zielLand.getName() + " ("
 					+ zielLand.getBesitzer().getName() + ") hat noch: "
 					+ zielLand.getAnzahlEinheiten() + " Einheiten uebrig");
 		} else {
 			System.out.println("Angriff2: " + angZwei + " schlaegt Defensive2: "
 					+ defZwei);
 
 			System.out.println(zielLand.getName() + "("
 					+ zielLand.getBesitzer().getName()
 					+ ") verliert 1 Einheit und " + zielLand.getName() + " ("
 					+ zielLand.getBesitzer().getName() + ") hat noch: "
 					+ zielLand.getAnzahlEinheiten() + " Einheiten uebrig");
 		}
 
 	}
 
 	public void eventMsgDefensive() {
 		// Anzahl der Angriffe die getaetigt werden
 		angriffe++;
 		// Runter setzten der Einheiten
 		quellLand.setAnzahlEinheiten(quellLand.getAnzahlEinheiten() - 1);
 
 		if (angriffe == 1) {
 			System.out.println("Defensive1: " + defEins + " schlaegt Offennsive1: "
 					+ angEins);
 
 			System.out.println(quellLand.getName() + "("
 					+ quellLand.getBesitzer().getName()
 					+ ") verliert 1 Einheit und " + quellLand.getName() + " ("
 					+ quellLand.getBesitzer().getName() + ") hat noch: "
 					+ quellLand.getAnzahlEinheiten() + " Einheiten uebrig");
 		} else {
 			System.out.println("Defensive2: " + defZwei + " schlaegt Offensive2: "
 					+ angZwei);
 
 			System.out.println(quellLand.getName() + "("
 
 					+ quellLand.getBesitzer().getName()
 					+ ") verliert 1 Einheit und " + quellLand.getName() + " ("
 					+ quellLand.getBesitzer().getName() + ") hat noch: "
 					+ quellLand.getAnzahlEinheiten() + " Einheiten uebrig");
 		}
 	}
 }
