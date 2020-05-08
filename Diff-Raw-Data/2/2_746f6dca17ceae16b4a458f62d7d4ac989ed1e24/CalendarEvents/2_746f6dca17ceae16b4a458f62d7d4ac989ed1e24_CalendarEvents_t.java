 package net.cyklotron.cms.modules.components.documents;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.cache.CacheFactory;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableColumn;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableRow;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.table.generic.ListTableModel;
 import org.objectledge.templating.Templating;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.documents.calendar.CalendarSearchParameters;
 import net.cyklotron.cms.documents.calendar.CalendarSearchService;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.modules.components.SkinableCMSComponent;
 import net.cyklotron.cms.search.PoolResource;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.searching.HitsViewPermissionFilter;
 import net.cyklotron.cms.search.searching.SearchHit;
 import net.cyklotron.cms.search.searching.SearchingException;
 import net.cyklotron.cms.search.searching.cms.LuceneSearchHit;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.ComponentDataCacheService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * CalendarEvents component displays calendar events.
  * 
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: CalendarEvents.java,v 1.8 2007-11-18 20:55:43 rafal Exp $
  */
 public class CalendarEvents
     extends SkinableCMSComponent
 {
     protected StructureService structureService;
 
     protected SearchService searchService;
 
     protected CacheFactory cacheService;
 
     protected IntegrationService integrationService;
 
     protected TableStateManager tableStateManager;
 
     protected CategoryQueryService categoryQueryService;
 
     protected SiteService siteService;
 
     private final ComponentDataCacheService componentDataCacheService;
 
     private final CalendarSearchService searchUtil;
 
     public CalendarEvents(org.objectledge.context.Context context, Logger logger,
         Templating templating, CmsDataFactory cmsDataFactory, SkinService skinService,
         MVCFinder mvcFinder, StructureService structureService, SearchService searchService,
         CacheFactory cacheFactory, IntegrationService integrationService,
         CategoryQueryService categoryQueryService, SiteService siteService,
         TableStateManager tableStateManager, ComponentDataCacheService componentDataCacheService, CalendarSearchService searchUtil)
     {
         super(context, logger, templating, cmsDataFactory, skinService, mvcFinder);
         this.structureService = structureService;
         this.searchService = searchService;
         this.cacheService = cacheFactory;
         this.integrationService = integrationService;
         this.tableStateManager = tableStateManager;
         this.categoryQueryService = categoryQueryService;
         this.siteService = siteService;
         this.componentDataCacheService = componentDataCacheService;
         this.searchUtil = searchUtil;
     }
 
     public void process(Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext,
         CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             Parameters config = getConfiguration();
             TableState state = tableStateManager.getState(context,
                 "cached.cms.documents.calendar.events.results");
             state.setRootId(null);
             state.setTreeView(false);
 
             // - execute search and put results into the context
             SearchHit[] hits = getHits(config, coralSession, i18nContext, parameters);
             if(hits == null)
             {
                 return;
             }
             TableTool<SearchHit> hitsTable = new TableTool<SearchHit>(state, null, new ListTableModel<SearchHit>(hits,
                 (TableColumn<SearchHit>[])null));
             templatingContext.put("hits_table", hitsTable);
         }
         catch(TableException e)
         {
             cmsDataFactory.getCmsData(context).getComponent()
                 .error("Error preparing table tool", e);
         }
     }
 
     protected SearchHit[] getHits(Parameters config, CoralSession coralSession,
         I18nContext i18nContext, Parameters parameters)
         throws ProcessingException
     {
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         int cacheInterval = config.getInt("cacheInterval", 0);
         if(cacheInterval > 0L)
         {
             Object guard = componentDataCacheService.getGuard(cmsData, null);
             synchronized(guard)
             {
                 SearchHit[] results = componentDataCacheService.getCachedData(cmsData, null);
                 if(results == null)
                 {
                     results = getHits2(config, coralSession, i18nContext, parameters);
                     componentDataCacheService.setCachedData(cmsData, null, results, cacheInterval);
                 }
                 return results;
             }
         }
         else
         {
             logger.warn("non-cachable category query results screen nodeId="
                 + cmsData.getNode().getIdString());
             return getHits2(config, coralSession, i18nContext, parameters);
         }
     }
 
     private SearchHit[] getHits2(Parameters config, CoralSession coralSession,
         I18nContext i18nContext, Parameters parameters)
         throws ProcessingException
     {
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         try
         {
             Set<PoolResource> indexPools = searchUtil.searchPools(config, coralSession, cmsData);
 
             CalendarSearchParameters searchParameters;
             if(parameters.isDefined("year") && parameters.isDefined("month")
                 && parameters.isDefined("day"))
             {
                 searchParameters = new CalendarSearchParameters(parameters.getInt("year"),
                     parameters.getInt("month"), parameters.getInt("day"),
                     config.getInt("offset", 0), i18nContext.getLocale(), indexPools);
             }
             else
             {
                 searchParameters = new CalendarSearchParameters(cmsData.getDate(),
                     config.getInt("offset", 0), i18nContext.getLocale(), indexPools);
             }
 
             String categoryQueryName = config.get("categoryQueryName", "");
             Resource[] queries = coralSession.getStore().getResource(
                 categoryQueryService.getCategoryQueryRoot(coralSession, cmsData.getSite()),
                 categoryQueryName);
             if(queries.length == 1)
             {
                 CategoryQueryResource categoryQuery = (CategoryQueryResource)queries[0];
                 searchParameters.setCategoryQuery(categoryQuery);
             }
 
             TableModel<LuceneSearchHit> hitsTableModel = searchUtil.search(searchParameters,
                 false, context, parameters, i18nContext, coralSession);
 
             TableState state = tableStateManager.getState(context,
                 "cms.documents.calendar.events.results");
 
             List<TableFilter<LuceneSearchHit>> filters = new ArrayList<TableFilter<LuceneSearchHit>>();
             TableFilter<LuceneSearchHit> filter = new HitsViewPermissionFilter<LuceneSearchHit>(
                 coralSession.getUserSubject(), coralSession);
             filters.add(filter);
 
             TableTool<LuceneSearchHit> hitsTable = new TableTool<LuceneSearchHit>(state, filters, hitsTableModel);
 
             List<TableRow<LuceneSearchHit>> rows = hitsTable.getRows();
             SearchHit[] searchHits = new SearchHit[rows.size()];            
             int i = 0;
             for(TableRow<LuceneSearchHit> row : rows)
             {
                searchHits[i++] = row.getObject();
             }
             return searchHits;
         }
         catch(TableException e)
         {
             cmsData.getComponent().error("Error preparing table tool", e);
             return null;
         }
         catch(SearchingException e)
         {
             cmsData.getComponent().error("Error while searching", e);
             return null;
         }
         catch(Exception e)
         {
             cmsData.getComponent().error("Cannot execute category query", e);
             return null;
         }
     }
 }
