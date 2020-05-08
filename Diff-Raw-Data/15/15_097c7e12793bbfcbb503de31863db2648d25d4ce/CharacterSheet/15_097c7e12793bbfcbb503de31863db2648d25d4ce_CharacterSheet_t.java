 package DD.Character.CharacterSheet;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Random;
 import DD.ActionBox.Dice;
 import DD.Character.CharacterSheet.Race.Human;
 import DD.Character.CharacterSheet.Race.Race;
 import DD.Character.CharacterSheet.Race.RaceContainer;
 import DD.Character.Equipment.Armor;
 import DD.Character.Equipment.Armory;
 import DD.Character.Equipment.Equipment;
 import DD.Character.Equipment.Weapon;
 import DD.SlickTools.DDImage;
 
 
 /*****************************************************************************************************
  * This class holds the characters most important information. It contains all the information that
  * concerns a character. The Character Sheet itself is modeled after the Pathfinder character. Thus,
  * there might be some differing elements from other D&D games.
  ******************************************************************************************************/
 
 public class CharacterSheet implements Serializable
 {
 	private static final long serialVersionUID = 3487802644679243707L;
 
 	public CharacterSheet()
 	{
 		String raceName = "Monster";
 		name = "";
 	    player = "";
 		race = new Human();
 		languages = "";
 		size = 0; // medium
 		gender = 0; //male
 		height = 6;//6 feet
 		weight = 130; //130lbs
 		age = 25;
 		alignments="";
 		deity="";
 		background ="";
 		occupation="";
 		base =0; 
 		inher=0;
 		enhance=0;
 		misc=0;
 		EquippedWeapon.add(new Weapon (0, "Unarmed",Dice.DieSize.D3,2,0,0,'M','B',"Note:",0));
 		
 	}
 	/*THE DICE!*/
 	Dice dice = new Dice();
 	/************************************ Class Constants *************************************/
 
 	
 	
 	public static final int ABILITY_COUNT = 5;		/* The number of abilities possessed by a character */
 	public static final int ABILITY_FACTORS = 5;
 	/* The number of factors that affect the ability score */
 	/*
 			 * row index 1 = fort
 			 * row index 2 = reflex
 			 * row index 3 = willpower
 			 * 
 			 * Y:
 			 * column index 0 = total
 			 * column index 1 ...class base.. 
 			 * column index 2 = ability
 			 * column index 3 = enhance
 			 */
 
 	
 	/************************************ Class Attributes *************************************/
 	
 	/******** Basic Info *********/
 	private String raceName;
 	protected String name;
 	private String player;
 	Race race;
 	private String languages;
 	private int size;
 	private int gender;
 	private int height;
 	private int weight;
 	private int age;
 	private String alignments;
 	private String deity;
 	private String background;
 	private String occupation;
 	private DDImage image = null;
 	private int netID;  //TODO: should not be serialized. 
 	
 	private int currency = 1000;
 	
 	
 	public void subCurrency(int money)
 	{
 			currency = currency - money;
 	}
 	
 	public int getCurrency()
 	{
 		return currency;
 	}
 	
 	public void setCurrency(int value)
 	{
 		currency = 1000;
 	}
 	/********* Ability *********/
 	
 	public static final int ABILITY_STRENGTH = 0;
 	public static final int ABILITY_DEXTERITY = 1;
 	public static final int ABILITY_CONSTITUTION = 2;
 	public static final int ABILITY_INTELLIGENCE = 3;
 	public static final int ABILITY_WISDOM = 4;
 	public static final int ABILITY_CHARISMA = 5;
 	public static final int ABILITY_TOTAL = 0;
 	public static final int ABILITY_MODIFIER = 1; // how to get?
 	public static final int ABILITY_BASE = 2;
 	public static final int ABILITY_INHERENT = 3; // how to get?
 	public static final int ABILITY_ENHANCE = 4; // how to get?
 	public static final int ABILITY_MISC = 5;    //what is this?
 	/* 
 	 * each row is Stat names
 	 * X:
 	 * row index 0,: = str
 	 * row index 1,: = dex
 	 * row index 2,: = con
 	 * row index 3,: = intel
 	 * row index 4,: = wis
 	 * row index 5,: = cha
 	 * Y:
 	 * column index 0 = Total of stat
 	 * column index 1 = mod
 	 * column index 2 = base
 	 * column index 3 = inherent
 	 * column index 4 = enhance
 	 * column index 5 = misc
 	 */
 	
 	
 	public int[][] rawStats = new int[6][6];
 	int stat;
 	int base; 
 	int inher;
 	int enhance;
 	int misc;
 	
 	/*STRENGTH*/
 	
 	//SETTERS
 	public void setStrTotal()//enchance set to 0
 	{
 		base =rawStats[ABILITY_STRENGTH][ABILITY_BASE];
 		inher =rawStats[ABILITY_STRENGTH][ABILITY_INHERENT];
 		enhance = 0;
 		misc = 0;
 		
 		rawStats[ABILITY_STRENGTH][ABILITY_TOTAL]= base + inher + enhance + misc;
 		
 	}
 	public void setStrMod()
 	{
 		int total = getStrTotal();
 		rawStats[ABILITY_STRENGTH][ABILITY_MODIFIER] =(total - 10 )/ 2; 
 		
 	}
 	public void setStrBase()
 	{
 		stat = abilityRoll();
 		rawStats[ABILITY_STRENGTH][ABILITY_BASE] = stat;
 	}
 	public void setStrInher(Race race)
 	{
 		stat = race.getStr();
 		rawStats[ABILITY_STRENGTH][ABILITY_INHERENT] = stat;
 	}
 	public void setStrEnhance(/*Not sure what goes here*/)
 	{
 		stat = 0;
 		rawStats[ABILITY_STRENGTH][ABILITY_ENHANCE] = stat;
 	}
 	public void setStrMisc(/*Not sure what goes here*/)
 	{
 		stat = 0;
 		rawStats[ABILITY_STRENGTH][ABILITY_MISC] = stat;
 	}
 	//GETTERS
 	
 	public int getStrTotal()
 	{
 		stat =rawStats[ABILITY_STRENGTH][ABILITY_TOTAL];
 		return stat;
 	}
 	public int getStrMod()
 	{
 		stat =rawStats[ABILITY_STRENGTH][ABILITY_MODIFIER];
 		return stat;
 	}
 	public int getStrBase()
 	{
 		stat =rawStats[ABILITY_STRENGTH][ABILITY_BASE];
 		return stat;
 	}
 	public int getStrInherent()
 	{
 		stat =rawStats[ABILITY_STRENGTH][ABILITY_INHERENT];
 		return stat;
 	}
 	public int getStrEnhance()
 	{
 		stat =rawStats[ABILITY_STRENGTH][ABILITY_ENHANCE];
 		return stat;
 	}
 	public int getStrMisc()
 	{
 		stat =rawStats[ABILITY_STRENGTH][ABILITY_MISC];
 		return stat;
 	}
 	
 	
 	/*DEX*/
 	
 	//SETTERS
 	public void setDexTotal()//enchance set to 0
 	{
 		base =rawStats[ABILITY_DEXTERITY][ABILITY_BASE];
 		inher =rawStats[ABILITY_DEXTERITY][ABILITY_INHERENT];
 		enhance = 0;
 		misc = 0;
 		
 		rawStats[ABILITY_DEXTERITY][ABILITY_TOTAL]= base + inher + enhance + misc;
 		
 	}
 		public void setDexMod()
 		{
 			int total = getDexTotal();
 			rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER] =(total - 10 )/ 2; 
 			
 		}
 		public void setDexBase()
 		{
 			stat = abilityRoll();
 			rawStats[ABILITY_DEXTERITY][ABILITY_BASE] = stat;
 		}
 		public void setDexInher(Race race)
 		{
 			stat = race.getDex();
 			rawStats[ABILITY_DEXTERITY][ABILITY_INHERENT] = stat;
 		}
 		public void setDexEnhance(/*Not sure what goes here*/)
 		{
 			stat = 0;
 			rawStats[ABILITY_DEXTERITY][ABILITY_ENHANCE] = stat;
 		}
 		public void setDexMisc(/*Not sure what goes here*/)
 		{
 			stat = 0;
 			rawStats[ABILITY_DEXTERITY][ABILITY_MISC] = stat;
 		}
 		//GETTERS
 		
 		public int getDexTotal()
 		{
 			stat =rawStats[ABILITY_DEXTERITY][ABILITY_TOTAL];
 			return stat;
 		}
 		public int getDexMod()
 		{
 			stat =rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER];
 			return stat;
 		}
 		public int getDexBase()
 		{
 			stat =rawStats[ABILITY_DEXTERITY][ABILITY_BASE];
 			return stat;
 		}
 		public int getDexInherent()
 		{
 			stat =rawStats[ABILITY_DEXTERITY][ABILITY_INHERENT];
 			return stat;
 		}
 		public int getDexEnhance()
 		{
 			stat =rawStats[ABILITY_DEXTERITY][ABILITY_ENHANCE];
 			return stat;
 		}
 		public int getDexMisc()
 		{
 			stat =rawStats[ABILITY_DEXTERITY][ABILITY_MISC];
 			return stat;
 		}
 	
 	/*CON*/
 		//SETTERS
 		public void setConTotal()//enchance set to 0
 		{
 			base =rawStats[ABILITY_CONSTITUTION][ABILITY_BASE];
 			inher =rawStats[ABILITY_CONSTITUTION][ABILITY_INHERENT];
 			enhance = 0;
 			misc = 0;
 			
 			rawStats[ABILITY_CONSTITUTION][ABILITY_TOTAL]= base + inher + enhance + misc;
 			
 		}
 		public void setConMod()
 		{
 			int total = getConTotal();
 			rawStats[ABILITY_CONSTITUTION][ABILITY_MODIFIER] =(total - 10 )/ 2; 
 			
 		}
 		public void setConBase()
 		{
 			stat = abilityRoll();
 			rawStats[ABILITY_CONSTITUTION][ABILITY_BASE] = stat;
 		}
 		public void setConInher(Race race)
 		{
 			stat = race.getCon();
 			rawStats[ABILITY_CONSTITUTION][ABILITY_INHERENT] = stat;
 		}
 		public void setConEnhance(/*Not sure what goes here*/)
 		{
 			stat = 0;
 			rawStats[ABILITY_CONSTITUTION][ABILITY_ENHANCE] = stat;
 		}
 		public void setConMisc(/*Not sure what goes here*/)
 		{
 			stat = 0;
 			rawStats[ABILITY_CONSTITUTION][ABILITY_MISC] = stat;
 		}
 		//GETTERS
 		
 		public int getConTotal()
 		{
 			stat =rawStats[ABILITY_CONSTITUTION][ABILITY_TOTAL];
 			return stat;
 		}
 		public int getConMod()
 		{
 			stat =rawStats[ABILITY_CONSTITUTION][ABILITY_MODIFIER];
 			return stat;
 		}
 		public int getConBase()
 		{
 			stat =rawStats[ABILITY_CONSTITUTION][ABILITY_BASE];
 			return stat;
 		}
 		public int getConInherent()
 		{
 			stat =rawStats[ABILITY_CONSTITUTION][ABILITY_INHERENT];
 			return stat;
 		}
 		public int getConEnhance()
 		{
 			stat =rawStats[ABILITY_CONSTITUTION][ABILITY_ENHANCE];
 			return stat;
 		}
 		public int getConMisc()
 		{
 			stat =rawStats[ABILITY_CONSTITUTION][ABILITY_MISC];
 			return stat;
 		}
 	
 	/*INT*/
 		//SETTERS
 		
 	
 		public void setIntTotal()//enchance set to 0
 		{
 			base =rawStats[ABILITY_INTELLIGENCE][ABILITY_BASE];
 			inher =rawStats[ABILITY_INTELLIGENCE][ABILITY_INHERENT];
 			enhance = 0;
 			misc = 0;
 			
 			rawStats[ABILITY_INTELLIGENCE][ABILITY_TOTAL]= base + inher + enhance + misc;
 			
 		}
 		public void setIntMod()
 		{
 			int total = getIntTotal();
 			rawStats[ABILITY_INTELLIGENCE][ABILITY_MODIFIER] =(total - 10 )/ 2; 
 			
 		}
 		public void setIntBase()
 		{
 			stat = abilityRoll();
 			rawStats[ABILITY_INTELLIGENCE][ABILITY_BASE] = stat;
 		}
 		public void setIntInher(Race race)
 		{
 			stat = race.getIntel();
 			rawStats[ABILITY_INTELLIGENCE][ABILITY_INHERENT] = stat;
 		}
 		public void setIntEnhance(/*Not sure what goes here*/)
 		{
 			stat = 0;
 			rawStats[ABILITY_INTELLIGENCE][ABILITY_ENHANCE] = stat;
 		}
 		public void setIntMisc(/*Not sure what goes here*/)
 		{
 			stat = 0;
 			rawStats[ABILITY_INTELLIGENCE][ABILITY_MISC] = stat;
 		}
 	
 	//GETTERS
 	public int getIntTotal()
 	{
 		stat =rawStats[ABILITY_INTELLIGENCE][ABILITY_TOTAL];
 		return stat;
 	}
 	public int getIntMod()
 	{
 		stat =rawStats[ABILITY_INTELLIGENCE][ABILITY_MODIFIER];
 		return stat;
 	}
 	public int getIntBase()
 	{
 		stat =rawStats[ABILITY_INTELLIGENCE][ABILITY_BASE];
 		return stat;
 	}
 	public int getIntInherent()
 	{
 		stat =rawStats[ABILITY_INTELLIGENCE][ABILITY_INHERENT];
 		return stat;
 	}
 	public int getIntEnhance()
 	{
 		stat =rawStats[ABILITY_INTELLIGENCE][ABILITY_ENHANCE];
 		return stat;
 	}
 	public int getIntMisc()
 	{
 		stat =rawStats[ABILITY_INTELLIGENCE][ABILITY_MISC];
 		return stat;
 	}
 	
 	
 	/*WIS*/
 	//SETTERS
 	public void setWisTotal()//enchance set to 0
 	{
 		base =rawStats[ABILITY_WISDOM][ABILITY_BASE];
 		inher =rawStats[ABILITY_WISDOM][ABILITY_INHERENT];
 		enhance = 0;
 		misc = 0;
 		
 		rawStats[ABILITY_WISDOM][ABILITY_TOTAL]= base + inher + enhance + misc;
 		
 	}
 		public void setWisMod()
 		{
 			int total = getWisTotal();
 			rawStats[ABILITY_WISDOM][ABILITY_MODIFIER] =(total - 10 )/ 2; 
 			
 		}
 		public void setWisBase()
 		{
 			stat = abilityRoll();
 			rawStats[ABILITY_WISDOM][ABILITY_BASE] = stat;
 		}
 		public void setWisInher(Race race)
 		{
 			stat = race.getWis();
 			rawStats[ABILITY_WISDOM][ABILITY_INHERENT] = stat;
 		}
 		public void setWisEnhance(/*Not sure what goes here*/)
 		{
 			stat = 0;
 			rawStats[ABILITY_WISDOM][ABILITY_ENHANCE] = stat;
 		}
 		public void setWisMisc(/*Not sure what goes here*/)
 		{
 			stat = 0;
 			rawStats[ABILITY_WISDOM][ABILITY_MISC] = stat;
 		}
 	
 	//GETTERS
 	public int getWisTotal()
 	{
 		stat =rawStats[ABILITY_WISDOM][ABILITY_TOTAL];
 		return stat;
 	}
 	public int getWisMod()
 	{
 		stat =rawStats[ABILITY_WISDOM][ABILITY_MODIFIER];
 		return stat;
 	}
 	public int getWisBase()
 	{
 		stat =rawStats[ABILITY_WISDOM][ABILITY_BASE];
 		return stat;
 	}
 	public int getWisInherent()
 	{
 		stat =rawStats[ABILITY_WISDOM][ABILITY_INHERENT];
 		return stat;
 	}
 	public int getWisEnhance()
 	{
 		stat =rawStats[ABILITY_WISDOM][ABILITY_ENHANCE];
 		return stat;
 	}
 	public int getWisMisc()
 	{
 		stat =rawStats[ABILITY_WISDOM][ABILITY_MISC];
 		return stat;
 	}
 	/*CHA*/
 	//SETTERS
 	
 	public void setChaTotal()//enchance set to 0
 	{
 		base =rawStats[ABILITY_CHARISMA][ABILITY_BASE];
 		inher =rawStats[ABILITY_CHARISMA][ABILITY_INHERENT];
 		enhance = 0;
 		misc = 0;
 		
 		rawStats[ABILITY_CHARISMA][ABILITY_TOTAL]= base + inher + enhance + misc;
 		
 	}
 	public void setChaMod()
 	{
 		int total = getChaTotal();
 		rawStats[ABILITY_CHARISMA][ABILITY_MODIFIER] =(total - 10 )/ 2; 
 				
 	}
 	public void setChaBase()
 	{
 		stat = abilityRoll();
 		rawStats[ABILITY_CHARISMA][ABILITY_BASE] = stat;
 	}
 	public void setChaInher(Race race)
 	{
 		stat = race.getcha();
 		rawStats[ABILITY_CHARISMA][ABILITY_INHERENT] = stat;
 	}
 	public void setChaEnhance(/*Not sure what goes here*/)
 	{
 		stat = 0;
 		rawStats[ABILITY_CHARISMA][ABILITY_ENHANCE] = stat;
 	}
 	public void setChaMisc(/*Not sure what goes here*/)
 	{
 		stat = 0;
 		rawStats[ABILITY_CHARISMA][ABILITY_MISC] = stat;
 	}
 	
 	//GETTERs
 	public int getChaTotal()
 	{
 		stat =rawStats[ABILITY_CHARISMA][ABILITY_TOTAL];
 		return stat;
 	}
 	public int getChaMod()
 	{
 		stat =rawStats[ABILITY_CHARISMA][ABILITY_MODIFIER];
 		return stat;
 	}
 	public int getChaBase()
 	{
 		stat =rawStats[ABILITY_CHARISMA][ABILITY_BASE];
 		return stat;
 	}
 	public int getChaInherent()
 	{
 		stat =rawStats[ABILITY_CHARISMA][ABILITY_INHERENT];
 		return stat;
 	}
 	public int getChaEnhance()
 	{
 		stat =rawStats[ABILITY_CHARISMA][ABILITY_ENHANCE];
 		return stat;
 	}
 	//END OF ABILITY
 	/********* HITPOINTS *********/
 	public int hitpoints;//work on this later
 	
 	//END OF HITPOINTS
 	
 	/********************************
 	 ***** Attacks And Defense ******
 	 ********************************/								
 	
 	public static final int DEFENSE_AC = 0;
 	public static final int DEFENSE_TOUCH = 1;
 	public static final int DEFENSE_FLATFOOT = 2;
 	public static final int DEFENSE_TOTAL = 0;
 	public static final int DEFENSE_ARMOR = 1;
 	public static final int DEFENSE_SHIELD = 2;
 	public static final int DEFENSE_DEX = 3;
 	public static final int DEFENSE_SIZE = 4;
 	public static final int DEFENSE_DODGE = 5;
 	public static final int DEFENSE_NATURAL = 6;
 	public static final int DEFENSE_DEFLECT = 7;
 	/*
 	 * 
 	 * X:
 	 * row index 0 = ac
 	 * row index 1 = touch
 	 * row index 2 = flatfoot
 	 * 
 	 * Y:
 	 * column index 0 = total
 	 * column index 1 = armor 
 	 * column index 2 = shield
 	 * column index 3 = dex
 	 * column index 4 = size
 	 * column index 5 = dodge
 	 * column index 6 = natural
 	 * column index 7 = deflect
 	 */
 	int dex;
 	
 	private int[][] armorClass = new int[3][8];
 	
 	int armorPenalty;
 	//AC
 	public void setACArmor()
 	{
 		int armor = EquippedArmor.get(0).getAcBonus();
 		armorClass[DEFENSE_AC][DEFENSE_ARMOR]= armor;
 	}
 	
 	public void setACArmor(int total)//for monsters
 	{
 		armorClass[DEFENSE_AC][DEFENSE_ARMOR]= total;
 	}
 	public void setACShield()
 	{
 		int shield = EquippedArmor.get(1).getAcBonus();
 		armorClass[DEFENSE_AC][DEFENSE_SHIELD]= shield;
 	}
 	public void setACShield(int total)//used by monsters
 	{
 		armorClass[DEFENSE_AC][DEFENSE_SHIELD]= total;
 	}
 	
 	
 	public void setACDex()
 	{
 		dex = rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER];
 		armorClass[DEFENSE_AC][DEFENSE_DEX]= dex;
 	}
 	public void setACSize()
 	{
 		 size = race.getSize();
 		armorClass[DEFENSE_AC][DEFENSE_SIZE]= size;
 	}
 	public void setACDodge(/*need armor and feats Class*/)
 	{
 		
 		armorClass[DEFENSE_AC][DEFENSE_DODGE]= 0;
 	}
 	public void setACNaturalArmor(Race race/*need Skills and feats Class also*/)
 	{
 		
 		armorClass[DEFENSE_AC][DEFENSE_NATURAL]= 0;
 	}
 	public void setACDeflect(/*may need Feats to determine*/)
 	{
 		
 		armorClass[DEFENSE_AC][DEFENSE_DEFLECT]= 0;
 	}
 	public void setACTotal(int total)//used for monsters
 	{
 		armorClass[DEFENSE_AC][DEFENSE_TOTAL]= total;
 	}
 	public void setACTotal()
 	{
 		int armor = getACArmor();
 		int shield = getACShield();
 		int dex = getDexMod();
 		int size = getACSize();
 		int dodge = getACDodge();
 		int deflect = getACDeflect();
 		armorClass[DEFENSE_AC][DEFENSE_TOTAL]= 10 + armor + shield + dex + size + dodge + deflect;
 	}
 	public int getACTotal()
 	{
 		
 		stat = armorClass[DEFENSE_AC][DEFENSE_TOTAL];
 		return stat;
 	}
 	public int getACArmor()
 	{
 		
 		stat = armorClass[DEFENSE_AC][DEFENSE_ARMOR];
 		return stat;
 	}
 	public int getACShield()
 	{
 		
 		stat = armorClass[DEFENSE_AC][DEFENSE_SHIELD];
 		return stat;
 	}
 	public int getACDex()
 	{
 		
 		stat = armorClass[DEFENSE_AC][DEFENSE_DEX];
 		return stat;
 	}
 	public int getACSize()
 	{
 		
 		stat = armorClass[DEFENSE_AC][DEFENSE_SIZE];
 		return stat;
 	}
 	public int getACDodge()
 	{
 		
 		stat = armorClass[DEFENSE_AC][DEFENSE_DODGE];
 		return stat;
 	}
 	public int getACNatural()
 	{
 		
 		stat = armorClass[DEFENSE_AC][DEFENSE_NATURAL];
 		return stat;
 	}
 	public int getACDeflect()
 	{
 		
 		stat = armorClass[DEFENSE_AC][DEFENSE_DEFLECT];
 		return stat;
 	}
 	
 	
 	//TOUCH
 	
 	public void setTouchDex()
 	{
 		dex = rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER];
 		armorClass[DEFENSE_TOUCH][DEFENSE_DEX]= dex;
 	}
 	public void setTouchSize()
 	{
 		 size = race.getSize();
 		armorClass[DEFENSE_TOUCH][DEFENSE_SIZE]= size;
 	}
 	public void setTouchDodge(/*need armor and feats Class*/)
 	{
 		
 		armorClass[DEFENSE_TOUCH][DEFENSE_DODGE]= 0;
 	}
 	
 	public void setTouchDeflect(/*may need Feats to determine*/)
 	{
 		
 		armorClass[DEFENSE_TOUCH][DEFENSE_DEFLECT]= 0;
 	}
 	public void setTouchTotal()
 	{
 		int dodge = getTouchDodge();
 		int dex = getDexMod();
 		int size = getTouchSize();
 		int deflect = getTouchDeflect();
 		armorClass[DEFENSE_TOUCH][DEFENSE_TOTAL]= 10 + dodge + dex + size + deflect;
 	}
 	public void setTouchTotal(int total)//used for monsters
 	{
 		
 		armorClass[DEFENSE_TOUCH][DEFENSE_TOTAL]= total;
 	}
 	public int getTouchTotal()
 	{
 		
 		stat = armorClass[DEFENSE_TOUCH][DEFENSE_TOTAL];
 		return stat;
 	}
 	public int getTouchDex()
 	{
 		
 		stat = armorClass[DEFENSE_TOUCH][DEFENSE_DEX];
 		return stat;
 	}
 	public int getTouchSize()
 	{
 		
 		stat = armorClass[DEFENSE_TOUCH][DEFENSE_SIZE];
 		return stat;
 	}
 	public int getTouchDodge()
 	{
 		
 		stat = armorClass[DEFENSE_TOUCH][DEFENSE_DODGE];
 		return stat;
 	}
 	public int getTouchDeflect()
 	{
 		
 		stat = armorClass[DEFENSE_TOUCH][DEFENSE_DEFLECT];
 		return stat;
 	}
 	
 	//FlatFoot
 	public void setFlatfootArmor()
 	{
 		int armor = EquippedArmor.get(0).getAcBonus();
 		armorClass[DEFENSE_FLATFOOT][DEFENSE_ARMOR]= armor;
 	}
 	
 	public void setFlatfootArmor(int total)//for monsters
 	{
 		armorClass[DEFENSE_FLATFOOT][DEFENSE_ARMOR]= total;
 	}
 	public void setFlatfootShield()
 	{
 		int shield = EquippedArmor.get(1).getAcBonus();
 		armorClass[DEFENSE_FLATFOOT][DEFENSE_SHIELD]= shield;
 	}
 	public void setFlatfootShield(int total)//used by monsters
 	{
 		armorClass[DEFENSE_FLATFOOT][DEFENSE_SHIELD]= total;
 	}
 	public void setFlatfootSize()
 	{
 		 size = race.getSize();
 		armorClass[DEFENSE_FLATFOOT][DEFENSE_SIZE]= size;
 	}
 	public void setFlatfootSize(int size)//for monsters
 	{
 		 
 		armorClass[DEFENSE_FLATFOOT][DEFENSE_SIZE]= size;
 	}
 	
 	public void setFlatfootNaturalArmor(Race race/*need Skills and feats Class also*/)
 	{
 		
 		armorClass[DEFENSE_FLATFOOT][DEFENSE_NATURAL]= 0;
 	}
 	public void setFlatfootDeflect(/*may need Feats to determine*/)
 	{
 		
 		armorClass[DEFENSE_FLATFOOT][DEFENSE_DEFLECT]= 0;
 	}
 	public void setFlatfootTotal()
 	{
 		int armor = getFlatfootArmor();
 		int shield = getFlatfootShield();
 	
 		int size = getFlatfootSize();
 		int natural = getFlatfootNatural();
 		int deflect = getFlatfootDeflect();
 		armorClass[DEFENSE_FLATFOOT][DEFENSE_TOTAL]= 10 + armor + shield  + size  + deflect + natural;
 	}
 	public void setFlatfootTotal(int total)//used for monsters
 	{
 		
 		armorClass[DEFENSE_FLATFOOT][DEFENSE_TOTAL]= total;
 	}
 	
 	public int getFlatfootTotal()
 	{
 		
 		stat = armorClass[DEFENSE_FLATFOOT][DEFENSE_TOTAL];
 		return stat;
 	}
 	public int getFlatfootArmor()
 	{
 		
 		stat = armorClass[DEFENSE_FLATFOOT][DEFENSE_ARMOR];
 		return stat;
 	}
 	public int getFlatfootShield()
 	{
 		
 		stat = armorClass[DEFENSE_FLATFOOT][DEFENSE_SHIELD];
 		return stat;
 	}
 	public int getFlatfootSize()
 	{
 		
 		stat = armorClass[DEFENSE_FLATFOOT][DEFENSE_SIZE];
 		return stat;
 	}
 	public int getFlatfootDeflect()
 	{
 		
 		stat = armorClass[DEFENSE_FLATFOOT][DEFENSE_DEFLECT];
 		return stat;
 	}
 	public int getFlatfootNatural()
 	{
 		
 		stat = armorClass[DEFENSE_FLATFOOT][DEFENSE_NATURAL];
 		return stat;
 	}
 	
 	/************************************ Saving Throws Setters/Getters *************************************/
 	/*Setters*/
 		
 		/*
 		 * NOTE ON BASE SAVE (CLASS BASE)
 		 * Base Save is the number given to you by your class(es). 
 			Ex: 1st Level Fighter FORT +2 / REF +0 / WILL +0 
 				1st Level Rogue FORT +0 / REF +2 / WILL +0 
 				1st Level Wizard FORT +0 / REF +0 / WILL +2 
 				1st Level Monk FORT +2 / REF +2 / WILL +2
 	Multiclass Ex: - 1st Fighter/1st Monk FORT +4 / REF +2 / WILL +2
 		 * 
 		 * so each character's class has it's own fort ref and will
 		 */
 		
 		
 		/*SavingThrows
 		 * X:
 		 * row index 1 = fort
 		 * row index 2 = reflex
 		 * row index 3 = willpower
 		 * 
 		 * Y:
 		 * column index 0 = total
 		 * column index 1 ...class base.. 
 		 * column index 2 = ability
 		 * column index 3 = enhance
 		 */
 	
 	
 	public static final int SAVE_FORT = 0;
 	public static final int SAVE_REF = 1;
 	public static final int SAVE_WILL = 2;
 	
 	public static final int SAVE_TOTAL = 0;
 	public static final int SAVE_CLASSBASE = 1;
 	public static final int SAVE_ABILITY = 2;
 	public static final int SAVE_ENHANCE = 3;
 	 
 	private int[][] savingthrows = new int[3][4];
 	
 	//FORT
 	
 	//SETTERS
 	public void setFortBase(CharacterClass c)
 	{
 		stat =c.getFort();
 		savingthrows[SAVE_FORT][ SAVE_CLASSBASE] = stat;
 	}
 	public void setFortAbility()
 	{
 		stat =rawStats[ABILITY_CONSTITUTION][ABILITY_MODIFIER];
 		savingthrows[SAVE_FORT][ SAVE_ABILITY] = stat;
 	}
 	public void setFortEnhance(/*need something to give enhance values*/)
 	{
 		stat = 0;
 		savingthrows[SAVE_FORT][ SAVE_ENHANCE] = stat;
 	}
 	public void setFortTotal()
 	{
 		int base = getFortBase();
 		int ability = getFortAbility();
 		int enhance = getFortEnhance();
 		stat = base + ability + enhance;
 		savingthrows[SAVE_FORT][ SAVE_TOTAL] = stat;
 	}
 	public void setFortTotal(int total)//for monsters
 	{
 		
 		savingthrows[SAVE_FORT][ SAVE_TOTAL] = total;
 	}
 	
 	
 	//Getters
 	public int getFortBase()
 	{
 		
 		stat =savingthrows[SAVE_FORT][ SAVE_CLASSBASE]; 
 		return stat;
 	}
 	public int getFortAbility()
 	{
 		
 		stat =savingthrows[SAVE_FORT][ SAVE_ABILITY]; 
 		return stat;
 	}
 	public int getFortEnhance()
 	{
 		
 		stat =savingthrows[SAVE_FORT][ SAVE_ENHANCE]; 
 		return stat;
 	}
 	public int getFortTotal()
 	{
 		
 		stat =savingthrows[SAVE_FORT][ SAVE_TOTAL]; 
 		return stat;
 	}
 	
 	
 	/* REFLEX */
 	//SETTERS
 		public void setRefBase(CharacterClass cr)
 		{
 			stat =cr.getRef();
 			savingthrows[SAVE_REF][ SAVE_CLASSBASE] = stat;
 		}
 		public void setRefAbility()
 		{
 			stat =rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER];
 			savingthrows[SAVE_REF][ SAVE_ABILITY] = stat;
 		}
 		public void setRefEnhance(/*need something to give enhance values*/)
 		{
 			stat = 0;
 			savingthrows[SAVE_REF][ SAVE_ENHANCE] = stat;
 		}
 		public void setRefTotal()
 		{
 			int base = getRefBase();
 			int ability = getRefAbility();
 			int enhance = getRefEnhance();
 			stat = base + ability + enhance;
 			savingthrows[SAVE_REF][ SAVE_TOTAL] = stat;
 		}
 		public void setRefTotal(int total)//used by monsters
 		{
 			
 			savingthrows[SAVE_REF][ SAVE_TOTAL] = total;
 		}
 		
 		//Getters
 		public int getRefBase()
 		{
 			
 			stat =savingthrows[SAVE_REF][ SAVE_CLASSBASE]; 
 			return stat;
 		}
 		public int getRefAbility()
 		{
 			
 			stat =savingthrows[SAVE_REF][ SAVE_ABILITY]; 
 			return stat;
 		}
 		public int getRefEnhance()
 		{
 			
 			stat =savingthrows[SAVE_REF][ SAVE_ENHANCE]; 
 			return stat;
 		}
 		public int getRefTotal()
 		{
 			
 			stat =savingthrows[SAVE_REF][ SAVE_TOTAL]; 
 			return stat;
 		}
 	
 		//WILL
 		//SETTERS
 		public void setWillBase(CharacterClass cr)
 		{
 			stat =cr.getWill();
 			savingthrows[SAVE_WILL][ SAVE_CLASSBASE] = stat;
 		}
 		public void setWillAbility()
 		{
 			stat =rawStats[ABILITY_WISDOM][ABILITY_MODIFIER];
 			savingthrows[SAVE_WILL][ SAVE_ABILITY] = stat;
 		}
 		public void setWillEnhance(/*need something to give enhance values*/)
 		{
 			stat = 0;
 			savingthrows[SAVE_WILL][ SAVE_ENHANCE] = stat;
 		}
 		public void setWillTotal()
 		{
 			int base = getWillBase();
 			int ability = getWillAbility();
 			int enhance = getWillEnhance();
 			stat = base + ability + enhance;
 			savingthrows[SAVE_WILL][ SAVE_TOTAL] = stat;
 		}
 		public void setWillTotal(int total)//for monsters
 		{
 			
 			savingthrows[SAVE_WILL][ SAVE_TOTAL] = total;
 		}
 		
 		//Getters
 		public int getWillBase()
 		{
 			
 			stat =savingthrows[SAVE_WILL][ SAVE_CLASSBASE]; 
 			return stat;
 		}
 		public int getWillAbility()
 		{
 			
 			stat =savingthrows[SAVE_WILL][ SAVE_ABILITY]; 
 			return stat;
 		}
 		public int getWillEnhance()
 		{
 			
 			stat =savingthrows[SAVE_WILL][ SAVE_ENHANCE]; 
 			return stat;
 		}
 		public int getWillTotal()
 		{
 			
 			stat =savingthrows[SAVE_WILL][ SAVE_TOTAL]; 
 			return stat;
 		}
 	
 	
 	
 	/*
 	 * ATTACKS
 	 */
 	
 	/*
 	 * X:
 	 * row index 0 = Melee
 	 * row index 1 = Ranged
 	 * row index 2 = CMB
 	 * Row index 3 = CMD **has a -10
 	 * 
 	 * Y:
 	 * column index 0 = total
 	 * column index 1,2,3,4,5 .. base attack.. 
 	 * column index 6 = ability
 	 * column index 7 = size
 	 */
 		public static final int ATTACK_MELEE = 0;
 		public static final int ATTACK_RANGED = 1;
 		public static final int ATTACK_CMB= 2;
 		public static final int ATTACK_CMD = 3;
 		
 		
 		public static final int ATTACK_BABFIRST = 0;
 		public static final int ATTACK_BABSECOND = 1;
 		public static final int ATTACK_BABTHIRD = 2;
 		public static final int ATTACK_BABFOURTH = 3;
 		public static final int ATTACK_BABFIFTH = 4;
 		public static final int ATTACK_ABILITY = 5;
 		public static final int ATTACK_SIZE = 6; 
 		public static final int ATTACK_TOTAL = 7;
 		
 	public int[][] attacks = new int[4][8];
 	//Melee
 	public void setMeleeBab(CharacterClass clas)
 	{
 		int[] babValues = clas.getBab();
 		int value = babValues[0];
 		attacks[ATTACK_MELEE][ATTACK_BABFIRST] = value;
 		value = babValues[1];
 		attacks[ATTACK_MELEE][ATTACK_BABSECOND] = value;
 		value = babValues[2];
 		attacks[ATTACK_MELEE][ATTACK_BABTHIRD] = value;
 		value = babValues[3];
 		attacks[ATTACK_MELEE][ATTACK_BABFOURTH] = value;
 		value = babValues[4];
 		attacks[ATTACK_MELEE][ATTACK_BABFIFTH] = value;
 		
 	}
 	public void setMeleeAbility()
 	{
 		int strMod = rawStats[ABILITY_STRENGTH][ABILITY_MODIFIER];
 		attacks[ATTACK_MELEE][ATTACK_ABILITY] = strMod;
 		
 	}
 	public void setMeleeSize()
 	{
 		
 		attacks[ATTACK_MELEE][ATTACK_SIZE] = race.getSize();
 		
 	}
 	public int getMeleeAbility()
 	{
 		
 		return attacks[ATTACK_MELEE][ATTACK_ABILITY];
 		
 	}
 	public int getMeleeSize()
 	{
 		
 		return attacks[ATTACK_MELEE][ATTACK_SIZE];
 		
 	}
 	public int[] getMeleeBab()
 	{
 		int[] bab = new int[5];
 		bab[0]= attacks[ATTACK_MELEE][ATTACK_BABFIRST];
 	
 		bab[1]= attacks[ATTACK_MELEE][ATTACK_BABSECOND];
 		
 		bab[2]= attacks[ATTACK_MELEE][ATTACK_BABTHIRD];
 	
 		bab[3]= attacks[ATTACK_MELEE][ATTACK_BABFOURTH];
 		
 		bab[4]= attacks[ATTACK_MELEE][ATTACK_BABFIFTH];
 		return bab;
 		
 	}
 	//Ranged
 	public void setRangedBab(CharacterClass clas)
 	{
 		int[] babValues = clas.getBab();
 		int value = babValues[0];
 		attacks[ATTACK_RANGED][ATTACK_BABFIRST] = value;
 		value = babValues[1];
 		attacks[ATTACK_RANGED][ATTACK_BABSECOND] = value;
 		value = babValues[2];
 		attacks[ATTACK_RANGED][ATTACK_BABTHIRD] = value;
 		value = babValues[3];
 		attacks[ATTACK_RANGED][ATTACK_BABFOURTH] = value;
 		value = babValues[4];
 		attacks[ATTACK_RANGED][ATTACK_BABFIFTH] = value;
 		
 	}
 	public void setRangedAbility()
 	{
 		int dexMod = rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER];
 		attacks[ATTACK_RANGED][ATTACK_ABILITY] = dexMod;
 		
 	}
 	public void setRangedSize()
 	{
 		
 		attacks[ATTACK_RANGED][ATTACK_SIZE] = race.getSize();
 		
 	}
 	public int getRangedAbility()
 	{
 		
 		return attacks[ATTACK_RANGED][ATTACK_ABILITY];
 		
 	}
 	public int getRangedSize()
 	{
 		
 		return attacks[ATTACK_RANGED][ATTACK_SIZE];
 		
 	}
 	public int[] getRangedBab()
 	{
 		int[] bab = new int[5];
 		bab[0]= attacks[ATTACK_RANGED][ATTACK_BABFIRST];
 	
 		bab[1]= attacks[ATTACK_RANGED][ATTACK_BABSECOND];
 		
 		bab[2]= attacks[ATTACK_RANGED][ATTACK_BABTHIRD];
 	
 		bab[3]= attacks[ATTACK_RANGED][ATTACK_BABFOURTH];
 		
 		bab[4]= attacks[ATTACK_RANGED][ATTACK_BABFIFTH];
 		return bab;
 		
 	}
 	//CMB
 	public void setCmbBab(CharacterClass clas)
 	{
 		int[] babValues = clas.getBab();
 		int value = babValues[0];
 		attacks[ATTACK_CMB][ATTACK_BABFIRST] = value;
 		value = babValues[1];
 		attacks[ATTACK_CMB][ATTACK_BABSECOND] = value;
 		value = babValues[2];
 		attacks[ATTACK_CMB][ATTACK_BABTHIRD] = value;
 		value = babValues[3];
 		attacks[ATTACK_CMB][ATTACK_BABFOURTH] = value;
 		value = babValues[4];
 		attacks[ATTACK_CMB][ATTACK_BABFIFTH] = value;
 		
 	}
 	public void setCmbAbility()
 	{
 		int strMod = rawStats[ABILITY_STRENGTH][ABILITY_MODIFIER];
 		attacks[ATTACK_CMB][ATTACK_ABILITY] = strMod;
 		
 	}
 	public void setCmbSize()
 	{
 		
 		attacks[ATTACK_CMB][ATTACK_SIZE] = race.getSize();
 		
 	}
 	public int getCmbAbility()
 	{
 		
 		return attacks[ATTACK_CMB][ATTACK_ABILITY];
 		
 	}
 	public int getCmbSize()
 	{
 		
 		return attacks[ATTACK_CMB][ATTACK_SIZE];
 		
 	}
 	public int[] getCmbBab()
 	{
 		int[] bab = new int[5];
 		bab[0]= attacks[ATTACK_CMB][ATTACK_BABFIRST];
 	
 		bab[1]= attacks[ATTACK_CMB][ATTACK_BABSECOND];
 		
 		bab[2]= attacks[ATTACK_CMB][ATTACK_BABTHIRD];
 	
 		bab[3]= attacks[ATTACK_CMB][ATTACK_BABFOURTH];
 		
 		bab[4]= attacks[ATTACK_CMB][ATTACK_BABFIFTH];
 		return bab;
 		
 	}
 	
 	//cmd
 	public void setCmdBab(CharacterClass clas)
 	{
 		int[] babValues = clas.getBab();
 		int value = babValues[0];
 		attacks[ATTACK_CMD][ATTACK_BABFIRST] = value;
 		value = babValues[1];
 		attacks[ATTACK_CMD][ATTACK_BABSECOND] = value;
 		value = babValues[2];
 		attacks[ATTACK_CMD][ATTACK_BABTHIRD] = value;
 		value = babValues[3];
 		attacks[ATTACK_CMD][ATTACK_BABFOURTH] = value;
 		value = babValues[4];
 		attacks[ATTACK_CMD][ATTACK_BABFIFTH] = value;
 		
 	}
 	public void setCmdAbility()
 	{
 		int strMod = rawStats[ABILITY_STRENGTH][ABILITY_MODIFIER];
 		attacks[ATTACK_CMD][ATTACK_ABILITY] = strMod;
 		
 	}
 	public void setCmdSize()
 	{
 		
 		attacks[ATTACK_CMD][ATTACK_SIZE] = race.getSize();
 		
 	}
 	
 	
 	
 	
 	public int getCmdAbility()
 	{
 		
 		return attacks[ATTACK_CMD][ATTACK_ABILITY];
 		
 	}
 	public int getCmdSize()
 	{
 		
 		return attacks[ATTACK_CMD][ATTACK_SIZE];
 		
 	}
 	public int[] getCmdBab()
 	{
 		int[] bab = new int[5];
 		bab[0]= attacks[ATTACK_CMD][ATTACK_BABFIRST];
 	
 		bab[1]= attacks[ATTACK_CMD][ATTACK_BABSECOND];
 		
 		bab[2]= attacks[ATTACK_CMD][ATTACK_BABTHIRD];
 	
 		bab[3]= attacks[ATTACK_CMD][ATTACK_BABFOURTH];
 		
 		bab[4]= attacks[ATTACK_CMD][ATTACK_BABFIFTH];
 		return bab;
 		
 	}
 	//not attack value, passing in if it's the first attack or send or so on
 	
 	public void setCmdTotal(int total)//mostly used for monsters, use getCmdTotal for characters
 	{
 		attacks[ATTACK_CMD][ATTACK_TOTAL]= total;
 	}
 	public int getCmdTotal(int attack)
 	{
 		int r = 10 + attacks[ATTACK_CMD][attack-1]+rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER]+rawStats[ABILITY_STRENGTH][ABILITY_MODIFIER] + getCmdSize();
 		return r;
 	}
 	
 	public void setCmbTotal(int total)//mostly used for monsters, use getCmbTotal for characters
 	{
 		attacks[ATTACK_CMB][ATTACK_TOTAL]= total;
 	}
 	public int getCmbTotal(int attack)
 	{
 		int r = attacks[ATTACK_CMB][attack-1]+rawStats[ABILITY_STRENGTH][ABILITY_MODIFIER] + getCmbSize();
 		return r;
 	}
 	
 	public int getRangedTotal(int attack)
 	{
 		int r = attacks[ATTACK_RANGED][attack-1]+rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER] + getCmbSize();
 		return r;
 	}
 	public void setMeleeTotal(int total)//mostly used for monsters, use getCmdTotal for characters
 	{
 		attacks[ATTACK_MELEE][ATTACK_TOTAL]= total;
 	}
 	public int getMeleeTotal(int attack)
 	{
 		int r = attacks[ATTACK_MELEE][attack-1]+rawStats[ABILITY_STRENGTH][ABILITY_MODIFIER] + getCmbSize();
 		return r;
 	}
 	//NEED SKILLS
 	/*
 	 * Just returns the String name of the skill.
 	 * Will need to implement a 2d array for values
 	 * of the skills
 	 */
 	
 	public int[][] skills = new int[32][4];
 	//public int[][] classSkills = new int[32][4];
 	SkillsList list;
 	
 	public int[][] getSkills()
 	{
 		//bad overrides current skills
 		
 		return skills;
 	}
 	
 	/*
 	 * need to setClassSkills first then ability then ranks
 	 * should only have to do this once
 	*/
 	
 	public void setClassSkills(CharacterClass clas)
 	{
 	    list = new SkillsList(clas);
 		skills = list.getList();
 		
 	}
 	/*
 	 * This method sets the ability values of Skills based on the mod value of the abilities
 	 * Certain skills require a specific ability mod. 
 	 */
 	public void setAbilitySkills()//or updateAbilitySkills
 	{
 		int str = rawStats[ABILITY_STRENGTH][ABILITY_MODIFIER];
 		int dex = rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER];
 		int inte = rawStats[ABILITY_INTELLIGENCE][ABILITY_MODIFIER];
 		int wis = rawStats[ABILITY_WISDOM][ABILITY_MODIFIER];
 		int cha = rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER];
 		
 		
 		skills[list.ACROBATICS][list.ABILITY] = dex;
 		skills[list.APPRAISE][list.ABILITY] = inte;
 		skills[list.BLUFF][list.ABILITY] = cha;
 		skills[list.CLIMB][list.ABILITY] = str;
 		skills[list.CRAFT1][list.ABILITY] = inte;
 		skills[list.CRAFT2][list.ABILITY] = inte;
 		skills[list.DIPLOMACY][list.ABILITY] = cha;
 		skills[list.DISABLEDEVICE][list.ABILITY] = dex;
 		skills[list.DISGUISE][list.ABILITY] = cha;
 		skills[list.ESCAPEARTIST][list.ABILITY] = dex;
 		skills[list.FLY][list.ABILITY] = dex;
 		skills[list.HANDLEANIMAL][list.ABILITY] = cha;
 		skills[list.HEAL][list.ABILITY] = wis;
 		skills[list.INTIMIDATE][list.ABILITY] = cha;
 		skills[list.KNOWLEDGE1][list.ABILITY] = inte;
 		skills[list.KNOWLEDGE2][list.ABILITY] = inte;
 		skills[list.KNOWLEDGE3][list.ABILITY] = inte;
 		skills[list.KNOWLEDGE4][list.ABILITY] = inte;
 		skills[list.KNOWLEDGE5][list.ABILITY] = inte;
 		skills[list.KNOWLEDGE6][list.ABILITY] = inte;
 		skills[list.LINGUISTICS][list.ABILITY] = inte;
 		skills[list.PERCEPTION][list.ABILITY] = wis;
 		skills[list.PERFORM][list.ABILITY] = cha;
 		skills[list.PROFESSION][list.ABILITY] = inte;
 		skills[list.RIDE][list.ABILITY] = dex;
 		skills[list.SENSEMOTIVE][list.ABILITY] = wis;
 		skills[list.SLEIGHTOFHAND][list.ABILITY] = dex;
 		skills[list.SPELLCRAFT][list.ABILITY] = inte;
 		skills[list.STEALTH][list.ABILITY] = dex;
 		skills[list.SURVIVAL][list.ABILITY] = wis;
 		skills[list.SWIM][list.ABILITY] = str;
 		skills[list.USEMAGICDEVICE][list.ABILITY] = inte;
 		
 		
 		
 		
 		
 	}
 	/*
 	 * This method will be asked using skill points, when calling this method 
 	 * you will need to set the amount of skills available and decrease it every time
 	 * after calling this method. It does not take into account how many skills can
 	 * be assigned it will just assign the value +1 each time it is called. Will need to pass
 	 * in the row value you wish to add a rank to. row value is the skill you want to increase
 	 * it's numerical value can be found in SkillsList.java
 	 */
 	public void setRanksSkills(int skillValue)
 	{
 		
 	}
 	/*
 	 * does the same thing setAbilitySkills() does but makes more sense to use this after
 	 * the first time
 	 */
 	public void updateAbilitySkills()
 	{
 		int str = rawStats[ABILITY_STRENGTH][ABILITY_MODIFIER];
 		int dex = rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER];
 		int inte = rawStats[ABILITY_INTELLIGENCE][ABILITY_MODIFIER];
 		int wis = rawStats[ABILITY_WISDOM][ABILITY_MODIFIER];
 		int cha = rawStats[ABILITY_DEXTERITY][ABILITY_MODIFIER];
 		
 		
 		skills[list.ACROBATICS][list.ABILITY] = dex;
 		skills[list.APPRAISE][list.ABILITY] = inte;
 		skills[list.BLUFF][list.ABILITY] = cha;
 		skills[list.CLIMB][list.ABILITY] = str;
 		skills[list.CRAFT1][list.ABILITY] = inte;
 		skills[list.CRAFT2][list.ABILITY] = inte;
 		skills[list.DIPLOMACY][list.ABILITY] = cha;
 		skills[list.DISABLEDEVICE][list.ABILITY] = dex;
 		skills[list.DISGUISE][list.ABILITY] = cha;
 		skills[list.ESCAPEARTIST][list.ABILITY] = dex;
 		skills[list.FLY][list.ABILITY] = dex;
 		skills[list.HANDLEANIMAL][list.ABILITY] = cha;
 		skills[list.HEAL][list.ABILITY] = wis;
 		skills[list.INTIMIDATE][list.ABILITY] = cha;
 		skills[list.KNOWLEDGE1][list.ABILITY] = inte;
 		skills[list.KNOWLEDGE2][list.ABILITY] = inte;
 		skills[list.KNOWLEDGE3][list.ABILITY] = inte;
 		skills[list.KNOWLEDGE4][list.ABILITY] = inte;
 		skills[list.KNOWLEDGE5][list.ABILITY] = inte;
 		skills[list.KNOWLEDGE6][list.ABILITY] = inte;
 		skills[list.LINGUISTICS][list.ABILITY] = inte;
 		skills[list.PERCEPTION][list.ABILITY] = wis;
 		skills[list.PERFORM][list.ABILITY] = cha;
 		skills[list.PROFESSION][list.ABILITY] = inte;
 		skills[list.RIDE][list.ABILITY] = dex;
 		skills[list.SENSEMOTIVE][list.ABILITY] = wis;
 		skills[list.SLEIGHTOFHAND][list.ABILITY] = dex;
 		skills[list.SPELLCRAFT][list.ABILITY] = inte;
 		skills[list.STEALTH][list.ABILITY] = dex;
 		skills[list.SURVIVAL][list.ABILITY] = wis;
 		skills[list.SWIM][list.ABILITY] = str;
 		skills[list.USEMAGICDEVICE][list.ABILITY] = inte;
 		
 		
 		
 		
 		
 	}
 	
 	/*
 	 * Need to set Ranks values before totals will have any values other than 0
 	 */
 	public void setTotalSkills()
 	{
 		int rank;
 		for (int i = 0; i < list.TOTAL_SKILL_ROWS; i++) {
 			
 				rank = skills[i][list.RANKS];
 				if(rank < 1)
 				{
 					skills[i][list.TOTAL] = 0;
 				}
 				else // add up the ranks,ability mod, and the trained.
 				{
 					skills[i][list.TOTAL] = skills[i][list.RANKS] + skills[i][list.ABILITY] + skills[i][list.TRAINED];
 				}
 			
 		}
 	}
 	
 	/*
 	 * input which skill you want, the numerical value of each skill is found on
 	 * SkillsList.java
 	 */
 	public int getTotalSkill(int skill)
 	{
 		return skills[skill][list.TOTAL];
 	}
 	//NEED EXPERIANCE
 	private int currentXP;
 	private int xpTotalLvl;
 	
 	//NEED INIT
 	
 	public int init;
 	public int getInit()
 	{
 		int dex = rawStats[ABILITY_DEXTERITY][ABILITY_TOTAL];
 		return dex;
 		
 	//NEED MORE TO DETERMINE INITITIVE!!
 		
 		
 	}
 	
 	public int getSpeed()
 	{
 		return race.baseSpeed;
 	}
 	//NEED DR-DAMAGE REDUCTION
 	int damageReduction =0;
 	//NEED SR-SPELL RESIST
 	int spellResist = 0;
 	//NEED AP-ACTION POINTS
 	int actionPoints = 0;
 	
 	/************************************ Class Getters *************************************/
 	public int getAP()
 	{
 		return actionPoints;
 	}
 	
 	public int getSR()
 	{
 		return spellResist;
 	}
 	
 	public int getDR()
 	{
 		return damageReduction;
 	}
 	/*
 	 * Setting a race with a value in a raceContainer
 	 */
 	
 	
 	public void setRace(Race race)
 	{
 		this.race=race;
 		
 	}
 	public Race getRace()
 	{
 		return(this.race);
 	}
 	
 
 	
 	
 	
 	//ROLL FOR ABILITY METHOD
 	/*
 	 * This method can be used for deciding the initial values of your abilities
 	 * 
 	 */
 	public int abilityRoll()
 	{
 		int returner= dice.roll(3, 4, 6);
 		return returner;
 	}
 	
 	/**
 	 * EQUIPMENT
 	 */
 	
 	
 	/*
 	 * Equipment List of everything the character has.
 	 */
 	ArrayList<Equipment> equipmentList = new ArrayList<Equipment>();
 	
 	public void setEquipment(Equipment e)
 	{
 		equipmentList.add(e);
 	}
 	
 	
 	
 	/**
 	 * ARMOR  
 	  **/
 	//HERE IS THE CONTAINER OF ALL ARMOR IN OUR GAME
 	Armory armory = new Armory();
 	
 	//This is what should be shown on the GUI, the ArrayList of Armor
 	//The character can have on him currently
 	ArrayList<Armor> EquippedArmor = new ArrayList<Armor>(2);
 	
 	//put armor in the arrayList and into the equipmentList
 	
 	//WHEN EQUIPING ARMOR, INDEX 0 IS ARMOR INDEX 1 IS SHIELD
 	public void EquipArmor(Armor arm)
 	{
 		EquippedArmor.add(0,arm);
 		//equipmentList.add(arm);
 	}
 	
 	public void EquipShield(Armor arm)
 	{
 		EquippedArmor.add(1,arm);
 		//equipmentList.add(arm);
 	}
 	
 	//get the armor from the arraylist
 	public Armor getEquippedArmor()
 	{
 		return EquippedArmor.get(0);
 	}
 	
 	public Armor getEquippedShield()
 	{
 		if(EquippedArmor.size() < 2)
 		{
 			return null;
 		}
 		else
 		{
 			return EquippedArmor.get(1);
 		}
 	}
 	
 	/*
 	 * Look at Armory for list of all armor  and weapons and their index
 	 * this will help get the armor you want to put in the arrayList
 	 */
 	public Armor getArmorFromArmory(int index)
 	{
 		return armory.armor.get(index);
 	}
 	
 	
 	//
 	public ArrayList<Weapon> EquippedWeapon = new ArrayList<Weapon>(2);
 	//public ArrayList<Weapon> SheathedWeapon = new ArrayList<Weapon>(3);
 	
 	
 	//Select the which weapon goes into which hand, 0 main hand and 1 offhand
 	public void equipWeapon(Weapon w, int hand)
 	{
 		
 		if(hand == 1)
 		{
 			if(getEquippedShield() == null)
 			{
 				if(EquippedWeapon.get(hand) == armory.weapons.get(0))// if it is unarmed
 				{
 					
 					
 					
 					equipmentList.add(EquippedWeapon.remove(hand));
 					EquippedWeapon.add(hand,w);
 				}
 					
 			}
 		}
 		else
 		{
 			
 			equipmentList.add(EquippedWeapon.remove(hand));
 			EquippedWeapon.add(hand,w);
 			
 			
 		}
 		
 		if(EquippedWeapon.size() > 2)
 		{
 			for(int i = 2; i < EquippedWeapon.size(); i++)
 			{
 				EquippedWeapon.remove(i);
 			}
 		}
 		for (int j = 0; j < equipmentList.size(); j++) {
 			
 			System.out.println("w " + w);
 			System.out.println("list" + equipmentList.get(j).getName());
 			if(w.getName().equals(equipmentList.get(j).getName()))
 			{
 				equipmentList.remove(j);
 				break;
 			}
 		}
 		
 	}
 	
 	
 	public void unequipWeapons()
 	{
 		
 		for (int i = 0; i < EquippedWeapon.size(); i++) {
 		
 			
 			equipmentList.add(EquippedWeapon.remove(0));
 			
 			
 		}
 		
 		//Equip both hands with unarmed
 		for (int i = 0; i < 2; i++) {
 		
 			
 			EquippedWeapon.add(armory.weapons.get(0));
 			
 			
 		}
 		
 		
 		
 		
 		//need to add to inventory if not there
 	}
 	
 	
 	//get the armor from the arraylist
 	public Weapon getEquippedWeapon(int index)
 	{
 		return EquippedWeapon.get(index);
 	}
 	
 	/*
 	 * Look at Armory for list of all armor  and weapons and their index
 	 * this will help get the armor you want to put in the arrayList
 	 */
 	public Weapon getWeaponFromArmory(int index)
 	{
 		return armory.weapons.get(index);
 	}
 	
 	public void addToEquipment(Equipment e)
 	{
 		equipmentList.add(e);
 	}
 	
 	
 	
 	
 	
 	
 	/** FILL CHARACTER SHEET METHODS **/
 	/*
 	 * FILL IN THIS ORDER OR IT WILL BREAK
 	 * fillBasic(...)
 	 * fillAbilities()
 	 * 
 	 */
 	public void fillBasic(String name,String player,int race,String languages, int size,
 						  int gender,int height,int weight,int age,String alignments,
 						  String deity,String background,String occupation)
 	{
 		RaceContainer container = new RaceContainer();
 		this.name =name;
 		this.player=player;
 		this.race=container.getRace(race);
 		this.raceName = this.race.getRaceName();
 		this.languages = languages;
 		this.size = size;
 		this.gender = gender;
 		this.height = height;
 		this.weight = weight;
 		this.age = age;
 		this.alignments = alignments;
 		this.deity = deity;
 		this.background = background;
 		this.occupation = occupation;
 	
 	}
 	
 	public void fillAbilities()
 	{
 		//Str
 		setStrBase();
 		setStrInher(race);
 		setStrEnhance();
 		setStrMisc();
 		setStrTotal();
 		setStrMod();
 		//Dex
 		setDexBase();
 		setDexInher(race);
 		setDexEnhance();
 		setDexMisc();
 		setDexTotal();
 		setDexMod();
 		//Con
 		setConBase();
 		setConInher(race);
 		setConEnhance();
 		setConMisc();
 		setConTotal();
 		setConMod();
 		//Int
 		setIntBase();
 		setIntInher(race);
 		setIntEnhance();
 		setIntMisc();
 		setIntTotal();
 		setIntMod();
 		//Wis
 		setWisBase();
 		setWisInher(race);
 		setWisEnhance();
 		setWisMisc();
 		setWisTotal();
 		setWisMod();
 		//Cha
 		setChaBase();
 		setChaInher(race);
 		setChaEnhance();
 		setChaMisc();
 		setChaTotal();
 		setChaMod();
 	}
 	/*input is based on value of the class container 
 	 *  input is 0 then barb
 	 *  
 	
 	*/
 	public CharacterClass chooseClass(int clas)
 	{
 		ClassContainer container = new ClassContainer(this);
 		CharacterClass r = container.getClass(clas);
 		return r;
 	}
 	ArrayList<ClassRecorder> recorder = new ArrayList<ClassRecorder>();
 	public void fillRecorder(CharacterClass clas)
 	{
 		ClassRecorder rec = new ClassRecorder(clas);
 		recorder.add(rec);
 	}
 	
 	public void fillAttacksAndDefense(CharacterClass clas)
 	{
 		//setACArmor();
 		//setACShield();
 		setACDex();
 		setACSize();
 		setACDodge();
 		setACNaturalArmor(race);
 		setACDeflect();
 		setACTotal();
 		
 		setTouchDex();
 		setTouchSize();
 		setTouchDodge();
 		setTouchDeflect();
 		setTouchTotal();
 		
 		
 		//setFlatfootArmor();
 		//setFlatfootShield();
 		setFlatfootSize();
 		setFlatfootNaturalArmor(race);
 		setFlatfootDeflect();
 		
 		setFortBase(clas);
 		setFortAbility();
 		setFortEnhance();
 		setFortTotal();
 		
 		setRefBase(clas);
 		setRefAbility();
 		setRefEnhance();
 		setRefTotal();
 		
 		setWillBase(clas);
 		setWillAbility();
 		setWillEnhance();
 		setWillTotal();
 		
 		setMeleeBab(clas);
 		setMeleeAbility();
 		setMeleeSize();
 		
 		setRangedBab(clas);
 		setRangedAbility();
 		setRangedSize();
 		
 		setCmbBab(clas);
 		setCmbAbility();
 		setCmbSize();
 		
 		setCmdBab(clas);
 		setCmdAbility();
 		setCmdSize();
 		
 		
 	}
 	
 	//Call the EquipArmor(Armor armor) Method
 	//should look like this: 
 	
 	//EquipArmor(armor);
 	//EquipShield(shield);
 	
 	//where armor and shield are chosen by the user and added to the arraylist, remember index 0 is armor and 
 	//index 1 is shield, this is already implemented in code so you don't have to worry about setting it.
 	
 	public void applyArmorAndShield()
 	{
		
		if(getEquippedShield() != null)
		{
			setACShield();
			setFlatfootShield();
		}
		if(getEquippedArmor() != null)
		{
			setFlatfootArmor();
			setACArmor();
		}
 	}
 	
 	public String getName(){
 		return name;
 	}
 	
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	
 	public String toString()
 	{
 		String r = "Name: " +name + " \n" +
 				   "Player Name: " + player + " \n" +
 				   "Race: " + raceName + " \n" +
 				   "Gender: " + gender + " \n" +
 				   "Age: " + age + " \n\n" +
 				   
 				   "Str: " + rawStats[ABILITY_STRENGTH][ABILITY_TOTAL] + "\n" +
 				   "Dex: " + rawStats[ABILITY_DEXTERITY][ABILITY_TOTAL] + "\n" +
 				   "Con: " + rawStats[ABILITY_CONSTITUTION][ABILITY_TOTAL] + "\n" +
 				   "Int: " + rawStats[ABILITY_INTELLIGENCE][ABILITY_TOTAL] + "\n" +
 				   "Wis: " + rawStats[ABILITY_WISDOM][ABILITY_TOTAL] + "\n" +
 				   "Cha: " + rawStats[ABILITY_CHARISMA][ABILITY_TOTAL] + "\n\n" +
 					"AC: " + this.getACTotal() + " \n\n";
 		if (recorder.size() > 0)
 		{
 			ClassRecorder clas = recorder.get(0);
 			r += "Class: " + clas.className + "\n" +
 					   "Hp: " + clas.hp;
 		} /* end if */
 		
 		return r;
 	}
 	
 	public int getHP()
 	{
 		int hp = 0;
 		for (int i = 0; i < recorder.size(); i++) hp += recorder.get(i).hp;
 		return hp;
 	} /* end getHP method */
 	
 	public void setNetID(int netID)
 	{
 		this.netID = netID;
 	}
 	
 	public int getNetID()
 	{
 		return netID;
 	}
 	
 	public DDImage getImage()
 	{
 		return image;
 	}
 	
 	public void setImage(DDImage image)
 	{
 		this.image = image;
 	} /* end setImage method */
 	
 } /* end CharacterSheet method */
