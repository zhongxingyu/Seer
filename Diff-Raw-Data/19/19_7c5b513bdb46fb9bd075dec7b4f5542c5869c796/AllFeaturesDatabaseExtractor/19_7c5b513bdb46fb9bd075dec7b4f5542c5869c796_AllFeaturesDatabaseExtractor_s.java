 package de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import de.l3s.boilerpipe.BoilerpipeProcessingException;
 import de.l3s.boilerpipe.extractors.ArticleExtractor;
 import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
 import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.impl.PrimitiveMapFeatureVector;
 
 /**
  * 
  * @author Nils R. (TF-IDF Extraction), Fabian T. (SQLITE USAGE)
  *
  */
 public class AllFeaturesDatabaseExtractor {
 	
 	private static final int MINIMAL_ARTICLE_LENGTH = 50;
 
 	private static Logger logger = LogManager
 			.getFormatterLogger(AllFeaturesDatabaseExtractor.class.getName());
 
 	static Options options = null;
 	static String connectionString = null;
 	static String outPath = null;
 	static Connection connection = null;
 	static ResultSet resultSet = null;
 	static Statement statement = null;
 	/**Tweet remover*/
 	static public Pattern tweets 		= Pattern.compile("Follow \\@\\w+");
 	/**Links*/
 	static public Pattern links 		= Pattern.compile("http\\S*\\s");
 	static public Pattern htmltags 		= Pattern.compile("\\<.*?>");
 	/**Too many whitespace remover*/
 	static public Pattern multispaces  = Pattern.compile(" +");
 	/**Multi to one line*/
 	static public Pattern multiline    = Pattern.compile("\n");
 	static public int PPLEN = new String("Previous Page").length();
 	static final ArticleExtractor FULLTEXT_EXTRACTOR = new ArticleExtractor();
 	static boolean debug = true; // Print debug info
 	
 	/**
 	 * 
 	 * @author Nils Rethmeier
 	 *
 	 */
 	public enum FeatureType {
 		/**
 		 * Take all nouns as features.
 		 */
 	    ALL,
 	    /**
 	     * Only use the N most and least frequent nouns per article. Normally, N=3;
 	     */
 	    BEST_WORST_N,
 	    /**
 	     * Only use the best noun of an article if its not already a collectionwide noun.
 	     */
 	    BEST 
 	}
 
 	public static void main(String[] args) throws IOException, NumberFormatException, SQLException {
 
 		options = createOptions();
 		CommandLineParser parser = new PosixParser();
 		try {
 			CommandLine line = parser.parse(options, args);
 			if (line.hasOption("help")) {
 				HelpFormatter formatter = new HelpFormatter();
 				formatter.printHelp("LSH", options);
 			}
 			if (evaluateCLIParameters(line)) {
 				int LIMIT = 1000;
 				
 //				// Test 1
 //				HashSet<String> descriptiveNouns = getAllNouns(FeatureType.BEST_WORST_N, 3, LIMIT);		  // Get all nouns from the corpus
 //				if (debug) {System.out.println("NOUN#=" + descriptiveNouns.size());}
 //				LinkedList<ImmutablePair<Long, HashMap<Integer, Double>>> articleFeatureVecs = new LinkedList<ImmutablePair<Long, HashMap<Integer, Double>>>();
 //				HashMap<Integer, Long> termInNumDocsCounts = new HashMap<Integer, Long>(descriptiveNouns.size());	
 //				long docCount = genFeatureVecs(descriptiveNouns, LIMIT, articleFeatureVecs, termInNumDocsCounts);
 //				//TFIDF
 //				augment2TFIDF(articleFeatureVecs, termInNumDocsCounts, docCount);
 //				descriptiveNouns = null; articleFeatureVecs = null; termInNumDocsCounts= null;// reset
 //				
 //				// Test 2
 //				HashSet<String> descriptiveNouns = getAllNouns(FeatureType.BEST, -1, LIMIT);		  // Get all nouns from the corpus
 //				if (debug) {System.out.println("NOUN#=" + descriptiveNouns.size());}
 //				LinkedList<ImmutablePair<Long, HashMap<Integer, Double>>> articleFeatureVecs = new LinkedList<ImmutablePair<Long, HashMap<Integer, Double>>>();
 //				HashMap<Integer, Long> termInNumDocsCounts = new HashMap<Integer, Long>(descriptiveNouns.size());		
 //				long docCount = genFeatureVecs(descriptiveNouns, LIMIT, articleFeatureVecs, termInNumDocsCounts);
 //				//TFIDF
 //				augment2TFIDF(articleFeatureVecs, termInNumDocsCounts, docCount);
 //				descriptiveNouns = null; articleFeatureVecs = null; termInNumDocsCounts= null;// reset
 				
 				// Test 3
 				HashSet<String> descriptiveNouns = null; // reset
 				descriptiveNouns = getAllNouns(FeatureType.BEST_WORST_N, 2, LIMIT);		  // Get all nouns from the corpus
 				if (debug) {System.out.println("NOUN#=" + descriptiveNouns.size());}
 				Set<FeatureVector<Double>> articleFeatureVecs = new HashSet<FeatureVector<Double>>();
 				HashMap<Integer, Long> termInNumDocsCounts = new HashMap<Integer, Long>(descriptiveNouns.size());	
 				HashMap<String, Integer> globalFeaturePositionMap = new HashMap<String, Integer>(descriptiveNouns.size(),1.0f);
 				NounExtractor nE = new NounExtractor(); // Actual extractor is exchangable
 				long docCount = genFeatureVecs(descriptiveNouns, LIMIT, articleFeatureVecs, termInNumDocsCounts, globalFeaturePositionMap, nE);
 				//TFIDF
 				augment2TFIDF(articleFeatureVecs, termInNumDocsCounts, docCount);
 				
 				writeFeatures(articleFeatureVecs, "corpora/augmentedTFIDF.lsh");
 				 
 				Set<FeatureVector<? extends Number>> tfidfFeatures = readfeatures("corpora/augmentedTFIDF.lsh");
 				printFeatureVec(tfidfFeatures);
 				
 				// TODO TRY THIS OUT = Add new feature
 				String uncleanedArticle = "Bla di bla ich bin ein Test article";
				addFeature(articleFeatureVecs, termInNumDocsCounts, globalFeaturePositionMap, uncleanedArticle, nE, docCount);
 				 
 			}
 		} catch (ParseException exp) {
 			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
 		}
 
 	}
 
 	
 	
 	
 
 	static void writeFeatures(Set<FeatureVector<Double>> articleFeatureVecs, String path) {
 		 try{
 		      //use buffering
 		      OutputStream file = new FileOutputStream(path);
 		      OutputStream buffer = new BufferedOutputStream( file );
 		      ObjectOutput output = new ObjectOutputStream( buffer );
 		      try{
 		        output.writeObject(articleFeatureVecs);
 		      }
 		      finally{
 		        output.close();
 		      }
 		    }  
 		    catch(IOException ex){
 		      logger.error("Cannot perform output." + ex);
 		    }		
 	}
 
 	@SuppressWarnings("unchecked")
 	public static Set<FeatureVector<? extends Number>> readfeatures(String path) {
 		Set<FeatureVector<? extends Number>> recoveredSet  = null; 
 		try{
 		      //use buffering
 		      InputStream file = new FileInputStream(path);
 		      InputStream buffer = new BufferedInputStream( file );
 		      ObjectInput input = new ObjectInputStream ( buffer );
 		      try{
 		        //deserialize the List
 		    	  recoveredSet = (Set<FeatureVector<? extends Number>>) input.readObject();
 		      }
 		      finally{
 		        input.close();
 		      }
 		    }
 		    catch(ClassNotFoundException ex){
 		      logger.error("Cannot perform input. Class not found." + ex);
 		    }
 		    catch(IOException ex){
 		    	logger.error("Cannot perform input. " + ex);
 		    }
 		return recoveredSet;
 	}
 
 	static void printFeatureVec(
 			Set<FeatureVector<? extends Number>> recoveredList) {
 		for (FeatureVector<? extends Number> featureVec : recoveredList) {
 			logger.trace(featureVec);
 		}
 	}
 
 	/**
 	 * Turn TF vectors into TFIDF vectors.
 	 * @param articleFeatureVecs
 	 * @param termInNumDocsCounts
 	 * @param docCount
 	 * @return 
 	 */
 	public static Set<FeatureVector<Double>> augment2TFIDF(
 			Set<FeatureVector<Double>> articleFeatureVecs,
 					   HashMap<Integer, Long>   termInNumDocsCounts,
 					   					long    docCount) {
 		for (FeatureVector<Double> featureVec : articleFeatureVecs) {
 			
 			augment2TFIDF(featureVec, termInNumDocsCounts, docCount);
 //			for (int pos = 0; pos < featureVec.getDimensionality(); pos++) {
 //				if(featureVec.getValue(pos)==null){
 //					continue;
 //				}
 //				double TF  = featureVec.getValue(pos);
 //				double IDF = Math.log((double) docCount/
 //						    (double) termInNumDocsCounts.get(pos));
 //				
 //				// Update the TF value to the TF IDF value
 //				featureVec.setValue(pos, (double) (TF*IDF));
 //			}
 		}
 		return articleFeatureVecs;
 	}
 	
 	/**
 	 * Turn TF into TF IDF vector.
 	 * @param articleFeatureVecs
 	 * @param termInNumDocsCounts
 	 * @param docCount
 	 * @return 
 	 */
 	private static FeatureVector<Double> augment2TFIDF(
 				   FeatureVector<Double> articleFeatureVec,
 					   HashMap<Integer, Long>   termInNumDocsCounts,
 					   					long    docCount) {
 			// for every counted noun in the article
 			for (int pos = 0; pos < articleFeatureVec.getDimensionality(); pos++) {
 				if(articleFeatureVec.getValue(pos)==null){
 					continue;
 				}
 				double TF  = articleFeatureVec.getValue(pos);
 				double IDF = Math.log((double) docCount/
 						    (double) termInNumDocsCounts.get(pos));
 				
 				// Update the TF value to the TF IDF value
 				articleFeatureVec.setValue(pos, (double) (TF*IDF));
 			}
 			++docCount; // Update the feature count;
 		return articleFeatureVec;
 	}
 
 	/**
 	 * Method that counts for every documents its feature counts.
 	 * 
 	 * @param commonNouns Golbal nouns determined during preprocessing
 	 * @param limit	num aricle to read (-1 = unlimited)
 	 * @param articleFeatureVecs List of per document noun counts (TF)
 	 * @param termInNumDocsCounts (IDF word in docs counts)
 	 * @param globalFeaturePositionMap (noun Order each article feature will follow. So they all have the same order)
 	 * @return
 	 */
 	public static long genFeatureVecs(HashSet<String> commonNouns, long limit, Set<FeatureVector<Double>> articleFeatureVecs, 
 			HashMap<Integer, Long> termInNumDocsCounts, HashMap<String, Integer> globalFeaturePositionMap, NounExtractor nE) {
 		/** IDF parts. */
 		long doccount = 0;
 		
 		// Sort the global features so they appear in the same order in ever article feature vector.
 		globalFeaturePositionMap = toSortedGlobalFeatureMap(commonNouns);
 		logger.trace(globalFeaturePositionMap);
 		
 		String LIMIT = "";
 		if (limit != -1) {
 			LIMIT = " LIMIT " + limit;
 		}
 		
 		try {
 			if (connectionString.contains("sqlite")) {
 				Class.forName("org.sqlite.JDBC");
 			} else if (connectionString.contains("postgresql")) {
 				Class.forName("org.postgresql.Driver");
 			} else {
 				throw new IllegalArgumentException(
 						"There is no known DBMS for your given connection string: "
 								+ connectionString);
 			}
 
 			connection = DriverManager
 					.getConnection(connectionString);
 			statement = connection.createStatement();
 			resultSet = statement
 					.executeQuery("SELECT id, cleaned_text FROM rss_article" + LIMIT + ";");
 			
 			
 			// Create nounextraction object
 			int lines = 0;
 			while (resultSet.next()) {
 				// Feedback
 				if (lines++%1000 ==0) {
 					logger.trace("Processed Lines:" + (lines-1));
 				}
 				
 				// Pattern
 				// Ends with. previous page
 				String fulltext = resultSet.getString("cleaned_text");
 				
 				// Skip articles that have no content.
 				if(fulltext==null || fulltext.length() < MINIMAL_ARTICLE_LENGTH) {
 					continue;
 				}
 				
 				doccount++;
 				
 				fulltext = cleanText(fulltext);
 								
 				// Generate augmentedTF feature vector. Also get IDF counts.
 				articleFeatureVecs.add(new PrimitiveMapFeatureVector<Double>
 											(Long.parseLong(resultSet.getString("id")), globalFeaturePositionMap.size(),
 									         nE.generateFeature(globalFeaturePositionMap, termInNumDocsCounts, fulltext))); 
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				resultSet.close();
 				statement.close();
 				connection.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return doccount;
 	}
 	
 	
 	static void addFeature(Set<FeatureVector<Double>> articleFeatureVecs, 
			HashMap<Integer, Long> termInNumDocsCounts, HashMap<String, Integer> globalFeaturePositionMap, String fulltext, NounExtractor nE, long docCount) throws NumberFormatException, SQLException {
 		
 		// Get TF Info.
 		PrimitiveMapFeatureVector<Double> articleFeature = new PrimitiveMapFeatureVector<Double>
		(Long.parseLong(resultSet.getString("id")), globalFeaturePositionMap.size(),
          nE.generateFeature(globalFeaturePositionMap, termInNumDocsCounts, cleanText(fulltext)));
 		
 		// Add IDF info.
 		articleFeatureVecs.add(augment2TFIDF(articleFeature, termInNumDocsCounts, docCount)); 
 	}
 
 	/**
 	 * Sort the global features so they appear in the same order in every articles' feature vector.
 	 * @param commonNouns
 	 * @return
 	 */
 	private static HashMap<String, Integer> toSortedGlobalFeatureMap(
 			HashSet<String> commonNouns) {
 		HashMap<String, Integer> globalFeaturePositionMap = new HashMap<String, Integer>(commonNouns.size());
 		ArrayList<String> collectionFeaturePosition = new ArrayList<String>(new TreeSet<String>(commonNouns)); // Sort them. e.g. lexicographically
 		int i = 0;
 		for (String gloabalFeature : collectionFeaturePosition) {
 			globalFeaturePositionMap.put(gloabalFeature, i++);
 		}
 		return globalFeaturePositionMap;
 	}
 
 	/**
 	 * Textual clean up.
 	 * @param fulltext
 	 * @return
 	 */
 	private static String cleanText(String fulltext) {
 		if (fulltext.endsWith("Previous Page")) {
 			// rem the link text
 			fulltext = fulltext.substring(0,fulltext.length()-PPLEN-1);
 		}
 		
 		// Rem tweet tags
 		Matcher lnks = links.matcher(fulltext);
 		fulltext = lnks.replaceAll(" ");
 		
 		// Erase HTML tags
 		Matcher tags = htmltags.matcher(fulltext);
 		fulltext = tags.replaceAll(" ");
 		
 		// Rem tweet tags
 		Matcher twt = tweets.matcher(fulltext);
 		fulltext = twt.replaceAll("");
 		// Rem multiline
 		Matcher mline = multiline.matcher(fulltext);
 		fulltext = mline.replaceAll(" ");
 		// Rem multispace
 		Matcher mspace = multispaces.matcher(fulltext);
 		return mspace.replaceAll(" ").trim();
 	}
 
 
 	/**
 	 * Extends the global map by a list of at most one new noun needed to describe ONE article.
 	 * At most, because the map gets no adds if it already contains at least ONE noun that is 
 	 * also present in the article.
 	 * @param collectionMap
 	 * @param fulltext
 	 * @param nE
 	 */
 	private static void addNeededNouns(HashSet<String> collectionMap, String fulltext, NounExtractor nE) {
 		// Extract nouns
 		long max = -1l;
 		boolean alreadyGlobal = false;
 		HashMap<String, Long> localNouns = nE.getLowercaseNouns(fulltext);
 		String mostFrequentLocal = null;
 		for (String noun: localNouns.keySet()) {
 			noun = noun.toLowerCase();
 			if (collectionMap.contains(noun)) {
 				alreadyGlobal = true;	
 			} else {
 				// determine most frequent local noun
 				if (localNouns.get(noun) > max) {
 					max = localNouns.get(noun);
 					mostFrequentLocal = noun;
 				}
 			} 
 		}
 		if (!alreadyGlobal && (mostFrequentLocal!=null)) {
 			collectionMap.add(mostFrequentLocal);
 		}
 		
 	}
 
 	/**
 	 * Function extracts feature nouns form corpus. 
 	 * There are three types of extraction.
 	 * <p> All nouns = {@link FeatureType.ALL} (i.e. full Zipf <b>nouns-distribution</b>)
 	 * <p> Minimum Set (Every article has at least one entry > 0 => to avoid empty features) = {@link FeatureType.BEST}. This gives the noun Zipf short tail (!= word short tail, which would be stop words)
 	 * <p> Most/Least frequent N nouns per article (Parse Zipf short+long tail). {@link FeatureType.BEST_WORST_N}. Requires to specify an N = numWorstBest
 	 * @param ftype
 	 * @param numWorstBest (only meaningful with {@link FeatureType.BEST_WORST_N})
 	 * @param articleLimit number of articles to process (-1 means no limit).
 	 * @return The set of collection-wide nouns.
 	 */
 	public static HashSet<String> getAllNouns(FeatureType ftype, int numWorstBest, int articleLimit) {
 		HashSet<String> collectionMap = new HashSet<String>(1000, 0.95f);
 		try {
 			if (connectionString.contains("sqlite")) {
 				Class.forName("org.sqlite.JDBC");
 			} else if (connectionString.contains("postgresql")) {
 				Class.forName("org.postgresql.Driver");
 			} else {
 				throw new IllegalArgumentException(
 						"There is no known DBMS for your given connection string: "
 								+ connectionString);
 			}
 
 			connection = DriverManager
 					.getConnection(connectionString);
 			statement = connection.createStatement();
 			resultSet = statement
 					.executeQuery("SELECT id, cleaned_text FROM rss_article;");
 			
 			
 			// Create nounextraction object
 			NounExtractor nE = new NounExtractor();
 			int lines = 0;
 			while (resultSet.next() && (lines!=articleLimit)) {
 				// Feedback
 				if (++lines%1000 == 0 && debug) {
 					System.out.println("Processed Lines:" + (lines));
 				}
 				
 				// Pattern
 				// Ends with. previous page
 				String fulltext = resultSet.getString("cleaned_text");
 				// Skip articles that have no content.
 				if(fulltext==null || fulltext.length() < 50) {
 					continue;
 				}
 				
 				if (ftype==FeatureType.BEST) {
 					addNeededNouns(collectionMap, cleanText(fulltext), nE);
 				}
 				// Add all nouns
 				else if (ftype==FeatureType.ALL ) {
 					for (String noun : nE.getLowercaseNouns(cleanText(fulltext)).keySet() ) {
 						collectionMap.add(noun);
 					}
 				}
 				else if (ftype==FeatureType.BEST_WORST_N ) {
 					HashMap<String, Long> articleNouns = nE.getLowercaseNouns(cleanText(fulltext));
 					ArrayList<Long> nouncounts = new ArrayList<Long>(articleNouns.values());
 					Collections.sort(nouncounts);
 					
 					// Extract the N least and most frequent nouns.
 					HashSet<Long> worstNBestN = new HashSet<Long>(2 * numWorstBest, 1.0f); // Wont get larger anyways
 					int numNouns = nouncounts.size();
 					if (!nouncounts.isEmpty()) {
 						// In case there are too few nouns in an article. Adjust number of bestWorst nouns.
 						numWorstBest = Math.min(numNouns, numWorstBest);
 						// Add Best
 						for (int i = 0; i < numWorstBest; ++i) {
 							worstNBestN.add(nouncounts.get(i));
 						}
 						// Add Worst
 						for (int i = numNouns-1; i > (numNouns-1)-numWorstBest ; --i) {
 							worstNBestN.add(nouncounts.get(i));
 						}
 					}
 					// Extract Article nouns of valid count
 					for (String aNoun : articleNouns.keySet()) {
 						Long aNounCount = articleNouns.get(aNoun);
 						// Add noun if the count fits
 						if (worstNBestN.contains(aNounCount)) {
 							collectionMap.add(aNoun);
 							worstNBestN.remove(aNounCount); // only add one word of this count
 						} if (worstNBestN.isEmpty()) {
 							break; 							// All worst/best words added.
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				resultSet.close();
 				statement.close();
 				connection.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return collectionMap;
 	}
 	
 //	private static HashSet<String> getDescriptiveNouns() {
 //		HashSet<String> collectionMap = new HashSet<String>(1000, 0.95f);
 //		try {
 //			if (connectionString.contains("sqlite")) {
 //				Class.forName("org.sqlite.JDBC");
 //			} else if (connectionString.contains("postgresql")) {
 //				Class.forName("org.postgresql.Driver");
 //			} else {
 //				throw new IllegalArgumentException(
 //						"There is no known DBMS for your given connection string: "
 //								+ connectionString);
 //			}
 //
 //			connection = DriverManager
 //					.getConnection(connectionString);
 //			statement = connection.createStatement();
 //			resultSet = statement
 //					.executeQuery("SELECT id, cleaned_text FROM rss_article;");
 //			
 //			
 //			// Create nounextraction object
 //			NounExtractor nE = new NounExtractor();
 //			int lines = 0;
 //			while (resultSet.next()) {
 ////				System.out.println(resultSet.getString("id")+";"+resultSet.getString("cleaned_text"));
 //				
 //				// Pattern
 //				// Ends with. previous page
 //				String fulltext = resultSet.getString("cleaned_text");
 //				// Skip articles that have no content.
 //				if(fulltext==null || fulltext.length() < 50) {
 //					continue;
 //				}
 //				
 //				// Feedback
 //				if (lines++%1000 ==0 && debug) {
 //					System.out.println("Processed Lines:" + (lines-1));
 //				}
 //	
 //				fulltext = cleanText(fulltext);
 //				
 //				getNeededNounSet(collectionMap, fulltext, nE);
 //			}
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		} finally {
 //			try {
 //				resultSet.close();
 //				statement.close();
 //				connection.close();
 //			} catch (Exception e) {
 //				e.printStackTrace();
 //			}
 //		}
 //		
 //		return collectionMap;
 //	}
 	
 	/**
 	 * Extends the global map by a list of at most one new noun needed to describe ONE article.
 	 * At most, because the map gets no adds if it already contains at least ONE noun that is 
 	 * also present in the article.
 	 * <p>
 	 * NOTE: This is a more minimal version of {@link #addNeededNouns(HashMap, String, NounExtractor)}
 	 * @param collectionMap
 	 * @param fulltext
 	 * @param nE
 	 * @return NONE. SET IS MODIFIED DIRECTLY.
 	 */
 	private static void getNeededNounSet(HashSet<String> collectionMap,
 			String fulltext, NounExtractor nE) {
 		// Extract nouns
 		long max = -1l;
 		boolean alreadyGlobal = false;
 		HashMap<String, Long> localNouns = nE.getLowercaseNouns(fulltext);
 		String mostFrequentLocal = null;
 		for (String noun: localNouns.keySet()) {
 			noun = noun.toLowerCase();
 			if (collectionMap.contains(noun)) {
 				alreadyGlobal = true;	
 				// Update global noun counts
 				collectionMap.add(noun);
 			} else {
 				// determine most frequent local noun
 				if (localNouns.get(noun) > max) {
 					max = localNouns.get(noun);
 					mostFrequentLocal = noun;
 				}
 			} 
 		}
 		if (!alreadyGlobal && (mostFrequentLocal!=null)) {
 			collectionMap.add(mostFrequentLocal);
 		}
 	}
 
 	@SuppressWarnings("static-access")
 	private static Options createOptions() {
 		Options cliOptions = new Options();
 
 		Option connectionString = OptionBuilder
 				.withArgName("connectionString")
 				.hasArg()
 				.withDescription(
 						"the jdbc connection string, eg. 'jdbc:sqlite:/media/zwerg/art/ARTposgresdb/news.sqlite' or 'jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP' - DBMS type will be infered")
 				.create("connectionString");
 
 		Option help = new Option("help", "print this message");
 		Option out = OptionBuilder
 				.withArgName("out")
 				.hasArg()
 				.withDescription(
 						"the jdbc connection string, eg. 'jdbc:sqlite:/media/zwerg/art/ARTposgresdb/news.sqlite' or 'jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/art2013?user=art2013&password=nVesq3TfTmeqRkP' - DBMS type will be infered")
 				.create("out");
 
 		return cliOptions.addOption(connectionString).addOption(help).addOption(out);
 	}
 
 	private static boolean evaluateCLIParameters(CommandLine line) {
 		if (line.hasOption("connectionString")) {
 			connectionString = line.getOptionValue("connectionString");
 		} else {
 			HelpFormatter formatter = new HelpFormatter();
 			formatter.printHelp("DatabaseExtractor", options);
 			return false;
 		}
 		if (line.hasOption("out")) {
 			outPath = line.getOptionValue("out");
 		} /*else {
 			System.err.println("ERROR: NO OUTPATH SPECIFIED");
 			return false;
 		}*/
 		return true;
 	}
 	
 	/**
 	 * Extracts Fulltext from an HTML Document.
 	 * 
 	 * @param html
 	 * @return
 	 */
 	public String htmlToFulltext(String html) {
 		String fulltext = null;
 		try {
 			fulltext = FULLTEXT_EXTRACTOR.getText(html);
 		} catch (BoilerpipeProcessingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return fulltext;
 	}
 	
 	static HashSet<String> loadFakeCollectionSet() {
 		HashSet<String> fake = new HashSet<String>();
 		String globalNouns[] = "auction alliance snowfall need leeds president jeopardy fish lord party carluccio parade time thrones malley video somalia image companies smoke hits air salvador hospital instruments austin mclaren john boccanegra oil southampton trouble berry nicosia porsche cookies man barnet look tags beat eu web hour living fire titanic wiggins comments banks amendment hong bob firms surgery expenses doctor march planes challengers bang vibe victory dozens milan magicians savills odi timescast sale warwickshire sign australia mushroom update sex set ipad pulse education business argentina twin lights knicks england style earnings sites music mail winners thatcher precision speed houston source rome nba wedding peace lions pistons ireland manager elin harlequins street shuttle mcauliffe court french roth council operation barrier bangui futures villa lira nations thoughts ot screen museum health depression funds pc month inflation series creation home scott fashion inmates arbor click space publisher chicken joe volcano squash water enthusiasm bombs mr green links plane whale afp second yacht carpet stockton twitter lewis mayer newshour thompson howard hagel erizku credit inaoka peter bed printing league flood mattress trafalgar panel scotland chairman rafael pernisco moon loss cheese security stage network war army steve japan mungiu oprah agenda it knox list griffiths clegg election prank football hostel abortion spinach things energy howse havens fight elliott countries cornwall teachers temperature practices olympics snow movements year naughton signs books francis priebus ebert miami williams simpson sunday steubenville liew press taxi advertising use date boing benedict spring villiers strike life redknapp crisis baby hair confinement park celaya carnival news colville vegetables cyprus tv dog world percent com dreams arsenal kabul toronto album earthquake sports faber igor science bangkok carl takes goodies mud gummer cheltenham cars swansea control index prix cocaine billion shares james binge syrup cast islands sofia car raid android children singer re sun singapore hurricane hand child neruda tom top cbs approval feet blair alarm norma masters journalism birth surface fox test guardian norovirus tim people course hostages airport retron sunderland heather rearranges information wind dame problems triffids goat josh celebrities plagiarism jerusalem tax alcohol david position might budget medals line title words opera india garrido campaign richardson migration barcroft bali murray post coes mayor payphones gun point leg transplant russia cloud stones photographers phone bolt bus contrast minerva police woman power gym shoe alvarez work patrick hartley mark picture market mars food company images filter west bolden jane messina fans paper movies greenfield state north apartments hatches religions witches york couple office film aerospace chaff boycott pyro minutes inch justin hanson gates women george maps yn turkey".split(" ");
 		for (String string : globalNouns) {
 			fake.add(string);
 		}
 		return fake;
 	}
 }
