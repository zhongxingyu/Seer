 /*
  * Copyright 2007 Sun Microsystems, Inc.
  *
  * This file is part of jVoiceBridge.
  *
  * jVoiceBridge is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License version 2 as 
  * published by the Free Software Foundation and distributed hereunder 
  * to you.
  *
  * jVoiceBridge is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied this 
  * code. 
  */
 
 package com.sun.mpk20.voicelib.impl.app;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.DataManager;
 import com.sun.sgs.app.ManagedReference;
 
 import com.sun.mpk20.voicelib.app.AudioSource;
 import com.sun.mpk20.voicelib.app.AudioSink;
 import com.sun.mpk20.voicelib.app.BridgeInfo;
 import com.sun.mpk20.voicelib.app.Call;
 import com.sun.mpk20.voicelib.app.CallBeginEndListener;
 import com.sun.mpk20.voicelib.app.CallSetup;
 import com.sun.mpk20.voicelib.app.Player;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 import com.sun.mpk20.voicelib.app.VoiceService;
 import com.sun.mpk20.voicelib.app.Util;
 
 import com.sun.voip.CallParticipant;
 
 import com.sun.voip.client.connector.CallStatus;
 import com.sun.voip.client.connector.CallStatusListener;
 
 import com.sun.voip.client.connector.impl.VoiceBridgeConnection;
 
 import java.math.BigInteger;
 
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 public class CallImpl implements Call, CallStatusListener, Serializable {
 
     private static final Logger logger =
         Logger.getLogger(CallImpl.class.getName());
 
     private CallSetup setup;
 
     private String id;
 
     private Player player;
 
     private boolean isMuted;
 
     public CallImpl(String id, CallSetup setup) throws IOException {
 	this.id = id;
 	this.setup = setup;
 	
 	if (setup.cp == null) {
 	    logger.warning("CallParticipant is null!");
 	    throw new IOException("CallParticipant is null!");
 	}
 
 	if (setup.cp.getCallId() == null) {
 	    if (id != null) {
 	        setup.cp.setCallId(Util.generateUniqueId(id));
 	    } else {
 		id = this.toString();
 
 		int ix = id.indexOf("@");
 
 		if (ix >= 0) {
 		    id = id.substring(ix + 1);
 		}
 
 	        setup.cp.setCallId(id);
 	    }
 	} 

 	String callee = setup.cp.getPhoneNumber();
 
 	logger.info("createCall:  callId " + this.id + " callee: " + callee
 	    + " Bridge: " + setup.bridgeInfo);
 
 	String name = this.id;
 
         int end;
 
         if (callee != null && (end = callee.indexOf("@")) >= 0) {
             name = callee.substring(0, end);
 
             int start;
 
 	    String pattern = "sip:";
 
             if ((start = callee.indexOf(pattern)) >= 0) {
                 name = callee.substring(start + pattern.length(), end);
             }
         } 
 
 	setup.cp.setName(name);
 
 	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	vm.addCallStatusListener(this, this.id);
 	
 	if (setup.listener != null) {
 	    vm.addCallStatusListener(setup.listener, this.id);
 	}
 
	if (setup.incomingCall == false) {
	    VoiceService backingManager = 
		AppContext.getManager(VoiceManager.class).getBackingManager();

            backingManager.createCall(setup);
	}
 
 	Player p = AppContext.getManager(VoiceManager.class).getPlayer(this.id);
 
         if (p != null) {
             logger.info("Call " + this.id + " player exists, reset privateMixes.");
 
             p.setPrivateMixes(true);
             return;
         }
 
 	vm.getCalls().put(this.id, this);
     }
 
     public String getId() {
 	return id;
     }
 
     public CallSetup getSetup() {
 	return setup;
     }
 
     public void setPlayer(Player player) {
 	this.player = player;
     }
 
     public Player getPlayer() {
 	return player;
     }
 
     public void mute(boolean isMuted) throws IOException {
 	this.isMuted = isMuted;
 
 	VoiceService backingManager = AppContext.getManager(VoiceManager.class).getBackingManager();
 	backingManager.muteCall(id, isMuted);
 
 	if (player != null) {
 	    player.setPrivateMixes(true);
 	}
     }
 
     public boolean isMuted() {
 	return isMuted;
     }
 
     public void transfer(CallParticipant cp) throws IOException {
 	VoiceService backingManager = AppContext.getManager(VoiceManager.class).getBackingManager();
   	backingManager.transferCall(cp);
 	setup.cp = cp;
     }
 
     public void transferToConference(String conferenceId) throws IOException {
         String[] tokens = conferenceId.split(":");
 
 	VoiceService backingManager = AppContext.getManager(VoiceManager.class).getBackingManager();
 	backingManager.transferToConference(id, tokens[0]);
     }
 
     public void playTreatment(String treatment) throws IOException {
 	VoiceService backingManager = AppContext.getManager(VoiceManager.class).getBackingManager();
 	backingManager.playTreatmentToCall(id, treatment);
     }
 
     public void pauseTreatment(String treatment) throws IOException {
 	VoiceService backingManager = AppContext.getManager(VoiceManager.class).getBackingManager();
 	backingManager.pauseTreatmentToCall(id, treatment);
     }
 
     public void stopTreatment(String treatment) throws IOException {
 	VoiceService backingManager = AppContext.getManager(VoiceManager.class).getBackingManager();
 	backingManager.stopTreatmentToCall(id, treatment);
     }
 
     public void end(boolean removePlayer) throws IOException {
         logger.fine("ending call " + id);
 
 	VoiceService backingManager = AppContext.getManager(VoiceManager.class).getBackingManager();
 	backingManager.endCall(id);
 
 	if (removePlayer) {
 	    AppContext.getManager(VoiceManager.class).removePlayer(id);
 	}
     }
 
     public void callStatusChanged(CallStatus callStatus) {
 	int code = callStatus.getCode();
 
 	String callId = callStatus.getCallId();
 
         if (callId == null || id.equals(callId) == false) {
             return;
         }
 
 	logger.finer("Call:  callStatus " + callStatus);
 
 	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
 	switch (code) {
         case CallStatus.ESTABLISHED:
         case CallStatus.MIGRATED:
             logger.warning("callEstablished: " + callId);
 
 	    String s = callStatus.getOption("IncomingCall");
 
 	    if (s != null && s.equals("true")) {
 		handleIncomingCall(callStatus);
 	    }
 
             break;
 
         case CallStatus.STARTEDSPEAKING:
             break;
 
         case CallStatus.STOPPEDSPEAKING:
             break;
 
         case CallStatus.ENDED:
 	    logger.info(callStatus.toString());
 	    vm.removeCallStatusListener(this);
 	    break;
 	}
     }
 
     private void sendStatus(int statusCode, String callId, String info) {
         String s = "SIPDialer/1.0 " + statusCode + " "
             + CallStatus.getCodeString(statusCode)
             + " CallId='" + callId + "'"
             + " CallInfo='" + info + "'";
 
         CallStatus callStatus = null;
 
         try {
             callStatus = VoiceBridgeConnection.parseCallStatus(s);
 
             if (callStatus == null) {
                 logger.info("Unable to parse call status:  " + s);
                 return;
             }
 
 	    callStatusChanged(callStatus);
         } catch (IOException e) {
             logger.info("Unable to parse call status:  " + s
 		+ " " + e.getMessage());
         }
     }
 
     private void handleIncomingCall(CallStatus callStatus) {
     }
 
     public String toString() {
 	return setup.cp.toString();
     }
 
     public void setBridgeInfo(BridgeInfo bridgeInfo) {
     }
 
     public BridgeInfo getBridgeInfo() {
 	return null;
     }
 
     public String sourceMoved(AudioSource source,
 	    double x, double y, double z, double orientation, double attenuation) {
 
 	return null;
     }
 
     public String sinkMoved(AudioSink sink,
              double x, double y, double z, double orientation, double attenuation) {
 	
 	return null;
     }
 
     public String dump() {
 	return "  " + setup.cp + (isMuted ? " MUTED" : "");
     }
 
 }
