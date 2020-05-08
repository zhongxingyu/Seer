 package edu.brown.cs32.goingrogue.gameobjects.creatures.util;
 
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Creature;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.CreatureStats;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Inventory;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Stats;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class CombatUtil {
 
     public static void incurDamage(Creature attacker, Creature target) {
         Stats attackerStats = attacker.getStats();
         Inventory attackerInventory = attacker.getInventory();
         Stats targetStats = target.getStats();
         Inventory targetInventory = target.getInventory();
         double attack = attackerStats.getAttack() + attackerInventory.getAttackSum();
         double defense = targetStats.getDefense() + targetInventory.getDefenseSum();
         double accuracy = attackerStats.getAccuracy() + attackerInventory.getAccuracySum();
         double speed = targetStats.getSpeed() + targetInventory.getSpeedSum();
         
        double damage = (attack / defense) + (accuracy / speed); // TODO come up with a better formula
         if (damage > 0) {
             System.out.println(target.getName()+" took "+damage);
             targetStats.reduceHealth((int) damage);
         }
     }
 }
