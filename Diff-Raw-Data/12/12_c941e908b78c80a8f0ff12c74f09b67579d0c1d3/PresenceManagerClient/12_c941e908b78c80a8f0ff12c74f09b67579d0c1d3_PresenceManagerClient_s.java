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
 package org.jdesktop.wonderland.modules.presencemanager.client;
 
 import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
 import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;
 import org.jdesktop.wonderland.client.comms.BaseConnection;
 import org.jdesktop.wonderland.client.comms.CellClientSession;
 import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.input.InputManager;
 import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
 import org.jdesktop.wonderland.common.comms.ConnectionType;
 import org.jdesktop.wonderland.common.cell.CallID;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.CellStatus;
 import org.jdesktop.wonderland.common.messages.Message;
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceManagerConnectionType;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientConnectMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.ClientConnectResponseMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.PlayerInRangeMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.PresenceInfoAddedMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.PresenceInfoChangeMessage;
 import org.jdesktop.wonderland.modules.presencemanager.common.messages.PresenceInfoRemovedMessage;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.CellManager;
 
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode.EventType;
 
 import java.util.ArrayList;
 
 import java.util.logging.Logger;
 
 /**
  *
  * @author jprovino
  */
 public class PresenceManagerClient extends BaseConnection implements
         ViewCellConfiguredListener {
 
     private static final Logger logger =
             Logger.getLogger(PresenceManagerClient.class.getName());
     private WonderlandSession session;
     private CellID cellID;
     private boolean connected = true;
     private PresenceManagerImpl pm;
     private PresenceInfo presenceInfo;
     private static PresenceManagerClient client;
 
     public static PresenceManagerClient getInstance() {
 	return client;
     }
 
     /** 
      * Create a new PresenceManagerClient
      * @param session the session to connect to, guaranteed to be in
      * the CONNECTED state
      * @throws org.jdesktop.wonderland.client.comms.ConnectionFailureException
      */
     public PresenceManagerClient() {
         logger.fine("Starting PresenceManagerClient");
 	client = this;
     }
 
     public synchronized void execute(final Runnable r) {
     }
 
     @Override
     public void connect(WonderlandSession session)
             throws ConnectionFailureException
     {
         super.connect(session);
         this.session = session;
 
 	/*
 	 * Depending on timing, we may or may not have been called at viewConfigured().
 	 * We create and add the presence info for the client when viewConfigured() is called.
 	 */
         pm = (PresenceManagerImpl) PresenceManagerFactory.getPresenceManager(session);
 
         LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();
         avatar.addViewCellConfiguredListener(this);
         if (avatar.getViewCell() != null) {
             // if the view is already configured, fake an event
             viewConfigured(avatar);
         }
     }
 
     @Override
     public void disconnect() {
         // LocalAvatar avatar = ((CellClientSession)session).getLocalAvatar();
         // avatar.removeViewCellConfiguredListener(this);
         super.disconnect();
 	
 	pm.removePresenceInfo(presenceInfo);
 
 	PresenceManagerFactory.reset();
     }
 
     public void viewConfigured(LocalAvatar localAvatar) {
         cellID = localAvatar.getViewCell().getCellID();
 
         String callID = CallID.getCallID(cellID);
 
         SoftphoneControlImpl.getInstance().setCallID(callID);
 
 	presenceInfo = new PresenceInfo(cellID, session.getID(), session.getUserID(), callID);
 
 	pm.addPresenceInfo(presenceInfo);
 
         session.send(this, new ClientConnectMessage());
 
         logger.fine("[PresenceManagerClient] view configured fpr " + cellID + " in " + pm);
     }
 
     @Override
     public void handleMessage(Message message) {
         logger.fine("got a message... " + message);
 
 	if (message instanceof ClientConnectResponseMessage) {
 	    ClientConnectResponseMessage msg = (ClientConnectResponseMessage) message;
 
 	    ArrayList<String> nameTagList = new ArrayList();
 
 	    PresenceInfo[] presenceInfoList = msg.getPresenceInfoList();
 
 	    for (int i = 0; i < presenceInfoList.length; i++) {
 		PresenceInfo presenceInfo = presenceInfoList[i];
 
 		logger.fine("Client connected: " + presenceInfo);
 
 		logger.fine("Got ClientConnectResponse:  adding pi " + presenceInfo);
 		pm.presenceInfoAdded(presenceInfo);
 
 		String username = presenceInfo.userID.getUsername();
 
 		NameTagNode nameTag = NameTagNode.getNameTagNode(username);
 
 		if (presenceInfo.usernameAlias.equals(username) == false) {
  		    pm.changeUsernameAlias(presenceInfo);
  		}
 
 		if (nameTag == null) {
 		    nameTagList.add(username);
 		} else {
 		    nameTag.updateLabel(presenceInfo.usernameAlias, presenceInfo.inConeOfSilence,
 		        presenceInfo.isSpeaking, presenceInfo.isMuted);
 	        }
 	    }
 
 	    if (nameTagList.size() > 0) {
 		new NameTagUpdater(nameTagList);
 	    } 
 
 	    return;
 	}
 
         if (message instanceof PlayerInRangeMessage) {
 	    PlayerInRangeMessage msg = (PlayerInRangeMessage) message;
 
 	    PresenceInfo info = pm.getPresenceInfo(msg.getCallID());
 
 	    if (info == null) {
 		System.out.println("PlayerInRangeMessage:  no presence info for callID "
 		    + msg.getCallID());
 		return;
 	    }
 
 	    pm.playerInRange(info, msg.isInRange());
 	    return;
 	}
 
         if (message instanceof PresenceInfoAddedMessage) {
             PresenceInfoAddedMessage m = (PresenceInfoAddedMessage) message;
 
             logger.fine("GOT PresenceInfoAddedMessage for " + m.getPresenceInfo());
 
             pm.presenceInfoAdded(m.getPresenceInfo());
             return;
         }
 
         if (message instanceof PresenceInfoRemovedMessage) {
             PresenceInfoRemovedMessage m = (PresenceInfoRemovedMessage) message;
 
             logger.fine("GOT PresenceInfoRemovedMessage for " + m.getPresenceInfo());
             pm.presenceInfoRemoved(m.getPresenceInfo());
             return;
         }
 
         if (message instanceof PresenceInfoChangeMessage) {
             PresenceInfoChangeMessage m = (PresenceInfoChangeMessage) message;
 
             logger.fine("GOT PresenceInfoChangeMessage for " + m.getPresenceInfo());
             return;
         }
 
         throw new UnsupportedOperationException("Unknown message:  " + message);
     }
 
     /*
      * There is no way to know when other avatar names have been initialized.
      * When we connect, if we can't update the names with mute and alias info
      * because a name tag doesn't yet exist, we shedule the update for later.
      */
     class NameTagUpdater extends Thread {
 	
 	private ArrayList<String> nameTagList;
 
 	public NameTagUpdater(ArrayList<String> nameTagList) {
 	    this.nameTagList = nameTagList;
 
 	    start();
 	}
 
 	public void run() {
 	    while (true) {
 		String[] names = nameTagList.toArray(new String[0]);
 
 		for (int i = 0; i < names.length; i++) {
 		    String name = names[i];
 
 		    NameTagNode nameTag = NameTagNode.getNameTagNode(name);
 
 		    if (nameTag == null) {
 			continue;
 		    }
 
 		    nameTagList.remove(name);
 
		    PresenceInfo[] info = pm.getUserPresenceInfo(name);
 
		    if (info == null || info.length == 0) {
 			System.out.println("No presence info for " + name);
 			continue;
 		    }
 		
		    PresenceInfo pi = info[0];

		    nameTag.updateLabel(pi.usernameAlias, pi.inConeOfSilence,
			pi.isSpeaking, pi.isMuted);
 		}
 
 		if (nameTagList.size() == 0) {
 		    break;
 		}
 
 	        try {
 		    Thread.sleep(200);
 		} catch (InterruptedException e) {
 		}
 	    }
  	}
     }
 
     public ConnectionType getConnectionType() {
         return PresenceManagerConnectionType.CONNECTION_TYPE;
     }
 
 }
