 package org.aimas.craftingquest.core;
 
 import java.util.List;
 
 import org.aimas.craftingquest.core.actions.Action;
 import org.aimas.craftingquest.core.energyreplenishmodels.EnergyReplenishModel;
 import org.aimas.craftingquest.state.BasicUnit;
 import org.aimas.craftingquest.state.CellState;
 import org.aimas.craftingquest.state.GameState;
 import org.aimas.craftingquest.state.PlayerState;
 import org.aimas.craftingquest.state.Point2i;
 import org.aimas.craftingquest.state.Transition;
 import org.aimas.craftingquest.state.Transition.ActionType;
 import org.aimas.craftingquest.state.TransitionResult;
 import org.aimas.craftingquest.state.TransitionResult.TransitionError;
 import org.aimas.craftingquest.state.UnitState;
 import org.aimas.craftingquest.state.objects.Tower;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @author alex
  */
 public class ActionEngine {
 
 	private static Logger gui_logger = Logger.getLogger("org.aimas.craftingquest.core.guilogger");
 	
 	GameState game;
 
 	public ActionEngine(GameState gameState) {
 		game = gameState;
 	}
 
 	
 	public TransitionResult process(PlayerState player, Transition transition) {
 		refresh(player);
 		
 		// first check if the operator is of Nothing or RequestState type
 		// these are just for filling up and synchronization - so return the OK
 		if(transition.operator == ActionType.Nothing || transition.operator == ActionType.RequestState) {
 			TransitionResult res = new TransitionResult(transition.id);
 			res.errorType = TransitionResult.TransitionError.NoError;
 			return res;
 		}
 		else {
 			// if not it must be a game action
 			Action playerAction = Action.getInstance(transition.operator);
 			if (playerAction == null) {
 				return new TransitionResult(transition.id, TransitionError.UnknownActionError, "Unknown Action");
 			}
 			else {
 				TransitionResult res = playerAction.doAction(game, player, transition);
 				
 				// if no error then log the action for GUI
 				if (res.valid()) {
 					playerAction.printToGuiLog(game, player, transition);
 				}
 				
 				return res;
 			}
 		}
 		
 	}
 	
 	
 	/* =========================================================================================== */
 	
 	
 	// clears transient fields (currentCellResources, scannedAttributes) of each of the players units
 	// removes dead units
 	private void refresh(PlayerState player) {
 		player.response = null;							// reset player transition response
 
 		for (UnitState unit : player.units) {			// reset each unit's dig results
 			unit.currentCellResources.clear();
 			unit.retaliateEnergy = 0;					// and retaliateEnergy points
 			unit.retaliateThreshold = 0;
 		}
 	}
 	
 	protected void unfreeze(GameState state, Integer playerID) {
 		PlayerState playerState = state.playerStates.get(playerID);
 		playerState.unfreeze();
 	}
 	
 	protected void doTowerDrain(GameState state, Integer playerID) {
 		PlayerState playerState = state.playerStates.get(playerID);
 		
 		for (UnitState unit : playerState.units) {
 			List<Tower> opponentTowers = state.getOpponentTowers(playerID);
 			
 			for (Tower oppTower : opponentTowers) {
 				if (Math.abs(oppTower.getPosition().x - unit.pos.x) <= oppTower.getRange() && 
 					Math.abs(oppTower.getPosition().y - unit.pos.y) <= oppTower.getRange()) {
 					
 					int distance = Math.min( Math.abs(oppTower.getPosition().x - unit.pos.x), Math.abs(oppTower.getPosition().y - unit.pos.y) );
 					if (distance == 0) {	// can happen if a player constructs a tower in a cell
 						distance = 1;		// that contains an opponents unit
 					}
 					int drainAmount = oppTower.getDrain() / distance;
 					
 					unit.energy -= drainAmount;						// drain unit energy
 					oppTower.weakenTower(drainAmount);				// and also weaken tower with the same amount
 					
 					if (oppTower.getRemainingStrength() <= 0) {		// if the tower has been weakened enough => destroy it
 						state.playerStates.get(oppTower.getPlayerID()).availableTowers.remove(oppTower);
 						gui_logger.info(state.round.currentRound + " RemoveTower " + oppTower.getPosition().x + " " + oppTower.getPosition().y);
 					}
 					
 				}
 			}
 				
 		}
 	}
 	
 	
 	protected void updatePlayerSight(GameState state, Integer playerID) {
 		PlayerState playerState = state.playerStates.get(playerID);
 		for (UnitState unit : playerState.units) {
 			updateUnitSight(state, unit);
 		}
 	}
 	
 	private void updateUnitSight(GameState game, UnitState playerUnit) {
 		int sightRadius = GamePolicy.sightRadius;
 		Point2i pos = playerUnit.pos;
 		
 		CellState[][] unitSight = playerUnit.sight;
 		for (int i = 0, y = pos.y - sightRadius; y <= pos.y + sightRadius; y++, i++) {
 			for (int j = 0, x = pos.x - sightRadius; x <= pos.x + sightRadius; x++, j++) {
 				unitSight[i][j] = null;
 				if (x >= 0 && x < GamePolicy.mapsize.x && y >= 0 && y < GamePolicy.mapsize.y) {
 					// update opponent perspective before updating unit sight
 					updateOpponentPerspective(game.map.cells[y][x]);
 					unitSight[i][j] = game.map.cells[y][x];
 				}
 			}
 		}
 	}
 	
 	private void updateOpponentPerspective(CellState cellState) {
 		// update the BasicUnit opponent perspective with the stats of the actual
 		// UnitState units that are situated in this cell
 		for (BasicUnit bu : cellState.cellUnits) {
 			PlayerState player = game.playerStates.get(bu.playerID);
 			
 			for (UnitState unit : player.units) {
 				if (unit.id == bu.unitId) {
 					int attackLevel = (unit.equipedSword != null)? unit.equipedSword.getLevel() : 0;
					int defenceLevel = (unit.equipedArmour != null)? unit.equipedArmour.getLevel() : 0;
 					bu.updateStats(unit.energy, unit.life, attackLevel, defenceLevel);
 					
 					break;
 				}
 			}
 		}
 	}
 
 
 	protected void updateTowerSight(GameState state, Integer playerID) {
 		PlayerState playerState = state.playerStates.get(playerID);
 		
 		for (Tower tower : playerState.availableTowers) {
 			int sightRadius = tower.getRange();
 			Point2i towerPos = tower.getPosition();
 			
 			int len = 2 * sightRadius + 1;
 			CellState[][] towerSight = new CellState[len][len];
 			
 			for (int i = 0, y = towerPos.y - sightRadius; y <= towerPos.y + sightRadius; y++, i++) {
 				for (int j = 0, x = towerPos.x - sightRadius; x <= towerPos.x + sightRadius; x++, j++) {
 					towerSight[i][j] = null;
 					if (x >= 0 && x < GamePolicy.mapsize.x && y >= 0 && y < GamePolicy.mapsize.y) {
 						towerSight[i][j] = game.map.cells[y][x];
 					}
 				}
 			}
 			
 			// update tower sight
 			tower.sight = towerSight;
 		}
 	}
 	
 	
 	protected void replenishEnergy() {
 		for (PlayerState pState : game.playerStates.values()) {
 			for (UnitState unit : pState.units) {
 				if (pState.isFrozen(unit))
 					unit.energy = 0;
 				else
 					unit.energy = EnergyReplenishModel
 						.getInstance(GamePolicy.energyReplenishModel)
 						.replenishEnergy(unit.energy, unit.life);
 			}
 		}
 	}
 }
