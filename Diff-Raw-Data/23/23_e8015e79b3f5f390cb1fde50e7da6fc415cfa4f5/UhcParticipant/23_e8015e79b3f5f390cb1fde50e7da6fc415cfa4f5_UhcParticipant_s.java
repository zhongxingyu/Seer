 package com.martinbrook.tesseractuhc;
 
 import java.util.HashSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World.Environment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.martinbrook.tesseractuhc.customevent.UhcArmorChangeEvent;
 import com.martinbrook.tesseractuhc.customevent.UhcHealthChangeEvent;
 import com.martinbrook.tesseractuhc.startpoint.UhcStartPoint;
 import com.martinbrook.tesseractuhc.util.ArmorPoints;
 import com.martinbrook.tesseractuhc.util.PluginChannelUtils;
 
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
 	private boolean worldEdgeWarningActive = false;
 	
 	private int kills = 0;
 	private int shotsFired = 0;
 	private int shotsHit = 0;
 	
 	private int currentHealth;
 	private int currentArmor;
 	
 	public UhcParticipant(UhcPlayer pl, UhcTeam team) {
 		this.player = pl;
 		this.team = team;
		this.currentHealth = pl.getPlayer().getHealth();
		this.currentArmor = ArmorPoints.fromPlayerInventory(pl.getPlayer().getInventory());
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
 		
 		tool.setDurability((short) (tool.getDurability() + penalty));
 
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
 
 	public void doWorldEdgeWarning() {
 		if (worldEdgeWarningActive) return;
 		worldEdgeWarningActive=true;
 		sendMessage("You are close to the edge of the world!");
 		player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0));
 		player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
 		
 	}
 
 	public void clearWorldEdgeWarning() {
 		if (!worldEdgeWarningActive) return;
 		worldEdgeWarningActive=false;
 		player.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
 		player.getPlayer().removePotionEffect(PotionEffectType.SLOW);
 		
 	}
 
 	public boolean isOutsideBorder(int border) {
 		Location l = player.getLocation();
 		return l.getBlockX() > border || l.getBlockZ() > border
 				|| l.getBlockX() < -border || l.getBlockZ() < -border;
 	}
 	
 	public int getKills(){
 		return kills;
 	}
 
 	public void addKill(){
 		++kills;
 		PluginChannelUtils.messageSpectators("player", this.getName(), "kills", Integer.toString(getKills()));
 	}
 	
 	public int getDeaths(){
 		return isDead() ? 1 : 0;
 	}
 	
 	public void incrementShotsFired(){
 		++shotsFired;
 		PluginChannelUtils.messageSpectators("player", getName(), "accuracy", getAccuracy());
 	}
 	
 	public void incrementShotsHit(){
 		++shotsHit;
 		PluginChannelUtils.messageSpectators("player", getName(), "accuracy", getAccuracy());
 	}
 	
 	public String getAccuracy(){
 		return Integer.toString(shotsFired == 0 ? 0 : (100 * shotsHit / shotsFired));
 	}
 	
 	public void setIsOnline(boolean online){
 		PluginChannelUtils.messageSpectators("player", getName(), online ? "login" : "logout");
 	}
 	
 	public void updateHealth(){
 		Player player = getPlayer().getPlayer();
 		if (player == null) return;
 
 		int newHealth = Math.max(0, player.getHealth());
 
 		if (newHealth != currentHealth){
 			PluginChannelUtils.messageSpectators("player", getName(), "hp", Integer.toString(newHealth));
 			this.player.getMatch().getServer().getPluginManager().callEvent(new UhcHealthChangeEvent(this.player.getMatch(), this.player.getLocation(), player, newHealth));
 			currentHealth = newHealth;
 		}
 	}
 	
 	public void updateArmor(){
 		Player player = getPlayer().getPlayer();
 		if (player == null) return;
 
 		int newArmor = ArmorPoints.fromPlayerInventory(player.getInventory());
 
 		if (newArmor != currentArmor){
 			PluginChannelUtils.messageSpectators("player", getName(), "armor", Integer.toString(newArmor));
 			this.player.getMatch().getServer().getPluginManager().callEvent(new UhcArmorChangeEvent(this.player.getMatch(), this.player.getLocation(), player, newArmor));
 			currentArmor = newArmor;
 		}
 	}
 
 	public void updateDimension() {
 		Player player = getPlayer().getPlayer();
 		if (player == null) return;
 
 		Environment env = player.getWorld().getEnvironment();
 		String envString = "overworld";
 		if (env == Environment.NETHER)
 			envString = "nether";
 		else if (env == Environment.THE_END)
 			envString = "end";
 		
 		PluginChannelUtils.messageSpectators("player", getName(), "dimension", envString);
 	}
 	
 	public void updateInventory(){
 		for(UhcPlayer up : TesseractUHC.getInstance().getMatch().getOnlinePlayers()) {
 			if (up.isSpectator())
 				updateSpectatorOnInventory(up.getPlayer());
 		}
 	}
 	
 	public void updateSpectatorOnInventory(Player spec){
 		Player player = getPlayer().getPlayer();
 		if (player == null) return;
 		
 		PlayerInventory inv = player.getInventory();
 		int goldIngots = 0;
 		int goldenApples = 0;
 		int notchApples = 0;
 		for (ItemStack is : inv.getContents()){
 			if (is != null){
 				if (is.getType() == Material.GOLD_INGOT)
 					goldIngots += is.getAmount();
 				if (is.getType() == Material.GOLD_BLOCK)
 					goldIngots += is.getAmount() * 9;
 				if (is.getType() == Material.GOLD_NUGGET)
 					goldIngots += is.getAmount() / 9;
 				
 				if (is.getType() == Material.GOLDEN_APPLE){
 					if (is.getDurability() == 0)
 						goldenApples += is.getAmount();
 					if (is.getDurability() == 1)
 						notchApples += is.getAmount();
 				}
 			}
 		}
 		
 		PluginChannelUtils.messageSpectator(spec,"player", getName(), "itemcount", "266", Integer.toString(goldIngots));
 		PluginChannelUtils.messageSpectator(spec,"player", getName(), "itemcount", "322,0", Integer.toString(goldenApples));
 		PluginChannelUtils.messageSpectator(spec,"player", getName(), "itemcount", "322,1", Integer.toString(notchApples));
 	}
 
 }
