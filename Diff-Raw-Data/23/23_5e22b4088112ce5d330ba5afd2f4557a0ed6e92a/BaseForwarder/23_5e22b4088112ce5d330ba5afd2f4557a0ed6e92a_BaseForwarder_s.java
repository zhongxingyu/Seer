 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public abstract class BaseForwarder implements Runnable, Forwarder {
     private final Map<Integer, ConnectionServer> _connections;
 
     public BaseForwarder() {
         _connections = new HashMap<Integer, ConnectionServer>();
     }
 
     protected final void sendTo(DataOutputStream out, int port, int requestId, byte[] data, int len) throws IOException {
         synchronized (out) {
             out.writeInt(port);
             out.writeInt(requestId);
             if (data == null) {
                 out.write(-1);
             } else {
                 out.writeInt(len);
                 out.write(data, 0, len);
             }
         }
     }
 
     public void addConnection(ConnectionServer conn) {
         synchronized (_connections) {
             _connections.put(conn.getRequestId(), conn);
         }
     }
 
     public void removeConnection(ConnectionServer conn) {
         synchronized (_connections) {
             _connections.remove(conn.getRequestId());
         }
     }
 
     protected final void kill() {
         List<ConnectionServer> conns = new ArrayList<ConnectionServer>();
         synchronized (_connections) {
             conns.addAll(_connections.values());
         }
         for (ConnectionServer conn : conns) {
             conn.fromForwarder(null, 0);
         }
     }
 
     protected final void loop(DataInputStream in, boolean createOnDemand) throws IOException {
         byte[] buf = new byte[MAX_BLOCK_SIZE];
 
         while (true) {
             int port;
             try {
                 port = in.readInt();
             } catch (IOException ioe) {
                 break;
             }
             int requestId = in.readInt();
             ConnectionServer conn;
             synchronized (_connections) {
                 conn = _connections.get(requestId);
                 if (conn == null) {
                     if (! createOnDemand) {
                         throw new IOException("Unexpected request id: " + requestId);
                     }
                     conn = new ConnectionServer(this, port, requestId);
                     conn.start();
                 }
             }
             int len = in.readInt();
             if (len < 0) {
                 conn.fromForwarder(null, 0);
             } else {
                 in.readFully(buf, 0, len);
                 conn.fromForwarder(buf, len);
             }
         }
     }
 }
