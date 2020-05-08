 package com.modcrafting.achievement.event;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.Event;
 
public abstract class AchievementGetEvent extends Event implements Cancellable {
	
	private static final long serialVersionUID = -83839173849184L;
 	
 	protected Player player;
 	protected boolean cancelled;
 	protected String type;
 	protected AchievementGetEvent(String name) {
		super(name);
 	}
 	public AchievementGetEvent(String event, Player player, String type) {
		super(event);
 		this.player = player;
 		this.type = type;
 	}
 	public Player getPlayer() {
 		return player;
 	}
 	public boolean isCancelled() {
 	    return cancelled;
 	}
 	public void setCancelled(Boolean cancel) {
 		cancelled = cancel;
 	}
 }
