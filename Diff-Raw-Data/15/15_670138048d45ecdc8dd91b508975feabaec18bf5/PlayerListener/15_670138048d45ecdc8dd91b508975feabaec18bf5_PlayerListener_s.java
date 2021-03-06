 package net.dmulloy2.swornrpg.listeners;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import net.dmulloy2.swornrpg.SwornRPG;
 import net.dmulloy2.swornrpg.types.BlockDrop;
 import net.dmulloy2.swornrpg.types.Permission;
 import net.dmulloy2.swornrpg.types.PlayerData;
 import net.dmulloy2.swornrpg.util.FormatUtil;
 import net.dmulloy2.swornrpg.util.InventoryUtil;
 import net.dmulloy2.swornrpg.util.TimeUtil;
 import net.dmulloy2.swornrpg.util.Util;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.event.player.PlayerToggleSprintEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.earth2me.essentials.Essentials;
 import com.earth2me.essentials.User;
 
 /**
  * @author dmulloy2
  */
 
 public class PlayerListener implements Listener
 {
 	private boolean salvagingEnabled;
 	private boolean deathCoordinateMessages;
 	private boolean checkForUpdates;
 	private boolean fishingEnabled;
 	private boolean fishDropsEnabled;
 	private boolean speedBoostEnabled;
 
 	private int fishingGain;
 	private int speedBoostOdds;
 	private int speedBoostDuration;
 	private int speedBoostStrength;
 
 	private HashMap<String, ItemStack> bookMap;
 
 	private final SwornRPG plugin;
 
 	public PlayerListener(SwornRPG plugin)
 	{
 		this.plugin = plugin;
 
 		this.salvagingEnabled = plugin.getConfig().getBoolean("salvaging");
 		this.deathCoordinateMessages = plugin.getConfig().getBoolean("deathCoordinateMessages");
 		this.checkForUpdates = plugin.getConfig().getBoolean("checkForUpdates");
 		this.fishingEnabled = plugin.getConfig().getBoolean("levelingMethods.fishing.enabled");
 		this.fishDropsEnabled = plugin.getConfig().getBoolean("fishDropsEnabled");
 		this.speedBoostEnabled = plugin.getConfig().getBoolean("speedBoost.enabled");
 
 		this.fishingGain = plugin.getConfig().getInt("levelingMethods.fishing.xpgain");
 		this.speedBoostOdds = plugin.getConfig().getInt("speedBoost.odds");
 		this.speedBoostDuration = plugin.getConfig().getInt("speedBoost.duration");
 		this.speedBoostStrength = plugin.getConfig().getInt("speedBoost.strength");
 
 		this.bookMap = new HashMap<String, ItemStack>();
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		if (! salvagingEnabled || event.isCancelled())
 			return;
 
 		if (event.getAction() != Action.LEFT_CLICK_BLOCK)
 			return;
 
 		if (! event.hasBlock())
 			return;
 
 		Block block = event.getClickedBlock();
 		if (plugin.isDisabledWorld(block))
 			return;
 
 		Player player = event.getPlayer();
 		if (player.getGameMode() != GameMode.SURVIVAL)
 			return;
 
 		String blockType = "";
 		if (block.getType() == Material.IRON_BLOCK)
 			blockType = "Iron";
 		if (block.getType() == Material.GOLD_BLOCK)
 			blockType = "Gold";
 		if (block.getType() == Material.DIAMOND_BLOCK)
 			blockType = "Diamond";
 
 		if (! blockType.isEmpty())
 		{
 			if ((block.getRelative(-1, 0, 0).getType() == Material.FURNACE) || (block.getRelative(1, 0, 0).getType() == Material.FURNACE)
 					|| (block.getRelative(0, 0, -1).getType() == Material.FURNACE)
 					|| (block.getRelative(0, 0, 1).getType() == Material.FURNACE))
 			{
 				ItemStack item = player.getItemInHand();
 
 				Material type = item.getType();
 
 				double mult = 1.0D - ((double) item.getDurability() / item.getType().getMaxDurability());
 				double amt = 0.0D;
 
 				if (plugin.getSalvageRef().get(blockType).containsKey(type))
 					amt = Math.round(plugin.getSalvageRef().get(blockType).get(type) * mult);
 
 				if (amt > 0.0D)
 				{
 					String article = "a";
 					if (blockType == "Iron")
 						article = "an";
 					String materialExtension = " ingot";
 					if (blockType == "Diamond")
 						materialExtension = "";
 					String plural = "";
 					if (amt > 1.0D)
 						plural = "s";
 
 					String itemName = FormatUtil.getFriendlyName(item.getType());
 					player.sendMessage(plugin.getPrefix() + 
 							FormatUtil.format(plugin.getMessage("salvage_success"),
 									article, itemName, amt, blockType.toLowerCase(), materialExtension, plural));
 
 					plugin.outConsole(plugin.getMessage("log_salvage"), player.getName(), itemName, amt, blockType.toLowerCase(),
 							materialExtension, plural);
 
 					PlayerInventory inv = player.getInventory();
 					inv.removeItem(item);
 
 					Material give = null;
 					if (blockType == "Iron")
 						give = Material.IRON_INGOT;
 					if (blockType == "Gold")
 						give = Material.GOLD_INGOT;
 					if (blockType == "Diamond")
 						give = Material.DIAMOND;
 
 					ItemStack salvaged = new ItemStack(give, (int) amt);
 					InventoryUtil.addItems(inv, salvaged);
 					event.setCancelled(true);
 				}
 				else
 				{
 					String itemName = FormatUtil.getFriendlyName(item.getType());
 					player.sendMessage(plugin.getPrefix() + 
 							FormatUtil.format(plugin.getMessage("not_salvagable"), itemName, blockType.toLowerCase()));
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerDeath(PlayerDeathEvent event)
 	{
 		if (! deathCoordinateMessages)
 			return;
 
 		Player player = event.getEntity();
 		if (plugin.checkFactions(player, true))
 			return;
 
 		Location loc = player.getLocation();
 		int x = loc.getBlockX();
 		int y = loc.getBlockY();
 		int z = loc.getBlockZ();
 
 		PlayerData data = plugin.getPlayerDataCache().getData(player.getName());
 		if (! data.isDeathbookdisabled())
 		{
 			Essentials ess = plugin.getEssentials();
 			if (ess != null)
 			{
 				User user = ess.getUser(player);
 				
 				Player killer = plugin.getKiller(player);
 				if (killer != null)
 				{
 					String mail = FormatUtil.format(plugin.getMessage("mail_pvp_format"), 
 							killer.getName(), x, y, z, loc.getWorld().getName(), TimeUtil.getLongDateCurr());
 					user.addMail(mail);
 					
 					player.sendMessage(plugin.getPrefix() + 
 							FormatUtil.format(plugin.getMessage("death_coords_mail")));
 					plugin.debug(plugin.getMessage("log_death_coords"), player.getName(), "sent", "mail message");
 				}
 				else
 				{
 					String world = player.getWorld().getName();
 
 					String mail = FormatUtil.format(plugin.getMessage("mail_pve_format"), 
 							x, y, z, world, TimeUtil.getLongDateCurr());
 					user.addMail(mail);
 
 					player.sendMessage(plugin.getPrefix() + 
 							FormatUtil.format(plugin.getMessage("death_coords_mail")));
 					plugin.debug(plugin.getMessage("log_death_coords"), player.getName(), "sent", "mail message");
 				}
 			}
 			else
 			{
 				ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
 				BookMeta meta = (BookMeta) book.getItemMeta();
 
 				meta.setTitle(ChatColor.RED + "DeathCoords");
 				meta.setAuthor(ChatColor.GOLD + "SwornRPG");
 
 				List<String> pages = new ArrayList<String>();
 				pages.add(FormatUtil.format(plugin.getMessage("book_format"), player.getName(), x, y, z));
 				meta.setPages(pages);
 
 				book.setItemMeta(meta);
 
 				if (! bookMap.containsKey(player.getName()))
 				{
 					bookMap.put(player.getName(), book);
 					plugin.debug(plugin.getMessage("log_death_coords"), player.getName(), "given", "book");
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerRespawn(PlayerRespawnEvent event)
 	{
 		Player player = event.getPlayer();
 		if (bookMap.containsKey(player.getName()))
 		{
 			InventoryUtil.addItems(player.getInventory(), bookMap.get(player.getName()));
 			bookMap.remove(player.getName());
 		}
 
 		plugin.getHealthBarHandler().updateHealth(player);
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		Player player = event.getPlayer();
 		PlayerData data = plugin.getPlayerDataCache().getData(player);
 		if (data == null)
 		{
 			plugin.debug(plugin.getMessage("log_new_data"), player.getName());
 
 			data = plugin.getPlayerDataCache().newData(player);
			data.setXpneeded(100 + (data.getPlayerxp() / 4));
 			data.setLevel(0);
 		}
 
 		/** Update Notification **/
 		if (checkForUpdates && plugin.updateNeeded())
 		{
 			if (plugin.getPermissionHandler().hasPermission(player, Permission.UPDATE_NOTIFY))
 			{
 				player.sendMessage(plugin.getPrefix() + FormatUtil.format(plugin.getMessage("update_message")));
 				player.sendMessage(FormatUtil.format("&6[SwornRPG]&e " + plugin.getMessage("update_url")));
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		onPlayerDisconnect(event.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerKick(PlayerKickEvent event)
 	{
 		if (! event.isCancelled())
 		{
 			onPlayerDisconnect(event.getPlayer());
 		}
 	}
 
 	/** Basic stuff needed when a player leaves **/
 	public void onPlayerDisconnect(Player player)
 	{
 		PlayerData data = plugin.getPlayerDataCache().getData(player);
 
 		data.setFrenzyEnabled(false);
 		data.setSuperPickaxeEnabled(false);
 		data.setUnlimitedAmmoEnabled(false);
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerTeleport(PlayerTeleportEvent event)
 	{
 		if (event.getCause() != TeleportCause.COMMAND)
 			return;
 
 		Player player = event.getPlayer();
 		if (player.getVehicle() != null)
 		{
 			player.leaveVehicle();
 		}
 
 		if (player.getPassenger() != null)
 		{
 			player.eject();
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onSuperPickActivate(PlayerInteractEvent event)
 	{
 		Action action = event.getAction();
 		Player player = event.getPlayer();
 
 		if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR)
 			return;
 
 		String inhand = FormatUtil.getFriendlyName(player.getItemInHand().getType());
 		String[] array = inhand.split(" ");
 
 		if (array.length < 2)
 			return;
 
 		if (! array[0].equalsIgnoreCase("diamond") && ! array[0].equalsIgnoreCase("iron"))
 			return;
 
 		if (! array[1].equalsIgnoreCase("pickaxe") && ! array[1].equalsIgnoreCase("spade"))
 			return;
 
 		plugin.getAbilityHandler().activateSuperPickaxe(player, false, action);
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onFrenzyActicate(PlayerInteractEvent event)
 	{
 		Action action = event.getAction();
 		Player player = event.getPlayer();
 
 		if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR)
 			return;
 
 		String inhand = FormatUtil.getFriendlyName(player.getItemInHand().getType());
 		String[] array = inhand.split(" ");
 
 		if (array.length < 2)
 			return;
 
 		if (! array[0].equalsIgnoreCase("diamond") && ! array[0].equalsIgnoreCase("iron"))
 			return;
 
 		if (! array[1].equalsIgnoreCase("sword"))
 			return;
 
 		plugin.getAbilityHandler().activateFrenzy(player, false, action);
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerFish(PlayerFishEvent event)
 	{
 		if (! fishingEnabled || event.isCancelled())
 			return;
 
 		Player player = event.getPlayer();
 		if (plugin.isDisabledWorld(player))
 			return;
 
 		Entity caught = event.getCaught();
 		if (caught == null || caught.getType() != EntityType.DROPPED_ITEM)
 			return;
 
 		/** Fishing XP Gain **/
 		String message = plugin.getPrefix() + 
 				FormatUtil.format(plugin.getMessage("fishing_gain"), fishingGain);
 		plugin.getExperienceHandler().onXPGain(event.getPlayer(), fishingGain, message);
 
 		/** Fish Drops **/
 		if (! fishDropsEnabled)
 			return;
 
 		if (player.getGameMode() != GameMode.SURVIVAL)
 			return;
 
 		PlayerData data = plugin.getPlayerDataCache().getData(player);
 
 		int level = data.getLevel();
 		if (level <= 10)
 			level = 10;
 
 		List<BlockDrop> drops = new ArrayList<BlockDrop>();
 		for (int i = 0; i < level; i++)
 		{
 			if (plugin.getFishDropsMap().containsKey(i))
 			{
 				for (BlockDrop fishDrop : plugin.getFishDropsMap().get(i))
 				{
 					if (fishDrop.getItem() == null)
 						continue;
 
 					if (Util.random(fishDrop.getChance()) == 0)
 					{
 						drops.add(fishDrop);
 					}
 				}
 			}
 		}
 
 		if (! drops.isEmpty())
 		{
 			int rand = Util.random(drops.size());
 			BlockDrop fishDrop = drops.get(rand);
 			if (fishDrop != null)
 			{
 				caught.getWorld().dropItemNaturally(caught.getLocation(), fishDrop.getItem());
 
 				String name = FormatUtil.getFriendlyName(fishDrop.getItem().getType());
 				String article = FormatUtil.getArticle(name);
 				player.sendMessage(plugin.getPrefix() + FormatUtil.format(plugin.getMessage("fishing_drop"), article, name));
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerToggleSprint(PlayerToggleSprintEvent event)
 	{
 		if (! speedBoostEnabled || event.isCancelled())
 			return;
 
 		Player player = event.getPlayer();
 		if (plugin.isDisabledWorld(player))
 			return;
 
 		if (plugin.checkFactions(player, false))
 			return;
 
 		if (player.isSneaking())
 			return;
 
 		if (player.getGameMode() != GameMode.SURVIVAL)
 			return;
 
 		if (player.isSprinting())
 		{
 			if (Util.random(speedBoostOdds) == 0)
 			{
 				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedBoostDuration, speedBoostStrength));
 				player.sendMessage(plugin.getPrefix() + 
 						FormatUtil.format(plugin.getMessage("speed_boost")));
 			}
 		}
 	}
 
 	// TODO: Make sure this actually works
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerToggleSneak(PlayerToggleSneakEvent event)
 	{
 		Player player = event.getPlayer();
 		if (player.isInsideVehicle())
 		{
 			Entity vehicle = player.getVehicle();
 			EntityType type = vehicle.getType();
 			if (type == EntityType.ARROW || type == EntityType.PLAYER)
 			{
 				event.setCancelled(true);
 			}
 		}
 	}
 }
