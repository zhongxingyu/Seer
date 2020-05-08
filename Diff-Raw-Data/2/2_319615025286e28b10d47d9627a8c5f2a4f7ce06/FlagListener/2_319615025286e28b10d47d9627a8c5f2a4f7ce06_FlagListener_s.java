 package com.wolvencraft.prison.mines.events;
 
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockExpEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.PlayerItemBreakEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 
 import com.wolvencraft.prison.hooks.EconomyHook;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 import com.wolvencraft.prison.mines.util.constants.MineFlag;
 import com.wolvencraft.prison.mines.util.flags.BaseFlag;
 
 public class FlagListener implements Listener {
 	
 	public FlagListener(PrisonMine plugin) {
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 		Message.debug("| + FlagListener Initialized");
 	}
 	
 	@EventHandler
 	public void NoPlayerDamageListener (EntityDamageEvent event) {
 		if(event.isCancelled()) return;
 		if(!(event.getEntity() instanceof Player)) return;
 		
 		Player player = (Player) event.getEntity();
 		for(Mine mine : PrisonMine.getLocalMines()) {
 			if(!mine.getRegion().isLocationInRegion(player.getLocation())) continue;
 			
 			if(!mine.hasFlag(MineFlag.NoPlayerDamage)) continue;
 			
 			if(!player.hasPermission("prison.mine.flags.noplayerdamage." + mine.getId()) && !player.hasPermission("prison.mine.flags.noplayerdamage")) { continue; }
 			
 			event.setCancelled(true);
 			return;
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	public void NoToolDamageListener (BlockBreakEvent event) {
 		if(event.isCancelled()) return;
 		
 		Block b = event.getBlock();
 		
 		for(Mine mine : PrisonMine.getLocalMines()) {
 			if(!mine.getRegion().isLocationInRegion(b.getLocation())) continue;
 			
 			if(mine.hasFlag(MineFlag.NoToolDamage)) {
 				Player player = event.getPlayer();
 				if(!player.hasPermission("prison.mine.flags.notooldamage." + mine.getId()) && !player.hasPermission("prison.mine.flags.notooldamage")) { continue; }
 
 				Message.debug("NoToolDamage flag is in effect");
 				ItemStack tool = player.getInventory().getItemInHand();
 				player.getInventory().remove(tool);
 				for(Material mat : PrisonMine.getSettings().TOOLS) {
 					if(mat.equals(tool.getType())) {
 						short durability = tool.getDurability();
 						if(durability != 0) {
 							Message.debug("Old durability: " + durability);
 							tool.setDurability((short)(durability - 1));
 							Message.debug("new durability: " + tool.getDurability());
 						} else tool.setDurability((short) 0);
 						break;
 					}
 				}
 				player.setItemInHand(tool);
 				player.updateInventory();
 				return;
 			}
 		}
 	}
 	
 	@EventHandler
 	public void SuperToolsListener (BlockDamageEvent event) {
 		if(event.isCancelled()) return;
 		
 		Block b = event.getBlock();
 		
 		for(Mine mine : PrisonMine.getLocalMines()) {
 			if(!mine.getRegion().isLocationInRegion(b.getLocation())) continue;
 			
 			if(mine.hasFlag(MineFlag.SuperTools)) {
 				Player player = event.getPlayer();
 				if(!player.hasPermission("prison.mine.flags.supertools." + mine.getId()) && !player.hasPermission("prison.mine.flags.supertools")) { continue; }
 				Message.debug("SuperTools flag is in effect");
 				b.breakNaturally(player.getItemInHand());
 				return;
 			}
 		}
 	}
 
 	@EventHandler
 	public void ToolReplaceListener (PlayerItemBreakEvent event) {
 		
 		for(Mine mine : PrisonMine.getLocalMines()) {
 			if(!mine.getRegion().isLocationInRegion(event.getPlayer().getLocation())) continue;
 			
 			Player player = event.getPlayer();
 			if(!player.hasPermission("prison.mine.flags.toolreplace." + mine.getId()) && !player.hasPermission("prison.mine.flags.toolreplace")) { continue; }
 			
 			ItemStack brokenItem = event.getBrokenItem();
 			player.getInventory().addItem(new ItemStack(brokenItem.getType()));
 			return;
 		}
 	}
 	
 	@EventHandler
 	public void NoHungerChangeListener (FoodLevelChangeEvent event) {
 		if(event.isCancelled()) return;
 		Player player = (Player) event.getEntity();
 		Message.debug(player.getPlayerListName() + "'s hunger level changed to " + event.getFoodLevel());
 		
 		if(event.getFoodLevel() < player.getFoodLevel()) return;
 		
 		for(Mine mine : PrisonMine.getLocalMines()) {
 			if(!mine.getRegion().isLocationInRegion(player.getLocation())) continue;
 			
 			if(!player.hasPermission("prison.mine.flags.nohungerchange." + mine.getId()) && !player.hasPermission("prison.mine.flags.nohungerchange")) { continue; }
 			
 			if(player.getFoodLevel() < 20) event.setFoodLevel(player.getFoodLevel() + 3);
 			Message.debug(player.getPlayerListName() + "'s hunger level was reset to " + player.getFoodLevel());
 		}
 	}
 	
 	@EventHandler
 	public void PlayerEffectListener (PlayerMoveEvent event) {
 		if(event.isCancelled()) return;
 		
 		for(Mine mine : PrisonMine.getLocalMines()) {
 			if(!mine.hasFlag(MineFlag.PlayerEffect)) continue;
 			
 			Player player = event.getPlayer();
 			if(!mine.getRegion().isLocationInRegion(player.getLocation())) continue;
 			
 			if(!player.hasPermission("prison.mine.flags.playereffect." + mine.getId()) && !player.hasPermission("prison.mine.flags.playereffect")) { continue; }
 			
 			List<BaseFlag> effects = mine.getFlagsByType(MineFlag.PlayerEffect);
 			for(BaseFlag effect : effects)
 				player.addPotionEffect(new PotionEffect(Util.getEffect(effect.getOption()), 100, 1));
 		}
 	}
 	
 	@EventHandler
 	public void NoExpDropListener (BlockExpEvent event) {
 		for(Mine mine : PrisonMine.getLocalMines()) {
 			if(!mine.hasFlag(MineFlag.NoExpDrop)) continue;
 			if(!mine.getRegion().isLocationInRegion(event.getBlock().getLocation())) continue;
 			event.setExpToDrop(0);
 		}
 	}
 	
 	@EventHandler
 	public void MoneyRewardListener (BlockBreakEvent event) {
 		if(event.isCancelled()) return;
 		if(!EconomyHook.usingVault()) return;
 		
 		for(Mine mine : PrisonMine.getLocalMines()) {
 			Player player = event.getPlayer();
 			if(!player.hasPermission("prison.mine.flags.moneyreward." + mine.getId()) && !player.hasPermission("prison.mine.flags.moneyreward")) { continue; }
 			
			if(!mine.hasFlag(MineFlag.MoneyReward)) {
 				EconomyHook.deposit(player, Double.parseDouble(mine.getFlag(MineFlag.MoneyReward).getOption()));
 			} else if(mine.hasFlag(MineFlag.MoneyRewardPlus)) {
 				if(event.getBlock().getType().equals(mine.getMostCommonBlock().getBlock().getItemType())) continue;
 				EconomyHook.deposit(player, Double.parseDouble(mine.getFlag(MineFlag.MoneyReward).getOption()));
 			} else continue;
 		}
 	}
 }
