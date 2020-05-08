 package net.cyklotron.cms.search.searching;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.FilteredQuery;
 import org.apache.lucene.search.Query;
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.Instantiator;
 import org.objectledge.coral.modules.views.BaseCoralView;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableRow;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.table.generic.EmptyTableModel;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.utils.StackTrace;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.query.CategoryQueryBuilder;
 import net.cyklotron.cms.category.query.CategoryQueryException;
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.category.query.CategoryQueryResourceImpl;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.search.ExternalPoolResource;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.searching.cms.LuceneSearchHandler;
 import net.cyklotron.cms.search.searching.cms.LuceneSearchHit;
 import net.cyklotron.cms.site.SiteResource;
 import bak.pcj.set.LongOpenHashSet;
 import bak.pcj.set.LongSet;
 
 /**
  * Searching implementation.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: SearchScreen.java,v 1.6 2005-02-09 22:20:46 rafal Exp $
  */
 public class SearchScreen
     extends BaseCoralView
 {
     /** logging facility */
     private Logger logger;
 
     /** search service for getting searchers. */
     private SearchService searchService;
  
     private IntegrationService integrationService;
     
     private CategoryQueryService categoryQueryService;
 
     /** table service for hit list display. */
     private TableStateManager tableStateManager;
 
     private CmsDataFactory cmsDataFactory;
     
     private TableFilter filter;
     
     private Instantiator instantiator;
 
     public SearchScreen(Context context, Logger logger, TableStateManager tableStateManager,
         SearchService searchService, IntegrationService integrationService,
         CategoryQueryService categoryQueryService, CmsDataFactory cmsDataFactory,
         TableFilter filter, Instantiator instantiator)
     {
         super(context);
         this.logger = logger;
         this.searchService = searchService;
         this.tableStateManager = tableStateManager;
         this.integrationService = integrationService;
         this.categoryQueryService = categoryQueryService;
         this.cmsDataFactory = cmsDataFactory;
         this.filter = filter;
         this.instantiator = instantiator;
     }
     
     /**
      * {@inheritDoc}
      */
     public void process(Parameters parameters, TemplatingContext templatingContext,
                         MVCContext mvcContext, 
                         I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         // determine search method
         SearchMethod method = null;
         if(parameters.get("field",null) != null)
         {
             method =
                 new AdvancedSearchMethod(searchService, parameters, i18nContext.getLocale());
         }
         else
         {
             method =
                 new SimpleSearchMethod(searchService, parameters, i18nContext.getLocale());
         }
         // put parameters into the templating context for redisplaying the query form
         method.storeQueryParameters(templatingContext);
         
         // get the query
         Query query = null;
         try
         {
             query = method.getQuery(coralSession);
             templatingContext.put("query", method.getQueryString(coralSession));
         }
         catch(Exception e)
         {
             query = null;
             templatingContext.put("query", method.getErrorQueryString());
             templatingContext.put("result", "bad_query");
             return;
         }
 
         // do not search if there is no query
         if(query == null)
         {
             return;
         }
 
         SiteResource site = cmsData.getSite();
 
         // get pools
         Resource[] pools = null;
         try
         {
             long poolId = parameters.getLong("pool_id",-1);
             if(poolId == -1)
             {
                 Resource parent = searchService.getPoolsRoot(coralSession, site);
                 pools = coralSession.getStore().getResource(parent);
                 if(pools.length == 0)
                 {
                     templatingContext.put("result", "no_index_pools_configured");
                     return;
                 }
             }
             else
             {
                 Resource poolResource = coralSession.getStore().getResource(poolId);
                 templatingContext.put("selected_pool", poolResource);
                 pools = new Resource[1];
                 pools[0] = poolResource;
             }
         }
         catch(Exception e)
         {
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e).toString());
             logger.error("cannot get a chosen index pool", e);
             return;
         }
 
         CategoryQueryBuilder queryBuilder = null;
         try
         {
             long[] requiredQueriesIds = parameters.getLongs("required_queries");
             Set<CategoryQueryResource> requiredQueries = new HashSet<CategoryQueryResource>();
             for(int i = 0; i < requiredQueriesIds.length; i++)
             {
                 long queryId = requiredQueriesIds[i];
                 if(queryId != -1)
                 {
                     CategoryQueryResource categoryQueryResource = CategoryQueryResourceImpl
                         .getCategoryQueryResource(coralSession, queryId);
                     requiredQueries.add(categoryQueryResource);
                 }
                 else
                 {
                     requiredQueries.clear();
                     break;
                 }
             }
             templatingContext.put("selected_required_queries", requiredQueries);
 
             long[] optionalQueriesIds = parameters.getLongs("optional_queries");
             List<Set<CategoryQueryResource>> optionalQueriesList = new ArrayList<Set<CategoryQueryResource>>();
             Set<CategoryQueryResource> optionalQueries = new HashSet<CategoryQueryResource>();
             for(int i = 0; i < optionalQueriesIds.length; i++)
             {
                 long queryId = optionalQueriesIds[i];
                 if(queryId != -1)
                 {
                     CategoryQueryResource categoryQueryResource = CategoryQueryResourceImpl
                         .getCategoryQueryResource(coralSession, queryId);
                     optionalQueries.add(categoryQueryResource);
                 }
                 else
                 {
                     optionalQueries.clear();
                     break;
                 }
             }
             if(optionalQueries.size() > 0)
             {
                 optionalQueriesList.add(optionalQueries);
             }
 
             templatingContext.put("selected_optional_queries", optionalQueries);
             
             Integer i = 0;
             String additional_queries_name = "additional_queries_" + (i++).toString();
             while(parameters.isDefined(additional_queries_name))
             {
                 long[] additionalQueriesIds = parameters.getLongs(additional_queries_name);
                 Set<CategoryQueryResource> additionalQueries = new HashSet<CategoryQueryResource>();
                 for(int j = 0; j < additionalQueriesIds.length; j++)
                 {
                     long queryId = additionalQueriesIds[j];
                     if(queryId != -1)
                     {
                         CategoryQueryResource categoryQueryResource = CategoryQueryResourceImpl
                             .getCategoryQueryResource(coralSession, queryId);
                         additionalQueries.add(categoryQueryResource);
                     }
                     else
                     {
                         additionalQueries.clear();
                         break;
                     }
                 }
                if(additionalQueries.size() > 0)
                 {
                     optionalQueriesList.add(additionalQueries);
                 }
                 templatingContext.put("selected_additional_queries_" + i.toString(), additionalQueries);
                 additional_queries_name = "additional_queries_" + (++i).toString();
             }
             
             if(!requiredQueries.isEmpty() || !optionalQueriesList.isEmpty() )
             {
                 queryBuilder = new CategoryQueryBuilder(requiredQueries, optionalQueriesList);
                 LongSet docIds = categoryQueryService.forwardQueryIds(coralSession, queryBuilder.getQuery(), null);
                 ((AdvancedSearchMethod)method).setDocIds(docIds);
             }
         }
         catch(Exception e)
         {
             templatingContext.put("result", "exception");
             templatingContext.put("trace", new StackTrace(e).toString());
             logger.error("cannot get a chosen category query", e);
             return;
         }
         
 
         // search
         // - prepare display state
         TableState state = tableStateManager.getState(context, "cms.search.results."+site.getName());
         method.setupTableState(state);
         // - prepare search handler
         LuceneSearchHandler searchHandler = null;
         if(pools.length == 1 && pools[0] instanceof ExternalPoolResource)
         {
             ExternalPoolResource extPool = (ExternalPoolResource)pools[0];
             try
             {
                 Class<?> clazz = instantiator.loadClass(extPool.getSearchHandler());
                 searchHandler = (LuceneSearchHandler)(instantiator.newInstance(clazz));
             }
             catch(Exception e)
             {
                 templatingContext.put("result", "exception");
                 templatingContext.put("trace", new StackTrace(e).toString());
                 logger.error("cannot instantiate search handler", e);
                 return;
             }
         }
         else
         {
             searchHandler = 
                 new LuceneSearchHandler(context, searchService, integrationService, cmsDataFactory);
         }
         // - execute seach and put results into the templatingContext
         TableTool hitsTable = null;
         try
         {
             ArrayList filters = new ArrayList();
             filters.add(filter);
 
             TableModel hitsTableModel = searchHandler.search(coralSession, pools, method, state, parameters, i18nContext);
             hitsTable = new TableTool<LuceneSearchHit>(state, filters, hitsTableModel);
         }
         catch(Exception e1)
         {
             throw new ProcessingException("Problem while searching", e1);
         }
         if(hitsTable == null)
         {
             try
             {
                 hitsTable = new TableTool(state, null, new EmptyTableModel());
             }
             catch(TableException e)
             {
                 // there is nothing we can do
             }
         }
         templatingContext.put("hits_table", hitsTable);
     }
 
     
     private TableTool<LuceneSearchHit> getHitsTable(CoralSession coralSession, SearchMethod method,
         TableState state, List<TableFilter<? super LuceneSearchHit>> filters,
         TableModel<LuceneSearchHit> model, LuceneSearchHandler searchHandler,
         CategoryQueryBuilder queryBuilder)
         throws TableException, SearchingException
     {
 
         if(queryBuilder == null)
         {
             return new TableTool<LuceneSearchHit>(state, filters, model);
         }
         else
         {
             TableState allHits = new TableState("<local>", -1);
             allHits.setPageSize(-1);
 
             TableModel<LuceneSearchHit> hitsTableModel;
                         
             TableRow<LuceneSearchHit>[] rows = model.getRowSet(allHits, null).getRows();
             LongSet docIds = new LongOpenHashSet();
             for(TableRow<LuceneSearchHit> row : rows)
             {
                 docIds.add(row.getObject().getId());
             }
 
             try
             {
                 System.out.println("query::"+queryBuilder.getQuery());
                 // run category query, limited to document set returned by lucene search
                 docIds = categoryQueryService.forwardQueryIds(coralSession,
                     queryBuilder.getQuery(), docIds);
 
                 // retain only those documents present in category query results
                 List<LuceneSearchHit> filteredHits = new ArrayList<LuceneSearchHit>(docIds.size());
                 for(TableRow<LuceneSearchHit> row : rows)
                 {
                     if(docIds.contains(row.getObject().getId()))
                     {
                         filteredHits.add(row.getObject());
                     }
                 }
 
                 hitsTableModel = searchHandler.hitsTableModel(filteredHits, coralSession);;
                 
                 return new TableTool<LuceneSearchHit>(state, filters, hitsTableModel);
             }
             catch(CategoryQueryException e)
             {
                 throw new SearchingException("category query failed", e);
             }
         }
     }
 }
