 /**
  * 
  */
 package in.ac.iitd.cse.Tests;
 
 import in.ac.iitd.cse.Properties.Common;
 import in.ac.iitd.cse.Properties.Hollywood2Dataset;
 import in.ac.iitd.cse.Properties.YouTubeDataset;
 import in.ac.iitd.cse.YouTubeClip.YTClip;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Map.Entry;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 
 import edu.mit.jwi.IDictionary;
 import edu.mit.jwi.item.ISynset;
 import edu.mit.jwi.item.ISynsetID;
 import edu.mit.jwi.item.POS;
 import edu.mit.jwi.item.Pointer;
 import edu.mit.jwi.morph.WordnetStemmer;
 import edu.stanford.nlp.tagger.maxent.MaxentTagger;
 import edu.sussex.nlp.jws.WuAndPalmer;
 
 /**
  * @author mcs122810
  * 
  */
 class Utilities
 {
 	static List < YTClip >			allTrainingClips	= new ArrayList < YTClip >();
 
 	static List < YTClip >			allTestingClips		= new ArrayList < YTClip >();
 
 	static ArrayList < ISynsetID >	roots				= new ArrayList < ISynsetID >();
 
 	static double[][]				centroids			= null;
 
 	/**
 	 * Total features in all training clips.
 	 */
 	static int						totalFeatures		= 0;
 
 	/**
 	 * Adds a clip if not already added. If already added, just add verbs from
 	 * description.<br/>
 	 * Will use tagger to tag verbs and stemmer to stem them.
 	 * 
 	 * @param clipName
 	 *            - Name of the clip.
 	 * @param description
 	 *            - Natural language desciption of the clip
 	 * @param tagger
 	 *            - POS tagger
 	 * @param stemmer
 	 *            - Verb stemmer
 	 */
 	static void addClip( String clipName, String description, MaxentTagger tagger, WordnetStemmer stemmer )
 	{
 		boolean found = false;
 
 		List < String > allVerbs = null;
 
 		YTClip currentClip = null;
 
 		for ( YTClip clip : allTrainingClips )
 		{
 			if ( clip.getName().equalsIgnoreCase( clipName ) )
 			{
 				found = true;
 
 				allVerbs = clip.getVerbsList();
 
 				currentClip = clip;
 
 				break;
 			}
 		}
 
 		if ( found == false )
 		{
 			allVerbs = new ArrayList < String >();
 
 			currentClip = new YTClip( clipName );
 
 			allTrainingClips.add( currentClip );
 		}
 
 		String[] tagged = tagger.tagString( description ).split( " " );
 
 		// extract only the verbs
 
 		for ( String string : tagged )
 		{
 			if ( string.contains( "_VB" ) )
 			{
 				for ( String stem : stemmer.findStems( string.split( "_" )[ 0 ], POS.VERB ) )
 					if ( !stem.equalsIgnoreCase( "be" ) )
 						allVerbs.add( stem );
 			}
 		}
 
 		currentClip.setVerbsList( allVerbs );
 	}
 
 	/**
 	 * This routine can only be used after CSVProcessing has been called at
 	 * least once which prepares initial CSV file.<br/>
 	 * CSVProcessing takes long time, hence its output can be stored.<br/>
 	 * This output is then fed to this routine, AddClipsFromCSV. The list -
 	 * allTrainingClips is first made empty by this routine and then built
 	 * fresh.
 	 * 
 	 * @param csvName
 	 *            - CSV file formed using previous results.
 	 * @throws IOException
 	 *             Thrown when I/O error occurs while reading CSV.
 	 */
 	static void AddClipsFromCSV( String csvName ) throws IOException
 	{
 		CSVReader csvReader = new CSVReader( new FileReader( csvName ) );
 
 		String[] nextLine;
 
 		allTrainingClips.clear();
 
 		while ( ( nextLine = csvReader.readNext() ) != null )
 		{
 			String clipName = nextLine[ 0 ];
 			YTClip clip = new YTClip( clipName );
 
 			List < String > initialLabel = new ArrayList < String >( 1 );
 			initialLabel.add( nextLine[ 1 ] );
 			clip.setLabel( initialLabel );
 
 			allTrainingClips.add( clip );
 		}
 	}
 
 	static void ClipsToHistograms() throws Exception
 	{
 		int descLength = 0;
 		int numCluster = 0;
 		String featuresFileBase = null;
 		String histogramFileBase = null;
 
 		if ( Common.state.CLIP_TO_HISTOGRAM_DONE.isDone() == true )
 			return;
 
 		if ( Common.state.ALL_CLIPS_INITIALISED.isDone() == false )
 		{
 			System.err.println( "Please initialise ALL clips before running this function." );
 			return;
 		}
 
 		if ( Common.DataSet.YOUTUBE.currentDS() == true )
 		{
 			descLength = YouTubeDataset.DescriptorLength;
 			featuresFileBase = YouTubeDataset.stipFeaturesDirPath + File.separator;
 			histogramFileBase = YouTubeDataset.histogramDirPath + File.separator;
 			numCluster = YouTubeDataset.KMeansNumClusters;
 		}
 		else
 			if ( Common.DataSet.HOLLYWOOD2.currentDS() == true )
 			{
 				descLength = Hollywood2Dataset.DescriptorLength;
 				featuresFileBase = Hollywood2Dataset.stipFeaturesDirPath + File.separator;
 				histogramFileBase = Hollywood2Dataset.histogramDirPath + File.separator;
 				numCluster = Hollywood2Dataset.KMeansNumClusters;
 			}
 		// read the centroids - output of Kmeans clustering
 
 		ReadCentroids();
 
 		double[] currentDescriptor = new double[descLength];
 
 		System.err.println( "Started converting Clips to histogram." );
 
 		// convert all training as well as testing clips to histogram
 		List < YTClip > allClips = allTrainingClips;
 		allClips.addAll( allTestingClips );
 
 		for ( YTClip clip : allClips )
 		{
 			double minDist = Double.POSITIVE_INFINITY;
 			int nearestClusterIndex = -1;
 			double currentDistance = -1;
 
 			// open corresponding features' file
 			String featuresFile = featuresFileBase + clip.getName() + ".features";
 			String histogramFile = histogramFileBase + clip.getName() + ".histogram";
 
 			File file = new File( histogramFile );
 
 			// if we have already processed, then save some time.
 			// If you DO want to write new histograms, either delete old ones from disk
 			// or change histogramDirPath from properties package
 			// or worse, comment following condition check.
 			if ( file.exists() )
 				continue;
 
 			Scanner scanner;
 
 			try
 			{
 				scanner = new Scanner( new FileReader( featuresFile ) );
 			}
 			catch ( Exception e )
 			{
 				// You need to extract STIP features before running this code.
 				// Please make sure that path for features' directory has been provided correctly.
 
 				System.err.println( "\nThis file should exists : " + featuresFile + "\n" );
 
 				clip.setHistogram( null );
 
 				continue;
 			}
 
 			System.err.println( "ClipToHistogram : " + clip.getName() + " started." );
 
 			clip.setHistogram( new int[numCluster] );
 
 			while ( scanner.hasNextDouble() )
 			{
 				// read current descriptor
 				for ( int i = 0; i < descLength; i++ )
 					currentDescriptor[ i ] = scanner.nextDouble();
 
 				// find the nearest cluster
 				for ( int i = 0; i < numCluster; i++ )
 				{
 					currentDistance = EuDistance( centroids[ i ], currentDescriptor );
 
 					if ( currentDistance < minDist )
 					{
 						minDist = currentDistance;
 						nearestClusterIndex = i;
 					}
 				}
 
 				// increment count of nearest cluster
 				clip.incrHistogram( nearestClusterIndex );
 			}
 
 			scanner.close();
 
 			BufferedWriter writer = new BufferedWriter( new FileWriter( histogramFile ) );
 
 			int[] currentHistogram = clip.getHistogram();
 
 			for ( int i = 0; i < currentHistogram.length - 1; i++ )
 			{
 				// write all but last elements with trailing space
 				writer.write( currentHistogram[ i ] + " " );
 			}
 
 			// write last element without trailing space.
 			writer.write( currentHistogram[ currentHistogram.length - 1 ] + System.getProperty( "line.separator" ) );
 
 			writer.close();
 
 			System.err.println( "ClipToHistogram : " + clip.getName() + " done." );
 		}
 
 		Common.state.CLIP_TO_HISTOGRAM_DONE.isDone( true );
 
 		System.err.println( "Done converting Clips to histogram." );
 
 		currentDescriptor = null;
 	}
 
 	/**
 	 * This routine reads the natural language descriptions of youtube clips
 	 * from a csv file.<br/>
 	 * It initially checks if it can use previous result, if it can, then save
 	 * some time.<br/>
 	 * The routine will read each line in CSV and add the clip. While adding
 	 * clips, it uses tagger and stemmer to make note of most frequent verb for
 	 * a particular video clip.
 	 * 
 	 * @param tagger
 	 *            - Stanford POS tagger
 	 * @param wordNetStemmer
 	 *            - MIT wordnet stemmer
 	 * @throws IOException
 	 *             Thrown if descriptions' CSV file is not accessible / absent.
 	 *             Or if result cannot be written to the disk.
 	 */
 	static void CSVProcessing( MaxentTagger tagger, WordnetStemmer wordNetStemmer ) throws IOException
 	{
 		if ( Common.state.ALL_TRAINING_CLIPS_INITIALISED.isDone() == true )
 			return;
 
 		boolean UsePrevResult = true;
 
 		String[] nextLine;
 
 		// Read CSV file for descriptions.
 
 		int linesDone = 0;
 
 		System.err.println( "Started processing CSV." );
 
 		try
 		{
 			CSVReader csvReader = new CSVReader( new FileReader( "initialLabels.csv" ) );
 			UsePrevResult = true;
 			csvReader.close();
 		}
 		catch ( Exception e )
 		{
 			// if file does not exist, then scan whole CSV - YouTubeDataset.descriptionsCSVPath
 			UsePrevResult = false;
 		}
 
 		if ( UsePrevResult == true )
 		{
 			AddClipsFromCSV( "initialLabels.csv" );
 		}
 		else
 		{
 			if ( Common.state.TAGGER_INITIALISED.isDone() == false
 					|| Common.state.STEMMER_INITIALISED.isDone() == false )
 			{
 				System.err.println( "Please initialise tagger and stemmer properly." );
 				return;
 			}
 			// Create a CSV file reader
 
 			CSVReader csvReader = new CSVReader( new FileReader( YouTubeDataset.descriptionsCSVPath ) );
 
 			while ( ( nextLine = csvReader.readNext() ) != null )
 			{
 				linesDone++;
 
 				if ( linesDone > 0 && linesDone <= YouTubeDataset.numLinesInCSV )
 				{
 					addClip( nextLine[ 0 ], nextLine[ 1 ], tagger, wordNetStemmer );
 
 					if ( linesDone % 1000 == 0 )
 					{
 						System.gc();
 					}
 					if ( linesDone % 1000 == 0 )
 					{
 						System.err.println( "Lines Done = " + linesDone );
 					}
 				}
 			}
 
 			csvReader.close();
 			csvReader = null;
 
 			// write current result so that it can be used next time.
 
 			CSVWriter csvWriter = new CSVWriter( new FileWriter( "initialLabels.csv" ) );
 
 			String[] csvLine = new String[2];
 
 			for ( YTClip clip : allTrainingClips )
 			{
 				// set labels of each video clip initially to the most frequent verb
 
 				clip.setMostFreqVerb( getMostFreqVerb( clip.getVerbsList() ) );
 				clip.addLabel( clip.getMostFreqVerb() );
 
 				// free some memory
 
 				clip.setVerbsList( null );
 
 				csvLine[ 0 ] = clip.getName();
 				csvLine[ 1 ] = clip.getMostFreqVerb();
 
 				csvWriter.writeNext( csvLine );
 			}
 
 			csvWriter.close();
 		}
 
 		Common.state.ALL_TRAINING_CLIPS_INITIALISED.isDone( true );
 		System.err.println( "Done with CSV processing." );
 	}
 
 	/**
 	 * Finds euclidean distance between a cluster and a HoGHoF descriptor.
 	 * 
 	 * @param cluster
 	 *            - One of the clusters from KMeans clustering.
 	 * @param descriptor
 	 *            - Descriptor from a video clip.
 	 * @return Euclidean distance.
 	 */
 	static double EuDistance( double[] cluster, double[] descriptor )
 	{
 		double distance = 0;
 
 		for ( int i = 0; i < YouTubeDataset.DescriptorLength; i++ )
 			distance += ( ( cluster[ i ] - descriptor[ i ] ) * ( cluster[ i ] - descriptor[ i ] ) );
 		return distance;
 	}
 
 	/**
 	 * Amongst the input list of verbs, find the one occuring most number of
 	 * times.
 	 * 
 	 * @param stemmedVerbsList
 	 *            - Input list of stemmed verbs
 	 * @return The most frequent verb representing the video clip.
 	 */
 	static String getMostFreqVerb( List < String > stemmedVerbsList )
 	{
 		Map < String, Integer > map = new HashMap < String, Integer >();
 
 		for ( String stemmedVerb : stemmedVerbsList )
 		{
 			if ( map.get( stemmedVerb ) != null )
 			{
 				map.put( stemmedVerb, 1 + map.get( stemmedVerb ) );
 			}
 			else
 				map.put( stemmedVerb, 1 );
 		}
 
 		Iterator < Entry < String, Integer >> iter = map.entrySet().iterator();
 		int max = 0;
 		String retVal = null;
 
 		while ( iter.hasNext() )
 		{
 			Map.Entry < String, Integer > mEntry = iter.next();
 			if ( max < mEntry.getValue() )
 			{
 				max = mEntry.getValue();
 				retVal = mEntry.getKey();
 			}
 		}
 
 		map = null;
 
 		return retVal;
 	}
 
 	/**
 	 * Used in similarity comparisons using java interface of wordNet
 	 * similarity. (jws)
 	 * 
 	 * @param pos
 	 *            - For example, edu.mit.jwi.item.POS.VERB
 	 * @param dict
 	 *            - The wordNet dictionary
 	 */
 	static void getRoots( POS pos, IDictionary dict )
 	{
 		if ( Common.state.ROOTS_INITIALISED.isDone() == true )
 			return;
 
 		ISynset synset = null;
 
 		Iterator < ISynset > iterator = null;
 
 		List < ISynsetID > hypernyms = null;
 
 		List < ISynsetID > hypernym_instances = null;
 
 		iterator = dict.getSynsetIterator( pos );
 
 		while ( iterator.hasNext() )
 		{
 			synset = iterator.next();
 			hypernyms = synset.getRelatedSynsets( Pointer.HYPERNYM );
 			hypernym_instances = synset.getRelatedSynsets( Pointer.HYPERNYM_INSTANCE );
 
 			if ( hypernyms.isEmpty() && hypernym_instances.isEmpty() )
 			{
 				roots.add( synset.getID() );
 			}
 		}
 
 		Common.state.ROOTS_INITIALISED.isDone( true );
 	}
 
 	/**
 	 * Amongst from all HoGHoF descriptors choose randomly.<br/>
 	 * 
 	 * Notice that longer videos have more probability in sampling over shorter
 	 * videos. Other strategy : Giving equal weightage to all the clips.
 	 * Roughly, this function will take 1 hour for 100,000 samples.
 	 * 
 	 * @throws IOException
 	 */
 	static void prepareKMeansInputFile() throws IOException
 	{
 		if ( Common.state.ALL_TRAINING_CLIPS_INITIALISED.isDone() == false )
 		{
 			System.err.println( "Please initialise all clips before running this function." );
 			return;
 		}
 
 		int numOfDescForClustering = 0;
 
 		// prepare KMeans Input file
 		String kMeansFileName = null;
 
 		// STIP HoGHoF features' directory
 		String featuresDir = null;
 
 		if ( Common.DataSet.YOUTUBE.currentDS() == true )
 		{
 			numOfDescForClustering = YouTubeDataset.numOfDescriptorsForClustering;
 			kMeansFileName = YouTubeDataset.KMeansInputFile;
 			featuresDir = YouTubeDataset.stipFeaturesDirPath + File.separator;
 		}
 		else
 			if ( Common.DataSet.HOLLYWOOD2.currentDS() == true )
 			{
 				numOfDescForClustering = Hollywood2Dataset.numOfDescriptorsForClustering;
 				kMeansFileName = Hollywood2Dataset.KMeansInputFile;
 				featuresDir = Hollywood2Dataset.stipFeaturesDirPath + File.separator;
 			}
 
 		// if file already exists, then dont process
 		boolean process = false;
 
 		try
 		{
 			BufferedReader reader = new BufferedReader( new FileReader( kMeansFileName ) );
 
 			// if file exists, close it don't process further.
 			reader.close();
 
 			process = false;
 		}
 		catch ( Exception e1 )
 		{
 			// if exception occurs, then we need to process.
 			process = true;
 		}
 
 		if ( process == true )
 		{
 			countFeaturesPerClip( featuresDir );
 
 			System.err.println( "Started with Preparing KMeans Input File." );
 
 			BufferedWriter writer = new BufferedWriter( new FileWriter( kMeansFileName ) );
 
 			Random random = new Random();
 
 			for ( int i = 0; i < numOfDescForClustering; i++ )
 			{
 				// in each iteration add one feature
 
 				// select a random descriptor from all available training descriptors.
 				int descrIndex = random.nextInt( totalFeatures );
 
 				YTClip selectedClip = null;
 
 				int remaining = descrIndex;
 
 				// find the clip containing this descriptor ( or feature ).
 				{
 					for ( YTClip clip : allTrainingClips )
 					{
 						if ( remaining < clip.getNumOfFeatures() )
 						{
 							selectedClip = clip;
 							break;
 						}
 						else
 						{
 							remaining -= clip.getNumOfFeatures();
 						}
 					}
 				}
 
 				// open corresponding features' file.
 				BufferedReader reader = new BufferedReader( new FileReader( featuresDir + selectedClip.getName()
 						+ ".features" ) );
 
 				String selectedDescriptor = null;
 
 				// go till required index and read the descriptor ( or feature ) & close the reader.
 				{
 					// 'remaining' is the number of descriptors we need to skip.
 					while ( remaining-- > 0 )
 						reader.readLine();
 
 					// next line is required descriptor ( or feature )
 					selectedDescriptor = reader.readLine();
 
 					reader.close();
 				}
 
 				reader = null; // help garbage collection ( gc ) ?
 
 				// write descriptor ( or feature ) to the k-means input file.
 				{
 					writer.write( selectedDescriptor );
 
 					// keep one feature per line
 
 					writer.newLine();
 				}
 
 				if ( i % 1000 == 0 )
 				{
 					System.err.print( "." );
 				}
 			}
 
 			System.err.println();
 
 			writer.close();
 
 			System.err.println( "Done with Preparing KMeans Input File." );
 		}
 		else
 			System.err.println( "Using previous KMeans input file." );
 
 		Common.state.KMEANS_INPUT_PREPARED.isDone( true );
 	}
 
 	/**
 	 * Count how many HoGHoF features appear in each clip.
 	 * 
 	 * @param featuresDir
 	 *            - The directory containing STIP features for all training
 	 *            clips. It should end with a slash character.
 	 * @throws IOException
 	 */
 	static private void countFeaturesPerClip( String featuresDir ) throws IOException
 	{
 		totalFeatures = 0;
 
 		int clipsDone = 0;
 
 		System.err.println( "Counting number of descriptors ( or features ) for each training clip. - Started." );
 
 		for ( YTClip clip : allTrainingClips )
 		{
 			BufferedReader reader = new BufferedReader( new FileReader( featuresDir + clip.getName() + ".features" ) );
 
 			int featureCount = 0;
 
 			while ( reader.readLine() != null )
 				featureCount++;
 
 			reader.close();
 
 			reader = null; // help gc ?
 
 			// save with clip the number of features for it.
 			clip.setNumOfFeatures( featureCount );
 
 			// keep count of total features
 			totalFeatures += featureCount;
 
 			if ( clipsDone++ % 10 == 0 )
 				System.err.print( "." );
 		}
 
 		System.err.println();
 
 		System.err.println( "Counting number of descriptors ( or features ) for each training clip. - Completed." );
 	}
 
 	/**
 	 * Randomly choose a clip to get a random descriptors from that clip.<br/>
 	 * Longer clips are given more weightage.
 	 * 
 	 * @param shouldOverwrite
 	 *            - Should previous file be over-written?
 	 * @throws IOException
 	 */
 	static void prepareKMeansInputFile_1( boolean shouldOverwrite ) throws IOException
 	{
 		if ( Common.state.ALL_TRAINING_CLIPS_INITIALISED.isDone() == false )
 		{
 			System.err.println( "Please initialise all clips before running this function." );
 			return;
 		}
 
 		int numOfDescForClustering = 0;
 
 		// prepare KMeans Input file
 		String fileName = null;
 
 		// features directory
 		String featuresDir = null;
 
 		if ( Common.DataSet.YOUTUBE.currentDS() == true )
 		{
 			numOfDescForClustering = YouTubeDataset.numOfDescriptorsForClustering;
 			fileName = YouTubeDataset.KMeansInputFile;
 			featuresDir = YouTubeDataset.stipFeaturesDirPath + File.separator;
 		}
 		else
 			if ( Common.DataSet.HOLLYWOOD2.currentDS() == true )
 			{
 				numOfDescForClustering = Hollywood2Dataset.numOfDescriptorsForClustering;
 				fileName = Hollywood2Dataset.KMeansInputFile;
 				featuresDir = Hollywood2Dataset.stipFeaturesDirPath + File.separator;
 			}
 
 		Random random = new Random();
 
 		// whether to process in case the file exists. 
 		boolean process = shouldOverwrite;
 
 		try
 		{
 			BufferedReader reader = new BufferedReader( new FileReader( fileName ) );
 
 			// no exception means file exists
 			reader.close();
 
 			if ( shouldOverwrite == false )
 				process = false;
 		}
 		catch ( Exception e1 )
 		{
 			// if exception occurs, then we need to process.
 			process = true;
 		}
 
 		if ( process == true )
 		{
 			countFeaturesPerClip( featuresDir );
 
 			// first randomly select a clip. then randomly just select the index of descriptor.
 			// when the file is read, read all the descriptor chosen for sampling at once.
 			{
 				for ( int i = 0; i < numOfDescForClustering; i++ )
 				{
 					// select a random clip.
 					int randClipIndex = random.nextInt( allTrainingClips.size() );
 
 					YTClip randClip = allTrainingClips.get( randClipIndex );
 
 					// find out total number of descriptors in that clip.
 					int numOfDesc = randClip.getNumOfFeatures();
 
 					// among them, chose a random descriptor
 					int randDescIndex = random.nextInt( numOfDesc );
 
 					// add index to the clip - to be sampled later.
 					randClip.addDescIndex( randDescIndex );
 				}
 			}
 
 			System.err.println( "Started with Preparing KMeans Input File." );
 
 			BufferedWriter writer = new BufferedWriter( new FileWriter( fileName ) );
 
 			for ( YTClip clip : allTrainingClips )
 			{
 				String featuresFile = featuresDir + clip.getName() + ".features";
 
 				BufferedReader reader;
 
 				// try to open corresponding .features file of the clip.
 				try
 				{
 					reader = new BufferedReader( new FileReader( featuresFile ) );
 				}
 				catch ( Exception e )
 				{
 					// You need to extract STIP features before running this code.
 					// Please make sure that path for features' directory has been provided correctly.
 
 					System.err.println( "\nThis file should exists : " + featuresFile + "\n" );
 
 					// we continue to next clip.
 					// we just warn for non existing files - do not halt / throw exception.
 					continue;
 				}
 
 				List < Integer > indices = clip.getDecsIndices();
 
 				Collections.sort( indices );
 
 				int currentIndex = 0;
 
 				for ( int index : indices )
 				{
 					// skip ( index - currentIndex ) descriptors
 					while ( currentIndex < index )
 					{
 						reader.readLine();
 						currentIndex++;
 					}
 
 					writer.write( reader.readLine() );
 					currentIndex++;
 
 					// keep one feature per line
 					writer.newLine();
 				}
 
 				reader.close();
 			}
 
 			writer.close();
 
 			System.err.println( "Done with Preparing KMeans Input File." );
 		}
 		else
 			System.err.println( "Using previous KMeans input file." );
 
 		Common.state.KMEANS_INPUT_PREPARED.isDone( true );
 	}
 
 	/**
 	 * Randomly choose a clip to get a random descriptors from that clip.<br/>
 	 * Each clip is given equal weightage.
 	 * 
 	 * @param shouldOverwrite
 	 *            - Should previous file be over-written?
 	 * @throws IOException
 	 */
 	static void prepareKMeansInputFile_2( boolean shouldOverwrite ) throws IOException
 	{
 		if ( Common.state.ALL_TRAINING_CLIPS_INITIALISED.isDone() == false )
 		{
 			System.err.println( "Please initialise all clips before running this function." );
 			return;
 		}
 
 		int numOfDescForClustering = 0;
 
 		// prepare KMeans Input file
 		String fileName = null;
 
 		// features directory
 		String featuresDir = null;
 
 		if ( Common.DataSet.YOUTUBE.currentDS() == true )
 		{
 			numOfDescForClustering = YouTubeDataset.numOfDescriptorsForClustering;
 			fileName = YouTubeDataset.KMeansInputFile;
 			featuresDir = YouTubeDataset.stipFeaturesDirPath + File.separator;
 		}
 		else
 			if ( Common.DataSet.HOLLYWOOD2.currentDS() == true )
 			{
 				numOfDescForClustering = Hollywood2Dataset.numOfDescriptorsForClustering;
 				fileName = Hollywood2Dataset.KMeansInputFile;
 				featuresDir = Hollywood2Dataset.stipFeaturesDirPath + File.separator;
 			}
 
 		Random random = new Random();
 
 		// whether to process in case the file exists. 
 		boolean process = shouldOverwrite;
 
 		try
 		{
 			BufferedReader reader = new BufferedReader( new FileReader( fileName ) );
 
 			// no exception means file exists
 			reader.close();
 
 			if ( shouldOverwrite == false )
 				process = false;
 		}
 		catch ( Exception e1 )
 		{
 			// if exception occurs, then we need to process.
 			process = true;
 		}
 
 		if ( process == true )
 		{
 			countFeaturesPerClip( featuresDir );
 
 			int maxSamplesPerClip = numOfDescForClustering / allTrainingClips.size() + 1;
 
 			for ( YTClip clip : allTrainingClips )
 			{
 				clip.clearIndices();
 
 				for ( int i = 0; i < maxSamplesPerClip && i < clip.getNumOfFeatures(); i++ )
 				{
 					int randIndex = random.nextInt( clip.getNumOfFeatures() );
 					clip.addDescIndex( randIndex );
 				}
 			}
 
 			System.err.println( "Started with Preparing KMeans Input File." );
 
 			BufferedWriter writer = new BufferedWriter( new FileWriter( fileName ) );
 
 			for ( YTClip clip : allTrainingClips )
 			{
 				String featuresFile = featuresDir + clip.getName() + ".features";
 
 				BufferedReader reader;
 
 				// try to open corresponding .features file of the clip.
 				try
 				{
 					reader = new BufferedReader( new FileReader( featuresFile ) );
 				}
 				catch ( Exception e )
 				{
 					// You need to extract STIP features before running this code.
 					// Please make sure that path for features' directory has been provided correctly.
 
 					System.err.println( "\nThis file should exists : " + featuresFile + "\n" );
 
 					// we continue to next clip.
 					// we just warn for non existing files - do not halt / throw exception.
 					continue;
 				}
 
 				List < Integer > indices = clip.getDecsIndices();
 
 				Collections.sort( indices );
 
 				int currentIndex = 0;
 
 				for ( int index : indices )
 				{
 					if( currentIndex > index )
 					{
 						reader.close();
 						reader = new BufferedReader( new FileReader( featuresFile ) );
 						currentIndex = 0;
 					}
 					// skip ( index - currentIndex ) descriptors
 					while ( currentIndex < index )
 					{
 						reader.readLine();
 						currentIndex++;
 					}
 
 					String desc = reader.readLine();
 					writer.write( desc );
 					currentIndex++;
 
 					// keep one feature per line
 					writer.newLine();
 				}
 
 				reader.close();
 			}
 
 			writer.close();
 			
 			// see the distribution
 			for( YTClip clip : allTrainingClips )
 				if( clip.getNumofSampledDesc() != maxSamplesPerClip )
 					System.out.println( clip.getName() + "\t" + clip.getNumofSampledDesc() );
 			System.out.println( "Other clips have " + maxSamplesPerClip + " samples taken from them." );
 
 			System.err.println( "Done with Preparing KMeans Input File." );
 		}
 		else
 			System.err.println( "Using previous KMeans input file." );
 
 		Common.state.KMEANS_INPUT_PREPARED.isDone( true );
 	}
 
 	/**
 	 * Use after running KMeans. It reads output of KMeans.
 	 * 
 	 * @throws Exception
 	 */
 	static void ReadCentroids() throws Exception
 	{
 		// read in the data
 		String fileName = null;
 
 		try
 		{
 			int numCluster = 0;
 			int descLength = 0;
 
 			if ( Common.DataSet.YOUTUBE.currentDS() == true )
 			{
 				fileName = YouTubeDataset.KMeansOutputFile;
 				numCluster = YouTubeDataset.KMeansNumClusters;
 				descLength = YouTubeDataset.DescriptorLength;
 				centroids = new double[YouTubeDataset.KMeansNumClusters][YouTubeDataset.DescriptorLength];
 			}
 			else
 				if ( Common.DataSet.HOLLYWOOD2.currentDS() == true )
 				{
 					fileName = Hollywood2Dataset.KMeansOutputFile;
 					numCluster = Hollywood2Dataset.KMeansNumClusters;
 					descLength = Hollywood2Dataset.DescriptorLength;
 					centroids = new double[Hollywood2Dataset.KMeansNumClusters][Hollywood2Dataset.DescriptorLength];
 				}
 
 			Scanner input = new Scanner( new File( fileName ) );
 
 			for ( int i = 0; i < numCluster; ++i )
 			{
 				for ( int j = 0; j < descLength; ++j )
 				{
 					if ( input.hasNextDouble() )
 					{
 						centroids[ i ][ j ] = input.nextDouble();
 					}
 				}
 			}
 
 			input.close();
 		}
 		catch ( FileNotFoundException e )
 		{
 			throw new Exception( "Please run Kmeans first. Please make sure this file exists and is readable :\n"
 					+ fileName );
 		}
 	}
 
 	/**
 	 * Run KMeans clustering. Currently it gives you commands to be executed
 	 * with matlab. Kindly run matlab KMeans externally.<br/>
 	 * In future, running KMeans will be made automatic.
 	 */
 	static void RunKMeans() //throws IOException, InterruptedException
 	{
 		if ( Common.state.KMEANS_INPUT_PREPARED.isDone() == false )
 		{
 			System.err.println( "Please prepare KMeans input file before running this function." );
 			//			return;
 		}
 		int numClusters = 0;
 		String KmeansIPFile = null;
 		String kmeansOPFile = null;
 
 		if ( Common.DataSet.YOUTUBE.currentDS() == true )
 		{
 			numClusters = YouTubeDataset.KMeansNumClusters;
 			KmeansIPFile = YouTubeDataset.KMeansInputFile;
 			kmeansOPFile = YouTubeDataset.KMeansOutputFile;
 		}
 		else
 			if ( Common.DataSet.HOLLYWOOD2.currentDS() == true )
 			{
 				numClusters = Hollywood2Dataset.KMeansNumClusters;
 				KmeansIPFile = Hollywood2Dataset.KMeansInputFile;
 				kmeansOPFile = Hollywood2Dataset.KMeansOutputFile;
 			}
 
 		System.out.println( "Use Matlab to run KMeans. Run following with matlab : " );
 		System.out.println( "X = load( '" + KmeansIPFile + "', '-ascii');\n"
 				+ "opts = statset('Display', 'iter', 'MaxIter', 200);" + "[~, C] = kmeans(X, " + numClusters
 				+ ", 'emptyaction', 'singleton', 'Options', opts  );\n" + "dlmwrite('" + kmeansOPFile + "', C, ' ');\n"
 				+ "exit;" );
 	}
 
 	/**
 	 * TODO : Change lustering to Agglomerative Hierarchical Clustering. Maybe,
 	 * try using some library.
 	 * 
 	 * @param dict
 	 *            - MIT Java WordNet Interface (JWI) dictionary instance.
 	 * @throws IOException
 	 *             Thrown when output label cannot be written to the disk.
 	 */
 	static void SimilarityComparisons( IDictionary dict ) throws IOException
 	{
 		if ( Common.state.SIMILARITY_DONE.isDone() == true )
 			return;
 
 		if ( Common.state.ALL_TRAINING_CLIPS_INITIALISED.isDone() == false )
 		{
 			System.err.println( "Please initialise all clips before running this function." );
 			return;
 		}
 
 		if ( Common.state.DICT_OPENED.isDone() == false )
 		{
 			System.err.println( "Please initialise and open the MIT JWI dictionary before calling this function." );
 			return;
 		}
 		System.err.println( "Started with Similarity comparisons." );
 
 		getRoots( POS.VERB, dict );
 
 		// Initialize WordNet Similarity
 
 		WuAndPalmer wuAndPalmer = new WuAndPalmer( dict, roots );
 
 		// Compare verbs for similarity and update labels
 
 		for ( int i = 0; i < allTrainingClips.size() - 1; i++ )
 		{
 			// check if label for this clip already exists on disk
 			String labelFile = YouTubeDataset.labelDirPath + File.separator + allTrainingClips.get( i ).getName()
 					+ ".label";
 
 			try
 			{
 				BufferedReader reader = new BufferedReader( new FileReader( labelFile ) );
 				// if file exists, do not process this clip and proceed next
 				reader.close();
 				continue;
 			}
 			catch ( Exception e )
 			{
 				// if label is not present on the disk, we process the clip and generate a label.
 			}
 
 			for ( int j = i + 1; j < allTrainingClips.size(); j++ )
 			{
 				YTClip clip1 = allTrainingClips.get( i );
 				YTClip clip2 = allTrainingClips.get( j );
 
 				List < String > label1 = clip1.getLabel();
 				List < String > label2 = clip2.getLabel();
 
 				double maxScore = 0.0;
 
 				// compare labels from one list with another
 
 				for ( String lbl1 : label1 )
 				{
 					for ( String lbl2 : label2 )
 					{
 						double tempScore = wuAndPalmer.wup( lbl1, 1, lbl2, 1, "v" );
 
 						maxScore = tempScore > maxScore ? tempScore : maxScore;
 					}
 				}
 
 				// if similarity is more than threshold, merge two labels' lists.
 
 				if ( maxScore > YouTubeDataset.similarityThreashold )
 				{
 					// add all label1 labels
 
 					List < String > newLabel = new ArrayList < String >( label1 );
 
 					// add those label2 labels that are not in lable1
 
 					for ( String lbl2 : label2 )
 					{
 						if ( newLabel.contains( lbl2 ) == false )
 						{
 							newLabel.add( lbl2 );
 						}
 					}
 
 					// assign new labels to both clips
 
 					clip1.setLabel( newLabel );
 					clip2.setLabel( newLabel );
 				}
 
 			}
 
 			System.err.println( i + " done.." );
 		}
 
 		System.err.println( "Writing labels to disk" );
 
 		for ( YTClip clip : allTrainingClips )
 		{
 			// check if label for this clip already exists on disk
 
 			String labelFile = YouTubeDataset.labelDirPath + File.separator + clip.getName() + ".label";
 
 			try
 			{
 				BufferedReader reader = new BufferedReader( new FileReader( labelFile ) );
 				// if file exists, do not write label of this clip and proceed next
 				reader.close();
 				continue;
 			}
 			catch ( Exception e )
 			{
 				// if label is not present on the disk, we write it.
 			}
 
 			BufferedWriter writer = new BufferedWriter( new FileWriter( labelFile ) );
 
 			List < String > allLabels = clip.getLabel();
 
 			// write all but last label with trailing spaces.
 			for ( int i = 0; i < allLabels.size() - 1; i++ )
 			{
 				writer.write( allLabels.get( i ) + " " );
 			}
 
 			// write last label without trailing space.
 			writer.write( allLabels.get( allLabels.size() - 1 ) + System.getProperty( "line.separator" ) );
 
 			writer.close();
 		}
 
 		Common.state.SIMILARITY_DONE.isDone( true );
 
 		System.err.println( "Done with Similarity comparisons." );
 	}
 
 }
