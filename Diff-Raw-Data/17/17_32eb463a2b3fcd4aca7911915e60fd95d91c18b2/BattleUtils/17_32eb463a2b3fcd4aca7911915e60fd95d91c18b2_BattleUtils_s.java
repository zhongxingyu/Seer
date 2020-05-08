 package com.slauson.tactics.utils;
 
 import com.slauson.tactics.model.Region;
 import com.slauson.tactics.model.Unit;
 
 /**
  * Helper methods relating to battles.
  * @author josh
  *
  */
 public class BattleUtils {
 
 	/**
 	 * Calculates battle damage between two regions.
 	 * Returns attackingRegionAttackDamage, defendingRegionAttackDamage
 	 * @param attackingRegion
 	 * @param defendingRegion
 	 * @param randomFactor
 	 * @return
 	 */
 	public static float[] calculateBattleDamage(Region attackingRegion, Region defendingRegion, float randomFactor) {
 		
 		// calculate attack factor for attacking region 
 		float attackFactor = 1f;
 
 		switch (attackingRegion.unit.type) {
 		case CIRCLE:
 		case RANGED_CIRCLE:
 			switch (defendingRegion.unit.type) {
 			case CIRCLE:
 			case RANGED_CIRCLE:
 				attackFactor += -randomFactor + 2*randomFactor*Utils.random().nextFloat();
 				break;
 			case SQUARE:
 			case RANGED_SQUARE:
 				attackFactor += Unit.UNIT_WEAKNESS_FACTOR - randomFactor + 2*randomFactor*Utils.random().nextFloat();
 				break;
 			case TRIANGLE:
 			case RANGED_TRIANGLE:
 				attackFactor -= Unit.UNIT_WEAKNESS_FACTOR - randomFactor + 2*randomFactor*Utils.random().nextFloat();
 				break;
 			}
 			break;
 		case SQUARE:
 		case RANGED_SQUARE:
 			switch (defendingRegion.unit.type) {
 			case CIRCLE:
 			case RANGED_CIRCLE:
 				attackFactor -= Unit.UNIT_WEAKNESS_FACTOR - randomFactor + 2*randomFactor*Utils.random().nextFloat();
 				break;
 			case SQUARE:
 			case RANGED_SQUARE:
 				attackFactor += -randomFactor + 2*randomFactor*Utils.random().nextFloat();
 				break;
 			case TRIANGLE:
 			case RANGED_TRIANGLE:
 				attackFactor += Unit.UNIT_WEAKNESS_FACTOR - randomFactor + 2*randomFactor*Utils.random().nextFloat();
 				break;
 			}
 			break;
 		case TRIANGLE:
 		case RANGED_TRIANGLE:
 			switch (defendingRegion.unit.type) {
 			case CIRCLE:
 			case RANGED_CIRCLE:
 				attackFactor += Unit.UNIT_WEAKNESS_FACTOR - randomFactor + 2*randomFactor*Utils.random().nextFloat();
 				break;
 			case SQUARE:
 			case RANGED_SQUARE:
 				attackFactor -= Unit.UNIT_WEAKNESS_FACTOR - randomFactor + 2*randomFactor*Utils.random().nextFloat();
 				break;
 			case TRIANGLE:
 			case RANGED_TRIANGLE:
 				attackFactor += -randomFactor + 2*randomFactor*Utils.random().nextFloat();
 				break;
 			}
 			break;
 		}
 		
 		float defendFactor = 2 - attackFactor;
 
 		// calculate how much damage each region can do
 		float[] result = {
 			attackFactor * attackingRegion.unit.health,
 			defendFactor * defendingRegion.unit.health
 		};
 		
 		// special case for ranged units
 		if ((attackingRegion.unit.type.isRanged() && !defendingRegion.unit.type.isRanged()) ||
 				defendingRegion.unit.type.isRanged() && !attackingRegion.unit.type.isRanged())
 		{
 			result[1] = 0;
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Returns likelihood that attacking region could defeat defending region.
 	 * (1: attacker more likely to win, 0: defender more likely to win)
 	 * @param attackingRegion
 	 * @param defendingRegion
 	 * @return
 	 */
 	public static float calculateBattleLikelihood(Region attackingRegion, Region defendingRegion) {
 		if (attackingRegion.unit == null) {
 			return 0;
 		}
 		if (defendingRegion.unit == null) {
 			return 1;
 		}
 		
 		float[] battleDamage = BattleUtils.calculateBattleDamage(attackingRegion, defendingRegion, 0);
 		
 		// ((attacker damage - defender damage) / total damage + 1) / 2 
 		return ((battleDamage[0] / defendingRegion.unit.health - battleDamage[1] / attackingRegion.unit.health)
 				/ (battleDamage[0] / defendingRegion.unit.health + battleDamage[1] / attackingRegion.unit.health)
 				+ 1) / 2;
 	}
 	
 	/**
 	 * Returns attacking region health, defending region health
 	 * @param attackingRegion attacking region
 	 * @param defendingRegion defending region
 	 * @param battleDamage battle damage
 	 * @return
 	 */
 	public static float[] handleBattleDamage(Region attackingRegion, Region defendingRegion, float[] battleDamage) {
 		
 		float[] battleHealth  = {attackingRegion.unit.health, defendingRegion.unit.health};
 		
 		if (battleDamage == null) {
 			battleDamage = calculateBattleDamage(attackingRegion, defendingRegion, Unit.UNIT_WEAKNESS_RANDOM_FACTOR);
 		}
 		
 		float attackerAttackDamage = battleDamage[0];
 		float defenderAttackDamage = battleDamage[1];
 		
 		// update health
 		battleHealth[0] -= defenderAttackDamage;
 		battleHealth[1] -= attackerAttackDamage;
 		
 		//System.out.println("battle result (" + attackingRegion.unit + ") (" + defendingRegion.unit + ")");
 		
 		// special case of both units defeated
 		if (battleHealth[0] <= 0 && battleHealth[1] <= 0) {
 
 			// this is unlikely
 			
 			// determine percentage of attack that went wasted
 			float attackerOverkillFactor = -battleHealth[0] / defenderAttackDamage;
 			float defenderOverkillFactor = -battleHealth[1] / attackerAttackDamage;
 			
 			// defender defeated first
 			if (defenderOverkillFactor >= attackerOverkillFactor) {
 				// update attacker health
 				battleHealth[0] += (defenderOverkillFactor * defenderAttackDamage);
 			}
 			// attacker defeated first
 			else {
 				// update defender health
 				battleHealth[1] += (attackerOverkillFactor * attackerAttackDamage);
 			}
 		}
 		
 		return battleHealth;
 	}
 	
 	/**
 	 * Handles battle between two regions.
 	 * @param attackingRegion
 	 * @param defendingRegion
 	 * @param battleDamage
 	 * @return attacker's updated region
 	 */
 	public static Region handleBattle(Region attackingRegion, Region defendingRegion, float[] battleDamage) {
 		
 		//System.out.println("handleBattle (" + attackingRegion + ") vs (" + defendingRegion + ")");
 		
 		float[] battleHealth = handleBattleDamage(attackingRegion, defendingRegion, battleDamage);
 		
 		attackingRegion.unit.health = battleHealth[0];
 		defendingRegion.unit.health = battleHealth[1];
 		
 		// only allow a single attack
 		attackingRegion.unit.hasAttack = false;
 		
 		// attacker victory
 		if (defendingRegion.unit.health <= 0) {
 
 			defendingRegion.player.units--;
 			
 			// normal attacking unit
 			if (!attackingRegion.unit.type.isRanged()) {
 				
 				attackingRegion.player.regions++;
 				defendingRegion.player.regions--;
 				
 				defendingRegion.player = attackingRegion.player;
 				defendingRegion.unit = attackingRegion.unit;
 				attackingRegion.unit = null;
 				
 				return defendingRegion;
 			}
 			// ranged attacking unit
 			else {
 				defendingRegion.unit = null;
 				return null;
 			}
 		}
 		// defender victory
 		else if (attackingRegion.unit.health <= 0) {
 			// remove unit from attacking region
 			attackingRegion.unit = null;
 			
 			attackingRegion.player.units--;
 			
 			return null;
 		}
 		// no victory
 		else {
 			// do nothing
 			return null;
 		}
 	}
 	
 }
