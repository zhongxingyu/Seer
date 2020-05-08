 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.TreeSet;
 import java.util.logging.FileHandler;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 public class Main {
 
     private static Logger logger;
     private static String stopWordFile = null;
     private static String nounsFile = null;
     private static String queryFile = null;
     private static String queryFolder = null;
     private static String relevancyList = null;
     private static String chartFile = "chart";
 
     private static Crawler crawler;
     private static DataSet dataSet;
     private static QueryHandler queryHandler;
     private static PrecisionRecall precisionRecall;
     
     public static void printDynamicStats(String queryFile, String query, 
             TreeSet<QueryResult> results, long time, TablePerQuery table) {
 
         logger.log(Config.LOG_LEVEL, "Query file: " + queryFile + "\n");
         logger.log(Config.LOG_LEVEL, "Query: " + query + "\n");
         logger.log(Config.LOG_LEVEL, "Response time: " + time + " ms\n");
         logger.log(Config.LOG_LEVEL, "Number of results: " + results.size() + "\n\n");
         logger.log(Config.LOG_LEVEL, "Results:\n");
 
         for (QueryResult result : results) {
             logger.log(Config.LOG_LEVEL, result + "\n");
         }
 
         if (table != null) {
             logger.log(Config.LOG_LEVEL, "\n" + table.toString() + "\n");
         }
 
         logger.log(Config.LOG_LEVEL, "------------------------------------------------------------\n");
     }
 
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
 
     public static void initializeDataSet(String corpusFolder) {
         crawler = new Crawler(corpusFolder);
         
         try {
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
 
             dataSet = crawler.readDocuments();
             dataSet.computeDocLengths();
         } catch (IOException e) {
             System.out.println("Could not read the documents. Exiting...");
             System.exit(1);
         }
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
             } else if (args[i].startsWith(Config.PARAM_NOUNSFILE)) {
                 int eqPos = args[i].indexOf("=");
                 nounsFile = args[i].substring(eqPos + 1, args[i].length());
             } else if (args[i].startsWith(Config.PARAM_QUERYFOLDER)) {
                 int eqPos = args[i].indexOf("=");
                 queryFolder = args[i].substring(eqPos + 1, args[i].length());
             } else if (args[i].startsWith(Config.PARAM_QUERYFILE)) {
                 int eqPos = args[i].indexOf("=");
                 queryFile = args[i].substring(eqPos + 1, args[i].length());
             } else if (args[i].startsWith(Config.PARAM_RELEVANCY)) {
                 int eqPos = args[i].indexOf("=");
                 relevancyList = args[i].substring(eqPos + 1, args[i].length());
             }
         }
     }
 
     public static void handleQueries() throws IOException
     {
         precisionRecall = null;
         Scanner in = new Scanner(System.in);
         String params = "";
         while(true) {
          
             if(relevancyList != null){
                 precisionRecall = new PrecisionRecall(relevancyList);
             }
             
             System.out.println("\nType \"quit\" anytime to finish handling of queries.");
             System.out.print("Enter query file / folder: ");
 
             String queryLocation = in.nextLine();
             if (queryLocation.equals("quit")) {
                 return;
             }
             
             System.out.print("Enter query expansion (0 / NONE, 1 / LOCAL, 2 / GLOBAL): ");
             Config.queryType = Integer.valueOf(in.nextLine().toString());
             if (Config.queryType == 1) {
                 System.out.print("Enter alpha: ");
                 Config.alpha = Double.valueOf(in.nextLine().toString());
                 params = "_a=" + Config.alpha * 100;
                 
                 System.out.print("Enter beta: ");
                 Config.beta = Double.valueOf(in.nextLine().toString());
                 params = params + "_b=" + Config.beta * 100;
             }
            else {
                params = "";
            }
 
             ArrayList<String> queryFiles = getQueryFiles(queryLocation);
 
             for (String queryFile : queryFiles) {
                 String queryString = readQuery(queryFile);
                 
                 long startTime = System.currentTimeMillis();
                 Query query = new Query(crawler, queryString);
                 query.setType(Config.queryType);
                 System.out.println("Query name: " + queryFile);
                 TreeSet<QueryResult> results = queryHandler.retrieveDocument(query);
 
                 long time = System.currentTimeMillis() - startTime;
                 TablePerQuery table = computePrecisionRecallForFile(queryFile, results);
                 
                 // Logging stuff for each query.
                 printStatsForQuery(queryString, results.size(), time);
                 for (QueryResult res : results) {
                     System.out.println(res);
                 }
                 System.out.println();
                 printDynamicStats(queryFile, queryString, results, time, table);
             }
 
             if (relevancyList == null) {
                 continue;
             }
 
             double[] avg = precisionRecall.computeAverageOverAllQueries();
             String chartFileName = chartFile + "_" + "type:" + Config.queryType + params + ".png";
             precisionRecall.generatePrecisionRecallGraph(avg, chartFileName);
             
         }
     }
     
     private static TablePerQuery computePrecisionRecallForFile(String queryFile,
             TreeSet<QueryResult> results) {
         if (relevancyList == null) {
             return null;
         }
 
         int indexFileName = queryFile.lastIndexOf("/");
         String file = queryFile.substring(indexFileName, queryFile.length());
         String queryNumber = file.replaceAll("[^0-9]", "");
         if (queryNumber.isEmpty()) {
             System.out.println("The name of the file for query" + queryFile
                     + " does not have the proper format: Q[0-9]^+ !");
             return null;
         }
         
         int queryId = Integer.parseInt(queryNumber);
         return precisionRecall.computePrecisionAndRecall(queryId, results);
     }
 
     private static void printStatsForQuery(String queryString, int size, long time) {
         System.out.println("Query: " + queryString);
         System.out.println("The query was processed in " + time + " milliseconds.");
         System.out.println("Number of documents: " + size);
         System.out.println("Results:");
     }
 
     private static ArrayList<String> getQueryFiles(String queryLocation) {
         ArrayList<String> queryFiles = new ArrayList<String>();
         
         File file = new File(queryLocation);
         if (file.isDirectory()) {
             // Fetch all files within the directory.
             File[] listOfFiles = file.listFiles();
 
             for (int i = 0; i < listOfFiles.length; i++) {
                 queryFiles.add(queryLocation + "/" + listOfFiles[i].getName());
             }
         } else {
             // We have only one file, already given as parameter.
             queryFiles.add(queryLocation);
         }
         
         chartFile = "chart" + "_" + file.getName();
 
         return queryFiles;
     }
 
     public static void main(String args[]) throws IOException, FileNotFoundException {
         if (args.length < 1) {
             System.out.println("Usage: Main <document_folder>"
                     + " [" + Config.PARAM_STOPWORD + "]"
                     + " [" + Config.PARAM_STOPWORDFILE + "]"
                     + " [" + Config.PARAM_STEMMING + "]"
                     + " [" + Config.PARAM_QUERYFOLDER + "]"
                     + " [" + Config.PARAM_QUERYFILE + "]"
                     + " [" + Config.PARAM_RELEVANCY + "]");
 
             return;
         }
 
         initializeLogging();
         if (args.length >= 1) {
             initializeFlags(args);
         }
         initializeDataSet(args[0]);
 
         queryHandler = new QueryHandler(nounsFile, dataSet);
         handleQueries();
     }
 
     private static String readQuery(String queryFile) {
         FileInputStream inputStream;
 
         try {
             inputStream = new FileInputStream(queryFile);
             DataInputStream dataInput = new DataInputStream(inputStream);
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     dataInput));
 
             StringBuilder queryBuilder = new StringBuilder();
 
             String line;
             while ((line = reader.readLine()) != null) {
                 queryBuilder.append(line.toUpperCase());
                 queryBuilder.append(" ");
             }
 
             inputStream.close();
 
             return queryBuilder.toString();
         } catch (IOException e) {
             System.out.println("The query file could not be found!");
             System.exit(2);
         }
 
         return "";
     }
 }
