 /**
  * Created On: 10-Aug-07 3:51:24 PM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container;
 
 import java.awt.KeyboardFocusManager;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import com.thinkparity.codebase.StringUtil;
 import com.thinkparity.codebase.StringUtil.Separator;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.email.EMailBuilder;
 import com.thinkparity.codebase.email.EMailFormatException;
 import com.thinkparity.codebase.swing.SwingUtil;
 
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.profile.ProfileEMail;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.ophelia.model.user.UserUtils;
 
 import com.thinkparity.ophelia.browser.application.browser.display.provider.dialog.container.PublishContainerProvider;
 import com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar;
 import com.thinkparity.ophelia.browser.util.localization.Localization;
 
 
 /**
  * @author rob_masako@shaw.ca
  * @version $Revision$
  */
 public class PublishToUserJTextArea extends javax.swing.JTextArea
         implements PublishToUserControl {
 
     /** The maximum number of popup menu items <code>int</code>. */
     private static final int MAX_MENU_ITEMS;
 
     /** An instance of <code>UserUtils</code>. */
     private static final UserUtils USER_UTIL;
 
     static {
         MAX_MENU_ITEMS = 5;
         USER_UTIL = UserUtils.getInstance();
     }
 
     /** A <code>List</code> of all <code>Contact</code>s that are not team members. */
     private List<Contact> contactsNotOnTeam;
 
     /** A <code>List</code> of all <code>Contact</code>s that are also team members. */
     private List<Contact> contactsOnTeam;
 
     /** The container id <code>Long</code>. */
     private Long containerId;
 
     /** The <code>PublishContainerProvider</code>. */
     private PublishContainerProvider contentProvider;
 
     /** A <code>List</code> of <code>String</code> delimiters. */
     private List<String> delimiters;
 
     /** The <code>Localization</code>. */
     private Localization localization;
 
     /** The parent <code>Avatar</code>. */
     private final Avatar parent;
 
     /** The profile email addresses. */
     private List<String> profileEMailAddresses;
 
     /** The <code>PublishToUserPopupDelegate</code> popup delegate. */
     private final PublishToUserPopupDelegate publishToUserPopupDelegate;
 
     /** A <code>List</code> of all <code>TeamMember</code>s. */
     private List<TeamMember> teamMembers;
 
     /**
      * Creates PublishToUserJTextArea.
      */
     public PublishToUserJTextArea(final Avatar parent) {
         super();
         this.parent = parent;
         this.publishToUserPopupDelegate = new PublishToUserPopupDelegate();
         initEmailDelimiters();
         addDocumentListener();
         adjustTabBehavior();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishToUserControl#extractContacts()
      * 
      * Extract the list of contacts.
      * The list will not include contacts that are also team members.
      * 
      * @return A <code>List</code> of <code>Contact</code>.
      */
     public List<Contact> extractContacts() {
         final List<String> participants = extractParticipants();
         final List<Contact> extractedContacts = new ArrayList<Contact>();
         for (final String participant : participants) {
             final Contact contact = getContact(contactsNotOnTeam, participant.trim());
             if (null != contact && !extractedContacts.contains(contact)) {
                 extractedContacts.add(contact);
             }
         }
         return extractedContacts;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishToUserControl#extractEMails()
      * 
      * Extract the list of emails.
      * The list will include only valid emails and it will
      * not include emails that are also contacts.
      * 
      * @return A <code>List</code> of <code>EMail</code>.
      */
     public List<EMail> extractEMails() {
         final List<String> participants = extractParticipants();
         final List<EMail> extractedEMails = new ArrayList<EMail>();
         for (final String participant : participants) {
             if (isEMail(participant)) {
                 final EMail email = convertEMailAddressToEMail(participant.trim());
                 if (null != email &&
                         !extractedEMails.contains(email) &&
                         null == getContact(contactsNotOnTeam, email) &&
                         null == getContact(contactsOnTeam, email)) {
                     extractedEMails.add(email);
                 }
             }
             
         }
         return extractedEMails;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishToUserControl#extractTeamMembers()
      * 
      * Extract the list of team members.
      * 
      * @return A <code>List</code> of <code>TeamMember</code>.
      */
     public List<TeamMember> extractTeamMembers() {
         final List<String> participants = extractParticipants();
         final List<TeamMember> extractedTeamMembers = new ArrayList<TeamMember>();
         for (final String participant : participants) {
             final TeamMember teamMember = getTeamMember(teamMembers, contactsOnTeam, participant.trim());
             if (null != teamMember && !extractedTeamMembers.contains(teamMember)) {
                 extractedTeamMembers.add(teamMember);
             }
         }
         return extractedTeamMembers;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishToUserControl#getInputError()
      * 
      * Gets an input error localized message, or null if input is valid.
      * 
      * @return A <code>String</code> error.
      */
     public String getInputError() {
         final List<String> unknownNames = extractUnknownNames();
         final List<String> invalidEMails = extractInvalidEMails();
         String message = null;
         if (!unknownNames.isEmpty()) {
             message = localization.getString("ErrorUnknownName",
                     new Object[] {unknownNames.get(0)});
         } else if (!invalidEMails.isEmpty()) {
             message = localization.getString("ErrorInvalidEMail",
                     new Object[] {invalidEMails.get(0)});
         } else {
             final List<EMail> emails = extractEMails();
             for (final EMail email : emails) {
                 if (profileEMailAddresses.contains(email.toString().toLowerCase())) {
                     message = localization.getString("ErrorProfileEMail");
                     break;
                 }
             }
         }
         return message;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishToUserControl#isInputValid()
      * 
      * Determine if the input is valid.
      * 
      * @return A <code>Boolean</code>, true if the input is valid.
      */
     public Boolean isInputValid() {
         return (null == getInputError());
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishToUserControl#reload()
      * 
      * Reload.
      */
     public void reload() {
         teamMembers = readTeamMembers();
         contactsNotOnTeam = readContactsNotOnTeam(teamMembers);
         contactsOnTeam = readContactsOnTeam(contactsNotOnTeam);
         profileEMailAddresses = readEMailAddresses();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishToUserControl#setContainerId(java.lang.Long)
      * 
      * Set the container id.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      */
     public void setContainerId(final Long containerId) {
         this.containerId = containerId;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishToUserControl#setContentProvider(com.thinkparity.ophelia.browser.application.browser.display.provider.dialog.container.PublishContainerProvider)
      * 
      * Set the content provider.
      * 
      * @param contentProvider
      *            A <code>PublishContainerProvider</code>.
      */
     public void setContentProvider(final PublishContainerProvider contentProvider) {
         this.contentProvider = contentProvider;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishToUserControl#setLocalization(com.thinkparity.ophelia.browser.util.localization.Localization)
      * 
      * Set the localization.
      * 
      * @param localization
      *            A <code>Localization</code>.
      */
     public void setLocalization(final Localization localization) {
         this.localization = localization;
     }
 
     /**
      * Add a document listener.
      */
     private final void addDocumentListener() {
         final String listenerId = parent.getId().name() + "#publishToUserDocumentListener";
         DocumentListener listener = (DocumentListener) getClientProperty(listenerId);
         if (null == listener) {
             listener = new DocumentListener() {
                 public void changedUpdate(final DocumentEvent e) {
                     showPublishToUserPopup();
                 }
                 public void insertUpdate(final DocumentEvent e) {
                     showPublishToUserPopup();
                 }
                 public void removeUpdate(final DocumentEvent e) {
                     showPublishToUserPopup();
                 }
             };
         }
         putClientProperty(listenerId, listener);
         getDocument().removeDocumentListener(listener);
         getDocument().addDocumentListener(listener);
     }
 
     /**
      * Modify tab behavior so it moves to the next control.
      */
     private void adjustTabBehavior() {
         // Reset the text area to use the default tab keys.
         setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
         setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
     }
 
     /**
      * Convert a contact that is also a team member to a team member.
      * 
      * @param contact
      *            A <code>Contact</code> that is on the team.
      * @return A <code>TeamMember</code>, or null if there is no match.      
      */
     private TeamMember convertContactToTeamMember(final Contact contact) {
         final int index = USER_UTIL.indexOf(teamMembers, contact);
         if (index >= 0) {
             return teamMembers.get(index);
         } else {
             return null;
         }
     }
 
     /**
      * Convert an email address to an email.
      * Note that this method will handle emails in various forms,
      * for example: Joe(joe@abc.com)
      * 
      * @param emailAddress
      *            A <code>String</code> email address.
      * @return An <code>EMail</code>, or null if the address is invalid.  
      */
     private EMail convertEMailAddressToEMail(final String emailAddress) {
         EMail email;
         try {
             email = EMailBuilder.parse(emailAddress.trim());
         } catch (final EMailFormatException efx) {
             email = null;
         }
         return email;
     }
 
     /**
      * Extract the list of invalid emails as strings.
      * 
      * @return A <code>List</code> of invalid email <code>String</code>s.
      */
     private List<String> extractInvalidEMails() {
         final List<String> participants = extractParticipants();
         final List<String> extractedInvalidEMails = new ArrayList<String>();
         for (final String participant : participants) {
             if (isEMail(participant)) {
                 final String address = participant.trim();
                 final EMail email = convertEMailAddressToEMail(address);
                 if (null == email &&
                         !extractedInvalidEMails.contains(address)) {
                     extractedInvalidEMails.add(address);
                 }
             }
         }
         return extractedInvalidEMails;
     }
 
     /**
      * Extract the text for the last participant, ie. the
      * text after the last delimeter.
      * 
      * @return The last participant <code>String</code>, or null if none.
      */
     private String extractLastParticipant() {
         final String text = SwingUtil.extract(this, Boolean.TRUE);
         if (null == text) {
             return null;
         } else {
             final int offset = getOffsetLastDelimiter(text);
             if (0 > offset) {
                 return text;
             } else if (text.length()-1 == offset) {
                 return null;
             } else {
                 return text.substring(offset+1).trim();
             }
         }
     }
 
     /**
      * Extract the participants, including both e-mails and names,
      * as strings. Participants are separated by commas or semicolons.
      * 
      * @return A <code>List</code> of extracted <code>String</code>s.
      */
     private List<String> extractParticipants() {
         final String text = SwingUtil.extract(this, Boolean.TRUE);
         final List<String> participants;
         if (null == text) {
             participants = Collections.emptyList();
         } else {
             participants = StringUtil.tokenize(text, delimiters,
                     new ArrayList<String>());
         }
         return participants;
     }
 
     /**
      * Extract the list of unknown names as strings.
      * 
      * @return A <code>List</code> of unknown name <code>String</code>s.
      */
     private List<String> extractUnknownNames() {
         final List<String> participants = extractParticipants();
         final List<String> extractedUnknownNames = new ArrayList<String>();
         for (final String participant : participants) {
             if (!isEMail(participant)) {
                 final String name = participant.trim();
                 if (!extractedUnknownNames.contains(name) &&
                        null == getTeamMember(teamMembers, contactsOnTeam, name) &&
                        null == getContact(contactsNotOnTeam, name)) {
                     extractedUnknownNames.add(name);
                 }
             }
         }
         return extractedUnknownNames;
     }
 
     /**
      * Locate the contact from the list that matches the email.
      * 
      * @param contacts
      *            A <code>List</code> of <code>Contact</code>.
      * @param email
      *            An <code>EMail</code>.
      * @return A <code>Contact</code>, or null if there is no match.
      */
     private Contact getContact(final List<Contact> contacts, final EMail email) {
         if (null != email) {
             final String address = email.toString();
             for (final Contact contact : contacts) {
                 if (isActivatedEMail(contact) && getEMailString(contact).equalsIgnoreCase(address)) {
                     return contact;
                 }
             }
         }
         return null;
     }
 
     /**
      * Locate the contact from the list that matches the text.
      * The match can be by name or email.
      * 
      * @param contacts
      *            A <code>List</code> of <code>Contact</code>.
      * @param text
      *            A text <code>String</code>.
      * @return A <code>Contact</code>, or null if there is no match.
      */
     private Contact getContact(final List<Contact> contacts, final String text) {
         if (isEMail(text)) {
             return getContact(contacts, convertEMailAddressToEMail(text));
         } else {
             for (final Contact contact : contacts) {
                 if (contact.getName().equalsIgnoreCase(text)) {
                     return contact;
                 }
             }
         }
         return null;
     }
 
     /**
      * Get the email string of a contact.
      * 
      * @param contact
      *            A <code>Contact</code>.
      * @return The email <code>String</code>.
      */
     private String getEMailString(final Contact contact) {
         if (isActivatedEMail(contact)) {
             return contact.getEmails().get(0).toString();
         } else {
             return null;
         }
     }
 
     /**
      * Get the JScrollPane.
      * It is assumed that the JTextArea is embedded in a JScrollPane.
      * 
      * @return The <code>JScrollPane</code>.
      */
     private javax.swing.JScrollPane getJScrollPane() {
         Assert.assertNotNull("Null parent in PublishToUserJTextArea.", getParent());
         Assert.assertNotNull("Null grandparent in PublishToUserJTextArea.", getParent().getParent());
         Assert.assertTrue("No JScrollPane in PublishToUserJTextArea.",
                 getParent().getParent() instanceof javax.swing.JScrollPane);
         return (javax.swing.JScrollPane)getParent().getParent();
     }
 
     /**
      * Get the menu location.
      * 
      * @return The menu location <code>Point</code>.
      */
     private Point getMenuLocation() {
         javax.swing.JScrollPane jScrollPane = getJScrollPane();
         return new Point(jScrollPane.getX(), jScrollPane.getY() + jScrollPane.getHeight());
     }
 
     /**
      * Get the menu width.
      * 
      * @return The menu width <code>int</code>.
      */
     private int getMenuWidth() {
         // The menu width is slightly less than the width of the control.
         return getJScrollPane().getWidth() - 2;
     }
 
     /**
      * Get the offset of the last delimiter.
      * 
      * @param text
      *            A text <code>String</code>.
      * @return The <code>int</code> offset of the last delimiter, or -1 if none.
      */
     private int getOffsetLastDelimiter(final String text) {
         int offset = -1;
         if (null != text && text.length() > 0) {
             for (final String delimiter : delimiters) {
                 int delimiterOffset = text.lastIndexOf(delimiter);
                 if (delimiterOffset > offset) {
                     offset = delimiterOffset;
                 }
             }
         }
         return offset;
     }
 
     /**
      * Get a list of users that match the provided text.
      * 
      * @param matchText
      *            A <code>String</code>, users starting with this are processed.
      * @param filterTeamMembers
      *            A <code>List</code> of <code>TeamMember</code> to filter out.
      * @param filterContacts
      *            A <code>List</code> of <code>Contact</code> to filter out.
      * @return A <code>List</code> of <code>PublishToUser</code>.
      */
     private List<PublishToUser> getPublishToUsers(final String matchText,
             final List<TeamMember> filterTeamMembers,
             final List<Contact> filterContacts) {
         if (null == matchText) {
             return Collections.emptyList();
         }
         final List<PublishToUser> publishToUsers = new ArrayList<PublishToUser>();
         // add team member names if there is a match on name
         for (final TeamMember teamMember : teamMembers) {
             if (isMatchName(matchText, teamMember) && !filterTeamMembers.contains(teamMember)) {
                 publishToUsers.add(new PublishToUserImpl(teamMember));
             }
         }
         // add contact names if there is a match on name
         for (final Contact contact : contactsNotOnTeam) {
             if (isMatchName(matchText, contact) && !filterContacts.contains(contact)) {
                 publishToUsers.add(new PublishToUserImpl(contact, Boolean.FALSE));
             }
         }
         // add contact emails if there is a match on email (even if the contact was already added)
         for (final Contact contact : contactsOnTeam) {
             final TeamMember teamMember = convertContactToTeamMember(contact);
             if (isMatchEMail(matchText, contact) && !filterTeamMembers.contains(teamMember)) {
                 publishToUsers.add(new PublishToUserImpl(contact, Boolean.TRUE));
             }
         }
         for (final Contact contact : contactsNotOnTeam) {
             if (isMatchEMail(matchText, contact) && !filterContacts.contains(contact)) {
                 publishToUsers.add(new PublishToUserImpl(contact, Boolean.TRUE));
             }
         }
         return publishToUsers;
     }
 
     /**
      * Locate the team member from the list that matches the text.
      * The match can be by name or email. (Email applies in the case that the team
      * member is also a contact.)
      * 
      * @param teamMembers
      *            A <code>List</code> of <code>TeamMember</code>.
      * @param contactsOnTeam
      *            A <code>List</code> of <code>Contact</code>.
      * @param text
      *            A text <code>String</code>.
      * @return A <code>TeamMember</code>, or null if there is no match.
      */
     private TeamMember getTeamMember(final List<TeamMember> teamMembers,
             final List<Contact> contactsOnTeam, final String text) {
         if (isEMail(text)) {
             final Contact contact = getContact(contactsOnTeam, convertEMailAddressToEMail(text));
             if (null != contact) {
                 return convertContactToTeamMember(contact);
             }
         } else {
             for (final TeamMember teamMember : teamMembers ) {
                 if (teamMember.getName().equalsIgnoreCase(text)) {
                     return teamMember;
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Hide the publish to user popup.
      */
     private void hidePublishToUserPopup() {
         if (publishToUserPopupDelegate.isVisible()) {
             final Point popupLocation = getMenuLocation();
             publishToUserPopupDelegate.hide();
             parent.paintImmediately(popupLocation.x, popupLocation.y,
                     parent.getWidth() - popupLocation.x, parent.getHeight() - popupLocation.y);
         }
     }
 
     /**
      * Initialize the email delimiters.
      */
     private void initEmailDelimiters() {
         this.delimiters = new ArrayList<String>(2);
         delimiters.add(",");
         delimiters.add(";");
     }
 
     /**
      * Determine if the contact has an activated email.
      * 
      * @param contact
      *            A <code>Contact</code>.
      * @return A <code>boolean</code>, true if the contact has an activated email.  
      */
     private Boolean isActivatedEMail(final Contact contact) {
         return (0 < contact.getEmailsSize());
     }
 
     /**
      * Determine if the text represents an email.
      * It is considered an email if it contains a '@'.
      * 
      * @param text
      *            A text <code>String</code>.
      * @return A <code>boolean</code>, true if the text is an email.   
      */
     private Boolean isEMail(final String text) {
         return text.contains("@");
     }
 
     /**
      * Determine if the text string matches the provided text.
      * 
      * @param matchText
      *            A match text <code>String</code>.
      * @param text
      *            A text <code>String</code>.
      * @return A <code>boolean</code>, true if there is a match.     
      */
     private boolean isMatch(final String matchText, final String text) {
         if (text.length() >= matchText.length()) {
             final String clipText = text.substring(0, matchText.length());
             if (clipText.equalsIgnoreCase(matchText)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Determine if the contact email matches the provided text.
      * 
      * @param matchText
      *            A match text <code>String</code>.
      * @param contact
      *            A <code>Contact</code>.
      * @return A <code>boolean</code>, true if the contact is a match.     
      */
     private boolean isMatchEMail(final String matchText, final Contact contact) {
         if (isActivatedEMail(contact)) {
             return isMatch(matchText, getEMailString(contact));
         }
         return false;
     }
 
     /**
      * Determine if the name string matches the provided text.
      * The match test is done on all names (first, middle, last etc.)
      * 
      * @param matchText
      *            A match text <code>String</code>.
      * @param fullName
      *            A name <code>String</code>.
      * @return A <code>boolean</code>, true if there is a match.     
      */
     private boolean isMatchName(final String matchText, final String fullName) {
         final List<String> names = StringUtil.tokenize(fullName, Separator.Space,
                 new ArrayList<String>());
         for (final String name : names) {
             if (isMatch(matchText, name.trim())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Determine if the user name matches the provided text.
      * 
      * @param matchText
      *            A match text <code>String</code>.
      * @param user
      *            A <code>User</code>.
      * @return A <code>boolean</code>, true if the user is a match.     
      */
     private boolean isMatchName(final String matchText, final User user) {
         return isMatchName(matchText, user.getName());
     }
 
     /**
      * Read contacts, excluding contacts that are also team members.
      * 
      * @param teamMembers
      *          A <code>List</code> of <code>TeamMember</code>s.
      * @return A <code>List</code> of <code>Contact</code>s.
      */
     private List<Contact> readContactsNotOnTeam(final List<TeamMember> teamMembers) {
         return contentProvider.readPublishToContacts(teamMembers);
     }
 
     /**
      * Read contacts.
      * 
      * @param nonTeamMembers
      *          A <code>List</code> of non-team member <code>Contact</code>s.
      * @return A <code>List</code> of <code>Contact</code>s.
      */
     private List<Contact> readContactsOnTeam(final List<Contact> nonTeamMembers) {
         final List<Contact> contacts = contentProvider.readPublishToContacts();
         for (final Contact nonTeamMember : nonTeamMembers) {
             USER_UTIL.remove(contacts, nonTeamMember);
         }
         return contacts;
     }
 
     /**
      * Read the profile email addresses from the content provider.
      * 
      * @return A <code>List&lt;String&gt;</code>.
      */
     private List<String> readEMailAddresses() {
         final List<ProfileEMail> emails = readEMails();
         final List<String> emailAddresses = new ArrayList<String>(emails.size());
         for (final ProfileEMail email : emails) {
             emailAddresses.add(email.getEmail().toString().toLowerCase());
         }
         return emailAddresses;
     }
 
     /**
      * Read the profile emails from the content provider.
      * 
      * @return A <code>List&lt;ProfileEMail&gt;</code>.
      */
     private List<ProfileEMail> readEMails() {
         return contentProvider.readEMails();
     }
 
     /**
      * Read the team members for the container.
      * 
      * @return A <code>List</code> of <code>TeamMember</code>s.
      */
     private List<TeamMember> readTeamMembers() {
         return contentProvider.readPublishToTeam(containerId);
     }
 
     /**
      * Show the publish to user popup.
      */
     private void showPublishToUserPopup() {
         final List<PublishToUser> publishToUsers = getPublishToUsers(
                 extractLastParticipant(), extractTeamMembers(),
                 extractContacts());
         // show the popup if it is not visible or it changed.
         if (!publishToUserPopupDelegate.isVisible()
                 || !publishToUserPopupDelegate.getPublishToUsers().equals(publishToUsers)) {
             hidePublishToUserPopup();
             publishToUserPopupDelegate.setMaxMenuItems(MAX_MENU_ITEMS);
             publishToUserPopupDelegate.setMenuWidth(getMenuWidth());
             publishToUserPopupDelegate.initialize(parent, getMenuLocation().x, getMenuLocation().y);
             publishToUserPopupDelegate.setPublishToUsers(publishToUsers);
             publishToUserPopupDelegate.show();
             requestFocusInWindow();
         }
     }
 
     /**
      * Replace the text of the last participant.
      * 
      * @param participant
      *            A participant <code>String</code>.
      */
     private void updateLastParticipant(final String participant) {
         final String text = SwingUtil.extract(this, Boolean.TRUE);
         final int offset = getOffsetLastDelimiter(text);
         if (null == text || 0 >= offset) {
             final StringBuffer participants = new StringBuffer(participant.trim()).append(", ");
             setText(participants.toString());
         } else {
             final StringBuffer participants = new StringBuffer(text.substring(0, offset).trim())
                     .append(", ").append(participant.trim()).append(", ");
             setText(participants.toString());
         }
     }
 
     /**
      * The implementation of the PublishToUser.
      * A PublishToUser is a team member or contact that will be listed in
      * the popup associated with this control.
      * 
      * @author rob_masako@shaw.ca
      * @version $Revision$
      */
     private class PublishToUserImpl implements PublishToUser {
 
         /** A <code>Contact</code>. */
         private Contact contact;
 
         /** A <code>Boolean</code> indicating the email will be displayed. */
         private Boolean displayEMail;
 
         /** A <code>TeamMember</code>. */
         private TeamMember teamMember;
 
         /**
          * Create a PublishToUserImpl.
          * 
          * @param contact
          *            A <code>Contact</code>.
          * @param displayEMail
          *            A <code>Boolean</code> indicating the email will be displayed.
          */
         private PublishToUserImpl(final Contact contact,
                 final Boolean displayEMail) {
             super();
             this.contact = contact;
             this.displayEMail = displayEMail;
         }
 
         /**
          * Create a PublishToUserImpl.
          * 
          * @param teamMember
          *            A <code>TeamMember</code>.
          */
         private PublishToUserImpl(final TeamMember teamMember) {
             super();
             this.teamMember = teamMember;
         }
 
         /**
          * @see java.lang.Object#equals(java.lang.Object)
          */
         @Override
         public boolean equals(Object obj) {
             if (null != obj && obj instanceof PublishToUserImpl) {
                 final PublishToUserImpl other = (PublishToUserImpl) obj;
                 if (isSetTeamMember() && other.isSetTeamMember()) {
                     return teamMember.equals(other.teamMember);
                 } else if (isSetContact() && other.isSetContact()) {
                     return contact.equals(other.contact)
                             && displayEMail.equals(other.displayEMail);
                 }
             }
             return false;
         }
 
         /**
          * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishToUser#getAction()
          */
         public Action getAction() {
             return new AbstractAction() {
                 public void actionPerformed(final ActionEvent e) {
                     updateLastParticipant(getDisplayText());
                 }
             };
         }
 
         /**
          * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.dialog.container.PublishToUser#getPopupText()
          */
         public String getPopupText() {
             String text;
             if (isSetContact()) {
                 if (isDisplayEMail()) {
                     text = localization.getString("EmailText",
                             new Object[] {getEMailString(contact), contact.getName()} );
                 } else {
                     text = localization.getString("UserName",
                             new Object[] {contact.getName(), contact.getTitle(), contact.getOrganization()} );
                 }
                 return SwingUtil.limitWidthWithEllipsis(text, getPopupTextWidth(), getGraphics());
             } else if (isSetTeamMember()) {
                 text = localization.getString("UserName",
                         new Object[] {teamMember.getName(), teamMember.getTitle(), teamMember.getOrganization()} );
                 return SwingUtil.limitWidthWithEllipsis(text, getPopupTextWidth(), getGraphics());
             } else {
                 Assert.assertUnreachable("Inconsistent publish to user state.");
             }
             return null;
         }
 
         /**
          * Get the display text (for display in the text area).
          * 
          * @return The display text <code>String</code>.
          */
         private String getDisplayText() {
             if (isSetContact()) {
                 return contact.getName();
             } else if (isSetTeamMember()) {
                 return teamMember.getName();
             } else {
                 Assert.assertUnreachable("Inconsistent publish to user state.");
             }
             return null;
         }
 
         /**
          * Get the popup text width.
          * 
          * @return The popup text width <code>int</code>.
          */
         private int getPopupTextWidth() {
             return getMenuWidth() - 35;
         }
 
         /**
          * Determine if the email will be displayed.
          * 
          * @return True if the email will be displayed.
          */
         private Boolean isDisplayEMail() {
             return displayEMail;
         }
 
         /**
          * Determine if the contact is set.
          * 
          * @return True if the contact is set.
          */
         private Boolean isSetContact() {
             return null != contact;
         }
 
         /**
          * Determine if the team member is set.
          * 
          * @return True if the team member is set.
          */
         private Boolean isSetTeamMember() {
             return null != teamMember;
         }
     }
 }
