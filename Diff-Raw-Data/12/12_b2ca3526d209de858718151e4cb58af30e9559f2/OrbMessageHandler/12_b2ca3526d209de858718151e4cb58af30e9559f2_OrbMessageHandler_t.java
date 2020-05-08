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
 package org.jdesktop.wonderland.modules.orb.client.cell;
 
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
 import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
 
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jdesktop.wonderland.client.ClientContext;
 
 import org.jdesktop.wonderland.client.cell.ChannelComponent;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.MovableComponent;
 
 import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
 
 import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
 
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 
 import org.jdesktop.wonderland.common.cell.messages.CellMessage;
 
 import org.jdesktop.wonderland.common.messages.Message;
 
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbAttachMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbEndCallMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbMuteCallMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbChangeNameMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbChangePositionMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbSetVolumeMessage;
 import org.jdesktop.wonderland.modules.orb.common.messages.OrbSpeakingMessage;
 
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagComponent;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode.EventType;
 
 import org.jdesktop.wonderland.client.comms.CellClientSession;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.swing.SwingUtilities;
 
 import org.jdesktop.wonderland.client.cell.TransformChangeListener;
 import org.jdesktop.wonderland.client.cell.TransformChangeListener.ChangeSource;
 
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.SceneWorker;
 
 import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
 
 import com.jme.math.Vector3f;
 
 import java.awt.Color;
 
 import com.jme.scene.Node;
 
 /**
  *
  * @author jprovino
  */
 public class OrbMessageHandler implements TransformChangeListener, FollowMeListener {
 
     private static final Logger logger =
             Logger.getLogger(OrbMessageHandler.class.getName());
 
     private ChannelComponent channelComp;
         
     private OrbCell orbCell;
 
     private Cell avatarCell;
 
     private Cell hostCell;
 
     private WonderlandSession session;
 
     private OrbDialog orbDialog;
 
     private String username;
 
     private NameTagNode nameTag;
 
     private PresenceManager pm;
 
     private PresenceInfo presenceInfo;
 
     private FollowMe followMe;
 
     private static HashMap<Cell, ArrayList<OrbCell>> attachedOrbMap = new HashMap();
 
     private static ArrayList<OrbCell> detachedOrbList = new ArrayList();
 
     public OrbMessageHandler(OrbCell orbCell, WonderlandSession session) {
 	this.orbCell = orbCell;
 	this.session = session;
 
 	synchronized (detachedOrbList) {
 	    detachedOrbList.add(orbCell);
 	}
 
 	avatarCell = ((CellClientSession)session).getLocalAvatar().getViewCell();
 	    
 	CellTransform transform = orbCell.getLocalTransform();
 	Vector3f translation = orbCell.getLocalTransform().getTranslation(null);
 	
 	followMe = new FollowMe(
 	    orbCell.getComponent(MovableComponent.class), translation, this);
 
         channelComp = orbCell.getComponent(ChannelComponent.class);
 
         logger.finer("OrbCellID " + orbCell.getCellID() + ", Channel comp is " 
 	    + channelComp);
 
         ChannelComponent.ComponentMessageReceiver msgReceiver =
             new ChannelComponent.ComponentMessageReceiver() {
                 public void messageReceived(CellMessage message) {
                     processMessage(message);
                 }
             };
 
         channelComp.addMessageReceiver(OrbAttachMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbChangeNameMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbEndCallMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbMuteCallMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbSetVolumeMessage.class, msgReceiver);
         channelComp.addMessageReceiver(OrbSpeakingMessage.class, msgReceiver);
 
         pm = PresenceManagerFactory.getPresenceManager(session);
 
 	username = orbCell.getUsername();
 
 	WonderlandIdentity userID = 
 	    new WonderlandIdentity(username, username, null);
 
         presenceInfo = new PresenceInfo(orbCell.getCellID(), null, userID, null);
 
 	pm.addSession(presenceInfo);
 
         NameTagComponent comp = new NameTagComponent(orbCell, username, (float) .17);
 	    orbCell.addComponent(comp);
 	nameTag = comp.getNameTagNode();
 
 	if (orbCell.getPlayerWithVpCallID().length() > 0) {
 	    PresenceInfo info = pm.getPresenceInfo(orbCell.getPlayerWithVpCallID());
 
 	    if (info == null) {
 		System.out.println("Can't find presence info for CallID " 
 		    + orbCell.getPlayerWithVpCallID());
 		return;
 	    }
 
 	    logger.fine("Attach orb " + orbCell.getCellID() 
 		+ " player with " + orbCell.getPlayerWithVpCallID() + " to " + info);
 
             channelComp.send(new OrbAttachMessage(orbCell.getCellID(), info.cellID, true));
 	} else {
 	    /*
 	     * Ask the server to tell us if the orb is attached.
 	     */
             channelComp.send(new OrbAttachMessage(orbCell.getCellID(), null, true));
 	}
     }
 
     private Node orbRootNode;
 
     public void setOrbRootNode(Node orbRootNode) {
 	this.orbRootNode = orbRootNode;
 	orbRootNode.attachChild(nameTag);
     }
 	
    private boolean done;

     public void done() {
	if (done) {
	    return;
	}

	done = true;

 	synchronized (detachedOrbList) {
 	    detachedOrbList.remove(orbCell);
 	    reorderDetachedOrbs();
 
 	    if (hostCell != null) {
 		ArrayList<OrbCell> attachedOrbList = attachedOrbMap.get(hostCell);
 
 		if (attachedOrbList != null) {
 		    attachedOrbList.remove(orbCell);
 		}
 	    }
 
 	    reorderAttachedOrbs();
 	}
 
	followMe.done();

         channelComp.removeMessageReceiver(OrbAttachMessage.class);
 	channelComp.removeMessageReceiver(OrbChangeNameMessage.class);
 	channelComp.removeMessageReceiver(OrbEndCallMessage.class);
         channelComp.removeMessageReceiver(OrbMuteCallMessage.class);
 	channelComp.removeMessageReceiver(OrbSetVolumeMessage.class);
         channelComp.removeMessageReceiver(OrbSpeakingMessage.class);
 
 	orbRootNode.detachChild(nameTag);
 	nameTag.done();
 
 	pm.removeSession(presenceInfo);
     }
 
     public void processMessage(final Message message) {
 	logger.finest("process message " + message);
 
 	if (message instanceof OrbSpeakingMessage) {
 	    OrbSpeakingMessage msg = (OrbSpeakingMessage) message;
 
 	    logger.info("Orb speaking " + msg.isSpeaking());
 
 	    pm.setSpeaking(presenceInfo, msg.isSpeaking());
 
 	    if (msg.isSpeaking()) {
 	        nameTag.setNameTag(EventType.STARTED_SPEAKING, 
 		    presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
 	    } else {
 	        nameTag.setNameTag(EventType.STOPPED_SPEAKING, 
 		    presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
 	    }
 
 	    return;
 	}
 
 	if (message instanceof OrbMuteCallMessage) {
 	    OrbMuteCallMessage msg = (OrbMuteCallMessage) message;
 
 	    pm.setMute(presenceInfo, msg.isMuted());
 
 	    if (msg.isMuted()) {
                 nameTag.setNameTag(EventType.MUTE, 
 		    presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
 	    } else {
                 nameTag.setNameTag(EventType.UNMUTE, 
 		    presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
 	    }
 
 	    return;
 	}
 
 	if (message instanceof OrbChangeNameMessage) {
 	    OrbChangeNameMessage msg = (OrbChangeNameMessage) message;
 
 	    username = msg.getName();
 	    pm.changeUsername(presenceInfo, username);
 	    nameTag.setNameTag(EventType.CHANGE_NAME, 
 		presenceInfo.userID.getUsername(), presenceInfo.usernameAlias);
 	    return;
 	}
 
 	if (message instanceof OrbAttachMessage) {
 	    OrbAttachMessage msg = (OrbAttachMessage) message;
 
 	    Cell newHostCell = ClientContext.getCellCache(session).getCell(msg.getHostCellID());
 
 	    if (newHostCell == null) {
 		System.out.println("Can't find host cell for " + msg.getHostCellID());
 		return;
 	    }
 
 	    if (logger.isLoggable(Level.FINE)) {
 	        String s = "None";
 
 	        if (hostCell != null) {
 	     	    s = hostCell.getCellID().toString();
 	        }
 
 	        logger.fine("Attach " + msg.isAttached() + " avatarCellID " 
 		    + avatarCell.getCellID() + " new host " + newHostCell.getCellID()
 		    + " current host " + s);
 	    }
 
 	    if (msg.isAttached()) {
 		if (hostCell != null) {
 		    /*
 		     * Someone else has attached the Orb.
 		     */
 		    detachOrb(false);
 		}
 
 		synchronized (detachedOrbList) {
 		    detachedOrbList.remove(orbCell);
 		}
 
 		ArrayList<OrbCell> attachedOrbList = attachedOrbMap.get(newHostCell);
 
 		if (attachedOrbList == null) {
 		    attachedOrbList = new ArrayList();
 
 		    attachedOrbMap.put(newHostCell, attachedOrbList);
 		} 
 
 		synchronized (attachedOrbList) {
 		    attachedOrbList.remove(orbCell);
 		    attachedOrbList.add(orbCell);
 		}
 		
 		hostCell = newHostCell;
 		newHostCell.addTransformChangeListener(this);
 		transformChanged(newHostCell, true);
 	    } else {
 		detachOrb(true);
 	    }
 	    return;
 	}
     }
 
     private void detachOrb(boolean setTransform) {
 	if (hostCell == null) {
 	    return;
 	}
 
         hostCell.removeTransformChangeListener(this);
 
 	if (setTransform) {
 	    transformChanged(hostCell, false);
 	}
 
 	ArrayList<OrbCell> attachedOrbList = attachedOrbMap.get(hostCell);
 
 	synchronized (attachedOrbList) {
 	    attachedOrbList.remove(orbCell);
 	}
 
 	synchronized (detachedOrbList) {
 	    detachedOrbList.add(orbCell);
 	}
 
 	if (orbDialog != null) {
             orbDialog.orbDetached();
 	}
 	hostCell = null;
     }
 
     public void transformChanged(Cell cell, ChangeSource source) {
 	transformChanged(cell, true);
     }
 
     private void transformChanged(Cell cell, boolean isAttached) {
 	logger.finest("Cell " + cell.getName() + " moved to " 
 	    + cell.getLocalTransform());
 
 	CellTransform transform = cell.getLocalTransform();
 	Vector3f translation = transform.getTranslation(null);
 	
 	if (isAttached) {
 	    // Position ourself based on other orbs
 	    float orbHeight = getOrbHeight();
 
 	    translation.setY(orbHeight);  // Raise orb.
 	    followMe.setTargetPosition(translation);
 	} else {
 	    translation.setZ(translation.getZ() + (float) .2);
 	    translation.setY((float) .5);  // lower orb.
 	    followMe.setTargetPosition(translation,
 		transform.getRotation(null));
 	}
     }
 
     public void positionChanged(Vector3f position) {
 	channelComp.send(new OrbChangePositionMessage(orbCell.getCellID(), position));
     }
 
     public void targetReached(Vector3f position) {
     }
 
     private float getOrbHeight() {
 	int i = detachedOrbList.indexOf(orbCell);
 
 	if (i >= 0) {
 	    return (float) (.1 + (.3 * i)); 
 	}
 
 	i = 0;
 
 	if (hostCell != null) {
 	    ArrayList<OrbCell> attachedOrbList = attachedOrbMap.get(hostCell);
 
 	    if (attachedOrbList != null) {
 	         i = attachedOrbList.indexOf(orbCell);
 
 	        if (i < 0) {
 	            i = 0;
 	        }
 	    } 
 	}
 
 	return (float) (2.2 + (.3 * i)); 
     }
 
     private void reorderDetachedOrbs() {
 	synchronized (detachedOrbList) {
 	    for (int i = 0 ; i < detachedOrbList.size(); i++) {
 		transformChanged(detachedOrbList.get(i), true);
 	    }
 	}
     }
 
     private void reorderAttachedOrbs() {
 	if (hostCell == null) {
 	    return;
 	}
 	
 	ArrayList<OrbCell> attachedOrbList = attachedOrbMap.get(hostCell);
 
 	if (attachedOrbList == null) {
 	    return;
 	}
 
 	synchronized (attachedOrbList) {
 	    for (int i = 0; i < attachedOrbList.size(); i++) {
 		transformChanged(attachedOrbList.get(i), true);
 	    }
 	}
     }
 
     public void orbSelected() {
 	if (orbDialog == null) {
 	    LocalAvatar avatar = ((CellClientSession)session).getLocalAvatar();
 	    
 	    orbDialog = new OrbDialog(orbCell, channelComp, 
 		avatar.getViewCell().getCellID());
 	} 
 
 	orbDialog.setVisible(true);
     }
 
 }
