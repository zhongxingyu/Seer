 package com.TeamNovus.Supernaturals.Listeners.Custom;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import com.TeamNovus.Supernaturals.SNPlayers;
 import com.TeamNovus.Supernaturals.Supernaturals;
 import com.TeamNovus.Supernaturals.Commands.CommandManager;
 import com.TeamNovus.Supernaturals.Events.PlayerLevelUpEvent;
 import com.TeamNovus.Supernaturals.Player.SNPlayer;
 import com.TeamNovus.Supernaturals.Util.ChatUtil;
 
 public class ExperienceListener implements Listener {
 	private HashMap<Material, Integer> blockBreakSources = new HashMap<Material, Integer>();
 	private HashMap<EntityType, Integer> entityKillSources = new HashMap<EntityType, Integer>();
 	
 	public ExperienceListener() {
 		// Setup block break sources:
		blockBreakSources.put(Material.LOG, 2);
 		blockBreakSources.put(Material.COAL_ORE, 2);
 		blockBreakSources.put(Material.IRON_ORE, 4);
 		blockBreakSources.put(Material.GOLD_ORE, 6);
 		blockBreakSources.put(Material.DIAMOND_ORE, 7);
 		blockBreakSources.put(Material.LAPIS_ORE, 3);
 		blockBreakSources.put(Material.REDSTONE_ORE, 3);
 
 		// Setup entity kill sources:
 		entityKillSources.put(EntityType.BLAZE, 7);
 		entityKillSources.put(EntityType.CAVE_SPIDER, 8);
 		entityKillSources.put(EntityType.ENDERMAN, 8);
 		entityKillSources.put(EntityType.ZOMBIE, 5);
 		entityKillSources.put(EntityType.SPIDER, 5);
 		entityKillSources.put(EntityType.SKELETON, 5);
 		entityKillSources.put(EntityType.CREEPER, 5);
 		entityKillSources.put(EntityType.GHAST, 12);
 		entityKillSources.put(EntityType.GIANT, 20);
 		entityKillSources.put(EntityType.PIG_ZOMBIE, 8);
 		entityKillSources.put(EntityType.WITCH, 10);
 		entityKillSources.put(EntityType.ENDER_DRAGON, 1000);
 		entityKillSources.put(EntityType.WITHER, 1500);
 		entityKillSources.put(EntityType.PLAYER, 50);
 	}
 	
 	@EventHandler
 	public void onPlayerLevelUp(PlayerLevelUpEvent event) {
 		SNPlayer player = SNPlayers.i.get(event.getPlayer());
 		
 		event.getPlayer().sendMessage(ChatColor.GREEN + "You are now level " + ChatColor.YELLOW + player.getLevel() + ChatColor.GREEN + "!");
 		Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " is now a level " + ChatColor.YELLOW + player.getLevel() + " " + player.getPlayerClass().getColor() + player.getPlayerClass().getName() + ChatColor.GREEN + "!");
 	}
 	
 	public void gainExp(SNPlayer player, Integer exp) {
 		if(Supernaturals.getPlugin().getConfig().getStringList("settings.disabled-worlds").contains(player.getPlayer().getWorld().getName().toLowerCase())) {
 			return;
 		}
 		
 		if(!(player.getPlayer().getGameMode().equals(GameMode.CREATIVE)) && player.getLevel() < player.getPlayerClass().getMaxLevel()) {
 			player.setExperience(player.getExperience() + exp);
 						
 			if(player.isVerbose()) {
 				player.sendMessage(CommandManager.getDark() + "Experience: "
 						+ ChatColor.RED + "[" + ChatUtil.fillBar(50, ChatColor.GOLD, ChatColor.GRAY, (player.getExperience() - player.getTotalExperienceFor(player.getLevel() - 1)), (player.getTotalExperienceFor(player.getLevel()) - player.getTotalExperienceFor(player.getLevel() - 1))) + ChatColor.RED + "]"
 						+ " (" + (player.getExperience() - player.getTotalExperienceFor(player.getLevel() - 1)) + "/" + (player.getTotalExperienceFor(player.getLevel()) - player.getTotalExperienceFor(player.getLevel() - 1)) + ")");
 			}	
 		}
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		SNPlayer player = SNPlayers.i.get(event.getPlayer());
 		
 		if(blockBreakSources.keySet().contains(event.getBlock().getType())) {
 			gainExp(player, blockBreakSources.get(event.getBlock().getType()));
 		}
 	}
 	
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event) {
 		if(event.getEntity().getKiller() == null) return;
 		
 		SNPlayer player = SNPlayers.i.get(event.getEntity().getKiller());
 		
 		if(entityKillSources.keySet().contains(event.getEntityType())) {
 			gainExp(player, entityKillSources.get(event.getEntityType()));
 		}
 	}
 
 }
