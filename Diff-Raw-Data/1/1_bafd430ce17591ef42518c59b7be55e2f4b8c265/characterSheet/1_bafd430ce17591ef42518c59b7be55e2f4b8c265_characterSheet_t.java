 package com.app.swcharsheet;
 
 import java.lang.Math;
 /**
  * @author Mike Rushford
  * @author Eric Martin
  * @version 20100809
  */
 public class characterSheet{
 	String  charName,
 			playerName,
 			charSpecies,
 			charDestiny;
 	char charGender;
 	static int  charClass,
 				charAge,
 				charLevel,
 				charWeight,
 				charBaseSpeed,
 				charBaseAtk,
 				charTotalForcePoints,
 				charTotalHealthPoints,
 				charXP;
 	static int[] abilities;
 	int[][] skills;
 	double charHeight;
 	int charCurrentForcePoints,
 		charCurrentHealthPoints,
 		charCurrentDestinyPoints,
 		charDmgThresh,
 		levelXP,
 		toNextLevelXP,
 		wearingArmor = 0;
 	double XPProgress;
 	int[] modifiers;
 	
 	/**
 	 * Default Constructor
 	 */
 	characterSheet(){
 		
 		initStats();
 		
 		modifiers = new int[6];
 		modifiers = getMods(abilities);
 		
 		charCurrentHealthPoints = charTotalHealthPoints;
 		charCurrentForcePoints = charTotalForcePoints;
 		
 		charDmgThresh = getDefense(0);
 		
 		levelXP = 0;
 		for(int j = charLevel; j > 0; j--)
 			levelXP += (j - 1) * 1000;
 		
 		toNextLevelXP = (charLevel - 1) * 1000;
 		XPProgress = (double) (charXP - levelXP) / (double) toNextLevelXP;	
 	}
 	
 	private void initStats(){
 		charName = "Zohan";
 		playerName = "Brian";
 		charClass = 0;
 		charSpecies = "Twi\'lek";
 		charLevel = 4;
 		charAge = 24;
 		charGender = 'M';
 		charHeight = 1.8;
 		charWeight = 65;
 		charDestiny = "None";
 		abilities = new int[] {16,15,13,13,15,20};
 		skills = new int[][] {{1,5,0,0},	//0 Acrobatics
 						    {0,0,0,0},	//1 Climb
 						    {5,0,0,0},	//2 Deception
 						    {2,0,0,0},	//3 Endurance
 						    {5,0,0,0},	//4 Gather Information
 						    {1,0,0,0},	//5 Initiative
 						    {0,0,0,0},	//6 Jump
 						    {3,0,0,0},	//7 Knowledge
 						    {3,0,0,0},	//8 Knowledge
 						    {3,0,0,0},	//9 Mechanics
 						    {4,5,0,0}, //10 Perception
 						    {5,0,0,0}, //11 Persuasion
 						    {1,0,0,0}, //12 Pilot
 						    {1,0,0,0}, //13 Ride
 						    {1,0,0,0}, //14 Stealth
 						    {4,0,0,0}, //15 Survival
 						    {0,0,0,0}, //16 Swim
 						    {4,0,0,0}, //17 Treat Injury
 						    {3,0,0,0}, //18 Use Computer
 						    {5,5,0,0}};//19 Use the Force
 		
 		charXP = 6291;
 		charTotalHealthPoints = 57;
 		charTotalForcePoints = 7;
 		charBaseAtk = 4;
 		charBaseSpeed = 6;
 	}
 	/**
 	 * @return int Character's age in years
 	 */
 	public int getAge() {
 		return charAge;
 	}
 	
 	public int getBaseAttack(){
 		return charBaseAtk;
 	}
 	
 	//TODO add destinies to javadoc code
 	/**
 	 * @return String Character's destiny [a,b,c,d,e]
 	 */
 	public String getDestiny() {
 		return charDestiny;
 	}
 
 	static private int[] getMods(int[] abils){
 		int[] mods = new int[6];
 		for (int i=0; i<6; i++)
 			mods[i]=(int) Math.floor(abils[i]/2)-5;
 		return mods;
 		}
 	
 	/**
 	 * @return String class of the current character
 	 */
 	public String getCharClass() {
 		String thisclass[] = {"Jedi",
 							  "Noble",
 							  "Scoundrel",
 							  "Scout",
 							  "Soldier"};
 		
 		return thisclass[charClass];
 	}
 
 	public int getCharLevel() {
 		return charLevel;
 	}
 	
 	public String getCharName() {
 		return charName;
 	}
 	
 	/**
 	 * @param which
 	 * @return
 	 */
 	public int getDefense(int which) {
 		int defense, abilmod, levOrArmor, classBonus, misc;
 		switch(which) {
 			case 0:
 				abilmod=modifiers[2];
 				//classBonus=
 				break;
 			case 1:
 				abilmod=modifiers[1];
 				break;
 			case 2:
 				abilmod=modifiers[4];
 				break;
 			default:
 				abilmod=0;
 				break;
 		}
 		
 		classBonus=0; misc=0;
 		if(wearingArmor==1) levOrArmor=5;
 		else levOrArmor=charLevel;
 		defense=10+levOrArmor+classBonus+abilmod+misc;
 		return defense;
 	}
 
 	/**
 	 * @return char [M|F]
 	 */
 	public char getGender() {
 		return charGender;
 	}
 
 	/**
 	 * @return double Height in meters
 	 */
 	public double getHeight() {
 		return charHeight;
 	}
 
 	/**
 	 * @return The Player's name associated with this character
 	 */
 	public String getPlayerName() {
 		return playerName;
 	}
 
 	public int getSkillBonus(int skill) {
 		return (int) Math.floor (charLevel / 2) + modifiers[skills[skill][0]] + 
 			skills[skill][1] + skills[skill][2] + skills[skill][3];
 	}
 	
 	/**
 	 * @return String character species
 	 */
 	public String getSpecies() {
 		return charSpecies;
 	}
 
 	/** 
 	 * @return Character's base speed in squares
 	 */
 	public int getSpeed() {
 		return charBaseSpeed;
 	}
 
 	/**
 	 * @return int weight in kilograms
 	 */
 	public int getWeight() {
 		return charWeight;
 	}
 
 	public void adjHP(int x){
 		charCurrentHealthPoints+=x;
 	}
 	public void adjXP(int x){
 		charXP+=x;
 	}
 	public void adjFP(int x){
 		charCurrentForcePoints+=x;
 	}
 }
