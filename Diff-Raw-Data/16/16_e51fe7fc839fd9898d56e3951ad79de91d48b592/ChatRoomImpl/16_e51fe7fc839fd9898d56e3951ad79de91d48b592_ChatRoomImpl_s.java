 /**
  * $Revision: $
  * $Date: $
  *
  * Copyright (C) 2006 Jive Software. All rights reserved.
  *
  * This software is published under the terms of the GNU Lesser Public License (LGPL),
  * a copy of which is included in this distribution.
  */
 
 package org.jivesoftware.spark.ui.rooms;
 
 import org.jivesoftware.resource.SparkRes;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.filter.AndFilter;
 import org.jivesoftware.smack.filter.FromContainsFilter;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.filter.PacketTypeFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.util.StringUtils;
 import org.jivesoftware.smackx.MessageEventManager;
 import org.jivesoftware.smackx.MessageEventRequestListener;
 import org.jivesoftware.smackx.packet.VCard;
 import org.jivesoftware.spark.SparkManager;
 import org.jivesoftware.spark.ui.ChatRoom;
 import org.jivesoftware.spark.ui.ChatRoomButton;
 import org.jivesoftware.spark.ui.ContactItem;
 import org.jivesoftware.spark.ui.ContactList;
 import org.jivesoftware.spark.ui.MessageEventListener;
 import org.jivesoftware.spark.ui.RosterDialog;
 import org.jivesoftware.spark.ui.VCardPanel;
 import org.jivesoftware.spark.ui.status.StatusItem;
 import org.jivesoftware.spark.util.ModelUtil;
 import org.jivesoftware.spark.util.SwingWorker;
 import org.jivesoftware.spark.util.log.Log;
 import org.jivesoftware.sparkimpl.profile.VCardManager;
 
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.Icon;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.event.DocumentEvent;
 
 /**
  * This is the Person to Person implementation of <code>ChatRoom</code>
  * This room only allows for 1 to 1 conversations.
  */
 public class ChatRoomImpl extends ChatRoom {
 
     private List messageEventListeners = new ArrayList();
     private String roomname;
     private Icon tabIcon;
     private String roomTitle;
     private String tabTitle;
     private String participantJID;
     private String participantNickname;
     private MessageEventRequestListener messageEventRequestListener;
 
     boolean isOnline = true;
 
     private Presence presence;
 
     private boolean offlineSent;
 
     private Roster roster;
 
     private long lastTypedCharTime;
     private boolean sendNotification;
 
     private Timer typingTimer;
     private boolean sendTypingNotification;
     private String threadID;
 
     private long lastActivity;
 
 
     /**
      * Constructs a 1-to-1 ChatRoom.
      *
      * @param participantJID      the participants jid to chat with.
      * @param participantNickname the nickname of the participant.
      * @param title               the title of the room.
      */
     public ChatRoomImpl(final String participantJID, String participantNickname, String title) {
         this.participantJID = participantJID;
         this.participantNickname = participantNickname;
 
         AndFilter presenceFilter = new AndFilter(new PacketTypeFilter(Presence.class), new FromContainsFilter(this.participantJID));
 
         // Register PacketListeners
 
         AndFilter messageFilter = new AndFilter(new PacketTypeFilter(Message.class), new FromContainsFilter(participantJID));
         SparkManager.getConnection().addPacketListener(this, messageFilter);
 
         SparkManager.getConnection().addPacketListener(this, presenceFilter);
 
         // The roomname will be the participantJID
         this.roomname = participantJID;
 
         // Use the agents username as the Tab Title
         this.tabTitle = title;
 
         // The name of the room will be the node of the user jid + conversation.
         final SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        this.roomTitle = participantNickname + " - Started " + formatter.format(new Date());
 
         // Add RoomInfo
         this.getSplitPane().setRightComponent(null);
         getSplitPane().setDividerSize(0);
 
 
         roster = SparkManager.getConnection().getRoster();
         presence = roster.getPresence(participantJID);
         StatusItem statusItem = SparkManager.getWorkspace().getStatusBar().getItemFromPresence(presence);
 
         RosterEntry entry = roster.getEntry(participantJID);
 
         if (statusItem == null) {
             tabIcon = SparkRes.getImageIcon(SparkRes.CLEAR_BALL_ICON);
         }
         else {
             String status = presence.getStatus();
             if (status != null && status.indexOf("phone") != -1) {
                 tabIcon = SparkRes.getImageIcon(SparkRes.ON_PHONE_IMAGE);
             }
             else {
                 tabIcon = statusItem.getIcon();
             }
         }
 
         Icon icon = SparkManager.getChatManager().getPresenceIconForContactHandler(presence);
         if (icon != null) {
             tabIcon = icon;
         }
 
         PacketFilter filter = new AndFilter(new PacketTypeFilter(Presence.class), new FromContainsFilter(participantJID));
         SparkManager.getConnection().addPacketListener(new PacketListener() {
             public void processPacket(Packet packet) {
                 presence = (Presence)packet;
             }
         }, filter);
 
         // Create toolbar buttons.
         ChatRoomButton infoButton = new ChatRoomButton("", SparkRes.getImageIcon(SparkRes.PROFILE_IMAGE_24x24));
         infoButton.setToolTipText("View information about this user");
 
         // Create basic toolbar.
         getToolBar().addChatRoomButton(infoButton);
 
         // If the user is not in the roster, then allow user to add them.
         if (entry == null && !StringUtils.parseResource(participantJID).equals(participantNickname)) {
             ChatRoomButton addToRosterButton = new ChatRoomButton("", SparkRes.getImageIcon(SparkRes.ADD_IMAGE_24x24));
             addToRosterButton.setToolTipText("Add this user to your roster.");
             getToolBar().addChatRoomButton(addToRosterButton);
             addToRosterButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     RosterDialog rosterDialog = new RosterDialog();
                     rosterDialog.setDefaultJID(participantJID);
                     rosterDialog.setDefaultNickname(getParticipantNickname());
                     rosterDialog.showRosterDialog(SparkManager.getChatManager().getChatContainer().getChatFrame());
                 }
             });
         }
 
         // Show VCard.
         infoButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 VCardManager vcard = SparkManager.getVCardManager();
                 vcard.viewProfile(participantJID, SparkManager.getChatManager().getChatContainer());
             }
         });
 
         // If this is a private chat from a group chat room, do not show toolbar.
         if (StringUtils.parseResource(participantJID).equals(participantNickname)) {
             getToolBar().setVisible(false);
         }
 
 
         typingTimer = new Timer(2000, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 long now = System.currentTimeMillis();
                 if (now - lastTypedCharTime > 2000) {
                     if (!sendNotification) {
                         // send cancel
                         SparkManager.getMessageEventManager().sendCancelledNotification(getParticipantJID(), threadID);
 
                         sendNotification = true;
                     }
 
 
                 }
             }
         });
 
         typingTimer.start();
 
         // Add message event request listener
         messageEventRequestListener = new ChatMessageEventRequestListener();
         SparkManager.getMessageEventManager().addMessageEventRequestListener(messageEventRequestListener);
 
 
         lastActivity = System.currentTimeMillis();
     }
 
 
 
     public void closeChatRoom() {
         super.closeChatRoom();
 
         SparkManager.getChatManager().removeChat(this);
 
         SparkManager.getConnection().removePacketListener(this);
     }
 
     public void sendMessage() {
         String text = getChatInputEditor().getText();
         sendMessage(text);
     }
 
     public void sendMessage(String text) {
         final Message message = new Message();
 
         if (threadID == null) {
             threadID = StringUtils.randomString(6);
         }
         message.setThread(threadID);
 
         // Set the body of the message using typedMessage
         message.setBody(text);
 
         // IF there is no body, just return and do nothing
         if (!ModelUtil.hasLength(text)) {
             return;
         }
 
         // Fire Message Filters
         SparkManager.getChatManager().filterOutgoingMessage(this, message);
 
         sendMessage(message);
 
         sendNotification = true;
     }
 
     /**
      * Sends a message to the appropriate jid. The message is automatically added to the transcript.
      *
      * @param message the message to send.
      */
     public void sendMessage(Message message) {
         lastActivity = System.currentTimeMillis();
         
         // Check to see if the user is online to recieve this message.
         RosterEntry entry = roster.getEntry(participantJID);
         if (presence == null && !offlineSent && entry != null) {
             getTranscriptWindow().insertErrorMessage("The user is offline and will receive the message on their next login.");
             offlineSent = true;
         }
 
         try {
             getTranscriptWindow().insertMessage(getNickname(), message);
             getChatInputEditor().selectAll();
 
             getTranscriptWindow().validate();
             getTranscriptWindow().repaint();
             getChatInputEditor().clear();
         }
         catch (Exception ex) {
             Log.error("Error sending message", ex);
         }
 
         // Before sending message, let's add our full jid for full verification
         message.setType(Message.Type.CHAT);
         message.setTo(participantJID);
         message.setFrom(SparkManager.getSessionManager().getJID());
 
         // Notify users that message has been sent
         fireMessageSent(message);
 
         addToTranscript(message, false);
 
         getChatInputEditor().setCaretPosition(0);
         getChatInputEditor().requestFocusInWindow();
         scrollToBottom();
 
 
         MessageEventManager.addNotificationsRequests(message, true, true, true, true);
 
         // Send the message that contains the notifications request
         try {
             fireOutgoingMessageSending(message);
             SparkManager.getConnection().sendPacket(message);
         }
         catch (Exception ex) {
             Log.error("Error sending message", ex);
         }
     }
 
     public String getRoomname() {
         return roomname;
     }
 
 
     public Icon getTabIcon() {
         return tabIcon;
     }
 
     public String getTabTitle() {
         return tabTitle;
     }
 
     public String getRoomTitle() {
         return roomTitle;
     }
 
     public Message.Type getChatType() {
         return Message.Type.CHAT;
     }
 
     public void leaveChatRoom() {
         // There really is no such thing in Agent to Agent
     }
 
     public boolean isActive() {
         return true;
     }
 
 
     public String getParticipantJID() {
         return participantJID;
     }
 
     /**
      * Returns the users full jid (ex. macbeth@jivesoftware.com/spark).
      *
      * @return the users Full JID.
      */
     public String getJID() {
         presence = roster.getPresence(getParticipantJID());
         if (presence == null) {
             return getParticipantJID();
         }
         return presence.getFrom();
     }
 
     /**
      * Process incoming packets.
      *
      * @param packet - the packet to process
      */
     public void processPacket(final Packet packet) {
         final Runnable runnable = new Runnable() {
             public void run() {
                 if (packet instanceof Presence) {
                     final Presence presence = (Presence)packet;
 
                     ContactList list = SparkManager.getWorkspace().getContactList();
                     ContactItem contactItem = list.getContactItemByJID(getParticipantJID());
 
                     final SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
                     String time = formatter.format(new Date());
 
                     if (presence.getType() == Presence.Type.unavailable && contactItem != null) {
                         if (isOnline) {
                             getTranscriptWindow().insertNotificationMessage("*** " + participantNickname + " went offline at " + time + ".");
                         }
                         isOnline = false;
                     }
                     else if (presence.getType() == Presence.Type.available) {
                         if (!isOnline) {
                             getTranscriptWindow().insertNotificationMessage("*** " + participantNickname + " is online at " + time + ".");
                         }
                         isOnline = true;
                     }
                 }
                 else if (packet instanceof Message) {
                     lastActivity = System.currentTimeMillis();
 
                     // Do something with the incoming packet here.
                     final Message message = (Message)packet;
                     if (message.getError() != null) {
                         return;
                     }
 
                     if (threadID == null) {
                         threadID = message.getThread();
                         if (threadID == null) {
                             threadID = StringUtils.randomString(6);
                         }
                     }
 
                     boolean broadcast = message.getProperty("broadcast") != null;
 
                     // If this is a group chat message, discard
                     if (message.getType() == Message.Type.GROUP_CHAT || broadcast) {
                         return;
                     }
 
                     // Do not accept Administrative messages.
                     String host = SparkManager.getSessionManager().getServerAddress();
                     if (host.equals(message.getFrom())) {
                         return;
                     }
 
                     // If the message is not from the current agent. Append to chat.
                     if (message.getBody() != null) {
                         if (!participantJID.equals(message.getFrom())) {
                             // Create new Chat Object.
                             participantJID = message.getFrom();
                         }
                         participantJID = message.getFrom();
                         insertMessage(message);
 
                         showTyping(false);
                     }
                 }
             }
         };
         SwingUtilities.invokeLater(runnable);
     }
 
     /**
      * Returns the nickname of the user chatting with.
      *
      * @return the nickname of the chatting user.
      */
     public String getParticipantNickname() {
         return participantNickname;
     }
 
 
     /**
      * The current SendField has been updated somehow.
      *
      * @param e - the DocumentEvent to respond to.
      */
     public void insertUpdate(DocumentEvent e) {
         checkForText(e);
 
         lastTypedCharTime = System.currentTimeMillis();
 
         // If the user pauses for more than two seconds, send out a new notice.
         if (sendNotification) {
             try {
                 if (sendTypingNotification) {
                     SparkManager.getMessageEventManager().sendComposingNotification(getParticipantJID(), threadID);
                 }
                 sendNotification = false;
             }
             catch (Exception exception) {
                 Log.error("Error updating", exception);
             }
         }
     }
 
     public void insertMessage(Message message) {
         // Debug info
         super.insertMessage(message);
 
         getTranscriptWindow().insertOthersMessage(participantNickname, message);
 
         // Set the participant jid to their full JID.
         participantJID = message.getFrom();
     }
 
     public void addMessageEventListener(MessageEventListener listener) {
         messageEventListeners.add(listener);
     }
 
     public void removeMessageEventListener(MessageEventListener listener) {
         messageEventListeners.remove(listener);
     }
 
     public Collection getMessageEventListeners() {
         return messageEventListeners;
     }
 
     public void fireOutgoingMessageSending(Message message) {
         Iterator messageEventListeners = new ArrayList(getMessageEventListeners()).iterator();
         while (messageEventListeners.hasNext()) {
             ((MessageEventListener)messageEventListeners.next()).sendingMessage(message);
         }
     }
 
     public void fireReceivingIncomingMessage(Message message) {
         Iterator messageEventListeners = new ArrayList(getMessageEventListeners()).iterator();
         while (messageEventListeners.hasNext()) {
             ((MessageEventListener)messageEventListeners.next()).receivingMessage(message);
         }
     }
 
     /**
      * Internal implementation of the MessageEventRequestListener.
      */
     private class ChatMessageEventRequestListener implements MessageEventRequestListener {
         public void deliveredNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) {
             // DefaultMessageEventRequestListener automatically responds that the message was delivered when receives this request
             messageEventManager.sendDeliveredNotification(from, packetID);
         }
 
         public void displayedNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) {
             // Send to the message's sender that the message was displayed
             messageEventManager.sendDisplayedNotification(from, packetID);
         }
 
         public void composingNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) {
             sendTypingNotification = true;
         }
 
         public void offlineNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) {
             // The XMPP server should take care of this request. Do nothing.
         }
     }
 
     public void showTyping(boolean typing) {
         if (typing) {
             String isTypingText = participantNickname + " is typing a message...";
             getNotificationLabel().setText(isTypingText);
             getNotificationLabel().setIcon(SparkRes.getImageIcon(SparkRes.SMALL_MESSAGE_EDIT_IMAGE));
         }
         else {
             // Remove is typing text.
             getNotificationLabel().setText("");
             getNotificationLabel().setIcon(SparkRes.getImageIcon(SparkRes.BLANK_IMAGE));
         }
 
     }
 
     public long getLastActivity() {
         return lastActivity;
     }
 
 
 }
