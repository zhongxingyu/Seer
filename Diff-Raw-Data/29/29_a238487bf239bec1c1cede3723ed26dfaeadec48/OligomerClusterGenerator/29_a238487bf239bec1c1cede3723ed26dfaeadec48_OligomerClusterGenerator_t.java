 /*
  * Acacia - GS-FLX & Titanium read error-correction and de-replication software.
  * Copyright (C) <2011>  <Lauren Bragg and Glenn Stone - CSIRO CMIS & University of Queensland>
  * 
  * 	This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pyromaniac.Algorithm;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 
 import pyromaniac.AcaciaConstants;
 import pyromaniac.DataStructures.MutableInteger;
 import pyromaniac.DataStructures.Pair;
 import pyromaniac.DataStructures.Pyrotag;
 import pyromaniac.IO.AcaciaLogger;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class OligomerClustering.
  */
 public class OligomerClusterGenerator extends ClusterGenerator 
 {
 		
 		/** The hc. */
		private ManhattanClustering hc;
 	
 		/**
 		 * Instantiates a new oligomer clustering.
 		 */
 		public OligomerClusterGenerator ()
 		{
 			super();
 			hc = null;
 		}
 
 			
 		
 	
 	/* (non-Javadoc)
 	 * @see pyromaniac.Algorithm.Clustomatic#runClustering()
 	 */
 	@Override
 	public void runClustering() throws Exception
 	{
 		HashMap <String, int [] > hexReps = new HashMap <String, int [] >();
 		HashMap <String, Integer> hexToPos = new HashMap <String, Integer>();
 		MutableInteger index = new MutableInteger(0); 
 		
 		LinkedList <Pair <String, String>> mergeThisWithThat = new LinkedList <Pair <String, String>>();
 		
		Integer maximumManhattan = Integer.parseInt(settings.get(AcaciaConstants.OPT_MAXIMUM_MANHATTAN_DIST));
 		
 		int prefixLength = -1;
 		
 		if(initialClusters.size() == 0)
 		{
 			throw new Exception("Perfect clusters is empty!");
 		}
 		
 		for(String seq: initialClusters.keySet())
 		{
 			if(prefixLength == -1)
 				prefixLength = seq.length(); //was determined in the perfect clusters original case.
 			hexReps.put(seq, getHex(seq, hexToPos, index));
 		} //initialises the hexamer representations of each 'perfect identity representative'
 	
 		LinkedList <String> freqSortedList = sortedBasedOnFreq(initialClusters);
 		
 
 		boolean verbose = false;
 		
 		if(verbose)
 		{
 			for(String s: freqSortedList)
 			{
 				logger.writeLog("Sorted on size: " + initialClusters.get(s).size() + ", rep: " + s, AcaciaLogger.LOG_DEBUG);
 			}
 		}
 		
 		
 		//all things for tracking whilst using an iterator.
 
 		String toProcess = null;
 		boolean done = false;
 		
 		
 		//DONE means I have worked my way up the list based on abundance, and 
 		//identified the best match for each sequence further up in the list
 		
 		while(!done)
 		{
 			Iterator <String> it = freqSortedList.iterator(); //have to keep going through the list, and don't have a pointer to the last compared :/
 			
 			int ctr = 0;
 			//boolean seen = false;
 			boolean processed = true; //assume it is processed
 			
 			int bestMatch = -1;
 			int bestDist = 100000; //arbitrarily large distance
 			int bestClusterSize = 0;
 
 			//to process is the first in the list, assuming it is low abundance.
 			if(it.hasNext() && toProcess == null)
 			{
 				toProcess = it.next(); //first element is the first to Process
 				ctr++;
 				processed = false; //have not processed the prefix 'processing'.
 				//seen = true;
 			}
 			
 			//this seems to only happen if there is only one rep to begin with, rare!
 			if(!it.hasNext())
 			{
 				done = true;
 			}
 			
 			while(it.hasNext())
 			{
 				String curr = it.next(); //get the next element
 				
 				if(verbose)
 				{
 					logger.writeLog("toProcess: " + toProcess + " with " + initialClusters.get(toProcess).size(),AcaciaLogger.LOG_DEBUG);
 					logger.writeLog("Current prefix is: " + curr + ", consisting of " + initialClusters.get(curr).size() + "tags!", AcaciaLogger.LOG_DEBUG);
 				}
 				
 				if(processed && curr == toProcess)//working back through the iterator, start at the next spot!
 				{
 					if(verbose)
 					{
 						System.out.println("Curr == lastProcessed");
 					}
 					
 					if(it.hasNext()) //process them!
 					{
 						if(verbose)
 						{
 							System.out.println("There are more to process");
 						}
 						ctr++;
 						toProcess = it.next();
 						processed = false;				
 						
 						if(!it.hasNext())
 						{
 							done = true;
 						}
 					}
 					else
 					{
 						if(verbose)
 						{
 							System.out.println("We are done");
 						}
 						done = true;
 					}
 				}
 				else if(!processed)
 				{
 					//we are going to process "curr"
 					int [] hexToProcess = hexReps.get(toProcess);
 					int [] hexCurr = hexReps.get(curr);
 					
 					
 					int dist = calculateHexDist(hexToProcess, hexCurr);
 					
 
 					if(verbose)
 					{
 						logger.writeLog("curr: " + curr + " with " + initialClusters.get(curr).size(),AcaciaLogger.LOG_DEBUG);
 						logger.writeLog("toProcess: " + toProcess + " with " + initialClusters.get(toProcess).size(),AcaciaLogger.LOG_DEBUG);
 						logger.writeLog("dist: " + dist, AcaciaLogger.LOG_DEBUG);
 						
 						LinkedList <Pyrotag> currCluster = initialClusters.get(curr);
 						LinkedList <Pyrotag> processCluster = initialClusters.get(toProcess);
 						
 						if(currCluster.size() == 1)
 						{
 							logger.writeLog("Curr contains: " + currCluster.getFirst().getID(), AcaciaLogger.LOG_DEBUG);
 							logger.writeLog("Process contains: " + processCluster.getFirst().getID(), AcaciaLogger.LOG_DEBUG);
 						}
 						
 					}
 					
 					//better to merge with the biggest cluster
					if(dist <= bestDist && dist <= maximumManhattan && bestClusterSize < initialClusters.get(curr).size())
 					{
 						if(verbose)
 						{
 							logger.writeLog("Dist is < bestDist: " + dist + " < " + bestDist,AcaciaLogger.LOG_DEBUG);
 						}
 						
 						bestDist = dist;
 						bestMatch = ctr;
 						bestClusterSize = initialClusters.get(curr).size();
 					}
 				}
 				ctr++;
 			}
 			
			if(bestDist < maximumManhattan)
 			{
 				if(verbose)
 				{
					logger.writeLog("bestDist less than max manhattan: " + bestDist, AcaciaLogger.LOG_DEBUG);
 				}
 				mergeThisWithThat.add(new Pair <String, String> (toProcess, freqSortedList.get(bestMatch)));
 			}
 			//adding this
 			processed = true;
 		}		
 		
 		//merge this with that - does not seem to be used.
 		for(Pair <String, String> toMerge: mergeThisWithThat)
 		{
 			String smallClusterRep = toMerge.getFirst();
 			String bigClusterRep = toMerge.getSecond();
 			
 			if(bigClusterRep == null)
 			{
 				throw new Exception("Cluster representative is not defined for bigger cluster.");
 			}
 			
 			LinkedList <Pyrotag> bigCluster =  initialClusters.get(bigClusterRep);
 			LinkedList <Pyrotag> smallCluster = initialClusters.get(smallClusterRep);
 				
 			bigCluster.addAll(smallCluster);
 			initialClusters.remove(smallClusterRep);
 			
 		}
 		
 		//remembers the hexamer position, and the prefix length. Maybe it was for parallelisation?
		ManhattanClustering hc = new ManhattanClustering (hexToPos, index,prefixLength); //why do I keep this around? If I have to do multiple clusterings or something?
 		this.hc = hc;
 
 	}
 	
 	/**
	 * The Class ManhattanClustering.
 	 */
	private class ManhattanClustering
 	{
 		
 		/**
 		 * Gets the last index.
 		 *
 		 * @return the last index
 		 */
 		public MutableInteger getLastIndex() 
 		{
 			return lastIndex;
 		}
 
 		/**
 		 * Sets the last index.
 		 *
 		 * @param lastIndex the new last index
 		 */
 		public void setLastIndex(MutableInteger lastIndex) 
 		{
 			this.lastIndex = lastIndex;
 		}
 
 		/**
 		 * Gets the sub string length.
 		 *
 		 * @return the sub string length
 		 */
 		public int getSubStringLength() {
 			return subStringLength;
 		}
 
 		/**
 		 * Sets the sub string length.
 		 *
 		 * @param subStringLength the new sub string length
 		 */
 		public void setSubStringLength(int subStringLength) {
 			this.subStringLength = subStringLength;
 		}
 
 		/**
 		 * Gets the hexes.
 		 *
 		 * @return the hexes
 		 */
 		public HashMap<String, Integer> getHexes() {
 			return hexes;
 		}
 
 		/**
 		 * Sets the hexes.
 		 *
 		 * @param hexes the hexes
 		 */
 		public void setHexes(HashMap<String, Integer> hexes) {
 			this.hexes = hexes;
 		}
 
 		/** The last index. */
 		private MutableInteger lastIndex;
 		
 		/** The sub string length. */
 		private int subStringLength;
 		
 		/** The hexes. */
 		HashMap <String, Integer> hexes;
 		
 		/**
		 * Instantiates a new manhattan clustering.
 		 *
 		 * @param hexes the hexes
 		 * @param lastIndex the last index
 		 * @param subStringLength the sub string length
 		 */
		public ManhattanClustering(HashMap <String, Integer> hexes, MutableInteger lastIndex, int subStringLength)
 		{
 			this.hexes = hexes;
 			this.lastIndex = lastIndex;
 			this.subStringLength = subStringLength;
 		}
 	}
 	
 	/**
 	 * Calculate hex dist.
 	 *
 	 * @param hexToProcess the hex to process
 	 * @param hexCurr the hex curr
 	 * @return the int
 	 */
 	private int calculateHexDist(int[] hexToProcess, int[] hexCurr) 
 	{
 		int numEdits = 0;
 		for(int i = 0; i < hexToProcess.length; i++)
 		{
 			numEdits += Math.abs(hexToProcess[i] - hexCurr[i]);
 		}
 		return numEdits;
 	}
 	
 	/**
 	 * Gets the hex.
 	 *
 	 * @param sequence the sequence
 	 * @param hexToPos the hex to pos
 	 * @param index the index
 	 * @return the hex
 	 */
 	private int [] getHex(String sequence, HashMap <String, Integer> hexToPos, MutableInteger index)
 	{
 		double numHex = Math.pow(4,AcaciaConstants.CLUSTERING_OLIGO_LENGTH);
 		int [] hexCounts = new int [(int)numHex];
 		
 		for(int i = 0; i <numHex; i++)
 		{
 			hexCounts[i] = 0;
 		}
 		
 		for(int i = 0; (sequence.length() - i) >= AcaciaConstants.CLUSTERING_OLIGO_LENGTH ; i++)
 		{
 			String hexamer = sequence.substring(i, i + AcaciaConstants.CLUSTERING_OLIGO_LENGTH);
 
 			if(!hexToPos.containsKey(hexamer))
 			{
 				hexToPos.put(hexamer, index.value());
 				index.increment();
 			}
 			
 			hexCounts[hexToPos.get(hexamer)] = hexCounts[hexToPos.get(hexamer)] + 1; 
 		}
 		
 		return hexCounts;
 	}
 
 	/**
 	 * Sorted based on frequency.
 	 *
 	 * @param perfectClusters the perfect clusters
 	 * @return the linked list
 	 */
 	private LinkedList <String> sortedBasedOnFreq(HashMap <String, LinkedList <Pyrotag>> perfectClusters)
 	{
 		Set <String> _set = perfectClusters.keySet();
 		
 		LinkedList <String> sortedBasedOnFreq = new LinkedList <String>();
 		
 		for(String s: _set)
 		{
 			int size = perfectClusters.get(s).size();
 			
 			int index = 0;
 			
 			Iterator <String> it = sortedBasedOnFreq.iterator();
 			
 			//insert at index.
 			while(it.hasNext())
 			{
 				String next = it.next();
 				
 				if(perfectClusters.get(next).size() > size)
 				{
 					break;
 				}
 				else
 				{
 					index++;
 				}
 			}
 			
 			sortedBasedOnFreq.add(index, s);
 		}
 		return sortedBasedOnFreq;
 	}
 }
