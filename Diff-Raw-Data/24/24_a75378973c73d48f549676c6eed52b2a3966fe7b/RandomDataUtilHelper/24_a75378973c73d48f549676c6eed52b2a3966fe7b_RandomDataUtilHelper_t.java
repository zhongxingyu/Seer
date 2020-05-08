 package edu.brown.cs32.goingrogue.gameobjects.creatures.util;
 
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Attribute;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.CreatureStats;
 import edu.brown.cs32.goingrogue.gameobjects.items.ItemStats;
 import java.util.List;
 import static edu.brown.cs32.goingrogue.gameobjects.creatures.Attribute.*;
 import static edu.brown.cs32.bweedon.random.BweedonRandom.*;
 import static java.lang.Math.PI;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class RandomDataUtilHelper {
 
     static CreatureStats getEmuStats(List<Attribute> attributes) {
         double attack = randomDouble(75, 100);
         double defense = randomDouble(100, 125);
         int health = randomInt(30, 40);
         double accuracy = randomDouble(75, 100);
        double speed = randomDouble(0.05, 0.06);
         return new CreatureStats(attack, defense, health, accuracy, speed);
     }
 
     static CreatureStats getDragonStats(List<Attribute> attributes) {
         double attack = randomDouble(200, 260);
         double defense = randomDouble(250, 300);
         int health = randomInt(500, 550);
         double accuracy = randomDouble(30, 60);
        double speed = randomDouble(0.009, 0.01);
         return new CreatureStats(attack, defense, health, accuracy, speed);
     }
 
     static CreatureStats getHobGoblinStats(List<Attribute> attributes) {
         double attack = randomDouble(80, 120);
         double defense = randomDouble(80, 100);
         int health = randomInt(50, 70);
         double accuracy = randomDouble(80, 110);
        double speed = randomDouble(0.02, 0.03);
         return new CreatureStats(attack, defense, health, accuracy, speed);
     }
 
     static CreatureStats getGiantStats(List<Attribute> attributes) {
         double attack = randomDouble(150, 200);
         double defense = randomDouble(200, 250);
         int health = randomInt(200, 220);
         double accuracy = randomDouble(10, 15);
        double speed = randomDouble(0.01, 0.015);
         return new CreatureStats(attack, defense, health, accuracy, speed);
     }
 
     static CreatureStats getSnakeStats(List<Attribute> attributes) {
         double attack = randomDouble(90, 100);
         double defense = randomDouble(30, 40);
         int health = randomInt(5, 10);
         double accuracy = randomDouble(100, 120);
        double speed = randomDouble(0.04, 0.05);
         return new CreatureStats(attack, defense, health, accuracy, speed);
     }
 
     static CreatureStats getDogStats(List<Attribute> attributes) {
         double attack = randomDouble(70, 80);
         double defense = randomDouble(50, 65);
         int health = randomInt(20, 30);
         double accuracy = randomDouble(75, 90);
        double speed = randomDouble(0.05, 0.06);
         return new CreatureStats(attack, defense, health, accuracy, speed);
     }
 
     static ItemStats getSwordStats(List<Attribute> attributes) {
         double attack = scaleToAttributes(randomDouble(20, 25), attributes, true);
         double defense = scaleToAttributes(randomDouble(10, 15), attributes, true);
         int health = (int) scaleToAttributes(randomInt(0, 0), attributes, true);
         double accuracy = scaleToAttributes(randomDouble(5, 10), attributes, true);
         double speed = scaleToAttributes(randomDouble(5, 10), attributes, false);
         double reloadRate = scaleToAttributes(randomDouble(20, 25), attributes, false);
         double range = scaleToAttributes(randomDouble(0.5, 0.75), attributes, true);
         double arcLength = scaleToAttributes(randomDouble((PI / 2.0) - (PI / 3.0), (PI / 2.0) + (PI / 3.0)),
                 attributes, true);
         int attackTimer = getAttackTimer(speed);//(int) scaleToAttributes(randomInt(10, 15), attributes, false);
         return new ItemStats(attack, defense, health, accuracy, speed, reloadRate,
                 range, arcLength, attackTimer);
     }
 
     static ItemStats getAxeStats(List<Attribute> attributes) {
         double attack = scaleToAttributes(randomDouble(30, 35), attributes, true);
         double defense = scaleToAttributes(randomDouble(5, 10), attributes, true);
         int health = (int) scaleToAttributes(randomInt(0, 0), attributes, true);
         double accuracy = scaleToAttributes(randomDouble(2, 6), attributes, true);
         double speed = scaleToAttributes(randomDouble(10, 15), attributes, false);
         double reloadRate = scaleToAttributes(randomDouble(25, 30), attributes, false);
         double range = scaleToAttributes(randomDouble(0.25, 0.5), attributes, true);
         double arcLength = scaleToAttributes(randomDouble((PI / 2.0) - (PI / 6.0), (PI / 2.0) + (PI / 3.0) + (PI / 6.0)),
                 attributes, true);
         int attackTimer = getAttackTimer(speed);//(int) scaleToAttributes(randomInt(15, 20), attributes, false);
         return new ItemStats(attack, defense, health, accuracy, speed, reloadRate,
                 range, arcLength, attackTimer);
     }
 
     static ItemStats getWarHammerStats(List<Attribute> attributes) {
         double attack = scaleToAttributes(randomDouble(25, 30), attributes, true);
         double defense = scaleToAttributes(randomDouble(10, 15), attributes, true);
         int health = (int) scaleToAttributes(randomInt(0, 0), attributes, true);
         double accuracy = scaleToAttributes(randomDouble(1, 5), attributes, true);
         double speed = scaleToAttributes(randomDouble(10, 20), attributes, false);
         double reloadRate = scaleToAttributes(randomDouble(30, 35), attributes, false);
         double range = scaleToAttributes(randomDouble(0.25, 0.5), attributes, true);
         double arcLength = scaleToAttributes(randomDouble((PI / 2.0) - (PI / 3.0), (PI / 2.0) + (PI / 3.0)),
                 attributes, true);
         int attackTimer = getAttackTimer(speed);//(int) scaleToAttributes(randomInt(15, 20), attributes, false);
         return new ItemStats(attack, defense, health, accuracy, speed, reloadRate,
                 range, arcLength, attackTimer);
     }
 
     static ItemStats getSpearStats(List<Attribute> attributes) {
         double attack = scaleToAttributes(randomDouble(30, 35), attributes, true);
         double defense = scaleToAttributes(randomDouble(5, 10), attributes, true);
         int health = (int) scaleToAttributes(randomInt(0, 0), attributes, true);
         double accuracy = scaleToAttributes(randomDouble(20, 25), attributes, true);
         double speed = scaleToAttributes(randomDouble(2, 5), attributes, false);
         double reloadRate = scaleToAttributes(randomDouble(10, 5), attributes, false);
         double range = scaleToAttributes(randomDouble(1.0, 1.5), attributes, true);
         double arcLength = scaleToAttributes(randomDouble(PI / 6.0, PI / 6.0),
                 attributes, true);
         int attackTimer = getAttackTimer(speed);//(int) scaleToAttributes(randomInt(7, 12), attributes, false);
         return new ItemStats(attack, defense, health, accuracy, speed, reloadRate,
                 range, arcLength, attackTimer);
     }
 
     static ItemStats getArmourStats(List<Attribute> attributes) {
         double attack = scaleToAttributes(randomDouble(0, 0), attributes, true);
         double defense = scaleToAttributes(randomDouble(40, 45), attributes, true);
         int health = (int) scaleToAttributes(randomInt(0, 0), attributes, true);
         double accuracy = scaleToAttributes(randomDouble(0, 0), attributes, true);
         double speed = scaleToAttributes(randomDouble(20, 25), attributes, false);
         double reloadRate = scaleToAttributes(randomDouble(0, 0), attributes, false);
         double range = 0.0;
         double arcLength = 0.0;
         int attackTimer = 0;
         return new ItemStats(attack, defense, health, accuracy, speed, reloadRate,
                 range, arcLength, attackTimer);
     }
 
     static ItemStats getShieldStats(List<Attribute> attributes) {
         double attack = scaleToAttributes(randomDouble(0, 0), attributes, true);
         double defense = scaleToAttributes(randomDouble(30, 35), attributes, true);
         int health = (int) scaleToAttributes(randomInt(0, 0), attributes, true);
         double accuracy = scaleToAttributes(randomDouble(0, 0), attributes, true);
         double speed = scaleToAttributes(randomDouble(10, 15), attributes, false);
         double reloadRate = scaleToAttributes(randomDouble(0, 0), attributes, false);
         double range = 0.0;
         double arcLength = 0.0;
         int attackTimer = 0;
         return new ItemStats(attack, defense, health, accuracy, speed, reloadRate,
                 range, arcLength, attackTimer);
     }
 
     static ItemStats getPotionStats(List<Attribute> attributes) {
         double attack = scaleToAttributes(randomDouble(0, 0), attributes, true);
         double defense = scaleToAttributes(randomDouble(0, 0), attributes, true);
         int health = (int) scaleToAttributes(randomInt(0, 0), attributes, true);
         double accuracy = scaleToAttributes(randomDouble(0, 0), attributes, true);
         double speed = scaleToAttributes(randomDouble(0, 0), attributes, false);
         double reloadRate = scaleToAttributes(randomDouble(0, 0), attributes, false);
         double range = 0.0;
         double arcLength = 0.0;
         int attackTimer = 0;
         return new ItemStats(attack, defense, health, accuracy, speed, reloadRate,
                 range, arcLength, attackTimer);
     }
 
     private static double scaleToAttributes(double origValue, List<Attribute> attributes, boolean multiply) {
         if (multiply) {
             if (attributes.contains(WOOD)) {
                 return origValue * 1.0;
             } else if (attributes.contains(BRONZE)) {
                 return origValue * 1.25;
             } else if (attributes.contains(IRON)) {
                 return origValue * 1.5;
             } else if (attributes.contains(STEEL)) {
                 return origValue * 1.75;
             } else if (attributes.contains(MITHRIL)) {
                 return origValue * 2.0;
             } else {
                 return origValue;
             }
         } else {
             if (attributes.contains(WOOD)) {
                 return origValue / 1.0;
             } else if (attributes.contains(BRONZE)) {
                 return origValue / 1.25;
             } else if (attributes.contains(IRON)) {
                 return origValue / 1.5;
             } else if (attributes.contains(STEEL)) {
                 return origValue / 1.75;
             } else if (attributes.contains(MITHRIL)) {
                 return origValue / 2.0;
             } else {
                 return origValue;
             }
         }
     }
 
     //Returns an attack timer for an object of given speed
     static int getAttackTimer(double speed) {
         return (int) (20 * (50 + speed));
     }
 }
