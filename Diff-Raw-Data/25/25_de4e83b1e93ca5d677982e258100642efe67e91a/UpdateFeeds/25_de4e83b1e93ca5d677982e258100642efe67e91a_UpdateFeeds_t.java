 package net.cyklotron.cms.modules.jobs.httpfeed;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.event.ResourceDeletionListener;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.scheduler.Job;
 
 import net.cyklotron.cms.httpfeed.HttpFeedException;
 import net.cyklotron.cms.httpfeed.HttpFeedResource;
 import net.cyklotron.cms.httpfeed.HttpFeedService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 
 /**
  * A job that updates http feeds defined for the sites.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: UpdateFeeds.java,v 1.4 2005-03-29 11:56:03 pablo Exp $
  */
 public class UpdateFeeds extends Job
     implements ResourceDeletionListener
 {
     // instance variables ////////////////////////////////////////////////////
 
     /** logging service */
     protected Logger log;
 
     protected CoralSessionFactory sessionFactory;
     
     /** The site service. */
     private SiteService siteService;
 
     /** The http feed resource class. */
     private ResourceClass httpFeedResourceClass;
 
     /** The httpfeed service. */
     private HttpFeedService httpFeedService;
 
     /** deleted resources ids */
    private Set<Long> deletedFeedsIds = new HashSet<Long>();
 
     // initialization ///////////////////////////////////////////////////////
 
     /**
      *
      */
     public UpdateFeeds(Logger logger, CoralSessionFactory sessionFactory, 
         HttpFeedService httpFeedService, SiteService siteService)
     {            
         this.log = logger;
         this.sessionFactory = sessionFactory;
         this.siteService = siteService;
        this.httpFeedService = httpFeedService;
         CoralSession coralSession = sessionFactory.getRootSession();
         try
         {
             httpFeedResourceClass = coralSession.getSchema()
                                 .getResourceClass(HttpFeedResource.CLASS_NAME);
         }
         catch(EntityDoesNotExistException e)
         {
             throw new ComponentInitializationError("Could not find '"+HttpFeedResource.CLASS_NAME
                                           +"' resource class");
         }
         finally
         {
             coralSession.close();
         }
     }
 
 
     // Job interface ////////////////////////////////////////////////////////
 
     /**
      * Performs the mainteance.
      */
     public void run(String[] args)
     {
         CoralSession coralSession = sessionFactory.getRootSession();
         try
         {
             execute(coralSession, args);
         }
         finally
         {
             coralSession.close();
         }
     }
 
     
     /**
      * Performs the feed updates.
      */
     public void execute(CoralSession coralSession, String[] args)
     {
         // prepare for feeds updates
         deletedFeedsIds.clear();
         coralSession.getEvent().addResourceDeletionListener(this, httpFeedResourceClass);
 
         Calendar cal = Calendar.getInstance();
         Date now = cal.getTime();
 
         SiteResource[] sites = siteService.getSites(coralSession);
         for(int i=0; i<sites.length; i++)
         {
             try
             {
                 HttpFeedResource[] feeds =  httpFeedService.getFeeds(coralSession, sites[i]);
 
                 for(int j=0; j<feeds.length; j++)
                 {
                     HttpFeedResource feed = feeds[j];
 
                     // check if feed needs to be refreshed
                     boolean makeRefresh = (feed.getFailedUpdates() > 0)
                                           || (feed.getLastUpdate() == null);
                     if(!makeRefresh)
                     {
                         // calculate next update time
                         cal.setTime(feed.getLastUpdate());
                         cal.add(Calendar.MINUTE, feed.getInterval());
                         Date nextUpdate = cal.getTime();
 
                         makeRefresh = nextUpdate.before(now);
                     }
 
                     if(makeRefresh)
                     {
                         String content = httpFeedService.getContent(feed.getUrl());
                         synchronized(feed)
                         {
                             if(!deletedFeedsIds.contains(feed.getIdObject()))
                             {
                                 httpFeedService.refreshFeed(feed, content, coralSession.getUserSubject());
                             }
                         }
                     }
                 }
             }
             catch(HttpFeedException e)
             {
                 log.error("Cannot get http feeds for the site '"+sites[i].getName()+"'", e);
             }
         }
 
         // cleanup after feed updates
         coralSession.getEvent().removeResourceDeletionListener(this, httpFeedResourceClass);
         deletedFeedsIds.clear();
     }
 
     /**
      * Terminates the job asynchronously.
      *
      * <p>Not supported.</p>
      *
      * @param thread the Thread executing the job.
      */
     public void terminate(Thread thread)
     {
         // not supported
     }
 
     /** Called when <code>Resource</code>'s data change.
      *
      * @param resource the resource that changed.
      *
      */
     public void resourceDeleted(Resource resource)
     {
         deletedFeedsIds.add(resource.getIdObject());
     }
 }
