 package org.bahmni.feed.openerp.client;
 
 import org.bahmni.feed.openerp.FeedException;
 import org.bahmni.feed.openerp.OpenERPAtomFeedProperties;
 import org.bahmni.feed.openerp.job.Jobs;
 import org.bahmni.feed.openerp.worker.WorkerFactory;
 import org.bahmni.openerp.web.client.OpenERPClient;
 import org.bahmni.webclients.ClientCookies;
 import org.ict4h.atomfeed.client.factory.AtomFeedProperties;
 import org.ict4h.atomfeed.client.repository.AllFailedEvents;
 import org.ict4h.atomfeed.client.repository.AllFeeds;
 import org.ict4h.atomfeed.client.repository.AllMarkers;
 import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
 import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
 import org.ict4h.atomfeed.client.service.AtomFeedClient;
 import org.ict4h.atomfeed.client.service.FeedClient;
 import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
 
 public class AtomFeedClientHelper {
     private OpenERPAtomFeedProperties atomFeedProperties;
     private JdbcConnectionProvider jdbcConnectionProvider;
     private OpenERPClient openERPClient;
     private FeedClientFactory feedClientFactory;
     private AllMarkers allMarkers;
     private AllFeeds allFeeds;
     private AllFailedEvents allFailedEvents;
     private WebClientProvider webClientProvider;
 
     public AtomFeedClientHelper(OpenERPAtomFeedProperties atomFeedProperties, JdbcConnectionProvider jdbcConnectionProvider, OpenERPClient openERPClient) {
         this.atomFeedProperties = atomFeedProperties;
         this.jdbcConnectionProvider = jdbcConnectionProvider;
         this.openERPClient = openERPClient;
         this.webClientProvider = new WebClientProvider(atomFeedProperties);
     }
 
     AtomFeedClientHelper(OpenERPAtomFeedProperties atomFeedProperties, JdbcConnectionProvider jdbcConnectionProvider, OpenERPClient openERPClient, FeedClientFactory feedClientFactory, AllMarkers allMarkersJdbc, AllFeeds allFeeds, AllFailedEvents allFailedEvents,WebClientProvider webClientProvider) {
         this.atomFeedProperties = atomFeedProperties;
         this.jdbcConnectionProvider = jdbcConnectionProvider;
         this.openERPClient = openERPClient;
         this.feedClientFactory = feedClientFactory;
         this.allMarkers = allMarkersJdbc;
         this.allFeeds = allFeeds;
         this.allFailedEvents = allFailedEvents;
         this.webClientProvider = webClientProvider;
     }
 
     public FeedClient getAtomFeedClient(String feedName, Jobs jobName) throws FeedException {
         if(this.feedClientFactory == null){
             WorkerFactory workerFactory = new WorkerFactory(webClientProvider);
             feedClientFactory = new FeedClientFactory(workerFactory);
         }
 
         return getAtomFeedClient(feedName, jobName, feedClientFactory);
     }
 
     FeedClient getAtomFeedClient(String feedName, Jobs jobName, FeedClientFactory feedClientFactory) throws FeedException {
 
         ClientCookies cookies = webClientProvider.getWebClient(jobName).getCookies();

         allFeeds = getAllFeeds(atomFeedProperties, cookies);
         allMarkers = new AllMarkersJdbcImpl(jdbcConnectionProvider);
         allFailedEvents = new AllFailedEventsJdbcImpl(jdbcConnectionProvider);
         AtomFeedClient atomFeedClient = feedClientFactory.getFeedClient(atomFeedProperties, jdbcConnectionProvider, feedName, openERPClient, allFeeds, allMarkers, allFailedEvents, jobName);
         return atomFeedClient;
     }
 
     static AllFeeds getAllFeeds(OpenERPAtomFeedProperties atomFeedProperties, ClientCookies cookies) {
         AtomFeedProperties feedProperties = new AtomFeedProperties();
         feedProperties.setConnectTimeout(atomFeedProperties.getConnectionTimeoutInMilliseconds());
         feedProperties.setReadTimeout(atomFeedProperties.getReplyTimeoutInMilliseconds());
         feedProperties.setMaxFailedEvents(atomFeedProperties.getMaxFailedEvents());
         return new AllFeeds(feedProperties, cookies);
     }
 
 }
