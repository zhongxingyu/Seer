 /** 
  * Author:  anthony.fodor@gmail.com
  * 
  * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version,
 * provided that any use properly credits the author.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details at http://www.gnu.org * * */
 
 package kmerDatabase;
 
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 
 public class KmerToBits
 {
 	private static final int A_INT = 0x0000;
 	private static final int C_INT = 0x0001;
 	private static final int G_INT = 0x0002;
 	private static final int T_INT = 0x0003;
 	private int bits = 0x0000;
 	private final String s;
 	private int index=0;
 	
 	public static float KMER_SIZE =16;
 	
 	public KmerToBits(String stringToHash) throws Exception
 	{
 		this.s =stringToHash;
 	
 		if(s.length() < KMER_SIZE)
 			throw new Exception("String must be longer than " + KMER_SIZE);
 		
 		for( int x=0; x < 16; x++)
 		{
 			addChar(s.charAt(x));
 			index++;
 		}	
 	}
 	
 	public void advance() throws Exception
 	{
 		addChar(s.charAt(index));
 		index++;
 	}
 	
 	public boolean canAdvance()
 	{
		return index < s.length()-1;
 	}
 	
 	public int getCurrentIndex()
 	{
 		return index;
 	}
 	
 	public int getHashAtCurrentPosition()
 	{
 		return bits;
 	}
 	
 	private void addChar( char c ) throws Exception
 	{
 		int mask = getMask(c);
 		bits = bits << 2;
 		bits = bits | mask;
 	}
 	
 	private int getMask(char c) throws Exception
 	{
 		if( c == 'A' )
 			return A_INT;
 			
 		if( c == 'C' )
 			return C_INT;
 			
 		if( c == 'G' )
 			return G_INT;
 			
 		if( c == 'T' )
 			return T_INT;
 			
 		throw new Exception("Invalid char " + c);
 	}
 	
 	private static char getChar( int i) throws Exception
 	{
 		if( i == A_INT  )
 			return 'A';
 			
 		if( i == C_INT )
 			return 'C';
 			
 		if( i == G_INT)
 			return 'G';
 			
 		if( i == T_INT)
 			return 'T';
 			
 		throw new Exception("Invalid hash " + i);
 	}
 	
 	public HashSet<Integer> getAllRemainingHashes() throws Exception
 	{
 		HashSet<Integer> set = new LinkedHashSet<Integer>();
 		
 		set.add( getHashAtCurrentPosition());
 		
 		while( canAdvance())
 		{
 			advance();
 			set.add( getHashAtCurrentPosition());
 		}
 		
 		return set;
 	}
 	
 	public static String getKmer(int hash) throws Exception
 	{
 		StringBuffer buff = new StringBuffer();
 		
 		for( int x=0; x < KMER_SIZE; x++)
 		{
 			buff.append ( getChar(T_INT & hash));
 			hash = hash >> 2;
 		}
		
 		return buff.toString();
 	}
 	
 	public static void main(String[] args) throws Exception
 	{
		String s1 = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAATTTCTTGTTTTTAAT";
 		
 		KmerToBits kmers = new KmerToBits(s1);
 		
		HashSet<Integer> set1 = new HashSet<Integer>();
		
		set1.add( kmers.getHashAtCurrentPosition());
		while( kmers.canAdvance())
		{
			kmers.advance();
			set1.add( kmers.getHashAtCurrentPosition());
		}
		
		System.out.println(set1);
		
		for( int i: set1 )
 			System.out.println(getKmer(i));
 	}
 }
