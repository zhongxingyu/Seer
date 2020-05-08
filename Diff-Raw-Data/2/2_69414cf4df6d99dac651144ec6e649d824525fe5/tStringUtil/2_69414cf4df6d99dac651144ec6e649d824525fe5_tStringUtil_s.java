 package net.teumert.java.util;
 
 import java.util.Random;
 
 public class tStringUtil {
 	
 	/**
 	 * Currently unused	 
 	 * @param source Array containing the objects that should be imploded in string representation
 	 * @param delimiter Separator for the strings
 	 * @return String containing all objects in string representation separated by delimiter
 	 */
 	public static String implode(Object[] source, String delimiter) {
 		StringBuilder sb = new StringBuilder(source[0].toString());
 		for (int i = 1; i < source.length; i++) {
 			sb.append(delimiter);
 			sb.append(source[i].toString());
 		}		
 		return sb.toString();
 	}
 	
 	/**
 	 * {@link}http://stackoverflow.com/questions/4951997/generating-random-words-in-java
 	 * @param numberOfWords
 	 * @return
 	 */
 	public static String[] generateRandomWords(int numberOfWords)
 	{
 	    String[] randomStrings = new String[numberOfWords];
 	    Random random = new Random();
 	    for(int i = 0; i < numberOfWords; i++)
 	    {
 	        char[] word = new char[random.nextInt(8)+3]; // words of length 3 through 10. (1 and 2 letter words are boring.)
 	        for(int j = 0; j < word.length; j++)
 	        {
 	            word[j] = (char)('a' + random.nextInt(26));
 	        }
 	        randomStrings[i] = new String(word);
 	    }
 	    return randomStrings;
 	}
 	
 }
