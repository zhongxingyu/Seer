 package net.cyklotron.cms.modules.views.security;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.authentication.DefaultPrincipal;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.coral.security.Role;
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
 import net.cyklotron.cms.security.SecurityService;
 import net.cyklotron.cms.site.SiteResource;
 
 /**
  *
  */
 public class MemberList
     extends BaseSecurityScreen
 {
     protected UserManager userManager;
 
     protected TableColumn[] columns;
 
     
     
     public MemberList(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager, SecurityService securityService,
         UserManager userManager)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager,
                         securityService);
         this.userManager = userManager;
         try
         {
             columns = new TableColumn[5];
             columns[0] = new TableColumn("id", null);
             columns[1] = new TableColumn("login", new MapComparator("login"));
             columns[2] = new TableColumn("name", new MapComparator("name"));
             columns[3] = new TableColumn("administrator", null);
             columns[4] = new TableColumn("member_since", new MapComparator("member_since"));
         }
         catch(TableException e)
         {
             throw new ComponentInitializationError("failed to initialize table columns", e);
         }
     }
 
     public void process(Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
             SiteResource site = getSite();
             Role teamMember = site.getTeamMember();
             Subject[] members = teamMember.getSubjects();
             ArrayList memberList = new ArrayList();
             for(int i=0; i<members.length; i++)
             {
                 HashMap memberDesc = new HashMap();
                 memberDesc.put("id",members[i].getIdObject());
                 memberDesc.put("login", userManager.getLogin(members[i].getName()));
                 Parameters pc = new DirectoryParameters(userManager.getPersonalData(new DefaultPrincipal(members[i].getName())));
                memberDesc.put("name", pc.get("cn", null));
                 if(members[i].hasRole(site.getAdministrator()))
                 {
                     memberDesc.put("administrator", Boolean.TRUE);
                 }
                 RoleAssignment[] assignments = members[i].getRoleAssignments();
                 for(int j=0; j<assignments.length; j++)
                 {
                     if(assignments[j].getRole().equals(site.getTeamMember()))
                     {
                         memberDesc.put("member_since", assignments[j].getGrantTime());
                     }
                 }
                 memberList.add(memberDesc);
             }
             TableState state = tableStateManager.getState(context, getTableName());
             if(state.isNew())
             {
                 state.setTreeView(false);
                 state.setPageSize(10);
             }
             TableModel model = new ListTableModel(memberList, columns);
             templatingContext.put("table", new TableTool(state, null, model));
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
 
     public String getTableName()
     {
         return "screens:cms:security,MemberList";
     }
 }
