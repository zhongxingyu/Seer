 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.presencemanager.server;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.ManagedReference;
 
 import com.sun.mpk20.voicelib.app.ManagedCallBeginEndListener;
 import com.sun.mpk20.voicelib.app.ManagedPlayerInRangeListener;
 import com.sun.mpk20.voicelib.app.Player;
 
 import com.sun.mpk20.voicelib.app.VoiceManager;
 
 import com.sun.voip.client.connector.CallStatus;
 
 import org.jdesktop.wonderland.common.messages.Message;
 
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceManagerConnectionType;
 
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.PlayerInRangeMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.PlayerInRangeListenerMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.PresenceInfoAddedMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.PresenceInfoChangeMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.PresenceInfoRemovedMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientConnectMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientConnectResponseMessage;
 
 import org.jdesktop.wonderland.common.comms.ConnectionType;
 
 import org.jdesktop.wonderland.server.WonderlandContext;
 
 import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
 import org.jdesktop.wonderland.server.comms.CommsManager;
 import org.jdesktop.wonderland.server.comms.CommsManagerFactory;
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 
 import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
 
 import java.math.BigInteger;
 
 import java.util.logging.Logger;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Properties;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 import com.sun.sgs.app.ManagedObject;
 
 /**
  * Presence Manager
  * 
  * @author jprovino
  */
 public class PresenceManagerConnectionHandler implements 
 	ClientConnectionHandler, Serializable, ManagedObject, ManagedCallBeginEndListener {
 
     private static final Logger logger =
             Logger.getLogger(PresenceManagerConnectionHandler.class.getName());
     
     private ConcurrentHashMap<BigInteger, PresenceInfo> presenceInfoMap = new ConcurrentHashMap();
 
     private CopyOnWriteArrayList<PresenceInfo> presenceInfoList = new CopyOnWriteArrayList();
 
     private ConcurrentHashMap<BigInteger, ManagedReference<PlayerInRangeNotifier>> notifiers =
 	new ConcurrentHashMap();
 
     public PresenceManagerConnectionHandler() {
         super();
 
 	AppContext.getManager(VoiceManager.class).addCallBeginEndListener(this);
     }
 
     public ConnectionType getConnectionType() {
         return PresenceManagerConnectionType.CONNECTION_TYPE;
     }
 
     public void registered(WonderlandClientSender sender) {
 	logger.fine("Presence Server manager connection registered");
     }
 
     public void clientConnected(WonderlandClientSender sender, 
 	    WonderlandClientID clientID, Properties properties) {
 
 	logger.fine("client connected...");
     }
 
     public void messageReceived(WonderlandClientSender sender, 
 	    WonderlandClientID clientID, Message message) {
 
         // mark ourself for update
         AppContext.getDataManager().markForUpdate(this);
 
	//dump(message.toString());

 	if (message instanceof ClientConnectMessage) {
 	    /*
              * Send back all of the PresenceInfo data to the new client
              */
 	    //for (PresenceInfo info : presenceInfoList) {
 	    //    System.out.println("PI: " + info);
 	    //}
 
             sender.send(clientID, new ClientConnectResponseMessage(
 		presenceInfoList.toArray(new PresenceInfo[0])));
 	    return;
 	}
 
 	if (message instanceof PlayerInRangeListenerMessage) {
 	    PlayerInRangeListenerMessage msg = (PlayerInRangeListenerMessage) message;
 
 	    PresenceInfo info = presenceInfoMap.get(clientID.getID());
 
 	    if (info == null) {
 		System.out.println("PlayerInRangeListenerMessage:  No presence info for clientID "
 		    + clientID.getID());
 		return;
 	    }
 
 	    if (msg.getAdd() == true) {
 	        PlayerInRangeNotifier notifier = new PlayerInRangeNotifier(info);
 
 	        ManagedReference<PlayerInRangeNotifier> notifierRef =
 		    AppContext.getDataManager().createReference(notifier);
 
 	        notifiers.put(clientID.getID(), notifierRef);
 		return;
 	    }
 
 	    ManagedReference<PlayerInRangeNotifier> notifierRef = notifiers.remove(clientID.getID());
 
 	    if (notifierRef == null) {
 	        System.out.println("Can't find notifier for " + clientID.getID());	
 		return;
 	    }
 
 	    notifierRef.get().done();
 	    return;
 	}
 
 	if (message instanceof PresenceInfoAddedMessage) {
 	    PresenceInfo presenceInfo = ((PresenceInfoAddedMessage) message).getPresenceInfo();
 
 	    if (presenceInfoList.contains(presenceInfo)) {
 		return;
 	    }
 
	    if (presenceInfo.clientID != null) {
	        presenceInfoMap.put(presenceInfo.clientID, presenceInfo);
	    }
 
 	    presenceInfoList.add(presenceInfo);
 	    logger.fine("PRESENCEINFOADDEDMESSAGE:  " + presenceInfo);
 
 	    /*
 	     * Send presenceInfo to all clients
 	     */
 	    sender.send(message);
 	    return;
 	} 
 
 	if (message instanceof PresenceInfoRemovedMessage) {
 	    PresenceInfo presenceInfo = ((PresenceInfoRemovedMessage) message).getPresenceInfo();
 
	    if (presenceInfo.clientID != null) {
	        presenceInfoMap.remove(presenceInfo.clientID);
	    }

 	    presenceInfoList.remove(presenceInfo);
 	    sender.send(message);
 	    return;
 	}
 
 	if (message instanceof PresenceInfoChangeMessage) {
 	    PresenceInfo presenceInfo = ((PresenceInfoChangeMessage) message).getPresenceInfo();
 
 	    presenceInfoList.remove(presenceInfo);
 	    presenceInfoList.add(presenceInfo);
 	    sender.send(message);
 	    return;
 	}
 
         throw new UnsupportedOperationException("Unknown message: " + message);
     }
 
     public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
 	PresenceInfo info = presenceInfoMap.get(clientID.getID());
 
 	logger.warning("client disconnected " + clientID.getID() + " " + info);
 
 	if (info == null) {
	    logger.warning("PRESENCE:  No PresenceInfo for " + clientID.getID());
 	    return;
 	}
 
         // mark ourself for update
         AppContext.getDataManager().markForUpdate(this);
 
 	ManagedReference<PlayerInRangeNotifier> notifierRef = notifiers.remove(clientID.getID());
 
 	if (notifierRef == null) {
 	    System.out.println("Can't find notifier for " + clientID.getID());	
 	} else {
 	    notifierRef.get().done();
 	}
 
 	presenceInfoList.remove(info);
 	sender.send(new PresenceInfoRemovedMessage(info));
     }
 
     private void dump(String msg) {
 	System.out.println("\n========  " + msg);
 
 	for (PresenceInfo info : presenceInfoList) {
 	    System.out.println("PI: " + info);
         }
 
 	System.out.println("========  " + msg + "\n");
     }
 
     public void callBeginEndNotification(CallStatus status) {
 	System.out.println("PI CS: " + status);
 
 	if (status.getCode() != CallStatus.ENDED) {
 	    return;
 	}
 
 	if (status.getOption("Reason").equalsIgnoreCase("Warm Start") == false) {
 	    return;
 	}
 
 	/*
 	 * For some reason, we don't get called at clientDisconnected()
 	 * during warm start, so we cleanup here.
 	 */
 	System.out.println("Clearing presence info");
 	presenceInfoList.clear();
     }
 
     static class PlayerInRangeNotifier implements ManagedPlayerInRangeListener {
 
 	private PresenceInfo presenceInfo;
 	
 	public PlayerInRangeNotifier(PresenceInfo presenceInfo) {
 	    this.presenceInfo = presenceInfo;
 
             VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	    Player player = vm.getPlayer(presenceInfo.callID);
 
 	    if (player == null) {
 	        System.out.println("PlayerInRangeListener:  No player for " + presenceInfo.callID);
 		return;
 	    }
 
 	    player.addPlayerInRangeListener(this);
 	}
 
 	public void done() {
             VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	    Player player = vm.getPlayer(presenceInfo.callID);
 
 	    if (player == null) {
 	        System.out.println("PRESENCE clientDisconnected:  No player for " + presenceInfo.callID);
 	    } else {
 	        player.removePlayerInRangeListener(this);
 	    }
 	}
 	
         public void playerInRange(Player player, Player playerInRange, boolean isInRange) {
 	    WonderlandClientSender sender =
                 WonderlandContext.getCommsManager().getSender(PresenceManagerConnectionType.CONNECTION_TYPE);
     	    
 	    WonderlandClientID clientID =
                CommsManagerFactory.getCommsManager().getWonderlandClientID(presenceInfo.clientID);
 
 	    sender.send(clientID, new PlayerInRangeMessage(playerInRange.getId(), isInRange));
         }
 
     }
 
 }
