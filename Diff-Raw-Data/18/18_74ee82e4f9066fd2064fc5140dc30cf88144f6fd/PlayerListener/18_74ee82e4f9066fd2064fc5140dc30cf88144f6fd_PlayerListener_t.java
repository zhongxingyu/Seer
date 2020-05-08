 package net.kiwz.ThePlugin.listeners;
 
 import net.kiwz.ThePlugin.ThePlugin;
 import net.kiwz.ThePlugin.utils.Color;
 import net.kiwz.ThePlugin.utils.Place;
 import net.kiwz.ThePlugin.utils.MyPlayer;
 import net.kiwz.ThePlugin.utils.MyWorld;
 import net.kiwz.ThePlugin.utils.Perm;
 import net.kiwz.ThePlugin.utils.ServerManager;
 import net.kiwz.ThePlugin.utils.Util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.help.HelpTopic;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class PlayerListener implements Listener {
 	private String denyString = Color.WARNING + "Du har ingen tilgang her";
 	private Server server = Bukkit.getServer();
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		MyPlayer myPlayer = MyPlayer.getPlayer(player);
 		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
 		Material soil = Material.SOIL;
 		Material crops = Material.CROPS;
 		Material wDoor = Material.WOODEN_DOOR;
 		Material boat = Material.BOAT;
 		Material cart = Material.MINECART;
 		Material sCart = Material.STORAGE_MINECART;
 		Material pCart = Material.POWERED_MINECART;
 		Material hCart = Material.HOPPER_MINECART;
 		Material wPlate = Material.WOOD_PLATE;
 		Material sPlate = Material.STONE_PLATE;
 		Material iPlate = Material.IRON_PLATE;
 		Material gPlate = Material.GOLD_PLATE;
 		
 		if (!MyWorld.getWorld(block.getWorld()).getTrample() && event.getAction().equals(Action.PHYSICAL)) {
 			if ((block.getType().equals(soil)) || (block.getType().equals(crops))) {
 				event.setCancelled(true);
 				return;
 			}
 		}
 		
 		if (event.getClickedBlock() != null) {
 			Location clickedBlockLoc = event.getClickedBlock().getLocation();
 			Material clickedBlock = event.getClickedBlock().getType();
 			Material heldItem = player.getItemInHand().getType();
 			Place place = Place.getPlace(clickedBlockLoc);
 			
 			if (clickedBlock.equals(wDoor)) {
 				return;
 			}
 			
 			if (place != null) {
 				if (!place.hasAccess(myPlayer)) {
 					event.setCancelled(true);
 					/**
 					 * TODO iPlate og gPlate kaller ikke p denne eventen!
 					 */
 					if (clickedBlock.equals(wPlate) || clickedBlock.equals(sPlate) ||
 							clickedBlock.equals(iPlate) || clickedBlock.equals(gPlate)) {
 					} else {
 						player.updateInventory();
 						player.sendMessage(denyString);
 					}
 				}
 			} else if (!myPlayer.isAdmin() && MyWorld.getWorld(clickedBlockLoc.getWorld()).getClaimable() && clickedBlockLoc.getBlockY() > 40) {
 				if (!(heldItem.equals(boat) || heldItem.equals(cart) || heldItem.equals(sCart) ||
 						heldItem.equals(pCart) || heldItem.equals(hCart))) {
 					event.setCancelled(true);
 					/// M fikse iPlate og gPlate
 					if (clickedBlock.equals(wPlate) || clickedBlock.equals(sPlate) ||
 							clickedBlock.equals(iPlate) || clickedBlock.equals(gPlate)) {
 						return;
 					}
 					player.updateInventory();
 					player.sendMessage(denyString);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
 		Player player = event.getPlayer();
 		MyPlayer myPlayer = MyPlayer.getPlayer(player);
 		
 		if (event.getRightClicked() != null) {
 			Location loc = event.getRightClicked().getLocation();
 			Place place = Place.getPlace(loc);
 			if (place != null) {
 				if (!place.hasAccess(myPlayer)) {
 					event.setCancelled(true);
 					player.sendMessage(denyString);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
 		Player player = event.getPlayer();
 		Location loc = event.getBlockClicked().getLocation();
 		MyPlayer myPlayer = MyPlayer.getPlayer(player);
 		Place place = Place.getPlace(loc);
 		
 		if (place != null) {
 			if (!place.hasAccess(myPlayer)) {
 				event.setCancelled(true);
 				player.sendMessage(denyString);
 			}
 		} else if (!myPlayer.isAdmin() && MyWorld.getWorld(loc.getWorld()).getClaimable() && loc.getBlockY() > 40) {
 			event.setCancelled(true);
 			player.sendMessage(denyString);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
 		Player player = event.getPlayer();
 		Location loc = event.getBlockClicked().getLocation();
 		MyPlayer myPlayer = MyPlayer.getPlayer(player);
 		Place place = Place.getPlace(loc);
 		
 		if (place != null) {
 			if (!place.hasAccess(myPlayer)) {
 				event.setCancelled(true);
 				player.sendMessage(denyString);
 			}
 		} else if (!myPlayer.isAdmin() && MyWorld.getWorld(loc.getWorld()).getClaimable() && loc.getBlockY() > 40) {
 			event.setCancelled(true);
 			player.sendMessage(denyString);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
     public void onInventoryClose(InventoryCloseEvent event) {
 		Inventory inv = event.getView().getTopInventory();
 		InventoryType invType = inv.getType();
 		if (invType.equals(InventoryType.PLAYER)) {
 			Player holder = (Player) inv.getHolder();
 			if (!holder.isOnline()) {
 				ItemStack[] content = inv.getHolder().getInventory().getContents();
 				Player player = MyPlayer.getPlayer(holder.getName()).getOfflinePlayer();
 				player.getInventory().setContents(content);
 				player.saveData();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		if (!event.isCancelled()) {
 			Player player = event.getPlayer();
 			String[] msg = event.getMessage().split(" ");
 			String cmd = msg[0];
 			String arg = "";
 			if (msg.length > 1) arg = msg[1];
 	    	HelpTopic topic = server.getHelpMap().getHelpTopic(cmd);
 	    	if (topic != null) {
 	    		ServerManager.logString("[COMMAND] " + player.getName() + ": " + event.getMessage());
 		    	if (server.getPlayer(arg) != null && (cmd.equalsIgnoreCase("/op") || cmd.equalsIgnoreCase("/deop"))) {
 		    		final Player target = server.getPlayer(arg);
 		    		server.getScheduler().scheduleSyncDelayedTask(ThePlugin.getPlugin(), new Runnable() {
 		    			public void run() {
 		    				Util.setTabColor(target);
 		    			}
 		    		}, 1);
 		    	}
 	    	}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		//event.setFormat("%s: %s");
 		Player player = event.getPlayer();
 		MyPlayer myPlayer = MyPlayer.getPlayer(player);
 		String msg = MyPlayer.getColorName(myPlayer) + Color.PLAYER + ": " + Color.WHITE + event.getMessage();
 		
 		if (myPlayer.isMuted()) {
 			player.sendMessage(Color.WARNING + "En admin har bestemt at du ikke fr snakke av gode grunner");
 			event.setCancelled(true);
 			return;
 		}
 		
 		if (myPlayer.isAdmin()) {
 			//event.setFormat(ChatColor.RED + "%s: " + ChatColor.WHITE + "%s");
 			msg = MyPlayer.getColorName(myPlayer) + Color.ADMIN + ": " + ChatColor.WHITE + event.getMessage();
 		}
 		
 		for (Player otherPlayer : server.getOnlinePlayers()) {
 			MyPlayer myOther = MyPlayer.getPlayer(otherPlayer);
 			if (!myOther.getIgnored().contains(myPlayer)) {
 				otherPlayer.sendMessage(msg);
 			}
 		}
 		
 		ServerManager.logString("[CHAT] " + player.getName() + ": " + event.getMessage());
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		MyPlayer myPlayer = MyPlayer.getPlayer(event.getEntity());
 		if (myPlayer.isDamaged()) {
 			Bukkit.getServer().getScheduler().cancelTask(myPlayer.getDamagedTaskId());
 			myPlayer.setDamaged(false, -1);
 		}
 		myPlayer.setXP(event.getEntity().getTotalExperience());
 		event.setDroppedExp(0);
 		event.setDeathMessage("");
 		Location loc = event.getEntity().getLocation();
 		String world = loc.getWorld().getName();
 		int x = loc.getBlockX();
 		int y = loc.getBlockY();
 		int z = loc.getBlockZ();
 		event.getEntity().sendMessage(Color.WARNING + "Du dde i " + Color.VARIABLE + world
 				+ Color.WARNING + " X: " + Color.VARIABLE + x + Color.WARNING + " Y: " + Color.VARIABLE + y + Color.WARNING + " Z: " + Color.VARIABLE + z);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		final Player player = event.getPlayer();
 		if (player.getBedSpawnLocation() == null) {
 			Bukkit.getScheduler().scheduleSyncDelayedTask(ThePlugin.getPlugin(), new Runnable() {
 				@Override
                 public void run() {
 					player.teleport(MyWorld.getWorld(player.getWorld()).getSpawn());
 					player.giveExp(Math.min(1760, MyPlayer.getPlayer(player).getXP() / 3));
 				}
 			}, 1);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerJoin(PlayerJoinEvent event) {
         final Player player = event.getPlayer();
         String playerName = player.getName();
         String ip = player.getAddress().getAddress().toString().replace("/", "");
         String worldName = player.getWorld().getName();
         String coords = player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ();
         String log;
         
         MyPlayer myPlayer = new MyPlayer(player);
         
         if (!myPlayer.save()) {
         	myPlayer = MyPlayer.getPlayer(player);
         	myPlayer.setName(playerName);
         	myPlayer.setIp(ip);
         	myPlayer.setLastPlayed(System.currentTimeMillis() / 1000);
 	        log = playerName + " logget inn (" + worldName + " " + coords + ") [" + ip + "]";
 			event.setJoinMessage(MyPlayer.getColorName(myPlayer) + Color.HEADER + " logget inn");
 			player.sendMessage(Color.INFO + "############################################");
 	        player.sendMessage(Color.INFO + "Velkommen til LarvikGaming");
 	        player.sendMessage(Color.INFO + "Hjemmeside:" + Color.HEADER + " http://larvikgaming.net");
 	        player.sendMessage(Color.INFO + "Kommandoer:" + Color.HEADER + " http://larvikgaming.net/kommandoer.php");
 	        player.sendMessage(Color.INFO + "Kart:" + Color.HEADER + " http://larvikgaming.net/dynmap.php");
 	        player.sendMessage(Color.INFO + "Forum:" + Color.HEADER + " http://larvikgaming.net/forum");
 	        player.sendMessage(Color.INFO + "Mumble:" + Color.HEADER + " mumble.larvikgaming.net:60000");
 			player.sendMessage(Color.INFO + "############################################");
         } else {
 	        log = playerName + " logget inn for frste gang (" + worldName + " " + coords + ") [" + ip + "]";
 			event.setJoinMessage(MyPlayer.getColorName(myPlayer) + Color.HEADER + " logget inn for frste gang");
 			player.teleport(MyWorld.getWorld(player.getWorld()).getSpawn());
 			Bukkit.getScheduler().scheduleSyncDelayedTask(ThePlugin.getPlugin(), new Runnable() {
 				public void run() {
 					player.teleport(MyWorld.getWorld(player.getWorld()).getSpawn());
 				}
 			}, 10L);
 			giveItem(player, Material.IRON_PICKAXE, 1);
 			giveItem(player, Material.IRON_AXE, 1);
 			giveItem(player, Material.IRON_PICKAXE, 1);
 			giveItem(player, Material.IRON_AXE, 1);
 			giveItem(player, Material.COOKED_BEEF, 20);
 			giveItem(player, Material.GOLD_INGOT, 5);
 			giveItem(player, Material.WOOD, 64);
 			giveItem(player, Material.IRON_HELMET, 1);
 			giveItem(player, Material.IRON_CHESTPLATE, 1);
 			player.sendMessage(Color.INFO + "############################################");
 			player.sendMessage(Color.INFO + "Velkommen som ny spiller p LarvikGaming.net");
 			player.sendMessage(Color.INFO + "Kjekt om du vil lese Info-Tavlen i spawnen og " + Color.HEADER + "/regler");
 			player.sendMessage(Color.INFO + "Skriv " + Color.HEADER + "/hjelp" + Color.INFO +
 					" for hjelp, skriv " + Color.HEADER + "/plass" + Color.INFO + " for beskyttelse");
 			player.sendMessage(Color.INFO + "Skriv " + Color.HEADER + "/spawn farm" + Color.INFO +" for  skaffe materialer");
 			player.sendMessage(Color.INFO + "Skriv " + Color.HEADER + "/spawn world " + Color.INFO + "for  finne ett sted du vil bygge");
 			player.sendMessage(Color.INFO + "Skriv " + Color.HEADER + "/plass ny <plass-navn>" + Color.INFO + " for  lage plass");
 			player.sendMessage(Color.INFO + "Kostnad for  lage eller flytte plass er 5 gullbarer");
 			player.sendMessage(Color.INFO + "Du kan eie 3 plasser og invitere hvem du nsker til din plass");
 			player.sendMessage(Color.INFO + "############################################");
         }
         
         myPlayer = MyPlayer.getPlayer(player);
         
         Perm.setOpPerm(player);
     	if (myPlayer.isAdmin()) {
     		Perm.setAdminPermissions(player);
     	}
     	
         if (!player.isOp()) {
         	player.setGameMode(GameMode.SURVIVAL);
         	player.setAllowFlight(false);
         }
         
         Util.setTabColor(player);
 		ServerManager.logString("[CONN] " + log);
         server.getLogger().info(log);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerLogin(PlayerLoginEvent event) {
 		Player player = event.getPlayer();
 		MyPlayer myPlayer = MyPlayer.getPlayer(player);
		if (myPlayer != null) {
			if (myPlayer.isBanned()) {
				MyPlayer myBannedBy = MyPlayer.getPlayer(myPlayer.getBannedBy());
				event.disallow(PlayerLoginEvent.Result.KICK_BANNED, Color.WARNING + "Du ble bannet av " + MyPlayer.getColorName(myBannedBy)
						+ Color.WARNING + "\nKom tilbake igjen " + Color.VARIABLE + Util.getTimeFullDate(myPlayer.getBanExpire() + 60)
						+ Color.WARNING + "\nBegrunnelse: " + Color.VARIABLE + myPlayer.getBanReason());
			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void OnPlayerQuit(PlayerQuitEvent event) {
 		MyPlayer myPlayer = MyPlayer.getPlayer(event.getPlayer());
 		long curr = (System.currentTimeMillis() / 1000);
 		long login = myPlayer.getLastPlayed();
 		long tot = myPlayer.getTimePlayed();
 		myPlayer.setTimePlayed(curr - login + tot);
 		if (myPlayer.isDamaged()) {
 			event.getPlayer().damage(1000);
 		}
 		event.setQuitMessage(MyPlayer.getColorName(myPlayer) + Color.WARNING + " logget ut");
 		ServerManager.logString("[CONN] " + myPlayer.getName() + " logget ut");
 	}
 	
 	private void giveItem(Player player, Material material, int amount) {
 		PlayerInventory inventory = player.getInventory();
 		ItemStack itemstack = new ItemStack(material, amount);
 		inventory.addItem(itemstack);
 	}
 }
