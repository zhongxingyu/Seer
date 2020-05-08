 /*
  * Copyright (C) 2010 France Telecom
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package memoplayer;
 
 /**
  * Thread-safe class to broadcast messages to scenes.
  * When the Message node is enabled it can receive messages.
  * When the Message node is disabled, it can send messages.
  */
 public class Message extends Node {
 
 
     /** 
      * Add an object at the end of the linked list to use it as a LIFO, not a FIFO 
      * @param ol teh oBjLink to add at the end of the list
      * @return the parameter itself for convenient management of hasCode 
      */
     private static ObjLink addToEndOfList (ObjLink ol) {
         if (s_incoming == null) { 
             s_incoming = ol;
         } else {
             ObjLink tmp = s_incoming;
             while (tmp.m_next != null) {
                 tmp = tmp.m_next;
             }
             tmp.m_next = ol;
             ol.m_next = null; // should be the case, but just in case!
         }
         return ol;
     }
 
     /**
      * Will send a message to all Message nodes that have the same url
      * @param url matching url
      * @param data data to send in message
      */
     public synchronized static void sendMessage(String url, MFString data) {
         //s_incoming = ObjLink.create(url, data, s_incoming);
         addToEndOfList (ObjLink.create(url, data, (ObjLink)null));
     }
     
     /**
      * Will send a message to all Message nodes that have the same url
      * @param url matching url
      * @param data data to send in message
      */
     public synchronized static void sendMessage(String url, String data) {
         //s_incoming = ObjLink.create(url, data, s_incoming);
         addToEndOfList (ObjLink.create(url, data, (ObjLink)null));
     }
     
     private synchronized static void sendMessage(String url, MFString data, int hashCode) {
         //s_incoming = ObjLink.create(url, data, s_incoming);
         //s_incoming.m_z = hashCode;
         addToEndOfList (ObjLink.create(url, data, (ObjLink)null)).m_z = hashCode;
     }
     
     /**
      * Queues must be switched after each loop by the MyCanvas thread. 
      */
     public synchronized static boolean switchQueues() {
         ObjLink.releaseAll (s_delivered);
         s_delivered = s_incoming;
         s_incoming = null;
         return s_delivered != null;
     }
     
     /**
      * Queue of messages being delivered.
      */
     private static ObjLink s_delivered;
     
     /**
      * Queue of incoming messages that will be delivered on next queue switch
      */
     private static ObjLink s_incoming;

    /**
     * Keep a static counter to uniquely identify by integer all Message instances
     */
    private static int s_count;
     
     private String m_url;
     private MFString m_data;
     private boolean m_enabled;
    private final int m_id = s_count++; // unique integer id (safe as always instantiated from main thread)
     
     Message() {
         super(3);
         m_field[0] = new MFString(this); // url
         m_field[1] = new MFString(); // set_data
         m_field[2] = new MFString(); // data_changed
     }
     
     void start(Context c) {
         if (m_data == null) { // first start
             m_data = (MFString)m_field[1];
             fieldChanged(m_field[0]); // url
             fieldChanged(m_field[1]); // set_data
         }
         m_enabled = true;
         m_field[1].addObserver(this);
     }
     
     void stop(Context c) {
         // Always stop listening to messages when stopped
         if (m_enabled) {
             m_field[1].removeObserver(this);
             m_enabled = false;
         }
     }
     
     boolean compose(Context c, Region clip, boolean forceUpdate) {
         if (m_enabled) {
             ObjLink o = s_delivered;
             while (o != null) {
                // check id is different so the sender does not receive the message
                if(m_url.equals(o.m_object) && o.m_z != m_id) {
                     if (o.m_param instanceof MFString) {
                         ((MFString)m_field[2]).setValues((MFString)o.m_param);
                     } else {
                         ((MFString)m_field[2]).m_size = 0; // Empty MFString
                         ((MFString)m_field[2]).setValue(0, (String)o.m_param);
                     }
                 }
                 o = o.m_next;
             }
             if (m_isUpdated) {
                 m_isUpdated = false;
                 // Copy values in new temporary MFString
                 MFString data = new MFString();
                 data.setValues(m_data);
                 // Send message to incoming queue
                sendMessage(m_url, data, m_id);
                 
             }
         } 
         return false;
     }
     
     public void fieldChanged(Field f) {
         if (f == m_field[0]) { // url
             m_url = ((MFString)f).getValue(0);
         } else if (f == m_field[1]) { // set_data
             // Wait for next compose to send data !
             m_isUpdated = m_data.m_size != 0;
         }
     }
 }
