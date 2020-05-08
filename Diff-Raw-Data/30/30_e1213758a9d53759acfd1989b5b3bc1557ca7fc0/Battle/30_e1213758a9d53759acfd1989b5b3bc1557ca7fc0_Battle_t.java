 package com.bettername.thepokemonone;
 
 /*
 
 This class will access the current user singleton
 
 */
 
import android.graphics.ImageFormat;
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
        if (!currentUser.isAnyoneThere()) {
            declareWinner(opponent);
        } else if (!opponent.isAnyoneThere()) {
            declareWinner(currentUser);
         } else {
            // Continue turn logic
            turnNumber++;
         }
     }
 
    private void declareWinner(User winner) {

    }

 
 	private boolean isBattleComplete() {
 		return false;
 	}
 	
 }
