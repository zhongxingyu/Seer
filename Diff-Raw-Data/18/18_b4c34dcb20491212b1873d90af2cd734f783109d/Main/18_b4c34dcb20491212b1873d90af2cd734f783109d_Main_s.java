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
 		}
 
 		docSetList = crawler.readDocSet();       
 	}
 
 	public static void initializeFlags(String args[]) {
 
 		for (int i = 0; i < args.length; ++i) {
 			if (args[i].equals(Config.PARAM_STOPWORD)) {
 				Config.enableStopwordElimination = true;
 				chartFile += "StopWord";
 			} else if (args[i].equals(Config.PARAM_STEMMING)) {
 				System.out.println("Stemmming is selected.....");
 				Config.enableStemming = true;
 				chartFile += "Stemming";
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
 		int totalSpamDocs = 0;
 		int totalHamDocs = 0;
 
 		// Precomputing the total values.
 		for (DocSet docSet: docSetList) { 
 			totalMap.add(docSet);
 			totalSpamDocs += docSet.getNumSpamDocs();
 			totalHamDocs += docSet.getNumHamDocs();
 		}
 		
 		Classifier classifier = new Classifier();
         ROCGraph graph = new ROCGraph();
 
 		int run = 0;
 		
 		// Testing
 		for (DocSet docSet: docSetList) {
 			run++;
 
 			FrequencyMap trainingMap = totalMap.subtract(docSet);
 
 			int trainingSpamDocs = totalSpamDocs - docSet.getNumSpamDocs();
 			int trainingHamDocs = totalHamDocs - docSet.getNumHamDocs();
			int totalDocs = trainingSpamDocs + trainingHamDocs;
			double spamProb = totalSpamDocs / (double) totalDocs;
 			double hamProb = 1 - spamProb;
 
 			classifier.reset();
 			classifier.classify(docSet, trainingMap, spamProb);
 
 			int TP = classifier.getTruePositives();
 			int FP = classifier.getFalsePositives();
 			int TN = classifier.getTrueNegatives();
 			int FN = classifier.getFalseNegatives();
 
			double precision = TP / (double) TP + FP;
			double recall = TP / (double) TP + FN;
			double fpRate = FP / (double) FP + TN;
 			double tpRate = recall;
 
 			//TODO: logging
 			//TODO: format doubles
 
 			System.out.println("====================================================================");
 			System.out.println("Run no. " + run);
			System.out.println("Total training size: " + totalDocs);
			System.out.println("Total spam documents: " + totalSpamDocs);
			System.out.println("Total correct documents: " + totalHamDocs);
 			System.out.println();
 			System.out.println("Prior probabilities:");
 			System.out.println("Spam - " + spamProb);
 			System.out.println("Ham - " + hamProb);
 			System.out.println("TP = " + TP + ", FN = " + FN + ", FP = " + FP + ", TN = " + TN );
 			System.out.println("Precision = " + precision + ", Recall = " + recall);
 
 			graph.addPoint(tpRate, fpRate);
 		}
 
 		graph.createROCGraph();
 	}
 
 }
