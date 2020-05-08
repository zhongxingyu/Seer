 package eu.socialsensor.sfc.streams.management;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 import eu.socialsensor.framework.common.domain.Feed;
 import eu.socialsensor.framework.streams.Stream;
 import eu.socialsensor.framework.streams.StreamConfiguration;
 import eu.socialsensor.framework.streams.StreamException;
 import eu.socialsensor.sfc.streams.StreamsManagerConfiguration;
 import eu.socialsensor.sfc.streams.input.FeedsCreatorImpl.ConfigFeedsCreator;
 import eu.socialsensor.sfc.streams.input.FeedsCreatorImpl.MongoFeedCreator;
 import eu.socialsensor.sfc.streams.monitors.StreamsMonitor;
 
 
 /**
  * Thread-safe class for retrieving content according to 
  * keywords - user - location feeds from social networks
  * (Twitter,Youtube,Facebook,Flickr,Instagram,Tumblr,GooglePlus)
  * 
  * @author manosetro
  * @email  manosetro@iti.gr
  * @author ailiakop
  * @email  ailiakop@iti.gr
  */
 public class StreamsManager {
 	
 	public final Logger logger = Logger.getLogger(StreamsManager.class);
 	
 	enum ManagerState {
 		OPEN, CLOSE
 	}
 
 	private Map<String, Stream> streams = null;
 	private Map<String, Stream> subscribers = null;
 	private StreamsManagerConfiguration config = null;
 	private StoreManager storeManager;
 	private ConfigFeedsCreator configFeedsCreator;
 	private MongoFeedCreator mongoFeedsCreator;
 	private StreamsMonitor monitor;
 	private ManagerState state = ManagerState.CLOSE;
 	private int numberOfConsumers = 1; //for multi-threaded items' storage
 
 	private List<Feed> feeds = new ArrayList<Feed>();
 
 	public StreamsManager(StreamsManagerConfiguration config) throws StreamException {
 
 		if (config == null) {
 			throw new StreamException("Manager's configuration must be specified");
 		}
 
 		this.config = config;
 		
 		//Set up the Subscribers
 		initSubscribers();
 		
 		//Set up the Streams
 		initStreams();
 		//If there are Streams to monitor start the StreamsMonitor
 		if(streams != null && !streams.isEmpty()){
 			monitor = new StreamsMonitor(streams.size());
 		}
 
 		Runtime.getRuntime().addShutdownHook(new Shutdown(this));
 	}
 	/**
 	 * Opens Manager by starting the auxiliary modules and setting up
 	 * the database for reading/storing
 	 * @throws StreamException
 	 */
 	public synchronized void open() throws StreamException {
 		
 		if (state == ManagerState.OPEN) {
 			return;
 		}
 		state = ManagerState.OPEN;
 		logger.info("Streams are now open");
 		
 		try {
 			//Start store Manager 
 			storeManager = new StoreManager(config, numberOfConsumers);
 			storeManager.start();	
 			logger.info("Store Manager is ready to store.");
 			
 			//Start the Subscribers
 			for(String subscriberId : subscribers.keySet()){
 				logger.info("Stream Manager - Start Subscriber : "+subscriberId);
 				StreamConfiguration srconfig = config.getSubscriberConfig(subscriberId);
 				Stream stream = subscribers.get(subscriberId);
 				stream.setHandler(storeManager);
 				stream.setAsSubscriber();
 				stream.open(srconfig);
 				
 				/**
 				 * Here is to add the query builder that is a different instance for subscribers and streams
 				 * The output will be the input feeds - if input feeds are null or none for the subscribers, 
 				 * the subscribers just trace messages of general content 
 				 */
 				//track with news hounds from mongo - temporary solution
 				//Input - This will change
 				mongoFeedsCreator = new MongoFeedCreator(config);
 				mongoFeedsCreator.setTypeOfStream(subscriberId);
 				mongoFeedsCreator.extractFeedInfo();
 				feeds = mongoFeedsCreator.createFeeds();
 				stream.setUserLists(mongoFeedsCreator.usersToLists);
 				stream.stream(feeds);
 			}
 			
 			//Start the Streams
 			for (String streamId : streams.keySet()) {
 				logger.info("Stream Manager - Start Stream : "+streamId);
 				StreamConfiguration sconfig = config.getStreamConfig(streamId);
 				Stream stream = streams.get(streamId);
 				stream.setHandler(storeManager);
 				stream.open(sconfig);
 				
 				/**
 				 * Here is to add the query builder that is a different instance for subscribers and streams
 				 * The output will be the input feeds - if input feeds are null or none for the streams, 
 				 * the streams close
 				 */
 			
 				//track with data from config file
 				configFeedsCreator = new ConfigFeedsCreator(sconfig);
 				configFeedsCreator.extractFeedInfo();
 				
 				feeds = configFeedsCreator.createFeeds();
 				
 				//track with news hounds from mongo - temporary solution
 				//Input - This will change
 				//mongoFeedsCreator = new MongoFeedCreator(config);
 				//mongoFeedsCreator.setTypeOfStream(streamId);
 				//mongoFeedsCreator.extractFeedInfo();
 				//feeds = mongoFeedsCreator.createFeeds();
 				
 				if(feeds.isEmpty()){
 					logger.error("No feeds for Stream : "+streamId);
 					logger.error("Close Stream : "+streamId);
 					stream.close();
 					continue;
 				}
 				
 				monitor.addStream(streamId, stream, feeds);
 				monitor.startStream(streamId);
 			}
 			
 			if(monitor != null && monitor.getNumberOfStreamFetchTasks() > 0){
 				monitor.startReInitializer();
 			}
 
 		}catch(Exception e) {
 			e.printStackTrace();
 			throw new StreamException("Error during streams open", e);
 		}
 	}
 	
 	/**
 	 * Closes Manager along with its auxiliary modules
 	 * @throws StreamException
 	 */
 	public synchronized void close() throws StreamException {
 		
 		if (state == ManagerState.CLOSE) {
 			return;
 		}
 		
 		try{
 			for (Stream stream : streams.values()) {
 				stream.close();
 			}
 			
 			if (storeManager != null) {
 				storeManager.stop();
 			}
 			
 			state = ManagerState.CLOSE;
 		}catch(Exception e) {
 			throw new StreamException("Error during streams close",e);
 		}
 	}
 	
 	/**
 	 * Initializes the streams that correspond to the wrappers 
 	 * that are used for multimedia retrieval
 	 * @throws StreamException
 	 */
 	private void initStreams() throws StreamException {
 		streams = new HashMap<String,Stream>();
 		try{
 			for (String streamId : config.getStreamIds()){
 				StreamConfiguration sconfig = config.getStreamConfig(streamId);
 				streams.put(streamId,(Stream)Class.forName(sconfig.getParameter(StreamConfiguration.CLASS_PATH)).newInstance());
 			}
 		}catch(Exception e) {
 			e.printStackTrace();
 			throw new StreamException("Error during streams initialization",e);
 		}
 	}
 	
 	private void initSubscribers() throws StreamException {
 		subscribers = new HashMap<String,Stream>();
 		try{
 			for (String subscriberId : config.getSubscriberIds()){
 				StreamConfiguration sconfig = config.getSubscriberConfig(subscriberId);
 				subscribers.put(subscriberId,(Stream)Class.forName(sconfig.getParameter(StreamConfiguration.CLASS_PATH)).newInstance());
 			}
 		}catch(Exception e) {
 			e.printStackTrace();
 			throw new StreamException("Error during streams initialization",e);
 		}
 	}
 
 	/**
 	 * Class in case system is shutdown 
 	 * Responsible to close all services 
 	 * that are running at the time being
 	 * @author ailiakop
 	 *
 	 */
 	
 	private class Shutdown extends Thread {
 		StreamsManager manager = null;
 
 		public Shutdown(StreamsManager manager) {
 			this.manager = manager;
 		}
 
 		public void run() {
 			System.out.println("Shutting down stream manager...");
 			if (manager != null) {
 				try {
 					manager.close();
 				} catch (StreamException e) {
 					e.printStackTrace();
 				}
 			}
 			System.out.println("Done...");
 		}
 	}
 	
	public static void main(String[] args) {
		try {
			
			File configFile;
			
			if(args.length != 1 ) {
				configFile = new File("./conf/streams.conf.xml");
				
			}
			else {
				configFile = new File(args[0]);
			
			}
			
			StreamsManagerConfiguration config = StreamsManagerConfiguration.readFromFile(configFile);		
				
	        
			StreamsManager streamsManager = new StreamsManager(config);
			streamsManager.open();
		
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (StreamException e) {
			e.printStackTrace();
		}
	}
	
 }
