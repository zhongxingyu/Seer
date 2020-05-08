 package edu.txstate.hearts.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import javax.swing.JButton;
 import javax.swing.JMenuItem;
 import edu.txstate.hearts.gui.AchievementsDisplay;
 import edu.txstate.hearts.gui.ConfigurationUI;
 import edu.txstate.hearts.gui.HeartsUI;
 import edu.txstate.hearts.gui.HeartsUI.Position;
 import edu.txstate.hearts.gui.PointsDisplay;
 import edu.txstate.hearts.gui.RulesWindow;
 import edu.txstate.hearts.model.*;
 import edu.txstate.hearts.model.Card.Face;
 import edu.txstate.hearts.model.Card.Suit;
 
 /**
  * Hearts Controller
  * 
  * @author Neil Stickels, I Gede Sutapa, Jonathan Shelton 
  */
 public class Hearts implements ActionListener
 {
 	public enum CardAction
 	{
 		Passing, Playing, Idle
 	}
 
 	public enum Passing
 	{
 		Left, Right, Front, Stay
 	}
 
 	public final static boolean silent = false;
 	
 	private final int maxTurnsPlayed = 13;
 	private final int maxCardsPerTurn = 4;
 	
 	private int cardsSelectedToPassCount;
 	private CardAction cardAction;
 	private ConfigurationUI configurationUI;
 	private Deck deck;
 	private List<Player> players;
 	private List<Card> cardsSelectedToPass;
 	private List<JButton> buttonCardsSelectedToPass;
 	private boolean notifyHeartsBroken; // implement notification "Hearts has been broken"
 	private Passing passing;
 	private int endScore;
 	private boolean showOpponentCards = true;
 	private User user; // for achievements
 	private HeartsUI heartsUI;
 	private PointsDisplay pointDisplay;
 	private boolean startThePartyAchieve = false;
 	private Boolean achievementNotify = false;
 	private int currentPlayerIndexThisTurn;
 	private int currentTurn;
 	private List<Card> currentCardsPlayed;
 	private boolean currentTurnFirst;
 	private Card currentTurnFirstPlayedCard;
 	private Card currentTurnHighestValueCard;
 	private Player currentTurnPlayerWithHighestValue;
 	private boolean heartsBroken;
 
 	/**
 	 * Constructor
 	 *
 	 */
 	public Hearts()
 	{
 	}
 
 	/**
 	 * @return the players
 	 */
 	public List<Player> getPlayers() {
 		return players;
 	}
 
 	/**
 	 * @return the endScore
 	 */
 	public int getEndScore() {
 		return this.endScore;
 	}
 
 	/**
 	 * Add UI to be used for hearts game
 	 *
 	 * @param heartsUI	UI object
 	 */
 	public void addUI(HeartsUI heartsUI)
 	{
 		this.heartsUI = heartsUI;
 	}
 
 	/**
 	 * The beginning of hearts game
 	 *
 	 */
 	public void run()
 	{
 		this.configurationUI = new ConfigurationUI(this);
 		configurationUI.showDialog();
 	}
 
 	/**
 	 * Initializing when the game starts
 	 *
 	 * @param playerName		player name
 	 * @param endScore			score to determine when the game ends
 	 * @param levelOfDifficulty	level of difficulty
 	 */
 	public void initialize(String playerName, int endScore, String levelOfDifficulty)
 	{
 		this.passing = Passing.Left; // initial
 		this.endScore = endScore; // default
 		this.cardsSelectedToPass = new ArrayList<Card>(3);
 		this.buttonCardsSelectedToPass = new ArrayList<JButton>(3);
 
 		this.players = new ArrayList<Player>(4);
 
 		Player player1 = new User(playerName, 0);
 		Player player2;
 		Player player3;
 		Player player4;
 
 		user = (User) player1; // for achievements
 
 		if (levelOfDifficulty.equalsIgnoreCase("Easy"))
 		{
 			player2 = new AgentGoofy("Neil", 1);
 			player3 = new AgentGoofy("Jonathan", 2);
 			player4 = new AgentDetermined("Maria", 3);
 		}
 		else if (levelOfDifficulty.equalsIgnoreCase("Medium"))
 		{
 			player2 = new AgentGoofy("Neil", 1);
 			player3 = new AgentAggressive("Jonathan", 2);
 			player4 = new AgentDetermined("Maria", 3);
 		}
 		else
 		{
 			player2 = new AgentAggressive("Neil", 1);
 			player3 = new AgentAggressive("Jonathan", 2);
 			player4 = new AgentDetermined("Maria", 3);
 		}
 
 		players.add(player1);
 		players.add(player2);
 		players.add(player3);
 		players.add(player4);
 	}
 
 	/**
 	 * Shuffle cards in a deck
 	 *
 	 */
 	private void shuffleCards()
 	{
 		deck.shuffleCards();
 
 		if (!silent)
 		{
 			deck.printCards();
 			System.out.println("");
 		}
 	}
 
 	/**
 	 * Deal cards to players
 	 *
 	 */
 	private void dealCards()
 	{
 		// deal cards
 		for (int i = 0; i < 13; i++)
 		{
 			for (int j = 0; j < players.size(); j++)
 			{
 				players.get(j).addCard(deck.dealCard());
 			}
 		}
 
 		// print each player cards
 		for (int i = 0; i < players.size(); i++)
 		{
 			Player player = players.get(i);
 			player.sortCards();
 			if (!silent)
 			{
 				System.out.println(player.getName());
 				player.printHand();
 				System.out.println("");
 				System.out.println("");
 			}
 		}
 	}
 
 	/**
 	 * Passing the selected cards
 	 *
 	 */
 	private void passingCards()
 	{
 		if (this.passing == Passing.Stay)
 			return;
 
 		List<Card> p0CardsToPass = this.cardsSelectedToPass;
 		for (int i = 0; i < p0CardsToPass.size(); i++)
 		{
 			this.players.get(0).getHand().remove(p0CardsToPass.get(i));
 		}
 
 		achievementPassingTheBuck(p0CardsToPass);
 
 		List<Card> p1CardsToPass = this.players.get(1).getCardsToPass(this.passing);
 		List<Card> p2CardsToPass = this.players.get(2).getCardsToPass(this.passing);
 		List<Card> p3CardsToPass = this.players.get(3).getCardsToPass(this.passing);
 
 		if (this.passing == Passing.Left)
 		{
 			this.players.get(0).addCards(p3CardsToPass);
 			this.players.get(1).addCards(p0CardsToPass);
 			this.players.get(2).addCards(p1CardsToPass);
 			this.players.get(3).addCards(p2CardsToPass);
 		}
 		else if (this.passing == Passing.Right)
 		{
 			this.players.get(0).addCards(p1CardsToPass);
 			this.players.get(1).addCards(p2CardsToPass);
 			this.players.get(2).addCards(p3CardsToPass);
 			this.players.get(3).addCards(p0CardsToPass);
 		}
 		else if (this.passing == Passing.Front)
 		{
 			this.players.get(0).addCards(p2CardsToPass);
 			this.players.get(1).addCards(p3CardsToPass);
 			this.players.get(2).addCards(p0CardsToPass);
 			this.players.get(3).addCards(p1CardsToPass);
 		}
 		if (!silent)
 			System.out.println("=====After passing cards to the " + this.passing.toString() + "=====");
 		// print each player cards
 		for (int i = 0; i < players.size(); i++)
 		{
 			Player player = players.get(i);
 			player.sortCards();
 			if (!silent)
 			{
 				System.out.println(player.getName());
 				player.printHand();
 				System.out.println("");
 				System.out.println("");
 			}
 		}
 	}
 
 	/**
 	 * Running the game
 	 *
 	 */
 	private void runGame()
 	{
 		this.heartsUI.setUI(this.showOpponentCards);
 		this.heartsUI.setPlayers(this.players);
 		this.heartsUI.showDialog();
 
 		this.runNextGame();
 	}
 
 	/**
 	 * Initialize method for before the game starts
 	 *
 	 */
 	private void runNextGame()
 	{
 		this.deck = new Deck();
 		shuffleCards();
 		dealCards();
 
 		this.cardsSelectedToPass.clear();
 		this.buttonCardsSelectedToPass.clear();
 
 		this.cardAction = CardAction.Idle;
 		this.cardsSelectedToPassCount = 0;
 		this.heartsUI.displayCards();
 		this.heartsUI.setPassButtonVisible(false);
 
 		if (this.passing != Passing.Stay)
 		{
 			this.cardAction = CardAction.Passing;
 			this.heartsUI.setPassButtonVisible(true);
 		}
 		else
 		{
 			this.cardAction = CardAction.Playing;
 			this.heartsUI.setPassButtonVisible(false);
 			this.initializeFirstTurn();
 		}
 	}
 
 	/**
 	 * Initialize game before first turn
 	 *
 	 */
 	private void initializeFirstTurn()
 	{
 		// clear player's in play cards
 		for (int i = 0; i < players.size(); i++)
 			players.get(i).clearInPlayCards();
 
 		this.currentTurn = 0;
 		this.currentPlayerIndexThisTurn = this.findPlayerToStart();
 		this.initializeTurn();
 
 		// user move
 		this.heartsUI.ShowBalloonTip("First turn somewhere");
 		if (this.currentPlayerIndexThisTurn == 0)
 		{
 			startThePartyAchieve = true;
 			// do nothing, wait for user move
 		}
 		else
 		{
 			this.runAITurns();
 		}
 	}
 
 	/**
 	 * Run AI turns as long as it is not the user's turn
 	 *
 	 */
 	private void runAITurns()
 	{
 		// while the next player to move is AI and this turn is not finished yet
 		while (this.currentPlayerIndexThisTurn != 0 && this.currentCardsPlayed.size() != this.maxCardsPerTurn)
 		{
 			// AI move
 			this.runTurn(this.currentPlayerIndexThisTurn);
 		}
 
 		if (this.currentCardsPlayed.size() == this.maxCardsPerTurn)
 		{
 			this.summarizeTurn();
 			this.currentTurn++;
 			this.initializeTurn();
 			this.nextTurn();
 		}
 	}
 
 	/**
 	 * Finalizing player turn after each move
 	 *
 	 * @param card		played card
 	 * @param position	player position
 	 * @param player	player object
 	 */
 	private void finalizePlayerTurn(Card card, Position position, Player player)
 	{
 		JButton cardButton = this.heartsUI.findButton(position, card);
 		this.heartsUI.removeButton(cardButton, position);
 		this.heartsUI.showPlayedCardButton(position, card);
 
 		try
 		{
 			Thread.sleep(500);
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 
 		this.currentCardsPlayed.add(card);
 		if (this.currentTurnFirst)
 		{
 			this.currentTurnFirstPlayedCard = card;
 			this.currentTurnHighestValueCard = card;
 			this.currentTurnPlayerWithHighestValue = player;
 		}
 		else
 		{
 			// compare with highest value
 			if (card.getSuit() == this.currentTurnFirstPlayedCard.getSuit())
 			{
 				if (card.getFace().ordinal() > this.currentTurnHighestValueCard.getFace().ordinal())
 				{
 					this.currentTurnHighestValueCard = card;
 					this.currentTurnPlayerWithHighestValue = player;
 				}
 			}
 		}
 
 		this.currentTurnFirst = false;
 		if (!silent)
 			System.out.println("Player " + player.getName() + " played " + card);
 
 		// notify that hearts has been broken (once per round)
 		notifyHeartsBroken(this.currentCardsPlayed, player);
 
 		// add that card to each player's in play cards
 		for (int i = 0; i < players.size(); i++)
 			players.get(i).addInPlayCards(card);
 
 		this.currentPlayerIndexThisTurn++;
 		this.currentPlayerIndexThisTurn %= 4;
 	}
 
 	/**
 	 * Run turn
 	 *
 	 * @param playerNum	player index
 	 */
 	private void runTurn(int playerNum)
 	{
 		Card card = null;
 
 		Player player = players.get(playerNum);
 		Position position = this.getPlayerPosition(playerNum);
 
 		card = player.playCard(this.currentCardsPlayed, this.heartsBroken, (this.currentTurnFirst && this.currentTurn == 0));
 
 		// Give achievement if user has played correct card, boolean is true
 		achievementStartTheParty();
 
 		this.finalizePlayerTurn(card, position, player);
 	}
 
 	/**
 	 * Summarizing turn after all players played their cards
 	 *
 	 */
 	private void summarizeTurn()
 	{
 		this.heartsUI.hideAllPlayedCardButtons();
 
 		// also add to the played cards
 		// add that card to each player's list of played cards
 		for (int i = 0; i < players.size(); i++)
 		{
 			boolean tookCards = players.get(i).equals(this.currentTurnPlayerWithHighestValue);
 			players.get(i).addPlayedCards(this.currentCardsPlayed, tookCards, this.currentTurn);
 
 			if (tookCards)
 			{
 				// add cards to the player with highest value
 				for (int j = 0; j < this.currentCardsPlayed.size(); j++)
 				{
 					this.currentTurnPlayerWithHighestValue.addTakenCard(this.currentCardsPlayed.get(j), !silent);
 
 					// figure out if hearts already broken
 					if (!this.heartsBroken && this.currentCardsPlayed.get(j).getSuit() == Suit.Hearts)
 						this.heartsBroken = true;
 				}
 			}
 		}
 
 		// find player to start next
 		this.currentPlayerIndexThisTurn = this.players.indexOf(this.currentTurnPlayerWithHighestValue);
 
 		// If player who won the cards is the user, give achievement
 		achievementHatTrick();
 	}
 
 	/**
 	 * Initializing turn before players start playing their cards
 	 *
 	 */
 	private void initializeTurn()
 	{
 		this.currentCardsPlayed = new ArrayList<Card>(4);
 
 		for (int i = 0; i < players.size(); i++)
 			players.get(i).clearInPlayCards();
 
 		this.currentTurnFirst = true;
 		this.currentTurnFirstPlayedCard = null;
 		this.currentTurnHighestValueCard = null;
 		this.currentTurnPlayerWithHighestValue = null;
 	}
 
 	/**
 	 * Determine what the next turn is, run AI if this is AI turns
 	 *
 	 */
 	private void nextTurn()
 	{
 		if (this.currentTurn == this.maxTurnsPlayed)
 		{
 			this.assignScoresToPlayers();
 
 			// check for highest point
 			Player playerWithHighestTotalPoints = null;
 			Player playerWithLowestTotalPoints = null;
 
 			for (int i = 0; i < players.size(); i++)
 			{
 				Player player = players.get(i);
 				if (playerWithHighestTotalPoints == null)
 				{
 					playerWithHighestTotalPoints = player;
 					playerWithLowestTotalPoints = player;
 				}
 				else if (playerWithHighestTotalPoints.getScore() < player.getScore())
 					playerWithHighestTotalPoints = player;
 				else if (playerWithLowestTotalPoints.getScore() > player.getScore())
 					playerWithLowestTotalPoints = player;
 			}
 
 			// determine if the game ends
 			if (playerWithHighestTotalPoints.getScore() >= this.endScore)
 			{
 				if (!silent)
 					System.out.println("Player " + playerWithHighestTotalPoints.getName() + " loses with total score of "
 							+ playerWithHighestTotalPoints.getScore());
 				if (!silent)
 					System.out.println("Player " + playerWithLowestTotalPoints.getName() + " wins with total score of "
 							+ playerWithLowestTotalPoints.getScore());
 
 				PointsDisplay pointDisplay = new PointsDisplay(this, this.players, playerWithLowestTotalPoints.getName());
 				this.pointDisplay = pointDisplay;
 				pointDisplay.showDialog();
				AgentAggressive.serializeThresholds();
 				return;
 			}
 
 			// figure out next passing
 			if (this.passing == Passing.Left)
 				this.passing = Passing.Right;
 			else if (this.passing == Passing.Right)
 				this.passing = Passing.Front;
 			else if (this.passing == Passing.Front)
 				this.passing = Passing.Stay;
 			else if (this.passing == Passing.Stay)
 				this.passing = Passing.Left;
 
 			this.runNextGame();
 		}
 		else
 		{
 			while (this.currentPlayerIndexThisTurn != 0 && this.currentCardsPlayed.size() != 4)
 			{
 				// AI move
 				this.runTurn(this.currentPlayerIndexThisTurn);
 			}
 		}
 	}
 
 	/**
 	 * This displays an instant notification when hearts have been broken
 	 * during a round. The notification also includes the name of the player
 	 * who broke hearts.
 	 * 
 	 * @param cardsPlayed	to monitor the cards on the table
 	 * @param player		player that broke the heart
 	 */
 	private void notifyHeartsBroken(List<Card> cardsPlayed, Player player)
 	{
 		if (notifyHeartsBroken == false)
 		{
 			for (int i = 0; i < cardsPlayed.size(); i++)
 			{
 				if (cardsPlayed.get(i).getSuit() == Suit.Hearts)
 				{
 					this.heartsUI.ShowBalloonTip("Hearts have been broken by " + player.getName());
 					if (!silent)
 						System.out.println("*****Hearts have been broken by " + player.getName() + "*****");
 					notifyHeartsBroken = true;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks internal requirements for Start The Party achievement and passes
 	 * any additional requirements to the Achievements class for further
 	 * processing. If the Achievements class returns true, this method will
 	 * then pass the name of the earned Achievement to the achievementNotify
 	 * method to process the message.
 	 * 
 	 */
 	private void achievementStartTheParty()
 	{
 		String achievement = "Start The Party";
 		if (startThePartyAchieve)
 		{
 			achievementNotify = user.getAchievements().StartTheParty(currentCardsPlayed);
 			if (achievementNotify)
 			{
 				achievementNotify(achievement);
 			}
 			startThePartyAchieve = false;
 		}
 	}
 
 	/**
 	 * Checks internal requirements for Passing The Buck achievement and passes
 	 * any additional requirements to the Achievements class for further
 	 * processing. If the Achievements class returns true, this method will
 	 * then pass the name of the earned Achievement to the achievementNotify
 	 * method to process the message.
 	 * 
 	 * @param cardsToPass	list of cards user has passed
 	 */
 	private void achievementPassingTheBuck(List<Card> cardsToPass)
 	{
 		String achievement = "Passing The Buck";
 		achievementNotify = user.getAchievements().PassingTheBuck(true, cardsToPass);
 		if (achievementNotify)
 		{
 			achievementNotify(achievement);
 		}
 	}
 
 	/**
 	 * Checks internal requirements for Hat Trick achievement and passes any
 	 * additional requirements to the Achievements class for further
 	 * processing. If the Achievements class returns true, this method will
 	 * then pass the name of the earned Achievement to the achievementNotify
 	 * method to process the message.
 	 * 
 	 */
 	private void achievementHatTrick()
 	{
 		String achievement = "Hat Trick";
 		if (this.currentPlayerIndexThisTurn == 0)
 		{
 			achievementNotify = user.getAchievements().HatTrick();
 			if (achievementNotify)
 			{
 				achievementNotify(achievement);
 			}
 		}
 	}
 
 	/**
 	 * Runs through all three achievements that need end game scores. The
 	 * method called within this method should return an integer that
 	 * corresponds to a particular achievement, 0 if no achievement earned. If
 	 * any number lower than 0 or higher than 3 is returned, it will throw an
 	 * exception as this shouldn't be possible.
 	 * 
 	 * @param scores
 	 */
 	private void achievementsEndGame(List<Integer> scores)
 	{
 		int earned = 0;
 		earned = user.getAchievements().endGameAchievements(scores.get(0));
 		try
 		{
 			if (earned == 1)
 			{
 				achievementNotify("Broken Heart");
 			}
 			else if (earned == 2)
 			{
 				achievementNotify("Shooting The Moon");
 			}
 			else if (earned == 3)
 			{
 				achievementNotify("Overshooting The Moon");
 			}
 			else if (earned == 0)
 			{
 				// Do nothing. No achievement earned
 			}
 			else
 			{
 				throw new Exception("Value of 'earned' shouldn't fall outside" + "the range 0-3");
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		earned = 0; // reset to zero to be safe
 	}
 
 	/**
 	 * Pops the notification tooltip for the user that displays the achievement
 	 * unlocked message for a particular achievement. This method MUST be
 	 * called from within the actionPerformed method in Hearts.java, or nested
 	 * in a method that can be traced back to actionPerformed, otherwise it
 	 * will throw a NullPointerException
 	 * 
 	 * @param achievement	the name of the achievement
 	 */
 	private void achievementNotify(String achievement)
 	{
 		this.heartsUI.ShowBalloonTip("Achievement Unlocked - " + achievement);
 		achievementNotify = false;
 	}
 
 	/**
 	 * Finding player to start the game
 	 *
 	 * @return	true index number of player who starts the game
 	 */
 	private int findPlayerToStart()
 	{
 		for (int i = 0; i < players.size(); i++)
 		{
 			Player p = players.get(i);
 			if (p.hasTwoOfClubs())
 				return i;
 		}
 		throw new RuntimeException("Couldn't find the 2 of clubs");
 	}
 
 	/**
 	 * Assign scores to players after each game
 	 *
 	 */
 	private void assignScoresToPlayers()
 	{
 		// see if a player collects all hearts and queen of spade
 		int numHearts;
 		int numQueenSpade;
 
 		List<Integer> scores = Arrays.asList(0, 0, 0, 0);
 		for (int i = 0; i < players.size(); i++)
 		{
 			numHearts = 0;
 			numQueenSpade = 0;
 
 			Player player = players.get(i);
 			List<Card> takenCards = player.getTakenCards();
 
 			for (int j = 0; j < takenCards.size(); j++)
 			{
 				Card takenCard = takenCards.get(j);
 				if (takenCard.getSuit() == Suit.Hearts)
 					numHearts++;
 				else if (takenCard.getSuit() == Suit.Spades && takenCard.getFace() == Face.Queen)
 					numQueenSpade++;
 			}
 
 			// if this player gets all the hearts & queen of spade
 			if (numHearts == 13 && numQueenSpade == 1)
 			{
 				for (int j = 0; j < players.size(); j++)
 				{
 					if (i != j)
 						scores.set(j, 26);
 					else
 						scores.set(j, 0);
 				}
 				break;
 			}
 			else
 			{
 				scores.set(i, numHearts + (numQueenSpade * 13));
 			}
 		}
 
 		// Run check for set of three achievements based on score --
 		achievementsEndGame(scores);
 
 		for (int i = 0; i < players.size(); i++)
 		{
 			Player player = players.get(i);
 			player.addScore(scores.get(i));
 			if (!silent)
 			{
 				System.out.println("This round, " + player.getName() + " collected " + scores.get(i) + " points ");
 				System.out.println("Total points for " + player.getName() + " is " + player.getScore() + " points ");
 				System.out.println("=======================================");
 			}
 		}
 	}
 
 	/**
 	 * Gets player position based on index
 	 *
 	 * @param num	player index
 	 * @return	player position
 	 */
 	public Position getPlayerPosition(int num)
 	{
 		switch (num)
 		{
 			case 0:
 				return Position.South;
 			case 1:
 				return Position.West;
 			case 2:
 				return Position.North;
 			case 3:
 				return Position.East;
 			default:
 				return Position.South;
 		}
 	}
 
 	/**
 	 * Handle events from UI
 	 *
 	 * @param actionEvent	action event object
 	 */
 	@Override
 	public void actionPerformed(ActionEvent actionEvent)
 	{
 		Object source = actionEvent.getSource();
 
 		if (source.getClass() == JButton.class)
 		{
 			JButton jButton = (JButton) source;
 			String buttonType = (String) jButton.getClientProperty("ButtonType");
 
 			if (buttonType.equals("ConfigurationOK"))
 			{
 				String playerName = this.configurationUI.getPlayerName();
 				if (playerName == null || playerName.trim().equals(""))
 				{
 					if (!configurationUI.isUserErrorShown())
 					{
 						configurationUI.displayErrorDialog("Enter a user name");
 						configurationUI.setUserErrorShown(true);
 					}
 				}
 				else
 				{
 					String levelOfDifficulty = this.configurationUI.getLevelofDifficulty();
 					int endScore = this.configurationUI.getEndScore();
 
 					if (!silent)
 					{
 						System.out.println("Player: " + playerName + ", Level: " + levelOfDifficulty + ", End Score: " + endScore);
 					}
 					this.configurationUI.getFrame().dispose();
 
 					initialize(playerName, endScore, levelOfDifficulty);
 					runGame();
 				}
 			}
 			else if (buttonType.equals("ConfigurationCancel"))
 			{
 				System.exit(0);
 			}
 			else if (buttonType.equals("PointDisplayPlayAgain"))
 			{
 				this.pointDisplay.dispose();
 				this.heartsUI.getframe().dispose();
 				this.run();
 			}
 			else if (buttonType.equals("PointDisplayQuit"))
 			{
 				System.exit(0);
 			}
 			else if (buttonType.equals("PassButton"))
 			{
 				if (this.cardsSelectedToPassCount != 3)
 				{
 					this.heartsUI.ShowBalloonTip("Pick 3 cards to pass!");
 					return;
 				}
 
 				passingCards();
 				this.heartsUI.redrawCards();
 
 				// set selected cards to normal mode
 				for (int k = 0; k < this.buttonCardsSelectedToPass.size(); k++)
 					this.heartsUI.setCardUnselected(this.buttonCardsSelectedToPass.get(k));
 
 				this.heartsUI.setPassButtonVisible(false);
 				this.cardAction = CardAction.Playing;
 				this.initializeFirstTurn();
 			}
 			else if (buttonType.equals("CardButton"))
 			{
 				Card card = (Card) jButton.getClientProperty("Card");
 				if (this.cardAction == CardAction.Passing)
 				{
 					boolean isSelected = (boolean) jButton.getClientProperty("Selected");
 
 					if (isSelected)
 					{
 						this.heartsUI.setCardUnselected(jButton);
 						this.buttonCardsSelectedToPass.remove(jButton);
 						this.cardsSelectedToPass.remove(card);
 						this.cardsSelectedToPassCount--;
 					}
 					else
 					{
 						if (this.cardsSelectedToPassCount != 3)
 						{
 							this.heartsUI.setCardSelected(jButton);
 							this.buttonCardsSelectedToPass.add(jButton);
 							this.cardsSelectedToPass.add(card);
 							this.cardsSelectedToPassCount++;
 						}
 						else
 						{
 							this.heartsUI.ShowBalloonTip("You can only select 3 cards to pass!");
 						}
 					}
 				}
 				else if (this.cardAction == CardAction.Playing)
 				{
 					// user turn
 					if (this.currentPlayerIndexThisTurn == 0)
 					{
 						try
 						{
 							User user = (User) this.players.get(0);
 							user.TryPlayCard(card, this.currentCardsPlayed, this.heartsBroken,
 									(this.currentTurnFirst && this.currentTurn == 0));
 
 							this.finalizePlayerTurn(card, Position.South, this.players.get(0));
 
 							this.runAITurns();
 						}
 						catch (Exception ex)
 						{
 							// TODO show why this is illegal move
 							ex.printStackTrace();
 							this.heartsUI.ShowBalloonTip(ex.getMessage());
 						}
 					}
 				}
 			}
 		}
 		else if (source.getClass() == JMenuItem.class)
 		{
 			JMenuItem jMenuItem = (JMenuItem) source;
 			String menuItemType = (String) jMenuItem.getClientProperty("MenuItemType");
 
 			if (menuItemType.equalsIgnoreCase("Rules"))
 			{
 				RulesWindow rulesWindow = new RulesWindow();
 				rulesWindow.showDialog();
 			}
 			else if (menuItemType.equalsIgnoreCase("Achievements"))
 			{
 				User user = (User) this.players.get(0);
 				AchievementsDisplay achivementsDisplay = new AchievementsDisplay(user);
 				achivementsDisplay.showDialog();
 			}
 			else if (menuItemType.equalsIgnoreCase("Exit"))
 			{
 				System.exit(0);
 			}
 		}
 	}
 }
