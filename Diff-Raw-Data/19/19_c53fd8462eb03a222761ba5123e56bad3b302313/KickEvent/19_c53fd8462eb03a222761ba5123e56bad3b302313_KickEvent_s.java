 package com.alta189.chavabot.events.botevents;
 
 import com.alta189.chavabot.events.Cancellable;
 import com.alta189.chavabot.events.HandlerList;
 import com.alta189.chavabot.events.Listener;
 import com.alta189.chavabot.events.Order;
 import com.alta189.chavabot.plugins.Plugin;
 
 public class KickEvent extends BotEvent<KickEvent> implements Cancellable {
 	private static final KickEvent instance = new KickEvent();
 	private static final HandlerList<KickEvent> handlers = new HandlerList<KickEvent>();
 	private String channel;
 	private String recipient;
 	private String reason;
 	
 	public static KickEvent getInstance(String channel, String recipient, String reason) {
 		instance.channel = channel;
 		instance.recipient = recipient;
 		instance.reason = reason;
 		return instance;
 	}
 
 	public String getChannel() {
 		return channel;
 	}
 
 	public String getRecipient() {
 		return recipient;
 	}
 	
 	public String getReason() {
 		return reason;
 	}
 	
 	public static void register(Listener<KickEvent> listener, Order order, Plugin plugin) {
 		handlers.register(listener, order, plugin);
 	}
 	
 	@Override
 	public HandlerList<KickEvent> getHandlers() {
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
