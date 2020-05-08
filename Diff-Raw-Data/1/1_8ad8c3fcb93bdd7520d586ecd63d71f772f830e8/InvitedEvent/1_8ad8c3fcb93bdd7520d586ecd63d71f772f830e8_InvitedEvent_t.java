 package com.alta189.chavabot.events.botevents;
 
 import com.alta189.chavabot.ChavaUser;
 import com.alta189.chavabot.events.HandlerList;
 
 public class InvitedEvent extends BotEvent<InvitedEvent> {
 	private static final InvitedEvent instance = new InvitedEvent();
 	private static final HandlerList<InvitedEvent> handlers = new HandlerList<InvitedEvent>();
 	private String channel;
 	private ChavaUser user;
 	
 	public static InvitedEvent getInstance(String channel, ChavaUser sender) {
 		instance.channel = channel;
		instance.user = sender;
 		return instance;
 	}
 
 	public String getChannel() {
 		return channel;
 	}
 
 	public ChavaUser getSender() {
 		return user;
 	}
 	
 	@Override
 	public HandlerList<InvitedEvent> getHandlers() {
 		return handlers;
 	}
 
 	@Override
 	protected String getEventName() {
 		return "Invited Event";
 	}
 	
 	public void setCancelled(boolean cancelled) {
         super.setCancelled(cancelled);
     }
 	
 }
