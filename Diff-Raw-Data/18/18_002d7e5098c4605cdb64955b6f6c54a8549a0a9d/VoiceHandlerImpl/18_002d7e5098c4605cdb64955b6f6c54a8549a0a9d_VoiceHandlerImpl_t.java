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
 
 import com.sun.mpk20.voicelib.app.DefaultSpatializer;
 import com.sun.mpk20.voicelib.app.ZeroVolumeSpatializer;
 import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
 import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
 import com.sun.mpk20.voicelib.app.ManagedCallBeginEndListener;
 import com.sun.mpk20.voicelib.app.VoiceHandler;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.DataManager;
 import com.sun.sgs.app.ManagedObject;
 import com.sun.sgs.app.ManagedReference;
 import com.sun.sgs.app.NameNotBoundException;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.sun.voip.client.connector.CallStatus;
 import com.sun.voip.client.connector.CallStatusListener;
 import com.sun.voip.CallParticipant;
 
 import com.sun.mpk20.voicelib.app.Spatializer;
 
 public class VoiceHandlerImpl implements VoiceHandler, 
 	ManagedCallStatusListener, Serializable {
 
     /** The serialVersionUID for this class. */
     private final static long serialVersionUID = 1L;
 
     public static final String NAME = VoiceManager.class.getName();
     public static final String DS_PREFIX = NAME + ".";
 
     /* The name of the list for call status listeners */
     private static final String DS_CALL_STATUS_LISTENERS =
         DS_PREFIX + "CallStatusListeners";
     
     /* The name of the list for call begin/end listeners */
     private static final String DS_CALL_BEGIN_END_LISTENERS =
         DS_PREFIX + "CallBeginEndListeners";
     
     private final static String DEFAULT_CONFERENCE_ID = "Test:PCM/44100/2";
 
     private final static String AUDIO_DIR =
             "com.sun.mpk20.gdcdemo.server.AUDIO_DIR";
 
     private final static String DEFAULT_AUDIO_DIR = ".";
 
     private static final Logger logger =
         Logger.getLogger(VoiceHandlerImpl.class.getName());
 
     // the audio directory
     private static String audioDir = 
             System.getProperty(AUDIO_DIR, DEFAULT_AUDIO_DIR);
     
     private static final String SCALE = 
 	"org.jdesktop.lg3d.wonderland.darkstar.server.VoiceHandler.SCALE";
 
     private static double scale = 100.;
 
     static {
 	String s = System.getProperty(SCALE);
 
 	if (s != null) {
 	    try {
 		scale = Double.parseDouble(s);
 	    } catch (NumberFormatException e) {
 		logger.info("Invalid scale factor:  " + s);
 	    }
 	}
     }
 
     public static VoiceHandler getInstance() {
         DataManager dm = AppContext.getDataManager();
         
         VoiceHandlerImpl vh;
         
         try {
             vh = dm.getBinding(NAME, VoiceHandlerImpl.class);
         } catch (NameNotBoundException nnbe) {
             vh = new VoiceHandlerImpl();
             dm.setBinding(NAME, vh);
         }
         
         return vh;
     }
 
     private VoiceHandlerImpl() {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 	voiceManager.addCallStatusListener(this);
 
         DataManager dm = AppContext.getDataManager();
 
         try {
             dm.getBinding(DS_CALL_STATUS_LISTENERS, CallStatusListeners.class);
         } catch (NameNotBoundException e) {
             try {
                 dm.setBinding(DS_CALL_STATUS_LISTENERS, new CallStatusListeners());
             }  catch (RuntimeException re) {
                 logger.warning("failed to bind pending map " + re.getMessage());
                 throw re;
             }
         }
 
         try {
             dm.getBinding(DS_CALL_BEGIN_END_LISTENERS, CallBeginEndListeners.class);
         } catch (NameNotBoundException e) {
             try {
                 dm.setBinding(DS_CALL_BEGIN_END_LISTENERS, new CallBeginEndListeners());
             }  catch (RuntimeException re) {
                 logger.warning("failed to bind pending map " + re.getMessage());
                 throw re;
             }
         }
 
 	try {
 	    /*
 	     * XXX We just do this so the voiceService can have a 
 	     * default task owner to use.
 	     * Also so we start monitoring the conference status in case someone
 	     * places a call from outside the client.
 	     */
 	    String conferenceId = System.getProperty(
 	        "com.sun.mpk20.gdcdemo.client.sample.CONFERENCE_ID",
 	        DEFAULT_CONFERENCE_ID);
 
 	    voiceManager.monitorConference(conferenceId);
 	} catch (IOException e) {
 	    logger.severe("Unable to communicate with voice bridge:  " 
 		+ e.getMessage());
 	    return;
 	} 
     }
 
     public static final class CallStatusListeners extends HashMap<ManagedReference, String> 
     	    implements ManagedObject, Serializable {
 
          private static final long serialVersionUID = 1;
     }
 
     private static final class CallBeginEndListeners extends
             ArrayList<ManagedReference> implements ManagedObject, Serializable {
 
          private static final long serialVersionUID = 1;
     }
 
     public String getVoiceBridge() {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	String vb = voiceManager.getVoiceBridge();
 	logger.finer("voice bridge is " + vb);
 	return vb;
     }
 
     /*
      * The callId must be unique.  For wonderland, the userId is guaranteed to
      * be unique and is used as the callId for the softphone call.
      */
     public String setupCall(String callId, String sipUrl, String bridge) {
 	logger.info("setupCall:  callId " + callId + " Url: " + sipUrl
 	    + " Bridge: " + bridge);
 
 	CallParticipant cp = new CallParticipant();
 
 	String conferenceId = System.getProperty(
 	    "com.sun.mpk20.gdcdemo.client.sample.CONFERENCE_ID",
 	    DEFAULT_CONFERENCE_ID);
 
 	cp.setConferenceId(conferenceId);
 
 	String name = "Anonymous";
 
 	cp.setPhoneNumber(sipUrl);
 
         int end;
 
         if ((end = sipUrl.indexOf("@")) >= 0) {
             name = sipUrl.substring(0, end);
 
             int start;
 
             if ((start = sipUrl.indexOf(":")) >= 0) {
                 name = sipUrl.substring(start + 1, end);
             }
         }
 
 	cp.setName(name);
 
 	cp.setCallId(callId);
 
         cp.setVoiceDetection(true);
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 
	    //DefaultSpatializer spatializer = new DefaultSpatializer();

	    //spatializer.setFallOff(.95);

	    //voiceManager.setupCall(cp, 0, 0, 0, 0, spatializer, bridge);

 	    voiceManager.setupCall(cp, 0, 0, 0, 0, null, bridge);
 	    return callId;
 	} catch (IOException e) {
 	    logger.info("Unable to place call to " + cp.getPhoneNumber()
 		+ " " + e.getMessage());
 	} 
 
 	return null;
     }
     
     public void setSpatializer(String callId, Spatializer spatializer) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.setSpatializer(callId, spatializer);
     }
     
     public void setPrivateSpatializer(String targetCallId, String sourceCallId,
 	    Spatializer spatializer) {
 
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.setPrivateSpatializer(targetCallId, sourceCallId,
 	     spatializer);
     }
 
     public void removePrivateSpatializer(String targetCallId, String sourceCallId) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	voiceManager.setPrivateSpatializer(targetCallId, sourceCallId, null);
     }
 
     private static Object treatmentLock = new Object();
 
     /* Maps callId's to treatmentGroupId */
     private static Hashtable<String, String> treatmentCallIds;
 
     /* Maps treatmentGroupId's to treatmentGroups */
     private static Hashtable<String, Hashtable> treatmentGroups;
 
     /* Maps callId to treatment information */
     private static ConcurrentHashMap<String, TreatmentInfo> treatmentInfo =
 	new ConcurrentHashMap<String, TreatmentInfo>();;
 
     class TreatmentInfo {
 	public String treatment;
 	public String group;
 	public ManagedCallStatusListener listener;
 	public double lowerLeftX;
 	public double lowerLeftY; 
 	public double lowerLeftZ;
 	public double upperRightX;
 	public double upperRightY;
 	public double upperRightZ;
 
 	public TreatmentInfo(String treatment, String group,
                 ManagedCallStatusListener listener,
                 double lowerLeftX, double lowerLeftY, double lowerLeftZ,
                 double upperRightX, double upperRightY, double upperRightZ) {
 
 	    this.treatment = treatment;
 	    this.group = group;
 	    this.listener = listener;
 	    this.lowerLeftX = lowerLeftX;
 	    this.lowerLeftY = lowerLeftY;
 	    this.lowerLeftZ = lowerLeftZ;
 	    this.upperRightX = upperRightX;
 	    this.upperRightY = upperRightY;
 	    this.upperRightZ = upperRightZ;
 	}
     }
 
     private String setupTreatment(String id, TreatmentInfo t) {
 	return setupTreatment(id, t.treatment, t.group,
 	    t.listener, t.lowerLeftX, t.lowerLeftY, t.lowerLeftZ,
             t.upperRightX, t.upperRightY, t.upperRightZ);
     }
 
     public String setupTreatment(String id, String treatment, String group, 
 	    ManagedCallStatusListener listener,
 	    double lowerLeftX, double lowerLeftY, double lowerLeftZ, 
 	    double upperRightX, double upperRightY, double upperRightZ) {
 
 	logger.finer("setupTreatment:  id " + id + " treatment " + treatment
             + " group: " + group);
         
         if (treatment == null) {
             return null;
         }
         
 	String callId = id;
 
 	CallParticipant cp = new CallParticipant();
 
 	String conferenceId = System.getProperty(
 	    "com.sun.mpk20.gdcdemo.client.sample.CONFERENCE_ID",
 	    DEFAULT_CONFERENCE_ID);
 
 	cp.setConferenceId(conferenceId);
 
 	cp.setInputTreatment(getTreatmentFile(treatment));
 	cp.setName(id);
 	//cp.setVoiceDetection(true);
         
 	String treatmentGroupId = group;
 
 	cp.setCallId(callId);
 
 	if (listener != null) {
 	    addCallStatusListener(listener, callId);
 	}
 
         // get a spatializer
         Spatializer spatializer = null;
         
 	if (lowerLeftX != upperRightX) {
 	     spatializer = new AmbientSpatializer(
 		lowerLeftX, lowerLeftY, lowerLeftZ,
 		upperRightX, upperRightY, upperRightZ);
 	}
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 
 	    voiceManager.setupCall(cp, 0, 0, 0, 0, spatializer, null);
             voiceManager.setAttenuationVolume(callId, 1);
 	} catch (IOException e) {
 	    logger.info("Unable to place call to " + cp.getPhoneNumber()
 		+ " " + e.getMessage());
 
 	    return null;
 	} 
 
 	logger.finest("back from starting treatment...");
 
         DataManager dm = AppContext.getDataManager();
         
 	treatmentInfo.put(callId, new TreatmentInfo(treatment, group,
             listener, lowerLeftX, lowerLeftY, lowerLeftZ,
             upperRightX, upperRightY, upperRightZ));
 
 	synchronized (treatmentLock) {
 	    if (treatmentCallIds == null) {
 	        treatmentCallIds = new Hashtable<String, String>();
 	    }
 
 	    if (treatmentGroups == null) {
 	        treatmentGroups = new Hashtable<String, Hashtable>();
 	    }
 
             if (treatmentGroupId == null) {
                 treatmentGroupId = callId;
             }
             
 	    Hashtable treatmentGroup = treatmentGroups.get(treatmentGroupId);
 	
 	    if (treatmentGroup == null) {
 	        /*
 	         * This is a new treatment group
 	         */
 		logger.finer("creating new treatment group for " 
 		    + treatmentGroupId);
 
 	        treatmentGroup = new Hashtable<String, String>();
 	        treatmentGroups.put(treatmentGroupId, treatmentGroup);
 	    }
 
 	    logger.finest("Adding " + callId + " to treatment group " 
 		+ treatmentGroupId);
 
 	    treatmentCallIds.put(callId, treatmentGroupId);
 	    treatmentGroup.put(callId, new Boolean(false));
 	}
 
 	restartInputTreatments(treatmentGroupId);
 	return callId;
     }
 
     private void treatmentDone(String callId, boolean restart) {
 	TreatmentInfo t = treatmentInfo.get(callId);
 
 	treatmentDone(callId, t, restart);
     }
 
     private void treatmentDone(String callId, TreatmentInfo t, 
 	    boolean restart) {
 
 	logger.finest("treatment done " + callId + " restart " + restart);
 
 	if (treatmentCallIds == null) {
 	    return;   // there haven't been any treatments
 	}
 
 	if (t != null) {
 	    if (t.listener != null) {
  		removeCallStatusListener(t.listener);
 	    }
 	} else {
 	    logger.fine("callId " + callId + " treatment info is null");
 	}
 
 	/*
 	 * Check if all treatments in the group have finished.  
 	 * If so, restart all of them.
 	 */
 	String treatmentGroupId;
 
 	synchronized (treatmentLock) {
 	    treatmentGroupId = treatmentCallIds.get(callId);
 
 	    if (treatmentGroupId == null) {
 		logger.finest("call id not found for treatment done... " 
 		    + callId);
 		return; // call wasn't started here
 	    }
 	    
 	    Hashtable<String, Boolean> treatmentGroup = 
 		treatmentGroups.get(treatmentGroupId);
 
 	    /*
 	     * Treatment is now done
 	     */
 	    treatmentGroup.remove(callId);
 
 	    if (restart == true) {
 	        treatmentGroup.put(callId, new Boolean(true));
 	    } else {
 		treatmentCallIds.remove(callId);
 		treatmentInfo.remove(callId);
 	    }
 
 	    Enumeration<String> keys = treatmentGroup.keys();
 
 	    while (keys.hasMoreElements()) {
 		String id = keys.nextElement();
 
 		Boolean done = treatmentGroup.get(id);
 
 		if (done == false) {
 		    logger.finest("Waiting for other treatments to finish");
 		    return;	// still some treatments which haven't finished
 		}
 	    }	
 	}
 
 	restartInputTreatments(treatmentGroupId);
     }
 
     public void newInputTreatment(String callId, String treatment, 
 	    String group) {
 
 	logger.info("new treatment " + callId);
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 	    voiceManager.newInputTreatment(callId, 
                     getTreatmentFile(treatment));
 	} catch (IOException e) {
 	    logger.info("Unable to start new treatment " + callId
 		+ " " + e.getMessage());
 	}
     }
 
     public void stopInputTreatment(String callId) {
 	logger.finest("stop treatment" + callId);
 
 	treatmentDone(callId, false);  // don't restart treatment
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 	    voiceManager.stopInputTreatment(callId);
 	} catch (IOException e) {
 	    logger.info("Unable to end call " + callId
 		+ " " + e.getMessage());
 	}
     }
 
     public void endCall(String callId) {
 	logger.fine("Ending call " + callId);
 
         DataManager dm = AppContext.getDataManager();
 
 	try {
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 	    voiceManager.endCall(callId);
 	} catch (IOException e) {
 	    logger.fine("Unable to end call " + callId
 		+ " " + e.getMessage());
 	}
     }
 
     public void muteCall(String callId, boolean isMuted) {
 	if (isMuted) {
             logger.info("muting call " + callId);
 	} else {
             logger.info("unmuting call " + callId);
 	}
 
         try {
             VoiceManager voiceManager =
                 AppContext.getManager(VoiceManager.class);
             voiceManager.muteCall(callId, isMuted);
         } catch (IOException e) {
             logger.info("Unable to mute/unmute " + callId
                 + " " + e.getMessage());
         }
     }
 
     /*
      * Restart treatments in the group if there's more than one call
      */
     private void restartInputTreatments(String treatmentGroupId) {
 	logger.finer("Restarting input treatments for " + treatmentGroupId);
 
 	synchronized (treatmentLock) {
 	    Hashtable treatmentGroup = treatmentGroups.get(treatmentGroupId);
 
 	    Enumeration keys = treatmentGroup.keys();
 
 	    VoiceManager voiceManager = 
 		AppContext.getManager(VoiceManager.class);
 
 	    while (keys.hasMoreElements()) {
 	        String id = (String) keys.nextElement();
 
 	        try {
 		    logger.finer("Restarting input treatment for call " + id);
 	            voiceManager.restartInputTreatment(id);
 	        } catch (IOException e) {
 	            logger.warning("Unable to restart treatment for " + id
 		        + " " + e.getMessage());
 	        }
 	    }
 	}
     }
 
     /**
      * Resolve a treatment name into an absolute file name.  If the treatment
      * name does not start with the path separator, prepend the value of
      * the audioDir property to it to get the file name
      * @param treatment the unresolved treatment name
      * @return the resolved treatment name
      */
     private String getTreatmentFile(String treatment) {
         String ps = System.getProperty("file.separator");
         if (!treatment.startsWith(ps)) {
             treatment = audioDir + ps + treatment;
         }
         
         return treatment;
     }
     
     public void setFallOff(double fallOff) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
         voiceManager.getDefaultSpatializer().setFallOff(fallOff);
     }
 
     public void setFallOffFunction(String s) {
     }
 
     public void setFullVolumeRadius(double fullVolumeRadius) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 	voiceManager.getDefaultSpatializer().setFullVolumeRadius(fullVolumeRadius);
     }
 
     public void setZeroVolumeRadius(double zeroVolumeRadius) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 	voiceManager.getDefaultSpatializer().setZeroVolumeRadius(zeroVolumeRadius);
     }
 
     public void setMaximumVolume(double maximumVolume) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 	voiceManager.getDefaultSpatializer().setMaximumVolume(maximumVolume);
     }
 
     public void setSpatialAudio(boolean enabled) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	try {
 	    voiceManager.setSpatialAudio(enabled);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio: " + e.getMessage());
 	}
     }
 
     public void setSpatialMinVolume(double spatialMinVolume) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	try {
 	    voiceManager.setSpatialMinVolume(spatialMinVolume);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio min volume: " 
 		+ e.getMessage());
 	}
     }
 
     public void setSpatialFallOff(double spatialFallOff) {
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	try {
 	    voiceManager.setSpatialFallOff(spatialFallOff);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio fall off: " 
 		+ e.getMessage());
 	}
     }
 
     public void setSpatialEchoDelay(double spatialEchoDelay) {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setSpatialEchoDelay(spatialEchoDelay);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio echo delay: "
                 + e.getMessage());
         }
     }
 
     public void setSpatialEchoVolume(double spatialEchoVolume) {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setSpatialEchoVolume(spatialEchoVolume);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio echo volume: "
                 + e.getMessage());
         }
     }
 
     public void setSpatialBehindVolume(double spatialBehindVolume) {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setSpatialBehindVolume(spatialBehindVolume);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio behind volume: "
                 + e.getMessage());
         }
     }
 
     /*
      * Add a listener for all calls
      */
     public void addCallStatusListener(ManagedCallStatusListener listener) {
 	 addCallStatusListener(listener, null);
     }
 
     /*
      * Add a listener for a specific callId
      */
     public void addCallStatusListener(ManagedCallStatusListener listener, 
 	    String callId) {
 	
	logger.fine("Adding listener " + listener + " for callId " + callId);

         DataManager dm = AppContext.getDataManager();
 
         CallStatusListeners listeners =
             dm.getBinding(DS_CALL_STATUS_LISTENERS, CallStatusListeners.class);
 
         /*
          * Create a reference to listener and keep that.
          */
 	synchronized (listeners) {
             listeners.put(dm.createReference(listener), callId);
 	}
 
         logger.finest("VS:  listeners size " + listeners.size());
     }
 
     public void removeCallStatusListener(ManagedCallStatusListener listener) {
	logger.fine("removing listener " + listener); 

         DataManager dm = AppContext.getDataManager();
 
         CallStatusListeners listeners =
             dm.getBinding(DS_CALL_STATUS_LISTENERS, CallStatusListeners.class);
 
 	//ManagedCallStatusListener ml = listener.get(ManagedCallStatusListener.class);
 
 	synchronized (listeners) {
	    if (listeners.remove(listener) == null) {
		logger.info("listener " + listener 
		    + " is not in map of call status listeners!");
	    }
 	}
     }
 
     public void addCallBeginEndListener(ManagedCallBeginEndListener listener) {
         DataManager dm = AppContext.getDataManager();
 
         CallBeginEndListeners listeners =
             dm.getBinding(DS_CALL_BEGIN_END_LISTENERS, CallBeginEndListeners.class);
 
         /*
          * Create a reference to listener and keep that.
          */
 	synchronized (listeners) {
             listeners.add(dm.createReference(listener));
 	}
 
         logger.finest("VS:  listeners size " + listeners.size());
     }
 
     public void removeCallBeginEndListener(ManagedCallBeginEndListener listener) {
         DataManager dm = AppContext.getDataManager();
 
         CallBeginEndListeners listeners =
             dm.getBinding(DS_CALL_BEGIN_END_LISTENERS, CallBeginEndListeners.class);
 
 	//ManagedCallBeginEndListener ml =
         //        listener.get(ManagedCallBeginEndListener.class);
 
 	synchronized (listeners) {
 	    listeners.remove(listener);
 	}
     }
 
     public void callStatusChanged(CallStatus callStatus) {
 	int code = callStatus.getCode();
 
 	String callId = callStatus.getCallId();
 
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
         
 	notifyCallStatusListeners(callStatus);
 
 	switch (code) {
         case CallStatus.ESTABLISHED:
             logger.fine("callEstablished: " + callId);
 
             try {
 	        voiceManager.callEstablished(callId);
 	    } catch (IOException e) {
 		logger.info(e.getMessage());
 	    }
 	    notifyCallBeginEndListeners(callStatus);
             break;
 
         case CallStatus.STARTEDSPEAKING:
             break;
 
         case CallStatus.STOPPEDSPEAKING:
             break;
 
         case CallStatus.ENDED:
 	    logger.info(callStatus.toString());
 
 	    treatmentDone(callId, false);
 
 	    try {
 	        voiceManager.endCall(callId);
 	    } catch (IOException e) {
 	    }
 
 	    notifyCallBeginEndListeners(callStatus);
 	    break;
 
 	case CallStatus.TREATMENTDONE:
 	    logger.finer("Treatment done: " + callStatus);
 	    treatmentDone(callId, true);
 	    break;
 
 	case CallStatus.BRIDGE_OFFLINE:
 	    logger.info("Bridge offline: " + callStatus);
 	    
 	    /*
 	     * Treatments are automatically restarted here.
 	     * Clients are notified that the bridge went down
 	     * and they notify the softphone which then reconnects.
 	     */
 	    TreatmentInfo t = treatmentInfo.get(callId);
 
 	    if (t != null) {
 		logger.info("Restarting treatment " + t.treatment);
 		restartInputTreatments(t.group);
 	    } 
 
 	    /*
 	     * After the last bridge_offline call (callID=''),
 	     * we have to tell the voice manager to restore
 	     * all the pm's for live players.
 	     */
 	    if (callId.length() == 0) {
 		logger.info("Restoring private mixes...");
 
 		try {
 	    	    voiceManager.restorePrivateMixes();
 		} catch (IOException e) {
 	    	    logger.info("restorePrivateMixes failed:  "
 		        + e.getMessage());
 		}
 	    }
 	    break;
 
         }
     }
 
     private void notifyCallStatusListeners(CallStatus status) {
         DataManager dm = AppContext.getDataManager();
 
         CallStatusListeners callStatusListeners =
             dm.getBinding(DS_CALL_STATUS_LISTENERS, CallStatusListeners.class);
 
         ArrayList<ManagedReference> listenerList = new ArrayList<ManagedReference>();
 	ArrayList<String> callIdList = new ArrayList<String>();
 
         synchronized (callStatusListeners) {
 	    Set<ManagedReference> set = callStatusListeners.keySet();
 
             Iterator<ManagedReference> iterator = set.iterator();
 
 	    while (iterator.hasNext()) {
 		ManagedReference mr = iterator.next();
 
 		listenerList.add(mr);
 
 		callIdList.add(callStatusListeners.get(mr));
 	    }
 	}
 	
         for (int i = 0; i < listenerList.size(); i++) {
             ManagedCallStatusListener listener =
                 listenerList.get(i).get(ManagedCallStatusListener.class);
 
 	    String callId = callIdList.get(i);
 	    
 	    if (callId == null || callId.equals(status.getCallId())) {
                 listener.callStatusChanged(status);
 	    } 
         }
     }
 
     private void notifyCallBeginEndListeners(CallStatus status) {
         DataManager dm = AppContext.getDataManager();
 
         CallBeginEndListeners callBeginEndListeners =
             dm.getBinding(DS_CALL_BEGIN_END_LISTENERS, CallBeginEndListeners.class);
 
         ManagedReference[] listenerList;
 
         synchronized (callBeginEndListeners) {
             listenerList = callBeginEndListeners.toArray(new ManagedReference[0]);
         }
 
         for (int i = 0; i < listenerList.length; i++) {
             ManagedCallBeginEndListener listener =
                 listenerList[i].get(ManagedCallBeginEndListener.class);
 
             logger.finest("Notifying listener " + i + " status " + status);
             listener.callBeginEndNotification(status);
         }
     }
 
     public void setPosition(String callId, double x, double y, double z) {
 	logger.finest("setPosition for " + callId 
 	    + " (" + (Math.round(x * 1000) / 1000.) 
 	    + ", " + (Math.round(y * 1000) / 1000.)
 	    + ", " + (Math.round(z * 1000) / 1000.) + ")");
 
 	VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
 	try {
 	    voiceManager.setPosition(callId, x / scale, y / scale, z / scale);
 	} catch (IOException e) {
 	    logger.info("callId " + callId + " setPosition failed:  "
 		+ e.getMessage());
 	}
     }
 
     public void setOrientation(String callId, double orientation) {
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setOrientation(callId, orientation);
         } catch (IOException e) {
             logger.info("callId " + callId + " setOrientation failed:  "
                 + e.getMessage());
         }
     }
 
     public void setAttenuationRadius(String callId,
 	    double attenuationRadius) {
 
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setAttenuationRadius(callId, attenuationRadius);
         } catch (IOException e) {
             logger.info("callId " + callId + " setAttenuationRadius failed:  "
                 + e.getMessage());
         }
     }
 
     public void setAttenuationVolume(String callId,
 	    double attenuationVolume) {
 
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.setAttenuationVolume(callId, attenuationVolume);
         } catch (IOException e) {
             logger.info("callId " + callId + " setAttenuationVolume failed:  "
                 + e.getMessage());
         }
     }
 
     public void addWall(double startX, double startY, double endX,
 	    double endY, double characteristic) {
 
         VoiceManager voiceManager = AppContext.getManager(VoiceManager.class);
 
         try {
             voiceManager.addWall(startX, startY, endX, endY, characteristic);
         } catch (IOException e) {
             logger.info(" addWall failed:  " + e.getMessage());
         }
     }
 
     static class AmbientSpatializer implements Spatializer {
         double minX;
         double maxX;
         double minY;
         double maxY;
         double minZ;
         double maxZ;
 
         public AmbientSpatializer(double lowerLeftX, double lowerLeftY,
 	 	double lowerLeftZ, double upperRightX,
 		double upperRightY, double upperRightZ) {
 
 	    setBounds(lowerLeftX, lowerLeftY, lowerLeftZ,
 		upperRightX, upperRightY, upperRightZ);
         }
         
 	public void setBounds(double lowerLeftX, double lowerLeftY,
 	 	double lowerLeftZ, double upperRightX,
 		double upperRightY, double upperRightZ) {
 
 	    //logger.finest("lX " + lowerLeftX + " lY " + lowerLeftY
 	    //	+ " lZ " + lowerLeftZ + " uX " + upperRightX
 	    //	+ " uY " + upperRightY + " uZ " + upperRightZ);
 
             minX = Math.min(lowerLeftX / scale, upperRightX / scale);
             maxX = Math.max(lowerLeftX / scale, upperRightX / scale);
             minY = Math.min(lowerLeftY / scale, upperRightY / scale);
             maxY = Math.max(lowerLeftY / scale, upperRightY / scale);
             minZ = Math.min(lowerLeftZ / scale, upperRightZ / scale);
             maxZ = Math.max(lowerLeftZ / scale, upperRightZ / scale);
 	}
 
         public double[] spatialize(double sourceX, double sourceY, 
                                    double sourceZ, double sourceOrientation, 
                                    double destX, double destY, 
                                    double destZ, double destOrientation)
         {
             // see if the destination is inside the ambient audio range
             if (isInside(destX, destY, destZ)) {
 		//logger.finest("inside min (" + round(minX) + ", " + round(minY) 
 		//  + ", " + round(minZ) + ") "
 		//  + " max (" + round(maxX) + ", " + round(maxY) + ", " 
 		//  + round(maxZ) + ") " + " dest (" + round(destX) + ", " 
 		//  + round(destY) + ", " + round(destZ) + ")");
                 return new double[] { 0, 0, 0, 1 };
             } else {
 		//logger.info("outside min (" + round(minX) + ", " + round(minY) 
 		//  + ", " + round(minZ) + ") "
 		//  + " max (" + round(maxX) + ", " + round(maxY) + ", " 
 		//  + round(maxZ) + ") " + " dest (" + round(destX) + ", " 
 		//  + round(destY) + ", " + round(destZ) + ")");
                 return new double[] { 0, 0, 0, 0 };
             }
         }
         
 	private boolean isInside(double x, double y, double z) {
 	    //logger.info("isInside: x " + round(x) + " y " + round(y) 
 	    //	+ " z " + z
 	    //	+ " minX " + round(minX) + " maxX " + round(maxX)
 	    //	+ " minY " + round(minY) + " maxY " + round(maxY)
 	    //	+ " minZ " + minZ + " maxZ " + maxZ); 
 
 	    /*
 	     * Don't check z because we always expect 0, but it's always
 	     * non-zero.
 	     */
             return x >= minX && x <= maxX &&
                y >= minY && y <= maxY;
 	}
     }
 
     private static double round(double d) {
 	return Math.round(d * 100) / 100.;
     }
 
 }
