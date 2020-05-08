 package se.chalmers.dryleafsoftware.androidrally.libgdx;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.controller.GameController;
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
 
 	// TODO: the client must somehow know which robotID the player has.
 	private final se.chalmers.dryleafsoftware.androidrally.controller.GameController controller;
 	private final int clientID, robotID;
 	// TODO: load the clientID from the user's phone's data.
 	// TODO: save the clientID when assigned one from the server.
 		
 	/**
 	 * Creates a new client instance.
 	 * @param clientID The ID number of the player.
 	 */
 	public Client(int clientID) {
 		this.controller = new GameController(8, 24, 40); 
 		this.clientID = clientID;
 		this.robotID = 0;
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
 		for(int i = 0; i < 8; i++) {
 			if(i != robotID) {
 				controller.setChosenCardsToRobot(i, "-1:-1:-1:-1:-1"); // TODO: remove
 			}
 		}
 	}
 	
 	/**
 	 * Gives all the actions which was created during the last round.
 	 * @return A list of all the actions was created during the last round.
 	 */
 	public RoundResult getRoundResult() {		
 		RoundResult result = new RoundResult();	
 		String indata = controller.getRoundResults();		
 		String[] allActions = indata.split(";");
 		
 		Texture damageAnim = new Texture(Gdx.files.internal("textures/special/damageAnim.png"));
 		damageAnim.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		Texture explodeAnim = new Texture(Gdx.files.internal("textures/special/explodeAnim.png"));
 		damageAnim.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		
 		System.out.println("To client: \"" + indata + "\"");
 		
 		for(String s : allActions) {
 			String[] parallel = s.split("#");
 			if(parallel[0].equals("R")) {
 				result.newPhase();
 			}else if(parallel[0].substring(0, 1).equals("B")) {
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
 //						result.addAction(new HealthAction(id, 0, HealthAction.DECREASE_ONE));
 					}
 				}else if(phase == GameAction.PHASE_CHECKPOINT) {
 					// TODO: checkpoint action!
 					for(int i = 1; i < parallel.length; i++) {
 						String[] data = parallel[i].split(":");
 						System.out.println("Robot: " + data[0] + ", reached checkpoint: " + data[1]);
 						result.addAction(new CheckPointAction(Integer.parseInt(data[0]),
 								Integer.parseInt(data[1]), false));
 					}
 				}else{
 					GameAction action;
 					// If no actions follows:
 					if(parallel.length == 1) {
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
 		String temp = controller.getCards(robotID);
 		System.out.println("To client: \"" + temp + "\"");
 		return temp;
 	}
 	
 	/**
 	 * Gives all the players robots in the current game as a list.
 	 * @param texture The textures to use when displaying the robots.
 	 * @param dockPositions All the docks' positions.
 	 * @return A list of all the robots.
 	 */
 	public List<RobotView> getRobots(Texture texture, Vector2[] dockPositions) {	
 		// TODO: server input
 		System.out.println("To client: \"" + controller.getNbrOfPlayers() + "\"");
 		List<RobotView> robots = new ArrayList<RobotView>();	
 		for(int i = 0; i < Integer.parseInt(controller.getNbrOfPlayers()); i++) {
 			RobotView robot = new RobotView(i, new TextureRegion(texture, i * 64, 448, 64, 64),
 					new LaserView(new TextureRegion(texture, 64 * i, 384, 64, 64), 0), "Player " + i);
 			robot.setPosition(dockPositions[i].x, dockPositions[i].y);
 			robot.setOrigin(20, 20);
 			robots.add(robot);
 		}		
 		return robots;
 	}
 }
