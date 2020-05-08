 package net.cyklotron.cms.search.searching.cms;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.Searcher;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.TopFieldCollector;
 import org.objectledge.authentication.AuthenticationContext;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.Timer;
 import org.objectledge.web.mvc.tools.LinkTool;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.CmsLinkTool;
 import net.cyklotron.cms.ProtectedResource;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.integration.ResourceClassResource;
 import net.cyklotron.cms.search.PoolResource;
 import net.cyklotron.cms.search.SearchException;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.searching.SearchHandler;
 import net.cyklotron.cms.search.searching.SearchMethod;
 import net.cyklotron.cms.search.searching.SearchingException;
 
 /**
  * SearchHandler implementation for searching lucene indexes used by CMS.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: LuceneSearchHandler.java,v 1.7 2005-06-03 07:29:35 pablo Exp $
  */
 public class LuceneSearchHandler implements SearchHandler<LuceneSearchHit>
 {
     /** search service for getting searchers. */
     private SearchService searchService;
     /** integration service for building URLs for search hits. */
     private IntegrationService integrationService;
 
     private Context context;
     
     public LuceneSearchHandler(Context context, SearchService searchService,
         IntegrationService integrationService, CmsDataFactory cmsDateFactory)
     {
         this.searchService = searchService;
         this.integrationService = integrationService;
         this.context = context;
     }
 
     public TableModel<LuceneSearchHit> search(CoralSession coralSession, Resource[] searchPools,
         SearchMethod method, TableState state, Parameters parameters, I18nContext i18nContext)
         throws SearchingException
     {
         // get the query
         Query query = null;
         try
         {
             query = method.getQuery(coralSession);
         }
         catch(Exception e)
         {
             throw new SearchingException("problem while getting the query", e);
         }
 
         // setup sorting
         SortField[] sortFields = method.getSortFields();
         Sort sort = null;
         if(sortFields != null)
         {
             sort = new Sort(sortFields);
         }
         
         // get index pools from chosen search pools
         PoolResource[] pools = null;
         ArrayList<PoolResource> tmpPools = new ArrayList<PoolResource>(searchPools.length);
         for(int i=0; i<searchPools.length; i++)
         {
             Resource pool = searchPools[i];
             if(pool instanceof PoolResource)
             {
                 tmpPools.add((PoolResource)pool);
             }
         }
         pools = new PoolResource[tmpPools.size()];
         pools = (PoolResource[])(tmpPools.toArray(pools));
         
         // search
         IndexSearcher searcher = null;
         List<LuceneSearchHit> hits;
         try
         {	
         	Timer timer = new Timer();
             Optional<IndexSearcher> optional = searchService.getSearchingFacility().getSearcher(
                 pools, coralSession.getUserSubject());
             if(optional.isPresent())
             {
                 searcher = optional.get();
                 hits = getLuceneSearchHits(searcher, query, sort);
                 searchService.logQueryExecution(query.toString(), timer.getElapsedMillis(),
                     hits.size());
             }
             else
             {
                 hits = Collections.emptyList();
             }
         }
         catch(SearchException e)
         {
             throw new SearchingException("problem while getting the searcher", e);
         }
         catch(IOException e)
         {
             throw new SearchingException("problem while searching the indexes", e);
         }
         catch(Exception e)
         {
             throw new SearchingException("problem while getting the searcher", e);
         }
         finally
         {
             searchService.getSearchingFacility().returnSearcher(searcher);
         }
         TableModel<LuceneSearchHit> model = hitsTableModel(hits, coralSession);
         return model;
     }
 
     public TableModel<LuceneSearchHit> hitsTableModel(List<LuceneSearchHit> hits,
         CoralSession coralSession)
     {
         // prepare link tool
         TemplatingContext tContext = (TemplatingContext)
         context.getAttribute(TemplatingContext.class);
         CmsLinkTool link = (CmsLinkTool)tContext.get("link");
         link = (CmsLinkTool)(link.unsetAction().unsetView());
         
         Subject subject = coralSession.getUserSubject();
         AuthenticationContext authContext = AuthenticationContext.getAuthenticationContext(context);
         TableModel<LuceneSearchHit> model = new HitsTableModel<LuceneSearchHit>(context, hits, this, link, subject, authContext.isUserAuthenticated());
         return model;
     }
 
     List<LuceneSearchHit> getLuceneSearchHits(IndexSearcher searcher, Query query, Sort sort)
         throws IOException
     {
         TopDocs hits;
         TopFieldCollector topFieldCollector;
        int numHits = searcher.maxDoc()>0 ? searcher.maxDoc() : 1;
 
         if(sort == null)
         {
             hits = searcher.search(query, null, numHits);
         }
         else
         {
             topFieldCollector = TopFieldCollector.create(sort, numHits, true, true, true, false);
             searcher.search(query, topFieldCollector);
             hits = topFieldCollector.topDocs();
         }
         ScoreDoc[] scoreDocs = hits.scoreDocs;
         float percent = 1/hits.getMaxScore();
         Set<LuceneSearchHit> searchHits = new LinkedHashSet<LuceneSearchHit>(scoreDocs.length);
         for(int i = 0; i < scoreDocs.length; i++)
         {
             final ScoreDoc sDoc = scoreDocs[i];
             searchHits.add(new LuceneSearchHit(searcher.doc(sDoc.doc), sDoc.score * percent));
         }
         List<LuceneSearchHit> uniqueHits = new ArrayList<LuceneSearchHit>(searchHits);
         return uniqueHits;
     }
 
     ResourceClassResource getHitResourceClassResource(CoralSession coralSession, LuceneSearchHit hit)
     throws EntityDoesNotExistException
     {
         return integrationService.getResourceClass(coralSession,
             coralSession.getSchema().getResourceClass(hit.getResourceClassId()));
     }
     
     Resource getHitResource(CoralSession coralSession, LuceneSearchHit hit)
         throws EntityDoesNotExistException
     {
         try
         {
             return coralSession.getStore().getResource(hit.getId());
         }
         catch(EntityDoesNotExistException e)
         {
             return null;
         }
     }
     
     public void resolveUrls(LuceneSearchHit hit, Subject subject, Context context,
         boolean generateEditLink, LinkTool link)
     {
         try
         {
             CoralSession coralSession = context.getAttribute(CoralSession.class);
             ResourceClassResource rcr = getHitResourceClassResource(coralSession, hit);
             hit.setUrl(link.view(rcr.getView()).set("res_id", hit.getId()).toString());
             if(generateEditLink)
             {
                 Resource resource = getHitResource(coralSession, hit);
                 if(resource != null)
                 {
                     if(!(resource instanceof ProtectedResource)
                         || ((ProtectedResource)resource).canModify(coralSession, subject))
                     {
                         if(rcr.getEditView() != null)
                         {
                             hit.setEditUrl(link.view(rcr.getEditView()).set("res_id", hit.getId())
                                 .toString());
                         }
                     }
                 }
             }
         }
         catch(EntityDoesNotExistException e)
         {
             // could not retrieve ResourceClass - leave empty URL
         }
     }
 }
