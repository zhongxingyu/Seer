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
 
 import org.jdesktop.wonderland.common.messages.Message;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.AudioManagerConnectionType;
 
 import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.CellStatusChangeMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.DisconnectCallMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetVoiceBridgeResponseMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.MuteCallMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlaceCallMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.SpeakingMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.TransferCallMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage;
 
 import org.jdesktop.wonderland.common.cell.CellID;
 
 import org.jdesktop.wonderland.common.comms.ConnectionType;
 
 import org.jdesktop.wonderland.server.WonderlandContext;
 
 import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
 import org.jdesktop.wonderland.server.comms.CommsManager;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 
 import java.io.Serializable;
 
 import java.util.logging.Logger;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Properties;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.ManagedReference;
 
 import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
 import com.sun.mpk20.voicelib.app.Call;
 import com.sun.mpk20.voicelib.app.CallSetup;
 import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
 import com.sun.mpk20.voicelib.app.Player;
 import com.sun.mpk20.voicelib.app.PlayerSetup;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 
 import com.sun.voip.CallParticipant;
 
 import com.sun.voip.client.connector.CallStatus;
 
 import java.io.IOException;
 
 import java.math.BigInteger;
 
 /**
  * Audio Manager
  * 
  * @author jprovino
  */
 public class AudioManagerConnectionHandler 
         implements ClientConnectionHandler, Serializable, ManagedCallStatusListener
 {
     private static final Logger logger =
             Logger.getLogger(AudioManagerConnectionHandler.class.getName());
     
     private VoiceChatHandler voiceChatHandler = new VoiceChatHandler();
 
     private ConcurrentHashMap<BigInteger, String> sessionCallIDMap = 
 	new ConcurrentHashMap();
 
     private static AudioManagerConnectionHandler handler;
 
     public static AudioManagerConnectionHandler getInstance() {
 	if (handler == null) {
 	    handler = new AudioManagerConnectionHandler();
 	}
 
 	return handler;
     }
 
     private AudioManagerConnectionHandler() {
         super();
     }
 
     public ConnectionType getConnectionType() {
         return AudioManagerConnectionType.CONNECTION_TYPE;
     }
 
     public void registered(WonderlandClientSender sender) {
 	logger.fine("Audio Server manager connection registered");
     }
 
     public void clientConnected(WonderlandClientSender sender, 
 	    WonderlandClientID clientID, Properties properties) {
 
 	logger.fine("client connected...");
     }
 
     public void messageReceived(WonderlandClientSender sender, 
 	    WonderlandClientID clientID, Message message) {
 
 	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	if (message instanceof CellStatusChangeMessage) {
 	    return;
 	}
 
 	if (message instanceof GetVoiceBridgeMessage) {
	    String voiceBridge;
 
 	    try {
	        voiceBridge = vm.getVoiceBridge().toString();
	        logger.info("Got voice bridge '" + voiceBridge + "'");
 	    } catch (IOException e) {
 		logger.warning("unable to get voice bridge:  " + e.getMessage());
 		return;
 	    }
 
	    sender.send(clientID, new GetVoiceBridgeResponseMessage(voiceBridge));
 	    return;
 	}
 
 	if (message instanceof PlaceCallMessage) {
 	    logger.fine("Got place call message from " + clientID);
 
 	    PlaceCallMessage msg = (PlaceCallMessage) message;
 
 	    PresenceInfo info = msg.getPresenceInfo();
 
 	    CallSetup setup = new CallSetup();
 
 	    CallParticipant cp = new CallParticipant();
 
 	    setup.cp = cp;
 	    //setup.listener = this;
 
 	    String callID = info.callID;
 	
 	    vm.addCallStatusListener(this, callID);
 	
 	    logger.fine("callID " + callID);
 
 	    if (callID == null) {
 	        logger.fine("Can't place call to " + msg.getSipURL()
 		    + ".  No cell for " + callID);
 		return;
 	    }
 
 	    cp.setCallId(callID);
 	    cp.setName(info.userID.getUsername());
             cp.setPhoneNumber(msg.getSipURL());
             cp.setConferenceId(vm.getVoiceManagerParameters().conferenceId);
             cp.setVoiceDetection(true);
             cp.setDtmfDetection(true);
             cp.setVoiceDetectionWhileMuted(true);
             cp.setHandleSessionProgress(true);
             cp.setJoinConfirmationTimeout(0);
 	    cp.setCallAnsweredTreatment(null);
 
 	    sessionCallIDMap.put(clientID.getID(), callID);
 
 	    try {
 	        setupCall(callID, setup, msg.getX(), 
 		    msg.getY(), msg.getZ(), msg.getDirection());
 	    } catch (IOException e) {
 		logger.warning("Unable to place call " + cp + " " 
 		    + e.getMessage());
 		sessionCallIDMap.remove(clientID.getID());
 	    }
 	    return;
 	}
 
 	if (message instanceof MuteCallMessage) {
 	    MuteCallMessage msg = (MuteCallMessage) message;
 
 	    String callID = msg.getCallID();
 
 	    Call call = vm.getCall(callID);
 
 	    if (call == null) {
 		logger.warning("Unable to mute/unmute call " + callID);
 		return;
 	    }	
 
 	    try {
 	        call.mute(msg.isMuted());
 	    } catch (IOException e) {
 		logger.warning("Unable to mute/unmute call " + callID + ": "
 		    + e.getMessage());
 		return;
 	    }
 
 	    sender.send(new MuteCallMessage(callID, msg.isMuted()));
 	    return;
 	}
 	
 	if (message instanceof TransferCallMessage) {
 	    TransferCallMessage msg = (TransferCallMessage) message;
 
 	    String callID = msg.getPresenceInfo().callID;
 
 	    Call call = vm.getCall(callID);
 
 	    if (call == null) {
 		// XXX we should be nicer and place the call!
 		logger.warning("Unable to transfer call.  No Call for " + callID);
 		return;
 	    }
 
 	    CallParticipant cp = call.getSetup().cp;
 
             cp.setPhoneNumber(msg.getPhoneNumber());
             cp.setJoinConfirmationTimeout(90);
 
 	    String callAnsweredTreatment = System.getProperty(
                 "com.sun.sgs.impl.app.voice.CALL_ANSWERED_TREATMENT");
 
 	    if (callAnsweredTreatment == null || callAnsweredTreatment.length() == 0) {
 		callAnsweredTreatment = "dialtojoin.au";
 	    }
 
             cp.setCallAnsweredTreatment(callAnsweredTreatment);
 
 	    try {
 	        call.transfer(cp);
 	    } catch (IOException e) {
 		logger.warning("Unable to transfer call:  " + e.getMessage());
 	    }
 	    return;
 	}
 
 	if (message instanceof DisconnectCallMessage) {
 	    logger.fine("got DisconnectCallMessage");
 	    return;
 	}
 
 	if (message instanceof VoiceChatMessage) {
 	    voiceChatHandler.processVoiceChatMessage(sender, clientID, 
 		(VoiceChatMessage) message);
 	    return;
 	}
 
         throw new UnsupportedOperationException("Unknown message:  " + message);
     }
 
     private void setupCall(String callID, CallSetup setup, double x, 
 	    double y, double z, double direction) throws IOException {
 
 	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	Player p = vm.getPlayer(callID);
 
 	Call call;
 
         call = vm.createCall(callID, setup);
 
 	callID = call.getId();
 
         PlayerSetup ps = new PlayerSetup();
 
 	if (p == null) {
             ps.x = x;
             ps.y = y;
             ps.z = z;
 	} else {
 	    ps.x = p.getSetup().x;
 	    ps.y = p.getSetup().y;
 	    ps.z = p.getSetup().z;
 	}
 
         ps.orientation = direction;
         ps.isLivePlayer = true;
 
 	Player player = vm.createPlayer(callID, ps);
 
         call.setPlayer(player);
         player.setCall(call);
 
         vm.getVoiceManagerParameters().livePlayerAudioGroup.addPlayer(player,
             new AudioGroupPlayerInfo(true, 
 	    AudioGroupPlayerInfo.ChatType.PUBLIC));
 
         AudioGroupPlayerInfo info = new AudioGroupPlayerInfo(false,
             AudioGroupPlayerInfo.ChatType.PUBLIC);
 
         info.defaultSpeakingAttenuation = 0;
 
         vm.getVoiceManagerParameters().stationaryPlayerAudioGroup.addPlayer(
 	    player, info);
     }
 
     public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
 	BigInteger sessionID = clientID.getID();
 
 	String callID = sessionCallIDMap.get(sessionID);
 
 	if (callID == null) {
 	    logger.warning("Unable to find callID for client session " 
 		+ sessionID);
 	    return;
 	}
 
 	sessionCallIDMap.remove(sessionID);
 
 	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	Call call = vm.getCall(callID);
 
 	if (call == null) {
 	    logger.warning("Can't find call for " + callID);
 	    return;
 	}
 
 	try {
 	    AppContext.getManager(VoiceManager.class).endCall(call, true);
 	} catch (IOException e) {
 	    logger.warning("Unable to end call " + call + " " + e.getMessage());
 	}
     }
 
     public void callStatusChanged(CallStatus status) {
 	logger.finer("GOT STATUS " + status);
 
 	int code = status.getCode();
 
 	String callId = status.getCallId();
 
 	if (callId == null) {
 	    logger.warning("No callId in status:  " + status);
 	    return;
 	}
 
 	CommsManager cm = WonderlandContext.getCommsManager();
 
 	WonderlandClientSender sender = cm.getSender(AudioManagerConnectionType.CONNECTION_TYPE);
 
 	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	switch (code) {
 	case CallStatus.ESTABLISHED:
 	    Call call = vm.getCall(callId);
 
 	    if (call == null) {
 		logger.warning("Couldn't find call for " + callId);
 		return;
 	    }
 
 	    Player player = call.getPlayer();
 
 	    if (player == null) {
 		logger.warning("Couldn't find player for " + call);
 		return;
 	    }
 
 	    vm.dump("all");
 	    player.setPrivateMixes(true);
 	    break;
 
         case CallStatus.STARTEDSPEAKING:
 	    sender.send(new SpeakingMessage(callId, true));
             break;
 
         case CallStatus.STOPPEDSPEAKING:
 	    sender.send(new SpeakingMessage(callId, false));
             break;
 
 	case CallStatus.ENDED:
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
 		    setupCall(callId, c.getSetup(), -p.getX(), p.getY(), p.getZ(), 
 		        p.getOrientation());
 		} catch (IOException e) {
 		    logger.warning("Unable to setupCall " + c + " "
 			+ e.getMessage());
 		}
 	    }
 
             break;
         }
     }
 
 }
 
