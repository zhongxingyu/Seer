 package com.eatthatgame.robo.contenders;
 
 import com.eatthatgame.robo.Contender;
 
 /**
  * @author Adam Dudley
  * Robot fighter (Contender) for java robot game
  */
 
 public class Adam extends Contender 
 {
 
     public Adam() 
     {
         this.name = "Awesom-O";
         this.age = 19;
     }
 /**
  * Custom AI routine that checks own health and adjusts attack & defense depending on move
  */
     @Override
     public void AI()
     {
         
         if(getHealth() >= 60){
             //System.out.println(name + " attacks!");
             attack = true;
            attackPower = 70;
             defensePower = 0;
         }
         
         else {
             //System.out.println(name + " defends!");
             attack = false;
             attackPower = 0;
             defensePower = 20;
         }
     }
     
     /**
      * Custom AI that if opponent's health drops to / below 60 the attack power adjusts to finish off the bot
      */
     @Override
     public void AI(Contender opponent)
     {
         
         if(getHealth() >= 60 && opponent.getHealth() >= 60){
             //System.out.println(name + " attempts to excecute it's opponent!");
             attack = true;
             attackPower = opponent.getHealth();
             defensePower = 0;
         }
         
         else if (getHealth() < 60) {
             //System.out.println(name + " defends!");
             attack = false;
             attackPower = 0;
             defensePower = 20;
         }
         
         else {
             //System.out.println(name + " attacks!");
             attack = true;
             attackPower = 60;
             defensePower = 0;
         }
     }
 }
