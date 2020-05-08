 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.login;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigService;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigSettings;
 import org.gridlab.gridsphere.services.core.request.GenericRequest;
 import org.gridlab.gridsphere.services.core.request.RequestService;
 import org.gridlab.gridsphere.services.core.security.group.GroupManagerService;
 import org.gridlab.gridsphere.services.core.security.auth.modules.LoginAuthModule;
 import org.gridlab.gridsphere.services.core.security.password.PasswordEditor;
 import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;
 import org.gridlab.gridsphere.services.core.security.role.RoleManagerService;
 import org.gridlab.gridsphere.services.core.user.LoginService;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 import org.gridlab.gridsphere.services.core.messaging.TextMessagingService;
 import org.gridsphere.tmf.TextMessagingException;
 import org.gridsphere.tmf.message.MailMessage;
 
 import javax.servlet.UnavailableException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.*;
 import java.security.cert.X509Certificate;
 
 public class LoginPortlet extends ActionPortlet {
 
     private static String FORGOT_PASSWORD_LABEL ="forgotpassword";
     private static String ACTIVATE_ACCOUNT_LABEL ="activateaccount";
     private static String LOGIN_NUMTRIES = "ACCOUNT_NUMTRIES";
     private static String LOGIN_NAME = "LOGIN_NAME";
     public static String SAVE_PASSWORDS = "SAVE_PASSWORDS";
     public static String SUPPORT_X509_AUTH = "SUPPORT_X509_AUTH";
     public static String SEND_USER_FORGET_PASSWORD = "SEND_USER_FORGET_PASSWD";
 
     private static long REQUEST_LIFETIME = 1000*60*24*3; // 3 days
 
     public static final String LOGIN_ERROR_FLAG = "LOGIN_FAILED";
     public static final Integer LOGIN_ERROR_UNKNOWN = new Integer(-1);
 
     public static final String DO_VIEW_USER_EDIT_LOGIN = "login/createaccount.jsp"; //edit user
     public static final String DO_FORGOT_PASSWORD = "login/forgotpassword.jsp";
     public static final String DO_NEW_PASSWORD = "login/newpassword.jsp";
 
     public static final String DO_CONFIGURE = "login/config.jsp"; //configure login
 
     private boolean canUserCreateAccount = false;
     private int defaultNumTries = 0;
 
     private UserManagerService userManagerService = null;
     private GroupManagerService groupService = null;
     private RoleManagerService roleService = null;
     private PasswordManagerService passwordManagerService = null;
     private PortalConfigService portalConfigService = null;
     private RequestService requestService = null;
     private LoginService loginService = null;
     private TextMessagingService tms = null;
 
     public void init(PortletConfig config) throws UnavailableException {
 
         super.init(config);
         try {
             userManagerService = (UserManagerService) getPortletConfig().getContext().getService(UserManagerService.class);
             groupService = (GroupManagerService) getPortletConfig().getContext().getService(GroupManagerService.class);
             roleService = (RoleManagerService) getPortletConfig().getContext().getService(RoleManagerService.class);
             passwordManagerService = (PasswordManagerService) getPortletConfig().getContext().getService(PasswordManagerService.class);
             requestService = (RequestService) getPortletConfig().getContext().getService(RequestService.class);
             tms = (TextMessagingService) getPortletConfig().getContext().getService(TextMessagingService.class);
             portalConfigService = (PortalConfigService) getPortletConfig().getContext().getService(PortalConfigService.class);
             canUserCreateAccount = portalConfigService.getPortalConfigSettings().getCanUserCreateAccount();
             PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
             if (settings.getAttribute(SEND_USER_FORGET_PASSWORD) == null) {
                 settings.setAttribute(SEND_USER_FORGET_PASSWORD, Boolean.TRUE.toString());
             }
             if (settings.getAttribute(SAVE_PASSWORDS) == null) {
                 settings.setAttribute(SAVE_PASSWORDS, Boolean.TRUE.toString());
             }
             String numTries = settings.getAttribute(LOGIN_NUMTRIES);
 
             if (numTries == null) {
                 settings.setAttribute(LOGIN_NUMTRIES, "-1");
                 defaultNumTries = -1;
             } else {
                 defaultNumTries = Integer.valueOf(numTries).intValue();
             }
             portalConfigService.savePortalConfigSettings(settings);
             loginService = (LoginService)getPortletConfig().getContext().getService(LoginService.class);
         } catch (PortletServiceException e) {
             throw new UnavailableException("Unable to initialize services");
         }
         DEFAULT_VIEW_PAGE = "doViewUser";
         DEFAULT_CONFIGURE_PAGE = "showConfigure";
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
     }
 
     public void doViewUser(FormEvent event) throws PortletException {
         log.debug("in LoginPortlet: doViewUser");
         PortletRequest request = event.getPortletRequest();
         User user = request.getUser();
         PasswordBean pass = event.getPasswordBean("password");
         pass.setValue("");
         request.setAttribute("user", user);
 
         // Check certificates
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         String x509supported = settings.getAttribute(SUPPORT_X509_AUTH);
         if ((x509supported != null) && (x509supported.equalsIgnoreCase("true"))) {
             X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
             if (certs != null && certs.length > 0) {
                 request.setAttribute("certificate", certs[0].getSubjectDN().toString());
             }
         }
 
         if (user == null) {
             if (canUserCreateAccount) request.setAttribute("canUserCreateAcct", "true");
             boolean dispUser = Boolean.valueOf(settings.getAttribute(SEND_USER_FORGET_PASSWORD)).booleanValue();
             if (dispUser) request.setAttribute("dispPass", "true");
             setNextState(request, "login/login.jsp");
         } else {
             showConfigure(event);
         }
     }
 
 
     public void doTitle(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         User user = request.getUser();
         PrintWriter out = response.getWriter();
 
         if (user == null) {
             out.println(getNextTitle(request));
         } else {
             out.println(getLocalizedText(request, "LOGIN_CONFIGURE"));
         }
     }
 
     public void gs_login(FormEvent event) throws PortletException {
         log.debug("in LoginPortlet: gs_login");
         PortletRequest req = event.getPortletRequest();
 
         String errorMsg = (String) req.getAttribute(LoginPortlet.LOGIN_ERROR_FLAG);
 
         if (errorMsg != null) {
             Integer numTries = (Integer)req.getSession(true).getAttribute(LoginPortlet.LOGIN_NUMTRIES);
             String loginname = (String)req.getSession(true).getAttribute(LoginPortlet.LOGIN_NAME);
             int i = 1;
             if (numTries != null) {
                 i = numTries.intValue();
                 i++;
             }
             numTries = new Integer(i);
             req.getSession(true).setAttribute(LoginPortlet.LOGIN_NUMTRIES, numTries);
             req.getSession(true).setAttribute(LoginPortlet.LOGIN_NAME, req.getParameter("username"));
             System.err.println("num tries = " + i);
             // tried one to many times using same name
            if (req.getParameter("username") != null && req.getParameter("username").equals(loginname)) {
                 if ((i >= defaultNumTries) && (defaultNumTries != -1)) {
                     disableAccount(event);
                     errorMsg = this.getLocalizedText(req, "LOGIN_TOOMANY_ATTEMPTS");
                     req.getSession(true).removeAttribute(LoginPortlet.LOGIN_NUMTRIES);
                     req.getSession(true).removeAttribute(LoginPortlet.LOGIN_NAME);
                 }
             }
             createErrorMessage(event, errorMsg);
             req.removeAttribute(LoginPortlet.LOGIN_ERROR_FLAG);
         }
 
         setNextState(req, "doViewUser");
     }
 
     public void disableAccount(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         String loginName = req.getParameter("username");
         User user = userManagerService.getUserByUserName(loginName);
         if (user != null) {
             System.err.println("user= " + user);
             user.setAttribute(User.DISABLED, "true");
             userManagerService.saveUser(user);
 
             MailMessage mailToUser = tms.getMailMessage();
             StringBuffer body = new StringBuffer();
             body.append(getLocalizedText(req, "LOGIN_DISABLED_MSG1") + " " + getLocalizedText(req, "LOGIN_DISABLED_MSG2") + "\n\n");
             mailToUser.setBody(body.toString());
             mailToUser.setSubject(getLocalizedText(req, "LOGIN_DISABLED_SUBJECT"));
             mailToUser.setTo(user.getEmailAddress());
             mailToUser.setServiceid("mail");
 
             org.gridsphere.tmf.message.MailMessage mailToAdmin = tms.getMailMessage();
             StringBuffer body2 = new StringBuffer();
             body2.append(getLocalizedText(req, "LOGIN_DISABLED_ADMIN_MSG") + " " + user.getUserName());
             mailToAdmin.setBody(body2.toString());
             mailToAdmin.setSubject(getLocalizedText(req, "LOGIN_DISABLED_SUBJECT") + " " + user.getUserName());
             mailToAdmin.setTo(tms.getServiceUserID("mail", "root"));
             mailToUser.setServiceid("mail");
 
 
             try {
                 tms.send(mailToUser);
                 tms.send(mailToAdmin);
             } catch (TextMessagingException e) {
                 log.error("Unable to send mail message!", e);
                 createErrorMessage(event, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
                 return;
             }
         }
     }
 
     public void doNewUser(FormEvent evt)
             throws PortletException {
         if (!canUserCreateAccount) return;
 
         PortletRequest req = evt.getPortletRequest();
 
         MessageBoxBean msg = evt.getMessageBoxBean("msg");
         msg.setKey("LOGIN_CREATE_ACCT");
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         setNextState(req, DO_VIEW_USER_EDIT_LOGIN);
         log.debug("in doViewNewUser");
     }
 
     public void doConfirmEditUser(FormEvent evt)
             throws PortletException {
         PortletRequest req = evt.getPortletRequest();
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         if (!canUserCreateAccount) return;
 
         try {
             //check if the user is new or not
             validateUser(evt);
             //new and valid user and will save it
             notifyNewUser(evt);
 
             setNextState(req, "doViewUser");
         } catch (PortletException e) {
             //invalid user, an exception was thrown
             //back to edit
             setNextState(req, DO_VIEW_USER_EDIT_LOGIN);
         }
     }
 
     private void validateUser(FormEvent event)
             throws PortletException {
         log.debug("Entering validateUser()");
         PortletRequest req = event.getPortletRequest();
         StringBuffer message = new StringBuffer();
         boolean isInvalid = false;
         // Validate user name
         String userName = event.getTextFieldBean("userName").getValue();
         if (userName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NAME_BLANK") + "<br />");
             isInvalid = true;
         }
 
         if (this.userManagerService.existsUserName(userName)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_EXISTS") + "<br />");
             isInvalid = true;
         }
 
 
         // Validate full name
 
         String familyName = event.getTextFieldBean("fullName").getValue();
         if (familyName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_FULLNAME_BLANK") + "<br />");
             isInvalid = true;
         }
 
         // Validate e-mail
         String eMail = event.getTextFieldBean("emailAddress").getValue();
         if (eMail.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             isInvalid = true;
         } else if ((eMail.indexOf("@") < 0)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             isInvalid = true;
         } else if ((eMail.indexOf(".") < 0)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             isInvalid = true;
         }
 
         //Validate password
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             if (!isInvalid) {
                 isInvalid = isInvalidPassword(event);
             }
         }
         // Throw exception if error was found
         if (isInvalid) {
             throw new PortletException(message.toString());
         }
         log.debug("Exiting validateUser()");
     }
 
     private boolean isInvalidPassword(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
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
 
     private User saveUser(GenericRequest request) {
         log.debug("Entering saveUser()");
         // Account request
 
         // Create edit account request
 
         User newuser = this.userManagerService.createUser();
 
         // Edit account attributes
         newuser.setUserName(request.getAttribute("userName"));
         newuser.setFullName(request.getAttribute("fullName"));
         newuser.setEmailAddress(request.getAttribute("emailAddress"));
         newuser.setOrganization(request.getAttribute("organization"));
 
         // Submit changes
         this.userManagerService.saveUser(newuser);
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
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
         Set groups = portalConfigService.getPortalConfigSettings().getDefaultGroups();
         Iterator it = groups.iterator();
         while (it.hasNext()) {
             PortletGroup group = (PortletGroup) it.next();
             this.groupService.addUserToGroup(user, group);
         }
         roleService.addUserToRole(user, PortletRole.USER);
     }
 
     public void showConfigure(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         CheckBoxBean acctCB = event.getCheckBoxBean("acctCB");
         acctCB.setSelected(canUserCreateAccount);
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
 
         CheckBoxBean notifyCB = event.getCheckBoxBean("notifyCB");
         notifyCB.setSelected(Boolean.valueOf(settings.getAttribute(SEND_USER_FORGET_PASSWORD)).booleanValue());
 
         CheckBoxBean savepassCB = event.getCheckBoxBean("savepassCB");
         savepassCB.setSelected(Boolean.valueOf(settings.getAttribute(SAVE_PASSWORDS)).booleanValue());
 
         CheckBoxBean supportX509CB = event.getCheckBoxBean("supportx509CB");
         supportX509CB.setSelected(Boolean.valueOf(settings.getAttribute(SUPPORT_X509_AUTH)).booleanValue());
 
         String numTries = settings.getAttribute(LOGIN_NUMTRIES);
         TextFieldBean numTriesTF = event.getTextFieldBean("numTriesTF");
         if (numTries == null) {
             numTries = "-1";
         }
         numTriesTF.setValue(numTries);
 
         List authModules = loginService.getAuthModules();
         req.setAttribute("authModules", authModules);
         setNextState(req, DO_CONFIGURE);
     }
 
     public void setLoginSettings(FormEvent event) throws PortletException {
         PortletRequest req = event.getPortletRequest();
         if (!req.getRoles().contains(PortletRole.ADMIN.getName())) return;
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         CheckBoxBean acctCB = event.getCheckBoxBean("acctCB");
         String useracct = acctCB.getSelectedValue();
 
         canUserCreateAccount = (useracct != null);
 
         settings.setCanUserCreateAccount(canUserCreateAccount);
 
         CheckBoxBean notifyCB = event.getCheckBoxBean("notifyCB");
         String notify = notifyCB.getSelectedValue();
         boolean sendForget = (notify != null);
 
         CheckBoxBean savepassCB = event.getCheckBoxBean("savepassCB");
         String savepass = savepassCB.getSelectedValue();
         boolean savePasswords;
         if (savepass != null) {
             savePasswords = true;
         } else {
             // if save passwords is false than can't send notification to update password so both must be false
             savePasswords = false;
             sendForget = false;
         }
 
         CheckBoxBean supportsx509CB = event.getCheckBoxBean("supportx509CB");
         String supportx509val = supportsx509CB.getSelectedValue();
         boolean supportx509 = (supportx509val != null);
         settings.setAttribute(SUPPORT_X509_AUTH, Boolean.toString(supportx509));
 
         settings.setAttribute(SAVE_PASSWORDS, Boolean.toString(savePasswords));
         settings.setAttribute(SEND_USER_FORGET_PASSWORD, Boolean.toString(sendForget));
         portalConfigService.savePortalConfigSettings(settings);
         showConfigure(event);
     }
 
     public void configAccountSettings(FormEvent event) throws PortletException {
         PortletRequest req = event.getPortletRequest();
         if (!req.getRoles().contains(PortletRole.ADMIN.getName())) return;
         TextFieldBean numTriesTF = event.getTextFieldBean("numTriesTF");
         String numTries = numTriesTF.getValue();
         int numtries = -1;
         try {
             numtries = Integer.valueOf(numTries).intValue();
         } catch (NumberFormatException e) {
             // do nothing
         }
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         settings.setAttribute(LOGIN_NUMTRIES, numTries);
         defaultNumTries = numtries;
         portalConfigService.savePortalConfigSettings(settings);
         showConfigure(event);
     }
 
     public void displayForgotPassword(FormEvent event) {
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         boolean sendMail = Boolean.valueOf(settings.getAttribute(SEND_USER_FORGET_PASSWORD)).booleanValue();
         if (sendMail) {
             PortletRequest req = event.getPortletRequest();
             setNextState(req, DO_FORGOT_PASSWORD);
         }
     }
 
     public void notifyUser(FormEvent evt) {
         PortletRequest req = evt.getPortletRequest();
         PortletResponse res = evt.getPortletResponse();
 
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
         GenericRequest request = requestService.createRequest(FORGOT_PASSWORD_LABEL);
         long now = Calendar.getInstance().getTime().getTime();
 
         request.setLifetime(new Date(now + REQUEST_LIFETIME));
         request.setUserID(user.getID());
         requestService.saveRequest(request);
 
         org.gridsphere.tmf.message.MailMessage mailToUser = tms.getMailMessage();
         mailToUser.setTo(emailTF.getValue());
         mailToUser.setSubject(getLocalizedText(req, "MAIL_SUBJECT_HEADER"));
         StringBuffer body = new StringBuffer();
 
         body.append(getLocalizedText(req, "LOGIN_FORGOT_MAIL") + "\n\n");
 
         PortletURI uri = res.createURI();
 
         uri.addAction("newpassword");
         uri.addParameter("reqid", request.getOid());
 
         body.append(uri.toString());
         mailToUser.setBody(body.toString());
         mailToUser.setServiceid("mail");
 
         try {
             tms.send(mailToUser);
         } catch (TextMessagingException e) {
             log.error("Unable to send mail message!", e);
             createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
             return;
         }
         createSuccessMessage(evt, this.getLocalizedText(req, "LOGIN_SUCCESS_MAIL"));
 
     }
 
     public void notifyNewUser(FormEvent evt) {
         PortletRequest req = evt.getPortletRequest();
         PortletResponse res = evt.getPortletResponse();
 
 
         TextFieldBean emailTF = evt.getTextFieldBean("emailAddress");
 
         if (emailTF.getValue().equals("")) {
             createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_NO_EMAIL"));
             return;
         }
 
         // create a request
         GenericRequest request = requestService.createRequest(ACTIVATE_ACCOUNT_LABEL);
         long now = Calendar.getInstance().getTime().getTime();
 
         request.setLifetime(new Date(now + REQUEST_LIFETIME));
 
         // request.setUserID(user.getID());
 
         request.setAttribute("userName", evt.getTextFieldBean("userName").getValue());
         request.setAttribute("fullName", evt.getTextFieldBean("fullName").getValue());
         request.setAttribute("emailAddress", evt.getTextFieldBean("emailAddress").getValue());
         request.setAttribute("organization", evt.getTextFieldBean("organization").getValue());
 
         // put hashed pass in request
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             String pass = evt.getPasswordBean("password").getValue();
             pass = passwordManagerService.getHashedPassword(pass);
             request.setAttribute("password", pass);
         }
 
         requestService.saveRequest(request);
 
         MailMessage mailToUser = tms.getMailMessage();
         mailToUser.setTo(emailTF.getValue());
         mailToUser.setSubject(getLocalizedText(req, "MAIL_SUBJECT_HEADER"));
         StringBuffer body = new StringBuffer();
 
         body.append(getLocalizedText(req, "LOGIN_ACTIVATE_MAIL") + "\n\n");
 
         PortletURI uri = res.createURI();
         uri.addAction("activate");
         uri.addParameter("reqid", request.getOid());
 
         body.append(uri.toString());
         mailToUser.setBody(body.toString());
         mailToUser.setServiceid("mail");
 
         try {
             tms.send(mailToUser);
         } catch (TextMessagingException e) {
             log.error("Unable to send mail message!", e);
             createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
             return;
         }
         createSuccessMessage(evt, this.getLocalizedText(req, "LOGIN_ACCT_MAIL"));
 
     }
 
     private void createErrorMessage(FormEvent evt, String text) {
         MessageBoxBean msg = evt.getMessageBoxBean("msg");
         msg.setValue(text);
         msg.setMessageType(MessageStyle.MSG_ERROR);
         msg.setDefaultImage(true);
     }
 
     private void createSuccessMessage(FormEvent evt, String text) {
         MessageBoxBean msg = evt.getMessageBoxBean("msg");
         msg.setValue(text);
         msg.setMessageType(MessageStyle.MSG_SUCCESS);
         msg.setDefaultImage(true);
     }
 
     public void newpassword(FormEvent evt) {
 
         PortletRequest req = evt.getPortletRequest();
 
         String id = req.getParameter("reqid");
 
         GenericRequest request = requestService.getRequest(id, FORGOT_PASSWORD_LABEL);
         if (request != null) {
             HiddenFieldBean reqid = evt.getHiddenFieldBean("reqid");
             reqid.setValue(id);
             setNextState(req, DO_NEW_PASSWORD);
         } else {
             setNextState(req, DEFAULT_VIEW_PAGE);
         }
 
     }
 
     public void activate(FormEvent evt) {
 
         PortletRequest req = evt.getPortletRequest();
 
         String id = req.getParameter("reqid");
 
         GenericRequest request = requestService.getRequest(id, ACTIVATE_ACCOUNT_LABEL);
         if (request != null) {
             User user = saveUser(request);
 
             createSuccessMessage(evt, this.getLocalizedText(req, "USER_NEW_ACCOUNT") +
                     "<br>" + this.getLocalizedText(req, "USER_PLEASE_LOGIN") +
                     " " + user.getUserName());
 
             requestService.deleteRequest(request);
             setNextState(req, "doViewUser");
         }
 
     }
 
     public void doSaveAuthModules(FormEvent event) {
 
         PortletRequest req = event.getPortletRequest();
         CheckBoxBean cb = event.getCheckBoxBean("authModCB");
 
         List activeAuthMods = cb.getSelectedValues();
         for (int i = 0; i < activeAuthMods.size(); i++) {
             System.err.println("active auth mod: " + (String)activeAuthMods.get(i));
         }
 
         List authModules = loginService.getAuthModules();
         Iterator it = authModules.iterator();
         while (it.hasNext()) {
             LoginAuthModule authMod = (LoginAuthModule)it.next();
             // see if a checked active auth module appears
             if (activeAuthMods.contains(authMod.getModuleName())) {
                 authMod.setModuleActive(true);
             } else {
                 authMod.setModuleActive(false);
             }
             String priority = req.getParameter(authMod.getModuleName());
             authMod.setModulePriority(Integer.valueOf(priority).intValue());
             loginService.saveAuthModule(authMod);
         }
 
         showConfigure(event);
     }
 
 
 
     public void doSavePass(FormEvent event) {
 
         PortletRequest req = event.getPortletRequest();
 
         HiddenFieldBean reqid = event.getHiddenFieldBean("reqid");
         String id = reqid.getValue();
         GenericRequest request = requestService.getRequest(id, FORGOT_PASSWORD_LABEL);
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
