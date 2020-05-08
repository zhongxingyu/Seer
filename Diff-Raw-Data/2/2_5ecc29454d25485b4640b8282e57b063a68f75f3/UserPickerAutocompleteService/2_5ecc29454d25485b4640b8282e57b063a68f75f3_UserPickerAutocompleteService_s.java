 /*
  * Created by Dmitry Miroshnichenko 12-02-2013. Copyright Mail.Ru Group 2013.
  * All rights reserved.
  */
 package ru.mail.jira.plugins.up;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.log4j.Logger;
 import org.apache.velocity.exception.VelocityException;
 
 import ru.mail.jira.plugins.up.common.Utils;
 import ru.mail.jira.plugins.up.structures.AutocompleteUniversalData;
 import ru.mail.jira.plugins.up.structures.HtmlEntity;
 import ru.mail.jira.plugins.up.structures.ISQLDataBean;
 import ru.mail.jira.plugins.up.structures.ProjRole;
 
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.ComponentManager;
 import com.atlassian.jira.component.ComponentAccessor;
 import com.atlassian.jira.issue.fields.CustomField;
 import com.atlassian.jira.project.Project;
 import com.atlassian.jira.security.JiraAuthenticationContext;
 import com.atlassian.jira.security.Permissions;
 import com.atlassian.jira.security.groups.GroupManager;
 import com.atlassian.jira.security.roles.ProjectRoleManager;
 import com.atlassian.jira.user.UserProjectHistoryManager;
 import com.atlassian.jira.user.util.UserUtil;
 import com.atlassian.jira.util.I18nHelper;
 import com.atlassian.jira.util.json.JSONException;
 
 
 @Path("/upautocompletesrv")
 public class UserPickerAutocompleteService
 {
     private final Logger log = Logger
         .getLogger(UserPickerAutocompleteService.class);
 
     private final PluginData settings;
 
     private final GroupManager grMgr;
 
     private final ProjectRoleManager projectRoleManager;
 
     public UserPickerAutocompleteService(PluginData settings,
         GroupManager grMgr, ProjectRoleManager projectRoleManager)
     {
         this.settings = settings;
         this.grMgr = grMgr;
         this.projectRoleManager = projectRoleManager;
     }
 
     @POST
     @Path("/getcfvals")
     @Produces({MediaType.APPLICATION_JSON})
     public Response getCfVals(@Context HttpServletRequest req)
     {
         JiraAuthenticationContext authCtx = ComponentManager.getInstance()
             .getJiraAuthenticationContext();
         I18nHelper i18n = authCtx.getI18nHelper();
         User currentUser = authCtx.getLoggedInUser();
         UserProjectHistoryManager userProjectHistoryManager = ComponentManager
             .getComponentInstanceOfType(UserProjectHistoryManager.class);
         Project currentProject = userProjectHistoryManager.getCurrentProject(
             Permissions.BROWSE, authCtx.getLoggedInUser());
         if (currentUser == null)
         {
             log.error("UserPickerAutocompleteService::getCfVals - User is not logged");
             return Response.ok(i18n.getText("mailru.service.user.notlogged"))
                 .status(401).build();
         }
 
         String cfId = req.getParameter("cf_id");
         String issueKey = req.getParameter("issue_id");
         String pattern = req.getParameter("pattern");
         String rowCount = req.getParameter("rowcount");
 
         AutocompleteUniversalData data;
         List<ISQLDataBean> values = null;
 
         if (Utils.isValidStr(cfId) && issueKey != null
             && Utils.isValidLongParam(rowCount) && pattern != null)
         {
             CustomField cf = ComponentManager.getInstance()
                 .getCustomFieldManager().getCustomFieldObject(cfId);
             if (cf == null)
             {
                 log.error("UserPickerAutocompleteService::getCfVals - Custom field is null. Incorrect data in plugin settings");
                 return Response
                     .ok(i18n.getText("mailru.service.error.cfid.invalid"))
                     .status(400).build();
             }
 
             Set<String> storedData;
             if (Utils.isOfGroupRoleUserPickerType(cf.getCustomFieldType()
                 .getKey()))
             {
                 Map<String, String> map = new HashMap<String, String>();
                 List<String> groups = new ArrayList<String>();
                 List<ProjRole> projRoles = new ArrayList<ProjRole>();
                 try
                 {
                     Utils.fillDataLists(settings.getRoleGroupFieldData(cfId),
                         groups, projRoles);
                 }
                 catch (JSONException e)
                 {
                     log.error(
                         "AdRoleGroupUserCfService::getVelocityParameters - Incorrect field data",
                         e);
                     // --> impossible
                 }
 
                 for (String group : groups)
                 {
                     Collection<User> users = grMgr.getUsersInGroup(group);
                     if (users != null)
                     {
                         for (User user : users)
                         {
                             map.put(user.getName(), user.getDisplayName());
                         }
                     }
                 }
 
                 for (ProjRole pr : projRoles)
                 {
                     if (currentProject != null
                         && currentProject.getId().toString()
                             .equals(pr.getProject()))
                     {
                         map.putAll(Utils.getProjectRoleUsers(
                             projectRoleManager, pr.getRole(), currentProject));
                     }
                 }
                 storedData = map.keySet();
             }
             else
             {
                 storedData = settings.getStoredUsers(cfId);
             }
 
             if (storedData != null)
             {
                 UserUtil userUtil = ComponentManager.getInstance()
                     .getUserUtil();
                 values = new ArrayList<ISQLDataBean>();
                 User user;
 
                 long elemsAdded = 0;
                 long maxElems = Long.valueOf(rowCount);
                 pattern = pattern.toUpperCase();
                 for (String username : storedData)
                 {
                     user = userUtil.getUserObject(username);
                     if (user.getName().toUpperCase().indexOf(pattern) != -1
                         || (user.getDisplayName() != null && user
                            .getDisplayName().indexOf(pattern) != -1))
                     {
                         data = new AutocompleteUniversalData();
                         data.setName(user.getName());
                         data.setDescription(user.getDisplayName());
 
                         values.add(data);
                         elemsAdded++;
                     }
                     if (elemsAdded >= maxElems)
                     {
                         break;
                     }
                 }
             }
             else
             {
                 log.error("UserPickerAutocompleteService::getCfVals - Incorrect stored data");
             }
         }
         else
         {
             log.error("UserPickerAutocompleteService::getCfVals - Incorrect parameters");
             return Response
                 .ok(i18n.getText("mailru.service.error.parameters.invalid"))
                 .status(400).build();
         }
 
         Response resp;
         if (values != null)
         {
             GenericEntity<List<ISQLDataBean>> retVal = new GenericEntity<List<ISQLDataBean>>(
                 values)
             {
             };
 
             resp = Response.ok().entity(retVal).status(200).build();
         }
         else
         {
             resp = Response.ok().status(200).build();
         }
 
         return resp;
     }
 
     @POST
     @Path("/getuserhtml")
     @Produces({MediaType.APPLICATION_JSON})
     public Response getUserHtml(@Context HttpServletRequest req)
     {
         JiraAuthenticationContext authCtx = ComponentManager.getInstance()
             .getJiraAuthenticationContext();
         I18nHelper i18n = authCtx.getI18nHelper();
         User user = authCtx.getLoggedInUser();
         if (user == null)
         {
             log.error("UserPickerAutocompleteService::validateInput - User is not logged");
             return Response.ok(i18n.getText("mailru.service.user.notlogged"))
                 .status(401).build();
         }
 
         String cfId = req.getParameter("cf_id");
         String cfValue = req.getParameter("cf_value");
 
         if (!Utils.isValidStr(cfId) || !Utils.isValidStr(cfValue))
         {
             log.error("UserPickerAutocompleteService::validateInput - Invalid parameters");
             return Response
                 .ok(i18n.getText("mailru.service.error.parameters.invalid"))
                 .status(400).build();
         }
 
         CustomField cf = ComponentManager.getInstance().getCustomFieldManager()
             .getCustomFieldObject(cfId);
         if (cf == null)
         {
             log.error("UserPickerAutocompleteService::validateInput - Custom field is null. Incorrect data in plugin settings");
             return Response
                 .ok(i18n.getText("mailru.service.error.cfid.invalid"))
                 .status(400).build();
         }
 
         AutocompleteUniversalData entity = new AutocompleteUniversalData();
         if (Utils.isOfMultiUserType(cf.getCustomFieldType().getKey()))
         {
             User userParam = ComponentManager.getInstance().getUserUtil()
                 .getUserObject(cfValue);
             if (userParam == null)
             {
                 // nothing to do. Sending object with empty key
             }
             else
             {
                 entity.setName(userParam.getName());
                 entity.setDescription(userParam.getDisplayName());
 
                 Map<String, Object> params = new HashMap<String, Object>();
                 params.put("cfid", cfId);
                 params.put("baseUrl", Utils.getBaseUrl(req));
                 params.put("data", entity);
                 params.put("i18n", i18n);
 
                 try
                 {
                     String body = ComponentAccessor.getVelocityManager()
                         .getBody("templates/aduserpicker/",
                             "edit-multiuserpicker-representation.vm", params);
                     return Response.ok(new HtmlEntity(body)).build();
                 }
                 catch (VelocityException vex)
                 {
                     log.error(
                         "AutocompleteService::manageInput - Velocity parsing error",
                         vex);
                     return Response
                         .ok(i18n.getText("mailru.service.velocity.parseerror"))
                         .status(500).build();
                 }
             }
         }
 
         return Response.ok().status(200).build();
     }
 }
