 package com.max.algs.string.matching;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public enum RabinKarpMatcher implements IStringMatcher {
 
 	INST;
 	
 	
 	private static final int BASE = 101;
 	private static final int BIG_PRIME = 113;
 	
 	
 	/**
 	 * 
 	 * Assumption: all patterns have the same length. 
 	 * 
 	 */
 	public List<Integer> validShiftsMulti( String text, List<String> patterns ) {
 		
 		checkForNull(text, "NULL 'text' passed");
 		checkForNull(patterns, "NULL 'patterns' passed");
 		
 		if( patterns.isEmpty() ){
 			return new ArrayList<>();
 		}					
 		
 		Map<Integer, List<String>> patternsHashes = new HashMap<>();
 		
 		for( String pattern : patterns ){
 			int hash = calculateFingerprint( pattern, 0, pattern.length() );
 			
 			List<String> samesHashedStrings = patternsHashes.get(hash);
 			
 			if( samesHashedStrings == null ){
 				samesHashedStrings = new ArrayList<>();
 			}
 			
 			samesHashedStrings.add( pattern );			
 			patternsHashes.put(hash, samesHashedStrings);
 		}
 		
 		
 		int patternLength =  patterns.get(0).length(); 
 		
 		int height = calculateHeight( patterns.get(0) );	
 		
 		List<Integer> shifts = new ArrayList<>();
 		
 		int slidingHash = 0;
 		
 		for( int i = 0; i < text.length() - patternLength + 1; i++ ){			
 			if( i == 0 ){
 				slidingHash = calculateFingerprint( text, 0, patternLength );
 			}
 			else {						
 				slidingHash = recalculateHash(text.charAt(i-1),  text.charAt( i+patternLength-1 ), slidingHash, height);
 			}
 			
 			List<String> patternCandidates = patternsHashes.get(slidingHash);
 			
 			if( patternCandidates != null ){			
 				for( String candidate : patternsHashes.get(slidingHash) ){
 					 if( isEquals(candidate, text, i) ){
 						 shifts.add( i ); 
 					 }
 				}
 			}
 		}
 		
 		
 		return shifts;
 	}
 	
 
 	private <T> void checkForNull(T obj, String errorMsg) {		
 		if( obj == null ){
 			throw new IllegalArgumentException(errorMsg);
 		}		
 	}
 
 
 	@Override
 	public List<Integer> validShifts(String text, String pattern) {
 		
 		checkForNull(text, "NULL 'text' passed");
 		checkForNull(pattern, "NULL 'pattern' passed");
 		
 		int height = calculateHeight(pattern);			
 		
 		List<Integer> possibleShifts = new ArrayList<>();
 		
 		final int patternLength = pattern.length();
 		final int textLength = text.length();
 		
 		int patternHash = calculateFingerprint( pattern, 0, pattern.length() );
 		
 		int slidingHash = 0; 
 		
 		for( int i = 0; i < textLength - patternLength + 1; i++ ){			
 			if( i == 0 ){
 				slidingHash = calculateFingerprint( text, 0, patternLength );
 			}
 			else {						
 				slidingHash = recalculateHash(text.charAt(i-1),  text.charAt( i+patternLength-1 ), slidingHash, height);
 			}
 			
 			if( patternHash == slidingHash ){				
 				possibleShifts.add( i );
 			}
 		}			
 		
 		return filterRuns(text, pattern, possibleShifts);
 	}
 	
 	
 	private List<Integer> filterRuns(String text, String pattern, List<Integer> possibleShifts){
 		
 		if( possibleShifts.isEmpty() ){
 			return new ArrayList<>();
 		}
 		
 		int halfPatternLength = pattern.length()/2;
 		
 		List<Integer> validShifts = new ArrayList<>();
 		
 		List<Integer> singleRun = new ArrayList<>();
 		singleRun.add( possibleShifts.get(0) ); 
 		
 		for( int i = 1; i < possibleShifts.size(); i++ ){
 			
 			if( singleRun.isEmpty() ){
 				singleRun.add( possibleShifts.get(i) );
 			}
 			else {
 				
 				int prevShift = possibleShifts.get(i-1);
 				int curShift = possibleShifts.get(i);
 				
 				int d = curShift- prevShift;
 				
 				if( d <= halfPatternLength ){
 					singleRun.add( curShift );
 				}
 				else {
 					validShifts.addAll( filterSingleRun(text, pattern, singleRun) );
 					singleRun = new ArrayList<>();
 					singleRun.add(curShift);
 				}
 			}
 				
 		}
 		
 		if( ! singleRun.isEmpty() ){
 			validShifts.addAll( filterSingleRun(text, pattern, singleRun) );
 		}
 		
 		
 		return validShifts;
 		
 	}
 	
 	
 	
 	private List<Integer> filterSingleRun(String text, String pattern, List<Integer> singleRun){
 		
 		 List<Integer> validShifts = new ArrayList<>();
 		
 		if( singleRun.size() < 3 ){
 			for( int shift : singleRun ){
 				if( isEquals(pattern, text, shift) ){
 					validShifts.add(shift);
 				}
 			}
 		}
 		else {
 			
 			int first = singleRun.get(0);
 			int second = singleRun.get(1);
 			
 			boolean firstMatch = isEquals(pattern, text, first);
 			boolean secondMatch = isEquals(pattern, text, second);
 			
 			if( !firstMatch || !secondMatch ){
 				return validShifts;
 			}
 			else {
 				int d = second - first;
 				
 				for( int i = 2; i < singleRun.size(); i++ ){
 					
 					int currentD = singleRun.get(i) - singleRun.get(i-1);
 					
 					if( currentD != d ){
 						return new ArrayList<>();
 					}
 				 
 					validShifts.add( singleRun.get(i) );					
 				}
 				
 				for( int i = 2; i < singleRun.size(); i++ ){
 					int shift = singleRun.get(i);					
 					isEquals(pattern, pattern.length()-d, text, shift, d);
 				}				
 			}			
 		}
 		
 		return validShifts;
 	}
 	
 	private int calculateHeight(String pattern){
 		
 		int count = pattern.length()-1;
 		int res = 1;
 		
 		while( count > 0 ){
 			res = (BASE*res) % BIG_PRIME;
 			--count;
 		}
 		
 		return res;
 		
 	}
 	
 	// recalculate fingerprint	
 	private int recalculateHash( char charToRemove, char newCh, int prevHash, int height ){	
 		
 		int newHashValue = prevHash - (charToRemove * height) % BIG_PRIME;
 		
 		newHashValue = (BASE * newHashValue) % BIG_PRIME;
 		
 		newHashValue = (newHashValue + newCh) % BIG_PRIME;
 		
 		return newHashValue;	
 	}
 	
 	private boolean isEquals(String pattern, String text, int textOffset){		
 		return isEquals(pattern, 0, text, textOffset, pattern.length() );
 	}
 	
 	private boolean isEquals(String pattern, int patternFrom, String text, int textFrom, int count){
 		
 		for( int i = 0; i < count; i++ ){
 			if( pattern.charAt(patternFrom + i) != text.charAt(textFrom + i) ){
 				return false;
 			}
 		}		
 		
 		return true;		
 	}
 	
 	private int calculateFingerprint( String str, int from, int to ){
 		
 		int fingerprint = 0; 
 		
 		for( int i = from; i < to; i++ ){
 			fingerprint = (( BASE * fingerprint ) % BIG_PRIME) + (int)str.charAt(i); 
 		}
 		
 		return fingerprint % BIG_PRIME;		
 	}	
 	
 
 }
