 /*
  * Manage.java
  *
  * Created on March 25, 2006, 11:10 AM
  */
 
 package com.thinkparity.browser.application.browser.display.avatar.contact;
 
 import com.thinkparity.browser.application.browser.display.avatar.*;
 import java.awt.Color;
 
 import javax.swing.DefaultListModel;
 import javax.swing.SwingUtilities;
 
 import com.thinkparity.browser.application.browser.component.ButtonFactory;
 import com.thinkparity.browser.application.browser.component.LabelFactory;
 import com.thinkparity.browser.application.browser.component.ListFactory;
 import com.thinkparity.browser.application.browser.display.avatar.session.ContactCellRenderer;
 import com.thinkparity.browser.application.browser.display.provider.FlatContentProvider;
 import com.thinkparity.browser.platform.application.display.avatar.Avatar;
 import com.thinkparity.browser.platform.util.State;
 
 import com.thinkparity.model.xmpp.contact.Contact;
 
 /**
  *
  * @author  raymond
  */
 public class Manage extends Avatar {
 
 	/**
 	 * @see java.io.Serializable
 	 * 
 	 */
 	private static final long serialVersionUID = 1;
 
     /**
      * The contacts list model.
      *
      */
     private DefaultListModel contactsModel;
     
     /**
      * Creates new form Manage
      */
     public Manage() {
 	super("ManageContacts", Color.WHITE);
 	initComponents();
     }
 
     /**
      * Obtain the avatar id.
      *
      *
      * @return The avatar id.
      */
     public AvatarId getId() { return AvatarId.SESSION_MANAGE_CONTACTS; }
 
     /**
      * Obtain the avatar's state information.
      *
      *
      * @return The avatar's state information.
      */
     public State getState() { return null; }
 
     /**
      * @see com.thinkparity.browser.platform.application.display.avatar.Avatar#reload()
      *
      */
     public void reload() {
         contactsModel.clear();
         loadContacts(contactsModel, getContacts());
 
         reloadRemoveJButton();
         reloadViewJButton();
     }
 
     /**
      * Set the avatar state.
      *
      *
      * @param state
      *            The avatar's state information.
      */
     public void setState(final State state) {}
 
     /**
      * Obtain the list of contacts.
      *
      * @return The list of contacts.
      */
     private Contact[] getContacts() {
 		return (Contact[]) ((FlatContentProvider) contentProvider)
 				.getElements(null);
 	}
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         javax.swing.JScrollPane contactsJScrollPane;
         javax.swing.JLabel eaJLabel;
         javax.swing.JButton inviteJButton;
         javax.swing.JButton searchJButton;
 
         eaJLabel = LabelFactory.create(getString("EmbeddedAssistance"));
         contactsJScrollPane = new javax.swing.JScrollPane();
         contactsJList = ListFactory.create();
         inviteJButton = ButtonFactory.create(getString("InviteButton"));
         searchJButton = ButtonFactory.create(getString("SearchButton"));
         removeJButton = ButtonFactory.create(getString("RemoveButton"));
         viewJButton = ButtonFactory.create(getString("ViewButton"));
 
        eaJLabel.setText("!Embedded Assistance!");

         contactsModel = new DefaultListModel();
         contactsJList.setModel(contactsModel);
         contactsJList.setCellRenderer(new ContactCellRenderer());
         contactsJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                 contactsJListValueChanged(e);
             }
         });
 
         contactsJScrollPane.setViewportView(contactsJList);
 
        inviteJButton.setText("!Invite!");
         inviteJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 inviteJButtonActionPerformed(e);
             }
         });
 
        searchJButton.setText("!Search!");
         searchJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 searchJButtonActionPerformed(e);
             }
         });
 
        removeJButton.setText("!Remove!");
         removeJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 removeJButtonActionPerformed(e);
             }
         });
 
        viewJButton.setText("!View!");
         viewJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent e) {
                 viewJButtonActionPerformed(e);
             }
         });
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, eaJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                     .add(layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(layout.createSequentialGroup()
                                 .add(searchJButton)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(inviteJButton))
                             .add(org.jdesktop.layout.GroupLayout.TRAILING, contactsJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(viewJButton)
                             .add(removeJButton))))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(eaJLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(contactsJScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(searchJButton)
                             .add(inviteJButton)))
                     .add(layout.createSequentialGroup()
                         .add(viewJButton)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(removeJButton)))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void viewJButtonActionPerformed(java.awt.event.ActionEvent e) {//GEN-FIRST:event_viewJButtonActionPerformed
 // TODO add your handling code here:
     }//GEN-LAST:event_viewJButtonActionPerformed
 
     private void contactsJListValueChanged(javax.swing.event.ListSelectionEvent e) {//GEN-FIRST:event_contactsJListValueChanged
         reloadRemoveJButton();
         reloadViewJButton();
     }//GEN-LAST:event_contactsJListValueChanged
 
     private void reloadRemoveJButton() {
         removeJButton.setEnabled(!contactsJList.isSelectionEmpty());
     }
 
     private void reloadViewJButton() {
         viewJButton.setEnabled(!contactsJList.isSelectionEmpty());
     }
 
     private void removeJButtonActionPerformed(java.awt.event.ActionEvent e) {//GEN-FIRST:event_removeJButtonActionPerformed
     }//GEN-LAST:event_removeJButtonActionPerformed
 
     private void searchJButtonActionPerformed(java.awt.event.ActionEvent e) {//GEN-FIRST:event_searchJButtonActionPerformed
         disposeWindow();
         getController().displayContactSearch();
     }//GEN-LAST:event_searchJButtonActionPerformed
 
     private void disposeWindow() {
 	SwingUtilities.getWindowAncestor(this).dispose();
     }
 
     private void inviteJButtonActionPerformed(java.awt.event.ActionEvent e) {//GEN-FIRST:event_inviteJButtonActionPerformed
 	disposeWindow();
 	getController().displaySessionInvitePartner();
     }//GEN-LAST:event_inviteJButtonActionPerformed
 
     /**
      * Load the contacts into the model.
      * 
      * @param listModel
      *            The contacts model.
      * @param contacts
      *            The list of contacts.
      */
     private void loadContacts(final DefaultListModel listModel,
 			final Contact[] contacts) {
     	for(final Contact contact : contacts) {
     		listModel.addElement(contact);
     	}
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JList contactsJList;
     private javax.swing.JButton removeJButton;
     private javax.swing.JButton viewJButton;
     // End of variables declaration//GEN-END:variables
 }
