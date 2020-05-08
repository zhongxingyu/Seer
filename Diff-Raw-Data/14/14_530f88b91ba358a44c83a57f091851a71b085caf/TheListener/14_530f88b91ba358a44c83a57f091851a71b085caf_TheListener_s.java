 package net.immortalcraft.races.listener;
 
 import net.immortalcraft.races.Conf;
 import net.immortalcraft.races.RPlayer;
 import net.immortalcraft.races.Races;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.massivecraft.mcore.util.PlayerUtil;
 
 
 public class TheListener implements Listener
 {
 	// -------------------------------------------- //
 	// META
 	// -------------------------------------------- //
 
 	public Races r;
 	public TheListener(Races r)
 	{
 		this.r = r;
 		Bukkit.getServer().getPluginManager().registerEvents(this, r);
 	}
 	// -------------------------------------------- //
 	// MISC
 	// -------------------------------------------- //
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void blockEvents(EntityDamageEvent event)
 	{
 		Entity entity = event.getEntity();
 		Conf conf = Conf.get(entity);
 
 		if ( ! (entity instanceof Player)) return;
 		if ( ! conf.blockDamageFromOrc.contains(event.getCause())) return;
 
 		RPlayer rplayer = (RPlayer)entity;
 
 		if (rplayer.isOrc()) event.setCancelled(true);
 
 		if ( ! (entity instanceof Player)) return;
 		if ( ! conf.blockDamageFromDemon.contains(event.getCause())) return;
 
 		if (rplayer.isDemon()) event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void blockEvents(EntityRegainHealthEvent event)
 	{
 		Entity entity = event.getEntity();
 		Conf conf = Conf.get(entity);
 
 		if ( ! (entity instanceof Player)) return;
 		if ( ! conf.blockHealthFrom.contains(event.getRegainReason())) return;
 
 		Player player = (Player) entity;		
 		RPlayer rplayer = RPlayer.get(player);
 
 		if (rplayer.isDemon()) event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void blockEvents(FoodLevelChangeEvent event)
 	{
 		Entity entity = event.getEntity();
 		if ( ! (entity instanceof Player)) return;
 
 		Player player = (Player) entity;		
 		RPlayer rplayer = RPlayer.get(player);
 
 		if (rplayer.isOrc())
 		{
 			event.setCancelled(true);
 			PlayerUtil.sendHealthFoodUpdatePacket(player);
 		}
 	}
 
 	// -------------------------------------------- //
 	// UPDATE
 	// -------------------------------------------- //
 
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void updateOnJoin(PlayerJoinEvent event)
 	{
 		final Player player = event.getPlayer();
 		final RPlayer rplayer = RPlayer.get(player);
 		rplayer.update();
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void updateOnTeleport(PlayerTeleportEvent event)
 	{
 		final Player player = event.getPlayer();
 
 		Bukkit.getScheduler().scheduleSyncDelayedTask(r, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				RPlayer rplayer = RPlayer.get(player);
 				rplayer.update();
 			}
 		});
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void updateOnDeath(EntityDeathEvent event)
 	{
 		// If a race dies ...
 		RPlayer rplayer = RPlayer.get(event.getEntity());
 		if (rplayer == null) return;
 		if (rplayer.isDemon() == false)return;
 		if (rplayer.isElf() == false) return;
 		if (rplayer.isHuman() == false) return;
 		if (rplayer.isOrc() == false) return;
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void updateOnRespawn(PlayerRespawnEvent event)
 	{
 		// If the player is a race ...
 		final RPlayer rplayer = RPlayer.get(event.getPlayer());
 		if (rplayer == null) return;
 		if ( ! rplayer.isDemon()) return;
 		if ( ! rplayer.isElf()) return;
 		if ( ! rplayer.isHuman())return;
 		if ( ! rplayer.isOrc())return;
 		
 		// ... modify food and health levels and force another speed-update.
 		Bukkit.getScheduler().scheduleSyncDelayedTask(r, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				Player player = rplayer.getPlayer();
 				if (player == null) return;
 				Conf conf = Conf.get(player);
 				player.setFoodLevel(conf.updateRespawnFood);
 				player.setHealth(conf.updateRespawnHealth);
 				rplayer.update();
 			}
 		});
 	}
 
 	public void updateNameColor(Player player)
 	{
 		Conf conf = Conf.get(player); 
 		if (conf.updateNameColor == false) return;
 		RPlayer rplayer = RPlayer.get(player);
 		if ( ! rplayer.isDemon()) return;
 		if ( ! rplayer.isElf()) return;
 		if ( ! rplayer.isHuman())return;
 		if ( ! rplayer.isOrc())return;
 		rplayer.setDisplayName(conf.updateNameColorTo.toString()+ChatColor.stripColor(player.getDisplayName()));
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void updateNameColor(AsyncPlayerChatEvent event)
 	{
 		updateNameColor(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void updateNameColor(PlayerJoinEvent event)
 	{
 		updateNameColor(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void updateNameColor(PlayerTeleportEvent event)
 	{
 		updateNameColor(event.getPlayer());
 	}
 
 
 	// -------------------------------------------- //
 	// DROP SELF
 	// -------------------------------------------- //
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void dropSelf(BlockBreakEvent event)
 	{
 		// If a non-creative player ...
 		Player player = event.getPlayer();
 		Conf conf = Conf.get(player);
 		if (player != null && player.getGameMode() == GameMode.CREATIVE) return;
 
 		// ... broke a self-dropping block ...  
 		Material material = event.getBlock().getType();
 		if ( ! conf.dropSelfMaterials.contains(material)) return;
 
 		// ... then we make it drop itself.	
 		event.setCancelled(true);
 		event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(material, 1));
 		event.getBlock().setType(Material.AIR);
 	}
 }
