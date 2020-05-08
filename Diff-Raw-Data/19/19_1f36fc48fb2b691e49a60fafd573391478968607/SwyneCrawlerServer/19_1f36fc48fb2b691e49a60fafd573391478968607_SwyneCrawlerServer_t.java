 package edu.iit.swyne.crawler.server;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.InvalidPropertiesFormatException;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import edu.iit.swyne.crawler.FeedListener;
 import edu.iit.swyne.crawler.Indexer;
 
 public class SwyneCrawlerServer {
 	
 	private final static String DEFAULT_INDEXER = "edu.iit.swyne.crawler.mock.MockIndexer";
 	private final static String DEFAULT_PORT = "6970";
 	private final static String DEFAULT_MAX_THREADS = "5";
 	private final static String DEFAULT_POLLING_INTERVAL_SECS = "3600";
 
 	protected boolean initialized;
 	protected boolean listening;
 	protected Properties props;
 	protected Indexer indexer;
 	
 	private Map<URL, ScheduledFuture<?>> feedTasks = Collections.synchronizedMap(new HashMap<URL, ScheduledFuture<?>>());
 	private ScheduledExecutorService scheduler;
 
 	public SwyneCrawlerServer(Properties props) {
 		Properties defaultProps = new Properties();
 		defaultProps.setProperty("server.port", DEFAULT_PORT);
 		defaultProps.setProperty("server.maxThreads", DEFAULT_MAX_THREADS);
 		defaultProps.setProperty("feeds.pollingInterval", DEFAULT_POLLING_INTERVAL_SECS);
 		defaultProps.setProperty("indexer.class", DEFAULT_INDEXER);
 		
		this.props = new Properties(defaultProps);
 		this.props.putAll(props);
 	}
 	
 	public SwyneCrawlerServer() {
 		this(new Properties());
 	}
 
 	public synchronized void init() {
 		if(!this.initialized) {
 			this.initialized = true;
 			this.scheduler = Executors.newScheduledThreadPool(Integer.parseInt(props.getProperty("server.maxThreads")));
 			try {
 				this.indexer = (Indexer) Class.forName(props.getProperty("indexer.class", "edu.iit.swyne.crawler.mock.MockIndexer")).newInstance();
 			} catch (InstantiationException e) {
 				System.err.println(e.getMessage());
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				System.err.println(e.getMessage());
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				System.err.println(e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public synchronized Indexer getIndexer() {
 		return indexer;
 	}
 
 	public synchronized boolean isRunning() {
 		return listening;
 	}
 
 	public synchronized void shutdown() {
 		this.listening = false;
 		scheduler.shutdown();
 		try {
 			scheduler.awaitTermination(60, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			System.err.println("ERROR: "+e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	public synchronized void start() {
 		if(!this.isInitialized()) {
 			this.init();
 		}
 		this.listening = true;
 	}
 
 	private synchronized boolean isInitialized() {
 		return initialized;
 	}
 
 	public synchronized void addFeed(URL feedURL) throws FeedAlreadyTrackedException{
 		if (!this.isRunning()) {
 			this.start();
 		}
 		FeedListener listener = new FeedListener(feedURL);
 		listener.setIndexer(indexer);
 		if (!feedTasks.containsKey(feedURL)) {
 			feedTasks.put(feedURL, scheduler.scheduleWithFixedDelay(listener, 0, Integer.parseInt(props.getProperty("feeds.pollingInterval")), TimeUnit.SECONDS));
 		}
 		else throw new FeedAlreadyTrackedException("Feed " + feedURL.toString() + " has been previously added.");
 	}
 
 	public synchronized int numFeedsTracking() {
 		return feedTasks.size();
 	}
 
 	public synchronized boolean isTrackingFeed(URL feedURL) {
 		return feedTasks.containsKey(feedURL);
 	}
 
 	public synchronized void removeFeed(URL feedURL) {
 		ScheduledFuture<?> feedJob = feedTasks.remove(feedURL);
 		feedJob.cancel(false);
 	}
 	
 	public static void main(String[] args) {
 		Properties props = new Properties();
 		
 		// If a file path is specified on the command-line, load that file into properties
 		if (args.length > 0)
 			try {
 				props.loadFromXML(new FileInputStream(args[0]));
 			} catch (InvalidPropertiesFormatException e) {
 				System.err.println("ERROR: "+e.getMessage());
 				e.printStackTrace();
 			} catch (FileNotFoundException e) {
 				System.err.println("ERROR: "+e.getMessage());
 				e.printStackTrace();
 			} catch (IOException e) {
 				System.err.println("ERROR: "+e.getMessage());
 				e.printStackTrace();
 			}
 			
 		SwyneCrawlerServer server = new SwyneCrawlerServer(props);
 		server.init();
 		server.start();
 	}
 	
 	@SuppressWarnings("serial")
 	public class FeedAlreadyTrackedException extends Exception {
 		public FeedAlreadyTrackedException(String string) {
 			super(string);
 		}
 	}
 }
