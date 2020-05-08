 package game.communications.multicast;
 
 import game.communications.tcpcomms.TCPServer;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.util.Date;
 import java.text.DateFormat;
 
 public class Connect4MultiCastServerThread extends Thread
 {
     private long FIVE_SECONDS = 5000;
     private int port;
     private boolean run = true;
     private DatagramSocket _socket;
     private String formattedTime;
 
     public Connect4MultiCastServerThread(int port) throws IOException
     {
         super("MulticastServerThread");
         this.port = port;
         _socket = new DatagramSocket(0);
     }
 
     public void run()
     {
         while(run)
         {
             if(TCPServer.getInstance().getPort() == 0)
             {
                 continue;
             }
             byte[] packetData = new byte[256];
 
             StringBuffer assemblePacketData = new StringBuffer();
             assemblePacketData.append(Integer.toString(TCPServer.getInstance().getPort()));
//         assemblePacketData.append("/").append(formattedTime);
 
             Date now = new Date();
             DateFormat time = DateFormat.getTimeInstance();
             String formattedTime = time.format(now);
 //         System.out.println("sent time: " + formattedTime);
 
             String s = new String(assemblePacketData);
             packetData = s.getBytes();
 
             InetAddress group = null;
 
             try
             {
                 group = InetAddress.getByName("230.0.0.1");
                 DatagramPacket packet = new DatagramPacket(packetData, packetData.length, group, 4446);
                 _socket.send(packet);
             }
             catch(Exception e)
             {
                 e.printStackTrace();
             }
 
             try
             {
                 sleep((long) (Math.random() * FIVE_SECONDS));
             }
             catch(Exception e)
             {
                 e.printStackTrace();
                 run = false;
             }
         }
         _socket.close();
     }
 }
