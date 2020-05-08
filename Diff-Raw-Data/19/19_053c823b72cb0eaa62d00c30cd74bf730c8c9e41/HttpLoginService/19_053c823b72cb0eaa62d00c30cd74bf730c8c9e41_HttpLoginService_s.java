 package org.dspace.webmvc.model.login;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.dspace.core.ConfigurationManager;
 import org.dspace.core.Context;
 import org.dspace.eperson.EPerson;
 import org.dspace.webmvc.utils.DSpaceRequestUtils;
 import org.dspace.webmvc.utils.RequestInfo;
 import org.dspace.webmvc.utils.RequestInfoService;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.sql.SQLException;
 import java.util.Locale;
 
 public class HttpLoginService implements LoginService {
     private static final Logger log = Logger.getLogger(HttpLoginService.class);
 
     private static final String DSPACE_CURRENT_USER_ID = "dspace.current.user.id";
     private static final String DSPACE_CURRENT_REMOTE_ADDR = "dspace.current.remote.addr";
 
     private HttpServletRequest request;
 
     private RequestInfoService ris = new RequestInfoService();
 
     public HttpLoginService(HttpServletRequest pRequest) {
         request = pRequest;
     }
 
     public EPerson getUser(Context context) {
         HttpSession session = request.getSession();
         Integer userID = (Integer) session.getAttribute(DSPACE_CURRENT_USER_ID);
 
         if (userID != null) {
             String remAddr = (String)session.getAttribute(DSPACE_CURRENT_REMOTE_ADDR);
             if (remAddr != null && remAddr.equals(request.getRemoteAddr())) {
                 try {
                     return EPerson.find(context, userID.intValue());
                 } catch (SQLException e) {
                     log.error("Unable to retrieve EPerson details", e);
                 }
             } else {
                 log.warn("POSSIBLE HIJACKED SESSION: request from " + request.getRemoteAddr() + " does not match original " + "session address: " + remAddr + ". Authentication rejected.");
             }
         }
 
         return null;
     }
 
     public void createUserSession(Context context, EPerson person) {
         HttpSession session = request.getSession();
 
         // For security reasons after login, give the user a new session
         if ((!session.isNew()) && (session.getAttribute(DSPACE_CURRENT_USER_ID) == null)) {
 
             // Keep the user's locale setting if set
             Locale sessionLocale = DSpaceRequestUtils.getSessionLocale(request);
 
             // Get info about the interrupted request, if set
             RequestInfo requestInfo = ris.getRequestInfoSession(request);
 
             // Get the original URL of interrupted request, if set
 //            String requestUrl = (String) session.getAttribute("interrupted.request.url");
 
             // Invalidate session unless dspace.cfg says not to
             if (ConfigurationManager.getBooleanProperty("webui.session.invalidate", true)) {
                 session.invalidate();
             }
 
             // Give the user a new session
             session = request.getSession();
 
             // Restore the session locale
             if (sessionLocale != null) {
                 DSpaceRequestUtils.setSessionLocale(request, sessionLocale);
             }
 
             // Restore interrupted request information and url to new session
             if (requestInfo != null) {
                 ris.setRequestInfoSession(request, requestInfo);
 //                session.setAttribute("interrupted.request.info", requestInfo);
 //                session.setAttribute("interrupted.request.url", requestUrl);
             }
         }
 
         context.setCurrentUser(person);
 
 //        boolean isAdmin = false;
 
 //        try {
 //            isAdmin = AuthorizeManager.isAdmin(context);
 //        }
 //        catch (SQLException se) {
 //            log.warn("Unable to use AuthorizeManager " + se);
 //        } finally {
 //            request.setAttribute("is.admin", Boolean.valueOf(isAdmin));
 //        }
 
         // We store the current user in the request as an EPerson object...
 //        request.setAttribute("dspace.current.user", person);
 
         // and in the session as an ID
         session.setAttribute(DSPACE_CURRENT_USER_ID, Integer.valueOf(person.getID()));
 
         // and the remote IP address to compare against later requests
         // so we can detect session hijacking.
         session.setAttribute(DSPACE_CURRENT_REMOTE_ADDR, request.getRemoteAddr());
     }
 
     public String getInterruptedRequestURL() {
         RequestInfo interruptedRequest = ris.getRequestInfoSession(request);
        if (StringUtils.isEmpty(interruptedRequest.getServletPath())) {
            return interruptedRequest.getActualPath();
         }
 
        return interruptedRequest.getServletPath();
     }
 }
