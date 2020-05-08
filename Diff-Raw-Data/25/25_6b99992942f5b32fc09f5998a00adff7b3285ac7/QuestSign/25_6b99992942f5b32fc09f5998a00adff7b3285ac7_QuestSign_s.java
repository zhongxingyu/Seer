 package com.theminequest.MineQuest.Frontend.Sign;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 import com.theminequest.MineQuest.API.Quest.QuestDetailsUtils;
 import com.theminequest.MineQuest.API.Tracker.QuestStatistic;
 
 public class QuestSign implements Listener {
 
 	public QuestSign(){
 		Managers.log("[QuestSign] Starting Sign Frontends...");
 	}
 
 	private boolean isQuestSign(Sign sign){
 		String[] line = sign.getLines();
 		if (line[1] != null && line[1].contains("[Quest]")){
 			return (line[2] != null);
 		}
 		return false;
 	}
 
 	//Listeners For Sign interact and place. 
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteract(PlayerInteractEvent event){
 		Action action = event.getAction();
 		Block block = event.getClickedBlock();
 		Player player = event.getPlayer();
 		if (!(block.getState() instanceof Sign))
 			return;
 		Sign sign = (Sign) block.getState();
 		if (!isQuestSign(sign))
 			return;
 		String quest_name = sign.getLine(2);
 		QuestDetails d = Managers.getQuestManager().getDetails(quest_name);
 		if (d==null){
 			block.breakNaturally();
 			player.sendMessage(ChatColor.RED + "Yikes! We can't find this quest anymore...");
 		}
 		if (action == Action.RIGHT_CLICK_BLOCK) {
 			player.sendMessage(QuestDetailsUtils.getOverviewString(d).split("\n"));
 			if (QuestDetailsUtils.requirementsMet(d, player))
 				player.sendMessage("This quest is currently " + ChatColor.BOLD + ChatColor.GREEN + "available" + ChatColor.RESET + " to you - left click to accept!");
 			else
 				player.sendMessage("This quest is currently " + ChatColor.BOLD + ChatColor.RED + "not available" + ChatColor.RESET + " to you.");
 		} else if (action == Action.LEFT_CLICK_BLOCK) {
 			if (QuestDetailsUtils.requirementsMet(d, player)) {
				QuestStatistic pstat = Managers.getStatisticManager().getStatistic(player.getName(), QuestStatistic.class);
				pstat.addGivenQuest(quest_name);
				player.sendMessage(ChatColor.GREEN + "Successfully added " + d.getProperty(QuestDetails.QUEST_DISPLAYNAME) + " to your quest list!");
 			} else
 				player.sendMessage("This quest is currently " + ChatColor.BOLD + ChatColor.RED + "not available" + ChatColor.RESET + " to you.");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onSignChangeEvent(SignChangeEvent event) {
 		if (!event.getLine(1).equalsIgnoreCase("[Quest]") && !event.getLine(1).equalsIgnoreCase("quest"))
 			return;
 		if (event.getLine(2).equalsIgnoreCase("")){
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.RED + "Must specify a quest!");
 			event.getBlock().breakNaturally();
 			return;
 		}
 		if (Managers.getQuestManager().getDetails(event.getLine(2))==null){
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.RED + "No such quest!");
 			event.getBlock().breakNaturally();
 			return;
 		}
 		// oh, prettify it ;D
 		event.setLine(1,ChatColor.GREEN+"[Quest]");
 	}
 }
