 package com.bananity.util;
 
 
 import com.bananity.text.TextNormalizer;
 import com.bananity.util.HashBag;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 
 /**
  *  @author Andreu Correa Casablanca
  */
 public class SearchesTokenizer
 {
 	/**
 	 *
 	 */
 	private final static int MIN_WORD_LENGTH = 2;
 
 	/**
 	 *
 	 */
 	public static ArrayList<String> getSubTokensList (String searchTerm) throws Exception {		
 		ArrayList<String> tokens = tokenize(searchTerm, true);
 
 		int maxLength = 0;
 		for (String token : tokens) {
 			if (token.length() > maxLength) maxLength = token.length();
 		}
 
 		if (maxLength == 1) return tokens; // Hack to handle cases such as "A A A"
 
 		ArrayList<String> subTokens = new ArrayList<String>();
 		for (String token : tokens) {
 			subTokens.addAll(getAllSubstrings(token, MIN_WORD_LENGTH));
 		}
 
 		// This block increments the index size, but allows more precise searches
 		for (int i=0, n=tokens.size()-1; i<n; i++) {
 			subTokens.add(new StringBuilder(tokens.get(i)).append(" ").append(tokens.get(i+1)).toString());
 		}
 
 		return subTokens;
 	}
 
 	/**
 	 *
 	 */
 	public static HashBag<String> getSubTokensBag (String searchTerm) throws Exception {
 		ArrayList<String> tokens = tokenize(searchTerm, true);
 
 		int maxLength = 0;
 		for (String token : tokens) {
 			if (token.length() > maxLength) maxLength = token.length();
 		}
 
 		if (maxLength == 1) return new HashBag(tokens); // Hack to handle cases such as "A A A"
 
 		HashBag<String> subTokens = new HashBag<String>();
 		for (String token : tokens) {
 			subTokens.addAll(getSubstringsBag(token, MIN_WORD_LENGTH));
 		}
 
 		// This block increments the index size, but allows more precise searches
 		for (int i=0, n=tokens.size()-1; i<n; i++) {
 			subTokens.put(new StringBuilder(tokens.get(i)).append(" ").append(tokens.get(i+1)).toString());
 		}
 
 		return subTokens;
 	}
 
 	/**
 	 *
 	 */
 	public static ArrayList<String> getAllSubstrings( String token, int minLength ) {
 		int l = token.length();
 		ArrayList<String> t = new ArrayList<String>();
 
 		if (l < minLength) {
 			return t;
 		}
 
 		minLength--;
 
 		for ( int c = 0; c < l; c++ ) {
 			for ( int r = minLength; r < l - c; r++ ){
 				t.add( token.substring( c, c + r + 1 ) );
 			}
 		}
 		return t;
 	}
 
 	/**
 	 *
 	 */
 	public static HashBag<String> getSubstringsBag( String token, int minLength ) {
 		HashBag<String> result = new HashBag<String>();
 
 		int l = token.length();
 		if (l < minLength) {
 			return result;
 		}
 
 		minLength--;
 
 		for ( int c = 0; c < l; c++ ) {
 			for ( int r = minLength; r < l - c; r++ ) {
 				result.put ( token.substring( c, c + r + 1 ) );
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 *
 	 */
 	public static ArrayList<String> tokenize (final String text, boolean flattenText) {
 		String processedText;
 
 		if (flattenText) {
 			processedText = TextNormalizer.flattenText(text);
 		} else {
 			processedText = text;
 		}
 
 		return new ArrayList<String>(Arrays.asList(text.split("\\s+")));
 	}
 }
