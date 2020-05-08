 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package SimpleSpace;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  *
  * @author G89390
  */
 public class entity {
     /*
      * class primary variables
      */
     public boolean isAlive;
     public double HP, dmgModifier;
     public int lootvalue, lootrolls;
     public String name, initSpam;
     public Random gen = new Random();
     ArrayList<item> inventory;
     double HPMax;
     
     entity() {
         isAlive = true;
         HP = 2000;
         name = "Porphyrian Battle Cruiser";
         dmgModifier = 150;
         lootvalue = 200;
         lootrolls = 1;
         initSpam = "(V)...(-,,,-)...(Y) A crab like ship approaches!";
     }
     
     entity(entity a) {
         this.isAlive = a.isAlive;
         this.HP = a.HP;
         this.dmgModifier = a.dmgModifier;
         this.lootvalue = a.lootvalue;
         this.lootrolls = a.lootrolls;
         this.name = a.name;
         this.initSpam = a.initSpam;
         this.gen = a.gen;
         this.inventory = a.inventory;
         this.HPMax = a.HPMax;
     }
     
     entity(boolean alv, double hp, String nm, String is) {
         isAlive = alv;
         HP = hp;
         name = nm;
         lootrolls = 1;
     }
     
     entity(boolean alive, double hitpoints, String entName, double dmgM, int lootv ) {
         isAlive = alive;
         HP = hitpoints;
         name = entName;
         dmgModifier = dmgM;
         lootvalue = lootv;
         lootrolls = 1;
     } 
     
     entity(boolean alive, double hitpoints, String entName, double dmgM, int lootv, int lootrolls ) {
         isAlive = alive;
         HP = hitpoints;
         name = entName;
         dmgModifier = dmgM;
         lootvalue = lootv;
     }
     
     entity(boolean alive, double hitpoints, String entName, double dmgM) {
         isAlive = alive;
         HP = hitpoints;
         HPMax = hitpoints;
         name = entName;
         dmgModifier = dmgM;
         inventory = new ArrayList();
     }    
     public double dealDamage() {
         double result = dmgModifier * gen.nextDouble();
         System.out.println(name + " fired for " + result + " damage!");
         return result;
     }
     
     public void takeDamage(double damage) {
         System.out.println(name + " took " + damage + " damage!");
         HP -= damage;
         System.out.println(name + "'s health: " + HP);
         if (HP <= 0) isAlive = false;
         else isAlive = true;
     }
     
     public void battle(entity target) {
         takeDamage(target.dealDamage());
         target.takeDamage(dealDamage());
     }
     
     public void loot( ArrayList<item> inventory) {
         for(;lootrolls > 0; lootrolls--) {
            int choice = gen.nextInt(inventory.size() - 1);
             inventory.get(choice).qty += lootvalue;
             System.out.println("Looting " + name + " yielded " + lootvalue + " " + inventory.get(choice).title + " (Total = " + inventory.get(choice).qty + ")");
         }
     }
 }
