 package org.aimas.craftingquest.core.actions;
 
 import java.util.Iterator;
 
 import org.aimas.craftingquest.core.GamePolicy;
 import org.aimas.craftingquest.state.BasicUnit;
 import org.aimas.craftingquest.state.CellState;
 import org.aimas.craftingquest.state.CellState.CellType;
 import org.aimas.craftingquest.state.GameState;
 import org.aimas.craftingquest.state.ICarriable;
 import org.aimas.craftingquest.state.PlayerState;
 import org.aimas.craftingquest.state.Point2i;
 import org.aimas.craftingquest.state.Transition;
 import org.aimas.craftingquest.state.Transition.ActionType;
 import org.aimas.craftingquest.state.TransitionResult;
 import org.aimas.craftingquest.state.UnitState;
 import org.aimas.craftingquest.state.objects.ICrafted;
 import org.aimas.craftingquest.state.objects.Tower;
 import org.aimas.craftingquest.state.objects.TrapObject;
 import org.aimas.craftingquest.state.resources.ResourceType;
 import org.apache.log4j.Logger;
 
 public class MoveAction extends Action {
 	private static Logger gui_logger = Logger.getLogger("org.aimas.craftingquest.core.guilogger");
 	
 	public MoveAction(ActionType type) {
 		super(type);
 	}
 
 	@Override
 	protected TransitionResult handle(GameState game, PlayerState player, Transition transition) {
 		// check allowed distance
 		Point2i toPos = (Point2i) transition.operands[1];
 		Point2i fromPos = playerUnit.pos;
 		if (Math.abs(toPos.x - fromPos.x) > 1 || Math.abs(toPos.y - fromPos.y) > 1) {
 			TransitionResult res = new TransitionResult(transition.id);
 			res.errorType = TransitionResult.TransitionError.MoveError;
 			res.errorReason = "Move allowed only to neighboring cells";
 			return res;
 		}
 
 		// check position bounds
 		if (toPos.x < 0 || toPos.y < 0 || toPos.x >= game.map.cells.length
 				|| toPos.y >= game.map.cells.length) {
 			TransitionResult res = new TransitionResult(transition.id);
 			res.errorType = TransitionResult.TransitionError.MoveError;
 			res.errorReason = "Move allowed only within map bounds";
 			return res;
 		}
 		
 		// check terrain type
 		if(game.map.cells[toPos.y][toPos.x].type == CellType.Rock) {
 			TransitionResult res = new TransitionResult(transition.id);
 			res.errorType = TransitionResult.TransitionError.TerrainError;
 			res.errorReason = "Move not allowed to cells with rocky terrain.";
 			return res;
 		}
 		
 		// check no object is there
 		if (game.map.cells[toPos.y][toPos.x].strategicObject != null
 				&& game.map.cells[toPos.y][toPos.x].strategicObject instanceof Tower) {
 			TransitionResult res = new TransitionResult(transition.id);
 			res.errorType = TransitionResult.TransitionError.ObstacleError;
 			res.errorReason = "Move not allowed to cells containing strategic structures.";
 			return res;
 		}
 		
 		
 		// check enough energy for action
 		int carriedResourcesWeight = 0;
 		Iterator<ICrafted> coit = (Iterator<ICrafted>) playerUnit.carriedObjects.keySet().iterator();
 		while (coit.hasNext()) {
 			ICarriable co = (ICarriable) coit.next();
 			carriedResourcesWeight += co.getWeight() * playerUnit.carriedObjects.get(co);
 		}
 		Iterator<ResourceType> crit = (Iterator<ResourceType>) playerUnit.carriedResources.keySet().iterator();
 		while(crit.hasNext()) {
 			ResourceType rt = crit.next();
 			carriedResourcesWeight += rt.getWeight() * playerUnit.carriedResources.get(rt);
 		}
 		
 		int requiredEnergy = (int) (GamePolicy.moveBase * (1 + 
 				GamePolicy.movePenaltyWeight * carriedResourcesWeight / 100.0 ));
 
 		if (playerUnit.energy < requiredEnergy) {
 			TransitionResult res = new TransitionResult(transition.id);
 			res.errorType = TransitionResult.TransitionError.NoEnergyError;
 			res.errorReason = "Not enough energy points left for move";
 			return res;
 		}
 
 		// all ok..then move - update unit position, sightMatrix and energy
 		// points
 		playerUnit.pos = toPos;
 		playerUnit.energy -= requiredEnergy;
 		// updateUnitSight(game, playerUnit, toPos);
 
 		// update cells with new unit position
 		Iterator<BasicUnit> it = game.map.cells[fromPos.y][fromPos.x].cellUnits.iterator();
 		while (it.hasNext()) {
 			BasicUnit u = it.next();
			if (u.playerID == playerUnit.playerID) {
 				it.remove();
 				break;
 			}
 		}
 		game.map.cells[toPos.y][toPos.x].cellUnits.add(playerUnit.getOpponentPerspective());
 		
 		
 		// check if there is a trap in the new position - if so mark the opponent unit as frozen
 		if (game.map.cells[toPos.y][toPos.x].strategicObject != null
 				&& game.map.cells[toPos.y][toPos.x].strategicObject instanceof TrapObject) {
 			TrapObject trap = (TrapObject) game.map.cells[toPos.y][toPos.x].strategicObject;
 			playerUnit.energy = 0;
 			player.freeze(playerUnit, trap.getLevel() + 1);
 
 			PlayerState opponentState = game.playerStates.get(trap.getPlayerID());
 			opponentState.availableTraps.remove(trap);	// trap is no longer available
 			opponentState.triggerTrap();
 			
 			gui_logger.info(game.round.currentRound + " RemoveTrap " + trap.getPosition().x + " " + trap.getPosition().y);
 		}
 		
 		TransitionResult moveres = new TransitionResult(transition.id);
 		moveres.errorType = TransitionResult.TransitionError.NoError;
 		return moveres;
 	}
 
 	@Override
 	protected boolean validOperands(Transition transition) {
 		return true;
 	}
 
 	
 	@Override
 	public void printToGuiLog(GameState game, PlayerState player, Transition transition) {
 		if (playerUnit != null) {
 			gui_logger.info(game.round.currentRound + " " + transition.operator.name() + " " + player.id + " " 
 					+ playerUnit.id + " " + playerUnit.pos.x + " " + playerUnit.pos.y + " " + player.gold + " " 
 					+ playerUnit.energy);
 		}
 	}
 }
