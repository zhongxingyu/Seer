 package net.skycraftmc.SkyQuest.event;
 
 import net.skycraftmc.SkyQuest.quest.Quest;
 
 import org.bukkit.entity.Player;
 
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
 }
