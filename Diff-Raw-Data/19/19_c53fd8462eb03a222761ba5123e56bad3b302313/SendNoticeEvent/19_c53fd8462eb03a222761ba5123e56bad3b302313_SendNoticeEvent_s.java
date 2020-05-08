 package com.alta189.chavabot.events.botevents;
 
 import com.alta189.chavabot.events.Cancellable;
 import com.alta189.chavabot.events.HandlerList;
 import com.alta189.chavabot.events.Listener;
 import com.alta189.chavabot.events.Order;
 import com.alta189.chavabot.plugins.Plugin;
 
 public class SendNoticeEvent extends BotEvent<SendNoticeEvent> implements Cancellable {
 	private static final SendNoticeEvent instance = new SendNoticeEvent();
 	private static final HandlerList<SendNoticeEvent> handlers = new HandlerList<SendNoticeEvent>();
 	private String notice;
 	private String target;
 
 	public static SendNoticeEvent getInstance(String notice, String target) {
 		instance.notice = notice;
 		instance.target = target;
 		return instance;
 	}
 
 	public void setNotice(String notice) {
 		this.notice = notice;
 	}
 
 	public void setTarget(String target) {
 		this.target = target;
 	}
 
 	public String getNotice() {
 		return notice;
 	}
 
 	public String getTarget() {
 		return target;
 	}
 	
 	public static void register(Listener<SendNoticeEvent> listener, Order order, Plugin plugin) {
 		handlers.register(listener, order, plugin);
 	}
 
 	@Override
 	public HandlerList<SendNoticeEvent> getHandlers() {
 		return handlers;
 	}
 
 	@Override
 	protected String getEventName() {
 		return "Send Notice Event";
 	}
 
 	public void setCancelled(boolean cancelled) {
 		super.setCancelled(cancelled);
 	}
 
 }
