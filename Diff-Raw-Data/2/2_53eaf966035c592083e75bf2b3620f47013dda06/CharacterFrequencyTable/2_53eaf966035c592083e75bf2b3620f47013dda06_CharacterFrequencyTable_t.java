 package com.szuhanchang.hangman;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /*
  * Maps a character in the alphabet to the frequency in which it occurs in the PossibleWordsSet.
  */
 public class CharacterFrequencyTable extends ConcurrentHashMap<Character, Integer> {
 	private static final long serialVersionUID = 1L;
 
 	final int LETTERS_IN_ALPHABET = 26; // Assume standard English alphabet for now.                    
     
 	protected Map<Character, Integer> table = new ConcurrentHashMap<Character, Integer>(LETTERS_IN_ALPHABET);
 	
 	public void increment(char character) {
 		Integer prevValue = table.get(character);
 		if (prevValue == null) {
 			prevValue = 0;
 		}
 		table.put(character, prevValue + 1);
 	}
 	
 	public void decrement(char character) {
 		Integer prevValue = table.get(character);
 		if (prevValue == null) {
			return;
 		}
 		table.put(character, prevValue - 1);
 	}
 	
 	public int getFrequency(char character) {
 		return table.get(character);
 	}
 	
 	public int remove(char character) {
 		return table.remove(character);
 	}
 	
 	public char getCharacterWithHighestFrequency() {
 		System.out.println(table.toString());
 		// FIXME: This returns by alphabetic sorting. Fix to use frequency sorting.
 		int max = 0;
 		char mostFrequentChar = ' ';
 		for (Entry<Character, Integer> e : table.entrySet()) {
 			if (e.getValue() == 0) {
 				table.remove(e.getKey());
 			}
 			else if (e.getValue() > max) {
 				max = e.getValue();
 				mostFrequentChar = e.getKey();
 			}
 		}
 		
 		if (mostFrequentChar == ' ') {
 			System.err.println("HORRIBLE THINGS HAVE HAPPENED");
 			System.exit(-1);
 		}
 		
 		return mostFrequentChar;
 	}
 }
