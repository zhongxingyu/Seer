 package com.eatthatgame.robo.contenders;
 
 import com.eatthatgame.robo.Contender;
 
 public class John extends Contender 
 {
             
     public John() 
     {
         this.name = "Blitz";
         this.age = 6;
         this.attackPower = 25; 
         this.defensePower = 12;
     }
     
     @Override
     public void AI()
     {
         if(getHealth() > 80)
         {
             System.out.println(name + " uses rocket grab!");
             attack = true;
         }
         
         else if(getHealth() < 60 && getHealth() > 40)
         {
             System.out.println(name + " overdrives its defenses!");
             attack = false;
         }
         
         else if(getHealth() < 25)
         {
             System.out.println(name + " uses mana barrier!");
             attackPower = 5;
             defensePower = 32;
             attack = false;
         }
         
         else
         {
             System.out.println(name + " does it's Power Fist and Static Field Combo Wumbo!");
             attack = true;
         }
     }
}
