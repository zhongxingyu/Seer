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
 
 
 package bottomUpTree;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import parsers.FastaSequence;
 import parsers.FastaSequenceOneAtATime;
 import probabilisticNW.KmerDatabaseForProbSeq;
 import probabilisticNW.KmerQueryResultForProbSeq;
 import probabilisticNW.ProbNW;
 import probabilisticNW.ProbSequence;
 import utils.ConfigReader;
 
 public class ClusterAtLevel
 {
 public static final boolean LOG = true;
 	
 	private static int getNumExpected(List<ProbSequence> list ) 
 	{
 		int expectedSeq =0;
 		
 		for( ProbSequence probSeq : list)
 		{
 			//System.out.println(expectedSeq + " " + probSeq.getNumRepresentedSequences());
 			expectedSeq += probSeq.getNumRepresentedSequences();
 		}
 		
 		return expectedSeq;
 	}
 	
 	static int getNumberOfDereplicatedSequences(FastaSequence fs) throws Exception
 	{
 		StringTokenizer header = new StringTokenizer(fs.getFirstTokenOfHeader(), "_");
 		header.nextToken();
 		header.nextToken();
 		
 		int returnVal = Integer.parseInt(header.nextToken());
 		
 		if( header.hasMoreTokens())
 			throw new Exception("Parsing error " + fs.getHeader());
 		
 		if( returnVal <= 0)
 			throw new Exception("Parsing error " + fs.getHeader());
 		
 		return returnVal;
 	}
 	
 	private static void writeToLog(String type, int numberAlignmentPeformed, int targetIndex, int numQuerySequences,
 			BufferedWriter logWriter, ProbSequence possibleAlignment, ProbSequence querySequence,
 				KmerQueryResultForProbSeq kmerResult) throws Exception
 	{
 		logWriter.write(type + "\t");
 		logWriter.write( numberAlignmentPeformed + "\t");
 		logWriter.write((targetIndex + 1)  +"\t");
 		logWriter.write( numQuerySequences + "\t");
		logWriter.write(  kmerResult.getCounts() + "\t"  );
 		logWriter.write( querySequence.getNumRepresentedSequences() + "\t");
 		logWriter.write( (kmerResult == null ? "-1" :  kmerResult.getProbSeq().getNumRepresentedSequences()) + "\t");
 		logWriter.write( (querySequence.getNumRepresentedSequences()  + kmerResult.getProbSeq().getNumRepresentedSequences() ) + "\t" );
 		logWriter.write( possibleAlignment.getAverageDistance() + "\t");
 		logWriter.write( possibleAlignment.getAlignmentScoreAveragedByCol() + "\n");
 		logWriter.flush();
 	}
 	
 	/*
 	 * 
 	 * As a side effect, all seqs are removed from seqsToCluster
 	 */
 	public static void clusterAtLevel( List<ProbSequence> alreadyClustered,
 					List<ProbSequence> seqstoCluster, 
 								float levelToCluster, float stopSearchThreshold, String runID) throws Exception
 	{
 		System.out.println("STARTING");
 		BufferedWriter logWriter = null;
 		
 		if( LOG)
 		{
 			logWriter= new BufferedWriter( new FileWriter(new File(ConfigReader.getETreeTestDir() + 
 					File.separator + "log_" + runID + "_"+  System.currentTimeMillis() + ".txt")));
 			logWriter.write("alignmentPerformed\talignmentInSeries\tnumberOfQuerySequences\tkmersInCommon\tnumSeqsQuery\tnumSeqsPossibleTarget\t" + 
 					"totalNumSequences\tprobAlignmentDistnace\taverageAlignmentScore\n");
 			logWriter.flush();
 			
 		}
 		
 		if( stopSearchThreshold < levelToCluster)
 			throw new Exception("Illegal arguments ");
 		
 		int expectedSeq = getNumExpected(seqstoCluster) + getNumExpected(alreadyClustered);
 
 		KmerDatabaseForProbSeq db = KmerDatabaseForProbSeq.buildDatabase(alreadyClustered);
 		int numAlignmentsPerformed =0;
 		int originalQuerySize = seqstoCluster.size();
 		
 		while( seqstoCluster.size() > 0)
 		{
 			numAlignmentsPerformed++;
 			ProbSequence querySeq = seqstoCluster.remove(0);
 			
 			List<KmerQueryResultForProbSeq> targets = 
 					db.queryDatabase(querySeq.getConsensusUngapped());
 			
 			ProbSequence targetSequence = null;
 			List<KmerQueryResultForProbSeq> matchingList =new ArrayList<KmerQueryResultForProbSeq>();
 			int targetIndex =0;
 			
 			while( targetIndex < targets.size())
 			{
 				KmerQueryResultForProbSeq possibleMatch = targets.get(targetIndex);
 				ProbSequence possibleAlignment = 
 							ProbNW.align(querySeq, possibleMatch.getProbSeq());
 				
 				if(LOG)
 					writeToLog("primary", numAlignmentsPerformed, targetIndex, originalQuerySize-seqstoCluster.size(), 
 							logWriter, possibleAlignment, querySeq, possibleMatch);
 				
 				double distance =possibleAlignment.getAverageDistance();		
 				
 				if(  distance <= levelToCluster)
 				{
 					matchingList.add(possibleMatch);
 					possibleMatch.setAlignSeq(possibleAlignment);
 				}
 			
 				targetIndex++;	
 			}
 			
 			if( matchingList.size() > 1) 
 			System.out.println("MATHCING LIST=" + matchingList.size() );
 			
 			if( matchingList.size() >= 1)
 			{
 				Collections.sort(matchingList, new KmerQueryResultForProbSeq.SortByNumSequences());
 				targetSequence = matchingList.get(0).getProbSeq();
 				matchingList.get(0).getAlignSeq().setMapCount(targetSequence, querySeq);
 				targetSequence.replaceWithDeepCopy(matchingList.get(0).getAlignSeq());
 				// pick up any new kmers that we migtht have acquired
 				db.addSequenceToDatabase(targetSequence);
 				
 				//get all vs. all merge
 				//System.out.println("GOT " + matchingList.size() + " possibles ");
 				
 				// now check to see if we should merge any of the OTUs that had good matches to the target
 				numAlignmentsPerformed=0;
 				boolean mergedOne = false;
 				for( int x=0; x < matchingList.size()-1 && ! mergedOne; x++)
 				{
 					ProbSequence xSeq = matchingList.get(x).getProbSeq();
 					
 					for( int y=x+1; y < matchingList.size() && ! mergedOne; y++)
 					{
 						ProbSequence ySeq = matchingList.get(y).getProbSeq();
 						ProbSequence possibleAlignment = ProbNW.align(xSeq, ySeq);
 						
 						numAlignmentsPerformed++;
 						
 						if( possibleAlignment.getAverageDistance() <= levelToCluster )
 						{
 							mergedOne = true;
 							xSeq.setMapCount(xSeq, ySeq);
 							xSeq.replaceWithDeepCopy(possibleAlignment);
 							if ( ! alreadyClustered.remove(ySeq))
 								throw new Exception("Could not remove object");
 							
 							db = KmerDatabaseForProbSeq.buildDatabase(alreadyClustered);
 						}
 						
 						if(LOG)
 							writeToLog("secondary", numAlignmentsPerformed, targetIndex, xSeq.getNumRepresentedSequences(), 
 									logWriter, possibleAlignment, ySeq, null);
 						
 					}
 				}
 					
 			}
 			else if( targetSequence == null)
 			{
 				System.out.println("Adding new");
 				alreadyClustered.add(querySeq);
 				db.addSequenceToDatabase(querySeq);
 			}
 			
 			if( alreadyClustered.size() %1000 ==0 )
 				System.out.println(alreadyClustered.size());
 		}
 		
 		if( seqstoCluster.size() != 0)
 			throw new Exception("Expecting 0 for " + seqstoCluster.size());
  
 		int gottenSeqs = getNumExpected(alreadyClustered);
 		
 		if( expectedSeq != gottenSeqs)
 			throw new Exception("Expecting " + expectedSeq + " but got " + gottenSeqs + " in " + alreadyClustered.size() + " clusters ");
 		
 		if( LOG)
 		{
 			logWriter.flush(); logWriter.close();
 		}
 		
 	}
 	
 	public static List<ProbSequence> getInitialSequencesFromFasta(String fastaPath, String sampleId,
 			float threshold, float stopThreshold, String runID) throws Exception
 	{
 		List<ProbSequence> probSeqs = new ArrayList<ProbSequence>();
 		
 		FastaSequenceOneAtATime fsoat = new FastaSequenceOneAtATime(fastaPath);
 	
 		int expectedSum =0;
 		for(FastaSequence fs = fsoat.getNextSequence(); fs != null; fs = fsoat.getNextSequence())
 			if( fs.isOnlyACGT())
 			{
 				expectedSum += getNumberOfDereplicatedSequences(fs);
 				//System.out.println(getNumberOfDereplicatedSequences(fs));
 				probSeqs.add(new ProbSequence(fs.getSequence(), getNumberOfDereplicatedSequences(fs),sampleId));
 			}
 		
 		List<ProbSequence> clustered = new ArrayList<ProbSequence>();
 		
 		clusterAtLevel(clustered, probSeqs, threshold, stopThreshold, runID);
 		
 		int numClustered = 0;
 		for(ProbSequence ps : clustered)
 		{
 			//System.out.println(ps);
 			numClustered += ps.getNumRepresentedSequences();
 		}
 			
 		if( numClustered != expectedSum )
 			throw new Exception("Finished with " + clustered.size()  + " clusters with " + numClustered + " sequences");
 		
 		return clustered;
 	}
 }
