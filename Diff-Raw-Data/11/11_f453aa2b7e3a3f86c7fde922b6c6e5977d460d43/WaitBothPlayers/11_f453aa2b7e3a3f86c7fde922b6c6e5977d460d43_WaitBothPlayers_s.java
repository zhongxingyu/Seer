 package Client;
 
 import Game.Game;
 import Game.Interface;
 import Game.RepaintThread;
 /**
  * <u>WaitBothPlayers</u><br>
  * Startet die Spielmechanismen erst wenn besttigt ist das alle Daten die bentigt werden vorhanden sind.
  * 
  * @author Jan Reckfort
  * @author Bastian Siefen
  */
 public class WaitBothPlayers extends Thread {
 
 	public void run() {
 		boolean wait = true;
		System.out.println("WaitBothPlayers: Gestartet.");
 		while (wait == true) {
 			if (ClientGameReader.gameOn == true) {
 				wait = false;
 				Game.main(null);
 				Interface.closeMenuOpenGame();
 
				System.out.println("WaitBothPlayers: Beendet.");

 				new RepaintThread().start();
 
 			}
 			try {
 				sleep(100);
 			} catch (InterruptedException e) {
 
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
