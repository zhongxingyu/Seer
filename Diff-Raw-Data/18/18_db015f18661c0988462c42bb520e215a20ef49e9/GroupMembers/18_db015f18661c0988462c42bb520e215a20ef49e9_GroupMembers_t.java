 package net.cyklotron.cms.modules.views.security;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.authentication.DefaultPrincipal;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.context.Context;
 import org.objectledge.coral.security.RoleAssignment;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.directory.DirectoryParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableColumn;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.table.comparator.MapComparator;
 import org.objectledge.table.generic.ListTableModel;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.security.RoleResource;
 import net.cyklotron.cms.security.RoleResourceImpl;
 import net.cyklotron.cms.security.SecurityService;
 
 public class GroupMembers
     extends BaseSecurityScreen
 {
     protected UserManager userManager;
 
     protected TableColumn[] columns;
 
     public GroupMembers(Context context, Logger logger, PreferencesService preferencesService,
         CmsDataFactory cmsDataFactory, TableStateManager tableStateManager,
         SecurityService securityService, UserManager userManager)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager,
                         securityService);
         this.userManager = userManager;
         try
         {
             columns = new TableColumn[4];
             columns[0] = new TableColumn<Map<String, Long>>("id", null);
             columns[1] = new TableColumn<Map<String, String>>("login",
                 new MapComparator<String, String>("login"));
             columns[2] = new TableColumn<Map<String, String>>("name",
                 new MapComparator<String, String>("name"));
             columns[3] = new TableColumn<Map<String, Date>>("member_since",
                 new MapComparator<String, Date>("member_since"));
         }
         catch(TableException e)
         {
             throw new ComponentInitializationError("failed to initialize table columns", e);
         }
     }
 
     @Override
     public void process(Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext,
         CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             long groupId = parameters.getLong("group_id");
             RoleResource group = RoleResourceImpl.getRoleResource(coralSession, groupId);
            RoleAssignment[] assignments = group.getRole().getRoleAssignments();
             List<Map<String, Object>> memberList = new ArrayList<Map<String, Object>>(
                assignments.length);
            for (RoleAssignment assignment : assignments)
             {
                Subject subject = assignment.getSubject();
                 Parameters personalData = new DirectoryParameters(userManager
                     .getPersonalData(new DefaultPrincipal(subject.getName())));
                 Map<String, Object> memberDesc = new HashMap<String, Object>();
                 memberDesc.put("id", subject.getIdObject());
                 memberDesc.put("login", userManager.getLogin(subject.getName()));
                 memberDesc.put("name", personalData.get("cn", ""));
                memberDesc.put("member_since", assignment.getGrantTime().getTime());
                 memberList.add(memberDesc);
             }
             TableState state = tableStateManager.getState(context, "view:security,GroupMemberList");
             if(state.isNew())
             {
                 state.setTreeView(false);
                 state.setPageSize(10);
             }
             TableModel<Map<String, Object>> model = new ListTableModel<Map<String, Object>>(
                 memberList, columns);
             templatingContext.put("table", new TableTool<Map<String, Object>>(state, null, model));
             templatingContext.put("group", group);
             if(cmsSecurityService.isGroupResource(group))
             {
                 templatingContext.put("groupName", cmsSecurityService.getShortGroupName(group));
             }
         }
         catch(Exception e)
         {
             if(e instanceof ProcessingException)
             {
                 throw (ProcessingException)e;
             }
             throw new ProcessingException("failed to retreive data", e);
         }
     }
 }
