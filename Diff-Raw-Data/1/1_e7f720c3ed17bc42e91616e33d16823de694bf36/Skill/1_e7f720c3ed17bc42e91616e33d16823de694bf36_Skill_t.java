 package uw.cse403.minion;
 
 import java.util.*; 
 
 import android.content.ContentValues;
 import android.database.sqlite.SQLiteDatabase;
 
 /**
  * A representation of a single skill in Pathfinder, such as 
  * stealth, perception or Knowledge(Dance).
  * 
  * @author Loki White (lokiw)
  */
 public class Skill {
 	private static final int CLASS_BONUS = 3;
 
 	private int charID;
 
 	private int skillID; // get skill ID from ref db
 	private String name;
 	private String title; // write-in fields for Craft, Perform, Profession
 	private int ranks;
 	private boolean classSkill;
 	private Map<String,Integer> modifiers;
 	private AbilityName assocAbility;
 	private int abMod;
 
 	//	/** not needed
 	//	 * Initializes a new skill with the given name and it's associated ability.
 	//	 * Initializes skill with no ranks, as not a class skill and no miscellaneous modifiers.
 	//	 * 
 	//	 * @param name		String name of new skill
 	//	 * @param attribute	an AbilityName of which attribute is associated with this skill
 	//	 */
 	//	public Skill(String name, AbilityName attribute){
 	//		this.name = name;
 	//		ranks = 0;
 	//		classSkill = false;
 	//		modifiers = new HashMap<String,Integer>();
 	//		assocAbility = attribute;
 	//		abMod = -1;
 	//		
 	//	}
 
 	//	public Skill(int charID, int skillID) {
 	//		this.charID = charID;
 	//		this.skillID = skillID;
 	//		
 	//		//loadFromDB();
 	//	}
 
 	public Skill(int skillID, String name, AbilityName attribute, int rank, boolean classSkill){
 		this(skillID, name, null, attribute, rank, classSkill);
 	}
 
 	/**
 	 * Initializes a new skill with almost all necessary information. Sets values for
 	 * given name, associated ability, rank and whether or not it is a class skill.
 	 * 
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
 		// TODO: Consider not allowing negative ranks
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
 	 * 
 	 * @return	String name of skill
 	 */
 	public int getID(){
 		return skillID;
 	}
 
 	/**
 	 * Returns the name of the skill
 	 * 
 	 * @return	String name of skill
 	 */
 	public String getName(){
 		return name;
 	}
 
 	/**
 	 * Returns the title of the skill
 	 * 
 	 * @return	String title of skill, may be null
 	 */
 	public String getTitle(){
 		return title;
 	}
 
 	/**
 	 * Add given value (or subtract if negative) from the current
 	 * rank of the skill. Will not set rank lower than 0.
 	 * 
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
 	 * 
 	 * @return	an integer representing ranks in skill
 	 */
 	public int getRank() {
 		return ranks;
 	}
 
 	/**
 	 * Returns the modifier under the given name. Can return both negative
 	 * and positive modifiers. These modifiers represent values that will be
 	 * either added or subtracted from the skill.
 	 * 
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
 	 * 
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
 	 * 
 	 * @param name	the name of the modifier
 	 * @param value	the value of the modifier
 	 * @modifies this
 	 */
 	public void addModifier(String name, int value){
 		//TODO: Consider already existing values
 		modifiers.put(name, value);
 	}
 
 	/**
 	 * Returns the total bonus of the skill accounting for ranks, class bonus,
 	 * and miscellaneous modifiers.
 	 * 
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
 	public int getAbMod() {
 		return 0;
 	}
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
 		// TODO implement
 
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
