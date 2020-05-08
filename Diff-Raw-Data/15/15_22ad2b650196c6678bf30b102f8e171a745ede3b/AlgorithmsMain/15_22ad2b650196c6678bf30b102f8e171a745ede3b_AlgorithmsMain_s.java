 package com.max.algs;
 
 import com.max.algs.string.matching.NaiveStringMatcher;
 
 
 
 
 
 public final class AlgorithmsMain {	
 	
 	 
 	
 
 	private AlgorithmsMain() throws Exception {	
 		
		String text = "max is the best all over the world is cool";
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
