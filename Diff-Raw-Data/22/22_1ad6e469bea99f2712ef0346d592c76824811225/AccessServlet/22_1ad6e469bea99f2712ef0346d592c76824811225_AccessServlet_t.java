 package org.makumba.parade.access;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.auth.Authorizer;
 import org.makumba.parade.auth.DatabaseAuthorizer;
 import org.makumba.parade.auth.DirectoryAuthorizer;
 import org.makumba.parade.auth.DirectoryAuthorizerException;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.init.ParadeProperties;
 import org.makumba.parade.model.User;
 import org.makumba.parade.tools.HttpLogin;
 import org.makumba.parade.tools.ParadeLogger;
 import org.makumba.parade.tools.TriggerFilter;
 
 /**
  * The servlet called at the begining of each parade access, in all servlet contexts. It performs login and passes the
  * username to an {@link ActionLogDTO} that comes from the {@link TriggerFilter}.
  * 
  * TODO: implement an equivalent of the previous Config.reloadLoggingConfig()
  * TODO: refactoring: there's some useless code in here, from the time in which ParaDe used Makumba
  * 
  * @author Cristian Bogdan
  * @author Manuel Gay
  * 
  */
 public class AccessServlet extends HttpServlet {
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     private static final String PARADE_USER = "org.makumba.parade.user";
 
     public static final String PARADE_LDAP_USER = "org.makumba.parade.ldapUser";
 
     ServletContext context;
 
     static Logger logger = ParadeLogger.getParadeLogger(AccessServlet.class.getName());
 
     HttpLogin checker;
 
     boolean isMakumbaContext;
 
     @Override
     public void init() {
         context = getServletConfig().getServletContext();
 
         String authClass = ParadeProperties.getParadeProperty("parade.authorizerClass");
         String authMessage = ParadeProperties.getParadeProperty("parade.authorizationMessage");
         if (authClass == null)
             return;
         String db = ParadeProperties.getParadeProperty("parade.authorizationDB");
         Authorizer auth = null;
         try {
             auth = (Authorizer) getClass().getClassLoader().loadClass(authClass).newInstance();
             if (db != null && (auth instanceof DatabaseAuthorizer))
                 ((DatabaseAuthorizer) auth).setDatabase(db);
             checker = new HttpLogin(auth, authMessage) {
                 @Override
                 public boolean login(ServletRequest req, ServletResponse res) throws Exception {
                     HttpServletRequest req1 = (HttpServletRequest) req;
                     String user = (String) req1.getSession(true).getAttribute(PARADE_USER);
                     return user != null || super.login(req, res);
                 }
 
                 @Override
                 protected boolean checkAuth(String user, String pass, HttpServletRequest req) throws Exception {
                     boolean passes = super.checkAuth(user, pass, req);
                     if (passes) {
                         req.getSession(true).setAttribute(PARADE_USER, user);
 
                         // let's check if we know this user
                         Session s = null;
                         Transaction tx = null;
                         try {
                             s = InitServlet.getSessionFactory().openSession();
                             tx = s.beginTransaction();
 
                             Query q;
                             q = s.createQuery("from User u where u.login = ?");
                             q.setString(0, user);
 
                             List<User> results = q.list();
                             User u = null;
 
                             if (results.size() > 1) {
                                 logger.severe("Multiple possibilities for user " + user
                                         + ". Please contact developers.");
                             } else if (results.size() == 1) {
                                 // we know the guy, let's put more stuff in the session
                                 u = results.get(0);
                                 setUserAttributes(req, u);
                             } else if (results.size() == 0) {
                                 // maybe we can get the guy from LDAP
                                 if (a instanceof DirectoryAuthorizer) {
                                     DirectoryAuthorizer auth = (DirectoryAuthorizer) a;
                                     u = new User(user, auth.getGivenName(), auth.getSn(), auth.getCn(), auth.getMail());
                                     u.setJpegPhoto(auth.getJpegPhoto());
                                     s.save(u);
 
                                     setUserAttributes(req, u);
                                 }
                             }
 
                         } finally {
                             tx.commit();
                             s.close();
                         }
 
                     }
                     return passes;
                 }
             };
         } catch (Throwable t) {
             throw new RuntimeException("Error initializing the login authorizer: " + t);
         }
     }
 
     @Override
     public void destroy() {
     }
 
     boolean shouldLogin(ServletRequest req) {
         if (checker == null)
             return false;
         if (((HttpServletRequest) req).getContextPath().equals("/manager")) {
             ((HttpServletRequest) req).getSession(true).setAttribute(PARADE_USER, "tomcat-manager");
             return false;
         }
         if (((HttpServletRequest) req).getRequestURI().startsWith("/servlet/cvscommit")) {
             ((HttpServletRequest) req).getSession(true).setAttribute(PARADE_USER, "cvs-hook");
             return false;
         }
         if (((HttpServletRequest) req).getRequestURI().startsWith("/unauthorized")) {
             return false;
         }
 
         return true;
     }
 
     HttpServletRequest checkLogin(ServletRequest req, ServletResponse resp) throws Exception {
 
         if (checker.login(req, (HttpServletResponse) req.getAttribute("org.eu.best.tools.TriggerFilter.response"))) {
             return new HttpServletRequestWrapper((HttpServletRequest) req) {
                 @Override
                 public String getRemoteUser() {
                     String user = (String) ((HttpServletRequest) getRequest()).getSession(true).getAttribute(
                             PARADE_USER);
                     return user;
                 }
             };
         }
 
         return null;
     }
 
     String setOutputPrefix(HttpServletRequest req, HttpServletResponse resp) {
         String contextPath = req.getContextPath();
         if (contextPath.equals("")) {
             contextPath = TriggerFilter.actionLog.get().getContext();
         } else
             contextPath = contextPath.substring(1);
         String nm = (String) req.getSession(true).getAttribute(PARADE_USER);
         if (nm == null)
             nm = "(unknown user)";
 
         ServletContext ctx = (ServletContext) req.getAttribute("org.eu.best.tools.TriggerFilter.context");
 
         try {
             if (ctx.getResource("/WEB-INF/lib/makumba.jar") != null) {
                 HttpServletRequest dummyRequest = (HttpServletRequest) req
                         .getAttribute("org.eu.best.tools.TriggerFilter.dummyRequest");
                 dummyRequest.setAttribute("org.makumba.systemServlet.command", "setLoggingRoot");
                 dummyRequest.setAttribute("org.makumba.systemServlet.param1", "org.makumba.parade-context."
                         + contextPath);
                 ctx.getRequestDispatcher("/servlet/org.makumba.devel.SystemServlet").include(dummyRequest, resp);
             }
         } catch (Throwable t) {
             t.printStackTrace();
         }
 
         return nm;
     }
 
     @Override
     public void service(ServletRequest req, ServletResponse resp) throws java.io.IOException, ServletException {
 
         HttpServletRequest origReq = (HttpServletRequest) req;
         req = (HttpServletRequest) req.getAttribute("org.eu.best.tools.TriggerFilter.request");
 
         // Config.reloadLoggingConfig();
         // TODO implement equivalent of reloadLoggingConfig()
         setOutputPrefix((HttpServletRequest) req, (HttpServletResponse) resp);
         ServletRequest req1 = req;
         try {
           if (!shouldLogin(req) || (req1 = checkLogin(req, resp)) != null) {
               // we set the output prefix again, now that we know the user
               String user = setOutputPrefix((HttpServletRequest) req, (HttpServletResponse) resp);
 
               // we also set the userObject again and all the necessary attributes
               User u = (User) ((HttpServletRequest) req).getSession(true).getAttribute("org.makumba.parade.userObject");
               if (u == null) {
                   Session sess = null;
                   Transaction tx = null;
                   try {
                       sess = InitServlet.getSessionFactory().openSession();
                       tx = sess.beginTransaction();
 
                       Query q;
                       q = sess.createQuery("from User u where u.login = ?");
                       q.setString(0, user);
 
                       List<User> results = q.list();
 
                       if (results.size() == 1) {
                           // we know the guy, let's put more stuff in the session
                           u = results.get(0);
                           setUserAttributes(req, u);
                       }
                       
                   } finally {
                      if(tx != null && sess != null) {
                          tx.commit();
                          sess.close();
                      }
                   }
               }
 
               // let's also put the user in the actionlog
               ActionLogDTO log = (ActionLogDTO) req.getAttribute("org.eu.best.tools.TriggerFilter.actionlog");
               log.setUser(user);
               origReq.setAttribute("org.eu.best.tools.TriggerFilter.request", req1);
 
           } else {
               origReq.removeAttribute("org.eu.best.tools.TriggerFilter.request");
               origReq.setAttribute("org.makumba.parade.unauthorizedAccess", new Boolean(true));
           }
         } catch(Throwable t) {
               if(t instanceof DirectoryAuthorizerException) {
                 origReq.removeAttribute("org.eu.best.tools.TriggerFilter.request");
                 origReq.setAttribute("org.makumba.parade.directoryAccessError", new Boolean(true));
               } else {
                   logger.warning("Exception happened during authentication: " + t.getMessage());
                  t.printStackTrace();
                   origReq.removeAttribute("org.eu.best.tools.TriggerFilter.request");
                  origReq.setAttribute("org.makumba.parade.authenticationError", t);
               }
            }
     }
 
     private void setUserAttributes(ServletRequest req, User u) {
         ((HttpServletRequest) req).getSession().setAttribute("org.makumba.parade.userObject", u);
         ((HttpServletRequest) req).getSession().setAttribute("user_login", u.getLogin());
         ((HttpServletRequest) req).getSession().setAttribute("user_name", u.getName());
         ((HttpServletRequest) req).getSession().setAttribute("user_surname", u.getSurname());
         ((HttpServletRequest) req).getSession().setAttribute("user_nickname", u.getNickname());
         ((HttpServletRequest) req).getSession().setAttribute("user_email", u.getEmail());
     }
 
 }
