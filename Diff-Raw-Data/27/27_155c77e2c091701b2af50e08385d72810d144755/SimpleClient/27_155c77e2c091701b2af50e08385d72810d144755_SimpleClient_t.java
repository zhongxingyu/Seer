 package niagara.client;
 
 import java.io.*;
 import java.util.*;
 
 public class SimpleClient implements UIDriverIF {
 	  protected boolean ecode = false;
     protected ConnectionManager cm;
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
                 new SimpleConnectionReader(host, port, this));
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
 				if(!ecode) {
           String results = ((SimpleConnectionReader) acr).getResults();
           if (resultListeners.size() == 0) {
               System.out.println(results);
           } else {
               for (int i = 0; i < resultListeners.size(); i++) {
                   ((ResultsListener) resultListeners.get(i)).notifyNewResults(
                       results);
               }
           }
        }
     }
 
     public void setConnectionReader(AbstractConnectionReader acr) {
         this.acr = acr;
     }
     
     public void queryDone() {
 	synchronized(this) {
 	    m_stop = System.currentTimeMillis();
 	    notify();
             if (!silent && m_start != 0) {
                 System.err.println("Total time: " + 
 				   (m_stop - m_start)/1000.0 + " sec.");
 	    }
 	}
     }
     
     public void notifyFinalResult(int id) {
         queryDone();
         if (resultListeners.size() > 0) {
             for (int i = 0; i < resultListeners.size(); i++) {
                 ((ResultsListener) resultListeners.get(i)).notifyFinal();
             }
         }
     }
 
     public void errorMessage(int id, String err) {
         if (resultListeners.size() == 0) {
 	    System.err.println();
             System.err.println("SimpleClient Received Error: " + err);
 	    // Wake up main thread which is holding the mutex on this 
 	    // object, must wake up otherwise we deadlock in queryDone
 	    // main thread may be waiting to get the server id
 
 	    // note we may get an error during creation of connection
 	    // manager, if so, don't report the query error
 	    if(cm != null)
 		cm.queryError(id);
 	    queryDone(); // prints time used
 	}
         else {
 	    queryDone();
             for (int i = 0; i < resultListeners.size(); i++) {
                 ((ResultsListener) resultListeners.get(i)).notifyError(err);
             }
         }
 	System.exit(-1);
     }
 
     protected static void usage() {
         System.err.println(
             "Usage: SimpleClient -qf QueryFileName(s) [-port port] \n");
                                         //       "[-h host] [-t timeout]" +
           //  + "[-x repetitions] [-d delay] [-w wait] [-o outputFileName]\n"
           //  + "[-quiet] [-prepare | -explain] [-execute-prepared queryId] [-set \"tunable=value\"] [-ch client host] [-cp client port] [-silent]");
         System.exit(-1);
     }
 
     protected static ArrayList<Request> requests = new ArrayList<Request>();
     protected static boolean queryfile = false;
     protected static String host;
     protected static int port;
     
     public enum RequestType {
         /** Run this query */
         RUN,
         /** Just show the output of the optimizer for the query */
         EXPLAIN,
         /** Prepare a query for later execution */
         PREPARE,
         /** Execute a prepared query */
         EXECUTE_PREPARED,
         /** Set a tunable parameter */
         SET_TUNABLE
     }
     
     private static class Request {
         RequestType type;
         String value;
         int repetitions;
         int delay;
         int wait;
     };
     
     protected static boolean silent;
     // XXX vpapad: ugly... SimpleClient can't do this yet
     protected static String outputFileName;
     // XXX vpapad: even uglier... these only apply to MQPClient
     protected static boolean quiet;
     protected static String clientHost = "localhost";
     protected static int clientPort = 3020;
     
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
             } else if (args[i].equals("-port")) {
                 port = Integer.parseInt(args[i + 1]);
                 i += 2;
             } else if (args[i].equals("-t")) {
                 timeout = Integer.parseInt(args[i + 1]);
                 i += 2;
             } else if (args[i].equals("--help")) {
                 usage();
             } else if (args[i].equals("-qf")) {
                 Request req = new Request();
                 requests.add(req);
                 req.type = RequestType.RUN;
                 req.value = args[i + 1];
                 req.repetitions = 1;
                 req.delay = 0;
                 req.wait = 0;
                 i += 2;
             } else if (args[i].equals("-x")) {
                 requests.get(requests.size() - 1).repetitions = 
                     Integer.parseInt(args[i + 1]);
                 i += 2;
             } else if (args[i].equals("-d")) {
                 requests.get(requests.size() - 1).delay = 
                     Integer.parseInt(args[i + 1]);
                 i += 2;
             } else if (args[i].equals("-w")) {
                 requests.get(requests.size() - 1).wait = 
                     Integer.parseInt(args[i + 1]);
                 i += 2;
             } else if (args[i].equals("-o")) {
                 outputFileName = args[i + 1];
                 i += 2;
             } else if (args[i].equals("-quiet")) {
                 quiet = true;
                 i++;
             } else if (args[i].equals("-explain")) {
                 if (requests.size() == 0)
                         usage();
                 Request req = requests.get(requests.size() - 1);
                 req.type = RequestType.EXPLAIN;
                 i += 1;
             } else if (args[i].equals("-prepare")) {
                 if (requests.size() == 0)
                         usage();
                 Request req = requests.get(requests.size() - 1);
                 req.type = RequestType.PREPARE;
                 i += 1;
             } else if (args[i].equals("-execute-prepared")) {
                 Request req = new Request();
                 requests.add(req);
                 req.type = RequestType.EXECUTE_PREPARED;
                 req.value = args[i + 1];
                 req.repetitions = 1;
                 req.delay = 0;
                 req.wait = 0;
                 i += 2;
             } else if (args[i].equals("-set")) {
                 Request req = new Request();
                 requests.add(req);
                 req.type = RequestType.SET_TUNABLE;
                 req.value = args[i + 1];
                 req.repetitions = 1;
                 req.delay = 0;
                 req.wait = 0;
                 i += 2;
             } else if (args[i].equals("-ch")) {
                 clientHost = args[i + 1];
                 i += 2;
             } else if (args[i].equals("-cp")) {
                 clientPort = Integer.parseInt(args[i + 1]);
                 i += 2;
             } else if (args[i].equals("-silent")) {
                 silent = true;
                 i++;
             } else {
                 usage();
             }
 
             if (starti == i) {
                 usage();
             }
         }
 	if(requests.size() == 0)
 	    usage();
     }
     
     protected void processQueries() {
         String query = null;
 
         try {
 	    int nQueries = requests.size();
 	    for (int i = 0; i < nQueries; i++) {
 	        Request req = requests.get(i);
 	        switch (req.type) {
 	            case RUN:
 	            case EXPLAIN:
 	            case PREPARE:
 	                char cbuf[] = new char[MAX_QUERY_LEN];
 	                query = getQueryFromFile((String) req.value, cbuf);
                         break;
 	            case EXECUTE_PREPARED:
 	            case SET_TUNABLE:
 	                query = req.value;
                         break;
                     // XXX vpapad: Can we throw PEException on the client?!
 	        }
 	        
 	        int reps = req.repetitions;
 	        int delay = req.delay;
 	        int wait = req.wait;
 	        for (int j = 0; j < reps; j++) {
 	            if (j > 0 && delay > 0)
 	                Thread.sleep(delay);
 	            // kt - don't think this needs to be synch
 	            // I removed it - hope this doesn't cause trouble!
 	            // if it is synchronized, causes deadlock on error
 	            synchronized (this) {
 	                processQuery(req.type, query);
 	                wait();
 	            }
 	        }
 	        if (i < nQueries - 1 && wait > 0)
 	            Thread.sleep(wait);
 	        
 	    }
 	    System.exit(0);
         } catch (IOException e) {
 			      ecode = true;
             System.err.println(
                 "SimpleClient: IO error while reading query or query file");
             e.printStackTrace();
             System.exit(-1);
         } catch (InterruptedException e) {
 			ecode = true;
 	    System.err.println("Unexpected Thread Interruption: " + e.getMessage());
 	} catch (ClientException ce) {
 			ecode = true;
 	    System.err.println("Error processing query: " + ce.getMessage());
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
        int qLen = 0;
        StringBuffer queryString = new StringBuffer();

        while (qLen != -1) {
                for (int i=0; i<MAX_QUERY_LEN; i++)
                        cbuf[i] = '\0';
                qLen = br.read(cbuf, 0, MAX_QUERY_LEN);

                if (qLen == MAX_QUERY_LEN) {
                        System.out.println(
                                        "Query exceeds maximum length (" + MAX_QUERY_LEN + ")");
                        return null;
                } else {
                        if (qLen != -1)
                                queryString.append(cbuf, 0, qLen);
                        else
                                queryString.append(cbuf);

                }
         }
        return queryString.toString();
 
     }
 
     public void processQuery(RequestType type, String fullQueryText) 
         throws ClientException {
         // remove any leading or trailing whitespace
         String queryText = fullQueryText.trim();
         if (!silent) {
             System.err.println("Executing query: " + queryText);
             System.err.flush(); // make query print before results
         }
         if (queryText.equalsIgnoreCase(ConnectionManager.RUN_GC)) {
             System.out.println("Requesting garbage collection");
             cm.runSpecialFunc(ConnectionManager.RUN_GC);
             return;
         } else if (queryText.equalsIgnoreCase(ConnectionManager.SHUTDOWN)) {
             System.out.println("Shutting down server");
             cm.runSpecialFunc(ConnectionManager.SHUTDOWN);
             return;
         } else if (queryText.equalsIgnoreCase(ConnectionManager.DUMPDATA)) {
             System.out.println("Requesting profile data dump");
             cm.runSpecialFunc(ConnectionManager.DUMPDATA);
             return;
         }
 
         Query q = null;
         if (type == RequestType.EXPLAIN)
             q = new ExplainQPQuery(queryText);
         else if (type == RequestType.PREPARE)
             q = new PrepareQPQuery(queryText);
         else if (type == RequestType.EXECUTE_PREPARED)
             q = new ExecutePreparedQPQuery(queryText);
         else if (type == RequestType.SET_TUNABLE)
             q = new SetTunable(queryText);
         else
             q = QueryFactory.makeQuery(queryText);
         m_start = System.currentTimeMillis();
         final int id = cm.executeQuery(q, Integer.MAX_VALUE);
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
