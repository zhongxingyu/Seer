 /*
  * Created On:  December 19, 2006, 9:01 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.contact;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.text.MessageFormat;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.swing.SwingUtil;
 
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.ophelia.model.contact.IncomingInvitation;
 import com.thinkparity.ophelia.model.contact.OutgoingInvitation;
 
 import com.thinkparity.ophelia.browser.Constants.Colors;
 import com.thinkparity.ophelia.browser.application.browser.BrowserSession;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.main.MainPanelImageCache.TabPanelIcon;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabPanel;
 
 /**
  * <b>Title:</b><br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public class ContactTabPanel extends DefaultTabPanel {
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JLabel collapsedAdditionalTextJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel collapsedIconJLabel = new javax.swing.JLabel();
     private final javax.swing.JPanel collapsedJPanel = new javax.swing.JPanel();
     private final javax.swing.JLabel collapsedTextJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel contactAdditionalTextJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel contactCityValueJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel contactCountryValueJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel contactEMailValueJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel contactIconJLabel = new javax.swing.JLabel();
     private final javax.swing.JPanel contactJPanel = new javax.swing.JPanel();
     private final javax.swing.JLabel contactMobliePhoneValueJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel contactPhoneValueJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel contactTextJLabel = new javax.swing.JLabel();
     private final javax.swing.JPanel expandedJPanel = new javax.swing.JPanel();
     private final javax.swing.JLabel incomingAdditionalTextJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel incomingIconJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel incomingInvitedAsValueJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel incomingInvitedByValueJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel incomingInvitedOnValueJLabel = new javax.swing.JLabel();
     private final javax.swing.JPanel incomingJPanel = new javax.swing.JPanel();
     private final javax.swing.JLabel incomingTextJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel outgoingAdditionalTextJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel outgoingIconJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel outgoingInvitedAsValueJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel outgoingInvitedOnValueJLabel = new javax.swing.JLabel();
     private final javax.swing.JPanel outgoingJPanel = new javax.swing.JPanel();
     private final javax.swing.JLabel outgoingTextJLabel = new javax.swing.JLabel();
     // End of variables declaration//GEN-END:variables
 
     /** An <code>ActionDelegate</code>. */
     private ActionDelegate actionDelegate;
 
     /** A <code>Contact</code>. */
     private Contact contact;
 
     /** The panel's expanded state. */
     private boolean expanded;
 
     /** The panel's animating state. */
     private boolean animating;
 
     /** A contact <code>IncomingInvitation</code>. */
     private IncomingInvitation incoming;
 
     /** A contact <code>OutgoingInvitation</code>. */
     private OutgoingInvitation outgoing;
 
     /** A contact tab <code>PopupDelegate</code>. */
     private PopupDelegate popupDelegate;
 
     /**
      * Create ContactTabPanel.
      * 
      * @param session
      *            A <code>BrowserSession</code>.
      */
     public ContactTabPanel(final BrowserSession session) {
         super(session);
         initComponents();
     }
 
     /**
      * Collapse the panel.
      *
      */
     public void collapse(final boolean animate) {
         doCollapse(animate);
     }
 
     /**
      * Expand the panel.
      *
      */
     public void expand(final boolean animate) {
         doExpand(animate);
     }
 
     /**
      * Obtain the contact panel data.
      * 
      * @return A <code>Contact</code>.
      */
     public Contact getContact() {
         return contact;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabPanel#getId()
      *
      */
     @Override
     public Object getId() {
         final StringBuffer id = new StringBuffer(getClass().getName())
             .append("//");
         if (isSetContact())
             id.append(contact.getId());
         else if (isSetIncoming())
             id.append(incoming.getId());
         else if (isSetOutgoing())
             id.append(outgoing.getId());
         else
             Assert.assertUnreachable("Inconsistent contact tab panel state.");
         return id.toString();
     }
 
     /**
      * Obtain the incoming invitation.
      * 
      * @return An <code>IncomingInvitation</code>.
      */
     public IncomingInvitation getIncoming() {
         return incoming;
     }
 
     /**
      * Obtain the outgoing invitation.
      * 
      * @return An <code>OutgoingInvitation</code>.
      */
     public OutgoingInvitation getOutgoing() {
         return outgoing;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel#getPopupDelegate()
      *
      */
     public PopupDelegate getPopupDelegate() {
         return popupDelegate;
     }
 
     /**
      * Determine if the panel is expanded.
      * 
      * @return True if the panel is expanded.
      */
     public Boolean isExpanded() {
         return Boolean.valueOf(expanded);
     }
 
     /**
      * Determine if the panel is animating a collapse or expand operation.
      * 
      * @return True if the panel is in the process of expanding or collapsing.
      */
     public Boolean isAnimating() {
         return animating;
     }
 
     /**
      * Determine if the contact is set.
      * 
      * @return True if the contact is set.
      */
     public Boolean isSetContact() {
         return null != contact;
     }
 
     /**
      * Determine if the incoming invitation is set.
      * 
      * @return True if the invitation is set.
      */
     public Boolean isSetIncoming() {
         return null != incoming;
     }
 
     /**
      * Determine if the outgoing invitation is set.
      * 
      * @return True if the invitation is set.
      */
     public Boolean isSetOutgoing() {
         return null != outgoing;
     }
 
     /**
      * Set actionDelegate.
      *
      * @param actionDelegate
      *      A ContainerTabActionDelegate.
      */
     public void setActionDelegate(final ActionDelegate actionDelegate) {
         this.actionDelegate = actionDelegate;
     }
 
     /**
      * Makes the panel expanded or not.
      * 
      * @param expanded
      *            Whether to expand the panel.
      */
     public void setExpanded(final Boolean expanded) {
         if (expanded.booleanValue())
             doExpand(false);
         else
             doCollapse(false);
     }
 
     /**
      * Set the panel data.
      * 
      * @param contact
      *            A <code>Contact</code>.
      */
     public void setPanelData(final Contact contact) {
         this.contact = contact;
         collapsedIconJLabel.setIcon(IMAGE_CACHE.read(TabPanelIcon.USER));
         reload(collapsedTextJLabel, contact.getName());
         reload(collapsedAdditionalTextJLabel, getAdditionalText(contact));
 
         reload(contactTextJLabel, contact.getName());
         reload(contactAdditionalTextJLabel, getAdditionalText(contact));
 
         reload(contactCityValueJLabel, contact.getCity());
         reload(contactCountryValueJLabel, contact.getCountry());
         if (0 < contact.getEmailsSize())
             reload(contactEMailValueJLabel, contact.getEmails().get(0).toString());
         else
             reload(contactEMailValueJLabel, null);
         reload(contactMobliePhoneValueJLabel, contact.getMobilePhone());
         reload(contactPhoneValueJLabel, contact.getPhone());
     }
     
     /**
      * Set the panel data.
      * 
      * @param incomingInvitation
      *            An <code>IncomingInvitation</code>.
      * @param invitedBy
      *            The invited by <code>User</code>.
      */
     public void setPanelData(final IncomingInvitation incoming,
             final User invitedBy) {
         this.incoming = incoming;
         collapsedIconJLabel.setIcon(IMAGE_CACHE.read(TabPanelIcon.USER_NOT_RECEIVED));
         reload(collapsedAdditionalTextJLabel, getAdditionalText(invitedBy));
         reload(collapsedTextJLabel, invitedBy.getName());
 
         incomingIconJLabel.setIcon(collapsedIconJLabel.getIcon());
         reload(incomingAdditionalTextJLabel, getAdditionalText(invitedBy));
         reload(incomingInvitedAsValueJLabel, incoming.getInvitedAs().toString());
         reload(incomingInvitedByValueJLabel, invitedBy.getName());
         reload(incomingInvitedOnValueJLabel, formatFuzzy(incoming.getCreatedOn()));
         reload(incomingTextJLabel, invitedBy.getName());
     }
 
     /**
      * Set the panel data.
      * 
      * @param contact
      *            A <code>Contact</code>.
      */
     public void setPanelData(final OutgoingInvitation outgoing) {
         this.outgoing = outgoing;
         collapsedIconJLabel.setIcon(IMAGE_CACHE.read(TabPanelIcon.USER_NOT_RECEIVED));
         reload(collapsedAdditionalTextJLabel, null);
         reload(collapsedTextJLabel, outgoing.getEmail().toString());
         
         outgoingIconJLabel.setIcon(collapsedIconJLabel.getIcon());
         reload(outgoingAdditionalTextJLabel, null);
         reload(outgoingInvitedAsValueJLabel, outgoing.getEmail().toString());
         reload(outgoingInvitedOnValueJLabel, formatFuzzy(outgoing.getCreatedOn()));
         reload(outgoingTextJLabel, outgoing.getEmail().toString());
     }
 
     /**
      * Set popupDelegate.
      *
      * @param popupDelegate
      *      A ContainerTabPopupDelegate.
      */
     public void setPopupDelegate(final PopupDelegate popupDelegate) {
         this.popupDelegate = popupDelegate;
     }
 
     /**
      * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
      *
      */
     @Override
     protected void paintComponent(final Graphics g) {
         super.paintComponent(g);
         if (expanded || animating) {
             renderer.paintExpandedBackground(g, this);
         } else {
             renderer.paintBackground(g, getWidth(), getHeight());
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.DefaultTabPanel#repaintLists()
      *
      */
     @Override
     protected void repaintLists() {}
 
     private void collapsedIconJLabelMouseEntered(java.awt.event.MouseEvent e) {//GEN-FIRST:event_collapsedIconJLabelMouseEntered
         SwingUtil.setCursor((javax.swing.JLabel) e.getSource(), java.awt.Cursor.HAND_CURSOR);
     }//GEN-LAST:event_collapsedIconJLabelMouseEntered
 
     private void collapsedIconJLabelMouseExited(java.awt.event.MouseEvent e) {//GEN-FIRST:event_collapsedIconJLabelMouseExited
         SwingUtil.setCursor((javax.swing.JLabel) e.getSource(), java.awt.Cursor.DEFAULT_CURSOR);
     }//GEN-LAST:event_collapsedIconJLabelMouseExited
     
     private void collapsedIconJLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_collapsedIconJLabelMousePressed
         actionDelegate.invokeForContact(contact);
     }//GEN-LAST:event_collapsedIconJLabelMousePressed
 
     private void collapsedJPanelMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_collapsedJPanelMouseClicked
         jPanelMouseClicked((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_collapsedJPanelMouseClicked
 
     private void collapsedJPanelMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_collapsedJPanelMousePressed
         jPanelMousePressed((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_collapsedJPanelMousePressed
 
     private void collapsedJPanelMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_collapsedJPanelMouseReleased
         jPanelMouseReleased((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_collapsedJPanelMouseReleased
 
     private void contactJPanelMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_contactJPanelMouseClicked
         jPanelMouseClicked((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_contactJPanelMouseClicked
 
     private void contactJPanelMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_contactJPanelMousePressed
         jPanelMousePressed((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_contactJPanelMousePressed
 
     private void contactJPanelMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_contactJPanelMouseReleased
         jPanelMouseReleased((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_contactJPanelMouseReleased
 
     private void contactScrolledJPanelMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_contactScrolledJPanelMouseClicked
         jPanelMouseClicked((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_contactScrolledJPanelMouseClicked
 
     private void contactScrolledJPanelMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_contactScrolledJPanelMousePressed
         jPanelMousePressed((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_contactScrolledJPanelMousePressed
 
     private void contactScrolledJPanelMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_contactScrolledJPanelMouseReleased
         jPanelMouseReleased((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_contactScrolledJPanelMouseReleased
 
     /**
      * Collapse the panel.
      * 
      * @param animate
      *            Whether or not to animate.
      */
     private void doCollapse(final boolean animate) {
         if (animate) {
             final Dimension expandedPreferredSize = getPreferredSize();
            expandedPreferredSize.height = ANIMATION_MAXIMUM_HEIGHT;
             setPreferredSize(expandedPreferredSize);
             animating = true;
             animator.collapse(ANIMATION_HEIGHT_ADJUSTMENT,
                     ANIMATION_MINIMUM_HEIGHT, new Runnable() {
                         public void run() {
                             animating = false;
                             expanded = false;
 
                             if (isAncestorOf(expandedJPanel))
                                 remove(expandedJPanel);
                             if (isAncestorOf(collapsedJPanel))
                                 remove(collapsedJPanel);
                             add(collapsedJPanel, constraints.clone());
                             
                             revalidate();
                             repaint();
                             firePropertyChange("expanded", !expanded, expanded);
                         }
             });
         } else {
             expanded = false;
 
             if (isAncestorOf(expandedJPanel))
                 remove(expandedJPanel);
             if (isAncestorOf(collapsedJPanel))
                 remove(collapsedJPanel);
             add(collapsedJPanel, constraints.clone());
            
            final Dimension preferredSize = getPreferredSize();
            preferredSize.height = ANIMATION_MINIMUM_HEIGHT;
            setPreferredSize(preferredSize);
 
             revalidate();
             repaint();
             firePropertyChange("expanded", !expanded, expanded);
         }
     }
 
     /**
      * Expand the panel.
      * 
      * @param animate
      *            Whether or not to animate.
      */
     private void doExpand(final boolean animate) {
         expandedJPanel.removeAll();
         if (isSetContact())
             expandedJPanel.add(contactJPanel, constraints.clone());
         else if (isSetIncoming())
             expandedJPanel.add(incomingJPanel, constraints.clone());
         else if (isSetOutgoing())
             expandedJPanel.add(outgoingJPanel, constraints.clone());
         else
             Assert.assertUnreachable("Inconsistent contact tab panel state.");
 
         if (isAncestorOf(expandedJPanel))
             remove(expandedJPanel);
         if (isAncestorOf(collapsedJPanel))
             remove(collapsedJPanel);
         add(expandedJPanel, constraints.clone());
 
         if (animate) {
             final Dimension preferredSize = getPreferredSize();
             preferredSize.height = ANIMATION_MINIMUM_HEIGHT;
             setPreferredSize(preferredSize);
             animating = true;
             animator.expand(ANIMATION_HEIGHT_ADJUSTMENT,
                     ANIMATION_MAXIMUM_HEIGHT, new Runnable() {
                         public void run() {
                             expanded = true;
                             animating = false;
 
                             revalidate();
                             repaint();
                             firePropertyChange("expanded", !expanded, expanded);
                         }
             });
         } else {
             expanded = true;
            final Dimension preferredSize = getPreferredSize();
            preferredSize.height = ANIMATION_MAXIMUM_HEIGHT;
            setPreferredSize(preferredSize);
 
             revalidate();
             repaint();
             firePropertyChange("expanded", !expanded, expanded);
         }
     }
 
     /**
      * Extract additional text from a user.
      * 
      * @param <T>
      *            A user type.
      * @param user
      *            A <code>T</code>.
      * @return Additional display text <code>String</code>.
      */
     private <T extends User> String getAdditionalText(final T user) {
         final String pattern;
         final Object[] values;
         if (user.isSetTitle()) {
             if (user.isSetOrganization()) {
                 pattern = "({0}, {1})";
                 values = new Object[] { user.getTitle(),
                         user.getOrganization() };
             } else {
                 pattern = "({0})";
                 values = new Object[] { user.getTitle() };
             }
         } else {
             if (user.isSetOrganization()) {
                 pattern = "({0})";
                 values = new Object[] { user.getOrganization() };
             } else {
                 pattern = "";
                 values = new Object[] {};
             }
         }
         return new MessageFormat(pattern).format(values);
     }
 
     private void incomingJPanelMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_incomingJPanelMouseClicked
         jPanelMouseClicked((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_incomingJPanelMouseClicked
 
     private void incomingJPanelMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_incomingJPanelMousePressed
         jPanelMousePressed((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_incomingJPanelMousePressed
 
     private void incomingJPanelMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_incomingJPanelMouseReleased
         jPanelMouseReleased((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_incomingJPanelMouseReleased
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         final javax.swing.JScrollPane contactJScrollPane = new javax.swing.JScrollPane();
         final javax.swing.JPanel contactScrolledJPanel = new javax.swing.JPanel();
         final javax.swing.JLabel contactEMailJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel contactPhoneJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel contactMobilePhoneJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel contactCountryJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel contactCityJLabel = new javax.swing.JLabel();
         final javax.swing.JPanel outgoingNestedJPanel = new javax.swing.JPanel();
         final javax.swing.JLabel outgoingInvitedAsJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel outgoingInvitedOnJLabel = new javax.swing.JLabel();
         final javax.swing.JPanel incomingNestedJPanel = new javax.swing.JPanel();
         final javax.swing.JLabel incomingInvitedByJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel incomingInvitedAsJLabel = new javax.swing.JLabel();
         final javax.swing.JLabel incomingInvitedOnJLabel = new javax.swing.JLabel();
 
         collapsedJPanel.setLayout(new java.awt.GridBagLayout());
 
         collapsedJPanel.setOpaque(false);
         collapsedJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 collapsedJPanelMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 collapsedJPanelMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 collapsedJPanelMouseReleased(evt);
             }
         });
 
         collapsedIconJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/IconUser.png")));
         collapsedIconJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 collapsedIconJLabelMouseEntered(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 collapsedIconJLabelMouseExited(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 collapsedIconJLabelMousePressed(evt);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         gridBagConstraints.insets = new java.awt.Insets(4, 32, 0, 4);
         collapsedJPanel.add(collapsedIconJLabel, gridBagConstraints);
 
         collapsedTextJLabel.setText("!Contact Text!");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.insets = new java.awt.Insets(5, 3, 4, 0);
         collapsedJPanel.add(collapsedTextJLabel, gridBagConstraints);
 
         collapsedAdditionalTextJLabel.setForeground(Colors.Browser.List.INNER_LIST_SELECTION_BORDER);
         collapsedAdditionalTextJLabel.setText("!Contact Additional Text!");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 2, 4, 0);
         collapsedJPanel.add(collapsedAdditionalTextJLabel, gridBagConstraints);
 
         expandedJPanel.setLayout(new java.awt.GridBagLayout());
 
         expandedJPanel.setOpaque(false);
         contactJPanel.setLayout(new java.awt.GridBagLayout());
 
         contactJPanel.setOpaque(false);
         contactJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 contactJPanelMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 contactJPanelMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 contactJPanelMouseReleased(evt);
             }
         });
 
         contactIconJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/IconUser.png")));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         gridBagConstraints.insets = new java.awt.Insets(4, 32, 0, 4);
         contactJPanel.add(contactIconJLabel, gridBagConstraints);
 
         contactTextJLabel.setText("!Name!");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 3, 4, 0);
         contactJPanel.add(contactTextJLabel, gridBagConstraints);
 
         contactAdditionalTextJLabel.setForeground(Colors.Browser.List.INNER_LIST_SELECTION_BORDER);
         contactAdditionalTextJLabel.setText("!Title, Organization!");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 2, 4, 0);
         contactJPanel.add(contactAdditionalTextJLabel, gridBagConstraints);
 
         contactJScrollPane.setBorder(null);
         contactJScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         contactJScrollPane.setOpaque(false);
         contactJScrollPane.getViewport().setOpaque(false);
         contactScrolledJPanel.setOpaque(false);
         contactScrolledJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 contactScrolledJPanelMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 contactScrolledJPanelMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 contactScrolledJPanelMouseReleased(evt);
             }
         });
 
         contactEMailValueJLabel.setText("!E-Mail Address!");
 
         contactEMailJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("TAB_CONTACT.emailJLabel"));
 
         contactPhoneJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("TAB_CONTACT.phoneJLabel"));
 
         contactPhoneValueJLabel.setText("!Phone!");
 
         contactMobilePhoneJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("TAB_CONTACT.mobilePhoneJLabel"));
 
         contactMobliePhoneValueJLabel.setText("!Mobile Phone!");
 
         contactCountryJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("TAB_CONTACT.countryJLabel"));
 
         contactCountryValueJLabel.setText("!Country!");
 
         contactCityJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("TAB_CONTACT.cityJLabel"));
 
         contactCityValueJLabel.setText("!City!");
 
         org.jdesktop.layout.GroupLayout contactScrolledJPanelLayout = new org.jdesktop.layout.GroupLayout(contactScrolledJPanel);
         contactScrolledJPanel.setLayout(contactScrolledJPanelLayout);
         contactScrolledJPanelLayout.setHorizontalGroup(
             contactScrolledJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(contactScrolledJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(contactScrolledJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, contactCityJLabel)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, contactCountryJLabel)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, contactMobilePhoneJLabel)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, contactPhoneJLabel)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, contactEMailJLabel))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(contactScrolledJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(contactEMailValueJLabel)
                     .add(contactPhoneValueJLabel)
                     .add(contactMobliePhoneValueJLabel)
                     .add(contactCountryValueJLabel)
                     .add(contactCityValueJLabel))
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         contactScrolledJPanelLayout.setVerticalGroup(
             contactScrolledJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(contactScrolledJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(contactScrolledJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(contactEMailJLabel)
                     .add(contactEMailValueJLabel))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(contactScrolledJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(contactPhoneJLabel)
                     .add(contactPhoneValueJLabel))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(contactScrolledJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(contactMobilePhoneJLabel)
                     .add(contactMobliePhoneValueJLabel))
                 .add(20, 20, 20)
                 .add(contactScrolledJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(contactCountryJLabel)
                     .add(contactCountryValueJLabel))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(contactScrolledJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(contactCityJLabel)
                     .add(contactCityValueJLabel))
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         contactJScrollPane.setViewportView(contactScrolledJPanel);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 44, 0, 0);
         contactJPanel.add(contactJScrollPane, gridBagConstraints);
 
         outgoingJPanel.setLayout(new java.awt.GridBagLayout());
 
         outgoingJPanel.setOpaque(false);
         outgoingJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 outgoingJPanelMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 outgoingJPanelMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 outgoingJPanelMouseReleased(evt);
             }
         });
 
         outgoingIconJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/IconUserNotReceived.png")));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         gridBagConstraints.insets = new java.awt.Insets(4, 32, 0, 4);
         outgoingJPanel.add(outgoingIconJLabel, gridBagConstraints);
 
         outgoingTextJLabel.setText("!Outgoing Text!");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 3, 4, 0);
         outgoingJPanel.add(outgoingTextJLabel, gridBagConstraints);
 
         outgoingAdditionalTextJLabel.setForeground(Colors.Browser.List.INNER_LIST_SELECTION_BORDER);
         outgoingAdditionalTextJLabel.setText("!Outgoing Additional Text!");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 2, 4, 0);
         outgoingJPanel.add(outgoingAdditionalTextJLabel, gridBagConstraints);
 
         outgoingNestedJPanel.setOpaque(false);
         outgoingInvitedAsJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("TAB_CONTACT.invitedAsJLabel"));
 
         outgoingInvitedOnJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("TAB_CONTACT.invitedOnJLabel"));
 
         outgoingInvitedAsValueJLabel.setText("!Sent To!");
 
         outgoingInvitedOnValueJLabel.setText("!Invited On!");
 
         org.jdesktop.layout.GroupLayout outgoingNestedJPanelLayout = new org.jdesktop.layout.GroupLayout(outgoingNestedJPanel);
         outgoingNestedJPanel.setLayout(outgoingNestedJPanelLayout);
         outgoingNestedJPanelLayout.setHorizontalGroup(
             outgoingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(outgoingNestedJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(outgoingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, outgoingInvitedAsJLabel)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, outgoingInvitedOnJLabel))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(outgoingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(outgoingInvitedOnValueJLabel)
                     .add(outgoingInvitedAsValueJLabel))
                 .addContainerGap(126, Short.MAX_VALUE))
         );
         outgoingNestedJPanelLayout.setVerticalGroup(
             outgoingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(outgoingNestedJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(outgoingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(outgoingInvitedAsJLabel)
                     .add(outgoingInvitedAsValueJLabel))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(outgoingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(outgoingInvitedOnJLabel)
                     .add(outgoingInvitedOnValueJLabel))
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 44, 0, 0);
         outgoingJPanel.add(outgoingNestedJPanel, gridBagConstraints);
 
         incomingJPanel.setLayout(new java.awt.GridBagLayout());
 
         incomingJPanel.setOpaque(false);
         incomingJPanel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 incomingJPanelMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 incomingJPanelMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 incomingJPanelMouseReleased(evt);
             }
         });
 
         incomingIconJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/IconUserNotReceived.png")));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
         gridBagConstraints.insets = new java.awt.Insets(4, 32, 0, 4);
         incomingJPanel.add(incomingIconJLabel, gridBagConstraints);
 
         incomingTextJLabel.setText("!Incoming Text!");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(5, 3, 4, 0);
         incomingJPanel.add(incomingTextJLabel, gridBagConstraints);
 
         incomingAdditionalTextJLabel.setForeground(Colors.Browser.List.INNER_LIST_SELECTION_BORDER);
         incomingAdditionalTextJLabel.setText("!Incoming Additional Text!");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 2, 4, 0);
         incomingJPanel.add(incomingAdditionalTextJLabel, gridBagConstraints);
 
         incomingNestedJPanel.setOpaque(false);
         incomingInvitedByJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("TAB_CONTACT.invitedByJLabel"));
 
         incomingInvitedByValueJLabel.setText("!Invited By!");
 
         incomingInvitedAsJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("TAB_CONTACT.invitedAsJLabel"));
 
         incomingInvitedAsValueJLabel.setText("!Sent To!");
 
         incomingInvitedOnJLabel.setText(java.util.ResourceBundle.getBundle("localization/JPanel_Messages").getString("TAB_CONTACT.invitedOnJLabel"));
 
         incomingInvitedOnValueJLabel.setText("!Invited On!");
 
         org.jdesktop.layout.GroupLayout incomingNestedJPanelLayout = new org.jdesktop.layout.GroupLayout(incomingNestedJPanel);
         incomingNestedJPanel.setLayout(incomingNestedJPanelLayout);
         incomingNestedJPanelLayout.setHorizontalGroup(
             incomingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(incomingNestedJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(incomingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, incomingInvitedAsJLabel)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, incomingInvitedOnJLabel)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, incomingInvitedByJLabel))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(incomingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(incomingInvitedByValueJLabel)
                     .add(incomingInvitedOnValueJLabel)
                     .add(incomingInvitedAsValueJLabel))
                 .addContainerGap(147, Short.MAX_VALUE))
         );
         incomingNestedJPanelLayout.setVerticalGroup(
             incomingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(incomingNestedJPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(incomingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(incomingInvitedByJLabel)
                     .add(incomingInvitedByValueJLabel))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(incomingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(incomingInvitedOnJLabel)
                     .add(incomingInvitedOnValueJLabel))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(incomingNestedJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(incomingInvitedAsJLabel)
                     .add(incomingInvitedAsValueJLabel))
                 .addContainerGap())
         );
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 44, 0, 0);
         incomingJPanel.add(incomingNestedJPanel, gridBagConstraints);
 
         setLayout(new java.awt.GridBagLayout());
 
         setBorder(BORDER);
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Handle the mouse clicked event for any one of the panels in this tab. If
      * the click is a double click; the panel will be expanded/collapsed.
      * 
      * @param jPanel
      *            A <code>JPanel</code>.
      * @param e
      *            A <code>MouseEvent</code>.
      */
     private void jPanelMouseClicked(final javax.swing.JPanel jPanel,
             final java.awt.event.MouseEvent e) {
         if (0 == e.getClickCount() % 2)
             tabDelegate.toggleExpansion(this);
     }
 
     /**
      * Handle the mouse pressed event for any one of the jpanels in this tab.
      * Depending on whether or not the event is a popup trigger the popup
      * appropriate to the state of the panel will be dislpayed.
      * 
      * @param jPanel
      *            A <code>JPanel</code>.
      * @param e
      *            A <code>MouseEvent</code>.
      */
     private void jPanelMousePressed(final javax.swing.JPanel jPanel,
             final java.awt.event.MouseEvent e) {
         if (expanded && e.isPopupTrigger()) {
             popupDelegate.initialize(jPanel, e.getX(), e.getY());
             if (isSetContact())
                 popupDelegate.showForContact(contact);
             else if (isSetIncoming())
                 popupDelegate.showForInvitation(incoming);
             else if (isSetOutgoing())
                 popupDelegate.showForInvitation(outgoing);
             else
                 Assert.assertUnreachable("Inconsistent contact tab panel state.");
         }
     }
 
     /**
      * Handle the mouse released event for any one of the jpanels in this tab.
      * Depending on whether or not the event is a popup trigger the popup
      * appropriate to the state of the panel will be dislpayed.
      * 
      * @param jPanel
      *            A <code>JPanel</code>.
      * @param e
      *            A <code>MouseEvent</code>.
      */
     private void jPanelMouseReleased(final javax.swing.JPanel jPanel,
             final java.awt.event.MouseEvent e) {
         if (expanded && e.isPopupTrigger()) {
             popupDelegate.initialize(jPanel, e.getX(), e.getY());
             if (isSetContact())
                 popupDelegate.showForContact(contact);
             else if (isSetIncoming())
                 popupDelegate.showForInvitation(incoming);
             else if (isSetOutgoing())
                 popupDelegate.showForInvitation(outgoing);
             else
                 Assert.assertUnreachable("Inconsistent contact tab panel state.");
         }
     }
 
     private void outgoingJPanelMouseClicked(java.awt.event.MouseEvent e) {//GEN-FIRST:event_outgoingJPanelMouseClicked
         jPanelMouseClicked((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_outgoingJPanelMouseClicked
 
     private void outgoingJPanelMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_outgoingJPanelMousePressed
         jPanelMousePressed((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_outgoingJPanelMousePressed
 
     private void outgoingJPanelMouseReleased(java.awt.event.MouseEvent e) {//GEN-FIRST:event_outgoingJPanelMouseReleased
         jPanelMouseReleased((javax.swing.JPanel) e.getSource(), e);
     }//GEN-LAST:event_outgoingJPanelMouseReleased
 
     /**
      * Reload a display label.
      * 
      * @param jLabel
      *            A swing <code>JLabel</code>.
      * @param value
      *            The label value.
      */
     private void reload(final javax.swing.JLabel jLabel,
             final String value) {
         jLabel.setText(null == value ? " " : value);
     }
 }
