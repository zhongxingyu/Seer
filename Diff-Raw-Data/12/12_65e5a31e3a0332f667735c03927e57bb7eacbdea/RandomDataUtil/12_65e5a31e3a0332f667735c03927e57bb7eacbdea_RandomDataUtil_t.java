 package edu.brown.cs32.goingrogue.gameobjects.creatures.util;
 
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Attribute;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import static edu.brown.cs32.goingrogue.gameobjects.creatures.Attribute.*;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.CreatureStats;
 import edu.brown.cs32.goingrogue.gameobjects.items.ItemStats;
 import static edu.brown.cs32.goingrogue.graphics.GraphicsPaths.*;
 import static edu.brown.cs32.goingrogue.gameobjects.creatures.util.RandomDataUtilHelper.*;
 import edu.brown.cs32.goingrogue.util.CreatureSize;
 import edu.brown.cs32.goingrogue.util.Text;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class RandomDataUtil {
 
     public static List<Attribute> randomCreatureAttributes(int playerLevel) {
         List<Attribute> attributes = new ArrayList<>();
         Random generator = new Random(System.currentTimeMillis());
 
         // elemental type of monster
         int choice = generator.nextInt(4);
         switch (choice) {
             case 0:
                 attributes.add(FIRE_TYPE);
                 break;
             case 1:
                 attributes.add(WATER_TYPE);
                 break;
             case 2:
                 attributes.add(EARTH_TYPE);
                 break;
             case 3:
                 attributes.add(AIR_TYPE);
                 break;
         }
 
         // type of monster
         if (playerLevel <= 2) {
             choice = generator.nextInt(3);
         } else if (playerLevel == 3) {
             choice = generator.nextInt(4);
         } else if (playerLevel == 4) {
             choice = generator.nextInt(5);
         } else if (playerLevel >= 5) {
             choice = generator.nextInt(6);
         }
         switch (choice) {
             case 0:
                 attributes.add(EMU);
                 break;
             case 1:
                 attributes.add(SNAKE);
                 break;
             case 2:
                 attributes.add(DOG);
                 break;
             case 3:
                 attributes.add(HOB_GOBLIN);
                 break;
             case 4:
                 attributes.add(GIANT);
                 break;
             case 5:
                 attributes.add(DRAGON);
                 break;
         }
         return attributes;
     }
 
     public static List<Attribute> randomItemAttributes() {
         List<Attribute> attributes = new ArrayList<>();
         attributes.add(ITEM);
         Random generator = new Random(System.currentTimeMillis());
 
         // type of item
         int choice = generator.nextInt(6);
         switch (choice) {
             case 0:
                 attributes.add(WEAPON);
                 // type of weapon
                 choice = generator.nextInt(4);
                 switch (choice) {
                     case 0:
                         attributes.add(SWORD);
                         break;
                     case 1:
                         attributes.add(AXE);
                         break;
                     case 2:
                         attributes.add(WAR_HAMMER);
                         break;
                     case 3:
                         attributes.add(SPEAR);
                         break;
                 }
                 break;
             case 1:
                 attributes.add(ARMOUR);
                 break;
             case 2:
                 attributes.add(SHIELD);
                 break;
             case 3:
                 attributes.add(POTION_TYPE);
                 choice = generator.nextInt(3);
                 switch (choice) {
                    case 0:
                         attributes.add(ATTACK_POTION);
                        break;
                    case 1:
                         attributes.add(DEFENSE_POTION);
                        break;
                    case 2:
                         attributes.add(HEALTH_POTION);
                        break;
                 }
                 break;
             case 4:
                 attributes.add(HELMET);
                 break;
             case 5:
                 attributes.add(BOOTS);
                 break;
         }
 
         // type of material
         choice = generator.nextInt(5);
         switch (choice) {
             case 0:
                 attributes.add(STEEL);
                 break;
             case 1:
                 attributes.add(IRON);
                 break;
             case 2:
                 attributes.add(BRONZE);
                 break;
             case 3:
                 attributes.add(WOOD);
                 break;
             case 4:
                 attributes.add(MITHRIL);
                 break;
         }
         return attributes;
     }
 
     public static String getCreatureName(List<Attribute> attributes) {
         String name = "";
         if (attributes.contains(FIRE_TYPE)) {
             name += "Fire ";
         } else if (attributes.contains(WATER_TYPE)) {
             name += "Water ";
         } else if (attributes.contains(EARTH_TYPE)) {
             name += "Earth ";
         } else if (attributes.contains(AIR_TYPE)) {
             name += "Air ";
         }
 
         if (attributes.contains(EMU)) {
             name += "Emu";
         } else if (attributes.contains(DRAGON)) {
             name += "Dragon";
         } else if (attributes.contains(HOB_GOBLIN)) {
             name += "Hob Goblin";
         } else if (attributes.contains(GIANT)) {
             name += "Ice Giant";
         } else if (attributes.contains(SNAKE)) {
             name += "Snake";
         } else if (attributes.contains(DOG)) {
             name += "Dog";
         }
 
         return name;
     }
 
     public static String getItemName(List<Attribute> attributes) {
 
         return Text.getText(attributes).getText();
     }
 
     public static String getSprite(List<Attribute> attributes) {
         if (attributes.contains(EMU)) {
             return EMU_SPRITE.path;
         } else if (attributes.contains(DRAGON)) {
             return DRAGON_SPRITE.path;
         } else if (attributes.contains(HOB_GOBLIN)) {
             return HOB_GOBLIN_SPRITE.path;
         } else if (attributes.contains(GIANT)) {
             return GIANT_SPRITE.path;
         } else if (attributes.contains(SNAKE)) {
             return SNAKE_SPRITE.path;
         } else if (attributes.contains(DOG)) {
             return DOG_SPRITE.path;
         } else if (attributes.contains(SWORD)) {
             return SWORD_SPRITE.path;
         } else if (attributes.contains(AXE)) {
             return AXE_SPRITE.path;
         } else if (attributes.contains(WAR_HAMMER)) {
             return WAR_HAMMER_SPRITE.path;
         } else if (attributes.contains(SPEAR)) {
             return SPEAR_SPRITE.path;
         } else if (attributes.contains(ARMOUR)) {
             return ARMOUR_SPRITE.path;
         } else if (attributes.contains(SHIELD)) {
             return SHIELD_SPRITE.path;
         } else if (attributes.contains(ATTACK_POTION)) {
             return ATTACK_POTION_SPRITE.path;
         } else if (attributes.contains(DEFENSE_POTION)) {
             return DEFENSE_POTION_SPRITE.path;
         } else if (attributes.contains(HEALTH_POTION)) {
             return HEALTH_POTION_SPRITE.path;
         } else if (attributes.contains(HELMET)) {
             return HELMET_SPRITE.path;
         } else if (attributes.contains(BOOTS)) {
             return BOOTS_SPRITE.path;
         } else {
             return null; // TODO leave null here?
         }
     }
 
     public static CreatureStats randomCreatureStats(List<Attribute> attributes) {
         if (attributes.contains(EMU)) {
             return getEmuStats(attributes);
         } else if (attributes.contains(DRAGON)) {
             return getDragonStats(attributes);
         } else if (attributes.contains(HOB_GOBLIN)) {
             return getHobGoblinStats(attributes);
         } else if (attributes.contains(GIANT)) {
             return getGiantStats(attributes);
         } else if (attributes.contains(SNAKE)) {
             return getSnakeStats(attributes);
         } else if (attributes.contains(DOG)) {
             return getDogStats(attributes);
         } else {
             return null; // TODO OK to return null?
         }
     }
 
     public static ItemStats randomItemStats(List<Attribute> attributes) {
         if (attributes.contains(SWORD)) {
             return getSwordStats(attributes);
         } else if (attributes.contains(AXE)) {
             return getAxeStats(attributes);
         } else if (attributes.contains(WAR_HAMMER)) {
             return getWarHammerStats(attributes);
         } else if (attributes.contains(SPEAR)) {
             return getSpearStats(attributes);
         } else if (attributes.contains(ARMOUR)) {
             return getArmourStats(attributes);
         } else if (attributes.contains(SHIELD)) {
             return getShieldStats(attributes);
         } else if (attributes.contains(HELMET)) {
             return getHelmetStats(attributes);
         } else if (attributes.contains(BOOTS)) {
             return getBootsStats(attributes);
         } else if (attributes.contains(POTION_TYPE)) {
             return getPotionStats(attributes);
         } else {
             return null; // TODO OK to return null?
         }
     }
 
     public static CreatureSize getCreatureSize(List<Attribute> attributes) {
         if (attributes.contains(EMU)) {
             return new CreatureSize(1.0, 1.0);
         } else if (attributes.contains(DRAGON)) {
             return new CreatureSize(2.0, 7.0);
         } else if (attributes.contains(HOB_GOBLIN)) {
             return new CreatureSize(1.0, 1.0);
         } else if (attributes.contains(GIANT)) {
             return new CreatureSize(3.0, 3.0);
         } else if (attributes.contains(SNAKE)) {
             return new CreatureSize(1.0, 1.0);
         } else if (attributes.contains(DOG)) {
             return new CreatureSize(1.0, 1.0);
         } else {
             return null; // TODO OK to return null?
         }
     }
 }
