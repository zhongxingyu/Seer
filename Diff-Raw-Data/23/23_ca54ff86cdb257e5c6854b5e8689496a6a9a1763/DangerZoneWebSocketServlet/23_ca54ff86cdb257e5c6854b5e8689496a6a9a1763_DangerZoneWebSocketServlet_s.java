 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.codejive.websocket.wstestserver;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.websocket.WebSocket;
 import org.eclipse.jetty.websocket.WebSocketServlet;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DangerZoneWebSocketServlet extends WebSocketServlet {
 
     private final Map<String, DangerZoneWebSocket> _members = new ConcurrentHashMap<String, DangerZoneWebSocket>();
     private final Set<String> _storageIds = new CopyOnWriteArraySet<String>();
     private final Map<String, JSONObject> _storage = new ConcurrentHashMap<String, JSONObject>();
     private long _zoneId = 1;
     
     private enum Event {
         ready, store, remove, clear, clients, client, ping, pong
     }
 
     Logger logger = LoggerFactory.getLogger(DangerZoneWebSocketServlet.class);
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws javax.servlet.ServletException, IOException {
         getServletContext().getNamedDispatcher("default").forward(request, response);
     }
 
     @Override
     public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
         super.service(req, res);
     }
 
     @Override
     protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
         return new DangerZoneWebSocket();
     }
 
     /* ------------------------------------------------------------ */
     /* ------------------------------------------------------------ */
     class DangerZoneWebSocket implements WebSocket {
 
         private Outbound _outbound;
         private String _socketId;
         private String _userName;
 
         private static final String ACTION_INIT = "init";
         private static final String ACTION_ACTIVATE = "activate";
         private static final String ACTION_MULTI = "multi";
         private static final String ACTION_CLIENTS = "clients";
         private static final String ACTION_CLIENT_CHANGE = "client";
         private static final String ACTION_CLIENT_CONNECT = "connect";
         private static final String ACTION_CLIENT_DISCONNECT = "disconnect";
         private static final String ACTION_PONG = "pong";
 
         @Override
         public void onConnect(Outbound outbound) {
             logger.info(this + " onConnect");
             _outbound = outbound;
             _socketId = Long.toString(_zoneId++);
             _userName = "Client #" + _socketId;
             _members.put(_socketId, this);
             sendAll(ACTION_CLIENT_CONNECT, newClientInfo(this), false);
         }
 
         @Override
         public void onMessage(byte frame, byte[] data, int offset, int length) {
             // Log.info(this+" onMessage: "+TypeUtil.toHexString(data,offset,length));
         }
 
         @Override
         public void onMessage(byte frame, String msg) {
             logger.info(this + " onMessage: " + msg);
             try {
                 JSONParser parser = new JSONParser();
                 JSONObject info = (JSONObject) parser.parse(msg);
                 String to = (String) info.get("to");
                 String action = (String) info.get("action");
                 Object data = info.get("data");
                 if (to == null || "sys".equals(to)) {
                     // The message is for the server
                     Event event = Event.valueOf(action.toLowerCase());
                     onAction(event, data);
                 } else if ("all".equals(to)) {
                     // Send the message to all conencted sockets
                     sendAll(action, data, false);
                 } else {
                     // Send the message to the indicated socket
                     sendTo(to, action, data);
                 }
             } catch (ParseException ex) {
                 logger.error("Couldn't parse incoming message", ex);
             }
         }
 
         @Override
         public void onDisconnect() {
             logger.info(this + " onDisconnect");
             _members.remove(_socketId);
             sendAll(ACTION_CLIENT_DISCONNECT, newClientInfo(this), false);
         }
 
         private void onAction(Event event, Object data) {
             switch (event) {
                 case ready: {
                     LinkedList<JSONObject> list = new LinkedList();
                     list.add(newActionInfo(ACTION_INIT, _socketId));
                     list.add(newActionInfo(ACTION_CLIENTS, getClientList()));
                     list.add(newActionInfo(ACTION_ACTIVATE, "rates"));
                    list.add(newActionInfo(ACTION_ACTIVATE, "clients"));
                    list.add(newActionInfo(ACTION_ACTIVATE, "starbutton"));
                    list.add(newActionInfo(ACTION_ACTIVATE, "keepalive"));
                     for (String id : _storageIds) {
                         JSONObject info = _storage.get(id);
                         String action = (String) info.get("action");
                         Object dat = info.get("data");
                         list.add(newActionInfo(action, dat));
                     }
                     sendMultiple("sys", list);
                     break;
                 }
                 case store: {
                     // Store and send the message to all connected sockets
                     JSONObject info = (JSONObject) data;
                     String id = (String) info.get("id");
                     _storageIds.add(id);
                     _storage.put(id, info);
                     String action = (String) info.get("action");
                     Object dat = info.get("data");
                     sendAll(action, dat, false);
                     break;
                 }
                 case remove: {
                    // "Unstore" and send the message to all conencted sockets
                    String id = (String) data;
                    _storageIds.remove(id);
                    _storage.remove(id);
                     break;
                 }
                 case clear: {
                     _storageIds.clear();
                     _storage.clear();
                     break;
                 }
                 case clients: {
                     send("sys", ACTION_CLIENTS, getClientList());
                     break;
                 }
                 case client: {
                     JSONObject info = (JSONObject) data;
                     _userName = (String) info.get("name");
                     sendAll(ACTION_CLIENT_CHANGE, newClientInfo(this), true);
                     break;
                 }
                 case ping: {
                     send("sys", ACTION_PONG, data);
                     break;
                 }
                 case pong: {
                     // Do nothing
                     break;
                 }
             }
         }
 
         private List<JSONObject> getClientList() {
             LinkedList<JSONObject> list = new LinkedList();
             for (DangerZoneWebSocket member : _members.values()) {
                 list.add(newClientInfo(member));
             }
             return list;
 
         }
         
         private JSONObject newClientInfo(DangerZoneWebSocket member) {
             JSONObject obj = new JSONObject();
             obj.put("id", member._socketId);
             obj.put("name", member._userName);
             return obj;
         }
 
         private JSONObject newActionInfo(String action, Object data) {
             JSONObject obj = new JSONObject();
             obj.put("action", action);
             obj.put("data", data);
             return obj;
         }
 
         private void sendMultiple(String from, Object... info) {
             LinkedList<JSONObject> list = new LinkedList();
             for (int i = 0; i < info.length; i += 2) {
                 list.add(newActionInfo((String) info[i], info[i + 1]));
             }
             sendMultiple(from, list);
         }
 
         private void sendMultiple(String from, List<JSONObject> list) {
             send(from, ACTION_MULTI, list);
         }
 
         private void send(String from, String action, Object data) {
             try {
                 JSONObject info = new JSONObject();
                 info.put("action", action);
                 info.put("data", data);
                 info.put("from", from);
                 String jsonText = JSONValue.toJSONString(info);
                 _outbound.sendMessage(info.toString());
             } catch (IOException e) {
                 logger.error("Could not send message, disconnecting socket", e);
                 _outbound.disconnect();
                 _members.remove(_socketId);
                 sendAll(ACTION_CLIENT_DISCONNECT, newClientInfo(this), false);
             }
         }
 
         private void sendAll(String action, Object data, boolean meToo) {
             for (DangerZoneWebSocket member : _members.values()) {
                 if (meToo || member != this) {
                     member.send(_socketId, action, data);
                 }
             }
         }
 
         private void sendTo(String to, String action, Object data) {
             DangerZoneWebSocket member = _members.get(to);
             if (member != null) {
                 member.send(_socketId, action, data);
             }
         }
     }
 }
