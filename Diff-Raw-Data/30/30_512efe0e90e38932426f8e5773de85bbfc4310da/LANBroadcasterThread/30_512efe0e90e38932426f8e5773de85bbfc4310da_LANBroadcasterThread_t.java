 package com.oneofthesevenbillion.ziah.ZCord.network;
 
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.util.logging.Level;
 
 import net.md_5.bungee.BungeeCord;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.config.ListenerInfo;
 
 import com.oneofthesevenbillion.ziah.ZCord.ZCord;
 
 public class LANBroadcasterThread extends Thread {
     private DatagramSocket socket;
 
     public LANBroadcasterThread() {
         super("ZCord LANBroadcaster");
         try {
             socket = new DatagramSocket();
             socket.setSoTimeout(3000);
         } catch (Exception e) {
             e.printStackTrace();
             ProxyServer.getInstance().getLogger().severe("Host does not support datagram sockets!");
         }
     }
 
     @Override
     public void run() {
         try {
             byte[] ad = getAd();
             DatagramPacket packet = new DatagramPacket(ad, ad.length, InetAddress.getByName("224.0.2.60"), 4445);
             while (!isInterrupted()) {
                 socket.send(packet);
                 try {
                     sleep(1500L);
                 } catch (InterruptedException e) {
                     break;
                 }
             }
         } catch (Exception e) {
         	ProxyServer.getInstance().getLogger().log(Level.WARNING, "Exception when running!", e);
         }
         socket.close();
     }
 
     private byte[] getAd() {
         String motd = ((ListenerInfo) BungeeCord.getInstance().config.getListeners().toArray()[0]).getMotd();
        //< 1.6 = String ip = getLanIP();
         int port = ZCord.instance.bungeehost.getPort();
        String ad = String.valueOf(port);//< 1.6 = ip + ":" + port
         byte[] adBytes = ("[MOTD]" + motd + "[/MOTD][AD]" + ad + "[/AD]").getBytes();
         return adBytes;
     }
 
    /* < 1.6 =
     private String getLanIP() {
         if (!ZCord.instance.bungeehost.getAddress().getHostAddress().equals("")) return ZCord.instance.bungeehost.getAddress().getHostAddress();
         try {
             Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
             while (interfaces.hasMoreElements()) {
                 NetworkInterface iface = interfaces.nextElement();
                 Enumeration<InetAddress> addresses = iface.getInetAddresses();
                 while (addresses.hasMoreElements()) {
                     InetAddress address = addresses.nextElement();
                     if (address instanceof Inet4Address && !address.isLoopbackAddress()) return address.getHostAddress();
                 }
             }
             throw new Exception("No usable IPv4 non-loopback address found");
         } catch (Exception e) {
             ProxyServer.getInstance().getLogger().log(Level.SEVERE, "Could not automatically detect LAN IP, please set server-ip in server.properties.", e);
             try {
                 return InetAddress.getLocalHost().getHostAddress();
             } catch (UnknownHostException ex) {
                 return "End of the world";
             }
         }
    }*/
 }
