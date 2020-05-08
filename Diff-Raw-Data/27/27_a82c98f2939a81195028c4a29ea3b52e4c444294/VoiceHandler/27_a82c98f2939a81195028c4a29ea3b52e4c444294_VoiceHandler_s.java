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
 
 package com.sun.mpk20.voicelib.app;
 
 import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
 
 import java.io.IOException;
 
 public interface VoiceHandler {
 
     /*
      * Find an available voice bridge.  The voice bridge address is given
      * to the softphone so it can register with the voice bridge.
      */ 
     public String getVoiceBridge();
 
     /*
      * Initiate a call on a specific bridge.
      */
     public String setupCall(String callId, String sipUrl, String bridge,
 	boolean confirmAnswered);
 
     public void createPlayer(String callId, double x, double y, double z,
 	double orientation, boolean isOutworlder);
 
     public void removePlayer(String callId);
 
     public void transferCall(String callId) throws IOException;
 
     public void setPublicSpatializer(String callId, Spatializer spatializer);
 
     public Spatializer getPublicSpatializer(String callId);
 
     public void removePublicSpatializer(String callId);
 
     public void setPrivateSpatializer(String targetCallId, String sourceCallId,
 	Spatializer spatializer);
 
     public void removePrivateSpatializer(String targetCallId, 
 	String sourceCallId);
 
     public void setIncomingSpatializer(String targetCallId, 
 	Spatializer spatializer);
 
     public Spatializer getIncomingSpatializer(String targetCallId);
 
     public void removeIncomingSpatializer(String targetCallId);
 
     public void setTalkAttenuator(String callId, double talkAttenuator);
 
     public double getTalkAttenuator(String callId);
 
     public void setListenAttenuator(String callId, double listenAttenuator);
 
     public double getListenAttenuator(String callId);
 
     public String setupTreatment(String id, String treatment, String group, 
 	ManagedCallStatusListener listener,
 	double lowerLeftX, double lowerLeftY, double lowerLeftZ,
 	double upperRightX, double upperRightY, double upperRightZ);
 
     public void newInputTreatment(String callId, String treatment, 
 	String group);
 
     public void stopInputTreatment(String callId);
 
     public void playTreatmentToCall(String callId, String treatment);
 
     public void pauseTreatmentToCall(String callId, String treatment);
 
     public void stopTreatmentToCall(String callId, String treatment);
 
     public void endCall(String callId);
 
     public void disconnectCall(String callId);
     
     public void muteCall(String callId, boolean isMuted);
 
     public Spatializer getLivePlayerSpatializer();
 
     public Spatializer getStationarySpatializer();
 
     public Spatializer getOutworlderSpatializer();
 
     public void setMasterVolume(String callId, double masterVolume);
 
     public double getMasterVolume(String callId);
 
     public void setSpatialMinVolume(double spatialMinVolume);
 
     public void setSpatialFalloff(double spatialFalloff);
 
     public void setSpatialEchoDelay(double spatialEchoDelay);
 
     public void setSpatialEchoVolume(double spatialEchoVolume);
 
     public void setSpatialBehindVolume(double spatialBehindVolume);
 
     public void addCallStatusListener(ManagedCallStatusListener listener);
 
     public void addCallStatusListener(ManagedCallStatusListener listener, 
 	String callId);
 	
     public void removeCallStatusListener(ManagedCallStatusListener listener);
 
     public void addCallBeginEndListener(ManagedCallBeginEndListener listener);
 
     public void removeCallBeginEndListener(ManagedCallBeginEndListener listener);
 
     public void setPositionAndOrientation(String callId , double x, double y, 
 	double z, double orientation);
 
     public void setPosition(String callId , double x, double y, double z);
 
     public void setOrientation(String callId, double orientation);
 
     public void setAttenuationRadius(String callId,
 	double attenuationRadius);
 
     public void setAttenuationVolume(String callId,
 	double attenuationVolume);
 
     public void addWall(double startX, double startY, double endX, double endY, 
 	double characteristic);
 
     public void setVoiceManagerParameters(VoiceManagerParameters p);
 
     public VoiceManagerParameters getVoiceManagerParameters();
 
     public int getNumberOfPlayersInRange(double x, double y, double z);
 
     public int getNumberOfPlayersInRange(String callId);
 
     public void setupRecorder(String id, double x, double y, double z, 
 	String recordingDirectoryPath) throws IOException;
 
     public void startRecording(String id, String recordingFile)
         throws IOException;
 
     public void stopRecording(String id) throws IOException;
 
     public void playRecording(String id, String recordingFile)
 	throws IOException; 
 
     public void stopPlayingRecording(String id, String recordingFile) 
 	throws IOException; 
 
     public void migrateCall(String callId, String phoneNumber)
 	throws IOException;
 
     public void setGroupId(String callId, String groupId);
 
     public String getGroupId(String callId);
 
 }
