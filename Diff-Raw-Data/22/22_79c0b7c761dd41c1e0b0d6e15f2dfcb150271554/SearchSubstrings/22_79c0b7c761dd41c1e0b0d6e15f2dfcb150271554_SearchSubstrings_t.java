 package com.bananity.util;
 
 
 import com.bananity.text.TextNormalizer;
 import com.bananity.util.SearchesTokenizer;
 
 import java.util.Set;
 import java.util.HashSet;
 import java.util.ArrayList;
 
 
 public class SearchSubstrings {
 
 	private ArrayList<String> tokens;
 	private ArrayList<Integer> tokensMaxLength;
 	private ArrayList<HashBag<String>> tokensSubs; // substrings
 	private HashBag<String> hashBag;
 	private int maxTokenLength;
 
 	private static final int THRESHOLD = 2;
 
 	public SearchSubstrings (final String searchTerm) throws Exception {
 		this(searchTerm, THRESHOLD);
 	}
 
 	public SearchSubstrings (final String searchTerm, int threshold) throws Exception {
		this(SearchesTokenizer.tokenize(searchTerm, true, true), threshold);
 	}
 
 	public SearchSubstrings (final ArrayList<String> basicTokens ) {
 		this(basicTokens, THRESHOLD);
 	}
 
 	public SearchSubstrings (final ArrayList<String> basicTokens, int threshold) {
 		tokens = basicTokens;
 		maxTokenLength = 0;
 		hashBag = new HashBag<String>();
 		tokensMaxLength = new ArrayList<Integer>();
 		tokensSubs = new ArrayList<HashBag<String>>();
 		
 		for ( String s : tokens ) {
 			maxTokenLength = Math.max( maxTokenLength, s.length() );
 		}
 
 		for ( String s : tokens ) {
 			tokensMaxLength.add( s.length() );
 			HashBag<String> tmp = SearchesTokenizer.getSubstringsBag( s, (maxTokenLength>1)?threshold:1 );
 			tokensSubs.add( tmp );
 			hashBag.addAll( tmp );
 		}
 	}
 
 	public int getMaxTokenLength() {
 		return maxTokenLength;
 	}
 
 	public HashBag<String> getHashBag() {
 		return hashBag;
 	}
 
 	public HashSet<String> getUniqByLength( int threshold ) {
 		HashSet s = new HashSet();
 		for ( int i = 0; i < tokens.size(); i++ ) {
 			if ( tokensMaxLength.get( i ) <= threshold ) {
 				s.add( tokens.get( i ) );
 			} else {
 				for ( String ss : tokensSubs.get( i ).getUniqElements() ) {
 					if ( ss.length() >= threshold ) {
 						s.add( ss );
 					}
 				}
 			}
 		}
 		return s;
 	}
 
 	public String toString() {
 		StringBuilder sb = new StringBuilder( "[" );
 		for ( int i = 0; i < tokens.size(); i++ ) {
 			if ( i > 0 ) {
 				sb.append( ", " );
 			}
 			
 			sb.append( "{global: " )
 				.append( hashBag.toString() )
 				.append( ", token: " )
 				.append( tokens.get( i ) )
 				.append( ", length: " )
 				.append( tokensMaxLength.get( i ) )
 				.append( ", substrings: " )
 				.append( tokensSubs.get( i ).toString() )
 				.append( "}" );
 		}
 		sb.append( "]" );
 		return sb.toString();
 	}
 
 }
