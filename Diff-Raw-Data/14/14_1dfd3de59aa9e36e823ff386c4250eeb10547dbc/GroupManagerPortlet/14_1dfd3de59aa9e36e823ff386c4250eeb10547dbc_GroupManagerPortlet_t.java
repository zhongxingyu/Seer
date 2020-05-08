 /*
  * $Id$
  */
 package org.gridlab.gridsphere.portlets.core.admin.groups;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
 import org.gridlab.gridsphere.services.core.security.acl.GroupEntry;
 import org.gridlab.gridsphere.services.core.security.acl.GroupRequest;
 import org.gridlab.gridsphere.services.core.security.acl.GroupAction;
 
 import javax.servlet.UnavailableException;
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Vector;
 
 
 public class GroupManagerPortlet extends ActionPortlet {
 
       // JSP pages used by this portlet
     public static final String DO_VIEW_GROUP_LIST = "admin/groups/groupList.jsp";
     public static final String DO_VIEW_GROUP_VIEW = "admin/groups/groupView.jsp";
     public static final String DO_VIEW_GROUP_EDIT = "admin/groups/groupEdit.jsp";
     public static final String DO_VIEW_GROUP_ENTRY_VIEW = "admin/groups/groupEntryView.jsp";
     public static final String DO_VIEW_GROUP_ENTRY_EDIT = "admin/groups/groupEntryEdit.jsp";
     public static final String DO_VIEW_GROUP_ENTRY_ADD = "admin/groups/groupEntryAdd.jsp";
     public static final String DO_VIEW_GROUP_ENTRY_ADD_CONFIRM = "admin/groups/groupEntryAddConfirm.jsp";
     public static final String DO_VIEW_GROUP_ENTRY_REMOVE = "admin/groups/groupEntryRemove.jsp";
     public static final String DO_VIEW_GROUP_ENTRY_REMOVE_CONFIRM = "admin/groups/groupEntryRemoveConfirm.jsp";
 
     // Portlet services
     private UserManagerService userManagerService = null;
     private AccessControlManagerService aclManagerService = null;
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         this.log.debug("Entering initServices()");
         try {
             this.userManagerService = (UserManagerService)config.getContext().getService(UserManagerService.class);
             this.aclManagerService = (AccessControlManagerService)config.getContext().getService(AccessControlManagerService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to initialize services!", e);
         }
 
         DEFAULT_VIEW_PAGE = "doViewListGroup";
 
         log.debug("Exiting init()");
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
         getPortletLog().info("Exiting initConcrete()");
     }
 
     public void doViewListGroup(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewListGroup");
         PortletRequest req = evt.getPortletRequest();
         List groupDescs = new Vector();
         List groupList = this.aclManagerService.getGroups();
         Iterator it = groupList.iterator();
         while (it.hasNext()) {
             PortletGroup g = (PortletGroup)it.next();
             groupDescs.add(this.aclManagerService.getGroupDescription(g.getName()));
         }
         req.setAttribute("groupList", groupList);
         req.setAttribute("groupDescs", groupDescs);
         setNextTitle(req, "Portlet Group Manager [List Groups]");
         setNextState(req, DO_VIEW_GROUP_LIST);
         this.log.debug("Exiting doViewListGroup");
     }
 
     public void doViewViewGroup(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewViewGroup");
         PortletRequest req = evt.getPortletRequest();
 
         String groupId = evt.getAction().getParameter("groupID");
 
         if (groupId == null) {
             HiddenFieldBean groupIDBean = evt.getHiddenFieldBean("groupID");
             groupId = groupIDBean.getValue();
         }
         PortletGroup group = loadGroup(evt, groupId);
 
         setGroupEntryList(evt, group);
         setNextTitle(req, "Portlet Group Manager [View Group]");
         setNextState(req, DO_VIEW_GROUP_VIEW);
         this.log.debug("Exiting doViewViewGroup");
     }
 
     public void doViewViewGroupEntry(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewViewGroupEntry");
         PortletRequest req = evt.getPortletRequest();
 
         loadGroup(evt);
         GroupEntry groupEntry = loadGroupEntry(evt);
         viewGroupEntry(evt, groupEntry);
         setNextTitle(req, "Portlet Group Manager [View Group Entry]");
         setNextState(req, DO_VIEW_GROUP_ENTRY_VIEW);
         this.log.debug("Exiting doViewViewGroupEntry");
     }
 
     public void doViewEditGroupEntry(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewEditGroupEntry");
         PortletRequest req = evt.getPortletRequest();
         loadGroup(evt);
         loadGroupEntry(evt);
         setNextTitle(req, "Portlet Group Manager [Edit Group Entry]");
         setNextState(req, DO_VIEW_GROUP_ENTRY_EDIT);
         this.log.debug("Exiting doViewEditGroupEntry");
     }
 
     public void doViewConfirmEditGroupEntry(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewConfirmEditGroupEntry");
         PortletRequest req = evt.getPortletRequest();
         loadGroup(evt);
         GroupEntry groupEntry = loadGroupEntry(evt);
 
         viewGroupEntry(evt, groupEntry);
         setNextTitle(req, "Portlet Group Manager [View Group Entry]");
         setNextState(req, DO_VIEW_GROUP_ENTRY_VIEW);
 
         this.log.debug("Exiting doViewConfirmEditGroupEntry");
     }
 
     public void doViewCancelEditGroupEntry(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewCancelEditGroupEntry");
         doViewViewGroup(evt);
         this.log.debug("Exiting doViewCancelEditGroupEntry");
     }
 
     public void doViewAddGroupEntry(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewAddGroupEntry");
         PortletRequest req = evt.getPortletRequest();
         PortletGroup group = loadGroup(evt);
         List usersNotInGroupList = this.aclManagerService.getUsersNotInGroup(group);
 
         viewUsersNotInGroupList(evt, usersNotInGroupList);
         setNextTitle(req, "Portlet Group Manager [Add User]");
         setNextState(req, DO_VIEW_GROUP_ENTRY_ADD);
         this.log.debug("Exiting doViewAddGroupEntry");
     }
 
     public void doViewConfirmAddGroupEntry(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewConfirmAddGroupEntry");
         PortletRequest req = evt.getPortletRequest();
         PortletGroup group = loadGroup(evt);
         try {
             addGroupEntries(evt, group);
             setGroupEntryList(evt, group);
             setNextTitle(req, "Portlet Group Manager [Added Users]");
             setNextState(req, DO_VIEW_GROUP_ENTRY_ADD_CONFIRM);
         } catch (PortletException e) {
             List usersNotInGroupList = this.aclManagerService.getUsersNotInGroup(group);
 
             viewUsersNotInGroupList(evt, usersNotInGroupList);
 
             setNextTitle(req, "Portlet Group Manager [Add User]");
             setNextState(req, DO_VIEW_GROUP_ENTRY_ADD);
         }
     }
 
     public void doViewCancelAddGroupEntry(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewCancelAddGroupEntry");
         doViewViewGroup(evt);
         this.log.debug("Exiting doViewCancelAddGroupEntry");
     }
 
     public void doViewRemoveGroupEntry(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewRemoveGroupEntry");
         PortletRequest req = evt.getPortletRequest();
         PortletGroup group = loadGroup(evt);
         setGroupEntryList(evt, group);
         setNextTitle(req, "Portlet Group Manager [Remove Users]");
         setNextState(req, DO_VIEW_GROUP_ENTRY_REMOVE);
         this.log.debug("Exiting doViewRemoveGroupEntry");
     }
 
     public void doViewConfirmRemoveGroupEntry(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewConfirmRemoveGroupEntry");
         PortletRequest req = evt.getPortletRequest();
         loadGroup(evt);
         removeGroupEntries(evt);
         setNextTitle(req, "Portlet Group Manager [Users Removed]");
         setNextState(req, DO_VIEW_GROUP_ENTRY_REMOVE_CONFIRM);
         this.log.debug("Exiting doViewConfirmRemoveGroupEntry");
     }
 
     public void doViewCancelRemoveGroupEntry(FormEvent evt)
             throws PortletException {
         this.log.debug("Entering doViewCancelRemoveGroupEntry");
         doViewViewGroup(evt);
         this.log.debug("Exiting doViewCancelRemoveGroupEntry");
     }
 
 
     private PortletGroup loadGroup(FormEvent evt) {
         HiddenFieldBean groupIDBean = evt.getHiddenFieldBean("groupID");
         String groupId = groupIDBean.getValue();
        if (groupId == null) {
            groupId = evt.getAction().getParameter("groupEntryID");
        }
         return loadGroup(evt, groupId);
     }
 
     private PortletGroup loadGroup(FormEvent evt, String groupID) {
         // Get group id
         PortletGroup group = null;
         TextBean groupNameBean = evt.getTextBean("groupName");
         TextBean groupLabelBean = evt.getTextBean("groupLabel");
         TextBean groupDescBean = evt.getTextBean("groupDescription");
         HiddenFieldBean groupIDBean = evt.getHiddenFieldBean("groupID");
         groupIDBean.setValue(groupID);
 
         String groupDescription = "";
 
         // Load group
         if (!groupID.equals("")) {
             group = this.aclManagerService.getGroup(groupID);
             groupDescription = this.aclManagerService.getGroupDescription(group.getName());
         }
 
         if (groupID.equals("")) {
             // Clear group attributes
             groupNameBean.setValue("");
             groupLabelBean.setValue("");
             groupDescBean.setValue("");
         }  else {
             // Set attributes
             groupNameBean.setValue(group.getName());
             groupLabelBean.setValue(group.getLabel());
             groupDescBean.setValue(groupDescription);
         }
         log.debug("Exiting loadGroup()");
         return group;
     }
 
     private void setGroupEntryList(FormEvent evt, PortletGroup group) {
         PortletRequest req = evt.getPortletRequest();
         List groupEntryList = this.aclManagerService.getGroupEntries(group);
         req.setAttribute("groupEntryList", groupEntryList);
     }
 
     private void viewUsersNotInGroupList(FormEvent evt, List usersNotInGroupList) {
         log.debug("Entering viewUsersNotInGroupList()");
         ListBoxBean usersNotInGroupListBox = evt.getListBoxBean("usersNotInGroupList");
         usersNotInGroupListBox.setMultipleSelection(false);
         usersNotInGroupListBox.setSize(1);
         if (usersNotInGroupList.size() == 0) {
             ListBoxItemBean item = new ListBoxItemBean();
             item.setValue("&lt;No more users to add to this group&gt;");
             usersNotInGroupListBox.addBean(item);
         } else {
             // Add rows to table
             Iterator userIterator = usersNotInGroupList.iterator();
             while (userIterator.hasNext()) {
                 // Get next user
                 User user = (User)userIterator.next();
                 //String userID = user.getID();
                 String userName = user.getUserName();
                 String fullName = user.getFullName();
                 String userLabel = userName + " (" + fullName + ")";
                 ListBoxItemBean item = new ListBoxItemBean();
                 item.setValue(userName);
                 usersNotInGroupListBox.addBean(item);
             }
         }
 
         ListBoxBean userRoles = evt.getListBoxBean("groupEntryRole");
         userRoles.setMultipleSelection(false);
         userRoles.setSize(1);
         ListBoxItemBean adminItem = new ListBoxItemBean();
         adminItem.setValue("ADMIN");
         adminItem.setSelected(false);
         ListBoxItemBean userItem = new ListBoxItemBean();
         userItem.setValue("USER");
         userItem.setSelected(true);
         userRoles.addBean(userItem);
         userRoles.addBean(adminItem);
     }
 
     private GroupEntry loadGroupEntry(FormEvent evt) {
         log.debug("Entering loadGroupEntry()");
         String groupEntryID = evt.getAction().getParameter("groupEntryID");
         GroupEntry groupEntry = this.aclManagerService.getGroupEntry(groupEntryID);
         return groupEntry;
     }
 
     private void viewGroupEntry(FormEvent evt, GroupEntry groupEntry) {
         log.debug("Entering viewGroupEntry()");
 
         HiddenFieldBean groupEntryIDBean = evt.getHiddenFieldBean("groupEntryID");
         TextBean groupEntryUserNameBean = evt.getTextBean("groupEntryUserName");
         TextBean groupEntryUserFullNameBean = evt.getTextBean("groupEntryUserFullName");
         TextBean groupEntryRoleBean = evt.getTextBean("groupEntryRole");
         if (groupEntry == null) {
             // Clear group entry attributes
 
             groupEntryIDBean.setValue("");
             groupEntryUserNameBean.setValue("");
             groupEntryUserFullNameBean.setValue("");
             groupEntryRoleBean = new TextBean("");
         } else {
             User groupEntryUser = groupEntry.getUser();
             // Set group entry attributes
             groupEntryIDBean.setValue(groupEntry.getID());
             groupEntryUserNameBean.setValue(groupEntryUser.getUserName());
             groupEntryUserFullNameBean.setValue(groupEntryUser.getFullName());
             groupEntryRoleBean.setValue(groupEntry.getRole().toString());
         }
 
         log.debug("Exiting viewGroupEntry()");
     }
 
     private void addGroupEntries(FormEvent evt, PortletGroup group)
             throws PortletException {
         // Group entry role to apply to all users below...
         ListBoxBean groupEntryRoleListBox = evt.getListBoxBean("groupEntryRole");
         String selectedGroupRole = groupEntryRoleListBox.getSelectedValue();
 
         PortletRole groupEntryRole = PortletRole.toPortletRole(selectedGroupRole);
         // Users to add...
         ListBoxBean userList = evt.getListBoxBean("usersNotInGroupList");
         String groupEntryUserID = userList.getSelectedValue();
 
         User groupEntryUser = this.userManagerService.getUserByUserName(groupEntryUserID);
 
         if (groupEntryUser != null) {
             addGroupEntry(groupEntryUser, group, groupEntryRole);
         } else {
             log.debug("Unable to get user: " + groupEntryUserID);
         }
 
     }
 
     private GroupEntry addGroupEntry(User user, PortletGroup group, PortletRole role)
             throws PortletException {
         // Create add to group request
         GroupRequest groupRequest = this.aclManagerService.createGroupRequest();
         groupRequest.setUser(user);
         groupRequest.setGroup(group);
         groupRequest.setRole(role);
 
         // Create access right
         this.aclManagerService.submitGroupRequest(groupRequest);
         this.aclManagerService.approveGroupRequest(groupRequest);
         // Return the new group
         return this.aclManagerService.getGroupEntry(user, group);
     }
 
     private void removeGroupEntries(FormEvent evt)
             throws PortletException {
         // Create group entry list
         List groupEntryList = new ArrayList();
         PortletRequest req = evt.getPortletRequest();
         CheckBoxBean groupEntryIDCheckBox = evt.getCheckBoxBean("groupEntryID");
 
         List groupEntryIDList = groupEntryIDCheckBox.getSelectedValues();
 
         if (groupEntryIDList.size() == 0) {
             throw new PortletException("No users were selected for removal. Please try again.");
         }
 
         for (int ii = 0; ii < groupEntryIDList.size(); ++ii) {
             // Get entry to remove
             String groupEntryID = (String)groupEntryIDList.get(ii);
             GroupEntry entry = this.aclManagerService.getGroupEntry(groupEntryID);
             // Remove group entry
             removeGroupEntry(entry);
             // Put entry in list
             groupEntryList.add(entry);
         }
         req.setAttribute("groupEntryList", groupEntryList);
     }
 
     private void removeGroupEntry(GroupEntry entry)
             throws PortletException {
         // Create remove from group request
         GroupRequest groupRequest = this.aclManagerService.createGroupRequest(entry);
         groupRequest.setGroupAction(GroupAction.REMOVE);
         // Create access right
         this.aclManagerService.submitGroupRequest(groupRequest);
         this.aclManagerService.approveGroupRequest(groupRequest);
     }
 
 }
