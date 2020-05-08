 package DD.Character.CharacterSheet.Monster;
 
 import DD.ActionBox.Dice;
 import DD.Character.CharacterSheet.CharacterSheet;
 import DD.Character.Equipment.Weapon;
 
 public class Goblin {

 	CharacterSheet sheet = new CharacterSheet();
 	int XP = 135;
 	
 	public Goblin()
 	{
 		sheet.rawStats[CharacterSheet.ABILITY_STRENGTH][CharacterSheet.ABILITY_TOTAL] = 11;
 		sheet.rawStats[CharacterSheet.ABILITY_DEXTERITY][CharacterSheet.ABILITY_TOTAL] = 15;
 		sheet.rawStats[CharacterSheet.ABILITY_CONSTITUTION][CharacterSheet.ABILITY_TOTAL] = 12;
 		sheet.rawStats[CharacterSheet.ABILITY_INTELLIGENCE][CharacterSheet.ABILITY_TOTAL] = 10;
 		sheet.rawStats[CharacterSheet.ABILITY_WISDOM][CharacterSheet.ABILITY_TOTAL] = 9;
 		sheet.rawStats[CharacterSheet.ABILITY_CHARISMA][CharacterSheet.ABILITY_TOTAL] = 6;
 		
 		//sheet.attacks[CharacterSheet.ATTACK_MELEE][CharacterSheet.ATTACK_TOTAL]= 1;
 		sheet.setMeleeTotal(1);
 		
 	
 		sheet.setACTotal(16);
 		sheet.setTouchTotal(13);
 		sheet.setFlatfootArmor(2);
 		sheet.setFlatfootShield(1);
 		sheet.setFlatfootSize(1);
 		sheet.setFlatfootTotal(14);
 		
 		sheet.setFortTotal(3);
 		sheet.setRefTotal(2);
 		sheet.setWillTotal(-1);
 		
 		//sheet has a hitpoints variable that holds the hp of the monster or person this sets it to 6 + rolls
 		//a 1d10 die to determine the hp
		sheet.setHP(6);
 		
 		sheet.init = sheet.getInit() + 6;
 		
 		sheet.EquipWeapon(new Weapon("Cutlass",Dice.DieSize.D6,2,18,5,'M','S',"Note:",4));
 		
 		 XP = 135;
 	}
 	
 	public int getXP()
 	{
 		return XP;
 	}
 	public int getHP()
 	{
		return sheet.hitpoints;
 	}
 	public int getMelee()
 	{
 		return 1;
 	}
 	public int getCmb()
 	{
 		return 0;
 	}
 	public int getCmd()
 	{
 		return 12;
 	}
 	public int getFort()
 	{
 		return 3;
 	}
 	public int getRef()
 	{
 		return 2;
 	}
 	public int getWill()
 	{
 		return -1;
 	}
 	public int getAC()
 	{
 		return 16;
 	}
 	public int getTouch()
 	{
 		return 13;
 	}
 	public int getFlatfoot()
 	{
 		return 14;
 	}
 	public int getArmor()
 	{
 		return 2;
 	}
 	public int getShield()
 	{
 		return 1;
 	}
 	public int getSize()
 	{
 		return 1;
 	}
 	public int getInit()
 	{
 		return sheet.init;
 	}
 	
 	public Weapon getWeapon()
 	{
 		return sheet.getEquippedWeapon(0);
 	}
 }
