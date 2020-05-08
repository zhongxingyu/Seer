 /*
  * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
  * @version $Id: UserManagerPortlet.java 5088 2006-08-18 22:53:27Z novotny $
  */
 package org.gridsphere.portlets.core.admin.users;
 
 import org.gridsphere.portlet.service.PortletServiceException;
 import org.gridsphere.provider.event.jsr.ActionFormEvent;
 import org.gridsphere.provider.event.jsr.RenderFormEvent;
 import org.gridsphere.provider.portlet.jsr.ActionPortlet;
 import org.gridsphere.provider.portletui.beans.*;
 import org.gridsphere.provider.portletui.model.DefaultTableModel;
 import org.gridsphere.services.core.mail.MailMessage;
 import org.gridsphere.services.core.mail.MailService;
 import org.gridsphere.services.core.persistence.QueryFilter;
 import org.gridsphere.services.core.portal.PortalConfigService;
 import org.gridsphere.services.core.security.password.PasswordEditor;
 import org.gridsphere.services.core.security.password.PasswordManagerService;
 import org.gridsphere.services.core.security.role.PortletRole;
 import org.gridsphere.services.core.security.role.RoleManagerService;
 import org.gridsphere.services.core.user.User;
 import org.gridsphere.services.core.user.UserManagerService;
 
 import javax.portlet.*;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 
 public class UserManagerPortlet extends ActionPortlet {
 
     // JSP pages used by this portlet
     public static final String DO_VIEW_USER_LIST = "admin/users/doViewUserList.jsp";
     public static final String DO_VIEW_USER_VIEW = "admin/users/doViewUserView.jsp";
     public static final String DO_VIEW_USER_EDIT = "admin/users/doViewUserEdit.jsp";
 
     public static final String DO_SEND_EMAIL = "admin/users/doSendEmail.jsp";
 
     // Portlet services
     private UserManagerService userManagerService = null;
     private PasswordManagerService passwordManagerService = null;
 
     private RoleManagerService roleManagerService = null;
     private PortalConfigService portalConfigService = null;
     private MailService mailService = null;
 
     private String NUM_PAGES = getClass() + ".NUM_PAGES";
     private String EMAIL_QUERY = getClass() + ".EMAIL_QUERY";
     private String ORG_QUERY = getClass() + ".ORG_QUERY";
 
     public void init(PortletConfig config) throws PortletException {
         super.init(config);
         this.userManagerService = (UserManagerService) createPortletService(UserManagerService.class);
         this.roleManagerService = (RoleManagerService) createPortletService(RoleManagerService.class);
         this.passwordManagerService = (PasswordManagerService) createPortletService(PasswordManagerService.class);
         this.mailService = (MailService) createPortletService(MailService.class);
         this.portalConfigService = (PortalConfigService) createPortletService(PortalConfigService.class);
         DEFAULT_HELP_PAGE = "admin/users/help.jsp";
         DEFAULT_VIEW_PAGE = "doListUsers";
     }
 
     public void doListUsers(ActionFormEvent evt)
             throws PortletException {
         setNextState(evt.getActionRequest(), DEFAULT_VIEW_PAGE);
     }
 
     public void doListUsers(RenderFormEvent evt)
             throws PortletException {
         PortletRequest req = evt.getRenderRequest();
 
         String numPages = (String) req.getPortletSession().getAttribute(NUM_PAGES);
         numPages = (numPages != null) ? numPages : "10";
 
         String[] itemList = {"10", "20", "50", "100"};
         ListBoxBean usersPageLB = evt.getListBoxBean("usersPageLB");
         usersPageLB.clear();
         for (int i = 0; i < itemList.length; i++) {
             ListBoxItemBean item = new ListBoxItemBean();
             item.setName(itemList[i]);
             item.setValue(itemList[i]);
             if (numPages.equals(itemList[i])) item.setSelected(true);
             usersPageLB.addBean(item);
         }
 
         String likeEmail = (String) req.getPortletSession().getAttribute(EMAIL_QUERY);
         likeEmail = (likeEmail != null) ? likeEmail : "";
         String likeOrganization = (String) req.getPortletSession().getAttribute(ORG_QUERY);
         likeOrganization = (likeOrganization != null) ? likeOrganization : "";
 
         Integer maxRows = Integer.parseInt(numPages);
 
         int numUsers = userManagerService.getNumUsers();
 
         QueryFilter filter = evt.getQueryFilter(maxRows, numUsers);
 
         List userList = userManagerService.getUsersByFullName(likeEmail, likeOrganization, filter);
 
         req.setAttribute("userList", userList);
 
         int dispPages = (numUsers / Integer.valueOf(numPages).intValue());
         //System.err.println("numUsers= " + numUsers + " numPages= " + numPages + " dispPages= " + dispPages);
         req.setAttribute("dispPages", Integer.valueOf(dispPages));
 
         //System.err.println("sizeof users=" + userList.size());
         //req.setAttribute("numUsers", Integer.valueOf(numUsers));
         //req.setAttribute("maxRows", Integer.valueOf(maxRows));
 
         TableBean userTable = evt.getTableBean("userTable");
         userTable.setQueryFilter(filter);
         //userTable.setMaxRows(maxRows);
         //userTable.setNumEntries(numUsers);
 
         setNextState(req, DO_VIEW_USER_LIST);
     }
 
     public void doReturn(ActionFormEvent event) {
         setNextState(event.getActionRequest(), "doListUsers");
     }
 
     public void doViewUser(ActionFormEvent evt)
             throws PortletException {
         PortletRequest req = evt.getActionRequest();
 
         String userID = evt.getAction().getParameter("userID");
         User user = userManagerService.getUser(userID);
         if (user != null) {
             // should check for non-null user !
             req.setAttribute("user", user);
             HiddenFieldBean hf = evt.getHiddenFieldBean("userID");
             hf.setValue(user.getID());
 
             List userRoles = roleManagerService.getRolesForUser(user);
             Iterator it = userRoles.iterator();
             String userRole = "";
             while (it.hasNext()) {
                 userRole += ((PortletRole) it.next()).getName() + ", ";
             }
             if (userRole.length() > 2) {
                 req.setAttribute("role", userRole.substring(0, userRole.length() - 2));
             } else {
                 req.setAttribute("role", this.getLocalizedText(req, "ROLES_HASNOROLES"));
             }
 
             SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d yyyy hh:mm a");
             String createtime = (String) user.getAttribute(User.CREATEDATE);
             String createdate;
             if (createtime == null) {
                 createdate = "Unknown";
             } else {
                 createdate = dateFormat.format(Long.valueOf(createtime));
             }
             req.setAttribute("createdate", createdate);
             CheckBoxBean accountCB = evt.getCheckBoxBean("accountCB");
             String disabled = (String) user.getAttribute(User.DISABLED);
             if ((disabled != null) && ("TRUE".equalsIgnoreCase(disabled))) {
                 accountCB.setSelected(true);
             }
             accountCB.setDisabled(true);
             setNextState(req, DO_VIEW_USER_VIEW);
         } else {
             setNextState(req, DEFAULT_VIEW_PAGE);
         }
     }
 
     public void doNewUser(ActionFormEvent evt)
             throws PortletException {
 
         PortletRequest req = evt.getActionRequest();
         req.setAttribute("newuser", "true");
         // indicate to edit JSP this is a new user
         HiddenFieldBean hf = evt.getHiddenFieldBean("newuser");
         hf.setValue("true");
 
         String savePasswd = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswd.equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         makeRoleFrame(evt, null);
 
         setNextState(req, DO_VIEW_USER_EDIT);
         log.debug("in doNewUser");
     }
 
     /**
      * Creates the role table
      *
      * @param evt  the action form event
      * @param user the user if this is editing an existing user, null if a new user
      */
     private void makeRoleFrame(ActionFormEvent evt, User user) {
         FrameBean roleFrame = evt.getFrameBean("roleFrame");
 
         DefaultTableModel model = new DefaultTableModel();
 
         TableRowBean tr = new TableRowBean();
         tr.setHeader(true);
         TableCellBean tc = new TableCellBean();
         TextBean text = new TextBean();
         text.setKey("USER_SELECT_ROLES");
         tc.addBean(text);
         tc.setWidth("100");
         tr.addBean(tc);
         tc = new TableCellBean();
         text = new TextBean();
         text.setKey("USER_ROLE_NAME");
         tc.setWidth("200");
         tc.addBean(text);
         tr.addBean(tc);
 
         model.addTableRowBean(tr);
 
         List<PortletRole> roles = roleManagerService.getRoles();
         List myroles = new ArrayList<PortletRole>();
         List defaultRoles = roleManagerService.getDefaultRoles();
         if (user != null) myroles = roleManagerService.getRolesForUser(user);
 
         for (PortletRole role : roles) {
             tr = new TableRowBean();
             tc = new TableCellBean();
             CheckBoxBean cb = new CheckBoxBean();
             if (myroles.contains(role)) {
                 cb.setSelected(true);
             }
             if ((user == null) && (defaultRoles.contains(role))) cb.setSelected(true);
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
     }
 
     public void doEditUser(ActionFormEvent evt)
             throws PortletException {
 
         PortletRequest req = evt.getActionRequest();
 
         // indicate to edit JSP this is an existing user
         HiddenFieldBean newuserHF = evt.getHiddenFieldBean("newuser");
         newuserHF.setValue("false");
 
         String userID = evt.getAction().getParameter("userID");
 
         // get user
         User user = this.userManagerService.getUser(userID);
         if (user == null) {
             doReturn(evt);
             return;
         }
 
         makeRoleFrame(evt, user);
 
         setUserValues(evt, user);
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         String supportX509 = portalConfigService.getProperty(PortalConfigService.SUPPORT_X509_AUTH);
         if (supportX509.equals(Boolean.TRUE.toString())) {
             req.setAttribute("certSupport", "true");
         }
 
         CheckBoxBean accountCB = evt.getCheckBoxBean("accountCB");
         String disabled = (String) user.getAttribute(User.DISABLED);
         if ((disabled != null) && ("TRUE".equalsIgnoreCase(disabled))) {
             accountCB.setSelected(true);
         }
 
         setNextState(req, DO_VIEW_USER_EDIT);
     }
 
     public void doConfirmEditUser(ActionFormEvent evt)
             throws PortletException {
 
         PortletRequest req = evt.getActionRequest();
 
         HiddenFieldBean hf = evt.getHiddenFieldBean("newuser");
         String newuser = hf.getValue();
         try {
             User user;
 
             log.debug("in doConfirmEditUser: " + newuser);
             if (newuser.equals("true")) {
                 validateUser(evt, true);
                 user = saveUser(evt, null);
                 HiddenFieldBean userHF = evt.getHiddenFieldBean("userID");
                 userHF.setValue(user.getID());
                 CheckBoxBean cb = evt.getCheckBoxBean("emailUserCB");
                 if (cb.isSelected()) mailUserConfirmation(evt, user);
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
             String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
             if (savePasswds.equals(Boolean.TRUE.toString())) {
                 req.setAttribute("savePass", "true");
             }
             if (newuser.equals("true")) {
                 // setNextState(req, "doNewUser");
             } else {
 
             }
             setNextState(req, DO_VIEW_USER_EDIT);
         }
     }
 
     public void doDeleteUser(ActionFormEvent event)
             throws PortletException {
 
         ActionRequest req = event.getActionRequest();
         String[] users = req.getParameterValues("usersCB");
         if (users != null) {
             for (int i = 0; i < users.length; i++) {
                 User user = this.userManagerService.getUser(users[i]);
                 this.passwordManagerService.deletePassword(user);
                 List<PortletRole> userRoles = this.roleManagerService.getRolesForUser(user);
                 for (PortletRole role : userRoles) {
                     this.roleManagerService.deleteUserInRole(user, role);
                 }
                 this.userManagerService.deleteUser(user);
             }
         }
         createSuccessMessage(event, this.getLocalizedText(req, "USER_DELETE_SUCCESS"));
 
         /*
         HiddenFieldBean hf = evt.getHiddenFieldBean("userID");
         String userId = hf.getValue();
         User user = this.userManagerService.getUser(userId);
         if (user != null) {
             req.setAttribute("user", user);
             this.passwordManagerService.deletePassword(user);
             List<PortletRole> userRoles = this.roleManagerService.getRolesForUser(user);
             for (PortletRole role : userRoles) {
                 this.roleManagerService.deleteUserInRole(user, role);
             }
             this.userManagerService.deleteUser(user);
             createSuccessMessage(evt, this.getLocalizedText(req, "USER_DELETE_SUCCESS"));
         }
         setNextState(req, "doListUsers");
         */
     }
 
     public void doComposeEmail(ActionFormEvent event) {
         ActionRequest req = event.getActionRequest();
         String[] users = req.getParameterValues("usersCB");
         if (users == null) return;
         req.getPortletSession().setAttribute("emails", users);
         setNextState(req, "doComposeEmail");
     }
 
     public void doComposeEmail(RenderFormEvent event) {
         RenderRequest req = event.getRenderRequest();
         String[] users = (String[]) req.getPortletSession().getAttribute("emails");
         StringBuffer emails = new StringBuffer();
         for (int i = 0; i < users.length; i++) {
             User user = this.userManagerService.getUser(users[i]);
             System.err.println(user.getEmailAddress());
             emails.append(user.getEmailAddress()).append(", ");
         }
 
         String mailFrom = portalConfigService.getProperty(PortalConfigService.MAIL_FROM);
         TextFieldBean senderTF = event.getTextFieldBean("senderTF");
         senderTF.setValue(mailFrom);
 
         TextFieldBean emailAddressTF = event.getTextFieldBean("emailAddressTF");
         // chop off last , from emails CSV
         emailAddressTF.setValue(emails.substring(0, emails.length() - 2));
         req.getPortletSession().removeAttribute("emails");
         setNextState(req, DO_SEND_EMAIL);
     }
 
     public void doSendEmail(ActionFormEvent event) {
         ActionRequest req = event.getActionRequest();
         MailMessage msg = new MailMessage();
         msg.setEmailAddress(event.getTextFieldBean("emailAddressTF").getValue());
         msg.setSender(event.getTextFieldBean("senderTF").getValue());
         msg.setSubject(event.getTextFieldBean("subjectTF").getValue());
         msg.setBody(event.getTextAreaBean("bodyTA").getValue());
 
         RadioButtonBean toRB = event.getRadioButtonBean("toRB");
         if (toRB.getValue().equals("TO")) {
             msg.setRecipientType(MailMessage.TO);
         } else {
             msg.setRecipientType(MailMessage.BCC);
         }
         try {
             mailService.sendMail(msg);
             createErrorMessage(event, "Successfully sent message");
             setNextState(req, "doListUsers");
         } catch (PortletServiceException e) {
             log.error("Unable to send mail message!", e);
             createErrorMessage(event, getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
             setNextState(req, "doSendEmail");
         }
     }
 
     private void setUserValues(ActionFormEvent event, User user) {
         event.getTextFieldBean("userName").setValue(user.getUserName());
         event.getTextFieldBean("lastName").setValue(user.getLastName());
         event.getTextFieldBean("firstName").setValue(user.getFirstName());
 
         //event.getTextFieldBean("fullName").setValue(user.getFullName());
 
         event.getTextFieldBean("emailAddress").setValue(user.getEmailAddress());
         event.getTextFieldBean("organization").setValue(user.getOrganization());
         event.getPasswordBean("password").setValue("");
         event.getTextFieldBean("certificate").setValue((String) user.getAttribute("user.certificate"));
     }
 
     private void validateUser(ActionFormEvent event, boolean newuser)
             throws PortletException {
         log.debug("Entering validateUser()");
         PortletRequest req = event.getActionRequest();
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
 
         // Validate first and last name
         String firstName = event.getTextFieldBean("firstName").getValue();
 
         if (firstName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_GIVENNAME_BLANK") + "<br />");
             isInvalid = true;
         }
 
         String lastName = event.getTextFieldBean("lastName").getValue();
 
         if (lastName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_FAMILYNAME_BLANK") + "<br />");
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
 
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             if (isInvalidPassword(event, newuser)) {
                 isInvalid = true;
             }
         }
         // Throw exception if error was found
         if (isInvalid) {
             throw new PortletException(message.toString());
         }
         log.debug("Exiting validateUser()");
     }
 
     private boolean isInvalidPassword(ActionFormEvent event, boolean newuser) {
         // Validate password
         PortletRequest req = event.getActionRequest();
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
 
     private User saveUser(ActionFormEvent event, User user) {
         log.debug("Entering saveUser()");
         // Account request
 
         boolean newuserflag = false;
         // Create edit account request
         if (user == null) {
             user = this.userManagerService.createUser();
             long now = Calendar.getInstance().getTime().getTime();
             user.setAttribute(User.CREATEDATE, String.valueOf(now));
             newuserflag = true;
         }
 
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             PasswordEditor editor = passwordManagerService.editPassword(user);
             String password = event.getPasswordBean("password").getValue();
             boolean isgood = this.isInvalidPassword(event, newuserflag);
             if (isgood) {
                 setNextState(event.getActionRequest(), DO_VIEW_USER_EDIT);
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
 
     private void editAccountRequest(ActionFormEvent event, User accountRequest) {
         log.debug("Entering editAccountRequest()");
         accountRequest.setUserName(event.getTextFieldBean("userName").getValue());
         accountRequest.setFirstName(event.getTextFieldBean("firstName").getValue());
         accountRequest.setLastName(event.getTextFieldBean("lastName").getValue());
         accountRequest.setFullName(event.getTextFieldBean("lastName").getValue() + ", " + event.getTextFieldBean("firstName").getValue());
         accountRequest.setEmailAddress(event.getTextFieldBean("emailAddress").getValue());
         accountRequest.setOrganization(event.getTextFieldBean("organization").getValue());
         if (event.getCheckBoxBean("accountCB").isSelected()) {
             accountRequest.setAttribute(User.DISABLED, "true");
         } else {
             accountRequest.setAttribute(User.DISABLED, "false");
         }
         String certval = event.getTextFieldBean("certificate").getValue();
         if (certval != null) accountRequest.setAttribute("user.certificate", certval);
     }
 
     private void saveUserRole(ActionFormEvent event, User user) {
         log.debug("Entering saveUserRole()");
 
         List<PortletRole> roles = roleManagerService.getRoles();
         for (PortletRole role : roles) {
             CheckBoxBean cb = event.getCheckBoxBean(role.getName() + "CB");
             if (cb.isSelected()) {
                 roleManagerService.addUserToRole(user, role);
             } else {
                 if (roleManagerService.isUserInRole(user, role))
                     if ((!role.equals(PortletRole.ADMIN)) || (roleManagerService.getUsersInRole(PortletRole.ADMIN).size() > 1)) {
                         roleManagerService.deleteUserInRole(user, role);
                     } else {
                         log.warn("Can't delete user, one user in role ADMIN necessary");
                         createErrorMessage(event, "Unable to delete user! One user with ADMIN role is necessary");
                     }
             }
             log.debug("Exiting saveUserRole()");
         }
     }
 
     public void filterUserList(ActionFormEvent event) {
         PortletRequest req = event.getActionRequest();
         ListBoxBean usersPageLB = event.getListBoxBean("usersPageLB");
         String numPages = usersPageLB.getSelectedValue();
         try {
             Integer.parseInt(numPages);
         } catch (Exception e) {
             numPages = "10";
         }
 
         TextFieldBean userEmailTF = event.getTextFieldBean("userEmailTF");
         TextFieldBean userOrgTF = event.getTextFieldBean("userOrgTF");
 
         req.getPortletSession().setAttribute(NUM_PAGES, numPages);
         req.getPortletSession().setAttribute(EMAIL_QUERY, userEmailTF.getValue());
         req.getPortletSession().setAttribute(ORG_QUERY, userOrgTF.getValue());
 
     }
 
     private void mailUserConfirmation(ActionFormEvent evt, User user) {
         PortletRequest req = evt.getActionRequest();
         MailMessage mailToUser = new MailMessage();
         String body = portalConfigService.getProperty("LOGIN_APPROVED_BODY");
         if (body == null) body = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
         StringBuffer message = new StringBuffer(body);
         String subject = portalConfigService.getProperty("LOGIN_APPROVED_SUBJECT");
         if (subject == null) subject = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
         mailToUser.setSubject(subject);
         message.append("\n\n");
         message.append(getLocalizedText(req, "USERNAME")).append("\t");
         message.append(user.getUserName()).append("\n");
         message.append(getLocalizedText(req, "GIVENNAME")).append("\t");
         message.append(user.getFirstName()).append("\n");
         message.append(getLocalizedText(req, "FAMILYNAME")).append("\t");
         message.append(user.getLastName()).append("\n");
         message.append(getLocalizedText(req, "ORGANIZATION")).append("\t");
         message.append(user.getOrganization()).append("\n");
         message.append(getLocalizedText(req, "EMAILADDRESS")).append("\t");
         message.append(user.getEmailAddress()).append("\n");
         message.append("\n");
         message.append(getLocalizedText(req, "USER_PASSWD_MSG"));
         message.append("\t").append(evt.getPasswordBean("password").getValue());
         mailToUser.setBody(message.toString());
         mailToUser.setEmailAddress(user.getEmailAddress());
         mailToUser.setSender(portalConfigService.getProperty(PortalConfigService.MAIL_FROM));
         try {
             mailService.sendMail(mailToUser);
         } catch (PortletServiceException e) {
             log.error("Unable to send mail message!", e);
             createErrorMessage(evt, getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
         }
 
     }
 }
