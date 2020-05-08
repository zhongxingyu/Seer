 package com.jaredpearson.puzzlestrike;
 
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Represents one selectable game character
  * @author jared.pearson
  */
 public class GameCharacter {
 	private static final Map<String, GameCharacter> CHARACTERS = initializeCharacters();
 	private String name;
 	private Set<? extends Chip> chips;
 	
 	private GameCharacter(String name, Set<? extends Chip> chips) {
 		this.name = name;
 		this.chips = chips;
 	}
 	
 	/**
 	 * The name of the character.
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * Gets the chips that are associated to the specified character.
 	 */
 	public Set<? extends Chip> getChips() {
 		return chips;
 	}
 	
 	/**
 	 * Gets the character with the specified name.
 	 */
 	public static GameCharacter withName(String name) {
 		for(String characterName : CHARACTERS.keySet()) {
 			if(characterName.equalsIgnoreCase(name)) {
 				return CHARACTERS.get(characterName);
 			}
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	private static Map<String, GameCharacter> initializeCharacters() {
 		String[][] characterDefinitions = new String[][]{
 			new String[]{"Grave", "Reversal", "Martial Mastery", "Versatile Style"},
 			new String[]{"Midori", "Dragon Form", "Rigorous Training", "Purge Bad Habits"},
 			new String[]{"Setsuki", "Speed of the Fox", "Bag of Tricks", "Double-take"}
 		};
 		
 		Hashtable<String, GameCharacter> characters = new Hashtable<String, GameCharacter>(3);
 		
 		for(String[] characterDefinition : characterDefinitions) {
 			HashSet<Chip> chips = new HashSet<Chip>(3);
 			for(int index = 1; index < characterDefinition.length; index++) {
 				chips.add(new CharacterChip(characterDefinition[index]));
 			}
 			
 			characters.put(characterDefinition[0], new GameCharacter(characterDefinition[0], chips));
 		}
 		
 		return characters;
 	}
 	
 	private static class CharacterChip implements Chip {
 		private String name;
 		
 		public CharacterChip(String name) {
 			this.name = name;
 		}
 		
 		public String getName() {
 			return name;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((name == null) ? 0 : name.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			CharacterChip other = (CharacterChip) obj;
 			if (name == null) {
 				if (other.name != null)
 					return false;
 			} else if (!name.equals(other.name))
 				return false;
 			return true;
 		}
 	}
 }
