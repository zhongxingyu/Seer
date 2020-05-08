 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.services.core.user.impl;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletUserImpl;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portlet.service.PortletServiceNotFoundException;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.services.core.security.auth.AuthorizationException;
 import org.gridlab.gridsphere.services.core.security.auth.AuthenticationException;
 import org.gridlab.gridsphere.services.core.security.auth.modules.LoginAuthModule;
 import org.gridlab.gridsphere.services.core.security.auth.modules.impl.descriptor.AuthModulesDescriptor;
 import org.gridlab.gridsphere.services.core.security.auth.modules.impl.descriptor.AuthModuleCollection;
 import org.gridlab.gridsphere.services.core.security.auth.modules.impl.descriptor.AuthModuleDefinition;
 import org.gridlab.gridsphere.services.core.user.LoginService;
 import org.gridlab.gridsphere.services.core.user.LoginUserModule;
 import org.gridlab.gridsphere.services.core.user.UserSessionManager;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 
 import java.lang.reflect.Constructor;
 import java.util.*;
 import java.security.cert.X509Certificate;
 
 /**
  * The <code>LoginService</code> is the primary interface that defines the login method used to obtain a
  * <code>User</code> from a username and password. The <code>LoginService</code> is configured
  * dynamically at run-time with login authorization modules. By default the PASSWORD_AUTH_MODULE is
  * selected which uses the GridSphere database to store passwords. Other authorization modules
  * can use external directory servers such as LDAP, etc
  */
 public class LoginServiceImpl implements LoginService, PortletServiceProvider {
 
     private UserSessionManager userSessionManager = UserSessionManager.getInstance();
     private PortletLog log = SportletLog.getInstance(LoginServiceImpl.class);
     private UserManagerService userManagerService = null;
     private static boolean inited = false;
     private List authModules = new ArrayList();
     private static LoginUserModule activeLoginModule = null;
 
     private PersistenceManagerRdbms pm = null;
 
     private String authMappingPath = GridSphereConfig.getServletContext().getRealPath("/WEB-INF/mapping/auth-modules-mapping.xml");
     private String authModulesPath = GridSphereConfig.getServletContext().getRealPath("/WEB-INF/authmodules.xml");
 
     public LoginServiceImpl() {
     }
 
     public List getAuthModules() {
         return authModules;
     }
 
     public List getActiveAuthModules() {
         List activeMods = new ArrayList();
         Iterator it = authModules.iterator();
         while (it.hasNext()) {
             LoginAuthModule authModule = (LoginAuthModule)it.next();
             if (authModule.isModuleActive()) activeMods.add(authModule);
         }
         return activeMods;
     }
 
     public List getSupportedAuthModules() {
         return authModules;
     }
 
     public LoginUserModule getActiveLoginModule() {
         return activeLoginModule;
     }
 
     public void setActiveLoginModule(LoginUserModule loginModule) {
         activeLoginModule = loginModule;
     }
 
     public List getActiveUserIds() {
         return userSessionManager.getUserIds();
     }
 
     /**
      * Initializes the portlet service.
      * The init method is invoked by the portlet container immediately after a portlet service has
      * been instantiated and before it is passed to the requestor.
      *
      * @param config the service configuration
      * @throws PortletServiceUnavailableException
      *          if an error occurs during initialization
      */
     public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
         log.debug("in login service init");
         if (!inited) {
             pm = PersistenceManagerFactory.createGridSphereRdbms();
             String loginClassName = config.getInitParameter("LOGIN_MODULE");
             try {
                 PortletServiceFactory factory = SportletServiceFactory.getInstance();
                 Class loginModClass = Class.forName(loginClassName);
                 activeLoginModule = (LoginUserModule) factory.createPortletService(loginModClass, true);
             } catch (ClassNotFoundException e) {
                 log.error("Unable to create class from class name: " + loginClassName, e);
             } catch (PortletServiceNotFoundException e) {
                 log.error("Unable to get service from portlet service factory: " + loginClassName, e);
             }
             log.debug("Created a login module service: " + loginClassName);
 
             loadAuthModules(authModulesPath, Thread.currentThread().getContextClassLoader());
 
             SportletServiceFactory factory = SportletServiceFactory.getInstance();
             try {
                 userManagerService = (UserManagerService)factory.createPortletService(UserManagerService.class, true);
             } catch (PortletServiceException e) {
                 log.error("Unable to create a user manager service", e);
             }
             inited = true;
         }
     }
 
     public void loadAuthModules(String authModsPath, ClassLoader classloader) {
 
         AuthModulesDescriptor desc;
         try {
             desc = new AuthModulesDescriptor(authModsPath, authMappingPath);
 
             AuthModuleCollection coll = desc.getCollection();
             List modList = coll.getAuthModulesList();
             Iterator it = modList.iterator();
             log.info("loading auth modules:");
             while (it.hasNext()) {
                 AuthModuleDefinition def = (AuthModuleDefinition)it.next();
                 log.info(def.toString());
                 String modClassName = def.getModuleImplementation();
 
                 // before initializing check if we know about this mod in the db
                 AuthModuleDefinition am = getAuthModuleDefinition(def.getModuleName());
                 if (am != null) {
                     def.setModulePriority(am.getModulePriority());
                     def.setModuleActive(am.getModuleActive());
                 } else {
                     pm.saveOrUpdate(def);
                 }
                 Class c = Class.forName(modClassName, true, classloader);
                 Class[] parameterTypes = new Class[]{AuthModuleDefinition.class};
                 Object[] obj = new Object[]{def};
                 Constructor con = c.getConstructor(parameterTypes);
                 LoginAuthModule authModule = (LoginAuthModule) con.newInstance(obj);
                 authModules.add(authModule);
             }
         } catch (Exception e) {
             log.error("Error loading auth module!", e);
         }
     }
 
     public void saveAuthModule(LoginAuthModule authModule) {
         try {
             log.debug("saving auth module: " + authModule.getModuleName() + " " +
                     authModule.getModulePriority() + " " + authModule.isModuleActive());
             AuthModuleDefinition am = getAuthModuleDefinition(authModule.getModuleName());
             if (am != null) {
                 am.setModulePriority(authModule.getModulePriority());
                 am.setModuleActive(authModule.isModuleActive());
                 pm.update(am);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private AuthModuleDefinition getAuthModuleDefinition(String moduleName) {
         AuthModuleDefinition am = null;
         try {
             am = (AuthModuleDefinition)pm.restore("select authmodule from " + AuthModuleDefinition.class.getName() +
                 " authmodule where authmodule.ModuleName='" +
                 moduleName + "'");
         } catch (PersistenceManagerException e) {
             e.printStackTrace();
         }
         return am;
     }
 
     public List getAuthModuleDefinitions() {
         List mods = null;
         try {
             mods = pm.restoreList("select authmod from "
                 + AuthModuleDefinition.class.getName()
                 + " authmod ");
         } catch (PersistenceManagerException e) {
             e.printStackTrace();
         }
         return mods;
     }
 
 
     /**
      * The destroy method is invoked by the portlet container to destroy a portlet service.
      * This method must free all resources allocated to the portlet service.
      */
     public void destroy() {
     }
 
     public User login(PortletRequest req)
             throws AuthenticationException, AuthorizationException {
         String loginName = req.getParameter("username");
         String loginPassword = req.getParameter("password");
         String certificate = null;
 
         X509Certificate[] certs = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
         if (certs != null && certs.length > 0) {
             certificate = certificateTransform(certs[0].getSubjectDN().toString());
         }
 
         User user = null;
 
         // if using client certificate, then don't use login modules
         if (certificate == null) {
             if ((loginName == null) || (loginPassword == null)) {
                 throw new AuthorizationException(getLocalizedText(req, "LOGIN_AUTH_BLANK"));
             }
             // first get user
             user = activeLoginModule.getLoggedInUser(loginName);
         } else {
 
             log.debug("Using certificate for login :" + certificate);
             List userList = userManagerService.getUsersByAttribute("certificate", certificate);
             if (!userList.isEmpty()) {
                return (User)userList.get(0);
             }
         }
 
         if (user == null) throw new AuthorizationException(getLocalizedText(req, "LOGIN_AUTH_NOUSER"));
 
         String accountStatus = (String)user.getAttribute(User.DISABLED);
         if ((accountStatus != null) && ("TRUE".equalsIgnoreCase(accountStatus)))
             throw new AuthorizationException(getLocalizedText(req, "LOGIN_AUTH_DISABLED"));
 
         // second invoke the appropriate auth module
         List modules = this.getActiveAuthModules();
 
         Collections.sort(modules);
         AuthenticationException authEx = null;
 
         Iterator it = modules.iterator();
         log.debug("in login: Active modules are: ");
         boolean success = false;
         while (it.hasNext()) {
             success = false;
             LoginAuthModule mod = (LoginAuthModule) it.next();
             log.debug(mod.getModuleName());
             try {
                 mod.checkAuthentication(user, loginPassword);
                 success = true;
             } catch (AuthenticationException e) {
                 String errMsg = mod.getModuleError(e.getMessage(), req.getLocale());
                 if (errMsg != null) {
                     authEx = new AuthenticationException(errMsg);
                 } else {
                     authEx = e;
                 }
             }
             if (success) break;
         }
         if (!success) throw authEx;
 
         return user;
     }
 
     /**
      *  Transform certificate subject from :
      *  CN=Engbert Heupers, O=sara, O=users, O=dutchgrid
      *  to :
      *  /O=dutchgrid/O=users/O=sara/CN=Engbert Heupers
      * @param certificate string
      * @return certificate string
      */
     private String certificateTransform(String certificate) {
         String ls[] = certificate.split(", ");
         StringBuffer res = new StringBuffer();
         for(int i = ls.length - 1; i >= 0; i--) {
             res.append("/");
             res.append(ls[i]);
         }
         return res.toString();
     }
 
     protected String getLocalizedText(PortletRequest req, String key) {
         Locale locale = req.getLocale();
         ResourceBundle bundle = ResourceBundle.getBundle("gridsphere.resources.Portlet", locale);
         return bundle.getString(key);
     }
 
 }
