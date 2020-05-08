 package gov.nih.nci.ncicb.cadsr.common.security;
 
 import gov.nih.nci.ncicb.cadsr.common.CaDSRConstants;
 import gov.nih.nci.ncicb.cadsr.common.formbuilder.common.FormBuilderConstants;
 import gov.nih.nci.ncicb.cadsr.common.servicelocator.ApplicationServiceLocator;
 import gov.nih.nci.ncicb.cadsr.common.servicelocator.ServiceLocatorException;
 import gov.nih.nci.ncicb.cadsr.common.util.SessionUtils;
 
 import gov.nih.nci.ncicb.cadsr.common.util.TimeUtils;
 import java.io.IOException;
 
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 public class LogoutServlet extends HttpServlet {
  private static String LOGOUT_JSP = "jsp/logout.jsp";
  private static String AUTHORIZATION_ERROR_JSP = "jsp/authorizationError.jsp";
   private String[] logoutKeys =
     { CaDSRConstants.USER_KEY, CaDSRConstants.USER_CONTEXTS };
   protected static Log log = LogFactory.getLog(LogoutServlet.class.getName());
   
   public LogoutServlet() {
   }
 
   protected void doGet(
     HttpServletRequest p0,
     HttpServletResponse p1) throws ServletException, IOException {
     doPost(p0, p1);
   }
 
   protected void doPost(
     HttpServletRequest request,
     HttpServletResponse response) throws ServletException, IOException {
     
     //unlock all forms locked by this session
     HttpSession session = request.getSession();
     getApplicationServiceLocator(session.getServletContext()).findLockingService().unlockFormByUser(request.getRemoteUser());
      
     synchronized (SessionUtils.sessionObjectCache) {
       log.error("LogoutServlet.doPost at start:"+TimeUtils.getEasternTime());
       String error = request.getParameter("authorizationError");
       String forwardUrl;
       if (error == null ) {
         forwardUrl = "/" + LOGOUT_JSP;
       }
       else {
         forwardUrl = "/" + AUTHORIZATION_ERROR_JSP;
       }
 
       if ((session != null) && isLoggedIn(request)) {
         for (int i = 0; i < logoutKeys.length; i++) {
           session.removeAttribute(logoutKeys[i]);
         }
 
         //remove formbuilder specific objects
         //TODO has to be moved to an action
         Collection keys =
           (Collection) session.getAttribute(
             FormBuilderConstants.CLEAR_SESSION_KEYS);
         if (keys != null) {
           Iterator it = keys.iterator();
           while (it.hasNext()) {
             session.removeAttribute((String) it.next());
           }
         }
         HashMap allMap = new HashMap();
         allMap.put(
           CaDSRConstants.GLOBAL_SESSION_KEYS, copyAllsessionKeys(session));
         allMap.put(
           CaDSRConstants.GLOBAL_SESSION_MAP, copyAllsessionObjects(session));
         SessionUtils.addToSessionCache(session.getId(), allMap);
         forwardUrl =
           forwardUrl + "?" + CaDSRConstants.PREVIOUS_SESSION_ID + "=" +
           session.getId();
         session.invalidate();
       }
 
       RequestDispatcher dispacher = request.getRequestDispatcher(forwardUrl);
       dispacher.forward(request, response);
       log.error("LogoutServlet.doPost at end:"+TimeUtils.getEasternTime());
     }
   }
 
   private Map copyAllsessionObjects(HttpSession session) {
     log.error("LogoutServlet.copyAllsessionObjects start:"+TimeUtils.getEasternTime());
     HashMap map = new HashMap();
     Enumeration keys = session.getAttributeNames();
     for (; keys.hasMoreElements();) {
       String key = (String) keys.nextElement();
       map.put(key, session.getAttribute(key));
     }
     log.error("LogoutServlet.copyAllsessionObjects end:"+TimeUtils.getEasternTime());
     return map;
   }
 
   private Set copyAllsessionKeys(HttpSession session) {
     log.error("LogoutServlet.copyAllsessionKeys end:"+TimeUtils.getEasternTime());
     HashSet set = new HashSet();
     Enumeration keys = session.getAttributeNames();
     for (; keys.hasMoreElements();) {
       String key = (String) keys.nextElement();
       set.add(key);
     }
     log.error("LogoutServlet.copyAllsessionKeys start:"+TimeUtils.getEasternTime());
     return set;
   }
 
   private boolean isLoggedIn(HttpServletRequest request) {
     String user = request.getRemoteUser();
     if (user == null) {
       return false;
     }
     if ("".equals(user)) {
       return false;
     }
 
     return true;
   }
 
 
     protected ApplicationServiceLocator getApplicationServiceLocator(ServletContext sc)
       throws ServiceLocatorException {
       ApplicationServiceLocator appServiceLocator =
         (ApplicationServiceLocator) sc.getAttribute(
           ApplicationServiceLocator.APPLICATION_SERVICE_LOCATOR_CLASS_KEY);
       if(appServiceLocator==null)
         throw new ServiceLocatorException("Could no find ApplicationServiceLocator with key ="+ ApplicationServiceLocator.APPLICATION_SERVICE_LOCATOR_CLASS_KEY);
       return appServiceLocator;
     } 
         
 }
