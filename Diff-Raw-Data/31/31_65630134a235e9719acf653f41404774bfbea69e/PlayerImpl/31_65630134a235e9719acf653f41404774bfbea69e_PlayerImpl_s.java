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
 
 import java.math.BigInteger;
 
 import java.io.IOException;
 import java.io.Serializable;
 
 import java.lang.Integer;
 
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.sun.mpk20.voicelib.app.AudioGroup;
 import com.sun.mpk20.voicelib.app.AudioGroupPlayerInfo;
 import com.sun.mpk20.voicelib.app.AudioGroupSetup;
 import com.sun.mpk20.voicelib.app.AudioSink;
 import com.sun.mpk20.voicelib.app.AudioSource;
 import com.sun.mpk20.voicelib.app.BridgeInfo;
 import com.sun.mpk20.voicelib.app.Call;
 import com.sun.mpk20.voicelib.app.DefaultSpatializer;
 import com.sun.mpk20.voicelib.app.DefaultSpatializers;
 import com.sun.mpk20.voicelib.app.FullVolumeSpatializer;
 import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
 import com.sun.mpk20.voicelib.app.Player;
 import com.sun.mpk20.voicelib.app.PlayerSetup;
 import com.sun.mpk20.voicelib.app.Spatializer;
 import com.sun.mpk20.voicelib.app.Util;
 import com.sun.mpk20.voicelib.app.VirtualPlayer;
 import com.sun.mpk20.voicelib.app.VoiceManager;
 import com.sun.mpk20.voicelib.app.VoiceService;
 import com.sun.mpk20.voicelib.app.VoiceManagerParameters;
 import com.sun.mpk20.voicelib.app.ZeroVolumeSpatializer;
 import com.sun.mpk20.voicelib.app.Wall;
 
 import com.sun.voip.client.connector.CallStatus;
 import com.sun.voip.client.connector.CallStatusListener;
 
 import java.awt.geom.Line2D;
 
public class PlayerImpl implements Player, CallStatusListener {
 
     private static final Logger logger =
         Logger.getLogger(PlayerImpl.class.getName());
 
     private static final double ZERO_VOLUME = .009;
 
     private PlayerSetup setup;
 
     private String id;
 
     private Call call;
 
     private AudioSink audioSink;
 
     private AudioSource audioSource;
 
     /*
      * We would like to use the Cartesian Coordinate system.
      * However, with Darkstar, x is positive to the left in stead of the right.
      * Also, y and z are reversed.
      */
     private double x;
  
     private double y;
 
     private double z;
 
     private double orientation;  
 
     private boolean isRecording;
 
     private double masterVolume = 1.0;
 
     private Spatializer publicSpatializer;
 
     private CopyOnWriteArrayList<AudioGroup> audioGroups = new CopyOnWriteArrayList();
 
     private CopyOnWriteArrayList<VirtualPlayer> virtualPlayers = new CopyOnWriteArrayList();
 
     private ConcurrentHashMap<String, Spatializer> privateSpatializers =
 	new ConcurrentHashMap<String, Spatializer>();
 
     private CopyOnWriteArrayList<Player> playersInRange = new CopyOnWriteArrayList<Player>();
 
     private static double scale;
 
     static {
 	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
         scale =  vm.getScale();
     }
 
     public PlayerImpl(String id, PlayerSetup setup) {
 	this.id = id;
 	this.setup = setup;
 
 	setup.x = -setup.x;
 
 	publicSpatializer = setup.publicSpatializer;
 
 	logger.info("creating player for " + id
             + " at (" + setup.x + ", " + setup.y + ", " + setup.z + ": "
 	    + setup.orientation + ")" + " setup.isOutworlder " + setup.isOutworlder);
 
 	setPosition(setup.x, setup.y, setup.z);
 	setOrientation(setup.orientation);
 
 	AppContext.getManager(VoiceManager.class).addCallStatusListener(this, id);
     }
 
     public String getId() {
 	return id;
     }
 
     public PlayerSetup getSetup() {
 	return setup;
     }
 
     public void removePlayer() {
 	VoiceManager vm = AppContext.getManager(VoiceManager.class);
 
         ConcurrentHashMap<String, AudioGroup> audioGroups = vm.getAudioGroups();
 
 	Collection <AudioGroup> values = audioGroups.values();
 
 	Iterator<AudioGroup> it = values.iterator();
 
 	while (it.hasNext()) {
 	    it.next().removePlayer(this);
 	}
 
 	for (VirtualPlayer vp : virtualPlayers) {
              vm.removePlayer(vp.player.getId());
         }
 
         virtualPlayers.clear();
     }
 
     public void setCall(Call call) {
 	this.call = call;
 
 	setPrivateMixes(true);
     }
 
     public Call getCall() {
 	return call;
     }
 
     public void addVirtualPlayer(VirtualPlayer vp) {
 	virtualPlayers.add(vp);
     }
 
     public void removeVirtualPlayer(VirtualPlayer vp) {
 	virtualPlayers.remove(vp);
     }
 
     public CopyOnWriteArrayList<VirtualPlayer> getVirtualPlayers() {
 	return virtualPlayers;
     }
 
     public AudioSink getAudioSink() {
 	return audioSink;
     }
 
     public void setAudioSink(AudioSink AudioSink) {
 	this.audioSink = audioSink;
     }
 
     public AudioSource getAudioSource() {
 	return audioSource;
     }
 
     public void setAudioSource(AudioSource AudioSource) {
 	this.audioSource = audioSource;
     }
 
     public double getX() {
 	return x;
     }
 
     public double getY() {
 	return y;
     }
 
     public double getZ() {
 	return z;
     }
 
     public double getOrientation() {
 	return orientation;
     }
 
     public void moved(double x, double y, double z, double orientation) {
 	boolean positionChanged;
 
 	x = -x;
 
 	positionChanged = getX() != x || getY() != y || getZ() != z;
 
 	logger.finest("Player " + this + " moved to " + x + ":" + y + ":" + z
 	    + " orientation " + orientation + " positionChanged = " + positionChanged);
 
 	setPosition(x, y, z);
 	setOrientation(orientation);
 	setPrivateMixes(positionChanged);
     }
 
     private void setPosition(double x, double y, double z) {
     	this.x = Util.round100(x / scale);
 	this.y = Util.round100(y / scale);
 	this.z = Util.round100(z / scale);
     }
 
     public boolean samePosition(double x, double y, double z) {
     	return getX() == Util.round100(x) && getY() == Util.round100(y) &&
 	    getZ() == Util.round100(z);
     }
 	
     private void setOrientation(double orientation) {
 	orientation = Util.round100(Math.toRadians(orientation));
 
 	this.orientation = orientation;
     }
 
     public boolean sameOrientation(double orientation) {
 	orientation = Util.round100(Math.toRadians(orientation));
 
 	return getOrientation() == orientation;
     }
 
     public void setRecording(boolean isRecording) {
 	this.isRecording = isRecording;
     }
 
     public boolean isRecording() {
 	return isRecording;
     }
 
     public void setPublicSpatializer(Spatializer spatializer) {
 	this.publicSpatializer = publicSpatializer;
     }
 
     public Spatializer getPublicSpatializer() {
 	return publicSpatializer;
     }
 
     public void setPrivateSpatializer(Player player, Spatializer spatializer) {
 	if (spatializer == null) {
 	    privateSpatializers.remove(player.getId());
 	    return;
 	}
 
 	privateSpatializers.put(player.getId(), spatializer);
 	setPrivateMixes(true);
     }
 
     public Spatializer getPrivateSpatializer(Player player) {
 	return privateSpatializers.get(player.getId());
     }
 
     public void removePrivateSpatializer(Player player) {
 	privateSpatializers.remove(player.getId());
     }
 
     public void setMasterVolume(double masterVolume) {
 	this.masterVolume = masterVolume;
     }
 
     public double getMasterVolume() {
 	return masterVolume;
     }
 
     public void addAudioGroup(AudioGroup audioGroup) {
 	if (audioGroups.contains(audioGroup) == false) {
 	    audioGroups.add(audioGroup);
 	} else {
 	    logger.warning(this + " is aleady in audio group " + audioGroup);
 	}
 
 	updateAttenuation();
 	setPrivateMixes(true);
     }
 
     public void removeAudioGroup(AudioGroup audioGroup) {
 	if (audioGroups.contains(audioGroup)) {
 	    audioGroups.remove(audioGroup);
 	    logger.warning("removed " + this + " from " + audioGroup);
 	}
 
 	updateAttenuation();
     }
 
     private void updateAttenuation() {
 	AudioGroup privateAudioGroup = null;
 
 	for (AudioGroup audioGroup : audioGroups) {
 	    if (audioGroup.getPlayerInfo(this).chatType == AudioGroupPlayerInfo.ChatType.PUBLIC == false) {
 		privateAudioGroup = audioGroup;
 		logger.warning(this + " belongs to private audio group " + audioGroup);
 		break;
 	    } 
 	}
 
 	if (privateAudioGroup != null) {
 	    attenuateOtherGroups(privateAudioGroup, 0,
 		AudioGroup.MINIMAL_LISTEN_ATTENUATION);
 	} else {
 	    attenuateOtherGroups(null, 0, 0);
 	}
     }
 
     public void attenuateOtherGroups(AudioGroup audioGroup, double speakingAttenuation,
 	    double listenAttenuation) {
 
 	boolean inExclusiveAudioGroup = getExclusiveAudioGroup() != null;
 
 	for (AudioGroup ag : audioGroups) {
 	    if (audioGroup != null && ag.equals(audioGroup)) {
 		continue;
 	    }
 
 	    AudioGroupPlayerInfo info = ag.getPlayerInfo(this);
 
 	    if (info == null) {
 	  	logger.warning("info for " + this + " is null for audio group " + ag); 
 		continue;
 	    }
 
 	    if (inExclusiveAudioGroup) {
 	        info.speakingAttenuation = 0;
 	        info.listenAttenuation = 0;
 		logger.finest("group " + audioGroup + " " + this 
 		    + " in exclusive group, attenuation to 0 " + ag);
 		continue;
 	    }
 
 	    if (audioGroup != null) {
 	        info.speakingAttenuation = speakingAttenuation;
 	        info.listenAttenuation = listenAttenuation;
 		logger.finest("group " + audioGroup + " " + this 
 		    + " setting listen attenuation to " 
 		    + listenAttenuation + " " + ag);
 	    } else {
 	        info.speakingAttenuation = info.defaultSpeakingAttenuation;
 	        info.listenAttenuation = info.defaultListenAttenuation;
 	    }
 
 	    logger.warning(this + " group " + audioGroup + " speakingAttenuation " 
 		+ speakingAttenuation + " listenAttenuation " + listenAttenuation);
 	}
 
 	setPrivateMixes(true);
     }
 
     private AudioGroup getExclusiveAudioGroup() {
 	for (AudioGroup audioGroup : audioGroups) {
 	    if (audioGroup.getPlayerInfo(this).chatType == AudioGroupPlayerInfo.ChatType.EXCLUSIVE) {
 		return audioGroup;
 	    }
 	}
 
 	return null;
     }
 
     public CopyOnWriteArrayList<AudioGroup> getAudioGroups() {
 	return audioGroups;
     }
 
     public boolean isInRange(Player p) {
 	return playersInRange.contains(p);
     }
 
     public void addPlayerInRange(Player p) {
 	if (playersInRange.contains(p)) {
 	    logger.warning("playersInRange already contains " + p);
 	    return;
 	}
 	playersInRange.add(p);
     }
 
     public void removePlayerInRange(Player p) {
 	if (playersInRange.contains(p) == false) {
 	    logger.warning("playersInRange doesn't contain " + p);
 	    return;
 	}
 	playersInRange.remove(p);
     }
 
     private long timeToSpatialize;
 
     private long timeToSetMixes;
  
     private int numberOfPrivateMixesSet;
 
     private int skipped;
 
     private Integer lock = new Integer(0);
 
     public void setPrivateMixes(boolean positionChanged) {
 	//Adjust private mixes for all calls
 	//For each call, we have to determine where it is
 	// in 3-space relative to each other call
 	// and set the private mix accordingly.
 
 	long startTime = System.nanoTime();
 
         Player[] playersArray = 
 	    AppContext.getManager(VoiceManager.class).getPlayers().values().toArray(new Player[0]);
 
 	logger.finer("Players " + playersArray.length + " changed " + this);
 
 	/*
 	 * We only need to adjust the private mixes between the
 	 * changed player and other calls.
 	 */
 	for (int i = 0; i < playersArray.length; i++) {
 	    Player p1 = playersArray[i];
 
 	    if (p1 == this) {
 		skipped++;
 		continue;
 	    }
 
 	    /*
 	     * If we didn't change position,
 	     * then the mix for other players won't be affected
 	     * because our player's orientation has
 	     * no effect on the direction other player's hear
 	     * the our player's sound coming from.
 	     */
 	    if (positionChanged == true &&
 		    (p1.getSetup().isLivePlayer == true || p1.isRecording())) {
 
 		/*
 		 * Set the private mix p1 has for us
 		 */
 		setPrivateMix(p1, this);
 	    } else {
 		skipped++;
 	    }
 
 	    if (getSetup().isLivePlayer == false && isRecording() == false) {
 		/*
 		 * Only live players have private mixes
 		 */
 		skipped++;
 	    } else {
 		/*
 		 * Set the private mix we have for p1
 		 */
 		setPrivateMix(this, p1);
 	    }
 	}
 
 	if (logger.isLoggable(Level.FINE) == false) {
 	    return;
 	}
 
 	long now = System.nanoTime();
 
 	//synchronized (lock) {
 	    timeToSetMixes += (now - startTime);
 
 	    if (numberOfPrivateMixesSet < 1000) {
 		return;
 	    }
 
 	    double elapsed = timeToSetMixes / 1000000000.;
 
 	    double avg = elapsed / numberOfPrivateMixesSet;
 
 	    logger.info("elapsed " + elapsed + " avg time to set " 
 		+ numberOfPrivateMixesSet + " mixes "
 		+ avg + ", number of players " + playersArray.length 
 		+ ", avg time to spatialize " 
 		+ (timeToSpatialize / 1000000000. / 
 		  (numberOfPrivateMixesSet + skipped))
 		+ ", out of range " + skipped);
 
 	    numberOfPrivateMixesSet = 0;
 	    timeToSetMixes = 0;
 	    timeToSpatialize = 0;
 	    skipped = 0;
 	//}
     }
 
     /*
      * Set the private mix p1 has for p2
      */
     private void setPrivateMix(Player p1, Player p2) {
 	logger.finest("setting pm which " + p1 + " has for " + p2);
 
 	if (p1.getCall() == null || p2.getCall() == null) {
 	    logger.warning("Can't set pm " + p1 + " has for " + p2
 		+ " p1 call " + p1.getCall() + " p2 call " + p2.getCall());
 	    return;
 	}
 
 	if (p1.getCall().getId().equals(p2.getCall().getId())) {
 	    return;
 	}
 
 	double volume = p1.spatialize(p2);
 
 	if (volume == 0) {
 	    if (p1.isInRange(p2) == false) {
 		/*
 		 * This is an optimization.  p2 was not in range
 		 * and we already knew that.
 		 */
 	        logger.finest("pmx for " + p1 + ": "
 	            + p2 + " is not in range."); 
 
 		skipped++;
 		return;
 	    }
 
 	    logger.finest("pmx for " + p1 + ": "
 	        + p2 + " no longer in range."); 
 
 	    p1.removePlayerInRange(p2);   // p2 is not in range any more
 	} else {
 	    if (p1.isInRange(p2) == false) {
 	    	logger.finest("pmx for " + p1 + ": "
 	            + p2 + " setting in range."); 
 
 		p1.addPlayerInRange(p2);  // p2 is in range now
 	    }
 	}
     }
 
     /*
      * For each AudioGroup in which this Player is a member,
      * determine if we need to set a private mix for other
      * players in the AudioGroup.
      */
     public double spatialize(Player p) {
         VoiceService backingManager =
             AppContext.getManager(VoiceManager.class).getBackingManager();
 
 	CopyOnWriteArrayList<AudioGroup> groups = p.getAudioGroups();
 
 	//logger.warning("groups size " + audioGroups.size() + " for " + this);
 	//logger.warning("groups size " + groups.size() + " for " + p);
 
 	double[] privateMixParameters = new double[4];
 
 	if (p.getCall().isMuted() == false) {
 	    for (AudioGroup audioGroup : audioGroups) {
 
 	        //logger.warning("ag " + audioGroup);
 
 	        if (groups.contains(audioGroup) == false) {
 		    logger.finest(p + " not in audio group " + audioGroup + " of " + this);
 		    continue;
 	        }
 	
 	        AudioGroupPlayerInfo info = audioGroup.getPlayerInfo(p);
 
 	        if (info.isSpeaking == false) {
 		    logger.fine(this + "::: " + p + " not speaking in " + audioGroup);
 		    continue;  // p is not speaking in the group
 	        }
 
 		Spatializer spatializer = audioGroup.getSetup().spatializer;
 
 		if (p.getPublicSpatializer() != null) {
 		    spatializer = p.getPublicSpatializer();
 		}
 
 	        double[] pmp = spatializer.spatialize(
 	            p.getX(), p.getY(), p.getZ(), p.getOrientation(), 
 		    getX(), getY(), getZ(), getOrientation());
 
 	        if (pmp[3] > privateMixParameters[3]) {
 	            logger.finest("group " + audioGroup + " " + this + " has pm for " + p 
 		        + " vol " + privateMixParameters[3] + " ag " + audioGroup
 		        + " la " + audioGroup.getPlayerInfo(this).listenAttenuation + " sap "
 		        + info.speakingAttenuation + " pmp[3] " + pmp[3]);
 
 		    privateMixParameters = pmp;
 	        }
 
 	        if (privateMixParameters[3] != 0) {
 	            privateMixParameters[3] *= 
 		        audioGroup.getPlayerInfo(this).listenAttenuation * info.speakingAttenuation;
 	        }
 	    }
 	} else {
 	    logger.finest(p + " is Muted");
 	}
 
 	logger.finest("pm vol is " + privateMixParameters[3]);
 
 	if (privateMixParameters[3] != 0) {
 	    /*
 	     * Only use a private spatializer if we would have heard something
 	     * from p.
 	     */
 	    Spatializer spatializer = getPrivateSpatializer(p);
 
 	    if (spatializer != null) {
 	        privateMixParameters = spatializer.spatialize(
 	            p.getX(), p.getY(), p.getZ(), p.getOrientation(), 
 		    getX(), getY(), getZ(), getOrientation());
 	    } else {
 		privateMixParameters[3] *= getWallAttenuation(p);
 	    }
 
 	    privateMixParameters[3] *= masterVolume;
 	} else {
 	    if (isInRange(p) == false) {
 		return 0;	// it's not in range and we already knew it
 	    }
 	}
 
         if (privateMixParameters[3] > .1) {
             logger.finest("this=" + this + " p=" + p + " mix "
                 + Util.round100(privateMixParameters[0]) + ", "
                 + Util.round100(privateMixParameters[1]) + ", "
                 + Util.round100(privateMixParameters[2]) + ", "
                 + Util.round100(privateMixParameters[3]));
         }
 
         try {
             backingManager.setPrivateMix(getCall().getId(), p.getCall().getId(), 
 		privateMixParameters);
         } catch (IOException e) {
             logger.info("Unable to set private mix " + this
                 + " has for " + p + " " + e.getMessage());
         }
 
 	numberOfPrivateMixesSet++;
 
 	return privateMixParameters[3];
     }
 
     private double getWallAttenuation(Player p2) {
 	double wallAttenuation = 1.0;
 
 	for (Wall wall : walls) {
 	    if (Line2D.linesIntersect(x, y, p2.getX(), p2.getY(), 
 		wall.startX, wall.startY, wall.endX, wall.endY)) {
 
 		logger.finest(wall + " between " + this + " and " + p2);
 
 		return wall.characteristic;
 	    }
 	}
 
 	return wallAttenuation;
     }
 
     public void setSpatialAudio(boolean enabled) {
 	try {
 	    VoiceService backingManager =  AppContext.getManager(VoiceManager.class).getBackingManager();
 	    backingManager.setSpatialAudio(enabled);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio: " + e.getMessage());
 	}
     }
 
     public void setSpatialMinVolume(double spatialMinVolume) {
 	try {
 	    VoiceService backingManager =  AppContext.getManager(VoiceManager.class).getBackingManager();
 	    backingManager.setSpatialMinVolume(spatialMinVolume);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio min volume: " 
 		+ e.getMessage());
 	}
     }
 
     public void setSpatialFalloff(double spatialFalloff) {
 	try {
 	    VoiceService backingManager =  AppContext.getManager(VoiceManager.class).getBackingManager();
 	    backingManager.setSpatialFalloff(spatialFalloff);
 	} catch (IOException e) {
 	    logger.warning("Unable to set spatial audio fall off: " 
 		+ e.getMessage());
 	}
     }
 
     public void setSpatialEchoDelay(double spatialEchoDelay) {
         try {
 	    VoiceService backingManager =  AppContext.getManager(VoiceManager.class).getBackingManager();
             backingManager.setSpatialEchoDelay(spatialEchoDelay);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio echo delay: "
                 + e.getMessage());
         }
     }
 
     public void setSpatialEchoVolume(double spatialEchoVolume) {
         try {
 	    VoiceService backingManager =  AppContext.getManager(VoiceManager.class).getBackingManager();
             backingManager.setSpatialEchoVolume(spatialEchoVolume);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio echo volume: "
                 + e.getMessage());
         }
     }
 
     public void setSpatialBehindVolume(double spatialBehindVolume) {
         try {
 	    VoiceService backingManager =  AppContext.getManager(VoiceManager.class).getBackingManager();
             backingManager.setSpatialBehindVolume(spatialBehindVolume);
         } catch (IOException e) {
             logger.warning("Unable to set spatial audio behind volume: "
                 + e.getMessage());
         }
     }
 
 
     private static CopyOnWriteArrayList<Wall> walls = new CopyOnWriteArrayList<Wall>();
 
     public void addWall(double startX, double startY, double endX, double endY,
         double characteristic) {
 
         //synchronized (walls) {
             walls.add(new Wall(startX, startY, endX, endY, characteristic));
         //}
 
         setPrivateMixes(true);
     }
 
     public void setDefaultSpatializers(DefaultSpatializers defaultSpatializers) {
     }
 
     private DefaultSpatializers defaultSpatializers = new DefaultSpatializers();
 
     public DefaultSpatializers getDefaultSpatializers() {
         return defaultSpatializers;
     }
 
     public void setDefaultSpatializers(DefaultSpatializers defaultSpatializers,
             double startX, double startY, double endX, double endY) {
 
     }
 
     public DefaultSpatializers getDefaultSpatializers(
             double startX, double startY, double endX, double endY) {
 
         return null;
     }
 
     public int getNumberOfPlayersInRange() {
 	return AppContext.getManager(VoiceManager.class).getNumberOfPlayersInRange(
 	    x, y, z);
     }
 
     public void callStatusChanged(CallStatus callStatus) {
 	int code = callStatus.getCode();
 
 	String callId = callStatus.getCallId();
 
 	switch (code) {
         case CallStatus.ESTABLISHED:
         case CallStatus.MIGRATED:
             logger.fine("callEstablished: " + callId);
  
  	    setPrivateMixes(true);
             break;
 
 	case CallStatus.ENDED:
 	    if (callId == null || id.equals(callId) == false) {
 		return;
 	    }
 
 	    //removePlayer();
 	    break;
         }
     }
 
     public String dump() {
 	String s = "  " + toString() + " masterVolume " + masterVolume;
 	
 	s += "\n    Audio Groups: ";
 
 	for (AudioGroup audioGroup : audioGroups) {
 	    s += " " + audioGroup.getId();
 	}
 
 	if (privateSpatializers.size() > 0) {
 	    s += "\n    Private Spatializers:";
 
 	    Enumeration<String> keys = privateSpatializers.keys();
 
 	    while (keys.hasMoreElements()) {
 		s += " " + keys.nextElement();
 	    }
 	}
 	
 	if (virtualPlayers.size() > 0) {
 	    s = "\n    Virtual Players:  ";
 
 	    for (VirtualPlayer vp : virtualPlayers) {
 		s += " " + vp.player.getId();
 	    }
 	}
 
 	return s;
     }
 
     public boolean equals(Object o) {
 	if (o instanceof Player == false) {
 	    return false;
 	}
 
 	Player p = (Player) o;
 
 	return id.equals(p.getId());
     }
 
     public String toString() {
 	double xR = Util.round100(x);
 	double yR = Util.round100(y);
 	double zR = Util.round100(z);
 
 	int a = ((int) Math.toDegrees(orientation)) % 360;
 
 	if (a < 0) {
 	    a += 360;
 	}
 
 	return getId() + ":(" + xR + "," + yR + "," + zR + "," + a + ")" 
 	    + " " + (setup.isLivePlayer ? "LivePlayer" : "") 
 	    + (publicSpatializer != null ? publicSpatializer.toString() : "");
     }
 
 }
