 package com.couchbase.demo;
 
 import com.couchbase.client.CouchbaseClient;
 import java.io.IOException;
import java.net.SocketAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author ingenthr
  */
 public class CouchbaseCheck {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, ExecutionException {
         CouchbaseCheck tester = new CouchbaseCheck();
         String uriArgs[] = {"localhost"};
         int runningTime = 45;
         int iterationsPerThread = 101;
         if (args.length == 1) {
             runningTime = Integer.parseInt(args[0]);
             iterationsPerThread = Integer.parseInt(args[1]);
         } else if (args.length > 2) {
            uriArgs = new String[args.length - 2];
            System.arraycopy(args, 2, uriArgs, 0, args.length - 2);
         }
 
         List<URI> servers = new ArrayList<URI>();
         for (String arg : uriArgs) {
             servers.add(new URI("http://" + arg + ":8091/pools"));
         }
 
         tester.testThem(servers, runningTime, iterationsPerThread);
     }
 
     public void testThem(List<URI> servers, int time, int iterations) throws IOException, InterruptedException, ExecutionException {
 
         // Tell spy to use the SunLogger
         Properties systemProperties = System.getProperties();
         systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
         System.setProperties(systemProperties);
 
         Logger.getLogger("net.spy.memcached").setLevel(Level.ALL);
 
         //get the top Logger
         Logger topLogger = java.util.logging.Logger.getLogger("");
 
         // Handler for console (reuse it if it already exists)
         Handler consoleHandler = null;
         //see if there is already a console handler
         for (Handler handler : topLogger.getHandlers()) {
             if (handler instanceof ConsoleHandler) {
                 //found the console handler
                 consoleHandler = handler;
                 break;
             }
         }
 
         if (consoleHandler == null) {
             //there was no console handler found, create a new one
             consoleHandler = new ConsoleHandler();
             topLogger.addHandler(consoleHandler);
         }
 
         //set the console handler to fine:
         consoleHandler.setLevel(java.util.logging.Level.CONFIG);
 
         long preconnect = System.nanoTime();
         CouchbaseClient cbc = new CouchbaseClient(servers, "default", "");
         long postconnect = System.nanoTime();
         long connectTime = postconnect - preconnect;
         Logger.getLogger(SleepingTester.class.getName()).log(Level.INFO,
                 "Took " + (double) connectTime / 1000000.0 + "ms to connect.");
 
 
         ExecutorService threadPool = Executors.newFixedThreadPool(10);
         // TODO: make the threads run forever and report on threadpool shutdown
         threadPool.submit(new SleepingTester(cbc, 101));
         threadPool.submit(new SleepingTester(cbc, 101));
         threadPool.submit(new SleepingTester(cbc, 101));
         threadPool.submit(new SleepingTester(cbc, 101));
         threadPool.submit(new SleepingTester(cbc, 101));
 
         threadPool.awaitTermination(time, TimeUnit.SECONDS);
         threadPool.shutdown();
//        Map<SocketAddress, Map<String, String>> stats = cbc.getStats("timings");
//
//        StringBuilder statsString = new StringBuilder();
//
//        for ( Map.Entry<SocketAddress, Map<String,String>> node: stats.entrySet()) {
//             statsString.append("For node ").append(node.getKey()).append("\n");
//             for (Map.Entry<String,String> stat : node.getValue().entrySet()) {
//                 statsString.append("stat ").append(stat.getKey());
//                 statsString.append(" value ").append(stat.getValue()).append("\n");
//             }
//
//        }
//
//        Logger.getLogger(SleepingTester.class.getName()).log(Level.INFO,
//                "Server timing stats are: " + statsString);


         cbc.shutdown(10, TimeUnit.SECONDS);
 
     }
 }
