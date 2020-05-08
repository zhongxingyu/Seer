 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.logging.FileHandler;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 public class Main {
    
    private static Logger logger;
 
     public static void printDynamicStats(String query, ArrayList<String> docs, long time) {

         logger.log(Config.LOG_LEVEL, "Query: " + query + "\n");
         logger.log(Config.LOG_LEVEL, "Response time: " + time + "\n");
         logger.log(Config.LOG_LEVEL, "Number of results: " + docs.size() + "\n");
         logger.log(Config.LOG_LEVEL, "Results:\n");
         
         for (String doc: docs) {
             logger.log(Config.LOG_LEVEL, doc + "\n");
         }
         
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
         for (int i = 1; i < args.length; ++i) {
             if (args[i].equals(Config.PARAM_STOPWORD)) {
                 Config.enableStopwordElimination = true;
             } else if (args[i].equals(Config.PARAM_STEMMING)) {
                 Config.enableStemming = true;
             }
         }
     }
     
     public static void main(String args[]) throws IOException, FileNotFoundException {
 
         if (args.length < 1) {
             System.out.println("Usage: Main <document_folder>" +
             		"[" + Config.PARAM_STOPWORD + "]" +
             		"[" + Config.PARAM_STEMMING + "]");
             
             return;
         }
         
         initializeLogging();
         
         if (args.length >= 2)
             initializeFlags(args);
 
         Crawler crawler = new Crawler(args[0]);
         DataSet dataSet;
 
         try {
             dataSet = crawler.readDocuments();
         }
         catch (IOException e) {
             System.out.println("Could not read the documents. Exiting...");
             return;
         }
 
         dataSet.logStaticStats();
 
         QueryHandler handler = new QueryHandler(dataSet);
 
         Scanner in = new Scanner(System.in);
 
         while (true) {
             System.out.println("Enter new query (or \"quit\" to quit): ");
             String queryString = in.nextLine();
 
             if (queryString.equals("quit")) {
                 break;
             } else {
                 long startTime = System.currentTimeMillis();
 
                 Query query = new Query(queryString);
                 ArrayList<String> docs = handler.retrieveDocumentsForQuery(query);
 
                 long time = System.currentTimeMillis() - startTime;
                 
                 printDynamicStats(queryString, docs, time);
                 
                 /*
                 System.out.println("The query was processed in " + time
                         + " milliseconds.");
                 System.out.println("Number of documents: " + docs.size());
                 System.out.println("Results:");
                 
                 for (String s: docs) {
                     System.out.println(s);
                 }
                 */
 
                 System.out.println();
 
             }
         }
     }
 }
