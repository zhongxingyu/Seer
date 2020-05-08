 package domain;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import ui.UserInterface;
 import valueobjects.Player;
 import valueobjects.Territory;
 
 /**
  * The game class manages a complete game of Risk
  * 
  * @author Jannes
  * 
  */
 public class Game {
 
 	/**
 	 * Phases of a player's turn
 	 */
 	public static enum Phases {
 		PLACEUNITS, ATTACK, MOVE, DEFEND
 	};
 
 	private PlayerManager playerManager;
 	private TerritoryManager territoryManager;
 	private Player activePlayer;
 	private UserInterface ui;
 	private ArrayList<Integer> bonusSteps;
 	private Iterator<Integer> bonusIter;
 
 	/**
 	 * Constructor for a new game of Risk
 	 */
 	public Game(UserInterface ui) {
 		this.ui = ui;
 		
 		// Setup the steps in which bonus units are allocated
 		bonusSteps = new ArrayList<Integer>(Arrays.asList(4, 6, 8, 10, 15, 20, 25, 30, 35, 40, 45,
 				50, 55, 60));
 		bonusIter = bonusSteps.iterator();
 
 		// Create territory manager
 		territoryManager = new TerritoryManager();
 
 		// Create player manager
 		playerManager = new PlayerManager(ui);
 
 		// Anfangsrunde
 		placeStartUnits();
 	}
 
 	private void placeStartUnits() {
 		// Entweder zufällig platzieren oder Spieler entscheiden lassen
 		// Anfang start einheiten bei 2 - 4 Spielern
 		// 2 Spieler: 36
 		// 3 Spieler: 35
 		// 4 Spieler: 30
 
 		// Place starting units in a random fashion
 		if (ui.getPlaceMethod()) {
 			// Gets the total amount of start units per player
 			int startUnits;
 			switch (playerManager.getPlayerCount()) {
 			case 2:
 				startUnits = 36;
 				break;
 			case 3:
 				startUnits = 35;
 				break;
 			default:
 				startUnits = 30;
 			}
 
 			// Set the start units for each player
 			for (Player player : playerManager) {
 				player.addSupply(startUnits);
 			}
 
 			// Randomly places a unit on one territory each
 			Player currentPlayer;
 			for (Territory territory : territoryManager.getRandomTerritoryList()) {
 				// Cycle through all players
 				currentPlayer = playerManager.getNextPlayer();
 
 				// Assign the territory to the player's list of territories
 				currentPlayer.addTerritory(territory);
 
 				// Place one unit on the territory and remove it from the player's supply
 				territory.setUnits(1);
 				currentPlayer.subtractSupply(1);
 			}
 
 			// Place the remaining units randomly
 			while (!playerManager.supplyAllocated()) {
 				// Cycle through all players
 				currentPlayer = playerManager.getNextPlayer();
 
 				// Add one unit to a random territory
 				currentPlayer.getRandomTerritory().addUnits(1);
 				// Remove it from the player's supply
 				currentPlayer.subtractSupply(1);
 			}
 		} else {
 			// abwechselnd setzten algorithmus
 		}
 	}
 
 	public boolean ended() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void run() {
 		// Herausfinden, welcher Spieler dran ist
 		activePlayer = playerManager.getNextPlayer();
 
 		ui.announceCurrentPlayer(activePlayer);
 
 		// Wie viel Verstärkung?
 		int supply = 0;
 
 		// Wie viele Einheiten bekommt der Spieler durch eroberte Länder?
 		supply += activePlayer.getTerritoryCount() / 3;
 		// Der Spieler bekommt mindestens 3 Einheiten
 		if (supply < 3) {
 			supply = 3;
 		}
 
 		// Bonuseinheiten durch eroberte Kontinente
 //		supply += activePlayer.getContinentBonus();
 
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
 		Territory targetTerritory = null;
 		Territory originatingTerritory = null;
 		int amountUnitPlace;
 
 		while (supply > 0) {
 
 			activePlayer.addSupply(supply);
 
 			// Auf welches Land sollen Einheiten platziert werden?
 			do {
 				targetTerritory = ui.getTargetTerritory(activePlayer, Phases.PLACEUNITS,
 						targetTerritory);
 			} while (!targetTerritory.getOwner().equals(activePlayer));
 
 			// Wieviele Einheiten sollen platziert werden?
 			do {
 				amountUnitPlace = ui.getAmountUnit(activePlayer, originatingTerritory,
 						targetTerritory, Phases.PLACEUNITS);
 			} while (amountUnitPlace > supply);
 
 			// supply Aktualisieren
 			supply -= amountUnitPlace;
 			targetTerritory.setUnits(targetTerritory.getUnitCount() + amountUnitPlace);
 		}
 
 	}
 
 	private void attack() {
 
 		// Schleife die den aktuellen Spieler Fragt ob er angreifen möchte.
 		while (ui.askForPhase(activePlayer, Phases.ATTACK)) {
 
 			Territory originatingTerritory;
 			Territory targetTerritory;
 			int amountUnitAttack;
 			int amountUnitDefense;
 
 			// Abfrage durch die CLI von welchem Land welches Angegriffen werden
 			// soll. Gehört es dem Spieler nicht erneute Abfrage. Auch neue
 			// Abfrage insofern zu wenig Einheiten zum angreifen vorhanden sind.
 			do {
 				originatingTerritory = ui.getOriginatingTerritory(activePlayer,
 						Phases.ATTACK);
 			} while (!originatingTerritory.getOwner().equals(activePlayer)
 					|| originatingTerritory.getUnitCount() == 1);
 
 			// Abfrage durch die CLI welches Land welches Angegriffen werden
 			// soll. Gehört es dem Spieler erneute Abfrage.
 			do {
 				targetTerritory = ui.getTargetTerritory(activePlayer, Phases.ATTACK,
 						originatingTerritory);
 			} while (targetTerritory.getOwner().equals(activePlayer));
 
 			// Abfrage durch die CLI mit wievielen Einheiten angegriffen werden
 			// soll. Es können zwischen 1 und 3 Einheiten gewählt werden bei
 			// Falscheingabe wiederholung.
 			do {
 				amountUnitAttack = ui.getAmountUnit(activePlayer, originatingTerritory,
 						targetTerritory, Phases.ATTACK);
 			} while (((originatingTerritory.getUnitCount() - 1) < amountUnitAttack)
 					|| (amountUnitAttack < 1 || amountUnitAttack > 3));
 
 			// Besitzer des angegriffenden Landes ermitteln
 			Player attackedPlayer = targetTerritory.getOwner();
 
 			// Abfrage durch die CLI mit wievielen Einheiten verteidigt werden
 			// soll. Es können zwischen 1 und 2 Einheiten gewählt werden.
 			do {
 				amountUnitDefense = ui.getAmountUnit(attackedPlayer,
 						originatingTerritory, targetTerritory, Phases.DEFEND);
 			} while ((targetTerritory.getUnitCount() < amountUnitDefense)
 					|| (amountUnitDefense < 0 || amountUnitDefense > 2));
 
 			BattleSystem battleSystem = new BattleSystem(amountUnitAttack, amountUnitDefense,
 					originatingTerritory, targetTerritory);
 
 			/*
 			 * TODO Wenn gewonnen wurde Land besetzten müsste meiner Meinung Nach wohl in das
 			 * BattleSystem mit reinl. Da steig ich noch nicht ganz durch also @Timur wäre ne saubere
 			 * Sache wenn du das realisierst!
 			 */
 		}
 	}
 
 	private void moveUnits() {
 
 		Territory originatingTerritory;
 		Territory targetTerritory;
 		int amountUnitMove;
 
 		if (ui.askForPhase(activePlayer, Phases.MOVE)) {
 			do {
 				originatingTerritory = ui.getOriginatingTerritory(activePlayer,
 						Phases.MOVE);
 			} while (originatingTerritory.getOwner().equals(activePlayer)
 					&& originatingTerritory.getUnitCount() < 1);
 
 			do {
 				targetTerritory = ui.getTargetTerritory(activePlayer, Phases.MOVE,
 						originatingTerritory);
 			} while (originatingTerritory.getOwner().equals(activePlayer)
 					&& originatingTerritory.getUnitCount() < 1);
 
 			do {
 				amountUnitMove = ui.getAmountUnit(activePlayer, originatingTerritory,
 						targetTerritory, Phases.MOVE);
 			} while ((originatingTerritory.getUnitCount() - 1) < amountUnitMove);
 
 			// Einheiten entsprechend der Eingabe verschieben
 			originatingTerritory.setUnits(originatingTerritory.getUnitCount() - amountUnitMove);
 			targetTerritory.setUnits(targetTerritory.getUnitCount() + amountUnitMove);
 		}
 
 	}
 
 	private int useBonusCards() {
 		// TODO Auto-generated method stub
 
 		int bonus = 0;
 		if (ui.turnInCards()) {
 			bonus = getCardBonus();
 		}
 		return bonus;
 	}
 
 	private int getCardBonus() {
 		// TODO Kartenbonus berechnen (Reiter)
 		return 0;
 	}
 
 	public Player getWinner() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
