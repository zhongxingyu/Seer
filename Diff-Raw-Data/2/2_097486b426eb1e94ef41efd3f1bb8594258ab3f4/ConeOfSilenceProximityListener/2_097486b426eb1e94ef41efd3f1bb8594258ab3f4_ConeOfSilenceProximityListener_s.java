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
 package org.jdesktop.wonderland.modules.audiomanager.server;
 
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.ConeOfSilenceEnterExitMessage;
 
 import com.sun.mpk20.voicelib.app.AudioGroup;
 import com.sun.mpk20.voicelib.app.AudioGroupListener;
 import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
 import com.sun.mpk20.voicelib.app.AudioGroupSetup;
 import com.sun.mpk20.voicelib.app.DefaultSpatializer;
 import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
 import com.sun.mpk20.voicelib.app.Player;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 import com.sun.sgs.app.AppContext;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.cell.CallID;
 import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.server.WonderlandContext;
 import org.jdesktop.wonderland.server.cell.CellMO;
 import org.jdesktop.wonderland.server.cell.ProximityListenerSrv;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 import com.jme.bounding.BoundingVolume;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;
 import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;
 
 import java.io.Serializable;
 
 import org.jdesktop.wonderland.common.cell.security.ViewAction;
 import org.jdesktop.wonderland.common.security.Action;
 import org.jdesktop.wonderland.server.cell.CellResourceManager;
 import org.jdesktop.wonderland.server.security.ActionMap;
 import org.jdesktop.wonderland.server.security.Resource;
 import org.jdesktop.wonderland.server.security.ResourceMap;
 import org.jdesktop.wonderland.server.security.SecureTask;
 import org.jdesktop.wonderland.server.security.SecurityManager;
 
 /**
  * A server cell that provides conference coneofsilence functionality
  * @author jprovino
  */
 public class ConeOfSilenceProximityListener implements ProximityListenerSrv, 
 	AudioGroupListener, Serializable {
 
     private static final Logger logger =
             Logger.getLogger(ConeOfSilenceProximityListener.class.getName());
 
     private CellID cellID;
     private String callID;
     private String name;
     private double outsideAudioVolume;
     private boolean entered;
 
     public ConeOfSilenceProximityListener(CellMO cellMO, String name, double outsideAudioVolume) {
 	cellID = cellMO.getCellID();
         this.name = name;
 	this.outsideAudioVolume = outsideAudioVolume;
     }
 
     public void viewEnterExit(boolean entered, CellID cellID,
             CellID viewCellID, BoundingVolume proximityVolume,
             int proximityIndex) {
 
 	logger.info("viewEnterExit:  " + entered + " cellID " + cellID
 	    + " viewCellID " + viewCellID);
 
 	//System.out.println("viewEnterExit:  " + entered + " cellID " + cellID
 	//    + " viewCellID " + viewCellID + " " + proximityVolume);
 
 	this.entered = entered;
 	this.callID = CallID.getCallID(viewCellID);
 
 	if (entered) {
 	    cellEntered(viewCellID);
 	} else {
 	    cellExited(viewCellID);
 	}
     }
 
     public void cellEntered(CellID cellID) {
         // get the security manager
         SecurityManager security = AppContext.getManager(SecurityManager.class);
         CellResourceManager crm = AppContext.getManager(CellResourceManager.class);
 
         // create a request
         Action viewAction = new ViewAction();
        Resource resource = crm.getCellResource(cellID);
         if (resource != null) {
             // there is security on this cell perform the enter notification
             // securely
             ActionMap am = new ActionMap(resource, new Action[] { viewAction });
             ResourceMap request = new ResourceMap();
             request.put(resource.getId(), am);
 
             // perform the security check
             security.doSecure(request, new CellEnteredTask(resource.getId(), cellID));
         } else {
             // no security, just make the call directly
             cellEntered(CallID.getCallID(cellID));
         }
     }
 
     private class CellEnteredTask implements SecureTask, Serializable {
         private String resourceID;
         private CellID softphoneCellID;
 
         public CellEnteredTask(String resourceID, CellID softphoneCellID) {
             this.resourceID = resourceID;
             this.softphoneCellID = softphoneCellID;
         }
 
         public void run(ResourceMap granted) {
             ActionMap am = granted.get(resourceID);
             if (am != null && !am.isEmpty()) {
                 // request was granted -- the user has permission to
                 // enter the COS
                 cellEntered(CallID.getCallID(softphoneCellID));
             } else {
                 logger.warning("Access denied to enter Cone of Silence");
             }
         }
     }
 
     private void cellEntered(String callId) {
         /*
          * A cell has entered the ConeOfSilence cell.
          * Set the public and incoming spatializers for the cell to be
          * the zero volume spatializer.
          * Set a private spatializer for the given fullVolume radius
          * for all the other avatars in the cell.
          * For each cell already in the cell, set a private spatializer
          * for this cell.
          */
         VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
         Player player = vm.getPlayer(callId);
 
         //System.out.println(callId + " entered cone " + name + " player " + player);
 
         if (player == null) {
             logger.warning("Can't find player for " + callId);
             return;
         }
 
         AudioGroup audioGroup = vm.getAudioGroup(name);
 
         if (audioGroup == null) {
 	    AudioGroupSetup setup = new AudioGroupSetup();
 
 	    setup.audioGroupListener = this;
 
 	    setup.spatializer = new FullVolumeSpatializer();
 
             setup.spatializer.setAttenuator(DefaultSpatializer.DEFAULT_MAXIMUM_VOLUME);
 
 	    //System.out.println("Creating audio group for " + name);
 
 	    audioGroup = vm.createAudioGroup(name, setup);
         }
 
 	//System.out.println("CONE PROX Player:  " + player);
 
         audioGroup.addPlayer(player, new AudioGroupPlayerInfo(true,
        	    AudioGroupPlayerInfo.ChatType.PRIVATE));
 
 	WonderlandClientSender sender =
             WonderlandContext.getCommsManager().getSender(AudioManagerConnectionType.CONNECTION_TYPE);
 
 	sender.send(new ConeOfSilenceEnterExitMessage(callId, true));
     }
 
     public void playerAdded(AudioGroup audioGroup, Player player, AudioGroupPlayerInfo info) {
 	//System.out.println("Player added:  " + player);
 
 	logger.fine("Attenuate other groups to " + outsideAudioVolume + " name " + name);
 
 	//System.out.println("Attenuate other groups to " + outsideAudioVolume + " name " + name);
 
 	Player p = AppContext.getManager(VoiceManager.class).getPlayer(player.getId());
 
 	if (player.toString().equals(p.toString()) == false) {
 	    System.out.println("WRONG player!");
 	    player = p;
 	}
 
 	player.attenuateOtherGroups(audioGroup, 0, outsideAudioVolume);
     }
 
     public void cellExited(CellID cellID) {
         cellExited(CallID.getCallID(cellID));
     }
 
     private void cellExited(String callId) {
         logger.info(callId + " exited cone " + name + " avatar cell ID " + callId);
 
         //System.out.println(callId + " exited cone " + name + " avatar cell ID " + callId);
 
         VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
         AudioGroup audioGroup = vm.getAudioGroup(name);
 
         if (audioGroup == null) {
             logger.warning("No audio group " + name);
             return;
         }
 
         Player player = vm.getPlayer(callId);
 
         if (player == null) {
             logger.warning("Can't find player for " + callId);
             return;
         }
 
         audioGroup.removePlayer(player);
 
 	WonderlandClientSender sender =
             WonderlandContext.getCommsManager().getSender(AudioManagerConnectionType.CONNECTION_TYPE);
 
 	sender.send(new ConeOfSilenceEnterExitMessage(callId, false));
     }
 
     public void playerRemoved(AudioGroup audioGroup, Player player, AudioGroupPlayerInfo info) {
 	VoiceChatHandler.updateAttenuation(player);
 
 	if (entered) {
 	    entered = false;
 
 	    WonderlandClientSender sender =
                 WonderlandContext.getCommsManager().getSender(AudioManagerConnectionType.CONNECTION_TYPE);
 
 	    sender.send(new ConeOfSilenceEnterExitMessage(callID, false));
 	}
     }
 
     public void remove() {
         VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
         AudioGroup audioGroup = vm.getAudioGroup(name);
 
 	logger.warning("Remove " + audioGroup + " name " + name);
 
 	if (audioGroup == null) {
 	    return;
 	}
 
 	vm.removeAudioGroup(audioGroup);
     }
 }
