 package com.boomui.ddcharactersheet;
 
 import java.util.*;
 
 public class SpellsPrepared{
 	public static String CHARACTER_SPLIT = "!####!";
 	public static String LEVEL_SPLIT = "!###@!";
 	public static String SPELL_SPLIT = "!##@#!";
 	public static String SPELL_DATA_SPLIT = "!##@@!";
 
 	private List<PreparedSpellLevel> levels;
 	String characterClass;
 	
 	public SpellsPrepared(FragmentCommunicator com, String characterClass){
 		this(com.loadData(CharacterDataKey.SPELLS_PREPARED), characterClass);
 	}
 	public SpellsPrepared(String savedData, String characterClass){
 		this.characterClass = characterClass;
 		
 		levels = new LinkedList<PreparedSpellLevel>();
 		
 		for(int i = 0; i < 10; i++){
 			levels.add(new PreparedSpellLevel() );
 		}
 		
 		//There won't be anything stored yet, this is just a sample
 		if(savedData == null){
			savedData = "Misc."+LEVEL_SPLIT+"Detect Magic"+SPELL_DATA_SPLIT+"false"+SPELL_SPLIT+"Read Magic"+SPELL_DATA_SPLIT+"false"+SPELL_SPLIT+" "+SPELL_DATA_SPLIT+"true"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-" + CHARACTER_SPLIT + "Sorcerer."+LEVEL_SPLIT+"Acid Splash"+SPELL_DATA_SPLIT+"false"+LEVEL_SPLIT+"Magic Missile"+SPELL_DATA_SPLIT+"false"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-" + CHARACTER_SPLIT + "Wizard."+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+LEVEL_SPLIT+"-"+CHARACTER_SPLIT;
 		}
 		
 		String[] classes = savedData.split(CHARACTER_SPLIT);
 		for(String c : classes){
 			if(c.startsWith(characterClass) ){
 				//Remove the name of the class from the list of spells
 				c = c.substring(c.indexOf(LEVEL_SPLIT) + LEVEL_SPLIT.length() );
 				
 				String[] spellLevels = c.split(LEVEL_SPLIT);
 				for(int i = 0; i < spellLevels.length; i++){
 					if(spellLevels[i].equals("-") ){
 						continue;
 					}
 					
 					PreparedSpellLevel levelHolder = levels.get(i);
 					String[] spells = spellLevels[i].split(SPELL_SPLIT);
 					for(String spell : spells){
 						String[] spellData = spell.split(SPELL_DATA_SPLIT, -1);
 						
 						levelHolder.add(new PreparedSpell(spellData[0], Boolean.parseBoolean(spellData[1]) ) );
 					}
 				}
 				break;
 			}
 		}
 	}
 	
 	public String getSpellName(int level, int index){
 		return levels.get(level).getSpellName(index);
 	}
 	public PreparedSpell getSpell(int level, int index){
 		return levels.get(level).getSpell(index);
 	}
 	public String getLevelName(int level){
 		return "Level " + level;
 	}
 	
 	public int getNumSpells(int level){
 		return levels.get(level).getNumSpells();
 	}
 	public int getNumLevels(){
 		return levels.size();
 	}
 	
 	public void addSpell(String name, int level){
 		levels.get(level).add(new PreparedSpell(name, false) );
 	}
 	public void removeLastSpell(int level){
 		levels.get(level).removeLastSpell();
 	}
 	public void removeSpellByName(int level, String name){
 		levels.get(level).removeSpellByName(name);
 	}
 	
 	/*
 	 * This method attempts to add a spell to an empty spell slot.  If one isn't found, it adds it to the end of the list instead
 	 */
 	public void addNewSpell(String name, int lv){
 		PreparedSpellLevel level = levels.get(lv);
 		for(int i = 0; i < level.getNumSpells(); i++){
 			if(level.getSpellName(i).equals(" ") ){
 				level.getSpell(i).setName(name);
 				level.sort();
 				return;
 			}
 		}
 		level.add(new PreparedSpell(name, false) );
 	}
 
 	public String save(){
 		String retVal = characterClass;
 		
 		for(PreparedSpellLevel level : levels){
 			retVal += LEVEL_SPLIT;
 			retVal += level.save();
 		}
 		
 		return retVal + CHARACTER_SPLIT;
 	}
 	
 	public void sort(){
 		for(PreparedSpellLevel level : levels){
 			level.sort();
 		}
 	}
 }
 class PreparedSpellLevel{
 	private List<PreparedSpell> spells;
 	
 	public String getSpellName(int index){
 		return spells.get(index).getName();
 	}
 	public int getNumSpells(){
 		return spells.size();
 	}
 	public PreparedSpell getSpell(int index){
 		return spells.get(index);
 	}
 	public String save(){
 		String retVal = "";
 		for(PreparedSpell spell : spells){
 			retVal += spell.save() + SpellsPrepared.SPELL_SPLIT;
 		}
 		
 		if(retVal.equals("") ){
 			retVal = "-";
 		}
 		
 		return retVal;
 	}
 	
 	public void add(PreparedSpell sp){
 		spells.add(sp);
 		sort();
 	}
 	public void removeLastSpell(){
 		if(!spells.isEmpty() ){
 			spells.remove(spells.size() - 1);
 		}
 		sort();
 	}
 	public void removeSpellByName(String name){
 		for(PreparedSpell spell : spells){
 			if(spell.getName().equals(name) ){
 				spell.setName(" ");
 				break;
 			}
 		}
 		
 		sort();
 	}
 	
 	public void sort(){
 		Comparator<PreparedSpell> c = new Comparator<PreparedSpell>(){
 			public int compare(PreparedSpell a, PreparedSpell b){
 				if(a.getName().equals(" ") ){
 					return 1;
 				}
 				if(b.getName().equals(" ") ){
 					return -1;
 				}
 				
 				return a.getName().compareTo(b.getName() );
 			}
 		};
 		
 		Collections.sort(spells, c);
 	}
 	
 	public PreparedSpellLevel(){
 		spells = new LinkedList<PreparedSpell>();
 	}
 }
 class PreparedSpell{
 	private String name;
 	boolean used;
 	
 	public PreparedSpell(String name, boolean used){
 		this.name = name;
 		this.used = used;
 	}
 	
 	public String save(){
 		return name + SpellsPrepared.SPELL_DATA_SPLIT + used;
 	}
 	
 	public String getName(){
 		return name;
 	}
 	public void setName(String newName){
 		name = newName;
 	}
 }
