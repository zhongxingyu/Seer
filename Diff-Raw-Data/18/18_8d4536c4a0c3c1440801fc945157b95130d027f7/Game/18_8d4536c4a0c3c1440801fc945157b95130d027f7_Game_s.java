 package domain;
 
 // Import phase constants
import static domain.Constants.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import ui.UserInterface;
 import ui.cli.CommandLineInterface;
 import valueobjects.Land;
 import valueobjects.Spieler;
 
 public class Game {
 
 	private Spielerverwaltung spielerverwaltung;
 	private Laenderverwaltung laenderverwaltung;
 	private Spieler activePlayer;
 	private UserInterface userInterface;
 	private ArrayList<Integer> bonusAmountSteps;
 	private Iterator<Integer> bonusAmountIter;
 
 	public Game() {
 		// Goldener Reiter
 		bonusAmountSteps = new ArrayList<Integer>(Arrays.asList(4, 6, 8, 10,
 				15, 20, 25, 30, 35, 40, 45, 50, 55, 60));
 		bonusAmountIter = bonusAmountSteps.iterator();
 
 		// Laenderverwaltung erstellen
 		laenderverwaltung = new Laenderverwaltung();
 
 		// Spielerverwaltung erstellen (Spielerzahl, namen, farben)
 		spielerverwaltung = new Spielerverwaltung();
 
 		CommandLineInterface userInterface = new CommandLineInterface();
 
 		// Anfangsrunde
 		placeStartUnits();
 	}
 
 	private void placeStartUnits() {
 		// TODO Auto-generated method stub
 		// Entweder zufällig platzieren oder Spieler entscheiden lassen
 		// Anfang start einheiten bei 2 - 4 Spielern
 		// 2 Spieler: 36
 		// 3 Spieler: 35
 		// 4 Spieler: 30
 	}
 
 	public boolean ended() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void run() {
 		// Herausfinden, welcher Spieler dran ist
 		activePlayer = spielerverwaltung.welcherSpielerIstDran();
 
 		/*
 		 * 1. Einheiten Reserve Länderanzahl/3 aber mindestens 3 Besetzte
 		 * Kontinente evtl. Karten eintauschen 2. Einheiten verteilen
 		 * 
 		 * 3. beliebig oft Kämpfen (solange er irgendwo mehr als eine Einheit
 		 * hat) Beim Kampf bei einem Land muss mindestens 1 Soldat auif dem
 		 * Quellland bleiben und der Kampf erfolgt äber Wärfel.
 		 * 
 		 * Es mässen mindestens so viele Einheiten mitgenommen werden, wie
 		 * gekämpft haben Er kann nach einem gewonnen KAmpf alle bis auf eine
 		 * Einheit mitnehmen.
 		 * 
 		 * 4. Einheiten verschieben zwischen angrenzenden Länder, Es därfen nur
 		 * Einheiten verschoben werden, die nicht gekämpf Ansonsten kännen
 		 * beliebig viele Einheiten verschoben werden
 		 */
 
 		// Wie viel Verstärkung?
 		int supply = 0;
 
 		// Wie viele Einheiten bekommt der Spieler durch eroberte Länder?
 		supply += activePlayer.getAnzahlLaender() / 3;
 		// Der Spieler bekommt mindestens 3 Einheiten
 		if (supply < 3) {
 			supply = 3;
 		}
 
 		// Bonuseinheiten durch eroberte Kontinente
 		supply += activePlayer.getContinentBonus();
 
 		// Bonuseinheiten durch Karten SPäTER, weil kein interface vorhanden
 		supply += useBonusCards();
 
 		// Einheiten setzen lassen
 		placeUnits(supply);
 
 		// Angreifen
 		attack();
 
 		// Einheiten verschieben
 		moveUnits();
 	}
 
 	private void placeUnits(int supply) {
 		Land targetCountry;
 		int amountUnitPlace;
 
 		while (supply < 0) {
 			
 			//Auf welches Land sollen Einheiten platziert werden?
 			do {
 				targetCountry = userInterface.getTargetCountry(activePlayer,
 						Phases.PLACEUNITS);
 			} while (!targetCountry.getBesitzer().equals(activePlayer));
 			
 			//Wieviele Einheiten sollen platziert werden?
 			do{
 				amountUnitPlace = userInterface.getAmountUnit(activePlayer, Phases.PLACEUNITS);
 			} while (amountUnitPlace > supply);
 			
 			//supply Aktualisieren
 			supply -= amountUnitPlace;
 		}
 
 	}
 
 	private void attack() {
 
 		// Schleife die den aktuellen Spieler Fragt ob er angreifen möchte.
 		while (userInterface.askForAttack(activePlayer)) {
 
 			Land originatingCountry;
 			Land targetCountry;
 			int amountUnitAttack;
 			int amountUnitDefense;
 
 			// Abfrage durch die CLI von welchem Land welches Angegriffen werden
 			// soll. Gehört es dem Spieler nicht erneute Abfrage. Auch neue
 			// Abfrage insofern zu wenig Einheiten zum angreifen vorhanden sind.
 			do {
 				originatingCountry = userInterface.getOriginatingCountry(activePlayer,
 						Phases.ATTACK);
 			} while (!originatingCountry.getBesitzer().equals(activePlayer)
 					|| originatingCountry.getAnzahlEinheiten() == 1);
 
 			// Abfrage durch die CLI welches Land welches Angegriffen werden
 			// soll. Gehört es dem Spieler erneute Abfrage.
 			do {
 				targetCountry = userInterface.getTargetCountry(activePlayer, Phases.ATTACK);
 			} while (targetCountry.getBesitzer().equals(activePlayer));
 
 			// Abfrage durch die CLI mit wievielen Einheiten angegriffen werden
 			// soll. Es können zwischen 1 und 3 Einheiten gewählt werden bei
 			// Falscheingabe wiederholung.
 			do {
 				amountUnitAttack = userInterface.getAmountUnit(activePlayer, Phases.ATTACK);
 			} while (originatingCountry.getAnzahlEinheiten() > amountUnitAttack
 					&& (amountUnitAttack < 1 || amountUnitAttack > 3));
 
 			// Besitzer des angegriffenden Landes ermitteln
 			Spieler attackedPlayer = targetCountry.getBesitzer();
 
 			// Abfrage durch die CLI mit wievielen Einheiten verteidigt werden
 			// soll. Es können zwischen 1 und 2 Einheiten gewählt werden.
 			do {
 				amountUnitDefense = userInterface.getAmountUnit(attackedPlayer, Phases.DEFENSE);
 			} while (targetCountry.getAnzahlEinheiten() > amountUnitDefense
 					&& (amountUnitDefense < 1 || amountUnitDefense > 2));
 
 			Kampfsystem kampf = new Kampfsystem(amountUnitAttack,
 					amountUnitDefense, originatingCountry, targetCountry);
 		}
 	}
 
 	private void moveUnits() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private int useBonusCards() {
 		// TODO Auto-generated method stub
 
 		int bonus = 0;
 		if (userInterface.turnInCards()) {
 			bonus = getCardBonus();
 		}
 		return bonus;
 	}
 
 	private int getCardBonus() {
 		// TODO Kartenbonus berechnen (Reiter)
 		return 0;
 	}
 
 	public Spieler getGewinner() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
