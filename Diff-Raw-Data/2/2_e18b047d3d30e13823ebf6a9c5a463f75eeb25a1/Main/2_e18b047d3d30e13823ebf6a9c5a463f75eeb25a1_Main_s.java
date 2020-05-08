 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.logging.FileHandler;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 public class Main {
 
 	private static Logger logger;
 	private static String stopWordFile = null;
 	private static String docFolder = null;
 	private static String chartFile = "chart";
 
 	private static Crawler crawler;
 	private static ArrayList<DocSet> docSetList;
 
 	public static void initializeLogging() {
 		try {
 			InputStream inputStream = new FileInputStream("logging.properties");
 			LogManager.getLogManager().readConfiguration(inputStream);
 
 			FileHandler handler =
 				new FileHandler(Config.DYNAMIC_STATS_FILE, Config.LOG_FILE_SIZE, Config.LOG_FILE_COUNT);
 			logger = Logger.getLogger(Main.class.getName());
 			logger.addHandler(handler);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void initializeDataSet(String corpusFolder) throws IOException {
 		crawler = new Crawler(corpusFolder);
 
 		if (Config.enableStopwordElimination == true) {
 			if (stopWordFile == null) {
 				System.out.println("The stop word file was not given as parameter!"
 						+ " When the stopWord flag is set also the file of stop words"
 						+ " needs to be given as parameter!");
 				System.exit(1);
 			}
 
 			crawler.setStopWordsFile(stopWordFile);
 			crawler.readStopWords();
 
 			System.out.println("Stop Words Elimination Selected...");
 			
 			logger.log(Config.LOG_LEVEL, "With stopwords elimination...\n\n");
 		}
 
 		docSetList = crawler.readDocSet();       
 	}
 
 	public static void initializeFlags(String args[]) {
 
 		for (int i = 0; i < args.length; ++i) {
 			if (args[i].equals(Config.PARAM_STOPWORD)) {
 				Config.enableStopwordElimination = true;
 				chartFile += "_StopWord";
 			} else if (args[i].equals(Config.PARAM_STEMMING)) {
 				System.out.println("Stemmming is selected.....");
 				Config.enableStemming = true;
 				chartFile += "_Stemming";
 				logger.log(Config.LOG_LEVEL, "With stemming...\n\n");
 			} else if (args[i].startsWith(Config.PARAM_STOPWORDFILE)) {
 				int eqPos = args[i].indexOf("=");
 				stopWordFile = args[i].substring(eqPos + 1, args[i].length());
 			} else if (args[i].startsWith(Config.PARAM_FOLDER)) {
                 int eqPos = args[i].indexOf("=");
                 docFolder = args[i].substring(eqPos + 1, args[i].length());
             } 
 		}
 	}
 
 
 
 	public static void main(String args[]) throws IOException, FileNotFoundException {
 		if (args.length < 1) {
 			System.out.println("Usage: Main <document_folder>"
 					+ " [" + Config.PARAM_STOPWORD + "]"
 					+ " [" + Config.PARAM_STOPWORDFILE + "]"
 					+ " [" + Config.PARAM_STEMMING + "]"
 					+ " [" + Config.PARAM_FOLDER + "]"
 			);
 
 			return;
 		}
 
 		initializeLogging();
 
 		if (args.length >= 1) {
 			initializeFlags(args);
 		}
 
 		initializeDataSet(docFolder);
 
 		FrequencyMap totalMap = new FrequencyMap();
 
 		Classifier classifier = new Classifier();
        ROCGraph graph = new ROCGraph();
 
 		int run = 0;
 		
 		// Testing
         for (DocSet docSet : docSetList) {
             int totalSpamDocs = 0;
             int totalHamDocs = 0;
             
             // Re-compute total values.
             for (DocSet documentSet : docSetList) { 
                 totalMap.add(documentSet);
                 totalSpamDocs += documentSet.getNumSpamDocs();
                 totalHamDocs += documentSet.getNumHamDocs();
             }
 		        
 			run++;
 
 			FrequencyMap trainingMap = totalMap.subtract(docSet);
 
 			int trainingSpamDocs = totalSpamDocs - docSet.getNumSpamDocs();
 			int trainingHamDocs = totalHamDocs - docSet.getNumHamDocs();
 			int totalTrainingDocs = trainingSpamDocs + trainingHamDocs;
 			
 			double spamProb = trainingSpamDocs / (double) totalTrainingDocs;
 			double hamProb = 1 - spamProb;
 
 			classifier.reset();
 			classifier.classify(docSet, trainingMap, spamProb);
 
 			int TP = classifier.getTruePositives();
 			int FP = classifier.getFalsePositives();
 			int TN = classifier.getTrueNegatives();
 			int FN = classifier.getFalseNegatives();
 
 			double precision = TP / ((double) TP + FP);
 			double recall = TP / ((double) TP + FN);
 			double fpRate = FP / ((double) FP + TN);
 			double tpRate = recall;
 
 			StringBuilder stats = new StringBuilder();
 			
 			stats.append("Run no. " + run + "\n");
 			stats.append("Total training size: " + totalTrainingDocs + "\n");
 			stats.append("Total spam documents: " + trainingSpamDocs + "\n");
 			stats.append("Total ham documents: " + trainingHamDocs + "\n");
 			stats.append("Prior probabilities:\n");
 			stats.append("Spam - " + spamProb + "\n");
 			stats.append("Ham - " + hamProb + "\n");
 			stats.append("TP = " + TP + ", FN = " + FN + ", FP = " + FP + ", TN = " + TN  + "\n");
 			stats.append("Precision = " + precision + ", Recall = " + recall + "\n");
 			stats.append("====================================================================\n");
 			
 			logger.log(Config.LOG_LEVEL, stats.toString());
 			System.out.println(stats);
 
 			graph.addPoint(tpRate, fpRate);
 		}
 
 		graph.createROCGraph(chartFile + ".png");
 	}
 
 }
