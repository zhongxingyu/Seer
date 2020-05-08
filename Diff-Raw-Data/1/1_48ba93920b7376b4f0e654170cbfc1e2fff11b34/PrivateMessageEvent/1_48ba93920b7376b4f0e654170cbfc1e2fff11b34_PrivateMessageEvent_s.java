 package com.alta189.chavabot.events.botevents;
 
 import com.alta189.chavabot.ChavaUser;
 import com.alta189.chavabot.events.HandlerList;
 import com.alta189.chavabot.events.Listener;
 import com.alta189.chavabot.events.Order;
 import com.alta189.chavabot.plugins.Plugin;
 
 public class PrivateMessageEvent extends BotEvent<PrivateMessageEvent> {
 	private static final PrivateMessageEvent instance = new PrivateMessageEvent();
 	private static final HandlerList<PrivateMessageEvent> handlers = new HandlerList<PrivateMessageEvent>();
 	private String message;
 	private ChavaUser user;
 	
 	public static PrivateMessageEvent getInstance(String message, ChavaUser sender) {
 		instance.message = message;
 		return instance;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public ChavaUser getSender() {
 		return user;
 	}
 	
 	public static void register(Listener<PrivateMessageEvent> listener, Order order, Plugin plugin) {
 		handlers.register(listener, order, plugin);
 	}
 	
 	@Override
 	public HandlerList<PrivateMessageEvent> getHandlers() {
 		return handlers;
 	}
 
 	@Override
 	protected String getEventName() {
 		return "Private Message Event";
 	}
 	
 }
