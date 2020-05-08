 package com.alta189.chavaadmin;
 
 import com.alta189.chavabot.ChavaManager;
 import com.alta189.chavabot.events.Listener;
 import com.alta189.chavabot.events.ircevents.ConnectEvent;
 
 public class ConnectListener implements Listener<ConnectEvent> {
 
 	public void onEvent(ConnectEvent event) {
		if (ChavaAdmin.getSettings().checkProperty("znc-auth-enabled") && ChavaAdmin.getSettings().getPropertyBoolean("znc-auth-enabled", false)) {
 			String user = ChavaAdmin.getSettings().getPropertyString("znc-user", null);
 			String pass = ChavaAdmin.getSettings().getPropertyString("znc-pass", null);
 
 			if (user != null && pass != null) {
 				ChavaManager.getInstance().getChavaBot().sendRawLine(new StringBuilder().append("PASS ").append(user).append(":").append(pass).toString());
 			}
 		}
 		
 		String logChan = ChavaAdmin.getLogChannel();
 		if (logChan != null) {
 			ChavaManager.getInstance().getChavaBot().joinChannel(logChan);
 		}
 	}
 
 }
