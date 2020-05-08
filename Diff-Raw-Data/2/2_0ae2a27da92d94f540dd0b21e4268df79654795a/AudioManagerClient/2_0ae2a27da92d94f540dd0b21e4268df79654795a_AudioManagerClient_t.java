 /**
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
 package org.jdesktop.wonderland.modules.audiomanager.client;
 
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import java.util.ResourceBundle;
 import javax.swing.ImageIcon;
 import javax.swing.JMenuItem;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.Cell.RendererType;
 import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
 import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;
 import org.jdesktop.wonderland.client.comms.BaseConnection;
 import org.jdesktop.wonderland.client.comms.CellClientSession;
 import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
 import org.jdesktop.wonderland.client.hud.HUD;
 import org.jdesktop.wonderland.client.hud.HUDComponent;
 import org.jdesktop.wonderland.client.hud.HUDEvent;
 import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
 import org.jdesktop.wonderland.client.hud.HUDEventListener;
 import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
 import org.jdesktop.wonderland.client.input.InputManager;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.client.jme.MainFrame;
 import org.jdesktop.wonderland.client.softphone.AudioQuality;
 import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
 import org.jdesktop.wonderland.client.softphone.SoftphoneListener;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.comms.ConnectionType;
 import org.jdesktop.wonderland.common.messages.Message;
 import org.jdesktop.wonderland.common.NetworkAddress;
 import org.jdesktop.wonderland.modules.audiomanager.client.voicechat.AddHUDPanel;
 import org.jdesktop.wonderland.modules.audiomanager.client.voicechat.AddHUDPanel.Mode;
 import org.jdesktop.wonderland.modules.audiomanager.client.voicechat.IncomingCallHUDPanel;
 import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.audio.CallEndedMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.audio.CallEstablishedMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.audio.CallMigrateMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.audio.CallMutedMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.audio.CallSpeakingMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.ChangeUsernameAliasMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.ConeOfSilenceEnterExitMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetPlayersInRangeResponseMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeRequestMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeResponseMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.MuteCallRequestMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlaceCallRequestMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlayerInRangeMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.TransferCallRequestMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatBusyMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatCallEndedMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatHoldMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatInfoResponseMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatJoinAcceptedMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatJoinRequestMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatLeaveMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage.ChatType;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarNameEvent;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode.EventType;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 /**
  *
  * @author jprovino
  * @author Ronny Standtke <ronny.standtke@fhnw.ch>
  */
 public class AudioManagerClient extends BaseConnection implements
         AudioMenuListener, SoftphoneListener, ViewCellConfiguredListener {
 
     private static final Logger logger =
             Logger.getLogger(AudioManagerClient.class.getName());
     private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
             "org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle");
     private WonderlandSession session;
     private boolean connected = true;
     private PresenceManager pm;
     private PresenceInfo presenceInfo;
     private Cell cell;
     private JMenuItem userListJMenuItem;
     private ArrayList<DisconnectListener> disconnectListeners = new ArrayList();
     private HashMap<String, ArrayList<MemberChangeListener>> memberChangeListeners =
             new HashMap();
     private ArrayList<UserInRangeListener> userInRangeListeners =
             new ArrayList();
     private HUDComponent userListHUDComponent;
     private UserListHUDPanel userListHUDPanel;
     private boolean usersMenuSelected = false;
     private HUDComponent micVuMeterComponent;
     private ImageIcon voiceChatIcon;
     private ImageIcon userListIcon;
 
     /**
      * Create a new AudioManagerClient
      * @param session the session to connect to, guaranteed to be in
      * the CONNECTED state
      * @throws org.jdesktop.wonderland.client.comms.ConnectionFailureException
      */
     public AudioManagerClient() {
         AudioMenu.getAudioMenu(this).setEnabled(false);
 
         voiceChatIcon = new ImageIcon(getClass().getResource(
                 "/org/jdesktop/wonderland/modules/audiomanager/client/" +
                 "resources/UserListChatVoice32x32.png"));
         userListIcon = new ImageIcon(getClass().getResource(
                 "/org/jdesktop/wonderland/modules/audiomanager/client/" +
                 "resources/GenericUsers32x32.png"));
 
         userListJMenuItem = new javax.swing.JCheckBoxMenuItem();
         userListJMenuItem.setText(BUNDLE.getString("Users"));
         userListJMenuItem.setSelected(usersMenuSelected);
         userListJMenuItem.addActionListener(new ActionListener() {
 
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 usersMenuSelected = !usersMenuSelected;
                 userListJMenuItem.setSelected(usersMenuSelected);
                 showUsers(evt);
             }
         });
         userListJMenuItem.setEnabled(false);
         logger.fine("Starting AudioManagerCLient");
     }
 
     public WlAvatarCharacter getWlAvatarCharacter() {
         AvatarImiJME rend =
                 (AvatarImiJME) cell.getCellRenderer(RendererType.RENDERER_JME);
         return rend.getAvatarCharacter();
     }
 
     public void addDisconnectListener(DisconnectListener listener) {
         disconnectListeners.add(listener);
     }
 
     public void removeDisconnectListener(DisconnectListener listener) {
         disconnectListeners.add(listener);
     }
 
     private void notifyDisconnectListeners() {
         for (DisconnectListener listener : disconnectListeners) {
             listener.disconnected();
         }
     }
 
     public void addMemberChangeListener(
             String group, MemberChangeListener listener) {
         ArrayList<MemberChangeListener> listeners =
                 memberChangeListeners.get(group);
 
         if (listeners == null) {
             listeners = new ArrayList();
             memberChangeListeners.put(group, listeners);
         }
 
         listeners.add(listener);
     }
 
     public void removeMemberChangeListener(
             String group, MemberChangeListener listener) {
         ArrayList<MemberChangeListener> listeners =
                 memberChangeListeners.get(group);
         listeners.remove(listener);
     }
 
     public void notifyMemberChangeListeners(
             String group, PresenceInfo member, boolean added) {
         logger.fine("Member change for group " + group +
                 " member " + member + " added " + added);
         ArrayList<MemberChangeListener> listeners =
                 memberChangeListeners.get(group);
 
         if (listeners == null) {
             logger.fine("NO LISTENERS!");
             return;
         }
 
         for (MemberChangeListener listener : listeners) {
             listener.memberChange(member, added);
         }
     }
 
     public void notifyMemberChangeListeners(
             String group, PresenceInfo[] members) {
         ArrayList<MemberChangeListener> listeners =
                 memberChangeListeners.get(group);
 
         if (listeners == null) {
             logger.fine("NO LISTENERS!");
             return;
         }
 
         for (MemberChangeListener listener : listeners) {
             listener.setMemberList(members);
         }
     }
 
     public void addUserInRangeListener(UserInRangeListener listener) {
         if (userInRangeListeners.contains(listener)) {
             return;
         }
 
         userInRangeListeners.add(listener);
     }
 
     public void removeUserInRangeListener(UserInRangeListener listener) {
         userInRangeListeners.remove(listener);
     }
 
     public void notifyUserInRangeListeners(PresenceInfo info,
             PresenceInfo userInRange, boolean isInRange) {
 
         for (UserInRangeListener listener : userInRangeListeners) {
             listener.userInRange(info, userInRange, isInRange);
         }
     }
 
     public void showUsers(java.awt.event.ActionEvent evt) {
         if (presenceInfo == null) {
             return;
         }
 
         if (userListHUDComponent == null) {
             userListHUDPanel = new UserListHUDPanel(this, session, pm, cell);
             HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
             userListHUDComponent = mainHUD.createComponent(userListHUDPanel);
             userListHUDPanel.setHUDComponent(userListHUDComponent);
             userListHUDComponent.setPreferredLocation(Layout.NORTHWEST);
             userListHUDComponent.setName(BUNDLE.getString("Users"));
             userListHUDComponent.setIcon(userListIcon);
 
             mainHUD.addComponent(userListHUDComponent);
             userListHUDComponent.addEventListener(new HUDEventListener() {
 
                 public void HUDObjectChanged(HUDEvent e) {
                     if (e.getEventType().equals(HUDEventType.DISAPPEARED)) {
                         usersMenuSelected = false;
                         userListJMenuItem.setSelected(usersMenuSelected);
                     }
                 }
             });
         }
 
         userListHUDPanel.setUserList();
         userListHUDComponent.setVisible(usersMenuSelected);
     }
 
     public synchronized void execute(final Runnable r) {
     }
 
     @Override
     public void connect(WonderlandSession session)
             throws ConnectionFailureException {
         super.connect(session);
 
         this.session = session;
 
         pm = PresenceManagerFactory.getPresenceManager(session);
 
         LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();
         avatar.addViewCellConfiguredListener(this);
         if (avatar.getViewCell() != null) {
             // if the view is already configured, fake an event
             viewConfigured(avatar);
         }
 
         SoftphoneControlImpl.getInstance().addSoftphoneListener(this);
 
         // enable the menus
         AudioMenu.getAudioMenu(this).setEnabled(true);
         userListJMenuItem.setEnabled(true);
     }
 
     @Override
     public void disconnected() {
         super.disconnected();
 
         // TODO: add methods to remove listeners!
 
         LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();
         avatar.removeViewCellConfiguredListener(this);
 
         SoftphoneControlImpl.getInstance().removeSoftphoneListener(this);
        SoftphoneControlImpl.getInstance().sendCommandToSoftphone("Shutdown");
         //JmeClientMain.getFrame().removeAudioMenuListener(this);
         notifyDisconnectListeners();
     }
 
     public void addMenus() {
         MainFrame mainFrame = JmeClientMain.getFrame();
         mainFrame.addToToolsMenu(AudioMenu.getAudioMenuItem(this), 1);
         mainFrame.addToWindowMenu(userListJMenuItem, 5);
 
         AudioMenu.getAudioMenu(this).addMenus();
     }
 
     public void removeMenus() {
         MainFrame mainFrame = JmeClientMain.getFrame();
         mainFrame.removeFromToolsMenu(AudioMenu.getAudioMenuItem(this));
         mainFrame.removeFromWindowMenu(userListJMenuItem);
 
         AudioMenu.getAudioMenu(this).removeMenus();
     }
 
     public void viewConfigured(LocalAvatar localAvatar) {
         cell = localAvatar.getViewCell();
         CellID cellID = cell.getCellID();
 
         /*
          * We require the PresenceManager so by the time we get here,
          * our presenceInfo has to be available.
          */
         presenceInfo = pm.getPresenceInfo(cellID);
 
         logger.fine("[AudioManagerClient] view configured for cell " +
                 cellID + " presence: " + presenceInfo + " from " + pm);
 
         connectSoftphone();
     }
 
     public void connectSoftphone() {
         logger.fine("[AudioManagerClient] " +
                 "Sending message to server to get voice bridge...");
 
         WonderlandSession.Status status = session.getStatus();
         if (status == WonderlandSession.Status.CONNECTED) {
             logger.warning("Sending message to server to get voice bridge... " +
                     "session is " + status);
 
             session.send(this, new GetVoiceBridgeRequestMessage());
         }
     }
 
     public void showSoftphone() {
         SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
         sc.setVisible(!sc.isVisible());
     }
 
     public void setAudioQuality(AudioQuality audioQuality) {
         SoftphoneControlImpl.getInstance().setAudioQuality(audioQuality);
 
         logger.info("Set audio quality to " + audioQuality +
                 ", now reconnect softphone");
         reconnectSoftphone();
     }
 
     public void testAudio() {
         SoftphoneControlImpl.getInstance().runLineTest();
     }
 
     public void reconnectSoftphone() {
         connectSoftphone();
     }
     private CallMigrationForm callMigrationForm;
 
     public void transferCall() {
         AudioParticipantComponent component =
                 cell.getComponent(AudioParticipantComponent.class);
 
         if (component == null) {
             logger.warning("Can't transfer call:  " +
                     "No AudioParticipantComponent for " + cell.getCellID());
             return;
         }
 
         if (callMigrationForm == null) {
             callMigrationForm = new CallMigrationForm(this);
         }
 
         callMigrationForm.setVisible(true);
     }
 
     public void logAudioProblem() {
         SoftphoneControlImpl.getInstance().logAudioProblem();
     }
 
     public void mute(boolean isMuted) {
         this.isMuted = isMuted;
 
         SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
         sc.mute(isMuted);
 
         if (session.getStatus() == WonderlandSession.Status.CONNECTED) {
             session.send(this,
                     new MuteCallRequestMessage(sc.getCallID(), isMuted));
         } else {
             logger.warning("Unable to send MuteCallRequestMessage. " +
                     "Session is not connected.");
         }
     }
 
     public void personalPhone() {
         voiceChat();
     }
 
     public void voiceChat() {
         if (presenceInfo == null) {
             return;
         }
 
         AddHUDPanel addPanel = new AddHUDPanel(
                 this, session, presenceInfo, presenceInfo, Mode.INITIATE);
 
         HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
 
         final HUDComponent addComponent = mainHUD.createComponent(addPanel);
         addPanel.setHUDComponent(addComponent);
         addComponent.setPreferredLocation(Layout.CENTER);
         addComponent.setName(BUNDLE.getString("Voice_Chat"));
         addComponent.setIcon(voiceChatIcon);
         mainHUD.addComponent(addComponent);
         addComponent.addEventListener(new HUDEventListener() {
 
             public void HUDObjectChanged(HUDEvent e) {
                 if (e.getEventType().equals(HUDEventType.DISAPPEARED)) {
                 }
             }
         });
 
         PropertyChangeListener plistener = new PropertyChangeListener() {
 
             public void propertyChange(PropertyChangeEvent pe) {
                 String propName = pe.getPropertyName();
                 if (propName.equals("ok") || propName.equals("cancel")) {
                     addComponent.setVisible(false);
                 }
             }
         };
         addPanel.addPropertyChangeListener(plistener);
         addComponent.setVisible(true);
     }
 
     public void softphoneVisible(boolean isVisible) {
     }
     private boolean isMuted = true;
 
     public void softphoneMuted(boolean isMuted) {
         if (this.isMuted == isMuted) {
             return;
         }
 
         AudioMenu.getAudioMenu(this).mute(isMuted);
         mute(isMuted);
     }
 
     public void softphoneConnected(boolean connected) {
     }
 
     public void softphoneExited() {
         logger.warning("Softphone exited, reconnect");
 
         /*
          * If presenceInfo is null, connectSoftphone will be called when
          * the presenceInfo is set.
          */
         if (presenceInfo != null) {
             connectSoftphone();
         }
     }
 
     public void microphoneGainTooHigh() {
     }
 
     public void microphoneVolume() {
         try {
             if (!SoftphoneControlImpl.getInstance().isConnected()) {
                 return;
             }
         } catch (IOException e) {
             return;
         }
 
         if (micVuMeterComponent == null) {
             final MicVuMeterPanel micVuMeterPanel = new MicVuMeterPanel(this);
 
             HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
 
             micVuMeterComponent = mainHUD.createComponent(micVuMeterPanel);
             micVuMeterComponent.setPreferredLocation(Layout.SOUTH);
             micVuMeterComponent.setName("Microphone Level");
             micVuMeterComponent.setIcon(voiceChatIcon);
             micVuMeterComponent.addEventListener(new HUDEventListener() {
 
                 public void HUDObjectChanged(HUDEvent event) {
                     switch (event.getEventType()) {
                         case APPEARED:
                             micVuMeterPanel.startVuMeter(true);
                             break;
                         case DISAPPEARED:
                             micVuMeterPanel.startVuMeter(false);
                             break;
                         default:
                             break;
                     }
                 }
             });
             mainHUD.addComponent(micVuMeterComponent);
             micVuMeterPanel.startVuMeter(true);
         }
 
         micVuMeterComponent.setVisible(true);
     }
 
     public void transferCall(String phoneNumber) {
         session.send(this, new TransferCallRequestMessage(
                 presenceInfo, phoneNumber, false));
     }
 
     public void cancelCallTransfer() {
         session.send(this, new TransferCallRequestMessage(
                 presenceInfo, "", true));
     }
 
     @Override
     public void handleMessage(Message message) {
         logger.fine("got a message...");
 
         if (message instanceof GetPlayersInRangeResponseMessage) {
             GetPlayersInRangeResponseMessage msg =
                     (GetPlayersInRangeResponseMessage) message;
 
             String[] playersInRange = msg.getPlayersInRange();
 
             for (int i = 0; i < playersInRange.length; i++) {
                 playerInRange(new PlayerInRangeMessage(
                         msg.getPlayerID(), playersInRange[i], true));
             }
 
             return;
         }
 
         if (message instanceof GetVoiceBridgeResponseMessage) {
             startSoftphone((GetVoiceBridgeResponseMessage) message);
             return;
         }
 
         if (message instanceof ChangeUsernameAliasMessage) {
             changeUsernameAlias((ChangeUsernameAliasMessage) message);
             return;
         }
 
         if (message instanceof VoiceChatJoinRequestMessage) {
             logger.warning("Got VoiceChatJoinRequestMessage");
 
             final IncomingCallHUDPanel incomingCallHUDPanel =
                     new IncomingCallHUDPanel(this, session, cell.getCellID(),
                     (VoiceChatJoinRequestMessage) message);
 
             HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
             final HUDComponent incomingCallHUDComponent =
                     mainHUD.createComponent(incomingCallHUDPanel);
             incomingCallHUDPanel.setHUDComponent(incomingCallHUDComponent);
             incomingCallHUDComponent.setPreferredLocation(Layout.CENTER);
             incomingCallHUDComponent.setIcon(voiceChatIcon);
 
             mainHUD.addComponent(incomingCallHUDComponent);
             incomingCallHUDComponent.addEventListener(new HUDEventListener() {
 
                 public void HUDObjectChanged(HUDEvent e) {
                     if (e.getEventType().equals(HUDEventType.DISAPPEARED)) {
                         incomingCallHUDPanel.busy();
                     }
                 }
             });
 
             incomingCallHUDComponent.setVisible(true);
             return;
         }
 
         if (message instanceof VoiceChatBusyMessage) {
             VoiceChatBusyMessage msg = (VoiceChatBusyMessage) message;
 
             VoiceChatBusyHUDPanel voiceChatBusyHUDPanel =
                     new VoiceChatBusyHUDPanel(msg.getCallee());
             HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
             HUDComponent voiceChatBusyHUDComponent =
                     mainHUD.createComponent(voiceChatBusyHUDPanel);
             voiceChatBusyHUDPanel.setHUDComponent(voiceChatBusyHUDComponent);
             voiceChatBusyHUDComponent.setPreferredLocation(Layout.CENTER);
             voiceChatBusyHUDComponent.setIcon(voiceChatIcon);
 
             mainHUD.addComponent(voiceChatBusyHUDComponent);
             voiceChatBusyHUDComponent.addEventListener(new HUDEventListener() {
 
                 public void HUDObjectChanged(HUDEvent e) {
                     if (e.getEventType().equals(HUDEventType.DISAPPEARED)) {
                     }
                 }
             });
 
             voiceChatBusyHUDComponent.setVisible(true);
 
             notifyMemberChangeListeners(msg.getGroup(), msg.getCallee(), false);
             return;
         }
 
         if (message instanceof VoiceChatInfoResponseMessage) {
             VoiceChatInfoResponseMessage msg =
                     (VoiceChatInfoResponseMessage) message;
             notifyMemberChangeListeners(msg.getGroup(), msg.getChatters());
             return;
         }
 
         if (message instanceof VoiceChatJoinAcceptedMessage) {
             joinVoiceChat((VoiceChatJoinAcceptedMessage) message);
             return;
         }
 
         if (message instanceof VoiceChatHoldMessage) {
             VoiceChatHoldMessage msg = (VoiceChatHoldMessage) message;
             return;
         }
 
         if (message instanceof VoiceChatLeaveMessage) {
             leaveVoiceChat((VoiceChatLeaveMessage) message);
             return;
         }
 
         if (message instanceof VoiceChatCallEndedMessage) {
             VoiceChatCallEndedMessage msg = (VoiceChatCallEndedMessage) message;
             voiceChatCallEnded(msg);
             session.send(this,
                     new VoiceChatLeaveMessage(msg.getGroup(), msg.getCallee()));
             return;
         }
 
         if (message instanceof ConeOfSilenceEnterExitMessage) {
             coneOfSilenceEnterExit((ConeOfSilenceEnterExitMessage) message);
             return;
         }
 
         if (message instanceof PlayerInRangeMessage) {
             playerInRange((PlayerInRangeMessage) message);
             return;
         }
 
         if (message instanceof CallEstablishedMessage) {
             if (callMigrationForm != null) {
                 callMigrationForm.setStatus("Migrated");
             }
 
             return;
         }
 
         if (message instanceof CallMigrateMessage) {
             callMigrate((CallMigrateMessage) message);
             return;
         }
 
         if (message instanceof CallMutedMessage) {
             callMuted((CallMutedMessage) message);
             return;
         }
 
         if (message instanceof CallSpeakingMessage) {
             callSpeaking((CallSpeakingMessage) message);
             return;
         }
 
         if (message instanceof CallEndedMessage) {
             callEnded((CallEndedMessage) message);
             return;
         }
 
         logger.warning("Unknown message " + message);
 
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     private void startSoftphone(GetVoiceBridgeResponseMessage msg) {
         logger.warning("Got voice bridge " + msg.getBridgeInfo());
 
         String phoneNumber = System.getProperty(
                 "org.jdesktop.wonderland.modules.audiomanager.client.PHONE_NUMBER");
 
         System.setProperty(
                 "org.jdesktop.wonderland.modules.audiomanager.client.PHONE_NUMBER", "");
 
         if (phoneNumber != null && phoneNumber.length() > 0) {
             session.send(this, new PlaceCallRequestMessage(
                     presenceInfo, phoneNumber, 0., 0., 0., 90., false));
             return;
         }
 
         SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
 
         /*
          * The voice bridge info is a String of values separated by ":".
          * The numbers indicate the index in tokens[].
          *
          *     0      1      2                   3                 4
          * <bridgeId>::<privateHostName>:<privateControlPort>:<privateSipPort>
          *			 5                   6                 7
          *   	  :<publicHostName>:<publicControlPort>:<publicSipPort>
          */
         String tokens[] = msg.getBridgeInfo().split(":");
 
         String registrarAddress = tokens[5] + ";sip-stun:";
 
         registrarAddress += tokens[7];
 
         String localAddress = null;
 
         try {
             InetAddress ia = NetworkAddress.getPrivateLocalAddress(
                     "server:" + tokens[5] + ":" + tokens[7] + ":10000");
 
             localAddress = ia.getHostAddress();
         } catch (UnknownHostException e) {
             logger.warning(e.getMessage());
 
             logger.warning("The client is unable to connect to the bridge " +
                     "public address. Trying the bridge private Address.");
 
             try {
                 InetAddress ia = NetworkAddress.getPrivateLocalAddress(
                         "server:" + tokens[2] + ":" + tokens[4] + ":10000");
 
                 localAddress = ia.getHostAddress();
             } catch (UnknownHostException ee) {
                 logger.warning(ee.getMessage());
             }
         }
 
         if (localAddress != null) {
             try {
                 String sipURL = sc.startSoftphone(
                         presenceInfo.userID.getUsername(), registrarAddress,
                         10, localAddress);
 
                 logger.fine("Starting softphone:  " + presenceInfo);
 
                 if (sipURL != null) {
                     // XXX need location and direction
                     session.send(this, new PlaceCallRequestMessage(
                             presenceInfo, sipURL, 0., 0., 0., 90., false));
                 } else {
                     logger.warning("Failed to start softphone, retrying.");
 
                     try {
                         Thread.sleep(2000);
                     } catch (InterruptedException e) {
                     }
 
                     connectSoftphone();
                 }
             } catch (IOException e) {
                 logger.warning(e.getMessage());
             }
         } else {
             // XXX Put up a dialog box here
             logger.warning("LOCAL ADDRESS IS NULL.  " +
                     "AUDIO WILL NOT WORK!!!!!!!!!!!!");
             /*
              * Try again.
              */
             connectSoftphone();
         }
     }
 
     private void changeUsernameAlias(ChangeUsernameAliasMessage msg) {
         PresenceInfo info = msg.getPresenceInfo();
 
         pm.changeUsernameAlias(info);
 
         AvatarNameEvent avatarNameEvent = new AvatarNameEvent(
                 EventType.CHANGE_NAME, info.userID.getUsername(),
                 info.usernameAlias);
 
         InputManager.inputManager().postEvent(avatarNameEvent);
     }
 
     private void joinVoiceChat(VoiceChatJoinAcceptedMessage msg) {
         logger.fine("GOT JOIN ACCEPTED MESSAGE FOR " + msg.getCallee());
 
         PresenceInfo info = pm.getPresenceInfo(msg.getCallee().callID);
 
         logger.fine(
                 "GOT JOIN ACCEPTED FOR " + msg.getCallee() + " info " + info);
 
         if (info == null) {
             info = msg.getCallee();
 
             logger.warning("adding pm for " + info);
             pm.addPresenceInfo(info);
         }
 
         if (msg.getChatType() == ChatType.SECRET) {
             info.inSecretChat = true;
         } else {
             info.inSecretChat = false;
         }
 
         notifyMemberChangeListeners(msg.getGroup(), info, true);
     }
 
     private void leaveVoiceChat(VoiceChatLeaveMessage msg) {
         PresenceInfo callee = msg.getCallee();
 
         logger.info("GOT LEAVE MESSAGE FOR " + callee);
 
         notifyMemberChangeListeners(msg.getGroup(), callee, false);
 
         if (callee.clientID == null) {
             pm.removePresenceInfo(callee);	// it's an outworlder
         }
     }
 
     private void voiceChatCallEnded(VoiceChatCallEndedMessage msg) {
         PresenceInfo callee = msg.getCallee();
 
         String reason = getUserFriendlyReason(msg.getReasonCallEnded());
 
         logger.warning("Call ended for " + callee + " Reason:  " + reason);
 
         if (!reason.equalsIgnoreCase("Hung up") &&
                 !reason.equalsIgnoreCase("User requested call termination")) {
             callEnded(callee, reason);
         }
 
         if (callee.clientID == null) {
             pm.removePresenceInfo(callee);	// it's an outworlder
         }
 
         notifyMemberChangeListeners(msg.getGroup(), callee, false);
     }
 
     private void coneOfSilenceEnterExit(ConeOfSilenceEnterExitMessage msg) {
         pm.setEnteredConeOfSilence(presenceInfo, msg.entered());
 
         PresenceInfo info = pm.getPresenceInfo(msg.getCallID());
 
         if (info == null) {
             logger.warning("No presence info for " + msg.getCallID());
             return;
         }
 
         AvatarNameEvent avatarNameEvent;
 
         if (msg.entered()) {
             avatarNameEvent = new AvatarNameEvent(
                     EventType.ENTERED_CONE_OF_SILENCE,
                     info.userID.getUsername(), info.usernameAlias);
         } else {
             avatarNameEvent = new AvatarNameEvent(
                     EventType.EXITED_CONE_OF_SILENCE,
                     info.userID.getUsername(), info.usernameAlias);
         }
 
         InputManager.inputManager().postEvent(avatarNameEvent);
     }
 
     private void callMigrate(CallMigrateMessage msg) {
         if (callMigrationForm == null) {
             return;
         }
 
         if (msg.isSuccessful()) {
             callMigrationForm.setStatus("Migrated");
         } else {
             callMigrationForm.setStatus("Migration failed");
         }
     }
 
     private void callMuted(CallMutedMessage msg) {
         PresenceInfo info = pm.getPresenceInfo(msg.getCallID());
 
         if (info == null) {
             logger.fine("No presence info for " + msg.getCallID());
             return;
         }
 
         pm.setMute(info, msg.isMuted());
 
         AvatarNameEvent avatarNameEvent;
 
         if (msg.isMuted()) {
             avatarNameEvent = new AvatarNameEvent(EventType.MUTE,
                     info.userID.getUsername(), info.usernameAlias);
         } else {
             avatarNameEvent = new AvatarNameEvent(EventType.UNMUTE,
                     info.userID.getUsername(), info.usernameAlias);
         }
 
         InputManager.inputManager().postEvent(avatarNameEvent);
     }
 
     private void callSpeaking(CallSpeakingMessage msg) {
         PresenceInfo info = pm.getPresenceInfo(msg.getCallID());
 
         if (info == null) {
             logger.warning("No presence info for " + msg.getCallID());
             return;
         }
 
         logger.fine("Speaking " + msg.isSpeaking() + " " + info);
 
         pm.setSpeaking(info, msg.isSpeaking());
 
         AvatarNameEvent avatarNameEvent;
 
         if (msg.isSpeaking()) {
             avatarNameEvent = new AvatarNameEvent(EventType.STARTED_SPEAKING,
                     info.userID.getUsername(), info.usernameAlias);
         } else {
             avatarNameEvent = new AvatarNameEvent(EventType.STOPPED_SPEAKING,
                     info.userID.getUsername(), info.usernameAlias);
         }
 
         InputManager.inputManager().postEvent(avatarNameEvent);
     }
 
     private String getUserFriendlyReason(String reason) {
         if (reason.indexOf("Not Found") >= 0) {
             return "Invalid phone number";
         }
 
         if (reason.indexOf("No voip Gateway!") >= 0) {
             return "No connection to phone system";
         }
 
         return reason;
     }
 
     private void callEnded(CallEndedMessage msg) {
         PresenceInfo info = pm.getPresenceInfo(msg.getCallID());
 
         if (info != null && info.clientID == null) {
             pm.removePresenceInfo(info);	// it's an outworlder
         }
 
         String callID = msg.getCallID();
 
         if (!callID.equals(SoftphoneControlImpl.getInstance().getCallID())) {
             return;
         }
 
         if (callMigrationForm == null) {
             return;
         }
 
         String reason = getUserFriendlyReason(msg.getReason());
 
         if (!reason.equals("User requested call termination") &&
                 reason.indexOf("migrated") < 0) {
 
             callMigrationForm.setStatus("Call ended:  " + reason);
         }
     }
 
     private void callEnded(PresenceInfo callee, String reason) {
         CallEndedHUDPanel callEndedHUDPanel =
                 new CallEndedHUDPanel(callee, reason);
         HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
         HUDComponent callEndedHUDComponent =
                 mainHUD.createComponent(callEndedHUDPanel);
         callEndedHUDPanel.setHUDComponent(callEndedHUDComponent);
         callEndedHUDComponent.setPreferredLocation(Layout.CENTER);
         callEndedHUDComponent.setIcon(voiceChatIcon);
 
         mainHUD.addComponent(callEndedHUDComponent);
         callEndedHUDComponent.addEventListener(new HUDEventListener() {
 
             public void HUDObjectChanged(HUDEvent e) {
                 if (e.getEventType().equals(HUDEventType.DISAPPEARED)) {
                 }
             }
         });
 
         callEndedHUDComponent.setVisible(true);
     }
 
     private void playerInRange(PlayerInRangeMessage message) {
         String playerID = message.getPlayerID();
         String playerInRangeID = message.getPlayerInRangeID();
         boolean inRange = message.isInRange();
         logger.info("Player in range " + inRange + " " +
                 playerID + " player in range " + playerInRangeID);
 
         PresenceInfo info = pm.getPresenceInfo(playerID);
 
         if (info == null) {
             String waring = "No PresenceInfo for " + playerID;
             logger.warning(waring);
             System.out.println("playerInRange:  " + waring);
             return;
         }
 
         PresenceInfo userInRangeInfo = pm.getPresenceInfo(playerInRangeID);
 
         if (userInRangeInfo == null) {
             String warning = "No PresenceInfo for " + playerInRangeID;
             logger.warning(warning);
             System.out.println("inRange user " + warning);
             return;
         }
 
         notifyUserInRangeListeners(info, userInRangeInfo, inRange);
         return;
     }
 
     public ConnectionType getConnectionType() {
         return AudioManagerConnectionType.CONNECTION_TYPE;
     }
 }
