 package com.alta189.chavabot.events.botevents;
 
 import com.alta189.chavabot.events.Cancellable;
 import com.alta189.chavabot.events.HandlerList;
 
 public class SendActionEvent extends BotEvent<SendActionEvent> implements Cancellable {
 	private static final SendActionEvent instance = new SendActionEvent();
 	private static final HandlerList<SendActionEvent> handlers = new HandlerList<SendActionEvent>();
 	private String action;
 	private String target;
 	
 	public static SendActionEvent getInstance(String action, String target) {
 		instance.action = action;
 		instance.target = target;
 		return instance;
 	}
 	
 	public void setAction(String action) {
 		this.action = action;
 	}
 
 	public void setTarget(String target) {
 		this.target = target;
 	}
 
 	public String getAction() {
 		return action;
 	}
 
 	public String getTarget() {
 		return target;
 	}
 	
 	@Override
 	public HandlerList<SendActionEvent> getHandlers() {
 		return handlers;
 	}
 	
 	@Override
 	protected String getEventName() {
 		return "Send Action Event";
 	}
 	
 	public void setCancelled(boolean cancelled) {
         super.setCancelled(cancelled);
     }
 	
 }
 
