 /**
  * Klasse fuer Extrasfelder
  * @author philip
  *
  */
 public class Extrasfeld extends Feld
 {
 	/**
 	 * Gibt an, welche Art von Extrafeld betreten wurde.
 	 */
 	public int art;
 	/**
 	 * Wird veraendert, wenn Extra freigelegt wurde. Startzustand: Wahr
 	 */
 	private boolean covered=true;
 	/**
 	 * Konstruktor zur Klasse Extrasfeld
 	 * @param art: Art des Extras
 	 */
 	public Extrasfeld(int art){
 		this.art = art;
 	}
 	
 	/**
 	 * Zeichnet die Grafik des Extras
 	 */
 	public void draw(int x, int y){
 		if (covered==true){
 			Renderer.Tile_Break.draw(x*size, y*size);
 			Renderer.print(x*size, y*size, ""+art, 0.5f);
 		} else {
 			Renderer.Tile_Empty.draw(x*size, y*size); 
 			switch(art)
 			{
 				case 1: Renderer.Tile_Health.draw(x*size, y*size); break; //Dieses Feld generiert ein zusaetzliches Leben, um einen Bombentreffer zu ueberleben}
 				case 2: Renderer.Tile_addbomb.draw(x*size, y*size); break; //Dieses Feld generiert ein Upgrade, um die Anzahl der Bomben, die man gleichzeitig legen kann, um 1 zu erhoehen
 				case 3: Renderer.Tile_kick.draw(x*size, y*size); break; //Dieses Feld generiert ein Upgrade, wodurch der Spieler dazu befaehigt wird, die Bomben linear zu treten
				case 4: break;//Dieses Feld generiert die einmalige Befähigung, sich für 3 Sekunden unsichtbar zu machen.
				case 5: Renderer.Tile_slow.draw(x*size, y*size); break;//Dieses Feld generiert eine temporaere Steuerungsbehinderung fuer alle feindlichen/anderen Spieler. Alle ausser der Spieler, der das Feld betritt, unterliegen einer umgekehrten Steuerung (Ausnahme: Bombe legen)
 				case 6: Renderer.Tile_teleport.draw(x*size, y*size); break;//Dieses Feld generiert ein Teleportationsfeld. Der Spieler der auf dieses Feld tritt wird mit sofortiger Wirkung zum entsprechenden Feld teleportiert
 				case 7: Renderer.Tile_speed.draw(x*size, y*size); break;//Dieses Feld generiert ein Upgrade, welches einen temporaeren Geschwindigkeitsbonus fuer den Spieler gibt, der es aufsammelt
				case 8: Renderer.Tile_confuse.draw(x*size, y*size); break;//Dieses Feld generiert eine temporäre Steuerungsbehinderung für alle feindlichen/anderen Spieler, in Form einer legbaren Falle.
				case 9: //Dieses Feld generiert einen auslösbaren Schild.
				case 10: //Dieses Feld generiert ein Upgrade zur Verbesserung der Bombenreichweite.				case 12: //Dieses Feld generiert einen FROST-SCHOCK. Der Spieler der es aufsammelt, darf sich temporaer nicht bewegen. Wenn der Frost-Schock vorbei ist, erhaelt der Spieler seine komplette Bewegungsfreiheit.
 			}
 		}
 	}
 		/**
 		 * Setzt covered auf false, wenn freigelegt wurde
 		 */
 		public void setUncovered(){
 			covered=false;
 		}
 		/**
 		 * Abfrage, ob Extra versteckt ist
 		 * @return: Status von covered. True, wenn versteckt; false, wenn sichtbar
 		 */
 		public boolean isCovered(){
 			return covered;
 		}
 		/*
 		 * Variable fuer das jeweilige Extra; Aufruf einer getter
 		 * Renderer.Tile_   .draw(x * 128 * 0.33f, y * 128 * 0.33f);
 		 */
 	}
 
