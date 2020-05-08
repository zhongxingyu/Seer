 package gameObjects;
 
 import java.util.*;
 
 import gameObjects.items.*;
 import gameObjects.items.Armors.*;
 import gameObjects.items.Weapons.*;
 import gameObjects.items.Potions.*;
 
 public class ItemFactory {
 
 	
 	public static IItem generate() {
 	  IItem item = null;
 	  Random rand = new Random();
 	  int itemType = rand.nextInt(3) + 1; 
 	  
 	  switch (itemType) {
 	  case 1:
 		item = generateArmor();
 		break;
 	  case 2:
 		item = generateWeapon();
 		break;
 	  case 3:
 	    item = generatePotion();
 	    break;
 	  }
 	  
	  
 	  return item;
 	}
 	
 	public static IItem generateArmor() {
 	  Random rand = new Random();
 	  IItem armor = null;
 	  int armorType = rand.nextInt(7) + 1;
 	  
 	  switch (armorType) {
 	  case 1:
 		armor = new Chainmail();
 		break;
 	  case 2:
 		armor = new Clothes();
 		break;
 	  case 3:
 		armor = new GlassArmor();
 		break;
 	  case 4:
 		armor = new LeatherArmor();
 		break;
 	  case 5:
 		armor = new LoinCloth();
 		break;
 	  case 6:
 		armor = new PlateArmor();
 		break;
 	  case 7:
 		armor = new WizardRobe();
 		break;
 	  }
 	  
 	  
 	  return armor;
 	  
 	}
 	
 	public static IItem generateWeapon() {
 		Random rand = new Random();
 		IItem weapon = null;
 		int weaponType = rand.nextInt(6) + 1;
 
 		  switch (weaponType) {
 		  case 1:
 			weapon = new BastardSword();
 			break;
 		  case 2:
 			weapon = new Broadsword();
 			break;
 		  case 3:
 			weapon = new Katana();
 			break;
 		  case 4:
 			weapon = new StaffOfMana();
 			break;
 		  case 5:
 			weapon = new TwinDaggers();
 			break;
 		  case 6:
 			weapon = new WoodenSword();
 			break;
 		  }
 		
 		
 		return weapon;
 	}
 	
 	public static IItem generatePotion() {
 		  Random rand = new Random();
 		  IItem potion = null;
 		  int potionType = rand.nextInt(6) + 1;
 		  
 		  switch (potionType) {
 		  case 1:
 			potion = new CureAllPotion();
 			break;
 		  case 2:
 			potion = new MajorHealthPotion();
 			break;
 		  case 3:
 			potion = new MinorDefensePotion();
 			break;
 		  case 4:
 			potion = new MinorHealthPotion();
 			break;
 		  case 5:
 			potion = new StandardHealthPotion();
 			break;
 		  case 6:
 			potion = new UltraHealthPotion();
 			break;
 		  }
 		  
 		  
 		  return potion;
 		  
 		}
 }
