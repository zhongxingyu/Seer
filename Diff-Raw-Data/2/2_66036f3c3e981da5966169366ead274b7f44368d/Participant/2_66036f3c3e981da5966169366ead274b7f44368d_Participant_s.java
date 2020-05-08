 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.sample.portlet.chat;
 
 import org.icefaces.sample.portlet.chat.resources.ResourceUtil;
 
 import javax.annotation.PreDestroy;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.event.ActionEvent;
 import java.io.Serializable;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * The Participant class stores information about an individual participant
  * in the chat room. Since this is a fairly simple example, it also stores
  * information about the state of the current conversation for the the user
  * (e.g. what part of the chat history they are currently viewing).  In a
  * more sophisticated application, this could potentially held in a separate
  * bean.
  * <p/>
  */
 @ManagedBean
 @SessionScoped
 public class Participant implements Serializable {
 
     private static Logger log = Logger.getLogger(Participant.class.getName());
 
     @ManagedProperty(value="#{chatRoom}")
     private transient ChatRoom chatRoom;
 
     private String handle;
     private String message;
 
     private int firstMessageIndex = 0;
     private int numOfDisplayedMessages = 5;
 
     public Participant() {
     }
 
     public String getHandle() {
         return handle;
     }
 
     public void setHandle(String handle) {
         this.handle = handle.trim();
     }
 
     public ChatRoom getChatRoom() {
         return chatRoom;
     }
 
     public void setChatRoom(ChatRoom chatRoom) {
         this.chatRoom = chatRoom;
     }
 
     public String getMessage() {
         return "";
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public void login(ActionEvent event) {
         if (handle != null && handle.trim().length() > 0) {
             if (!chatRoom.hasParticipant(this)) {
                 chatRoom.addParticipant(this);
             } else {
                 ResourceUtil.addLocalizedMessage("alreadyRegistered", handle);
                 handle = null;
             }
         } else {
             ResourceUtil.addLocalizedMessage("badHandle");
         }
     }
 
     public void sendMessage(ActionEvent event) {
         if (!chatRoom.hasParticipant(this) || message == null || message.trim().length() < 1) {
             return;
         }
         chatRoom.addMessage(this, message);
     }
 
     public void logout(ActionEvent event) {
        logout(null);
     }
 
     @PreDestroy
     public void logout() {
         chatRoom.removeParticipant(this);
         handle = null;
     }
 
     public boolean isRegistered() {
         return chatRoom.hasParticipant(this);
     }
 
     public void setRegistered(boolean registered) {
     }
 
     public String toString() {
         return super.toString() + " [" + handle + "]";
     }
 
     public int getFirstMessageIndex() {
         return firstMessageIndex;
     }
 
     public void setFirstMessageIndex(int firstMessageIndex) {
     }
 
     public int getNumOfDisplayedMessages() {
         return numOfDisplayedMessages;
     }
 
     public void setNumOfDisplayedMessages(int numOfDisplayedMessages) {
     }
 
     public boolean isOlder() {
         return (firstMessageIndex + numOfDisplayedMessages) < chatRoom.getNumberOfMessages();
     }
 
     public void olderMessages(ActionEvent event) {
         firstMessageIndex++;
         if (firstMessageIndex > chatRoom.getNumberOfMessages() - 1) {
             firstMessageIndex = chatRoom.getNumberOfMessages() - 1;
         }
     }
 
     public boolean isNewer() {
         return firstMessageIndex > 0;
     }
 
     public void newerMessages(ActionEvent event) {
         firstMessageIndex--;
         if (firstMessageIndex < 0) {
             firstMessageIndex = 0;
         }
     }
 
     public List getMessages() {
         return chatRoom.getMessages(firstMessageIndex, numOfDisplayedMessages);
     }
 
 
 }
