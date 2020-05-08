 /*
  * Copyright (C) 2008  Lars Pötter <Lars_Poetter@gmx.de>
  * All Rights Reserved.
  *
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
 package org.FriendsUnited.NetworkLayer;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 
 import org.FriendsUnited.FriendWorker;
 import org.FriendsUnited.Packets.AnnouncementPackage;
 import org.FriendsUnited.Packets.Packet;
 import org.FriendsUnited.Util.Tool;
 import org.FriendsUnited.Util.Option.IntegerOption;
 import org.FriendsUnited.Util.Option.OptionCollection;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class UdpPeerFinder extends FriendWorker
 {
     private final Logger log = Logger.getLogger(this.getClass().getName());
     private final NodeDirectory NodeDir;
     private final InetAddress bindipaddr;
     private final int Port;
     private IntegerOption recievedPackets;
     private IntegerOption droppedPackets;
     private final OptionCollection status;
     private final PacketQueue pq;
 
     /**
      * @param  bindipaddr IP Address to bind to
      * @param Port Port Number to listen on
      * @param NodeDir Node Directory that found nodes will be filled in
      */
     public UdpPeerFinder(final InetAddress bindipaddr,
                          final int Port,
                          final NodeDirectory NodeDir,
                          final OptionCollection status,
                          final PacketQueue pq)
     {
         super("UdpPeerFinder");
         recievedPackets = new IntegerOption("number of recieved Packets", 0);
         droppedPackets = new IntegerOption("number of dropped Packets", 0);
         status.add(recievedPackets);
         this.bindipaddr = bindipaddr;
         this.Port = Port;
         this.NodeDir = NodeDir;
         this.status = status;
         this.pq = pq;
     }
 
     @Override
     public final FriendWorker getDuplicate()
     {
         final UdpPeerFinder res = new UdpPeerFinder(bindipaddr, Port, NodeDir, status, pq);
         return res;
     }
 
     /**
      *
      */
     @Override
     public final void run()
     {
         while(true == isActive())
         {
             // Create UDP Socket
             try
             {
                 final DatagramSocket ds = new DatagramSocket(Port, bindipaddr);
                 final byte[] buf = new byte[Packet.PACKET_SIZE_MAX_BYTES];
                 final DatagramPacket ap = new DatagramPacket(buf, buf.length);
                 while(true == isActive())
                 {
                     // listen for Packets
                     ds.receive(ap);
                     if(null != ap)
                     {
                         final byte[] recievedPacket = new byte[ap.getLength()];
                         final byte[] completeBuffer = ap.getData();
                         for(int i = 0; i < ap.getLength(); i++)
                         {
                             recievedPacket[i] = completeBuffer[i];
                         }
                         final AnnouncementPackage AnnouncePacket = new AnnouncementPackage(recievedPacket);
                         // add received Info to NodeDir
                         if(true == AnnouncePacket.isValid())
                         {
                             recievedPackets.inc();
                             final Location loc = AnnouncePacket.getLocation();
                             log.debug("Peer Finder will add Route to Node Dir!");
                             NodeDir.addRoute(AnnouncePacket.getNodeId(), loc, pq);
                         }
                         else
                         {
                             droppedPackets.inc();
                             log.info("Recieved Datagram that is not a valid Announcement Package !");
                         }
                     }
                 }
             }
             catch(final SocketException e)
             {
                log.error("SocketException");
                 log.error(Tool.fromExceptionToString(e));
                 endWork();
             }
             catch(final IOException e)
             {
                 log.error("IOException");
                 log.error(Tool.fromExceptionToString(e));
                 endWork();
             }
         }
     }
 
 }
