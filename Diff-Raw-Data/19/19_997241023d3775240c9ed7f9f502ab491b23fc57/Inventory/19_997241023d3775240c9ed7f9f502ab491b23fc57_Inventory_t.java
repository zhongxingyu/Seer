 package edu.brown.cs32.goingrogue.gameobjects.creatures;
 
 import edu.brown.cs32.goingrogue.gameobjects.actions.QuaffAction;
 import edu.brown.cs32.goingrogue.gameobjects.items.Item;
 
 import java.util.List;
 import static edu.brown.cs32.goingrogue.gameobjects.creatures.Attribute.*;
 import edu.brown.cs32.goingrogue.gameobjects.items.AttackPotion;
 import edu.brown.cs32.goingrogue.gameobjects.items.DefensePotion;
 import edu.brown.cs32.goingrogue.gameobjects.items.HealthPotion;
 import edu.brown.cs32.goingrogue.gameobjects.items.ItemStats;
 import edu.brown.cs32.goingrogue.gameobjects.items.Potion;
 import java.util.ArrayList;
 
 /**
  *
  * @author Ben Weedon (bweedon)
  */
 public class Inventory {
 
     private Item _weapon;
     private Item _armour;
     private Item _shield;
     private Item _helmet;
     private Item _boots;
     private List<Potion> _potions;
     private Creature _creature;
     private final int MAX_NUM_POTIONS = 5;
 
     public Inventory(Creature creature) {
         _weapon = null;
         _armour = null;
         _shield = null;
         _helmet = null;
         _boots = null;
         _potions = new ArrayList<>();
         _creature = creature;
     }
 
     public void add(Item item) {
         if (item.containsAttribute(WEAPON)) {
             if (_weapon != null) {
                 swap(item, _weapon);
             } else {
                 _weapon = item;
             }
         } else if (item.containsAttribute(ARMOUR)) {
             if (_armour != null) {
                 swap(item, _armour);
             } else {
                 _armour = item;
             }
         } else if (item.containsAttribute(SHIELD)) {
             if (_shield != null) {
                 swap(item, _shield);
             } else {
                 _shield = item;
             }
         } else if (item.containsAttribute(HELMET)) {
             if (_helmet != null) {
                 swap(item, _helmet);
             } else {
                 _helmet = item;
             }
         } else if (item.containsAttribute(BOOTS)) {
             if (_boots != null) {
                 swap(item, _boots);
             } else {
                 _boots = item;
             }
         } else if (item.containsAttribute(POTION_TYPE)) {
             if (_potions.size() < MAX_NUM_POTIONS) {
                 _potions.add(makePotion(item));
             } else {
                 _creature.addAction(new QuaffAction(item, _creature));
             }
         }
     }
 
     public Item getWeapon() {
         return _weapon;
     }
 
     public Item getArmour() {
         return _armour;
     }
 
     public Item getShield() {
         return _shield;
     }
 
     public Item getHelmet() {
         return _helmet;
     }
 
     public Item getBoots() {
         return _boots;
     }
 
     public Item getPotion(int index) {
         return _potions.get(index);
     }
 
     public void dropPotion(int index) {
         _potions.remove(index);
     }
 
     public int getNumPotions() {
         return _potions.size();
     }
 
     public double getAttackSum() {
         List<Item> thingsToAdd = getThingsToAdd(_weapon, _armour, _shield, _helmet, _boots);
         double sum = 0.0;
         for (Item currItem : thingsToAdd) {
             sum += currItem.getStats().getAttack();
         }
         return sum;
     }
 
     public double getDefenseSum() {
         List<Item> thingsToAdd = getThingsToAdd(_weapon, _armour, _shield, _helmet, _boots);
         double sum = 0.0;
         for (Item currItem : thingsToAdd) {
             sum += currItem.getStats().getDefense();
         }
         return sum;
     }
 
     public double getAccuracySum() {
         List<Item> thingsToAdd = getThingsToAdd(_weapon, _armour, _shield, _helmet, _boots);
         double sum = 0.0;
         for (Item currItem : thingsToAdd) {
             sum += currItem.getStats().getAccuracy();
         }
         return sum;
     }
 
     public double getSpeedSum() {
         List<Item> thingsToAdd = getThingsToAdd(_weapon, _armour, _shield, _helmet, _boots);
         double sum = 0.0;
         for (Item currItem : thingsToAdd) {
             sum += currItem.getStats().getSpeed();
         }
         return sum;
     }
 
     private List<Item> getThingsToAdd(Item weapon, Item armour, Item shield, Item helmet, Item boots) {
         List<Item> thingsToAdd = new ArrayList<>();
         if (weapon != null) {
             thingsToAdd.add(weapon);
         }
         if (armour != null) {
             thingsToAdd.add(armour);
         }
         if (shield != null) {
             thingsToAdd.add(shield);
         }
         if (helmet != null) {
             thingsToAdd.add(helmet);
         }
         if (boots != null) {
             thingsToAdd.add(boots);
         }
         return thingsToAdd;
     }
 
     private void swap(Item i1, Item i2) {
         ItemStats temp = i1.getGridItem().getItemStats();
         i2.getGridItem().setItemStats(temp);
         i1.getGridItem().setItemStats(i2.getGridItem().getItemStats());
         i1.getGridItem().setPickedUp(false);
         i2.getGridItem().setPickedUp(false);
     }
 
    public static Potion makePotion(Item item) {
         Potion returnPotion = null;
         if (item.containsAttribute(HEALTH_POTION)) {
             returnPotion = new HealthPotion(item.getGridItem());
         } else if (item.containsAttribute(ATTACK_POTION)) {
             returnPotion = new AttackPotion(item.getGridItem());
         } else if (item.containsAttribute(DEFENSE_POTION)) {
             returnPotion = new DefensePotion(item.getGridItem());
         }
         return returnPotion;
     }
 }
