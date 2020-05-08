 package com.alta189.chavaadmin;
 
 import com.alta189.chavabot.events.Listener;
 import com.alta189.chavabot.events.botevents.SendMessageEvent;
 
 public class SendMsgListener implements Listener<SendMessageEvent> {
 
 	private final ChavaAdmin core;
 
 	public SendMsgListener(ChavaAdmin core) {
 		this.core = core;
 	}
 
 	public void onEvent(SendMessageEvent event) {
 		if (core.isMuted(event.getTarget())) {
 			event.setCancelled(true);
		} else {
			ChavaAdmin.log("Not muted in " + event.getTarget());
 		}
 	}
 
 }
