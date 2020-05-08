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
 
 package pyromaniac;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import pyromaniac.IO.AcaciaLogger;
 import pyromaniac.IO.LogFileHandle;
 import pyromaniac.IO.LoggerOutput;
 import pyromaniac.IO.MIDReader;
 import pyromaniac.IO.MMFastaImporter;
 import pyromaniac.IO.MMFastqImporter;
 import pyromaniac.IO.StandardOutputHandle;
 import pyromaniac.IO.TagImporter;
 import pyromaniac.Algorithm.BinomialTest;
 import pyromaniac.Algorithm.ClusterGenerator;
 import pyromaniac.Algorithm.CoarseAlignSplitter;
 import pyromaniac.Algorithm.ConsensusGeneratorLocalTests;
 import pyromaniac.Algorithm.ConsensusGeneratorLocalTests.UnprocessedPyrotagResult;
 import pyromaniac.Algorithm.HypothesisTest;
 import pyromaniac.Algorithm.IonTorrentOUCallFrequencyTable;
 import pyromaniac.Algorithm.BalzerOUCallFrequencyTable;
 import pyromaniac.Algorithm.MultinomialOneSidedTest;
 import pyromaniac.Algorithm.OUFrequencyTable;
 import pyromaniac.Algorithm.OligomerClusterGenerator;
 import pyromaniac.Algorithm.SimpleClusterAligner;
 import pyromaniac.Algorithm.RLEAlignmentIndelsOnly;
 import pyromaniac.Algorithm.RLEAlignmentIndelsOnly.AlignmentColumn;
 import pyromaniac.DataStructures.FlowCycler;
 import pyromaniac.DataStructures.MIDPrimerCombo;
 import pyromaniac.DataStructures.Pair;
 import pyromaniac.DataStructures.PatriciaTrie;
 import pyromaniac.DataStructures.Pyrotag;
 import pyromaniac.DataStructures.MutableInteger;
 import pyromaniac.DataStructures.Triplet;
 
 // TODO: Auto-generated Javadoc
 /**
  * This Singleton class runs the series of processes required to sort, MID-split, filter, trim and correct reads. 
  */
 public class AcaciaEngine 
 {
 	//immutable.
 	/** The setting keys. */
 	private final String [] settingKeys;
 	
 	/** The setting values. */
 	private final String [] settingValues; 
 	
 	/** The Constant BUILD_PROPERTIES_FILE. */
 	private static final String BUILD_PROPERTIES_FILE = "/data/build_info.properties";
 	
 	/** The Constant BUILD_MAJOR_KEY. */
 	private static final String BUILD_MAJOR_KEY = "build.major.number";
 	
 	/** The Constant BUILD_MINOR_KEY. */
 	private static final String BUILD_MINOR_KEY = "build.minor.number";
 	
 	/** The Constant BUILD_REVISION_KEY. */
 	private static final String BUILD_REVISION_KEY = "build.number";
 
 	/**
 	 * Gets the setting keys.
 	 *
 	 * @return the setting keys
 	 */
 	public  String[] getSettingKeys() 
 	{
 		return settingKeys;
 	}
 	
 	// Private constructor prevents instantiation from other classes
 	/**
 	 * Instantiates a new acacia engine.
 	 */
 	private AcaciaEngine() 
 	{
 		String [] tmpSettingKeys = 
 		{
 				AcaciaConstants.OPT_FASTA, 
 				AcaciaConstants.OPT_FASTA_LOC, 
 				AcaciaConstants.OPT_QUAL_LOC, 
 				AcaciaConstants.OPT_FASTQ,
 				AcaciaConstants.OPT_FASTQ_LOC, 
 				AcaciaConstants.OPT_MID, 
 				AcaciaConstants.OPT_MID_FILE, 
 				AcaciaConstants.OPT_TRIM_TO_LENGTH, 
 				AcaciaConstants.OPT_OUTPUT_PREFIX,
 				AcaciaConstants.OPT_OUTPUT_DIR, 
 				AcaciaConstants.OPT_MAXIMUM_MANHATTAN_DIST, 
 				AcaciaConstants.OPT_SIGNIFICANCE_LEVEL,
 				AcaciaConstants.OPT_REPRESENTATIVE_SEQ,
 				AcaciaConstants.OPT_MIN_AVG_QUALITY, 
 				AcaciaConstants.OPT_SPLIT_ON_MID, 
 				AcaciaConstants.OPT_MAX_STD_DEV_LENGTH,  //
 				AcaciaConstants.OPT_ERROR_MODEL, 
 				AcaciaConstants.OPT_FLOW_KEY,
 				AcaciaConstants.OPT_MAX_RECURSE_DEPTH,
 				AcaciaConstants.OPT_TRUNCATE_READ_TO_FLOW,
 				AcaciaConstants.OPT_MIN_READ_REP_BEFORE_TRUNCATION,
 				AcaciaConstants.OPT_MIN_FLOW_TRUNCATION, 
 				AcaciaConstants.OPT_FILTER_READS_WITH_N_BEFORE_POS,
 				AcaciaConstants.OPT_SIGNIFICANT_WHEN_TWO,
 				AcaciaConstants.OPT_FLOW_CYCLE_STRING,
 				AcaciaConstants.OPT_MAX_COMPLETE_LINKAGE_DIST,
 				AcaciaConstants.OPT_HEX_CLUSTER_ONLY,
 				AcaciaConstants.OPT_CLEAN_DATA
 		};
 		
 		String [] tmpSettingValues = 
 		{
 				AcaciaConstants.DEFAULT_OPT_FASTA, 
 				AcaciaConstants.DEFAULT_FILE_LOC, 
 				AcaciaConstants.DEFAULT_FILE_LOC,
 				AcaciaConstants.DEFAULT_OPT_FASTQ, 
 				AcaciaConstants.DEFAULT_FILE_LOC, 
 				AcaciaConstants.DEFAULT_OPT_MID,
 				AcaciaConstants.DEFAULT_OPT_MID_LOC, 
 				AcaciaConstants.DEFAULT_OPT_TRIM_LENGTH,
 				AcaciaConstants.DEFAULT_OPT_OUTPUT_PREFIX, 
 				AcaciaConstants.DEFAULT_FILE_LOC,
 				AcaciaConstants.DEFAULT_OPT_MAXIMUM_MANHATTAN_DIST, 
 				AcaciaConstants.DEFAULT_OPT_SIGNIFICANCE_LEVEL,
 				AcaciaConstants.DEFAULT_OPT_REPRESENTATIVE_SEQ, 
 				AcaciaConstants.DEFAULT_OPT_MIN_AVG_QUALITY, 
 				AcaciaConstants.DEFAULT_OPT_SPLIT_ON_MID, 
 				AcaciaConstants.DEFAULT_OPT_MAX_STD_DEV_LENGTH, //
 				AcaciaConstants.DEFAULT_OPT_ERROR_MODEL, 
 				AcaciaConstants.DEFAULT_OPT_FLOW_KEY,
 				AcaciaConstants.DEFAULT_OPT_MAX_RECURSE_DEPTH,
 				AcaciaConstants.DEFAULT_OPT_TRUNCATE_READ_TO_FLOW,
 				AcaciaConstants.DEFAULT_OPT_MIN_READ_REP_BEFORE_TRUNCATION, 
 				AcaciaConstants.DEFAULT_OPT_MIN_FLOW_TRUNCATION,
 				AcaciaConstants.DEFAULT_FILTER_N_BEFORE_POS,
 				AcaciaConstants.DEFAULT_OPT_SIGNIFICANT_WHEN_TWO,
 				AcaciaConstants.DEFAULT_OPT_FLOW_CYCLE_STRING,
 				AcaciaConstants.DEFAULT_OPT_MAX_COMPLETE_LINKAGE_DIST,
 				AcaciaConstants.DEFAULT_OPT_HEX_CLUSTER_ONLY,
 				AcaciaConstants.DEFAULT_OPT_CLEAN_DATA
 		};
 		
 		settingKeys = tmpSettingKeys;
 		settingValues = tmpSettingValues;
 		
 	}
 
 	//singleton pattern
 	/**
 	 * Gets the singleton AcaciaEngine object.
 	 *
 	 * @return the AcaciaEngine object
 	 */
 	public static AcaciaEngine getEngine()
 	{
 		return AcaciaUtilityHolder.getInstance();
 	}
 	
 	//flow hash keeps a record of where each sequence is up to in the flow
 	//it is initially constructed during the _alignSequence phase, and marks the 'estimated' first flow position after the MID.
 	/**
 	 * Clone flow hash.
 	 *
 	 * @param toClone the flow hash to deep clone
 	 * @return a deep-cloned copy of the toClone hash-map
 	 */
 	public HashMap <Pyrotag, Pair <Integer, Character>> cloneFlowHash(HashMap <Pyrotag, Pair <Integer, Character>> toClone)
 	{
 		HashMap <Pyrotag, Pair <Integer, Character>> clone = new HashMap <Pyrotag, Pair <Integer, Character>>();
 		
 		for(Pyrotag p: toClone.keySet())
 		{
 			Pair <Integer, Character> oldPair = toClone.get(p);
 			Pair <Integer, Character> newPair = new Pair <Integer, Character>(oldPair.getFirst(), oldPair.getSecond());
 			clone.put(p,newPair);
 		}
 		return clone;
 	}
 	
 	//initialises the output files. These consist of a stats file, a sequence output file, a reference output file, and a mapping file, which
 	//describes which sequences are represented by which reference sequence.
 	/**
 	 * Initialises the output files (STATS, REFOUT, SEQOUT, MAPOUT, HISTOUT.
 	 *
 	 * @param settings the Acacia runtime settings
 	 * @param mid the MID associated with these output files
 	 * @return HashMap containing the key (output type), value (file handle) pairs
 	 * @throws IOException Signal that an IO exception occurred in output file initialisation
 	 */
 	private HashMap <String, BufferedWriter> initOutputFiles(HashMap <String, String> settings, MIDPrimerCombo mid) throws IOException
 	{
 		String outDir = settings.get(AcaciaConstants.OPT_OUTPUT_DIR);
 		String outputPrefix = settings.get(AcaciaConstants.OPT_OUTPUT_PREFIX);
 		
 		String midStr = (mid != null)? mid.getDescriptor() : "mid_unspecified";
 		
 		String statOut = outDir + getPlatformSpecificPathDivider() + outputPrefix + "_" + midStr + "." + AcaciaConstants.STATS_SUFFIX;
 		String seqOut = outDir + getPlatformSpecificPathDivider()+ outputPrefix +   "_" + midStr + "." + AcaciaConstants.SEQOUT_SUFFIX;
 		String refOut = outDir + getPlatformSpecificPathDivider() + outputPrefix +  "_" + midStr + "." + AcaciaConstants.REFOUT_SUFFIX;
 		String mapOut = outDir + getPlatformSpecificPathDivider() + outputPrefix +  "_" + midStr + "." + AcaciaConstants.MAPOUT_SUFFIX;
 		String histOut = outDir + getPlatformSpecificPathDivider() + outputPrefix + "_" + midStr + "." + AcaciaConstants.HISTOUT_SUFFIX;
 		
 		BufferedWriter statOutWriter = new BufferedWriter(new FileWriter(new File(statOut), false));
 		BufferedWriter seqOutWriter = new BufferedWriter(new FileWriter(new File (seqOut), false));
 		BufferedWriter refOutWriter = new BufferedWriter (new FileWriter (new File (refOut), false));
 		BufferedWriter mapOutWriter = new BufferedWriter (new FileWriter (new File (mapOut), false));
 		BufferedWriter histOutWriter = new BufferedWriter (new FileWriter (new File(histOut), false));
 		
 		HashMap <String, BufferedWriter> outputHandles = new HashMap <String, BufferedWriter>();
 		outputHandles.put(AcaciaConstants.STAT_OUT_FILE, statOutWriter);
 		outputHandles.put(AcaciaConstants.SEQ_OUT_FILE, seqOutWriter);
 		outputHandles.put(AcaciaConstants.REF_OUT_FILE, refOutWriter);
 		outputHandles.put(AcaciaConstants.MAP_OUT_FILE, mapOutWriter);
 		outputHandles.put(AcaciaConstants.HIST_OUT_FILE, histOutWriter);
 		return outputHandles;
 	}
 	
 	//close all the output handles, this does not include the DEBUG/PROGRESS/ERROR output files.
 	
 	/**
 	 * Closes the output file handles.
 	 *
 	 * @param outputHandles the output handles to close
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	private void closeOutputFiles(HashMap <String, BufferedWriter> outputHandles) throws IOException
 	{
 		for(String output : outputHandles.keySet())
 		{
 			outputHandles.get(output).close();
 		}
 	}
 	
 	//output singleton sequences.
 	/**
 	 * Processes singleton reads, writes them to output file.
 	 *
 	 * @param singleton the singleton pyrotag
 	 * @param settings the Acacia run settings
 	 * @param outputHandles the output handles for this dataset
 	 * @param representativeSeqs the collection of representative sequences so far
 	 * @param logger the runtime logger
 	 * @throws Exception Any exception that occurs during the processing and output of the singleton sequence
 	 */
 	private void processSingleton(Pyrotag singleton, HashMap <String, String> settings, HashMap <String, BufferedWriter> outputHandles,
 			HashMap<Pyrotag, Integer> representativeSeqs, AcaciaLogger logger) throws Exception
 	{
 		
 		this.outputSequence(settings, outputHandles.get(AcaciaConstants.SEQ_OUT_FILE), new String(singleton.getProcessedString()), singleton);
 		this.outputSequence(settings, outputHandles.get(AcaciaConstants.REF_OUT_FILE), new String(singleton.getProcessedString()), singleton);
 		
 		String id = this.getOutputID(settings, singleton, false);
 		String toPrint = id  + "\t" + id + System.getProperty("line.separator");
 		outputHandles.get(AcaciaConstants.MAP_OUT_FILE).write(toPrint);
 		representativeSeqs.put(singleton,1); //singleton representative
 	}
 	
 	
 
 	/**
 	 * Gets the parameter MIN_READ_REP_BEFORE_TRUNCATION.
 	 *
 	 * @param settings the Acacia runtime settings
 	 * @return Double the minimum read representation (coverage) before truncation of the consensus
 	 */
 	public Double getMinReadRepTruncation(HashMap<String, String> settings) 
 	{
 		if(settings.get(AcaciaConstants.OPT_MIN_READ_REP_BEFORE_TRUNCATION) != null)
 		{
 			return Double.parseDouble(settings.get(AcaciaConstants.OPT_MIN_READ_REP_BEFORE_TRUNCATION));
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Gets the significance threshold.
 	 *
 	 * @param string The threshold value (String) from the command line /configuration file
 	 * @return the double significance threshold
 	 */
 	public double parseSignificanceThreshold(String string) 
 	{
 		if(string.equals(AcaciaConstants.SIGN_THRESHOLD_ZERO))
 		{
 			return (double)0;
 		}
 		else
 		{
 			return Math.pow(10, Integer.parseInt(string));
 		}
 	}
 
 	
 
 	//generates perfect clusterings based on prefix of reads, and filters out reads that are not satisfying thresholds (also does trimming).
 	/**
 	 * Generates clusters based on perfect identity. Read trimming and filtering are also performed.
 	 *
 	 * @param settings the Acacia runtime settings
 	 * @param logger the Acacia logger
 	 * @param rc the RunCharacterisation object
 	 * @param midsToUse the MIDs to use
 	 * @param outputHandles the output handles for this run
 	 * @param representativeSeqs the representative sequences so far
 	 * @return HashMap containing the consensus prefix to read mapping, which describes the perfect identity clusters
 	 * @throws Exception any exception that occurred during the generation of the perfect identity clusters
 	 */
 	public 	HashMap <String, LinkedList <Pyrotag>> generatePerfectClusters(HashMap <String, String> settings, AcaciaLogger logger,
 			RunCharacterisation rc, LinkedList <MIDPrimerCombo> midsToUse, HashMap<String, BufferedWriter> outputHandles, HashMap<Pyrotag, Integer> representativeSeqs) throws Exception
 	{
 		HashMap <String, LinkedList <Pyrotag>> perfectClusters = new HashMap <String, LinkedList <Pyrotag>>();
 			
 		int usableSeqs = 0;
 
 		HashMap <String, Integer> dereplicated = new HashMap<String, Integer>();
 		
 		boolean verbose = false;
 		
 		//another outfile
 		LinkedList <MIDPrimerCombo> midsToProcess = midsToUse;
 				
 		if(midsToProcess.size() == 1 && midsToProcess.getFirst() == AcaciaConstants.NO_MID_GROUP)
 		{
 			midsToProcess = new LinkedList <MIDPrimerCombo>(rc.MIDToSequences.keySet());
 		}
 		
 		for(MIDPrimerCombo midPrimer: midsToProcess)
 		{
 			//there were no tags for that MID
 			if(!rc.MIDToSequences.containsKey(midPrimer))
 			{
 				continue;
 			}
 			
 			LinkedList <Pyrotag> seqs = rc.MIDToSequences.get(midPrimer);
 		
 			for(Pyrotag p: seqs)
 			{	
 							
 				//trim to first N.
 				//note that if the first N occurs straight after the MID primer, the read will have length zero.
 				int firstN = p.firstOccurrenceOfAmbiguous(); //
 				
 				if(firstN != Pyrotag.NO_N)
 				{
 					throw new Exception("Sequence containing N found in data: maybe you should run cleaning first.");
 				}
 				
 			
 					
 				//dont touch the other things.
 				p.setInternalID(usableSeqs + 1);	
 				usableSeqs++;
 						
 				char [] trimmedSequence = p.getProcessedString();
 			
 				if(! dereplicated.containsKey(new String(trimmedSequence)))
 				{
 					dereplicated.put(new String(trimmedSequence), 0);
 				}
 					
 				dereplicated.put(new String(trimmedSequence), dereplicated.get(new String(trimmedSequence) + 1));
 				
 				char [] trimmedCollapsed = Arrays.copyOf(p.getCollapsedRead(), AcaciaConstants.DEFAULT_OPT_TRIM_COLLAPSED);
 				
 				String rle = new String(trimmedCollapsed);
 				
 				if(!perfectClusters.containsKey(rle))
 				{
 					perfectClusters.put(rle, new LinkedList <Pyrotag>());
 				}
 				perfectClusters.get(rle).add(p);
 			}
 			
 			if(usableSeqs == 0)
 			{
 				logger.writeLog("MID: " + midPrimer.getDescriptor() + " had no reads", AcaciaLogger.LOG_PROGRESS);	
 			}
 		}
 		
 		if(usableSeqs == 0)
 		{
 			logger.writeLog("No sequences satisified all filters", AcaciaLogger.LOG_PROGRESS);
 			return null;
 		}
 		
 		return perfectClusters;
 	}
 	
 	
 	
 	/**
 	 * Run acacia.
 	 *
 	 * @param settings the Acacia runtime settings
 	 * @param validTags the acceptable MID/primer prefixes which are valid for processing
 	 * @param logger the Acacia logger
 	 * @param worker the ErrorCorrectionWorker thread - running this method
 	 * @param version the Acacia version
 	 * @throws Exception any exception during the runAcacia method
 	 */
 	public void runAcacia(HashMap <String, String> settings, LinkedList <MIDPrimerCombo> validTags,  AcaciaLogger logger, 
 			ErrorCorrectionWorker worker, String version, boolean runningFromGUI) throws Exception
 	{
 		
 		this.initLogFiles(settings, logger, runningFromGUI, validTags);
 		
 		if(worker != null && worker.isCancelled())
 		{
 			throw new InterruptedException("Job cancelled");
 		}
 		
 
 		if(worker != null && worker.isCancelled())
 		{
 			System.out.println("Failed 3");
 			throw new InterruptedException("Job cancelled");
 		}
 
 		try
 		{
 			logger.writeLog("Analysing file...", AcaciaLogger.LOG_PROGRESS);
 			
 			RunCharacterisation rc = null;
 			DataCleaner dc = DataCleaner.getDataCleaner();
 			rc = dc.initialiseRunCharacterisation(settings, logger, validTags);
 
 
 			//TODO: test this has  not buggered up.
 			// get stats of tags in file.
 			if(worker != null && worker.isCancelled())
 			{
 				System.out.println("Failed 4");
 				throw new InterruptedException("Job cancelled");
 			}
 
 			FlowCycler fc = new FlowCycler(settings.get(AcaciaConstants.OPT_FLOW_CYCLE_STRING), logger);
 			
 			//all tags to be processed must have a valid MID if mids were specified by the user.
 			LinkedList <MIDPrimerCombo> aggregateAs = new LinkedList <MIDPrimerCombo>();
 			//sets up the mids to use.
 			if(settings.get(AcaciaConstants.OPT_SPLIT_ON_MID).equals("FALSE"))
 			{
 				if(validTags.size() == 1)
 				{
 					aggregateAs.add(validTags.get(0));
 				}
 				else
 				{
 					aggregateAs.add(AcaciaConstants.NO_MID_GROUP);
 				}
 			}
 			else if(settings.get(AcaciaConstants.OPT_SPLIT_ON_MID).equals("TRUE"))
 			{
 				aggregateAs.addAll(validTags);
 			}
 			
 			for(MIDPrimerCombo m: aggregateAs)
 			{
 				LinkedList <MIDPrimerCombo> midsToProcess = new LinkedList <MIDPrimerCombo>();
 				midsToProcess.add(m);
 				
 				logger.writeLog("Processing MID " + m.getDescriptor() + ": " + rc.getTagCountForMIDs(midsToProcess) + " tags", AcaciaLogger.LOG_PROGRESS);
 				
 				HashMap <String, BufferedWriter> outputHandles = this.initOutputFiles(settings, m);
 				
 				if(settings.containsKey(AcaciaConstants.OPT_CLEAN_DATA) && Boolean.parseBoolean(settings.get(AcaciaConstants.OPT_CLEAN_DATA)))
 				{
 					dc.filterAndTrimReads(settings, logger, rc, validTags, outputHandles);
 				}
 				
 				HashMap <Pyrotag, Integer> representativeSeqs = new HashMap <Pyrotag, Integer> ();
 				
 				int numSeqsCorrected = 0;
 				HashMap <String, LinkedList <Pyrotag>> perfectClusters =  generatePerfectClusters(settings, logger, rc, midsToProcess,
 						outputHandles, representativeSeqs);
 
 				if(perfectClusters == null)
 				{
 					continue;
 				}
 				
 				if(aggregateAs.size() == 1)
 					rc = null; //no longer need it (save memory).
 
 				logger.writeLog("There are " + perfectClusters.size() + "RLE prefix clusters before hexamer recruiting", AcaciaLogger.LOG_PROGRESS);
 
 				ClusterGenerator clusterer = new OligomerClusterGenerator();
 
 				clusterer.initialise(perfectClusters, settings, logger, outputHandles);
 				clusterer.runClustering();
 				
 				logger.writeLog("There are " + perfectClusters.size() + " after hexamer recruiting", AcaciaLogger.LOG_PROGRESS);
 
 				//at this point, do not care about the relationship between clusters...
 				String hexClustOnly = settings.get(AcaciaConstants.OPT_HEX_CLUSTER_ONLY).toUpperCase();
 				
 				boolean outputClusterMemberships = Boolean.parseBoolean(hexClustOnly); 
 				int clusterID = 0;
 				
 				//do we need all those other output files... how do I never open them...
 				if(outputClusterMemberships)
 				{
 					logger.writeLog("Creating fasta files per sequence cluster...", AcaciaLogger.LOG_PROGRESS);
 					for(String clusterRep: perfectClusters.keySet())
 					{
 						String outDir = settings.get(AcaciaConstants.OPT_OUTPUT_DIR);
 						String outputPrefix = settings.get(AcaciaConstants.OPT_OUTPUT_PREFIX);
 						String midStr = (m != null)? m.getDescriptor() : "mid_unspecified";
 						String hexOut = outDir + getPlatformSpecificPathDivider() + outputPrefix + "_" + midStr + "_hexamer_cluster_" + clusterID +".fasta";
 						BufferedWriter hexOutBuff = new BufferedWriter (new FileWriter (new File(hexOut), false));
 						
 						logger.writeLog("Outputting cluster of size: " + perfectClusters.get(clusterRep).size(), AcaciaLogger.LOG_PROGRESS);
 						
 						
 						for(Pyrotag p: perfectClusters.get(clusterRep))
 						{
 							outputSequence(settings, hexOutBuff, new String(p.getProcessedString()), p);
 						}
 						hexOutBuff.close();
 						
 						clusterID++;
 					}
 				}
 				else
 				{
 					logger.writeLog("Performing error correction on clusters...", AcaciaLogger.LOG_PROGRESS);
 					for(String clusterRep: perfectClusters.keySet())
 					{	
 						LinkedList <Pyrotag> clusterMembers = perfectClusters.get(clusterRep);
 
 						//alignment for the cluster				
 						ArrayDeque <Pair <RLEAlignmentIndelsOnly, HashMap <Pyrotag, Pair <Integer, Character>>>>  mainAlignRes = 
 							new ArrayDeque <Pair <RLEAlignmentIndelsOnly, HashMap <Pyrotag, Pair <Integer, Character>>>>();
 
 						//maybe this adds the same sequence twice to singletons?
 						LinkedList <Pyrotag> singletons = new LinkedList <Pyrotag>();
 
 						//alignment only allows sequence to belong to one cluster.
 
 						if(perfectClusters.get(clusterRep).size() > 1)
 						{
 							logger.writeLog("Processing cluster of " + perfectClusters.get(clusterRep).size() + " reads...", AcaciaLogger.LOG_PROGRESS);	
 						}
 
 						//run the alignment algorithm, it will populate the results collections.
 						SimpleClusterAligner.getInstance().generateAlignments(logger, settings, clusterMembers, clusterRep, outputHandles, 
 								representativeSeqs, mainAlignRes, singletons);
 
 						while(mainAlignRes.size() > 0)
 						{
 							Pair <RLEAlignmentIndelsOnly, HashMap <Pyrotag, Pair <Integer, Character>>> alignRes = mainAlignRes.pop();
 							///can I pass in the alignment singletons here... to see if they will align using a substitution only aligner? 
 
 							//this was the way the consensus was generated in the original acacia
 							boolean originalConsensusGeneration = true;
 							
 							if(originalConsensusGeneration)
 							{
 								HashMap <Pyrotag, Pair <Integer,Character>> flowMapInner = cloneFlowHash(alignRes.getSecond());
 
 								numSeqsCorrected += generateConsensusAndOutput(logger, settings, outputHandles, alignRes.getFirst(), flowMapInner, representativeSeqs, fc);
 								
 							}
 							else //new way, which attempts to partition the alignment if there are obvious deviations.
 							{
 								//don't want to do the full tests when there are obvious, highly significant deviations.
 								HashSet <HashSet <Pyrotag>> splitFurther = CoarseAlignSplitter.getInstance().scanAlignmentForObviousDeviations(logger, settings, outputHandles, alignRes, fc);
 								
 								//System.out.println("After hierarchical clustering, we have " + splitFurther.size() + " sub-clusters");
 								LinkedList <RLEAlignmentIndelsOnly> result = alignRes.getFirst().splitNonConforming(splitFurther);
 								
 								for(RLEAlignmentIndelsOnly subalign: result)
 								{
 									HashMap <Pyrotag, Pair <Integer,Character>> flowMapInner = cloneFlowHash(alignRes.getSecond());
 									
 									numSeqsCorrected += generateConsensusAndOutput(logger, settings, outputHandles, subalign, flowMapInner, representativeSeqs, fc);
 								}
 							}
 
 						}
 
 						//ideally here... we have a set of consensus sequences, and a set of singletons...				
 						for(Pyrotag solo: singletons)
 						{
 							this.processSingleton(solo, settings, outputHandles, representativeSeqs, logger);
 						}
 
 						clusterMembers = null;
 						mainAlignRes = null;
 						singletons = null;
 
 					}
 
 
 				logger.writeLog("Outputting final statistics...", AcaciaLogger.LOG_PROGRESS);
 				
 				//do we want to keep all the representative seqs in memory... just for this? Could probably just store the ID for the pyrotag.
 				outputFinalStatsAndHist(numSeqsCorrected,settings,logger,outputHandles,representativeSeqs, version);
 				}
 				perfectClusters = null; //clean that up.
 			}
 		}
 		catch(OutOfMemoryError error)
 		{
 			logger.writeLog("Out of memory: " + Thread.currentThread().getName(), AcaciaLogger.LOG_DEBUG);
 			throw(error);
 		}
 		finally
 		{
 			logger.writeLog("Finished: " + Thread.currentThread().getName(), AcaciaLogger.LOG_PROGRESS);
 		}
 	}   
 
 
 
 	/**
 	 * Outputs the consensus sequences for all reads in the ThreadedAlignment.
 	 *
 	 * @param consensusClusterCollection the consensus cluster collection
 	 * @param motherAlignment the mother alignment
 	 * @param settings the Acacia settings
 	 * @param logger the Acacia logger
 	 * @param outputHandles the output handles for this run
 	 * @param representativeSeqs the representative sequences generated so far
 	 * @return the number of reads corrected in this cluster
 	 * @throws Exception any exceptions that occurred during this method
 	 */
 	
 	private int outputCluster
 	(
 			Collection <HashSet <Pyrotag>> consensusClusterCollection, 
 			RLEAlignmentIndelsOnly motherAlignment,
 			HashMap<String, String> settings, AcaciaLogger logger,
 			HashMap<String, BufferedWriter> outputHandles,
 			HashMap<Pyrotag, Integer> representativeSeqs
 	) throws Exception
 	{
 		int counter = 0;
 		int numSeqsCorrected = 0;
 		
 		for(HashSet <Pyrotag> clusterMembers: consensusClusterCollection)
 		{	
 			//hash containing all the corrected sequences
 			HashMap <Pyrotag, String> correctedTags = new HashMap<Pyrotag, String>();
 			
 			//its a singleton
 			if(clusterMembers.size() == 1)
 			{
 				//much cleaner
 				Pyrotag p = clusterMembers.iterator().next();		
 				this.processSingleton(p, settings, outputHandles, representativeSeqs, logger);
 				continue;
 			}
 			
 			//the longest consensus sequence, and which base in consensus the tag aligns up to.
 			Pair<String, HashMap<Pyrotag, MutableInteger>> longestConsensus = motherAlignment.getLongestConsensus(clusterMembers);
 			
 			for(Pyrotag p: longestConsensus.getSecond().keySet())
 			{
 				//get the longest consensus
 				String substr = longestConsensus.getFirst().substring(0, longestConsensus.getSecond().get(p).value());
 				
 				//if they are not identical... then the sequence was modified at least once
 				if(!substr.equals(new String(p.getProcessedString())))
 				{
 					numSeqsCorrected++;	
 				}
 					
 				//add p to the correct tags
 				correctedTags.put(p, substr);
 				
 				//somehow this sequence gets two cluster memberships.
 				//we output the corrected tag...
 				outputSequence(settings, outputHandles.get(AcaciaConstants.SEQ_OUT_FILE),substr, p);
 			}
 			
 			//afterwards, we identify who is the representative sequence for the corrected tags.
 			Pair <Pyrotag, String> representativeSeq = this.getRepresentativeSeq(correctedTags, settings);
 			
 			int clusterSize = correctedTags.size();
 			
 			if(representativeSeqs.containsKey(representativeSeq.getFirst()))
 			{
 				//we have one representative representing two clusters...				
 				clusterSize += representativeSeqs.get(representativeSeq.getFirst());
 			}
 		
 			representativeSeqs.put(representativeSeq.getFirst(), correctedTags.size());
 			
 			//we output the representative sequence in the reference file
 			if(clusterSize == correctedTags.size()) //we've outputted this sequence before..
 			{
 				outputSequence(settings, outputHandles.get(AcaciaConstants.REF_OUT_FILE), representativeSeq.getSecond(), representativeSeq.getFirst());
 			}
 			
 			String repID = this.getOutputID(settings, representativeSeq.getFirst(), false);
 			
 			//we output all the mappings...
 			for(Pyrotag p: longestConsensus.getSecond().keySet()) //its possible that one read can map to multiple clusters, but they wont be output multiple times.
 			{
 				 String memberID = this.getOutputID(settings,p, false);
 				 outputHandles.get(AcaciaConstants.MAP_OUT_FILE).write(repID + "\t" + memberID + System.getProperty("line.separator")); 
 			}
 			
 			counter++;
 		}
 		return numSeqsCorrected;
 	}
 	
 	/**
 	 * Output final stats and hist.
 	 *
 	 * @param numSeqsCorrected the num seqs corrected
 	 * @param settings the settings
 	 * @param logger the logger
 	 * @param outputHandles the output handles
 	 * @param representativeSeqs the representative seqs
 	 * @param version the version
 	 * @throws Exception the exception
 	 */
 	private void outputFinalStatsAndHist(int numSeqsCorrected,
 			HashMap<String, String> settings, AcaciaLogger logger,
 			HashMap<String, BufferedWriter> outputHandles,
 			HashMap<Pyrotag, Integer> representativeSeqs, String version) throws Exception
 	{
 		Comparator <Integer> c = new Comparator <Integer>(){
 
 			public int compare(Integer arg0, Integer arg1) 
 			{
 				if(arg0 > arg1)
 				{
 					return -1;
 				}
 				else if(arg1 > arg0)
 				{
 					return 1;
 				}
 				return 0;
 			}
 		};
 		
 		TreeMap <Integer, Integer> clusterSizeHist = new TreeMap <Integer, Integer>(c);
 		outputHandles.get(AcaciaConstants.HIST_OUT_FILE).write("CLUSTER_SIZE, FREQUENCY" + System.getProperty("line.separator"));
 		
 		for(Pyrotag rep: representativeSeqs.keySet())
 		{
 			int clusterSize = representativeSeqs.get(rep);
 			
 			if(! clusterSizeHist.containsKey(clusterSize))
 			{
 				clusterSizeHist.put(clusterSize, 1);
 			}
 			else
 			{
 				clusterSizeHist.put(clusterSize, clusterSizeHist.get(clusterSize) + 1);
 			}	
 		}
 
 		for(int clusterSize: clusterSizeHist.keySet())
 		{
 			if(clusterSize == 1)
 			{
 				outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("# Singletons: " + clusterSizeHist.get(clusterSize) + System.getProperty("line.separator"));
 			}
 			
 			outputHandles.get(AcaciaConstants.HIST_OUT_FILE).write(clusterSize + "," + clusterSizeHist.get(clusterSize)  + System.getProperty("line.separator"));
 		}
 
 		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("# Reference sequences: " + representativeSeqs.size() +System.getProperty("line.separator"));
 		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("# Sequences corrected: " + numSeqsCorrected + System.getProperty("line.separator"));
 		outputHandles.get(AcaciaConstants.STAT_OUT_FILE).write("Acacia version: " + version);
 		this.closeOutputFiles(outputHandles);
 	}
 	
 	/**
 	 * Process and correct alignments.
 	 *
 	 * @param logger the logger
 	 * @param settings the settings
 	 * @param outputHandles the output handles
 	 * @param motherAlign the mother align
 	 * @param motherFlow the mother flow
 	 * @param representativeSeqs the representative seqs
 	 * @return the linked list
 	 * @throws Exception the exception
 	 */
 	
 	//potentiall in this bit, a sequence is added to somewhere twice.
 	private int generateConsensusAndOutput
 	(
 			AcaciaLogger logger, HashMap<String, String> settings,
 			HashMap<String, BufferedWriter> outputHandles, 
 			RLEAlignmentIndelsOnly motherAlign,
 			HashMap<Pyrotag, Pair<Integer, Character>> motherFlow,
 			HashMap<Pyrotag, Integer> representativeSeqs,
 			FlowCycler fc
 			//, HashSet <Pyrotag> singletons //to be added later -- these are guys thrown out during the alignment process.
 			
 	) throws Exception
 	{
 
 		int numCorrected = 0; 
 		HashSet <Pyrotag> toProcess = motherAlign.getAllTags();	 //grabs the results of the last run	
 
 		
 		
 		
 		//running over ThreadedAlignment... always clone the flow hash, in preparation for error correction later
 		HashMap <Pyrotag, Pair <Integer,Character>> flowMap = cloneFlowHash(motherFlow); //this cloning operation is unavoidable unless huge changes made. 
 
 		LinkedList <HashSet <Pyrotag>> consensusClusters = new LinkedList<HashSet<Pyrotag>>();
 		//first is the consensus members, and the second is the other sets.
 		UnprocessedPyrotagResult failedFirst =
 			ConsensusGeneratorLocalTests.getInstance().generateConsensus(logger, settings, motherAlign, toProcess, consensusClusters, flowMap, fc, false);
 
 						
 		if(failedFirst != null && failedFirst.getTags().size() > 0)
 		{		
 			LinkedList <HashSet<Pyrotag>> clustersWhichVaryTogether = ConsensusGeneratorLocalTests.getInstance().generateClustersFromVaryTogether(logger, settings, consensusClusters, failedFirst, true);
 
 			for(HashSet <Pyrotag> nc : clustersWhichVaryTogether)
 			{	
 				//try generating a consensus branch....
 				HashMap <Pyrotag, Pair <Integer,Character>> flowMapInner = cloneFlowHash(motherFlow);
 
 				//passing true to vary identically makes sure every seequence is processed this time.
 				UnprocessedPyrotagResult failedSecond = ConsensusGeneratorLocalTests.getInstance().generateConsensus(logger, settings, motherAlign, nc, consensusClusters, flowMapInner, fc, false); 
 
 				//last resource is trie.
 				if(failedSecond != null && failedSecond.getTags().size() > 0)
 				{
 					PatriciaTrie trie = ConsensusGeneratorLocalTests.getInstance().generateTrieFromUnprocessedTags(logger, failedSecond.getTags());
 					HashSet <Pyrotag> recorded = new HashSet <Pyrotag>();
 					
 					LinkedList <HashSet <Pyrotag>> trieClusters = new LinkedList <HashSet <Pyrotag>>();
 					
 					//need to check that the trie is not maintaining reads in several clusters
 					for(HashSet <Pyrotag> set : trie)
 					{
 						HashSet <Pyrotag> toRemove = new HashSet <Pyrotag>();
 						for(Pyrotag inner: set)
 						{
 							if(recorded.contains(inner))
 							{
 									toRemove.add(inner);
 							}
 							recorded.add(inner);
 						}
 						set.removeAll(toRemove);
 						
 						if(set.size() > 0)
 						{
 							trieClusters.add(set);
 						}
 					}
 					numCorrected += this.outputCluster(trieClusters, motherAlign, settings, logger, outputHandles, representativeSeqs);
 				}	
 			}
 		}
 
 		numCorrected += this.outputCluster(consensusClusters, motherAlign, settings, logger, outputHandles, representativeSeqs);	
 		return numCorrected;
 	}
 
 	public static OUFrequencyTable getErrorModel(AcaciaLogger logger, HashMap <String, String> settings) throws Exception
 	{		
 		OUFrequencyTable table = null;
 		
 		String errModel = settings.get(AcaciaConstants.OPT_ERROR_MODEL); 
 		
 		if(errModel.equals(AcaciaConstants.OPT_FLOWSIM_ERROR_MODEL))
 		{ 
 			table = new BalzerOUCallFrequencyTable(AcaciaConstants.FLOWSIM_PROBS_LOCATION);
 		}
 		else if (errModel.equals(AcaciaConstants.OPT_ACACIA_TITANIUM_ERROR_MODEL))
 		{
 			//this model is still be evaluated
 			table = new BalzerOUCallFrequencyTable(AcaciaConstants.ACACIA_EMP_MODEL_TITANIUM_LOCATION);
 		}
 		else if(errModel.equals(AcaciaConstants.OPT_ACACIA_IT_OT_100bp_314_MODEL))
 		{
 			table = new IonTorrentOUCallFrequencyTable(settings, logger, AcaciaConstants.IONTORRENT_314_100bp_PROBS_LOCATION, 
 					AcaciaConstants.IONTORRENT_314_100bp_ZERO_COEF, AcaciaConstants.IONTORRENT_314_100bp_ONE_COEF, 
 					AcaciaConstants.IONTORRENT_314_100bp_OTHER_COEF);
 		}
 		else if(errModel.equals(AcaciaConstants.OPT_ACACIA_IT_OT_100bp_316_MODEL))
 		{
 			table = new IonTorrentOUCallFrequencyTable(settings, logger, AcaciaConstants.IONTORRENT_316_100bp_PROBS_LOCATION, 
 					AcaciaConstants.IONTORRENT_316_100bp_ZERO_COEF, AcaciaConstants.IONTORRENT_316_100bp_ONE_COEF,
 					AcaciaConstants.IONTORRENT_316_100bp_OTHER_COEF);
 		}
 		else if (errModel.equals(AcaciaConstants.OPT_ACACIA_IT_MAN_200bp_314_MODEL))
 		{
 			table = new IonTorrentOUCallFrequencyTable(settings, logger, AcaciaConstants.IONTORRENT_314_200bp_PROBS_LOCATION, 
 					AcaciaConstants.IONTORRENT_314_200bp_ZERO_COEF, AcaciaConstants.IONTORRENT_314_200bp_ONE_COEF, 
 					AcaciaConstants.IONTORRENT_314_200bp_OTHER_COEF);
 		}
 		else if(errModel.equals(AcaciaConstants.OPT_ACACIA_IT_MAN_200bp_316_MODEL))
 		{
 			table = new IonTorrentOUCallFrequencyTable(settings, logger, AcaciaConstants.IONTORRENT_316_200bp_PROBS_LOCATION,
 					AcaciaConstants.IONTORRENT_316_200bp_ZERO_COEF, AcaciaConstants.IONTORRENT_316_200bp_ONE_COEF, 
 					AcaciaConstants.IONTORRENT_316_200bp_OTHER_COEF);
 		}
 		else if(errModel.equals(AcaciaConstants.OPT_ACACIA_IT_OT_200bp_314))
 		{
 			table = new IonTorrentOUCallFrequencyTable(settings, logger, AcaciaConstants.IONTORRENT_314_200bpOneTouch_PROBS_LOCATION, 
 					AcaciaConstants.IONTORRENT_314_200bpOneTouch_ZERO_COEF, AcaciaConstants.IONTORRENT_314_200bpOneTouch_ONE_COEF, 
 					AcaciaConstants.IONTORRENT_314_200bpOneTouch_OTHER_COEF);
 		}
 		else if (errModel.equals(AcaciaConstants.OPT_ACACIA_IT_OT_200bp_316))
 		{
 			table = new IonTorrentOUCallFrequencyTable(settings, logger, AcaciaConstants.IONTORRENT_316_200bpOneTouch_PROBS_LOCATION, 
 					AcaciaConstants.IONTORRENT_316_200bpOneTouch_ZERO_COEF, AcaciaConstants.IONTORRENT_316_200bpOneTouch_ONE_COEF, 
 					AcaciaConstants.IONTORRENT_316_200bpOneTouch_OTHER_COEF);
 		}
 		else //quince
 		{
 			table = new pyromaniac.Algorithm.QuinceOUFrequencyTable(AcaciaConstants.PYRONOISE_PROBS_LOCATION);
 		}
 		return table;
 	}
 		
 	
 	//inspect the file, characterise the reads.
 	/**
 	 * Prepare file for clustering.
 	 *
 	 * @param logger the logger
 	 * @param settings the settings
 	 * @param validTags the valid tags
 	 * @return the run characterisation
 	 * @throws Exception the exception
 	 */
 	private RunCharacterisation prepareFileForClustering(AcaciaLogger logger, HashMap <String, String> settings,  LinkedList <MIDPrimerCombo> validTags) throws Exception
 	{
 		//this method needs to rename the sequences too.
 		
 		HashMap <MIDPrimerCombo, LinkedList <Pyrotag>> MIDToSequences = new HashMap <MIDPrimerCombo, LinkedList <Pyrotag> >();
 		HashMap <MIDPrimerCombo, Integer> MIDseqLength = new HashMap <MIDPrimerCombo, Integer>();
 		HashMap <MIDPrimerCombo, Integer> MIDcollapsedSeqLength = new HashMap <MIDPrimerCombo, Integer>();
 		HashMap <MIDPrimerCombo, Double> MIDqualities = new HashMap <MIDPrimerCombo, Double>();
 		
 		
 		if(validTags.size() == 0)
 			validTags.add(AcaciaConstants.NO_MID_GROUP);
 		
 		int fileIndex = 0;
 		
 		logger.writeLog("Preparing sequence importer", AcaciaLogger.LOG_PROGRESS);
 		TagImporter importer = this.getTagImporter(settings, logger);
 		
 		Pyrotag p = importer.getPyrotagAtIndex(fileIndex);
 		
 		//loading only sequences which have valid MID.
 		
 		int invalidMID = 0;
 		int validMID = 0;
 		
 		while (p != null) 
 		{
 	//		System.out.println("P is " + p.getID());
 	//		System.out.println("MID matching");
 			MIDPrimerCombo matching = p.whichMID(validTags);
 				
 			if(matching == null)
 			{
 				fileIndex++;
 				p = importer.getPyrotagAtIndex(fileIndex);
 				
 				//skipping over not matching MIDS.
 				
 				invalidMID++;
 				
 				continue;
 			}
 			
 			validMID++;
 			
 	//		System.out.println("Set MID matching");
 			p.setMIDPrimerCombo(matching); //may already be initialised?
 			
 			if(! MIDToSequences.containsKey(matching))
 			{
 				MIDToSequences.put(matching, new LinkedList <Pyrotag>());
 			}
 			MIDToSequences.get(matching).add(p);
 			
 			if(! MIDseqLength.containsKey(matching))
 			{
 				MIDseqLength.put(matching, 0);
 			}
 			
 			MIDseqLength.put(matching, MIDseqLength.get(matching) + p.getLength());
 			
 			if(p.getQualities() != null)
 			{
 				if(! MIDqualities.containsKey(matching))
 				{
 					MIDqualities.put(matching, 0.0);
 				}
 				
 				MIDqualities.put(matching, MIDqualities.get(matching) +  p.getUntrimmedAvgQuality());
 			}
 
 		//	System.out.println("Getting collapsed read");
 			
 			char [] collapsedReadMinusMid = p.getCollapsedRead();
 			
 			if(!MIDcollapsedSeqLength.containsKey(matching))
 			{
 				MIDcollapsedSeqLength.put(matching, 0);
 			}
 			
 			MIDcollapsedSeqLength.put(matching, MIDcollapsedSeqLength.get(matching) +  collapsedReadMinusMid.length);
 			
 			fileIndex++;
 			p = importer.getPyrotagAtIndex(fileIndex);
 		}	
 		
 		RunCharacterisation rc = new RunCharacterisation(MIDToSequences, MIDseqLength, MIDcollapsedSeqLength, MIDqualities, validMID, invalidMID);
 		return rc;
 	}
 
 
 	//get Tag Importer
 	/**
 	 * Gets the tag importer.
 	 *
 	 * @param settings the settings
 	 * @param logger the logger
 	 * @return the tag importer
 	 */
 	public TagImporter getTagImporter(HashMap <String, String> settings, AcaciaLogger logger) 
 	{
 		TagImporter importer;
 		
 		if (settings.get(AcaciaConstants.OPT_FASTA).equals("TRUE")) 
 		{
 			//System.out.println("Getting importer from " + settings.get(AcaciaConstants.OPT_FASTA_LOC));
 			String fastaFile = settings.get(AcaciaConstants.OPT_FASTA_LOC);
 			String qualFile = settings.get(AcaciaConstants.OPT_QUAL_LOC);
 			
 			String flowCycle = settings.get(AcaciaConstants.OPT_FLOW_CYCLE_STRING);
 			
 			importer = new MMFastaImporter(fastaFile, qualFile, flowCycle, logger);
 			return importer;
 		}
 		else if(settings.get(AcaciaConstants.OPT_FASTQ).equals("TRUE"))
 		{
 		//	System.out.println("Getting importer from " + settings.get(AcaciaConstants.OPT_FASTQ_LOC));
 			String fastqFile = settings.get(AcaciaConstants.OPT_FASTQ_LOC);
 			String flowCycle = settings.get(AcaciaConstants.OPT_FLOW_CYCLE_STRING);
 			
 			importer = new MMFastqImporter(fastqFile, flowCycle, logger);
 			return importer;
 		}
 		else
 		{
 		//	System.out.println("Get tag importer returns null");
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Checks if the trim length is defined.
 	 *
 	 * @param settings the Acacia runtime settings
 	 * @return true if the trim length is defined, false otherwise
 	 */
 	public boolean trimDefined(HashMap <String, String> settings) 
 	{
 		String sTrimLength = settings.get(AcaciaConstants.OPT_TRIM_TO_LENGTH);
 		if (sTrimLength != null && !sTrimLength.equals("NONE")) 
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Gets the trim length.
 	 *
 	 * @param settings the Acacia settings
 	 * @return the trim length
 	 * @throws NumberFormatException Any formatting exception that occurs while parsing the trim length parameter
 	 */
 	public int getTrim(HashMap <String, String> settings) throws NumberFormatException 
 	{
 		String sTrimLength = settings.get(AcaciaConstants.OPT_TRIM_TO_LENGTH);
 		int trimLength = 0;
 		if (sTrimLength != null && !sTrimLength.equals("")) 
 		{
 			trimLength = Integer.parseInt(sTrimLength);
 			return trimLength;
 		}
 		return -1; // fail
 	}
 
 
 	/**
 	 * Output sequence.
 	 *
 	 * @param settings the settings
 	 * @param out the out
 	 * @param corrected the corrected
 	 * @param original the original
 	 * @throws Exception the exception
 	 */
 	private void outputSequence(HashMap <String, String> settings, BufferedWriter out, String corrected, Pyrotag original) throws Exception
 	{
 		String outputID = getOutputID(settings, original, true);
 		
 		//they use split libraries first, which will already have a name of X_[0-9]
 		//either re-use the name from split_libaries, or create my own.
 		
 		//some MIDS were specified
 		out.write(">" + outputID + System.getProperty("line.separator"));
 		out.write(corrected + System.getProperty("line.separator"));
 	}
 	
 	/**
 	 * Gets the output id.
 	 *
 	 * @param settings the settings
 	 * @param p the p
 	 * @param withDesc the with desc
 	 * @return the output id
 	 */
 	private String getOutputID(HashMap <String, String> settings, Pyrotag p, boolean withDesc)
 	{
 		StringBuilder id = new StringBuilder();	
 		if(! settings.get(AcaciaConstants.OPT_MID).equals(AcaciaConstants.OPT_NO_MID) && p.getMultiplexTag() != AcaciaConstants.NO_MID_GROUP)
 		{
 			id.append(p.getMultiplexTag().getDescriptor());
 			id.append("_" + p.getInternalID());
 			
 			if(withDesc)
 			{
 				String newDesc = " " + p.getID() + " orig_bc=" + p.getMultiplexTag().getMID() + " new_bc=" + p.getMultiplexTag().getMID() + " bc_diffs=0";
 				id.append(newDesc);
 			}
 		}
 		else
 		{
 			//no mids were supplied, return with the original ID.
 			id.append(p.getID());
 			
 			if(withDesc)
 				id.append(" " + p.getDescription());
 		}			
 		
 		return id.toString();
 	}
 	
 	
 	// returns the ID of the sequence which should be used as the reference
 	/**
 	 * Gets the representative seq.
 	 *
 	 * @param tagsOfInterest tags to select a representative sequences from
 	 * @param settings the Acacia runtime settings
 	 * @return the representative sequence selected
 	 */
 	private Pair<Pyrotag, String> getRepresentativeSeq(
 			HashMap<Pyrotag, String> tagsOfInterest, HashMap <String, String> settings) 
 			{
 		String refOption = settings.get(AcaciaConstants.OPT_REPRESENTATIVE_SEQ);
 
 		HashMap<Integer, ArrayList<Pyrotag>> lengthToFreq = new HashMap<Integer, ArrayList<Pyrotag>>();
 
 		int max = -1;
 		Pyrotag maxP = null;
 
 		int min = 100;
 		Pyrotag minP = null;
 
 		int modeLength = -1;
 		int modeFreq = -1;
 
 		for (Pyrotag p : tagsOfInterest.keySet()) 
 		{
 			String corrected = tagsOfInterest.get(p);
 			int correctedLength = corrected.length();
 			if (!lengthToFreq.containsKey(correctedLength)) 
 			{
 				lengthToFreq.put(correctedLength, new ArrayList<Pyrotag>());
 			}
 
 			if (correctedLength < min) 
 			{
 				min = correctedLength;
 				minP = p;
 			}
 
 			if (correctedLength > max) 
 			{
 				max = correctedLength;
 				maxP = p;
 			}
 
 			ArrayList<Pyrotag> tags = lengthToFreq.get(correctedLength);
 			tags.add(p);
 
 			if (tags.size() > modeFreq) 
 			{
 				modeFreq = tags.size();
 				modeLength = correctedLength;
 			}
 		}
 
 		if (refOption.equals(AcaciaConstants.OPT_MODE_REPRESENTATIVE)) 
 		{
 
 			Pyrotag first = lengthToFreq.get(modeLength).get(0);
 			return new Pair<Pyrotag, String>(first,tagsOfInterest.get(first)); 
 		} 
 		else if (refOption.equals(AcaciaConstants.OPT_MAX_REPRESENTATIVE)) 
 		{
 			return new Pair<Pyrotag, String>(maxP, tagsOfInterest.get(maxP));
 		} 
 		else if (refOption.equals(AcaciaConstants.OPT_MIN_REPRESENTATIVE)) // assume its refOption MIN
 		{
 			return new Pair<Pyrotag, String>(minP, tagsOfInterest.get(minP));
 		}
 		else //default is median representative
 		{
 			return null;
 		}
 	}
 
 	/**
 	 * Generates all possible MID sequences (expands ambiguous nucleotides).
 	 *
 	 * @param midString the mid string to generate the explicit
 	 * @return String [] containing all the possible explicit sequences that are represented by the MID sequence
 	 */
 	public String [] allPossibleVariants(String midString)
 	{
 		//first check whether there is any non nucleotide.
 		StringBuilder [] prefixes = new StringBuilder [] {new StringBuilder()};
 		String ambiguousNucleotide =  "[^ATGC]";
 
 		// Compile and get a reference to a Pattern object.
 		Pattern pattern = Pattern.compile(ambiguousNucleotide);
 
 		// Get a matcher object - we cover this next.
 		Matcher matcher = pattern.matcher(midString);
 
 		int prevMatch = -1;
 
 		while(matcher.find())
 		{
 			int pos = matcher.start();
 			char ambiguous = midString.charAt(pos);
 			String gapSeq = midString.substring(prevMatch + 1, pos);
 			StringBuilder [] newPrefixes = new StringBuilder[prefixes.length * AcaciaConstants.IUPAC_AMBIGUOUS_MAPPINGS.get(ambiguous).length];	
 			int arrayPos = 0;
 
 			for(Character possVal: AcaciaConstants.IUPAC_AMBIGUOUS_MAPPINGS.get(ambiguous))
 			{
 				for(StringBuilder orig: prefixes) 
 				{
 					StringBuilder newBuilder =  new StringBuilder(orig.toString());
 					newBuilder.append(gapSeq);
 					newBuilder.append(possVal);
 					newPrefixes[arrayPos] = newBuilder;
 					arrayPos++;
 				}
 			}
 			prefixes = newPrefixes;
 			prevMatch = pos;
 		}
 
 		String [] result = new String [prefixes.length];
 		String lastBit = midString.substring(prevMatch + 1, midString.length());
 
 
 		for(int i = 0; i < prefixes.length; i++)
 		{
 			StringBuilder b = prefixes[i];
 			if(prevMatch != midString.length() - 1) 
 			{
 				b.append(lastBit);
 				result[i] = b.toString();
 			}
 			else
 			{
 				result[i] = b.toString(); 
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Gets the image url.
 	 *
 	 * @param fileName the image file to get a URL for 
 	 * @return the image url
 	 * @throws Exception Any exception that occurred while getting the URL
 	 */
 	public static URL getImageUrl(String fileName) throws Exception
 	{
 		// try to get the URL as a system resource
 		URL url = AcaciaUtilityHolder.getInstance().getClass().getResource(fileName);
 		if (url == null)
 		{
 			// try to get the URL directly from the filename
 				url = new URL("file:" + fileName);
 		}
 		return url;
 	}
 
 	/**
 	 * Inits the log files.
 	 *
 	 * @param settings the Acacia runtime settings
 	 * @param logger the Acacia logger
 	 * @param runningFromGUI whether Acacia is running from the GUI
 	 * @param validMIDS which MIDS to process during this run
 	 * @throws Exception any exception that occurs during initialisation of the log files
 	 */
 	public void initLogFiles(HashMap <String, String> settings, AcaciaLogger logger, boolean runningFromGUI, LinkedList<MIDPrimerCombo> validMIDS) throws Exception
 	{
 		String outDir = settings.get(AcaciaConstants.OPT_OUTPUT_DIR);
 
 		if(outDir.lastIndexOf(AcaciaEngine.getPlatformSpecificPathDivider()) != (outDir.length() - 1))
 		{
 			outDir = outDir + AcaciaEngine.getPlatformSpecificPathDivider();
 		}
 
 		String runPrefix = settings.get(AcaciaConstants.OPT_OUTPUT_PREFIX);
 		
 		//String stdOutLoc = outDir + AcaciaMain.STANDARD_OUT_NAME;
 		//String stdErrLoc = outDir + AcaciaMain.STANDARD_ERR_NAME;
 		//String stdDebugLoc = outDir + AcaciaMain.STANDARD_DEBUG_NAME;
 		
 		String stdOutLoc = outDir + runPrefix + "_" + AcaciaMain.STANDARD_OUT_SUFFIX;
 		String stdErrLoc = outDir + runPrefix + "_" + AcaciaMain.STANDARD_ERR_SUFFIX;
 		String stdDebugLoc = outDir + runPrefix + "_" + AcaciaMain.STANDARD_DEBUG_SUFFIX;
 		
 		String runSettingsLoc = outDir + runPrefix + ".config";
 		String runSettingsMIDLoc = outDir + runPrefix + ".selectedMIDS";
 		
 		BufferedWriter out = null, err = null, debug = null, config = null, midOut = null;
 
 
 		out = new BufferedWriter(new FileWriter(stdOutLoc));
 		err = new BufferedWriter(new FileWriter(stdErrLoc));
 		debug = new BufferedWriter(new FileWriter(stdDebugLoc));
 
 		//output the run settings as a record.
 		if(runningFromGUI)
 		{
 			config = new BufferedWriter(new FileWriter(runSettingsLoc));
 			midOut = new BufferedWriter(new FileWriter(runSettingsMIDLoc));
 			outputSettings(config, midOut, runSettingsMIDLoc, settings, validMIDS);
 		}
 
 		LogFileHandle stdOut = new LogFileHandle(out);
 		LogFileHandle stdErr = new LogFileHandle(err);
 		LogFileHandle stdDebug = new LogFileHandle(debug);
 		LoggerOutput console = new StandardOutputHandle(); 
 
 		logger.addOutput(console, AcaciaLogger.LOG_ALL);
 		logger.addOutput(stdOut, AcaciaLogger.LOG_PROGRESS);
 		logger.addOutput(stdErr, AcaciaLogger.LOG_ERROR);
 		logger.addOutput(stdDebug, AcaciaLogger.LOG_DEBUG);
 	}
 
 	/**
 	 * Output settings.
 	 *
 	 * @param config the config
 	 * @param selectedMIDs the selected mi ds
 	 * @param selectedMIDsFileName the selected mi ds file name
 	 * @param settings the settings
 	 * @param validMIDS the valid mids
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	private void outputSettings(BufferedWriter config, BufferedWriter selectedMIDs, String selectedMIDsFileName,
 			HashMap<String, String> settings, LinkedList<MIDPrimerCombo> validMIDS) throws IOException 
 	{
 		TreeSet<String> keys = new TreeSet<String>(settings.keySet());
 		
 		//need to sort alphabetically, sick of the inconsistency...
 		for(String setting: keys)
 		{
 			if(!(setting.equals(AcaciaConstants.OPT_MID) || setting.equals(AcaciaConstants.OPT_MID_FILE) || settings.get(setting) == null))
 			{
 				config.write(setting + AcaciaConstants.CONFIG_DELIMITER + settings.get(setting) + System.getProperty("line.separator"));
 			}
 		}
 		
 		
 		//now has a column for the primer sequence
 		if(validMIDS.size() == 0)
 		{
 			config.write(AcaciaConstants.OPT_MID + AcaciaConstants.CONFIG_DELIMITER + AcaciaConstants.OPT_NO_MID + System.getProperty("line.separator"));
			config.write(AcaciaConstants.OPT_MID_FILE + AcaciaConstants.CONFIG_DELIMITER + System.getProperty("line.separator"));
 		}
 		else
 		{
 			config.write(AcaciaConstants.OPT_MID + AcaciaConstants.CONFIG_DELIMITER + AcaciaConstants.OPT_LOAD_MIDS + System.getProperty("line.separator"));
 			config.write(AcaciaConstants.OPT_MID_FILE + AcaciaConstants.CONFIG_DELIMITER + selectedMIDsFileName);
 		
 			for(MIDPrimerCombo mid : validMIDS)
 			{
 					selectedMIDs.write(mid.getDescriptor() + "," + mid.getMID() + "," + mid.getPrimer() + System.getProperty("line.separator"));
 			}
 		}
 		
 		config.close();
 		selectedMIDs.close();
 	}
 
 	//creates a hashmap containing the default settings
 	/**
 	 * Gets the default settings.
 	 *
 	 * @return the default settings
 	 */
 	public HashMap <String, String> getDefaultSettings()
 	{
 		HashMap <String, String> settings = new HashMap <String, String>();
 		for (int i = 0; i < settingKeys.length; i++) {
 			System.out.println("Loading " + settingKeys[i] + " to value "
 					+ settingValues[i]);
 			settings.put(settingKeys[i], settingValues[i]);
 		}
 		return settings;
 	}
 
 	/**
 	 * Load mids.
 	 *
 	 * @param filename the file containing the MIDs and descriptions
 	 * @param logger the Acacia logger
 	 * @return a list of MIDS found in the file 
 	 * @throws Exception any exception that occurred while loading the MIDS
 	 */
 	public LinkedList <MIDPrimerCombo> loadMIDS(String filename, AcaciaLogger logger) throws Exception
 	{
 		if(filename == null)
 		{
 			return null;
 		}
 
 		MIDReader mReader = new MIDReader(filename);
 		LinkedList <MIDPrimerCombo> mids = mReader.loadMIDS();
 		return mids;
 	}
 
 	/**
 	 * Gets the platform specific path divider.
 	 *
 	 * @return the platform specific path divider
 	 */
 	public static String getPlatformSpecificPathDivider() 
 	{
 		String pathSep = System.getProperty("file.separator");
 		return pathSep;
 	}
 	
 	/**
 	 * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
 	 * or the first access to SingletonHolder.INSTANCE, not before.
 	 */
 	private static class AcaciaUtilityHolder 
 	{ 
 		
 		/** The Constant INSTANCE. */
 		private static final AcaciaEngine INSTANCE = new AcaciaEngine();
 
 		/**
 		 * Gets the single instance of AcaciaUtilityHolder.
 		 *
 		 * @return single instance of AcaciaUtilityHolder
 		 */
 		public static AcaciaEngine getInstance() 
 		{
 			return AcaciaUtilityHolder.INSTANCE;
 		}
 
 	}
 
 	/**
 	 * Checks if a character is recognised as IUPAC.
 	 *
 	 * @param curr the character to check
 	 * @return true, if is iUPAC
 	 */
 	public boolean isIUPAC(char curr) 
 	{
 		String valid = "ATGCatgc"; //for now, it is either upper case or lowercase....
 		if(valid.indexOf(curr) >= 0 || AcaciaConstants.IUPAC_AMBIGUOUS_MAPPINGS.containsKey(Character.toUpperCase(curr)))
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Gets the Acacia executable version number.
 	 *
 	 * @return the version
 	 * @throws Exception the exception
 	 */
 	public static String getVersion() throws Exception
 	{
 		URL url = AcaciaEngine.getEngine().getClass().getResource(BUILD_PROPERTIES_FILE);
 		
 		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 		String major = null;
 		String minor = null;
 		String build = null;
 
 		String line = in.readLine();
 
 		while(line != null)
 		{
 			String [] keyValue = line.split("=");
 
 			if(keyValue[0].equals(BUILD_MAJOR_KEY))
 			{
 				major = keyValue[1];
 			}
 			else if (keyValue[0].equals(BUILD_MINOR_KEY))
 			{
 				minor = keyValue[1];
 			}
 			else if(keyValue[0].equals(BUILD_REVISION_KEY))
 			{
 				build = keyValue[1];
 			}
 
 			line = in.readLine();
 		}
 
 		String version = major + "." + minor + "-b" + build;
 		return version;
 	}
 }
