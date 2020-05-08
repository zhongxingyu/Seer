 package com.bettername.thepokemonone;
 
 /*
 
 This class will access the current user singleton
 
 */
 
 import com.bettername.thepokemonone.monster.Monster;
 
 import java.util.Random;
 
 public class Battle {
 	
 	public int turnNumber;
 	public User opponent;
     public User currentUser;
 	
 	public Battle() {
 
 	
 	}
 
     private void attack(Attack attack, Monster reciever) {
         Random rand = new Random();
         int attackDamage = rand.nextInt(attack.getMaxDamageRange())+attack.getMinDamageRange();
         reciever.setCurrentHealth(reciever.getCurrentHealth()-attackDamage);
     }
 
     private void turn() {
        if (currentUser.isAnyoneThere() && opponent.isAnyoneThere()) {
            // If both users have creatures left turn code
            turnNumber++;
         } else {
            // Code if one user is out of creatures
         }
     }
 
 
 	private boolean isBattleComplete() {
 		return false;
 	}
 	
 }
