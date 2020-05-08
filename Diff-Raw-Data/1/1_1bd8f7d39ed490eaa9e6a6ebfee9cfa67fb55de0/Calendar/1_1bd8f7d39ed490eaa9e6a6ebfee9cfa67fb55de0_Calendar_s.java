 package net.cyklotron.cms.modules.views.documents;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
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
 import net.cyklotron.cms.documents.internal.CalendarSearchMethod;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.searching.HitsViewPermissionFilter;
 import net.cyklotron.cms.search.searching.SearchHandler;
 import net.cyklotron.cms.search.searching.cms.LuceneSearchHandler;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 
 /**
  *
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: Calendar.java,v 1.8 2008-10-30 17:54:28 rafal Exp $
  */
 public class Calendar
     extends BaseSkinableDocumentScreen
 {
     /** search serivce for analyzer nad searcher getting. */
     protected SearchService searchService;
     
     protected IntegrationService integrationService;
     
     public Calendar(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         StructureService structureService, StyleService styleService, SkinService skinService,
         MVCFinder mvcFinder, TableStateManager tableStateManager,
         SearchService searchService, IntegrationService integrationService)
     {
         super(context, logger, preferencesService, cmsDataFactory, structureService, styleService,
                         skinService, mvcFinder, tableStateManager);
         this.searchService = searchService;
         this.integrationService = integrationService;
     }
 
     public void prepareDefault(Context context)
         throws ProcessingException
     {
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
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
 
         String period = parameters.get("period","daily");
         if(period.equals("daily"))
         {
             calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
             calendar.set(java.util.Calendar.MINUTE, 0);
             calendar.set(java.util.Calendar.SECOND, 0);
             startDate = calendar.getTime();
             calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
             calendar.set(java.util.Calendar.MINUTE, 59);
             calendar.set(java.util.Calendar.SECOND, 59);
             endDate = calendar.getTime();
         }
         if(period.equals("monthly"))
         {
             calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
             calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
             calendar.set(java.util.Calendar.MINUTE, 0);
             calendar.set(java.util.Calendar.SECOND, 0);
             startDate = calendar.getTime();
             calendar.add(java.util.Calendar.MONTH, 1);
             calendar.add(java.util.Calendar.SECOND, -1);
             endDate = calendar.getTime();
         }
         if(period.equals("weekly"))
         {
             int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);
             int firstDayOfWeek = calendar.getFirstDayOfWeek();
             if(dayOfWeek > firstDayOfWeek)
             {
                 calendar.add(java.util.Calendar.DAY_OF_MONTH, - (dayOfWeek-firstDayOfWeek));
             }
             if(dayOfWeek < firstDayOfWeek)
             {
                 calendar.add(java.util.Calendar.DAY_OF_MONTH, - (dayOfWeek+7-firstDayOfWeek));
             }
             calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
             calendar.set(java.util.Calendar.MINUTE, 0);
             calendar.set(java.util.Calendar.SECOND, 0);
             startDate = calendar.getTime();
             calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1);
             calendar.add(java.util.Calendar.SECOND, -1);
             endDate = calendar.getTime();
         }
 
         String range = parameters.get("range","all");
         String textQuery = parameters.get("text_query", "");
         long firstCatId = parameters.getLong("category_id_1", -1);
         long secondCatId = parameters.getLong("category_id_2", -1);
 
         templatingContext.put("day",new Integer(day));
         templatingContext.put("month",new Integer(month));
         templatingContext.put("year",new Integer(year));
         templatingContext.put("period", period);
 		templatingContext.put("range", range);
 		templatingContext.put("category_id_1",new Long(firstCatId));
 		templatingContext.put("category_id_2",new Long(secondCatId));
         templatingContext.put("start_date", startDate);
         templatingContext.put("end_date", endDate);
 		
 		try
 		{
 			SiteResource site = cmsData.getSite();
 			Parameters screenConfig = getScreenConfig();
 			Resource[] pools = null;
 			long indexId = screenConfig.getLong("index_id",-1);
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
 			CalendarSearchMethod method = new CalendarSearchMethod(
                 searchService, parameters, i18nContext.getLocale(), logger, startDate, endDate, textQuery);
 			templatingContext.put("query", method.getQueryString(coralSession));
 			TableFilter filter = new HitsViewPermissionFilter(coralSession.getUserSubject(), coralSession);			
 			TableState state = 
                 tableStateManager.getState(context, "cms.documents.calendar.results."+site.getName());
 			method.setupTableState(state);
 			
 			// - prepare search handler
 			SearchHandler searchHandler = 
                 new LuceneSearchHandler(context, searchService, integrationService, cmsDataFactory);
 					
 			// - execute seach and put results into the context
             ArrayList filters = new ArrayList();
             filters.add(filter);
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
