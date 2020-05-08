 import java.io.*;
 import java.net.*;
 
 class ConnectionServer extends Thread {
     private final Forwarder _forwarder;
     private final int _port;
     private final int _requestId;
     private final Socket _conn;
     private InputStream _in;
     private OutputStream _out;
 
     ConnectionServer(Forwarder forwarder, int port, Socket conn) throws IOException {
         _forwarder = forwarder;
         _port = port;
         _requestId = conn.getPort();
         _conn = conn;
         _in = _conn.getInputStream();
         _out = _conn.getOutputStream();
 
         _forwarder.addConnection(this);
     }
 
     ConnectionServer(Forwarder forwarder, int port, int requestId) throws IOException {
         _forwarder = forwarder;
         _port = port;
         _requestId = requestId;
        _conn = new Socket(InetAddress.getLocalHost(), _port);
         _out = _conn.getOutputStream();
         _in = _conn.getInputStream();
 
         _forwarder.addConnection(this);
     }
 
     int getRequestId() {
         return _requestId;
     }
 
     void fromForwarder(byte[] data, int len) {
         if (data != null) {
             try {
                 _out.write(data, 0, len);
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
             return;
         }
 
         try {
             _out.close();
         } catch (IOException ioe) {
         }
         _out = null;
         checkClose();
     }
 
     private synchronized void checkClose() {
         if (_in == null && _out == null) {
             try {
                 _conn.close();
             } catch (IOException e) {
             }
             _forwarder.removeConnection(this);
         }
     }
 
     public void run() {
         byte[] b = new byte[Forwarder.MAX_BLOCK_SIZE];
         while (true) {
             try {
                 int len = _in.read(b);
                 if (len < 0) {
                     break;
                 }
                 _forwarder.send(_port, _requestId, b, len);
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
         }
 
         try {
             _forwarder.send(_port, _requestId, null, 0);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
 
         try {
             _in.close();
         } catch (IOException ioe) {
         }
         _in = null;
         checkClose();
     }
 }
