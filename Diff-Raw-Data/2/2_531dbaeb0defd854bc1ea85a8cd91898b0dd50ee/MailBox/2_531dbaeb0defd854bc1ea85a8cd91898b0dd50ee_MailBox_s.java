 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of VinciCode.
  * 
  * VinciCode is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * VinciCode is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VinciCode.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.vincicode.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.minestar.minestarlibrary.messages.Message;
 
 public class MailBox {
 
     private int currentMessagePosition = 0;
     private ArrayList<Message> mailBox;
 
     private boolean newMessages;
 
     // NEW MAIL BOX
     public MailBox() {
         mailBox = new ArrayList<Message>();
         this.newMessages = false;
     }
 
     // MAIL BOX FROM DATABASE
     public MailBox(List<Message> messages) {
         mailBox = new ArrayList<Message>(messages);
         searchForNewMessages();
     }
 
     public boolean hasNext() {
         return currentMessagePosition < this.mailBox.size();
     }
 
     public Message next() {
         if (this.hasNext()) {
             ++currentMessagePosition;
             if (currentMessagePosition > this.mailBox.size()) {
                 currentMessagePosition = this.mailBox.size();
             }
             return this.mailBox.get(currentMessagePosition - 1);
         }
         return null;
     }
 
     public boolean hasPrev() {
         return currentMessagePosition > 1;
     }
 
     public Message prev() {
         if (this.hasPrev()) {
             --currentMessagePosition;
             if (currentMessagePosition < 0) {
                currentMessagePosition = 0;
             }
             return this.mailBox.get(currentMessagePosition - 1);
         }
         return null;
     }
 
     public void add(Message message) {
         mailBox.add(message);
         this.newMessages = true;
     }
 
     public void deleteCurrent() {
         mailBox.remove(this.currentMessagePosition - 1);
     }
 
     public boolean hasNewMessages() {
         return newMessages;
     }
 
     public void markAsRead(Message message) {
         message.setRead(false);
         searchForNewMessages();
     }
 
     private void searchForNewMessages() {
         for (Message message : mailBox) {
             if (!message.isRead()) {
                 this.newMessages = true;
                 break;
             }
         }
     }
 
     public List<Message> getAllMessages() {
         return new ArrayList<Message>(mailBox);
     }
 
     public int getMessageCount() {
         return this.mailBox.size();
     }
 
     public int getCurrentMessagePosition() {
         return currentMessagePosition;
     }
 
     @Override
     public String toString() {
         if (mailBox.isEmpty())
             return "[]";
         StringBuilder sBuilder = new StringBuilder(mailBox.size() * 32);
         sBuilder.append('[');
         for (Message msg : mailBox) {
             sBuilder.append(msg.getCompleteMessage());
             sBuilder.append(", ");
         }
 
         sBuilder.deleteCharAt(sBuilder.length() - 1);
         sBuilder.setCharAt(sBuilder.length() - 1, ']');
 
         return sBuilder.toString();
     }
 }
