 package com.fatwire.dta.sscrawler;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.Executor;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 
 import com.fatwire.dta.sscrawler.domain.HostConfig;
 import com.fatwire.dta.sscrawler.events.PageletRenderedEvent;
 import com.fatwire.dta.sscrawler.events.PageletRenderingListener;
 import com.fatwire.dta.sscrawler.handlers.BodyHandler;
 import com.fatwire.dta.sscrawler.jobs.ProgressMonitor;
 import com.fatwire.dta.sscrawler.util.HelperStrings;
 import com.fatwire.dta.sscrawler.util.SSUriHelper;
 
 import org.apache.commons.httpclient.Cookie;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class URLReaderService {
     private final Log log = LogFactory.getLog(getClass());
 
     private volatile boolean stopped = false;
 
     private HostConfig hostConfig;
 
     private SSUriHelper uriHelper;
 
     private BodyHandler handler;
 
     private MultiThreadedHttpConnectionManager connectionManager;
 
     private HttpClientService httpClientService = new HttpClientService() {
         private ThreadLocal<HttpClient> tl = new ThreadLocal<HttpClient>() {
 
             /*
              * (non-Javadoc)
              * 
              * @see java.lang.ThreadLocal#initialValue()
              */
             @Override
             protected HttpClient initialValue() {
                 return initClient();
             }
 
         };
 
         public HttpClient get() {
             return tl.get();
         }
     };
 
     private int maxPages = Integer.MAX_VALUE;
 
     private final Set<PageletRenderingListener> listeners = new CopyOnWriteArraySet<PageletRenderingListener>();
 
     private final List<QueryString> startUrls = new ArrayList<QueryString>();
 
     private final Scheduler scheduler;
 
     public URLReaderService(final Executor readerPool) {
         super();
         scheduler = new Scheduler(readerPool);
 
     }
 
     interface HttpClientService {
         HttpClient get();
     }
 
     protected HttpClient initClient() {
         HttpClient client = new HttpClient(connectionManager);
         client.getHostConfiguration().setHost(hostConfig.getHostname(), hostConfig.getPort(), hostConfig.getProtocol());
 
         if (hostConfig.getProxyHost() != null) {
             client.getHostConfiguration().setProxyHost(hostConfig.getProxyHost());
             if (hostConfig.getProxyCredentials() != null) {
                 client.getState().setProxyCredentials(AuthScope.ANY, hostConfig.getProxyCredentials());
             }
         }
 
         client.getParams().setParameter(HttpMethodParams.USER_AGENT, "ss-crawler-0.9");
 
         // RFC 2101 cookie management spec is used per default
         // to parse, validate, format & match cookies
         // client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
         // client.getParams().setCookiePolicy(CookiePolicy.DEFAULT);
         client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
 
         // client.getParams().makeStrict();
         client.getParams().getDefaults().setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, true);
 
         client.getState().addCookie(
                 new Cookie(hostConfig.getHostname(), HelperStrings.SS_CLIENT_INDICATOR, Boolean.TRUE.toString(),
                         hostConfig.getDomain(), -1, false));
         return client;
     }
 
     public void start(final ProgressMonitor monitor) {
 
         connectionManager = new MultiThreadedHttpConnectionManager();
         connectionManager.getParams().setConnectionTimeout(30000);
         connectionManager.getParams().setDefaultMaxConnectionsPerHost(1500);
         connectionManager.getParams().setMaxTotalConnections(30000);
         MBeanServer platform = java.lang.management.ManagementFactory.getPlatformMBeanServer();
         try {
             platform.registerMBean(new ReaderService(scheduler, connectionManager), new ObjectName(
                     "com.fatwire.crawler:name=scheduler"));
         } catch (Throwable t) {
             log.error(t.getMessage(), t);
         }
 
         monitor.beginTask("Crawling on " + hostConfig.toString(), maxPages == Integer.MAX_VALUE ? -1 : maxPages);
         scheduler.monitor = monitor;
         for (final QueryString thingToDo : startUrls) {
 
             scheduler.schedulePage(thingToDo);
         }
         scheduler.waitForlAllTasksToFinish();
         try {
             this.connectionManager.shutdown();
         } catch (Throwable t) {
             log.error(t.getMessage(), t);
         }
         try {
             platform.unregisterMBean(new ObjectName("com.fatwire.crawler:name=scheduler"));
         } catch (Throwable t) {
             log.error(t.getMessage(), t);
         }
 
         monitor.done();
 
     }
 
     class Scheduler {
 
         private final Set<QueryString> urlsDone = new HashSet<QueryString>();
 
         private final Executor executor;
 
         private final CountDownLatch complete = new CountDownLatch(1);
 
         private final AtomicInteger scheduledCounter = new AtomicInteger();
 
         private AtomicInteger count = new AtomicInteger();
 
         private AtomicInteger completeCount = new AtomicInteger();
 
         private ProgressMonitor monitor;
 
        private boolean requestPageData=true;
 
         /**
          * @param executor
          */
         public Scheduler(final Executor readerPool) {
             super();
             executor = readerPool;
         }
 
         synchronized void schedulePage(final QueryString qs) {
             if (monitor.isCanceled()) {
                 return;
             }
 
             if (qs instanceof Link) {
                 if (count.incrementAndGet() > maxPages) {
                     return; // do not schedule beyond max number of pages
                 }
             }
             urlsDone.add(qs);
             final String uri = checkUri(qs);
 
             scheduledCounter.incrementAndGet();
             final UrlRenderingCallable downloader = new UrlRenderingCallable(httpClientService, uri, qs);
 
             try {
                 int priority = 0;
                 if (qs instanceof Link) {
                     priority = 5;
                 }
                 executor.execute(new Harvester(downloader, qs.toString(), monitor, priority, count.get()));
 
             } catch (final Exception e) {
                 scheduledCounter.decrementAndGet();
                 log.error(e.getMessage(), e);
 
             }
 
         }
 
         private String checkUri(final QueryString ssuri) {
             final String uri = uriHelper.toLink(ssuri);
             if (requestPageData) {
                 if (ssuri.getParameters().containsKey(HelperStrings.SS_PAGEDATA_REQUEST) == false) {
                     return uri + "&" + HelperStrings.SS_PAGEDATA_REQUEST + "=true";
                 }
             } else {
                 if (ssuri.getParameters().containsKey(HelperStrings.SS_CLIENT_INDICATOR) == false) {
                     return uri + "&" + HelperStrings.SS_CLIENT_INDICATOR + "=true";
                 }
 
             }
             return uri;
         }
 
         void pageComplete(final ResultPage page) {
 
             synchronized (this) {
                 completeCount.incrementAndGet();
                 for (final QueryString ssUri : page.getMarkers()) {
 
                     if (!urlsDone.contains(ssUri)) {
                         if (log.isDebugEnabled()) {
                             log.debug("adding " + ssUri);
                         }
                         schedulePage(ssUri);
                     }
                 }
                 for (final QueryString ssUri : page.getLinks()) {
                     if (!urlsDone.contains(ssUri)) {
                         if (log.isDebugEnabled()) {
                             log.debug("adding " + ssUri);
                         }
                         schedulePage(ssUri);
                     }
                 }
             }
             final PageletRenderedEvent event = new PageletRenderedEvent(page);
             for (final PageletRenderingListener listener : listeners) {
                 listener.renderPerformed(event);
             }
         }
 
         public void taskFinished() {
             log.debug("Active workers: " + scheduledCounter.get());
             if (scheduledCounter.decrementAndGet() == 0) {
                 complete.countDown();
             }
 
         }
 
         void waitForlAllTasksToFinish() {
             try {
                 complete.await();
             } catch (final InterruptedException e) {
                 log.warn(e, e);
             }
 
         }
 
         public int getCount() {
             return this.count.get();
         }
 
         public int getScheduledCount() {
             return this.scheduledCounter.get();
         }
 
         public int getCompleteCount() {
             return this.completeCount.get();
         }
 
     }
 
     class Harvester implements Runnable, Comparable<Harvester> {
         private final UrlRenderingCallable downloader;
 
         private final String taskInfo;
 
         private final ProgressMonitor monitor;
 
         private final int priority;
 
         private final int orderNumber;
 
         /**
          * @param downloader
          */
         public Harvester(final UrlRenderingCallable downloader, final String taskInfo, final ProgressMonitor monitor,
                 final int priority, int orderNumber) {
             super();
             this.downloader = downloader;
             this.taskInfo = taskInfo;
             this.monitor = monitor;
             this.priority = priority;
             this.orderNumber = orderNumber;
         }
 
         public void run() {
             if (monitor.isCanceled()) {
                 return;
             }
             try {
                 monitor.subTask(taskInfo);
                 final ResultPage page;
                 page = downloader.call();
                 if (page.getBody() != null) {
                     handler.visit(page);
                 }
                 scheduler.pageComplete(page);
             } catch (final Exception e) {
                 log.error(e, e);
             } finally {
                 scheduler.taskFinished();
             }
 
         }
 
         public int compareTo(Harvester o) {
             // comparing on priority and orderNumber, with same priority, the
             // lower order number comes first.
             if (priority != o.priority) {
                 return (priority < o.priority ? -1 : 1);
             } else if (this.orderNumber == o.orderNumber) {
                 return 0;
             } else {
                 return this.orderNumber < o.orderNumber ? -1 : 1;
             }
 
         }
 
     }
 
     /**
     * @return the stopped
      */
     public boolean isStopped() {
         return stopped;
     }
 
     /**
     * @param stopped the stopped to set
      */
     public void stop() {
         stopped = true;
     }
 
     /**
      * @return the maxPages
      */
     public int getMaxPages() {
         return maxPages;
     }
 
     /**
      * @param maxPages the maxPages to set
      */
     public void setMaxPages(final int maxPages) {
         this.maxPages = maxPages;
     }
 
     public void addListener(final PageletRenderingListener listener) {
         listeners.add(listener);
     }
 
     public void removeListener(final PageletRenderingListener listener) {
         listeners.remove(listener);
     }
 
     public void setHostConfig(final HostConfig hostConfig) {
         this.hostConfig = hostConfig;
 
     }
 
     public void addStartUris(final Collection<QueryString> uri) {
         startUrls.addAll(uri);
     }
 
     /**
      * @return the handler
      */
     public BodyHandler getHandler() {
         return handler;
     }
 
     /**
      * @param handler the handler to set
      */
     public void setHandler(final BodyHandler handler) {
         this.handler = handler;
     }
 
     /**
      * @return the uriHelper
      */
     public SSUriHelper getUriHelper() {
         return uriHelper;
     }
 
     /**
      * @param uriHelper the uriHelper to set
      */
     public void setUriHelper(final SSUriHelper uriHelper) {
         this.uriHelper = uriHelper;
     }
 
 }
