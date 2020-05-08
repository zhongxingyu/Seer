 package ru.mail.jira.plugins;
 
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.exception.VelocityException;
 import ru.mail.jira.plugins.structs.TimePair;
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.ComponentManager;
 import com.atlassian.jira.component.ComponentAccessor;
 import com.atlassian.jira.security.JiraAuthenticationContext;
 import com.atlassian.jira.security.groups.GroupManager;
 import com.atlassian.jira.util.json.JSONException;
 
 /**
  * @author grundic
  */
 @Path("/arrival-mark")
 public class ArrivalMark
 {
     private static Log log = LogFactory.getLog(ArrivalMark.class);
 
     private final GroupManager groupManager;
 
     private final ConfigurationManager configurationManager;
 
     public ArrivalMark(
         ConfigurationManager configurationManager,
         GroupManager groupManager)
     {
         this.configurationManager = configurationManager;
         this.groupManager = groupManager;
     }
 
     private boolean checkUserGroups(User user)
     {
         if (user == null)
         {
             return false;
         }
 
         String[] arrSavedGroups = configurationManager.getReportGroups();
         Collection<String> cUserGroups = groupManager.getGroupNamesForUser(user.getName());
         if (arrSavedGroups == null || cUserGroups == null)
         {
             return false;
         }
 
         Collection <String> userGroups = new HashSet<String>(cUserGroups);
         Collection <String> savedGroups = new HashSet<String>(Arrays.asList(arrSavedGroups));
 
         userGroups.retainAll(savedGroups);
         return userGroups.size() > 0;
     }
 
     @GET
     @Produces ({ MediaType.APPLICATION_JSON})
     @Path("/arrivals")
     public Response getArrivals(@Context HttpServletRequest req)
     throws JSONException
     {
         JiraAuthenticationContext authenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
         User user = authenticationContext.getLoggedInUser();
 
         Map<String, Object> params = ArrivalUtils.getParams(authenticationContext);
         params.put("i18n", authenticationContext.getI18nHelper());
         params.put("baseUrl", getBaseUrl(req));
 
         if (checkUserGroups(user))
         {
             RegistratorSQL reg = new RegistratorSQL(configurationManager);
             try
             {
                 TimePair timePair = reg.getRegistrationTime(user.getName());
                 if (timePair.isTimeOn() || timePair.isCTime())
                 {
                     params.put("arrivals", reg.getWeekReport(user.getName()));
                    params.put("header_fmt", new SimpleDateFormat("d/MMM [EEE]"));
                     params.put("arrival_fmt", new SimpleDateFormat("HH:mm:ss"));
                     params.put("view", "0");
                 }
                 else
                 {
                     params.put("arrivals", reg.getWeekReport(user.getName()));
                    params.put("header_fmt", new SimpleDateFormat("d/MMM [EEE]"));
                     params.put("arrival_fmt", new SimpleDateFormat("HH:mm:ss"));
                     params.put("view", "1");
                 }
             }
             catch (SQLException e)
             {
                 log.error("SQL processing exception", e);
                 params.put("error", e.toString());
                 params.put("view", "2");
             }
         }
         else
         {
             params.put("view", "3");
         }
 
         try
         {
             return Response.ok(new HTMLRepresentation(ComponentAccessor.getVelocityManager().getBody("templates/", "arrival-gadget.vm", params))).build();
         }
         catch (VelocityException e)
         {
             log.error("velocity exception", e);
             return Response.serverError().build();
         }
     }
 
     /**
      * Return context path of JIRA.
      */
     private String getBaseUrl(HttpServletRequest req)
     {
         return (req.getScheme() + "://" + req.getServerName() + ":" +
             req.getServerPort() + req.getContextPath());
     }
 }
