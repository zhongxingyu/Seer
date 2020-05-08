 package com.cee.news.server.content;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 import com.cee.news.client.content.SiteData;
 import com.cee.news.client.content.SiteData.SiteRetrivalState;
 import com.cee.news.client.content.SiteUpdateService;
 import com.cee.news.client.error.ServiceException;
 import com.cee.news.model.EntityKey;
 import com.cee.news.model.Site;
 import com.cee.news.model.WorkingSet;
 import com.cee.news.parser.SiteParser;
 import com.cee.news.store.SiteStore;
 import com.cee.news.store.StoreException;
 import com.cee.news.store.WorkingSetStore;
 
 public abstract class SiteUpdateServiceImpl implements SiteUpdateService {
 
 	private static final String UPDATE_SCHEDULER_THREAD_PREFIX = "updateScheduler";
 
 	private static final String SITE_UPDATER_THREAD_PREFIX = "siteUpdater";
 
 	private static final long serialVersionUID = 8695157160684778713L;
 
 	private static final String COULD_NOT_RETRIEVE_SITE = "Could not retrieve site: {}";
 	
 	private static final String COULD_NOT_RETRIEVE_SITE_MSG = "Could not retrieve site";
 	
 	private static final Logger LOG = LoggerFactory.getLogger(SiteUpdateServiceImpl.class);
 	
 	private int corePoolSize; 
 	
 	private int maxPoolSize;
 	
 	private long keepAliveTime;
 	
 	private long updateSchedulerFixedDelay;
 	
 	private ThreadPoolExecutor pool;
 	
 	private List<String> sitesInProgress = new ArrayList<String>();
 
 	private SiteStore siteStore;
 
 	private WorkingSetStore workingSetStore;
 
 	private ScheduledExecutorService scheduler;
 
 	private List<Runnable> runnablesInProgress = new ArrayList<Runnable>();
 
 	private LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
 	
 	public void setSiteStore(SiteStore siteStore) {
 		this.siteStore = siteStore;
 	}
 
 	public void setWorkingSetStore(WorkingSetStore workingSetStore) {
 		this.workingSetStore = workingSetStore;
 	}
 
 	/**
 	 * @return the minimum thread pool size
 	 */
 	public int getCorePoolSize() {
 		return corePoolSize;
 	}
 
 	public void setCorePoolSize(int corePoolSize) {
 		this.corePoolSize = corePoolSize;
 	}
 
 	/**
 	 * @return the maximum thread pool size
 	 */
 	public int getMaxPoolSize() {
 		return maxPoolSize;
 	}
 
 	public void setMaxPoolSize(int maxPoolSize) {
 		this.maxPoolSize = maxPoolSize;
 	}
 
 	/**
 	 * @return time in milliseconds an unused thread is kept alive if the pool size exceeds the core pool size
 	 */
 	public long getKeepAliveTime() {
 		return keepAliveTime;
 	}
 
 	public void setKeepAliveTime(long keepAliveTime) {
 		this.keepAliveTime = keepAliveTime;
 	}
 
 	/**
 	 * @return delay in seconds between update scheduler runs
 	 */
 	public long getUpdateSchedulerFixedDelay() {
 		return updateSchedulerFixedDelay;
 	}
 
 	public void setUpdateSchedulerFixedDelay(long updateSchedulerFixedDelay) {
 		this.updateSchedulerFixedDelay = updateSchedulerFixedDelay;
 	}
 	
 	private synchronized void ensureThreadPool() {
 		if (pool == null) {
 			pool = new ThreadPoolExecutor(
 					corePoolSize, 
 					maxPoolSize, 
 					keepAliveTime,
 					TimeUnit.MILLISECONDS, 
 					workQueue) {
 				
 					@Override
 					protected void afterExecute(Runnable r, Throwable t) {
 						super.afterExecute(r, t);
 						SiteUpdateServiceImpl.this.removeRunnable(r);
 					}
 			};
 			pool.setThreadFactory( new PrefixCountThreadFactory(SITE_UPDATER_THREAD_PREFIX) );
 		}
 	}
 
 	private synchronized void removeSite(String siteName) {
 		if (!sitesInProgress.remove(siteName)) {
 			LOG.warn("{} could not be removed from list of sites in progress", siteName);
 		}
 	}
 	
 	private synchronized void removeRunnable(Runnable runnable) {
 		if (!runnablesInProgress.remove(runnable)) {
 			LOG.warn("{} could not be removed from list of runnables", runnable);
 		}
 	}
 
 	@Override
 	public synchronized int addSiteToUpdateQueue(final String siteKey) {
 		ensureThreadPool();
 		if (!sitesInProgress.contains(siteKey)) {
 			Site siteEntity = null;
 			try {
 				siteEntity = siteStore.getSite(siteKey);
 			} catch (StoreException e) {
 				LOG.error(COULD_NOT_RETRIEVE_SITE, siteKey);
 				LOG.error(COULD_NOT_RETRIEVE_SITE, e);
 				
 				throw new ServiceException(COULD_NOT_RETRIEVE_SITE_MSG);
 			}
 			SiteUpdateCommand command = createSiteUpdateCommand();
 			command.addCommandCallback(new CommandCallback() {
 				
 				@Override
 				public void notifyFinished() {
 					removeSite(siteKey);
 				}
 				
 				@Override
 				public void notifyError(Exception ex) {
 					LOG.error("Site update for " + siteKey + " encountered an error", ex);
 					//TODO: how to handle error reporting for the user?
 				}
 			});
 			command.setSite(siteEntity);
 			sitesInProgress.add(siteKey);
 			runnablesInProgress.add(command);
 			pool.execute(command);
 		}
 		return sitesInProgress.size();
 	}
 
 	@Override
 	public synchronized int addSitesOfWorkingSetToUpdateQueue(String workingSetName) {
 		try {
 			WorkingSet ws = workingSetStore.getWorkingSet(workingSetName);
 			if (ws == null)
 				throw new IllegalArgumentException("Unknown working set: " + workingSetName);
 			for (EntityKey siteKey : ws.getSites()) {
 				addSiteToUpdateQueue(siteKey.getKey());
 			}
 			return sitesInProgress.size();
 		} catch (StoreException se) {
 			throw new ServiceException(se.toString());
 		}
 	}
 
 	@Override
 	public synchronized int getUpdateTasks() {
 		int taskCount = 0;
 		for (Runnable runnable : runnablesInProgress) {
 			if (runnable instanceof AbstractCommand) {
 				taskCount += ((AbstractCommand)runnable).getRemainingTasks();
 			} else {
 				taskCount++;
 			}
 		}
 		return taskCount;
 	}
 
 	@Override
 	public synchronized void clearQueue() {
 		LOG.info("Clearing work queue");
 		workQueue.clear();
 		runnablesInProgress.clear();
 		sitesInProgress.clear();
 	}
 
 	@Override
 	public SiteData retrieveSiteData(String location) {
 		SiteParser parser = createSiteParser();
 		SiteData info = new SiteData();
 		info.setIsNew(true);
 		URL locationUrl = null;
 		try {
 			locationUrl = new URL(location);
 		} catch (MalformedURLException e) {
 			info.setState(SiteRetrivalState.malformedUrl);
 			return info;
 		}
 		try {
 			Site site = parser.parse(locationUrl);
 			info = SiteConverter.createFromSite(site);
 			info.setState(SiteRetrivalState.ok);
 		} catch (IOException e) {
 			info.setState(SiteRetrivalState.ioError);
 		} catch (SAXException e) {
 			info.setState(SiteRetrivalState.parserError);
 		}
 		return info;
 	}
 	
 	@Override
 	public synchronized void startUpdateScheduler() {
 		if (scheduler == null) {
 			LOG.info("Starting update scheduler with a delay of {}.", updateSchedulerFixedDelay);
 			scheduler = new ScheduledThreadPoolExecutor(1, new PrefixCountThreadFactory(UPDATE_SCHEDULER_THREAD_PREFIX));
 			scheduler.scheduleWithFixedDelay(
 					new Runnable() {		
 						@Override
 						public void run() {
 							startSiteUpdates();
 						}
 					}, 
 					updateSchedulerFixedDelay, 
 					updateSchedulerFixedDelay,
 					TimeUnit.MINUTES);
 		}
 	}
 
 	private void startSiteUpdates() {
 		try {
 			LOG.info("Scheduler starting site updates");
 			List<EntityKey> sites = siteStore.getSitesOrderedByName();
 			for (EntityKey entityKey : sites) {
 				addSiteToUpdateQueue(entityKey.getKey());
 			}
 		} catch (Throwable t) {
 			LOG.error("Update scheduler encountered an error", t);
 		}
 	}
 
 	/**
 	 * Stops execution of the thread pool
 	 */
 	public synchronized void shutdown() {
 		if (pool != null) {
 			pool.shutdownNow();
 		}
 		if (scheduler != null) {
 			scheduler.shutdownNow();
 		}
 	}
 
 	/**
 	 * @return A update site command prepared with all dependencies
 	 */
 	protected abstract SiteUpdateCommand createSiteUpdateCommand();
 
 	/**
 	 * @return A site parser prepared with all dependencies
 	 */
 	protected abstract SiteParser createSiteParser();
 }
