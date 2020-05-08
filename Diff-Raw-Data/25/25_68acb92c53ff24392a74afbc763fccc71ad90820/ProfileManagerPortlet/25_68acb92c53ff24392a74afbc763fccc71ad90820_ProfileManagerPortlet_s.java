 /*
  * @author <a href="mailto:michael.russell@aei.mpg.de">Michael Russell</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.user;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletUser;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.provider.portletui.model.DefaultTableModel;
 import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
 import org.gridlab.gridsphere.services.core.security.acl.GroupRequest;
 import org.gridlab.gridsphere.services.core.security.acl.InvalidGroupRequestException;
 import org.gridlab.gridsphere.services.core.security.acl.GroupEntry;
 import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;
 import org.gridlab.gridsphere.services.core.security.password.InvalidPasswordException;
 import org.gridlab.gridsphere.services.core.security.password.PasswordEditor;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 import org.gridlab.gridsphere.services.core.layout.LayoutManagerService;
 import org.gridlab.gridsphere.services.core.messaging.TextMessagingService;
 import org.gridlab.gridsphere.services.core.utils.DateUtil;
 import org.gridlab.gridsphere.services.core.cache.CacheService;
 import org.gridlab.gridsphere.services.core.locale.LocaleService;
 import org.gridlab.gridsphere.tmf.config.TmfService;
 import org.gridlab.gridsphere.tmf.config.TmfUser;
 
 import javax.servlet.UnavailableException;
 import java.util.*;
 import java.text.DateFormat;
 import java.io.IOException;
 
 public class ProfileManagerPortlet extends ActionPortlet {
 
     // JSP pages used by this portlet
     public static final String VIEW_USER_JSP = "profile/viewuser.jsp";
     public static final String CONFIGURE_JSP = "profile/configure.jsp";
     public static final String HELP_JSP = "profile/help.jsp";
 
     // Portlet services
     private UserManagerService userManagerService = null;
     private PasswordManagerService passwordManagerService = null;
     private AccessControlManagerService aclManagerService = null;
     private LocaleService localeService = null;
     private LayoutManagerService layoutMgr = null;
     private TextMessagingService tms = null;
 
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         try {
             this.userManagerService = (UserManagerService)config.getContext().getService(UserManagerService.class);
             this.aclManagerService = (AccessControlManagerService)config.getContext().getService(AccessControlManagerService.class);
             this.passwordManagerService = (PasswordManagerService)config.getContext().getService(PasswordManagerService.class);
             this.localeService = (LocaleService)config.getContext().getService(LocaleService.class);
             this.layoutMgr = (LayoutManagerService)config.getContext().getService(LayoutManagerService.class);
             this.tms = (TextMessagingService) config.getContext().getService(TextMessagingService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to initialize services!", e);
         }
 
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
         DEFAULT_VIEW_PAGE = "doViewUser";
         DEFAULT_HELP_PAGE = HELP_JSP;
         DEFAULT_CONFIGURE_PAGE = "doConfigureSettings";
     }
 
     public void doViewUser(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         DefaultTableModel model = setUserTable(event, false);
         DefaultTableModel messaging = getMessagingFrame(event, false);
         FrameBean messagingFrame = event.getFrameBean("messagingFrame");
         FrameBean groupsFrame = event.getFrameBean("groupsFrame");
         groupsFrame.setTableModel(model);
         messagingFrame.setTableModel(messaging);
         setNextState(req, VIEW_USER_JSP);
     }
 
     public void doConfigureSettings(FormEvent event) throws PortletException {
         PortletRequest req = event.getPortletRequest();
         String locales = getPortletSettings().getAttribute("supported-locales");
         TextFieldBean localesTF = event.getTextFieldBean("localesTF");
         localesTF.setValue(locales);
         setNextState(req, CONFIGURE_JSP);
     }
 
    public void doSaveLocales(FormEvent event) {
        TextFieldBean tf = event.getTextFieldBean("localesTF");
        String locales = tf.getValue();
        if (locales != null) {
            FrameBean msg = event.getFrameBean("msgFrame");
            getPortletSettings().setAttribute("supported-locales", locales);
            try {
                getPortletSettings().store();
            } catch (IOException e) {
                msg.setKey("PROFILE_SAVE_ERROR");
                msg.setStyle("error");
            }
            msg.setKey("PROFILE_SAVE_SUCCESS");
            msg.setStyle("success");
        }
    }

     public DefaultTableModel setUserTable(FormEvent event, boolean disable) {
         PortletRequest req = event.getPortletRequest();
         User user = req.getUser();
 
         //String logintime = DateFormat.getDateTimeInstance().format(new Date(user.getLastLoginTime()));
         req.setAttribute("logintime", DateUtil.getLocalizedDate(user,
                 req.getLocale(),
                 user.getLastLoginTime(), DateFormat.FULL, DateFormat.FULL));
         req.setAttribute("username", user.getUserName());
 
         TextFieldBean userName =  event.getTextFieldBean("userName");
         userName.setValue(user.getUserName());
         userName.setDisabled(disable);
 
         TextFieldBean fullName =  event.getTextFieldBean("fullName");
         fullName.setValue(user.getFullName());
         fullName.setDisabled(disable);
 
         TextFieldBean organization =  event.getTextFieldBean("organization");
         organization.setValue(user.getOrganization());
         organization.setDisabled(disable);
 
         TextFieldBean email =  event.getTextFieldBean("emailAddress");
         email.setValue(user.getEmailAddress());
         email.setDisabled(disable);
 
 
         Locale locale = req.getLocale();
 
         req.setAttribute("locale", locale);
 
         ListBoxBean localeSelector = event.getListBoxBean("userlocale");
         localeSelector.clear();
         localeSelector.setSize(1);
         localeSelector.setDisabled(disable);
 
         Locale[] locales = localeService.getSupportedLocales();
 
         for (int i = 0; i < locales.length; i++) {
             Locale displayLocale = locales[i];
             ListBoxItemBean localeBean = makeLocaleBean(displayLocale.getDisplayLanguage(displayLocale), displayLocale.getLanguage(), locale);
             localeSelector.addBean(localeBean);
         }
         req.setAttribute(CacheService.NO_CACHE, CacheService.NO_CACHE);
 
         ListBoxBean timezoneList = event.getListBoxBean("timezones");
         Map zones = DateUtil.getLocalizedTimeZoneNames(locale);
         Set keys = zones.keySet();
         Iterator it2 = keys.iterator();
         String userTimeZone = (String)user.getAttribute(User.TIMEZONE);
         if (userTimeZone==null) {
             userTimeZone = TimeZone.getDefault().getID();
         }
 
         while (it2.hasNext()) {
             String zone = (String)it2.next();
             ListBoxItemBean item = new ListBoxItemBean();
             item.setValue((String)zones.get(zone));
             item.setName(zone);
             if (userTimeZone.equals(zone)) {
                 item.setSelected(true);
             }
             timezoneList.addBean(item);
         }
         timezoneList.setSize(6);
         timezoneList.sortByValue();
         timezoneList.setDisabled(disable);
         timezoneList.setMultipleSelection(false);
 
         DefaultTableModel model = new DefaultTableModel();
 
         // fill in groups model
 
         TableRowBean tr = new TableRowBean();
         tr.setHeader(true);
         TableCellBean tcGroups = new TableCellBean();
         TextBean tbGroups = new TextBean();
 
         String text = this.getLocalizedText(req, "PROFILE_GROUPS");
         tbGroups.setValue(text);
 
         TextBean tbGroupsDesc = new TextBean();
         String desc =  this.getLocalizedText(req, "PROFILE_GROUP_DESC");
         tbGroupsDesc.setValue(desc);
         tcGroups.addBean(tbGroups);
         TableCellBean tcGroupsDesc = new TableCellBean();
         tcGroupsDesc.addBean(tbGroupsDesc);
         tr.addBean(tcGroups);
         tr.addBean(tcGroupsDesc);
         model.addTableRowBean(tr);
 
         List groups = aclManagerService.getGroups();
         Iterator it = groups.iterator();
         TableRowBean groupsTR = null;
         TableCellBean groupsTC = null;
         TableCellBean groupsDescTC = null;
         while (it.hasNext()) {
             groupsTR = new TableRowBean();
             groupsTC = new TableCellBean();
             groupsDescTC = new TableCellBean();
             PortletGroup g = (PortletGroup)it.next();
             String groupDesc = g.getDescription();
 
             CheckBoxBean cb = new CheckBoxBean();
             cb.setBeanId("groupCheckBox");
             if (aclManagerService.isUserInGroup(user, g)) cb.setSelected(true);
             cb.setValue(g.getName());
             cb.setDisabled(disable);
             // make sure user cannot deselect core gridsphere group
             if (g.equals(PortletGroupFactory.GRIDSPHERE_GROUP)) cb.setDisabled(true);
 
             //System.err.println("g= " + g.getName() + " gridsphere group= " + PortletGroupFactory.GRIDSPHERE_GROUP.getName());
 
             TextBean groupTB = new TextBean();
             groupTB.setValue(g.getName());
             if (!g.isPublic() && (!cb.isSelected())) {
                 cb.setDisabled(true);
             }
             groupsTC.addBean(cb);
             groupsTC.addBean(groupTB);
             TextBean groupDescTB = new TextBean();
             groupDescTB.setValue(groupDesc);
             groupsDescTC.addBean(groupDescTB);
             if (!g.isPublic()) {
                 TextBean priv = event.getTextBean("privateTB");
                 priv.setValue("&nbsp;&nbsp;" + this.getLocalizedText(req, "GROUP_NOTIFY"));
                 List admins = aclManagerService.getUsers(g, PortletRole.ADMIN);
                 String emailAddress = "";
                 if (admins.isEmpty()) {
                     List supers = aclManagerService.getUsersWithSuperRole();
                     User root = (User)supers.get(0);
                     emailAddress = root.getEmailAddress();
                 } else {
                     User admin = (User)admins.get(0);
                     emailAddress = admin.getEmailAddress();
                 }
                 String mailhref = "&nbsp;<a href=\"mailto:" + emailAddress + "\">" + this.getLocalizedText(req, "GROUP_ADMIN") + "</a>";
                 TextBean mailTB = new TextBean();
                 mailTB.setValue(mailhref);
                 groupsDescTC.addBean(priv);
                 groupsDescTC.addBean(mailTB);
             }
             groupsTR.addBean(groupsTC);
             groupsTR.addBean(groupsDescTC);
             model.addTableRowBean(groupsTR);
         }
 
 
         return model;
     }
 
 
     private DefaultTableModel getMessagingFrame(FormEvent event, boolean readonly) {
         DefaultTableModel model = new DefaultTableModel();
         PortletRequest req = event.getPortletRequest();
 
         TableRowBean trMessaging = new TableRowBean();
 
         TableCellBean tcMessagingDesc = new TableCellBean();
         TableCellBean tcMessagingUserid = new TableCellBean();
 
 
         TextBean tbMessagingDesc = event.getTextBean("tbMessagingDesc");
         TextBean tbMessagingUserid = event.getTextBean("tbMessagingUserid");
         String text = this.getLocalizedText(req, "PROFILE_MESSAGING_SERVICE");
         tbMessagingDesc.setValue(text);
         tcMessagingDesc.addBean(tbMessagingDesc);
         tbMessagingUserid = event.getTextBean("tbMessagingUserid");
         text = this.getLocalizedText(req, "PROFILE_MESSAGING_USERID");
         tbMessagingUserid.setValue(text);
         tcMessagingUserid.addBean(tbMessagingUserid);
 
         trMessaging.addBean(tcMessagingDesc);
         trMessaging.addBean(tcMessagingUserid);
         // add the header to the model
         trMessaging.setHeader(true);
 
         model.addTableRowBean(trMessaging);
 
         List services = tms.getServices();
 
         if (services.size()==0) {
             TableRowBean noServiceRow = new TableRowBean();
             TableCellBean noServiceCell1 = new TableCellBean();
             TableCellBean noServiceCell2 = new TableCellBean();
             String localeText = this.getLocalizedText(req, "PROFILE_MESSAGING_NO_SERVICE_CONFIGURED");
             TextBean noServiceText = new TextBean();
             noServiceText.setValue(localeText);
             noServiceCell1.addBean(noServiceText);
             TextBean noServiceText2 = new TextBean();
             noServiceText2.setValue("&nbsp;");
             noServiceCell2.addBean(noServiceText2);
             noServiceRow.addBean(noServiceCell1);
             noServiceRow.addBean(noServiceCell2);
             model.addTableRowBean(noServiceRow);
 
         } else {
 
             for (int i=0;i<services.size();i++) {
 
                 TmfService tmfservice = (TmfService) services.get(i);
 
                 // tablerow
                 TableRowBean trService = new TableRowBean();
 
                 // NAME
                 TableCellBean tcServiceName = new TableCellBean();
                 // make text
                 TextBean servicename = new TextBean();
 
                 String localeText = this.getLocalizedText(req, tmfservice.getDescription());
                 servicename.setValue(localeText);
                 tcServiceName.addBean(servicename);
                 trService.addBean(tcServiceName);
 
                 // INPUT
                 TableCellBean tcServiceInput = new TableCellBean();
                 // make inputfield
                 TextFieldBean servicename_input = event.getTextFieldBean("TFSERVICENAME"+tmfservice.getMessageType());
                 TmfUser user = tms.getUser(req.getUser().getUserID());
                 if (user!=null) {
                     servicename_input.setValue(user.getUserNameForMessagetype(tmfservice.getMessageType()));
                 }
                 servicename_input.setDisabled(readonly);
                 tcServiceInput.addBean(servicename_input);
                 trService.addBean(tcServiceInput);
                 model.addTableRowBean(trService);
             }
         }
         return model;
     }
 
     public void doSavePass(FormEvent event) {
 
         PortletRequest req = event.getPortletRequest();
         User user = req.getUser();
 
         String origPasswd = event.getPasswordBean("origPassword").getValue();
         try {
             passwordManagerService.validateSuppliedPassword(user, origPasswd);
         } catch (InvalidPasswordException e) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_INVALID"));
             return;
         }
 
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
         }
     }
 
 
     public void doSaveGroups(FormEvent event) {
 
         PortletRequest req = event.getPortletRequest();
         User user = req.getUser();
 
         CheckBoxBean groupsCB = event.getCheckBoxBean("groupCheckBox");
         List selectedGroups = groupsCB.getSelectedValues();
 
         // first get groups user is already in
         List groupEntries = aclManagerService.getGroupEntries(user);
         Iterator geIt = groupEntries.iterator();
         List usergroups = new ArrayList();
         while (geIt.hasNext()) {
             GroupEntry ge = (GroupEntry)geIt.next();
             if (!ge.getGroup().equals(PortletGroupFactory.GRIDSPHERE_GROUP)) {
                 log.debug("user is in group: " + ge.getGroup());
                 //aclManagerService.deleteGroupEntry(ge);
                 usergroups.add(ge.getGroup().getName());
             }
         }
 
         // approve all selected group requests
         Iterator it = selectedGroups.iterator();
         while (it.hasNext()) {
             String groupStr = (String)it.next();
             log.debug("Selected group: " + groupStr);
             PortletGroup selectedGroup = this.aclManagerService.getGroupByName(groupStr);
             if (!usergroups.contains(selectedGroup.getName())) {
                 log.debug("does not have group: " + selectedGroup.getName());
                 GroupRequest groupRequest = this.aclManagerService.createGroupEntry();
                 groupRequest.setUser(user);
                 groupRequest.setGroup(selectedGroup);
                 groupRequest.setRole(PortletRole.USER);
 
                 this.aclManagerService.saveGroupEntry(groupRequest);
 
                 log.debug("adding tab " + selectedGroup.getName());
 
                 this.layoutMgr.addGroupTab(req, selectedGroup.getName());
                 this.layoutMgr.reloadPage(req);
             }
             usergroups.remove(selectedGroup.getName());
         }
 
         // subtract groups
         it = usergroups.iterator();
         while (it.hasNext()) {
             String groupStr = (String)it.next();
             log.debug("Removing group :" + groupStr);
             PortletGroup g = this.aclManagerService.getGroupByName(groupStr);
             GroupEntry entry = this.aclManagerService.getGroupEntry(user, g);
             GroupRequest groupRequest = this.aclManagerService.editGroupEntry(entry);
 
             createSuccessMessage(event, this.getLocalizedText(req, "USER_GROUPS_SUCCESS"));
             this.aclManagerService.deleteGroupEntry(groupRequest);
 
             this.layoutMgr.refreshPage(req);
             /*
             List portletIds =  null;
             // Very nasty JSR 168 hack since JSR webapp names are stored internally with a ".1" extension
 
             portletIds = portletRegistry.getAllConcretePortletIDs(req.getRole(), g.getName() + ".1");
             if (portletIds.isEmpty()) {
             portletIds =  portletRegistry.getAllConcretePortletIDs(req.getRole(), g.getName());
             }
             this.layoutMgr.removePortlets(req, portletIds);
             this.layoutMgr.reloadPage(req);
             */
         }
     }
 
     public void doSaveUser(FormEvent event) {
 
         PortletRequest req = event.getPortletRequest();
         User user = req.getUser();
 
         // validate user entries to create an account request
         SportletUser acctReq = validateUser(event);
         if (acctReq != null) {
             log.debug("approve account request for user: " + user.getID());
             userManagerService.saveUser(acctReq);
             createSuccessMessage(event, this.getLocalizedText(req, "USER_UPDATE_SUCCESS"));
         }
 
     }
 
 
 
     public void doSaveMessaging(FormEvent event) {
         // now do the messaging stuff
         PortletRequest req = event.getPortletRequest();
         //User user = req.getUser();
         TmfUser tmfuser = tms.getUser(req.getUser().getUserID());
         // if the user does not exist yet
         if (tmfuser==null) {
             tmfuser = new TmfUser(); 
             tmfuser.setName(event.getPortletRequest().getUser().getFullName());
             tmfuser.setUserid(req.getUser().getUserID());
             //tmfuser.setPreferred(h_service.getValue());
         }
 
         List services = tms.getServices();
         for (int i=0;i<services.size();i++) {
             TmfService tmfservice = (TmfService) services.get(i);
 
             TextFieldBean tfb = event.getTextFieldBean("TFSERVICENAME"+tmfservice.getMessageType());
             tmfuser.setMessageType(tmfservice.getMessageType(), tfb.getValue());
         }
 
 
         tms.saveUser(tmfuser);
 
     }
 
     private SportletUser validateUser(FormEvent event) {
         log.debug("Entering validateUser()");
         PortletRequest req = event.getPortletRequest();
         User user = req.getUser();
         StringBuffer message = new StringBuffer();
         boolean isInvalid = false;
 
         // get timezone
         String timeZone = event.getListBoxBean("timezones").getSelectedValue();
 
         // get timezone
         String locale = event.getListBoxBean("userlocale").getSelectedValue();
 
         // Validate user name
         String userName = event.getTextFieldBean("userName").getValue();
         if (userName.equals("")) {
             message.append(this.getLocalizedText(req, "USER_NAME_BLANK") + "<br>");
             isInvalid = true;
         }
 
         // Validate full name
         String fullName = event.getTextFieldBean("fullName").getValue();
         if (fullName.equals("")) {
             message.append(this.getLocalizedText(req, "USER_FULLNAME_BLANK") + "<br>");
             isInvalid = true;
         }
         // Validate given name
         String organization = event.getTextFieldBean("organization").getValue();
 
         // Validate e-mail
         String eMail = event.getTextFieldBean("emailAddress").getValue();
         if (eMail.equals("")) {
             message.append(this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br>");
             isInvalid = true;
         } else if ((eMail.indexOf("@") < 0)) {
             message.append(this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br>");
             isInvalid = true;
         } else if ((eMail.indexOf(".") < 0)) {
             message.append(this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br>");
             isInvalid = true;
         }
 
         // Throw exception if error was found
         if (isInvalid) {
             MessageBoxBean msg = event.getMessageBoxBean("msg");
             msg.setValue(message.toString());
             return null;
         }
 
         log.debug("creating account request for user: " + user.getID());
         SportletUser acctReq = userManagerService.editUser(user);
         acctReq.setEmailAddress(eMail);
         acctReq.setUserName(userName);
         acctReq.setFullName(fullName);
        if (locale!=null) acctReq.setAttribute(User.LOCALE, locale);
         if (timeZone!=null) acctReq.setAttribute(User.TIMEZONE, timeZone);
         if (organization != null) acctReq.setOrganization(organization);
 
 
         // Save password parameters if password was altered
         /*
         String passwordValue = event.getPasswordBean("password").getValue();
         if (passwordValue.length() > 0) {
             acctReq.setPasswordValue(passwordValue);
         }
         */
 
         log.debug("Exiting validateUser()");
         return acctReq;
     }
 
     private ListBoxItemBean makeLocaleBean(String language, String name, Locale locale) {
         ListBoxItemBean bean = new ListBoxItemBean();
         String display = language;
         display = language.substring(0, 1).toUpperCase() + language.substring(1);
 
         bean.setValue(display);
         bean.setName(name);
 
         if (locale.getLanguage().equals(name)) {
             bean.setSelected(true);
         }
         return bean;
     }
 
 
     private void createErrorMessage(FormEvent event, String msg) {
         MessageBoxBean msgBox = event.getMessageBoxBean("msg");
         msgBox.setMessageType(TextBean.MSG_ERROR);
         msgBox.setValue(msg);
     }
 
     private void createSuccessMessage(FormEvent event, String msg) {
         MessageBoxBean msgBox = event.getMessageBoxBean("msg");
         msgBox.setMessageType(TextBean.MSG_SUCCESS);
         msgBox.setValue(msg);
     }
 }
