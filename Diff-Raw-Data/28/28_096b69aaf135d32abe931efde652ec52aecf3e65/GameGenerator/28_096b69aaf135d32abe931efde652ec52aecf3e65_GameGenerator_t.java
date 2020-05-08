 package org.aimas.craftingquest.core;
 
 import java.io.EOFException;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeSet;
 
 import org.aimas.craftingquest.state.Blueprint;
 import org.aimas.craftingquest.state.CellState;
 import org.aimas.craftingquest.state.GameState;
 import org.aimas.craftingquest.state.MapState;
 import org.aimas.craftingquest.state.PlayerState;
 import org.aimas.craftingquest.state.Point2i;
 import org.aimas.craftingquest.state.UnitState;
 import org.aimas.craftingquest.state.CellState.CellType;
 import org.aimas.craftingquest.state.resources.ResourceType;
 
 
 public class GameGenerator {
 	public static final int SMALL_ROUND = 100;
 	public static final int MEDIUM_ROUND = 125;
 	public static final int BIG_ROUND = 150;
 	
 	
 	public static GameState setupGame() {
 		/* initialize scenario */
 		GamePolicy.initScenario();
 		
 		GameState game = checkForSavedGame();
 		//GameState game = null;
 		
 		/* create initial game state if saved state does not exist */
 		if (game == null) {
 			game = new GameState();
 			game.map = GamePolicy.map;
 			
 			/* set map resources */
 //			HashMap<ResourceType, Integer> resourceAmountsByType = ResourceGenerator.placeResources(game.map);
 //			GamePolicy.blueprints = ResourceGenerator.generateBlueprints(resourceAmountsByType);		// generate blueprints
 //			game.resourceAmountsByType = resourceAmountsByType;
 			
 			/* setup initial player states - there should be a maximum of 4 players */
 			for (int i = 1; i <= GamePolicy.nrPlayers; i++) {
 				if (i % GamePolicy.maxPlayers == 1) {
 					PlayerState player = setupPlayerState(i, GamePolicy.nrPlayerUnits, GamePolicy.initialPlayerPositions.get(i), game.map);
 					game.playerStates.put(player.id, player);
 					game.playerIds.add(player.id);
 				}
 				else if (i % GamePolicy.maxPlayers == 2) {
 					PlayerState player = setupPlayerState(i, GamePolicy.nrPlayerUnits, GamePolicy.initialPlayerPositions.get(i), game.map);
 					game.playerStates.put(player.id, player);
 					game.playerIds.add(player.id);
 				} 
 				else if (i % GamePolicy.maxPlayers == 3) {
 					PlayerState player = setupPlayerState(i, GamePolicy.nrPlayerUnits, GamePolicy.initialPlayerPositions.get(i), game.map);
 					game.playerStates.put(player.id, player);
 					game.playerIds.add(player.id);
 				}
 				else {
 					PlayerState player = setupPlayerState(i, GamePolicy.nrPlayerUnits, GamePolicy.initialPlayerPositions.get(i), game.map);
 					game.playerStates.put(player.id, player);
 					game.playerIds.add(player.id);
 				}
 			}
 		}
 		else {
 			/*reset number of turns according to map size*/
 			if (game.map.mapWidth >= 60 && game.map.mapWidth < 80) {
 				game.round.noRounds = SMALL_ROUND;
 			}
 			
 			if (game.map.mapWidth >= 80 && game.map.mapWidth < 100) {
 				game.round.noRounds = MEDIUM_ROUND;
 			}
 			
 			if (MapReader.mapWidth >= 100) {
 				game.round.noRounds = BIG_ROUND;
 			}
 			
 			/* setup initial player states - there should be only 2 players */
 			game.playerStates.clear();
 			
 			for(int y = 0; y < game.map.mapHeight; y++) {
 				for(int x = 0; x < game.map.mapWidth; x++) {
 					game.map.cells[y][x].cellUnits.clear();
 				}
 			}
 			
 			for (int i = 1; i <= GamePolicy.nrPlayers; i++) {
 				if (i % GamePolicy.maxPlayers == 1) {
					PlayerState player = setupPlayerState(i, GamePolicy.nrPlayerUnits, GamePolicy.initialPlayerPositions.get(i), game.map);
 					game.playerStates.put(player.id, player);
 					game.playerIds.add(player.id);
 				}
 				else if (i % GamePolicy.maxPlayers == 2) {
					PlayerState player = setupPlayerState(i, GamePolicy.nrPlayerUnits, GamePolicy.initialPlayerPositions.get(i), game.map);
 					game.playerStates.put(player.id, player);
 					game.playerIds.add(player.id);
 				}
 				else if (i % GamePolicy.maxPlayers == 3) {
					PlayerState player = setupPlayerState(i, GamePolicy.nrPlayerUnits, GamePolicy.initialPlayerPositions.get(i), game.map);
 					game.playerStates.put(player.id, player);
 					game.playerIds.add(player.id);
 				}
 				else {
					PlayerState player = setupPlayerState(i, GamePolicy.nrPlayerUnits, GamePolicy.initialPlayerPositions.get(i), game.map);
 					game.playerStates.put(player.id, player);
 					game.playerIds.add(player.id);
 				}
 			}
 		}
 		
 		//printResourceStatistics(game.resourceAmountsByType, GamePolicy.blueprints);
 		
 		return game;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static GameState checkForSavedGame() {
 		String savedResFilename = GamePolicy.mapName + ".cqres";
 		String mapFile = "maps/" + savedResFilename;
 		
 		ObjectInputStream objin = null;
 		GameState game = null;
 		HashMap<Point2i, HashMap<ResourceType, Integer>> cellResources = null;
 		
 		try {
 			objin = new ObjectInputStream (new FileInputStream(mapFile));
 			
 			Object obj = objin.readObject();
 			if (obj == null || !(obj instanceof GameState)) {
 				return null;
 			}
 			
 			game = (GameState)obj;
 						
 			cellResources = (HashMap<Point2i, HashMap<ResourceType, Integer>>)objin.readObject();
 			if (cellResources == null) {
 				return null;
 			}
 
 			for (Point2i p : cellResources.keySet()) {
 				game.map.cells[p.y][p.x].resources = new HashMap<ResourceType, Integer>(cellResources.get(p));
 				game.map.cells[p.y][p.x].resourceTypes = new TreeSet<ResourceType>(cellResources.get(p).keySet());
 			}
 			
 		}catch (EOFException ex) { 
             System.out.println("End of file reached.");
         } catch (ClassNotFoundException ex) {
             ex.printStackTrace();
         }catch(FileNotFoundException e) {
 			System.out.println("No .cqres file found. Will build fresh game state.");
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		finally{
 			//Close the ObjectInputStream
             try {
                 if (objin != null) {
                     objin.close();
                 }
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
 		}
 	
 		return game;
 	}
 	
 	private static PlayerState setupPlayerState(int playerID, int nrUnits, Point2i initPos, MapState map) {
 		PlayerState pState = new PlayerState();
 		
 		pState.id = playerID;
 		pState.gold = GamePolicy.initialTeamGold;
 		
 		pState.round.currentRound = 0;
 		pState.round.noRounds = GamePolicy.lastTurn;
 		pState.round.roundDuration = GamePolicy.playerActionTime;
 		
 		pState.mapHeight = map.mapHeight;
 		pState.mapWidth = map.mapWidth;
 		
 		// setup player blueprints - level 1 for each craftable object
 		for (Blueprint bp : GamePolicy.blueprints) {
 			if (bp.getLevel() == 1) {
 				pState.availableBlueprints.add(bp);
 			}
 		}
 		
 		
 		// setup player units
 		for (int i = 0; i < nrUnits; i++) {
 			// search for a position for the unit in the vicinity of the given initial position
 			Point2i unitPos = null;
 			if (initPos.x < GamePolicy.mapsize.x / 2) {
 				unitPos = setUnitInitialPosition1(map, initPos.x - 2, initPos.x + 8, initPos.y - 2, initPos.y + 8);
 				if (unitPos == null) {
 					unitPos = setUnitInitialPosition1(map,  initPos.x - 2, GamePolicy.mapsize.x / 2, initPos.y - 2, GamePolicy.mapsize.y / 2);
 				}
 			} else {
 				unitPos = setUnitInitialPosition2(map,  initPos.x - 8, initPos.x + 2, initPos.y - 8, initPos.y + 2);
 				if (unitPos == null) {
 					unitPos = setUnitInitialPosition2(map, GamePolicy.mapsize.x / 2, initPos.x + 2, GamePolicy.mapsize.y / 2, initPos.y + 2);
 				}
 			}
 
 			if (unitPos == null) { // it should never come to this
 				throw new NullPointerException("Unit initial position error for player: " + playerID);
 			}
 			
 			int unitID = playerID * nrUnits + i;
 			UnitState unit = new UnitState(unitID, playerID, unitPos, GamePolicy.initialUnitMaxLife, GamePolicy.sightRadius);
 			
 			// set unit's sight
 			for (int ii = 0, y = unit.pos.y - GamePolicy.sightRadius; y <= unit.pos.y + GamePolicy.sightRadius; y++, ii++) {
 				for (int jj = 0, x = unit.pos.x - GamePolicy.sightRadius; x <= unit.pos.x + GamePolicy.sightRadius; x++, jj++) {
 					unit.sight[ii][jj] = null;
 					if (x >= 0 && x < GamePolicy.mapsize.x && y >= 0 && y < GamePolicy.mapsize.y) {
 						unit.sight[ii][jj] = map.cells[y][x];
 					}
 				}
 			}
 			
 			pState.units.add(unit); // add unit to player's list
 			map.cells[unit.pos.y][unit.pos.x].cellUnits.add(unit.getOpponentPerspective()); // add unit to its cell
 			
 		}
 		
 		return pState;
 	}
 	
 	private static Point2i setUnitInitialPosition1(MapState map, int xmin, int xmax, int ymin, int ymax) {
 		for(int y = ymin; y < ymax; y++) {
 			for(int x = xmin; x < xmax; x++) {
 				CellState cell = map.cells[y][x];
 				if (cell.cellUnits.isEmpty() && cell.type == CellType.Grass) {
 					return new Point2i(x, y);
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	private static Point2i setUnitInitialPosition2(MapState map, int xmin, int xmax, int ymin, int ymax) {
 		for(int y = ymax; y > ymin; y--) {
 			for(int x = xmax; x > xmin; x--) {
 				CellState cell = map.cells[y][x];
 				if (cell.cellUnits.isEmpty() && cell.type == CellType.Grass) {
 					return new Point2i(x, y);
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	private static void printResourceStatistics(HashMap<ResourceType, Integer> resourceAmountsByType, 
 			List<Blueprint> blueprints) {
 		try {
 			FileWriter fw = new FileWriter("resource-stats.txt");
 			fw.write("======== Resources -- Quantity ========");
 			fw.write("\n");
 			for (ResourceType br : resourceAmountsByType.keySet()) {
 				fw.write(br.name() + ": " + resourceAmountsByType.get(br) + "\n");
 			}
 			
 			fw.write("\n");
 			fw.write("======== Objects -- Values ========");
 			fw.write("\n");
 			for (Blueprint bp : blueprints) {
 				fw.write(bp.getType() + ":" + bp.getLevel() + "\n");
 			}
 			
 			fw.close();
 		}catch(IOException ioe) {
 			ioe.printStackTrace();
 		}
 		
 	}
 
 }
