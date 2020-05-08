 package net.skycraftmc.SkyQuest.event;
 
 import net.skycraftmc.SkyQuest.quest.Objective;
 import net.skycraftmc.SkyQuest.quest.Quest;
 
 import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
 
 public class ObjectiveCompleteEvent extends org.bukkit.event.Event
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2147366048805027422L;
 	private Player player;
 	private Quest quest;
 	private Objective objective;
 	public ObjectiveCompleteEvent(Player player, Quest quest, Objective objective)
 	{
 		this.player = player;
 		this.quest = quest;
 		this.objective = objective;
 	}
 	public Quest getQuest()
 	{
 		return quest;
 	}
 	public Player getPlayer()
 	{
 		return player;
 	}
 	public Objective getObjective()
 	{
 		return objective;
 	}
	private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
 }
