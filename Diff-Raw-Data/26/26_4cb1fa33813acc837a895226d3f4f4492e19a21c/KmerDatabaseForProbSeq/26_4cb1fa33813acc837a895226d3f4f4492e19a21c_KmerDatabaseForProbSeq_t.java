 /** 
  * Author:  anthony.fodor@gmail.com    
  * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version,
 * provided that any use properly credits the author.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details at http://www.gnu.org * * */
 
 
 package probabilisticNW;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import kmerDatabase.KmerToBits;
 
 public class KmerDatabaseForProbSeq
 {
 	private HashMap<Integer, HashSet<ProbSequence>> kmerMap =
 			new HashMap<Integer, HashSet<ProbSequence>>();
 	
 	private void addHash(int i, ProbSequence probSeq)
 	{
 		HashSet<ProbSequence> set = kmerMap.get(i);
 		
 		if( set == null)
 		{
 			set = new HashSet<ProbSequence>();
 			kmerMap.put(i,set);
 		}
 		
 		set.add(probSeq);
 	}
 	
 	public List<KmerQueryResultForProbSeq> queryDatabase(String query) throws Exception
 	{
 		List<KmerQueryResultForProbSeq> list = new ArrayList<KmerQueryResultForProbSeq>();
 		
 		HashMap<ProbSequence, Integer> countMap = new HashMap<ProbSequence, Integer>();
 		
 		KmerToBits toBits = new KmerToBits(query);
 		addToCountMap(toBits.getHashAtCurrentPosition(), countMap);
 		
 		while(toBits.canAdvance())
 		{
 			toBits.advance();
 			addToCountMap(toBits.getHashAtCurrentPosition(), countMap);
 		}
 		
 		for(ProbSequence probSeq : countMap.keySet())
 			list.add( new KmerQueryResultForProbSeq(probSeq, countMap.get(probSeq)) );
 		
 		Collections.sort(list);
 		return list;
 	}
 	
 	private void addToCountMap( int hash,  HashMap<ProbSequence, Integer> countMap)
 	{
 		HashSet<ProbSequence> samples = kmerMap.get(hash);
 		
 		if( samples != null)
 		{
 			for(ProbSequence probSeq : samples)
 			{
 				Integer count = countMap.get(probSeq);
 				
 				if( count == null)
 					count=0;
 				
 				count++;
 				
 				countMap.put(probSeq, count);
 			}
 		}
 	}
 	
 	public void addSequenceToDatabase(ProbSequence probSeq)
 		throws Exception
 	{
 		KmerToBits toBits = new KmerToBits(probSeq.getConsensusUngapped());
 		
 		addHash(toBits.getHashAtCurrentPosition(), probSeq);
 		
 		while(toBits.canAdvance())
 		{
 			toBits.advance();
 			addHash(toBits.getHashAtCurrentPosition(), probSeq);
 		}
 	}
 	
 	/*
 	 * Any sequence with non ACGT is ignored
 	 */
 	public static KmerDatabaseForProbSeq buildDatabase(List<ProbSequence> list) throws Exception
 	{
 		KmerDatabaseForProbSeq db = new KmerDatabaseForProbSeq();
 		
 		for( ProbSequence probSeq : list)
 		{	
 			db.addSequenceToDatabase(probSeq);
 		}		
 		return db;
 	}
 }
