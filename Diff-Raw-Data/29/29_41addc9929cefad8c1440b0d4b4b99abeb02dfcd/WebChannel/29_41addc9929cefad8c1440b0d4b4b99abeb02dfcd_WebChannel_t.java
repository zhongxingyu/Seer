 /*
  *  Copyright 2010 Tako Schotanus <tako@codejive.org>.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package org.codejive.web.channel;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 import org.codejive.web.sws.StableWebSocket;
 import org.json.simple.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Tako Schotanus <tako@codejive.org>
  */
 public class WebChannel {
     private StableWebSocket socket;
     private String peerId;
     private String id;
     private String uniqueId;
 
     private Set<WebChannelListener> listeners;
     private boolean open;
     private boolean closed;
 
     private static final String ATTR_CMD = "$cmd";
 
     private static final String CMD_OPEN = "open";
     private static final String CMD_CLOSE = "close";
 
     private static final Logger log = LoggerFactory.getLogger(WebChannel.class);
 
     public String getId() {
         return id;
     }
 
     public String getPeerId() {
         return peerId;
     }
 
     public String getUniqueId() {
         return uniqueId;
     }
 
     public WebChannel(StableWebSocket socket, String peerId, String id) {
         this.socket = socket;
         this.peerId = peerId;
         this.id = id;
         uniqueId = makeUniqueId(socket, id);
         listeners = new HashSet<WebChannelListener>();
         open = false;
         closed = false;
     }
 
     boolean belongsTo(StableWebSocket socket) {
         return this.socket.getId().equals(socket.getId());
     }
 
     public void open(String service) throws IOException {
         if (open) {
             throw new IllegalStateException("Channel already open");
         }
         JSONObject msg = new JSONObject();
         msg.put(ATTR_CMD, CMD_OPEN);
         msg.put("service", service);
         send(msg);
     }
     
     protected void onOpen(String peerId) {
        if (peerId != null) {
            this.peerId = peerId;
        }
        log.info("Opened channel {}-{}", id, this.peerId);
         open = true;
         closed = false;
         fireOnOpen();
     }
 
     protected void onMessage(JSONObject msg) {
         log.debug("Received message on channel {}-{}", id, peerId);
         fireOnMessage(msg);
     }
 
     public void send(JSONObject msg) throws IOException {
         msg.put("$dst", peerId);
         msg.put("$src", id);
         socket.sendMessage(msg.toJSONString());
     }
 
     private void sendClose() throws IOException {
         JSONObject msg = new JSONObject();
         msg.put(ATTR_CMD, CMD_CLOSE);
         send(msg);
     }
     
     public void close() {
         log.info("Closing channel {}-{}", id, peerId);
         if (open) {
             try {
                 sendClose();
             } catch (IOException ex) {
                 log.warn("Couldn't send 'close' message to peer", ex);
             }
         }
         onClose();
     }
 
     protected void onClose() {
         if (!closed) {
             open = false;
             closed = true;
            log.info("Closed channel {}-{}", id, peerId);
             fireOnClose();
             listeners = null;
             socket = null;
         }
     }
 
     protected void onDisconnect() {
         log.info("Channel {}-{} disconnected", id, peerId);
         fireOnDisconnect();
     }
 
     protected void onReconnect() {
         log.info("Channel {}-{} reconnected", id, peerId);
         fireOnReconnect();
     }
 
     public void addWebChannelEventListener(WebChannelListener listener) {
         listeners.add(listener);
     }
 
     public void removeWebChannelEventListener(WebChannelListener listener) {
         listeners.remove(listener);
     }
 
     private void fireOnOpen() {
         for (WebChannelListener l : listeners) {
             try {
                 l.onOpen(this);
             } catch (Throwable th) {
                 log.warn("Error occurred while firing event 'open' on a listener", th);
             }
         }
     }
 
     private void fireOnMessage(JSONObject info) {
         for (WebChannelListener l : listeners) {
             try {
                 l.onMessage(this, info);
             } catch (Throwable th) {
                 log.warn("Error occurred while firing event 'message' on a listener", th);
             }
         }
     }
 
     private void fireOnDisconnect() {
         for (WebChannelListener l : listeners) {
             try {
                 l.onDisconnect(this);
             } catch (Throwable th) {
                 log.warn("Error occurred while firing event 'disconnect' on a listener", th);
             }
         }
     }
 
     private void fireOnReconnect() {
         for (WebChannelListener l : listeners) {
             try {
                 l.onReconnect(this);
             } catch (Throwable th) {
                 log.warn("Error occurred while firing event 'reconnect' on a listener", th);
             }
         }
     }
 
     private void fireOnClose() {
         for (WebChannelListener l : listeners) {
             try {
                 l.onClose(this);
             } catch (Throwable th) {
                 log.warn("Error occurred while firing event 'close' on a listener", th);
             }
         }
     }
 
     @Override
     public String toString() {
         return "WebChannel(" + id + ", " + peerId + ") on " + socket;
     }
 
     public static String makeUniqueId(StableWebSocket socket, String id) {
         return id + "@" + socket.getId();
     }
 }
