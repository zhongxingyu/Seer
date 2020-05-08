 package de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import de.uni_potsdam.de.hpi.fgnaumann.art.featureGeneration.AllFeaturesDatabaseExtractor.FeatureType;
 import de.uni_potsdam.de.hpi.fgnaumann.art.vectors.FeatureVector;
 
 public class FeatureGenerator {
 	
 	public Set<FeatureVector<Double>> articleFeatureVecs = new HashSet<FeatureVector<Double>>();
 	public HashMap<Integer, Long> termInNumDocsCounts = null;	
 	public HashMap<String, Integer> globalFeaturePositionMap = null;
 	public NounExtractor nE = null; // Actual extractor is exchangeable
 	public long docCount = 0;
 	private long originalCollectionSize = 0;
 	private float addlimit = 0.0f;
 	
 	/**
 	 * Method takes an article collection, extracts its features (using 1 of three methods {@see FeatureType})
 	 * and computes TF IDF values while also tracking collection statistics for TF and IDF. 
 	 * @param addLimit Limits how many features may be added via {@link #addArticleToFeatures(String)} after preprocessing. 
 	 * Each added article updated collection statistics which invalidated old TFIDF values if too many adds were made. A good value is < 0.05 = 5%. 
 	 * @param limit Number of articles to process
 	 * @param ftype The method of feature extraction
 	 * @param featureN Only valid for the method BEST_WORST_N -- see {@link FeatureType} for details.
 	 * @param articleFeatureVecs The TFIDFs of the collection.
 	 * @param termInNumDocsCounts IDF noun collection counts
 	 * @param globalFeaturePositionMap Noun feature position ordering (so all feature vector have the same ordering)
 	 * @param docCount	Num of docs in the collection (IDF)
 	 * @throws IOException
 	 */
 	public void runPreprocessing(float addLimit, int limit, FeatureType ftype, int featureN, Set<FeatureVector<Double>> articleFeatureVecs, 
 			HashMap<Integer, Long> termInNumDocsCounts, HashMap<String, Integer> globalFeaturePositionMap, long docCount) throws IOException {
 		HashSet<String> descriptiveNouns = null; // reset
 		descriptiveNouns = AllFeaturesDatabaseExtractor.getAllNouns(ftype, featureN, limit);		  // Get all nouns from the corpus
 		articleFeatureVecs = new HashSet<FeatureVector<Double>>();
 		termInNumDocsCounts = new HashMap<Integer, Long>(descriptiveNouns.size());	
 		globalFeaturePositionMap = new HashMap<String, Integer>(descriptiveNouns.size(),1.0f);
 		nE = new NounExtractor(); // Actual extractor is exchangable
 		docCount = AllFeaturesDatabaseExtractor.genFeatureVecs(descriptiveNouns, limit, articleFeatureVecs, termInNumDocsCounts, globalFeaturePositionMap, nE);
 		originalCollectionSize = docCount; // Save this size once
 		//TFIDF
 		AllFeaturesDatabaseExtractor.augment2TFIDF(articleFeatureVecs, termInNumDocsCounts, docCount);
 		
 //		AllFeaturesDatabaseExtractor.writeFeatures(articleFeatureVecs, "corpora/augmentedTFIDF.ser");
 //		 
 //		Set<FeatureVector<? extends Number>> tfidfFeatures = AllFeaturesDatabaseExtractor.readfeatures("corpora/augmentedTFIDF.lsh");
 //		AllFeaturesDatabaseExtractor.printFeatureVec(tfidfFeatures);
 		
 	}
 	
 	/**
 	 * Method to add a new article to the feature collection.
 	 * <b>Note:<b> Corpus noun counts and document statistics are updated when using this method.
 	 * However, the TFIDFs vectors would have to be recomputed each time a new article is added
 	 * due to collection statistics changes. This is infeasable. Instead, preexistent feature TFIDFs 
 	 * are left unchange and the method issues an ERROR if too many features are added for the old
 	 * values to still be correct (otherwise results will become unexplainable as features are added).
 	 * @param uncleanedArticle
 	 * @throws Exception 
 	 */
	public void addArticleToFeatures(String uncleanedArticle) throws Exception {
 		try {
			AllFeaturesDatabaseExtractor.addFeature(articleFeatureVecs, termInNumDocsCounts, globalFeaturePositionMap, uncleanedArticle, nE, docCount);
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (docCount > (1.07f * originalCollectionSize)) {
 			throw new Exception("To many features added. Collection statistics invalid. Rerun FeatureGenerator.runPreprocession() for the new and " +
 								"larger collection.");
 		}
 	}
 }
