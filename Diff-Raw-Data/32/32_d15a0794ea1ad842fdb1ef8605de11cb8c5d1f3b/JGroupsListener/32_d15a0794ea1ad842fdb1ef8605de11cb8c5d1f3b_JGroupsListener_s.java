 package org.jegrid.jgroups;
 
 import org.apache.log4j.Logger;
 import org.jegrid.NodeAddress;
 import org.jegrid.impl.GridImplementor;
 import org.jgroups.*;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * Translates JGroups messages (e.g. membership changes) into grid callbacks.
  * <br> User: jdavis
  * Date: Sep 30, 2006
  * Time: 9:35:11 PM
  */
 public class JGroupsListener implements ChannelListener, MessageListener, MembershipListener
 {
     private static Logger log = Logger.getLogger(JGroupsListener.class);
     private GridImplementor grid;
     private View currentView;
     private NodeAddress coordinator;
     boolean connected = false;
 
     public JGroupsListener(GridImplementor grid)
     {
         this.grid = grid;
     }
 
     public void channelConnected(Channel channel)
     {
         log.info("channelConnected() " + channel.getLocalAddress());
         synchronized (this)
         {
             connected = true;
         }
     }
 
     public void channelDisconnected(Channel channel)
     {
         log.info("channelDisconnected() " + channel.getLocalAddress());
         synchronized (this)
         {
             connected = false;
         }
     }
 
     public void channelClosed(Channel channel)
     {
         log.info("channelClosed() " + channel.getLocalAddress());
         synchronized (this)
         {
             connected = false;
         }
     }
 
     public void channelShunned()
     {
     }
 
     public void channelReconnected(Address addr)
     {
         log.info("channelReconnected() " + addr);
         synchronized (this)
         {
             connected = true;
         }
     }
 
     public void receive(Message msg)
     {
         log.info("receive " + msg);
     }
 
     public byte[] getState()
     {
         return new byte[0];
     }
 
     public void setState(byte[] state)
     {
     }
 
     public void viewAccepted(View newView)
     {
        log.info("viewAccepted " + newView);
         // Diff the views.
         ViewDiff diff;
         NodeAddress coord;
         synchronized (this)
         {
             diff = new ViewDiff(currentView, newView);
             if (diff.isCoordinatorChanged())
             {
                 coord = new JGroupsAddress(diff.getCoordinator());
                 log.info("*** Coordinator changed from " + coordinator + " to " + coord);
                 coordinator = coord;
             }
             else
             {
                 // Otherwise, the coordinator hasn't changed so get it out in to a local
                 // so we don't have to sync again.
                 coord = coordinator;
             }
             currentView = newView;
            if (!connected)
            {
                log.info("not connected");
                return;
            }
         } // synchronized
 
         // Notify about coordinator changes first.
         if (diff.isCoordinatorChanged())
             grid.onNewCoordinator(coord);
 
         // Create a set of JGroupsAddresses for the diff.
         Set joined = toNodeAddresses(diff.getJoined());
         Set left = toNodeAddresses(diff.getLeft());
         // Notify all the other components about the membership change.
         NodeAddress localAddress = grid.getLocalAddress();
         grid.onMembershipChange(joined, left, localAddress);
     }
 
     private Set toNodeAddresses(Set joined)
     {
         Set set = new HashSet(joined.size());
         for (Iterator iterator = joined.iterator(); iterator.hasNext();)
         {
             Address address = (Address) iterator.next();
             set.add(new JGroupsAddress(address));
         }
         return set;
     }
 
     public void suspect(Address suspected_mbr)
     {
     }
 
     public void block()
     {
     }
 
     NodeAddress getCoordinator()
     {
         synchronized (this)
         {
             return coordinator;
         }
     }
 }
