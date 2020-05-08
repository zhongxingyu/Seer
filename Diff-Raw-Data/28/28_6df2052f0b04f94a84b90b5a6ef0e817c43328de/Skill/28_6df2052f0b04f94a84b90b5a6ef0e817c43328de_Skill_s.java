 package uw.cse403.minion;
 
 import java.util.*; 
 
 import android.content.ContentValues;
 
 /**
  * A representation of a single skill in Pathfinder, such as 
  * stealth, perception or Knowledge(Dance).
  * 
  * @author Loki White (lokiw)
  */
 public class Skill {
 	private static final int CLASS_BONUS = 3;
 
 	public static final int ACROBATICS_ID 				= 1;
 	public static final int APPRAISE_ID 				= 2;
 	public static final int BLUFF_ID 					= 3;
 	public static final int CLIMB_ID 					= 4;
 	public static final int CRAFT_ID 					= 5;
 	public static final int DIPLOMACY_ID 				= 6;
 	public static final int DISABLE_DEVICE_ID 			= 7;
 	public static final int DISGUISE_ID 				= 8;
 	public static final int ESCAPE_ARTIST_ID 			= 9;
 	public static final int FLY_ID 						= 10;
 	public static final int HANDLE_ANIMAL_ID 			= 11;
 	public static final int HEAL_ID 					= 12;
 	public static final int INTIMIDATE_ID 				= 13;
 	public static final int KNOWLEDGE_ARCANA_ID 		= 14;
 	public static final int KNOWLEDGE_DUNGEONEERING_ID 	= 15;
 	public static final int KNOWLEDGE_ENGINEERING_ID 	= 16;
 	public static final int KNOWLEDGE_GEOGRAPHY_ID 		= 17;
 	public static final int KNOWLEDGE_HISTORY_ID 		= 18;
 	public static final int KNOWLEDGE_LOCAL_ID 			= 19;
 	public static final int KNOWLEDGE_NATURE_ID 		= 20;
 	public static final int KNOWLEDGE_NOBILITY_ID 		= 21;
 	public static final int KNOWLEDGE_PLANES_ID 		= 22;
 	public static final int KNOWLEDGE_RELIGION_ID 		= 23;
 	public static final int LINGUISTICS_ID 				= 24;
 	public static final int PERCEPTION_ID 				= 25;
 	public static final int PERFORM_ID 					= 26;
 	public static final int PROFESSION_ID 				= 27;
 	public static final int RIDE_ID 					= 28;
 	public static final int SENSE_MOTIVE_ID 			= 29;
 	public static final int SLEIGHT_OF_HAND_ID 			= 30;
 	public static final int SPELLCRAFT_ID 				= 31;
 	public static final int STEALTH_ID 					= 32;
 	public static final int SURVIVAL_ID 				= 33;
 	public static final int SWIM_ID 					= 34;
 	public static final int USE_MAGIC_DEVICE_ID 		= 35;
 
 	private int charID;
 
 	/** Various components the make up the skill information about a character **/
 	private int skillID; // get skill ID from ref db
 	private String name;
 	private String title; // write-in fields for Craft, Perform, Profession
 	private int ranks;
 	private boolean classSkill;
 	private Map<String,Integer> modifiers;
 	private AbilityName assocAbility;
 	private int abMod;
 
 	/**
 	 * Initializes a new skill object.
 	 * @param skillID 		ID used to reference what skill this object corresponds to
 	 * @param name 			String name of new skill
 	 * @param attribute 	an AbilityName of which attribute is associated with this skill
 	 * @param rank 			int ranks of new skill, will not set rank lower than 0
 	 * @param classSkill 	a boolean that if <code>false</code> means the skill is not a class
 	 * 						skill and if <code>true</code> is a class skill
 	 */
 	public Skill(int skillID, String name, AbilityName attribute, int rank, boolean classSkill){
 		this(skillID, name, null, attribute, rank, classSkill);
 	}
 
 	/**
 	 * Initializes a new skill with almost all necessary information. Sets values for
 	 * given name, associated ability, rank and whether or not it is a class skill.
 	 * @param name			String name of new skill
 	 * @param title			Secondary name for a skill such as craft, profession and perform
 	 * @param attribute		an AbilityName of which attribute is associated with this skill
 	 * @param rank			int ranks of new skill, will not set rank lower than 0
 	 * @param classSkill	a boolean that if <code>false</code> means the skill is not a class
 	 * 						skill and if <code>true</code> is a class skill
 	 */
 	public Skill(int skillID, String name, String title, AbilityName attribute, int rank, boolean classSkill){
 		this.skillID = skillID;
 		this.name = name;
 		this.title = title;
 		if (rank < 0) {
 			this.ranks = 0;
 		} else {
 			this.ranks = rank;
 		}
 		this.classSkill = classSkill;
 		modifiers = new HashMap<String,Integer>();
 		assocAbility = attribute;
 		abMod = -1;
 	}
 
 	/**
 	 * Returns the name of the skill
 	 * @return	String name of skill
 	 */
 	public int getID(){
 		return skillID;
 	}
 
 	/**
 	 * Returns the name of the skill
 	 * @return	String name of skill
 	 */
 	public String getName(){
 		return name;
 	}
 
 	/**
 	 * Returns the title of the skill
 	 * @return	String title of skill, may be null
 	 */
 	public String getTitle(){
 		return title;
 	}
 
 	/**
 	 * Add given value (or subtract if negative) from the current
 	 * rank of the skill. Will not set rank lower than 0.
 	 * @param modifier
 	 */
 	public void addToRank(int modifier){
 		if (ranks + modifier < 0) {
 			ranks = 0;
 		} else {
 			ranks += modifier;
 		}
 	}
 
 	/**
 	 * Get raw skill ranks
 	 * @return	an integer representing ranks in skill
 	 */
 	public int getRank() {
 		return ranks;
 	}
 
 	/**
 	 * Returns the modifier under the given name. Can return both negative
 	 * and positive modifiers. These modifiers represent values that will be
 	 * either added or subtracted from the skill.
 	 * @param name the name of the modifier whose value is retrieved
 	 * @return 	the value associated with the given String, may be either negative
 	 * 			or positive. Returns 0 if no modifier of the given name
 	 * 			was found
 	 */
 	public int getModifier(String name){
 		if (modifiers.containsKey(name)) {
 			return modifiers.get(name);
 		}
 		return 0;
 	}
 
 	/**
 	 * Removes the modifier under the given name as well as the record of that name.
 	 * @param name	the name of the modifier to remove
 	 * @modifies this
 	 */
 	public void removeModifier(String name){
 		if (modifiers.containsKey(name)) {
 			modifiers.remove(name);
 		}
 	}
 
 	/**
 	 * Adds a new modifier with the given name and value
 	 * @param name	the name of the modifier
 	 * @param value	the value of the modifier
 	 * @modifies this
 	 */
 	public void addModifier(String name, int value){
 		modifiers.put(name, value);
 	}
 
 	/**
 	 * Returns the total bonus of the skill accounting for ranks, class bonus,
 	 * and miscellaneous modifiers.
 	 * @param mod	the Ability for the given skill. Throws IllegalArgumentException 
 	 * 				exception if given ability not associated with that skill.
 	 * @return an int total bonus for given skill
 	 */
 	public int getBonus(Ability mod){
 		if (mod.getName() != assocAbility) {
 			throw new IllegalArgumentException();
 		}
 
 		//Add rank and class modifier (if appropriate)
 		int bonus = ranks;
 		if (classSkill && ranks > 0) {
 			bonus += CLASS_BONUS;
 		}
 
 		//Add associated ability modifier to bonus
 		bonus += mod.getMod();
 
 		//Add miscellaneous modifiers to bonus
 		Collection<Integer> mods = modifiers.values();
 		Iterator<Integer> it = mods.iterator();
 		while (it.hasNext()) {
 			bonus += it.next();
 		}
 
 		return bonus;
 	}
 
 	/**
 	 * Gets the associated ability modifier for this skill.
 	 * @return The associated ability modifier
 	 */
 	public int getAbMod() {
 		return 0;
 	}
 
 	/**
 	 * Gets the skill total for this skill.
 	 * @return the skill total
 	 */
 	public int getTotal() {
 		int mod = modifiers.get(modifiers.keySet().iterator().next());
 		return ranks + mod;
 	}
 
 	/** 
 	 * Writes Skill to database. SHOULD ONLY BE CALLED BY CHARACTER
 	 * @param id id of character
 	 * @param db database to write into
 	 */
 	public void writeToDB(long id) {
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelperSkills.COLUMN_CHAR_ID, id);
 		values.put(SQLiteHelperSkills.COLUMN_REF_S_ID, skillID);
 		if (skillID == 5 || skillID == 26 || skillID == 27) {
 			values.put(SQLiteHelperSkills.COLUMN_TITLE, title);
 		}
 		values.put(SQLiteHelperSkills.COLUMN_RANKS, ranks);
 		if (modifiers.size() > 0) {
 			int mod = modifiers.get(modifiers.keySet().iterator().next());
 			values.put(SQLiteHelperSkills.COLUMN_MISC_MOD, mod);
 		}
 		SQLiteHelperSkills.db.insert(SQLiteHelperSkills.TABLE_NAME, null, values);
 	}
 }
