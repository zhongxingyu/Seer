 package com.max.algs.string.matching;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public final class NaiveStringMatcher {
 
 	
 	private NaiveStringMatcher() {
 		super();
 		throw new IllegalStateException("Can't instantiate utility class '" + NaiveStringMatcher.class.getName() + "'");
 	}
 	
 	
 	/**
 	 * 
 	 * time: O((N-M+1*)M)
 	 * space: O(1)
 	 * 
 	 */
 	public static List<Integer> validShifts( String text, String pattern ){
 		
 		List<Integer> shifts = new ArrayList<>();
 		
 		int indexOffset = 0;
 		
 		while( true ){
 			
 			indexOffset = NaiveStringMatcher.indexOf(text, pattern, indexOffset);
 			
 			if( indexOffset < 0 ){
 				break;
 			}
 			
 			shifts.add( indexOffset );
 			++indexOffset;
 		}
 		
 		return shifts;
 		
 	}
 	
 	
 	public static int indexOf(String text, String pattern){
 		return indexOf(text, pattern, 0);
 	}
 	
 	
 	public static int indexOf(String text, String pattern, int from){
 		
		if( pattern.length() > text.length()-from ){
 			return -1;
 		}
 		
 		int n = text.length();
 		int m = pattern.length();
 		
		for( int i = from; i < n-m+1; i++){			
 			if( isMatched(text, pattern, i)){
 				return i;
 			}			
 		}
 		
 		
 		return -1;
 	}
 	
 	
 	
 	
	private static boolean isMatched(String text, String pattern, int offset){
 		
 		for( int i = 0; i < pattern.length(); i++ ){
 			if( text.charAt(offset+i) != pattern.charAt(i) ){
 				return false;
 			}
 		}
 		
 		return true;		
 	}
 	
 	
 
 }
