 package com.nweiler.ParcelMaze;
 
 
 import java.util.Random;
 import static java.lang.Math.*;
 
 /**
  * A fight object is created when the player encounters a monster.
  * The user is given a choice between punch, low damage/high probability of a hit
  * and kick, high damage/low probability of hit. The player and monster trade hits
  * until one of the two is defeated.
  */
 
 public class Fight
 {
     // private int bonus;
     private Maze maze = Maze.getInstance();
     private int bonus;
     /**
      * Empty constructor for fight class.
      * We may add something here...
      */
     public Fight() { }
     /**
      * Main class for fight. The player and monster trade hits until one of them dies.
      * @param player The player to be included in the fight
      * @param monster The monster to be included in the fight
      * @return Outcome of the fight. True if player dies, false if monster dies.
      */
     public boolean fight(Actor player, Actor monster) {
     	Random rand = new Random();
         // int monsterAttack = cthulu.checkAttack();
         int [] punch = {1, 1, 1, 1, 1, 4};
         int [] kick = {0, 0, 0, 4, 4, 4};
            
         int input = UI.readInt("There is a monster! Ahh!\nPress 1 to punch the monster. It doesn't hit hard, but it hits often\n"
                                     + "Press 2 to kick the monster. It might miss but it hits hard!\n");
         while(input != 1 && input != 2){
             input = UI.readInt("Nooo!!! Enter 1 or 2! ");
         }
        while(monster.getHealth() > 0  && player.getHealth() > 0 && (input == 1 || input == 2)) {
            System.out.println(monster.hashCode());
             if(input == 1){
                 bonus = rand.nextInt(6);
                 bonus = punch[bonus];                                              
                 if(bonus == 1){
                    monster.damage(player.attack());
                     System.out.print("You hit the monster! " + "Monster has " + max(0, monster.getHealth()) + " health left!\n");
                     player.damage(monster.attack());
                     System.out.print("The monster responded with a hit of his own!\n");
                     System.out.print("You have " + max(0, player.getHealth()) + " health left!\n");
                 }               
                 else{
                   monster.damage(player.attack()*2);
                    System.out.print("Critical hit!\n" + "Monster has " + max(0, monster.getHealth()) + " health left!\n");
                    System.out.print("The monster couldn't hit you because he stumbled!\n");
                    System.out.print("You have " + max(0, player.getHealth()) + " health left!\n");
                 }
             }
             if(input == 2) {
                 bonus = rand.nextInt(6);
                 bonus = kick[bonus];
                 if(bonus == 0){
                     System.out.print("You missed! " + "Monster has " + monster.getHealth() + " health left!\n");
                     player.damage(monster.attack()*2);
                     System.out.print("The monster hit you harder because you missed!\n");
                     System.out.print("You have " + max(0, player.getHealth()) + " health left!\n");
                 }   
                 else {
                     monster.damage(player.attack()*2);
                     System.out.print("Critical hit! " + "Monster has " + max(0,monster.getHealth()) + " health left!\n");
                     System.out.print("The monster couldn't hit you because he stumbled!\n");
                     System.out.print("You have " + max(0, player.getHealth()) + " health left!\n");
                 }
             }
             if(monster.getHealth() > 0 && player.getHealth() > 0) {
                 input = UI.readInt("Press 1 to punch or 2 to kick the monster!\n");
                 while(input < 1 || input > 2){
                     input = UI.readInt("You'll die! Press 1 or 2 to attack the monster and not die!\n");
                 }
             }
         }
         if(player.getHealth() <= 0){
             System.out.print("The monster dances on your corpse. You died.\n");
             player.die();
             return false;
         }
         else {
             System.out.print("Your foe is defeated! Continue your journey.\n");
             maze.reduceMonsterCount();
             maze.printMonsterCount();
             return true;
         }
     }
 }
