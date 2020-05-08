 package com.eatthatgame.robo;
 
 import java.util.ArrayList;
 
 /**
  * @author Adam Dudley
  * Game mode for robot fighting game
  * 
  * Game mode rules:
  * Two randomly chosen contenders fight against another two randomly chosen contenders.
  * Contender a fights Contender b while Contender a2 fights Contender b2
 * If a contender is defeated the opposing team will both target the remaining bot
 * For a team to win both opposing contenders have to be KO'd 
  */
 public class RandomMatchUp2v2 {
     public RandomMatchUp2v2(ArrayList contenders) {
         int randomContenderID;
         randomContenderID = (int) (Math.random()*contenders.size());
         Contender a = (Contender) contenders.remove(randomContenderID); 
         randomContenderID = (int) (Math.random()*contenders.size());
         Contender a2 = (Contender) contenders.remove(randomContenderID); 
         randomContenderID = (int) (Math.random()*contenders.size());
         Contender b = (Contender) contenders.remove(randomContenderID); 
         randomContenderID = (int) (Math.random()*contenders.size());
         Contender b2 = (Contender) contenders.remove(randomContenderID); 
             
         System.out.println("\nTeam A: " + a.name + " and " + a2.name + " vs. Team B: " + b.name + " and " +b2.name + "\n");
         System.out.println("FIGHT!");
         
         int roundCount = 0;
         int MAX_ROUNDS = 200;
         while(a.alive() && a2.alive() && b.alive() && b2.alive() && roundCount < MAX_ROUNDS) {
             
             roundCount++;
             System.out.println("\n======= Round: " + roundCount + " =======");
             
             a.AI();
             a2.AI();
             b.AI();
             b2.AI();        
             /**
              * All possible moves in combat
              */
             if(a.attack && a2.attack){
                 if(b.attack && b2.attack){
                     System.out.println("Both teams attack!");
                     System.out.println(a.name + " hits " + b.name + " for: " + a.attack());
                     System.out.println(b.name + " hits " + a.name + " for: " + b.attack());
                     System.out.println(a2.name + " hits " + b2.name + " for: " + a2.attack());
                     System.out.println(b2.name + " hits " + a2.name + " for: " + b2.attack());
                     b.hit(a.attack());
                     b2.hit(a2.attack());
                     a.hit(b.attack());
                     a2.hit(b2.attack());
                     
                 } else if (b.attack!=true && b2.attack!=true){
                     System.out.println("Team A attacks and Team B defends");
                     System.out.println(a.name + " hits " + b.name + " for: " + (a.attack()-b.defend()));
                     System.out.println(a2.name + " hits " + b2.name + " for: " + (a2.attack()-b2.defend()));
                     b.hit(a.attack()-b.defend());
                     b2.hit(a2.attack()-b2.defend());
                     a.hit(0);
                     a2.hit(0);
                     
                 } else if (b.attack && b2.attack!=true) {
                     System.out.println("Team A attacks and Team B mixes attack and defence");
                     System.out.println(a.name + " hits " + b.name + " for: " + (a.attack()-b.defend()));
                     System.out.println(a2.name + " hits " + b2.name + " for: " + (a2.attack()-b2.defend()));
                     b.hit(a.attack());
                     b2.hit(a2.attack()-b2.defend());
                     a.hit(b.attack());
                     a2.hit(0);
                 }
                 
                 else if (b.attack!=true && b2.attack) {
                     System.out.println("Team A attacks and Team B mixes attack and defence");
                     System.out.println(a.name + " hits " + b.name + " for: " + (a.attack()-b.defend()));
                     System.out.println(a2.name + " hits " + b2.name + " for: " + (a2.attack()-b2.defend()));
                     b.hit(a.attack()-b.defend());
                     b2.hit(a2.attack());
                     a.hit(0);
                     a2.hit(b2.attack());
                 }
                 
             } else if (a.attack!=true && a2.attack!=true){
                 if (b.attack && b2.attack){
                     System.out.println("Team A defends and Team B attacks");
                     System.out.println(b.name + " hits " + a.name + " for: " + (b.attack()-a.defend()));
                     System.out.println(b2.name + " hits " + a2.name + " for: " + (b2.attack()-a2.defend()));
                     b.hit(0);
                     b2.hit(0);
                     a.hit(b.attack()-a.defend());
                     a2.hit(b2.attack()-a2.defend());
                            
                 } else if (b.attack!=true && b2.attack){
                     System.out.println("Team A defends and Team B mixes attack and defence");
                     System.out.println(b.name + " hits " + a.name + " for: " + (b.attack()-a.defend()));
                     System.out.println(b2.name + " hits " + a2.name + " for: " + (b2.attack()-a2.defend()));
                     b.hit(0);
                     b2.hit(0);
                     a.hit(0);
                     a2.hit(b2.attack()-a2.defend());              
                 } 
                 
                 else if (b.attack && b2.attack!=true){
                     System.out.println("Team A defends and Team B mixes attack and defence");
                     System.out.println(b.name + " hits " + a.name + " for: " + (b.attack()-a.defend()));
                     System.out.println(b2.name + " hits " + a2.name + " for: " + (b2.attack()-a2.defend()));
                     b.hit(0);
                     b2.hit(0);
                     a.hit(b.attack()-a.defend());
                     a2.hit(0);              
                 } 
                 
                 else {
                     // both defend only energyCheck runs
                     System.out.println("Both teams defend!");
                     b.hit(0);
                     b2.hit(0);
                     a.hit(0);
                     a2.hit(0);
                 }
                 
             } else if (a.attack!=true && a2.attack){
                 if (b.attack && b2.attack){
                     System.out.println("Team A mixes attack and defence and Team B attacks");
                     System.out.println(b.name + " hits " + a.name + " for: " + (b.attack()-a.defend()));
                     System.out.println(b2.name + " hits " + a2.name + " for: " + (b2.attack()-a2.defend()));
                     b.hit(0);
                     b2.hit(a2.attack());
                     a.hit(b.attack()-a.defend());
                     a2.hit(b2.attack());
                            
                 } else if (b.attack!=true && b2.attack){
                     System.out.println("Both teams mix attack and defence!");
                     System.out.println(b.name + " hits " + a.name + " for: " + (b.attack()-a.defend()));
                     System.out.println(b2.name + " hits " + a2.name + " for: " + (b2.attack()-a2.defend()));
                     b.hit(0);
                     b2.hit(b2.attack());
                     a.hit(0);
                     a2.hit(b2.attack());              
                 } 
                 
                 else if (b.attack && b2.attack!=true){
                     System.out.println("Both teams mix attack and defence!");
                     System.out.println(b.name + " hits " + a.name + " for: " + (b.attack()-a.defend()));
                     System.out.println(b2.name + " hits " + a2.name + " for: " + (b2.attack()-a2.defend()));
                     b.hit(0);
                     b2.hit(b2.attack() - a2.defend());
                     a.hit(b.attack()-a.defend());
                     a2.hit(0);              
                 } 
                 
             } else if (a.attack && a2.attack!=true){
                 if (b.attack && b2.attack){
                     System.out.println("Team A mixes attack and defence and Team B attacks");
                     System.out.println(b.name + " hits " + a.name + " for: " + (b.attack()-a.defend()));
                     System.out.println(b2.name + " hits " + a2.name + " for: " + (b2.attack()-a2.defend()));
                     b.hit(a.attack());
                     b2.hit(0);
                     a.hit(b.attack());
                     a2.hit(b2.attack()-a2.defend());
                            
                 } else if (b.attack!=true && b2.attack){
                     System.out.println("Both teams mix attack and defence!");
                     System.out.println(b.name + " hits " + a.name + " for: " + (b.attack()-a.defend()));
                     System.out.println(b2.name + " hits " + a2.name + " for: " + (b2.attack()-a2.defend()));
                     b.hit(a.attack() - b.defend());
                     b2.hit(0);
                     a.hit(0);
                     a2.hit(b2.attack()-a2.defend());              
                 } 
                 
                 else if (b.attack && b2.attack!=true){
                     System.out.println("Both teams mix attack and defence!");
                     System.out.println(b.name + " hits " + a.name + " for: " + (b.attack()-a.defend()));
                     System.out.println(b2.name + " hits " + a2.name + " for: " + (b2.attack()-a2.defend()));
                     b.hit(a.attack());
                     b2.hit(0);
                     a.hit(b.attack());
                     a2.hit(0);              
                 }
             }
 
             System.out.println(a.name + " health: " + a.getHealth());
             System.out.println(a2.name + " health: " + a2.getHealth());
             System.out.println(b.name + " health: " + b.getHealth());
             System.out.println(b2.name + " health: " + b2.getHealth());
         }
         /**
          * Checks if and contenders have reached 0 health and if maximum number of rounds reached 
          */
         if(roundCount < MAX_ROUNDS) {
             if(a.alive() & a2.alive()) {
                 System.out.println("\n======= Team A: " + a.name + " and " + a2.name + " wins by K.O. (in " + roundCount + " rounds) =======");
             } else if(b.alive() & b2.alive()) {
                 System.out.println("\n======= Team B: " + b.name + " and " + b2.name + " wins by K.O. (in " + roundCount + " rounds) =======");
             } else {
                 System.out.println("\n=======Mutual Annihilation, all contenders are dead after " + roundCount + " rounds)");
             }
         } else {
             System.out.println("\n=======MAX Rounds Reached=======");
             if(a.getHealth()+ a2.getHealth() > b.getHealth()+ b2.getHealth()) {
                 System.out.println("\n======= Team A: " + a.name + " and " + a2.name + " wins by jury decision (in MAX " + roundCount + " rounds) =======");
             } else {
                 System.out.println("\n======= Team B: " + b.name + " and " + b2.name + " wins by jury decision (in MAX " + roundCount + " rounds) =======");
             }
         }
     }
}
