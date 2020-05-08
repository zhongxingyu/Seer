 /*
  * @author <a href="mailto:michael.russell@aei.mpg.de">Michael Russell</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.admin.users;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.provider.portletui.model.DefaultTableModel;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigService;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigSettings;
 import org.gridlab.gridsphere.services.core.security.password.PasswordEditor;
 import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;
 import org.gridlab.gridsphere.services.core.security.role.RoleManagerService;
 import org.gridlab.gridsphere.services.core.security.group.GroupManagerService;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 import org.gridlab.gridsphere.portlets.core.login.LoginPortlet;
 
 import javax.servlet.UnavailableException;
 import java.util.Iterator;
 import java.util.List;
 
 public class UserManagerPortlet extends ActionPortlet {
 
     // JSP pages used by this portlet
     public static final String DO_VIEW_USER_LIST = "admin/users/doViewUserList.jsp";
     public static final String DO_VIEW_USER_VIEW = "admin/users/doViewUserView.jsp";
     public static final String DO_VIEW_USER_EDIT = "admin/users/doViewUserEdit.jsp";
 
     // Portlet services
     private UserManagerService userManagerService = null;
     private PasswordManagerService passwordManagerService = null;
     private GroupManagerService groupManagerService = null;
     private RoleManagerService roleManagerService = null;
     private PortalConfigService portalConfigService = null;
 
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         log.debug("Entering initServices()");
         try {
             this.userManagerService = (UserManagerService) config.getContext().getService(UserManagerService.class);
             this.groupManagerService = (GroupManagerService) config.getContext().getService(GroupManagerService.class);
             this.roleManagerService = (RoleManagerService) config.getContext().getService(RoleManagerService.class);
             this.passwordManagerService = (PasswordManagerService) config.getContext().getService(PasswordManagerService.class);
             this.portalConfigService = (PortalConfigService) getPortletConfig().getContext().getService(PortalConfigService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to initialize services!", e);
         }
         log.debug("Exiting initServices()");
         DEFAULT_HELP_PAGE = "admin/users/help.jsp";
         DEFAULT_VIEW_PAGE = "doListUsers";
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
     }
 
     public void doListUsers(FormEvent evt)
             throws PortletException {
         PortletRequest req = evt.getPortletRequest();
         List userList = this.userManagerService.getUsers();
         req.setAttribute("userList", userList);
         setNextState(req, DO_VIEW_USER_LIST);
     }
 
     public void doViewUser(FormEvent evt)
             throws PortletException {
         PortletRequest req = evt.getPortletRequest();
 
         String userID = evt.getAction().getParameter("userID");
         User user = this.userManagerService.getUser(userID);
         if (user != null) {
             // should check for non-null user !
             req.setAttribute("user", user);
             HiddenFieldBean hf = evt.getHiddenFieldBean("userID");
             hf.setValue(user.getID());
 
             List userRoles = roleManagerService.getRolesForUser(user);
             Iterator it = userRoles.iterator();
             String userRole = "";
             while (it.hasNext()) {
                 userRole += ((PortletRole)it.next()).getName() + ", ";
             }
             req.setAttribute("role", userRole.substring(0, userRole.length() - 2));
             CheckBoxBean accountCB = evt.getCheckBoxBean("accountCB");
             String disabled = (String)user.getAttribute(User.DISABLED);
             if ((disabled != null) && ("TRUE".equalsIgnoreCase(disabled))) {
                 accountCB.setSelected(true);
             }
             accountCB.setDisabled(true);
             setNextState(req, DO_VIEW_USER_VIEW);
         } else {
             setNextState(req, DEFAULT_VIEW_PAGE);
         }
     }
 
     public void doNewUser(FormEvent evt)
             throws PortletException {
 
         PortletRequest req = evt.getPortletRequest();
 
         // indicate to edit JSP this is a new user
         HiddenFieldBean hf = evt.getHiddenFieldBean("newuser");
         hf.setValue("true");
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(LoginPortlet.SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         //setSelectedUserRole(evt, PortletRole.USER);
         FrameBean roleFrame = evt.getFrameBean("roleFrame");
 
         DefaultTableModel model = new DefaultTableModel();
 
         TableRowBean tr = new TableRowBean();
         tr.setHeader(true);
         TableCellBean tc = new TableCellBean();
         TextBean text = new TextBean();
         text.setValue("Select Roles");
         tc.addBean(text);
         tr.addBean(tc);
         tc = new TableCellBean();
         text = new TextBean();
         text.setValue("Role name");
         tc.addBean(text);
         tr.addBean(tc);
 
         model.addTableRowBean(tr);
 
         List roles = roleManagerService.getRoles();
         Iterator it = roles.iterator();
         while (it.hasNext()) {
             PortletRole role = (PortletRole)it.next();
             tr = new TableRowBean();
             tc = new TableCellBean();
             CheckBoxBean cb = new CheckBoxBean();
             cb.setBeanId(role.getName() + "CB");
             tc.addBean(cb);
             tr.addBean(tc);
             tc = new TableCellBean();
             text = new TextBean();
             text.setValue(role.getName());
             tc.addBean(text);
             tr.addBean(tc);
             model.addTableRowBean(tr);
         }
 
         roleFrame.setTableModel(model);
 
         setNextState(req, DO_VIEW_USER_EDIT);
         log.debug("in doViewNewUser");
     }
 
     public void doEditUser(FormEvent evt)
             throws PortletException {
 
         PortletRequest req = evt.getPortletRequest();
 
         // indicate to edit JSP this is an existing user
         HiddenFieldBean newuserHF = evt.getHiddenFieldBean("newuser");
         newuserHF.setValue("false");
 
         // load in User values
         HiddenFieldBean userHF = evt.getHiddenFieldBean("userID");
         String userID = userHF.getValue();
 
         // get user
         User user = this.userManagerService.getUser(userID);
         if (user == null) doNewUser(evt);
 
         FrameBean roleFrame = evt.getFrameBean("roleFrame");
 
         DefaultTableModel model = new DefaultTableModel();
 
         TableRowBean tr = new TableRowBean();
         tr.setHeader(true);
         TableCellBean tc = new TableCellBean();
         TextBean text = new TextBean();
         text.setValue("Select Roles");
         tc.addBean(text);
         tr.addBean(tc);
         tc = new TableCellBean();
         text = new TextBean();
         text.setValue("Role name");
         tc.addBean(text);
         tr.addBean(tc);
 
         model.addTableRowBean(tr);
 
         List roles = roleManagerService.getRoles();
         List myroles = roleManagerService.getRolesForUser(user);
         Iterator it = roles.iterator();
         while (it.hasNext()) {
             PortletRole role = (PortletRole)it.next();
             tr = new TableRowBean();
             tc = new TableCellBean();
             CheckBoxBean cb = new CheckBoxBean();
             if (myroles.contains(role)) {
                 cb.setSelected(true);
             }
             cb.setBeanId(role.getName() + "CB");
             tc.addBean(cb);
             tr.addBean(tc);
             tc = new TableCellBean();
             text = new TextBean();
             text.setValue(role.getName());
             tc.addBean(text);
             tr.addBean(tc);
             model.addTableRowBean(tr);
         }
 
         roleFrame.setTableModel(model);
 
         setUserValues(evt, user);
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(LoginPortlet.SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         if (settings.getAttribute(LoginPortlet.SUPPORT_X509_AUTH).equals(Boolean.TRUE.toString())) {
             req.setAttribute("certSupport", "true");
         }
 
         CheckBoxBean accountCB = evt.getCheckBoxBean("accountCB");
         String disabled = (String)user.getAttribute(User.DISABLED);
         if ((disabled != null) && ("TRUE".equalsIgnoreCase(disabled))) {
             accountCB.setSelected(true);
         }
 
         setNextState(req, DO_VIEW_USER_EDIT);
     }
 
     public void doConfirmEditUser(FormEvent evt)
             throws PortletException {
 
         PortletRequest req = evt.getPortletRequest();
         //User user = loadUser(evt);
         try {
             User user;
             HiddenFieldBean hf = evt.getHiddenFieldBean("newuser");
             String newuser = hf.getValue();
             log.debug("in doConfirmEditUser: " + newuser);
             if (newuser.equals("true")) {
                 validateUser(evt, true);
                 user = saveUser(evt, null);
                 HiddenFieldBean userHF = evt.getHiddenFieldBean("userID");
                 userHF.setValue(user.getID());
                 createSuccessMessage(evt, this.getLocalizedText(req, "USER_NEW_SUCCESS"));
             } else {
                 validateUser(evt, false);
                 // load in User values
                 HiddenFieldBean userHF = evt.getHiddenFieldBean("userID");
                 String userID = userHF.getValue();
                 User thisuser = this.userManagerService.getUser(userID);
                 user = saveUser(evt, thisuser);
                 createSuccessMessage(evt, this.getLocalizedText(req, "USER_EDIT_SUCCESS"));
             }
             req.setAttribute("user", user);
 
             setNextState(req, "doListUsers");
         } catch (PortletException e) {
             createErrorMessage(evt, e.getMessage());
             PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
             if (settings.getAttribute(LoginPortlet.SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
                 req.setAttribute("savePass", "true");
             }
             setNextState(req, DO_VIEW_USER_EDIT);
         }
     }
 
     public void doDeleteUser(FormEvent evt)
             throws PortletException {
 
         PortletRequest req = evt.getPortletRequest();
         HiddenFieldBean hf = evt.getHiddenFieldBean("userID");
         String userId = hf.getValue();
         User user = this.userManagerService.getUser(userId);
         if (user != null) {
             req.setAttribute("user", user);
             this.userManagerService.deleteUser(user);
             this.passwordManagerService.deletePassword(user);
             List groups = this.groupManagerService.getGroups(user);
             Iterator it = groups.iterator();
             while (it.hasNext()) {
                 PortletGroup group = (PortletGroup)it.next();
                 this.groupManagerService.deleteUserInGroup(user, group);
             }
             createSuccessMessage(evt, this.getLocalizedText(req, "USER_DELETE_SUCCESS"));
         }
         setNextState(req, "doListUsers");
     }
 
 
     private void setUserValues(FormEvent event, User user) {
         event.getTextFieldBean("userName").setValue(user.getUserName());
         event.getTextFieldBean("familyName").setValue(user.getFamilyName());
         event.getTextFieldBean("givenName").setValue(user.getGivenName());
         event.getTextFieldBean("fullName").setValue(user.getFullName());
         event.getTextFieldBean("emailAddress").setValue(user.getEmailAddress());
         event.getTextFieldBean("organization").setValue(user.getOrganization());
         event.getPasswordBean("password").setValue("");
         event.getTextFieldBean("certificate").setValue((String)user.getAttribute("user.certificate"));
     }
 
     private void validateUser(FormEvent event, boolean newuser)
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
         } else if (newuser) {
             if (this.userManagerService.existsUserName(userName)) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_EXISTS") + "<br />");
                 isInvalid = true;
             }
         }
 
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
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(LoginPortlet.SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             if (isInvalidPassword(event, newuser)){
                 isInvalid = true;
             }
         }
         // Throw exception if error was found
         if (isInvalid) {
             throw new PortletException(message.toString());
         }
         log.debug("Exiting validateUser()");
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
 
     private User saveUser(FormEvent event, User user) {
         log.debug("Entering saveUser()");
         // Account request
 
         boolean newuserflag = false;
         // Create edit account request
         if (user == null) {
             user = this.userManagerService.createUser();
             newuserflag = true;
         }
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(LoginPortlet.SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             PasswordEditor editor = passwordManagerService.editPassword(user);
             String password = event.getPasswordBean("password").getValue();
             boolean isgood = this.isInvalidPassword(event, newuserflag);
             if (isgood) {
                 setNextState(event.getPortletRequest(), DO_VIEW_USER_EDIT);
                 return user;
             } else {
                 if (!password.equals("")) {
                     editor.setValue(password);
                     passwordManagerService.savePassword(editor);
                 }
             }
         }
 
         // Edit account attributes
         editAccountRequest(event, user);
 
         // Submit changes
         this.userManagerService.saveUser(user);
 
 
         // Save user role
         saveUserRole(event, user);
         log.debug("Exiting saveUser()");
         return user;
     }
 
     private void editAccountRequest(FormEvent event, User accountRequest) {
         log.debug("Entering editAccountRequest()");
         accountRequest.setUserName(event.getTextFieldBean("userName").getValue());
         //accountRequest.setFamilyName(event.getTextFieldBean("familyName").getValue());
         //accountRequest.setGivenName(event.getTextFieldBean("givenName").getValue());
         accountRequest.setFullName(event.getTextFieldBean("fullName").getValue());
         accountRequest.setEmailAddress(event.getTextFieldBean("emailAddress").getValue());
         accountRequest.setOrganization(event.getTextFieldBean("organization").getValue());
         if (event.getCheckBoxBean("accountCB").isSelected()) {
             accountRequest.setAttribute(User.DISABLED, "true");
         } else {
             accountRequest.setAttribute(User.DISABLED, "false");
         }
         String certval = event.getTextFieldBean("certificate").getValue();
        if (!certval.equals("")) accountRequest.setAttribute("user.certificate", certval);
     }
 
     private void saveUserRole(FormEvent event, User user) {
         log.debug("Entering saveUserRole()");
 
         List roles = roleManagerService.getRoles();
         Iterator it = roles.iterator();
         while (it.hasNext()) {
             PortletRole role = (PortletRole)it.next();
             CheckBoxBean cb = event.getCheckBoxBean(role.getName() + "CB");
             if (cb.isSelected()) roleManagerService.addUserToRole(user, role);
         }
         groupManagerService.addUserToGroup(user, groupManagerService.getCoreGroup());
 
 
         log.debug("Exiting saveUserRole()");
     }
 
     private void createErrorMessage(FormEvent event, String msg) {
         MessageBoxBean msgBox = event.getMessageBoxBean("msg");
         msgBox.setMessageType(MessageStyle.MSG_ERROR);
         String msgOld = msgBox.getValue();
         msgBox.setValue((msgOld!=null?msgOld:"")+msg);
     }
 
     private void createSuccessMessage(FormEvent event, String msg) {
         MessageBoxBean msgBox = event.getMessageBoxBean("msg");
         msgBox.setMessageType(MessageStyle.MSG_SUCCESS);
         msgBox.setValue(msg);
     }
 
 
 }
