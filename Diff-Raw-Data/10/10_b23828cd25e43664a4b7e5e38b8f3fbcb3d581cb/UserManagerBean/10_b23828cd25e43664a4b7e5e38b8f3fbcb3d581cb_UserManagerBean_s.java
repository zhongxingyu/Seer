 /*
  * @author <a href="mailto:michael.russell@aei.mpg.de">Michael Russell</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.user;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletGroup;
 import org.gridlab.gridsphere.provider.ActionEventHandler;
 import org.gridlab.gridsphere.provider.ui.beans.*;
 import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
 import org.gridlab.gridsphere.services.core.security.acl.GroupRequest;
 import org.gridlab.gridsphere.services.core.security.password.InvalidPasswordException;
 import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;
 import org.gridlab.gridsphere.services.core.user.AccountRequest;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 public class UserManagerBean extends ActionEventHandler {
 
     // JSP pages used by this portlet
     public static final String DO_VIEW_USER_LIST = "/jsp/usermanager/doViewUserList.jsp";
     public static final String DO_VIEW_USER_VIEW = "/jsp/usermanager/doViewUserView.jsp";
     public static final String DO_CONFIG_USER_LIST = "/jsp/usermanager/doConfigureUserList.jsp";
     public static final String DO_CONFIG_USER_VIEW = "/jsp/usermanager/doConfigureUserView.jsp";
     public static final String DO_CONFIG_USER_EDIT = "/jsp/usermanager/doConfigureUserEdit.jsp";
     public static final String DO_CONFIG_USER_DELETE = "/jsp/usermanager/doConfigureUserDelete.jsp";
     public static final String DO_CONFIG_USER_DELETE_CONFIRM = "/jsp/usermanager/doConfigureUserDeleteConfirm.jsp";
 
     // Portlet services
     private UserManagerService userManagerService = null;
     private PasswordManagerService passwordManagerService = null;
     private AccessControlManagerService aclManagerService = null;
 
     // User variables
     private List userList = null;
     private User user = null;
     private PortletRole userRole = PortletRole.USER;
     // User id always in page
     private HiddenFieldBean userIDBean = null;
     // User view
     private TextBean userIDViewBean = null;
     private TextBean userNameBean = null;
     private TextBean familyNameBean = null;
     private TextBean givenNameBean = null;
     private TextBean fullNameBean = null;
     private TextBean emailAddressBean = null;
     private TextBean organizationBean = null;
     private TextBean userRoleBean = null;
     // User edit
     private TextFieldBean userNameEditBean = null;
     private TextFieldBean familyNameEditBean = null;
     private TextFieldBean givenNameEditBean = null;
     private TextFieldBean fullNameEditBean = null;
     private TextFieldBean emailAddressEditBean = null;
     private TextFieldBean organizationEditBean = null;
     private ListBoxBean userRoleEditBean = null;
     private PasswordBean passwordEditBean = null;
     private PasswordBean confirmPasswordEditBean = null;
 
     public UserManagerBean() {
     }
 
     public void init(PortletConfig config, PortletRequest request, PortletResponse response)
             throws PortletException {
         super.init(config, request, response);
         initServices();
     }
 
     private void initServices()
             throws PortletException {
         this.log.debug("Entering initServices()");
         this.userManagerService = (UserManagerService)getPortletService(UserManagerService.class);
         this.aclManagerService = (AccessControlManagerService)getPortletService(AccessControlManagerService.class);
         this.passwordManagerService = (PasswordManagerService)getPortletService(PasswordManagerService.class);
         this.log.debug("Exiting initServices()");
     }
 
     public void doView()
             throws PortletException {
         doViewListUser();
     }
 
     public void doViewListUser()
             throws PortletException {
         listUser();
         setTitle("User Accounts [List Users]");
         setPage(DO_VIEW_USER_LIST);
     }
 
     public void doViewViewUser()
             throws PortletException {
         loadUser();
         viewUser();
         setTitle("User Accounts [View User]");
         setPage(DO_VIEW_USER_VIEW);
     }
 
     public void doConfigure()
             throws PortletException {
         doConfigureListUser();
     }
 
     public void doConfigureListUser()
             throws PortletException {
         listUser();
         setTitle("User Accounts [List Users]");
         setPage(DO_CONFIG_USER_LIST);
     }
 
     public void doConfigureViewUser()
             throws PortletException {
         loadUser();
         viewUser();
         setTitle("User Accounts [View User]");
         setPage(DO_CONFIG_USER_VIEW);
     }
 
     public void doConfigureNewUser()
             throws PortletException {
         editUser();
         setTitle("User Accounts [New User]");
         setPage(DO_CONFIG_USER_EDIT);
     }
 
     public void doConfigureEditUser()
             throws PortletException {
         loadUser();
         editUser();
         setTitle("User Accounts [Edit User]");
         setPage(DO_CONFIG_USER_EDIT);
     }
 
     public void doConfigureConfirmEditUser()
             throws PortletException {
         loadUser();
         updateUser();
         try {
             validateUser();
             saveUser();
         } catch (PortletException e) {
             storeUserEdit();
             setIsFormInvalid(true);
             setFormInvalidMessage(e.getMessage());
             setTitle("User Accounts [Edit User]");
             setPage(DO_CONFIG_USER_EDIT);
             return;
         }
         viewUser();
         setTitle("User Accounts [View User]");
         setPage(DO_CONFIG_USER_VIEW);
     }
 
     public void doConfigureCancelEditUser()
             throws PortletException {
         doConfigureListUser();
     }
 
     public void doConfigureDeleteUser()
             throws PortletException {
         loadUser();
         viewUser();
         setTitle("User Accounts [Delete User]");
         setPage(DO_CONFIG_USER_DELETE);
     }
 
     public void doConfigureConfirmDeleteUser()
             throws PortletException {
         loadUser();
         viewUser();
         deleteUser();
         setTitle("User Accounts [Deleted User]");
         setPage(DO_CONFIG_USER_DELETE_CONFIRM);
     }
 
     public void doConfigureCancelDeleteUser()
             throws PortletException {
         doConfigureListUser();
     }
 
     private User getUser() {
         return this.user;
     }
 
     private User listUser() {
         log.debug("Entering listUser()");
         this.userList = this.userManagerService.getUsers();
         TableBean tableBean = null;
         if (this.userList.size() == 0) {
             String message = "No user accounts in database";
             tableBean = createTableBeanWithMessage(message);
         } else {
             // View action depends on which mode we're in...
             String viewActionName = null;
             if (getPortletMode().equals(Portlet.Mode.CONFIGURE)) {
                 // If in config mode, then want config mode view user
                 viewActionName = "doConfigureViewUser";
             } else {
                 // Otherwise want view mode view user
                 viewActionName = "doViewViewUser";
             }
 
             // Create headers
             List headers = new Vector();
             headers.add("User Name");
             headers.add("Full Name");
             headers.add("Email Address");
             headers.add("Organization");
             // Create table with headers
             tableBean = createTableBeanWithHeaders(headers);
             // Add rows to table
             Iterator userIterator = this.userList.iterator();
             while (userIterator.hasNext()) {
                 // Get next user
                 User user = (User)userIterator.next();
                 // Create new table row
                 TableRowBean rowBean = new TableRowBean();
                 // User name with user id action link
                ActionLinkBean userIDLinkBean
                        = new ActionLinkBean(createPortletURI(), viewActionName, user.getID());
                userIDLinkBean.addParamBean("userID", user.getUserName());
                TableCellBean cellBean = createTableCellBean(userIDLinkBean);
                 rowBean.add(cellBean);
                 // Full name
                 TextBean fullNameBean = new TextBean(user.getFullName());
                 cellBean = createTableCellBean(fullNameBean);
                 rowBean.add(cellBean);
                 // Email address
                 TextBean emailAddressBean = new TextBean(user.getEmailAddress());
                 cellBean = createTableCellBean(emailAddressBean);
                 rowBean.add(cellBean);
                 // Organization
                 TextBean organizationBean = new TextBean(user.getOrganization());
                 cellBean = createTableCellBean(organizationBean);
                 rowBean.add(cellBean);
                 // Add row to table
                 tableBean.add(rowBean);
             }
         }
         // All done
         tableBean.store("userList", request);
         log.debug("Exiting listUser()");
         return user;
     }
 
     private User loadUser() {
         log.debug("Entering loadUser()");
         String userID = getActionPerformedParameter("userID");
         this.user = this.userManagerService.getUser(userID);
         if (this.user != null) {
             //this.password = this.passwordManagerService.getValue(this.user);
             this.userRole = this.aclManagerService.getRoleInGroup(this.user, SportletGroup.CORE);
         }
         log.debug("Exiting loadUser()");
         return user;
     }
 
     private void viewUser() {
         log.debug("Entering viewUser()");
 
         if (this.user == null) {
 
             // Clear user attributes
             this.userIDBean = new HiddenFieldBean("userID", "");
             this.userNameBean = new TextBean("");
             this.familyNameBean = new TextBean("");
             this.givenNameBean = new TextBean("");
             this.fullNameBean = new TextBean("");
             this.emailAddressBean = new TextBean("");
             this.organizationBean = new TextBean("");
             this.userRoleBean = new TextBean("");
 
         } else {
 
             // Set attributes
             this.userIDBean = new HiddenFieldBean("userID", this.user.getID());
             this.userNameBean = new TextBean(this.user.getUserName());
             this.familyNameBean = new TextBean(this.user.getFamilyName());
             this.givenNameBean = new TextBean(this.user.getGivenName());
             this.fullNameBean = new TextBean(this.user.getFullName());
             this.emailAddressBean = new TextBean(this.user.getEmailAddress());
             this.organizationBean = new TextBean(this.user.getOrganization());
             this.userRoleBean = new TextBean(this.userRole.toString());
         }
         // Store beans
         storeUserView();
 
         log.debug("Exiting viewUser()");
     }
 
     private void storeUserView() {
         // Set user attributes
         PortletRequest portletRequest = getPortletRequest();
 
         // Store beans
         this.userIDBean.store("userID", portletRequest);
         this.userNameBean.store("userName", portletRequest);
         this.familyNameBean.store("familyName", portletRequest);
         this.givenNameBean.store("givenName", portletRequest);
         this.fullNameBean.store("fullName", portletRequest);
         this.emailAddressBean.store("emailAddress", portletRequest);
         this.organizationBean.store("organization", portletRequest);
         this.userRoleBean.store("userRole", portletRequest);
     }
 
     private void editUser() {
         log.debug("Entering editUser()");
         // Set user attributes
         PortletRequest portletRequest = getPortletRequest();
 
         PortletRole selectedRole = null;
 
         if (this.user == null) {
             log.debug("Editing new user");
             this.userIDBean = new HiddenFieldBean("userID", "");
             this.userNameEditBean = new TextFieldBean("userName", "");
             this.familyNameEditBean = new TextFieldBean("familyName", "");
             this.givenNameEditBean = new TextFieldBean("givenName", "");
             this.fullNameEditBean = new TextFieldBean("fullName", "");
             this.emailAddressEditBean = new TextFieldBean("emailAddress", "");
             this.organizationEditBean = new TextFieldBean("organization", "");
             selectedRole = PortletRole.USER;
         } else {
             log.debug("Editing existing user");
             this.userIDBean = new HiddenFieldBean("userID", this.user.getID());
             this.userNameEditBean = new TextFieldBean("userName", this.user.getUserName());
             this.userNameEditBean.setDisabled(true);
             this.userNameEditBean.setReadonly(true);
             this.familyNameEditBean = new TextFieldBean("familyName", this.user.getFamilyName());
             this.givenNameEditBean = new TextFieldBean("givenName", this.user.getGivenName());
             this.fullNameEditBean = new TextFieldBean("fullName", this.user.getFullName());
             this.emailAddressEditBean = new TextFieldBean("emailAddress", this.user.getEmailAddress());
             this.organizationEditBean = new TextFieldBean("organization", this.user.getOrganization());
             selectedRole = this.userRole;
         }
 
         // Set user role here, it's value is set only if
         this.userRoleEditBean = createUserRoleEditBean(selectedRole);
 
         // Password always blank first page
         this.passwordEditBean = new PasswordBean("password", "", false, false, 20, 16);
         // Confirm always blank first page
         this.confirmPasswordEditBean = new PasswordBean("confirmPassword", "", false, false, 20, 16);
 
         // Store beans
         storeUserEdit();
 
         log.debug("Exiting editUser()");
     }
 
     private ListBoxBean createUserRoleEditBean(PortletRole selectedRole) {
         ListBoxBean userRoles = new ListBoxBean("userRole");
         userRoles.setMultiple(false);
         userRoles.setSize(1);
         // Super role
         if (selectedRole.isSuper()) {
             this.log.debug("Setting super role");
             userRoles.add("Super Role", "SUPER", true);
         } else {
             userRoles.add("Super Role", "SUPER", false);
         }
         // Administrative role
         if (selectedRole.isAdmin()) {
             this.log.debug("Setting admin role");
             userRoles.add("Administrative Role", "ADMIN", true);
         } else {
             userRoles.add("Administrative Role", "ADMIN", false);
         }
         // User role
         if (selectedRole.isUser()) {
             this.log.debug("Setting user role");
             userRoles.add("User Role", "USER", true);
         } else {
             userRoles.add("User Role", "USER", false);
         }
         // Guest role
         if (selectedRole.isGuest()) {
             this.log.debug("Setting guest role");
             userRoles.add("Guest Role", "GUEST", true);
         } else {
             userRoles.add("Guest Role", "GUEST", false);
         }
         return userRoles;
     }
 
     private void updateUser() {
         log.debug("Entering updateUser()");
         // Retrieve user attributes from form
         this.userIDBean = getHiddenFieldBean("userID");
         this.userNameEditBean = getTextFieldBean("userName");
         this.familyNameEditBean = getTextFieldBean("familyName");
         this.givenNameEditBean = getTextFieldBean("givenName");
         this.fullNameEditBean = getTextFieldBean("fullName");
         this.emailAddressEditBean = getTextFieldBean("emailAddress");
         this.organizationEditBean = getTextFieldBean("organization");
         this.userRoleEditBean = getListBoxBean("userRole");
         this.passwordEditBean = getPasswordBean("password");
         this.confirmPasswordEditBean = getPasswordBean("confirmPassword");
         log.debug("Exiting updateUser()");
     }
 
     private void validateUser()
             throws PortletException {
         log.debug("Entering validateUser()");
         StringBuffer message = new StringBuffer();
         boolean isInvalid = false;
         // Validate user name
         String userName = this.userNameEditBean.getValue();
         if (userName.equals("")) {
             message.append("User name cannot be blank\n");
             isInvalid = true;
         } else if (this.userManagerService.existsUserName(userName)) {
             message.append("A user already exists with the same user name, please use a different name.\n");
             isInvalid = true;
         }
         // Validate family name
         String familyName = this.familyNameEditBean.getValue();
         if (familyName.equals("")) {
             message.append("Family name cannot be blank\n");
             isInvalid = true;
         }
         // Validate given name
         String givenName = this.givenNameEditBean.getValue();
         if (givenName.equals("")) {
             message.append("Given name cannot be blank\n");
             isInvalid = true;
         }
 
         isInvalid = isInvalidPassword(message);
         // Throw exception if error was found
         if (isInvalid) {
             throw new PortletException(message.toString());
         }
         log.debug("Exiting validateUser()");
     }
 
     private boolean isInvalidPassword(StringBuffer message) {
         // Validate password
         String passwordValue = this.passwordEditBean.getValue();
         String confirmPasswordValue = this.confirmPasswordEditBean.getValue();
         // If user is new and no password provided, error
         if (this.user == null) {
             if (passwordValue.length() == 0) {
                 message.append("All new users must have a password.\n");
                 return true;
             }
         // If user already exists and password unchanged, no problem
         } else if (passwordValue.length() == 0 &&
                    confirmPasswordValue.length() == 0) {
             return false;
         }
         // Otherwise, password must match confirmation
         if (!passwordValue.equals(confirmPasswordValue)) {
             message.append("Password must match confirmation\n");
             return true;
         // If they do match, then validate password with our service
         } else {
             try {
                 this.passwordManagerService.validatePassword(passwordValue);
             } catch (InvalidPasswordException e) {
                 message.append(e.getMessage());
                 return true;
             }
         }
         return false;
     }
 
     private void storeUserEdit() {
         // Set user attributes
         PortletRequest portletRequest = getPortletRequest();
         // Store the beans
         this.userIDBean.store("userID", portletRequest);
         this.userNameEditBean.store("userName", portletRequest);
         this.familyNameEditBean.store("familyName", portletRequest);
         this.givenNameEditBean.store("givenName", portletRequest);
         this.fullNameEditBean.store("fullName", portletRequest);
         this.emailAddressEditBean.store("emailAddress", portletRequest);
         this.organizationEditBean.store("organization", portletRequest);
         this.userRoleEditBean.store("userRole", portletRequest);
         this.log.debug("User role " + this.userRoleEditBean.toString());
         this.passwordEditBean.store("password", portletRequest);
         this.confirmPasswordEditBean.store("confirmPassword", portletRequest);
    }
 
     private void saveUser()
             throws PortletException {
         log.debug("Entering saveUser()");
         // Account request
         AccountRequest accountRequest = null;
         // If user is new
         if (this.user == null) {
             log.debug("Creating new user");
             // Create new account request
             accountRequest = this.userManagerService.createAccountRequest();
             accountRequest.setUserName(this.userNameEditBean.getValue());
         } else {
             log.debug("Updating existing user");
             // Create edit account request
             accountRequest = this.userManagerService.createAccountRequest(user);
         }
         // Edit account attributes
         editAccountRequest(accountRequest);
         // Submit changes
         this.userManagerService.submitAccountRequest(accountRequest);
         this.user = this.userManagerService.approveAccountRequest(accountRequest);
         // Save user role
         saveUserRole();
         log.debug("Exiting saveUser()");
     }
 
     private void editAccountRequest(AccountRequest accountRequest) {
         log.debug("Entering editAccountRequest()");
         accountRequest.setFamilyName(this.familyNameEditBean.getValue());
         accountRequest.setGivenName(this.givenNameEditBean.getValue());
         accountRequest.setFullName(this.fullNameEditBean.getValue());
         accountRequest.setEmailAddress(this.emailAddressEditBean.getValue());
         accountRequest.setOrganization(this.organizationEditBean.getValue());
         String passwordValue = this.passwordEditBean.getValue();
         // Save password parameters if password was altered
         if (passwordValue.length() > 0) {
             accountRequest.setPasswordValue(passwordValue);
         }
         log.debug("Exiting editAccountRequest()");
     }
 
     private void saveUserRole()
             throws PortletException {
         log.debug("Entering saveUserRole()");
 
         // Get selected role
         PortletRole selectedRole = getSelectedUserRole();
 
         // If super role was chosen
         if (selectedRole.equals(PortletRole.SUPER)) {
             this.log.debug("Granting super role");
             // Grant super role
             this.aclManagerService.grantSuperRole(user);
         } else {
             // Revoke super role (in case they had it)
             this.aclManagerService.revokeSuperRole(user);
             // Create appropriate access request
             GroupRequest accessRequest = this.aclManagerService.createGroupRequest();
             accessRequest.setUser(this.user);
             accessRequest.setGroup(SportletGroup.CORE);
             accessRequest.setRole(selectedRole);
             this.log.debug("Granting " + selectedRole + " role in gridsphere");
             // Submit changes
             this.aclManagerService.submitGroupRequest(accessRequest);
             this.aclManagerService.approveGroupRequest(accessRequest);
         }
         log.debug("Exiting saveUserRole()");
     }
 
     private PortletRole getSelectedUserRole() {
         // Iterate through list, return selected value
         List userRoleList = this.userRoleEditBean.getSelectedValues();
         this.log.debug("Updated role list " + this.userRoleEditBean.toString());
         if (userRoleList.size() == 0) {
             this.log.debug("No role was selected, setting to user");
             // Impossible, but if not selected return user role
             return PortletRole.USER;
         } else {
             // Otherwise, return the first selected value
             String userRoleItem = (String)userRoleList.get(0);
             this.log.debug("Selected role was " + userRoleItem);
             return PortletRole.toPortletRole(userRoleItem);
         }
     }
 
     private void deleteUser() {
         if (this.user != null) {
             log.debug("Deleting user...");
             this.userManagerService.deleteAccount(user);
         }
     }
 }
