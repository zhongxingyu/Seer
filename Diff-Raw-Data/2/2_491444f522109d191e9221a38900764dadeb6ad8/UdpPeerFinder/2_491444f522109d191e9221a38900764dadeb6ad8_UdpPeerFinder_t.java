 /*
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 
 /**
  *
  */
 package net.FriendsUnited.NodeLevel;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.FriendsUnited.Util.ByteConverter;
 import net.FriendsUnited.Util.Tool;
 
 
 /**
  *
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class UdpPeerFinder extends Thread
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
 
     public final static int PACKET_SIZE_MAX_BYTES = 32 * 1024;
 
     private final int Port;
     private final NodeDirectory nodeDir;
 
     /**
      * @param status
      * @param Port Port Number to listen on
      */
     public UdpPeerFinder(int port,  NodeDirectory nodeDir)
     {
         super("UdpPeerFinder");
         this.Port = port;
         this.nodeDir = nodeDir;
     }
 
     /**
      *
      */
     @Override
     public final void run()
     {
         boolean isActive = true;
         while(true == isActive)
         {
             // Create UDP Socket
             try
             {
                 MulticastSocket mc = new MulticastSocket(Port);
                 InetAddress group = InetAddress.getByName("230.230.230.230");
                 mc.joinGroup(group);
                 final byte[] buf = new byte[PACKET_SIZE_MAX_BYTES];
                 final DatagramPacket ap = new DatagramPacket(buf, buf.length);
                 log.info("Starting to listen for Peers");
                 while(true == isActive)
                 {
                     // listen for Packets
                     mc.receive(ap);
                     if(null != ap)
                     {
                         log.debug("Received something from a Peer,...");
                         final byte[] recievedPacket = new byte[ap.getLength()];
                         final byte[] completeBuffer = ap.getData();
                         for(int i = 0; i < ap.getLength(); i++)
                         {
                             recievedPacket[i] = completeBuffer[i];
                         }
                         checkReceivedData(recievedPacket, ap.getAddress());
                     }
                     if(true == interrupted())
                     {
                         isActive = false;
                     }
                 }
                 mc.leaveGroup(group);
             }
             catch(final SocketException e)
             {
                 log.error("SocketException: Port " + Port);
                 log.error(Tool.fromExceptionToString(e));
                 isActive = false;
             }
             catch(final IOException e)
             {
                 log.error("IOException");
                 log.error(Tool.fromExceptionToString(e));
                 isActive = false;
             }
         }
     }
 
     private void checkReceivedData(final byte[] recievedPacket, final InetAddress remoteAddress)
     {
         log.debug("Received Announcement from {} !", remoteAddress);
         ByteConverter bc = new ByteConverter(recievedPacket);
         byte type = bc.getByte();
         if(UdpAnnouncer.ANNOUNCER_MAGIC_BYTE != type)
         {
             log.info("Received Announcement Packet with Invalid Type of {} !", type);
             return;
         }
         String NodeId = bc.getString();
         String Location = bc.getString();
         int remotePort = bc.getInt();
         if(0 == remotePort)
         {
             log.error("Announcement Packet Data Invalid ! (NodeId: {}, Location: {}, Port: 0)", NodeId, Location);
             return;
         }
         try
         {
             InetAddress reportedAddress = InetAddress.getByName(Location);
            if(false == remoteAddress.equals(reportedAddress))
             {
                 log.error("attempted fraud detected ! {} claimed to be {} !", remoteAddress, reportedAddress);
                 return;
             }
             nodeDir.addNode(NodeId, reportedAddress, remotePort);
         }
         catch(UnknownHostException e)
         {
             log.error("The reportet Address ({}) is invalid !", Location);
             return;
         }
     }
 
 }
