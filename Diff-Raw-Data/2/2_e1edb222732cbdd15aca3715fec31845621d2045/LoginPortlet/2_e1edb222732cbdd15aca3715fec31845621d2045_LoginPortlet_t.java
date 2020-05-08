 /*
  * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
  * @version $Id: LoginPortlet.java 5087 2006-08-18 22:52:23Z novotny $
  */
 package org.gridsphere.portlets.core.login;
 
 import org.gridsphere.layout.PortletPageFactory;
 import org.gridsphere.portlet.impl.PortletURLImpl;
 import org.gridsphere.portlet.impl.SportletProperties;
 import org.gridsphere.portlet.service.PortletServiceException;
 import org.gridsphere.provider.event.jsr.ActionFormEvent;
 import org.gridsphere.provider.event.jsr.RenderFormEvent;
 import org.gridsphere.provider.portlet.jsr.ActionPortlet;
 import org.gridsphere.provider.portletui.beans.HiddenFieldBean;
 import org.gridsphere.provider.portletui.beans.MessageBoxBean;
 import org.gridsphere.provider.portletui.beans.PasswordBean;
 import org.gridsphere.provider.portletui.beans.TextFieldBean;
 import org.gridsphere.services.core.filter.PortalFilter;
 import org.gridsphere.services.core.filter.PortalFilterService;
 import org.gridsphere.services.core.mail.MailMessage;
 import org.gridsphere.services.core.mail.MailService;
 import org.gridsphere.services.core.portal.PortalConfigService;
 import org.gridsphere.services.core.request.Request;
 import org.gridsphere.services.core.request.RequestService;
 import org.gridsphere.services.core.security.auth.AuthModuleService;
 import org.gridsphere.services.core.security.auth.AuthenticationException;
 import org.gridsphere.services.core.security.auth.AuthorizationException;
 import org.gridsphere.services.core.security.auth.modules.LoginAuthModule;
 import org.gridsphere.services.core.security.password.PasswordEditor;
 import org.gridsphere.services.core.security.password.PasswordManagerService;
 import org.gridsphere.services.core.security.role.PortletRole;
 import org.gridsphere.services.core.security.role.RoleManagerService;
 import org.gridsphere.services.core.user.User;
 import org.gridsphere.services.core.user.UserManagerService;
 
 import javax.portlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.security.cert.X509Certificate;
 import java.util.*;
 
 public class LoginPortlet extends ActionPortlet {
 
     private static String FORGOT_PASSWORD_LABEL = "forgotpassword";
     private static String ACTIVATE_ACCOUNT_LABEL = "activateaccount";
 
     private static long REQUEST_LIFETIME = 1000 * 60 * 60 * 24 * 3; // 3 days
 
     public static final String LOGIN_ERROR_FLAG = "LOGIN_FAILED";
     public static final Integer LOGIN_ERROR_UNKNOWN = new Integer(-1);
 
     public static final String DO_VIEW_USER_EDIT_LOGIN = "login/createaccount.jsp"; //edit user
     public static final String DO_FORGOT_PASSWORD = "login/forgotpassword.jsp";
     public static final String DO_NEW_PASSWORD = "login/newpassword.jsp";
 
     private UserManagerService userManagerService = null;
     private RoleManagerService roleService = null;
     private PasswordManagerService passwordManagerService = null;
     private PortalConfigService portalConfigService = null;
     private RequestService requestService = null;
     private MailService mailService = null;
     private AuthModuleService authModuleService = null;
 
     private PortalFilterService portalFilterService = null;
 
     private String notificationURL = null;
     private String newpasswordURL = null;
     private String activateAccountURL = null;
     private String denyAccountURL = null;
     private String redirectURL = null;
 
     public void init(PortletConfig config) throws PortletException {
 
         super.init(config);
 
         userManagerService = (UserManagerService) createPortletService(UserManagerService.class);
         roleService = (RoleManagerService) createPortletService(RoleManagerService.class);
         passwordManagerService = (PasswordManagerService) createPortletService(PasswordManagerService.class);
         requestService = (RequestService) createPortletService(RequestService.class);
         mailService = (MailService) createPortletService(MailService.class);
         portalConfigService = (PortalConfigService) createPortletService(PortalConfigService.class);
         portalFilterService = (PortalFilterService) createPortletService(PortalFilterService.class);
         authModuleService = (AuthModuleService) createPortletService(AuthModuleService.class);
 
         DEFAULT_VIEW_PAGE = "doViewUser";
     }
 
     public void doViewUser(RenderFormEvent event) throws PortletException {
         log.debug("in LoginPortlet: doViewUser");
         PortletRequest request = event.getRenderRequest();
         RenderResponse response = event.getRenderResponse();
         if (notificationURL == null) notificationURL = response.createActionURL().toString();
 
         if (newpasswordURL == null) {
             PortletURL url = response.createActionURL();
             ((PortletURLImpl) url).setAction("newpassword");
             ((PortletURLImpl) url).setLayout("register");
             ((PortletURLImpl) url).setEncoding(false);
             newpasswordURL = url.toString();
         }
 
         if (redirectURL == null) {
             PortletURL url = response.createRenderURL();
             ((PortletURLImpl) url).setLayout(PortletPageFactory.USER_PAGE);
             ((PortletURLImpl) url).setEncoding(false);
             redirectURL = url.toString();
         }
 
         if (activateAccountURL == null) {
             PortletURL url = response.createActionURL();
             ((PortletURLImpl) url).setAction("approveAccount");
             ((PortletURLImpl) url).setLayout("register");
             ((PortletURLImpl) url).setEncoding(false);
             activateAccountURL = url.toString();
         }
         if (denyAccountURL == null) {
             PortletURL url = response.createActionURL();
             ((PortletURLImpl) url).setAction("denyAccount");
             ((PortletURLImpl) url).setLayout("register");
             ((PortletURLImpl) url).setEncoding(false);
             denyAccountURL = url.toString();
         }
         PasswordBean pass = event.getPasswordBean("password");
         pass.setValue("");
 
         // Check certificates
         String x509supported = portalConfigService.getProperty(PortalConfigService.SUPPORT_X509_AUTH);
         if ((x509supported != null) && (x509supported.equalsIgnoreCase("true"))) {
             X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
             if (certs != null && certs.length > 0) {
                 request.setAttribute("certificate", certs[0].getSubjectDN().toString());
             }
         }
 
         String remUser = portalConfigService.getProperty(PortalConfigService.REMEMBER_USER);
         if ((remUser != null) && (remUser.equalsIgnoreCase("TRUE"))) {
             request.setAttribute("remUser", "true");
         }
 
         Boolean useSecureLogin = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.USE_HTTPS_LOGIN));
 
         request.setAttribute("useSecureLogin", useSecureLogin.toString());
         boolean canUserCreateAccount = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.CAN_USER_CREATE_ACCOUNT)).booleanValue();
         if (canUserCreateAccount) request.setAttribute("canUserCreateAcct", "true");
         boolean dispUser = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.SEND_USER_FORGET_PASSWORD)).booleanValue();
         if (dispUser) request.setAttribute("dispPass", "true");
 
         String errorMsg = (String) request.getPortletSession(true).getAttribute(LOGIN_ERROR_FLAG);
 
         if (errorMsg != null) {
             createErrorMessage(event, errorMsg);
             request.getPortletSession(true).removeAttribute(LOGIN_ERROR_FLAG);
         }
 
         Boolean useUserName = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.USE_USERNAME_FOR_LOGIN));
         if (useUserName) request.setAttribute("useUserName", "true");
 
         setNextState(request, "login/login.jsp");
     }
 
     public void doCancel(ActionFormEvent event) throws PortletException {
         setNextState(event.getActionRequest(), DEFAULT_VIEW_PAGE);
     }
 
     public void gs_login(ActionFormEvent event) throws PortletException {
         log.debug("in LoginPortlet: gs_login");
         PortletRequest req = event.getActionRequest();
 
         try {
             login(event);
         } catch (AuthorizationException err) {
             log.debug(err.getMessage());
             req.getPortletSession(true).setAttribute(LOGIN_ERROR_FLAG, err.getMessage());
         } catch (AuthenticationException err) {
             log.debug(err.getMessage());
             req.getPortletSession(true).setAttribute(LOGIN_ERROR_FLAG, err.getMessage());
         }
 
         setNextState(req, DEFAULT_VIEW_PAGE);
     }
 
 
     /**
      * Handles login requests
      *
      * @param event a <code>GridSphereEvent</code>
      * @throws org.gridsphere.services.core.security.auth.AuthenticationException
      *          if auth fails
      * @throws org.gridsphere.services.core.security.auth.AuthorizationException
      *          if authz fails
      */
     protected void login(ActionFormEvent event) throws AuthenticationException, AuthorizationException {
 
         ActionRequest req = event.getActionRequest();
         ActionResponse res = event.getActionResponse();
 
         User user = login(req);
         Long now = Calendar.getInstance().getTime().getTime();
         user.setLastLoginTime(now);
         Integer numLogins = user.getNumLogins();
         if (numLogins == null) numLogins = 0;
         numLogins++;
 
         user.setNumLogins(numLogins);
         user.setAttribute(PortalConfigService.LOGIN_NUMTRIES, "0");
 
         userManagerService.saveUser(user);
 
         req.setAttribute(SportletProperties.PORTLET_USER, user);
         req.getPortletSession(true).setAttribute(SportletProperties.PORTLET_USER, user.getID(), PortletSession.APPLICATION_SCOPE);
 
         String query = event.getAction().getParameter("queryString");
 
         if (query != null) {
             //redirectURL.setParameter("cid", query);
         }
         //req.setAttribute(SportletProperties.LAYOUT_PAGE, PortletPageFactory.USER_PAGE);
 
 
         String realuri = redirectURL.toString().substring("http".length());
         Boolean useSecureRedirect = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.USE_HTTPS_REDIRECT));
         if (useSecureRedirect.booleanValue()) {
             realuri = "https" + realuri;
         } else {
             realuri = "http" + realuri;
         }
 
         List<PortalFilter> portalFilters = portalFilterService.getPortalFilters();
         for (PortalFilter filter : portalFilters) {
             filter.doAfterLogin((HttpServletRequest) req, (HttpServletResponse) res);
         }
 
         log.debug("in login redirecting to portal: " + realuri.toString());
         try {
             if (req.getParameter("ajax") != null) {
                 //res.setContentType("text/html");
                 //res.getWriter().print(realuri.toString());
             } else {
                 res.sendRedirect(realuri.toString());
             }
         } catch (IOException e) {
             log.error("Unable to perform a redirect!", e);
         }
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
             Boolean useUserName = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.USE_USERNAME_FOR_LOGIN));
             if (useUserName) {
 
                 user = userManagerService.getUserByUserName(loginName);
             } else {
                 user = userManagerService.getUserByEmail(loginName);
             }
 
         } else {
 
             log.debug("Using certificate for login :" + certificate);
             List userList = userManagerService.getUsersByAttribute("certificate", certificate, null);
             if (!userList.isEmpty()) {
                 user = (User) userList.get(0);
             }
         }
 
         if (user == null) throw new AuthorizationException(getLocalizedText(req, "LOGIN_AUTH_NOUSER"));
 
         // tried one to many times using same name
         int defaultNumTries = Integer.valueOf(portalConfigService.getProperty(PortalConfigService.LOGIN_NUMTRIES)).intValue();
         int numTriesInt;
         String numTries = (String) user.getAttribute(PortalConfigService.LOGIN_NUMTRIES);
         if (numTries == null) {
             numTriesInt = 1;
         } else {
             numTriesInt = Integer.valueOf(numTries).intValue();
         }
         System.err.println("num tries = " + numTriesInt);
         if ((defaultNumTries != -1) && (numTriesInt >= defaultNumTries)) {
             disableAccount(req);
             throw new AuthorizationException(getLocalizedText(req, "LOGIN_TOOMANY_ATTEMPTS"));
         }
 
         String accountStatus = (String) user.getAttribute(User.DISABLED);
         if ((accountStatus != null) && ("TRUE".equalsIgnoreCase(accountStatus)))
             throw new AuthorizationException(getLocalizedText(req, "LOGIN_AUTH_DISABLED"));
 
         // If authorized via certificates no other authorization needed
         if (certificate != null) return user;
 
         // second invoke the appropriate auth module
         List<LoginAuthModule> modules = authModuleService.getActiveAuthModules();
 
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
         if (!success) {
 
             numTriesInt++;
             user.setAttribute(PortalConfigService.LOGIN_NUMTRIES, String.valueOf(numTriesInt));
             userManagerService.saveUser(user);
 
             throw authEx;
         }
 
         return user;
     }
 
     /**
      * Transform certificate subject from :
      * CN=Engbert Heupers, O=sara, O=users, O=dutchgrid
      * to :
      * /O=dutchgrid/O=users/O=sara/CN=Engbert Heupers
      *
      * @param certificate string
      * @return certificate string
      */
     private String certificateTransform(String certificate) {
         String ls[] = certificate.split(", ");
         StringBuffer res = new StringBuffer();
         for (int i = ls.length - 1; i >= 0; i--) {
             res.append("/");
             res.append(ls[i]);
         }
         return res.toString();
     }
 
     protected String getLocalizedText(HttpServletRequest req, String key) {
         Locale locale = req.getLocale();
         ResourceBundle bundle = ResourceBundle.getBundle("gridsphere.resources.Portlet", locale);
         return bundle.getString(key);
     }
 
     public void disableAccount(PortletRequest req) {
         //PortletRequest req = event.getRenderRequest();
         String loginName = req.getParameter("username");
         User user = userManagerService.getUserByUserName(loginName);
         if (user != null) {
             user.setAttribute(User.DISABLED, "true");
             userManagerService.saveUser(user);
 
             MailMessage mailToUser = new MailMessage();
             StringBuffer body = new StringBuffer();
             body.append(getLocalizedText(req, "LOGIN_DISABLED_MSG1")).append(" ").append(getLocalizedText(req, "LOGIN_DISABLED_MSG2")).append("\n\n");
             mailToUser.setBody(body.toString());
             mailToUser.setSubject(getLocalizedText(req, "LOGIN_DISABLED_SUBJECT"));
             mailToUser.setEmailAddress(user.getEmailAddress());
 
             MailMessage mailToAdmin = new MailMessage();
             StringBuffer body2 = new StringBuffer();
             body2.append(getLocalizedText(req, "LOGIN_DISABLED_ADMIN_MSG")).append(" ").append(user.getUserName());
             mailToAdmin.setBody(body2.toString());
             mailToAdmin.setSubject(getLocalizedText(req, "LOGIN_DISABLED_SUBJECT") + " " + user.getUserName());
             String portalAdminEmail = portalConfigService.getProperty(PortalConfigService.PORTAL_ADMIN_EMAIL);
             mailToAdmin.setEmailAddress(portalAdminEmail);
 
             try {
                 mailService.sendMail(mailToUser);
                 mailService.sendMail(mailToAdmin);
             } catch (PortletServiceException e) {
                 log.error("Unable to send mail message!", e);
                 //createErrorMessage(event, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
             }
         }
     }
 
     public void doNewUser(RenderFormEvent evt)
             throws PortletException {
 
         boolean canUserCreateAccount = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.CAN_USER_CREATE_ACCOUNT)).booleanValue();
         if (!canUserCreateAccount) return;
 
         RenderRequest req = evt.getRenderRequest();
         RenderResponse res = evt.getRenderResponse();
         MessageBoxBean msg = evt.getMessageBoxBean("msg");
 
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         String error = (String) req.getPortletSession(true).getAttribute("error");
         if (error != null) {
             msg.setValue(error);
             req.getPortletSession(true).removeAttribute("error");
         } else {
             String adminApproval = portalConfigService.getProperty("ADMIN_ACCOUNT_APPROVAL");
             if (adminApproval.equals(Boolean.TRUE.toString())) {
                 msg.setKey("LOGIN_ACCOUNT_CREATE_APPROVAL");
             } else {
                 msg.setKey("LOGIN_CREATE_ACCT");
             }
         }
 
         res.setTitle("Create Account");
         setNextState(req, DO_VIEW_USER_EDIT_LOGIN);
         log.debug("in doViewNewUser");
     }
 
     public void doConfirmEditUser(ActionFormEvent evt)
             throws PortletException {
         ActionRequest req = evt.getActionRequest();
         ActionResponse res = evt.getActionResponse();
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         boolean canUserCreateAccount = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.CAN_USER_CREATE_ACCOUNT)).booleanValue();
         if (!canUserCreateAccount) return;
 
         try {
             //check if the user is new or not
             validateUser(evt);
             //new and valid user and will save it
             notifyNewUser(evt);
 
             setNextState(req, DEFAULT_VIEW_PAGE);
         } catch (PortletException e) {
             //invalid user, an exception was thrown
             //back to edit
             log.error("Could not create account: ", e);
             req.getPortletSession(true).setAttribute("error", e.getMessage());
             setNextState(req, "doNewUser");
         }
     }
 
     private void validateUser(ActionFormEvent event)
             throws PortletException {
         log.debug("Entering validateUser()");
         PortletRequest req = event.getActionRequest();
 
         // Validate user name
         String userName = event.getTextFieldBean("userName").getValue();
         if (userName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NAME_BLANK") + "<br />");
             throw new PortletException("user name is blank!");
         }
 
         if (this.userManagerService.existsUserName(userName)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_EXISTS") + "<br />");
             throw new PortletException("user exists already");
         }
 
         // Validate full name
 
         String firstName = event.getTextFieldBean("firstName").getValue();
         if (firstName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_GIVENNAME_BLANK") + "<br />");
             throw new PortletException("first name is blank");
         }
 
         String lastName = event.getTextFieldBean("lastName").getValue();
         if (lastName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_FAMILYNAME_BLANK") + "<br />");
             throw new PortletException("last name is blank");
         }
 
         // Validate e-mail
         String eMail = event.getTextFieldBean("emailAddress").getValue();
         if (eMail.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             throw new PortletException("email is blank");
         } else if ((eMail.indexOf("@") < 0)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             throw new PortletException("email address invalid");
         } else if ((eMail.indexOf(".") < 0)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             throw new PortletException("email address invalid");
         }
 
         //Validate password
 
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             if (isInvalidPassword(event)) throw new PortletException("password no good!");
         }
 
         //retrieve the response
         String response = event.getTextFieldBean("captchaTF").getValue();
 
         String captchaValue = (String) req.getPortletSession(true).getAttribute(nl.captcha.servlet.Constants.SIMPLE_CAPCHA_SESSION_KEY, PortletSession.APPLICATION_SCOPE);
         if (!response.equals(captchaValue)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_CAPTCHA_MISMATCH"));
             throw new PortletException("captcha challenge mismatch!");
         }
 
         log.debug("Exiting validateUser()");
     }
 
     private boolean isInvalidPassword(ActionFormEvent event) {
         PortletRequest req = event.getActionRequest();
         // Validate password
         String passwordValue = event.getPasswordBean("password").getValue();
         String confirmPasswordValue = event.getPasswordBean("confirmPassword").getValue();
 
         if (passwordValue == null) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_NOTSET"));
             return true;
         }
 
         // Otherwise, password must match confirmation
         if (!passwordValue.equals(confirmPasswordValue)) {
             createErrorMessage(event, (this.getLocalizedText(req, "USER_PASSWORD_MISMATCH")) + "<br />");
             return true;
             // If they do match, then validate password with our service
         } else {
             passwordValue = passwordValue.trim();
 
             if (passwordValue.length() == 0) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_BLANK"));
                 return true;
             }
             if (passwordValue.length() < 5) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_TOOSHORT"));
                 return true;
             }
         }
         return false;
     }
 
     private User saveUser(Request request) {
         log.debug("Entering saveUser()");
         // Account request
 
         // Create edit account request
 
         User newuser = this.userManagerService.createUser();
 
         // Edit account attributes
         newuser.setUserName(request.getAttribute("userName"));
         newuser.setFirstName(request.getAttribute("firstName"));
         newuser.setLastName(request.getAttribute("lastName"));
         newuser.setFullName(request.getAttribute("lastName") + ", " + request.getAttribute("firstName"));
         newuser.setEmailAddress(request.getAttribute("emailAddress"));
         newuser.setOrganization(request.getAttribute("organization"));
 
         long now = Calendar.getInstance().getTime().getTime();
         newuser.setAttribute(User.CREATEDATE, String.valueOf(now));
 
         // Submit changes
         this.userManagerService.saveUser(newuser);
 
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             PasswordEditor editor = passwordManagerService.editPassword(newuser);
             String password = request.getAttribute("password");
 
             editor.setValue(password);
             passwordManagerService.saveHashedPassword(editor);
         }
 
         // Save user role
         saveUserRole(newuser);
         log.debug("Exiting saveUser()");
         return newuser;
     }
 
     private void saveUserRole(User user) {
         log.debug("Entering saveUserRole()");
         List<PortletRole> defaultRoles = roleService.getDefaultRoles();
         for (PortletRole role : defaultRoles) {
             roleService.addUserToRole(user, role);
         }
     }
 
     public void displayForgotPassword(ActionFormEvent event) {
         boolean sendMail = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.SEND_USER_FORGET_PASSWORD)).booleanValue();
         if (sendMail) {
             PortletRequest req = event.getActionRequest();
             setNextState(req, DO_FORGOT_PASSWORD);
         }
     }
 
     public void notifyUser(ActionFormEvent evt) {
         PortletRequest req = evt.getActionRequest();
 
         User user;
         TextFieldBean emailTF = evt.getTextFieldBean("emailTF");
 
         if (emailTF.getValue().equals("")) {
             createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_NO_EMAIL"));
             return;
         } else {
             user = userManagerService.getUserByEmail(emailTF.getValue());
         }
         if (user == null) {
             createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_NOEXIST"));
             return;
         }
 
         // create a request
         Request request = requestService.createRequest(FORGOT_PASSWORD_LABEL);
         long now = Calendar.getInstance().getTime().getTime();
 
         request.setLifetime(new Date(now + REQUEST_LIFETIME));
         request.setUserID(user.getID());
         requestService.saveRequest(request);
 
         MailMessage mailToUser = new MailMessage();
         mailToUser.setEmailAddress(emailTF.getValue());
         String subjectHeader = portalConfigService.getProperty("LOGIN_FORGOT_SUBJECT");
         if (subjectHeader == null) subjectHeader = getLocalizedText(req, "MAIL_SUBJECT_HEADER");
         mailToUser.setSubject(subjectHeader);
         StringBuffer body = new StringBuffer();
 
         String forgotMail = portalConfigService.getProperty("LOGIN_FORGOT_BODY");
         if (forgotMail == null) forgotMail = getLocalizedText(req, "LOGIN_FORGOT_MAIL");
         body.append(forgotMail).append("\n\n");
 
         body.append(newpasswordURL).append("&reqid=").append(request.getOid());
         mailToUser.setBody(body.toString());
 
         try {
             mailService.sendMail(mailToUser);
             createSuccessMessage(evt, this.getLocalizedText(req, "LOGIN_SUCCESS_MAIL"));
         } catch (PortletServiceException e) {
             log.error("Unable to send mail message!", e);
             createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
             setNextState(req, DEFAULT_VIEW_PAGE);
         }
     }
 
     public void notifyNewUser(ActionFormEvent evt) throws PortletException {
         ActionRequest req = evt.getActionRequest();
         ActionResponse res = evt.getActionResponse();
         TextFieldBean emailTF = evt.getTextFieldBean("emailAddress");
 
         // create a request
         Request request = requestService.createRequest(ACTIVATE_ACCOUNT_LABEL);
         long now = Calendar.getInstance().getTime().getTime();
 
         request.setLifetime(new Date(now + REQUEST_LIFETIME));
 
         // request.setUserID(user.getID());
 
         request.setAttribute("userName", evt.getTextFieldBean("userName").getValue());
         request.setAttribute("firstName", evt.getTextFieldBean("firstName").getValue());
         request.setAttribute("lastName", evt.getTextFieldBean("lastName").getValue());
         request.setAttribute("emailAddress", evt.getTextFieldBean("emailAddress").getValue());
         request.setAttribute("organization", evt.getTextFieldBean("organization").getValue());
 
         // put hashed pass in request
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             String pass = evt.getPasswordBean("password").getValue();
             pass = passwordManagerService.getHashedPassword(pass);
             request.setAttribute("password", pass);
         }
 
         requestService.saveRequest(request);
 
         MailMessage mailToUser = new MailMessage();
         mailToUser.setSender(portalConfigService.getProperty(PortalConfigService.MAIL_FROM));
         StringBuffer body = new StringBuffer();
 
         String activateURL = activateAccountURL + "&reqid=" + request.getOid();
 
         String denyURL = denyAccountURL + "&reqid=" + request.getOid();
 
         // check if this account request should be approved by an administrator
         boolean accountApproval = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.ADMIN_ACCOUNT_APPROVAL)).booleanValue();
         if (accountApproval) {
             String admin = portalConfigService.getProperty(PortalConfigService.PORTAL_ADMIN_EMAIL);
             mailToUser.setEmailAddress(admin);
             String mailSubject = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ADMIN_MAILSUBJECT");
             if (mailSubject == null) mailSubject = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ADMIN_MAILSUBJECT");
             mailToUser.setSubject(mailSubject);
             String adminBody = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ADMIN_MAIL");
             if (adminBody == null) adminBody = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ADMIN_MAIL");
             body.append(adminBody).append("\n\n");
             body.append(getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ALLOW")).append("\n\n");
             body.append(activateURL).append("\n\n");
             body.append(getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_DENY")).append("\n\n");
             body.append(denyURL).append("\n\n");
         } else {
             mailToUser.setEmailAddress(emailTF.getValue());
 
             String mailSubjectHeader = portalConfigService.getProperty("LOGIN_ACTIVATE_SUBJECT");
             String loginActivateMail = portalConfigService.getProperty("LOGIN_ACTIVATE_BODY");
 
             if (mailSubjectHeader == null) mailSubjectHeader = getLocalizedText(req, "LOGIN_ACTIVATE_SUBJECT");
             mailToUser.setSubject(mailSubjectHeader);
 
             if (loginActivateMail == null) loginActivateMail = getLocalizedText(req, "LOGIN_ACTIVATE_MAIL");
             body.append(loginActivateMail).append("\n\n");
             body.append(activateURL).append("\n\n");
         }
 
         body.append(getLocalizedText(req, "USERNAME")).append("\t");
         body.append(evt.getTextFieldBean("userName").getValue()).append("\n");
         body.append(getLocalizedText(req, "GIVENNAME")).append("\t");
         body.append(evt.getTextFieldBean("firstName").getValue()).append("\n");
         body.append(getLocalizedText(req, "FAMILYNAME")).append("\t");
         body.append(evt.getTextFieldBean("lastName").getValue()).append("\n");
         body.append(getLocalizedText(req, "ORGANIZATION")).append("\t");
         body.append(evt.getTextFieldBean("organization").getValue()).append("\n");
         body.append(getLocalizedText(req, "EMAILADDRESS")).append("\t");
         body.append(evt.getTextFieldBean("emailAddress").getValue()).append("\n");
 
         mailToUser.setBody(body.toString());
 
         try {
             mailService.sendMail(mailToUser);
         } catch (PortletServiceException e) {
             createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
             throw new PortletException("Unable to send mail message!", e);
         }
 
         boolean adminRequired = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.ADMIN_ACCOUNT_APPROVAL));
         if (adminRequired) {
             createSuccessMessage(evt, this.getLocalizedText(req, "LOGIN_ACCT_ADMIN_MAIL"));
         } else {
             createSuccessMessage(evt, this.getLocalizedText(req, "LOGIN_ACCT_MAIL"));
         }
 
     }
 
 
     public void newpassword(ActionFormEvent evt) {
         PortletRequest req = evt.getActionRequest();
         String id = req.getParameter("reqid");
         Request request = requestService.getRequest(id, FORGOT_PASSWORD_LABEL);
         if (request != null) {
             HiddenFieldBean reqid = evt.getHiddenFieldBean("reqid");
             reqid.setValue(id);
             setNextState(req, DO_NEW_PASSWORD);
         } else {
             setNextState(req, DEFAULT_VIEW_PAGE);
         }
     }
 
     private void doEmailAction(ActionFormEvent event, String msg, boolean createAccount) {
         PortletRequest req = event.getActionRequest();
         String id = req.getParameter("reqid");
         User user = null;
         Request request = requestService.getRequest(id, ACTIVATE_ACCOUNT_LABEL);
         if (request != null) {
             requestService.deleteRequest(request);
 
             String subject = "";
             String body = "";
             if (createAccount) {
                 user = saveUser(request);
                 createSuccessMessage(event, msg + " " + user.getUserName());
 
                 // send the user an email
                 subject = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
                 if (subject == null) subject = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
                 body = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED_BODY");
                 if (body == null) body = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
             } else {
                 createSuccessMessage(event, msg);
 
                 // send the user an email
                 subject = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
                 if (subject == null) subject = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
                 body = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY_BODY");
                 if (body == null) body = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
             }
 
             MailMessage mailToUser = new MailMessage();
             mailToUser.setEmailAddress(user.getEmailAddress());
 
             mailToUser.setSubject(subject);
             StringBuffer msgbody = new StringBuffer();
 
             msgbody.append(body).append("\n\n");
             msgbody.append(notificationURL);
             mailToUser.setBody(body.toString());
 
             try {
                 mailService.sendMail(mailToUser);
             } catch (PortletServiceException e) {
                log.error("Error: ", e);
                 createErrorMessage(event, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
             }
         }
         setNextState(req, "doViewUser");
 
     }
 
     public void activate(ActionFormEvent event) {
         PortletRequest req = event.getActionRequest();
         String msg = this.getLocalizedText(req, "USER_NEW_ACCOUNT") +
                 "<br>" + this.getLocalizedText(req, "USER_PLEASE_LOGIN");
         doEmailAction(event, msg, true);
     }
 
     public void approveAccount(ActionFormEvent event) {
         PortletRequest req = event.getActionRequest();
         String msg = this.getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
         doEmailAction(event, msg, true);
     }
 
     public void denyAccount(ActionFormEvent event) {
         PortletRequest req = event.getActionRequest();
         String msg = this.getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
         doEmailAction(event, msg, false);
     }
 
 
     public void doSavePass(ActionFormEvent event) {
 
         PortletRequest req = event.getActionRequest();
 
         HiddenFieldBean reqid = event.getHiddenFieldBean("reqid");
         String id = reqid.getValue();
         Request request = requestService.getRequest(id, FORGOT_PASSWORD_LABEL);
         if (request != null) {
             String uid = request.getUserID();
             User user = userManagerService.getUser(uid);
 
             passwordManagerService.editPassword(user);
 
             String passwordValue = event.getPasswordBean("password").getValue();
             String confirmPasswordValue = event.getPasswordBean("confirmPassword").getValue();
 
             if (passwordValue == null) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_NOTSET"));
                 setNextState(req, DO_NEW_PASSWORD);
                 return;
             }
 
             // Otherwise, password must match confirmation
             if (!passwordValue.equals(confirmPasswordValue)) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_MISMATCH"));
                 setNextState(req, DO_NEW_PASSWORD);
                 // If they do match, then validate password with our service
             } else {
                 if (passwordValue.length() == 0) {
                     createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_BLANK"));
                     setNextState(req, DO_NEW_PASSWORD);
                 } else if (passwordValue.length() < 5) {
                     System.err.println("length < 5 password= " + passwordValue);
                     createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_TOOSHORT"));
                     setNextState(req, DO_NEW_PASSWORD);
                 } else {
                     // save password
                     //System.err.println("saving password= " + passwordValue);
                     PasswordEditor editPasswd = passwordManagerService.editPassword(user);
                     editPasswd.setValue(passwordValue);
                     editPasswd.setDateLastModified(Calendar.getInstance().getTime());
                     passwordManagerService.savePassword(editPasswd);
                     createSuccessMessage(event, this.getLocalizedText(req, "USER_PASSWORD_SUCCESS"));
                     requestService.deleteRequest(request);
                 }
             }
         }
     }
 }
