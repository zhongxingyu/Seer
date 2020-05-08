 package com.bananity.util;
 
 
 import com.bananity.nlp.AnalyzerModule;
 import com.bananity.util.HashBag;
 
 import java.util.ArrayList;
 
 
 /**
  *  @author Andreu Correa Casablanca
  */
 public class SearchesTokenizer
 {
 	private final static int MIN_WORD_LENGTH = 2;
 
 	public static ArrayList<String> getSubTokensList (String searchTerm) throws Exception {		
 		ArrayList<String> tokens = AnalyzerModule.analyze(searchTerm, true);
 
 		int maxLength = 0;
 		for (String token : tokens) {
 			if (token.length() > maxLength) maxLength = token.length();
 		}
 
 		if (maxLength == 1) return tokens; // Hack to handle cases such as "A A A"
 
 		ArrayList<String> subTokens = new ArrayList<String>();
 		for (String token : tokens) {
 			subTokens.addAll(AnalyzerModule.getAllSubstrings(token, MIN_WORD_LENGTH));
 		}
 
 		return subTokens;
 	}
 
 	public static HashBag<String> getSubTokensBag (String searchTerm) throws Exception {
 		ArrayList<String> tokens = AnalyzerModule.analyze(searchTerm, true);
 
 		int maxLength = 0;
 		for (String token : tokens) {
 			if (token.length() > maxLength) maxLength = token.length();
 		}
 
 		if (maxLength == 1) return new HashBag(tokens); // Hack to handle cases such as "A A A"
 
 		HashBag<String> subTokens = new HashBag<String>();
 		for (String token : tokens) {
 			subTokens.addAll(AnalyzerModule.getSubstringsBag(token, MIN_WORD_LENGTH));
 		}
 
 		return subTokens;
 	}
 }
