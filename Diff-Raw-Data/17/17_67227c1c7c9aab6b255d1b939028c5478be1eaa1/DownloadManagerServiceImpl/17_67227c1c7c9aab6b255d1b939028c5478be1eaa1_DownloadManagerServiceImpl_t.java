 package aic12.project3.service;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.support.GenericXmlApplicationContext;
 import org.springframework.stereotype.Component;
 
 import aic12.project3.common.beans.SentimentRequest;
 import aic12.project3.service.test.DownloadManagerServiceTestIF;
 
 
 /**
  * Singelton
  * @author haja
  *
  */
 public class DownloadManagerServiceImpl implements DownloadManagerService, 
 		DownloadManagerServiceTestIF {
 
 	private static DownloadManagerServiceImpl instance = new DownloadManagerServiceImpl();
 	private Map<SentimentRequest, DownloadThread> initialDownloadsMap = 
 			Collections.synchronizedMap(
 					new HashMap<SentimentRequest, DownloadThread>());
 	private Set<SentimentRequest> notifyOnDownloadFinishSet = Collections.synchronizedSet(new HashSet<SentimentRequest>());
 	
 	@Autowired
 	private TwitterAPI twitterAPI;
 	
 	private static Logger log;
 	
 	private DownloadManagerServiceImpl() {
 		 log = Logger.getLogger(DownloadManagerServiceImpl.class);
 		 log.debug("constr");
 	}
 	
 	public static DownloadManagerServiceImpl getInstance() {
 		log.debug("getInstance()");
 		return instance;
 	}
 
 	@Override
 	public void startInitialDownload(SentimentRequest req) {
 		DownloadThread thread = new DownloadThread(req);
 		initialDownloadsMap.put(req, thread);
 		thread.start();
 	}
 	
 	@Override
 	public void registerForTwitterStream(SentimentRequest req) {
 		twitterAPI.registerForTwitterStream(req);
 	}
 
 	/**
 	 * Checks if an initial download job/thread is still downloading tweets.
 	 */
 	@Override
 	public boolean isInitialDownloadFinished(SentimentRequest req) {
		log.debug("isInitialDownloadFinished; req: " + req);
		return ! initialDownloadsMap.containsKey(req);
 	}
 
 	@Override
 	public void notifyOnInitialDownloadFinished(SentimentRequest req) {
 		notifyOnDownloadFinishSet.add(req);
 	}
 
 	@Override
 	public void initialDownloadFinished(SentimentRequest req, DownloadThread thread) {
		log.debug("initialDownloadFinished with request " + req);
 		// check if we got the same reference to thread object
 		if(initialDownloadsMap.get(req) != thread) {
			log.warn("initialDownloadFinished called with wrong thread reference!");
 			return; // some thing bad may happen otherwise...
 		}
 		
 		// remove from initalDownloadMap
 		initialDownloadsMap.remove(req);
 		
 		// check if we need to notify about finishing
 		if(notifyOnDownloadFinishSet.contains(req)) {
 			notifyOnDownloadFinishSet.remove(req);
 			// TODO notify that download is finished
 		}
 	}
 
 	
 	// TEST IF methods
 	@Override
 	public void setInitialDownloadsMap(Map<SentimentRequest, DownloadThread> dlMap) {
 		initialDownloadsMap = dlMap;
 	}
 
 	@Override
 	public void setNotifyOnDownloadFinishSet(Set<SentimentRequest> notifySet) {
 		notifyOnDownloadFinishSet = notifySet;
 	}
 
 	@Override
 	public void setTwitterAPI(TwitterAPI twitterAPI) {
 		this.twitterAPI = twitterAPI;
 	}
 
 	
 	public static void recreateInstance() {
 		log.debug("recreateInstance()");
 		instance = new DownloadManagerServiceImpl();
 	}
 
 }
