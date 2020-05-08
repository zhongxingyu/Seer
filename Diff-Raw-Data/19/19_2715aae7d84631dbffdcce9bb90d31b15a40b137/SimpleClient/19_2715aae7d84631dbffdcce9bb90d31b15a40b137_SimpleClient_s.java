 package niagara.client;
 
 import java.io.*;
 import java.util.*;
 
 public class SimpleClient implements UIDriverIF {
     ConnectionManager cm;
     static final int MAX_QUERY_LEN = 10000;
     static String queryFilePath =
         "/disk/hopper/projects/disc/niagara/query_files/";
 
     // For partial result requests
     static int timeout;
     static Timer timer;
 
     //To get times
     long m_start, m_stop;
 
     AbstractConnectionReader acr;
 
     public SimpleClient(String host, int port) {
         cm =
             new ConnectionManager(
                 new SimpleConnectionReader(host, port, this, new DTDCache()));
     }
 
     public SimpleClient(String host) {
         this(host, ConnectionManager.SERVER_PORT);
     }
 
     public SimpleClient() {}
 
     public interface ResultsListener {
         void notifyNewResults(String results);
         void notifyError(String error);
         void notifyFinal();
     }
 
     Vector resultListeners = new Vector();
 
     public void addResultsListener(ResultsListener rl) {
         resultListeners.addElement(rl);
     }
 
     public void notifyNew(int id) {
         String results =
             ((SimpleConnectionReader) cm.getConnectionReader()).getResults();
         if (resultListeners.size() == 0) {
             System.out.println(results);
         } else {
             for (int i = 0; i < resultListeners.size(); i++) {
                 ((ResultsListener) resultListeners.get(i)).notifyNewResults(
                     results);
             }
         }
     }
 
     public synchronized void queryDone() {
       m_stop = System.currentTimeMillis();
       notify();
       System.out.println("Total time: " + (m_stop - m_start) + "ms.");
     }
     
     public void notifyFinalResult(int id) {
         queryDone();
         if (resultListeners.size() > 0) {
             for (int i = 0; i < resultListeners.size(); i++) {
                 ((ResultsListener) resultListeners.get(i)).notifyFinal();
             }
         }
     }
 
     public void errorMessage(String err) {
         if (resultListeners.size() == 0)
             System.err.println("SimpleClient error:" + err);
         else {
             for (int i = 0; i < resultListeners.size(); i++) {
                 ((ResultsListener) resultListeners.get(i)).notifyError(err);
             }
         }
     }
 
     protected static void usage() {
         System.err.println(
             "Usage: SimpleClient [-h host] [-t timeout] [-p port] [-qf QueryFileName]\n"
             + "[-x repetitions] [-d delay] [-w wait] [-o outputFileName]");
         System.exit(-1);
     }
 
     protected static ArrayList queryFiles = new ArrayList();
     protected static ArrayList repetitions = new ArrayList();
     protected static ArrayList delays = new ArrayList();
     protected static ArrayList waits = new ArrayList();
     protected static boolean queryfile = false;
     protected static String host;
     protected static int port;
     // XXX vpapad: ugly... SimpleClient can't do this yet
     protected static String outputFileName;
     
     protected static void parseArguments(String args[]) {
         // get the arguments
         host = "localhost"; // the defaults
         port = ConnectionManager.SERVER_PORT;
 
         int i = 0;
         while (i < args.length) {
             int starti = i;
             if (args[i].equals("-h")) {
                 host = args[i + 1];
                 i += 2;
             } else if (args[i].equals("-p")) {
                 port = Integer.parseInt(args[i + 1]);
                 i += 2;
             } else if (args[i].equals("-t")) {
                 timeout = Integer.parseInt(args[i + 1]);
                 i += 2;
             } else if (args[i].equals("--help")) {
                 usage();
             } else if (args[i].equals("-qf")) {
                 queryfile = true;
                 queryFiles.add(args[i + 1]);
                 repetitions.add(new Integer(1));
                 delays.add(new Integer(0));
                 waits.add(new Integer(0));
                 i += 2;
             } else if (args[i].equals("-x")) {
                 repetitions.set(
                     repetitions.size() - 1,
                     new Integer(args[i + 1]));
                 i += 2;
             } else if (args[i].equals("-d")) {
                 delays.set(delays.size() - 1, new Integer(args[i + 1]));
                 i += 2;
             } else if (args[i].equals("-w")) {
                 waits.set(waits.size() - 1, new Integer(args[i + 1]));
                 i += 2;
             } else if (args[i].equals("-o")) {
                 outputFileName = args[i + 1];
                 i += 2;
             } else {
                 usage();
             }
 
             if (starti == i) {
                 usage();
             }
         }
     }
     
     protected void processQueries() {
         String query;
 
         try {
             if (!queryfile) {
                 query = "";
                 String line = "";
 
                 BufferedReader br =
                     new BufferedReader(new InputStreamReader(System.in));
                 do {
                     query = query + line;
                     line = br.readLine();
                 } while (line != null);
                processQuery(query);
                return;

             } else {
 		int nQueries = queryFiles.size();
                 for (int i = 0; i < nQueries; i++) {
                     char cbuf[] = new char[MAX_QUERY_LEN];
                     query = getQueryFromFile((String) queryFiles.get(i), cbuf);
                     int reps = ((Integer) repetitions.get(i)).intValue();
                     int delay = ((Integer) delays.get(i)).intValue();
                     int wait = ((Integer) waits.get(i)).intValue();
                     try {
                         for (int j = 0; j < reps; j++) {
                             if (j > 0 && delay > 0)
                                 Thread.sleep(delay);
                             synchronized (this) {
                                 processQuery(query);
                                this.wait();
                             }
                         }
                         if (i < nQueries - 1 && wait > 0)
                             Thread.sleep(wait);
                     } catch (InterruptedException e) {
                         throw new RuntimeException("Unexpected interruption");
                     }
                 }
                 System.exit(0);
             }
             /* OLD Shell stuff - leave please, someday I will make this work - KT
              * else {
              *  char cbuf[] = new char[MAX_QUERY_LEN];
              *  boolean stop = false;
              *  StreamTokenizer stdIn =
              *      new StreamTokenizer(new 
              *      BufferedReader(new InputStreamReader(System.in)));
              *  stdIn.eolIsSignificant(true);
              *  int tt; * token type *
              *  while(!stop) {
              *    System.out.println("Query file: ");
              *    tt = stdIn.nextToken();
              *    if(tt == StreamTokenizer.TT_WORD) {
              *  if(stdIn.sval.equalsIgnoreCase("quit")) {
              *          stop = true;
              *  } else {
              *      stdIn.pushBack();
              *      String fn = getQueryFileName(stdIn);
              *      * read a file name from std in *
              *      query = getQueryFromFile(fn, cbuf);
              *      sc.processQuery(query);
              *  }
              *    } else if (tt == StreamTokenizer.TT_EOL) {
              *  * ignore eols *
              *  } else if (tt == StreamTokenizer.TT_NUMBER ||
              *         tt == StreamTokenizer.TT_EOF) {
              *  System.out.println("Invalid input");
              *    } else {
              *  System.out.println("Bad token type");
              *    }
              * }
              *  return;
              * }
              */
         } catch (IOException e) {
             System.err.println(
                 "SimpleClient: IO error while reading query or query file");
             e.printStackTrace();
             System.exit(-1);
         }
     }
     
     public static void main(String args[]) {
         parseArguments(args);
         SimpleClient sc = new SimpleClient(host, port);
         sc.processQueries();
     }
 
     protected static String getQueryFromFile(String fileName, char cbuf[])
         throws FileNotFoundException, IOException {
         BufferedReader br;
         try {
             br = new BufferedReader(new FileReader(fileName));
         } catch (FileNotFoundException e) {
             br = new BufferedReader(new FileReader(queryFilePath + fileName));
         }
 
         /* read the file into the buffer */
         int qLen = br.read(cbuf, 0, MAX_QUERY_LEN);
         if (qLen == MAX_QUERY_LEN) {
             System.out.println(
                 "Query exceeds maximum length (" + MAX_QUERY_LEN + ")");
             return null;
         }
         return new String(cbuf, 0, qLen); /* copies cbuf */
 
     }
 
     public void processQuery(String queryText) {
         System.err.println("Executing query " + queryText);
         if (queryText.equalsIgnoreCase("gc")) {
             cm.runGarbageCollector();
             return;
         } else if (queryText.equalsIgnoreCase("shutdown")) {
             cm.shutdownServer();
             return;
         }
 
         m_start = System.currentTimeMillis();
         final int id =
             cm.executeQuery(
                 QueryFactory.makeQuery(queryText),
                 Integer.MAX_VALUE);
         if (timeout > 0) {
             // Create a new timer thread as a daemon thread
             timer = new Timer(true);
             // Request partial results after timeout milliseconds
             timer.schedule(new TimerTask() {
                 public void run() {
                     cm.requestPartial(id);
                 }
             }, timeout);
         }
     }
 
     private static String getQueryFileName(StreamTokenizer stdIn) {
         try {
             String fn = "";
             int tt = stdIn.nextToken();
             while (tt != StreamTokenizer.TT_EOL
                 && tt != StreamTokenizer.TT_EOF) {
                 if (tt == StreamTokenizer.TT_NUMBER) {
                     System.out.println("Invalid file name");
                     return null;
                 }
                 fn += stdIn.sval;
             }
             return fn;
         } catch (java.io.IOException e) {
             System.out.println(
                 "Error getting query file name " + e.getMessage());
             return null;
         }
 
     }
 
 }
