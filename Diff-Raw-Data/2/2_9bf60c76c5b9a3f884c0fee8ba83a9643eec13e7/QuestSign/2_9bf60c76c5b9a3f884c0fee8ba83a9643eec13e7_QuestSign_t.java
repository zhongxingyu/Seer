 package com.theminequest.MineQuest.Frontend.Sign;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Date;
 import java.util.Map;
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
 import com.theminequest.MineQuest.API.Tracker.LogStatus;
 import com.theminequest.MineQuest.API.Tracker.QuestStatisticUtils;
 import com.theminequest.MineQuest.API.Tracker.QuestStatisticUtils.QSException;
 
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
 		if (block==null || block.getState()==null)
 			return;
 		if (!(block.getState() instanceof Sign))
 			return;
 		Sign sign = (Sign) block.getState();
 		if (!isQuestSign(sign))
 			return;
 		if (!player.hasPermission("minequest.sign.takequest")) {
 			player.sendMessage(ChatColor.RED + "You do not have permission to use this!");
 			return;
 		}
 		String quest_name = sign.getLine(2);
 		QuestDetails d = Managers.getQuestManager().getDetails(quest_name);
 		if (d==null){
 			player.sendMessage(ChatColor.RED + "Yikes! We can't find this quest anymore...");
 		}
 		Map<String, Date> givenquests = QuestStatisticUtils.getQuests(player.getName(), LogStatus.GIVEN);
 		for (String s : givenquests.keySet()){
 			if (quest_name.equals(s)){
 				player.sendMessage("You already have this quest!");
 				return;
 			}
 		}
 		if (action == Action.RIGHT_CLICK_BLOCK) {
 			player.sendMessage(QuestDetailsUtils.getOverviewString(d).split("\n"));
 			if (QuestDetailsUtils.requirementsMet(d, player))
 				player.sendMessage("This quest is currently " + ChatColor.BOLD + ChatColor.GREEN + "available" + ChatColor.RESET + " to you - left click to accept!");
 			else
 				player.sendMessage("This quest is currently " + ChatColor.BOLD + ChatColor.RED + "not available" + ChatColor.RESET + " to you.");
 		} else if (action == Action.LEFT_CLICK_BLOCK) {
 			if (QuestDetailsUtils.requirementsMet(d, player)) {
 				try {
 					QuestStatisticUtils.giveQuest(player.getName(), quest_name);
 					player.sendMessage(ChatColor.GREEN + "Successfully added " + d.getProperty(QuestDetails.QUEST_DISPLAYNAME) + " to your quest list!");
 				} catch (QSException e) {
 					player.sendMessage("This quest doesn't seem to like you.");
 					e.printStackTrace();
 				}
 			} else
 				player.sendMessage("This quest is currently " + ChatColor.BOLD + ChatColor.RED + "not available" + ChatColor.RESET + " to you.");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onSignChangeEvent(SignChangeEvent event) {
		if (!event.getLine(1).equalsIgnoreCase("[Quest]"))
 			return;
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		if (!player.hasPermission("minequest.sign.placesign")) {
 			player.sendMessage(ChatColor.RED + "You do not have permission to do this!");
 			block.breakNaturally();
 			return;
 		}
 		if (event.getLine(2).equalsIgnoreCase("")){
 			event.setCancelled(true);
 			player.sendMessage(ChatColor.RED + "Must specify a quest!");
 			block.breakNaturally();
 			return;
 		}
 		if (Managers.getQuestManager().getDetails(event.getLine(2))==null){
 			event.setCancelled(true);
 			player.sendMessage(ChatColor.RED + "No such quest!");
 			block.breakNaturally();
 			return;
 		}
 		// oh, prettify it ;D
 		event.setLine(1,ChatColor.GREEN+"[Quest]");
 	}
 }
