 package uw.cse403.minion;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Debug;
 
 /**
  * A wrapper class with a few associated calculations to do with combat
  * checks and scores such as armor class, hit points and CMB/CMD
  * 
  * @author lokiw
  */
 public class Combat {
 	/** Class constants for string representations **/
 	public static final String ARMOR_BONUS_STRING = "armorBonus";
 	public static final String ARMOR_SHIELD_STRING = "armorShield";
 	public static final String ARMOR_DEX_STRING = "armorDex";
 	public static final String ARMOR_SIZE_STRING = "armorSize";
 	public static final String ARMOR_NATURAL_STRING = "armorNatural";
 	public static final String ARMOR_DEFLECTION_STRING = "armorDeflection";
 	public static final String ARMOR_MISC_STRING = "armorMisc";
 
 	/** The unique id for a character **/
 	public long charID;
 	
 	/** Stores whether or not this combat information has been stored previously or not **/
 	public boolean isNew; 
 
 	/** Various components the make up the combat information about a character **/
 	private int baseHP;
 	private int damageReduction;
 
 	private int lethalDamage;
 	private int bludgeningDamage;
 	private Map<String,Integer> armorModifiers;
 	private Map<String,Integer> hpModifiers;
 
 	//Speed stored in feet
 	public int speedBase;
 	public int speedArmor;
 	private int speed;
 	private int initModifier;
 	private int bAb;
 
 	public int dexMod;
 	public int sizeMod;
 
 	/**
 	 * Creates a new Combat Object with all values initialized to 0
 	 * or empty lists accordingly
 	 */
 	public Combat() {
 		this(9000);
 	}
 
 	/**
 	 * Creates a Combat Object for the character and loads in all the
 	 * information stored in the local database for that character.
 	 * @param id The id for the character whose information is to be loaded
 	 */
 	public Combat(long id) {
 		baseHP = 0;
 		damageReduction = 0;
 		lethalDamage = 0;
 		bludgeningDamage = 0;
 		armorModifiers = new HashMap<String, Integer>();
 		speed = 0;
 		initModifier = 0;
 		bAb = 0;
 
 		if (TraceControl.TRACE)
 			Debug.startMethodTracing("Combat_loadFromDB");
 		
 		loadFromDB();
 		
 		if (TraceControl.TRACE)
 			Debug.stopMethodTracing();
 	}
 
 	/**
 	 * Populate fields with values from DB
 	 */
 	private void loadFromDB() {
 		isNew = true;
 		// attempt to load from DB
 		Cursor cursor = SQLiteHelperCombat.db.query(SQLiteHelperCombat.TABLE_NAME, SQLiteHelperCombat.ALL_COLUMNS, 
 				SQLiteHelperCombat.COLUMN_CHAR_ID + " = " + charID, null, null, null, null);
 		if (cursor.moveToFirst()) {
 			isNew = false;
 			// Columns: COLUMN_CHAR_ID, COLUMN_HP_TOTAL, COLUMN_HP_DR, COLUMN_SPEED_BASE, COLUMN_SPEED_ARMOR,
 			// COLUMN_INIT_MISC_MOD, COLUMN_ARMOR, COLUMN_ARMOR_SHIELD, COLUMN_ARMOR_NATURAL,
 			// COLUMN_ARMOR_DEFLEC, COLUMN_ARMOR_MISC, COLUMN_BASE_ATTACK_BONUS
 			baseHP 			= cursor.getInt(1);
 			damageReduction = cursor.getInt(2);
 			speedBase 		= cursor.getInt(3);
 			speedArmor 		= cursor.getInt(4);
 			initModifier 	= cursor.getInt(5);
 			armorModifiers.put(ARMOR_BONUS_STRING, cursor.getInt(6));
 			armorModifiers.put(ARMOR_SHIELD_STRING, cursor.getInt(7));
 			armorModifiers.put(ARMOR_NATURAL_STRING, cursor.getInt(8));
 			armorModifiers.put(ARMOR_DEFLECTION_STRING, cursor.getInt(9));
 			armorModifiers.put(ARMOR_MISC_STRING, cursor.getInt(10));
 			bAb 			= cursor.getInt(11);
 		}
 		cursor.close();
 	}
 
 	/**
 	 * Get base hit point value
 	 * @return	an integer representing base HP
 	 */
 	public int getBaseHP() {
 		return baseHP;
 	}
 
 	/**
 	 * Set base hit points to given value
 	 * @param baseHP an integer to set base HP to
 	 */
 	public void setBaseHP(int baseHP) {
 		this.baseHP = baseHP;
 	}
 
 	/**
 	 * Get the numeric value of Damage Reduction. Does not
 	 * include the effect type so in DR x/- this returns the
 	 * x but gives no indication of the -
 	 * @return	an integer representing the value of the Damage Reduction
 	 */
 	public int getDamageReduction() {
 		return damageReduction;
 	}
 
 	/**
 	 * Set the numeric value of Damage Reduction.
 	 * @param damageReduction the integer value of the Damage Reduction
 	 */
 	public void setDamageReduction(int damageReduction) {
 		this.damageReduction = damageReduction;
 	}
 
 	/**
 	 * The current lethal damage currently taken by the character, 
 	 * regardless of max or current hit points.
 	 * @return	A positive amount of damage taken by the character
 	 */
 	public int getLethalDamage() {
 		return lethalDamage;
 	}
 
 	/**
 	 * Set the current amount of lethal damage taken by character, 
 	 * overriding all previous damage with new value.
 	 * @param lethalDamage	Set damage taken by character
 	 */
 	public void setLethalDamage(int lethalDamage) {
 		this.lethalDamage = lethalDamage;
 	}
 
 	/**
 	 * The current bludgeoning  damage currently taken by the character, 
 	 * regardless of max or current hit points.
 	 * @return	A positive amount of damage taken by the character
 	 */
 	public int getBludgeoningDamage() {
 		return bludgeningDamage;
 	}
 
 	/**
 	 * Set the current amount of bludgeoning damage taken by character, 
 	 * overriding all previous damage with new value.
 	 * @param lethalDamage	Set damage taken by character
 	 */
 	public void setBludgeoningDamage(int bludgeningDamage) {
 		this.bludgeningDamage = bludgeningDamage;
 	}
 
 	/**
 	 * Returns the modifier under the given name. Can return both negative
 	 * and positive modifiers. These modifiers represent values that will be
 	 * either added or subtracted from a character's armor class.
 	 * @param armorName the name of the Armor Modifier whose value is retrieved
 	 * @return 	the value associated with the given String, may be either negative
 	 * 			or positive. Returns 0 if no Armor Modifier of the given name
 	 * 			was found
 	 */
 	public int getArmorModifier(String armorName){
 		if (armorModifiers.containsKey(armorName)) {
 			return armorModifiers.get(armorName);
 		}
 
 		return 0;
 	}
 
 	/**
 	 * Removes the modifier under the given name as well as the record of that name.
 	 * @param armorName	the name of the Armor Modifier to remove
 	 * @modifies this
 	 */
 	public void removeArmorModifier(String armorName){
 		if (armorModifiers.containsKey(armorName)) {
 			armorModifiers.remove(armorName);
 		}
 	}
 
 	/**
 	 * Adds a new Armor Modifier with the given name and value
 	 * @param armorName	the name of the Armor Modifier
 	 * @param armorValue	the value of the modifier
 	 * @modifies this
 	 */
 	public void addArmorModifier(String armorName, int armorValue){
 		armorModifiers.put(armorName, armorValue);
 	}
 
 	/**
 	 * Get base speed in US feet.
 	 * @return	an integer base speed in feet
 	 */
 	public int getSpeed() {
 		return speed;
 	}
 
 	/**
 	 * Set the base speed of a character stored in US feet.
 	 * @param speed	an integer base speed in feet
 	 */
 	public void setSpeed(int speedBase, int speedArmor) {
 		this.speedBase = speedBase;
 		this.speedArmor = speedArmor;
 		this.speed = speedBase - speedArmor;
 	}
 
 	/**
 	 * A single number representing all initiative modifiers,
 	 * not including dex modifier.
 	 * @return an integer of all modifiers to initiative added together
 	 */
 	public int getInitModifier() {
 		return initModifier;
 	}
 
 	/**
 	 * Override old initiative modifiers with new modifier representing
 	 * all modifiers to initiative except dexterity
 	 * @param initModifiers	integer modifier to initiative
 	 */
 	public void setInitModifiers(int initModifiers) {
 		this.initModifier = initModifiers;
 	}
 
 	/**
 	 * Return base attack bonus given by character class, user set
 	 * @return	an integer base attack bonus
 	 */
 	public int getbAb() {
 		return bAb;
 	}
 
 	/**
 	 * Set base attack bonus, overriding old base attack bonus
 	 * @param bAb	integer new base attack bonus
 	 */
 	public void setbAb(int bAb) {
 		this.bAb = bAb;
 	}
 
 	/**
 	 * Return total initiative
 	 * @return int total initiative
 	 */
 	public int getInitTotal() {
 		return dexMod + initModifier;
 	}
 
 	/**
 	 * Return total armor class
 	 * @return int total armor class
 	 */
 	public int getArmorTotal() {
 		int score = 10 + dexMod;
 		Collection<Integer> temps = armorModifiers.values();
 		Iterator<Integer> it = temps.iterator();
 		while (it.hasNext()) {
 			score += it.next();
 		}
 		return score;
 	}
 
 
 	/** 
 	 * Writes Combat to database. SHOULD ONLY BE CALLED BY CHARACTER
 	 * @param id id of character
 	 * @param db database to write into
 	 */
 	public void writeToDB(long charID) {
 		int skillID = 0; // get skill ID from ref db
 
 		// remove old data
 		SQLiteHelperCombat.db.delete(SQLiteHelperCombat.TABLE_NAME, SQLiteHelperCombat.COLUMN_CHAR_ID + " = " + charID, null);
 		// prepare new insert
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelperCombat.COLUMN_CHAR_ID, charID);
 		values.put(SQLiteHelperCombat.COLUMN_HP_TOTAL, baseHP);
 		values.put(SQLiteHelperCombat.COLUMN_HP_DR, damageReduction);
 		values.put(SQLiteHelperCombat.COLUMN_SPEED_BASE, speedBase);
 		values.put(SQLiteHelperCombat.COLUMN_SPEED_ARMOR, speedArmor);
 		values.put(SQLiteHelperCombat.COLUMN_INIT_MISC_MOD, initModifier);
 		values.put(SQLiteHelperCombat.COLUMN_ARMOR, armorModifiers.get(ARMOR_BONUS_STRING));
 		values.put(SQLiteHelperCombat.COLUMN_ARMOR_SHIELD, armorModifiers.get(ARMOR_SHIELD_STRING));
 		values.put(SQLiteHelperCombat.COLUMN_ARMOR_NATURAL, armorModifiers.get(ARMOR_NATURAL_STRING));
 		values.put(SQLiteHelperCombat.COLUMN_ARMOR_DEFLEC, armorModifiers.get(ARMOR_DEFLECTION_STRING));
 		values.put(SQLiteHelperCombat.COLUMN_ARMOR_MISC, armorModifiers.get(ARMOR_MISC_STRING));
 		values.put(SQLiteHelperCombat.COLUMN_BASE_ATTACK_BONUS, bAb);
 
 		SQLiteHelperCombat.db.insert(SQLiteHelperCombat.TABLE_NAME, null, values);
 	}
 }
