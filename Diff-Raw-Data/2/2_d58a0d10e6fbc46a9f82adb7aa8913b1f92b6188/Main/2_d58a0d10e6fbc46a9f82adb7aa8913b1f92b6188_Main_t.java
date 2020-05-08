 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.TreeSet;
 import java.util.logging.FileHandler;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 public class Main {
 
     private static Logger logger;
     private static String stopWordFile = null;
     private static String queryFile = null;
     private static String queryFolder = null;
     private static String relevancyList = null;
 
     public static void printDynamicStats(String query, TreeSet<QueryResult> results, long time) {
         logger.log(Config.LOG_LEVEL, "Query: " + query + "\n");
         logger.log(Config.LOG_LEVEL, "Response time: " + time + "\n");
         logger.log(Config.LOG_LEVEL, "Number of results: " + results.size() + "\n");
         logger.log(Config.LOG_LEVEL, "Results:\n");
 
         for (QueryResult result : results) {
             logger.log(Config.LOG_LEVEL, result + "\n");
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
 
     public static void initializeFlags(String args[]) {
         
         //TODO: add the query folder option
         
         for (int i = 0; i < args.length; ++i) {
             if (args[i].equals(Config.PARAM_STOPWORD)) {
                 Config.enableStopwordElimination = true;
             } else if (args[i].equals(Config.PARAM_STEMMING)) {
                 Config.enableStemming = true;
             } else if (args[i].startsWith(Config.PARAM_STOPWORDFILE)) {
                 int eqPos = args[i].indexOf("=");
                 stopWordFile = args[i].substring(eqPos + 1, args[i].length());
             } else if (args[i].startsWith(Config.PARAM_QUERYFOLDER)) {
                 int eqPos = args[i].indexOf("=");
                 queryFolder = args[i].substring(eqPos + 1, args[i].length());
             } else if (args[i].startsWith(Config.PARAM_QUERYFILE)) {
                 int eqPos = args[i].indexOf("=");
                 queryFile = args[i].substring(eqPos + 1, args[i].length());
             } else if(args[i].startsWith(Config.PARAM_RELEVANCY)){
                 int eqPos = args[i].indexOf("=");
                 relevancyList = args[i].substring(eqPos + 1, args[i].length());
             }
         }
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
 
         Crawler crawler = new Crawler(args[0]);
         DataSet dataSet;
 
         try {
             if (Config.enableStopwordElimination == true) {
                 if (stopWordFile == null) {
                     System.out.println("The stop word file was not given as parameter!"
                             + " When the stopWord flag is set also the file of stop words"
                             + " needs to be given as parameter!");
                     return;
                 }
 
                 crawler.setStopWordsFile(stopWordFile);
                 crawler.readStopWords();
 
                 System.out.println("Stop Words Elimination Selected...");
             }
 
             dataSet = crawler.readDocuments();
         } catch (IOException e) {
             System.out.println("Could not read the documents. Exiting...");
             return;
         }
 
         PrecisionRecall precisionRecall = new PrecisionRecall(relevancyList);;
         QueryHandler handler = new QueryHandler(dataSet);
         //Scanner in = new Scanner(System.in);
 
         ArrayList<String> queryFiles = new ArrayList<String>();
         if (queryFolder != null) {
             File folder = new File(queryFolder);
             File[] listOfFiles = folder.listFiles();
 
             for (int i = 0; i < listOfFiles.length; i++) {
                 queryFiles.add(queryFolder + "/" + listOfFiles[i].getName());
             }
         } else if (queryFile != null) {
             queryFiles.add(queryFile);
         } else {
             System.out.println("No queries!");
         }
         
         for (String queryFile : queryFiles) {
             String queryString = readQuery(queryFile);
 
             long startTime = System.currentTimeMillis();
 
             Query query = new Query(crawler, queryString);
             TreeSet<QueryResult> results = handler.retrieveDocumentsForQuery(query);
             
             long time = System.currentTimeMillis() - startTime;
             printDynamicStats(queryString, results, time);
             
            int queryId = Integer.parseInt(queryFile.replaceAll("[^0-9]", ""));
             precisionRecall.computePrecisionAndRecall(queryId, results);
 
             System.out.println("Query: " + queryString);
             System.out.println("The query was processed in " + time
                     + " milliseconds.");
             System.out.println("Number of documents: " + results.size());
             System.out.println("Results:");
             
             for (QueryResult res: results) {
                 System.out.print(res + "\n");
             }
 
             System.out.println();
         }
         precisionRecall.computeAverageOverAllQueries();
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
             System.out.println("The query file cannot be found!");
             System.exit(2);
         }
         
         return "";
     }
 }
