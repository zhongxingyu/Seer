 /*
 CallManager.java
 Copyright (C) 2010  Belledonne Communications, Grenoble, France
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 package org.linphone;
 
 import org.linphone.core.LinphoneAddress;
 import org.linphone.core.LinphoneCall;
 import org.linphone.core.LinphoneCallParams;
 import org.linphone.core.LinphoneCore;
 import org.linphone.core.LinphoneCoreException;
 import org.linphone.core.video.AndroidCameraRecordManager;
 
 /**
  * Handle call updating, reinvites.
  * 
  * @author Guillaume Beraudo
  *
  */
 class CallManager {
 
 	private static CallManager instance;
 	
 	private CallManager() {}
 	public static final synchronized CallManager getInstance() {
 		if (instance == null) instance = new CallManager();
 		return instance;
 	}
 	
 	private AndroidCameraRecordManager videoManager() {
 		return AndroidCameraRecordManager.getInstance();
 	}
 	private BandwidthManager bm() {
 		return BandwidthManager.getInstance();
 	}
 	
 
 	
 	
 	void inviteAddress(LinphoneAddress lAddress, boolean videoEnabled) throws LinphoneCoreException {
 		LinphoneCore lc = LinphoneManager.getLc();
 		
 		LinphoneCallParams params = lc.createDefaultCallParameters();
 		bm().updateWithProfileSettings(lc, params);
 
 		if (videoEnabled && params.getVideoEnabled()) {
 			videoManager().setMuted(false);
 			params.setVideoEnabled(true);
 		} else {
 			params.setVideoEnabled(false);
 		}
 
 		lc.inviteAddressWithParams(lAddress, params);
 	}
 
 
 
 	
 	/**
 	 * Add video to a currently running voice only call.
 	 * No re-invite is sent if the current call is already video
 	 * or if the bandwidth settings are too low.
 	 * @return if updateCall called
 	 */
 	boolean reinviteWithVideo() {
 		LinphoneCore lc =  LinphoneManager.getLc();
 		LinphoneCall lCall = lc.getCurrentCall();
 		LinphoneCallParams params = lCall.getCurrentParamsCopy();
 
 		if (params.getVideoEnabled()) return false;
 		
 
 		// Check if video possible regarding bandwidth limitations
 		bm().updateWithProfileSettings(lc, params);
 		if (!params.getVideoEnabled()) {
 			return false;
 		}
 
 		// Not yet in video call: try to re-invite with video
 		params.setVideoEnabled(true);
 		videoManager().setMuted(false);
 		lc.updateCall(lCall, params);
 		return true;
 	}
 
 
 	
 	/**
 	 * Re-invite with parameters updated from profile.
 	 */
 	void reinvite() {
 		LinphoneCore lc = LinphoneManager.getLc();
 		LinphoneCall lCall = lc.getCurrentCall();
 		LinphoneCallParams params = lCall.getCurrentParamsCopy();
 		bm().updateWithProfileSettings(lc, params);
 		lc.updateCall(lCall, params);
 	}
 
 	/**
 	 * Update current call, without reinvite.
 	 * The camera will be restarted when mediastreamer chain is recreated and setParameters is called.
 	 */
 	void updateCall() {
 		LinphoneCore lc = LinphoneManager.getLc();
 		LinphoneCall lCall = lc.getCurrentCall();
 		LinphoneCallParams params = lCall.getCurrentParamsCopy();
 		bm().updateWithProfileSettings(lc, params);
 		lc.updateCall(lCall, null);
 	}
 	
 }
