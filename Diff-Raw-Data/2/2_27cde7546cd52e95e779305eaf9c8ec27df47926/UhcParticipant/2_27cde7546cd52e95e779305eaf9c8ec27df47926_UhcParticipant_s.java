 package com.martinbrook.tesseractuhc;
 
 import java.util.HashSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.martinbrook.tesseractuhc.startpoint.UhcStartPoint;
 
 public class UhcParticipant implements PlayerTarget {
 	private boolean launched = false;
 	private UhcTeam team;
 	private HashSet<PlayerTarget> nearbyTargets = new HashSet<PlayerTarget>();
 	private UhcPlayer player;
 
 	private boolean dead = false;
 	private boolean miningFatigueAlerted = false;
 	private int miningFatigueGrace = 20;
 	private long lastDamageTime = 0;
 	private boolean warnedHardStone = false;
 	
 	public UhcParticipant(UhcPlayer pl, UhcTeam team) {
 		this.player = pl;
 		this.team = team;
 	}
 		
 	public String getName() {
 		return player.getName();
 	}
 
 
 	public boolean isLaunched() {
 		return launched;
 	}
 
 
 	public void setLaunched(boolean launched) {
 		this.launched = launched;
 	}
 
 
 	public UhcStartPoint getStartPoint() {
 		return team.getStartPoint();
 	}
 
 
 	public boolean isDead() {
 		return dead;
 	}
 
 	public void setDead(boolean dead) {
 		this.dead = dead;
 	}
 
 	public UhcTeam getTeam() {
 		return team;
 	}
 
 	public boolean isNearTo(PlayerTarget target) {
 		return nearbyTargets.contains(target);
 	}
 
 	public void setNearTo(PlayerTarget target, boolean b) {
 		if (b)
 			nearbyTargets.add(target);
 		else
 			nearbyTargets.remove(target);
 		
 	}
 
 	public boolean teleport(Player p) { return this.teleport(p, "You have been teleported!"); }
 	public boolean teleport(Location l) { return this.teleport(l, "You have been teleported!"); }
 	public boolean teleport(Player p, String message) { return this.teleport(p.getLocation(), message); }
 	public boolean teleport(Location l, String message) { return player.teleport(l, message); }
 	
 	public UhcPlayer getPlayer() { return player; }
 
 	public boolean sendToStartPoint() {
 		return (player.setGameMode(GameMode.ADVENTURE) && teleport(getStartPoint().getLocation()) && player.renew());
 	}
 	
 	public boolean start() {
 		return (player.feed() && player.clearXP() && player.clearPotionEffects() 
 				&& player.heal() && player.setGameMode(GameMode.SURVIVAL));
 	}
 	public boolean sendMessage(String message) { return player.sendMessage(message); }
 	
 
 
 	/**
 	 * Apply the mining fatigue game mechanic
 	 * 
 	 * Players who mine stone below a certain depth increase their hunger
 	 * 
 	 * @param player The player to act upon
 	 * @param blockY The Y coordinate of the mined block
 	 */
 	public void doMiningFatigue(int blockY) {
 		Double exhaustion = 0.0;
 		
 		if (blockY < UhcMatch.DIAMOND_LAYER) {
 			exhaustion = this.player.getMatch().getConfig().getMiningFatigueDiamond(); 
 		} else if (blockY < UhcMatch.GOLD_LAYER) {
 			exhaustion = this.player.getMatch().getConfig().getMiningFatigueGold();
 		}
 		
 		if (exhaustion > 0) {
 			if (!miningFatigueAlerted) {
 				sendMessage(ChatColor.GOLD + "Warning: mining at this depth will soon make you very hungry!");
 				miningFatigueAlerted=true;
 			}
 			if (miningFatigueGrace > 0) {
 				if (--miningFatigueGrace == 0)
 					sendMessage(ChatColor.GOLD + "Warning: mining any more at this depth will make you very hungry!");
 			} else {
 				player.getPlayer().setExhaustion((float) (player.getPlayer().getExhaustion() + exhaustion));
 				player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 1200, 0));
 
 			}
 				
 		}
 				
 	}
 	
 	/**
 	 * Apply the hard stone game mechanic
 	 * 
 	 * Players who mine stone below a certain depth increase their hunger
 	 * 
 	 * @param blockY The Y coordinate of the mined block
 	 * @param tool The tool that was used to mine the block
 	 */
 	public void doHardStone(int blockY, ItemStack tool) {
 		
 		// Calculate applicable durability penalty
 		
 		short penalty;
 		
 		if (tool.getType() == Material.GOLD_PICKAXE) {
 			penalty = UhcMatch.DURABILITY_PENALTY_GOLD;
 		} else if (tool.getType() == Material.WOOD_PICKAXE) {
 			penalty = UhcMatch.DURABILITY_PENALTY_WOOD;
 		} else if (tool.getType() == Material.STONE_PICKAXE) {
 			penalty = UhcMatch.DURABILITY_PENALTY_STONE;
 		} else if (tool.getType() == Material.IRON_PICKAXE) {
 			penalty = UhcMatch.DURABILITY_PENALTY_IRON;
 		} else if (tool.getType() == Material.DIAMOND_PICKAXE) {
 			penalty = UhcMatch.DURABILITY_PENALTY_DIAMOND;
 		} else return;
 		
 		// Warn the player the first time
 		
 		if (!warnedHardStone) {
 			player.sendMessage(ChatColor.GOLD + "Warning! Mining smoothstone will wear out your tools more quickly than in normal Minecraft.");
 			warnedHardStone=true;
 		}
 		
 		// Apply durability cost
 		
		tool.setDurability((short) (tool.getDurability() - penalty));
 
 	}
 	
 	/**
 	 * Mark the player as having taken damage
 	 */
 	public void setDamageTimer() {
 		lastDamageTime = player.getMatch().getStartingWorld().getFullTime();
 	}
 	
 	/**
 	 * @return whether the player has taken damage recently
 	 */
 	public boolean isRecentlyDamaged() {
 		return (player.getMatch().getStartingWorld().getFullTime() - lastDamageTime < UhcMatch.PLAYER_DAMAGE_ALERT_TICKS);
 	}
 
 
 }
