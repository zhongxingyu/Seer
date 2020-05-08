 /*
  * Created on July 29, 2006, 8:58 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Window;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowStateListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.event.AncestorEvent;
 import javax.swing.event.AncestorListener;
 
 import com.thinkparity.codebase.BytesFormat;
 import com.thinkparity.codebase.BytesFormat.Unit;
 import com.thinkparity.codebase.StringUtil.Separator;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.swing.GradientPainter;
 import com.thinkparity.codebase.swing.SwingUtil;
 import com.thinkparity.codebase.swing.border.TopBorder;
 
 import com.thinkparity.codebase.model.backup.Statistics;
 import com.thinkparity.codebase.model.contact.IncomingEMailInvitation;
 import com.thinkparity.codebase.model.contact.IncomingUserInvitation;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.profile.Profile;
 
 import com.thinkparity.ophelia.model.events.BackupEvent;
 import com.thinkparity.ophelia.model.events.ContactEvent;
 import com.thinkparity.ophelia.model.events.ContainerEvent;
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
  */
 public class MainStatusAvatar extends Avatar {
 
     /** A size format to use for the backup statistics. */
     private static final BytesFormat BYTES_FORMAT;
 
     static {
         BYTES_FORMAT = new BytesFormat();
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final javax.swing.JLabel backupStatisticsJLabel = new javax.swing.JLabel();
 
     /** The unit to use when displaying the backup information. */
     private final Unit backupUnit;    
 
     private final javax.swing.JLabel connectionJLabel = new javax.swing.JLabel();
 
     private final javax.swing.JLabel linkJLabel = LabelFactory.createLink("",Fonts.DefaultFont);
 
     private Runnable linkRunnable;
 
     private final javax.swing.JLabel optionalLinkJLabel = LabelFactory.createLink("",Fonts.DefaultFont);
 
     private Runnable optionalLinkRunnable;
 
     private final javax.swing.JLabel optionalTextJLabel = new javax.swing.JLabel();
 
     private final javax.swing.JLabel resizeJLabel = new javax.swing.JLabel();
 
     /** The resize offset size in the x direction. */
     private int resizeOffsetX;
 
     /** The resize offset size in the y direction. */
     private int resizeOffsetY;
 
     /** Backup statistics */
     private Statistics statistics;
 
     private final javax.swing.JLabel textJLabel = new javax.swing.JLabel();
 
     private final javax.swing.JLabel userJLabel = new javax.swing.JLabel();
     // End of variables declaration//GEN-END:variables
 
     /**
      * Create MainStatusAvatar.
      *
      */
     MainStatusAvatar() {
         super(AvatarId.MAIN_STATUS);
         initComponents();
         installResizer();
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
         this.backupUnit = Unit.AUTO;
     }
     
     /**
      * Fire a backup event.
      * 
      * @param e
      *            A <code>BackupEvent</code>.
      */
     public void fireBackupEvent(final BackupEvent e) {
         this.statistics = e.getStatistics();
         reloadBackupStatistics(statistics);
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
         reloadConnection();
         reloadBackupStatistics();
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
         reloadBackupStatistics();
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
 
         backupStatisticsJLabel.setFont(Fonts.DefaultFont);
         backupStatisticsJLabel.setText("1.0GB");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
         add(backupStatisticsJLabel, gridBagConstraints);
 
         userJLabel.setFont(Fonts.DefaultFont);
         userJLabel.setText("Dr. Who");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 6;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
         add(userJLabel, gridBagConstraints);
 
         connectionJLabel.setFont(Fonts.DefaultFont);
         connectionJLabel.setForeground(Colors.Browser.MainStatus.CONNECTION_FOREGROUND_OFFLINE);
         connectionJLabel.setText("Offline");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 7;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
         add(connectionJLabel, gridBagConstraints);
 
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
         gridBagConstraints.gridx = 8;
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
      * Determine if the backup feature is enabled.
      * 
      * @return True if the backup feature is enabled.
      */
     private boolean isBackupEnabled() {
         return ((MainStatusProvider) contentProvider).isBackupEnabled().booleanValue();
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
      * Read the backup statistics.
      * 
      * @return An instance of <code>Statistics</code>.
      */
     private Statistics readBackupStatistics() {
         return ((MainStatusProvider) contentProvider).readBackupStatistics();
     }
 
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
      * Read the unseen container count.
      * 
      * @return A count of the number of unseen containers.
      */
     private List<Container> readUnseenContainers() {
         return ((MainStatusProvider) contentProvider).readUnseenContainers();
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
      * Reload the backup statistics.  If the backup is enabled and online, the
      * backup statistics are displayed.
      *
      */
     private void reloadBackupStatistics() {
         backupStatisticsJLabel.setText("");
        if (null != statistics) {
             reloadBackupStatistics(statistics);
         } else if (isOnline() && isBackupEnabled()) {
             this.statistics = readBackupStatistics();
             reloadBackupStatistics(statistics);
        }
     }
 
     /**
      * Reload the backup statistics.  If the backup is enabled and online, the
      * backup statistics are displayed.
      *
      */
     private void reloadBackupStatistics(final Statistics statistics) {
         if (null == backupUnit)
             backupStatisticsJLabel.setText(BYTES_FORMAT.format(
                     statistics.getDiskUsage()));
         else
             backupStatisticsJLabel.setText(BYTES_FORMAT.format(backupUnit,
                     statistics.getDiskUsage()));
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
         textJLabel.setText(Separator.EmptyString.toString());
         linkJLabel.setText(Separator.EmptyString.toString());
         optionalTextJLabel.setText(Separator.EmptyString.toString());
         optionalLinkJLabel.setText(Separator.EmptyString.toString());
         if (isSetInputLink()) {
             final MainStatusAvatarLink link = getInputLink();
             textJLabel.setText(link.getText() + Separator.Space);
             linkJLabel.setText(link.getLinkText());
             linkRunnable = new Runnable() {
                 public void run() {
                     link.getTarget().run();
                     reloadLinks();
                 }
             };
         } else {
             linkRunnable = optionalLinkRunnable = null;
             final List<EMail> unVerifiedEMails = readUnverifiedEMails();
             final List<Container> unseenContainers = readUnseenContainers();
             final List<IncomingEMailInvitation> incomingEMail = readIncomingEMailInvitations();
             final List<IncomingUserInvitation> incomingUser = readIncomingUserInvitations();
             if (0 < unVerifiedEMails.size()) {
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
                 if (0 < unseenContainers.size()) {
                     if (0 < incomingEMail.size() || 0 < incomingUser.size()) {
                         // display container info/link
                         textJLabel.setText(getString("Text") + Separator.Space);
                         linkJLabel.setText(getString("ContainerLink", new Object[] {unseenContainers.size()}));
                         linkRunnable = new Runnable() {
                             public void run() {
                                 getController().selectTab(MainTitleAvatar.TabId.CONTAINER);
                                 getController().showContainer(unseenContainers.get(0));
                             }
                         };
                         // display invitation info/link
                         optionalTextJLabel.setText(Separator.Space + getString("OptionalText") + Separator.Space);
                         optionalLinkJLabel.setText(getString("InvitationLink", new Object[] {incomingEMail.size() + incomingUser.size()}));
                         if (0 < incomingEMail.size()) {
                             optionalLinkRunnable = new Runnable() {
                                 public void run() {
                                     getController().selectTab(MainTitleAvatar.TabId.CONTACT);
                                     getController().showContactIncomingEMailInvitation(incomingEMail.get(0));
                                 }
                             };
                         }
                         if (0 < incomingUser.size()) {
                             optionalLinkRunnable = new Runnable() {
                                 public void run() {
                                     getController().selectTab(MainTitleAvatar.TabId.CONTACT);
                                     getController().showContactIncomingUserInvitation(incomingUser.get(0));
                                 }
                             };
                         }
                     } else {
                         // display container info/link
                         textJLabel.setText(getString("Text") + Separator.Space);
                         linkJLabel.setText(getString("ContainerLink", new Object[] {unseenContainers.size()}));
                         linkRunnable = new Runnable() {
                             public void run() {
                                 getController().selectTab(MainTitleAvatar.TabId.CONTAINER);
                                 getController().showContainer(unseenContainers.get(0));
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
                                     getController().showContactIncomingEMailInvitation(incomingEMail.get(0));
                                 }
                             };
                         }
                         if (0 < incomingUser.size()) {
                             linkRunnable = new Runnable() {
                                 public void run() {
                                     getController().selectTab(MainTitleAvatar.TabId.CONTACT);
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
      * Reload the user name label. The user's name will only display for testing
      * and development purposes.
      * 
      */
     private void reloadProfile() {
         userJLabel.setText("");
         if ((isTestMode() || isDebugMode()))
             reloadProfile(readProfile());
     }
     /**
      * Reload the profile.
      * 
      * @param profile
      *            A <code>Profile</code>.
      */
     private void reloadProfile(final Profile profile) {
         userJLabel.setText(profile.getName());
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
             SwingUtil.setCursor((javax.swing.JLabel) e.getSource(), java.awt.Cursor.DEFAULT_CURSOR);
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
 
     public enum DataKey { LINK }
 }
