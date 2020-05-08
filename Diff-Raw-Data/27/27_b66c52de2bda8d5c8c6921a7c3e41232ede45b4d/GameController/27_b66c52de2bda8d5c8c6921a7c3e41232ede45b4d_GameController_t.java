 package se.chalmers.dryleafsoftware.androidrally.controller;
 
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.utils.Timer;
 
 import android.os.Looper;
 
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.gameModel.GameModel;
 import se.chalmers.dryleafsoftware.androidrally.model.robots.Robot;
 
 public class GameController implements PropertyChangeListener {
 	private GameModel gameModel;
 	private Timer timer;
 	private Timer.Task endOfRound;
 	private int hoursEachRound;
 	private boolean isRunRunning;
 	private int nbrOfRobotsDone;
 	private CardTimer[] cardTimer;
 	private int nbrOfRobotsAlive;
 
 
 	public GameController(int nbrOfPlayers) {
 		isRunRunning = false;
 		nbrOfRobotsDone = 0;
 		gameModel = new GameModel(nbrOfPlayers);
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
 				newRound();
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
 				setChosenCardsToRobot(new int[]{0,0,0,0,0}, i);
 			}
 		}
 	}
 
 	/**
 	 * Timer is scheduled to what hoursEachRound is set to.
 	 * 24 hours as default.
 	 */
 	public void startRoundTimer() {
 		timer.schedule(endOfRound, hoursEachRound * 3600000);
 	}
 
 	public void stopRoundTimer() {
 		timer.stop();
 		timer.clear(); //FIXME If there are problems with the timer, this might be it
 	}
 
 	/**
 	 * Set the given input from the client to a specific robots chosen cards.
 	 * 
 	 * @param indexOfChosenCard array of length == 5. Index of the robot's card that the robot have chosen.
 	 * @param robotID The index of the robot in the list of robots held by GameModel
 	 */
 	public void setChosenCardsToRobot(int[] indexOfChosenCard, int robotID) { //TODO ClientID?
 		cardTimer[robotID].stop();
		cardTimer[robotID].clear();
 		List<Card> chosenCards = new ArrayList<Card>();
 		Robot robot = gameModel.getRobots().get(robotID);
 		for (int i = 0; i < 5; i++) {
 			if (indexOfChosenCard[i] == -1) {
 				chosenCards.add(null);
 			} else {
 				chosenCards.add(robot.getCards().get(indexOfChosenCard[i]));
 			}
 		}
 		robot.setChosenCards(chosenCards);
 		robot.setSentCards(true);
 		nbrOfRobotsDone++;
 		if(nbrOfRobotsAlive == nbrOfRobotsDone && !isRunRunning) {
 			endOfRound.run();
 		}
 
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
			setChosenCardsToRobot(new int[]{-1,-1,-1,-1,-1}, (Integer)pce.getNewValue());
 		}
 	}
 
 	/**
 	 * Get the cards of a specific robot.
 	 * @param robotID the index of the robot in the GameModel's robot list.
 	 * @return the robot's cards (not chosen cards)
 	 */
 	public List<Card> getCards(int robotID) {
 		return gameModel.getRobots().get(robotID).getCards(); 
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
 //TODO game end how?
 
 }
