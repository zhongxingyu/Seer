 /**
  * 
  */
 package net.frontlinesms.ui.handler.contacts;
 
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_LABEL_STATUS;
 import static net.frontlinesms.ui.UiGeneratorControllerConstants.COMPONENT_RADIO_BUTTON_ACTIVE;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.repository.ContactDao;
 import net.frontlinesms.data.repository.GroupMembershipDao;
 import net.frontlinesms.ui.Icon;
 import net.frontlinesms.ui.ThinletUiEventHandler;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 /**
  * @author aga
  */
 public class ContactEditor implements ThinletUiEventHandler, SingleGroupSelecterDialogOwner {
 
 //> STATIC CONSTANTS
 	/** UI XML File Path: Edit and Create dialog for {@link Contact} objects */
 	private static final String UI_FILE_CREATE_CONTACT_FORM = "/ui/core/contacts/dgEditContact.xml";
 
 	public static final String COMPONENT_NEW_CONTACT_GROUP_LIST = "newContact_groupList";
 	public static final String COMPONENT_CONTACT_NAME = "contact_name";
 	public static final String COMPONENT_CONTACT_MOBILE_MSISDN = "contact_mobileMsisdn";
 	public static final String COMPONENT_CONTACT_OTHER_MSISDN = "contact_otherMsisdn";
 	public static final String COMPONENT_CONTACT_EMAIL_ADDRESS = "contact_emailAddress";
 	public static final String COMPONENT_CONTACT_NOTES = "contact_notes";
 	public static final String COMPONENT_CONTACT_DORMANT = "rb_dormant";
 
 	public static final String MESSAGE_EXISTENT_CONTACT = "message.contact.already.exists";
 
 //> INSTANCE PROPERTIES
 	private Logger LOG = Logger.getLogger(this.getClass());
 	private UiGeneratorController ui;
 	private ContactDao contactDao;
 	private GroupMembershipDao groupMembershipDao;
 	private ContactEditorOwner owner;
 	/** UI Component: the dialog that will contain the contact editor */
 	private Object dialogComponent;
 	/** UI Component: the list of groups that the contact is a member of */
 	private Object groupListComponent;
 	/** The contact we are editing, or <code>null</code> if we are creating a new contact. */
 	private Contact target;
 
 	/** Groups to add the contact to - this list contains only groups that he was not already a member of */
 	private Set<Group> addedGroups = new HashSet<Group>();
 	/** Groups to remove the contact from - this list contains only groups that he was already a member of */
 	private Set<Group> removedGroups = new HashSet<Group>();
 	
 //> CONSTRUCTORS
 	ContactEditor(UiGeneratorController ui, ContactEditorOwner owner) {
 		this.ui = ui;
 		this.contactDao = ui.getFrontlineController().getContactDao();
 		this.groupMembershipDao = ui.getFrontlineController().getGroupMembershipDao();
 		this.owner = owner;
 	}
 	
 //> INIT METHODS
 	/** Show dialog to create a new contact in a particular group. */
 	public void show(Group selectedGroup) {
 		initDialog();
		if(selectedGroup != null && !selectedGroup.isRoot()) {
 			addNewGroup(selectedGroup);
 		}
 		showDialog();
 	}
 	
 	/** Show dialog to edit an existing contact. */
 	public void show(Contact contact) {
 		this.target = contact;
 		initDialog();
 		populateContactDetails(contact);
 		for(Group g : groupMembershipDao.getGroups(contact)) {
 			addGroupToList(g);
 		}
 		showDialog();
 	}
 	
 	/** Adds {@link #dialogComponent} to {@link #ui} */
 	private void showDialog() {
 		ui.add(this.dialogComponent);
 	}
 
 	/** Initialise the dialog from the UI layout file */
 	private void initDialog() {
 		this.dialogComponent = ui.loadComponentFromFile(UI_FILE_CREATE_CONTACT_FORM, this);
 		this.groupListComponent = find(COMPONENT_NEW_CONTACT_GROUP_LIST);
 	}
 	
 	/** Populate the contact details ui components. */
 	private void populateContactDetails(Contact contact) {
 		setText(COMPONENT_CONTACT_NAME, contact.getName());
 		setText(COMPONENT_CONTACT_MOBILE_MSISDN, contact.getPhoneNumber());
 		setText(COMPONENT_CONTACT_OTHER_MSISDN, contact.getOtherPhoneNumber());
 		setText(COMPONENT_CONTACT_EMAIL_ADDRESS, contact.getEmailAddress());
 		setText(COMPONENT_CONTACT_NOTES, contact.getNotes());
 		contactDetails_setActive(contact.isActive());
 	}
 	
 //> GROUP SELECTION CALLBACKS
 	/** @see SingleGroupSelecterDialogOwner#groupSelectionCompleted(Group) */
 	public void groupSelectionCompleted(Group group) {
 		addNewGroup(group);
 	}
 	
 //> ACCESSORS
 	/** @return new groups to add the contact to */
 	private Set<Group> getAddedGroups() {
 		return addedGroups;
 	}
 	
 	/** @return groups to remove the contact from */
 	private Set<Group> getRemovedGroups() {
 		return removedGroups;
 	}
 	
 	/** Adds the group to the UI, and if appropriate to {@link #addedGroups} */
 	private void addNewGroup(Group group) {
 		if(!removedGroups.remove(group)) {
 			addedGroups.add(group);
 		}
 		addGroupToList(group);
 	}
 	
 	/** Removes group from the UI, and if appropriate from {@link #removedGroups} */
 	private void removeGroup(Group group) {
 		if(!addedGroups.remove(group)) {
 			removedGroups.add(group);
 		}
 		// Remove the Group from the list
 		for(Object listItem : ui.getItems(this.groupListComponent)) {
 			if(ui.getAttachedObject(listItem, Group.class).equals(group)) {
 				ui.remove(listItem);
 				break;
 			}
 		}
 	}
 	
 	/** Adds a group to the list of groups */
 	private void addGroupToList(Group group) {
 		ui.add(this.groupListComponent, createGroupListItem(group)); 
 	}
 	
 	/** @return thinlet list item for a group */
 	private Object createGroupListItem(Group group) {
 		String name = group.getName(); // TODO make this the qualified name
 		Object item = this.ui.createListItem(name, group);
 		this.ui.setIcon(item, Icon.GROUP);
 		return item;
 	}
 	
 //> UI EVENT METHODS
 	/**
 	 * Updates or create a contact with the details added by the user. <br>
 	 * This method is used by advanced mode, and also Contact Merge
 	 * TODO this method should be transactional
 	 * @param contactDetailsDialog
 	 */
 	public void save() {
 		// Extract the new details of the contact from the UI
 		String name = getText(COMPONENT_CONTACT_NAME);
 		String msisdn = getText(COMPONENT_CONTACT_MOBILE_MSISDN);
 		String otherMsisdn = getText(COMPONENT_CONTACT_OTHER_MSISDN);
 		String emailAddress = getText(COMPONENT_CONTACT_EMAIL_ADDRESS);
 		String notes = getText(COMPONENT_CONTACT_NOTES);
 		boolean isActive = contactDetails_getActive();
 		
 		// Update or save the contact
 		Contact contact = this.target;
 		try {
 			if (contact == null) {
 				LOG.debug("Creating a new contact [" + name + ", " + msisdn + "]");
 				contact = new Contact(name, msisdn, otherMsisdn, emailAddress, notes, isActive);
 				
 				this.contactDao.saveContact(contact);
 
 				// Update the groups that this contact is a member of
 				for(Group g : getAddedGroups()) {
 					groupMembershipDao.addMember(g, contact);
 				}
 				
 				removeDialog();
 				owner.contactCreationComplete(contact);
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
 
 				// Update the groups that this contact is a member of
 				for(Group g : getRemovedGroups()) {
 					groupMembershipDao.removeMember(g, contact);
 				}
 				for(Group g : getAddedGroups()) {
 					groupMembershipDao.addMember(g, contact);
 				}
 				
 				this.contactDao.updateContact(contact);
 				removeDialog();
 				owner.contactEditingComplete(contact);
 			}
 		} catch(DuplicateKeyException ex) {
 			LOG.debug("There is already a contact with this mobile number - cannot save!", ex);
 			showMergeContactDialog(contact, this.dialogComponent);
 		}
 	}
 	
 	/** Remove selected groups */
 	public void removeSelectedGroups() {
 		Object[] selecteds = ui.getSelectedItems(this.groupListComponent);
 		for(Object selected : selecteds) {
 			this.removeGroup(ui.getAttachedObject(selected, Group.class));
 		}
 	}
 	
 	/** Show selecter for new groups. */
 	public void addNewGroup() {
 		GroupSelecterDialog dialog = new GroupSelecterDialog(ui, this);
 		dialog.init(ui.getRootGroup());
 		dialog.show();
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
 	
 	/** Remove the dialog from view. */
 	public void removeDialog() {
 		this.ui.removeDialog(this.dialogComponent);
 	}
 	
 //> UI HELPER METHODS
 	private Object find(String componentName) {
 		return this.ui.find(this.dialogComponent, componentName);
 	}
 	
 	/**
 	 * Finds a child component by name, and then sets its text value.
 	 * @param parentComponent The parent component to search within
 	 * @param componentName The name of the child component
 	 * @param value the value to set the child's TEXT attribute to
 	 */
 	private void setText(String componentName, String value) {
 		if(value == null) value = "";
 		this.ui.setText(find(componentName), value);
 	}
 	
 	/**
 	 * Set the current state of the active/dormant component.
 	 * @param contactDetails
 	 * @param active
 	 */
 	private void contactDetails_setActive(boolean active) {
 		this.ui.setSelected(find(COMPONENT_RADIO_BUTTON_ACTIVE), active);
 		this.ui.setSelected(find(COMPONENT_CONTACT_DORMANT), !active);
 		if (active) {
 			this.ui.setIcon(find(COMPONENT_LABEL_STATUS), Icon.ACTIVE);
 		} else {
 			this.ui.setIcon(find(COMPONENT_LABEL_STATUS), Icon.DORMANT);
 		}
 	}
 	
 	/**
 	 * Finds a child component by name, and gets its text value.
 	 * @param parentComponent The parent component to search within
 	 * @param componentName The name of the child component
 	 * @return the text attribute of the child
 	 */
 	private String getText(String componentName) {
 		return this.ui.getText(find(componentName));
 	}
 	
 	/**
 	 * @param contactDetails contact details dialog
 	 * @return the current state of the active component 
 	 */
 	private boolean contactDetails_getActive() {
 		return this.ui.isSelected(find(COMPONENT_RADIO_BUTTON_ACTIVE));
 	}
 
 	/**
 	 * Show the form to allow merging between a previously-created contact, and an attempted-newly-created contact.
 	 * TODO if we work out a good-looking way of doing this, we should implement it.  Currently this just warns the user that a contact with this number already exists.
 	 */
 	private void showMergeContactDialog(Contact oldContact, Object createContactForm) { // FIXME remove arguments from this method
 		this.ui.alert(InternationalisationUtils.getI18NString(MESSAGE_EXISTENT_CONTACT));
 	}
 }
