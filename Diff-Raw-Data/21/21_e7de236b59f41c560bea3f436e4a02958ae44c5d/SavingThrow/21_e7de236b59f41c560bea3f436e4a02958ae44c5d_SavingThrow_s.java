 package com.example.myfirstapp;
 
 import java.util.*;
 
 import android.database.sqlite.SQLiteDatabase;
 
 /**
  * 
  * @author lokiw
  *
  */
 public class SavingThrow {
 	private AbilityName assocAbility;
 	private int baseSave;
 	private Map<String,Integer> modifiers;
 	
 	/**
 	 * Initialize a saving throw.
 	 * 
 	 * @param 			attribute	the associated attribute for the saving throw,
 	 * 					throws IllegalArgumentException if not WISDOME, DEXTERITY or
 	 * 					CONSTITUTION
 	 */
 	public SavingThrow(AbilityName attribute){
		if (attribute != AbilityName.WISDOME || attribute != AbilityName.DEXTERITY
				|| attribute != AbilityName.CONSTITUTION) {
 			throw new IllegalArgumentException();
 		}
 		assocAbility = attribute;
 		baseSave = 0;
 		modifiers = new HashMap<String,Integer>();
 	}
 	
 	/**
 	 * Set base save to given value
 	 * 
 	 * @param save	new base save
 	 */
 	public void setBaseSave(int save){
 		if (save < 0) {
 			throw new IllegalArgumentException();
 		}
 		
 		baseSave = save;
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
 	 * 
 	 * @param mod
 	 * @return
 	 */
 	public int getTotal(Ability mod){
 		if (mod.getName() != assocAbility) {
 			throw new IllegalArgumentException();
 		}
 		
 		int total = baseSave;
 		total += mod.getMod();
 		
 		Collection<Integer> mods = modifiers.values();
 		Iterator<Integer> it = mods.iterator();
 		while (it.hasNext()) {
 			total += it.next();
 		}
 		
 		return total;
 	}
 	
 	
 	/** 
 	 * Writes Saving Throw to database. SHOULD ONLY BE CALLED BY CHARACTER
 	 * @param id id of character
 	 * @param db database to write into
 	 */
 	public void writeToDB(long id, SQLiteDatabase db) {
 		// TODO implement
 	}
 }
