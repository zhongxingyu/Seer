 /**
  * JSkat - A skat program written in Java
  * by Jan Schäfer, Markus J. Luzius and Daniel Loreck
  *
  * Version 0.11.0
  * Copyright (C) 2012-08-28
  *
  * Licensed under the Apache License, Version 2.0. You may
  * obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.jskat.control;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jskat.data.GameAnnouncement;
 import org.jskat.data.GameAnnouncement.GameAnnouncementFactory;
 import org.jskat.data.GameSummary;
 import org.jskat.data.JSkatOptions;
 import org.jskat.data.SkatGameData;
 import org.jskat.data.SkatGameData.GameState;
 import org.jskat.data.SkatGameResult;
 import org.jskat.data.SkatTableOptions.RamschSkatOwner;
 import org.jskat.data.Trick;
 import org.jskat.gui.JSkatView;
 import org.jskat.player.JSkatPlayer;
 import org.jskat.util.Card;
 import org.jskat.util.CardDeck;
 import org.jskat.util.CardList;
 import org.jskat.util.GameType;
 import org.jskat.util.GameVariant;
 import org.jskat.util.JSkatResourceBundle;
 import org.jskat.util.Player;
 import org.jskat.util.SkatConstants;
 import org.jskat.util.rule.SkatRule;
 import org.jskat.util.rule.SkatRuleFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Controls a skat game
  */
 public class SkatGame extends JSkatThread {
 
	private static Logger log = LoggerFactory.getLogger(SkatGame.class);
 	private int maxSleep;
 	private final SkatGameData data;
 	private final GameVariant variant;
 	private CardDeck deck;
 	private final Map<Player, JSkatPlayer> player;
 	private final String tableName;
 	private JSkatView view;
 	private SkatRule rules;
 
 	private final JSkatResourceBundle strings = JSkatResourceBundle.instance();
 
 	/**
 	 * Constructor
 	 * 
 	 * @param newTableName
 	 *            Table name
 	 * @param variant
 	 *            game variant
 	 * @param newForeHand
 	 *            Fore hand player
 	 * @param newMiddleHand
 	 *            Middle hand player
 	 * @param newRearHand
 	 *            Rear hand player
 	 */
 	public SkatGame(final String newTableName, final GameVariant variant,
 			final JSkatPlayer newForeHand, final JSkatPlayer newMiddleHand,
 			final JSkatPlayer newRearHand) {
 		tableName = newTableName;
 		setName("SkatGame on table " + tableName); //$NON-NLS-1$
 		this.variant = variant;
 		player = new HashMap<Player, JSkatPlayer>();
 		player.put(Player.FOREHAND, newForeHand);
 		player.put(Player.MIDDLEHAND, newMiddleHand);
 		player.put(Player.REARHAND, newRearHand);
 
 		// inform all players about the starting of the new game
 		for (final Player pos : player.keySet()) {
 			getPlayerInstance(pos).newGame(pos);
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
 			log.debug("SkatGame.do --- Game state: " + data.getGameState()); //$NON-NLS-1$
 
 			switch (data.getGameState()) {
 			case GAME_START:
 				setGameState(GameState.DEALING);
 				break;
 			case DEALING:
 				dealCards();
 				setGameState(GameState.BIDDING);
 				break;
 			case BIDDING:
 				setActivePlayer(Player.MIDDLEHAND);
 
 				if (variant == GameVariant.RAMSCH) {
 					// ramsch games are enforced
 					final GameAnnouncementFactory gaf = GameAnnouncement
 							.getFactory();
 					gaf.setGameType(GameType.RAMSCH);
 					setGameAnnouncement(gaf.getAnnouncement());
 				} else {
 					// "normal" game (i.e. no ramsch)
 					bidding();
 				}
 
 				if (GameType.PASSED_IN.equals(data.getGameType())) {
 					setGameState(GameState.PRELIMINARY_GAME_END);
 				} else if (GameType.RAMSCH.equals(data.getGameType())) {
 					setGameState(GameState.RAMSCH_GRAND_HAND_ANNOUNCING);
 				} else {
 					view.setDeclarer(tableName, data.getDeclarer());
 					setGameState(GameState.PICKING_UP_SKAT);
 				}
 				break;
 			case RAMSCH_GRAND_HAND_ANNOUNCING:
 				boolean grandHandAnnounced = false;
 
 				for (final Player currPlayer : Player.getOrderedList()) {
 					setActivePlayer(currPlayer);
 					if (!grandHandAnnounced && playGrandHand(currPlayer)) {
 						setDeclarer(currPlayer);
 						grandHandAnnounced = true;
 					}
 				}
 
 				if (!grandHandAnnounced) {
 					if (JSkatOptions.instance().isSchieberRamsch(true)) {
 						log.debug("no grand hand - initiating schieberamsch"); //$NON-NLS-1$
 						setGameState(GameState.SCHIEBERAMSCH);
 					} else {
 						log.debug("no grand hand and no schieberamsch - play ramsch"); //$NON-NLS-1$
 						setGameState(GameState.TRICK_PLAYING);
 					}
 				} else {
 					log.debug(data.getDeclarer() + " is playing grand hand"); //$NON-NLS-1$
 					final GameAnnouncementFactory gaf = GameAnnouncement
 							.getFactory();
 					gaf.setGameType(GameType.GRAND);
 					gaf.setHand(Boolean.TRUE);
 					setGameAnnouncement(gaf.getAnnouncement());
 					setGameState(GameState.TRICK_PLAYING);
 					log.debug("grand hand game started"); //$NON-NLS-1$
 					break;
 				}
 				break;
 			case SCHIEBERAMSCH:
 				for (final Player currPlayer : Player.getOrderedList()) {
 					setActivePlayer(currPlayer);
 					if (!pickUpSkat(data.getActivePlayer())) {
 						log.debug(currPlayer + " schiebt"); //$NON-NLS-1$
 						data.addGeschoben();
 						view.setGeschoben(tableName, currPlayer);
 					} else {
 						view.setSkat(tableName, data.getSkat());
 						discarding();
 					}
 
 					if (Player.REARHAND.equals(data.getActivePlayer())) {
 						final GameAnnouncementFactory factory = GameAnnouncement
 								.getFactory();
 						factory.setGameType(GameType.RAMSCH);
 						setGameAnnouncement(factory.getAnnouncement());
 						setGameState(GameState.TRICK_PLAYING);
 					}
 				}
 				break;
 			case PICKING_UP_SKAT:
 				setActivePlayer(data.getDeclarer());
 				setGameState(GameState.DISCARDING);
 				if (pickUpSkat(data.getDeclarer())) {
 					data.setDeclarerPickedUpSkat(true);
 					view.setSkat(tableName, data.getSkat());
 				} else {
 					setGameState(GameState.DECLARING);
 				}
 				break;
 			case DISCARDING:
 				setActivePlayer(data.getDeclarer());
 				discarding();
 				setGameState(GameState.DECLARING);
 				break;
 			case DECLARING:
 				announceGame();
 				setGameState(GameState.TRICK_PLAYING);
 				break;
 			case TRICK_PLAYING:
 				playTricks();
 				setGameState(GameState.CALCULATING_GAME_VALUE);
 				break;
 			case PRELIMINARY_GAME_END:
 				setGameState(GameState.CALCULATING_GAME_VALUE);
 				break;
 			case CALCULATING_GAME_VALUE:
 				calculateGameValue();
 				setGameState(GameState.GAME_OVER);
 				break;
 			case GAME_OVER:
 				break;
 			}
 
 			checkWaitCondition();
 		} while (data.getGameState() != GameState.GAME_OVER && !isTerminated());
 
 		log.debug(data.getGameState().name());
 	}
 
 	private void setActivePlayer(final Player activePlayer) {
 		data.setActivePlayer(activePlayer);
 		view.setActivePlayer(tableName, activePlayer);
 	}
 
 	private boolean playGrandHand(final Player playerPosition) {
 		return getPlayerInstance(playerPosition).playGrandHand();
 	}
 
 	private boolean pickUpSkat(final Player playerPosition) {
 		return getPlayerInstance(playerPosition).pickUpSkat();
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
 			log.debug(deck.toString());
 		}
 
 		doSleep(maxSleep);
 
 		log.debug("dealing..."); //$NON-NLS-1$
 
 		// deal three rounds of cards
 		// deal three cards
 		dealCards(3);
 		// and put two cards into the skat
 		data.setDealtSkatCards(deck.remove(0), deck.remove(0));
 		// deal four cards
 		dealCards(4);
 		// deal three cards
 		dealCards(3);
 
 		// show cards in the view
 		final Map<Player, CardList> dealtCards = data.getDealtCards();
 		for (final Player currPlayer : Player.getOrderedList()) {
 
 			view.addCards(tableName, currPlayer, dealtCards.get(currPlayer));
 		}
 
 		doSleep(maxSleep);
 
 		log.debug("Fore hand: " + data.getPlayerCards(Player.FOREHAND)); //$NON-NLS-1$
 		log.debug("Middle hand: " //$NON-NLS-1$
 				+ data.getPlayerCards(Player.MIDDLEHAND));
 		log.debug("Rear hand: " + data.getPlayerCards(Player.REARHAND)); //$NON-NLS-1$
 		log.debug("Skat: " + data.getSkat()); //$NON-NLS-1$
 	}
 
 	/**
 	 * Deals a given number of cards to the players
 	 * 
 	 * @param cardCount
 	 *            Number of cards to be dealt to a player
 	 */
 	private void dealCards(final int cardCount) {
 
 		for (final Player hand : Player.getOrderedList()) {
 			// for all players
 			for (int j = 0; j < cardCount; j++) {
 				// deal amount of cards
 				final Card card = deck.remove(0);
 				// player can get original card object because Card is immutable
 				getPlayerInstance(hand).takeCard(card);
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
 
 		bidValue = twoPlayerBidding(Player.MIDDLEHAND, Player.FOREHAND,
 				bidValue);
 
 		log.debug("Bid value after first bidding: " //$NON-NLS-1$
 				+ bidValue);
 
 		final Player firstWinner = getBiddingWinner(Player.MIDDLEHAND,
 				Player.FOREHAND);
 
 		log.debug("First bidding winner: " + firstWinner); //$NON-NLS-1$
 		log.debug("ask rear hand and first winner..."); //$NON-NLS-1$
 
 		bidValue = twoPlayerBidding(Player.REARHAND, firstWinner, bidValue);
 
 		log.debug("Bid value after second bidding: " //$NON-NLS-1$
 				+ bidValue);
 
 		// get second winner
 		Player secondWinner = getBiddingWinner(Player.REARHAND, firstWinner);
 
 		if (secondWinner == Player.FOREHAND && bidValue == 0) {
 
 			log.debug("Check whether fore hand holds at least one bid"); //$NON-NLS-1$
 
 			view.setActivePlayer(tableName, Player.FOREHAND);
 
 			// check whether fore hand holds at least one bid
 			if (!(getPlayerInstance(Player.FOREHAND).bidMore(18) > -1)) {
 
 				log.debug("Fore hand passes too"); //$NON-NLS-1$
 				view.setPass(tableName, Player.FOREHAND);
 				secondWinner = null;
 			} else {
 
 				view.setBid(tableName, secondWinner, 18, true);
 				log.debug("Fore hand holds 18"); //$NON-NLS-1$
 			}
 		}
 
 		if (secondWinner != null) {
 			// there is a winner of the bidding
 			setDeclarer(secondWinner);
 			view.setActivePlayer(tableName, secondWinner);
 
 			data.setBidValue(bidValue);
 
 			log.debug("Player " + data.getDeclarer() //$NON-NLS-1$
 					+ " wins the bidding."); //$NON-NLS-1$
 		} else {
 			// FIXME (jansch 02.01.2012) use cloned rule options here (see
 			// MantisBT: 0000037)
 			final JSkatOptions options = JSkatOptions.instance();
 
 			if (options.isPlayRamsch() && options.isRamschEventNoBid()) {
 				log.debug("Playing ramsch due to no bid"); //$NON-NLS-1$
 				final GameAnnouncementFactory factory = GameAnnouncement
 						.getFactory();
 				factory.setGameType(GameType.RAMSCH);
 				setGameAnnouncement(factory.getAnnouncement());
 				view.setGameAnnouncement(tableName, data.getDeclarer(),
 						data.getAnnoucement());
 				// do not call "setGameAnnouncement(..)" here!
 			} else {
 				// pass in
 				final GameAnnouncementFactory factory = GameAnnouncement
 						.getFactory();
 				factory.setGameType(GameType.PASSED_IN);
 				setGameAnnouncement(factory.getAnnouncement());
 			}
 		}
 
 		doSleep(maxSleep);
 	}
 
 	private void informPlayersAboutBid(final Player bidPlayer,
 			final int bidValue) {
 		// inform all players about the last bid
 		for (final JSkatPlayer playerInstance : player.values()) {
 			playerInstance.bidByPlayer(bidPlayer, bidValue);
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
 	 * @return the final bid value
 	 */
 	private int twoPlayerBidding(final Player announcer, final Player hearer,
 			final int startBidValue) {
 
 		int currBidValue = startBidValue;
 		boolean announcerPassed = false;
 		boolean hearerPassed = false;
 
 		while (!announcerPassed && !hearerPassed) {
 
 			// get bid value
 			final int nextBidValue = SkatConstants
 					.getNextBidValue(currBidValue);
 			view.setBidValueToMake(tableName, nextBidValue);
 			// ask player
 			final int announcerBidValue = getPlayerInstance(announcer).bidMore(
 					nextBidValue);
 
 			if (announcerBidValue > -1
 					&& SkatConstants.bidOrder.contains(Integer
 							.valueOf(announcerBidValue))) {
 
 				log.debug("announcer bids " + announcerBidValue); //$NON-NLS-1$
 
 				// announcing hand holds bid
 				currBidValue = announcerBidValue;
 
 				data.setBidValue(announcerBidValue);
 				data.setPlayerBid(announcer, announcerBidValue);
 				informPlayersAboutBid(announcer, announcerBidValue);
 				view.setBid(tableName, announcer, announcerBidValue, true);
 
 				if (getPlayerInstance(hearer).holdBid(currBidValue)) {
 
 					log.debug("hearer holds " + currBidValue); //$NON-NLS-1$
 
 					// hearing hand holds bid
 					data.setBidValue(announcerBidValue);
 					data.setPlayerBid(hearer, announcerBidValue);
 					informPlayersAboutBid(hearer, announcerBidValue);
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
 
 	private Player getBiddingWinner(final Player announcer, final Player hearer) {
 
 		Player biddingWinner = null;
 
 		if (data.isPlayerPass(announcer)) {
 			biddingWinner = hearer;
 		} else if (data.isPlayerPass(hearer)) {
 			biddingWinner = announcer;
 		}
 
 		return biddingWinner;
 	}
 
 	private void discarding() {
 
 		final Player activePlayerPosition = data.getActivePlayer();
 		final JSkatPlayer activePlayer = getPlayerInstance(activePlayerPosition);
 
 		log.debug("Player (" + activePlayer + ") looks into the skat..."); //$NON-NLS-1$ //$NON-NLS-2$
 		log.debug("Skat before discarding: " + data.getSkat()); //$NON-NLS-1$
 
 		final CardList skatBefore = new CardList(data.getSkat());
 
 		// create a clone of the skat before sending it to the player
 		// otherwise the player could change the skat after discarding
 		activePlayer.takeSkat(data.getSkat());
 		data.addSkatCardsToDiscardingPlayer(activePlayerPosition);
 
 		// ask player for the cards to be discarded
 		// cloning is done to prevent the player
 		// from manipulating the skat afterwards
 		final CardList discardedSkat = new CardList();
 		discardedSkat.addAll(activePlayer.discardSkat());
 
 		if (!checkDiscardedCards(data.getActivePlayer(), discardedSkat)) {
 			// TODO throw an appropriate exceptions
 		}
 
 		log.debug("Discarded cards: " + discardedSkat); //$NON-NLS-1$
 
 		data.setDiscardedSkat(data.getActivePlayer(), discardedSkat);
 		if (!activePlayer.isHumanPlayer()) {
 			// human player has changed the cards in the GUI already
 			view.setDiscardedSkat(tableName, data.getActivePlayer(),
 					skatBefore, discardedSkat);
 		}
 	}
 
 	private boolean checkDiscardedCards(Player player, CardList discardedSkat) {
 
 		// TODO move this to skat rules?
 		boolean result = true;
 
 		if (discardedSkat == null) {
 
 			log.error("Player is fooling!!! Skat is empty!"); //$NON-NLS-1$
 			result = false;
 		} else if (discardedSkat.size() != 2) {
 
 			log.error("Player is fooling!!! Skat doesn't have two cards!"); //$NON-NLS-1$
 			result = false;
 		} else if (discardedSkat.get(0) == discardedSkat.get(1)) {
 			log.error("Player is fooling!!! Skat cards are identical!"); //$NON-NLS-1$
 			result = false;
 		} else if (!playerHasCard(player, discardedSkat.get(0))
 				|| !playerHasCard(player, discardedSkat.get(1))) {
 			log.error("Player is fooling!!! Player doesn't have had discarded card! Dis"); //$NON-NLS-1$
 			result = false;
 		}
 		// TODO check for jacks in the discarded skat in ramsch games
 
 		return result;
 	}
 
 	private void announceGame() {
 
 		log.debug("declaring game..."); //$NON-NLS-1$
 
 		// TODO check for valid game announcements
 		final GameAnnouncement ann = getPlayerInstance(data.getDeclarer())
 				.announceGame();
 		if (ann != null) {
 			setGameAnnouncement(ann);
 		} else {
 			view.showErrorMessage(
 					strings.getString("invalid_game_announcement_title"), //$NON-NLS-1$
 					strings.getString("invalid_game_announcement_message", ann)); //$NON-NLS-1$
 		}
 
 		doSleep(maxSleep);
 	}
 
 	private void playTricks() {
 
 		view.clearTrickCards(tableName);
 		Player trickWinner = null;
 
 		for (int trickNo = 0; trickNo < 10; trickNo++) {
 
 			log.debug("=============== Play trick " + (trickNo + 1) + " ==============="); //$NON-NLS-1$ //$NON-NLS-2$
 			doSleep(maxSleep);
 
 			view.setTrickNumber(tableName, trickNo + 1);
 
 			Player newTrickForeHand = null;
 			newTrickForeHand = getTrickForeHand(trickNo);
 
 			view.setTrickForeHand(tableName, newTrickForeHand);
 			view.setActivePlayer(tableName, newTrickForeHand);
 
 			final Trick trick = new Trick(trickNo, newTrickForeHand);
 			data.addTrick(trick);
 			informPlayersAboutNewTrick(trick);
 
 			// Ask players for their cards
 			log.debug("fore hand plays"); //$NON-NLS-1$
 			playCard(trick, newTrickForeHand, newTrickForeHand);
 
 			if (isFinished()) {
 				break;
 			}
 
 			doSleep(maxSleep);
 
 			log.debug("middle hand plays"); //$NON-NLS-1$
 			view.setActivePlayer(tableName, newTrickForeHand.getLeftNeighbor());
 			playCard(trick, newTrickForeHand,
 					newTrickForeHand.getLeftNeighbor());
 
 			if (isFinished()) {
 				break;
 			}
 
 			doSleep(maxSleep);
 
 			log.debug("rear hand plays"); //$NON-NLS-1$
 			view.setActivePlayer(tableName, newTrickForeHand.getRightNeighbor());
 			playCard(trick, newTrickForeHand,
 					newTrickForeHand.getRightNeighbor());
 
 			if (isFinished()) {
 				break;
 			}
 
 			doSleep(maxSleep);
 
 			log.debug("Calculate trick winner"); //$NON-NLS-1$
 			trickWinner = rules.calculateTrickWinner(data.getGameType(), trick);
 			trick.setTrickWinner(trickWinner);
 			data.addPlayerPoints(trickWinner, trick.getValue());
 			view.setPlayedTrick(tableName, trick);
 
 			informPlayersAboutCompletedTrick(trick);
 
 			// Check for preliminary ending of a null game
 			if (GameType.NULL.equals(data.getGameType())) {
 
 				if (trickWinner == data.getDeclarer()) {
 					// declarer has won a trick
 					setGameState(GameState.PRELIMINARY_GAME_END);
 				}
 			}
 
 			log.debug("Trick cards: " + trick.getCardList()); //$NON-NLS-1$
 			logPlayerPoints();
 
 			doSleep(maxSleep);
 
 			if (isFinished()) {
 				break;
 			}
 
 			checkWaitCondition();
 		}
 
 		addSkatPointsToPlayerPoints(trickWinner);
 
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
 
 	private void informPlayersAboutCompletedTrick(final Trick trick) {
 		for (final Player currPosition : Player.getOrderedList()) {
 			// inform all players
 			// cloning of trick information to prevent manipulation by
 			// player
 			try {
 				getPlayerInstance(currPosition)
 						.showTrick((Trick) trick.clone());
 			} catch (final CloneNotSupportedException e) {
 				log.warn("should not happen: " + e.getClass() + " - " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
 				getPlayerInstance(currPosition).showTrick(trick);
 			}
 		}
 	}
 
 	private void informPlayersAboutNewTrick(final Trick trick) {
 		for (final Player currPosition : Player.getOrderedList()) {
 			// inform all players
 			// cloning of trick information to prevent manipulation by
 			// player
 			try {
 				getPlayerInstance(currPosition).newTrick((Trick) trick.clone());
 			} catch (final CloneNotSupportedException e) {
 				log.warn("should not happen: " + e.getClass() + " - " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
 				getPlayerInstance(currPosition).newTrick(trick);
 			}
 		}
 	}
 
 	private Player getTrickForeHand(int trickNo) {
 		Player trickForeHand = null;
 		if (trickNo == 0) {
 			// first trick
 			trickForeHand = Player.FOREHAND;
 		} else {
 			// get trick winner as fore hand of next trick
 			trickForeHand = data.getTrickWinner(trickNo - 1);
 		}
 		return trickForeHand;
 	}
 
 	private void logPlayerPoints() {
 		log.debug("Points: forehand: " + data.getPlayerPoints(Player.FOREHAND) + //$NON-NLS-1$
 				" middlehand: " //$NON-NLS-1$
 				+ data.getPlayerPoints(Player.MIDDLEHAND) + " rearhand: " //$NON-NLS-1$
 				+ data.getPlayerPoints(Player.REARHAND));
 	}
 
 	private void addSkatPointsToPlayerPoints(Player lastTrickWinner) {
 		log.debug("Skat: " + data.getSkat());
 		if (data.getGameType() == GameType.RAMSCH) {
 			if (JSkatOptions.instance().getRamschSkatOwner() == RamschSkatOwner.LAST_TRICK) {
 				if (lastTrickWinner != null) {
 					log.debug("Skat cards (" + data.getSkat().getTotalValue() + " points) are added to player @ " //$NON-NLS-1$ //$NON-NLS-2$
 							+ lastTrickWinner + " (= last trick)"); //$NON-NLS-1$
 					data.addPlayerPoints(lastTrickWinner, data.getSkat()
 							.getTotalValue());
 				} else {
 					log.warn("Skat cards cannot be added to winner of final trick - trick winner is unknown"); //$NON-NLS-1$
 				}
 			}
 		} else {
 			// for all the other games, points to the declarer
 			data.addPlayerPoints(data.getDeclarer(), data.getSkat()
 					.getTotalValue());
 		}
 		logPlayerPoints();
 	}
 
 	private void playCard(final Trick trick, final Player trickForeHand,
 			final Player currPlayer) {
 
 		Card card = null;
 		final JSkatPlayer skatPlayer = getPlayerInstance(currPlayer);
 
 		boolean cardAccepted = false;
 		boolean aiPlayerPlayedSchwarz = false;
 
 		while (!cardAccepted && !aiPlayerPlayedSchwarz) {
 
 			try {
 				// ask player for the next card
 				card = skatPlayer.playCard();
 			} catch (final Exception exp) {
 				log.error("Exception thrown by player " + skatPlayer + " playing " + currPlayer + ": " + exp); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 
 			log.debug(card + " " + data); //$NON-NLS-1$
 
 			if (card == null) {
 
 				log.error("Player is fooling!!! Did not play a card!"); //$NON-NLS-1$
 
 				if (skatPlayer.isHumanPlayer()) {
 					view.showCardNotAllowedMessage(card);
 				} else {
 					// TODO create option for switching playing schwarz on/off
 					aiPlayerPlayedSchwarz = true;
 				}
 
 			} else if (!playerHasCard(currPlayer, card)) {
 
 				log.error("Player (" + getPlayerInstance(currPlayer) + ") is fooling!!! Doesn't have card " + card + "!"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
 
 				if (skatPlayer.isHumanPlayer()
 						|| JSkatOptions.instance().isCheatDebugMode()) {
 					view.showCardNotAllowedMessage(card);
 				} else {
 					// TODO create option for switching playing schwarz on/off
 					aiPlayerPlayedSchwarz = true;
 				}
 
 			} else if (!rules
 					.isCardAllowed(data.getGameType(), trick.getFirstCard(),
 							data.getPlayerCards(currPlayer), card)) {
 
 				log.error("Player " + skatPlayer.getClass().toString() + " card not allowed: " + card + " game type: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						+ data.getGameType() + " first trick card: " //$NON-NLS-1$
 						+ trick.getFirstCard() + " player cards: " //$NON-NLS-1$
 						+ data.getPlayerCards(currPlayer));
 
 				if (skatPlayer.isHumanPlayer()
 						|| JSkatOptions.instance().isCheatDebugMode()) {
 					view.showCardNotAllowedMessage(card);
 				} else {
 					// TODO create option for switching playing schwarz on/off
 					aiPlayerPlayedSchwarz = true;
 				}
 
 			} else {
 
 				cardAccepted = true;
 			}
 		}
 
 		if (card == null || aiPlayerPlayedSchwarz) {
 			// check schwarz
 			if (aiPlayerPlayedSchwarz && !cardAccepted) {
 				// end game immediately
 				data.getResult().setSchwarz(true);
 				if (data.getDeclarer().equals(currPlayer)) {
 					// declarer played schwarz
 					data.getResult().setWon(false);
 				} else {
 					// opponent played schwarz
 					data.getResult().setWon(true);
 				}
 				data.setGameState(GameState.PRELIMINARY_GAME_END);
 			}
 		} else {
 			// card was on players hand and is valid
 			data.removePlayerCard(currPlayer, card);
 			data.setTrickCard(currPlayer, card);
 
 			if (trick.getTrickNumberInGame() > 0
 					&& currPlayer.equals(trickForeHand)) {
 				// remove all cards from current trick panel first
 				view.clearTrickCards(tableName);
 
 				final Trick lastTrick = data.getTricks().get(
 						data.getTricks().size() - 2);
 
 				// set last trick cards
 				view.setLastTrick(tableName, lastTrick);
 			}
 
 			view.playTrickCard(tableName, currPlayer, card);
 
 			for (final JSkatPlayer playerInstance : player.values()) {
 				// inform all players
 				// cloning of card is not neccessary, because Card is immutable
 				playerInstance.cardPlayed(currPlayer, card);
 			}
 
 			log.debug("playing card " + card); //$NON-NLS-1$
 		}
 	}
 
 	private JSkatPlayer getPlayerInstance(final Player position) {
 
 		return player.get(position);
 	}
 
 	/**
 	 * Checks whether a player has the card on it's hand or not
 	 * 
 	 * @param card
 	 *            Card to check
 	 * @return TRUE if the card is on player's hand
 	 */
 	private boolean playerHasCard(final Player skatPlayer, final Card card) {
 
 		boolean result = false;
 
 		log.debug("Player has card: player cards: " + data.getPlayerCards(skatPlayer) + " card to check: " + card); //$NON-NLS-1$
 
 		for (final Card handCard : data.getPlayerCards(skatPlayer)) {
 
 			if (handCard.equals(card)) {
 
 				result = true;
 			}
 		}
 
 		return result;
 	}
 
 	private boolean isFinished() {
 
 		return data.getGameState() == GameState.PRELIMINARY_GAME_END
 				|| data.getGameState() == GameState.GAME_OVER;
 	}
 
 	private void calculateGameValue() {
 
 		log.debug("Calculate game value"); //$NON-NLS-1$
 
 		// FIXME (jan 07.12.2010) don't let a data class calculate it's values
 		data.calcResult();
 
 		log.debug("game value=" + data.getResult() + ", bid value=" //$NON-NLS-1$ //$NON-NLS-2$
 				+ data.getBidValue());
 
 		log.debug("Final game result: lost:" + data.isGameLost() + //$NON-NLS-1$
 				" game value: " + data.getResult()); //$NON-NLS-1$
 
 		int declarerPoints = 0;
 		int opponentPoints = 0;
 		for (Trick trick : data.getTricks()) {
 			String logString = "Trick: " + (trick.getTrickNumberInGame() + 1);
 			if (trick.getTrickWinner().equals(data.getDeclarer())) {
 				declarerPoints += trick.getValue();
 				logString += " Declarer wins: ";
 			} else {
 				opponentPoints += trick.getValue();
 				logString += " Opponents win: ";
 			}
 			logString += trick.getValue() + " -> " + declarerPoints + "/"
 					+ opponentPoints;
 			log.debug(logString);
 		}
 		log.debug("Final result: " + data.getDeclarerScore() + "/"
 				+ data.getOpponentScore());
 
 		for (final JSkatPlayer playerInstance : player.values()) {
 			playerInstance.setGameSummary(data.getGameSummary());
 			playerInstance.finalizeGame();
 		}
 
 		doSleep(maxSleep);
 	}
 
 	private void doSleep(final int milliseconds) {
 
 		try {
 			sleep(milliseconds);
 		} catch (final InterruptedException e) {
 			log.warn("sleep was interrupted..."); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Sets the view for the game
 	 * 
 	 * @param newView
 	 */
 	public void setView(final JSkatView newView) {
 
 		view = newView;
 	}
 
 	/**
 	 * Sets a new logger for the skat game
 	 * 
 	 * @param newLogger
 	 *            New logger
 	 */
 	public void setLogger(final Logger newLogger) {
 		log = newLogger;
 	}
 
 	/**
 	 * Sets the cards from outside
 	 * 
 	 * @param newDeck
 	 *            Card deck
 	 */
 	public void setCardDeck(final CardDeck newDeck) {
 
 		deck = newDeck;
 	}
 
 	/**
 	 * Sets the game announcement from the outside
 	 * 
 	 * @param ann
 	 *            Game announcement
 	 */
 	public void setGameAnnouncement(final GameAnnouncement ann) {
 
 		data.setAnnouncement(ann);
 		rules = SkatRuleFactory.getSkatRules(data.getGameType());
 		view.setGameAnnouncement(tableName, data.getDeclarer(), ann);
 
 		// inform all players
 		for (final JSkatPlayer playerInstance : player.values()) {
 			playerInstance.startGame(data.getDeclarer(), ann);
 		}
 
 		log.debug(".setGameAnnouncement(): " + data.getAnnoucement() + " by " + data.getDeclarer() + ", rules=" + rules); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	}
 
 	/**
 	 * Sets the game state from outside
 	 * 
 	 * @param newState
 	 *            Game state
 	 */
 	public void setGameState(final GameState newState) {
 
 		data.setGameState(newState);
 
 		if (view != null) {
 
 			view.setGameState(tableName, newState);
 
 			if (newState == GameState.GAME_START) {
 
 				view.clearTable(tableName);
 
 			} else if (newState == GameState.GAME_OVER) {
 
 				view.addGameResult(tableName, getGameSummary());
 
 				view.showCards(tableName, data.getCardsAfterDiscard());
 			}
 		}
 	}
 
 	/**
 	 * Sets the single player from outside
 	 * 
 	 * @param declarer
 	 *            Declarer
 	 */
 	public void setDeclarer(final Player declarer) {
 
 		data.setDeclarer(declarer);
 		view.setDeclarer(tableName, declarer);
 	}
 
 	/**
 	 * Gets the single player
 	 * 
 	 * @return Single player
 	 */
 	public Player getDeclarer() {
 		return data.getDeclarer();
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
 	public void setMaxSleep(final int newMaxSleep) {
 
 		maxSleep = newMaxSleep;
 	}
 
 	/**
 	 * Gets the game result
 	 * 
 	 * @return Game result
 	 */
 	public SkatGameResult getGameResult() {
 
 		return data.getGameResult();
 	}
 
 	/**
 	 * Gets a summary of the game
 	 * 
 	 * @return Game summary
 	 */
 	public GameSummary getGameSummary() {
 		return data.getGameSummary();
 	}
 
 	/**
 	 * @see Object#toString()
 	 */
 	@Override
 	public String toString() {
 
 		return data.getGameState().toString();
 	}
 
 	/**
 	 * Gets the game announcement
 	 * 
 	 * @return Game announcement
 	 */
 	public GameAnnouncement getGameAnnouncement() {
 		return data.getAnnoucement();
 	}
 
 	/**
 	 * Gets the game state
 	 * 
 	 * @return Game state
 	 */
 	public GameState getGameState() {
 		return data.getGameState();
 	}
 }
