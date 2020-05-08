 package com.fisherevans.lrk.rpg;
 
 import com.fisherevans.lrk.Resources;
 import com.fisherevans.lrk.rpg.items.Armor;
 import com.fisherevans.lrk.rpg.items.Consumable;
 import com.fisherevans.lrk.rpg.items.Equipment;
 import com.fisherevans.lrk.rpg.items.Weapon;
 import com.fisherevans.lrk.rpg.items.enchantments.UnholyMight;
import com.fisherevans.lrk.states.adventure.combat.skills.Slash;
import com.fisherevans.lrk.states.adventure.entities.AdventureEntity;
 
 /**
  * User: Fisher evans
  * Date: 5/31/13
  * Time: 8:03 AM
  */
 public class RPGEntityGenerator
 {
     public static Player getAnarok()
     {
         RPGEntity entity = new RPGEntity("Anarok", RPGEntity.Profession.Mage, 99, 80, 15, 20);
 
         Inventory inventory = new Inventory();
         inventory.addItem(new Weapon("Short Sword", "A simple sword", Resources.getImage("equipment/weapons/sword_01"), Equipment.Position.MainHand, 4, 0));
         inventory.addItem(new Weapon("Battle Axe", "A bloody axe used in battle", Resources.getImage("equipment/weapons/battle-axe_01"), Equipment.Position.MainHand, 7, 0));
         inventory.addItem(new Weapon("Wooden Shield", "Won't do you much good", Resources.getImage("equipment/weapons/wood-shield_01"), Equipment.Position.OffHand, 0, 5));
         inventory.addItem(new Weapon("Scythe", "Cuts more then grain", Resources.getImage("equipment/weapons/special/scythe"), Equipment.Position.MainHand, 0, 8, true));
 
         Weapon enchanted = new Weapon("Tuning Fork", "A large, powerful sword", Resources.getImage("equipment/weapons/special/tuning-fork"), Equipment.Position.MainHand, 11, 0, true);
         enchanted.setEnchantment(new UnholyMight(0.45f));
        enchanted.setSkill(new Slash(AdventureEntity.Team.Hostile));
         inventory.addItem(enchanted);
 
         inventory.addItem(new Armor("Iron Helm", "A modest helmet", Resources.getImage("equipment/armor/iron-helm_01"), Equipment.Position.Head, 0, 6));
         inventory.addItem(new Armor("Rusty Chainmail", "Old, but useful", Resources.getImage("equipment/armor/chainmail_01"), Equipment.Position.Chest, 0, 10));
         inventory.addItem(new Armor("Leather Boots", "Light and reliable", Resources.getImage("equipment/armor/leather-boots_01"), Equipment.Position.Legs, 0, 4));
         inventory.addItem(new Armor("Dragon Plate", "Stronger than diamond", Resources.getImage("equipment/armor/special/dragon-plate"), Equipment.Position.Chest, 2, 25));
         inventory.addItem(new Armor("Steel Plate", "Stronger than diamond", Resources.getImage("equipment/armor/steel-plate_01"), Equipment.Position.Chest, 2, 25));
 
         inventory.addItem(new Consumable("Health Potion", "Heals you for 20 health over 10 seconds", Resources.getImage("consumables/potion_02"), null, 10));
         inventory.addItem(new Consumable("Iron Flesh Potion", "Increases your armor by 10 for 3 seconds", Resources.getImage("consumables/potion_03"), null, 5));
         inventory.addItem(new Consumable("Fire Breath Tonic", "Your attacks set foes on fire 10 seconds", Resources.getImage("consumables/potion_01"), null, 5));
         inventory.addItem(new Consumable("Book of Earthworms", "Your attacks set foes on fire 10 seconds", Resources.getImage("consumables/book_01"), null, 5));
         inventory.addItem(new Consumable("Hamburger", "Your attacks set foes on fire 10 seconds", Resources.getImage("consumables/hamburger"), null, 5));
 
         Player character = new Player(entity, inventory);
 
         return character;
     }
     public static RPGEntity getBlob()
     {
         RPGEntity entity = new RPGEntity("Dumb Blob", RPGEntity.Profession.Warrior, 3, 50, 15, 20);
         return entity;
     }
 }
