 package executor.lightLogger.logger.syslog;
 
 import executor.lightLogger.Logger;
 
 import java.io.IOException;
 import java.net.*;
 import java.util.Date;
 
 /**
  * Sends a Syslog Message to a collector or relay.
  */
 public class SyslogSenderUDP implements ISyslogSender {
 
     private static SyslogSenderUDP instance = null;
     private InetAddress address;
     private int port;
 
     private SyslogSenderUDP(String address, int port) throws UnknownHostException {
         this.address = InetAddress.getByName(address);
         this.port = port;
     }
 
     public static synchronized SyslogSenderUDP getInstance(String address, int port) {
         if (instance == null)
             try {
                 instance = new SyslogSenderUDP(address, port);
             } catch (Exception e) {
                 Logger.error(SyslogSenderUDP.class, "Could not create instance: " + e.getMessage());
                 instance = null;
             }
         return instance;
     }
 
     public static void main(String[] args) {
         System.out.println("fff");
         try {
             SyslogSenderUDP syslogSender = SyslogSenderUDP.getInstance("192.168.0.117", 514);
             SyslogMessageBSD sm = new SyslogMessageBSD();
             sm.setPrival(EFacility.LOCAL1, ESeverity.ERROR);
             sm.setHostname("executor");
             sm.setTag("syslogtest");
             sm.setMsg("st messagwereq42");
             sm.setDate(new Date(System.currentTimeMillis()));
             syslogSender.sendMessage(sm);
         } catch (Exception e) {
             Logger.error(SyslogSenderUDP.class, e.getMessage());
         }
     }
 
     @Override
     public boolean connect() {
         if (address != null)
             return true;
         else
             return false;
     }
 
     @Override
     public boolean disconnect() {
         return true;
     }
 
     @Override
     public boolean sendMessage(ISyslogMessage syslogMsg) {
        if (syslogMsg == null) {
            Logger.error(SyslogSenderUDP.class, "Syslog message is null!");
            return false;
        }

        final byte[] data = syslogMsg.getBytes();
        if (data == null) {
            Logger.error(SyslogSenderUDP.class, "No data to send!");
            return false;
        }
         try {
             DatagramPacket packet = new DatagramPacket(data, data.length, this.address, this.port);
             DatagramSocket socket = new DatagramSocket();
             socket.send(packet);
         } catch (SocketException e) {
             Logger.error(SyslogSenderUDP.class, "Could not create socket: " + e.getMessage());
             return false;
         } catch (IOException e) {
             Logger.error(SyslogSenderUDP.class, "Could not sent message: " + e.getMessage());
             return false;
         }
         return true;
     }
 }
