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
 
 import java.util.logging.Logger;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.ManagedReference;
 
 import org.jdesktop.wonderland.common.cell.CellChannelConnectionType;
 import org.jdesktop.wonderland.common.cell.CallID;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 
 import org.jdesktop.wonderland.common.cell.messages.CellMessage;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioParticipantSpeakingMessage;
 import org.jdesktop.wonderland.modules.audiomanager.common.messages.AudioVolumeMessage;
 
 import org.jdesktop.wonderland.server.WonderlandContext;
 
 import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
 import org.jdesktop.wonderland.server.cell.CellMO;
 import org.jdesktop.wonderland.server.cell.CellComponentMO;
 import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
 import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;
 
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 
 import com.sun.mpk20.voicelib.app.DefaultSpatializer;
 import com.sun.mpk20.voicelib.app.Player;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 
 import com.jme.math.Vector3f;
 
 import com.sun.voip.client.connector.CallStatus;
 import com.sun.voip.client.connector.CallStatusListener;
 
 import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
 
 /**
  *
  * @author jprovino
  */
 public class AudioParticipantComponentMO extends CellComponentMO 
 	implements ManagedCallStatusListener {
 
     private static final Logger logger =
             Logger.getLogger(AudioParticipantComponentMO.class.getName());
 
     private MyTransformChangeListener myTransformChangeListener;
 
     /**
      * Create a AudioParticipantComponent for the given cell. 
      * @param cell
      */
     public AudioParticipantComponentMO(CellMO cellMO) {
         super(cellMO);
 
 	//System.out.println("Adding AudioParticipantComponent to " + cellMO.getName());
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
 
 	AppContext.getManager(VoiceManager.class).addCallStatusListener(this);
 
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
                 System.out.println("Can't find softphone player, callID " + softphoneCallID);
                 return;
             }
 
 	    String otherCallID = CallID.getCallID(cellID);
 
             if (softphoneCallID.equals(otherCallID)) {
                 //System.out.println("Setting master volume for " + getCell().getName());
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
 
 	WonderlandClientSender sender = 
 	    WonderlandContext.getCommsManager().getSender(CellChannelConnectionType.CLIENT_TYPE);
 
 	String callId = status.getCallId();
 
 	if (callId == null) {
 	    logger.warning("No callId in status:  " + status);
 	    return;
 	}
 
 	switch (status.getCode()) {
         case CallStatus.STARTEDSPEAKING:
 	    sender.send(new AudioParticipantSpeakingMessage(cellRef.get().getCellID(), true));
             break;
 
         case CallStatus.STOPPEDSPEAKING:
 	    sender.send(new AudioParticipantSpeakingMessage(cellRef.get().getCellID(), false));
             break;
 	}
     }
 
     static class MyTransformChangeListener implements TransformChangeListenerSrv {
 
         public void transformChanged(ManagedReference<CellMO> cellRef, 
 	        final CellTransform localTransform, final CellTransform localToWorldTransform) {
 
 	    logger.fine("localTransform " + localTransform + " world " 
 	        + localToWorldTransform);
 
 	    String callID = CallID.getCallID(cellRef.get().getCellID());
 
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
 	        logger.fine("can't find player for " + callID);
 		return;
 	    }
 
 	    player.moved(location.getX(), location.getY(), location.getZ(), angle);
 
 	    logger.fine(player + " x " + location.getX()
 	    	+ " y " + location.getY() + " z " + location.getZ()
 	    	+ " angle " + angle);
         }
 
     }
 
 }
