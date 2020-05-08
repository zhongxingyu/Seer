 package org.alphabet;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Class that represents an Alphabet.
  * 
  * @param <T extends Alphabet<?>> The own subclass type to be able to create parameterized words.
  * 
  * @author Miguel Andrade
  */
 public abstract class Alphabet<T extends Alphabet<?>>{
 	
 	/**
 	 * the characters in the alphabet
 	 */
 	private final char[] alphabet;
 	/**
 	 * map of characters in alphabet for faster lookup
 	 */
 	private final Map<Character, Integer> alphabetMap;
     /**
      * the radix of the alphabet
      */
     private final int radix;
     
     /**
      * Builds an Alphabet object from the provided alphabet.
      * 
      * @param alpha A String containing the valid characters in this alphabet.
      * @param exclusions A String containing the characters to exclude from this alphabet.
      */
     public Alphabet(String alpha, String exclusions) throws InvalidAlphabetException{
     	
     	//exclude chars from alphabet
     	for (int i = 0; i < exclusions.length(); i++){
     	    char c = exclusions.charAt(i);      
     	    //Process char
     	    if(alpha.contains(String.valueOf(c))){
    	    	alpha = alpha.replace(String.valueOf(c), "");
     	    }
     	    else{
     	    	throw new InvalidAlphabetException("You're trying to exclude character '" + c + "' that doesn't belong to the alphabet.");
     	    }
     	}
     	
     	// check that alphabet contains no duplicate chars and build alphabet map
     	char[] alphabet = alpha.toCharArray();
         Map<Character, Integer> alphabetMap = new HashMap<Character,Integer>();
         int index;
         char c;
         for(index = 0; index < alphabet.length; index++){
         	c = alphabet[index];
         	if(alphabetMap.containsKey(c))
         		throw new InvalidAlphabetException("Illegal alphabet. Duplicate character '" + c + "'");
         	else
         		alphabetMap.put(c,index);
         }
         
         // Set variables
     	this.alphabet = alphabet;
     	this.alphabetMap = alphabetMap;
     	this.radix = alphabet.length;
 	}
     
     /**
      * Builds an Alphabet object from the provided alphabet.
      * 
      * @param alpha A char array containing the valid characters in this alphabet.
      */
     public Alphabet(char[] alpha, String exclusions) throws InvalidAlphabetException{
     	this(new String(alpha), exclusions);
     }
  
     /**
      * Builds an Alphabet object of Unicode chars 0 to radix-1.
      * 
      * @param radix The radix defining the alphabet.
      */
 	public Alphabet(int radix, String exclusions) {
 		this(fromRadix(radix), exclusions);
 	}
 	
 	/**
 	 * Helper method that creates a char array with unicode characters from the provided radix.
 	 * 
 	 * @param radix Destination radix of this alphabet.
 	 * @return A char array.
 	 */
 	private static char[] fromRadix(int radix){
 		char[] alphabet = new char[radix];
 
 		// can't use char since R can be as big as 65,536
 		for (int i = 0; i < radix; i++)
 			alphabet[i] = (char) i;
 		
 		return alphabet;
 	}
 
     /**
      * Checks if a word is valid in this alphabet.
      * 
      * @param word The word containing the characters to check against this alphabet.
      * @return A boolean representing the validity of this word against this alphabet.
      */
 	public boolean validate(String word) {
 		if(word==null)
 			return false;
 		
 		boolean result = true;
 		int i;
 		for(i=0;i<word.length();i++){
 			if(!alphabetMap.containsKey(word.charAt(i)))
 				return false;
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Computes the offending characters of a word, i.e characters which aren't part of this alphabet.
 	 * 
 	 * @param word The word to test.
 	 * @return A list of the characters which which aren't part of this alphabet.
 	 */
 	public List<Character> offendingCharacters(String word){
 		if(word==null)
 			return null;
 		
 		List<Character> result = new ArrayList<Character>();
 		int i;
 		for(i=0;i<word.length();i++){
 			if(!alphabetMap.containsKey(word.charAt(i)))
 				result.add(word.charAt(i));
 		}
 		
 		return result;
 	}
 
 	/**
 	 * A private method that creates a Word object in this alphabet. No validation is made, to be used internally.
 	 * 
 	 * @param word The String that represents the word.
 	 * @return The created Word object from the provided String.
 	 */
 	private Word<T> makeWord(String word){
 		return new Word<T>(word, this);
 	}
 	
 	/**
 	 * Creates a valid Word object in this alphabet.
 	 * 
 	 * @param word The String that represents the word.
 	 * @return A valid word.
 	 */	
 	public Word<T> createWord(String word) throws InvalidWordException {
 		List<Character> offendingChars = offendingCharacters(word);
 		if (offendingChars.size() == 0)
 			return makeWord(word);
 		else
 			throw new InvalidWordException(
 					"Word '"+ word
 							+ "' is invalid against this alphabet. Offending characters are: "
 							+ offendingChars.toString());
 	}
 
 	/**
 	 * Parses a given decimal long into the corresponding word in this alphabet.
 	 * 
 	 * @param value The value to parse.
 	 * @return The Word object that represents the given long.
 	 */
 	public Word<T> parseLong(long value) {
 		int resMod;
 		int resDiv = radix;
 		String res = "";
 		
 		while(resDiv > 0){
 			resMod = (int) (value % radix);
 			resDiv = (int) (value / radix);
 			value = resDiv;
 			char a = alphabet[resMod];
 			res = a+res;
 		}
 		
 		return makeWord(res);
 	}
 
 	/**
 	 * Parses a given decimal int into the corresponding word in this alphabet.
 	 * 
 	 * @param value The value to parse.
 	 * @return The Word object that represents the given long.
 	 */
 	public Word<T> parseInt(int value) {
 		return parseLong(value);
 	}
 	
 	/**
 	 * Returns the decimal representation of the given word as a long.
 	 * 
 	 * @param wordObj The Word object to parse.
 	 * @return The decimal representation of the given word.
 	 */
 	public long toLong(Word<T> wordObj){
 		// unwrap wordObj
 		String word = wordObj.toString();
 		
 		long value = 0;
 		int pow = 0;
 		int i;
 		for(i=word.length()-1; i>=0; i--){
 			char c = word.charAt(i);
 			value += alphabetMap.get(c) * Math.pow(radix, pow);
 			pow++;
 		}
 		
 		return value;
 	}
 
 	/**
 	 * Returns the decimal representation of the given word as an int.
 	 * 
 	 * @param wordObj The Word object to parse.
 	 * @return The decimal representation of the given word.
 	 */
 	public int toInt(Word<T> wordObj){
 		return (int) toLong(wordObj);
 	}
 	
 	@Override
 	public String toString() {
 		return new String(alphabet);
 	}
 }
