 /*
  * Copyright (C) 2009 The Sipdroid Open Source Project
  * Copyright (C) 2008 Hughes Systique Corporation, USA (http://www.hsc.com)
  * 
  * This file is part of Sipdroid (http://www.sipdroid.org)
  * 
  * Sipdroid is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This source code is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this source code; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.sipdroid.sipua;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import org.sipdroid.net.KeepAliveSip;
 import org.sipdroid.sipua.ui.LoopAlarm;
 import org.sipdroid.sipua.ui.Receiver;
 import org.sipdroid.sipua.ui.Sipdroid;
 import org.zoolu.net.IpAddress;
 import org.zoolu.net.SocketAddress;
 import org.zoolu.sip.address.NameAddress;
 import org.zoolu.sip.provider.SipProvider;
 import org.zoolu.sip.provider.SipStack;
 
 import android.content.Context;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 
 public class SipdroidEngine implements RegisterAgentListener {
 
 	public static final int UNINITIALIZED = 0x0;
 	public static final int INITIALIZED = 0x2;
 
 	/** User Agent */
 	private UserAgent ua;
 
 	/** Register Agent */
 	private RegisterAgent ra;
 
 	/** UserAgentProfile */
 	private UserAgentProfile user_profile;
 
 	private SipProvider sip_provider;
 	
 	PowerManager.WakeLock wl;
 	
 	public boolean StartEngine() {
 		try {
 			PowerManager pm = (PowerManager) getUIContext().getSystemService(Context.POWER_SERVICE);
 			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sipdroid");
 
 			String opt_via_addr = "127.0.0.1";
 			
 			user_profile = new UserAgentProfile(null);
 			user_profile.username = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString("username",""); // modified
 			user_profile.passwd = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString("password","");
 			user_profile.realm = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString("server","");
 			user_profile.from_url = user_profile.username
 					+ "@"
 					+ user_profile.realm;			
 			user_profile.contact_url = user_profile.username
 					+ "@"
 					+ opt_via_addr;
 
 			SipStack.init(null);
 			SipStack.debug_level = 0;
 //			SipStack.log_path = "/data/data/org.sipdroid.sipua";
 			SipStack.max_retransmission_timeout = 4000;
 			SipStack.transaction_timeout = 30000;
 			SipStack.default_transport_protocols = new String[1];
 			SipStack.default_transport_protocols[0] = PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString("protocol",
 					user_profile.realm.equals("pbxes.org")?"tcp":"udp");
 			SipStack.default_port = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString("port",""+SipStack.default_port));
 			
 			String version = "Sipdroid/" + Sipdroid.getVersion();
 			SipStack.ua_info = version;
 			SipStack.server_info = version;
 				
 			sip_provider = new SipProvider(opt_via_addr, SipStack.default_port);
 			CheckEngine();
 			
 			ua = new UserAgent(sip_provider, user_profile);
 			ra = new RegisterAgent(sip_provider, user_profile.from_url,
 					user_profile.contact_url, user_profile.username,
 					user_profile.realm, user_profile.passwd, this, user_profile);
 
 			register();
 			listen();
 		} catch (Exception E) {
 		}
 
 		return true;
 	}
 	
 	void setOutboundProxy() {
 		try {
 			sip_provider.setOutboundProxy(new SocketAddress(
 					IpAddress.getByName(PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString("dns","")),
 					SipStack.default_port));
 		} catch (UnknownHostException e) {
 		}
 	}
 	
 	public void CheckEngine() {
 		if (!sip_provider.hasOutboundProxy())
 			setOutboundProxy();
 	}
 
 	public Context getUIContext() {
 		return Receiver.mContext;
 	}
 	
 	public int getRemoteVideo() {
 		return ua.remote_video_port;
 	}
 	
 	public int getLocalVideo() {
 		return ua.local_video_port;
 	}
 	
 	public String getRemoteAddr() {
 		return ua.remote_media_address;
 	}
 	
 	public void register() {	
 		if (!Receiver.isFast(false)) {
 			if (user_profile != null && !user_profile.username.equals("") &&
 					!user_profile.realm.equals("") &&
 					ra != null && ra.unregister()) {
 				Receiver.onText(Receiver.REGISTER_NOTIFICATION,getUIContext().getString(R.string.reg),R.drawable.sym_presence_idle,0);
 				wl.acquire();
 			}
 		} else {
 			if (ra != null && ra.register()) {
 				Receiver.onText(Receiver.REGISTER_NOTIFICATION,getUIContext().getString(R.string.reg),R.drawable.sym_presence_idle,0);
 				wl.acquire();
 			}
 		}
 	}
 
 	public void halt() { // modified
 		if (wl.isHeld())
 			wl.release();
 		keepAlive(false);
 		Receiver.onText(Receiver.REGISTER_NOTIFICATION, null, 0, 0);
 		if (ra != null)
 			ra.halt();
 		if (ua != null)
 			ua.hangup();
 		if (sip_provider != null)
 			sip_provider.halt();
 	}
 
 	public boolean isRegistered()
 	{
 		if (ra == null)
 		{
 			return false;
 		}
 		return ra.isRegistered();
 	}
 	
 	public void onUaRegistrationSuccess(RegisterAgent ra, NameAddress target,
 			NameAddress contact, String result) {
 		if (isRegistered())
 			Receiver.onText(Receiver.REGISTER_NOTIFICATION,getUIContext().getString(R.string.regok),R.drawable.sym_presence_available,0);
 		else
 			Receiver.onText(Receiver.REGISTER_NOTIFICATION, null, 0,0);
 		Receiver.registered();
 		if (wl.isHeld())
 			wl.release();
 	}
 
 	/** When a UA failed on (un)registering. */
 	public void onUaRegistrationFailure(RegisterAgent ra, NameAddress target,
 			NameAddress contact, String result) {
 		Receiver.onText(Receiver.REGISTER_NOTIFICATION,getUIContext().getString(R.string.regfailed)+" ("+result+")",R.drawable.sym_presence_away,0);
 		if (wl.isHeld())
 			wl.release();
 		updateDNS();
 	}
 	
 	public void updateDNS() {
 		Editor edit = PreferenceManager.getDefaultSharedPreferences(getUIContext()).edit();
 		try {
 			edit.putString("dns", IpAddress.getByName(PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString("server","")).toString());
 		} catch (UnknownHostException e1) {
 			return;
 		}
 		edit.commit();
 		setOutboundProxy();
 	}
 
 	/** Receives incoming calls (auto accept) */
 	public void listen() 
 	{
 		ua.printLog("UAS: WAITING FOR INCOMING CALL");
 		
 		if (!ua.user_profile.audio && !ua.user_profile.video)
 		{
 			ua.printLog("ONLY SIGNALING, NO MEDIA");
 		}
 		
 		ua.listen();
 	}
 	
 	public void info(char c) {
 		ua.info(c);
 	}
 	
 	/** Makes a new call */
 	public boolean call(String target_url) {
 		ua.printLog("UAC: CALLING " + target_url);
 		
 		if (!isRegistered() || !Receiver.isFast(true)) {
 			if (PreferenceManager.getDefaultSharedPreferences(getUIContext()).getBoolean("callback",false) &&
 					PreferenceManager.getDefaultSharedPreferences(getUIContext()).getString("posurl","").length() > 0) {
 				Receiver.url("n="+Uri.decode(target_url));
 				return true;
 			}
 			return false;
 		}
 
 		if (!ua.user_profile.audio && !ua.user_profile.video)
 		{
 			 ua.printLog("ONLY SIGNALING, NO MEDIA");
 		}
 		return ua.call(target_url, false);
 	}
 
 	public void answercall() 
 	{
 		ua.accept();
 	}
 
 	public void rejectcall() {
 		ua.printLog("UA: HANGUP");
 		ua.hangup();
 	}
 
 	public void togglehold() {
 		ua.reInvite(null, 0);
 	}
 	
 	public void togglemute() {
 		if (ua.call_state == UserAgent.UA_STATE_HOLD)
 			ua.reInvite(null, 0);
 		else
 			ua.muteMediaApplication();
 	}
 	
 	public int speaker(int mode) {
 		return ua.speakerMediaApplication(mode);
 	}
 	
 	/** When a new call is incoming */
 	public void onState(int state,String text) {
 			Receiver.onState(state,text);
 	}
 
 	KeepAliveSip ka;
 	
	public boolean keepAlive() {
 		if (ka != null && isRegistered())
 			try {
 				ka.sendToken();
				return true;
 			} catch (IOException e) {
 				if (!Sipdroid.release) e.printStackTrace();
 			}
		return false;
 	}
 	
 	public void keepAlive(boolean on_wlan) {
        	if (on_wlan) {
     		if (ka == null)
    			ka = new KeepAliveSip(sip_provider,15000);
    		Receiver.alarm(15, LoopAlarm.class);
     	} else
     		if (ka != null) {
     			ka.halt();
     			ka = null;
     			Receiver.alarm(0, LoopAlarm.class);
     		}	        	
 
 	}
 }
