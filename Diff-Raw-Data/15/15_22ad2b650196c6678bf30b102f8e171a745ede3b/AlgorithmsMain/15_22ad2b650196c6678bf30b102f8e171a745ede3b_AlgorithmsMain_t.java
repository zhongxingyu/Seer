 package com.max.algs;
 
 import com.max.algs.string.matching.NaiveStringMatcher;
 
 
 
 
 
 public final class AlgorithmsMain {	
 	
 	 
 	
 
 	private AlgorithmsMain() throws Exception {	
 		
		String text = "is max is the ttt temp  all cool cool is";
 		String pattern = "is";		
 		
 		for( int shift : NaiveStringMatcher.validShifts(text, pattern) ){
 			System.out.println( "Found at index: " + shift + ", str = " + text.subSequence(shift, shift+pattern.length()) );	
 		}
 		
 		System.out.println( "Main done" );
 		
 	}	
 
 	
 
 	public static void main(String[] args) {
 		try {			
 			new AlgorithmsMain();
 		}
 		catch( Exception ex ){
 			ex.printStackTrace();
 		}
 	}
 		
 
 }
