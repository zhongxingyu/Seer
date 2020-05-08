 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.login;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletUser;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.services.core.mail.MailMessage;
 import org.gridlab.gridsphere.services.core.mail.MailService;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigService;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigSettings;
 import org.gridlab.gridsphere.services.core.request.GenericRequest;
 import org.gridlab.gridsphere.services.core.request.RequestService;
 import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
 import org.gridlab.gridsphere.services.core.security.acl.GroupRequest;
 import org.gridlab.gridsphere.services.core.security.password.PasswordEditor;
 import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 
 import javax.mail.MessagingException;
 import javax.servlet.UnavailableException;
 import javax.servlet.http.Cookie;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Date;
 
 public class LoginPortlet extends ActionPortlet {
 
     private static String FORGOT_PASSWORD_LABEL ="forgotpassword";
     private static long PASSWORD_REQUEST_LIFETIME = 1000*60*24*7; // 1 week
     
     public static final String LOGIN_ERROR_FLAG = "LOGIN_FAILED";
     public static final Integer LOGIN_ERROR_UNKNOWN = new Integer(-1);
 
     public static final String DO_VIEW_USER_EDIT_LOGIN = "login/createaccount.jsp"; //edit user
     public static final String DO_FORGOT_PASSWORD = "login/forgotpassword.jsp";
     public static final String DO_NEW_PASSWORD = "login/newpassword.jsp";
 
     public static final String DO_CONFIGURE = "login/config.jsp"; //configure login
 
     private boolean canUserCreateAccount = false;
 
     private UserManagerService userManagerService = null;
     private AccessControlManagerService aclService = null;
     private PasswordManagerService passwordManagerService = null;
     private PortalConfigService portalConfigService = null;
     private RequestService requestService = null;
     private MailService mailService = null;
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         try {
             userManagerService = (UserManagerService) getPortletConfig().getContext().getService(UserManagerService.class);
             aclService = (AccessControlManagerService) getPortletConfig().getContext().getService(AccessControlManagerService.class);
             passwordManagerService = (PasswordManagerService) getPortletConfig().getContext().getService(PasswordManagerService.class);
             requestService = (RequestService) getPortletConfig().getContext().getService(RequestService.class);
             mailService = (MailService) getPortletConfig().getContext().getService(MailService.class);
             portalConfigService = (PortalConfigService) getPortletConfig().getContext().getService(PortalConfigService.class);
            if (portalConfigService == null) System.err.println("what the hell?");
             canUserCreateAccount = portalConfigService.getPortalConfigSettings().getCanUserCreateAccount();
         } catch (PortletServiceException e) {
             throw new UnavailableException("Unable to initialize services");
         }
         DEFAULT_VIEW_PAGE = "doViewFirstUser";
         DEFAULT_CONFIGURE_PAGE = "showConfigure";
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
     }
 
     public void doViewFirstUser(FormEvent event) throws PortletException {
         MessageBoxBean msg = event.getMessageBoxBean("msg");
         msg.clearMessage();
         doViewUser(event);
     }
 
     public void doViewUser(FormEvent event) throws PortletException {
         log.debug("in LoginPortlet: doViewUser");
         PortletRequest request = event.getPortletRequest();
         User user = request.getUser();
         PasswordBean pass = event.getPasswordBean("password");
         pass.setValue("");
         request.setAttribute("user", user);
 
         //CheckBoxBean cb = event.getCheckBoxBean("remloginCB");
 
 
         if (user instanceof GuestUser) {
             if (canUserCreateAccount) request.setAttribute("canUserCreateAcct", "true");
             setNextState(request, "login/login.jsp");
         } else {
             showConfigure(event);
         }
     }
 
 
     public void doTitle(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         User user = request.getUser();
         PrintWriter out = response.getWriter();
 
         if (user instanceof GuestUser) {
             out.println(getNextTitle(request));
         } else {
             out.println(getLocalizedText(request, "LOGIN_CONFIGURE"));
         }
     }
 
     public void gs_login(FormEvent event) throws PortletException {
         log.debug("in LoginPortlet: gs_login");
         PortletRequest req = event.getPortletRequest();
 
         String errorKey = (String) req.getAttribute(LoginPortlet.LOGIN_ERROR_FLAG);
 
         if (errorKey != null) {
             MessageBoxBean frame = event.getMessageBoxBean("msg");
             frame.setKey(LoginPortlet.LOGIN_ERROR_FLAG);
             frame.setMessageType(MessageStyle.MSG_ERROR);
             req.removeAttribute(LoginPortlet.LOGIN_ERROR_FLAG);
         }
 
         setNextState(req, "doViewUser");
     }
 
     public void doNewUser(FormEvent evt)
             throws PortletException {
         if (!canUserCreateAccount) return;
 
         PortletRequest req = evt.getPortletRequest();
         setNextState(req, DO_VIEW_USER_EDIT_LOGIN);
         log.debug("in doViewNewUser");
     }
 
     public void doConfirmEditUser(FormEvent evt)
             throws PortletException {
         PortletRequest req = evt.getPortletRequest();
         //User user = loadUser(evt);
         if (!canUserCreateAccount) return;
 
         try {
             //check if the user is new or not
             validateUser(evt);
             //new and valid user and will save it
             User user = saveUser(evt);
             //show the data of this user
             createSuccessMessage(evt, this.getLocalizedText(req, "USER_NEW_ACCOUNT") +
                     "<br>" + this.getLocalizedText(req, "USER_PLEASE_LOGIN") +
                     " " + user.getUserName());
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
             createErrorMessage(event, this.getLocalizedText(req, "USER_NAME_BLANK") + "<br>");
             isInvalid = true;
         }
 
         if (this.userManagerService.existsUserName(userName)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_EXISTS") + "<br>");
             isInvalid = true;
         }
 
 
         // Validate full name
 
         String familyName = event.getTextFieldBean("fullName").getValue();
         if (familyName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_FULLNAME_BLANK") + "<br>");
             isInvalid = true;
         }
         /*
         // Validate given name
         String givenName = event.getTextFieldBean("givenName").getValue();
         if (givenName.equals("")) {
             message.append(this.getLocalizedText(req, "USER_GIVENNAME_BLANK") + "<br>");
             isInvalid = true;
         }
         */
 
         // Validate e-mail
         String eMail = event.getTextFieldBean("emailAddress").getValue();
         if (eMail.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br>");
             isInvalid = true;
         } else if ((eMail.indexOf("@") < 0)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br>");
             isInvalid = true;
         } else if ((eMail.indexOf(".") < 0)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br>");
             isInvalid = true;
         }
 
         //Validate password
         if (!isInvalid) {
             isInvalid = isInvalidPassword(event);
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
 
 
         // Otherwise, password must match confirmation
         if (!passwordValue.equals(confirmPasswordValue)) {
             createErrorMessage(event, (this.getLocalizedText(req, "USER_PASSWORD_MISMATCH")) + "<br>");
             return true;
             // If they do match, then validate password with our service
         } else {
             if (passwordValue == null) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_NOTSET"));
                 return true;
             }
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
 
     private User saveUser(FormEvent event) {
         log.debug("Entering saveUser()");
         // Account request
 
         // Create edit account request
 
         SportletUser newuser = this.userManagerService.createUser();
 
 
         // Edit account attributes
         editSportletUser(event, newuser);
         // Submit changes
         this.userManagerService.saveUser(newuser);
 
         PasswordEditor editor = passwordManagerService.editPassword(newuser);
         String password = event.getPasswordBean("password").getValue();
         editor.setValue(password);
         passwordManagerService.savePassword(editor);
 
         // Save user role
         saveUserRole(newuser);
         log.debug("Exiting saveUser()");
         return newuser;
     }
 
     private void editSportletUser(FormEvent event, SportletUser SportletUser) {
         log.debug("Entering editSportletUser()");
         SportletUser.setUserName(event.getTextFieldBean("userName").getValue());
         //SportletUser.setFamilyName(event.getTextFieldBean("familyName").getValue());
         //SportletUser.setGivenName(event.getTextFieldBean("givenName").getValue());
         SportletUser.setFullName(event.getTextFieldBean("fullName").getValue());
         SportletUser.setEmailAddress(event.getTextFieldBean("emailAddress").getValue());
         SportletUser.setOrganization(event.getTextFieldBean("organization").getValue());
 
         log.debug("Exiting editSportletUser()");
     }
 
     private void saveUserRole(User user) {
         log.debug("Entering saveUserRole()");
 
         // Revoke super role (in case they had it)
         //this.aclService.revokeSuperRole(user);
         // Create appropriate access request
         Set groups = portalConfigService.getPortalConfigSettings().getDefaultGroups();
         Iterator it = groups.iterator();
         while (it.hasNext()) {
             PortletGroup group = (PortletGroup) it.next();
             GroupRequest groupRequest = this.aclService.createGroupEntry();
             groupRequest.setUser(user);
             groupRequest.setGroup(group);
             groupRequest.setRole(PortletRole.USER);
 
             // Submit changes
             this.aclService.saveGroupEntry(groupRequest);
         }
 
     }
 
     public void showConfigure(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         CheckBoxBean acctCB = event.getCheckBoxBean("acctCB");
         acctCB.setSelected(canUserCreateAccount);
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
 
         TextFieldBean mailServerTF = event.getTextFieldBean("mailHostTF");
         mailServerTF.setValue(settings.getAttribute(MailService.MAIL_SERVER_HOST));
         TextFieldBean mailSenderTF = event.getTextFieldBean("mailFromTF");
         mailSenderTF.setValue(settings.getAttribute(MailService.MAIL_SENDER));
 
         setNextState(req, DO_CONFIGURE);
     }
 
     public void setUserCreateAccount(FormEvent event) throws PortletException {
         PortletRequest req = event.getPortletRequest();
         if (req.getRole().compare(req.getRole(), PortletRole.ADMIN) < 0) return;
         CheckBoxBean acctCB = event.getCheckBoxBean("acctCB");
         String useracct = acctCB.getSelectedValue();
         if (useracct != null) {
             canUserCreateAccount = true;
         } else {
             canUserCreateAccount = false;
         }
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         settings.setCanUserCreateAccount(canUserCreateAccount);
         portalConfigService.savePortalConfigSettings(settings);
         showConfigure(event);
     }
 
     public void configMailSettings(FormEvent event) throws PortletException {
         PortletRequest req = event.getPortletRequest();
         if (req.getRole().compare(req.getRole(), PortletRole.ADMIN) < 0) return;
         TextFieldBean mailServerTF = event.getTextFieldBean("mailHostTF");
         String mailServer = mailServerTF.getValue();
 
         TextFieldBean mailSenderTF = event.getTextFieldBean("mailFromTF");
         String mailFrom = mailSenderTF.getValue();
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (!mailServer.equals("")) settings.setAttribute(MailService.MAIL_SERVER_HOST, mailServer);
         if (!mailFrom.equals("")) settings.setAttribute(MailService.MAIL_SENDER, mailFrom);
 
         portalConfigService.savePortalConfigSettings(settings);
         showConfigure(event);
     }
 
     public void displayForgotPassword(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         setNextState(req, DO_FORGOT_PASSWORD);
     }
 
     public void notifyUser(FormEvent evt) {
         PortletRequest req = evt.getPortletRequest();
         PortletResponse res = evt.getPortletResponse();
 
         User user = null;
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
 
         request.setLifetime(new Date(now + PASSWORD_REQUEST_LIFETIME));
         request.setUserID(user.getID());
         requestService.saveRequest(request);
 
         // mail user
         MailMessage message = new MailMessage();
         message.setEmailAddress(emailTF.getValue());
         message.setSubject(getLocalizedText(req, "MAIL_SUBJECT_HEADER"));
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(MailService.MAIL_SERVER_HOST) != null) {
             mailService.setMailServiceHost(settings.getAttribute(MailService.MAIL_SERVER_HOST));
         }
         if (settings.getAttribute(MailService.MAIL_SENDER) != null) {
             message.setSender(settings.getAttribute(MailService.MAIL_SENDER));
         }
 
         StringBuffer body = new StringBuffer();
 
         body.append(getLocalizedText(req, "LOGIN_FORGOT_MAIL") + "\n\n");
 
         PortletURI uri = res.createURI();
         uri.addAction("newpassword");
         uri.addParameter("reqid", request.getOid());
 
         body.append(uri.toString());
         message.setBody(body.toString());
 
         try {
             mailService.sendMail(message);
         } catch (MessagingException e) {
             log.error("Unable to send mail message!", e);
             createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
             return;
         }
         createSuccessMessage(evt, this.getLocalizedText(req, "LOGIN_SUCCESS_MAIL"));
 
     }
 
     private void createErrorMessage(FormEvent evt, String text) {
         MessageBoxBean msg = evt.getMessageBoxBean("msg");
         msg.setValue(text);
         msg.setMessageType(MessageStyle.MSG_ERROR);
     }
 
     private void createSuccessMessage(FormEvent evt, String text) {
         MessageBoxBean msg = evt.getMessageBoxBean("msg");
         msg.setValue(text);
         msg.setMessageType(MessageStyle.MSG_SUCCESS);
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
 
             // Otherwise, password must match confirmation
             if (!passwordValue.equals(confirmPasswordValue)) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_MISMATCH"));
                 // If they do match, then validate password with our service
             } else if (passwordValue == null) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_NOTSET"));
             } else if (passwordValue.length() == 0) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_BLANK"));
             } else if (passwordValue.length() < 5) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_TOOSHORT"));
             } else {
                 // save password
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
