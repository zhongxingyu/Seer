 package com.example.myfirstapp;
 
 import java.util.*;
 
 import android.database.sqlite.SQLiteDatabase;
 
 /**
  * A representation of a Pathfinder character with all of the information
  * stored on a standard character sheet.
  * 
  * @author Loki White (lokiw)
  * @author Thomas Eberlein (uwte)
  * @author Kevin Dong (kevinxd3)
  *
  */
 public class Character {
 	private static long counter = 0;
 	public long id;
 	private CharacterDescription desc;
 	
 	//Array of ability scores. 
 	// 0	 1	 2 	 3	 4	 5
 	//STR	DEX	CON	INT	WIS	CHA
 	private Ability abilityScores[];
 	private Map<String,Skill> skills;
 	private Combat combat;
 	private int level;
 	private SavingThrow will;
 	private SavingThrow fort;
 	private SavingThrow ref;
 	
 	/**
 	 * Initialize a new character with base or no values
 	 */
 	public Character(){
 		abilityScores = new Ability[6];
 		skills = new HashMap<String,Skill>();
		level = 1; 
 		will = new SavingThrow(AbilityName.WISDOME);
 		fort = new SavingThrow(AbilityName.CONSTITUTION);
 		ref = new SavingThrow(AbilityName.DEXTERITY);
 		combat = new Combat();
 		id = ++counter;
 	}
 	
 	/**
 	 * Sets the basic information of the character with the given information
 	 * 
 	 * @param basicInfo	initialize character with basicInfo
 	 * @modifies this
 	 */
 	public void setDescriptions(CharacterDescription basicInfo){
 		desc = basicInfo;
 	}
 	
 	/**
 	 * set the character level to the given level
 	 * 
 	 * @param level	int representation of a character level
 	 * @modifies this
 	 */
 	public void setLevel(int level){
 		this.level = level;
 	}
 	
 	/**
 	 * Get unique character id
 	 * 
 	 * @return	an unique long id of the character
 	 */
 	public long getId() {
 		return id;
 	}
 	
 	/**
 	 * Set unique character id
 	 * 
 	 * @param id	long id unique from other characters
 	 * @modifies this
 	 */
 	public void setId(long id){
 		this.id = id;
 	}
 	
 	/**
 	 * Get string name of character
 	 * 
 	 * @return	String name of character
 	 */
 	public String getName(){
 		return desc.name;
 	}
 	
 	/**
 	 * Set this character's name
 	 * 
 	 * @param name String of new character name
 	 * @modifies this
 	 */
 	public void setName(String name){
 		desc.name = name;
 	}
 	
 	/**
 	 * Get character's current combat maneuver defense
 	 * 
 	 * @return 	int representing a combat maneuver defense
 	 */
 	public int getCMD(){
 		//TODO: Fill in this method
 		return 10;
 	}
 	
 	/**
 	 * Get character's current combat maneuver bonus
 	 * 
 	 * @return 	int representing a combat maneuver bonus
 	 */
 	public int getCMB(){
 		//TODO: Fill in this method
 		return 0;
 	}
 	
 	/**
 	 * Get character's total will save bonus, including ability score
 	 * and miscellaneous modifiers.
 	 * 
 	 * @return	int representing total will save bonus
 	 */
 	public int getWillSave(){
 		return will.getTotal(abilityScores[4]);
 	}
 	
 	/**
 	 * Get character's total fortitude save bonus, including ability score
 	 * and miscellaneous modifiers.
 	 * 
 	 * @return	int representing total fortitude save bonus
 	 */
 	public int getFortitudeSave(){
 		return fort.getTotal(abilityScores[2]);
 	}
 	
 	/**
 	 * Get character's total reflex save bonus, including ability score
 	 * and miscellaneous modifiers.
 	 * 
 	 * @return	int representing total reflex save bonus
 	 */
 	public int getReflexSave(){
 		return ref.getTotal(abilityScores[1]);
 	}
 	
 	/**
 	 * Fill in skills with the basic skills found on current pathfinder character sheets
 	 * for ease of the user not having to insert them all themselves. These skills are:
 	 * <p>
 	 * Acrobatics, Appraise, Bluff, Climb, Craft_x, Diplomacy, Disable Device, Disguise,
 	 * Escape Artist, Fly, Handle Animal, Heal, Intimidate, Knowledge_x, Linguistics, 
 	 * Perception, Perform_x, Profession_x, Ride, Sense Motive, Sleight of Hand, 
 	 * Spellcraft, Stealth, Survival, Swim, Use Magic Device
 	 *  
 	 *  @modifies this
 	 */
 	public void setSkillsToDefault(){
 		//TODO: Fill in this method
 		skills.put("Acrobatics", new Skill("Acrobatics",AbilityName.DEXTERITY));
 	}
 	
 	/**
 	 * get the total hit points of the character considering base, and
 	 * constitution modifier per level
 	 * 
 	 * @return int for total hit points
 	 */
 	public int getTotalHitPoints(){
 		
 		//TODO: include hpModifiers
 		return combat.getBaseHP() + abilityScores[2].getMod()*level;
 	}
 	
 	/**
 	 * get the current hit points of the character based on total hit points
 	 * and damage taken both lethal and non lethal.
 	 * 
 	 * @return	current hit points of character.
 	 */
 	public int getCurrentHitPoints(){
 		return getTotalHitPoints() - (combat.getLethalDamage() + combat.getBludgeningDamage());
 	}
 	
 	/**
 	 * Writes Character to database. SHOULD ONLY BE CALLED BY CHARACTERDATASOURCE
 	 * @param dbBasicInfo
 	 * @param dbAbilityScores
 	 * @param dbASTempMods
 	 * @param dbSkills
 	 * @param dbCombat
 	 * @param dbArmor
 	 * @param dbSavingThrows
 	 * @param dbWeapons
 	 */
 	public void writeToDB(SQLiteDatabase dbBasicInfo, SQLiteDatabase dbAbilityScores, 
 			SQLiteDatabase dbASTempMods, SQLiteDatabase dbSkills, 
 			SQLiteDatabase dbCombat, SQLiteDatabase dbArmor, 
 			SQLiteDatabase dbSavingThrows, SQLiteDatabase dbWeapons) {
 		// write basic info / character description
 		this.desc.writeToDB(id, dbBasicInfo);
 		// write ability scores
 		for (int i = 0; i < abilityScores.length; i++) {
 			Ability a = abilityScores[i];
 			a.writeToDB(id, dbAbilityScores, dbASTempMods);
 		}
 		// write skills
 		for (String s : skills.keySet()) {
 			Skill skill = skills.get(s);
 			skill.writeToDB(id, dbSkills);
 		}
 		// write combat
 		this.combat.writeToDB(id, dbCombat);
 		this.will.writeToDB(id, dbSavingThrows);
 		this.fort.writeToDB(id, dbSavingThrows);
 		this.ref.writeToDB(id, dbSavingThrows);
 	}
 
 	@Override
 	public String toString(){
 		return desc.name;
 	}
 }
