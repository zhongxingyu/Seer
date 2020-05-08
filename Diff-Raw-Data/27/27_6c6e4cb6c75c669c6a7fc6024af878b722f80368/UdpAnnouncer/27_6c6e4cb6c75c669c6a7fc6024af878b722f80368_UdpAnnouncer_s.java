 /*
  * Copyright (C) 2009  Lars Pötter <Lars_Poetter@gmx.de>
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
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 package org.FriendsUnited.NetworkLayer;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 
 import org.FriendsUnited.FriendWorker;
 import org.FriendsUnited.Packets.AnnouncementPackage;
 import org.FriendsUnited.Util.Tool;
 import org.FriendsUnited.Util.Option.IntegerOption;
 import org.FriendsUnited.Util.Option.OptionCollection;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class UdpAnnouncer extends FriendWorker
 {
     private Logger log = Logger.getLogger(this.getClass().getName());
     private NodeId OwnNodeId;
     private InetAddress bindipaddr;
     private InetAddress sendipaddr;
     private int Port;
     private int cycle;
     private Location OwnLocation;
     private IntegerOption Announcementcnt;
     private final OptionCollection status;
 
     /**
      *
      * @param bindipaddr
      * @param sendipaddr
      * @param Port
      * @param OwnNodeId
      * @param cycle
      * @param OwnLocation
      */
     public UdpAnnouncer(final InetAddress bindipaddr,
                         final InetAddress sendipaddr,
                         final int Port,
                         final NodeId OwnNodeId,
                         final int cycle,
                         final Location OwnLocation,
                         final OptionCollection status)
     {
         super("UdpAnnouncer");
         Announcementcnt = new IntegerOption("Number of Announcenements send", 0);
         status.add(Announcementcnt);
         this.OwnNodeId = OwnNodeId;
         this.bindipaddr = bindipaddr;
         this.sendipaddr = sendipaddr;
         this.Port = Port;
         this.cycle = cycle;
         this.OwnLocation = OwnLocation;
         this.status = status;
     }
 
     @Override
     public final FriendWorker getDuplicate()
     {
         final UdpAnnouncer res = new UdpAnnouncer(bindipaddr, sendipaddr, Port, OwnNodeId, cycle, OwnLocation, status);
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
             // create the Announcement Packet
             final AnnouncementPackage ap = new AnnouncementPackage(OwnNodeId, OwnLocation);
             if(false == ap.isValid())
             {
                 log.fatal("Could not create valid Announcement Packet !");
                 endWork();
                 return;
             }
             final byte[] ba = ap.getData();
             final DatagramPacket AnnouncePacket = new DatagramPacket(ba, ba.length);
             AnnouncePacket.setPort(Port);
             AnnouncePacket.setAddress(sendipaddr);
             // Create UDP Socket
             try
             {
                 final DatagramSocket ds = new DatagramSocket(Port + 1, bindipaddr);
                 ds.setBroadcast(true);
                 while(true == isActive())
                 {
                     // Send Packet
                     ds.send(AnnouncePacket);
                     Announcementcnt.inc();
                     sleep(cycle);
                 }
             }
             catch(final SocketException e)
             {
                log.error("SocketException at " + bindipaddr + ":" + Port);
                 log.error(Tool.fromExceptionToString(e));
                 endWork();
             }
             catch(final IOException e)
             {
                 log.error("IOException");
                 log.error(Tool.fromExceptionToString(e));
                 endWork();
             }
             catch(final InterruptedException e)
             {
             }
         }
     }
 
 }
