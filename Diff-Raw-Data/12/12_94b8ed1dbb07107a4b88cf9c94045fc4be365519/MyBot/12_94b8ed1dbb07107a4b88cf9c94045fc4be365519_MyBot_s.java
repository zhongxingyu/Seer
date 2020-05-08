 import java.io.IOException;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Starter bot implementation.
  */
 public class MyBot extends Bot {
 	public static boolean seenTiles[][];
 	public static Ants ants = null;
	public static int turnNum = 0;
 		
 	public static AntPopulation antPop = new AntPopulation();
 	
 	// Stores enemy ants hills in case we die and it disappears
 	public static Set<Tile> enemyAntHills = new HashSet<Tile>();
 	
 
 	/**
 	 * Main method executed by the game engine for starting the bot.
 	 * 
 	 * @param args
 	 *            command line arguments
 	 * 
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public static void main(String[] args) throws IOException {
 		new MyBot().readSystemInput();
 	}
 
 	public boolean attemptMovement(Ants ants, HashMap<Tile, Tile> orders, Tile antLoc, Aim direction) {
 		// Track all moves, prevent collisions
 		Tile newLoc = ants.getTile(antLoc, direction);
 		if (ants.getIlk(newLoc).isUnoccupied() && !orders.containsKey(newLoc)) {
 			ants.issueOrder(antLoc, direction);
 			orders.put(newLoc, antLoc);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public void setup(int loadTime, int turnTime, int rows, int cols, int turns, int viewRadius2, int attackRadius2, int spawnRadius2) {
 		super.setup(loadTime, turnTime, rows, cols, turns, viewRadius2, attackRadius2, spawnRadius2);
 
 		seenTiles = new boolean[rows][cols];
 		setAllUnseen();
 	}
 	
 	@Override
 	public void removeAnt(int row, int col, int owner) {
 		super.removeAnt(row, col, owner);
 		
 		if(owner == 0) {
 			Ant ant = antPop.getAntAtRowCol(row, col);
 			Util.addToLog("Ant " + ant.getAntID() + ": Has 'retired' at " + ant.getPosition());
 			antPop.remove(ant);
 		}
 	}
 
 	@Override
 	public void doTurn() {
 		turnNum++;
 		HashMap<Tile, Tile> orders = new HashMap<Tile, Tile>();
 		MyBot.ants = getAnts();
 		
 		antPop.updateAntPopulation();
 		
 		updateSeen(ants);
 
 		Util.addToLog("------- Desicions for turn " + turnNum + " -------");
 		for(Ant ant : antPop) {
 			List<Aim> desiredMovement = ant.makeMovementDecision();
 			
 			desiredMovement = Util.removeIllegalMoves(desiredMovement, ant.getPosition());
 			
 			for (Aim direction : desiredMovement) {
 				if (attemptMovement(ants, orders, ant.getPosition(), direction)) {
 					Tile newPos = ants.getTile(ant.getPosition(), direction);
 					ant.setPosition(newPos);
 					ant.setLastDirection(direction);
 					break;
 				}
 			}
 			
 		}
 		Util.addToLog("-------------------------");
 		
 	}
 
 	public void setAllUnseen() {
 		for (int row = 0; row < seenTiles.length; row++) {
 			Arrays.fill(seenTiles[row], false);
 		}
 	}
 
 	public void updateSeen(Ants ants) {
 		Util.addToLog("---------- Map for turn " + turnNum + " ------------");
 		for (int row = 0; row < ants.getRows(); row++) {
 			String rowstatus = String.format("%3d ", row);			
 			for (int col = 0; col < ants.getCols(); col++) {
 				for (Tile ant : ants.getMyAnts()) {
 					if (ants.getDistance(ant, new Tile(row, col)) < ants.getViewRadius2()) {
 						seenTiles[row][col] = true;
 					}
 				}
 				if (seenTiles[row][col]) {
 					Tile currentTile = new Tile(row, col);
 
 					Ilk target = ants.getIlk(currentTile);
 					switch (target) {
 					case DEAD:
 						rowstatus += "x";
 						break;
 					case ENEMY_ANT:
 						rowstatus += "e";
 						break;
 					case FOOD:
 						if(ants.getFoodTiles().contains(currentTile)) {
 							rowstatus += "f";
 							break;							
 						} else {
 							ants.update(Ilk.LAND, currentTile);
 							rowstatus += " ";
 							break;
 						}
 					case LAND:
 						if(ants.getEnemyHills().contains(currentTile)) {
 							rowstatus += "E";
 							MyBot.enemyAntHills.add(currentTile);
 						} else {
 							rowstatus += " ";
 						}
 						break;
 					case MY_ANT:
 						rowstatus += "M";
 						break;
 					case WATER:
 						rowstatus += "w";
 						break;
 					}
 
 				} else {
 					rowstatus += "?";
 				}
 
 			}
 			Util.addToLog(rowstatus);
 		}
 		Util.addToLog("---------------------------");
 	}
 }
