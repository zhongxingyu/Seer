 /*
  * Project Wonderland
  * 
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  * 
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  * 
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  * 
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 
 /*
  * UserListHUDPanel.java
  *
  * Created on Jun 16, 2009, 11:57:10 AM
  */
 package org.jdesktop.wonderland.modules.audiomanager.client;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Logger;
 import javax.swing.DefaultListModel;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.ChannelComponent;
 import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
 import org.jdesktop.wonderland.client.hud.HUD;
 import org.jdesktop.wonderland.client.hud.HUDComponent;
 import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
 import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioVolumeMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.ChangeUsernameAliasMessage;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 /**
  *
  * @author nsimpson
  */
 public class UserListHUDPanel extends javax.swing.JPanel implements PresenceManagerListener,
         VolumeChangeListener, UsernameAliasChangeListener {
 
     private static final Logger logger = Logger.getLogger(UserListHUDPanel.class.getName());
     private ChannelComponent channelComp;
     private PresenceManager pm;
     private PresenceInfo presenceInfo;
    private HashMap<PresenceInfo, HUDComponent> volumeControlMap = new HashMap();
    private HashMap<PresenceInfo, HUDComponent> changeNameMap = new HashMap();
    private HashMap<String, Integer> usernameMap = new HashMap();
     private HUDComponent namePropertiesHUDComponent;
     private String[] selection = null;
     private DefaultListModel userListModel;
 
     public UserListHUDPanel(PresenceManager pm, Cell cell) {
         this.pm = pm;
 
         initComponents();
 
         userListModel = new DefaultListModel();
         userList.setModel(userListModel);
 
         channelComp = cell.getComponent(ChannelComponent.class);
 
         pm.addPresenceManagerListener(this);
         presenceInfo = pm.getPresenceInfo(cell.getCellID());
 
         if (presenceInfo == null) {
             logger.warning("No Presence info for cell " + cell.getCellID());
             return;
         }
 
         controlPanel.setVisible(false);
         volumeSlider.setEnabled(false);
         editButton.setEnabled(false);
         propertiesButton.setEnabled(true);
     }
 
     public void changeUsernameAlias(PresenceInfo info) {
         channelComp.send(new ChangeUsernameAliasMessage(info.cellID, info));
     }
 
     public void done() {
         setVisible(false);
     }
 
    public void setUserList() {
         PresenceInfo[] presenceInfoList = pm.getAllUsers();
 
         for (int i = 0; i < presenceInfoList.length; i++) {
             PresenceInfo info = presenceInfoList[i];
             if (info.callID == null) {
                 // It's a virtual player, skip it.
                 continue;
             }
 
             String username = info.userID.getUsername();
             String displayName = NameTagNode.getDisplayName(info.usernameAlias, info.isSpeaking,
                     info.isMuted);
 
             if (!usernameMap.containsKey(username)) {
                 // new user
                 // add to list model
                 userListModel.addElement(displayName);
                 // remember username and position in list model
                 usernameMap.put(username, userListModel.indexOf(displayName));
             } else {
                 // existing user
                 // update entry in list model
                 userListModel.setElementAt(displayName, usernameMap.get(username));
             }
         }
 
         // search for removed users
         Iterator<String> iter = usernameMap.keySet().iterator();
         while (iter.hasNext()) {
             // for each user previously displayed...
             String username = (String) iter.next();
             boolean found = false;
             
             // check if user is in current presence list
             for (int i = 0; i < presenceInfoList.length; i++) {
                 PresenceInfo info = presenceInfoList[i];
                 if (username.equals(info.userID.getUsername())) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 // user is not longer present, remove them from the user list
                 userListModel.removeElementAt(usernameMap.get(username));
                 usernameMap.remove(username);
             }
         }
     //SortUsers.sort(userData);
     }
 
     public void presenceInfoChanged(PresenceInfo info, ChangeType type) {
         setUserList();
     }
 
     public void usernameAliasChanged(PresenceInfo info) {
         setUserList();
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         controlPanel = new javax.swing.JPanel();
         editButton = new javax.swing.JButton();
         propertiesButton = new javax.swing.JButton();
         volumeLabel = new javax.swing.JLabel();
         volumeSlider = new javax.swing.JSlider();
         userListScrollPane = new javax.swing.JScrollPane();
         userList = new javax.swing.JList();
 
         editButton.setText("Edit");
         editButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editButtonActionPerformed(evt);
             }
         });
 
         propertiesButton.setText("Properties");
         propertiesButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 propertiesButtonActionPerformed(evt);
             }
         });
 
         volumeLabel.setText("Volume for:");
 
         volumeSlider.setMajorTickSpacing(1);
         volumeSlider.setMaximum(10);
         volumeSlider.setMinorTickSpacing(1);
         volumeSlider.setPaintLabels(true);
         volumeSlider.setPaintTicks(true);
         volumeSlider.setSnapToTicks(true);
         volumeSlider.setValue(5);
         volumeSlider.setMinimumSize(new java.awt.Dimension(36, 85));
         volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 volumeSliderStateChanged(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout controlPanelLayout = new org.jdesktop.layout.GroupLayout(controlPanel);
         controlPanel.setLayout(controlPanelLayout);
         controlPanelLayout.setHorizontalGroup(
             controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(controlPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(volumeSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                     .add(volumeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                     .add(controlPanelLayout.createSequentialGroup()
                         .add(editButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(propertiesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         controlPanelLayout.setVerticalGroup(
             controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, controlPanelLayout.createSequentialGroup()
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(controlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(editButton)
                     .add(propertiesButton))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(volumeLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         userList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 userListValueChanged(evt);
             }
         });
         userListScrollPane.setViewportView(userList);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 177, Short.MAX_VALUE)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, userListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
             .add(controlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 300, Short.MAX_VALUE)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .add(userListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                 .add(0, 0, 0)
                 .add(controlPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
         HUDComponent changeNameHUDComponent = changeNameMap.get(presenceInfo);
 
         if (changeNameHUDComponent == null) {
             ChangeNameHUDPanel changeNameHUDPanel = new ChangeNameHUDPanel(this, presenceInfo);
             HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
             final HUDComponent comp = mainHUD.createComponent(changeNameHUDPanel);
             comp.setPreferredLocation(Layout.NORTH);
             mainHUD.addComponent(comp);
             changeNameMap.put(presenceInfo, comp);
 
             PropertyChangeListener plistener = new PropertyChangeListener() {
 
                 public void propertyChange(PropertyChangeEvent pe) {
                     if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                         comp.setVisible(false);
                     }
                 }
             };
             changeNameHUDPanel.addPropertyChangeListener(plistener);
             changeNameHUDComponent = comp;
         }
 
         changeNameHUDComponent.setVisible(true);
 }//GEN-LAST:event_editButtonActionPerformed
 
     private void propertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertiesButtonActionPerformed
         if (namePropertiesHUDComponent == null) {
             NamePropertiesHUDPanel namePropertiesHUDPanel = new NamePropertiesHUDPanel(presenceInfo);
             HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
             namePropertiesHUDComponent = mainHUD.createComponent(namePropertiesHUDPanel);
             namePropertiesHUDComponent.setPreferredLocation(Layout.NORTH);
             mainHUD.addComponent(namePropertiesHUDComponent);
 
             PropertyChangeListener plistener = new PropertyChangeListener() {
 
                 public void propertyChange(PropertyChangeEvent pe) {
                     if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                         namePropertiesHUDComponent.setVisible(false);
                     }
                 }
             };
             namePropertiesHUDPanel.addPropertyChangeListener(plistener);
         }
 
         namePropertiesHUDComponent.setVisible(true);
 }//GEN-LAST:event_propertiesButtonActionPerformed
 
     private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSliderStateChanged
         javax.swing.JSlider source = (javax.swing.JSlider) evt.getSource();
         int volume = source.getValue();
 
         Object[] selectedValues = userList.getSelectedValues();
 
         if (selectedValues.length > 0) {
             for (int i = 0; i < selectedValues.length; i++) {
                 String username = NameTagNode.getUsername((String) selectedValues[i]);
 
                 PresenceInfo[] info = pm.getAliasPresenceInfo(username);
 
                 if (info == null) {
                     logger.warning("no PresenceInfo for " + username);
                     continue;
                 }
                 logger.info("changing volume for " + username + " to: " + volume);
                 PresenceInfo pi = info[0];
                 volumeChanged(pi.cellID, pi.callID, volume);
             }
         }
 }//GEN-LAST:event_volumeSliderStateChanged
 
     public void volumeChanged(CellID cellID, String otherCallID, int volume) {
         SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
 
         channelComp.send(new AudioVolumeMessage(cellID, sc.getCallID(), otherCallID,
                 VolumeUtil.getServerVolume(volume)));
     }
 
     private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
         Object[] selectedValues = userList.getSelectedValues();
 
         if (selectedValues.length == 0) {
             editButton.setEnabled(false);
             volumeLabel.setText("Private volume");
             volumeSlider.setEnabled(false);
         //controlPanel.setVisible(false);
         } else if (selectedValues.length == 1) {
             // one user (self or someone else)
             controlPanel.setVisible(true);
             volumeSlider.setEnabled(true);
 
             String username = NameTagNode.getUsername((String) selectedValues[0]);
 
             PresenceInfo[] info = pm.getAliasPresenceInfo(username);
 
             if (info == null) {
                 logger.warning("no PresenceInfo for " + username);
                 editButton.setEnabled(false);
                 return;
             }
 
             if ((presenceInfo != null) && presenceInfo.equals(info[0])) {
                 // this user
                 volumeLabel.setText("Master volume for " + username);
                 editButton.setEnabled(true);
             } else {
                 // another user
                 volumeLabel.setText("Private volume for " + username);
                 editButton.setEnabled(false);
             }
         } else {
             // multiple users
             volumeLabel.setText("Private volume for " + selectedValues.length + " users");
             volumeSlider.setEnabled(true);
         }
 }//GEN-LAST:event_userListValueChanged
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel controlPanel;
     private javax.swing.JButton editButton;
     private javax.swing.JButton propertiesButton;
     private javax.swing.JList userList;
     private javax.swing.JScrollPane userListScrollPane;
     private javax.swing.JLabel volumeLabel;
     private javax.swing.JSlider volumeSlider;
     // End of variables declaration//GEN-END:variables
 }
