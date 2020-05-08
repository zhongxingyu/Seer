 package core;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import phase1.*;
 import phase2.PhaseTwoPlayer;
 import phase3.P3ContextAnalyzer;
 import phase3.PhaseThreePlayer;
 import third_party.*;
 
 public class TexasHoldEm {
 
 	public static final int MINPLAYERS = 2;
 	public static final int MAXPLAYERS = 10;
 	public static final int HANDSTOPLAY = 20;
 	public static final int BETTINGROUNDS = 2;
 
 	// TODO make these dynamic ?
 	// TOANSWER yes, plz
 	public static final int SMALLBLIND = 50;
 	public static final int BIGBLIND = 100;
 
 	public enum Action {
 		call(0), check(1), raise(2), fold(3);
 
 		int action_num;
 
 		private Action(int n) {
 			action_num = n;
 		}
 
 		public int getActionNum() {
 			return action_num;
 		}
 	}
 
 	public enum GameState {
 		none(-1), Pre_flop(0), Flop(1), Turn(2), River(3);
 
 		int state;
 
 		private GameState(int n) {
 			state = n;
 		}
 
 		public int getStateNum() {
 			return state;
 		}
 
 		public GameState next() {
 			switch (this) {
 			case none:
 				return Pre_flop;
 			case Pre_flop:
 				return Flop;
 			case Flop:
 				return Turn;
 			case Turn:
 				return River;
 			case River:
 				return none;
 			default:
 				return none;
 			}
 		}
 	}
 
 	private Scanner sc;
 
 	private CardDeck cardDeck;
 	private ArrayList<Player> players; // players in the game
 	private ArrayList<Player> activePlayers; // who are still left to play the
 	// current hand
 	private int turn; // keeps track of who's turn it is to post blinds (turn
 	// signifies the dealer)
 	private GameState gameState; // how far we are into a single game
 	private int handsPlayed; // how far we are into the overall game
 	private Card table[]; // contains flop, turn and river
 	private int pot; // total in pot at any given time
 	// private int pool; // keeps track of the pool in the current betting round
 	private int highbet; // keeps track of the highest bet of this hand
 	private P3ContextAnalyzer contextAnalyzer;
 
 	public TexasHoldEm() {
 		sc = new Scanner(System.in);
 		// signupPlayers();
 		contextAnalyzer = new P3ContextAnalyzer(this);
		fastSignupPlayers();
 		handsPlayed = 0;
 
 		gameState = GameState.none;
 		table = new Card[5];
 
 		playGame();
 	}
 
 	public void nextState() {
 		gameState = gameState.next();
 	}
 
 	public void resetState() {
 		gameState = GameState.none;
 	}
 
 	public int getNumPlayers() {
 		return players.size();
 	}
 
 	public GameState getGameState() {
 		return gameState;
 	}
 
 	public int getHandsPlayed() {
 		return handsPlayed;
 	}
 
 	public Card[] getTable() {
 		return table;
 	}
 
 	public int getPot() {
 		return pot;
 	}
 
 	public void playGame() {
 		turn = 0;
 		while (handsPlayed < HANDSTOPLAY)
 			// IS HANDSPLAYED INITIALISED HERE?
 			playHand();
 		// TODO print endofgame info etc
 		for (Player p : players)
 			System.out
 					.println(p.getName() + " has " + p.getStack() + " chips.");
 	}
 
 	public void playHand() {
 		cardDeck = new CardDeck();
 		activePlayers = new ArrayList<Player>();
 
 		// add from the player pool starting at the player that is to start
 		// betting
 		for (int i = 0; i < players.size(); i++) {
 			players.get((i + turn + 2) % players.size()).clearCards();
 			activePlayers.add(players.get((i + turn + 2) % players.size()));
 		}
 
 		dealCards();
 		nextState();
 		postBlinds();
 		printHands();
 
 		startBetting();
 		flop();
 		nextState();
 		startBetting();
 		turn();
 		nextState();
 		startBetting();
 		river();
 		nextState();
 		startBetting();
 		showdown();
 		contextAnalyzer.notifyEndOfHand(activePlayers);
 		nextState();
 		handsPlayed++;
 		turn = (turn + 1) % players.size();
 		highbet = 0;
 		for (Player p : players) {
 			p.setCurrentBet(0);
 			p.setPower(null);
 		}
 	}
 
 	private void postBlinds() {
 		players.get(turn).forceBet(SMALLBLIND);
 		System.out.println("\nSMALL " + players.get(turn).getName() + "\nBIG "
 				+ players.get((turn + 1) % players.size()).getName());
 		players.get((turn + 1) % players.size()).forceBet(BIGBLIND);
 		highbet = BIGBLIND;
 		pot = SMALLBLIND + BIGBLIND;
 	}
 
 	@SuppressWarnings("unused")
 	private void printHands() {
 		System.out.println("Holes:");
 		for (Player p : players) {
 			System.out.print(p.getName() + "\t");
 
 		}
 		System.out.print("\n");
 		for (Player p : players) {
 			System.out.print(p.hand[0].toString() + " and "
 					+ p.hand[1].toString() + "\t");
 
 		}
 		System.out.println();
 
 	}
 
 	public int getIndexOfPlayer(Player p) {
 		return players.indexOf(p);
 	}
 
 	private void showdown() {
 		Player tmp;
 		ArrayList<Player> bestPlayers = new ArrayList<Player>();
 		System.out.println("\nShowdown: ");
 		for (Player p : activePlayers) {
 			p.calculatePower();
 			System.out.println(p.getName() + " " + result(p.getPower()));
 			if (bestPlayers.size() == 0)
 				bestPlayers.add(p);
 			else {
 				tmp = comparePlayers(p, bestPlayers.get(0));
 				if (tmp == null) { // another player draws
 					bestPlayers.add(p);
 				} else if (tmp == p) {
 					bestPlayers.clear();
 					bestPlayers.add(tmp);
 				}
 			}
 		}
 
 		if (bestPlayers.size() == 1) {
 			System.out.println("Player " + bestPlayers.get(0).getName()
 					+ " has won with " + result(bestPlayers.get(0).getPower()));
 			bestPlayers.get(0).recieveMoneyFromWin(pot);
 		} else {
 			System.out.print("There was a draw, pot split between:\n");
 			for (Player p : bestPlayers) {
 				p.recieveMoneyFromWin(pot / bestPlayers.size());
 				System.out.println(p.getName() + " " + result(p.getPower())
 						+ " ");
 			}
 			System.out.println("\n");
 		}
 	}
 
 	public static String result(int power[]) {
 		switch (power[0]) {
 		case 9:
 			if (power[1] == 14)
 				return "Royal Straight Flush";
 			return "Straight Flush, " + power[1];
 		case 8:
 			return "Four of a Kind, " + power[1];
 		case 7:
 			return "Full House, " + power[1] + ", " + power[2];
 		case 6:
 			return "Flush, " + power[1];
 		case 5:
 			return "Straight, " + power[1];
 		case 4:
 			return "Three of a kind, " + power[1];
 		case 3:
 			return "Two Pairs, " + power[1] + ", " + power[2];
 		case 2:
 			return "A Pair, " + power[1] + ", highcard, " + power[2];
 		case 1:
 			return "High Card, " + power[1] + ", highcard #2 " + power[2];
 		}
 		return null;
 	}
 
 	private void river() {
 		cardDeck.dealCard();
 		table[4] = cardDeck.dealCard();
 		printTable(5);
 	}
 
 	private void turn() {
 		cardDeck.dealCard();
 		table[3] = cardDeck.dealCard();
 		printTable(4);
 	}
 
 	private void flop() {
 		cardDeck.dealCard();
 		table[0] = cardDeck.dealCard();
 		table[1] = cardDeck.dealCard();
 		table[2] = cardDeck.dealCard();
 		printTable(3);
 	}
 
 	private void printTable(int cards) {
 		System.out.print("\nCards on table:\n");
 		for (int i = 0; i < cards; i++)
 			System.out.print(table[i].toString() + " ");
 		System.out.println("The Pot is at: " + pot + "\n\n");
 	}
 
 	private void startBetting() {
 		contextAnalyzer.newBettingRound(players);
 		int better = 0, lastbet = 0, betround = 0, bet;
 		Player currentBetter, highBetter = null;
 		do {
 			if (activePlayers.size() == 1) {
 				break;
 			}
 			// printActivePlayers();
 			bet = 0;
 			currentBetter = activePlayers.get(better);
 			bet = currentBetter.bet();
 			if (bet == 0) { // check or fold
 				if (highbet > currentBetter.getCurrentBet()) {// folded
 					activePlayers.remove(currentBetter);
 					// contextAnalyzer.event(currentBetter, Action.fold);
 					// not needed when folded
 					System.out.println(currentBetter.getName() + " has folded");
 				} else {
 					lastbet++;
 					better++;
 					highBetter = null;
 					contextAnalyzer.event(currentBetter, Action.check);
 					System.out
 							.println(currentBetter.getName() + " has checked");
 				}
 			} else { // raise or call
 				if (bet + currentBetter.getCurrentBet() < highbet) {
 					// folded due to betting insufficient chips
 					activePlayers.remove(currentBetter);
 					// contextAnalyzer.event(currentBetter, Action.fold);
 					// not needed when folded
 					System.out.println(currentBetter.getName()
 							+ " has folded due to insuffiecent betting");
 				} else {
 
 					if (currentBetter.getCurrentBet() + bet > highbet) { // raise
 						contextAnalyzer.event(currentBetter, Action.raise);
 						pot += bet;
 						currentBetter.loseMoneyfromBet(bet);
 						currentBetter.setCurrentBet(bet
 								+ currentBetter.getCurrentBet());
 
 						lastbet = 0;
 						contextAnalyzer.raise();
 						int oldhigh = highbet;
 						highbet = currentBetter.getCurrentBet();
 						better++;
 						System.out.println(currentBetter.getName()
 								+ " has raised by " + (highbet - oldhigh));
 						highBetter = currentBetter;
 
 					} else { // call
 						contextAnalyzer.event(currentBetter, Action.call);
 						pot += bet;
 						currentBetter.loseMoneyfromBet(bet);
 						currentBetter.setCurrentBet(bet
 								+ currentBetter.getCurrentBet());
 						lastbet++;
 						better++;
 
 						System.out.println(currentBetter.getName()
 								+ " has called");
 						// highBetter = null;
 					}
 				}
 
 			}
 			if (better == activePlayers.size()) {
 				betround++;
 				better = 0;
 				System.out.println("Betround " + betround
 						+ " is now finished\n\n");
 			}
 		} while (lastbet < activePlayers.size() && betround < BETTINGROUNDS);
 		// 3 rounds of betting max
 
 		// For final betting round, so that end doesn't end with one having
 		// raised and the others not calling him.
 		if (highBetter != null) {
 			int iterator = 0;
 			while (iterator < activePlayers.size()) {
 				currentBetter = activePlayers.get(iterator);
 				if (currentBetter == highBetter)
 					break;
 				else {
 					bet = currentBetter.bet();
 					if ((bet + currentBetter.getCurrentBet() < highbet)) {
 						// did not bet enough to call
 						// contextAnalyzer.event(currentBetter, Action.fold);
 						// not needed when folded
 						activePlayers.remove(currentBetter);
 						System.out.println(currentBetter.getName()
 								+ " has folded");
 					} else {// bet enough to call or raise, but raise is not
 						// allowed, so will turn bet into finalBet which
 						// must be a call.
 						int finalBet = bet
 								- ((bet + currentBetter.getCurrentBet()) - highbet);
 						contextAnalyzer.event(currentBetter, Action.call);
 						// System.out.println(finalBet);
 						pot += finalBet;
 						currentBetter.loseMoneyfromBet(finalBet);
 						currentBetter.setCurrentBet(finalBet
 								+ currentBetter.getCurrentBet());
 						System.out.println(currentBetter.getName()
 								+ " has called");
 						iterator++;
 					}
 				}
 			}
 		}
 		System.out.println("Betting done");
 	}
 
 	@SuppressWarnings("unused")
 	private void fastSignupPlayers() {
 		players = new ArrayList<Player>();
 		players.add(new PhaseOnePlayer(1, this));
 		players.add(new PhaseOnePlayer(2, this));
 		//players.add(new PhaseOnePlayer(3, this));
 		players.add(new PhaseTwoPlayer("Phase2 4", this));
 		players.add(new PhaseTwoPlayer("Phase2 5", this));
 		//players.add(new PhaseTwoPlayer("Phase2 6", this));
 		// players.add(new PhaseThreePlayer("Phase3 7", this, contextAnalyzer));
 		 players.add(new PhaseThreePlayer("Phase3 8", this, contextAnalyzer));
 		 players.add(new PhaseThreePlayer("Phase3 9", this, contextAnalyzer));
 		// players.add(new PhaseTwoPlayer("phase2 4", this));
 		// players.add(new HumanPlayer(sc, this));
 	}
 
 	@SuppressWarnings("unused")
 	private void signupPlayers() {
 		players = new ArrayList<Player>();
 
 		System.out.println("New Game started sign up players\n"
 				+ " * 'h' for human player.\n"
 				+ " * '1' for phase one player.\n" + "\n"
 				+ " End with anything else.\n");
 
 		String s = sc.next();
 		int comps = 1;
 		while (s != null && players.size() < 11) {
 			switch (s.charAt(0)) {
 			case 'h':
 				players.add(new HumanPlayer(sc, this));
 				break;
 			case '1':
 				players.add(new PhaseOnePlayer(comps++, this));
 				break;
 			case '2':
 				players.add(new PhaseTwoPlayer("Phase2 " + (comps++), this));
 				break;
 			case '3':
 				break;
 			default:
 				s = null;
 				break;
 			// etc adding in possibilities of different kinds of phaseplayers as
 			// well later
 			}
 			if (s == null)
 				break;
 			s = sc.next();
 		}
 	}
 
 	public static int getSmallblind() {
 		return SMALLBLIND;
 	}
 
 	public static int getBigblind() {
 		return BIGBLIND;
 	}
 
 	/*
 	 * public ArrayList<Player> getPlayers() { return players; }
 	 */
 
 	/*
 	 * public ArrayList<Player> getActivePlayers() { return activePlayers; }
 	 */
 	public int getActivePlayersSize() {
 		return activePlayers.size();
 	}
 
 	public int getHighbet() {
 		return highbet;
 	}
 
 	private void dealCards() {
 		for (int i = 0; i < 2; i++)
 			for (Player p : players)
 				p.assignCard(cardDeck.dealCard());
 	}
 
 	/*
 	 * public int compareHands(ArrayList<Card> a, ArrayList<Card> b) { for (Card
 	 * c : table) { a.add(c); b.add(c); } int aPower[] =
 	 * cards_py.calcCardsPower(a); int bPower[] = cards_py.calcCardsPower(b);
 	 * for (int i = 0; i < aPower.length && i < bPower.length; i++) { if
 	 * (aPower[i] > bPower[i]) return 1; else if (aPower[i] < bPower[i]) return
 	 * -1; } return 0; }
 	 */
 
 	public static int compareHands(ArrayList<Card> a, ArrayList<Card> b,
 			Card table[], int tableSizeLimit) {
 
 		for (int i = 0; i < 5; i++) {
 			if (table[i] == null || i == tableSizeLimit)
 				break;
 			a.add(table[i]);
 			b.add(table[i]);
 		}
 		int aPower[] = cards_py.calcCardsPower(a);
 		int bPower[] = cards_py.calcCardsPower(b);
 		for (int i = 0; i < aPower.length && i < bPower.length; i++) {
 			if (aPower[i] > bPower[i])
 				return 1;
 			else if (aPower[i] < bPower[i])
 				return -1;
 		}
 		return 0;
 	}
 
 	public Player comparePlayers(Player a, Player b) {
 		if (b == null)
 			return a;
 		for (int i = 0; i < a.getPower().length && i < b.getPower().length; i++) {
 			if (a.getPower()[i] > b.getPower()[i])
 				return a;
 			else if (a.getPower()[i] < b.getPower()[i])
 				return b;
 		}
 		return null;
 	}
 
 	public boolean containsCard(Card c) {
 		for (Card card : table) {
 			if (card != null && card.isEqual(c))
 				return true;
 		}
 		return false;
 	}
 
 	public void printActivePlayers() {
 		System.out.println();
 		for (Player p : activePlayers) {
 			System.out.print(p.getName() + "\t");
 		}
 		System.out.println();
 	}
 }
