 package com.szuhanchang.hangman;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 public class PossibleWordsSet extends HashSet<String> {
 	private final CharacterFrequencyTable frequencyTable = new CharacterFrequencyTable();
 	
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public boolean addAll(Collection<? extends String> collection) {
 		for (String s : collection) {
 			for (char c : s.toCharArray()) {
 				frequencyTable.increment(c);
 			}
 		}
 		
 		return super.addAll(collection);
 	}
 	
 	@Override
 	public boolean remove(Object arg) {
 		String s = (String) arg;
 		for (char c : s.toCharArray()) {
 			frequencyTable.decrement(c);
 		}
 		return super.remove(arg);
 	}
 	
 	@Override
 	public void clear() {
 		frequencyTable.clear();
 		super.clear();
 	}
 	
 	/*
 	 * Retrieves the single remaining word in a set, otherwise returns null if the set is empty or has multiple elements.
 	 */
 	public String getLastWord() {
 		if (super.size() != 1) {
 			return null;
 		}
 		
 		String[] retval = super.toArray(HanChangGuessingStrategy.STRING_ARRAY_PLACEHOLDER);
 		return retval[0];
 	}
 	
	public int removeCharacter(char c) {
 		return frequencyTable.remove(c);
 	}
 	
 	public char getCharacterWithHighestFrequency() {
 		return frequencyTable.getCharacterWithHighestFrequency();
 	}
 	
 	public int getFrequencyTableSize() {
 		return frequencyTable.size();
 	}
 }
