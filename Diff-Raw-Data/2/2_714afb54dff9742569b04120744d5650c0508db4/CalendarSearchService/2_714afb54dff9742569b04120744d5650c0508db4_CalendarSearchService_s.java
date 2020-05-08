 package net.cyklotron.cms.documents.calendar;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableRow;
 import org.objectledge.table.TableState;
 import org.objectledge.templating.TemplatingContext;
 
 import bak.pcj.set.LongOpenHashSet;
 import bak.pcj.set.LongSet;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.query.CategoryQueryException;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.search.PoolResource;
 import net.cyklotron.cms.search.SearchException;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.searching.SearchMethod;
 import net.cyklotron.cms.search.searching.SearchingException;
 import net.cyklotron.cms.search.searching.cms.LuceneSearchHandler;
 import net.cyklotron.cms.search.searching.cms.LuceneSearchHit;
 import net.cyklotron.cms.site.SiteResource;
 
 public class CalendarSearchService
 {
     private final SearchService searchService;
 
     private final IntegrationService integrationService;
 
     private final CmsDataFactory cmsDataFactory;
 
     private final CategoryQueryService categoryQueryService;
 
     private final Logger logger;
 
     public CalendarSearchService(SearchService searchService,
         IntegrationService integrationService, CmsDataFactory cmsDataFactory,
         CategoryQueryService catetoryQueryService, Logger logger)
     {
         this.searchService = searchService;
         this.integrationService = integrationService;
         this.cmsDataFactory = cmsDataFactory;
         this.categoryQueryService = catetoryQueryService;
         this.logger = logger;
     }
 
     public Set<PoolResource> searchPools(Parameters config, CoralSession coralSession,
         CmsData cmsData)
         throws SearchException, EntityDoesNotExistException
     {
         Set<PoolResource> pools = new HashSet<PoolResource>();
         long indexId = config.getLong("index_id", -1);
         if(indexId == -1)
         {
             SiteResource site = cmsData.getSite();
             Resource parent = searchService.getPoolsRoot(coralSession, site);
             for(Resource child : coralSession.getStore().getResource(parent))
             {
                 if(child instanceof PoolResource)
                 {
                     pools.add((PoolResource)child);
                 }
             }
         }
         else
         {
             Resource index = coralSession.getStore().getResource(indexId);
             pools.add((PoolResource)index);
         }
         return pools;
     }
 
     public TableModel<LuceneSearchHit> search(CalendarSearchParameters searchParameters,
         boolean useLegacyMethod, Context context, Parameters parameters, TableState tableState,
         I18nContext i18nContext, CoralSession coralSession, TemplatingContext templatingContext)
         throws SearchingException
     {
         SearchMethod method;
         if(useLegacyMethod)
         {
             method = new CalendarSearchMethod(searchService, parameters, i18nContext.getLocale(),
                 logger, searchParameters.getStartDate(), searchParameters.getEndDate(),searchParameters.getTextQuery());
         }
         else
         {
             method = new CalendarEventsSearchMethod(searchService, parameters,
                 i18nContext.getLocale(), searchParameters);
         }
         
         method.setupTableState(tableState);
         String queryString = method.getQueryString(coralSession);
         templatingContext.put("query", queryString);
 
         LuceneSearchHandler searchHandler = new LuceneSearchHandler(context, searchService,
             integrationService, cmsDataFactory);
 
         Resource[] pools = searchParameters.getIndexPools().toArray(
             new Resource[searchParameters.getIndexPools().size()]);
         
         TableModel<LuceneSearchHit> hitsTableModel = searchHandler.search(coralSession, pools,
             method, null, parameters, i18nContext);
 
         if(searchParameters.getCategoryQuery() == null)
         {
             return hitsTableModel;
         }
         else
         {
             TableState allHits = new TableState("<local>", -1);
             allHits.setPageSize(-1);
 
             TableRow<LuceneSearchHit>[] rows = hitsTableModel.getRowSet(allHits, null).getRows();
            LongSet docIds = new LongOpenHashSet(rows.length);
             for(TableRow<LuceneSearchHit> row : rows)
             {
                 docIds.add(row.getObject().getId());
             }
 
             try
             {
                 // run category query, limited to document set returned by lucene search
                 docIds = categoryQueryService.forwardQueryIds(coralSession, searchParameters
                     .getCategoryQuery().getQuery(), docIds);
 
                 // retain only those documents present in category query results
                 List<LuceneSearchHit> filteredHits = new ArrayList<LuceneSearchHit>(docIds.size());
                 for(TableRow<LuceneSearchHit> row : rows)
                 {
                     if(docIds.contains(row.getObject().getId()))
                     {
                         filteredHits.add(row.getObject());
                     }
                 }
 
                 return searchHandler.hitsTableModel(filteredHits, coralSession);
             }
             catch(CategoryQueryException e)
             {
                 throw new SearchingException("category query failed", e);
             }
         }
     }
 }
