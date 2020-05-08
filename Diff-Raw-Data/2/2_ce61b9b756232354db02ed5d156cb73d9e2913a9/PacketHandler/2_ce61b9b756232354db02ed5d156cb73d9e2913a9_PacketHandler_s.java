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
 package net.FriendsUnited.NodeLevel;
 
 import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.UUID;
 import java.util.Vector;
 
 import net.FriendsUnited.FriendPacket;
 import net.FriendsUnited.Server;
 import net.FriendsUnited.ServerDirectory;
 import net.FriendsUnited.Util.Tool;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** Handles incomming and outpoing Packets on Node to Node Level.
  *
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class PacketHandler extends Thread implements NodeDirectory, ServerDirectory
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
 
     private final UUID ownNodeId = UUID.randomUUID();
     private Hashtable<String, RemoteNode> allNodes = new Hashtable<String, RemoteNode>();
     private Vector<PacketTransmitter> allPacketTransmitters = new Vector<PacketTransmitter>();
     private Vector<Server> servers = new Vector<Server>();
 
     /**
      *
      */
     public PacketHandler()
     {
         Enumeration<NetworkInterface> en;
         try
         {
             en = NetworkInterface.getNetworkInterfaces();
             while(true == en.hasMoreElements())
             {
                 NetworkInterface curInterface = en.nextElement();
                 log.info("Creating Packet Transmitter for  Interface {} !", curInterface.getDisplayName());
 
                 Enumeration<InetAddress> ei = curInterface.getInetAddresses();
                 while(true == ei.hasMoreElements())
                 {
                     InetAddress curIp = ei.nextElement();
                     // start Socket: Local - LAN - Internet
                     if(false == curIp.isLoopbackAddress())
                     {
                         if(true == (curIp instanceof Inet6Address ))
                         {
                             // TODO IPv6 support
                         }
                         else
                         {
                             // IPv4
                             PacketTransmitter pt = new PacketTransmitter(ownNodeId, curIp, this, this);
                             pt.start();
                             allPacketTransmitters.add(pt);
                         }
                     }
                     // else -> Loopback Address cannot be announced
                     // and is not needed we can use the public IP of this Host for loopback communication
 
                 }
             }
         }
         catch(SocketException e)
         {
             log.error(Tool.fromExceptionToString(e));
         }
     }
 
     public void run()
     {
         while(true)
         {
             Enumeration<RemoteNode> en = allNodes.elements();
             while(true == en.hasMoreElements())
             {
                 RemoteNode rn = en.nextElement();
                 rn.tick();
             }
             try
             {
                 Thread.sleep(1);
             }
             catch(InterruptedException e)
             {
                 log.info("Got Interrupted !");
             }
             if(isInterrupted())
             {
                 break;
             }
         }
     }
 
     public void registerServer(Server theNewServer)
     {
         if(null == theNewServer)
         {
             return;
         }
        log.info("Registering the Server {} !", theNewServer);
         servers.add(theNewServer);
     }
 
     public void unregisterServer(Server theStoppingServer)
     {
         if(null != theStoppingServer)
         {
             servers.remove(theStoppingServer.getName());
         }
     }
 
     public void sendPacketIntoNetwork(Server theServer, FriendPacket pkt)
     {
         if((null == theServer) || (null == pkt))
         {
             return;
         }
         Server target = null;
         String targetServerName = pkt.getTargetServer();
         if(null == targetServerName)
         {
             return;
         }
         for(int i = 0; i < servers.size(); i++)
         {
             Server curSer = servers.get(i);
             if(true == targetServerName.equals(curSer.getName()))
             {
                 target = curSer;
                 break;
             }
         }
         if(null == target)
         {
             log.info("Can not send packet to server {} !", pkt.getTargetServer());
         }
         else
         {
             if(false == theServer.getName().equals(pkt.getSourceServer()))
             {
                 // This server wants to cheat !
                 // Sends Packet that does not have himself as source !
                 log.error("The Server {} tried to send a packet claiming to be from {} !",
                           theServer.getName(), pkt.getSourceServer());
             }
             else
             {
                 target.receivePacketFromNetwork(pkt);
             }
         }
     }
 
     public boolean isServerRegisterd(String ServerName)
     {
         if(null == ServerName)
         {
             return false;
         }
         for(int i = 0; i< servers.size(); i++)
         {
             Server curSer = servers.get(i);
             if(true == ServerName.equals(curSer.getName()))
             {
                 return true;
             }
         }
         return false;
     }
 
 
     public void close()
     {
         // The Packet Handler
         this.interrupt();
         // The Remote Nodes
         Enumeration<RemoteNode> er = allNodes.elements();
         while(true == er.hasMoreElements())
         {
             RemoteNode rn = er.nextElement();
             rn.setChannelStateToClosed();
         }
         // The PacketTransmitter
         Enumeration<PacketTransmitter> ept = allPacketTransmitters.elements();
         while(true == ept.hasMoreElements())
         {
             PacketTransmitter pt = ept.nextElement();
             pt.interrupt();
         }
     }
 
     public void addNode(String NodeId, InetAddress host, int port, final PacketTransmitter pt)
     {
         if((null == NodeId) | (null == host) || (0 == port))
         {
             return;
         }
         // check if this is really a new node, or if we already know him
         if(true == ownNodeId.toString().equals(NodeId))
         {
             // we know us
             return;
         }
         RemoteNode rn = allNodes.get(NodeId);
         if(null != rn)
         {
             int rp = rn.getPort();
             InetAddress rip = rn.getLocation();
             if((port != rp) || (false == host.equals(rip)))
             {
                 log.error("New Node already known but with different Location/Port !");
                 rn.setChannelStateToClosed();
                 allNodes.remove(NodeId);
             }
             else
             {
                 // OK we know this Node -> nothing to do anymore
                 return;
             }
         }
         log.info("Added new Node : {}", NodeId);
         // this is a really new Node
         rn = new RemoteNode(ownNodeId,
                             UUID.fromString(NodeId),
                             pt,
                             host,
                             port,
                             PacketTransmitter.OUTGOING_CONNECTION_COST,
                             this,
                             this);
         allNodes.put(NodeId, rn);
     }
 
     @Override
     public int getNumberOfLocalServers()
     {
         return servers.size();
     }
 
     @Override
     public String getLocalServerNameAt(int index)
     {
         Server theServer = servers.elementAt(index);
         if(null != theServer)
         {
             return theServer.getName();
         }
         else
         {
             return "";
         }
     }
 
 }
