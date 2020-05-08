 package plusone;
 
 import plusone.utils.Dataset;
 import plusone.utils.Indexer;
 import plusone.utils.KNNGraphDistanceCache;
 import plusone.utils.KNNSimilarityCache;
 import plusone.utils.KNNSimilarityCacheLocalSVDish;
 import plusone.utils.MetadataLogger;
 import plusone.utils.PaperAbstract;
 import plusone.utils.PlusoneFileWriter;
 import plusone.utils.PredictionPaper;
 import plusone.utils.Terms;
 import plusone.utils.TrainingPaper;
 import plusone.utils.LocalSVDish;
 import java.util.Arrays;
 
 import plusone.clustering.Baseline;
 import plusone.clustering.ClusteringTest;
 import plusone.clustering.CommonNeighbors;
 import plusone.clustering.DTRandomWalkPredictor;
 import plusone.clustering.KNN;
 import plusone.clustering.KNNLocalSVDish;
 import plusone.clustering.KNNWithCitation;
 import plusone.clustering.LSI;
 //import plusone.clustering.SVDAndKNN;
 
 //import plusone.clustering.Lda;
 //import plusone.clustering.KNNRandomWalkPredictor;
 
 import java.io.File;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Set;
 
 public class Main {
     
     private final static int nTrialsPerPaper = 12;
 
     private Indexer<String> wordIndexer;
     private Indexer<PaperAbstract> paperIndexer;
 
     private Terms terms;
     private KNNSimilarityCache knnSimilarityCache;
     private KNNGraphDistanceCache knnGraphDistanceCache;
     private static MetadataLogger metadataLogger;
     private static Random randGen;
 
     // Document sets
     public List<TrainingPaper> trainingSet;
     public List<PredictionPaper> testingSet;
 
     // Clustering Methods
     private Baseline baseline;
     private CommonNeighbors cn;
     private DTRandomWalkPredictor dtRWPredictor;
     private KNN knn;
     private KNNWithCitation knnc;
     private LSI lsi;
     //private SVDAndKNN svdKnn;
     //private KNNRandomWalkPredictor knnRWPredictor;
 
 
     public Main(long randomSeed) {
 	randGen = new Random(randomSeed);
 	metadataLogger = new MetadataLogger();
     }
 
     public void load_data(String filename, double trainPercent) {
         Dataset dataset = Dataset.loadDatasetFromPath(filename);
         wordIndexer = dataset.getWordIndexer();
         paperIndexer = dataset.getPaperIndexer();
 	splitByTrainPercent(trainPercent, dataset.getDocuments());
     }
 
     /**
      * Splits all the documents into training and testing papers.
      * This function must be called before we can do execute any
      * clustering methods.
      */
     private void splitByTrainPercent(double trainPercent, 
 				    List<PaperAbstract> documents) {
     Random randGen = Main.getRandomGenerator();
 	trainingSet = new ArrayList<TrainingPaper>();
 	testingSet = new ArrayList<PredictionPaper>();
 	for (int i = 0; i < documents.size(); i ++) {
 	    if (randGen.nextDouble() < trainPercent)
 		trainingSet.add((TrainingPaper)documents.get(i));
 	    else
 		testingSet.add((PredictionPaper)documents.get(i));
 	}
 	System.out.println("trainingSet size: " + trainingSet.size());
 	System.out.println("testingSet size: " + testingSet.size());
     }
 
     public void splitHeldoutWords(double testWordPercent) {
 	Terms.Term[] terms = new Terms.Term[wordIndexer.size()];
 	for (int i = 0; i < wordIndexer.size(); i++) {
 	    terms[i] = new Terms.Term(i);
 	}
 	
 	for (TrainingPaper a : trainingSet){
 	    ((PaperAbstract)a).generateTf(testWordPercent, terms, false);
 	}
 	
 	for (PredictionPaper a : testingSet){
 	    ((PaperAbstract)a).generateTf(testWordPercent, null, true);
 	}
 
 	/*
 	terms_sorted = new Term[terms.length];
 
 	for (int c = 0; c < terms.length; c ++) {
 	terms_sorted[c] = terms[c];
 	}
 	Arrays.sort(terms_sorted);
 	*/
 	this.terms = new Terms(terms);
 
 	if (testIsEnabled("knn") || testIsEnabled("knnc"))
 	    knnSimilarityCache = 
 		new KNNSimilarityCache(trainingSet, testingSet);
 
 	if (testIsEnabled("knnc"))
 	    knnGraphDistanceCache = 
 		new KNNGraphDistanceCache(trainingSet, testingSet, 
 					  paperIndexer);
     }
     
     public static Random getRandomGenerator() { return randGen; }
 
     public static MetadataLogger getMetadataLogger() { return metadataLogger; }
 
     public double[] evaluate(PredictionPaper testingPaper,
 			     Integer[] prediction, int size, int k) {
     	int predicted = 0, total = 0;
     	double tfidfScore = 0.0, idfScore = 0.0, idf_top =  Math.log(size);
 
 	Set<Integer> givenWords = testingPaper.getTrainingWords();
 	Set<Integer> predictionWords = ((PaperAbstract)testingPaper).
 	    getTestingWords();
 	for (int j = 0; j < prediction.length && j < k; j ++) {
 	    Integer word = prediction[j];
 	    if (predictionWords.contains(word)) {
 		predicted ++;
 		double logVal = Math.log(terms.get(word).idfRaw() + 1.0);
 		
 		tfidfScore += ((PaperAbstract)testingPaper).
 		    getTestingTf(word) * 
 		    (idf_top - logVal);
 		idfScore += (idf_top - logVal);
 	    }
 	}
 
 	/* FIXME: We probably should divide by k here, rather than the total
 	 * number of predictions made; otherwise we reward methods that make
 	 * less predictions.  -James */
 	
 	return new double[]{(double)predicted, idfScore, 
 			    tfidfScore, (double)prediction.length}; 
    }
 
     public static void printResults(double[] results) {
 	System.out.println("Predicted: " + results[0]);
 	System.out.println("idf score: " + results[1]);
 	System.out.println("tfidf score: " + results[2]);
     }
 
     public static void printResults(File output, double[] results) {
 	PlusoneFileWriter writer = new PlusoneFileWriter(output);
 	writer.write("Predicted: " + results[0] + "\n");
 	writer.write("idf score: " + results[1] + "\n");
 	writer.write("tfidf score: " + results[2] + "\n");
 	writer.close();
     }
 
     public void runClusteringMethods(File outputDir, int[] ks) {
 
 	int size = trainingSet.size() + testingSet.size();
 	if (testIsEnabled("baseline")) {
 	    baseline = new Baseline(trainingSet, terms);
 	    runClusteringMethod(testingSet, baseline, outputDir, ks, size);
 	    
 	}
 		
 	if (testIsEnabled("dtrw")) {
 	    int rwLength =
 		Integer.getInteger("plusone.dtrw.walkLength", 4);
 	    boolean stoch = Boolean.getBoolean("plusone.dtrw.stochastic");
 	    int nSampleWalks = Integer.getInteger("plusone.dtrw.nSampleWalks");
 	    System.out.println("Random walk length: " + rwLength);
 	    if (stoch)
 		System.out.println("Stochastic random walk: " + nSampleWalks + " samples.");
             boolean finalIdf = Boolean.getBoolean("plusone.dtrw.finalIdf");
             boolean ndiw = Boolean.getBoolean("plusone.dtrw.normalizeDocsInWord");
             Boolean nwid = Boolean.getBoolean("plusone.dtrw.normalizeWordsInDoc");
 	    dtRWPredictor =
 		new DTRandomWalkPredictor(trainingSet, terms, rwLength, stoch, nSampleWalks, finalIdf, nwid, ndiw);
 	    runClusteringMethod(testingSet, dtRWPredictor, 
 		    outputDir, ks, size);
 	}
 
 
 	int[] closest_k =   parseIntList(System.getProperty("plusone.closestKValues", 
 					    "1,3,5,10,25,50,100,250,500,1000"));
 	int[] closest_k_svdish = parseIntList(System.getProperty("plusone.closestKSVDishValues", 
 					    "1,3,5,10,25,50,100,250,500,1000"));
 
 	KNNSimilarityCacheLocalSVDish KNNSVDcache = null;
 	LocalSVDish localSVD;
 	KNNLocalSVDish knnSVD;
 	if (testIsEnabled("svdishknn")){
 	    localSVD=new LocalSVDish(Integer.getInteger("plusone.svdishknn.nLevels"),
                                      parseIntList(System.getProperty("plusone.svdishknn.docEnzs")),
                                      parseIntList(System.getProperty("plusone.svdishknn.termEnzs")),
                                      parseIntList(System.getProperty("plusone.svdishknn.dtNs")),
                                      parseIntList(System.getProperty("plusone.svdishknn.tdNs")),
                                      parseIntList(System.getProperty("plusone.svdishknn.numLVecs")),
 				     trainingSet,terms.size());
 	    KNNSVDcache = new KNNSimilarityCacheLocalSVDish(trainingSet,testingSet,localSVD);
 	}
 
 	for (int ck = 0; ck < closest_k.length; ck ++) {
 	    if (testIsEnabled("knn")) {
 		knn = new KNN(closest_k[ck], trainingSet, paperIndexer, 
 			      terms, knnSimilarityCache);
 		runClusteringMethod(testingSet, knn, outputDir, ks, size);
 	    }
 	    if (testIsEnabled("knnc")) {
 		knnc = new KNNWithCitation(closest_k[ck], trainingSet,
 					   paperIndexer, knnSimilarityCache,
 					   knnGraphDistanceCache, terms);
 		runClusteringMethod(testingSet, knnc, outputDir, ks, size);
 	    }
 
 /*	    if (testIsEnabled("cn")) {
 		cn = new CommonNeighbors(closest_k[ck], trainingSet, paperIndexer,
 					 knnSimilarityCache, 
 					 knnGraphDistanceCache, terms);
 		runClusteringMethod(testingSet, cn, outputDir, ks, size);
 	    }
 	    
 
 	    
 	    if (testIsEnabled("svdknn")) {
 		svdKnn = new SVDAndKNN(closest_k[ck], trainingSet);
 		runClusteringMethod(testingSet, knnc, outputDir, ks, size);
 	    }
 	    		    
 	    
 	    if (testIsEnabled("knnrw")) {
 		knnRWPredictor =
 		    new KNNRandomWalkPredictor(closest_k[ck], trainingSet,
 					       wordIndexer, paperIndexer,
 					       1, 0.5, 1);
 		runClusteringMethod(trainingSet, testingSet,
 				    knnRWPredictor, outputDir, ks, usedWord);
 	    }*/
 	    
 	}
 
 	for (int ck = 0; ck < closest_k_svdish.length; ck ++) {
 	    if (testIsEnabled("svdishknn")){
 		knnSVD= new KNNLocalSVDish(closest_k_svdish[ck],trainingSet, paperIndexer,
 					   terms, KNNSVDcache);
 		runClusteringMethod(testingSet,knnSVD,outputDir,ks,size);
 	    }
         }
 
 	
         int[] dimensions = parseIntList(System.getProperty("plusone.svdDimensions", 
 					    "1,5,10,20"));
 	for (int dk = 0; dk < dimensions.length; dk ++) {
 	    if (testIsEnabled("lsi")) {
 		lsi = new LSI(dimensions[dk], trainingSet, terms);
 		
 		runClusteringMethod(testingSet, lsi, outputDir, ks, size);
 	    }
 	}
 	
 
     }
 
     public void runClusteringMethod(List<PredictionPaper> testingSet,
 				    ClusteringTest test, File outputDir,
 				    int[] ks, int size) {
 	long t1 = System.currentTimeMillis();
 	System.out.println("[" + test.testName + "] starting test" );
 	for (int ki = 0; ki < ks.length; ki ++) {
 		int k = ks[ki];
 		File kDir = null;
 		try {
 		    kDir = new File(outputDir, k + "");
 		    if (!kDir.exists()) kDir.mkdir();
 		} catch(Exception e) {
 		    e.printStackTrace();
 		}
 
 	double[] results = {0.0, 0.0, 0.0, 0.0};
 	MetadataLogger.TestMetadata meta = getMetadataLogger().getTestMetadata("k=" + k + test.testName);
 	test.addMetadata(meta);
 	List<Double> predictionScores = new ArrayList<Double>();
 	for (PredictionPaper testingPaper : testingSet) {
 	    double [] thisPaperResults = {0.0, 0.0, 0.0, 0.0};
 	    for (int i = 0; i < nTrialsPerPaper; ++i) {
 		Integer[] predict = test.predict(k, testingPaper);
 		double[] result = evaluate(testingPaper, predict, size, k);
 		for (int j = 0; j < 4; ++j) thisPaperResults[j] += result[j];
 	    }
 	    for (int j = 0; j < 4; ++j) results[j] += thisPaperResults[j];
 	    predictionScores.add(thisPaperResults[0]);
 	}
 	meta.createListValueEntry("predictionScore", predictionScores.toArray());
 	meta.createSingleValueEntry("numPredict", k);
 	
 	File out = new File(kDir, test.testName + ".out");
 	Main.printResults(out, new double[]{results[0]/results[3], 
 					    results[1], results[2]});
 	}
 	System.out.println("[" + test.testName + "] took " +
 			   (System.currentTimeMillis() - t1) / 1000.0 
 			   + " seconds.");
     }
 
     static double[] parseDoubleList(String s) {
 	String[] tokens = s.split(",");
 	double[] ret = new double[tokens.length];
 	for (int i = 0; i < tokens.length; ++ i) {
 	    ret[i] = Double.valueOf(tokens[i]);
 	}
 	return ret;
     }
 
     static int[] parseIntList(String s) {
 	String[] tokens = s.split(",");
 	int[] ret = new int[tokens.length];
 	for (int i = 0; i < tokens.length; ++ i) {
 	    ret[i] = Integer.valueOf(tokens[i]);
 	}
 	return ret;
     }
 
     static Boolean testIsEnabled(String testName) {
 	return Boolean.getBoolean("plusone.enableTest." + testName);
     }
 
     /*
      * data - args[0]
      * train percent - args[1]
      * test word percent - args[2] (currently ignored)
      */
     public static void main(String[] args) {
 
 	String data_file = System.getProperty("plusone.dataFile", "");
 
 	if (!new File(data_file).exists()) {
 	    System.out.println("Data file does not exist.");
 	    System.exit(0);
 	}
 
 	long randSeed = 
 	    new Long(System.getProperty("plusone.randomSeed", "0"));
 	Main main = new Main(randSeed);
 
 	double trainPercent = new Double
 	    (System.getProperty("plusone.trainPercent", "0.95"));
 	String experimentPath = System.getProperty("plusone.outPath", 
 						   "experiment");
 
 	main.load_data(data_file, trainPercent);
 	System.out.println("data file " + data_file);
 	System.out.println("train percent " + trainPercent);
 	//System.out.println("test word percent " + testWordPercent);
        System.out.println("Wordindexer size: " + main.wordIndexer.size());
 
 
 	/* These values can be set on the command line.  For example, to set
 	 * testWordPercents to {0.4,0.5}, pass the command-line argument
 	 * -Dplusone.testWordPercents=0.4,0.5 to java (before the class name)
 	 */
 	double[] testWordPercents = 
 	    parseDoubleList(System.getProperty("plusone.testWordPercents", 
 					       "0.1,0.3,0.5,0.7,0.9"));
 	int[] ks = 
 	    parseIntList(System.getProperty("plusone.kValues", 
 					    "1,5,10,15,20"));
 
 	for (int twp = 0; twp < testWordPercents.length; twp++) {
 	    double testWordPercent = testWordPercents[twp];	    
 	    File twpDir = null;
 	    try {
 		twpDir = new File(new File(experimentPath), 
 				  testWordPercent + "");
 		twpDir.mkdir();
 	    } catch(Exception e) {
 		e.printStackTrace();
 	    }
 	    
 	    main.splitHeldoutWords(testWordPercent);
 
 
 		System.out.println("processing testwordpercent: " + 
 				   testWordPercent);
 
 		main.runClusteringMethods(twpDir, ks);
 	    
 	}
 
 	if (Boolean.getBoolean("plusone.dumpMeta")) {
 	    PlusoneFileWriter writer = 
 		new PlusoneFileWriter(new File(new File(experimentPath),
 					       "metadata"));
 	    writer.write("var v = ");
 	    writer.write(Main.getMetadataLogger().getJson());
 	    writer.close();
 	}
     }
 }
