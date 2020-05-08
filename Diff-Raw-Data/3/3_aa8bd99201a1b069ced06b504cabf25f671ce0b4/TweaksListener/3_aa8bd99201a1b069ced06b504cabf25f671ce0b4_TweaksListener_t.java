 package com.bitlimit.Tweaks;
 
 import org.bukkit.block.Chest;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.weather.LightningStrikeEvent;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.Plugin;
 import java.util.*;
 
 import org.bukkit.event.*;
 import org.bukkit.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.entity.CreatureSpawnEvent.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.metadata.*;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.event.block.*;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.inventory.meta.SkullMeta;
 
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import static com.sk89q.worldguard.bukkit.BukkitUtil.*;
 
 public class TweaksListener implements Listener {
     private final Tweaks plugin; // Reference main plugin
 
     /*********************************************
      Initialization: TweaksListener(plugin)
      ----------- Designated Initializer ----------
      *********************************************/
 
     public TweaksListener(Tweaks plugin) {
         // Notify plugin manager that this plugin handles implemented events (block place, etc.)
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         this.plugin = plugin;
     }
 
     /*********************************************
      Event Handler: onCreatureSpawnEvent(Event)
      --------------- Event Handler --------------
      *********************************************/
 
     @EventHandler
     public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
         // CreatureSpawnEvent (Entity spawnee, CreatureType type, Location loc, SpawnReason reason
 
         FileConfiguration config = this.plugin.getConfig();
         if (!config.getConfigurationSection("preferences").getBoolean("slimes"))
             return;
 
         // Gather information to determine if these are the slimes we are looking for.
         EntityType entityType = event.getEntityType();
         SpawnReason reason = event.getSpawnReason();
         if (entityType == EntityType.SLIME && (reason == SpawnReason.NATURAL || reason == SpawnReason.SLIME_SPLIT))  {
             // Pseudo-randomly cancel slime spawns to reduce their numbers.
             boolean shouldCancel = getRandomBoolean(0.75F);
             event.setCancelled(shouldCancel);
         }
     }
 
     /******************************************
      Event Handler: Block Place(BlockPlaceEvent)
      ----------- Core Event Listener -----------
      ******************************************/
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onBlockPlaceEvent(BlockPlaceEvent event) {
         // Event reference
         // BlockPlaceEvent(Block placedBlock, BlockState replacedBlockState, Block placedAgainst, ItemStack itemInHand, Player thePlayer, boolean canBuild) 
 
         boolean confinementEnabled = this.plugin.getConfig().getConfigurationSection("preferences").getBoolean("tnt");
 
         if (event.getItemInHand().getType() == Material.TNT && confinementEnabled) {
             WorldGuardPlugin worldGuard = getWorldGuard();
             Block block = event.getBlockPlaced();
             Vector pt = toVector(block.getLocation());
             LocalPlayer localPlayer = worldGuard.wrapPlayer(event.getPlayer());
 
             RegionManager regionManager = worldGuard.getRegionManager(event.getPlayer().getWorld());
             ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
 
             if (set.size() == 0)
                 event.setCancelled(true);
             else
                 event.setCancelled(!set.isOwnerOfAll(localPlayer));
 
             if (event.isCancelled()) {
                 displaySmokeInWorldAtLocation(block.getWorld(), block.getLocation());
                 event.getPlayer().sendMessage(ChatColor.RED + "You are not authorized to place TNT in this location.");
             }
         }
 
         if (event.getItemInHand().getType() == Material.SKULL_ITEM || event.getItemInHand().getType() == Material.SKULL)
         {
             SkullMeta skullMeta = (SkullMeta)event.getItemInHand().getItemMeta();
             List<String> lore = skullMeta.getLore();
 
 	        if (lore != null)
 	        {
                 StringBuilder builder = new StringBuilder();
                 for (String value : lore)
                 {
                     builder.append(value);
                 }
 
                 String builtString = builder.toString();
                 String strippedString = ChatColor.stripColor(builtString);
                 builtString = builtString.replaceFirst(strippedString.substring(0, 1), strippedString.substring(0, 1).toLowerCase());
 	            event.getBlockPlaced().setMetadata("com.bitlimit.Tweaks.lore", new FixedMetadataValue(this.plugin, skullMeta.getLore()));
 		        event.getBlockPlaced().setMetadata("com.bitlimit.Tweaks.display", new FixedMetadataValue(this.plugin, ChatColor.YELLOW + skullMeta.getOwner() + ChatColor.AQUA + " was " + builtString));
 	        }
 	        else
 	        {
 		        event.getBlockPlaced().setMetadata("com.bitlimit.Tweaks.name", new FixedMetadataValue(this.plugin, skullMeta.getDisplayName()));
 	        }
 
         }
     }
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onPlayerInteractEvent(PlayerInteractEvent event) {
 
         Block block = event.getClickedBlock();
         if (block == null)
             return;
 
 		Player player = event.getPlayer();
 
 		if (block.hasMetadata("com.bitlimit.Tweaks.display"))
         {
 			List<MetadataValue> metadataValueList = event.getClickedBlock().getMetadata("com.bitlimit.Tweaks.display");
 
             if (metadataValueList.size() > 0) {
                 for (MetadataValue metadataValue : metadataValueList) {
                     String metaString = metadataValue.asString();
                     player.sendMessage(metaString);
                }
           }
         }
 
 	    if (block.getType() == Material.CAKE_BLOCK && player.getFoodLevel() < 20)
 	    {
 			this.onBlockBreakEvent(new BlockBreakEvent(block, player));
 	    }
     }
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onBlockBreakEvent(BlockBreakEvent event) {
         Block block = event.getBlock();
 
         if (block.hasMetadata("com.bitlimit.Tweaks.display"))
         {
 	        block.removeMetadata("com.bitlimit.Tweaks.display", this.plugin);
         }
 		else if (block.hasMetadata("com.bitlimit.Tweaks.name"))
         {
 	        List<MetadataValue> metadataValueList = block.getMetadata("com.bitlimit.Tweaks.name");
 	        String displayName = (String)metadataValueList.get(0).value();
 
 	        ItemStack itemStack = (ItemStack)block.getDrops().iterator().next();
             ItemMeta itemMeta = itemStack.getItemMeta();
 	        itemMeta.setDisplayName(displayName);
 	        itemStack.setItemMeta(itemMeta);
 
 
 	        block.getLocation().getWorld().dropItemNaturally(block.getLocation(), itemStack);
 
 	        event.setCancelled(true);
 	        block.setType(Material.AIR);
 
 	        block.removeMetadata("com.bitlimit.Tweaks.name", this.plugin);
 
 	        return;
         }
 
         if (block.hasMetadata("com.bitlimit.Tweaks.lore")) {
             List<MetadataValue> metadataValueList = block.getMetadata("com.bitlimit.Tweaks.lore");
             ItemStack itemStack = (ItemStack)block.getDrops().iterator().next();
 
             if (event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                 ArrayList<String> lore = new ArrayList<String>();
                 for (MetadataValue metadataValue : metadataValueList) {
                     lore.add(metadataValue.asString().substring(1, metadataValue.asString().length() - 1));
                 }
 
                 ItemMeta newMeta = itemStack.getItemMeta();
                 newMeta.setLore(lore);
                 itemStack.setItemMeta(newMeta);
             }
 
             block.getLocation().getWorld().dropItemNaturally(block.getLocation(), itemStack);
 
             event.setCancelled(true);
             block.setType(Material.AIR);
 
             block.removeMetadata("com.bitlimit.Tweaks.lore", this.plugin);
         }
 
 	    if (MHFBlocks().containsKey(block.getType()))
 	    {
 		    float probability = 0.01F;
 
 		    if (block.getType() == Material.CAKE_BLOCK)
 		    {
 			    probability = 0.09F;
 		    }
 		    else if (block.getType() == Material.MELON_BLOCK || block.getType() == Material.PUMPKIN)
 		    {
 			    probability = 0.005F;
 		    }
 
 		    if (event.getPlayer() != null)
 		    {
 			    Player player = event.getPlayer();
 			    if (player.getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS))
 			    {
 				    float enchantmentLevel = player.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
 
 				    enchantmentLevel += 1F;
 				    enchantmentLevel *= 0.75F;
 
 				    probability *= enchantmentLevel;
 			    }
 		    }
 
 		    boolean shouldDrop = getRandomBoolean(probability);
 		    if (!shouldDrop)
 		    {
 			    return;
 		    }
 
 		    ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
 
 		    SkullMeta meta = (SkullMeta)head.getItemMeta();
 		    meta.setOwner(getMHFNameForBlockType(block.getType()));
 		    meta.setDisplayName(humanize2(block.getType().toString().toLowerCase()).replace("Tnt", "TNT"));
 		    head.setItemMeta(meta);
 
 		    class DelayedLocationTask implements Runnable
 		    {
 			    private final ItemStack head;
 			    private final Location location;
 
 
 			    DelayedLocationTask(ItemStack head, Location location)
 			    {
 				    this.head = head;
 				    this.location = location;
 			    }
 
 			    public void run()
 			    {
 				    location.getWorld().dropItemNaturally(location, head);
 			    }
 		    }
 
 		    Location location = block.getLocation().add(0, 2, 0);
 			Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new DelayedLocationTask(head, location), 10L);
 	    }
     }
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPistonExtendEvent(BlockPistonExtendEvent event)
 	{
 		Block movedBlock = event.getBlock();
 		Block pistonRelative = movedBlock.getRelative(event.getDirection());
 
 		if (MHFFarmableBlock().contains(pistonRelative.getType()))
 		{
 			this.onBlockBreakEvent(new BlockBreakEvent(pistonRelative, null));
 
 			return;
 		}
 
 		for (Block block : event.getBlocks())
 		{
 			Block relative = block.getRelative(event.getDirection());
 
 			if (MHFFarmableBlock().contains(relative.getType()))
 			{
 				this.onBlockBreakEvent(new BlockBreakEvent(relative, null));
 
 				return;
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onBlockPhysicsEvent(BlockPhysicsEvent event)
 	{
 		if (event.getBlock().getType() == Material.CACTUS)
 		{
 			class DelayedCactusCheckTask implements Runnable
 			{
 				private final Block block;
 				DelayedCactusCheckTask(Block block)
 				{
 					this.block = block;
 				}
 
 				public void run()
 				{
 					if (this.block.getType() != Material.CACTUS)
 					{
 						boolean shouldDrop = getRandomBoolean(0.005F);
 						if (!shouldDrop)
 						{
 							return;
 						}
 
 						ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
 
 						SkullMeta meta = (SkullMeta)head.getItemMeta();
 						meta.setOwner((String)MHFBlocks().get(Material.CACTUS));
 						meta.setDisplayName(humanize2(Material.CACTUS.toString().toLowerCase()).replace("Tnt", "TNT"));
 						head.setItemMeta(meta);
 
 						Location location = block.getLocation();
 
 						Item item = location.getWorld().dropItem(location, head);
 						item.setVelocity(new org.bukkit.util.Vector(0.2, 0.2, 0.2));
 					}
 				}
 			}
 
 			Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new DelayedCactusCheckTask(event.getBlock()), 1L);
 		}
 	}
 
 	/******************************************
 			  Event Handler: Head Drops
 	 ----------- Core Event Listener -----------
 	 ******************************************/
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerDeath(PlayerDeathEvent event) {
         if (event.getEntity().getKiller() == null)
             return;
 
         if (event.getEntity().getKiller() instanceof Player) {
             ItemStack skullStack = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
 
             SkullMeta meta = (SkullMeta)skullStack.getItemMeta();
             meta.setOwner(event.getEntity().getDisplayName());
 
             Player killer = event.getEntity().getKiller();
             if (killer.getItemInHand().containsEnchantment(Enchantment.DAMAGE_ARTHROPODS)) {
                 ArrayList lore = new ArrayList();
                 lore.add(ChatColor.AQUA + "Slain by " + ChatColor.GOLD + event.getEntity().getKiller().getDisplayName() + ChatColor.AQUA + " on " + getFriendlyDate(Calendar.getInstance()));
                 meta.setLore(lore);
             }
 
             skullStack.setItemMeta(meta);
             event.getDrops().add(skullStack);
         }
     }
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onEntityDeathEvent(EntityDeathEvent event)
 	{
 		LivingEntity entity = event.getEntity();
 		if (entity instanceof Skeleton)
 		{
 			Skeleton skeleton = (Skeleton)entity;
 			if (skeleton.getSkeletonType() == Skeleton.SkeletonType.WITHER)
 			{
 				return;
 			}
 		}
 
 		float probability = 0.01F;
 		EntityType entityType = entity.getType();
 
 		if (entityType == EntityType.GHAST)
 		{
 			probability = 0.4F;
 		}
 		else if (entityType == EntityType.CREEPER)
 		{
 			probability = 0.18F;
 		}
 		else if (entityType == EntityType.SQUID || entityType == EntityType.SLIME)
 		{
 			probability = 0.08F;
 		}
 		else if (entityType == EntityType.WITHER)
 		{
 			probability = 1F;
 		}
 
 		if (entity.getKiller() != null)
 		{
 			Player player = entity.getKiller();
 			if (player.getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS))
 			{
 				float enchantmentLevel = player.getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
 
 				enchantmentLevel += 1F;
 				enchantmentLevel *= 0.75F;
 
 				probability *= enchantmentLevel;
 			}
 		}
 
 		boolean shouldDrop = getRandomBoolean(probability);
 		if (!MHFNames().containsKey(entity.getType()) || !shouldDrop)
 		{
 			 return;
 		}
 
 		String MHFName = getMHFNameForEntity(entity);
 
 		ItemStack head = null;
 		if (MHFName.equals("special"))
 		{
 			int type = 0;
 
 			if (entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.GIANT)
 			{
 				type = 2;
 			}
 			else if (entity.getType() == EntityType.CREEPER)
 			{
 				type = 4;
 			}
 
 			head = new ItemStack(Material.SKULL_ITEM, 1, (byte)type);
 
 			SkullMeta meta = (SkullMeta)head.getItemMeta();
 			meta.setDisplayName(humanize2(entity.getType().toString().toLowerCase() + " Head"));
 			head.setItemMeta(meta);
 		}
 		else
 		{
 			head = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
 
 			SkullMeta meta = (SkullMeta)head.getItemMeta();
 			meta.setOwner(MHFName);
 			meta.setDisplayName(humanize2(entity.getType().toString().toLowerCase() + " Head"));
 			head.setItemMeta(meta);
 		}
 
 		event.getDrops().add(head);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onLightningStrike(LightningStrikeEvent event)
 	{
 		Location location = event.getLightning().getLocation();
 
 		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
 
 		SkullMeta meta = (SkullMeta)head.getItemMeta();
 		meta.setOwner(getMHFNameForEntity(event.getLightning()));
 		meta.setDisplayName("Herobrine Head");
 		head.setItemMeta(meta);
 
 		Random random = new Random();
 
 		location.getWorld().dropItemNaturally(location.add(random.nextInt(10) - 7, 50, 0), head);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onChatEvent(AsyncPlayerChatEvent event)
 	{
 		boolean shouldNotDropItem = getRandomBoolean(0.97F);
 
 		if (shouldNotDropItem)
 		{
 			return;
 		}
 
 		class ChatHandlerTask implements Runnable
 		{
 			private final AsyncPlayerChatEvent event;
 
 			ChatHandlerTask(AsyncPlayerChatEvent event)
 			{
 				this.event = event;
 			}
 
 			public void run()
 			{
 				Player player = this.event.getPlayer();
 				if (player == null)
 				{
 					return;
 				}
 
 				Location location = player.getLocation();
 				if (location == null)
 				{
 					return;
 				}
 
 				ArrayList<String> bonuses = MHFBonuses();
 				String bonus = bonuses.get(new Random().nextInt(bonuses.size()));
 
 				ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
 
 				SkullMeta meta = (SkullMeta)head.getItemMeta();
 				meta.setOwner(bonus);
 				meta.setDisplayName(humanize2(bonus.replace("MHF_", "").replace("Arrow", "Arrow ") + " Head"));
 				head.setItemMeta(meta);
 
 				location.getWorld().dropItemNaturally(location.add(0, 2, 0), head);
 			}
 		}
 
 		Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new ChatHandlerTask(event));
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onExplosionEvent(EntityExplodeEvent event)
 	{
 		if (event.getEntity() instanceof TNTPrimed)
 		{
 			TNTPrimed entityTNT = (TNTPrimed)event.getEntity();
 
 
 			boolean shouldDrop = getRandomBoolean(0.25F);
 			if (!shouldDrop)
 			{
 				return;
 			}
 
 			ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
 
 			SkullMeta meta = (SkullMeta)head.getItemMeta();
 			meta.setOwner(getMHFNameForBlockType(Material.TNT));
 			meta.setDisplayName("TNT");
 			head.setItemMeta(meta);
 
 			Location location = entityTNT.getLocation();
 			Item item = location.getWorld().dropItemNaturally(location.add(0, 0, 0), head);
 		}
 	}
 
     /******************************************
           Event Handler: First Join Events
      --------------- Core Event ----------------
      *****************************************/
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerJoinEvent(PlayerJoinEvent event) {
         if (!event.getPlayer().hasPlayedBefore()) {
             if (!this.plugin.getConfig().getConfigurationSection("preferences").getBoolean("spawnItems"))
                 return;
 
             Location location = this.parseLocation(this.plugin.getConfig().getConfigurationSection("meta").getConfigurationSection("spawnItems").getConfigurationSection("location"));
             Block block = location.getWorld().getBlockAt(location);
 
             if (block.getType() == Material.CHEST) {
                 Chest chestBlock = (Chest)block.getState();
 
                 int index = 0;
                 for (ItemStack itemStack : chestBlock.getInventory().getContents()) {
                     event.getPlayer().getInventory().setItem(index, itemStack);
 
                     if (index < 27)
                         index++;
                 }
             }
         }
     }
 
     /******************************************
      External Getter: Returns World Guard Plugin
      ---------- Dependency Convenience ---------
      ******************************************/
 
     private WorldGuardPlugin getWorldGuard() {
         Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
 
         // WorldGuard may not be loaded
         if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
             return null; // Maybe you want throw an exception instead
         }
 
         return (WorldGuardPlugin) plugin;
     }
 
     /*********************************************
      ------------ Convenience Methods ------------
      *********************************************/
 
     public Location parseLocation(ConfigurationSection locationSection) {
         Location location = new Location(Bukkit.getWorld(locationSection.getString("world")), locationSection.getDouble("x"), locationSection.getDouble("y"), locationSection.getDouble("z"), Float.parseFloat(locationSection.getString("yaw")), Float.parseFloat(locationSection.getString("pitch")));
 
         return location;
     }
 
     public boolean getRandomBoolean(float probability)
     {
         Random random = new Random();
 
         return (random.nextInt(100) < (probability * 100));
     }
 
     private void displaySmokeInWorldAtLocation(World world, Location location) {
         world.playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
     }
 
     // Overloaded function to cut down on calling arguments, no default parameters in Java =/
     private static String getFriendlyDate(Calendar theDate)
     {
         return getFriendlyDate(theDate, false);
     }
 
     // Function to get a human readable version of a Calendar object
     // If verbose is true we slightly expand the date wording
     private static String getFriendlyDate(Calendar theDate, boolean verbose)
     {
         int year       = theDate.get(Calendar.YEAR);
         int month      = theDate.get(Calendar.MONTH);
         int dayOfMonth = theDate.get(Calendar.DAY_OF_MONTH);
         int dayOfWeek  = theDate.get(Calendar.DAY_OF_WEEK);
 
         // Get the day of the week as a String.
         // Note: The Calendar DAY_OF_WEEK property is NOT zero-based, and Sunday is the first day of week.
         String friendly = "";
         switch (dayOfWeek)
         {
             case 1:
                 friendly = "Sunday";
                 break;
             case 2:
                 friendly = "Monday";
                 break;
             case 3:
                 friendly = "Tuesday";
                 break;
             case 4:
                 friendly = "Wednesday";
                 break;
             case 5:
                 friendly = "Thursday";
                 break;
             case 6:
                 friendly = "Friday";
                 break;
             case 7:
                 friendly = "Saturday";
                 break;
             default:
                 friendly = "BadDayValue";
                 break;
         }
 
         // Add padding and the prefix to the day of month
         if (verbose == true)
         {
             friendly += " the " + dayOfMonth;
         }
         else
         {
             friendly += ", " + dayOfMonth;
         }
 
         String dayString = String.valueOf(dayOfMonth);   // Convert dayOfMonth to String using valueOf
 
         // Suffix is "th" for day of day of month values ending in 0, 4, 5, 6, 7, 8, and 9
         if (dayString.endsWith("0") || dayString.endsWith("4") || dayString.endsWith("5") || dayString.endsWith("6") ||
                 dayString.endsWith("7") || dayString.endsWith("8") || dayString.endsWith("9") || dayString.equals("13"))
         {
             friendly += "th ";
         } else if (dayString.endsWith("1"))
         {
             friendly += "st ";
         } else if (dayString.endsWith("2"))
         {
             friendly += "nd ";
         } else if (dayString.endsWith("3"))
         {
             friendly += "rd ";
         }
 
         // Add more padding if we've been asked to be verbose
         if (verbose == true)
         {
             friendly += "of ";
         }
 
 
         // Get a friendly version of the month.
         // Note: The Calendar MONTH property is zero-based to increase the chance of developers making mistakes.
         switch (month)
         {
             case 0:
                 friendly += "January";
                 break;
             case 1:
                 friendly += "February";
                 break;
             case 2:
                 friendly += "March";
                 break;
             case 3:
                 friendly += "April";
                 break;
             case 4:
                 friendly += "May";
                 break;
             case 5:
                 friendly += "June";
                 break;
             case 6:
                 friendly += "July";
                 break;
             case 7:
                 friendly += "August";
                 break;
             case 8:
                 friendly += "September";
                 break;
             case 9:
                 friendly += "October";
                 break;
             case 10:
                 friendly += "November";
                 break;
             case 11:
                 friendly += "December";
                 break;
             default:
                 friendly += "BadMonthValue";
                 break;
         }
 
         // Tack on the year and we're done. Phew!
         friendly += " " + year;
 
         return friendly;
 
     } // End of getFriendlyDate function
 
 	/**
 	 * Returns the given underscored_word_group as a Human Readable Word Group.
 	 * (Underscores are replaced by spaces and capitalized following words.)
 	 *
 	 * @param pWord
 	 *            String to be made more readable
 	 * @return Human-readable string
 	 */
 	public static String humanize2(String pWord)
 	{
 		StringBuilder sb = new StringBuilder();
 		String[] words = pWord.replaceAll("_", " ").split("\\s");
 		for (int i = 0; i < words.length; i++)
 		{
 			if (i > 0)
 				sb.append(" ");
 			if (words[i].length() > 0)
 			{
 				sb.append(Character.toUpperCase(words[i].charAt(0)));
 				if (words[i].length() > 1)
 				{
 					sb.append(words[i].substring(1));
 				}
 			}
 		}
 		return sb.toString();
 	}
 
 	private static String getMHFNameForEntity(Entity entity)
 	{
 		return MHFNames().get(entity.getType());
 	}
 
 	private static HashMap <EntityType, String> MHFNames()
 	{
 		HashMap <EntityType, String> entityNames = new HashMap<EntityType, String>();
 
 		entityNames.put(EntityType.CREEPER, "special");
 		entityNames.put(EntityType.GIANT, "special");
 		entityNames.put(EntityType.ZOMBIE, "special");
 		entityNames.put(EntityType.SKELETON, "special");
 
 		entityNames.put(EntityType.LIGHTNING, "MHF_Herobrine");
 
 		entityNames.put(EntityType.BLAZE, "MHF_Blaze");
 		entityNames.put(EntityType.CAVE_SPIDER, "MHF_CaveSpider");
 		entityNames.put(EntityType.CHICKEN, "MHF_Chicken");
 		entityNames.put(EntityType.COW, "MHF_Cow");
 		entityNames.put(EntityType.ENDERMAN, "MHF_Enderman");
 		entityNames.put(EntityType.GHAST, "MHF_Ghast");
 		entityNames.put(EntityType.IRON_GOLEM, "MHF_Golem");
 		entityNames.put(EntityType.MAGMA_CUBE, "MHF_LavaSlime");
 		entityNames.put(EntityType.MUSHROOM_COW, "MHF_MushroomCow");
 		entityNames.put(EntityType.OCELOT, "MHF_Ocelot");
 		entityNames.put(EntityType.PIG, "MHF_Pig");
 		entityNames.put(EntityType.PIG_ZOMBIE, "MHF_PigZombie");
 		entityNames.put(EntityType.SHEEP, "MHF_Sheep");
 		entityNames.put(EntityType.SLIME, "MHF_Slime");
 		entityNames.put(EntityType.SPIDER, "MHF_Spider");
 		entityNames.put(EntityType.SQUID, "MHF_Squid");
 		entityNames.put(EntityType.VILLAGER, "MHF_Villager");
 		entityNames.put(EntityType.WITHER, "MHF_Wither");
 
 		return entityNames;
 	}
 
 	private static ArrayList<String> MHFBonuses()
 	{
 		ArrayList bonuses = new ArrayList<String>();
 		bonuses.add("MHF_ArrowUp");
 		bonuses.add("MHF_ArrowDown");
 		bonuses.add("MHF_ArrowLeft");
 		bonuses.add("MHF_ArrowRight");
 		bonuses.add("MHF_Exclamation");
 		bonuses.add("MHF_Question");
 
 		return bonuses;
 	}
 
 	private static String getMHFNameForBlockType(Material type)
 	{
 		Object unknown = MHFBlocks().get(type);
 
 		if (unknown instanceof String)
 		{
 			return (String)unknown;
 		}
 		else
 		{
 			String[] names = (String[])unknown;
 			return names[new Random().nextInt(names.length)];
 		}
 	}
 
 	private static HashMap <Material, Object> MHFBlocks()
 	{
 		HashMap <Material, Object> blockNames = new HashMap<Material, Object>();
 
 		blockNames.put(Material.CACTUS, "MHF_Cactus");
 		blockNames.put(Material.CAKE_BLOCK, "MHF_Cake");
 		blockNames.put(Material.CHEST, "MHF_Chest");
 		blockNames.put(Material.MELON_BLOCK, "MHF_Melon");
 		blockNames.put(Material.LOG, "MHF_OakLog");
 		blockNames.put(Material.PUMPKIN, "MHF_Pumpkin");
 
 		String[] TNTNames = {"MHF_TNT", "MHF_TNT2"};
 		blockNames.put(Material.TNT, TNTNames);
 
 		return blockNames;
 	}
 
 	private static ArrayList<Material> MHFFarmableBlock()
 	{
 		ArrayList farmables = new ArrayList<Material>();
 		farmables.add(Material.PUMPKIN);
 		farmables.add(Material.MELON_BLOCK);
 
 		return farmables;
 	}
 }
 
