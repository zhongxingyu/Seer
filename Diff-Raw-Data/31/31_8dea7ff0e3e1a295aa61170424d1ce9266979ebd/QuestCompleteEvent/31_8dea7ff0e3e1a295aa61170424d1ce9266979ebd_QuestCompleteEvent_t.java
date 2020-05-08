 package net.skycraftmc.SkyQuest.event;
 
 import net.skycraftmc.SkyQuest.quest.Quest;
 
 import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
 
 public class QuestCompleteEvent extends org.bukkit.event.Event
 {
 	private Player player;
 	private Quest quest;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 144776011187714844L;
 	public QuestCompleteEvent(Player player, Quest quest)
 	{
 		this.player = player;
 		this.quest = quest;
 	}
 	public Quest getQuest()
 	{
 		return quest;
 	}
 	public Player getPlayer()
 	{
 		return player;
 	}
	private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
 }
