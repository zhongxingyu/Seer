 package me.TfT02.Assassin;
 
 import java.util.ArrayList;
 
 import me.TfT02.Assassin.util.PlayerData;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.material.MaterialData;
 import org.kitteh.tag.TagAPI;
 
 public class AssassinMode {
 
 	Assassin plugin;
 
 	public AssassinMode(Assassin instance) {
 		plugin = instance;
 	}
 
 	private PlayerData data = new PlayerData(plugin);
 
 	/**
 	 * Applies all the Assassin traits,
 	 * such as a different display name, nametag and helmet item.
 	 * 
 	 * @param player Player whom will be given the traits.
 	 */
 	public void applyTraits(final Player player) {
 		data.addLoginTime(player);
 		Bukkit.getScheduler().scheduleSyncDelayedTask(Assassin.getInstance(), new Runnable() {
 			@Override
 			public void run() {
 				player.sendMessage(ChatColor.DARK_RED + "YOU ARE NOW AN ASSASSIN");
 				String timeleft = data.getStringTimeLeft(player);
 				player.sendMessage(ChatColor.GOLD + "Time left in Assassin Mode = " + ChatColor.DARK_RED + timeleft);
 			}
 		}, 20 * 1);
 
 		player.setDisplayName(ChatColor.DARK_RED + "[ASSASSIN]" + ChatColor.RESET);
		int number = data.getAssassinNumber(player);
		player.setPlayerListName(ChatColor.DARK_RED + "ASSASSIN [" + number + "]");
 		TagAPI.refreshPlayer(player);
 	}
 
 	/**
 	 * Activate Assassin mode.
 	 * 
 	 * @param player Player who's mode will be changed.
 	 */
 	public void activateAssassin(Player player) {
 		data.addAssassin(player);
 		applyTraits(player);
 		Location location = player.getLocation();
 		data.addLocation(player, location);
 		Location loc = player.getLocation();
 		loc.setY(player.getWorld().getMaxHeight() + 30D);
 		player.getWorld().strikeLightningEffect(loc);
 		if (Assassin.getInstance().getConfig().getBoolean("Assassin.warn_others_on_activation")) {
 			double messageDistance = Assassin.getInstance().getConfig().getDouble("Assassin.messages_distance");
 			for (Player players : player.getWorld().getPlayers()) {
 				if (messageDistance > 0) {
 					if (players != player && players.getLocation().distance(player.getLocation()) < messageDistance) {
 						players.sendMessage(ChatColor.DARK_RED + "SOMEONE JUST PUT ON HIS MASK!");
 					} else {
 					}
 				}
 			}
 		}
 		applyMask(player);
 		data.addCooldownTimer(player);
 		if (Assassin.getInstance().getConfig().getBoolean("Assassin.particle_effects")) {
 			player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 1);
 		}
 	}
 
 	/**
 	 * Deactivate Assassin mode.
 	 * 
 	 * @param player Player who's mode will be changed.
 	 */
 	public void deactivateAssassin(Player player) {
 		String playername = player.getName();
 		data.leaveAssassinChat(player);
 		data.setNeutral(player);
 		player.sendMessage(ChatColor.GRAY + "ASSASSIN MODE DEACTIVATED");
 
 		player.setDisplayName(playername);
 		TagAPI.refreshPlayer(player);
 		removeMask(player);
 		if (Assassin.getInstance().getConfig().getBoolean("Assassin.teleport_on_deactivate")) {
 			Location previousLocation = data.getLocation(player);
			if (previousLocation == null){
				player.sendMessage(ChatColor.RED + "Location not found!");
			}
			else {
				player.teleport(previousLocation);
			}
 		}
 	}
 
 	/**
 	 * Applies a mask on the players head.
 	 * Also gives back the helmet the player was wearing, if any.
 	 * 
 	 * @param player Player who will get a mask.
 	 */
 	@SuppressWarnings("deprecation")
 	public void applyMask(Player player) {
 		PlayerInventory inventory = player.getInventory();
 		ItemStack assassinMask = getMaskPlain();
 
 		ItemStack itemHead = inventory.getHelmet();
 		int amountInHand = inventory.getItemInHand().getAmount();
 		int amount;
 		if (amountInHand > 1) {
 			amount = amountInHand - 1;
 		} else {
 			amount = 0;
 		}
 		ItemStack assassinMasks = getMask(amount);
 
 		int emptySlot = inventory.firstEmpty();
 		if (itemHead != null) {
 			inventory.setItem(emptySlot, itemHead);
 			inventory.setItemInHand(assassinMasks);
 		} else
 			inventory.setItemInHand(assassinMasks);
 
 		inventory.setHelmet(assassinMask);
 		player.updateInventory();
 	}
 
 	/**
 	 * Applies a mask on the players head with force.
 	 * 
 	 * @param player Player who will get a mask.
 	 */
 	@SuppressWarnings("deprecation")
 	public void applyMaskForce(Player player) {
 		PlayerInventory inventory = player.getInventory();
 		ItemStack assassinMask = getMaskPlain();
 
 		inventory.setHelmet(assassinMask);
 		player.updateInventory();
 	}
 
 	/**
 	 * Removes a mask on the players head.
 	 * Also puts back the helmet on the player, if any.
 	 * 
 	 * @param player Player who will lose a mask.
 	 */
 	@SuppressWarnings("deprecation")
 	public void removeMask(Player player) {
 		PlayerInventory inventory = player.getInventory();
 		ItemStack itemHead = inventory.getHelmet();
 		if (itemHead.getTypeId() != 0) inventory.setHelmet(new ItemStack(0));
 		//Gives back the mask if config says so
 		if (Assassin.getInstance().getConfig().getBoolean("Assassin.return_mask")) spawnMask(player, 1);
 
 		//If the player was wearing a helmet, put it back on
 		int helmetindex = -1;
 		if (inventory.contains(Material.DIAMOND_HELMET))
 			helmetindex = inventory.first(Material.DIAMOND_HELMET);
 		else if (inventory.contains(Material.IRON_HELMET))
 			helmetindex = inventory.first(Material.IRON_HELMET);
 		else if (inventory.contains(Material.GOLD_HELMET))
 			helmetindex = inventory.first(Material.GOLD_HELMET);
 		else if (inventory.contains(Material.LEATHER_HELMET)) helmetindex = inventory.first(Material.LEATHER_HELMET);
 		if (helmetindex >= 0) {
 			ItemStack helmet = inventory.getItem(helmetindex);
 			inventory.setItem(helmetindex, new ItemStack(0));
 			inventory.setHelmet(helmet);
 		}
 		player.updateInventory();
 	}
 
 	/**
 	 * Spawns a mask in inventory.
 	 * 
 	 * @param player Player who will receive a mask.
 	 */
 	@SuppressWarnings("deprecation")
 	public void spawnMask(Player player, int amount) {
 		PlayerInventory inventory = player.getInventory();
 		ItemStack assassinMask = getMask(amount);
 		int emptySlot = inventory.firstEmpty();
 		inventory.setItem(emptySlot, assassinMask);
 		player.updateInventory();
 	}
 	public ItemStack getMask(int amount){
 		MaterialData blackWool = new MaterialData(Material.WOOL, (byte) 15);
 		ItemStack is = blackWool.toItemStack(amount);
 		ItemMeta im = is.getItemMeta();
 		im.setDisplayName(ChatColor.DARK_RED + "Assassin Mask");
 		ArrayList<String> lore = new ArrayList<String>();
 		lore.add(ChatColor.GRAY + "Allows PVP");
 		lore.add("Hold in your hand and right-click");
 		lore.add("to activate assassin mode.");
 		im.setLore(lore);
 		is.setItemMeta(im);
 		return is;
 	}
 	public ItemStack getMaskPlain(){
 		MaterialData blackWool = new MaterialData(Material.WOOL, (byte) 15);
 		ItemStack is = blackWool.toItemStack();
 		ItemMeta im = is.getItemMeta();
 		im.setDisplayName(ChatColor.DARK_RED + "Assassin Mask");
 		ArrayList<String> lore = new ArrayList<String>();
 		lore.add(ChatColor.GRAY + "Allows PVP");
 		im.setLore(lore);
 		is.setItemMeta(im);
 		return is;
 	}
 }
