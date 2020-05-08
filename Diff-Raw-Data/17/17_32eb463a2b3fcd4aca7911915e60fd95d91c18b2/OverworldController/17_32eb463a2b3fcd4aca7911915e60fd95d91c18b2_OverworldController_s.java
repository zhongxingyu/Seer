 package com.slauson.tactics.controller;
 
 import com.slauson.tactics.TacticsGame;
 import com.slauson.tactics.ai.Move;
 import com.slauson.tactics.event.Event;
 import com.slauson.tactics.model.Battle;
 import com.slauson.tactics.model.Neighbor;
 import com.slauson.tactics.model.Overworld;
 import com.slauson.tactics.model.Overworld.Phase;
 import com.slauson.tactics.model.Player;
 import com.slauson.tactics.model.Region;
 import com.slauson.tactics.model.Unit;
 import com.slauson.tactics.utils.BattleUtils;
 import com.slauson.tactics.utils.MoveUtils;
 import com.slauson.tactics.utils.PlayerUtils;
 import com.slauson.tactics.utils.RegionUtils;
 
 /**
  * Controls the overworld.
  * @author josh
  *
  */
 public class OverworldController extends Controller {
 	
 	private static float TIME_PER_MOVE = 1;
 	
 	private Overworld overworld;
 	private Region selectedRegion;
 	
 	private Move previousMove;
 	private Move currentMove;
 	private float currentMoveTime;
 	private boolean paused;
 	
 	public OverworldController(TacticsGame game, Overworld overworld, Battle battle) {
 		super(game);
 		this.overworld = overworld;
 		
 		selectedRegion = null;
 		previousMove = null;
 		currentMove = new Move(Move.Type.DUMMY);
 		currentMoveTime = 0;
 	}
 	
 	@Override
 	public void update(float delta) {
 		if (delta > MAX_DELTA) {
 			delta = MAX_DELTA;
 		}
 
 		if (!paused) {
 			
 			if (overworld.activePlayer().type != Player.Type.PLAYER) {
 				handleMove(delta);
 			}
 		}
 	}
 	
 	@Override
 	public boolean touchDown(float worldX, float worldY) {
 		
 		// only allow selecting regions for player controlled players
 		if (overworld.activePlayer().type != Player.Type.PLAYER) {
 			return false;
 		}
 		
 		Region region = overworld.getContainingRegion(worldX, worldY);
 		if (region != null) {
 				
 			System.out.println("touched: " + region);
 			
 			switch (overworld.phase) {
 			case ATTACK:
 				// there's a currently selected region
 				if (selectedRegion != null) {
 					
 					System.out.println("deselected: " + selectedRegion);
 					
 					// unselect previously selected region
 					selectedRegion.selected = false;
 					
 					// unmark previously selected region neighbors
 					RegionUtils.unmarkRegionNeighbors(selectedRegion);
 					
 					// if selected again, just unselect
 					if (selectedRegion == region) {
 						selectedRegion = null;
 						return true;
 					}
 					
 					// region is in selected region's neighbors
 					Neighbor.Type neighborType = selectedRegion.neighbors.getNeighborType(region);
 					if (neighborType != null) {
 						
 						// attack
 						if (selectedRegion.unit.hasAttack && !selectedRegion.player.equals(region.player) && region.unit != null &&
 								((selectedRegion.unit.type.isRanged() && neighborType.isRanged()) || (!selectedRegion.unit.type.isRanged() && !neighborType.isRanged())))
 						{
 							fireEvent(new Event(Event.Type.BATTLE_BEGIN, selectedRegion, region));
 							overworld.phase = Overworld.Phase.BATTLE;
 							return true;
 						}
 						// move (unoccupied region)
 						else if (selectedRegion.unit.hasMove && (region.unit == null || region.unit.hasMove) && neighborType != Neighbor.Type.RANGED) {
 							selectedRegion = MoveUtils.handleMove(selectedRegion, region);
 							return true;
 						} else {
 							selectedRegion = null;
 						}
 					}
 				}
 				
 				// only allow selecting regions with units for player who has current turn and are active
 				if (region.unit != null &&
 						region.player == overworld.activePlayer() &&
 						(region.unit.hasAttack || region.unit.hasMove))
 				{
 				
 					// select new selected region
 					selectedRegion = region;
 					selectedRegion.selected = true;
 					
 					System.out.println("selected: " + selectedRegion);
 					
 					RegionUtils.markRegionNeighbors(selectedRegion);
 				}
 				break;
 			case REINFORCE:
 				if (region.player == overworld.activePlayer() && overworld.activePlayer().reinforcements > 0 && (region.unit == null || region.unit.health < Unit.MAX_HEALTH)) {
 
 					// mark previously selected region
 					if (selectedRegion != null) {
 						selectedRegion.marked = true;
 						selectedRegion.selected = false;
 						selectedRegion = null;
 					}
 					
 					// handle actual reinforcement
 					selectedRegion = MoveUtils.handleReinforcement(region);
 					
 					// keep selected so we can change unit type
 					if (selectedRegion != null) {
 						selectedRegion.selected = true;
 					}
 					
 					overworld.activePlayer().reinforcements--;
 				}
 				// switch unit type of new unit
 				else if (region == selectedRegion) {
 					region.unit.type = region.unit.type.next();
 				}
 				System.out.println("reinforced: " + region);
 				break;
 			case BATTLE:
 			default:
 				// do nothing
 				break;
 			}
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 
 	@Override
 	public boolean keyTyped(char character) {
 		super.keyTyped(character);
 		
 		switch (character) {
 		case ' ':
 			if (overworld.activePlayer().type == Player.Type.PLAYER && overworld.phase != Overworld.Phase.BATTLE) {
 				// unmark previously selected region
 				if (selectedRegion != null) {
 					RegionUtils.unmarkRegionNeighbors(selectedRegion);
 					selectedRegion.selected = false;
 					selectedRegion = null;
 				}
 				
 				// move to next phase
 				nextPhase();
 				
 				return true;
 			}
 		case 'p':
 			paused = !paused;
 			break;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public void handleEvent(Event event) {
 		switch (event.type) {
 		case BATTLE_BEGIN:
 			break;
 		case BATTLE_END:
 			// keep updated attacking region selected if it can move
			if (event.region1 != null && event.region1.unit != null && event.region1.unit.hasMove) {
				selectedRegion = event.region1;
 				selectedRegion.selected = true;
 				RegionUtils.markRegionNeighbors(selectedRegion);
 			}
 			overworld.phase = Phase.ATTACK;
 			break;
 		}
 	}
 	
 	/**
 	 * Moves to next phase of current turn, or first phase of next turn.
 	 */
 	private void nextPhase() {
 		overworld.nextPhase();
 		
 		// next turn
 		if (overworld.phase == Phase.ATTACK) {
 			
 			RegionUtils.unmarkAllRegions(overworld);
 			
 			// move to next turn
 			overworld.nextTurn();
 			
 			// get to next active player
 			while (overworld.activePlayer().regions == 0) {
 				overworld.nextTurn();
 			}
 			
 			PlayerUtils.setPlayerUnitsActive(overworld, overworld.activePlayer());
 		} else {
 			// handle reinforcements
 			PlayerUtils.updatePlayerReinforcements(overworld, overworld.activePlayer());
 			PlayerUtils.markPlayerReinforcements(overworld, overworld.activePlayer());
 			
 			// go directly to next phase if player doesn't have any reinforcements
 			if (overworld.activePlayer().reinforcements == 0) {
 				nextPhase();
 			}
 		}
 	}
 	
 	private void handleMove(float delta) {
 		currentMoveTime -= delta;
 		
 		if (currentMoveTime < 0) {
 			
 			// deselect and unmark neighbors for attack and move moves
 			if (previousMove != null && (previousMove.type == Move.Type.ATTACK || previousMove.type == Move.Type.MOVE)) {
 				if (previousMove.region != null) {
 					previousMove.region.selected = false;
 					RegionUtils.unmarkRegionNeighbors(previousMove.region);
 				}
 				if (previousMove.otherRegion != null) {
 					previousMove.otherRegion.selected = false;
 					RegionUtils.unmarkRegionNeighbors(previousMove.otherRegion);
 				}
 				
 				previousMove = null;
 			}
 			
 			currentMove.numPhases--;
 			
 			// numPhases - 1
 			switch (currentMove.numPhases) {
 			case 1:
 				switch (currentMove.type) {
 				case ATTACK:
 				case MOVE:
 					// select region
 					currentMove.region.selected = true;
 					
 					// mark neighbors
 					RegionUtils.markRegionNeighbors(currentMove.region);
 					break;
 				case REINFORCE:
 				case END_PHASE:
 				default:
 					break;
 				}
 				currentMoveTime = TIME_PER_MOVE;
 				break;
 			case 0:
 				switch (currentMove.type) {
 				case ATTACK:
 					Region updatedAttackingRegion = BattleUtils.handleBattle(currentMove.region, currentMove.otherRegion, null);
 					
 					// deselect region and unmark neighboring regions
 					currentMove.region.selected = false;
 					RegionUtils.unmarkRegionNeighbors(currentMove.region);
 					
 					if (updatedAttackingRegion != null) {
 						
 						// remove reference to region we're attacking if it was defeated
 						if (updatedAttackingRegion.equals(currentMove.otherRegion)) {
 							currentMove.otherRegion = null;
 						}
 						
 						currentMove.region = updatedAttackingRegion;
 						
 						// reselect and re-mark neighbors only if attacker can move
 						if (currentMove.region.unit != null && currentMove.region.unit.hasMove) {
 							currentMove.region.selected = true;
 							RegionUtils.markRegionNeighbors(currentMove.region);
 						}
 					}
 					break;
 				case MOVE:
 					MoveUtils.handleMove(currentMove.region, currentMove.otherRegion);
 					break;
 				case REINFORCE:
 					// mark region
 					currentMove.region.marked = true;
 					
 					// handle actual reinforcement
 					MoveUtils.handleReinforcement(currentMove.region, currentMove.unitType);
 					break;
 				case END_PHASE:
 				default:
 					nextPhase();
 					break;
 				}
 				currentMoveTime = TIME_PER_MOVE;
 				break;
 			case -1:
 				
 				// get next move
 				previousMove = currentMove;
 				currentMove = overworld.activePlayer().ai.getNextMove(overworld, overworld.activePlayer());
 				
 				System.out.println("next move: " + currentMove);
 				// don't reset move time here so we go directly into first phase of next move
 				break;
 			}
 		}
 	}
 }
