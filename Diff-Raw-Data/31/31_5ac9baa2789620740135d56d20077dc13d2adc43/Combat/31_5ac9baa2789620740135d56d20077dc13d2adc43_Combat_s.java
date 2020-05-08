 package rpisdd.rpgme.gamelogic.dungeon.model;
 
 import rpisdd.rpgme.gamelogic.player.StatType;
 
 //A class to hold functions related to combat
 public class Combat {
 
 	// Represents an attack delivered by any source
 	// Holds the stattype associated with the attack
 	// and the base power of that attack
 	public static class Attack {
 		public StatType type;
 		public int power;
 
 		public Attack(StatType t, int p) {
 			type = t;
 			power = p;
 		}
 
 	}
 
 	// The equation to use for calculating damage
 	public static int CalculateAttackDamage(int atk, int def) {
 		atk = atk - (atk * (def / (def + 100)));
 		if (atk < 1) {
 			atk = 1;
 		}
 		return atk;
 	}
 
 }
