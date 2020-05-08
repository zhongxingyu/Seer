 /**
  * 
  */
 package net.frontlinesms.ui.handler;
 
 // TODO remove static imports
 import static net.frontlinesms.FrontlineSMSConstants.ACTION_ADD_TO_GROUP;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_CONTACTS_IN_GROUP;
 import static net.frontlinesms.FrontlineSMSConstants.COMMON_GROUP;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_CONTACTS_DELETED;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_EXISTENT_CONTACT;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_GROUPS_AND_CONTACTS_DELETED;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_GROUP_ALREADY_EXISTS;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_IMPOSSIBLE_TO_CREATE_A_GROUP_HERE;
 import static net.frontlinesms.FrontlineSMSConstants.MESSAGE_REMOVING_CONTACTS;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_BUTTON_YES;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_DORMANT;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_EMAIL_ADDRESS;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_MANAGER_CONTACT_FILTER;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_MANAGER_CONTACT_LIST;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_MOBILE_MSISDN;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_NAME;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_NOTES;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_CONTACT_OTHER_MSISDN;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_DELETE_NEW_CONTACT;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_GROUPS_MENU;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LABEL_STATUS;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MENU_ITEM_MSG_HISTORY;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MENU_ITEM_VIEW_CONTACT;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MI_DELETE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_MI_SEND_SMS;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_NEW_CONTACT_GROUP_LIST;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_NEW_GROUP;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_PN_CONTACTS;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_RADIO_BUTTON_ACTIVE;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_SEND_SMS_BUTTON;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_VIEW_CONTACT_BUTTON;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import net.frontlinesms.Utils;
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.ContactDao;
 import net.frontlinesms.data.repository.GroupDao;
 import net.frontlinesms.ui.Icon;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 import org.apache.log4j.Logger;
 
 import thinlet.Thinlet;
 
 /**
  * Event handler for the Contacts tab and associated dialogs.
  * @author Alex
  */
 public class ContactsTabHandler extends BaseTabHandler implements PagedComponentItemProvider, SingleGroupSelecterOwner {
 //> STATIC CONSTANTS
 	/** UI XML File Path: the Home Tab itself */
 	private static final String UI_FILE_CONTACTS_TAB = "/ui/core/contacts/contactsTab.xml";
 	/** UI XML File Path: Edit and Create dialog for {@link Contact} objects */
 	private static final String UI_FILE_CREATE_CONTACT_FORM = "/ui/core/contacts/dgEditContact.xml";
 	private static final String UI_FILE_DELETE_OPTION_DIALOG_FORM = "/ui/dialog/deleteOptionDialogForm.xml"; // TODO move this to the correct path
 	private static final String UI_FILE_NEW_GROUP_FORM = "/ui/dialog/newGroupForm.xml"; // TODO move this to the correct path
 	
 	private static final String COMPONENT_GROUP_SELECTER_CONTAINER = "pnGroupsContainer";
 	
 //> INSTANCE PROPERTIES
 	/** Logging object */
 	private final Logger LOG = Utils.getLogger(this.getClass()); // FIXME rename to log
 	
 //> DATA ACCESS OBJECTS
 	/** Data access object for {@link Group}s */
 	private final GroupDao groupDao;
 	/** Data access object for {@link Contact}s */
 	private final ContactDao contactDao;
 	
 	
 //> CACHED THINLET UI COMPONENTS
 	/** UI Component component: contact list.  This is cached here to save searching for it later. */
 	private Object contactListComponent;
 	
 	/** Handler for paging of {@link #contactListComponent} */
 	private ComponentPagingHandler contactListPager;
 	/** String to filter the contacts by */
 	private String contactNameFilter;
 
 	private final GroupSelecter groupSelecter;
 
 //> CONSTRUCTORS
 	/**
 	 * Create a new instance of this class.
 	 * @param ui value for {@link #ui}
 	 * @param contactDao {@link #contactDao}
 	 * @param groupDao {@link #groupDao}
 	 */
 	public ContactsTabHandler(UiGeneratorController ui, ContactDao contactDao, GroupDao groupDao) {
 		super(ui);
 		this.contactDao = contactDao;
 		this.groupDao = groupDao;
 		this.groupSelecter = new GroupSelecter(ui, this);
 	}
 	
 	@Override
 	public void init() {
 		super.init();
 		this.groupSelecter.init(ui.getRootGroup());
		ui.add(find(COMPONENT_GROUP_SELECTER_CONTAINER), this.groupSelecter.getPanelComponent(), 0);
 	}
 
 //> ACCESSORS
 	/** Refreshes the data displayed in the tab. */
 	public void refresh() {
 		updateGroupList();
 	}
 	
 //> GROUP SELECTION METHODS
 	public void groupSelectionChanged(Group selectedGroup) {
 		System.out.println("Group selected: " + selectedGroup);
 //	}
 //	
 //	/**
 //	 * Method invoked when the group/contacts tree selection changes. 
 //	 * <br>This method updated the contact list according to the new selection.
 //	 * @param tree
 //	 * @param panel 
 //	 */
 //	public void selectionChanged(Object tree, Object panel) {
 		LOG.trace("ENTER");
 		this.ui.setText(this.ui.find(COMPONENT_CONTACT_MANAGER_CONTACT_FILTER), "");
 
 //		Group g = this.ui.getGroup(this.ui.getSelectedItem(tree));
 		Group g = this.groupSelecter.getSelectedGroup();
 		String toSet = InternationalisationUtils.getI18NString(COMMON_CONTACTS_IN_GROUP, g.getName());
 		this.ui.setText(find("pnContacts"), toSet); // FIXME this should be a constant
 		
 		Object buttonPanelContainer = find(COMPONENT_GROUP_SELECTER_CONTAINER);
 		Object deleteButton = this.ui.find(buttonPanelContainer, "deleteButton"); // FIXME this should be a constant
 		this.ui.setEnabled(deleteButton, !this.ui.isDefaultGroup(g));
 		
 		Object sms = this.ui.find(buttonPanelContainer, "sendSMSButtonGroupSide"); // FIXME this should be a constant
 		this.ui.setEnabled(sms, g != null);
 		
 		updateContactList();
 		LOG.trace("EXIT");
 	}
 	
 //> PAGING METHODS
 	public PagedListDetails getListDetails(Object list, int startIndex, int limit) {
 		if(list == this.contactListComponent) {
 			return getContactListDetails(startIndex, limit);
 		} else throw new IllegalStateException();
 	}
 	
 	private PagedListDetails getContactListDetails(int startIndex, int limit) {
 		int totalItemCount = (this.contactNameFilter == null || this.contactNameFilter.length() == 0) 
 				? this.contactDao.getContactCount()
 				: this.contactDao.getContactsFilteredByNameCount(this.contactNameFilter);
 		
 		List<Contact> contacts = (this.contactNameFilter == null || this.contactNameFilter.length() == 0) 
 				? this.contactDao.getAllContacts(startIndex, limit)
 				: this.contactDao.getContactsFilteredByName(this.contactNameFilter, startIndex, limit);
 		Object[] listItems = toThinletComponents(contacts);
 		
 		return new PagedListDetails(totalItemCount, listItems);
 		
 	}
 	
 	private Object[] toThinletComponents(List<Contact> contacts) {
 		Object[] components = new Object[contacts.size()];
 		for (int i = 0; i < components.length; i++) {
 			Contact c = contacts.get(i);
 			components[i] = ui.getRow(c);
 		}
 		return components;
 	}
 
 //> UI METHODS
 	/**
 	 * Shows the delete option dialog, which asks the user if he/she wants to remove
 	 * the selected contacts from database.
 	 * @param list
 	 */
 	public void showDeleteOptionDialog() {
 		Group g = this.groupSelecter.getSelectedGroup();
 		if (!this.ui.isDefaultGroup(g)) {
 			Object deleteDialog = ui.loadComponentFromFile(UI_FILE_DELETE_OPTION_DIALOG_FORM, this);
 			ui.add(deleteDialog);
 		}
 	}
 
 	/**
 	 * Shows contact dialog to allow edition of the selected contact.
 	 * <br>This method affects the advanced mode.
 	 * @param list
 	 */
 	public void showContactDetails(Object list) {
 		Object selected = this.ui.getSelectedItem(list);
 		if (this.ui.isAttachment(selected, Contact.class)) {
 			showContactDetails(this.ui.getContact(selected));
 		}
 	}
 	
 	/**
 	 * Populates the pop up menu with all groups create by users.
 	 * 
 	 * @param popUp
 	 * @param list
 	 */
 	public void populateGroups(Object popUp, Object list) {
 		Object[] selectedItems = this.ui.getSelectedItems(list);
 		this.ui.setVisible(popUp, this.ui.getSelectedItems(list).length > 0);
 		if (selectedItems.length == 0) {
 			//Nothing selected
 			boolean none = true;
 			for (Object o : this.ui.getItems(popUp)) {
 				if (this.ui.getName(o).equals(COMPONENT_NEW_GROUP)
 						|| this.ui.getName(o).equals("miNewContact")) {
 					this.ui.setVisible(o, true);
 					none = false;
 				} else {
 					this.ui.setVisible(o, false);
 				}
 			}
 			this.ui.setVisible(popUp, !none);
 		} else if (this.ui.getAttachedObject(selectedItems[0]) instanceof Contact) {
 			for (Object o : this.ui.getItems(popUp)) {
 				String name = this.ui.getName(o);
 				if (name.equals(COMPONENT_MENU_ITEM_MSG_HISTORY) 
 						|| name.equals(COMPONENT_MENU_ITEM_VIEW_CONTACT)) {
 					this.ui.setVisible(o, this.ui.getSelectedItems(list).length == 1);
 				} else if (!name.equals(COMPONENT_GROUPS_MENU)) {
 					this.ui.setVisible(o, true);
 				}
 			}
 			Object menu = this.ui.find(popUp, COMPONENT_GROUPS_MENU);
 			this.ui.removeAll(menu);
 			List<Group> allGroups = this.groupDao.getAllGroups();
 			for (Group g : allGroups) {
 				Object menuItem = Thinlet.create(Thinlet.MENUITEM);
 				this.ui.setText(menuItem, InternationalisationUtils.getI18NString(COMMON_GROUP) + "'" + g.getName() + "'");
 				this.ui.setIcon(menuItem, Icon.GROUP);
 				this.ui.setAttachedObject(menuItem, g);
 				this.ui.setAction(menuItem, "addToGroup(this)", menu, this);
 				this.ui.add(menu, menuItem);
 			}
 			this.ui.setVisible(menu, allGroups.size() != 0);
 			String menuName = InternationalisationUtils.getI18NString(ACTION_ADD_TO_GROUP);
 			this.ui.setText(menu, menuName);
 			
 			Object menuRemove = this.ui.find(popUp, "groupsMenuRemove");
 			if (menuRemove != null) {
 				Contact c = this.ui.getContact(this.ui.getSelectedItem(list));
 				this.ui.removeAll(menuRemove);
 				Collection<Group> groups = c.getGroups();
 				for (Group g : groups) {
 					Object menuItem = Thinlet.create(Thinlet.MENUITEM);
 					this.ui.setText(menuItem, g.getName());
 					this.ui.setIcon(menuItem, Icon.GROUP);
 					this.ui.setAttachedObject(menuItem, g);
 					this.ui.setAction(menuItem, "removeFromGroup(this)", menuRemove, this);
 					this.ui.add(menuRemove, menuItem);
 				}
 				this.ui.setEnabled(menuRemove, groups.size() != 0);
 			}
 		} else {
 			Group g = this.ui.getGroup(this.ui.getSelectedItem(list));
 			//GROUPS OR BOTH
 			for (Object o : this.ui.getItems(popUp)) {
 				String name = this.ui.getName(o);
 				if (COMPONENT_NEW_GROUP.equals(name) 
 						|| COMPONENT_MI_SEND_SMS.equals(name)
 						|| COMPONENT_MI_DELETE.equals(name)
 						|| COMPONENT_MENU_ITEM_MSG_HISTORY.equals(name)
 						|| "miNewContact".equals(name)) {
 					this.ui.setVisible(o, true);
 				} else {
 					this.ui.setVisible(o, false);
 				}
 				if (COMPONENT_MI_DELETE.equals(name)) {
 					this.ui.setVisible(o, !this.ui.isDefaultGroup(g));
 				}
 				
 				if (COMPONENT_NEW_GROUP.equals(name)) {
 					this.ui.setVisible(o, g!=this.ui.getUnnamedContacts() && g!=this.ui.getUngroupedContacts());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Shows the new group dialog.
 	 * @param groupList
 	 */
 	public void showNewGroupDialog() {
 		Object newGroupForm = this.ui.loadComponentFromFile(UI_FILE_NEW_GROUP_FORM, this);
 		ui.setAttachedObject(newGroupForm, this.groupSelecter.getSelectedGroup());
 //		this.ui.setAttachedObject(newGroupForm, this.ui.getGroupFromSelectedNode(this.ui.getSelectedItem(groupList)));
 		this.ui.add(newGroupForm);
 	}
 
 	/**
 	 * Shows the edit contact dialog. If the contact is null, then all fields are blank since
 	 * it's a new contact. Otherwise we set the fields with the contact details, leaving it
 	 * for editing.
 	 * @param contact 
 	 */
 	private void showContactDetails(Contact contact) {
 		Object createDialog = this.ui.loadComponentFromFile(UI_FILE_CREATE_CONTACT_FORM, this);
 		this.ui.setAttachedObject(createDialog, contact);
 		if (contact != null) {
 			setText(createDialog, COMPONENT_CONTACT_NAME, contact.getName());
 			setText(createDialog, COMPONENT_CONTACT_MOBILE_MSISDN, contact.getPhoneNumber());
 			setText(createDialog, COMPONENT_CONTACT_OTHER_MSISDN, contact.getOtherPhoneNumber());
 			setText(createDialog, COMPONENT_CONTACT_EMAIL_ADDRESS, contact.getEmailAddress());
 			setText(createDialog, COMPONENT_CONTACT_NOTES, contact.getNotes());
 			contactDetails_setActive(createDialog, contact.isActive());
 
 			Object groupList = this.ui.find(createDialog, COMPONENT_NEW_CONTACT_GROUP_LIST);
 			for (Group g : contact.getGroups()) {
 				Object item = this.ui.createListItem(g.getName(), g);
 				this.ui.setIcon(item, Icon.GROUP);
 				this.ui.add(groupList, item);
 			}
 		}
 		this.ui.add(createDialog);
 	}
 
 	/**
 	 * Shows the new contact dialog. This method affects the advanced mode.
 	 */
 	public void showNewContactDialog() {
 		Object createDialog = this.ui.loadComponentFromFile(UI_FILE_CREATE_CONTACT_FORM, this);
 		Object list = this.ui.find(createDialog, COMPONENT_NEW_CONTACT_GROUP_LIST);
 		Group sel = this.groupSelecter.getSelectedGroup();
 		List<Group> allGroups = this.groupDao.getAllGroups();
 		for (Group g : allGroups) {
 			Object item = this.ui.createListItem(g.getName(), g);
 			this.ui.setIcon(item, Icon.GROUP);
 			this.ui.setSelected(item, g.equals(sel));
 			this.ui.add(list, item);
 		}
 		this.ui.add(createDialog);
 	}
 
 	/** Updates the list of contacts with the new filter. */
 	public void filterContacts() {
 		updateContactList();
 	}
 
 	/**
 	 * Applies a text filter to the contact list.  The list is not updated until {@link #filterContacts()}
 	 * is called.
 	 * @param contactFilter The new filter.
 	 */	
 	public void setContactFilter(String contactFilter) {
 		this.contactNameFilter = contactFilter;
 	}
 
 	/**
 	 * Enables or disables the buttons on the Contacts tab (advanced mode).
 	 * @param contactList
 	 */
 	public void enabledButtonsAfterSelection(Object contactList) {
 		boolean enabled = this.ui.getSelectedItems(contactList).length > 0;
 		this.ui.setEnabled(this.ui.find(COMPONENT_DELETE_NEW_CONTACT), enabled);
 		this.ui.setEnabled(this.ui.find(COMPONENT_VIEW_CONTACT_BUTTON), enabled);
 		this.ui.setEnabled(this.ui.find(COMPONENT_SEND_SMS_BUTTON), enabled);
 	}
 
 	/**
 	 * Adds selected contacts to group.
 	 * 
 	 * @param item The item holding the destination group.
 	 */
 	public void addToGroup(Object item) {
 		LOG.trace("ENTER");
 		Object[] selected = null;
 		selected = this.ui.getSelectedItems(contactListComponent);
 		// Add to the selected groups...
 		Group destination = this.ui.getGroup(item);
 		// Let's check all the selected items.  Any that are groups should be added to!
 		for (Object component : selected) {
 			if (this.ui.isAttachment(component, Contact.class)) {
 				Contact contact = this.ui.getContact(component);
 				LOG.debug("Adding Contact [" + contact.getName() + "] to [" + destination + "]");
 				if(destination.addDirectMember(contact)) {
 					groupDao.updateGroup(destination);
 				}
 			}
 		}
 		updateGroupList();
 		LOG.trace("EXIT");
 	}
 	
 	/**
 	 * Remove selected groups and contacts.
 	 * 
 	 * @param button
 	 * @param dialog
 	 */
 	public void removeSelectedFromGroupList(final Object button, Object dialog) {
 		LOG.trace("ENTER");
 //		final Object[] selected;
 //		selected = this.ui.getSelectedItems(groupListComponent);
 		if (dialog != null) {
 			this.ui.removeDialog(dialog);
 		}
 //		for (Object o : selected) {
 //			Group group = ui.getGroupFromSelectedNode(o);
 			Group selectedGroup = this.groupSelecter.getSelectedGroup();
 			if(!ui.isDefaultGroup(selectedGroup)) {
 				boolean removeContactsAlso = false;
 				if (button != null) {
 					removeContactsAlso = ui.getName(button).equals(COMPONENT_BUTTON_YES);
 				}
 				LOG.debug("Selected Group [" + selectedGroup.getName() + "]");
 				LOG.debug("Remove Contacts from database [" + removeContactsAlso + "]");
 				if (!ui.isDefaultGroup(selectedGroup)) {
 					//Inside a default group
 					LOG.debug("Removing group [" + selectedGroup.getName() + "] from database");
 					groupDao.deleteGroup(selectedGroup, removeContactsAlso);
 				} else {
 					if (removeContactsAlso) {
 						LOG.debug("Group not destroyable, removing contacts...");
 						for (Contact c : selectedGroup.getDirectMembers()) {
 							LOG.debug("Removing contact [" + c.getName() + "] from database");
 							contactDao.deleteContact(c);
 						}
 					}
 				}
 			}
 //		}
 		Object sms = ui.find(find(COMPONENT_GROUP_SELECTER_CONTAINER), "sendSMSButtonGroupSide");
 //		ui.setEnabled(sms, ui.getSelectedItems(groupListComponent).length > 0);
 		ui.setEnabled(sms, selectedGroup != null);
 		ui.alert(InternationalisationUtils.getI18NString(MESSAGE_GROUPS_AND_CONTACTS_DELETED));
 		refresh();
 		LOG.trace("EXIT");
 	}
 
 	/**
 	 * Updates or create a contact with the details added by the user. <br>
 	 * This method is used by advanced mode, and also Contact Merge
 	 * TODO this method should be transactional
 	 * @param contactDetailsDialog
 	 */
 	public void saveContactDetailsAdvancedView(Object contactDetailsDialog) {
 		LOG.trace("ENTER");
 		Object attachment = this.ui.getAttachedObject(contactDetailsDialog);
 		Contact contact = null;
 		if (attachment != null) {
 			contact = (Contact)attachment;
 			LOG.debug("Attachment is a contact [" + contact.getName() + "]");
 		}
 		String name = getText(contactDetailsDialog, COMPONENT_CONTACT_NAME);
 		String msisdn = getText(contactDetailsDialog, COMPONENT_CONTACT_MOBILE_MSISDN);
 		String otherMsisdn = getText(contactDetailsDialog, COMPONENT_CONTACT_OTHER_MSISDN);
 		String emailAddress = getText(contactDetailsDialog, COMPONENT_CONTACT_EMAIL_ADDRESS);
 		String notes = getText(contactDetailsDialog, COMPONENT_CONTACT_NOTES);
 		boolean isActive = contactDetails_getActive(contactDetailsDialog);
 		
 		try {
 			if (contact == null) {
 				LOG.debug("Creating a new contact [" + name + ", " + msisdn + "]");
 				contact = new Contact(name, msisdn, otherMsisdn, emailAddress, notes, isActive);
 				this.contactDao.saveContact(contact);
 			} else {
 				// If this is not a new contact, we still need to update all details
 				// that would otherwise be set by the constructor called in the block
 				// above.
 				LOG.debug("Editing contact [" + contact.getName() + "]. Setting new values!");
 				contact.setPhoneNumber(msisdn);
 				contact.setName(name);
 				contact.setOtherPhoneNumber(otherMsisdn);
 				contact.setEmailAddress(emailAddress);
 				contact.setNotes(notes);
 				contact.setActive(isActive);
 				this.contactDao.updateContact(contact);
 			}
 
 			// Refresh the Contacts tab, and make sure that the group and contact who were previously selected are still selected
 			updateContactList();
 		} catch(DuplicateKeyException ex) {
 			LOG.debug("There is already a contact with this mobile number - cannot save!", ex);
 			showMergeContactDialog(contact, contactDetailsDialog);
 		} finally {
 			this.ui.removeDialog(contactDetailsDialog);
 		}
 		LOG.trace("EXIT");
 	}
 
 	/**
 	 * Removes the contacts selected in the contacts list from the group which is selected in the groups tree.
 	 * @param selectedGroup A set of thinlet components with group members attached to them.
 	 */
 	public void removeFromGroup(Object selectedGroup) {
 		Group g = this.ui.getGroup(selectedGroup);
 		Contact c = this.ui.getContact(this.ui.getSelectedItem(contactListComponent));
 		if(g.removeContact(c)) {
 			this.groupDao.updateGroup(g);
 		}
 		this.refresh();
 	}
 
 	/** Removes the selected contacts of the supplied contact list component. */
 	public void deleteSelectedContacts() {
 		LOG.trace("ENTER");
 		this.ui.removeConfirmationDialog();
 		this.ui.setStatus(InternationalisationUtils.getI18NString(MESSAGE_REMOVING_CONTACTS));
 		final Object[] selected = this.ui.getSelectedItems(contactListComponent);
 		for (Object o : selected) {
 			Contact contact = ui.getContact(o);
 			LOG.debug("Deleting contact [" + contact.getName() + "]");
 			for (Group g : contact.getGroups()) {
 				// FIXME what should be here?
 			}
 			contactDao.deleteContact(contact);
 		}
 		ui.alert(InternationalisationUtils.getI18NString(MESSAGE_CONTACTS_DELETED));
 		refresh();
 		LOG.trace("EXIT");
 	}
 
 	/**
 	 * Creates a new group with the supplied name.
 	 * 
 	 * @param newGroupName The desired group name.
 	 * @param dialog the dialog holding the information to where we should create this new group.
 	 */
 	public void createNewGroup(String newGroupName, Object dialog) {
 		// The selected parent group should be attached to this dialog.  Get it,
 		// create the new group, update the group list and then remove the dialog.
 		Group selectedParentGroup = this.ui.getGroup(dialog);
 		doGroupCreation(newGroupName, dialog, selectedParentGroup);		
 	}
 
 	/**
 	 * Update the icon for active/dormant.
 	 * @param radioButton
 	 * @param label
 	 */
 	public void updateIconActive(Object radioButton, Object label) {
 		String icon;
 		if (this.ui.getName(radioButton).equals(COMPONENT_RADIO_BUTTON_ACTIVE)) {
 			icon = Icon.ACTIVE;
 		} else {
 			icon = Icon.DORMANT;
 		}
 		this.ui.setIcon(label, icon);
 	}
 	
 //> PRIVATE UI HELPER METHODS
 //	/**
 //	 * Gets the node we are currently displaying for a group.
 //	 * @param component
 //	 * @param group
 //	 * @return
 //	 */
 //	private Object getNodeForGroup(Object component, Group group) {
 //		if(group == null) {
 //			group = this.ui.getRootGroup();
 //		}
 //		Object ret = null;
 //		for (Object o : this.ui.getItems(component)) {
 //			Group g = this.ui.getGroup(o);
 //			if (g.equals(group)) {
 //				ret = o;
 //				break;
 //			} else {
 //				ret = getNodeForGroup(o, group);
 //				if (ret != null) break;
 //			}
 //		}
 //		return ret;
 //	}
 	/**
 	 * @param tree
 	 * @return all the selected contacts to show in the contact list
 	 */
 	private LinkedHashMap<String, Contact> getContactsFromSelectedGroups(Object tree) {
 		LinkedHashMap<String, Contact> toBeShown = new LinkedHashMap<String, Contact>();
 		if (this.ui.isSelected(this.ui.getItems(tree)[0])) {
 			//Root group selected
 			//Show everyone
 			for (Contact c : contactDao.getAllContacts()) {
 				toBeShown.put(c.getPhoneNumber(), c);
 			}
 		} else {
 			for (Object o : this.ui.getSelectedItems(tree)) {
 				for(Contact c : this.ui.getGroup(o).getAllMembers()) {
 					toBeShown.put(c.getPhoneNumber(), c);
 				}
 			}
 		}
 		
 		return toBeShown;
 	}
 
 	/**
 	 * Show the form to allow merging between a previously-created contact, and an attempted-newly-created contact.
 	 * TODO if we work out a good-looking way of doing this, we should implement it.  Currently this just warns the user that a contact with this number already exists.
 	 */
 	private void showMergeContactDialog(Contact oldContact, Object createContactForm) { // FIXME remove arguments from this method
 		this.ui.alert(InternationalisationUtils.getI18NString(MESSAGE_EXISTENT_CONTACT));
 	}
 	/**
 	 * Creates a group with the supplied name and inside the supplied parent .
 	 * 
 	 * @param newGroupName The desired group name.
 	 * @param dialog The dialog to be removed after the operation.
 	 * @param selectedParentGroup
 	 */
 	private void doGroupCreation(String newGroupName, Object dialog, Group selectedParentGroup) {
 		LOG.trace("ENTER");
 		if(LOG.isDebugEnabled()) {
 			String parentGroupName = selectedParentGroup == null ? "null" : selectedParentGroup.getName();
 			LOG.debug("Parent group [" + parentGroupName + "]");
 		}
 		if(selectedParentGroup == this.ui.getRootGroup()) {
 			selectedParentGroup = null;
 		}
 		if (selectedParentGroup == this.ui.getUnnamedContacts() || selectedParentGroup == this.ui.getUngroupedContacts()) {
 			this.ui.alert(InternationalisationUtils.getI18NString(MESSAGE_IMPOSSIBLE_TO_CREATE_A_GROUP_HERE));
 			if (dialog != null) this.ui.remove(dialog);
 			return;
 		}
 		LOG.debug("Group Name [" + newGroupName + "]");
 		try {
 			if(LOG.isDebugEnabled()) LOG.debug("Creating group with name: " + newGroupName + " and parent: " + selectedParentGroup);
 			
 			Group g = new Group(selectedParentGroup, newGroupName);
 			this.groupDao.saveGroup(g);
 			
 			this.groupSelecter.addGroup(g);
 //			// Now we've saved the group, add it to the groups tree displayed in the contacts manager
 //			Object groupListComponent = getGroupTreeComponent();
 //			Object parentNode = getNodeForGroup(groupListComponent, selectedParentGroup);
 //			this.ui.add(parentNode, this.ui.getNode(g, true));
 			
 			if (dialog != null) this.ui.remove(dialog);
 			LOG.debug("Group created successfully!");
 		} catch (DuplicateKeyException e) {
 			LOG.debug("A group with this name already exists.", e);
 			this.ui.alert(InternationalisationUtils.getI18NString(MESSAGE_GROUP_ALREADY_EXISTS));
 		}
 		LOG.trace("EXIT");
 	}
 //	private void updateTree(Group group) {
 //		Object node = getNodeForGroup(groupListComponent, group); //Only advanced mode
 //		updateGroup(group, node);
 //	}
 	
 	/**
 	 * Finds a child component by name, and then sets its text value.
 	 * @param parentComponent The parent component to search within
 	 * @param componentName The name of the child component
 	 * @param value the value to set the child's TEXT attribute to
 	 */
 	private void setText(Object parentComponent, String componentName, String value) {
 		if(value == null) value = "";
 		this.ui.setText(this.ui.find(parentComponent, componentName), value);
 	}
 	
 	/**
 	 * Finds a child component by name, and gets its text value.
 	 * @param parentComponent The parent component to search within
 	 * @param componentName The name of the child component
 	 * @return the text attribute of the child
 	 */
 	private String getText(Object parentComponent, String componentName) {
 		return this.ui.getText(this.ui.find(parentComponent, componentName));
 	}
 	
 	/**
 	 * Set the current state of the active/dormant component.
 	 * @param contactDetails
 	 * @param active
 	 */
 	private void contactDetails_setActive(Object contactDetails, boolean active) {
 		this.ui.setSelected(this.ui.find(contactDetails, COMPONENT_RADIO_BUTTON_ACTIVE), active);
 		this.ui.setSelected(this.ui.find(contactDetails, COMPONENT_CONTACT_DORMANT), !active);
 		if (active) {
 			this.ui.setIcon(this.ui.find(contactDetails, COMPONENT_LABEL_STATUS), Icon.ACTIVE);
 		} else {
 			this.ui.setIcon(this.ui.find(contactDetails, COMPONENT_LABEL_STATUS), Icon.DORMANT);
 		}
 	}
 	/**
 	 * @param contactDetails contact details dialog
 	 * @return the current state of the active component 
 	 */
 	private boolean contactDetails_getActive(Object contactDetails) {
 		return this.ui.isSelected(this.ui.find(contactDetails, COMPONENT_RADIO_BUTTON_ACTIVE));
 	}
 	
 	/** Repopulates the contact list according to the current filter. */
 	public void updateContactList() {
 		this.contactListPager.setCurrentPage(0);
 		this.contactListPager.refresh();
 		enabledButtonsAfterSelection(contactListComponent);
 	}
 
 	/** Updates the group tree. */
 	private void updateGroupList() {
 //		Object groupListComponent = getGroupTreeComponent();
 //		
 //		Object selected = this.ui.getSelectedItem(groupListComponent);
 //		
 //		this.ui.removeAll(groupListComponent);
 //		this.ui.add(groupListComponent, this.ui.getNode(this.ui.getRootGroup(), true));
 //
 //		this.ui.setSelectedGC(selected, groupListComponent);
 		this.groupSelecter.refresh();
 		
 		updateContactList();
 	}
 
 	private void updateGroup(Group group, Object node) {
 		if (this.ui.getBoolean(node, Thinlet.EXPANDED) && group.hasDescendants())
 			this.ui.setIcon(node, Icon.FOLDER_OPEN);
 		else 
 			this.ui.setIcon(node, Icon.FOLDER_CLOSED);
 	}
 	
 //> EVENT HANDLER METHODS
 	public void addToContactList(Contact contact, Group group) {
 		List<Group> selectedGroupsFromTree = new ArrayList<Group>();
 		
 		Group g = this.groupSelecter.getSelectedGroup();
 		selectedGroupsFromTree.add(g);
 		
 		if (selectedGroupsFromTree.contains(group)) {
 			int limit = this.contactListPager.getMaxItemsPerPage();
 			//Adding
 			if (this.ui.getItems(contactListComponent).length < limit) {
 				this.ui.add(contactListComponent, this.ui.getRow(contact));
 			}
 		}
 		
 		this.groupSelecter.refresh();
 //		updateTree(group);
 	}
 	
 //> UI PASS-THROUGH METHODS TO UiGC
 	/** @see UiGeneratorController#groupList_expansionChanged(Object) */
 	public void groupList_expansionChanged(Object groupList) {
 		this.ui.groupList_expansionChanged(groupList);
 	}
 	/**
 	 * Shows the compose message dialog, populating the list with the selection of the 
 	 * supplied list.
 	 * @param list
 	 */
 	public void show_composeMessageForm() {
 		this.ui.show_composeMessageForm(this.groupSelecter.getPanelComponent());
 	}
 	/**
 	 * Shows the compose message dialog, populating the list with the selection of the 
 	 * supplied list.
 	 * @param list
 	 */
 	public void show_composeMessageForm(Object list) {
 		this.ui.show_composeMessageForm(list);
 	}
 	/**
 	 * Shows the export wizard dialog for exporting contacts.
 	 * @param list The list to get selected items from.
 	 * @param type the name of the type to export
 	 */
 	public void showExportWizard(Object list) {
 		this.ui.showExportWizard(list, "contacts");
 	}
 
 //> INSTANCE HELPER METHODS
 	/** Initialise dynamic contents of the tab component. */
 	protected Object initialiseTab() {
 		Object tabComponent = ui.loadComponentFromFile(UI_FILE_CONTACTS_TAB, this);
 		
 		
 		// Cache Thinlet UI components
 		contactListComponent = this.ui.find(tabComponent, COMPONENT_CONTACT_MANAGER_CONTACT_LIST);
 		
 		this.contactListPager = new ComponentPagingHandler(this.ui, this, contactListComponent);
 		Object pnContacts = this.ui.find(tabComponent, COMPONENT_PN_CONTACTS);
 		this.ui.add(pnContacts, this.contactListPager.getPanel());
 		
 		return tabComponent;
 	}
 
 //> STATIC FACTORIES
 
 //> STATIC HELPER METHODS
 }
