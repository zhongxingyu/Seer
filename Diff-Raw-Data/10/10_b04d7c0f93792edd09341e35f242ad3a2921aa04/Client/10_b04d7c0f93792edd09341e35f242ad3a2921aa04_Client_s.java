 package se.chalmers.dryleafsoftware.androidrally.libgdx;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.GameAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.MultiAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.SingleAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.RobotView;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.gameModel.GameModel;
 
 import com.badlogic.gdx.graphics.Texture;
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
 
 	// TODO: the client must somehow know which robotID the player has.
 	private final GameModel model;
 	private final int clientID;
 	
 //	String indata1 = "0:30906;0:20906";
 	// B = board element push
 	// P = robot push 
 	// R#[card nbr] = indicates new card
 	String indata1 = "R#0;1:10906;1:00906;0:00509;B#0:10509#1:00905;R#1;0:10609;0:00609;0:00608;0:00607;B#0:00507" +
				";B#1:00904#0:00506;R#2;0:00504;B#0:30504";
 	
 	/**
 	 * Creates a new client instance.
 	 * @param clientID The ID number of the player.
 	 */
 	public Client(int clientID) {
 		this.model = new GameModel(8);// TODO: remove
 		this.clientID = clientID;
 	}
 	
 	/**
 	 * Returns the map of the board as a matrix of strings.
 	 * @return A map of the board as a matrix of strings.
 	 */
 	public String[][] getMap() {
 		return model.getMap();// TODO: server output
 	}
 	
 	/**
 	 * Sends the cards to the server. Note: This list should not contain more then five!
 	 * @param cards The cards to send.
 	 * @return <code>true</code> if the client successfully sent the cards.
 	 */
 	public boolean sendCard(List<CardView> cards) {
 		// Send example: 12345:0:7:1:4:-1"
 		StringBuilder sb = new StringBuilder("" + clientID);
 		for(int i = 0; i < 5; i++) {
 			if(cards.size() > i) {
 				sb.append(":" + cards.get(i).getIndex());
 			}else{
 				sb.append(":-1");
 			}
 		}
 		// TODO: send to server
 		System.out.println("Client sending card: " + sb.toString());
 		return true;
 	}
 	
 	/**
 	 * Gives all the actions which was created during the last round.
 	 * @return A list of all the actions was created during the last round.
 	 */
 //	public List<GameAction> getRoundResult() {
 	public RoundResult getRoundResult() {
 		// From server example: "0:10101;0:10102;1:10203"	
 		RoundResult result = new RoundResult();	
 		String[] allActions = indata1.split(";");// TODO: server input
 		for(String s : allActions) {
 			String[] parallel = s.split("#");
 			if(parallel.length >= 2) {	
 				if(parallel[0].equals("R")) { // New phase
 					result.newPhase();
 				}else{
 					MultiAction multiAction = new MultiAction();
 					for(int i = 1; i < parallel.length; i++) {
 						multiAction.add(createSingleAction(parallel[i]));
 					}				
 					if(parallel[0].equals("B")) { // Conveyer belt
 						multiAction.setMoveRound(GameAction.PHASE_BOARD_ELEMENT);
 					}else if(parallel[0].equals("P")) { // Robot push
 						multiAction.setMoveRound(GameAction.PHASE_PUSHED);
 					}
 					result.addAction(multiAction);
 				}
 			}else{
 				result.addAction(createSingleAction(parallel[0]));
 			}
 		}
 		return result;
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
 	public List<CardView> getCards(Texture texture) {
 		// From server example: "410:420:480:660:780:840:190:200:90"
 		model.dealCards();// TODO: server input
 		List<CardView> cards = new ArrayList<CardView>();
 		
 		// TODO: change to robotID and input to string
 		for(int i = 0; i < model.getRobots().get(0).getCards().size(); i++) {
 			Card card = model.getRobots().get(0).getCards().get(i);
 			int prio = card.getPriority();
 			int regX = 0;
 			if(prio <= 60) {
 				regX = 0;	// UTURN
 			}else if(prio <= 410 && prio % 20 != 0) {
 				regX = 64;	// LEFT
 			}else if(prio <= 420 && prio % 20 == 0) {
 				regX = 128;	// LEFT
 			}else if(prio <= 480) {
 				regX = 192;	// Back 1
 			}else if(prio <= 660) {
 				regX = 256;	// Move 1
 			}else if(prio <= 780) {
 				regX = 320;	// Move 2
 			}else if(prio <= 840) {
 				regX = 384;	// Move 3
 			}				
 			CardView cv = new CardView(new TextureRegion(texture, regX, 0, 64, 90), 
 				card.getPriority(), i);
 			cv.setSize(78, 110);
 			cards.add(cv);
 		}
 		Collections.sort(cards);
 		return cards;
 	}
 	
 	/**
 	 * Gives all the players robots in the current game as a list.
 	 * @param texture The textures to use when displaying the robots.
 	 * @param dockPositions All the docks' positions.
 	 * @return A list of all the robots.
 	 */
 	public List<RobotView> getRobots(Texture texture, Vector2[] dockPositions) {
 		// From server example: "
 		// TODO: server input
 		List<RobotView> robots = new ArrayList<RobotView>();		
 		for(int i = 0; i < model.getRobots().size(); i++) {
 			RobotView robot = new RobotView(i, new TextureRegion(texture, i * 64, 64, 64, 64));
 			robot.setPosition(dockPositions[i].x, dockPositions[i].y);
 			robot.setOrigin(20, 20);
 			robots.add(robot);
 		}		
 		return robots;
 	}
 }
