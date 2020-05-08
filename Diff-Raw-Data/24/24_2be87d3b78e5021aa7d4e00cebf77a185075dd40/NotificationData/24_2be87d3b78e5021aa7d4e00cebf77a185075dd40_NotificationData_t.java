 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2011 Funambol, Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  * 
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol".
  */
 
 package com.funambol.client.controller;
 
 /**
  * An abstract and platform-independent implementation of a notification data.
  * Usually, a notification is a type of message to the user not
  * linked with the graphic interface of the main application.
  */
 public class NotificationData {
     /** Normal message */
     public static final int SEVERITY_NORMAL = 0;
     /** A warning message, normally with an associated warning mood (yellow icon, text message etc) */
     public static final int SEVERITY_WARNING = 1;
     /** An error message, normally with an associated error mood (red icon etc) */
     public static final int SEVERITY_ERROR = 2;
     
     
     private int id;
     /**
      * Gets the notification ID, a code useful for identify the notification.
      */
     public int getId() {
         return id;
     }
     /**
      * Sets the notification ID, use the value as you want
      * @param id
      */
     public NotificationData setId(int id) {
         this.id = id;
         return this;
     }
     
     private int severity;
     /**
      * Returns notification severity, one of the SEVERITY_XXX constrains
      */
     public int getSeverity() {
         return severity;
     }
     /**
      * Sets notification severity. Use one of the SEVERITY_XXX constrains
      * @param severity
      */
     public NotificationData setSeverity(int severity) {
         this.severity = severity;
         return this;
     }
     
     private String ticker;
     /**
      * Gets ticker text, a text that appears in notification bar 
      * @return
      */
     public String getTicker() {
         return ticker;
     }
     public NotificationData setTicker(String ticker) {
         this.ticker = ticker;
         return this;
     }
 
     private String title;
     /**
      * Gets title, a text that appears as notification title when the notification is displayed
      * @return
      */
     public String getTitle() {
         return title;
     }
     public NotificationData setTitle(String title) {
         this.title = title;
         return this;
     }
 
     private String message;
     /**
      * Gets notification main message, a text that appears when the notification is displayed
      * @return
      */
     public String getMessage() {
         return message;
     }
     /**
      * Sets notification main message.
      */
     public NotificationData setMessage(String message) {
         this.message = message;
         return this;
     }
     
     private Object tag;
     /**
      * Gets a generic object for the notification.
      */
     public Object getTag() {
         return tag;
     }
     /**
      * Sets a generic object for the notification. In Android, for example,
     * the tag contains the Activity to launch when the user press
      * the notification
      */
     public NotificationData setTag(Object tag) {
         this.tag = tag;
         return this;
     }
     
     
     
     private NotificationData() {
         super();
     }
     
     
     /**
      * Factory class
      */
     public static class Factory {
         /**
          * Creates a new notification object
          * 
          * @param id
          * @param severity
          * @param ticker
          * @param title
          * @param message
          * @param tag
          * @return
          */
         public static NotificationData create(
                 int id,
                 int severity,
                 String ticker,
                 String title,
                 String message,
                 Object tag) {
             NotificationData nd = new NotificationData()
                     .setId(id)
                     .setSeverity(severity)
                     .setTicker(ticker)
                     .setTitle(title)
                     .setMessage(message)
                     .setTag(tag);
             return nd;
         }
         public static NotificationData create(int id, int severity, String title, String message) {
             return create(id, severity, null, title, message, null);
         }
         public static NotificationData create(int severity, String title, String message) {
             return create(0, severity, null, title, message, null);
         }
         public static NotificationData create(int severity, String ticker, String title, String message, Object tag) {
             return create(0, severity, ticker, title, message, tag);
         }
     }
     
 }
