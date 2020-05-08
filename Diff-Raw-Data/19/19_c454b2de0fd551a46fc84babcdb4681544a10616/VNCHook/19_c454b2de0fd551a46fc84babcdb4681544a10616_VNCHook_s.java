 /*
  * VNCHook.java
  * Copyright (C) 2011,2012 Wannes De Smet
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.xenmaster.web;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.TimeUnit;

 import net.wgr.server.web.handling.WebCommandHandler;
 import net.wgr.utility.GlobalExecutorService;
 import net.wgr.wcp.Commander;
 import net.wgr.wcp.Scope;
 import net.wgr.wcp.command.Command;
 import net.wgr.wcp.command.CommandException;

 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
import org.xenmaster.connectivity.ConnectionMultiplexer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
 import org.xenmaster.api.Console;
 import org.xenmaster.api.VM;
 import org.xenmaster.controller.Controller;
 
 /**
  * 
  * @created Dec 15, 2011
  * @author double-u
  */
 public class VNCHook extends WebCommandHandler {
 
     protected static ConnectionMultiplexer cm;
     protected static ConcurrentHashMap<String, Connection> connections;
     protected ConnectionMultiplexer.ActivityListener al;
     protected static int connectionCounter;
     protected static Gson gson;
 
     public VNCHook() {
         super("vnc");
 
         if (cm == null) {
             gson = new Gson();
             setupInfrastructure();
         }
     }
 
     protected static String buildHttpConnect(URI uri) {
         StringBuilder sb = new StringBuilder();
         sb.append("CONNECT ");
         sb.append(uri.getPath()).append('?').append(uri.getQuery());
         sb.append(" HTTP/1.1").append("\r\n");
         sb.append("Cookie: session_id=").append(Controller.getSession().getReference());
         sb.append("\r\n\r\n");
         return sb.toString();
     }
 
     @Override
     public Object execute(Command cmd) {
         try {
             switch (cmd.getName()) {
                 case "openConnection":
                     if (!cmd.getData().isJsonObject() || !cmd.getData().getAsJsonObject().has("ref")) {
                         throw new IllegalArgumentException("No VM reference parameter given");
                     }
                     Connection conn = new Connection(cmd.getConnection().getId());
                     VM vm = new VM(cmd.getData().getAsJsonObject().get("ref").getAsString(), false);
                     for (Console c : vm.getConsoles()) {
                         if (c.getProtocol() == Console.Protocol.RFB) {
                             try {
                                 conn.console = c;
                                 URI uri = new URI(c.getLocation());
                                 conn.uri = uri;
                                 InetSocketAddress isa = new InetSocketAddress(uri.getHost(), 80);
                                 conn.waitForAddress = isa;
                                 conn.lastWriteTime = System.currentTimeMillis();
                                 connections.put(conn.getReference(), conn);
                                 cm.addConnection(isa);
                                 break;
                             } catch (URISyntaxException ex) {
                                 Logger.getLogger(getClass()).error("Failed to parse URI", ex);
                             } catch (IOException | InterruptedException ex) {
                                 Logger.getLogger(getClass()).error("Failed to create connection", ex);
                             }
                         }
                     }
 
                     return conn.getReference();
                 case "write":
                     Arguments data = Arguments.fromJson(cmd.getData());
                     if (!connections.containsKey(data.ref)) {
                         return new CommandException("Tried to write to unexisting connection", data.ref);
                     }
                     Connection c = connections.get(data.ref);
                     c.lastWriteTime = System.currentTimeMillis();
                     byte[] bytes = Base64.decodeBase64(data.data);
                     cm.write(c.connection, ByteBuffer.wrap(bytes));
                     break;
                 case "closeConnection":
                     Arguments close = Arguments.fromJson(cmd.getData());
                     Connection ci = connections.get(close.ref);
 
                     if (ci != null) {
                         // Indicate that we've initiated the connection close
                         ci.lastWriteTime = -1;
                         cm.close(ci.connection);
                     }
                     break;
                 case "connectionHeartbeat":
                     Arguments heartbeat = Arguments.fromJson( cmd.getData() );
                     if( !connections.containsKey(heartbeat.ref)) {
                         return new CommandException("Tried to write to unexisting connection", heartbeat.ref);
                     }
                     Connection ch = connections.get( heartbeat.ref );
                     ch.lastHeartbeat = System.currentTimeMillis();
                     break;
             }
         } catch (IOException | IllegalArgumentException ex) {
             Logger.getLogger(getClass()).error("Command failed : " + cmd.getName(), ex);
         }
 
         return null;
     }
 
     protected static class Connection {
 
         public UUID clientId;
         public int connection;
         protected String reference;
         public InetSocketAddress waitForAddress;
         public long lastWriteTime;
         public long lastHeartbeat;
         public URI uri;
         public boolean dismissedHttpOK;
         public Console console;
 
         public Connection(UUID client) {
             connectionCounter++;
             this.reference = "ConnectionRef:" + connectionCounter;
             this.clientId = client;
             this.lastHeartbeat = System.currentTimeMillis();
         }
 
         public String getReference() {
             return reference;
         }
     }
 
     protected static class Arguments {
 
         public String data;
         public String ref;
 
         public Arguments() {
         }
 
         public static Arguments fromJson(JsonElement data) {
             Arguments na = gson.fromJson(data, Arguments.class);
             if (na == null || na.ref == null || !na.ref.startsWith("ConnectionRef:")) {
                 throw new IllegalArgumentException("Illegal reference, is not a ConnectionRef");
             }
             return na;
         }
 
         public Arguments(String data, String ref) {
             this.data = data;
             this.ref = ref;
         }
     }
 
     protected static class AL implements ConnectionMultiplexer.ActivityListener {
 
         @Override
         public void dataReceived(ByteBuffer buffer, int connection, ConnectionMultiplexer cm) {
             Connection conn = null;
             for (Entry<String, Connection> entry : connections.entrySet()) {
                 if (entry.getValue().connection == connection) {
                     conn = entry.getValue();
                     break;
                 }
             }
 
             if (conn == null) {
                 Logger.getLogger(getClass()).warn("Received data on inactive connection " + connection + ". Closing ...");
                 try {
                     cm.close(connection);
                 } catch (IOException ex) {
                     Logger.getLogger(getClass()).error("Failed to close connection " + connection, ex);
                 }
                 return;
             }
 
             byte[] data = buffer.array();
 
             if (!conn.dismissedHttpOK) {
                 String content = new String(data);
                 // RFB xxx.xxx denotes start of VNC handshake
                 if (content.contains("RFB")) {
                     conn.dismissedHttpOK = true;
                     data = content.substring(content.indexOf("RFB")).getBytes();
                 } else {
                     return;
                 }
             }
 
             if (data.length < 1) {
                 return;
             }
 
             Arguments vncData = new Arguments();
             vncData.ref = conn.getReference();
             vncData.data = Base64.encodeBase64String(data).replace("\r\n", "");
             Command cmd = new Command("vnc", "updateScreen", vncData);
             ArrayList<UUID> ids = new ArrayList<>();
             ids.add(conn.clientId);
             Scope scope = new Scope(ids);
             Commander.getInstance().commandeer(cmd, scope);
         }
 
         @Override
         public void connectionClosed(int connection) {
             for (Iterator<Entry<String, Connection>> it = connections.entrySet().iterator(); it.hasNext();) {
                 Entry<String, Connection> entry = it.next();
                 if (entry.getValue().connection == connection) {
                     Connection conn = entry.getValue();
 
                     // Check if this disconnect was initiated by a user
                     if (conn.lastWriteTime == -1) {
                         Command cmd = new Command("vnc", "connectionClosed", new Arguments("", entry.getKey()));
                         Commander.getInstance().commandeer(cmd, new Scope(Scope.Target.ALL));
                         it.remove();
                     } else {
                         try {
                             // Try to reconnect
                             URI uri = new URI(conn.console.getLocation());
                             conn.uri = uri;
                             InetSocketAddress isa = new InetSocketAddress(uri.getHost(), 80);
                             conn.waitForAddress = isa;
                             conn.lastWriteTime = System.currentTimeMillis();
                             cm.addConnection(isa);
                         } catch (URISyntaxException | IOException | InterruptedException ex) {
                             Logger.getLogger(getClass()).error("Failed to reinitiate connection", ex);
                         }
                     }
                     break;
                 }
             }
         }
 
         @Override
         public void connectionEstablished(int connection, Socket socket) {
             Connection conn = null;
             InetSocketAddress isa = null;
             for (Entry<String, Connection> entry : connections.entrySet()) {
                 isa = entry.getValue().waitForAddress;
                 if (isa != null && isa.equals(socket.getRemoteSocketAddress())) {
                     conn = entry.getValue();
                     conn.waitForAddress = null;
                     break;
                 }
             }
 
             if (conn == null) {
                 Logger.getLogger(getClass()).warn("Unknown connection established to " + ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString());
                 return;
             }
 
             conn.connection = connection;
             cm.write(conn.connection, ByteBuffer.wrap(buildHttpConnect(conn.uri).getBytes()));
 
             Command cmd = new Command("vnc", "connectionEstablished", new Arguments("", conn.getReference()));
             ArrayList<UUID> ids = new ArrayList<>();
             ids.add(conn.clientId);
             Scope scope = new Scope(ids);
             Commander.getInstance().commandeer(cmd, scope);
         }
     }
 
     protected static void setupInfrastructure() {
         cm = new ConnectionMultiplexer();
         cm.addActivityListener(new AL());
         cm.start();
         connections = new ConcurrentHashMap<>();
         GlobalExecutorService.get().scheduleAtFixedRate(new Reaper(), 0, 10, TimeUnit.SECONDS);
     }
 
     protected static class Reaper implements Runnable {
 
         @Override
         public void run() {
             // Send a heartbeat
             Command cmd = new Command("vnc", "connectionHeartbeat", new Arguments());
             Commander.getInstance().commandeer(cmd, new Scope(Scope.Target.ALL));
             
             for (Entry<String, Connection> entry : connections.entrySet()) {
<<<<<<< HEAD
                if (System.currentTimeMillis() - entry.getValue().lastWriteTime > 1000 * 50) {
=======
                 // Skipped 2 hearbeats, is probably dead.
                 if (System.currentTimeMillis() - entry.getValue().lastHeartbeat > 1000 * 20) {
>>>>>>> 69e8dfbdaef30933325f15489e8059d65327a586
                     try {
                         Logger.getLogger(getClass()).info("Reaper closing inactive connection " + entry.getValue().connection);
                         cm.close(entry.getValue().connection);
                     } catch (IOException ex) {
                         Logger.getLogger(getClass()).error("Failed to close connection", ex);
                     }
                 }
             }
         }
     }
 }
