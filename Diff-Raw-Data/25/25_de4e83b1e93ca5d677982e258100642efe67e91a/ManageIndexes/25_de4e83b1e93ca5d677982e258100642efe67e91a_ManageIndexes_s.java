 package net.cyklotron.cms.modules.jobs.search;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.query.MalformedQueryException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.query.QueryResults.Row;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.scheduler.Job;
 
 import net.cyklotron.cms.search.IndexResource;
 import net.cyklotron.cms.search.SearchException;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.SearchUtil;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 
 /**
  * Performs added and modfied resources indexing and index optimisation.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: ManageIndexes.java,v 1.3 2005-03-23 10:36:49 zwierzem Exp $
  */
 public class ManageIndexes extends Job
 {
     // instance variables --------------------------------------------------------------------------
 
     /** logging service */
     protected Logger log;
 
     /** The search service. */
     private SearchService searchService;
 
     /** The site service. */
     private SiteService siteService;
     
     private CoralSessionFactory sessionFactory;
 
     public ManageIndexes(Logger logger, SearchService searchService, 
         CoralSessionFactory sessionFactory, SiteService siteService)
     {            
         this.log = logger;
         this.searchService = searchService;
         this.sessionFactory = sessionFactory;
         this.siteService = siteService;
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
             execute(coralSession);
         }
         finally
         {
             coralSession.close();
         }
     }
     
     // Job interface -------------------------------------------------------------------------------
     
 
     /**
      * Performs added and modfied resources indexing and index optimisation.
      */
     public void execute(CoralSession coralSession)
     {
         // prepare data for indexes manipulation
         
         // - get and set last indexing date - a date of last run of the indexing job
         ManageIndexesTimestamp ts = 
             new ManageIndexesTimestamp(log, searchService, coralSession);
         Date startDate = ts.getTimeStamp();
         
         // - get added resources ids
         Set addedResourcesIds = getResourcesIds(coralSession, "creation_time", startDate);
         // - get modified resources ids
         Set modifiedResourcesIds = getResourcesIds(coralSession, "modification_time", startDate);
         modifiedResourcesIds.removeAll(addedResourcesIds);
         // - divide modified resources between indexes
         Set resources = SearchUtil.getResources(coralSession, log, modifiedResourcesIds);
         Map modifiedResourcesByIndex = 
             searchService.getIndexingFacility().getResourcesByIndex(coralSession, resources);
         // - divide added resources between indexes
         resources = SearchUtil.getResources(coralSession, log, addedResourcesIds);
         Map addedResourcesByIndex = 
             searchService.getIndexingFacility().getResourcesByIndex(coralSession, resources);
         
         // run management tasks on every index
 
         SiteResource[] sites = siteService.getSites(coralSession);
         for(int i=0; i<sites.length; i++)
         {
             IndexResource[] indexes = getIndexes(coralSession, sites[i]);
             for(int j=0; j<indexes.length; j++)
             {
                 IndexResource index = indexes[j];
                 Set modifiedResources = (Set) modifiedResourcesByIndex.get(index);
                 Set addedResources = (Set) addedResourcesByIndex.get(index);
 //                poolService.runWorker(
 //                    new IndexManagementTask(index, modifiedResources, addedResources,
 //                        log, searchService, coralSession), "processing "+index.getPath());
                 new IndexManagementTask(index, modifiedResources, addedResources,
                     log, searchService, coralSession).run();
             }
         }
     }
 
     // modified and added resources retrieval ------------------------------------------------------
     
     private Set getResourcesIds(CoralSession coralSession, String dateFieldName, Date startDate)
     {
         SimpleDateFormat df = 
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", new Locale("en","US"));
         String startDateStr = df.format(startDate);
         try
         {
             QueryResults res = coralSession.getQuery().executeQuery(
                 "FIND RESOURCE FROM search.indexable WHERE "+dateFieldName+" >= '"+startDateStr+"'");
             Set set = new HashSet(1024);
             for (Iterator iter = res.iterator(); iter.hasNext();)
             {
                 long[] ids = ((Row) iter.next()).getIdArray();
                 for (int i = 0; i < ids.length; i++)
                 {
                     set.add(new Long(ids[i]));
                 }
             }
             return set;
         }
         catch (MalformedQueryException e)
         {
             // should not happen
             return null;
         }
     }
 
     // index management ----------------------------------------------------------------------------
 
     private static final IndexResource[] EMPTY_INDEXES = new IndexResource[0];
     
     private IndexResource[] getIndexes(CoralSession coralSession, SiteResource site)
     {
         try
         {
             Resource parent = searchService.getIndexesRoot(coralSession, site);
             Resource[] res = coralSession.getStore().getResource(parent);
             IndexResource[] indexes = new IndexResource[res.length];
             System.arraycopy(res, 0, indexes, 0, res.length);
             return indexes;
         }
         catch(SearchException e)
         {
             log.error("ManageIndexes: Cannot get indexes for the site '"+site.getName()+"'", e);
             return EMPTY_INDEXES;
         }
     }
 }
