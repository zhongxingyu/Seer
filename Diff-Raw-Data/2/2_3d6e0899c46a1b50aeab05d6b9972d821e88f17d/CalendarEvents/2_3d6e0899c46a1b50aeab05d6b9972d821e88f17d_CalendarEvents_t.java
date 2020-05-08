 package net.cyklotron.cms.modules.views.documents;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.table.generic.EmptyTableModel;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.category.query.CategoryQueryPoolResource;
 import net.cyklotron.cms.category.query.CategoryQueryPoolResourceImpl;
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.documents.internal.CalendarEventsSearchMethod;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.searching.HitsViewPermissionFilter;
 import net.cyklotron.cms.search.searching.SearchHandler;
 import net.cyklotron.cms.search.searching.cms.LuceneSearchHandler;
 import net.cyklotron.cms.search.searching.cms.LuceneSearchHit;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 import net.cyklotron.cms.util.CmsResourceListTableModel;
 import net.cyklotron.cms.util.SiteFilter;
 
 /**
  *
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: Calendar.java,v 1.8 2008-10-30 17:54:28 rafal Exp $
  */
 public class CalendarEvents
     extends BaseSkinableDocumentScreen
 {
     /** search serivce for analyzer nad searcher getting. */
     protected SiteService siteService;
 
     protected SearchService searchService;
     
     protected IntegrationService integrationService;
     
     protected CategoryQueryService categoryQueryService;
 
     public CalendarEvents(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         SiteService siteService, StructureService structureService,
         CategoryQueryService categoryQueryService,
         StyleService styleService, SkinService skinService,
         MVCFinder mvcFinder, TableStateManager tableStateManager,
         SearchService searchService, IntegrationService integrationService)
     {
         super(context, logger, preferencesService, cmsDataFactory, structureService, styleService,
                         skinService, mvcFinder, tableStateManager);
         this.siteService = siteService;
         this.searchService = searchService;
         this.integrationService = integrationService;
         this.categoryQueryService = categoryQueryService;
     }
 
     @Override
     public void prepareDefault(Context context)
         throws ProcessingException
     {
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         Parameters parameters = RequestParameters.getRequestParameters(context);
         final CoralSession coralSession = context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         // setup search date
         java.util.Calendar calendar = java.util.Calendar.getInstance(i18nContext.getLocale());
         calendar.setTime(cmsData.getDate());
         int day = parameters.getInt("day",calendar.get(java.util.Calendar.DAY_OF_MONTH));
         int month = parameters.getInt("month",calendar.get(java.util.Calendar.MONTH)+1);
         int year = parameters.getInt("year",calendar.get(java.util.Calendar.YEAR));
         calendar.set(java.util.Calendar.DAY_OF_MONTH, day);
         calendar.set(java.util.Calendar.MONTH, month-1);
         calendar.set(java.util.Calendar.YEAR, year);
 
         // setup search bounds 
         Date startDate = null;
         Date endDate = null;
 
         int period = parameters.getInt("period", 1);
         
         calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
         calendar.set(java.util.Calendar.MINUTE, 0);
         calendar.set(java.util.Calendar.SECOND, 0);
         startDate = calendar.getTime();
         calendar.add(java.util.Calendar.DAY_OF_YEAR, --period);
         calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
         calendar.set(java.util.Calendar.MINUTE, 59);
         calendar.set(java.util.Calendar.SECOND, 59);
         endDate = calendar.getTime();
 
        String range = parameters.get("range","ongoing");        
         String textQuery = parameters.get("text_query", "");
         long queryId = parameters.getLong("query_id", -1);
 
         templatingContext.put("day",new Integer(day));
         templatingContext.put("month",new Integer(month));
         templatingContext.put("year",new Integer(year));
         templatingContext.put("period", period);
         templatingContext.put("range", range);
         templatingContext.put("text_query", textQuery);
         templatingContext.put("query_id", new Long(queryId));
         templatingContext.put("start_date", startDate);
         templatingContext.put("end_date", endDate);
         
         
         
         try
         {
             SiteResource site = cmsData.getSite();
             Parameters screenConfig = getScreenConfig();
             Resource[] pools = null;
             long indexId = screenConfig.getLong("index_id",-1);
             long queryPoolId = screenConfig.getLong("query_pool_id", -1);
 
             if(indexId == -1)
             {
                 Resource parent = searchService.getPoolsRoot(coralSession, getSite());
                 pools = coralSession.getStore().getResource(parent);
             }
             else
             {
                 Resource index = coralSession.getStore().getResource(indexId);
                 pools = new Resource[1];
                 pools[0] = index;
             }
             if(queryPoolId != -1)
             {
                 CategoryQueryPoolResource queryPool = CategoryQueryPoolResourceImpl
                     .getCategoryQueryPoolResource(coralSession, queryPoolId);
                 templatingContext.put("queries", queryPool.getQueries());
             }
 
             CalendarEventsSearchMethod method = new CalendarEventsSearchMethod(
                 searchService, parameters, i18nContext.getLocale(), logger, startDate, endDate, textQuery);
             templatingContext.put("query", method.getQueryString(coralSession));
             TableFilter filter = new HitsViewPermissionFilter(coralSession.getUserSubject(), coralSession);         
             TableState state = tableStateManager.getState(context, "cms.documents.calendar_events.results."+site.getName());
             method.setupTableState(state);
             
             // - prepare search handler
             SearchHandler searchHandler = new LuceneSearchHandler(context, searchService, integrationService, cmsDataFactory);
                     
             // - execute seach and put results into the context
             ArrayList filters = new ArrayList();
             filters.add(filter);
 
             if(queryId != -1)
             {
                 CategoryQueryResource categoryQuery = (CategoryQueryResource)coralSession
                     .getStore().getResource(queryId);
                 String[] siteNames = categoryQuery.getAcceptedSiteNames();
                 Resource[] resources = categoryQueryService.forwardQuery(coralSession, categoryQuery
                     .getQuery());
 
                 SiteFilter siteFilter = null;
                 List<Resource> resSiteList = new ArrayList<Resource>();
                 if(siteNames != null && siteNames.length > 0)
                 {
                     siteFilter = new SiteFilter(coralSession, siteNames, siteService);
                     for (Resource res : resources)
                     {
                         if(siteFilter.accept(res))
                         {
                             resSiteList.add(res);
                         }
                     }
                 }
 
                 if(resSiteList != null)
                 {
                     final List<Resource> resList = resSiteList;
                     TableFilter<Object> hitsCategoryFilter = new TableFilter<Object>()
                         {
                             public boolean accept(Object object)
                             {
                                 if(object instanceof LuceneSearchHit)
                                 {
                                     try
                                     {
                                         LuceneSearchHit hit = (LuceneSearchHit)object;
                                         Resource resource = coralSession.getStore().getResource(
                                             hit.getId());
                                         return resList.contains(resource);
                                     }
                                     catch(EntityDoesNotExistException e)
                                     {
                                         return false;
                                     }
                                 }
                                 else
                                 {
                                     return true;
                                 }
                             }
                         };
                     filters.add(hitsCategoryFilter);
                 }
             }
 
             TableTool hitsTable = searchHandler.search(coralSession, pools, method, state, filters, parameters, i18nContext);
             
             if(hitsTable == null)
             {
                 hitsTable = new TableTool(state, null, new EmptyTableModel());
             }
             
             templatingContext.put("hits_table", hitsTable);
             prepareCategories(context, false);  
         }
         catch(Exception e)
         {
             throw new ProcessingException("Exception occurred",e);
         }
         
     }
 }
