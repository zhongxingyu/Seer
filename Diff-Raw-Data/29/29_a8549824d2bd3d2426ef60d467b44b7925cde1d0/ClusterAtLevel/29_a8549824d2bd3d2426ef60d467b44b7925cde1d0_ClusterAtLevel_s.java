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
 
 import dynamicProgramming.DNASubstitutionMatrix;
 import dynamicProgramming.NeedlemanWunsch;
 import dynamicProgramming.PairedAlignment;
 import dynamicProgramming.SubstitutionMatrix;
 import eTree.ENode;
 
 import parsers.FastaSequence;
 import parsers.FastaSequenceOneAtATime;
 import probabilisticNW.KmerDatabaseForProbSeq;
 import probabilisticNW.KmerQueryResultForProbSeq;
 import probabilisticNW.ProbNW;
 import probabilisticNW.ProbSequence;
 import utils.ConfigReader;
 
 public class ClusterAtLevel
 {
 	public static final boolean LOG = false;
 	public static final boolean CALCULATE_CANNONICAL_NW= false;
 	
 	private static int getNumExpected(List<ENode> list ) 
 	{
 		int expectedSeq =0;
 		
 		for( ENode eNode: list)
 		{
 			//System.out.println(expectedSeq + " " + probSeq.getNumRepresentedSequences());
 			expectedSeq += eNode.getProbSequence().getNumRepresentedSequences();
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
 	
 	private static void writeToLog(int numberAlignmentPeformed, int targetIndex, int numQuerySequences,
 			BufferedWriter logWriter, ProbSequence possibleAlignment, ProbSequence querySequence,
 				KmerQueryResultForProbSeq kmerResult) throws Exception
 	{
 		
 		if( CALCULATE_CANNONICAL_NW)
 		{
 			SubstitutionMatrix sm = new DNASubstitutionMatrix();
 			PairedAlignment pa = NeedlemanWunsch.globalAlignTwoSequences(querySequence.getConsensus(), 
 					kmerResult.getProbSeq().getConsensus(),sm, ProbNW.GAP_PENALTY, 99, true);
 			logWriter.write(pa.getDistance() + "\t");
 		}
 		
 		logWriter.write( numberAlignmentPeformed + "\t");
 		logWriter.write((targetIndex + 1)  +"\t");
 		logWriter.write( numQuerySequences + "\t");
 		logWriter.write( kmerResult.getCounts() + "\t"  );
 		logWriter.write( querySequence.getNumRepresentedSequences() + "\t");
 		logWriter.write( kmerResult.getProbSeq().getNumRepresentedSequences() + "\t");
 		logWriter.write( (querySequence.getNumRepresentedSequences()  
 		+  kmerResult.getProbSeq().getNumRepresentedSequences() ) + "\t" );
 		logWriter.write( possibleAlignment.getAverageDistance() + "\t");
 		logWriter.write( possibleAlignment.getAlignmentScoreAveragedByCol() + "\t");
 		float kMerDistance = kmerResult.getCounts() / ((float)querySequence.getConsensusUngapped().length());
 		logWriter.write( kMerDistance + "\n" );
 		logWriter.flush();
 	}
 	
 	public enum MODE { SISTER_MERGE, PARENT_MERGE, BOTTOM_LEVEL}
 	
 	/*
 	 * 
 	 * As a side effect, all seqs are removed from seqsToCluster
 	 */
 	public static void clusterAtLevel( List<ENode> alreadyClustered,
 					List<ENode> seqstoCluster, 
 								float levelToCluster, float stopSearchThreshold, 
 								String runID, MODE daughterRefOption) throws Exception
 	{
 		System.out.println("STARTING");
 		BufferedWriter logWriter = null;
 		
 		if( LOG)
 		{
 			logWriter= new BufferedWriter( new FileWriter(new File(ConfigReader.getETreeTestDir() + 
 					File.separator + "log_" + runID + "_"+  System.currentTimeMillis() + ".txt")));
 			
 			if( CALCULATE_CANNONICAL_NW)
 				logWriter.write("cannonicalNW\t");
 			
 			logWriter.write("alignmentPerformed\talignmentInSeries\tnumberOfQuerySequences\tkmersInCommon\tnumSeqsQuery\tnumSeqsPossibleTarget\t" + 
 					"totalNumSequences\tprobAlignmentDistnace\taverageAlignmentScore\tkmerDistance\n");
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
 			ENode querySeq = seqstoCluster.remove(0);
 			
 			List<KmerQueryResultForProbSeq> targets = 
 					db.queryDatabase(querySeq.getProbSequence().getConsensusUngapped());
 			
 			List<KmerQueryResultForProbSeq> matchingList =new ArrayList<KmerQueryResultForProbSeq>();
 			int targetIndex =0;
 			boolean keepGoing =true;
 			int mostNumSequences = -1;
 			
 			while( targetIndex < targets.size() && keepGoing)
 			{
 				KmerQueryResultForProbSeq possibleMatch = targets.get(targetIndex);
 				
 				// don't need to run the alignment if the possible match has fewer sequences than best hit
 				if( possibleMatch.getProbSeq().getNumRepresentedSequences() > mostNumSequences)
 				{
 					ProbSequence possibleAlignment = 
 							ProbNW.align(querySeq.getProbSequence(), possibleMatch.getProbSeq());
 				
 					if(LOG)
 						writeToLog(numAlignmentsPerformed, targetIndex, originalQuerySize-seqstoCluster.size(), 
 							logWriter, possibleAlignment, querySeq.getProbSequence(), possibleMatch);
 				
 					double distance =possibleAlignment.getAverageDistance();		
 				
 					if(  distance <= levelToCluster)
 					{
 						matchingList.add(possibleMatch);
 						possibleMatch.setAlignSeq(possibleAlignment);
 						mostNumSequences = Math.max(
 						possibleMatch.getProbSeq().getNumRepresentedSequences(), mostNumSequences);
 					}
 					else if (distance >= stopSearchThreshold)
 					{
 						keepGoing = false;
 					}
 				}
 			
 				targetIndex++;	
 			}
 			
 			ENode newNode = null;
 			if( matchingList.size() >= 1)
 			{
 				Collections.sort(matchingList, new KmerQueryResultForProbSeq.SortByNumSequences());
 				ENode targetNode = matchingList.get(0).getEnode();
 				matchingList.get(0).getAlignSeq().setMapCount(
 						targetNode.getProbSequence(), querySeq.getProbSequence());
 				targetNode.setProbSequence(matchingList.get(0).getAlignSeq());
 				// pick up any new kmers that we migtht have acquired
 				db.addSequenceToDatabase(targetNode);
 				
 				newNode = targetNode;
 									
 			}
 			else
 			{
 				newNode = new ENode(ProbSequence.makeDeepCopy(querySeq.getProbSequence()), 
 						querySeq.getNodeName() +"_" + levelToCluster, 
 						levelToCluster, null);
 				
 				alreadyClustered.add(newNode);
 				db.addSequenceToDatabase(newNode);
 			}	
 			
 			if( daughterRefOption == MODE.SISTER_MERGE)
 			{
				newNode.setDaughters( new ArrayList<ENode>( querySeq.getDaughters()));
 			} 
 			else if ( daughterRefOption == MODE.PARENT_MERGE)
 			{
 				newNode.getDaughters().add(querySeq);
 				querySeq.setParent(newNode);
 			}
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
 	
 	public static List<ENode> getInitialSequencesFromFasta(String fastaPath, String sampleId,
 			float threshold, float stopThreshold, String runID) throws Exception
 	{
 		int index=1;
 		List<ENode> probSeqs = new ArrayList<ENode>();
 		
 		FastaSequenceOneAtATime fsoat = new FastaSequenceOneAtATime(fastaPath);
 	
 		int expectedSum =0;
 		for(FastaSequence fs = fsoat.getNextSequence(); fs != null; fs = fsoat.getNextSequence())
 			if( fs.isOnlyACGT())
 			{
 				expectedSum += getNumberOfDereplicatedSequences(fs);
 				ProbSequence ps = new ProbSequence(fs.getSequence(), getNumberOfDereplicatedSequences(fs),sampleId);
 				ENode eNode = new ENode(ps, runID + "_" + index, threshold, null);
 				probSeqs.add(eNode);
 			}
 		
 		List<ENode> clustered = new ArrayList<ENode>();
 		
 		clusterAtLevel(clustered, probSeqs, threshold, stopThreshold, runID, MODE.BOTTOM_LEVEL);
 		
 		int numClustered = 0;
 		for(ENode ps : clustered)
 		{
 			//System.out.println(ps);
 			numClustered += ps.getProbSequence().getNumRepresentedSequences();
 		}
 			
 		if( numClustered != expectedSum )
 			throw new Exception("Finished with " + clustered.size()  + " clusters with " + numClustered + " sequences");
 		
 		return clustered;
 	}
 }
