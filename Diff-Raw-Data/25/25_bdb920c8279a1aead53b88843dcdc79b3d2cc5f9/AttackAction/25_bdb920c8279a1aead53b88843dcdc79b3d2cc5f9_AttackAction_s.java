 package org.aimas.craftingquest.core.actions;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.aimas.craftingquest.core.GamePolicy;
 import org.aimas.craftingquest.state.GameState;
 import org.aimas.craftingquest.state.PlayerState;
 import org.aimas.craftingquest.state.Transition;
 import org.aimas.craftingquest.state.Transition.ActionType;
 import org.aimas.craftingquest.state.objects.ICrafted;
 import org.aimas.craftingquest.state.resources.ResourceType;
 import org.aimas.craftingquest.state.TransitionResult;
 import org.aimas.craftingquest.state.UnitState;
 
 public class AttackAction extends Action {
 	public AttackAction(ActionType type) {
 		super(type);
 	}
 
 	@Override
 	protected TransitionResult handle(GameState game, PlayerState player, Transition transition) {
 		int attackedPlayerID = (Integer)transition.operands[1];
 		int attackedUnitID = (Integer)transition.operands[2];
 		int energy = (Integer)transition.operands[3];
 
 		// check for enough energy points
 		if (playerUnit.energy < energy) {
 			TransitionResult res = new TransitionResult(transition.id);
 			res.errorType = TransitionResult.TransitionError.NoEnergyError;
 			res.errorReason = "Not enough energy points left to do this attack";
 			return res;
 		}
 
 		// check if the attacked player is real
 		PlayerState attackedPlayer = game.playerStates.get(attackedPlayerID);
 		if (attackedPlayer == null) {
 			TransitionResult res = new TransitionResult(transition.id);
 			res.errorType = TransitionResult.TransitionError.PlayerIdError;
 			res.errorReason = "Cannot attack non-existing player";
 			return res;
 		}
 
 		// check if the attacked unit is real
 		UnitState attackedUnit = null;
 		for (UnitState u : attackedPlayer.units) {
 			if (u.id == attackedUnitID && u.playerID == attackedPlayerID) {
 				attackedUnit = u;
 				break;
 			}
 		}
 
 		if (attackedUnit == null) {
 			TransitionResult res = new TransitionResult(transition.id);
 			res.errorType = TransitionResult.TransitionError.UnitIdError;
 			res.errorReason = "Cannot attack non-existing unit";
 			return res;
 		}

 		// check neighbouring conditions
 		if (Math.abs(attackedUnit.pos.x - playerUnit.pos.x) > 1 ||
 				Math.abs(attackedUnit.pos.y - playerUnit.pos.y) > 1) {
 			TransitionResult res = new TransitionResult(transition.id);
 			res.errorType = TransitionResult.TransitionError.ProximityError;
 			res.errorReason = "Cannot attack non-neighbouring unit";
 			return res;
 		}
 
 		// ======== do attack ========
 		boolean attackedUnitAlreadyDead = attackedUnit.life <= 0;
 		
 		if (!attackedUnitAlreadyDead) {
 			int attackValue = computeAttackPower(energy, playerUnit, attackedUnit);
 			attackedUnit.life -= attackValue;
 			if (attackedUnit.energy > attackedUnit.life) {
 				// keep attacked unit energy consistent with its life (maximum energy)
 				attackedUnit.energy = attackedUnit.life;
 			}
 			
 			playerUnit.energy -= energy;
 	
 			
 			// ======== do retaliate ========
 			if (attackedUnit.life > 0 && attackedUnit.retaliateEnergy >= 0 &&
 					//attackedUnit.retaliateEnergy >= attackedUnit.retaliateThreshold &&
 					attackedUnit.energy >= attackedUnit.retaliateThreshold &&
 					attackedUnit.retaliateEnergy <= attackedUnit.energy) {
 				int retaliateValue = computeAttackPower(
 						attackedUnit.retaliateEnergy,
 						attackedUnit, playerUnit);
 				
 				playerUnit.life -= retaliateValue;
 				if (playerUnit.energy > playerUnit.life) {
 					// keep attacking unit energy consistent with its life (maximum energy)
 					playerUnit.energy = playerUnit.life;
 				}
 				
 				attackedUnit.energy -= attackedUnit.retaliateEnergy;
 			}
 	
 	
 			if (attackedUnit.life <= 0) {
 				game.killOne(player, false);
 				attackedPlayer.die();
 				respawnUnit(game, attackedUnit);
 			}
 	
 			if (playerUnit.life <= 0) {
 				game.killOne(attackedPlayer, true);
 				player.die();
 				respawnUnit(game, playerUnit);
 			}
 		}
 		
 		TransitionResult res = new TransitionResult(transition.id);
 		res.errorType = TransitionResult.TransitionError.NoError;
 		return res;
 	}
 
 	@Override
 	protected boolean validOperands(Transition transition) {
 		return true;
 	}
 
 	private int computeAttackPower(int baseAttack, UnitState attacker, UnitState defender) {
 		float swordMod = 1, shieldMod = 1;
 
 		if (attacker.equipedSword != null)
 			//swordMod += attacker.equipedSword.getDefence() / 100.0;
 			swordMod += attacker.equipedSword.getAttack() / 100.0;
 
 		if (defender.equipedArmour != null)
 			shieldMod -= defender.equipedArmour.getDefence() / 100.0;
 
 		return (int)Math.round(baseAttack * swordMod * shieldMod);
 	}
 	
 	
 	private void respawnUnit(GameState game, UnitState unit) {
 		HashMap<ResourceType, Integer> visibleCellResources = game.map.cells[unit.pos.y][unit.pos.x].visibleResources;
 		HashMap<ResourceType, Integer> carriedResources = unit.carriedResources;
 		
 		// drop all resources
 		Iterator<ResourceType> rit = carriedResources.keySet().iterator();
 		while(rit.hasNext()) {
 			ResourceType res = rit.next();
 			Integer existing = visibleCellResources.get(res);
 			Integer carried = carriedResources.get(res);
 			if (existing == null) {
 				visibleCellResources.put(res, carried);
 			} else {
 				visibleCellResources.put(res, existing + carried);
 			}
 			carriedResources.remove(res);
 		}
 		
 		// drop all objects
 		HashMap<ICrafted, Integer> cellObjects = game.map.cells[unit.pos.y][unit.pos.x].craftedObjects;
 		HashMap<ICrafted, Integer> carriedObjects = unit.carriedObjects;
 	
 		Iterator<ICrafted> oit = carriedObjects.keySet().iterator();
 		while(oit.hasNext()) {
 			ICrafted obj = oit.next();
 			Integer existing = cellObjects.get(obj);
 			Integer carried = carriedObjects.get(obj);
 			if (existing == null) {
 				cellObjects.put(obj, carried);
 			} else {
 				cellObjects.put(obj, existing + carried);
 			}
 			carriedObjects.remove(obj);
 		}
 		
 		//System.out.println("######## Life of dead unit: " + unit.life + " ########"); 
 		
 		// remove unit from current position
 		game.map.cells[unit.pos.y][unit.pos.x].cellUnits.remove(unit.getOpponentPerspective());
 		
 		// reset the unit stats and position
 		unit.reset(GamePolicy.initialUnitMaxLife, GamePolicy.initialUnitMaxLife, 
 				GamePolicy.initialPlayerPositions.get(unit.playerID));
 		
 		// add it to re-spawned place
 		game.map.cells[unit.pos.y][unit.pos.x].cellUnits.add(unit.getOpponentPerspective());
 	}
 	
 	
 	@Override
 	public void printToGuiLog(GameState game, PlayerState player, Transition transition) {
 		if (playerUnit != null) {
 			int attackedPlayerID = (Integer)transition.operands[1];
 			int attackedUnitID = (Integer)transition.operands[2];
 			int energy = (Integer)transition.operands[3];
 			
 			// get data for attacked unit
 			PlayerState attackedPlayer = game.playerStates.get(attackedPlayerID);
 			UnitState attackedUnit = null;
 			for (UnitState u : attackedPlayer.units) {
 				if (u.id == attackedUnitID && u.playerID == attackedPlayerID) {
 					attackedUnit = u;
 					break;
 				}
 			}
 			
 			gui_logger.info(game.round.currentRound + " " + transition.operator.name() + " " + player.id + " " 
 				+ playerUnit.id + " " + playerUnit.pos.x + " " + playerUnit.pos.y + " " + player.gold + " " 
 				+ playerUnit.energy + " " + attackedPlayerID + " " + attackedUnitID + " "
 				+ attackedUnit.pos.x + " " + attackedUnit.pos.y + " " + energy);
 			
 			// see if anyone died to log the event
 			if (playerUnit.life <= 0) {
 				gui_logger.info(game.round.currentRound + " " + "Die" + " " + player.id + " " 
 					+ playerUnit.id + " " + playerUnit.pos.x + " " + playerUnit.pos.y);
 			}
 			
 			if (attackedUnit.life <= 0) {
 				gui_logger.info(game.round.currentRound + " " + "Die" + " " + attackedPlayer.id + " " 
 					+ attackedUnit.id + " " + attackedUnit.pos.x + " " + attackedUnit.pos.y);
 			}
 		}
 	}
 }
