 /*
  * Created on July 29, 2006, 8:58 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Window;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowStateListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 
 import com.thinkparity.codebase.StringUtil.Separator;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.swing.GradientPainter;
 import com.thinkparity.codebase.swing.SwingUtil;
 import com.thinkparity.codebase.swing.border.TopBorder;
 
 import com.thinkparity.codebase.model.contact.IncomingEMailInvitation;
 import com.thinkparity.codebase.model.contact.IncomingUserInvitation;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.profile.Profile;
 
 import com.thinkparity.ophelia.model.events.ContactEvent;
 import com.thinkparity.ophelia.model.events.ContainerEvent;
 import com.thinkparity.ophelia.model.events.MigratorEvent;
 import com.thinkparity.ophelia.model.events.ProfileEvent;
 import com.thinkparity.ophelia.model.session.OfflineCode;
 
 import com.thinkparity.ophelia.browser.Constants.Colors;
 import com.thinkparity.ophelia.browser.Constants.Images;
 import com.thinkparity.ophelia.browser.Constants.Colors.Browser;
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants.Fonts;
 import com.thinkparity.ophelia.browser.application.browser.component.LabelFactory;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.Resizer.ResizeEdges;
 import com.thinkparity.ophelia.browser.application.browser.display.event.MainStatusDispatcher;
 import com.thinkparity.ophelia.browser.application.browser.display.provider.MainStatusProvider;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar;
 import com.thinkparity.ophelia.browser.platform.util.State;
 
 /**
  * <b>Title:</b>thinkParity Browser Status<br>
  * <b>Description:</b>Displays the status bar at the bottom of the browser
  * window.
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.33
  */
 public class MainStatusAvatar extends Avatar {
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JLabel connectionJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel linkJLabel = LabelFactory.createLink("",Fonts.DefaultFont);
     private final javax.swing.JLabel optionalLinkJLabel = LabelFactory.createLink("",Fonts.DefaultFont);
     private final javax.swing.JLabel optionalTextJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel resizeJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel textJLabel = new javax.swing.JLabel();
     private final javax.swing.JLabel userJLabel = LabelFactory.createLink("",Fonts.DialogFont);
     // End of variables declaration//GEN-END:variables
 
     /** A <code>Runnable</code> used to display the status bar link. */
     private Runnable linkRunnable;
 
     /** A <code>Runnable</code> used to display the optional status bar link. */
     private Runnable optionalLinkRunnable;
 
     /** A <code>Boolean</code> indicating a product update has been installed. */
     private Boolean productInstalled;
 
     /** The most recent <code>Profile</code>. */
     private Profile profile;
 
     /** The resize offset size in the x direction. */
     private int resizeOffsetX;
 
     /** The resize offset size in the y direction. */
     private int resizeOffsetY;
 
     /** A <code>Boolean</code> indicating the app is being restarted. */
     private Boolean restarting;
 
     /** A <code>Boolean</code> indicating if a short term link is displayed. */
     private Boolean shortTermLinkDisplayed;
 
     /**
      * Create MainStatusAvatar.
      *
      */
     MainStatusAvatar() {
         super(AvatarId.MAIN_STATUS);
         initComponents();
         installResizer();
         new FocusHelper().addFocusListener(this);
         installAncestorWindowStateListener();
         addPropertyChangeListener("eventDispatcher", new PropertyChangeListener() {
             public void propertyChange(final PropertyChangeEvent evt) {
                 if (null != evt.getOldValue())
                     ((MainStatusDispatcher) evt.getOldValue()).removeListeners(
                             MainStatusAvatar.this);
                 if (null != evt.getNewValue())
                     ((MainStatusDispatcher) evt.getNewValue()).addListeners(
                             MainStatusAvatar.this);
             }
         });
         initComponentListener();
         restarting = Boolean.FALSE;
         productInstalled = Boolean.FALSE;
     }
 
     /**
      * Fire a contact event.
      * 
      * @param e
      *            A <code>ContactEvent</code>.
      */
     public void fireContactEvent(final ContactEvent e) {
         reloadLinks();
     }
 
     /**
      * Fire a container event.
      * 
      * @param e
      *            A <code>ContainerEvent</code>.
      */
     public void fireContainerEvent(final ContainerEvent e) {
         reloadLinks();
     }
 
     /**
      * Fire a migrator event.
      *
      * @param e
      *      A <code>MigratorEvent</code>.
      */
     public void fireProductReleaseInstalled(final MigratorEvent e) {
         productInstalled = Boolean.TRUE;
         reloadLinks();
     }
 
     /**
      * Fire a profile e-mail event.
      * 
      * @param e
      *            A <code>ProfileEvent</code>.
      */
     public void fireProfileEMailEvent(final ProfileEvent e) {
         reloadLinks();
     }
 
     /**
      * Fire a profile event.
      * 
      * @param e
      *            A <code>ProfileEvent</code>.
      */
     public void fireProfileEvent(final ProfileEvent e) {
         userJLabel.setText("");
         if ((isTestMode() || isDebugMode()))
             reloadProfile(e.getProfile());
     }
 
     /**
      * Fire a session event.
      *
      */
     public void fireSessionEvent() {
         reloadLinks();
         reloadConnection();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getId()
      */
     @Override
     public AvatarId getId() { return AvatarId.MAIN_STATUS; }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getState()
      */
     @Override
     public State getState() { return null; }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#isAvatarBackgroundImage()
      */
     @Override
     public Boolean isAvatarBackgroundImage() {
         return Boolean.FALSE;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#reload()
      * 
      */
     @Override
     public void reload() {
         reloadLinks();
         reloadProfile();
         reloadConnection();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#setState(com.thinkparity.ophelia.browser.platform.util.State)
      */
     @Override
     public void setState(final State state) {}
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getResizeEdges()
      */
     @Override
     protected ResizeEdges getResizeEdges() {
         return Resizer.ResizeEdges.BOTTOM;
     }
 
     /**
      * Obtain a localized string for the connection.
      * 
      * @param connection
      *            A platform connection.
      * @return A localized string.
      */
     protected String getString(final OfflineCode offlineCode) {
        final String key = new StringBuilder("CXN.")
            .append(offlineCode.name())
            .toString();
        return getString(key);
     }
 
     /**
      * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
      */
     @Override
     protected void paintComponent(final Graphics g) {
         super.paintComponent(g);
         final Graphics g2 = g.create();
         try {
             GradientPainter.paintVertical(g2, getSize(),
                     Browser.MainStatus.BG_GRAD_START,
                     Browser.MainStatus.BG_GRAD_FINISH);
             
             // These images help to make the rounded corner look good.
             if (!getController().isBrowserWindowMaximized()) {
                 g2.drawImage(Images.BrowserTitle.BROWSER_BOTTOM_LEFT_INNER,
                         0,
                         getSize().height - Images.BrowserTitle.BROWSER_BOTTOM_LEFT_INNER.getHeight(),
                         Images.BrowserTitle.BROWSER_BOTTOM_LEFT_INNER.getWidth(),
                         Images.BrowserTitle.BROWSER_BOTTOM_LEFT_INNER.getHeight(), this);
                 g2.drawImage(Images.BrowserTitle.BROWSER_BOTTOM_RIGHT_INNER,
                         getSize().width - Images.BrowserTitle.BROWSER_BOTTOM_RIGHT_INNER.getWidth(),
                         getSize().height - Images.BrowserTitle.BROWSER_BOTTOM_RIGHT_INNER.getHeight(),
                         Images.BrowserTitle.BROWSER_BOTTOM_RIGHT_INNER.getWidth(),
                         Images.BrowserTitle.BROWSER_BOTTOM_RIGHT_INNER.getHeight(), this);
             }
         }
         finally { g2.dispose(); }
     }
 
     /**
      * Clear text and link JLabels
      */
     private void clearJLabels() {
         textJLabel.setText(Separator.EmptyString.toString());
         linkJLabel.setText(Separator.EmptyString.toString());
         optionalTextJLabel.setText(Separator.EmptyString.toString());
         optionalLinkJLabel.setText(Separator.EmptyString.toString());
     }
 
     /**
      * Obtain the input link.  The input is cleared after reading the link.
      * 
      * @return An input link.
      */
     private MainStatusAvatarLink getInputLink() {
         if (null == input) {
             return null;
         } else {
             final MainStatusAvatarLink link =
                 (MainStatusAvatarLink) ((Data) input).get(DataKey.LINK);
             ((Data) input).unset(DataKey.LINK);
             return link;
         }
     }
 
     /**
      * Obtain the session offline code.
      * 
      * @return An <code>OfflineCode</code>.
      */
     private OfflineCode getOfflineCode() {
         return ((MainStatusProvider) contentProvider).getOfflineCode();
     }
 
     /**
      * Initialize the component listener.
      */
     private void initComponentListener() {
         addComponentListener(new ComponentAdapter() {
             @Override
             public void componentResized(final ComponentEvent e) {
                 reloadProfile(profile);
             }
         });
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
     private void initComponents() {
         javax.swing.JLabel fillJLabel;
         java.awt.GridBagConstraints gridBagConstraints;
 
         fillJLabel = new javax.swing.JLabel();
 
         setLayout(new java.awt.GridBagLayout());
 
         setBorder(new TopBorder(Colors.Browser.MainStatus.TOP_BORDER));
         textJLabel.setText("You have ");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
         add(textJLabel, gridBagConstraints);
 
         linkJLabel.setFont(Fonts.DefaultFont);
         linkJLabel.setText("1 new package");
         linkJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 linkJLabelMousePressed(evt);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         add(linkJLabel, gridBagConstraints);
 
         optionalTextJLabel.setText(" and ");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         add(optionalTextJLabel, gridBagConstraints);
 
         optionalLinkJLabel.setText("1 contact invitation.");
         optionalLinkJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 optionalLinkJLabelMousePressed(evt);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         add(optionalLinkJLabel, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         add(fillJLabel, gridBagConstraints);
 
         connectionJLabel.setFont(Fonts.DefaultFont);
         connectionJLabel.setForeground(Colors.Browser.MainStatus.CONNECTION_FOREGROUND_OFFLINE);
         connectionJLabel.setText("Offline");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
         add(connectionJLabel, gridBagConstraints);
 
         userJLabel.setFont(Fonts.DefaultFont);
         userJLabel.setText("Dr. Who");
         userJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 userJLabelMousePressed(evt);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 6;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
         add(userJLabel, gridBagConstraints);
 
         resizeJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/BrowserStatus_Resize.png")));
         resizeJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 resizeJLabelMouseEntered(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 resizeJLabelMouseExited(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 resizeJLabelMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 resizeJLabelMouseReleased(evt);
             }
         });
         resizeJLabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseDragged(java.awt.event.MouseEvent evt) {
                 resizeJLabelMouseDragged(evt);
             }
         });
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 7;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
         add(resizeJLabel, gridBagConstraints);
 
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Install a window state listener on the ancestor of the component.
      * If the ancestor window is maximized then the resize control will be disabled.
      */
     private void installAncestorWindowStateListener() {
         addAncestorListener(new AncestorListener() {
             public void ancestorAdded(final AncestorEvent event) {
                 final Window window = SwingUtilities.getWindowAncestor(MainStatusAvatar.this);
                 if (null != window) {
                     installWindowStateListener(window);
                     resizeJLabel.setVisible(!SwingUtil.isInMaximizedWindow(MainStatusAvatar.this));
                 }
             }
             public void ancestorMoved(final AncestorEvent event) {}
             public void ancestorRemoved(final AncestorEvent event) {}
         });
     }
 
     /**
      * Install a window state listener.
      * If the ancestor window is maximized then the resize control will be disabled.
      * 
      * @param window
      *            The <code>Window</code>.
      */
     private void installWindowStateListener(final Window window) {
         window.addWindowStateListener(new WindowStateListener() {
             public void windowStateChanged(final WindowEvent e) {
                 if (e.getID() == WindowEvent.WINDOW_STATE_CHANGED) {
                     resizeJLabel.setVisible(!isMaximized(e));
                 }
             }
         });
     }
 
     /**
      * Determine if the input clear link flag is set.
      * The input is cleared after reading the flag.
      * 
      * @return true if the <code>Boolean</code> clear link flag is set.
      */
     private Boolean isInputClearLink() {
         if (null == input || !((Data)input).isSet(DataKey.CLEAR_LINK)) {
             return Boolean.FALSE;
         } else {
             final Boolean clearLink =  (Boolean) ((Data)input).get(DataKey.CLEAR_LINK);
             ((Data) input).unset(DataKey.CLEAR_LINK);
             return clearLink;
         }
     }
 
     /**
      * Determine if the window event indicates a maximized JFrame window.
      * 
      * @param e
      *            A <code>WindowEvent</code>.
      */
     private Boolean isMaximized(final WindowEvent e) {
         return (e.getNewState() & JFrame.MAXIMIZED_BOTH) > 0;
     }
 
     /**
      * Determine if the model is online.
      * 
      * @return True if the model is online.
      */
     private boolean isOnline() {
         return ((MainStatusProvider) contentProvider).isOnline().booleanValue();
     }
 
     /**
      * Determine if the input link is set.
      * 
      * @return True if the input link is set.
      */
     private boolean isSetInputLink() {
         return null != input && ((Data) input).isSet(DataKey.LINK);
     }
 
     private void linkJLabelMousePressed(final java.awt.event.MouseEvent e) {//GEN-FIRST:event_linkJLabelMousePressed
         linkRunnable.run();
     }//GEN-LAST:event_linkJLabelMousePressed
 
     private void optionalLinkJLabelMousePressed(final java.awt.event.MouseEvent e) {//GEN-FIRST:event_optionalLinkJLabelMousePressed
         optionalLinkRunnable.run();
     }//GEN-LAST:event_optionalLinkJLabelMousePressed
 
     /**
      * Read the incoming e-mail invitations.
      * 
      * @return A <code>List</code> of <code>IncomingEMailInvitation</code>s.
      */
     private List<IncomingEMailInvitation> readIncomingEMailInvitations() {
         return ((MainStatusProvider) contentProvider).readIncomingEMailInvitations();
     }
 
     /**
      * Read the incoming e-mail invitations.
      * 
      * @return A <code>List</code> of <code>IncomingEMailInvitation</code>s.
      */
     private List<IncomingUserInvitation> readIncomingUserInvitations() {
         return ((MainStatusProvider) contentProvider).readIncomingUserInvitations();
     }
 
     /**
      * Read the user's profile.
      * 
      * @return The user's <code>Profile</code>.
      */
     private Profile readProfile() {
         return ((MainStatusProvider) contentProvider).readProfile();
     }
 
     /**
      * Get the list of unseen container versions.
      * 
      * @return A <code>List</code> of <code>ContainerVersion</code>s.
      */
     private List<ContainerVersion> readUnseenContainerVersions() {
         return ((MainStatusProvider) contentProvider).readUnseenContainerVersions();
     }
 
     /**
      * Read a list of <code>EMail</code> addresses that have not yet been
      * verified.
      * 
      * @return A <code>List</code> of <code>EMail</code> addresses.
      */
     private List<EMail> readUnverifiedEMails() {
         return ((MainStatusProvider) contentProvider).readUnverifiedEMails();
     }
 
     /**
      * Reload the connection status message.
      * 
      */
     private void reloadConnection() {
         connectionJLabel.setText("");
         if (!isOnline()) {
             connectionJLabel.setForeground(Colors.Browser.MainStatus.CONNECTION_FOREGROUND_OFFLINE);
             connectionJLabel.setText(getString(getOfflineCode()));
         }
     }
 
     /**
      * Reload the link messages.
      * 
      */
     private void reloadLinks() {
         final Boolean updateLongTermLinks;
 
         // update the 'short term' link prepared by the input.
         if (isSetInputLink()) {
             shortTermLinkDisplayed = Boolean.TRUE;
             updateLongTermLinks = Boolean.FALSE;
             clearJLabels();
             final MainStatusAvatarLink link = getInputLink();
             textJLabel.setText(link.getText() + Separator.Space);
             linkJLabel.setText(link.getLinkText());
             linkRunnable = new Runnable() {
                 public void run() {
                     link.getTarget().run();
                     /* NOTE calling reload links after clicking on "Restart"
                      * thinkParity causes a NPE because the model has shutdown
                      */ 
                     reloadLinks();
                 }
             };
         } else if (isInputClearLink()) {
             updateLongTermLinks = shortTermLinkDisplayed;
         } else {
             updateLongTermLinks = Boolean.TRUE;
         }
 
         // Update the 'long term' links. For performance reasons this
         // is done only when necessary.
         if (updateLongTermLinks) {
             shortTermLinkDisplayed = Boolean.FALSE;
             clearJLabels();
             linkRunnable = optionalLinkRunnable = null;
 
             if (restarting) {
                 textJLabel.setForeground(Colors.Browser.MainStatus.CLIENT_MAINTENANCE_MESSAGE_FOREGROUND);
                 textJLabel.setText(getString("Text.Restarting") + Separator.Space);
             } else if (productInstalled) {
                 textJLabel.setForeground(Colors.Browser.MainStatus.CLIENT_MAINTENANCE_MESSAGE_FOREGROUND);
                 textJLabel.setText(getString("Text.ProductInstalled") + Separator.Space);
                 linkJLabel.setText(getString("Link.ProductInstalled"));
                 linkRunnable = new Runnable() {
                     public void run() {
                         getController().getPlatform().restart();
                         restarting = Boolean.TRUE;
                         reloadLinks();
                     }
                 };
             } else if (!isOnline() && OfflineCode.CLIENT_MAINTENANCE == getOfflineCode()) {
                 textJLabel.setForeground(Colors.Browser.MainStatus.CLIENT_MAINTENANCE_MESSAGE_FOREGROUND);
                 textJLabel.setText(getString("Text.ClientMaintenance"));
             } else if (0 < readUnverifiedEMails().size()) {
                 textJLabel.setForeground(Colors.Browser.MainStatus.MESSAGE_FOREGROUND);
                 if (isOnline()) {
                     /* if there is no verified e-mail address display a link about
                      * the e-mails only. */
                     textJLabel.setText(getString("Text.VerifyEMail") + Separator.Space);
                     linkJLabel.setText(getString("Link.VerifyEMail"));
                     optionalTextJLabel.setText(Separator.Space + getString("OptionalText.VerifyEMail"));
                     linkRunnable = new Runnable() {
                         public void run() {
                             getController().displayVerifyEmailDialog();
                         }
                     };
                 } else {
                     textJLabel.setText(getString("Text.VerifyEMailOffline"));
                 }
             } else {
                 final List<ContainerVersion> unseenContainerVersions = readUnseenContainerVersions();
                 final List<IncomingEMailInvitation> incomingEMail = readIncomingEMailInvitations();
                 final List<IncomingUserInvitation> incomingUser = readIncomingUserInvitations();
                 textJLabel.setForeground(Colors.Browser.MainStatus.MESSAGE_FOREGROUND);
                 if (0 < unseenContainerVersions.size()) {
                     if (0 < incomingEMail.size() || 0 < incomingUser.size()) {
                         // display container info/link
                         textJLabel.setText(getString("Text") + Separator.Space);
                         linkJLabel.setText(getString("ContainerLink", new Object[] {unseenContainerVersions.size()}));
                         linkRunnable = new Runnable() {
                             public void run() {
                                 getController().selectTab(MainTitleAvatar.TabId.CONTAINER);
                                 getController().showAllTabPanels();
                                 getController().showTopVisibleUnreadContainerVersion();
                             }
                         };
                         // display invitation info/link
                         optionalTextJLabel.setText(Separator.Space + getString("OptionalText") + Separator.Space);
                         optionalLinkJLabel.setText(getString("InvitationLink", new Object[] {incomingEMail.size() + incomingUser.size()}));
                         if (0 < incomingEMail.size()) {
                             optionalLinkRunnable = new Runnable() {
                                 public void run() {
                                     getController().selectTab(MainTitleAvatar.TabId.CONTACT);
                                     getController().showAllTabPanels();
                                     getController().showContactIncomingEMailInvitation(incomingEMail.get(0));
                                 }
                             };
                         }
                         if (0 < incomingUser.size()) {
                             optionalLinkRunnable = new Runnable() {
                                 public void run() {
                                     getController().selectTab(MainTitleAvatar.TabId.CONTACT);
                                     getController().showAllTabPanels();
                                     getController().showContactIncomingUserInvitation(incomingUser.get(0));
                                 }
                             };
                         }
                     } else {
                         // display container info/link
                         textJLabel.setText(getString("Text") + Separator.Space);
                         linkJLabel.setText(getString("ContainerLink", new Object[] {unseenContainerVersions.size()}));
                         linkRunnable = new Runnable() {
                             public void run() {
                                 getController().selectTab(MainTitleAvatar.TabId.CONTAINER);
                                 getController().showAllTabPanels();
                                 getController().showTopVisibleUnreadContainerVersion();
                             }
                         };
                     }
                 } else {
                     if (0 < incomingEMail.size() || 0 < incomingUser.size()) {
                         textJLabel.setText(getString("Text") + Separator.Space);
                         linkJLabel.setText(getString("InvitationLink", new Object[] {incomingEMail.size() + incomingUser.size()}));
                         if (0 < incomingEMail.size()) {
                             linkRunnable = new Runnable() {
                                 public void run() {
                                     getController().selectTab(MainTitleAvatar.TabId.CONTACT);
                                     getController().showAllTabPanels();
                                     getController().showContactIncomingEMailInvitation(incomingEMail.get(0));
                                 }
                             };
                         }
                         if (0 < incomingUser.size()) {
                             linkRunnable = new Runnable() {
                                 public void run() {
                                     getController().selectTab(MainTitleAvatar.TabId.CONTACT);
                                     getController().showAllTabPanels();
                                     getController().showContactIncomingUserInvitation(incomingUser.get(0));
                                 }
                             };
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Reload the user name label.
      */
     private void reloadProfile() {
         reloadProfile(readProfile());
     }
 
     /**
      * Reload the user name label.
      * The name is trimmed if more than 1/3 of the overall width.
      * 
      * @param profile
      *            A <code>Profile</code>.
      */
     private void reloadProfile(final Profile profile) {
         this.profile = profile;
         String clippedName = profile.getName();
         if (null != userJLabel.getGraphics()) {
             final int maxWidth = getWidth() / 3;
             clippedName = SwingUtil.limitWidthWithEllipsis(
                     profile.getName(), maxWidth,
                     userJLabel.getGraphics());
         }
         userJLabel.setText(clippedName);
     }
 
     private void resizeJLabelMouseDragged(java.awt.event.MouseEvent e) {                                          
         getController().resizeBrowserWindow(
                 new Dimension(e.getPoint().x - resizeOffsetX,
                         e.getPoint().y - resizeOffsetY));
     }// GEN-LAST:event_resizeJLabelMouseDragged
 
     private void resizeJLabelMouseEntered(java.awt.event.MouseEvent e) {//GEN-FIRST:event_resizeJLabelMouseEntered
         if (!isResizeDragging()) {
             SwingUtil.setCursor((javax.swing.JLabel) e.getSource(), java.awt.Cursor.SE_RESIZE_CURSOR);
         }
     }//GEN-LAST:event_resizeJLabelMouseEntered
 
     private void resizeJLabelMouseExited(java.awt.event.MouseEvent e) {//GEN-FIRST:event_resizeJLabelMouseExited
         if (!isResizeDragging()) {
             SwingUtil.setCursor((javax.swing.JLabel) e.getSource(), null);
         }
     }//GEN-LAST:event_resizeJLabelMouseExited
 
     private void resizeJLabelMousePressed(java.awt.event.MouseEvent e) {//GEN-FIRST:event_resizeJLabelMousePressed
         resizeOffsetX = e.getPoint().x;
         resizeOffsetY = e.getPoint().y;
         setResizeDragging(Boolean.TRUE);
     }//GEN-LAST:event_resizeJLabelMousePressed
 
     private void resizeJLabelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resizeJLabelMouseReleased
         setResizeDragging(Boolean.FALSE);
     }//GEN-LAST:event_resizeJLabelMouseReleased
 
     private void userJLabelMousePressed(final java.awt.event.MouseEvent e) {//GEN-FIRST:event_userJLabelMousePressed
         getController().displayUpdateProfileDialog();
     }//GEN-LAST:event_userJLabelMousePressed
 
     public enum DataKey { CLEAR_LINK, LINK }
 }
