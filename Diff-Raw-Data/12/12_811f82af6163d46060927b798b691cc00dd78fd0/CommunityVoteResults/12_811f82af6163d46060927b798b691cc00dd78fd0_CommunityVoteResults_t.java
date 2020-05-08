 package net.cyklotron.cms.modules.components.documents;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.modules.components.SkinableCMSComponent;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.StructureException;
 import net.cyklotron.cms.structure.vote.CommunityVote;
 import net.cyklotron.cms.structure.vote.SortOrder;
 import net.cyklotron.cms.util.CmsResourceListTableModel;
 import net.cyklotron.cms.util.ProtectedValidityViewFilter;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.templating.Templating;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 public class CommunityVoteResults
     extends SkinableCMSComponent
 {
     private final CommunityVote communityVote;
 
     private final IntegrationService integrationService;
 
     private final TableStateManager tableStateManager;
 
     public CommunityVoteResults(Context context, Logger logger, Templating templating,
         CmsDataFactory cmsDataFactory, SkinService skinService, MVCFinder mvcFinder,
         CommunityVote communityVote, IntegrationService integrationService,
         TableStateManager tableStateManager)
     {
         super(context, logger, templating, cmsDataFactory, skinService, mvcFinder);
         this.communityVote = communityVote;
         this.integrationService = integrationService;
         this.tableStateManager = tableStateManager;
     }
 
     @Override
     public void process(Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext,
         CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             CmsData cmsData = cmsDataFactory.getCmsData(context);
             List<NavigationNodeResource> list = new ArrayList<NavigationNodeResource>();
             CmsResourceListTableModel<NavigationNodeResource> model = new CmsResourceListTableModel<NavigationNodeResource>(
                 context, integrationService, list, i18nContext.getLocale());
 
             Parameters componentConfiguration = cmsData.getComponent().getConfiguration();
             int cutoffDateOffset = componentConfiguration.getInt("cutoffDateOffset", 30);
            
            String primarySortOrderNames[] = componentConfiguration.getStrings("primarySortOrders");
            if(primarySortOrderNames.length == 0)
            {
                primarySortOrderNames = "POSITIVE,NEGATIVE,TOTAL".split(",");
            }
            
             String secondarySortOrder = componentConfiguration.get("secondarySortOrder",
                 "priority.validity.start");
             String secondarySortDirection = componentConfiguration.get(
                 "secondarySortDirection", "ASC");
             int resulPageSize = componentConfiguration.getInt("resultPageSize", 10);
 
             Calendar cal = new GregorianCalendar();
             cal.set(Calendar.HOUR_OF_DAY, 0);
             cal.set(Calendar.MINUTE, 0);
             cal.set(Calendar.SECOND, 0);
             cal.set(Calendar.MILLISECOND, 0);
             cal.set(Calendar.DAY_OF_MONTH, -cutoffDateOffset);
             Date cutoffDate = cal.getTime();
             Set<SortOrder> primarySortOrders = new HashSet<SortOrder>();
            for(String sortOrderName : primarySortOrderNames)
             {
                 primarySortOrders.add(SortOrder.valueOf(sortOrderName));
             }
             Comparator<NavigationNodeResource> secondarySortOrderComparator = model.getColumn(
                 secondarySortOrder).getComparator();
             if("DESC".equals(secondarySortDirection))
             {
                 secondarySortOrderComparator = Collections
                     .reverseOrder(secondarySortOrderComparator);
             }
 
             Map<SortOrder, List<NavigationNodeResource>> results = communityVote.getResults(
                 cutoffDate, primarySortOrders, secondarySortOrderComparator, coralSession);
 
             TableFilter<NavigationNodeResource> filter = new ProtectedValidityViewFilter<NavigationNodeResource>(
                 coralSession, cmsData, coralSession.getUserSubject());
 
             List<TableFilter<NavigationNodeResource>> tableFilters = Collections
                 .singletonList(filter);
 
             List<String> sortOrderNames = new ArrayList<String>(primarySortOrders.size());
 
             for(SortOrder sortOrder : primarySortOrders)
             {
                 TableState tableState = tableStateManager.getState(context,
                     "cms.structure.communityVote/" + cmsData.getNode().getIdString() + "/"
                         + cmsData.getComponent().getInstanceName() + sortOrder.name());
                 if(tableState.isNew())
                 {
                     tableState.setTreeView(false);
                     tableState.setPageSize(resulPageSize);
                 }
                 TableModel<NavigationNodeResource> tableModel = new CmsResourceListTableModel<NavigationNodeResource>(
                     context, integrationService, results.get(sortOrder), i18nContext.getLocale());
                 TableTool<NavigationNodeResource> tableTool = new TableTool<NavigationNodeResource>(
                     tableState, tableFilters, tableModel);
                 templatingContext.put("table_" + sortOrder.name(), tableTool);
                 sortOrderNames.add(sortOrder.name());
             }
             Collections.sort(sortOrderNames);
             templatingContext.put("tables", sortOrderNames);
         }
         catch(Exception e)
         {
             throw new ProcessingException(e);
         }
     }
 }
