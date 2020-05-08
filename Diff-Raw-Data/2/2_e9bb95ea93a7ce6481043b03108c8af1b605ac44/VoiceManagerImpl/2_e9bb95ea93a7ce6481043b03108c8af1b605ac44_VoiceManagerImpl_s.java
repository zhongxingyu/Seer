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
 
 import com.sun.mpk20.voicelib.app.AudioGroup;
 import com.sun.mpk20.voicelib.app.AudioGroupSetup;
 import com.sun.mpk20.voicelib.app.BridgeInfo;
 import com.sun.mpk20.voicelib.app.Call;
 import com.sun.mpk20.voicelib.app.CallSetup;
 import com.sun.mpk20.voicelib.app.DefaultSpatializer;
 import com.sun.mpk20.voicelib.app.DefaultSpatializers;
 import com.sun.mpk20.voicelib.app.CallBeginEndListener;
 import com.sun.mpk20.voicelib.app.Player;
 import com.sun.mpk20.voicelib.app.PlayerSetup;
 import com.sun.mpk20.voicelib.app.Recorder;
 import com.sun.mpk20.voicelib.app.RecorderSetup;
 import com.sun.mpk20.voicelib.app.Treatment;
 import com.sun.mpk20.voicelib.app.TreatmentGroup;
 import com.sun.mpk20.voicelib.app.TreatmentSetup;
 import com.sun.mpk20.voicelib.app.VirtualPlayerListener;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 import com.sun.mpk20.voicelib.app.VoiceService;
 import com.sun.mpk20.voicelib.app.VoiceBridgeParameters;
 import com.sun.mpk20.voicelib.app.VoiceManagerParameters;
 
 import com.sun.sgs.kernel.KernelRunnable;
 
 import com.sun.voip.client.connector.CallStatus;
 import com.sun.voip.client.connector.CallStatusListener;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class VoiceManagerImpl implements VoiceManager {
 
     /** The serialVersionUID for this class. */
     private final static long serialVersionUID = 1L;
 
     private static final Logger logger =
         Logger.getLogger(VoiceManagerImpl.class.getName());
 
     private VoiceService backingManager;
 
     public VoiceManagerImpl(VoiceService backingManager) {
 	this.backingManager = backingManager;
     }
 
     /*
      * VoiceManager
      */
     public void setLogLevel(Level level) {
 	logger.setLevel(level);
 
         logger.info("level " + level + " set log level to "
             + logger.getLevel() + " int " + logger.getLevel().intValue());
 
         if (backingManager != null) {
 	    backingManager.setLogLevel(level);
 	}
     }
 
     public Level getLogLevel() {
 	return logger.getLevel();
     }
 
     public void setVoiceManagerParameters(VoiceManagerParameters parameters) {
 	backingManager.setVoiceManagerParameters(parameters);
     }
 
     public VoiceManagerParameters getVoiceManagerParameters() {
 	return backingManager.getVoiceManagerParameters();
     }
 
     /*
      * Voice bridge parameters.
      */
     public void setVoiceBridgeParameters(VoiceBridgeParameters parameters) {
     }
 	
     /*
      * Call Setup
      */
 
     /**
      * Get the next available voice bridge.
      */
     public BridgeInfo getVoiceBridge() throws IOException {
 	return backingManager.getVoiceBridge();
     }
 
     /**
      * Initiate a call
      */
     public Call createCall(String id, CallSetup setup) throws IOException {
 	return backingManager.createCall(id, setup);
     }
 
     public Call getCall(String id) {
 	return backingManager.getCall(id);
     }
 
     public Call[] getCalls() {
 	return backingManager.getCalls();
     }
 
     public void endCall(Call call, boolean removePlayer) throws IOException {
 	backingManager.endCall(call, removePlayer);
     }
 
     public void addCallStatusListener(CallStatusListener listener) {
 	addCallStatusListener(listener, null);
     }
 
     public void addCallStatusListener(CallStatusListener listener, 
 	    String callId) {
 
 	backingManager.addCallStatusListener(listener, callId);
     }
     
     public void removeCallStatusListener(CallStatusListener listener) {
 	removeCallStatusListener(listener, null);
     }
 
     public void removeCallStatusListener(CallStatusListener listener, 
 	    String callId) {
 
 	backingManager.removeCallStatusListener(listener, callId);
     }
   
     public void addCallBeginEndListener(CallBeginEndListener listener) {
 	backingManager.addCallBeginEndListener(listener);
     }
 
     public void removeCallBeginEndListener(CallBeginEndListener listener) {
	removeCallBeginEndListener(listener);
     }
   
     /*
      * Player Control
      */
     public Player createPlayer(String id, PlayerSetup setup) {
 	return backingManager.createPlayer(id, setup);
     }
 
     public Player getPlayer(String id) {
 	return backingManager.getPlayer(id);
     }
 
     public Player[] getPlayers() {
 	return backingManager.getPlayers();
     }
 
     public void removePlayer(Player player) {
 	backingManager.removePlayer(player);
     }
 
     public int getNumberOfPlayersInRange(double x, double y, double z) {
 	return backingManager.getNumberOfPlayersInRange(x, y, z);
     }
 
     /*
      * Group management
      */
     public AudioGroup createAudioGroup(String id, AudioGroupSetup setup) {
 	return backingManager.createAudioGroup(id, setup);
     }
 
     public AudioGroup getAudioGroup(String id) {
 	return backingManager.getAudioGroup(id);
     }
 
     public void removeAudioGroup(AudioGroup audioGroup) {
 	backingManager.removeAudioGroup(audioGroup);
     }
 
     /*
      * Treatments
      */
     public TreatmentGroup createTreatmentGroup(String id) {
 	return backingManager.createTreatmentGroup(id);
     }
 
     public void removeTreatmentGroup(TreatmentGroup group) throws IOException {
 	backingManager.removeTreatmentGroup(group);
     }
 	
     public TreatmentGroup getTreatmentGroup(String id) {
 	return backingManager.getTreatmentGroup(id);
     }
 
     public Treatment createTreatment(String id, TreatmentSetup setup) 
 	    throws IOException {
 
 	return backingManager.createTreatment(id, setup);
     }
 
     public Treatment getTreatment(String id) {
 	return backingManager.getTreatment(id);
     }
 
     /*
      * Recording setup and control
      */
     public Recorder createRecorder(String id, RecorderSetup setup) 
 	    throws IOException {
 
 	return backingManager.createRecorder(id, setup);
     }
 
     public Recorder getRecorder(String id) {
 	return backingManager.getRecorder(id);
     }
 
     public void setSpatialAudio(boolean enabled) {
 	try {
 	    backingManager.setSpatialAudio(enabled);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio: " + e.getMessage());
 	}
     }
 
     public void setSpatialMinVolume(double spatialMinVolume) {
 	try {
 	    backingManager.setSpatialMinVolume(spatialMinVolume);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio min volume: " 
 		+ e.getMessage());
 	}
     }
 
     public void setSpatialFalloff(double spatialFalloff) {
 	try {
 	    backingManager.setSpatialFalloff(spatialFalloff);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio fall off: " 
 		+ e.getMessage());
 	}
     }
 
     public void setSpatialEchoDelay(double spatialEchoDelay) {
         try {
             backingManager.setSpatialEchoDelay(spatialEchoDelay);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio echo delay: "
                 + e.getMessage());
         }
     }
 
     public void setSpatialEchoVolume(double spatialEchoVolume) {
         try {
             backingManager.setSpatialEchoVolume(spatialEchoVolume);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio echo volume: "
                 + e.getMessage());
         }
     }
 
     public void setSpatialBehindVolume(double spatialBehindVolume) {
         try {
             backingManager.setSpatialBehindVolume(spatialBehindVolume);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio behind volume: "
                 + e.getMessage());
         }
     }
 
     public void scheduleTask(KernelRunnable runnable, long startTime) {
 	backingManager.scheduleTask(runnable, startTime);
     }
 
     public String dump(String command) {
 	return backingManager.dump(command);
     }
 
 }
