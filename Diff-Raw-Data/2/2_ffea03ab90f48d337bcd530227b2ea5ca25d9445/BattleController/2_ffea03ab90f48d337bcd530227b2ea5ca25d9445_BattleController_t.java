 package com.slauson.tactics.controller;
 
 import com.slauson.tactics.TacticsGame;
 import com.slauson.tactics.event.Event;
 import com.slauson.tactics.model.Battle;
 import com.slauson.tactics.model.Region;
 import com.slauson.tactics.utils.BattleUtils;
 
 public class BattleController extends Controller {
 	
 	private static final float SHAKE_FACTOR = 0.05f;
 
 	private Battle battle;
 	
 	public BattleController(TacticsGame game, Battle battle) {
 		super(game);
 		this.battle = battle;
 	}
 	
 	@Override
 	public void update(float delta) {
 		if (delta > MAX_DELTA) {
 			delta = MAX_DELTA;
 		}
 		
 		if (!paused && battle.active()) {
 			
 			battle.update(delta);
 			
 			// update health/offsets/rotation
 			switch (battle.phases.get(0)) {
 			case ATTACKER_ATTACK:
 				battle.attackingRegion.unit.rotation = battle.percentPhaseComplete() * 360;
 				break;
 			case DEFENDER_ATTACK:
 				battle.defendingRegion.unit.rotation = battle.percentPhaseComplete() * 360;
 				break;
 			case ATTACKER_DAMAGE:
 				// + to -
 				if (battle.percentPhaseComplete() < 0.5) {
 					battle.attackingRegion.unit.offset.x = ((0.25f - battle.percentPhaseComplete()) / 0.25f)
 							* SHAKE_FACTOR * battle.attackingRegion.bounds.width;
 				}
 				// - to +
 				else {
 					battle.attackingRegion.unit.offset.x = -((0.75f - battle.percentPhaseComplete()) / 0.25f)
 							* SHAKE_FACTOR * battle.attackingRegion.bounds.width;
 				}
 				break;
 			case DEFENDER_DAMAGE:
 				// + to -
 				if (battle.percentPhaseComplete() < 0.5) {
 					battle.defendingRegion.unit.offset.x = ((0.25f - battle.percentPhaseComplete()) / 0.25f)
 							* SHAKE_FACTOR * battle.defendingRegion.bounds.width;
 				}
 				// - to +
 				else {
 					battle.defendingRegion.unit.offset.x = -((0.75f - battle.percentPhaseComplete()) / 0.25f)
 							* SHAKE_FACTOR * battle.defendingRegion.bounds.width;
 				}
 				break;
 			case UPDATE_HEALTH:
 				// update health
 				battle.attackingRegion.unit.health = battle.originalHealth[0] - ((battle.originalHealth[0] - battle.battleHealth[0]) * battle.percentPhaseComplete());
 				battle.defendingRegion.unit.health = battle.originalHealth[1] - ((battle.originalHealth[1] - battle.battleHealth[1]) * battle.percentPhaseComplete());
 				break;
 			case TAKEOVER:
 				// update position offsets
 				battle.attackingRegion.unit.offset.x = (battle.defendingRegion.position.x - battle.attackingRegion.position.x) * battle.percentPhaseComplete();
 				battle.attackingRegion.unit.offset.y = (battle.defendingRegion.position.y - battle.attackingRegion.position.y) * battle.percentPhaseComplete();
 				break;
 			default:
 				// do nothing
 			}
 			
 			// battle phase is over
 			if (battle.phaseTime < 0) {
 				
 				// reset health/offsets/rotation
 				switch (battle.phases.get(0)) {
 				case ATTACKER_ATTACK:
 					battle.attackingRegion.unit.rotation = 0;
 					break;
 				case DEFENDER_ATTACK:
 					battle.defendingRegion.unit.rotation = 0;
 					break;
 				case ATTACKER_DAMAGE:
 					battle.attackingRegion.unit.offset.x = 0;
 					break;
 				case DEFENDER_DAMAGE:
 					battle.defendingRegion.unit.offset.x = 0;
 					break;
 				case UPDATE_HEALTH:
 					// set final health
 					battle.attackingRegion.unit.health = battle.battleHealth[0];
 					battle.defendingRegion.unit.health = battle.battleHealth[1];
 					break;
 				case TAKEOVER:
 					// reset offset
 					battle.attackingRegion.unit.offset.x = 0;
 					battle.attackingRegion.unit.offset.y = 0;
 					break;
 				default:
 					// do nothing
 				}
 				
 				// go to next phase of battle
 				if (!battle.phases.isEmpty()) {
 					battle.phases.remove(0);
 					battle.phaseTime = Battle.PHASE_DURATION + battle.phaseTime;
 				}
 				
 				// battle is over
 				if (battle.phases.isEmpty()) {
 					// battle is over, still need to do post-battle
 					if (!battle.complete) {
 						
 						battle.battleHealth = BattleUtils.handleBattleDamage(battle.attackingRegion, battle.defendingRegion, battle.battleDamage);
 						battle.originalHealth = new float[] { battle.attackingRegion.unit.health, battle.defendingRegion.unit.health };
 						
 						battle.phases.add(Battle.Phase.UPDATE_HEALTH);
 						
 						// attacker takeover
						if (battle.battleHealth[1] <= 0 && battle.type == Battle.Type.DIRECT) {
 							battle.phases.add(Battle.Phase.TAKEOVER);
 						}
 						
 						// mark battle as complete
 						battle.complete = true;
 					}
 					// post-battle is over
 					else {
 						Region region = BattleUtils.handleBattle(battle.attackingRegion, battle.defendingRegion, battle.battleDamage);
 						battle.reset();
 						fireEvent(new Event(Event.Type.BATTLE_END, region));
 					}
 				}
 			}
 		}
 		
 		
 	}
 
 	@Override
 	public boolean touchDown(float worldX, float worldY) {
 		// ignore
 		return false;
 	}
 	
 	@Override
 	public void handleEvent(Event event) {
 		switch (event.type) {
 		case BATTLE_BEGIN:
 			battle.init(event.region1, event.region2);
 			break;
 		default:
 			// ignore
 			break;
 		}
 	}
 }
