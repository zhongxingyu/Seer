 package org.phyous.babelhose.processor;
 
 import com.twitter.hbc.ClientBuilder;
 import com.twitter.hbc.core.Constants;
 import com.twitter.hbc.core.endpoint.DefaultStreamingEndpoint;
 import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
 import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
 import com.twitter.hbc.core.processor.StringDelimitedProcessor;
 import com.twitter.hbc.httpclient.BasicClient;
 import com.twitter.hbc.httpclient.auth.Authentication;
 import com.twitter.hbc.httpclient.auth.OAuth1;
 
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import com.google.common.base.Strings;
 import com.memetix.mst.translate.Translate;
 
 import org.phyous.babelhose.Settings;
 import org.phyous.babelhose.twitter.TwitterStatus;
 
 public class TweetProcessor {
   private static final int NUM_THREADS = 20;
   static {
     // Load Bing translator credentials
     Translate.setClientId(Settings.getKey(Settings.Client.BING, "clientId"));
     Translate.setClientSecret(Settings.getKey(Settings.Client.BING, "clientSecret"));
   }
 
   private String consumerKey;
   private String consumerSecret;
   private String token;
   private String secret;
   private String langFilter;
   private List<String> filterStrings;
   private long processingDelay;
   private int queueSize;
   private int numTweets;
   private Mode mode;
 
   public TweetProcessor(String langFilter, List<String> filterStrings, float qps, int numTweets, Mode mode) {
     this.consumerKey = Settings.getKey(Settings.Client.TWITTER, "consumerKey");
     this.consumerSecret = Settings.getKey(Settings.Client.TWITTER, "consumerSecret");
     this.token = Settings.getKey(Settings.Client.TWITTER, "token");
     this.secret = Settings.getKey(Settings.Client.TWITTER, "secret");
 
     this.langFilter = langFilter;
     this.filterStrings = filterStrings;
     this.processingDelay = (long)(1000.0 / qps);
     this.queueSize = qps > 1 ? (int)qps : 1;
     this.numTweets = numTweets;
     this.mode = mode;
   }
 
   public void run() {
     Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
 
     // Create an appropriately sized blocking queue
     BlockingQueue<String> queue = new LinkedBlockingQueue<String>(queueSize);
 
     // Define our twitter endpoint
     DefaultStreamingEndpoint endpoint;
     if (filterStrings.isEmpty()){
       endpoint = new StatusesSampleEndpoint();
     } else {
       StatusesFilterEndpoint filterEndpoint = new StatusesFilterEndpoint();
       filterEndpoint.trackTerms(filterStrings);
       endpoint = filterEndpoint;
     }
     endpoint.stallWarnings(false);
 
     // Create a new BasicClient. By default gzip is enabled.
     // Setting offerTimeoutMillis = queueSize for StringDelimitedProcessor as a hack to keep fresh
     // tweets showing at low qps.
     BasicClient client = new ClientBuilder()
         .name("babelHoseClient")
         .hosts(Constants.STREAM_HOST)
         .endpoint(endpoint)
         .authentication(auth)
         .processor(new StringDelimitedProcessor(queue, queueSize))
         .build();
 
     // Establish a connection
     client.connect();
     // Set up a thread pool
     ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
 
     for (int msgRead = 0; msgRead < numTweets; msgRead++) {
       if (client.isDone()) {
         System.out.println("Client connection closed unexpectedly: " + client.getExitEvent().getMessage());
         break;
       }
 
       try {
         String msg = queue.poll(5, TimeUnit.SECONDS);
         if (msg == null) {
          System.out.println("Did not receive a message in 5 seconds");
         } else {
           TwitterStatus status = new TwitterStatus(msg);
           Runnable worker;
           switch(mode) {
             case HOSE:
               worker = new TweetPrintRunnable(status, langFilter);
               break;
             case ENTITY:
               worker = new EntityAnalysisRunnable(status);
               break;
             default:
               worker = new TweetPrintRunnable(status, langFilter);
           }
           executor.execute(worker);
         }
         if(processingDelay > 0) {
           Thread.sleep(processingDelay);
         }
       } catch (Exception e) {
         e.printStackTrace();
         break;
       }
     }
 
     // Wait until all threads are finish
     executor.shutdown();
     client.stop();
     while (!executor.isTerminated()) {}
     System.out.printf("The client read %d messages!\n", client.getStatsTracker().getNumMessages());
   }
 
   public enum Mode {
     HOSE,
     ENTITY
   }
 }
