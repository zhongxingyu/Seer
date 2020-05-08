 package se.chalmers.dryleafsoftware.androidrally.libgdx;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.IO.IOHandler;
 import se.chalmers.dryleafsoftware.androidrally.controller.GameController;
 import se.chalmers.dryleafsoftware.androidrally.game.GameSettings;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.AnimationAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.CheckPointAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.ExplodeAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.FallAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.GameAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.HealthAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.HolderAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.MultiAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.RespawnAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.SingleAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.LaserView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.RobotView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.view.CardView;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * This class talks to the server. It converts data from the server to appropriate classes which 
  * can then be fetched through getters. It also converts data is should sent to a format the server
  * can read.
  * 
  * @author
  *
  */
 public class Client {
 
 	
 	private se.chalmers.dryleafsoftware.androidrally.controller.GameController controller;
 	private final int clientID; // TODO: the client must be assigned a unique ID from server
 	private int robotID; // TODO: get robot ID from server
 	private int roundID = 0;
 	private static Client instance;
 		
 	/**
 	 * Creates a new client instance.
 	 */
 	private Client() {
 		this.clientID = 0;
 		this.robotID = 0;
 	}
 	
 	/**
 	 * Gets the singleton instance of client
 	 */
 	public static synchronized Client getInstance() {
 		if (instance == null) {
 			instance = new Client();
 		}
 		return instance;
 	}
 	
 	/**
 	 * Creates a new game with the supplied settings
 	 * @param settings The settings to use for the game
 	 */
 	public void createGame(GameSettings settings) {
 		resetGameValues();
 		this.controller = new GameController(settings.getNbrOfHumanPlayers(), settings.getNbrOfBots(),
 				settings.getHoursEachRound(), settings.getCardTimerSeconds(), settings.getMap());
 		controller.newRound();
 	}
 	
 	private void resetGameValues() {
 		roundID = 0;
 		robotID = 0; // TODO: from server!
 	}
 	
 	public void loadGame(int gameID) {
 		resetGameValues();
 		this.controller = new GameController(IOHandler.load(gameID, IOHandler.SERVER_DATA));
 		controller.newRound();
 	}
 	
 	public int getRobotID() {
 		return robotID;
 	}
 	
 	/**
 	 * Returns the map of the board as a matrix of strings.
 	 * @return A map of the board as a matrix of strings.
 	 */
 	public String getMap() {
 		System.out.println("To client: \"" + controller.getMap() + "\"");
 		return controller.getMap();
 	}
 	
 	/**
 	 * Sends the cards to the server. Note: This list should not contain more then five!
 	 * @param cards The cards to send.
 	 */
 	public void sendCard(CardView[] cards) {
 		StringBuilder sb = new StringBuilder();
 		int[] temp = new int[5]; 
 		for(int i = 0; i < 5; i++) {
 			if(cards[i] == null) {
 				temp[i] = -1;
 			}else{
 				temp[i] = cards[i].getIndex();
 			}
 			sb.append(":" + temp[i]);
 		}	
 		System.out.println("From client: \"" + sb.toString() + "\"");
 		controller.setChosenCardsToRobot(robotID, sb.toString().substring(1)); // TODO: server
 	}
 	
 	public void deleteGame(int gameID) {
 		IOHandler.remove(gameID, IOHandler.CLIENT_DATA);
 		IOHandler.remove(gameID, IOHandler.SERVER_DATA);
 	}
 	
 	/**
 	 * Gives the number of rounds the view is behind.
 	 * @return The number of rounds the view is behind.
 	 */
 	public int getRoundsBehind() {
 		return controller.getRound() - roundID;
 	}
 	
 	/**
 	 * Gives all the actions which was created during the last round.
 	 * @return A list of all the actions was created during the last round.
 	 */
 	public RoundResult getRoundResult() {	
 		RoundResult result = new RoundResult();	
 		String indata = controller.getRoundResults(roundID + 1);	
 		System.out.println("Asking for round: " + (roundID+1));
 		String[] allActions = indata.split(";");
 		
 		Texture damageAnim = new Texture(Gdx.files.internal("textures/special/damageAnim.png"));
 		damageAnim.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		Texture explodeAnim = new Texture(Gdx.files.internal("textures/special/explodeAnim.png"));
 		damageAnim.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		System.out.println("To client: \"" + indata + "\"");
 		
 		for(String s : allActions) {
 			String[] parallel = s.split("#");
 			if(parallel[0].equals("R")) { // R = card
 				result.newPhase();
 			}else if(parallel[0].substring(0, 1).equals("B")) { // B = phase
 				int phase = Integer.parseInt(parallel[0].substring(1));	
 				if(phase == GameAction.PHASE_LASER) { 
 					MultiAction multiDamage = new MultiAction();
 					result.addAction(multiDamage);
 					MultiAction multiExplode = new MultiAction();
 					result.addAction(multiExplode);
 					for(int i = 1; i < parallel.length; i++) {
 						String[] data = parallel[i].split(":");
 						HealthAction ha = new HealthAction(
 								Integer.parseInt(data[0]),
 								Integer.parseInt(data[1].substring(2)),
 								Integer.parseInt(data[1].substring(1, 2)));
 						multiDamage.add(ha);
 						multiDamage.add(new AnimationAction(Integer.parseInt(data[0]), 1000, 
 								new AnimatedImage(damageAnim, 4, 2, 1000)));
 						if(data[1].substring(0, 1).equals("1")) { // Robot should explode
 							multiExplode.add(new ExplodeAction(Integer.parseInt(data[0]), explodeAnim));
 						}
 					}
 					multiDamage.setDuration(1000);
 					multiDamage.setMoveRound(GameAction.PHASE_LASER);
 					System.out.println("Robot hit");
 				}else if(phase == GameAction.PHASE_RESPAWN) {
 					for(int i = 1; i < parallel.length; i++) {
 						SingleAction a = createSingleAction(parallel[i]);
 						a.setDuration(0);
 						result.addAction(a);
 						int id = Integer.parseInt(parallel[i].substring(0, 1));
 						result.addAction(new RespawnAction(id));
 					}
 				}else if(phase == GameAction.PHASE_CHECKPOINT) {
 					for(int i = 1; i < parallel.length; i++) {
 						String[] data = parallel[i].split(":");
 						System.out.println("Robot: " + data[0] + ", reached checkpoint: " + data[1]);
 						result.addAction(new CheckPointAction(Integer.parseInt(data[0]),
 								Integer.parseInt(data[1]), false));
 					}
 				}else{ // If the phase isn't that special.
 					GameAction action;					
 					if(parallel.length == 1) { // If no actions follows:
 						action = new HolderAction(1000);
 					}else{ // If actions
 						MultiAction multi = new MultiAction();
 						for(int i = 1; i < parallel.length; i++) {
 							multi.add(createSingleAction(parallel[i]));
 						}
 						action = multi;
 					}
 					action.setMoveRound(phase);
 					result.addAction(action);
 				}
 			}else if(parallel[0].equals("F")) { // When a player falls
 				String[] data = parallel[1].split(":");
 				result.addAction(new HealthAction(Integer.parseInt(data[0]), 0, 
 						Integer.parseInt(data[1])));
 				result.addAction(new FallAction(Integer.parseInt(data[0]), 1000));
 			}else if(parallel[0].equals("L")) { // When a player lose.
 				if(Integer.parseInt(parallel[1]) == robotID) {
 					result.addAction(new HolderAction(0, HolderAction.SPECIAL_PHASE_GAMEOVER));
 				}
 			}else if(parallel[0].equals("W")) { // When a player has won.
 				result.addAction(new CheckPointAction(
 						Integer.parseInt(parallel[1]), CheckPointAction.UNCHANGED, true));
 				result.addAction(new HolderAction(0, HolderAction.SPECIAL_PHASE_WON));
 			}
 			// Generic multiaction
 			else if(parallel.length > 1){
 				MultiAction a = new MultiAction();
 				for(int i = 0; i < parallel.length; i++) {
 					a.add(createSingleAction(parallel[i]));
 				}	
 				result.addAction(a);
 			}else{
 				result.addAction(createSingleAction(parallel[0]));
 			}
 		}
 		return result;	
 	}
 	
 	/**
 	 * Loads the data needed to restore the game board.
 	 * @param gameID
 	 * @return
 	 */
 	public String getSavedBoardData(int gameID) {
 		String data = IOHandler.load(gameID, IOHandler.CLIENT_DATA);
 		String[] chunks = data.split("c");
 		String[] clientData = chunks[0].split(":");
 		this.robotID = Integer.parseInt(clientData[0]);
 		this.roundID = Integer.parseInt(clientData[1]);
 		
 		return chunks[1];
 	}
 	
 	/**
 	 * Saves the current game.
 	 * @param gameID
 	 */
 	public void saveCurrentGame(int gameID, String boardData) {
 		// Force save of server data:
 		controller.save(gameID);
 		
 		// Save client specific data:
 		StringBuilder sb = new StringBuilder();
 		sb.append(robotID + ":");
 		sb.append(robotID);
 		sb.append("c");
 		sb.append(boardData);
 		IOHandler.save(sb.toString(), gameID, IOHandler.CLIENT_DATA);
 	}
 	
 	/**
 	 * Gives an array of gameID's of all the games the client is playing.
 	 * @return
 	 */
 	public int[] getSavedGames() {
 		return IOHandler.getGameIDs();
 	}
 	
 	/*
 	 * Creates a new action by reading the string provided.
 	 */
 	private SingleAction createSingleAction(String indata) {
 		String[] data = indata.split(":");
 		return new SingleAction(
 				Integer.parseInt(data[0]), 
 				Integer.parseInt(data[1].substring(0, 1)),
 				Integer.parseInt(data[1].substring(1, 3)),
 				Integer.parseInt(data[1].substring(3, 5)));
 	}
 	
 	/**
 	 * Gives the client's cards.
 	 * @return A list of the client's cards.
 	 */
 	public String loadCards() {	
 		String temp = controller.getCards(roundID + 1, robotID);
 		System.out.println("To client: \"" + temp + "\"");
 		return temp;
 	}
 	
 	public List<RobotView> getRobots(Texture texture) {
 		return getRobots(texture, null);
 	}
 	
 	/**
 	 * Gives all the players robots in the current game as a list.
 	 * @param texture The textures to use when displaying the robots.
 	 * @param dockPositions All the docks' positions.
 	 * @return A list of all the robots.
 	 */
 	public List<RobotView> getRobots(Texture texture, Vector2[] dockPositions) {	
 		// TODO: server input
 		System.out.println("To client: \"" + controller.getNbrOfRobots() + "\"");
 		List<RobotView> robots = new ArrayList<RobotView>();	
 		for(int i = 0; i < Integer.parseInt(controller.getNbrOfRobots()); i++) {
 			RobotView robot = new RobotView(i, new TextureRegion(texture, i * 64, 448, 64, 64),
 					new LaserView(new TextureRegion(texture, 64 * i, 384, 64, 64), 0), 
					"Player " + i);
 			if(dockPositions != null) {
 				robot.setPosition(dockPositions[i].x, dockPositions[i].y);
 			}
 			robot.setOrigin(20, 20);
 			robots.add(robot);
 		}		
 		return robots;
 	}
 	
 	/**
 	 * Gives the data needed when loading a game. E.g. the length of the timers.
 <<<<<<< HEAD
 	 * @return
 =======
 	 * @return The data needed when loading a game.
 >>>>>>> origin/keepDataOldRounds
 	 */
 	public String getGameData() {
 		return controller.getInitGameData();
 	}
 
 	/**
 	 * Increments the seen rounds by one.
 	 */
 	public void incrementRound() {
 		this.roundID++;
 	}
 	
 	public int getRoundID() {
 		return this.roundID;
 	}
 }
