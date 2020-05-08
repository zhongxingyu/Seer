 package bots.mctsbot.ai.bots.bot.gametree.mcts;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.swt.widgets.Display;
 
 import bots.mctsbot.ai.bots.bot.Bot;
 import bots.mctsbot.ai.bots.bot.BotFactory;
 import bots.mctsbot.ai.bots.bot.gametree.mcts.listeners.MCTSListener;
 import bots.mctsbot.ai.bots.bot.gametree.mcts.listeners.SWTTreeListener;
 import bots.mctsbot.ai.bots.bot.gametree.mcts.nodes.MCTSShowdownRollOutNode;
 import bots.mctsbot.ai.bots.bot.gametree.mcts.strategies.backpropagation.SampleWeightedBackPropStrategy;
 import bots.mctsbot.ai.bots.bot.gametree.mcts.strategies.selection.MaxValueSelector;
 import bots.mctsbot.ai.bots.bot.gametree.mcts.strategies.selection.SamplingSelector;
 import bots.mctsbot.ai.bots.bot.gametree.mcts.strategies.selection.SamplingToFunctionSelector;
 import bots.mctsbot.ai.bots.bot.gametree.mcts.strategies.selection.UCTSelector;
 import bots.mctsbot.ai.bots.bot.gametree.search.expander.sampling.Sampler;
 import bots.mctsbot.ai.bots.bot.gametree.search.expander.sampling.SmartSampler;
 import bots.mctsbot.ai.opponentmodels.weka.WekaRegressionModelFactory;
 import bots.mctsbot.client.common.GameStateContainer;
 import bots.mctsbot.client.common.gamestate.DetailedHoldemTableState;
 import bots.mctsbot.client.common.gamestate.GameState;
 import bots.mctsbot.client.common.gamestate.modifiers.BetState;
 import bots.mctsbot.client.common.gamestate.modifiers.BlindState;
 import bots.mctsbot.client.common.gamestate.modifiers.CallState;
 import bots.mctsbot.client.common.gamestate.modifiers.FoldState;
 import bots.mctsbot.client.common.gamestate.modifiers.NewCommunityCardsState;
 import bots.mctsbot.client.common.gamestate.modifiers.NewDealState;
 import bots.mctsbot.client.common.gamestate.modifiers.NewPocketCardsState;
 import bots.mctsbot.client.common.gamestate.modifiers.NextPlayerState;
 import bots.mctsbot.client.common.gamestate.modifiers.RaiseState;
 import bots.mctsbot.client.common.gamestate.modifiers.ShowHandState;
 import bots.mctsbot.common.api.lobby.holdemtable.event.BetEvent;
 import bots.mctsbot.common.api.lobby.holdemtable.event.BlindEvent;
 import bots.mctsbot.common.api.lobby.holdemtable.event.CallEvent;
 import bots.mctsbot.common.api.lobby.holdemtable.event.FoldEvent;
 import bots.mctsbot.common.api.lobby.holdemtable.event.NewCommunityCardsEvent;
 import bots.mctsbot.common.api.lobby.holdemtable.event.NewDealEvent;
 import bots.mctsbot.common.api.lobby.holdemtable.event.NextPlayerEvent;
 import bots.mctsbot.common.api.lobby.holdemtable.event.RaiseEvent;
 import bots.mctsbot.common.api.lobby.holdemtable.event.ShowHandEvent;
 import bots.mctsbot.common.api.lobby.holdemtable.holdemplayer.context.RemoteHoldemPlayerContext;
 import bots.mctsbot.common.api.lobby.holdemtable.holdemplayer.event.NewPocketCardsEvent;
 import bots.mctsbot.common.elements.player.PlayerId;
 import bots.mctsbot.common.elements.player.SeatedPlayer;
 import bots.mctsbot.common.elements.player.ShowdownPlayer;
 import bots.mctsbot.common.elements.table.DetailedHoldemTable;
 import bots.mctsbot.common.elements.table.SeatId;
 import bots.mctsbot.common.elements.table.TableConfiguration;
 import bots.mctsbot.common.elements.table.TableId;
 
 import com.biotools.meerkat.Action;
 import com.biotools.meerkat.Card;
 import com.biotools.meerkat.GameInfo;
 import com.biotools.meerkat.Hand;
 import com.biotools.meerkat.Holdem;
 import com.biotools.meerkat.Player;
 import com.biotools.meerkat.util.Preferences;
 
 public class MCTSMeerkatBot implements Player {
 	private int ourSeat; // our seat for the current hand
 	private Card c1, c2; // our hole cards
 	private GameInfo gi; // general game information
 	private Preferences prefs; // the configuration options for this bot
 
 	private Bot mctsBot;
 	private BotFactory botFactory;
 	private GameStateContainer gameStateContainer;
 	private Map<Integer, PlayerId> playerIdCache = new HashMap<Integer, PlayerId>();
 	private MyPlayerContext playerContext = new MyPlayerContext();
 
 	private static Display myDisplay;
 
 	public MCTSMeerkatBot() throws Exception {
 
 		gameStateContainer = new GameStateContainer(null);
 
 	}
 
 	/**
 	 * An event called to tell us our hole cards and seat number
 	 * @param c1 your first hole card
 	 * @param c2 your second hole card
 	 * @param seat your seat number at the table
 	 */
 	public void holeCards(Card c1, Card c2, int seat) {
 		this.c1 = c1;
 		this.c2 = c2;
 		this.ourSeat = seat;
 
 		if (mctsBot == null) {
 			seatUpBot(seat);
 		}
 
 		Hand myHand = new Hand();
 		myHand.addCard(c1);
 		myHand.addCard(c2);
 		gameStateContainer.setGameState(new NewPocketCardsState(gameStateContainer.getGameState(), new PlayerId(seat), new NewPocketCardsEvent(myHand)));
 
 	}
 
 	private void seatUpBot(int seat) {
 		final List<MCTSListener.Factory> listeners = new ArrayList<MCTSListener.Factory>();
 		if (prefs.getBooleanPreference("SHOW_GAMETREE_GUI", false)) {
 			if (myDisplay == null) {
 				setupSWTDisplayThread();
 			}
 			listeners.add(new SWTTreeListener.Factory(Display.getDefault()));
 		}
 
 		try {
 			Sampler s = new SmartSampler();
 			boolean overwrite = false;
 
 			botFactory = new MCTSBotFactory("MCTSBot", WekaRegressionModelFactory.createForZip(overwrite,
 					"bots/mctsbot/ai/opponentmodels/weka/models/model1.zip"), new SamplingToFunctionSelector(50, new UCTSelector(2000)),
 					new SamplingSelector(), new MaxValueSelector(), new MCTSShowdownRollOutNode.Factory(), new SampleWeightedBackPropStrategy.Factory(), s,
 					250, listeners.toArray(new MCTSListener.Factory[0]));
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 
 		mctsBot = botFactory.createBot(new PlayerId(seat), gameStateContainer, playerContext);
 	}
 
 	private void setupSWTDisplayThread() {
 		Thread displayThread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (MCTSMeerkatBot.this) {
 					myDisplay = new Display();
 					MCTSMeerkatBot.this.notify();
 				}
 				while (!myDisplay.isDisposed()) {
 					if (!myDisplay.readAndDispatch()) {
 						myDisplay.sleep();
 					}
 				}
 			}
 		});
 		displayThread.start();
 		synchronized (MCTSMeerkatBot.this) {
 			if (myDisplay == null) {
 				try {
 					this.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Requests an Action from the player
 	 * Called when it is the Player's turn to act.
 	 */
 	public Action getAction() {
 		GameState gameState = gameStateContainer.getGameState();
 		PlayerId playerId = playerIdCache.get(Integer.valueOf(ourSeat));
 
 		gameState = new NextPlayerState(gameState, new NextPlayerEvent(playerId));
 		gameStateContainer.setGameState(gameState);
 		mctsBot.doNextAction();
 		return playerContext.nextAction;
 
 	}
 
 	/**
 	 * Get the current settings for this bot.
 	 */
 	public Preferences getPreferences() {
 		return prefs;
 	}
 
 	/**
 	 * Load the current settings for this bot.
 	 */
 	public void init(Preferences playerPrefs) {
 		this.prefs = playerPrefs;
 	}
 
 	/**
 	 * A new betting round has started.
 	 */
 	public void stageEvent(int stage) {
 		GameState gameState = gameStateContainer.getGameState();
 		Hand newCards = new Hand();
 		switch (stage) {
 		case Holdem.FLOP:
 			newCards.addCard(gi.getBoard().getCard(1));
 			newCards.addCard(gi.getBoard().getCard(2));
 			newCards.addCard(gi.getBoard().getCard(3));
 			break;
 
 		case Holdem.TURN:
 			newCards.addCard(gi.getBoard().getCard(4));
 			break;
 		case Holdem.RIVER:
 			newCards.addCard(gi.getBoard().getCard(5));
 			break;
 
 		}
 		gameState = new NewCommunityCardsState(gameState, new NewCommunityCardsEvent(newCards));
 
 		gameStateContainer.setGameState(gameState);
 	}
 
 	/**
 	 * A showdown has occurred.
 	 * @param pos the position of the player showing
 	 * @param c1 the first hole card shown
 	 * @param c2 the second hole card shown
 	 */
 	public void showdownEvent(int seat, Card c1, Card c2) {
 		GameState gameState = gameStateContainer.getGameState();
 		PlayerId playerId = playerIdCache.get(Integer.valueOf(seat));
 
 		Hand showDownHand = new Hand();
 		showDownHand.addCard(c1);
 		showDownHand.addCard(c2);
 		ShowdownPlayer showdownPlayer = new ShowdownPlayer(playerId, showDownHand, "");
 		gameState = new ShowHandState(gameState, new ShowHandEvent(showdownPlayer));
 
 		gameStateContainer.setGameState(gameState);
 	}
 
 	/**
 	 * A new game has been started.
 	 * @param gi the game stat information
 	 */
 	public void gameStartEvent(GameInfo gInfo) {
 		this.gi = gInfo;
 		List<SeatedPlayer> seatedPlayer = new ArrayList<SeatedPlayer>();
 		playerIdCache = new HashMap<Integer, PlayerId>();
 		for (int seat = 0; seat < gInfo.getNumSeats(); seat++) {
 			if (gInfo.getPlayer(seat) != null) {
 				PlayerId player = new PlayerId(seat);
 				playerIdCache.put(Integer.valueOf(seat), player);
				seatedPlayer.add(new SeatedPlayer(player, new SeatId(seat), gInfo.getPlayerName(seat), (int) (gInfo.getBankRoll(seat) * 100), 0, true, true));
 			}
 		}
 
		TableConfiguration tableConfiguration = new TableConfiguration((int) (gi.getBigBlindSize() * 100));
 		DetailedHoldemTable table = new DetailedHoldemTable(new TableId(1), "Test", seatedPlayer, true, tableConfiguration);
 		GameState gameState = new DetailedHoldemTableState(table);
 		gameState = new NewDealState(new NewDealEvent(seatedPlayer, new PlayerId(gInfo.getButtonSeat())), gameState);
 		gameStateContainer.setGameState(gameState);
 
 	}
 
 	/**
 	 * An event sent when all players are being dealt their hole cards
 	 */
 	public void dealHoleCardsEvent() {
 	}
 
 	/**
 	 * An action has been observed. 
 	 */
 	public void actionEvent(int pos, Action act) {
 		GameState gameState = gameStateContainer.getGameState();
 		PlayerId playerId = playerIdCache.get(Integer.valueOf(pos));
		int amount = (int) (act.getAmount() * 100);
		int toCall = (int) (act.getToCall() * 100);
 
 		if (pos != ourSeat && !(act.getType() == Action.SMALL_BLIND || act.getType() == Action.BIG_BLIND)) {
 			// no nextPlayerState for
 			// - ourself (this state was already set in doNextAction)
 			// - the blinds
 			gameState = new NextPlayerState(gameState, new NextPlayerEvent(playerId));
 		}
 
 		switch (act.getType()) {
 		case Action.SMALL_BLIND:
 		case Action.BIG_BLIND:
 			gameState = new BlindState(gameState, new BlindEvent(playerId, amount));
 			break;
 		case Action.BET:
 			gameState = new BetState(gameState, new BetEvent(playerId, amount));
 			break;
 		case Action.CALL:
 			gameState = new CallState(gameState, new CallEvent(playerId, toCall));
 			break;
 		case Action.RAISE:
 			gameState = new RaiseState(gameState, new RaiseEvent(playerId, amount, amount + toCall));
 			break;
 		case Action.FOLD:
 			gameState = new FoldState(gameState, new FoldEvent(playerId));
 			break;
 		}
 
 		gameStateContainer.setGameState(gameState);
 	}
 
 	/**
 	 * The game info state has been updated
 	 * Called after an action event has been fully processed
 	 */
 	public void gameStateChanged() {
 	}
 
 	/**
 	 * The hand is now over. 
 	 */
 	public void gameOverEvent() {
 	}
 
 	/**
 	 * A player at pos has won amount with the hand handName
 	 */
 	public void winEvent(int pos, double amount, String handName) {
 	}
 
 	class MyPlayerContext implements RemoteHoldemPlayerContext {
 		Action nextAction = Action.sitout();
 
 		@Override
 		public void betOrRaise(int amount) {
 			if (gi.getAmountToCall(ourSeat) > 0) {
				nextAction = Action.raiseAction(gi, amount / 100D);
 			} else {
				nextAction = Action.betAction(amount / 100D);
 			}
 		}
 
 		@Override
 		public void checkOrCall() {
 			if (gi.getAmountToCall(ourSeat) > 0) {
 				nextAction = Action.callAction(gi);
 			} else {
 				nextAction = Action.checkAction();
 			}
 		}
 
 		@Override
 		public void fold() {
 			nextAction = Action.foldAction(gi);
 		}
 
 		@Override
 		public String toString() {
 			return nextAction.toString();
 		}
 
 	}
 
 }
