 /**
  * JSkat - A skat program written in Java
  * Copyright by Jan Schäfer and Markus J. Luzius
  *
  * Version: 0.8.0-SNAPSHOT
  * Build date: 2011-05-23 18:57:15
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package org.jskat.control;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jskat.ai.IJSkatPlayer;
 import org.jskat.data.GameAnnouncement;
 import org.jskat.data.SkatGameData;
 import org.jskat.data.SkatGameData.GameState;
 import org.jskat.data.Trick;
 import org.jskat.gui.IJSkatView;
 import org.jskat.gui.human.HumanPlayer;
 import org.jskat.util.Card;
 import org.jskat.util.CardDeck;
 import org.jskat.util.CardList;
 import org.jskat.util.GameType;
 import org.jskat.util.Player;
 import org.jskat.util.SkatConstants;
 import org.jskat.util.rule.BasicSkatRules;
 import org.jskat.util.rule.SkatRuleFactory;
 
 
 /**
  * Controls a skat game
  */
 public class SkatGame extends JSkatThread {
 
 	private static Log log = LogFactory.getLog(SkatGame.class);
 	private int maxSleep = 100;
 	private SkatGameData data;
 	private CardDeck deck;
 	private Map<Player, IJSkatPlayer> player;
 	private String tableName;
 	private IJSkatView view;
 	private BasicSkatRules rules;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param newTableName
 	 *            Table name
 	 * @param newForeHand
 	 *            Fore hand player
 	 * @param newMiddleHand
 	 *            Middle hand player
 	 * @param newRearHand
 	 *            Hind hand player
 	 */
 	public SkatGame(String newTableName, IJSkatPlayer newForeHand,
 			IJSkatPlayer newMiddleHand, IJSkatPlayer newRearHand) {
 
 		tableName = newTableName;
 		player = new HashMap<Player, IJSkatPlayer>();
 		player.put(Player.FOREHAND, newForeHand);
 		player.put(Player.MIDDLEHAND, newMiddleHand);
 		player.put(Player.REARHAND, newRearHand);
 
 		// inform all players about the starting of the new game
 		for (Player currPlayerPosition : player.keySet()) {
 			player.get(currPlayerPosition).newGame(currPlayerPosition);
 		}
 
 		data = new SkatGameData();
 		setGameState(GameState.GAME_START);
 	}
 
 	/**
 	 * @see Thread#run()
 	 */
 	@Override
 	public void run() {
 
 		view.clearTable(tableName);
 		view.setGameState(tableName, data.getGameState());
 
 		do {
 
 			log.debug("Game state " + data.getGameState()); //$NON-NLS-1$
 
 			switch (data.getGameState()) {
 			case GAME_START:
 				setGameState(GameState.DEALING);
 				break;
 			case DEALING:
 				dealCards();
 				setGameState(GameState.BIDDING);
 				break;
 			case BIDDING:
 				view.setActivePlayer(tableName, Player.MIDDLEHAND);
 				bidding();
 				if (data.getGameType() == GameType.PASSED_IN) {
 
 					setGameState(GameState.PRELIMINARY_GAME_END);
 				} else {
 
 					view.setDeclarer(tableName, data.getDeclarer());
 					setGameState(GameState.PICK_UP_SKAT);
 				}
 				break;
 			case PICK_UP_SKAT:
 				if (pickUpSkat()) {
 					setGameState(GameState.DISCARDING);
 					view.setSkat(tableName, data.getSkat());
 				} else {
 					data.setHand(true);
 					setGameState(GameState.DECLARING);
 				}
 				break;
 			case DISCARDING:
 				discarding();
 				setGameState(GameState.DECLARING);
 				break;
 			case DECLARING:
 				announceGame();
 				setGameState(GameState.TRICK_PLAYING);
 				break;
 			case TRICK_PLAYING:
 				playTricks();
 				setGameState(GameState.CALC_GAME_VALUE);
 				break;
 			case PRELIMINARY_GAME_END:
 				setGameState(GameState.CALC_GAME_VALUE);
 				break;
 			case CALC_GAME_VALUE:
 				calculateGameValue();
 				setGameState(GameState.GAME_OVER);
 				break;
 			case GAME_OVER:
 				break;
 			}
 
 			checkWaitCondition();
 		} while (data.getGameState() != GameState.GAME_OVER);
 
 		log.debug(data.getGameState());
 	}
 
 	private boolean pickUpSkat() {
 
 		return player.get(data.getDeclarer()).pickUpSkat();
 	}
 
 	/**
 	 * Deals the cards to the players and the skat
 	 */
 	public void dealCards() {
 
 		if (deck == null) {
 			// Skat game has no cards, yet
 			deck = new CardDeck();
 			log.debug("shuffling..."); //$NON-NLS-1$
 
 			deck.shuffle();
 			log.debug(deck);
 		}
 
 		doSleep(maxSleep);
 
 		log.debug("dealing..."); //$NON-NLS-1$
 
 		for (int i = 0; i < 3; i++) {
 			// deal three rounds of cards
 			switch (i) {
 			case 0:
 				// deal three cards
 				dealCards(3);
 				// and put two cards into the skat
 				data.setDealtSkatCards(deck.remove(0), deck.remove(0));
 				break;
 			case 1:
 				// deal four cards
 				dealCards(4);
 				break;
 			case 2:
 				// deal three cards
 				dealCards(3);
 				break;
 			}
 		}
 
 		// show cards in the view
 		Map<Player, CardList> dealtCards = data.getDealtCards();
 		for (Player currPlayer : Player.values()) {
 
 			view.addCards(tableName, currPlayer, dealtCards.get(currPlayer));
 		}
 
 		doSleep(maxSleep);
 
 		log.debug("Fore hand: " + data.getPlayerCards(Player.FOREHAND)); //$NON-NLS-1$
 		log.debug("Middle hand: " //$NON-NLS-1$
 				+ data.getPlayerCards(Player.MIDDLEHAND));
 		log.debug("Hind hand: " + data.getPlayerCards(Player.REARHAND)); //$NON-NLS-1$
 		log.debug("Skat: " + data.getSkat()); //$NON-NLS-1$
 	}
 
 	/**
 	 * Deals the cards to the players
 	 * 
 	 * @param deck
 	 *            Card deck
 	 * @param cardCount
 	 *            Number of cards to be dealt to a player
 	 */
 	private void dealCards(int cardCount) {
 
 		for (Player hand : Player.values()) {
 			// for all players
 			for (int j = 0; j < cardCount; j++) {
 				// deal amount of cards
 				Card card = deck.remove(0);
 				// player can get original card object because Card is immutable
 				player.get(hand).takeCard(card);
 				data.setDealtCard(hand, card);
 			}
 		}
 	}
 
 	/**
 	 * Controls the bidding of all players
 	 */
 	private void bidding() {
 
 		int bidValue = 0;
 
 		data.setBidValue(0);
 
 		log.debug("ask middle and fore hand..."); //$NON-NLS-1$
 
 		bidValue = twoPlayerBidding(Player.MIDDLEHAND, Player.FOREHAND, bidValue);
 
 		log.debug("Bid value after first bidding: " //$NON-NLS-1$
 				+ bidValue);
 
 		Player firstWinner = getBiddingWinner(Player.MIDDLEHAND, Player.FOREHAND);
 
 		log.debug("First bidding winner: " + firstWinner); //$NON-NLS-1$
 		log.debug("ask hind hand and first winner..."); //$NON-NLS-1$
 
 		bidValue = twoPlayerBidding(Player.REARHAND, firstWinner, bidValue);
 
 		log.debug("Bid value after second bidding: " //$NON-NLS-1$
 				+ bidValue);
 
 		// get second winner
 		Player secondWinner = getBiddingWinner(Player.REARHAND, firstWinner);
 
 		if (secondWinner == Player.FOREHAND && bidValue == 0) {
 
 			log.debug("Check whether fore hand holds at least one bid"); //$NON-NLS-1$
 
 			view.setActivePlayer(tableName, Player.FOREHAND);
 
 			// check whether fore hand holds at least one bid
 			if (!(player.get(Player.FOREHAND).bidMore(18) > -1)) {
 
 				log.debug("Fore hand passes too"); //$NON-NLS-1$
 				secondWinner = null;
 			} else {
 
 				view.setBid(tableName, secondWinner, 18, true);
 				log.debug("Fore hand holds 18"); //$NON-NLS-1$
 			}
 		}
 
 		if (secondWinner != null) {
 			// there is a winner of the bidding
 			setSinglePlayer(secondWinner);
 			view.setActivePlayer(tableName, secondWinner);
 
 			data.setBidValue(bidValue);
 
 			log.debug("Player " + data.getDeclarer() //$NON-NLS-1$
 					+ " wins the bidding."); //$NON-NLS-1$
 		} else {
 			// pass in
 			GameAnnouncement ann = new GameAnnouncement();
 			ann.setGameType(GameType.PASSED_IN);
 			setGameAnnouncement(ann);
 		}
 
 		doSleep(maxSleep);
 	}
 
 	private void informPlayerAboutBid(Player bidPlayer, int bidValue) {
 
 		// inform all players about the last bid
 		for (Player currPlayerPosition : player.keySet()) {
 			player.get(currPlayerPosition).bidByPlayer(bidPlayer, bidValue);
 		}
 	}
 
 	/**
 	 * Controls the bidding between two players
 	 * 
 	 * @param announcer
 	 *            Announcing player
 	 * @param hearer
 	 *            Hearing player
 	 * @param startBidValue
 	 *            Bid value to start from
 	 * @return
 	 */
 	private int twoPlayerBidding(Player announcer, Player hearer, int startBidValue) {
 
 		int currBidValue = startBidValue;
 		boolean announcerPassed = false;
 		boolean hearerPassed = false;
 
 		while (!announcerPassed && !hearerPassed) {
 
 			// get bid value
 			int nextBidValue = SkatConstants.getNextBidValue(currBidValue);
 			view.setBidValueToMake(tableName, nextBidValue);
 			// ask player
 			int announcerBidValue = player.get(announcer).bidMore(nextBidValue);
 
 			if (announcerBidValue > -1 && SkatConstants.bidOrder.contains(Integer.valueOf(announcerBidValue))) {
 
 				log.debug("announcer bids " + announcerBidValue); //$NON-NLS-1$
 
 				// announcing hand holds bid
 				currBidValue = announcerBidValue;
 
 				data.setBidValue(announcerBidValue);
 				data.setPlayerBid(announcer, announcerBidValue);
 				informPlayerAboutBid(announcer, announcerBidValue);
 				view.setBid(tableName, announcer, announcerBidValue, true);
 
 				if (player.get(hearer).holdBid(currBidValue)) {
 
 					log.debug("hearer holds " + currBidValue); //$NON-NLS-1$
 
 					// hearing hand holds bid
 					data.setBidValue(announcerBidValue);
 					data.setPlayerBid(hearer, announcerBidValue);
 					informPlayerAboutBid(hearer, announcerBidValue);
 					view.setBid(tableName, hearer, announcerBidValue, false);
 
 				} else {
 
 					log.debug("hearer passed at " + announcerBidValue); //$NON-NLS-1$
 
 					// hearing hand passed
 					hearerPassed = true;
 					data.setPlayerPass(hearer, true);
 					view.setPass(tableName, hearer);
 				}
 			} else {
 
 				log.debug("announcer passed at " + nextBidValue); //$NON-NLS-1$
 
 				// announcing hand passes
 				announcerPassed = true;
 				data.setPlayerPass(announcer, true);
 				view.setPass(tableName, announcer);
 			}
 		}
 
 		return currBidValue;
 	}
 
 	private Player getBiddingWinner(Player announcer, Player hearer) {
 
 		Player biddingWinner = null;
 
 		if (data.isPlayerPass(announcer)) {
 			biddingWinner = hearer;
 		} else if (data.isPlayerPass(hearer)) {
 			biddingWinner = announcer;
 		}
 
 		return biddingWinner;
 	}
 
 	private void discarding() {
 
 		log.debug("Player looks into the skat..."); //$NON-NLS-1$
 		log.debug("Skat before discarding: " + data.getSkat()); //$NON-NLS-1$
 
 		IJSkatPlayer declarer = player.get(data.getDeclarer());
 
 		// create a clone of the skat before sending it to the player
 		// otherwise the player could change the skat after discarding
 		declarer.takeSkat((CardList) data.getSkat().clone());
 
 		// ask player for the cards to be discarded
 		// cloning is done to prevent the player
 		// from manipulating the skat afterwards
 		CardList discardedSkat = new CardList();
 		discardedSkat.addAll(declarer.discardSkat());
 
 		if (!checkDiscardedCards(discardedSkat)) {
 			// TODO throw an appropriate exceptions
 
 		}
 
 		log.debug("Discarded cards: " + discardedSkat); //$NON-NLS-1$
 
 		data.setDiscardedSkat(data.getDeclarer(), discardedSkat);
 	}
 
 	private boolean checkDiscardedCards(CardList discardedSkat) {
 
 		// TODO move this to skat rules?
 		boolean result = true;
 
 		if (discardedSkat == null) {
 
 			log.error("Player is fooling!!! Skat is empty!"); //$NON-NLS-1$
 			result = false;
 		} else if (discardedSkat.size() != 2) {
 
 			log.error("Player is fooling!!! Skat doesn't have two cards!"); //$NON-NLS-1$
 			result = false;
 		}
 		// TODO check for jacks in the discarded skat in ramsch games
 
 		return result;
 	}
 
 	private void announceGame() {
 
 		log.debug("declaring game..."); //$NON-NLS-1$
 
 		// TODO check for valid game announcements
 		try {
 			GameAnnouncement ann = (GameAnnouncement) player.get(data.getDeclarer()).announceGame().clone();
 
 			setGameAnnouncement(ann);
 		} catch (NullPointerException e) {
 			// player has not returned an object
 			// TODO finish game immediately
 			e.printStackTrace();
 		} catch (CloneNotSupportedException e) {
 			// player has not returned a game announcement
 			// TODO finish game immediately
 			e.printStackTrace();
 		}
 
 		doSleep(maxSleep);
 	}
 
 	private void playTricks() {
 
 		view.clearTrickCards(tableName);
 
 		for (int trickNo = 0; trickNo < 10; trickNo++) {
 
 			log.debug("Play trick " + (trickNo + 1)); //$NON-NLS-1$
 			doSleep(maxSleep);
 
 			view.setTrickNumber(tableName, trickNo + 1);
 
 			Player newTrickForeHand = null;
 			if (trickNo == 0) {
 				// first trick
 				newTrickForeHand = Player.FOREHAND;
 			} else {
 				// all the other tricks
 				Trick lastTrick = data.getTricks().get(data.getTricks().size() - 1);
 
 				// set new trick fore hand
 				newTrickForeHand = lastTrick.getTrickWinner();
 			}
 
 			view.setTrickForeHand(tableName, newTrickForeHand);
 			view.setActivePlayer(tableName, newTrickForeHand);
 
 			Trick trick = new Trick(trickNo, newTrickForeHand);
 			data.addTrick(trick);
 
 			// Ask players for their cards
 			log.debug("fore hand plays"); //$NON-NLS-1$
 			playCard(trick, newTrickForeHand, newTrickForeHand);
 			doSleep(maxSleep);
 
 			log.debug("middle hand plays"); //$NON-NLS-1$
 			view.setActivePlayer(tableName, newTrickForeHand.getLeftNeighbor());
 			playCard(trick, newTrickForeHand, newTrickForeHand.getLeftNeighbor());
 			doSleep(maxSleep);
 
 			log.debug("hind hand plays"); //$NON-NLS-1$
 			view.setActivePlayer(tableName, newTrickForeHand.getRightNeighbor());
 			playCard(trick, newTrickForeHand, newTrickForeHand.getRightNeighbor());
 
 			doSleep(maxSleep);
 
 			log.debug("Calculate trick winner"); //$NON-NLS-1$
 			Player trickWinner = rules.calculateTrickWinner(data.getGameType(), trick);
 			trick.setTrickWinner(trickWinner);
 			data.addPlayerPoints(trickWinner, trick.getCardValueSum());
 
 			for (Player currPosition : Player.values()) {
 				// inform all players
 				// cloning of trick information to prevent manipulation by
 				// player
 				try {
 					player.get(currPosition).showTrick((Trick) trick.clone());
 				} catch (CloneNotSupportedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 			// Check for preliminary ending of a null game
 			if (GameType.NULL.equals(data.getGameType())) {
 
 				if (trickWinner == data.getDeclarer()) {
 					// declarer has won a trick
 					setGameState(GameState.PRELIMINARY_GAME_END);
 				}
 			}
 
 			doSleep(maxSleep);
 
 			log.debug("Trick cards: " + trick.getCardList()); //$NON-NLS-1$
 			log.debug("Points: fore hand: " + data.getPlayerPoints(Player.FOREHAND) + //$NON-NLS-1$
 					" middle hand: " //$NON-NLS-1$
 					+ data.getPlayerPoints(Player.MIDDLEHAND) + " hind hand: " //$NON-NLS-1$
 					+ data.getPlayerPoints(Player.REARHAND));
 
 			if (isFinished()) {
 				break;
 			}
 
 			checkWaitCondition();
 		}
 
 		if (data.getGameType() == GameType.RAMSCH) {
 			// TODO give the card points of the skat to a player defined in
 			// ramsch rules
 		} else {
 			// for all the other games, points to the declarer
 			data.addPlayerPoints(data.getDeclarer(), data.getSkat().getCardValueSum());
 		}
 
 		// set schneider/schwarz/jungfrau/durchmarsch flags
 		switch (data.getGameType()) {
 		case CLUBS:
 		case SPADES:
 		case HEARTS:
 		case DIAMONDS:
 		case GRAND:
 			data.setSchneiderSchwarz();
 			break;
 		case RAMSCH:
 			data.setJungfrauDurchmarsch();
 			break;
 		case NULL:
 		case PASSED_IN:
 			// do nothing
 			break;
 		}
 	}
 
 	private void playCard(Trick trick, Player trickForeHand, Player currPlayer) {
 
 		Card card = null;
 		IJSkatPlayer skatPlayer = getPlayerObject(currPlayer);
 
 		boolean isCardAccepted = false;
 		while (!isCardAccepted) {
 			// ask player for the next card
 			card = skatPlayer.playCard();
 
 			log.debug(card + " " + data); //$NON-NLS-1$
 
 			if (card == null) {
 
 				log.error("Player is fooling!!! Did not play a card!"); //$NON-NLS-1$
 
 				view.showCardNotAllowedMessage(card);
 
 			} else if (!playerHasCard(currPlayer, card)) {
 
 				log.error("Player is fooling!!! Doesn't have card " + card + "!"); //$NON-NLS-1$//$NON-NLS-2$
 
 				view.showCardNotAllowedMessage(card);
 
 			} else if (!rules.isCardAllowed(data.getGameType(), trick.getFirstCard(), data.getPlayerCards(currPlayer),
 					card)) {
 
				log.error("Player " + skatPlayer.getClass().toString() + " card not allowed: " + card + " game type: " //$NON-NLS-1$ //$NON-NLS-2$
 						+ data.getGameType() + " first trick card: " //$NON-NLS-1$
 						+ trick.getFirstCard() + " player cards: " //$NON-NLS-1$
 						+ data.getPlayerCards(currPlayer));
 
 				view.showCardNotAllowedMessage(card);
 
 				if (!(skatPlayer instanceof HumanPlayer)) {
 					// TODO create option for switching playing schwarz on/off
 					isCardAccepted = true;
 				}
 
 			} else {
 
 				isCardAccepted = true;
 			}
 		}
 
 		// card was on players hand and is valid
 		data.getPlayerCards(currPlayer).remove(card);
 		data.setTrickCard(currPlayer, card);
 
 		if (trick.getTrickNumberInGame() > 0 && currPlayer.equals(trickForeHand)) {
 			// remove all cards from current trick panel first
 			view.clearTrickCards(tableName);
 
 			Trick lastTrick = data.getTricks().get(data.getTricks().size() - 2);
 
 			// set last trick cards
 			view.setLastTrick(tableName, lastTrick.getForeHand(), lastTrick.getCard(Player.FOREHAND),
 					lastTrick.getCard(Player.MIDDLEHAND), lastTrick.getCard(Player.REARHAND));
 		}
 
 		view.playTrickCard(tableName, currPlayer, card);
 
 		for (Player currPosition : Player.values()) {
 			// inform all players
 			// cloning of card is not neccessary, because Card is immutable
 			player.get(currPosition).cardPlayed(currPlayer, card);
 		}
 
 		log.debug("playing card " + card); //$NON-NLS-1$
 	}
 
 	private IJSkatPlayer getPlayerObject(Player currPlayer) {
 
 		return player.get(currPlayer);
 	}
 
 	/**
 	 * Checks whether a player has the card on it's hand or not
 	 * 
 	 * @param card
 	 *            Card to check
 	 * @return TRUE if the card is on player's hand
 	 */
 	private boolean playerHasCard(Player skatPlayer, Card card) {
 
 		boolean result = false;
 
 		log.debug("Player cards: " + data.getPlayerCards(skatPlayer)); //$NON-NLS-1$
 
 		for (Card handCard : data.getPlayerCards(skatPlayer)) {
 
 			if (handCard.equals(card)) {
 
 				result = true;
 			}
 		}
 
 		return result;
 	}
 
 	private boolean isFinished() {
 
 		return data.getGameState() == GameState.PRELIMINARY_GAME_END || data.getGameState() == GameState.GAME_OVER;
 	}
 
 	private void calculateGameValue() {
 
 		log.debug("Calculate game value"); //$NON-NLS-1$
 
 		// FIXME (jan 07.12.2010) don't let a data class calculate it's values
 		data.calcResult();
 
 		log.debug("game value=" + data.getResult() + ", bid value=" //$NON-NLS-1$ //$NON-NLS-2$
 				+ data.getBidValue());
 
 		if (data.isGameWon() && data.getBidValue() > data.getGameResult()) {
 
 			log.debug("Overbidding: Game is lost"); //$NON-NLS-1$
 			// Game was overbidded
 			// game is lost despite the winning of the single player
 			data.setOverBidded(true);
 		}
 
 		log.debug("Final game result: lost:" + data.isGameLost() + //$NON-NLS-1$
 				" game value: " + data.getResult()); //$NON-NLS-1$
 
 		for (IJSkatPlayer currPlayer : player.values()) {
 			// no cloning neccessary because all parameters are primitive data
 			// types
 			currPlayer.setGameResult(data.isGameWon(), data.getGameResult());
 			currPlayer.finalizeGame();
 		}
 
 		doSleep(maxSleep);
 	}
 
 	private void doSleep(int milliseconds) {
 
 		try {
 			sleep(milliseconds);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sets the view for the game
 	 * 
 	 * @param newView
 	 */
 	public void setView(IJSkatView newView) {
 
 		view = newView;
 	}
 
 	/**
 	 * Sets the cards from outside
 	 * 
 	 * @param newDeck
 	 *            Card deck
 	 */
 	public void setCardDeck(CardDeck newDeck) {
 
 		deck = newDeck;
 	}
 
 	/**
 	 * Sets the game announcement from the outside
 	 * 
 	 * @param ann
 	 *            Game announcement
 	 */
 	public void setGameAnnouncement(GameAnnouncement ann) {
 
 		data.setAnnouncement(ann);
 		rules = SkatRuleFactory.getSkatRules(data.getGameType());
 
 		// inform all players
 		for (IJSkatPlayer currPlayer : player.values()) {
 			// no cloning neccessary, because all parameters are primitive data
 			// types
 			currPlayer.startGame(data.getDeclarer(), data.getGameType(), data.isHand(), data.isOuvert(),
 					data.isSchneiderAnnounced(), data.isSchwarzAnnounced());
 		}
 
 		view.setGameAnnouncement(tableName, data.getDeclarer(), ann);
 
 		log.debug(data.getAnnoucement());
 	}
 
 	/**
 	 * Sets the game state from outside
 	 * 
 	 * @param newState
 	 *            Game state
 	 */
 	public void setGameState(GameState newState) {
 
 		data.setGameState(newState);
 
 		if (view != null) {
 
 			view.setGameState(tableName, newState);
 
 			if (newState == GameState.GAME_START) {
 
 				view.clearTable(tableName);
 
 			} else if (newState == GameState.GAME_OVER) {
 
 				view.addGameResult(tableName, data);
 			}
 		}
 	}
 
 	/**
 	 * Sets the single player from outside
 	 * 
 	 * @param singlePlayer
 	 *            Single player
 	 */
 	public void setSinglePlayer(Player singlePlayer) {
 
 		data.setDeclarer(singlePlayer);
 	}
 
 	/**
 	 * Gets whether a game was won or not
 	 * 
 	 * @return TRUE if the game was won
 	 */
 	public boolean isGameWon() {
 
 		return data.isGameWon();
 	}
 
 	/**
 	 * Gets the maximum sleep time
 	 * 
 	 * @return Maximum sleep time
 	 */
 	public int getMaxSleep() {
 
 		return maxSleep;
 	}
 
 	/**
 	 * Sets the maximum sleep time
 	 * 
 	 * @param newMaxSleep
 	 *            Maximum sleep time
 	 */
 	public void setMaxSleep(int newMaxSleep) {
 
 		maxSleep = newMaxSleep;
 	}
 
 	/**
 	 * Gets the game result
 	 * 
 	 * @return Game result
 	 */
 	public int getGameResult() {
 
 		return data.getGameResult();
 	}
 
 	/**
 	 * @see Object#toString()
 	 */
 	@Override
 	public String toString() {
 
 		return data.getGameState().toString();
 	}
 }
