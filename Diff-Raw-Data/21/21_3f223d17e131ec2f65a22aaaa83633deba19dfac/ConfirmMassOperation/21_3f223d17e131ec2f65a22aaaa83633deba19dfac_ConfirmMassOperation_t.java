 package net.cyklotron.cms.modules.views;
 
 import java.util.ArrayList;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.CmsNodeResource;
 import net.cyklotron.cms.CmsNodeResourceImpl;
 import net.cyklotron.cms.forum.ForumService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.util.ProtectedViewFilter;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.ResourceListTableModel;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 public class ConfirmMassOperation
   extends BaseCMSScreen 
 {
     protected ForumService forumService;
 
     protected WorkflowService workflowService;
 
     public ConfirmMassOperation(Context context, Logger logger, PreferencesService preferencesService,
         CmsDataFactory cmsDataFactory, TableStateManager tableStateManager, 
         ForumService forumService, WorkflowService workflowService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager);
         this.forumService = forumService;
         this.workflowService = workflowService;
     }
 
     @Override
     public void process(Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext,
         CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             ArrayList<Resource> list = new ArrayList<Resource>();
             long[] ids = parameters.getLongs("op_node_id");
             for (long id : ids)
             {
                 CmsNodeResource node = CmsNodeResourceImpl.getCmsNodeResource(
                     coralSession, id);
                 
                 if(node.canView(coralSession, coralSession.getUserSubject()))
                 {
                     list.add(node);
                 }
             }
             TableState state = tableStateManager.getState(context,
                 "cms:screens:structure.ConfirmMassOperation");
             if(state.isNew())
             {
                 state.setPageSize(0);
             }
             TableModel model = new ResourceListTableModel(list, i18nContext.getLocale());
             try
             {
                 ArrayList<TableFilter> filters = new ArrayList<TableFilter>();
                 filters.add(new ProtectedViewFilter(coralSession, coralSession.getUserSubject()));
                 TableTool helper = new TableTool(state, filters, model);
                 templatingContext.put("table", helper);
             }
             catch(TableException e)
             {
                 throw new ProcessingException("failed to create TableTool", e);
             }
             templatingContext.put("operation", parameters.get("operation"));
             templatingContext.put("permission", parameters.get("permission"));
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to prepare list of nodes", e);
         }
     }
 
     public boolean checkAccessRights(Context context)
         throws ProcessingException
     {
         return true;
     }
 
 }
