 package com.alta189.chavabot.events.botevents;
 
 import com.alta189.chavabot.events.Cancellable;
 import com.alta189.chavabot.events.HandlerList;
 import com.alta189.chavabot.events.Listener;
 import com.alta189.chavabot.events.Order;
 import com.alta189.chavabot.plugins.Plugin;
 
 public class SendMessageEvent extends BotEvent<SendMessageEvent> implements Cancellable {
 	private static final SendMessageEvent instance = new SendMessageEvent();
 	private static final HandlerList<SendMessageEvent> handlers = new HandlerList<SendMessageEvent>();
 	private String message;
 	private String target;
 	
 	public static SendMessageEvent getInstance(String message, String target) {
 		instance.message = message;
 		instance.target = target;
		instance.setCancelled(false);
 		return instance;
 	}
 	
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	public void setTarget(String target) {
 		this.target = target;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public String getTarget() {
 		return target;
 	}
 	
 	public static void register(Listener<SendMessageEvent> listener, Order order, Plugin plugin) {
 		handlers.register(listener, order, plugin);
 	}
 	
 	@Override
 	public HandlerList<SendMessageEvent> getHandlers() {
 		return handlers;
 	}
 
 	@Override
 	protected String getEventName() {
 		return "Send Message Event";
 	}
 	
 	public void setCancelled(boolean cancelled) {
         super.setCancelled(cancelled);
     }
 	
 }
 
