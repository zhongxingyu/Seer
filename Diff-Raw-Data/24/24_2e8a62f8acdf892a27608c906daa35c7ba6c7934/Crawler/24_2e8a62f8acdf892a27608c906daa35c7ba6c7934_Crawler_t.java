 package com.fatwire.dta.sscrawler;
 
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Executor;
 
 import com.fatwire.dta.sscrawler.domain.HostConfig;
 import com.fatwire.dta.sscrawler.handlers.BodyHandler;
 import com.fatwire.dta.sscrawler.jobs.NullProgressMonitor;
 import com.fatwire.dta.sscrawler.jobs.ProgressMonitor;
 import com.fatwire.dta.sscrawler.reporting.Reporter;
 import com.fatwire.dta.sscrawler.reporting.ReportingListener;
 import com.fatwire.dta.sscrawler.util.SSUriHelper;
 
 /**
  * 
  * 
  * @author Dolf Dijkstra
  * 
  */
 public class Crawler {
 
     
     private HostConfig hostConfig;
 
     private URI startUri;
 
     private int maxPages = Integer.MAX_VALUE;
 
     private List<Reporter> reporters = new LinkedList<Reporter>();;
 
     private SSUriHelper uriHelper;
 
     private Executor executor;
 
     private ProgressMonitor progressMonitor;
 
     public void work() {
 
         final RenderCommand command = new RenderCommand(hostConfig, maxPages, executor);
         command.addStartUri(uriHelper.uriToQueryString(startUri));
         command.setUriHelper(uriHelper);
         command.setHandler(new BodyHandler(uriHelper));
 
         final ReportingListener reportingListener = new ReportingListener();
 
         for (Reporter reporter : reporters) {
             reportingListener.addReporter(reporter);
         }
 
         command.addListener(reportingListener);
 
         for (Reporter reporter : reporters) {
             reporter.startCollecting();
         }
         if (this.progressMonitor == null) {
             progressMonitor = new NullProgressMonitor();
         }
         command.execute(progressMonitor);
 
         for (Reporter reporter : reporters) {
             reporter.endCollecting();
         }
 
     }
 
 
     /**
      * @return the maxPages
      */
     public int getMaxPages() {
         return maxPages;
     }
 
     /**
     * @param max the maxPages to set
      */
     public void setMaxPages(int max) {
         if (max < 1)
             throw new IllegalArgumentException("max should be greater then 0");
         this.maxPages = max;
     }
 
     /**
      * @return the startUri
      */
     public URI getStartUri() {
         return startUri;
     }
 
     /**
      * @param startUri the startUri to set
      */
     public void setStartUri(URI startUri) {
         this.startUri = startUri;
     }
 
     /**
      * @return the reporters
      */
     public List<Reporter> getReporters() {
         return reporters;
     }
 
     /**
      * @param reporters the reporters to set
      */
     public void setReporters(List<Reporter> reporters) {
         this.reporters = reporters;
     }
 
     /**
      * @return the executor
      */
     public Executor getExecutor() {
         return executor;
     }
 
     /**
      * @param executor the executor to set
      */
     public void setExecutor(Executor executor) {
         this.executor = executor;
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
     public void setUriHelper(SSUriHelper uriHelper) {
         this.uriHelper = uriHelper;
     }
 
     /**
      * @return the progressMonitor
      */
     public ProgressMonitor getProgressMonitor() {
         return progressMonitor;
     }
 
     /**
      * @param progressMonitor the progressMonitor to set
      */
     public void setProgressMonitor(ProgressMonitor progressMonitor) {
         this.progressMonitor = progressMonitor;
     }
 
     public void setHostConfig(HostConfig hostConfig) {
         this.hostConfig = hostConfig;
     }
 
     public HostConfig getHostConfig() {
         return hostConfig;
     }
 
 
 }
