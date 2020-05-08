 package uk.ac.ox.oucs.oauth.servlet;
 
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.tool.api.*;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 import uk.ac.ox.oucs.oauth.domain.Accessor;
 import uk.ac.ox.oucs.oauth.domain.Consumer;
 import uk.ac.ox.oucs.oauth.service.OAuthHttpService;
 import uk.ac.ox.oucs.oauth.service.OAuthService;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * @author Colin Hebert
  */
 public class AuthorisationServlet extends HttpServlet {
     /**
      * Name of the "authorise" button in the authorisation page
      */
     public static final String AUTHORISE_BUTTON = "authorise";
     /**
      * Name of the "deny" button in the authorisation page
      */
     public static final String DENY_BUTTON = "deny";
     /**
      *
      */
     private static final String LOGIN_PATH = "/login";
 
 
     //Services and settings
     private OAuthService oAuthService;
     private OAuthHttpService oAuthHttpService;
     private UserDirectoryService userDirectoryService;
     private SessionManager sessionManager;
     private ActiveToolManager activeToolManager;
     private ServerConfigurationService serverConfigurationService;
     private String authorisePath;
     private static final String SAKAI_LOGIN_TOOL = "sakai.login";
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         oAuthService = (OAuthService) ComponentManager.getInstance().get(OAuthService.class.getCanonicalName());
         oAuthHttpService = (OAuthHttpService) ComponentManager.getInstance().get(OAuthHttpService.class.getCanonicalName());
         sessionManager = (SessionManager) ComponentManager.getInstance().get(SessionManager.class.getCanonicalName());
         activeToolManager = (ActiveToolManager) ComponentManager.getInstance().get(ActiveToolManager.class.getCanonicalName());
         userDirectoryService = (UserDirectoryService) ComponentManager.getInstance().get(UserDirectoryService.class.getCanonicalName());
         serverConfigurationService = (ServerConfigurationService) ComponentManager.getInstance().get(ServerConfigurationService.class.getCanonicalName());
         //TODO: get this path from the configuration (injection?)
         authorisePath = "/authorise.jsp";
     }
 
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         handleRequest(request, response);
     }
 
     @Override
     public void doPost(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         handleRequest(request, response);
     }
 
     /**
      * Filter users trying to obtain an OAuth authorisation token.
      * <p/>
      * Three outcomes are possible:
      * <ul>
      * <li>The user isn't logged in the application: He's sent toward the login tool</li>
      * <li>The user is logged in but hasn't filled the authorisation form: He's sent toward the form</li>
      * <li>The user has filled the form: The OAuth system attenpts to grant a token</li>
      * </ul>
      *
      * @param request  current servlet request
      * @param response current servlet response
      * @throws IOException
      * @throws ServletException
      */
     private void handleRequest(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
 
         String pathInfo = request.getPathInfo();
         String currentUserId = sessionManager.getCurrentSessionUserId();
         if (currentUserId == null || (pathInfo != null && pathInfo.startsWith(LOGIN_PATH)))
             //Redirect non logged-in users and currently logging-in users requests
             sendToLoginPage(request, response);
 
         else if (request.getParameter(AUTHORISE_BUTTON) == null && request.getParameter(DENY_BUTTON) == null)
             //If logged-in but haven't yet authorised (or denied)
             sendToAuthorisePage(request, response);
 
         else
             //Even if the authorisation has been denied, send the client to the consumer's callback
             handleRequestAuth(request, response);
     }
 
     private void sendToLoginPage(HttpServletRequest request, HttpServletResponse response) throws ToolException {
         //If not logging-in, set the return path and proceed to the login steps
         String pathInfo = request.getPathInfo();
         if (pathInfo == null || !pathInfo.startsWith(LOGIN_PATH)) {
             Session session = sessionManager.getCurrentSession();
 
             //Set the return path for after login if needed (Note: in session, not tool session, special for Login helper)
             StringBuffer returnUrl = request.getRequestURL();
             if (request.getQueryString() != null)
                 returnUrl.append('?').append(request.getQueryString());
             session.setAttribute(Tool.HELPER_DONE_URL, returnUrl.toString());
         }
 
         //Redirect to the login tool
         ActiveTool loginTool = activeToolManager.getActiveTool(SAKAI_LOGIN_TOOL);
         String context = request.getContextPath() + request.getServletPath() + LOGIN_PATH;
         loginTool.help(request, response, context, LOGIN_PATH);
     }
 
     /**
      * Pre-set request attributes to provide additional information on the authorisation form.
      *
      * @param request  current servlet request
      * @param response current servlet response
      * @throws IOException
      * @throws ServletException
      */
     private void sendToAuthorisePage(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
 
        Accessor accessor = oAuthService.getAccessor(request.getParameter("oauthToken"), Accessor.Type.REQUEST);
         Consumer consumer = oAuthService.getConsumer(accessor.getConsumerId());
         accessor = oAuthService.startAuthorisation(accessor.getToken());
 
         User user = userDirectoryService.getCurrentUser();
         request.setAttribute("userName", user.getDisplayName());
         request.setAttribute("userId", user.getDisplayId());
         request.setAttribute("uiName", serverConfigurationService.getString("ui.service", "Sakai"));
         request.setAttribute("skinPath", serverConfigurationService.getString("skin.repo", "/library/skin"));
         request.setAttribute("defaultSkin", serverConfigurationService.getString("skin.default", "default"));
         request.setAttribute("authorise", AUTHORISE_BUTTON);
         request.setAttribute("deny", DENY_BUTTON);
         request.setAttribute("oauthVerifier", accessor.getVerifier());
         request.setAttribute("appName", consumer.getName());
         request.setAttribute("appDesc", consumer.getDescription());
         request.setAttribute("token", accessor.getToken());
 
         request.getRequestDispatcher(authorisePath).forward(request, response);
     }
 
     /**
      * Pre-set request attributes to provide additional information to the token creation process.
      *
      * @param request  current servlet request
      * @param response current servlet response
      * @throws IOException
      * @throws ServletException
      */
     private void handleRequestAuth(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         boolean authorised = request.getParameter(AUTHORISE_BUTTON) != null && request.getParameter(DENY_BUTTON) == null;
         String token = request.getParameter("oauthToken");
         String verifier = request.getParameter("oauthVerifier");
         String userId = sessionManager.getCurrentSessionUserId();
 
         oAuthHttpService.handleRequestAuthorisation(request, response, authorised, token, verifier, userId);
     }
 }
