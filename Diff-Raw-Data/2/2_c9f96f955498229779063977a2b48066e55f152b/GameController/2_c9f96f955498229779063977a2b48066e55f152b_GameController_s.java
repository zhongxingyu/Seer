 package se.chalmers.dryleafsoftware.androidrally.controller;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import se.chalmers.dryleafsoftware.androidrally.IO.IOHandler;
 import se.chalmers.dryleafsoftware.androidrally.model.CardTimer;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.TurnType;
 import se.chalmers.dryleafsoftware.androidrally.model.gameModel.GameModel;
 import se.chalmers.dryleafsoftware.androidrally.model.robots.Robot;
 
 /**
  * Class that holds the GameModel and is the controller part.
  */
 public class GameController implements PropertyChangeListener {
 	public static final int PING_TIME = 1;
 	private GameModel gameModel;
 	private Timer timer;
 	private TimerTask endOfRound;
 	private int hoursEachRound;
 	private boolean isRunRunning;
 	private int nbrOfRobotsDone;
 	private CardTimer[] cardTimer;
 	private int nbrOfRobots;
 	private String mapAsString;
 	private AIRobotController aiRobotController;
 	private int nbrOfHumanPlayers;
 	private int nbrOfBots;
 	private List<String> allMoves;
 	private List<String[]> allCards;
 	private int cardTimerSeconds;
 	private Date endOfRoundDate;
 
	private static final String testMap = "yxxxxxxx113xx36:37xxxx46xxxyxxxxxxx113xx133xxxxx78x16xyxxx12xxxx113x5x133xxx32xxx58:16xyx5x103x103x103xxx113xx133xxxxxx38:16xyxxxxx06:07xxxxxxxx46xx16xyxxxxx06x5x123x123x123x123x123x4xxx18:16xyxxxx27x06xxxxxxxxxx28:16xyx123x123x123x123x06:07xxxxxxxx46xx16xyxxxxxx4x203x203x203x203x203x203xxx48:16xyxxx42xxx14x123x123x123x123x123x14xxx68:16xyxx5xxxx22x1xxxxxxx88xxyxxxxxxxxxxxx5x46xxx";
 	
 	/**
 	 * Creates a new GameController.
 	 * 
 	 * @param nbrOfHumanPlayers the number of playing people (for server-client
 	 * games)
 	 * 
 	 * @param nbrOfBots the number of robots controlled by the computer
 	 * 
 	 * @param hoursEachRound how long time the players have to play their cards
 	 * between the rounds (How long time they have before "Draw cards" is
 	 * pressed. Not used if nbrOfHumanPlayers == 1.
 	 * 
 	 * @param cardTimerSeconds the number of seconds a player have to place
 	 * his/her cards when "Draw cards" is pressed
 	 * 
 	 * @param map a String map on the for such as yxxxxxxxxxxxxxxxxyxxxxx...
 	 */
 	public GameController(int nbrOfHumanPlayers, int nbrOfBots,
 			int hoursEachRound, int cardTimerSeconds, String map) {
 		this.nbrOfHumanPlayers = Math.min(nbrOfHumanPlayers, 8); // So that no one can send in corrupt values.
 		this.nbrOfHumanPlayers = Math.max(this.nbrOfHumanPlayers, 1); // 1-8 players and no more than 8 robots incl. bots.
 		this.nbrOfBots = Math.min(nbrOfBots, 8 - this.nbrOfHumanPlayers);
 		this.nbrOfBots = Math.max(this.nbrOfBots, 0);
 		if (this.nbrOfHumanPlayers == 1 && this.nbrOfBots == 0) { // if only 1 player there has to be 1 bot
 			this.nbrOfBots = 1;
 		}
 		isRunRunning = false;
 		gameModel = new GameModel(this.nbrOfHumanPlayers + this.nbrOfBots,
 				testMap);
 		this.nbrOfRobots = gameModel.getRobots().size();
 		allMoves = new ArrayList<String>();
 		allCards = new ArrayList<String[]>();
 
 		aiRobotController = new AIRobotController(gameModel.getGameBoard());
 
 		mapAsString = gameModel.getMap();
 
 		this.cardTimerSeconds = Math.max(cardTimerSeconds, 15); // Make cardTimerSeconds be in the interval 15-180
 		this.cardTimerSeconds = Math.min(this.cardTimerSeconds, 180); // -''-
 		this.hoursEachRound = Math.max(hoursEachRound, 1); // Make hoursEachRound be in the interval 1-24
 		this.hoursEachRound = Math.min(this.hoursEachRound, 24);// -''-
 
 		timer = new Timer();
 		cardTimer = new CardTimer[nbrOfRobots];
 		for (int i = 0; i < nbrOfRobots; i++) {
 			cardTimer[i] = new CardTimer(this.cardTimerSeconds + PING_TIME, i); // let the time be a variable
 			cardTimer[i].addPropertyChangeListener(this);
 		}
 	}
 
 	/**
 	 * Resume a game using a the data of a saved game.
 	 * 
 	 * @param saveData
 	 *            a String with the data used to resume a game
 	 */
 	public GameController(String saveData) {
 		this(1, Integer.parseInt(saveData.split("b")[0].split(":")[2]) - 1,
 				Integer.parseInt(saveData.split("b")[0].split(":")[1]), Integer
 						.parseInt(saveData.split("b")[0].split(":")[0]),
 				saveData.split("b")[2]);
 		String[] dataChunks = saveData.split("b");
 
 		// Chunk 1:
 		int robotIndex = 0;
 		for (String robotData : dataChunks[1].split("c")) {
 			String[] subChunks = robotData.split("a");
 
 			// [xx][yy][dir][damage][lives]:[checkpoint]:[spawn xx][spawn yy]
 			String[] data = subChunks[0].split(":");
 			int posX = Integer.parseInt(data[0].substring(0, 2));
 			int posY = Integer.parseInt(data[0].substring(2, 4));
 			int direction = Integer.parseInt(data[0].substring(4, 5));
 			int hp = Integer.parseInt(data[0].substring(5, 6));
 			int life = Integer.parseInt(data[0].substring(6, 7));
 
 			int checkPoint = Integer.parseInt(data[1]);
 
 			int spawnX = Integer.parseInt(data[2].substring(0, 2));
 			int spawnY = Integer.parseInt(data[2].substring(2, 4));
 
 			// Cards
 			List<Card> cards = new ArrayList<Card>();
 			String[] cardData = subChunks[1].split(":");
 			for (int i = 0; i < 5; i++) {
 				cards.add(gameModel.getDeck().getCard(
 						Integer.parseInt(cardData[i])));
 			}
 			Robot robot = gameModel.getRobots().get(robotIndex);
 			robot.setChosenCards(cards);
 			robot.setX(spawnX);
 			robot.setY(spawnY);
 			robot.newSpawnPoint();
 			robot.setX(posX);
 			robot.setY(posY);
 			for (int j = Robot.STARTING_LIFE; j > life; j--) {
 				robot.die();
 			}
 			robot.damage(Robot.STARTING_HEALTH - hp);
 			for (int j = 1; j <= checkPoint; j++) {
 				robot.reachCheckPoint(j);
 			}
 			switch (direction) {
 			case 1:
 				robot.turn(TurnType.RIGHT);
 				break;
 			case 2:
 				robot.turn(TurnType.UTURN);
 				break;
 			case 3:
 				robot.turn(TurnType.LEFT);
 				break;
 			}
 			robotIndex++;
 		}
 	}
 
 	/**
 	 * Saves the game of a specific ID.
 	 * 
 	 * @param gameID
 	 *            the ID of the game running
 	 */
 	public void save(int gameID) {
 		// [timer data]b[robotinfo]a[robotcards]c[robotinfo]a[robotcards]cb[map]
 		StringBuilder sb = new StringBuilder();
 		sb.append(cardTimerSeconds + ":");
 		sb.append(hoursEachRound + ":");
 		sb.append(gameModel.getRobots().size());
 		sb.append("b");
 
 		// [xx][yy][dir][damage][lives]:[checkpoint]:[spawn xx][spawn yy]
 		for (Robot r : gameModel.getRobots()) {
 			sb.append(String.format("%02d", r.getX()));
 			sb.append(String.format("%02d", r.getY()));
 			sb.append(r.getDirection());
 			sb.append(r.getHealth());
 			sb.append(r.getLife());
 			sb.append(":");
 			sb.append(r.getLastCheckPoint());
 			sb.append(":");
 			sb.append(String.format("%02d", r.getSpawnPointX()));
 			sb.append(String.format("%02d", r.getSpawnPointY()));
 			sb.append("a");
 			for (int i = 0; i < r.getChosenCards().length; i++) {
 				Card c = r.getChosenCards()[i];
 				if (c != null && i >= r.getHealth()) {
 					sb.append(c.getPriority() + ":");
 				} else {
 					sb.append("-1:");
 				}
 			}
 			sb.append("c");
 		}
 		sb.append("b" + gameModel.getMap());
 		IOHandler.save(sb.toString(), gameID, IOHandler.SERVER_DATA);
 	}
 
 	/**
 	 * Set random cards for robots that haven't placed their cards when the time
 	 * is up.
 	 */
 	private void handleRemainingRobots() {
 		for (int i = 0; i < gameModel.getRobots().size(); i++) {
 			if (!gameModel.getRobots().get(i).haveSentCards()) {
 				setRandomCards(i);
 			}
 		}
 	}
 
 	/**
 	 * Timer is scheduled to what hoursEachRound is set to (<b>IF</b>
 	 * reScheduleTask() is called).
 	 */
 	public void startRoundTimer() {
 		if (!isSinglePlayer()) {
 			endOfRoundDate = new Date(System.currentTimeMillis()
 					+ hoursEachRound * 3600000);
 			timer.schedule(endOfRound, hoursEachRound * 3600000);
 		}
 	}
 
 	/**
 	 * Stops the round timer.
 	 */
 	public void stopRoundTimer() {
 		endOfRound.cancel();
 	}
 
 	/**
 	 * Called every time a new round is created. Called by startRoundTimer().
 	 */
 	public void reScheduleTask() {
 		endOfRound = new TimerTask() {
 			/*
 			 * Method that is executing if the round time is out or all robots
 			 * are done playing their cards.
 			 */
 			@Override
 			public synchronized void run() {
 				isRunRunning = true;
 				if (!isSinglePlayer()) {
 					stopRoundTimer();
 				}
 				handleRemainingRobots();
 
 				gameModel.moveRobots();
 				allMoves.add(gameModel.getAllMoves());
 				// If the game is over a new round will not be started. Game
 				// will end.
 				if (!gameModel.isGameOver()) {
 					newRound();
 				} else {
 					timer.cancel();
 				}
 				isRunRunning = false;
 			}
 		};
 	}
 
 	/**
 	 * Return a string representing the last chosen cards from a specific robot.
 	 * 
 	 * @param robotID
 	 *            the id of the robot to get cards for.
 	 * @return a string representing the last chosen cards from a specific
 	 *         robot.
 	 */
 	private String getCurrentChosenCards(int robotID) {
 		StringBuilder sb = new StringBuilder();
 		for (Card card : gameModel.getRobots().get(robotID).getChosenCards()) {
 			sb.append(card.getPriority() + ":");
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * If the server hasn't received information from the client within the time
 	 * of the card timer + ping "time out"-time randomized cards will be given
 	 * to the robot that the card timer went out from.
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent pce) {
 		if (pce.getPropertyName().equals(CardTimer.CARD_TIME_OUT)) {
 			setRandomCards((Integer) pce.getNewValue());
 		}
 	}
 
 	/**
 	 * Set the given input from the client to a specific robots chosen cards.
 	 * 
 	 * @param chosenCards
 	 *            should contain data about the robot and the chosenCards
 	 *            according to separate document.
 	 * @return a String containing data of the locked cards.
 	 */
 	public synchronized void setChosenCardsToRobot(int robotID,
 			String chosenCards) {
 		cardTimer[robotID].cancelTask();
 		if (!gameModel.getRobots().get(robotID).hasLost()) {
 			try {
 				String[] cardStrings = chosenCards.split(":");
 				List<Card> cards = new ArrayList<Card>();
 				Robot robot = gameModel.getRobots().get(robotID);
 				for (int i = 0; i < 5; i++) {
 					if (Integer.parseInt(cardStrings[i]) == -1) {
 						cards.add(null);
 					} else if (Integer.parseInt(cardStrings[i]) < robot
 							.getCards().size()) {
 						Card card = robot.getCards().get(
 								Integer.parseInt(cardStrings[i]));
 						if (cards.contains(card)) {
 							cards.add(null);
 						} else {
 							cards.add(robot.getCards().get(
 									Integer.parseInt(cardStrings[i])));
 						}
 					}
 				}
 				robot.setChosenCards(cards);
 			} catch (IllegalArgumentException e) {
 				// Do nothing
 			}
 			gameModel.getRobots().get(robotID).fillEmptyCardRegisters();
 			gameModel.getRobots().get(robotID).setSentCards(true);
 			gameModel.getRobots().get(robotID).setLastRoundChosenCards(getCurrentChosenCards(robotID));
 			allCards.get(allCards.size()-1)[robotID] = gameModel.getRobots().get(robotID).getLastRoundChosenCards();
 		}
 		nbrOfRobotsDone++;
 	
 		if (gameModel.getRobotsPlaying() == nbrOfRobotsDone && !isRunRunning) {
 			endOfRound.run();
 		}
 	}
 
 	/**
 	 * Gives data the client needs when connecting to a game. (Such as the
 	 * length of the timers etc.)
 	 * 
 	 * @return Data the client needs when connecting.
 	 */
 	public String getInitGameData() {
 		return cardTimerSeconds + ";" + hoursEachRound + ";"
 				+ (isSinglePlayer() ? -1 : endOfRoundDate.getTime());
 	}
 
 	/**
 	 * All robots will receive new cards and the round timer will start.
 	 * <p>
 	 * ONLY call when a new game is to be started. After that the method will be
 	 * called when another round has ended (unless the game is over).
 	 */
 	public void newRound() {
 		gameModel.dealCards();
 		reScheduleTask();
 		if (!isSinglePlayer()) {
 			startRoundTimer();
 		}
 		nbrOfRobotsDone = 0;
 		for (int i = nbrOfHumanPlayers; i < nbrOfRobots; i++) {
 			cardTimer[i].cancelTask();
 			if (!gameModel.getRobots().get(i).hasLost()) {
 				aiRobotController.makeMove(gameModel.getRobots().get(i));
 				gameModel.getRobots().get(i).fillEmptyCardRegisters();
 				gameModel.getRobots().get(i).setSentCards(true);
 				gameModel.getRobots().get(i).setLastRoundChosenCards(getCurrentChosenCards(i));
 			}
 			nbrOfRobotsDone++;
 		}
 		allCards.add(new String[gameModel.getRobots().size()]);
 	}
 
 	public String getMap() {
 		return mapAsString;
 	}
 
 	public String getNbrOfRobots() {
 		return String.valueOf(nbrOfRobots);
 	}
 
 	/**
 	 * Return a string containing all data from the last round.
 	 * 
 	 * @param round
 	 *            the round to get results from. The first round is 1 and not 0.
 	 * @return a string containing all data from the last round.
 	 */
 	public String getRoundResults(int round) {
 		return allMoves.get(round - 1);
 	}
 
 	/**
 	 * Returns a String representing the chosenCards for a specific robot and
 	 * round.
 	 * 
 	 * @param round
 	 *            the round to get the cards from. The first round is 1 and not
 	 *            0.
 	 * @param robot
 	 *            the robotID to get cards for
 	 * @return the chosenCards for a specific robot and round.
 	 */
 	public String getCards(int round, int robot) {
 		if (round > allMoves.size()) {
 			return getCards(robot);
 		} else {
 			return allCards.get(round - 1)[robot];
 		}
 	}
 
 	/**
 	 * Get the cards of a specific robot.
 	 * 
 	 * @param robotID
 	 *            the index of the robot in the GameModel's robot list.
 	 * @return the robot's cards represented as a string (not chosen cards).
 	 */
 	private String getCards(int robotID) {
 		List<Card> cards = gameModel.getRobots().get(robotID).getCards();
 		Card[] chosenCards = gameModel.getRobots().get(robotID)
 				.getChosenCards();
 		StringBuilder sb = new StringBuilder();
 	
 		for (Card c : cards) {
 			for (int i = 0; i < chosenCards.length; i++) {
 				if (chosenCards[i] == c) {
 					sb.append("L" + i + ";");
 					break;
 				}
 			}
 			sb.append(c.getPriority() + ":");
 		}
 		System.out.println("getCards" + sb.toString());
 		cardTimer[robotID].start();
 		return sb.toString();
 	}
 
 	/**
 	 * Return the current round. The first round is 1 and not 0.
 	 * 
 	 * @return the current round.
 	 */
 	public int getRound() {
 		return allMoves.size();
 	}
 
 	/**
 	 * Get how long the round is totally.
 	 * 
 	 * @return the number of hours a round is
 	 */
 	public int getHoursEachRound() {
 		return hoursEachRound;
 	}
 
 	/**
 	 * Set how long a round should be. (24 hours as default in the constructor).
 	 * 
 	 * @param hoursEachRound
 	 *            the number of hours a round should be
 	 */
 	public void setHoursEachRound(int hoursEachRound) {
 		this.hoursEachRound = hoursEachRound;
 	}
 
 	/**
 	 * Sets random chosen cards for a specific robot.
 	 * 
 	 * @param robotID
 	 *            a specific robots ID in the robot list in GameModel
 	 */
 	private void setRandomCards(int robotID) {
 		setChosenCardsToRobot(robotID, ":-1:-1:-1:-1:-1");
 	}
 
 	/**
 	 * To know if the round timer is to be used or not it's necessary to know
 	 * whether it's a single player game or not.
 	 * 
 	 * @return true if there is only 1 human player, else false
 	 */
 	private boolean isSinglePlayer() {
 		return nbrOfHumanPlayers == 1;
 	}
 }
