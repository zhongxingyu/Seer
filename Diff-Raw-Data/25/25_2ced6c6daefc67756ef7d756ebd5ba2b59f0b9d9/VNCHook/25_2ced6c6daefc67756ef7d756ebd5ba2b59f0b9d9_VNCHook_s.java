 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.web;
 
 import com.google.gson.Gson;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.TimeUnit;
 import net.wgr.server.web.handling.WebCommandHandler;
 import net.wgr.utility.GlobalExecutorService;
 import net.wgr.wcp.Commander;
 import net.wgr.wcp.Scope;
 import net.wgr.wcp.command.Command;
 import net.wgr.xenmaster.api.Console;
 import net.wgr.xenmaster.api.VM;
 import net.wgr.xenmaster.connectivity.ConnectionMultiplexer;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Dec 15, 2011
  * @author double-u
  */
 public class VNCHook extends WebCommandHandler {
     
     protected static ConnectionMultiplexer cm = new ConnectionMultiplexer();
     protected ConcurrentHashMap<String, Connection> connections;
     protected ConnectionMultiplexer.ActivityListener al;
     protected static int connectionCounter;
     protected Arguments vncData;
     
     public VNCHook() {
         super("vnc");
         
         vncData = new Arguments();
         al = new ConnectionMultiplexer.ActivityListener() {
             
             @Override
             public void dataReceived(ByteBuffer data, int connection, ConnectionMultiplexer cm) {
                 Connection conn = null;
                 for (Entry<String, Connection> entry : connections.entrySet()) {
                     if (entry.getValue().connection == connection) {
                         conn = entry.getValue();
                     }
                 }
                 vncData.ref = conn.getReference();
                 vncData.data = Base64.encodeBase64String(data.array()).replace("\r\n", "");
                 Command cmd = new Command("vnc", "updateScreen", vncData);
                 ArrayList<UUID> ids = new ArrayList<>();
                 ids.add(conn.clientId);
                 Scope scope = new Scope(ids);
                 Commander.getInstance().commandeer(cmd, scope);
             }
             
             @Override
             public void connectionClosed(int connection) {
                 for (Entry<String, Connection> entry : connections.entrySet()) {
                     if (entry.getValue().connection == connection) {
                         Command cmd = new Command("vnc", "connectionClosed", new Arguments("", entry.getKey()));
                         Commander.getInstance().commandeer(cmd, new Scope(Scope.Target.ALL));
                     }
                 }
             }
             
             @Override
             public void connectionEstablished(int connection, Socket socket) {
                 Connection conn = null;
                 for (Entry<String, Connection> entry : connections.entrySet()) {
                     if (entry.getValue().waitForAddress.equals(socket.getRemoteSocketAddress())) {
                         conn = entry.getValue();
                         break;
                     }
                 }
                
                 Command cmd = new Command("vnc", "connectionEstablished", new Arguments("", conn.getReference()));
                 ArrayList<UUID> ids = new ArrayList<>();
                 ids.add(conn.clientId);
                 Scope scope = new Scope(ids);
                 Commander.getInstance().commandeer(cmd, scope);
             }
         };
         
         cm.addActivityListener(al);
         cm.start();
         connections = new ConcurrentHashMap<>();
         GlobalExecutorService.get().scheduleAtFixedRate(new Reaper(), 0, 5, TimeUnit.MINUTES);
     }
     
     @Override
     public Object execute(Command cmd) {
         try {
             Gson gson = new Gson();
             
             switch (cmd.getName()) {
                 case "openConnection":
                     if (!cmd.getData().isJsonObject() || !cmd.getData().getAsJsonObject().has("ref")) {
                         throw new IllegalArgumentException("No VM reference parameter given");
                     }
                     Connection conn = new Connection(cmd.getConnection().getId());
                     VM vm = new VM(cmd.getData().getAsJsonObject().get("ref").getAsString(), false);
                     for (Console c : vm.getConsoles()) {
                         if (c.getProtocol() == Console.Protocol.RFB) {
                             InetSocketAddress isa = new InetSocketAddress(c.getLocation(), c.getPort());
                             conn.waitForAddress = isa;
                             cm.addConnection(isa);
                         }
                     }
                     
                     conn.lastWriteTime =  System.currentTimeMillis();
                     connections.put(conn.getReference(), conn);
                     return conn.getReference();
                 case "write":
                     Arguments data = gson.fromJson(cmd.getData(), Arguments.class);
                     Connection c = connections.get(data.ref);
                     c.lastWriteTime = System.currentTimeMillis();
                     cm.write(c.connection, ByteBuffer.wrap(Base64.decodeBase64(data.data)));
                     break;
                 case "closeConnection":
                     Arguments close = gson.fromJson(cmd.getData(), Arguments.class);
                     cm.close(connections.get(close.ref).connection);
                     break;
             }
         } catch (IOException ex) {
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
         
         public Connection(UUID client) {
             connectionCounter++;
             this.reference = "ConnectionRef:" + connectionCounter;
             this.clientId = client;
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
         
         public Arguments(String data, String ref) {
             this.data = data;
             this.ref = ref;
         }
     }
     
     protected class Reaper implements Runnable {
         
         @Override
         public void run() {
             for (Entry<String, Connection> entry : connections.entrySet()) {
                 if (System.currentTimeMillis() - entry.getValue().lastWriteTime > 1000 * 60) {
                     try {
                         cm.close(entry.getValue().connection);
                     } catch (IOException ex) {
                         Logger.getLogger(getClass()).error("Failed to close connection", ex);
                     }
                 }
             }
         }
     }
 }
