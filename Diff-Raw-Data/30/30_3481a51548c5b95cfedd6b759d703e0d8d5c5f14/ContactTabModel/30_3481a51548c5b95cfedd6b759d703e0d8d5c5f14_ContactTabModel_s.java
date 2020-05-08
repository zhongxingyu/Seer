 /*
  * Created On: Jun 21, 2006 11:32:04 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.contact;
 
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.user.User;
 import com.thinkparity.codebase.sort.DefaultComparator;
 import com.thinkparity.codebase.sort.StringComparator;
 
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortBy;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortByDelegate;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabPanelModel;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortBy.SortDirection;
 import com.thinkparity.ophelia.browser.application.browser.display.provider.tab.contact.ContactProvider;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.contact.ContactTabPanel;
 import com.thinkparity.ophelia.model.contact.IncomingInvitation;
 import com.thinkparity.ophelia.model.contact.OutgoingInvitation;
 
 /**
  * <b>Title:</b>thinkParity Contact Tab Model<br>
  * <b>Description:</b>The tab model for a contact<br>
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class ContactTabModel extends TabPanelModel implements
         TabAvatarSortByDelegate {
 
     /** The <code>ContactTabActionDelegate</code>. */
     private final ContactTabActionDelegate actionDelegate;
 
     /** The <code>ContactTabPopupDelegate</code>. */
     private final ContactTabPopupDelegate popupDelegate;
 
     /** A list of the current sort orderings. */
     private final List<SortBy> sortedBy;
 
     /**
      * Create ContactTabModel.
      * 
      */
     ContactTabModel() {
         super();
         this.actionDelegate = new ContactTabActionDelegate(this);
         this.popupDelegate= new ContactTabPopupDelegate(this);
         this.sortedBy = new ArrayList<SortBy>();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortByDelegate#getSortBy()
      *
      */
     public List<TabAvatarSortBy> getSortBy() {
         final List<TabAvatarSortBy> sortBy = new ArrayList<TabAvatarSortBy>();
         for (final SortBy sortByValue : SortBy.values()) {
             sortBy.add(new TabAvatarSortBy() {
                 public Action getAction() {
                     return new AbstractAction() {
                         public void actionPerformed(final ActionEvent e) {
                             applySort(sortByValue);
                         }
                     };
                 }
                 public String getText() {
                     return getString(sortByValue);
                 }
                 public SortDirection getDirection() {
                     return getSortDirection(sortByValue);
                 }
             });
         }
         return sortBy;
     }
 
     /**
      * Debug the contact avatar.
      * 
      */
     @Override
     protected void debug() {
         logger.logDebug("{0} container panels.", panels.size());
         logger.logDebug("{0} filtered panels.", filteredPanels.size());
         logger.logDebug("{0} visible panels.", visiblePanels.size());
         logger.logDebug("{0} model elements.", listModel.size());
         final TabPanel[] listModelPanels = new TabPanel[listModel.size()];
         listModel.copyInto(listModelPanels);
         for (final TabPanel listModelPanel : listModelPanels) {
             logger.logVariable("listModelPanel.getId()", listModelPanel.getId());
         }
         logger.logDebug("Search expression:  {0}", searchExpression);
         logger.logDebug("{0} search result hits.", searchResults.size());
     }
 
     /**
      * Initialize the contact model
      * <ol>
      * <li>Load the contacts from the provider.
      * <li>Synchronize the data with the model.
      * <ol>
      */
     @Override
     protected void initialize() {
         debug();
         clearPanels();
         final List<IncomingInvitation> incoming = readIncomingInvitations();
         for (final IncomingInvitation invitation : incoming) {
             addPanel(invitation);
         }
         final List<OutgoingInvitation> outgoing = readOutgoingInvitations();
         for (final OutgoingInvitation invitation : outgoing) {
             addPanel(invitation);
         }
         final List<Contact> contacts = readContacts();
         for (final Contact contact : contacts) {
             addPanel(contact);
         }
         applySort(getInitialSort());
         debug();
     }
 
     /**
      * Obtain the popup delegate.
      * 
      * @return A <code>ContainerTabPopupDelegate</code>.
      */
     ContactTabPopupDelegate getPopupDelegate() {
         return popupDelegate;
     }
 
     void syncContact(final JabberId contactId, final Boolean remote) {
         debug();
         final Contact contact = read(contactId);
         // remove the container from the panel list
         if (null == contact) {
             removePanel(contactId, true);
         } else {
             final int panelIndex = lookupIndex(contact.getId());
             if (-1 < panelIndex) {
                 // if the reload is the result of a remote event add the container
                 // at the top of the list; otherwise add it in the same location
                 // it previously existed
                 removePanel(contactId, false);
                 if (remote) {
                     addPanel(0, contact);
                 } else {
                     addPanel(panelIndex, contact);
                 }
             } else {
                 addPanel(0, contact);
             }
         }
         synchronize();
         debug();
     }
 
     void syncIncomingInvitation(final Long invitationId, final Boolean remote) {
         debug();
         final IncomingInvitation invitation = readIncomingInvitation(invitationId);
         // remove the container from the panel list
         if (null == invitation) {
             removePanel(invitationId, true);
         } else {
             final int panelIndex = lookupIndex(invitation.getId());
             if (-1 < panelIndex) {
                 // if the reload is the result of a remote event add the container
                 // at the top of the list; otherwise add it in the same location
                 // it previously existed
                 removePanel(invitationId, false);
                 if (remote) {
                     addPanel(0, invitation);
                 } else {
                     addPanel(panelIndex, invitation);
                 }
             } else {
                 addPanel(0, invitation);
             }
         }
         synchronize();
         debug();
     }
 
     void syncOutgoingInvitation(final Long invitationId, final Boolean remote) {
         debug();
         final OutgoingInvitation invitation = readOutgoingInvitation(invitationId);
         // remove the container from the panel list
         if (null == invitation) {
             removePanel(invitationId, true);
         } else {
             final int panelIndex = lookupIndex(invitation.getId());
             if (-1 < panelIndex) {
                 // if the reload is the result of a remote event add the container
                 // at the top of the list; otherwise add it in the same location
                 // it previously existed
                 removePanel(invitationId, false);
                 if (remote) {
                     addPanel(0, invitation);
                 } else {
                     addPanel(panelIndex, invitation);
                 }
             } else {
                 addPanel(0, invitation);
             }
         }
         synchronize();
         debug();
 
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabPanelModel#lookupId(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel)
      */
     @Override
     protected Object lookupId(final TabPanel tabPanel) {
         final ContactTabPanel panel = (ContactTabPanel)tabPanel;
         if (panel.isSetContact()) {
             return panel.getContact().getId();
         } else if (panel.isSetIncoming()) {
             return panel.getIncoming().getId();
         } else if (panel.isSetOutgoing()) {
             return (panel.getOutgoing().getId());
         } else {
             throw Assert.createUnreachable("Unknown contact panel type.");
         }
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabPanelModel#lookupPanel(java.lang.Object)
      */
     @Override
     protected TabPanel lookupPanel(final Object uniqueId) {
         final int panelIndex;
         if (uniqueId instanceof JabberId) {
             final JabberId contactId = (JabberId)uniqueId;
             panelIndex = lookupIndex(contactId); 
         } else if (uniqueId instanceof Long) {
             final Long invitationId = (Long)uniqueId;
             panelIndex = lookupIndex(invitationId); 
         } else {
             throw Assert.createUnreachable("Unknown contact id type.");
         }
         if (-1 == panelIndex)
             return null;
         else
             return panels.get(panelIndex);
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabPanelModel#readSearchResults()
      */
     @Override
     protected List<? extends Object> readSearchResults() {
         return ((ContactProvider) contentProvider).search(searchExpression);
     }
 
     /**
      * Add a contact to the end of the panels list.
      * 
      * @param contact
      *            A <code>Contact</code>.
      */
     private void addPanel(final Contact contact) {
         addPanel(panels.size() == 0 ? 0 : panels.size(), contact);
     }
 
     /**
      * Add an incoming invitation to the end of the panels list.
      * 
      * @param invitation
      *            An <code>IncomingInvitation</code>.
      */
     private void addPanel(final IncomingInvitation invitation) {
         addPanel(panels.size() == 0 ? 0 : panels.size(), invitation);
     }
 
     /**
      * Add a contact to the panels list.
      * 
      * @param index
      *            The index at which to add the contact.
      * @param contact
      *            A <code>Contact</code>.
      */
     private void addPanel(final int index, final Contact contact) {
         panels.add(index, toDisplay(contact));
     }
 
     /**
      * Add an incoming invitation to the panels list.
      * 
      * @param index
      *            The index at which to add the invitation.
      * @param invitation
      *            An <code>IncomingInvitation</code>.
      */
     private void addPanel(final int index, final IncomingInvitation invitation) {
         panels.add(index, toDisplay(invitation));
     }
 
     /**
      * Add an outgoing invitation to the panels list.
      * 
      * @param index
      *            The index at which to add the invitation.
      * @param invitation
      *            An <code>OutgoingInvitation</code>.
      */
     private void addPanel(final int index, final OutgoingInvitation invitation) {
         panels.add(index, toDisplay(invitation));
     }
     
     /**
      * Add an outgoing invitation to the end of the panels list.
      * 
      * @param index
      *            The index at which to add the invitation.
      * @param invitation
      *            An <code>IncomingInvitation</code>.
      */
     private void addPanel(final OutgoingInvitation invitation) {
         addPanel(panels.size() == 0 ? 0 : panels.size(), invitation);
     }
 
     /**
      * Apply the sort to the filtered list of panels.
      *
      */
     protected void applySort() {
         final DefaultComparator<TabPanel> comparator = new DefaultComparator<TabPanel>();
         for (final SortBy sortBy : sortedBy) {
             comparator.add(sortBy);
         }
         Collections.sort(filteredPanels, comparator);
     }
 
     /**
      * Apply an ordering to the panels.
      * 
      * @param ordering
      *            An <code>Ordering</code>.
      */
     private void applySort(final SortBy sortBy) {
         debug();
         if (isSortApplied(sortBy)) {
             sortBy.ascending = !sortBy.ascending;
         }
         sortedBy.clear();
         sortedBy.add(sortBy);
         persistence.set(sortByKey, sortBy);
         persistence.set(sortAscendingKey, sortBy.ascending);
         synchronize();
     }
     
     /**
      * Get the initial sort from persistence.
      * 
      * @return A <code>SortBy</code>.
      */
     private SortBy getInitialSort() {
        final SortBy sortBy = (SortBy)persistence.get(sortByKey, SortBy.NAME);
         sortBy.ascending = persistence.get(sortAscendingKey, true);
         return sortBy;
     }
     
     /**
      * Get the sort direction.
      * 
      * @param sortBy
      *            A <code>SortBy</code>.
      * @return A <code>SortDirection</code>.        
      */
     public SortDirection getSortDirection(final SortBy sortBy) {
         if (isSortApplied(sortBy)) {
             if (sortBy.ascending) {
                 return SortDirection.ASCENDING;
             } else {
                 return SortDirection.DESCENDING;
             }
         } else {
             return SortDirection.NONE;
         }
     }
 
     /**
      * Obtain a localized string for an ordering.
      * 
      * @param sortBy
      *            A <code>SortBy</code>.
      * @return A localized <code>String</code>.
      */
     private String getString(final SortBy sortBy) {
         return localization.getString(sortBy);
     }
 
     /**
      * Determine if an ordering is applied.
      * 
      * @param ordering
      *            An <code>Ordering</code>.
      * @return True if it is applied false otherwise.
      */
     private boolean isSortApplied(final SortBy sortBy) {
         debug();
         return sortedBy.contains(sortBy);
     }
 
     private int lookupIndex(final JabberId contactId) {
         Contact contact;
         for (int i = 0; i < panels.size(); i++) {
             contact = ((ContactTabPanel) panels.get(i)).getContact();
             if (null != contact && contact.getId().equals(contactId))
                 return i;
         }
         return -1;
     }
 
     private int lookupIndex(final Long invitationId) {
         ContactTabPanel panel;
         for (int i = 0; i < panels.size(); i++) {
             panel = ((ContactTabPanel) panels.get(i));
             if (panel.isSetIncoming())
                 if (panel.getIncoming().getId().equals(invitationId))
                     return i;
             if (panel.isSetOutgoing())
                 if (panel.getOutgoing().getId().equals(invitationId))
                     return i;
         }
         return -1;
     }
 
     private Contact read(final JabberId contactId) {
         return ((ContactProvider) contentProvider).readContact(contactId);
     }
 
     /**
      * Read the contacts from the provider.
      * 
      * @return The contacts.
      */
     private List<Contact> readContacts() {
         return ((ContactProvider) contentProvider).readContacts();
     }
 
     private IncomingInvitation readIncomingInvitation(final Long invitationId) {
         return ((ContactProvider) contentProvider).readIncomingInvitation(invitationId);
     }
 
     /**
      * Read the incoming invitations.
      * 
      * @return A <code>List</code> of <code>IncomingInvitation</code>.
      */
     private List<IncomingInvitation> readIncomingInvitations() {
         return ((ContactProvider) contentProvider).readIncomingInvitations();
     }
 
     private OutgoingInvitation readOutgoingInvitation(final Long invitationId) {
         return ((ContactProvider) contentProvider).readOutgoingInvitation(invitationId);
     }
 
     /**
      * Read the incoming invitations.
      * 
      * @return A <code>List</code> of <code>OutgoingInvitation</code>.
      */
     private List<OutgoingInvitation> readOutgoingInvitations() {
         return ((ContactProvider) contentProvider).readOutgoingInvitations();
     }
 
     /**
      * Read a user from the content provider.
      * 
      * @param jabberId
      *            A jabber id.
      * @return A user.
      */
     private User readUser(final JabberId jabberId) {
         return (User) ((ContactProvider) contentProvider).readUser(jabberId);
     }
 
     private void removePanel(final JabberId contactId,
             final boolean removeExpandedState) {
         final int panelIndex = lookupIndex(contactId);
         if (removeExpandedState) {
             final TabPanel panel = panels.remove(panelIndex);
             expandedState.remove(panel);
         } else {
             panels.remove(panelIndex);
         }
     }
 
     private void removePanel(final Long invitationId,
             final boolean removeExpandedState) {
         final int panelIndex = lookupIndex(invitationId);
         if (removeExpandedState) {
             final TabPanel panel = panels.remove(panelIndex);
             expandedState.remove(panel);
         } else {
             panels.remove(panelIndex);
         }
     }
 
     /**
      * Obtain the contact display cell for a contact.
      * 
      * @param contact
      *            A contact.
      * @return A contact display cell.
      */
     private TabPanel toDisplay(final Contact contact) {
         final ContactTabPanel panel = new ContactTabPanel(session);
         panel.setActionDelegate(actionDelegate);
         panel.setPanelData(contact);
         panel.setPopupDelegate(popupDelegate);
         panel.setExpanded(isExpanded(panel));
         panel.setTabDelegate(this);
         return panel;
     }
 
     /**
      * Obtain the contact display cell for a contact.
      * 
      * @param invitation
      *            An <code>OutgoingInvitation</code>.
      * @return A contact display cell.
      */
     private TabPanel toDisplay(final IncomingInvitation invitation) {
         final ContactTabPanel panel = new ContactTabPanel(session);
         panel.setActionDelegate(actionDelegate);
         panel.setPanelData(invitation, readUser(invitation.getInvitedBy()));
         panel.setPopupDelegate(popupDelegate);
         panel.setExpanded(isExpanded(panel));
         panel.setTabDelegate(this);
         return panel;
     }
 
     /**
      * Obtain the contact display cell for a contact.
      * 
      * @param invitation
      *            An <code>OutgoingInvitation</code>.
      * @return A contact display cell.
      */
     private TabPanel toDisplay(final OutgoingInvitation invitation) {
         final ContactTabPanel panel = new ContactTabPanel(session);
         panel.setActionDelegate(actionDelegate);
         panel.setPanelData(invitation);
         panel.setPopupDelegate(popupDelegate);
         panel.setExpanded(isExpanded(panel));
         panel.setTabDelegate(this);
         return panel;
     }
 
     private enum SortBy implements Comparator<TabPanel> {
     
         NAME(true), ORGANIZATION(true), TITLE(true);
 
         /** An ascending <code>StringComparator</code>. */
         private static final StringComparator STRING_COMPARATOR_ASC;
 
         /** A descending <code>StringComparator</code>. */
         private static final StringComparator STRING_COMPARATOR_DESC;
 
         static {
             STRING_COMPARATOR_ASC = new StringComparator(Boolean.TRUE);
             STRING_COMPARATOR_DESC = new StringComparator(Boolean.FALSE);
         }
         
         /** Whether or not to sort in ascending order. */
         private boolean ascending;
 
         /**
          * Create Ordering.
          *
          * @param ascending
          */
         private SortBy(final boolean ascending) {
             this.ascending = ascending;
         }
         
         /**
          * Apply a default ordering.
          */
         private int compareDefault(final ContactTabPanel p1, final ContactTabPanel p2) {
             return ascending
                 ? STRING_COMPARATOR_ASC.compare(
                         p1.getContact().getName(),
                         p2.getContact().getName())
                 : STRING_COMPARATOR_DESC.compare(
                         p1.getContact().getName(),
                         p2.getContact().getName());
         }
 
         /**
          * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
          * 
          */
         public int compare(final TabPanel o1, final TabPanel o2) {
             final ContactTabPanel p1 = (ContactTabPanel) o1;
             final ContactTabPanel p2 = (ContactTabPanel) o2;
             int result = 0;
             if (p1.isSetContact()) {
                 if (p2.isSetContact()) {
                     switch (this) {
                     case NAME:
                         // note the lack of multiplier
                         return ascending
                             ? STRING_COMPARATOR_ASC.compare(
                                     p1.getContact().getName(),
                                     p2.getContact().getName())
                             : STRING_COMPARATOR_DESC.compare(
                                     p1.getContact().getName(),
                                     p2.getContact().getName());
                     case TITLE:
                         // note the lack of multiplier
                         result =  ascending
                             ? STRING_COMPARATOR_ASC.compare(
                                     p1.getContact().getTitle(),
                                     p2.getContact().getTitle())
                             : STRING_COMPARATOR_DESC.compare(
                                     p1.getContact().getTitle(),
                                     p2.getContact().getTitle());
                         if (0 == result) {
                             result = compareDefault(p1, p2);
                         }
                         return result;
                     case ORGANIZATION:
                         // note the lack of multiplier
                         result = ascending
                             ? STRING_COMPARATOR_ASC.compare(
                                     p1.getContact().getOrganization(),
                                     p2.getContact().getOrganization())
                             : STRING_COMPARATOR_DESC.compare(
                                     p1.getContact().getOrganization(),
                                     p2.getContact().getOrganization());
                         if (0 == result) {
                             result = compareDefault(p1, p2);
                         }
                         return result;            
                     default:
                         throw Assert.createUnreachable("Unknown ordering.");
                     }
                 } else {
                     return 1;
                 }
             } else {
                 if (p2.isSetContact()) {
                     return -1;
                 } else {
                     return 1;
                 }
             }
         }
     }
 }
