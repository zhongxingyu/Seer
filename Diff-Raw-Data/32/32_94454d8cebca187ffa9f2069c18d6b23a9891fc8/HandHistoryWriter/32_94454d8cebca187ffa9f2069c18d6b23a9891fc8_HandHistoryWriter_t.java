 package game;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import util.Utils;
 
 import com.biotools.meerkat.Action;
 import com.biotools.meerkat.Card;
 import com.biotools.meerkat.GameInfo;
 import com.biotools.meerkat.GameObserver;
 import com.biotools.meerkat.Hand;
 
 /**
  * A HandHistoryWriter observes a game and on a {@link #gameOverEvent()} writes a hand-history-file to a given {@link Writer} in Full-Till format.<br>
  * Please note that the format is not 100% FullTilt, but shortened to the most important information that importers (like PokerTracker and HoldemManager) need
  * for correct processing.
  */
 
 public class HandHistoryWriter implements GameObserver {
 	Logger log = Logger.getLogger(this.getClass().getName());
 
 	private GameInfo gameInfo;
 	private Writer outWriter;
 	private StringBuffer currentHistory = new StringBuffer();
 	private DecimalFormat moneyFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ENGLISH));
 	private ShowdownDataCache showdownDataCache;
 
 	// values could be taken from gameInfo, but this is not ready yet
 	private Map<Integer, Double> amountInPotThisRound = new TreeMap<Integer, Double>();
 
 	/**
 	 * This writer will get a handhistory on each gameOverEvent
 	 * @param outWriter
 	 */
 	public void setWriter(Writer outWriter) {
 		this.outWriter = outWriter;
 	}
 
 	@Override
 	public void actionEvent(int pos, Action act) {
 		double playerInPotThisRound = amountInPotThisRound.get(pos);
 
		if (act.getType() == PublicGameInfo.SPECIAL_ACTION_RETURNUNCALLEDBET) {
			currentHistory.append("Uncalled bet of $").append(moneyFormat.format(act.getAmount())).append(" returned to ").append(gameInfo.getPlayerName(pos))
					.append("\n");
			return;
		}

 		currentHistory.append(gameInfo.getPlayerName(pos)).append(" ");
 		switch (act.getType()) {
 		case Action.SMALL_BLIND:
 			currentHistory.append("posts the small blind of $").append(moneyFormat.format(act.getAmount())).append("\n");
 			amountInPotThisRound.put(pos, Utils.roundToCents(playerInPotThisRound + act.getAmount()));
 			break;
 		case Action.BIG_BLIND:
 			currentHistory.append("posts the big blind of $").append(moneyFormat.format(act.getAmount())).append("\n");
 			currentHistory.append("The button is in seat #").append(gameInfo.getButtonSeat() + 1).append("\n");
 			amountInPotThisRound.put(pos, Utils.roundToCents(playerInPotThisRound + act.getAmount()));
 			break;
 		case Action.CALL:
 			currentHistory.append("calls $").append(moneyFormat.format(act.getToCall())).append("\n");
 			amountInPotThisRound.put(pos, Utils.roundToCents(playerInPotThisRound + act.getToCall()));
 			break;
 		case Action.RAISE:
 			currentHistory.append("raises to $").append(moneyFormat.format(playerInPotThisRound + act.getToCall() + act.getAmount())).append("\n");
 			amountInPotThisRound.put(pos, Utils.roundToCents(playerInPotThisRound + act.getToCall() + act.getAmount()));
 			break;
 		case Action.BET:
			if (playerInPotThisRound > 0) {
				// BigBlind bets, but in the history this should be treated as raise
				currentHistory.append("raises to $").append(moneyFormat.format(act.getAmount() + playerInPotThisRound)).append("\n");
			} else {
				currentHistory.append("bets $").append(moneyFormat.format(act.getAmount())).append("\n");
			}
 			amountInPotThisRound.put(pos, act.getAmount());
 			break;
 
 		case Action.CHECK:
 			currentHistory.append("checks\n");
 			break;
 		case Action.FOLD:
 			currentHistory.append("folds\n");
 			break;
 		case Action.MUCK:
 			currentHistory.append("mucks\n");
 			break;
 		default:
 			throw new IllegalStateException("Action " + act + " not supported (yet)");
 		}
 	}
 
 	@Override
 	public void dealHoleCardsEvent() {
 		currentHistory.append("*** HOLE CARDS ***\n");
 	}
 
 	@Override
 	public void gameOverEvent() {
 		writeSummaryGameInfo();
 
 		if (outWriter != null) {
 			try {
 				outWriter.write(currentHistory.toString());
 				outWriter.flush();
 			} catch (IOException e) {
 				// currently we don't rethrow so to not disturb the game ?
 				log.log(Level.SEVERE, "error writing handhistory", e);
 			}
 		} else {
 			log.severe("no writer set, can't write HandHistory");
 		}
 		currentHistory = new StringBuffer();
 	}
 
 	@Override
 	public void gameStartEvent(GameInfo gInfo) {
 		this.gameInfo = gInfo;
 		this.showdownDataCache = new ShowdownDataCache(gameInfo);
 		writeInitalGameInfo();
 	}
 
 	@Override
 	public void gameStateChanged() {
 		// not interesting
 	}
 
 	@Override
 	public void showdownEvent(int seat, Card c1, Card c2) {
 		currentHistory.append(gameInfo.getPlayerName(seat)).append(" shows ");
 		currentHistory.append("[").append(c1).append(" ").append(c2).append("]\n");
 		showdownDataCache.addShowDownCards(seat, c1, c2);
 	}
 
 	@Override
 	public void stageEvent(int stage) {
 		for (int i = 0; i < gameInfo.getNumSeats(); i++) {
 			amountInPotThisRound.put(i, Double.valueOf(0));
 		}
 
 		switch (stage) {
 		case 0:
 			break; // preflop not interesting
 		case 1:
 			currentHistory.append("*** FLOP *** ");
 			currentHistory.append(renderBoard(false)).append("\n");
 			break;
 		case 2:
 			currentHistory.append("*** TURN *** ");
 			currentHistory.append(renderBoard(true)).append("\n");
 			break;
 		case 3:
 			currentHistory.append("*** RIVER *** ");
 			currentHistory.append(renderBoard(true)).append("\n");
 			break;
 		default:
 			throw new IllegalStateException("stage " + stage + "not supported");
 		}
 	}
 
 	/**
 	 * creates a string representation of the current board, like [8c 8d 8s]
 	 * @param separateLastCard if true, the last card gets separated like [8c 8d 8s] [8h]
 	 * @return
 	 */
 	public String renderBoard(boolean separateLastCard) {
 		Hand board = gameInfo.getBoard();
 		String renderString = "[";
 		for (int i = 0; i < board.size(); i++) {
 			renderString += board.getCard(i + 1);
 			if (separateLastCard && (i == board.size() - 2)) {
 				renderString += "] [";
 			} else if (i < board.size() - 1) {
 				renderString += " ";
 			}
 		}
 		renderString += "]";
 		return renderString;
 	}
 
 	@Override
 	public void winEvent(int pos, double amount, String handName) {
 		// Text to create:
 		// player5 wins the pot ($0.05)
 		// player6 ties for the pot	($x.xx)
 		if (!showdownDataCache.areFinalPotsInitialized()) {
 			showdownDataCache.initializeFinalPot(gameInfo);
 		}
 
 		currentHistory.append(gameInfo.getPlayerName(pos));
 		if (showdownDataCache.getAmountFromPot(pos, amount)) {
 			currentHistory.append(" ties for the pot ");
 		} else {
 			currentHistory.append(" wins the pot ");
 		}
 
 		currentHistory.append("($").append(moneyFormat.format(amount)).append(")\n");
 	}
 
 	/**
 	 * Writes the beginning Header
 	 * 
 	 * <pre>
 	 * Full Tilt Poker Game #19342777650: Table Jay (shallow) - $0.01/$0.02 - No Limit Hold'em - 18:21:50 ET - 2010/03/17
 	 * Seat 1: player1 ($0.33)
 	 * Seat 3: player3 ($0.42)
 	 * </pre>
 	 * 
 	 */
 	private void writeInitalGameInfo() {
 		// First line:
 		// Full Tilt Poker Game #19342777650: Table Jay (shallow) - $0.01/$0.02 - No Limit Hold'em - 18:21:50 ET - 2010/03/17
 		currentHistory.append("Full Tilt Poker Game #").append(gameInfo.getGameID());
 		currentHistory.append(": Table OpenTestBed - $");
 		currentHistory.append(moneyFormat.format(gameInfo.getSmallBlindSize()));
 		currentHistory.append("/$");
 		currentHistory.append(moneyFormat.format(gameInfo.getBigBlindSize()));
 		currentHistory.append(" - ").append(gameInfo.isNoLimit() ? "No " : gameInfo.isPotLimit() ? "Pot " : "").append("Limit Hold'em");
 
 		DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss z - yyyy/MM/dd");
 		currentHistory.append(" - ").append(dateFormat.format(getGameTime()));
 		currentHistory.append("\n");
 
 		if (gameInfo.isActive(9)) {
 			throw new IllegalStateException("Full-Tilt format only supports 9 players, but 10 players are seated");
 		}
 
 		// Player Seats
 		// Seat 1: player1 ($0.33)
 		for (int playerId = 0; playerId < 9; playerId++) {
 			if (gameInfo.isActive(playerId)) {
 				currentHistory.append("Seat ").append(playerId + 1).append(": ");
 				currentHistory.append(gameInfo.getPlayerName(playerId));
 				currentHistory.append(" ($").append(moneyFormat.format(gameInfo.getBankRoll(playerId))).append(")\n");
 			}
 		}
 	}
 
 	/**
 	 * writes the summary like
 	 * <pre>
 	 * *** SUMMARY ***
 	 * Seat 1: redilts collected ($0.02)
 	 * Seat 4: player4 showed [Ad As] and won ($2.30)
 	 * </pre>
 	 */
 	private void writeSummaryGameInfo() {
 		currentHistory.append("*** SUMMARY ***\n");
 		for (Entry<Integer, Double> playerAmountWon : showdownDataCache.getAmountsWon().entrySet()) {
 			int seat = playerAmountWon.getKey();
 			double amount = playerAmountWon.getValue();
 			if (amount < 0.001) {
 				continue;
 			}
 			currentHistory.append("Seat ").append(seat + 1);
 			currentHistory.append(": ").append(gameInfo.getPlayerName(seat));
 			List<Card> cards = showdownDataCache.getHandsShown(seat);
 			if (cards != null) {
 				currentHistory.append(" showed [").append(cards.get(0)).append(" ").append(cards.get(1)).append("] and won ");
 			} else {
 				currentHistory.append(" collected ");
 			}
 			currentHistory.append("($").append(moneyFormat.format(amount)).append(")\n");
 		}
		currentHistory.append("\n\n");
 	}
 
 	/**
 	 * to be overridden by JUnit-Tests
 	 * @return
 	 */
 	protected Date getGameTime() {
 		return new Date();
 	}
 
 	/**
 	 * this helper class caches some results relevant for showdown and showdown summary.<br>
 	 * For instance the GameInfo doesn't give us infos about which player got what amount
 	 * from a certain pot and if this tied the pot (but in the output we have to provide this).
 	 * Thats why on ShowDown we copy the pot-sizes into our cache and for each payout determine
 	 * ourselfes if this was from a tied pot or a full pot.<br>
 	 * Furthermore we cache all shown cards and player winnings for later printing in the summary. 
 	 */
 
 	static class ShowdownDataCache {
 		/** pot sizes at showdown */
 		private List<Double> potsAtShowDown;
 		/** of a pot is tied */
 		private List<Boolean> tiedPotsAtShowDown;
 		private Map<Integer, List<Card>> handShown = new TreeMap<Integer, List<Card>>();
 		private Map<Integer, Double> amountWon = new TreeMap<Integer, Double>();
 
 		public ShowdownDataCache(GameInfo gameInfo) {
 			for (int i = 0; i < gameInfo.getNumSeats(); i++) {
 				amountWon.put(i, Double.valueOf(0));
 			}
 		}
 
 		public void addShowDownCards(int seat, Card c1, Card c2) {
 			handShown.put(seat, Arrays.asList(c1, c2));
 		}
 
 		/**
 		 * @return returns the cards of the corresponding player, if he showed
 		 * the cards (otherwise null)
 		 */
 		public List<Card> getHandsShown(int seat) {
 			return handShown.get(seat);
 		}
 
 		/**
 		 * @return Map<Integer, Double> map<PlayerId, Amount> of player winnings
 		 */
 		public Map<Integer, Double> getAmountsWon() {
 			return amountWon;
 		}
 
 		/**
 		 * reduces the given amount from the latest pot and returns, if the
 		 * amount tied the pot or was the full pot<br>
 		 * Furthermore this method accumulates each players winnings
 		 * @param amount
 		 * @return
 		 */
 		public boolean getAmountFromPot(int pos, double amount) {
 			boolean tied = false;
 
 			if (potsAtShowDown.size() == 0) {
 				throw new IllegalStateException("no pot left to get a payout from");
 			}
 
 			int currentPot = potsAtShowDown.size() - 1;
 			double currentSidePotAmount = potsAtShowDown.get(currentPot);
 
 			if (amount > currentSidePotAmount) {
 				throw new IllegalStateException("the current (side) pot doesn't contain enough money for this payout\n"
 						+ "Please ensure, that payouts match the pot sizes\n");
 			}
 
 			// if the amount is smaller than the amount, this pot is marked
 			// as tied
 			if (amount < currentSidePotAmount) {
 				tiedPotsAtShowDown.set(currentPot, Boolean.TRUE);
 			}
 
 			// either this amount has tied the pot or a previous amount
 			if (tiedPotsAtShowDown.get(currentPot)) {
 				tied = true;
 			}
 
 			// reduce the potAmount in our cache. If the full pot 
 			// was payed out, remove it
 			currentSidePotAmount = Utils.roundToCents(currentSidePotAmount - amount);
 
 			if (currentSidePotAmount < 0.001) {
 				potsAtShowDown.remove(currentPot);
 				tiedPotsAtShowDown.remove(currentPot);
 			} else {
 				potsAtShowDown.set(currentPot, Double.valueOf(currentSidePotAmount));
 			}
 
 			// update players winnings
 			double playerAmountWon = amountWon.get(pos);
 			amountWon.put(pos, Utils.roundToCents(playerAmountWon + amount));
 
 			return tied;
 		}
 
 		/**
 		 * copies the pot sizes into internal structure
 		 * @param gameInfo
 		 */
 		public void initializeFinalPot(GameInfo gameInfo) {
 			potsAtShowDown = new ArrayList<Double>();
 			tiedPotsAtShowDown = new ArrayList<Boolean>();
 
 			// remember main pot, not tied yet
 			potsAtShowDown.add(Double.valueOf(gameInfo.getMainPotSize()));
 			tiedPotsAtShowDown.add(Boolean.FALSE);
 			// side pots, not tied yet
 			for (int i = 0; i < gameInfo.getNumSidePots(); i++) {
 				potsAtShowDown.add(new Double(gameInfo.getSidePotSize(i)));
 				tiedPotsAtShowDown.add(Boolean.FALSE);
 			}
 		}
 
 		public boolean areFinalPotsInitialized() {
 			return potsAtShowDown != null;
 		}
 	}
 }
