 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public class HostForwarder extends BaseForwarder {
     private final int _port;
     private final int _numPorts;
     private DataOutputStream _toDevice;
 
     public HostForwarder(int port, int numPorts) {
         _port = port;
         _numPorts = numPorts;
     }
 
     public void send(int port, int requestId, byte[] data, int len) throws IOException {
         sendTo(_toDevice, port, requestId, data, len);
     }
 
     public void run() {
         Socket device = null;
         DataInputStream fromDevice = null;
         DataOutputStream toDevice = null;
         try {
             // the device-side forwarder instance has to initiate the connection
            device = new Socket(InetAddress.getLocalHost(), _port);
             fromDevice = new DataInputStream(device.getInputStream());
             toDevice = new DataOutputStream(device.getOutputStream());
 
             // magic handshake
             toDevice.writeInt(MAGIC);
             if (fromDevice.readInt() != MAGIC) {
                 System.err.println("Handshake to device machine failed!");
                 return;
             }
 
             _toDevice = toDevice;
 
             // read range of ports to forward
             toDevice.writeInt(_port + 1);
             toDevice.writeInt(_port + 1 + _numPorts);
 
             loop(fromDevice, true);
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 fromDevice.close();
             } catch (Exception e) {
             }
             try {
                 toDevice.close();
             } catch (Exception e) {
             }
             try {
                 device.close();
             } catch (Exception e) {
             }
         }
     }
 }
 
