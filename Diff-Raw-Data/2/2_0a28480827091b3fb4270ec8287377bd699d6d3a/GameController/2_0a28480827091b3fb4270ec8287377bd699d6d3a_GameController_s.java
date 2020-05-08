 package se.chalmers.dryleafsoftware.androidrally.controller;
 
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.utils.Timer;
 
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.gameModel.GameModel;
 import se.chalmers.dryleafsoftware.androidrally.model.robots.Robot;
 
 // TODO handle all sort of indata, i.e. handle exceptions if client sends incorrect data.
 
 public class GameController implements PropertyChangeListener {
 	private GameModel gameModel;
 	private Timer timer;
 	private Timer.Task endOfRound;
 	private int hoursEachRound;
 	private boolean isRunRunning;
 	private int nbrOfRobotsDone;
 	private CardTimer[] cardTimer;
 	private int nbrOfRobotsAlive;
 	private String nbrOfPlayers;
 	private String mapAsString;
 
 
 	public GameController(int nbrOfPlayers) {
 		this.nbrOfPlayers = String.valueOf(nbrOfPlayers);
 		isRunRunning = false;
 		nbrOfRobotsDone = 0;
 		gameModel = new GameModel(this, nbrOfPlayers);
 		
 		mapAsString = gameModel.getMap();
 		
 		timer = new Timer();
 		endOfRound = new Timer.Task() {
 			/* Method that is executing if the round time is out or
 			 * all robots are done playing their cards. */
 			@Override
 			public void run() {
 				isRunRunning = true;
 				stopRoundTimer();
 				handleRemainingRobots();
 				gameModel.moveRobots();
 				//If the game is over a new round will not be started.
 				if (gameModel.isGameOver()) {
 					newRound();
 				}
 				isRunRunning = false;
 			}
 		};
 		cardTimer = new CardTimer[nbrOfPlayers];
 		for (int i = 0; i < nbrOfPlayers; i++) {
 			cardTimer[i] = new CardTimer(65, i, this);
 		}
 		hoursEachRound = 24;
 	}
 	
 	/**
 	 * DO NOT USE!!!!!!!!!!!!!!!!!!
 	 * TODO: remove
 	 * @return
 	 */
 	public GameModel getModel() {
 		// TODO: remove
 		return this.gameModel;
 	}
 
 	public void handleRemainingRobots() {
 		for (int i = 0; i < gameModel.getRobots().size(); i++) {
 			if (!gameModel.getRobots().get(i).haveSentCards()) {
 				setChosenCardsToRobot(i + ":-1:-1:-1:-1:-1");
 			}
 		}
 	}
 
 	/**
 	 * Timer is scheduled to what hoursEachRound is set to.
 	 * 24 hours as default.
 	 */
 	public void startRoundTimer() {
 		Timer.schedule(endOfRound, hoursEachRound * 3600000);
 	}
 
 	public void stopRoundTimer() {
 		timer.stop();
 		timer.clear(); //FIXME If there are problems with the timer, this might be it
 	}
 
 	/**
 	 * Set the given input from the client to a specific robots chosen cards.
 	 * 
 	 * @param chosenCards should contain data about the robot and the chosenCards according to
 	 * separate document.
 	 * @return a String containing data of the locked cards.
 	 */
 	public String setChosenCardsToRobot(String chosenCards) { //TODO ClientID?
 		String[] cardStrings = chosenCards.split(":");
 		int robotID = Integer.parseInt(cardStrings[0]);
 		cardTimer[robotID].stop();
 		cardTimer[robotID].clear();
 		List<Card> cards = new ArrayList<Card>();
 		Robot robot = gameModel.getRobots().get(robotID);
		for (int i = 0; i < 5; i++) {
 			if (Integer.parseInt(cardStrings[i]) == -1) {
 				cards.add(null);
 			} else if(Integer.parseInt(cardStrings[i]) < robot.getCards().size()){
 				cards.add(robot.getCards().get(Integer.parseInt(cardStrings[i])));
 			}
 		}
 		robot.setChosenCards(cards);
 		robot.fillEmptyCardRegisters();
 		robot.setSentCards(true);
 		nbrOfRobotsDone++;
 		if(nbrOfRobotsAlive == nbrOfRobotsDone && !isRunRunning) {
 			endOfRound.run();
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for(Card card : gameModel.getRobots().get(robotID).getChosenCards()) {
 			sb.append(card.getPriority() + ":");
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Get how long the round is totally.
 	 * @return the number of hours a round is
 	 */
 	public int getHoursEachRound() {
 		return hoursEachRound;
 	}
 
 	/**
 	 * Set how long a round should be. (24 hours as default in the constructor).
 	 * @param hoursEachRound the number of hours a round should be
 	 */
 	public void setHoursEachRound(int hoursEachRound) {
 		this.hoursEachRound = hoursEachRound;
 	}
 
 	/**
 	 * If the server hasn't received information from the client within
 	 * the time of the card timer + ping "time out"-time randomized cards
 	 * will be given to the robot that the card timer went out from.
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent pce) {
 		if (pce.getPropertyName().equals("cardTimeOut")) {
 			setChosenCardsToRobot((Integer)pce.getNewValue() + ":-1:-1:-1:-1:-1");
 		} else if (pce.getPropertyName().equals(GameModel.ROBOT_LOST)) {
 			//send to client that a specific robot lost
 		} else if (pce.getPropertyName().equals(GameModel.ROBOT_WON)) {
 			//send to all clients which robot that has won
 			//end game
 		}
 	}
 
 	/**
 	 * Get the cards of a specific robot.
 	 * @param robotID the index of the robot in the GameModel's robot list.
 	 * @return the robot's cards represented as a string (not chosen cards).
 	 */
 	public String getCards(int robotID) {
 		List<Card> cards = gameModel.getRobots().get(robotID).getCards();
 		Card[] chosenCards = gameModel.getRobots().get(robotID).getChosenCards();
 		StringBuilder sb = new StringBuilder();
 		
 		for(Card c : cards) {
 			for(int i = 0; i < chosenCards.length; i++) {
 				if(chosenCards[i] == c) {
 					sb.append("L" + i + ";");
 					break;
 				}
 			}
 			sb.append(c.getPriority() + ":");
 		}
 		return sb.toString(); 
 	}
 	
 	/**
 	 * TODO remove?????
 	 * @param robotID
 	 * @return
 	 */
 	public Card[] getChosenCards(int robotID){
 		return gameModel.getRobots().get(robotID).getChosenCards();
 	}
 
 	/**
 	 * All robots will receive new cards and
 	 * the round timer will start.
 	 * <p>
 	 * ONLY call when a new game is to be started.
 	 * After that the method will be called when another
 	 * round has ended (unless the game is over).
 	 */
 	public void newRound() {
 		gameModel.dealCards();
 		startRoundTimer();
 		
 		//Check how many robots that are still alive this round
 		nbrOfRobotsAlive = 0;
 		for (Robot robotInList : gameModel.getRobots()) {
 			if (robotInList.getLife() > 0) {
 				nbrOfRobotsAlive++;
 			}
 		}
 		nbrOfRobotsDone = 0;
 	}
 	
 	public String getMap() {
 		return mapAsString;
 	}
 	
 	public String getNbrOfPlayers() {
 		return nbrOfPlayers;
 	}
 	
 	public String getRoundResults() {
 		return this.gameModel.getAllMoves();
 	}
 	
 //TODO game end how?
 
 }
