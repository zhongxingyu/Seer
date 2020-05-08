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
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 
 import java.util.logging.Logger;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.ManagedReference;
 
 import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
 import org.jdesktop.wonderland.common.cell.CallID;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.common.cell.ClientCapabilities;
 
 
 import org.jdesktop.wonderland.common.cell.messages.CellMessage;
 
 import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
 import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.AudioParticipantComponentClientState;
 import org.jdesktop.wonderland.modules.audiomanager.common.AudioParticipantComponentServerState;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioParticipantSpeakingMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioParticipantMuteCallMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioVolumeMessage;
 
 import org.jdesktop.wonderland.server.WonderlandContext;
 
 import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
 import org.jdesktop.wonderland.server.cell.CellMO;
 import org.jdesktop.wonderland.server.cell.CellComponentMO;
 import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
 import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;
 
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 
 import com.sun.mpk20.voicelib.app.AudioGroup;
 import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
 import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo.ChatType;
 import com.sun.mpk20.voicelib.app.Call;
 import com.sun.mpk20.voicelib.app.CallSetup;
 import com.sun.mpk20.voicelib.app.DefaultSpatializer;
 import com.sun.mpk20.voicelib.app.Player;
 import com.sun.mpk20.voicelib.app.PlayerSetup;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 import com.sun.mpk20.voicelib.app.VoiceManagerParameters;
 
 import com.jme.math.Vector3f;
 
 import com.sun.voip.client.connector.CallStatus;
 import com.sun.voip.client.connector.CallStatusListener;
 
 import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
 
 import org.jdesktop.wonderland.modules.orb.server.cell.OrbCellMO;
 
 /**
  *
  * @author jprovino
  */
 public class AudioParticipantComponentMO extends CellComponentMO 
 	implements ManagedCallStatusListener {
 
     private static final Logger logger =
             Logger.getLogger(AudioParticipantComponentMO.class.getName());
 
     private MyTransformChangeListener myTransformChangeListener;
 
     private CellID cellID;
 
     private boolean isSpeaking;
     private boolean isMuted;
 
     /**
      * Create a AudioParticipantComponent for the given cell. 
      * @param cell
      */
     public AudioParticipantComponentMO(CellMO cellMO) {
         super(cellMO);
 
 	cellID = cellMO.getCellID();
 
 	//System.out.println("Adding AudioParticipantComponent to " + cellMO.getName());
     }
 
     @Override
     public CellComponentServerState getServerState(CellComponentServerState serverState) {
         AudioParticipantComponentServerState state = (AudioParticipantComponentServerState) serverState;
 
         if (state == null) {
             state = new AudioParticipantComponentServerState(isSpeaking, isMuted);
         }
 
         return state;
     }
 
     @Override
     public CellComponentClientState getClientState(
             CellComponentClientState clientState,
             WonderlandClientID clientID,
             ClientCapabilities capabilities) {
 
	System.out.println("Get client state " + isSpeaking + " " + isMuted);
 	return new AudioParticipantComponentClientState(isSpeaking, isMuted);
     }
 
     @Override
     public void setLive(boolean live) {
         ChannelComponentMO channelComponent = (ChannelComponentMO)
             cellRef.get().getComponent(ChannelComponentMO.class);
 
 	if (live == false) {
 	    if (myTransformChangeListener != null) {
 	        cellRef.get().removeTransformChangeListener(myTransformChangeListener);
 		myTransformChangeListener = null;
 	    }
 
 	    AppContext.getManager(VoiceManager.class).removeCallStatusListener(this);
 
 	    channelComponent.removeMessageReceiver(AudioVolumeMessage.class);
 	    return;
 	}
 
 	myTransformChangeListener = new MyTransformChangeListener();
 
 	CellMO cellMO = cellRef.get();
 
 	cellMO.addTransformChangeListener(myTransformChangeListener);
 
 	channelComponent.addMessageReceiver(AudioVolumeMessage.class, 
             new ComponentMessageReceiverImpl(cellRef, this));
     }
 
     protected String getClientClass() {
 	return "org.jdesktop.wonderland.modules.audiomanager.client.AudioParticipantComponent";
     }
 
     private static class ComponentMessageReceiverImpl extends AbstractComponentMessageReceiver {
 
         private ManagedReference<AudioParticipantComponentMO> compRef;
 
         public ComponentMessageReceiverImpl(ManagedReference<CellMO> cellRef,
                 AudioParticipantComponentMO comp) {
 
             super(cellRef.get());
 
             compRef = AppContext.getDataManager().createReference(comp);
         }
 
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID,
                CellMessage message) {
 
             if (message instanceof AudioVolumeMessage == false) {
 		logger.warning("Unknown message:  " + message);
 		return;
 	    }
 
 	    AudioVolumeMessage msg = (AudioVolumeMessage) message;
 
             CellID cellID = msg.getCellID();
             String softphoneCallID = msg.getSoftphoneCallID();
 
             double volume = msg.getVolume();
 
             //System.out.println("GOT Volume message:  call " + softphoneCallID + " cell " + cellID 
 	    //	+ " volume " + volume);
 
             VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
             Player softphonePlayer = vm.getPlayer(softphoneCallID);
 
             if (softphonePlayer == null) {
                 logger.warning("Can't find softphone player, callID " + softphoneCallID);
                 return;
             }
 
 	    String otherCallID = CallID.getCallID(cellID);
 
             if (softphoneCallID.equals(otherCallID)) {
                 System.out.println("Setting master volume for " + getCell().getName());
                 softphonePlayer.setMasterVolume(volume);
                 return;
             }
 
             DefaultSpatializer spatializer = new DefaultSpatializer();
 
             spatializer.setAttenuator(volume);
 
             Player player = vm.getPlayer(otherCallID);
 
  	    if (player == null) {
                 System.out.println("Can't find player for callID " + otherCallID);
 		return;
             } 
 
 	    //System.out.println(softphonePlayer + " has private spatializer for " + player
 	    //	+ " spatializer " + spatializer);
 
             softphonePlayer.setPrivateSpatializer(player, spatializer);
             return;
         }
     }
 
     public void addCallStatusListener(CallStatusListener listener) {
         addCallStatusListener(listener, null);
     }
 
     public void addCallStatusListener(CallStatusListener listener, String callID) {
         AppContext.getManager(VoiceManager.class).addCallStatusListener(listener, callID);
     }
 
     public void removeCallStatusListener(CallStatusListener listener) {
         removeCallStatusListener(listener, null);
     }
 
     public void removeCallStatusListener(CallStatusListener listener, String callID) {
         AppContext.getManager(VoiceManager.class).removeCallStatusListener(listener, callID);
     }
 
     public void callStatusChanged(CallStatus status) {
 	logger.finer("AudioParticipantComponent go call status:  " + status);
 
         ChannelComponentMO channelCompMO = (ChannelComponentMO)
             cellRef.get().getComponent(ChannelComponentMO.class);
 
 	String callId = status.getCallId();
 
 	if (callId == null) {
 	    logger.warning("No callId in status:  " + status);
 	    return;
 	}
 
 	int code = status.getCode();
 
 	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	AudioGroup audioGroup;
 
 	Call call = vm.getCall(callId);
 
 	Player player = null;
 
 	if (call != null) {
 	    player = call.getPlayer();
 	}
 
 	switch (code) {
 	case CallStatus.ESTABLISHED:
 	    if (player == null) {
		logger.warning("Couldn't find player for " + callId);
 		return;
 	    }
 
 	    vm.dump("all");
 	    player.setPrivateMixes(true);
 	    break;
 
 	case CallStatus.MUTED:
 	    isMuted = true;
 	    channelCompMO.sendAll(null, new AudioParticipantMuteCallMessage(cellID, true));
 	    break;
 
 	case CallStatus.UNMUTED:
 	    isMuted = false;
 	    channelCompMO.sendAll(null, new AudioParticipantMuteCallMessage(cellID, false));
 	    break;
 
         case CallStatus.STARTEDSPEAKING:
 	    isSpeaking = true;
 
 	    if (player == null) {
		logger.warning("Couldn't find player for " + callId);
 		return;
 	    }
 
 	    if (playerIsChatting(player)) {
 		VoiceChatHandler.getInstance().setSpeaking(player, cellID, true);
 	    }
 
 	    if (inSecretChat(player)) {
 		return;
 	    }
 
 	    channelCompMO.sendAll(null, new AudioParticipantSpeakingMessage(cellID, true));
             break;
 
         case CallStatus.STOPPEDSPEAKING:
 	    isSpeaking = false;
 
 	    if (player == null) {
		logger.warning("Couldn't find player for " + callId);
 		return;
 	    }
 
 	    if (playerIsChatting(player)) {
 		VoiceChatHandler.getInstance().setSpeaking(player, cellID, false);
 	    }
 
 	    if (inSecretChat(player)) {
 		return;
 	    }
 
 	    channelCompMO.sendAll(null, new AudioParticipantSpeakingMessage(cellID, false));
             break;
 
 	case CallStatus.ENDED:
 	    if (call != null) {
 		try {
 		    call.end(false);
 		} catch (IOException e) {
 		    logger.warning("Call " + call + " ENDED, unable to clean up:  " + e.getMessage());
 		}
 	    }
 
 	    if (player == null) {
		logger.warning("Couldn't find player for " + call);
 		return;
 	    }
 
 	    ArrayList<AudioGroup> audioGroups = player.getAudioGroups();
 
 	    for (AudioGroup group: audioGroups) {
 		group.removePlayer(player);
 	    }
             break;
 	  
 	case CallStatus.BRIDGE_OFFLINE:
             logger.info("Bridge offline: " + status);
 		// XXX need a way to tell the voice manager to reset all of the private mixes.
 		Call c = vm.getCall(callId);
 
 	    if (callId == null || callId.length() == 0) {
                 /*
                  * After the last BRIDGE_OFFLINE notification
                  * we have to tell the voice manager to restore
                  * all the pm's for live players.
                  */
                 logger.fine("Restoring private mixes...");
 	    } else {
 		if (c == null) {
 		    logger.warning("No call for " + callId);
 		    break;
 		}
 
 		Player p = c.getPlayer();
 
 		if (p == null) {
 		    logger.warning("No player for " + callId);
 		    break;
 		}
 
 		try {
 		    c.end(true);
 		} catch (IOException e) {
 		    logger.warning("Unable to end call " + callId);
 		}
 
 		try {
 		    AudioManagerConnectionHandler.setupCall(
 		 	callId, c.getSetup(), -p.getX(), p.getY(), p.getZ(), p.getOrientation());
 		} catch (IOException e) {
 		    logger.warning("Unable to setupCall " + c + " "
 			+ e.getMessage());
 		}
 	    }
 
             break;
         }
     }
 
     private boolean playerIsChatting(Player player) {
 	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	VoiceManagerParameters parameters = vm.getVoiceManagerParameters();
 
 	ArrayList<AudioGroup> audioGroups = player.getAudioGroups();
 
 	for (AudioGroup audioGroup : audioGroups) {
	    if (audioGroup.equals(parameters.livePlayerAudioGroup) == false ||
 	    	    audioGroup.equals(parameters.stationaryPlayerAudioGroup) == false) {
 
 		return true;
 	    }
 	}
 
 	return false;
     }
 
     private boolean inSecretChat(Player player) {
 	ArrayList<AudioGroup> audioGroups = player.getAudioGroups();
 
         for (AudioGroup audioGroup : audioGroups) {
             AudioGroupPlayerInfo info = audioGroup.getPlayerInfo(player);
 
             if (info.chatType == AudioGroupPlayerInfo.ChatType.SECRET) {
 		return true;
 	    }
 	}
 
 	return false;
     }
 
     static class MyTransformChangeListener implements TransformChangeListenerSrv {
 
         public void transformChanged(ManagedReference<CellMO> cellRef, 
 	        final CellTransform localTransform, final CellTransform localToWorldTransform) {
 
 	    logger.finest("TRANSFORM CHANGED:  " + cellRef.get().getCellID() + " local "
 		+ localTransform);
 
 	    logger.fine("localTransform " + localTransform + " world " 
 	        + localToWorldTransform);
 
 	    String callID;
 
 	    CellMO cellMO = cellRef.get();
 
 	    if (cellMO instanceof OrbCellMO) {
 		callID = ((OrbCellMO) cellMO).getCallID();
 	    } else {
 	        callID = CallID.getCallID(cellRef.get().getCellID());
 	    }
 
 	    float[] angles = new float[3];
 
 	    localToWorldTransform.getRotation(null).toAngles(angles);
 
 	    double angle = Math.toDegrees(angles[1]) % 360 + 90;
 
 	    Vector3f location = localToWorldTransform.getTranslation(null);
 	
 	    Player player = 
 		AppContext.getManager(VoiceManager.class).getPlayer(callID);
 
 	    //AudioTreatmentComponentMO component = 
 	    //	cellRef.get().getComponent(AudioTreatmentComponentMO.class);
 
 	    //if (component != null) {
 	    //    component.transformChanged(location, angle);   // let subclasses know
 	    //}
 
 	    if (player == null) {
 	        logger.info("can't find player for " + callID);
 		return;
 	    }
 
 	    player.moved(location.getX(), location.getY(), location.getZ(), angle);
 
 	    logger.finest("PLAYER MOVED " + player + " x " + location.getX()
 	    	+ " y " + location.getY() + " z " + location.getZ()
 	    	+ " angle " + angle);
         }
 
     }
 
 }
