 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.server.cell;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.Channel;
 import com.sun.sgs.app.ChannelManager;
 import com.sun.sgs.app.ClientSession;
 import com.sun.sgs.app.DataManager;
 import com.sun.sgs.app.Delivery;
 import com.sun.sgs.app.ManagedObject;
 import com.sun.sgs.app.ManagedReference;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.common.cell.messages.CellMessage;
 import org.jdesktop.wonderland.common.cell.messages.MovableMessage;
 import org.jdesktop.wonderland.common.messages.Message;
 import org.jdesktop.wonderland.server.WonderlandContext;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 
 /**
  *
  * @author paulby
  */
 public class ChannelComponentMO extends CellComponentMO {
 
     private WonderlandClientSender cellSender;
     private ManagedReference<Channel> cellChannelRef;
     private HashMap<Class, ManagedReference<ComponentMessageReceiver>> messageReceivers = new HashMap();
     
     public ChannelComponentMO(CellMO cell) {
         super(cell);
     }
     
     /**
      * {@inheritDoc}
      */
     public void openChannel() {
         CellMO cell = cellRef.get();
         
         ChannelManager cm = AppContext.getChannelManager();
         Channel cellChannel = cm.createChannel("Cell "+cell.getCellID().toString(), 
                                                null,
                                                Delivery.RELIABLE);
         
         DataManager dm = AppContext.getDataManager();
         cellChannelRef = dm.createReference(cellChannel);
         
         // cache the sender for sending to cell clients.  This saves a
         // Darkstar lookup for every cell we want to send to.
         cellSender = WonderlandContext.getCommsManager().getSender(CellChannelConnectionType.CLIENT_TYPE);
         
     }
     
     /**
      * {@inheritDoc}
      */
     public void closeChannel() {
         DataManager dm = AppContext.getDataManager();
         Channel channel = cellChannelRef.get();
         dm.removeObject(channel);
         
         cellSender=null;
         cellChannelRef = null;
     }  
     
     @Override
     protected void setLive(boolean live) {
         AppContext.getDataManager().markForUpdate(this);
         if (live)
             openChannel();
         else
             closeChannel();
     }
     
     /**
      * Send message to all clients on this channel
      * @param message
      */
     public void sendAll(Message message) {
         if (cellChannelRef==null) {
             return;
         }
 //        System.out.println("Sending data "+cellSender.getSessions().size());
         cellSender.send(cellChannelRef.get(), message);
     }
     
     /**
      * Add user to the cells channel, if there is no channel simply return
      * @param userID
      */
     public void addUserToCellChannel(ClientSession session) {
         if (cellChannelRef == null)
             return;
             
         cellChannelRef.getForUpdate().join(session);
     }
     
     /**
      * Remove user from the cells channel
      * @param userID
      */
     public void removeUserFromCellChannel(ClientSession session) {
         if (cellChannelRef == null)
             return;
             
         cellChannelRef.getForUpdate().leave(session);        
     }
      
     /**
      * Register a receiver for a specific message class. Only a single receiver
      * is allowed for each message class, calling this method to add a duplicate
      * receiver will cause an IllegalStateException to be thrown.
      * 
      * @param msgClass
      * @param receiver
      */
     public void addMessageReceiver(Class<? extends CellMessage> msgClass, ComponentMessageReceiver receiver) {
         Object old = messageReceivers.put(msgClass, AppContext.getDataManager().createReference(receiver));
         if (old!=null)
             throw new IllegalStateException("Duplicate Message class added "+msgClass);
     }
     
     /**
      * Dispatch messages to any receivers registered for the particular message class
      * @param sender
      * @param session
      * @param message
      */
     public void messageReceived(WonderlandClientSender sender, 
                                 ClientSession session,
                                 CellMessage message ) {
         
         ManagedReference<ComponentMessageReceiver> recvRef = messageReceivers.get(message.getClass());
         if (recvRef==null) {
            Logger.getAnonymousLogger().warning("Not listener for message "+message.getClass());
             return;
         }
         
         recvRef.get().messageReceived(sender, session, message);
     }
     
     static public interface ComponentMessageReceiver extends ManagedObject, Serializable {
         public void messageReceived(WonderlandClientSender sender, 
                                     ClientSession session,
                                     CellMessage message );        
     }
 }
