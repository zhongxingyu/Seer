 /*
  * @author <a href="mailto:michael.russell@aei.mpg.de">Michael Russell</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.admin.setup;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.MessageBoxBean;
 import org.gridlab.gridsphere.provider.portletui.beans.MessageStyle;
 import org.gridlab.gridsphere.services.core.security.password.PasswordEditor;
 import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;
 import org.gridlab.gridsphere.services.core.security.role.RoleManagerService;
 import org.gridlab.gridsphere.services.core.security.group.GroupManagerService;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 import org.gridlab.gridsphere.layout.PortletPageFactory;
 
 import javax.servlet.UnavailableException;
 
 public class GridSphereSetupPortlet extends ActionPortlet {
 
     // Portlet services
     private UserManagerService userManagerService = null;
     private PasswordManagerService passwordManagerService = null;
     private RoleManagerService roleManagerService = null;
     private GroupManagerService groupManagerService = null;
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         log.debug("Entering initServices()");
         try {
             this.userManagerService = (UserManagerService) config.getContext().getService(UserManagerService.class);
             this.roleManagerService = (RoleManagerService) config.getContext().getService(RoleManagerService.class);
             this.groupManagerService = (GroupManagerService) config.getContext().getService(GroupManagerService.class);
             this.passwordManagerService = (PasswordManagerService) config.getContext().getService(PasswordManagerService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to get service instance!", e);
         }
         log.debug("Exiting initServices()");
         DEFAULT_HELP_PAGE = "admin/setup/help.jsp";
         DEFAULT_VIEW_PAGE = "admin/setup/doView.jsp";
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
     }
 
     private void validateUser(FormEvent event, boolean newuser)
             throws PortletException {
         log.debug("Entering validateUser()");
         PortletRequest req = event.getPortletRequest();
 
         boolean isInvalid = false;
         // Validate user name
         String userName = event.getTextFieldBean("userName").getValue();
         if (userName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NAME_BLANK") + "<br />");
             isInvalid = true;
         } else if (newuser) {
             if (this.userManagerService.existsUserName(userName)) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_EXISTS") + "<br />");
                 isInvalid = true;
             }
         }
 
         // Validate family name
         /*
         String familyName = event.getTextFieldBean("familyName").getValue();
         if (familyName.equals("")) {
             message.append(this.getLocalizedText(req, "USER_FAMILYNAME_BLANK") + "<br />");
             isInvalid = true;
         }
         */
         // Validate given name
         String givenName = event.getTextFieldBean("fullName").getValue();
 
         if (givenName.equals("")) {
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
 
         // Throw exception if error was found
         if (isInvalid) {
             throw new PortletException();
         }
         log.debug("Exiting validateUser()");
     }
 
     public void doSavePortalAdmin(FormEvent event) {
         try {
             validateUser(event, true);
         } catch (PortletException e) {
             return;
         }
         String password = event.getPasswordBean("password").getValue();
         boolean isbad = this.isInvalidPassword(event, true);
         if (isbad) {
             return;
         }
 
         User  accountRequest = this.userManagerService.createUser();
         accountRequest.setUserName(event.getTextFieldBean("userName").getValue());
         accountRequest.setFullName(event.getTextFieldBean("fullName").getValue());
         accountRequest.setEmailAddress(event.getTextFieldBean("emailAddress").getValue());
         accountRequest.setOrganization(event.getTextFieldBean("organization").getValue());
         PasswordEditor editor = passwordManagerService.editPassword(accountRequest);
         editor.setValue(password);
         passwordManagerService.savePassword(editor);
         userManagerService.saveUser(accountRequest);
 
         log.info("Granting super role to root user.");
         roleManagerService.addUserToRole(accountRequest, PortletRole.SUPER);
         roleManagerService.addUserToRole(accountRequest, PortletRole.ADMIN);
         roleManagerService.addUserToRole(accountRequest, PortletRole.USER);
         groupManagerService.addUserToGroup(accountRequest, groupManagerService.getCoreGroup());
        PortletPageFactory.setSetupNeeded(false);
         event.getPortletRequest().removeAttribute(PortletPageFactory.PAGE);
     }
 
     private boolean isInvalidPassword(FormEvent event, boolean newuser) {
         // Validate password
         PortletRequest req = event.getPortletRequest();
         String passwordValue = event.getPasswordBean("password").getValue();
         String confirmPasswordValue = event.getPasswordBean("confirmPassword").getValue();
 
         // If user already exists and password unchanged, no problem
         if (passwordValue.length() == 0 &&
                 confirmPasswordValue.length() == 0) {
             if (newuser) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_BLANK") + "<br />");
                 return true;
             }
             return false;
         }
         // Otherwise, password must match confirmation
         if (!passwordValue.equals(confirmPasswordValue)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_MISMATCH") + "<br />");
             return true;
             // If they do match, then validate password with our service
         } else {
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
 
     private void createErrorMessage(FormEvent event, String msg) {
            MessageBoxBean msgBox = event.getMessageBoxBean("msg");
            msgBox.setMessageType(MessageStyle.MSG_ERROR);
            String msgOld = msgBox.getValue();
            msgBox.setValue((msgOld!=null?msgOld:"")+msg);
     }
 
 }
