 package com.useful.useful;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.EntityEffect;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Dispenser;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandException;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityTeleportEvent;
 import org.bukkit.event.entity.ExplosionPrimeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerInventoryEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.server.PluginEvent;
 import org.bukkit.event.server.ServerCommandEvent;
 import org.bukkit.event.server.ServerListPingEvent;
 import org.bukkit.event.vehicle.VehicleCreateEvent;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.event.vehicle.VehicleExitEvent;
 import org.bukkit.event.vehicle.VehicleMoveEvent;
 import org.bukkit.event.vehicle.VehicleUpdateEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.material.Attachable;
 import org.bukkit.material.Diode;
 import org.bukkit.material.Lever;
 import org.bukkit.material.MaterialData;
 import org.bukkit.material.Redstone;
 import org.bukkit.material.RedstoneTorch;
 import org.bukkit.material.RedstoneWire;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.permissions.ServerOperator;
 import org.bukkit.util.Vector;
 import com.useful.useful.utils.*;
 
 public class UsefulListener implements Listener{
 	public static HashMap<String, ItemStack[]> playerInvs = new HashMap<String, ItemStack[]>();
 	
 	private useful plugin;
 	
 	public ListStore heros;
 	
 	public UsefulListener(useful plugin){
 		this.plugin = useful.plugin;
 	}
 	
 	@SuppressWarnings("deprecation")
 	public boolean carBoost(String playerName, final double power, final long lengthMillis, double defaultSpeed){
 		//TODO
 		final String p = playerName;
 		final double defMult = defaultSpeed;
 		if(!useful.carBoosts.containsKey(p)){
 			useful.carBoosts.put(p, (double)30);
 		}
 		final double Cur = useful.carBoosts.get(p);
 		if(Cur > defMult){
 			//Already boosting!
 			return false;
 		}
 		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){
 			public void run(){
 				World w = plugin.getServer().getPlayer(p).getLocation().getWorld();
 				w.playSound(plugin.getServer().getPlayer(p).getLocation(), Sound.FIZZ, 10, -2);
 				double speed = Cur + power;
 				useful.carBoosts.put(p, speed);
 				//Boosting!
 				try {
 					Thread.sleep(lengthMillis);
 				} catch (InterruptedException e) {
 					useful.carBoosts.put(p, defMult);
 					return;
 				}
 				//paused for set time!
 				useful.carBoosts.put(p, defMult);
 				//resumed normal speed!
 				return;
 			}
 		});
 		return true;
 	}
 	
 	public void ResetCarBoost(String playername, Minecart car, double defaultSpeed){
 		String p = playername;
 		World w = plugin.getServer().getPlayer(p).getLocation().getWorld();
 		w.playSound(plugin.getServer().getPlayer(p).getLocation(), Sound.BAT_TAKEOFF, 10, -2);
 		useful.carBoosts.put(p, defaultSpeed);
 		return;
 	}
 	
 	public boolean inACar(String playername){
 		Player p = plugin.getServer().getPlayer(playername);
 		if(p == null){
 			//Should NEVER happen(It means they r offline)
 			return false;
 		}
 	    if(!p.isInsideVehicle()){
 	    	return false;
 	    }
 	    Entity ent = p.getVehicle();
 		if(!(ent instanceof Vehicle)){
 			return false;
 		}
 		Vehicle veh = (Vehicle) ent;
 		if(!(veh instanceof Minecart)){
 			return false;
 		}
 		Minecart cart = (Minecart) veh;
 		Location loc = cart.getLocation();
 		float id = loc.getBlock().getTypeId();
 		if(id == 27 || id == 66 || id == 28){
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean isACar(Minecart cart){
 		Location loc = cart.getLocation();
 		float id = loc.getBlock().getTypeId();
 		if(id == 27 || id == 66 || id == 28){
 			return false;
 		}
 		return true;
 	}
 	
 	public double getJailTime(Player p) {
 		String name = p.getName();
 		if (useful.jailed.containsKey(name)){
 			ArrayList<String> array = JailInfo.getPlayerJailInfo(name);
 			String time = array.get(0);
 			double time2 = Double.parseDouble(time);
 			return time2;
 		}
 		else {
 		return 0;
 		}
 		
 	}
 	
 	public String getJail(Player p) {
 		String name = p.getName();
 		if (useful.jailed.containsKey(name)){
 			ArrayList<String> array = JailInfo.getPlayerJailInfo(name);
 			String jail = array.get(1);
 			return jail;
 		}
 		else {
 			return null;
 		}
 		
 	}
 
 	public boolean playerIsJailed(Player p) {
 		String name = p.getName();
 		if (useful.jailed.containsKey(name)){
 			return true;
 		}
 		else {
 		return false;
 		}
 	}
 
 	
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityDamage(EntityDamageEvent event) {
         if (event.getEntity() instanceof Player && event.getDamage()>0) {
         	Player damagedplayer = (Player) event.getEntity();
         		String name = damagedplayer.getName();
         		String pluginFolder = useful.pluginFolder;
         		this.heros = new ListStore(new File(pluginFolder + File.separator + "heros.dat"));
     			this.heros.load();
         		 boolean cancel = this.heros.contains(name);
         		 if (cancel == true){
                  	event.setCancelled(true);
 					Boolean contains = useful.jailed.containsKey(name);
         		if (contains){
         			event.setCancelled(true);
         		}
         		
         		
         	}
             
         }
         return;
     }
 	
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player playerJoined = event.getPlayer();
 		String name = playerJoined.getName();
 		String loginmsg = useful.config.getString("general.loginmessage");
 		loginmsg = useful.colorise(loginmsg);
 		loginmsg = loginmsg.replace("*name*", name);
 		loginmsg = plugin.colors.getInfo() + loginmsg;
 		if(useful.config.getBoolean("general.enableCustomLoginMessage")){
 		event.setJoinMessage(loginmsg);
 		}
 		if(useful.config.getBoolean("general.welcome_message.enable")){
 			String welcome = useful.config.getString("general.welcome_message.msg");
 			welcome = useful.colorise(welcome);
 			playerJoined.sendMessage(welcome);
 		}
 		boolean UpdateNotify = useful.updateManager.containsKey(name);
 		if(UpdateNotify){
 			boolean Notify = useful.updateManager.get(name);
 			if(Notify){
 				if(useful.config.getBoolean("version.update.notify")){
 				playerJoined.sendMessage(plugin.colors.getSuccess() + "[Useful]" + plugin.colors.getTitle() + "Hello operator this server is running useful plugin version " + useful.config.getDouble("version.current") + ". For info on this visit http://dev.bukkit.org/server-mods/useful/" +
 						" you will not be reminded again unless the server runs an auto update. The purpose of this reminder is to tell operators that new settings need to be configured.");
 				playerJoined.sendMessage(plugin.colors.getInfo() + "Changelog:");
 				Object[] changes = plugin.changelog.values.toArray();
 				for(int i=0;i<changes.length;i++){
 					String change = (String) changes[i];
 					playerJoined.sendMessage(ChatColor.BOLD + change);
 				}
 				useful.updateManager.put(name, false);
 				plugin.saveHashMapBoolean(useful.updateManager, plugin.getDataFolder() + File.separator + "updateManager.bin");
 				}		
 				}
 		}
 		boolean contains = useful.jailed.containsKey(name);
 		 if (contains){
 			 String jailname = getJail(playerJoined);
      		Location jail = null;
      		String locWorld = null;
      		double locX;
      		double locY;
      		double locZ;
      		float locPitch;
      		float locYaw;
 				try {
 					String query = "SELECT DISTINCT * FROM jails WHERE jailname='"+jailname+"' ORDER BY jailname";
 					ResultSet rs = plugin.sqlite.query(query);
 					try {
 						locWorld = rs.getString("locWorld");
 						locX = Double.parseDouble(rs.getString("locX"));
 						locY = Double.parseDouble(rs.getString("locY"));
 						locZ = Double.parseDouble(rs.getString("locZ"));
 						locPitch = Float.parseFloat((rs.getString("locPitch")));
 						locYaw = Float.parseFloat((rs.getString("locYaw")));
 						jail = new Location(plugin.getServer().getWorld(locWorld), locX, locY, locZ, locPitch, locYaw);
 						rs.close();
 					} catch (Exception e) {
 						e.printStackTrace();
 						return;
 					}
 				} catch (Exception e) {
 				return;
 				} 
 			 playerJoined.teleport(jail);
 			 playerJoined.sendMessage(plugin.colors.getError() + "You are in jail. (If your sentence is finished wait up to a maximum time of 20 seconds to be unjailed. Type /jailtime to view your remaining sentence.)");
 			 
 		 }
 		 ArrayList<String> array = new ArrayList<String>();
 		 if(useful.config.getBoolean("general.mail.enable")){
 			 if (useful.mail.containsKey(playerJoined.getName().toLowerCase())){
 					array = useful.mail.get(playerJoined.getName().toLowerCase());
 				}
 				playerJoined.sendMessage(ChatColor.GOLD + "Your mail: (type /mail clear to clear your inbox)");
 	         String listString = "";
 				
 				//String newLine = System.getProperty("line.separator");
 
 				for (String s : array)
 				{
 				    listString += s + " %n";
 				}
 				//sender.sendMessage(playerName + " " + listString);
 				String[] message = listString.split("%n"); // Split everytime the "\n" into a new array value
 						for(int x=0 ; x<message.length ; x++) {
 						playerJoined.sendMessage(plugin.colors.getSuccess() + "- " + message[x]); // Send each argument in the message
 						}
 		 }
 			
 					
 	        return;
 	}
 	
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void customQuitMsg(PlayerQuitEvent event){
 		if(useful.authed.containsKey(event.getPlayer().getName())){
 			useful.authed.put(event.getPlayer().getName(), false);
 		}
 		Player playerJoined = event.getPlayer();
 		String name = playerJoined.getName();
 		String msg = useful.config.getString("general.quitmessage");
 		msg = useful.colorise(msg);
 		msg = msg.replace("*name*", name);
 		msg = plugin.colors.getInfo() + msg;
 		if(useful.config.getBoolean("general.enableCustomQuitMessage")){
 		event.setQuitMessage(msg);
 		}
 	}
 	
 	
 	@EventHandler
 	public void jailSmash(BlockBreakEvent event)  {
 		
 		Player player = event.getPlayer();
 		String name = player.getName();
 		boolean contains = useful.jailed.containsKey(name);
 		 if (contains){
 			 event.setCancelled(true);
 			 player.sendMessage(plugin.colors.getError() + "You are not allowed to smash blocks whilst in jail.");
 		 }
 	        return;
 	}
 	
 	@EventHandler
 	public void wirelessRedstoneRemover(BlockBreakEvent event){
 		//TODO
 		Block block = event.getBlock();
 		if(!(block.getState() instanceof Sign)){
 			return;
 		}
 		BlockState state = block.getState();
 		Sign sign = (Sign) state;
 		if(sign.getLine(1).equalsIgnoreCase("[wir]")){
 			String number = sign.getLine(0);
 			Location loc = sign.getLocation();
 			String world;
 			double x;
 			double y;
 			double z;
 		world = loc.getWorld().getName();
 		x = loc.getX();
 		y = loc.getY();
 		z = loc.getZ();
 			String query = "DELETE FROM wir WHERE signNo='"+number+"' AND locWorld='"+world+"' AND locX='"+x+"' AND locY='"+y+"' AND locZ='"+z+"'";
 			ResultSet rs = plugin.sqlite.query(query);
 			try {
 				rs.close();
 				event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Successfully unregistered wireless reciever");
 			} catch (Exception e) {
 				event.getPlayer().sendMessage(plugin.colors.getError() + "Failed to unregister wireless reciever");
 				e.printStackTrace();
 				return;
 			}
 		}
 		return;
 	}
 	@EventHandler
 	public void wirelessRedstoneRemoverMKII(BlockPhysicsEvent event){
 		//TODO
 		
 		
 		Block block = event.getBlock();
 		if(!(block.getState() instanceof Sign)){
 			return;
 		}
 		BlockState state = block.getState();
 		Sign sign = (Sign) state;
 		if(sign.getLine(1).equalsIgnoreCase("[wir]")){
 			Block b = event.getBlock();
 			BlockState bs = b.getState();
 			if (bs.getData() instanceof Attachable) {
 				Attachable a = (Attachable)	b.getState().getData();
 				Block attachedBlock = b.getRelative(a.getAttachedFace());
 				if (attachedBlock.getTypeId() == 0) {
 					String number = sign.getLine(0);
 					Location loc = sign.getLocation();
 					String world;
 					double x;
 					double y;
 					double z;
 				world = loc.getWorld().getName();
 				x = loc.getX();
 				y = loc.getY();
 				z = loc.getZ();
 					String query = "DELETE FROM wir WHERE signNo='"+number+"' AND locWorld='"+world+"' AND locX='"+x+"' AND locY='"+y+"' AND locZ='"+z+"'";
 					ResultSet rs = plugin.sqlite.query(query);
 					try {
 						rs.close();
 					} catch (Exception e) {
 						e.printStackTrace();
 						return;
 					}
 				}
 			}
 			
 		}
 		return;
 	}
 	@EventHandler
 	public void jailBuild(BlockPlaceEvent event)  {
 		
 		Player player = event.getPlayer();
 		String name = player.getName();
 		boolean contains = useful.jailed.containsKey(name);
 		 if (contains){
 			 event.setCancelled(true);
 			 player.getWorld().playSound(player.getLocation(), Sound.ANVIL_LAND, 10, 1);
 			 player.sendMessage(plugin.colors.getError() + "You are not allowed to place blocks whilst in jail.");
 		 }
 	        return;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void jailRespawn(PlayerRespawnEvent event){
 		Player player = event.getPlayer();
 		if(playerInvs.containsKey(player.getName())){
 		ItemStack[] inv = playerInvs.get(player.getName());
 			PlayerInventory pinv = player.getInventory();
 			pinv.setContents(inv);
 			player.sendMessage(plugin.colors.getSuccess() + "Items successfully saved from your death!");
 			playerInvs.remove(player.getName());
 		}
 		boolean contains = useful.jailed.containsKey(player.getName());
 		 if (contains){
 			 /*
 			 Collection<SerializableLocation> jails = useful.jails.values();
 			 Object[] jailsarray = jails.toArray();
 			 SerializableLocation jailname = (SerializableLocation) jailsarray[0];
 			 */
 			 String jailname = getJail(player);
 			 Location jail = null;
 	     		String locWorld = null;
 	     		double locX;
 	     		double locY;
 	     		double locZ;
 	     		float locPitch;
 	     		float locYaw;
 					try {
 						String query = "SELECT DISTINCT * FROM jails WHERE jailname='"+jailname+"' ORDER BY jailname";
 						ResultSet rs = plugin.sqlite.query(query);
 						try {
 							locWorld = rs.getString("locWorld");
 							locX = Double.parseDouble(rs.getString("locX"));
 							locY = Double.parseDouble(rs.getString("locY"));
 							locZ = Double.parseDouble(rs.getString("locZ"));
 							locPitch = Float.parseFloat((rs.getString("locPitch")));
 							locYaw = Float.parseFloat((rs.getString("locYaw")));
 							jail = new Location(plugin.getServer().getWorld(locWorld), locX, locY, locZ, locPitch, locYaw);
 							rs.close();
 						} catch (Exception e) {
 							e.printStackTrace();
 							return;
 						}
 					} catch (Exception e) {
 					return;
 					} 
 			 event.setRespawnLocation(jail);
 			 player.sendMessage(plugin.colors.getError() + "Respawned back in jail.");
 		 }
 		 return;
 	}
 	
 	@EventHandler
 	public void mobBlock(CreatureSpawnEvent event){
 		if (event.getEntityType() == EntityType.ENDER_DRAGON){
 			if (event.getSpawnReason() == SpawnReason.SPAWNER_EGG){
 				boolean disable = useful.config.getBoolean("general.disable_enderdragon_player_spawning");
 				if (disable){
 			Entity ent = event.getEntity();
 			String wname = ent.getWorld().getName();
 			Location loc = event.getEntity().getLocation();
 			event.setCancelled(disable);
 			Bukkit.getWorld(wname).createExplosion(loc, 0);
 			Bukkit.getWorld(wname).playEffect(loc, Effect.GHAST_SHRIEK, 0);
 			Bukkit.getWorld(wname).playEffect(loc, Effect.EXTINGUISH, 0);
 			Bukkit.getWorld(wname).playSound(loc, Sound.SKELETON_DEATH, 5, 2);
 				}
 			}
 		}
 	
 		 
 		else if(event.getEntityType() == EntityType.WITHER){
 			boolean disable = useful.config.getBoolean("general.disable_wither_spawning");
 			if (disable){
 				Entity ent = event.getEntity();
 				String wname = ent.getWorld().getName();
 				event.getEntity().setHealth(0);
 				Location loc = event.getEntity().getLocation();
 				Bukkit.getWorld(wname).createExplosion(loc, 0);
 				Bukkit.getWorld(wname).playEffect(loc, Effect.GHAST_SHRIEK, 0);
 				Bukkit.getWorld(wname).playEffect(loc, Effect.EXTINGUISH, 0);
 				Bukkit.getWorld(wname).playSound(loc, Sound.SKELETON_DEATH, 5, 2);
 			}
 			//event.setCancelled(disable);
 	}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void blockPlacePermChecker(BlockPlaceEvent event){
 		PermSafeBlockPlaceEvent pevent = null;
 		if(event instanceof PermSafeBlockPlaceEvent){
 			pevent = (PermSafeBlockPlaceEvent) event;
 		}
 		else{
 			if(event.getBlockAgainst().getState() instanceof Sign){
 				event.setCancelled(true);
 				return;
 			}
 			return;
 		}
 		if(event.isCancelled()){
 				event.setBuild(false);
 				event.setCancelled(true);
 				return;
 		}
 		else{
 			pevent.place();
 		}
 		return;
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	void Auther(PlayerMoveEvent event){
 		String name = event.getPlayer().getName();
 		if(useful.authed.containsKey(name)){
 		//	they are on the need-to-be-authed list
 			Boolean isAuthed = useful.authed.get(name);
 			Location loc = event.getPlayer().getLocation();
 			if(!isAuthed){
 				event.getPlayer().sendMessage(plugin.colors.getError() + "You are on the authentication list! Do /login [Password] to login to the server!");
 				event.getPlayer().setVelocity(new Vector(0, 0, 0));
 				event.getPlayer().teleport(loc);
 				event.setCancelled(true);
 			}
 		}
  	   
 		return;
 	}
 	
 	@EventHandler
 	void worldGamemodes(PlayerTeleportEvent event){
 		if(!(useful.config.getBoolean("general.worldgm.enable"))){
 			return;
 		}
 		Player p = (Player) event.getPlayer();
 		if(p.hasPermission("useful.worldgm.bypass")){
 			return;
 		}
 		String wname = event.getTo().getWorld().getName();
 		boolean found = false;
         boolean sReturn = false;
 		   String query = "SELECT * FROM worldgm";
 		   ResultSet rs = plugin.sqlite.query(query);
 		   try {
 			   while(rs.next()){
 				   if(rs.getString("world").equalsIgnoreCase(wname)){
 					   found = true;
 				   }
 			   }
 			if(!found){
 				sReturn = true;
 			}
 			rs.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 			return;
 		}
 		if(sReturn || !found){
 			return;
 		}
 		//FOUND THE WORLD!
 		String gamemode = "";
 		String tquery = "SELECT * FROM worldgm WHERE world='"+wname+"'";
 		   ResultSet trs = plugin.sqlite.query(tquery);
 		   try {
 			   while(trs.next()){
 				gamemode = trs.getString("gamemode");   
 			   }
 			trs.close();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 			return;
 		}
 		   if(gamemode.equalsIgnoreCase("creative")){
 			   p.setGameMode(GameMode.CREATIVE);
 			   p.sendMessage(plugin.colors.getInfo() + "Your gamemode in this world is creative");
 		   }
 		   else if(gamemode.equalsIgnoreCase("survival")){
 			   p.setGameMode(GameMode.SURVIVAL);
 			   p.sendMessage(plugin.colors.getInfo() + "Your gamemode in this world is survival");
 		   }
 		   else if(gamemode.equalsIgnoreCase("adventure")){
 			   p.setGameMode(GameMode.ADVENTURE);
 			   p.sendMessage(plugin.colors.getInfo() + "Your gamemode in this world is adventure");
 		   }
 		   else{
 			   plugin.colLogger.log(Level.WARNING, "Invalid gamemode set for world: " + wname);
 			   return;
 		   }
 		return;
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	void Auther(PlayerCommandPreprocessEvent event){
 		if(event.getMessage().toLowerCase().startsWith("login", 1)){
 			return;
 		}
 		else{
 		String name = event.getPlayer().getName();
 		if(useful.authed.containsKey(name)){
 		//	they are on the need-to-be-authed list
 			Boolean isAuthed = useful.authed.get(name);
 			if(!isAuthed){
 				event.getPlayer().sendMessage(plugin.colors.getError() + "You are on the authentication list! Do /login [Password] to login to the server!");
 				event.setCancelled(true);
 			}
 		}
 		}
 		boolean contains = useful.jailed.containsKey(event.getPlayer().getName());
 		if(contains){
 			String msg = event.getMessage().toLowerCase();
 			if(msg.startsWith("jailtime", 1) || msg.startsWith("gm", 1) || msg.startsWith("jailed", 1) || msg.startsWith("creative", 1) || msg.startsWith("adventure", 1) || msg.startsWith("survival", 1)){
 				//Command is allowed in jail
 				return;
 			}
 			else{
 				event.getPlayer().sendMessage(plugin.colors.getError() + "You are not allowed to perform this command whilst in jail!");
 				event.setCancelled(true);
 				return;
 			}
 		}
 		return;
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	 void onPlayerMoveEvent(PlayerMoveEvent event){
 		Player player = event.getPlayer();
 		boolean contains = useful.jailed.containsKey(player.getName());
 		 if (contains){
 			 /*
 			 Collection<SerializableLocation> jails = useful.jails.values();
 			 Object[] jailsarray = jails.toArray();
 			 SerializableLocation jailname = (SerializableLocation) jailsarray[0];
 			 */
 			 String jailname = getJail(player);
 			 org.bukkit.Location jail = null;
      		String locWorld = null;
      		double locX;
      		double locY;
      		double locZ;
      		float locPitch;
      		float locYaw;
 				try {
 					String query = "SELECT DISTINCT * FROM jails WHERE jailname='"+jailname+"' ORDER BY jailname";
 					ResultSet rs = plugin.sqlite.query(query);
 					try {
 						locWorld = rs.getString("locWorld");
 						locX = Double.parseDouble(rs.getString("locX"));
 						locY = Double.parseDouble(rs.getString("locY"));
 						locZ = Double.parseDouble(rs.getString("locZ"));
 						locPitch = Float.parseFloat((rs.getString("locPitch")));
 						locYaw = Float.parseFloat((rs.getString("locYaw")));
 						jail = new Location(plugin.getServer().getWorld(locWorld), locX, locY, locZ, locPitch, locYaw);
 						rs.close();
 					} catch (Exception e) {
 						e.printStackTrace();
 						return;
 					}
 				} catch (Exception e) {
 				return;
 				}
 			 //player.teleport(jail);
 				//TODO a better jail blocking method
 				Location loc = event.getTo();
 				double x = loc.getX();
 				double y = loc.getY();
 				double z = loc.getZ();
 				double jailX = jail.getX();
 				double jailY = jail.getY();
 				double jailZ = jail.getZ();
 				if(x > (jailX + 2)){
 					player.teleport(jail);
 					player.sendMessage(plugin.colors.getError() + "Dont try to escape!");
 					return;
 				}
 				if(x < (jailX - 2)){
 					player.teleport(jail);
 					player.sendMessage(plugin.colors.getError() + "Dont try to escape!");
 					return;
 				}
 				if(z > (jailZ + 2)){
 					player.teleport(jail);
 					player.sendMessage(plugin.colors.getError() + "Dont try to escape!");
 					return;
 				}
 				if(z < (jailZ - 2)){
 					player.teleport(jail);
 					player.sendMessage(plugin.colors.getError() + "Dont try to escape!");
 					return;
 				}
 				if(y > (jailY + 5)){
 					player.teleport(jail);
 					player.sendMessage(plugin.colors.getError() + "Dont try to escape!");
 					return;
 				}
 				if(y < (jailY - 5)){
 					player.teleport(jail);
 					player.sendMessage(plugin.colors.getError() + "Dont try to escape!");
 					return;
 				}
 			 //player.sendMessage(plugin.colors.getError() + "Dont try to escape!");
 		 }
 	}
 
 	@EventHandler
 	void onInventoryCloseEvent(InventoryCloseEvent event){
 		if (useful.invsee.contains(event.getPlayer().getName())){
 			useful.invsee.remove(event.getPlayer().getName());
 		}
 		if(event.getPlayer().hasPermission("useful.ditems.bypass")){
 			return;
 		}
 		Inventory inv = ((Player)event.getPlayer()).getInventory();
 		String disabled_items = useful.config.getString("general.default_blocked_item_ids(separated_by_commas_)");
 		 ItemStack[] items = ((Player) event.getPlayer()).getInventory().getContents();
 		String[] ditems = disabled_items.split(",");
  	   for(int i=0 ; i<items.length ; i++) {
  		    //ItemStack[] items = inv.getContents();
  		   for(int x=0 ; x<ditems.length ; x++) {
  			   int id = Integer.parseInt(ditems[x]);
  			   while(items[i] != null && items[i].getType() == new ItemStack(id).getType()){
  				   ((Player) event.getPlayer()).sendMessage(plugin.colors.getError() + "You are not allowed " + new ItemStack(id).getType().toString().toLowerCase() + " in your inventory!");
  			   items[i].setAmount(0);
  			   inv.clear();
  			   inv.setContents(items);
  		   }
  		   }
  		  
 				}
 		return;
 	}
 	
 	@EventHandler
 	void itemRemovalPickup(PlayerPickupItemEvent event){
 		if(event.getPlayer().hasPermission("useful.ditems.bypass")){
 			return;
 		}
 		Inventory inv = ((Player)event.getPlayer()).getInventory();
 		String disabled_items = useful.config.getString("general.default_blocked_item_ids(separated_by_commas_)");
 		 ItemStack[] items = ((Player) event.getPlayer()).getInventory().getContents();
 		String[] ditems = disabled_items.split(",");
  	   for(int i=0 ; i<items.length ; i++) {
  		    //ItemStack[] items = inv.getContents();
  		   for(int x=0 ; x<ditems.length ; x++) {
  			  int id = Integer.parseInt(ditems[x]);
  			  Item item = event.getItem();
  				if(item != null && item.getItemStack().getTypeId() == new ItemStack(id).getTypeId()){
  					((Player) event.getPlayer()).sendMessage(plugin.colors.getError() + "You are not allowed " + new ItemStack(id).getType().toString().toLowerCase() + " in your inventory!");
  					inv.removeItem(item.getItemStack());
  					inv.remove(item.getItemStack());
  					event.setCancelled(true);
  					return;
  				}
  			   while(items[i] != null && items[i].getType() == new ItemStack(id).getType()){
  				   ((Player) event.getPlayer()).sendMessage(plugin.colors.getError() + "You are not allowed " + new ItemStack(id).getType().toString().toLowerCase() + " in your inventory!");
  			   items[i].setAmount(0);
  			   inv.clear();
  			   inv.setContents(items);
  		   }
  		   }
  		  
 				}
 	}
 	
 	@EventHandler
 	void playerInteractItemRemoval(PlayerInteractEvent event){
 		if(event.getPlayer().hasPermission("useful.ditems.bypass")){
 			return;
 		}
 		Inventory inv = ((Player)event.getPlayer()).getInventory();
 		String disabled_items = useful.config.getString("general.default_blocked_item_ids(separated_by_commas_)");
 		 ItemStack[] items = ((Player) event.getPlayer()).getInventory().getContents();
 		String[] ditems = disabled_items.split(",");
  	   for(int i=0 ; i<items.length ; i++) {
  		    //ItemStack[] items = inv.getContents();
  		   for(int x=0 ; x<ditems.length ; x++) {
  			   int id = Integer.parseInt(ditems[x]);
  			  ItemStack item = event.getItem();
 				if(item != null && item.getTypeId() == new ItemStack(id).getTypeId()){
 					((Player) event.getPlayer()).sendMessage(plugin.colors.getError() + "You are not allowed " + new ItemStack(id).getType().toString().toLowerCase() + " in your inventory!");
 					inv.removeItem(item);
 					inv.remove(item);
 					event.setCancelled(true);
 					return;
 				}
  			   while(items[i] != null && items[i].getType() == new ItemStack(id).getType()){
  				   ((Player) event.getPlayer()).sendMessage(plugin.colors.getError() + "You are not allowed " + new ItemStack(id).getType().toString().toLowerCase() + " in your inventory!");
  			   items[i].setAmount(0);
  			   inv.clear();
  			   inv.setContents(items);
  		   }
  		   }
  		  
 				}
 	}
 	
 	@EventHandler
 	void onInventoryClickEvent(InventoryClickEvent event){
 		if (useful.invsee.contains((event.getWhoClicked().getName()))){
 			boolean cancel = useful.config.getBoolean("general.invsee.allow-edit");
 			if (cancel == false){
 			event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler
 	void onPlayerPreDeathEvent(PlayerDeathEvent event){
 		Player player = event.getEntity();
 		if(useful.config.getBoolean("general.death.keepitems.enable") == false){
 			return;
 		}
 		if(useful.config.getBoolean("general.death.usepermission")){
 			if(player.hasPermission(useful.config.getString("general.death.keepitems.permission")) == false){
 				return;
 			}
 		}
 		//keep items
 			playerInvs.put(player.getName(), player.getInventory().getContents());
             List<ItemStack> drops = event.getDrops();
             drops.clear();
 		return;
 	}
 	
 	@EventHandler
 	void performanceMobSpawning(CreatureSpawnEvent event){
 		if(useful.uhost_settings.containsKey("performance")){
 			if(useful.uhost_settings.get("performance") == false){
 				return;
 			}
 		}
 		else {
 			return;
 		}
 		SpawnReason why = event.getSpawnReason();
 		EntityType ent = event.getEntityType();
 		if(ent == EntityType.WOLF || ent == EntityType.PLAYER || ent == EntityType.OCELOT || ent == EntityType.DROPPED_ITEM || ent == EntityType.ARROW || ent == EntityType.BOAT || ent == EntityType.ITEM_FRAME || ent == EntityType.PAINTING || ent == EntityType.EXPERIENCE_ORB || ent == EntityType.MINECART || ent == EntityType.PLAYER || ent == EntityType.PRIMED_TNT || ent == EntityType.FIREBALL || ent == EntityType.LIGHTNING || ent == EntityType.WEATHER || ent == EntityType.VILLAGER){
 			//do nothing
 			return;
 		}
 		else if (why == SpawnReason.BREEDING || why == SpawnReason.EGG || why == SpawnReason.SPAWNER || why == SpawnReason.SPAWNER_EGG || why == SpawnReason.VILLAGE_DEFENSE || why == SpawnReason.VILLAGE_INVASION || why == SpawnReason.CUSTOM || why == SpawnReason.BUILD_IRONGOLEM){
 			//do nothing
 			return;
 		}
 		else {
 		double radius = 100;
 		Double x = (double) radius;
 		Double y = (double) radius;
 		Double z = (double) radius;
 		List<Entity> near = event.getEntity().getNearbyEntities(x, y, z);
 		Object[] entarray = near.toArray();
         boolean player = false;
 		for (Object s  : entarray){
 			if(((Entity) s).getType() == EntityType.PLAYER){
 				player = true;
 			  }
 		    }
 		if(player == false){
 		event.setCancelled(true);	
 		 }
 		else {
 			return;
 		}
 		}
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	void commandEvent(PlayerCommandPreprocessEvent event){
 		Object[] blocked = useful.blockedCmds.toArray();
 		boolean allowed = true;
 		for(int i=0;i<blocked.length;i++){
 			String theCmd = (String) blocked[i];
 			theCmd = theCmd.toLowerCase();
 			String msgCmd = event.getMessage().toLowerCase();
 		if(msgCmd.startsWith(theCmd, 1)){
 			event.getPlayer().sendMessage(plugin.colors.getError() + useful.config.getString("general.disabledmessage"));
 			allowed = false;
 		}
 		}
 		if(useful.config.getBoolean("general.log_commands_to_console")){
 		String cmd = event.getMessage();
 		plugin.colLogger.info(event.getPlayer().getName() + " executed command " + cmd);
 		}
 		ArrayList<String> players = plugin.commandViewers.values;
 		Object[] loggers = players.toArray();
 		for (int i = 0; i < loggers.length; i++) {
 	         Player p = plugin.getServer().getPlayer((String) loggers[i]);
 	    if(p != null){     
 	    	String cmd = event.getMessage();
 	    	p.sendMessage(ChatColor.DARK_GRAY + event.getPlayer().getName() + " executed command " + cmd);
 	    }
 		}
 		if(!allowed){
 			event.setCancelled(true);
 		}
 		return;
 	}
 	@EventHandler
 	void backSaver(PlayerTeleportEvent event){
 		//TODO make a way of saving players previous locations.
 		Player player = event.getPlayer();
 		String pname = player.getName();
 		Location prev = event.getFrom();
 		String pluginFolder = plugin.getDataFolder().getAbsolutePath();
 		File pFile = new File(pluginFolder + File.separator + "player-data" + File.separator + pname + ".yml"); //Should create is doesn't exist?
 		FileConfiguration pData = new YamlConfiguration();
 		try {
 			pData.load(pFile);
 		} catch (FileNotFoundException e) {
 			try {
 				pFile.createNewFile();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			pFile.delete();
 			return;
 		}
 		pData.set("data.previous-location.world", prev.getWorld().getName());
 		pData.set("data.previous-location.x", prev.getX());
 		pData.set("data.previous-location.y", prev.getY());
 		pData.set("data.previous-location.z", prev.getZ());
 		pData.set("data.previous-location.yaw", prev.getYaw());
 		pData.set("data.previous-location.pitch", prev.getPitch());
 		try {
 			pData.save(pFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return;
 	}
 	@EventHandler
 	void backSaver(PlayerDeathEvent event){
 		//TODO make a way of saving players previous locations.
 		Player player = event.getEntity();
 		String pname = player.getName();
 		Location prev = player.getLocation();
 		String pluginFolder = plugin.getDataFolder().getAbsolutePath();
 		File pFile = new File(pluginFolder + File.separator + "player-data" + File.separator + pname + ".yml"); //Should create is doesn't exist?
 		FileConfiguration pData = new YamlConfiguration();
 		try {
 			pData.load(pFile);
 		} catch (FileNotFoundException e) {
 			try {
 				pFile.createNewFile();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			pFile.delete();
 			return;
 		}
 		pData.set("data.previous-location.world", prev.getWorld().getName());
 		pData.set("data.previous-location.x", prev.getX());
 		pData.set("data.previous-location.y", prev.getY());
 		pData.set("data.previous-location.z", prev.getZ());
 		pData.set("data.previous-location.yaw", prev.getYaw());
 		pData.set("data.previous-location.pitch", prev.getPitch());
 		try {
 			pData.save(pFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return;
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	void tpaEvent(TpaEvent event){
 		Player teleportee = event.getSender();
 		Player target = event.getTarget();
 		boolean tpa = event.getTpa();
 		if(tpa == true){
 		target.sendMessage(plugin.colors.getInfo() + teleportee.getName() + " requested to teleport to you. Do /tpaccept to allow them to do so!");
 		}
 		else if(tpa == false){
 	    target.sendMessage(plugin.colors.getInfo() + teleportee.getName() + " requested for you to teleport to them. Do /tpaccept to allow them to do so!");
 		}
 		return;
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	void pluginEvent(ServerListPingEvent event){
 		if(useful.config.getBoolean("general.possible_player.enable")){
 		plugin.colLogger.info("A player with the server on their list has just clicked on multiplayer!");
 		}
 		ArrayList<String> players = plugin.commandViewers.values;
 		Object[] loggers = players.toArray();
 		for (int i = 0; i < loggers.length; i++) {
 	         Player p = plugin.getServer().getPlayer((String) loggers[i]);
 	    if(p != null){     
 	    	p.sendMessage(ChatColor.DARK_GRAY + "A player with the server on their list has just clicked on multiplayer!");
 	    }
 		}
 		return;
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	void tntExplodeEvent(final ExplosionPrimeEvent event){
 		boolean disable = useful.config.getBoolean("general.disable_tnt_damage");
 		boolean richochet = useful.config.getBoolean("general.tnt_richochet");
 		if (disable){
 			event.setCancelled(true);
 			return;
 		}
 		if(richochet){
 					//execute event
 					Location loc = event.getEntity().getLocation();
 					World w = event.getEntity().getWorld();
 					int radius = useful.config.getInt("general.tnt_radius");
 					boolean fire = useful.config.getBoolean("general.tnt_fire");
 					w.createExplosion(loc, radius, fire);
 			event.setCancelled(true);
 			return;
 		}
 		else {
 			int radius = useful.config.getInt("general.tnt_radius");
 			boolean fire = useful.config.getBoolean("general.tnt_fire");
 			event.setRadius(radius);
 			event.setFire(fire);
 		}
 		
 	}
 	
 	
 	@EventHandler
 	void prefix(AsyncPlayerChatEvent event){
 		if(!useful.config.getBoolean("general.uchat.enable")){
 			return;
 		}
 		Player p = event.getPlayer();
 		String msg = event.getMessage();
 		msg = msg.replace("&0", "" + ChatColor.BLACK);
 		msg = msg.replace("&1", "" + ChatColor.DARK_BLUE);
 		msg = msg.replace("&2", "" + ChatColor.DARK_GREEN);
 		msg = msg.replace("&3", "" + ChatColor.DARK_AQUA);
 		msg = msg.replace("&4", "" + ChatColor.DARK_RED);
 		msg = msg.replace("&5", "" + ChatColor.DARK_PURPLE);
 		msg = msg.replace("&6", "" + ChatColor.GOLD);
 		msg = msg.replace("&7", "" + ChatColor.GRAY);
 		msg = msg.replace("&8", "" + ChatColor.DARK_GRAY);
 		msg = msg.replace("&9", "" + ChatColor.BLUE);
 		msg = msg.replace("&a", "" + ChatColor.GREEN);
 		msg = msg.replace("&b", "" + ChatColor.AQUA);
 		msg = msg.replace("&c", "" + ChatColor.RED);
 		msg = msg.replace("&d", "" + plugin.colors.getTp());
 		msg = msg.replace("&e", "" + ChatColor.YELLOW);
 		msg = msg.replace("&f", "" + ChatColor.WHITE);
 		msg = msg.replace("&r", "" + ChatColor.RESET);
 		msg = msg.replace("&l", "" + ChatColor.BOLD);
 		msg = msg.replace("&i", "" + ChatColor.ITALIC);
 		msg = msg.replace("&m", "" + ChatColor.MAGIC);
 		event.setMessage(msg);
 		if(useful.config.getBoolean("general.prefixes.enable")){
 			Set<String> ranksSet = useful.ranks.getConfigurationSection("ranks").getKeys(false);
 			Object[] ranks = ranksSet.toArray();
 			p = event.getPlayer();
 			String prefix = null;
 			for(int i = 0; i < ranks.length; i++){
 				String rank = (String) ranks[i];
 				String permPath = "ranks." + rank + ".permission";
 				String permission = useful.ranks.getString(permPath);
 				boolean has = false;
                 Permission perm = new Permission(permission);
                 perm.setDefault(PermissionDefault.FALSE);
 					if(p.hasPermission(perm)){
 						has = true;
 					}
 				if(has){
 					String prefixPath = "ranks." + rank + ".prefix";
 					prefix = useful.ranks.getString(prefixPath);
 				}
 			}
 			if(prefix == null){
 				prefix = useful.ranks.getString("ranks.default.prefix");
 			}
 			    try {
 					prefix = prefix.replace("*name*", p.getDisplayName());
 					prefix = prefix.replace("&0", "" + ChatColor.BLACK);
 					prefix = prefix.replace("&1", "" + ChatColor.DARK_BLUE);
 					prefix = prefix.replace("&2", "" + ChatColor.DARK_GREEN);
 					prefix = prefix.replace("&3", "" + ChatColor.DARK_AQUA);
 					prefix = prefix.replace("&4", "" + ChatColor.DARK_RED);
 					prefix = prefix.replace("&5", "" + ChatColor.DARK_PURPLE);
 					prefix = prefix.replace("&6", "" + ChatColor.GOLD);
 					prefix = prefix.replace("&7", "" + ChatColor.GRAY);
 					prefix = prefix.replace("&8", "" + ChatColor.DARK_GRAY);
 					prefix = prefix.replace("&9", "" + ChatColor.BLUE);
 					prefix = prefix.replace("&a", "" + ChatColor.GREEN);
 					prefix = prefix.replace("&b", "" + ChatColor.AQUA);
 					prefix = prefix.replace("&c", "" + ChatColor.RED);
 					prefix = prefix.replace("&d", "" + ChatColor.LIGHT_PURPLE);
 					prefix = prefix.replace("&e", "" + ChatColor.YELLOW);
 					prefix = prefix.replace("&f", "" + ChatColor.WHITE);
 					prefix = prefix.replace("&r", "" + ChatColor.RESET);
 					prefix = prefix.replace("&l", "" + ChatColor.BOLD);
 					prefix = prefix.replace("&i", "" + ChatColor.ITALIC);
 					prefix = prefix.replace("&m", "" + ChatColor.MAGIC);
 				} catch (Exception e) {
 				}
 			    Set<Player> recievers = event.getRecipients();
 			    Object[] array = recievers.toArray();
 			    for(int i = 0; i < array.length; i++){
 			    	if(!(array[i] == plugin.getServer().getConsoleSender())){
 			    		((Player) array[i]).sendMessage("<" + prefix + ChatColor.WHITE + "> " + msg);
 			    	}
 			    }
 			    plugin.getServer().getConsoleSender().sendMessage("<" + prefix + ChatColor.WHITE + "> " + msg);
 	    event.setCancelled(true);
 		}
 		else return;
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	void Signs(SignChangeEvent event){
 		String[] lines = event.getLines();
 		for(int i = 0; i < lines.length; i++){
 			String line = lines[i];
 			line = useful.colorise(line);
 			lines[i] = line;
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Warp]")){
 			lines[0] = plugin.colors.getError() + "" + ChatColor.BOLD + "[Warp]";
 			lines[1] = plugin.colors.getSuccess() + lines[1];
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[1]).equalsIgnoreCase("[Wis]")){
 			event.getPlayer().sendMessage(plugin.colors.getInfo() + "You have placed a wireless sender Channel number: "+ lines[0] +". Recieve it with [wir] (wireless reciever).");
 			lines[1] = "[Wis]";
 			lines[2] = ChatColor.ITALIC + "Wireless";
 			lines[3] = ChatColor.ITALIC + "Sender";
 		}
 		if(ChatColor.stripColor(lines[1]).equalsIgnoreCase("[Wir]")){
 			lines[0] = ChatColor.stripColor(lines[0]);
 			lines[1] = "[Wir]";
 			lines[2] = ChatColor.ITALIC + "Wireless";
 			lines[3] = ChatColor.ITALIC + "reciever";
 			Location loc = event.getBlock().getLocation();
 			String world;
 			double x;
 			double y;
 			double z;
 			double yaw;
 			double pitch;
 		world = loc.getWorld().getName();
 		x = loc.getX();
 		y = loc.getY();
 		z = loc.getZ();
 		yaw = loc.getYaw();
 		pitch = loc.getPitch();
 		//We now have all the location details set!
 		String theData = "INSERT INTO wir VALUES('"+lines[0]+"', '"+world+"', "+x+", "+y+", "+z+", "+yaw+", "+pitch+");";
 		ResultSet rsi = plugin.sqlite.query(theData);
 		try {
 			rsi.close();
 			event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Successfully registered wireless reciever channel: " + lines[0]);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return;
 		}
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Warps]")){
 			lines[0] = plugin.colors.getError() + "" + ChatColor.BOLD + "[Warps]";
 			lines[1] = plugin.colors.getSuccess() + "List of warps";
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Online]")){
 			lines[0] = plugin.colors.getError() + "" + ChatColor.BOLD + "[Online]";
 			lines[1] = plugin.colors.getSuccess() + "Who is online";
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Spawn]")){
 			lines[0] = plugin.colors.getError() + "" + ChatColor.BOLD + "[Spawn]";
 			lines[1] = plugin.colors.getSuccess() + "Go to spawn";
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Worlds]")){
 			lines[0] = plugin.colors.getError() + "" + ChatColor.BOLD + "[Worlds]";
 			lines[1] = plugin.colors.getSuccess() + "List worlds";
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[World]")){
 			lines[0] = plugin.colors.getError() + "" + ChatColor.BOLD + "[World]";
 			lines[1] = plugin.colors.getSuccess() + lines[1];
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Jails]")){
 			lines[0] = plugin.colors.getError() + "" + ChatColor.BOLD + "[Jails]";
 			lines[1] = plugin.colors.getSuccess() + "List of jails";
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[uCommands]")){
 			lines[0] = plugin.colors.getError() + "" + ChatColor.BOLD + "[uCommands]";
 			lines[1] = plugin.colors.getSuccess() + lines[1];
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[uCommand]")){
 			lines[0] = plugin.colors.getError() + "" + ChatColor.BOLD + "[uCommand]";
 			lines[1] = plugin.colors.getSuccess() + lines[1];
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Command]")){
 			lines[0] = plugin.colors.getError() + "" + ChatColor.BOLD + "[Command]";
 			lines[1] = plugin.colors.getSuccess() + lines[1];
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Gamemode]")){
 			lines[0] = plugin.colors.getError() + "" + ChatColor.BOLD + "[Gamemode]";
 			lines[1] = plugin.colors.getSuccess() + lines[1];
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[1]).equalsIgnoreCase("[Lift Up]")){
 			lines[1] = "[Lift Up]";
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[1]).equalsIgnoreCase("[Lift Down]")){
 			lines[1] = "[Lift Down]";
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[1]).equalsIgnoreCase("[Lift]")){
 			lines[1] = "[Lift]";
 			lines[2] = ChatColor.ITALIC + "Can only";
 			lines[3] = ChatColor.ITALIC + "recieve";
 		}
 		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[SpawnPoint]")){
 			lines[0] = plugin.colors.getError() + "[SpawnPoint]";
 			lines[1] = plugin.colors.getSuccess() + "Set home";
 			lines[2] = ChatColor.ITALIC + "Right click";
 			lines[3] = ChatColor.ITALIC + "to use";
 		}
 		if(ChatColor.stripColor(lines[1]).equalsIgnoreCase("[infinite]") || ChatColor.stripColor(lines[1]).equalsIgnoreCase("[inf]")){
 		if(event.getPlayer().hasPermission("useful.infinitesigns")){
 			lines[1] = "[Infinite]";
 		}
 		else{
 			event.getPlayer().sendMessage(plugin.colors.getError() + "You don't have the useful.infinitesigns permission required");
 			lines[1] = "";
 		}
 		}
 		return;
 	}
 	@EventHandler
 	public void onVehicleCreate(VehicleCreateEvent event) {
 		if (event.getVehicle() instanceof Minecart) {
 		}
 		return;
 	}
 	/*
 	@EventHandler
 	public void TntEasterEgg(BlockDispenseEvent event){
 		ItemStack item = event.getItem();
 		if(item.getTypeId() == 383 && item.getDurability() == 1001){
 			//spawn tnt entity
 			Block block = event.getBlock();
 			if(block.getType() == Material.DISPENSER){
 				int dirId = block.getData();
 				if(dirId == 3){
 					//looking south
 					Location loc = block.getRelative(BlockFace.SOUTH, 2).getLocation();
 					if(loc.getBlock().getTypeId() != 0){
 						return;
 					}
 					loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
 				}
 				else if(dirId == 4){
 					//looking west
 					Location loc = block.getRelative(BlockFace.WEST, 2).getLocation();
 					if(loc.getBlock().getTypeId() != 0){
 						return;
 					}
 					loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
 				}
 				else if(dirId == 2){
 					//looking north
 					Location loc = block.getRelative(BlockFace.NORTH, 2).getLocation();
 					if(loc.getBlock().getTypeId() != 0){
 						return;
 					}
 					loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
 				}
 				else if(dirId == 5){
 					//looking east
 					Location loc = block.getRelative(BlockFace.EAST, 2).getLocation();
 					if(loc.getBlock().getTypeId() != 0){
 						return;
 					}
 					loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
 				}
 			}
 		}
 		return;
 	}
 	*/
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void registerLoginPerms(PlayerLoginEvent event){
 		if(!(useful.config.getBoolean("uperms.enable"))){
 			return;
 		}
 		plugin.permManager.loadPerms(event.getPlayer());
 		plugin.permManager.refreshPerms(event.getPlayer());
 		event.getPlayer().recalculatePermissions();
 		return;
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void unRegisterQuitPerms(PlayerQuitEvent event){
 		String name = event.getPlayer().getName();
 		plugin.permManager.unLoadPerms(name);
 	return;
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void unRegisterKickPerms(PlayerKickEvent event){
 		String name = event.getPlayer().getName();
 		plugin.permManager.unLoadPerms(name);
 	return;
 	}
 	@EventHandler(priority = EventPriority.HIGH)
 	public void dispenseEvent(BlockDispenseEvent event){
 		if(!useful.config.getBoolean("general.infiniteDispenserSign.enable")){
 			return;
 		}
 		Sign sign = null;
 		Block disp = event.getBlock();
 		if(disp.getRelative(BlockFace.NORTH).getState() instanceof Sign){
 			sign = (Sign) disp.getRelative(BlockFace.NORTH).getState();
 		}
 		if(disp.getRelative(BlockFace.EAST).getState() instanceof Sign){
 			sign = (Sign) disp.getRelative(BlockFace.EAST).getState();
 		}
 		if(disp.getRelative(BlockFace.SOUTH).getState() instanceof Sign){
 			sign = (Sign) disp.getRelative(BlockFace.SOUTH).getState();
 		}
 		if(disp.getRelative(BlockFace.WEST).getState() instanceof Sign){
 			sign = (Sign) disp.getRelative(BlockFace.WEST).getState();
 		}
 		if(disp.getRelative(BlockFace.DOWN).getState() instanceof Sign){
 			sign = (Sign) disp.getRelative(BlockFace.DOWN).getState();
 		}
 		if(disp.getRelative(BlockFace.UP).getState() instanceof Sign){
 			sign = (Sign) disp.getRelative(BlockFace.UP).getState();
 		}
 		if(sign == null){
 			return;
 		}
 		String line = sign.getLine(1); //second line
 		if(line.equalsIgnoreCase("[Infinite]") || line.equalsIgnoreCase("[Inf]")){
 			ItemStack toDisp = event.getItem();
 			if(disp.getState() instanceof InventoryHolder){
 				BlockState di = disp.getState();
 				InventoryHolder d = (InventoryHolder) di;
 				Inventory inv = d.getInventory();
 				inv.addItem(toDisp);
 				di.update(true);
 			}
 		}
 		else{
 			return;
 		}
 		return;
 	}
 	@EventHandler
 	public void onVehicleUpdate(VehicleUpdateEvent event){
 
 //TODO
 	    Vehicle vehicle = event.getVehicle();
 	    Location under = vehicle.getLocation();
 	    		under.setY(vehicle.getLocation().getY() - 1);
 	    Block underblock = under.getBlock();
 	    Block underunderblock = underblock.getRelative(BlockFace.DOWN);
 	    Block normalblock = vehicle.getLocation().getBlock();
 	    Block up = normalblock.getLocation().add(0, 1, 0).getBlock();
 	    /*
 	    if(underblock.getTypeId() == 0 || underblock.getTypeId() == 10 || underblock.getTypeId() == 11 || underblock.getTypeId() == 8 || underblock.getTypeId() == 9 && underunderblock.getTypeId() == 0 || underunderblock.getTypeId() == 10 || underunderblock.getTypeId() == 11 || underunderblock.getTypeId() == 8 || underunderblock.getTypeId() == 9){
 	    	return;
 	    }
 	    */
 	    Entity passenger = vehicle.getPassenger();
 	    if (!(passenger instanceof Player)) {
 	      return;
 	    }
 	    
 
 	    Player player = (Player)passenger;
 	    	if (vehicle instanceof Minecart) {
 	    		if(!useful.config.getBoolean("general.cars.enable")){
 	    			return;
 	    		}
 	    		Minecart car = (Minecart) vehicle;
 	    		// It is a valid car!
 	    		Location exhaust = car.getLocation();
 	    		int blockBoostId = useful.config.getInt("general.cars.blockBoost");
 	    		int tid = underblock.getTypeId();
 	    		if(tid == blockBoostId){
 	    			if(inACar(player.getName())){
 	    				carBoost(player.getName(), 20, 6000, useful.config.getDouble("general.cars.defSpeed"));
 	    			}
 	    		}
 	    		int HighblockBoostId = useful.config.getInt("general.cars.HighblockBoost");
 	    		if(tid == HighblockBoostId){
 	    			if(inACar(player.getName())){
 	    				carBoost(player.getName(), 50, 8000, useful.config.getDouble("general.cars.defSpeed"));
 	    			}
 	    		}
 	    		int ResetblockBoostId = useful.config.getInt("general.cars.ResetblockBoost");
 	    		if(tid == ResetblockBoostId){
 	    			if(inACar(player.getName())){
 	    				ResetCarBoost(player.getName(), car, useful.config.getDouble("general.cars.defSpeed"));
 	    			}
 	    		}
 	    		Location loc = car.getLocation();
 	    		Vector playerVelocity = car.getPassenger().getVelocity();
 	    		double defMultiplier = useful.config.getDouble("general.cars.defSpeed");
 	    		double multiplier = 30;
 	    		if(!useful.carBoosts.containsKey(player.getName())){
 	    			useful.carBoosts.put(player.getName(), defMultiplier);
 	    		}
 	    		multiplier = useful.carBoosts.get(player.getName());
 	    		double maxSpeed = 5;
 	    		Vector Velocity = playerVelocity.multiply(multiplier);
 	    		if(loc.getBlock().getTypeId() == 27 || loc.getBlock().getTypeId() == 27 || loc.getBlock().getTypeId() == 66){
 	    			return;
 	    		}
 	    		if(!(player.isInsideVehicle())){
 	    			return;
 	    		}
 	    		if(car.getLocation().add(0, -1, 0).getBlock().getTypeId() == 27 || car.getLocation().add(0, -1, 0).getBlock().getTypeId() == 28 || car.getLocation().add(0, -1, 0).getBlock().getTypeId() == 66){
 	    			return;	
 	    		}
 	    		if(!player.hasPermission("useful.cars")){
 	    			player.sendMessage(plugin.colors.getInfo() + "You don't have the permission useful.cars required to drive a car!");
 	    			return;
 	    		}
 	    		if(normalblock.getTypeId() != 0 && normalblock.getTypeId() != 8 && normalblock.getTypeId() != 9 && normalblock.getTypeId() != 44 && normalblock.getTypeId() != 43 && normalblock.getTypeId() != 70 && normalblock.getTypeId() != 72 && normalblock.getTypeId() != 31){
	    			car.setVelocity(new Vector(0, 5, 0));
 	    	    	//player.getWorld().createExplosion(loc, 0);
 	    	    }
 	    		if(up.getTypeId() != 0 && up.getTypeId() != 8 && up.getTypeId() != 9 && up.getTypeId() != 44 && up.getTypeId() != 43){
	    			car.setVelocity(new Vector(0, 5, 0));
 	    	    	//player.getWorld().createExplosion(loc, 0);
 	    	    }
 	    		if(playerVelocity.getX() == 0 && playerVelocity.getZ() == 0){
 	    			return;
 	    		}
 	    		car.setMaxSpeed(maxSpeed);
 	    		Location before = car.getLocation();
 	    		float dir = (float)player.getLocation().getYaw();
 	    		BlockFace faceDir = ClosestFace.getClosestFace(dir);
 	    		int modX = faceDir.getModX() * 1;
 	    		int modY = faceDir.getModY() * 1;
 	    		int modZ = faceDir.getModZ() * 1;
 	    		before.add(modX, modY, modZ);
 	    		Block block = before.getBlock();
     		    //Block block = car.getLocation().getBlock().getRelative(faceDir );
 	    		//Block block = normalblock.getRelative(modX, modY, modZ);
 	    		//Block block = player.getTargetBlock(null, 1);
 	    		int bid = block.getTypeId();
 	    		if(block.getY() == under.getBlockY() || block.getY() > normalblock.getY()){
 	    			//On the floor or too high to jump
 	    			if(bid == 0 || bid == 10 || bid == 11 || bid == 8 || bid == 9 || bid == 139 || bid == 85 || bid == 107 || bid == 113 || bid == 70 || bid == 72){
 	    			car.getLocation().setYaw(dir);
         		    car.setVelocity(Velocity);
 	    			}
 	    			else if(block.getY() == under.getBlockY()){
	    				car.getLocation().setYaw(dir + 360);
	    				Velocity = new Vector(0, 2, 0);
 	        		    car.setVelocity(Velocity);
 	    			}
 	    			else{
 	    				return;//wall to high or on the floor
 	    			}
 	    			return;
 	    		}
     		    Location theNewLoc = block.getLocation();
     		    Location bidUpLoc = block.getLocation().add(0, 1, 0);
     		    int bidU = bidUpLoc.getBlock().getTypeId();
     		    if(bid != 0 && bid != 10 && bid != 11 && bid != 8 && bid != 9 && bid != 139 && bid != 85 && bid != 107 && bid != 113 && bid != 70 && bid != 72){
     		    	if(bidU == 0 || bidU == 10 || bidU == 11 || bidU == 8 || bidU == 9 || bidU == 44 || bidU == 43){
     		    		//if(block.getTypeId() == 44 || block.getTypeId() == 43){
     		    theNewLoc.add(0, 1.5d, 0);
    		    //TODO no londer works
    		     double y = Velocity.getY() + 13;
    		     Velocity.setY(y);
    		     car.setVelocity(Velocity);
    		    	//car.teleport(theNewLoc);
     		    	}
     		    }
     		    else {
     		    	car.getLocation().setYaw(dir);
         		    car.setVelocity(Velocity);
     		    	//theNewLoc.add(0, 1d, 0);
     		    }
 				//player.getWorld().playEffect(exhaust, Effect.SMOKE, 1);
 	    	}
 	    	
 	    	return;
 	}
 
 	
 	
 	
 	@EventHandler
 	void wirelessRedstone(BlockRedstoneEvent event){
 		int num = 0;
 		if(!(useful.config.getBoolean("general.wirelessRedstone.enable"))){
 			return;
 		}
 		Block powered = event.getBlock();
 		boolean toPower = true;
 		if(event.getNewCurrent() == 0){
 			toPower = false;
 		}
 		if(powered.getRelative(BlockFace.NORTH).getState() instanceof Sign){
 			BlockState signState = powered.getRelative(BlockFace.NORTH).getState();
 			Sign sign = (Sign) signState;
 			if(sign.getLine(1).equalsIgnoreCase("[wis]")){
 				num = 0;
 				try {
 					num = Integer.parseInt(sign.getLine(0));
 				} catch (Exception e) {
 					return;
 				}
 				String query = "SELECT * FROM wir WHERE signNo='"+num+"'";
  			   ResultSet rs = plugin.sqlite.query(query);
  			   try {
  				   while(rs.next()){
  					  String locWorld = rs.getString("locWorld");
 						double locX = Double.parseDouble(rs.getString("locX"));
 						double locY = Double.parseDouble(rs.getString("locY"));
 						double locZ = Double.parseDouble(rs.getString("locZ"));
 						Location wir = new Location(plugin.getServer().getWorld(locWorld), locX, locY, locZ);
 						BlockState state = wir.getBlock().getRelative(BlockFace.NORTH).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.WEST).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.SOUTH).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.EAST).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						
  				   }
 					rs.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 					return;
 				}
 				
 			}
 		}
 		if(powered.getRelative(BlockFace.EAST).getState() instanceof Sign){
 			BlockState signState = powered.getRelative(BlockFace.EAST).getState();
 			Sign sign = (Sign) signState;
 			if(sign.getLine(1).equalsIgnoreCase("[wis]")){
 				num = 0;
 				try {
 					num = Integer.parseInt(sign.getLine(0));
 				} catch (Exception e) {
 					return;
 				}
 				String query = "SELECT * FROM wir WHERE signNo='"+num+"'";
  			   ResultSet rs = plugin.sqlite.query(query);
  			   try {
  				   while(rs.next()){
  					  String locWorld = rs.getString("locWorld");
 						double locX = Double.parseDouble(rs.getString("locX"));
 						double locY = Double.parseDouble(rs.getString("locY"));
 						double locZ = Double.parseDouble(rs.getString("locZ"));
 						Location wir = new Location(plugin.getServer().getWorld(locWorld), locX, locY, locZ);
 						BlockState state = wir.getBlock().getRelative(BlockFace.NORTH).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.WEST).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.SOUTH).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.EAST).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
  				   }
 					rs.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 					return;
 				}
 				
 			}
 		}
 		if(powered.getRelative(BlockFace.SOUTH).getState() instanceof Sign){
 			BlockState signState = powered.getRelative(BlockFace.SOUTH).getState();
 			Sign sign = (Sign) signState;
 			if(sign.getLine(1).equalsIgnoreCase("[wis]")){
 			    num = 0;
 				try {
 					num = Integer.parseInt(sign.getLine(0));
 				} catch (Exception e) {
 					return;
 				}
 				
 				String query = "SELECT * FROM wir WHERE signNo='"+num+"'";
  			   ResultSet rs = plugin.sqlite.query(query);
  			   try {
  				   while(rs.next()){
  					  String locWorld = rs.getString("locWorld");
 						double locX = Double.parseDouble(rs.getString("locX"));
 						double locY = Double.parseDouble(rs.getString("locY"));
 						double locZ = Double.parseDouble(rs.getString("locZ"));
 						Location wir = new Location(plugin.getServer().getWorld(locWorld), locX, locY, locZ);
 						BlockState state = wir.getBlock().getRelative(BlockFace.NORTH).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.WEST).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.SOUTH).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.EAST).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						
  				   }
 					rs.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 					return;
 				}
 				
 			}
 		}
 		if(powered.getRelative(BlockFace.WEST).getState() instanceof Sign){
 			BlockState signState = powered.getRelative(BlockFace.WEST).getState();
 			Sign sign = (Sign) signState;
 			if(sign.getLine(1).equalsIgnoreCase("[wis]")){
 				
 				num = 0;
 				try {
 					num = Integer.parseInt(sign.getLine(0));
 				} catch (Exception e) {
 					return;
 				}
 				
 				String query = "SELECT * FROM wir WHERE signNo='"+num+"'";
  			   ResultSet rs = plugin.sqlite.query(query);
  			   try {
  				   while(rs.next()){
  					  String locWorld = rs.getString("locWorld");
 						double locX = Double.parseDouble(rs.getString("locX"));
 						double locY = Double.parseDouble(rs.getString("locY"));
 						double locZ = Double.parseDouble(rs.getString("locZ"));
 						Location wir = new Location(plugin.getServer().getWorld(locWorld), locX, locY, locZ);
 						BlockState state = wir.getBlock().getRelative(BlockFace.NORTH).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.WEST).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.SOUTH).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						state = wir.getBlock().getRelative(BlockFace.EAST).getState();
 						if(state.getType() == Material.LEVER){
 							int data = state.getBlock().getData();
 							if(toPower){
 								data = data | 0x8; // on	
 							}
 							else{
 								data = data & ~0x8; // off
 							}
 							state.getBlock().setData((byte)data);
 						}
 						
  				   }
 					rs.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 					return;
 				}
 			}
 		}
 		return;
 	}
 	
 	@EventHandler
 	void warpSignTele(PlayerInteractEvent event){
 		if(!(event.getAction() == Action.RIGHT_CLICK_BLOCK)){
 			return;
 		}
 		Block block = event.getClickedBlock();
 		if(event.getPlayer().getItemInHand().getTypeId() == 328){
 			//Its a minecart!
 			int iar = block.getTypeId();
 			if(iar == 66 || iar == 28 || iar == 27){
 				return;
 			}
 			if(!useful.config.getBoolean("general.cars.enable")){
 				return;
 			}
 			Location loc = block.getLocation().add(0, 1, 0);
 			Entity ent = event.getPlayer().getWorld().spawnEntity(loc, EntityType.MINECART);
 		    Minecart car = (Minecart) ent;
 		    Location carloc = car.getLocation();
 		    carloc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
 		    car.setVelocity(new Vector(0,0,0));
 		    car.teleport(carloc);
 		    car.setVelocity(new Vector(0,0,0));
 			event.getPlayer().sendMessage(plugin.colors.getInfo() + "You placed a car! Cars can be driven with similar controls to a boat!");
 			if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
 				event.getPlayer().getInventory().removeItem(new ItemStack(328));
 			}
 		}
 		int LowBoostId = useful.config.getInt("general.cars.lowBoost");
 		int MedBoostId = useful.config.getInt("general.cars.medBoost");
 		int HighBoostId = useful.config.getInt("general.cars.highBoost");
 		float bid = event.getPlayer().getItemInHand().getTypeId(); // booster id
 		if(bid == LowBoostId){
 			if(inACar(event.getPlayer().getName())){
 				boolean boosting = carBoost(event.getPlayer().getName(), 10, 3000, useful.config.getDouble("general.cars.defSpeed"));
 				if(boosting){
 					if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
 						// they r in survival
 						event.getPlayer().getInventory().removeItem(new ItemStack(LowBoostId));
 					}
 					event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Initiated low level boost!");
 					return;
 				}
 				else {
 					event.getPlayer().sendMessage(plugin.colors.getError() + "Already boosting!");
 				}
 				return;
 			}
 		}
 		if(bid == MedBoostId){
 			if(inACar(event.getPlayer().getName())){
 				boolean boosting = carBoost(event.getPlayer().getName(), 20, 6000, useful.config.getDouble("general.cars.defSpeed"));
 				if(boosting){
 					if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
 						// they r in survival
 						event.getPlayer().getInventory().removeItem(new ItemStack(MedBoostId));
 					}
 					event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Initiated medium level boost!");
 					return;
 				}
 				else {
 					event.getPlayer().sendMessage(plugin.colors.getError() + "Already boosting!");
 				}
 				return;
 			}
 		}
 		if(bid == HighBoostId){
 			if(inACar(event.getPlayer().getName())){
 				boolean boosting = carBoost(event.getPlayer().getName(), 50, 10000, useful.config.getDouble("general.cars.defSpeed"));
 				if(boosting){
 					if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
 						// they r in survival
 						event.getPlayer().getInventory().removeItem(new ItemStack(HighBoostId));
 					}
 					event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Initiated high level boost!");
 					return;
 				}
 				else {
 					event.getPlayer().sendMessage(plugin.colors.getError() + "Already boosting!");
 				}
 				return;
 			}
 		}
 			BlockState state = block.getState();
 			if (state instanceof Sign) {
 			    Sign sign = (Sign)state;
 			    if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[Warp]")){
 			    	if(!useful.config.getBoolean("signs.warpSigns.enable")){
 						return;
 					}
 			    	if(!event.getPlayer().hasPermission("useful.warp")){
 			    		event.getPlayer().sendMessage(plugin.colors.getError() + "You don't have the useful.warp permission");
 			    		return;
 			    	}
 			    	String warpname = sign.getLine(1).toLowerCase();
 			    	warpname = warpname.toString();
 			    	warpname = warpname.toLowerCase();
 			    	warpname = ChatColor.stripColor(warpname);
 	        		
 	        		Location warp = null;
 	        		String locWorld = null;
 	        		double locX;
 	        		double locY;
 	        		double locZ;
 	        		float locPitch;
 	        		float locYaw;
 					try {
 						String query = "SELECT DISTINCT * FROM warps WHERE warpname='"+warpname+"' ORDER BY warpname";
 						ResultSet wrs = plugin.sqlite.query(query);
 						try {
 							
 							try {
 								locWorld = wrs.getString("locWorld");
 								locX = Double.parseDouble(wrs.getString("locX"));
 								locY = Double.parseDouble(wrs.getString("locY"));
 								locZ = Double.parseDouble(wrs.getString("locZ"));
 								locPitch = Float.parseFloat((wrs.getString("locPitch")));
 								locYaw = Float.parseFloat((wrs.getString("locYaw")));
 								warp = new Location(plugin.getServer().getWorld(locWorld), locX, locY, locZ, locPitch, locYaw);
 							} catch (Exception e) {
 							}
 							wrs.close();
 						} catch (Exception e) {
 							e.printStackTrace();
 							return;
 						}
 						event.getPlayer().teleport(warp);
 						event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.NOTE_PLING, 1, 1);
 						event.getPlayer().sendMessage(plugin.colors.getInfo() + "Warping...");
 					} catch (Exception e) {
 						
 						
 						event.getPlayer().sendMessage(plugin.colors.getError() + "Warp not found! Do /warps for a list of them");
 					}
 			    }
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[Online]")){
 			    	if(!useful.config.getBoolean("signs.onlineSigns.enable")){
 						return;
 					}
 			    	Player[] players = plugin.getServer().getOnlinePlayers();
 			    	String msg = plugin.colors.getInfo() + "Online players: ";
 			    	event.getPlayer().sendMessage(msg);
 			    	for(int i=0; i < players.length; i++){
 			    		event.getPlayer().sendMessage(plugin.colors.getSuccess() + players[i].getName() + ",");
 			    	}
 			    	return;
 			    }
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[Warps]")){
 			    	if(!useful.config.getBoolean("signs.warpsSigns.enable")){
 						return;
 					}
 			    	ArrayList<String> warpnames = new ArrayList<String>();
 			    	   ResultSet warps = plugin.sqlite.query("SELECT DISTINCT warpname FROM warps ORDER BY warpname");
 			    	   try {
 			    		   while(warps.next()){
 			    			   String warpname = warps.getString("warpname");
 			    			   warpnames.add(warpname);
 			    		   }
 			    		   warps.close();
 					} catch (SQLException e) {
 						event.getPlayer().sendMessage(plugin.colors.getError() + "Errow listing warps from stored data!");
 						plugin.colLogger.log(Level.SEVERE, "Errow listing warps from stored data!", e);
 					}
 			    	   
 			    	   Object[] ver = warpnames.toArray();
 			    	   StringBuilder messagetosend = new StringBuilder();
 			           for (int i=0;i<ver.length;i++)
 			           {
 			        	   //output.add(v);
 			        	   String v = (String) ver[i];
 							messagetosend.append(" ");
 							messagetosend.append(v);
 							messagetosend.append(",");
 							
 			           }
 			           event.getPlayer().sendMessage(plugin.colors.getInfo() + "Warps:" + messagetosend);
 			    	return;
 			    }
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[Spawn]")){
 			    	if(!useful.config.getBoolean("signs.spawnSigns.enable")){
 						return;
 					}
 			    	Location loc = event.getPlayer().getLocation().getWorld().getSpawnLocation();
 			    	event.getPlayer().teleport(loc);
 			    	return;
 			    }
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[Worlds]")){
 			    	if(!useful.config.getBoolean("signs.worldsSigns.enable")){
 						return;
 					}
 			    	List<World> ver = plugin.getServer().getWorlds();
 			    	   StringBuilder messagetosend = new StringBuilder();
 			           for (World v : ver)
 			           {
 			        	   //output.add(v);
 			        	   
 							messagetosend.append(" ");
 							messagetosend.append(v.getName());
 							messagetosend.append(",");
 							
 			           }
 			           event.getPlayer().sendMessage(plugin.colors.getInfo() + "Worlds:" + messagetosend);
 			    	return;
 			    }
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[Jails]")){
 			    	if(!useful.config.getBoolean("signs.jailsSigns.enable")){
 						return;
 					}
 			    	ArrayList<String> jailnames = new ArrayList<String>();
 			    	   ResultSet jails = plugin.sqlite.query("SELECT DISTINCT jailname FROM jails ORDER BY jailname");
 			    	   try {
 			    		   while(jails.next()){
 			    			   String name = jails.getString("jailname");
 			    			   jailnames.add(name);
 			    		   }
 			    		   jails.close();
 					} catch (SQLException e) {
 						event.getPlayer().sendMessage(plugin.colors.getError() + "Errow listing jails from stored data!");
 						plugin.colLogger.log(Level.SEVERE, "Errow listing jails from stored data!", e);
 					}
 			    	   
 			    	   Object[] ver = jailnames.toArray();
 			    	   StringBuilder messagetosend = new StringBuilder();
 			           for (int i=0;i<ver.length;i++)
 			           {
 			        	   //output.add(v);
 			        	   String v = (String) ver[i];
 							messagetosend.append(" ");
 							messagetosend.append(v);
 							messagetosend.append(",");
 							
 			           }
 			           event.getPlayer().sendMessage(plugin.colors.getInfo() + "Jails:" + messagetosend);
 			    	return;
 			    }
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[uCommands]")){
 			    	if(!useful.config.getBoolean("signs.uCommandsSigns.enable")){
 			    		event.getPlayer().sendMessage(plugin.colors.getError() + "That type of sign is disabled.");
 			    		return;
 			    	}
 			    		int page = 1;
 			    		try {
 							page = Integer.parseInt(ChatColor.stripColor(sign.getLine(1).toLowerCase()));
 						} catch (Exception e) {
 							event.getPlayer().sendMessage(plugin.colors.getError() + "Invalid page number");
 							return;
 						}
 			    		event.getPlayer().performCommand("ucommands " + page);
 			    		return;
 			    	}
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[uCommand]")){
 			    	if(!useful.config.getBoolean("signs.uCommandsSigns.enable")){
 			    		event.getPlayer().sendMessage(plugin.colors.getError() + "That type of sign is disabled.");
 			    		return;
 			    	}
 			    		String page = "";
 			    		try {
 							page = ChatColor.stripColor(sign.getLine(1).toLowerCase());
 						} catch (Exception e) {
 							event.getPlayer().sendMessage(plugin.colors.getError() + "Error finding command.");
 							return;
 						}
 			    		event.getPlayer().performCommand("ucommand " + page);
 			    		return;
 			    	}
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[Command]")){
 			    	if(!useful.config.getBoolean("signs.CommandSigns.enable")){
 			    		event.getPlayer().sendMessage(plugin.colors.getError() + "That type of sign is disabled.");
 			    		return;
 			    	}
 			    	    event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Executing command /" + ChatColor.stripColor(sign.getLine(1)));
 			    		event.getPlayer().performCommand("" + ChatColor.stripColor(sign.getLine(1)));
 			    		return;
 			    }
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[SpawnPoint]")){
 			    	if(!useful.config.getBoolean("signs.spawnpointSigns.enable")){
 			    		event.getPlayer().sendMessage(plugin.colors.getError() + "That type of sign is disabled.");
 			    		return;
 			    	}
 			    	    event.getPlayer().setBedSpawnLocation(event.getPlayer().getLocation(), true);
 			    	    event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Your respawn point is set!");
 			    		return;
 			    }
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(1))).equalsIgnoreCase("[Lift Up]")){
 			    	if(!useful.config.getBoolean("signs.LiftSigns.enable")){
 			    		return;
 			    	}
 			    	    double y = sign.getLocation().getY();
 			    	    double x = sign.getLocation().getX();
 			    	    double z = sign.getLocation().getZ();
 			    	    BlockState lift = null;
 			    	    boolean found = false;
 			    	    for(double i= sign.getLocation().getY(); i < sign.getWorld().getMaxHeight(); i++){
 			    	    	if(!found){
 			    	    	Location check = new Location(sign.getWorld(), x, i, z);
 			    	    	if(check.getBlock().getState() instanceof Sign){
 			    	    		Sign theDest = (Sign)check.getBlock().getState();
 			    	    		if(theDest.getLine(1).contains("Lift") && theDest.getLocation().getY() != sign.getLocation().getY() && theDest.getLocation().getY() != y + 1){
 			    	    		found = true;
 			    	    		lift = check.getBlock().getState();
 			    	    		}
 			    	    	}
 			    	    	}
 			    	    }
 			    	    if(!found){
 			    	    	event.getPlayer().sendMessage(plugin.colors.getError() + "Could not find a lift.");
 			    	    	return;
 			    	    }
 			    	    Sign dest = (Sign)lift;
 			    	    double dy = dest.getLocation().getY();
 			    	    Location loc = event.getPlayer().getLocation();
 			    	    loc.setY(dy-1);
 			    	    Block destBlock = loc.getBlock();
 			    	    int id = destBlock.getTypeId();
 			    	    if(id != 0 && id != 70 && id != 72){
 			    	    	event.getPlayer().sendMessage(plugin.colors.getError() + "Destination obstructed");
 			    	    	return;
 			    	    }
 			    	    Location floorLoc = new Location(loc.getWorld(), loc.getX(), loc.getY() - 1, loc.getZ());
 			    	    Block floor = floorLoc.getBlock();
 			    	    int FloorId = floor.getTypeId();
 			    	    if(FloorId == 0){
 			    	    	Location RLoc = dest.getLocation();
 			    	    	RLoc.setY(RLoc.getY() - 1);
 			    	    	if(RLoc.getBlock().getTypeId() != 0 && RLoc.getBlock().getTypeId() != 70 && RLoc.getBlock().getTypeId() != 72){
 			    	    		event.getPlayer().sendMessage(plugin.colors.getError() + "Destination obstructed");
 			    	    		return;
 			    	    	}
 			    	    	Block RFloor = new Location(RLoc.getWorld(), RLoc.getX(), RLoc.getY()-1, RLoc.getZ()).getBlock();
 			    	    	if(RFloor.getTypeId() == 0){
 			    	    		event.getPlayer().sendMessage(plugin.colors.getError() + "There is no floor at the destination!");
 			    	    		return;
 			    	    	}
 			    	    	event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Going up a level!");
 				    	    event.getPlayer().teleport(RLoc);
 				    	    event.getPlayer().getWorld().playSound(RLoc, Sound.NOTE_PLING, 1, 1);
 				    	    return;
 			    	    }
 			    	    event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Going up a level!");
 			    	    event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.NOTE_PLING, 1, 1);
 			    	    event.getPlayer().teleport(loc);
 			    		return;
 			    }
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(1))).equalsIgnoreCase("[Lift Down]")){
 			    	if(!useful.config.getBoolean("signs.LiftSigns.enable")){
 			    		return;
 			    	}
 			    	    double y = sign.getLocation().getY();
 			    	    double x = sign.getLocation().getX();
 			    	    double z = sign.getLocation().getZ();
 			    	    BlockState lift = null;
 			    	    boolean found = false;
 			    	    for(double i= sign.getLocation().getY(); i > 1; i--){
 			    	    	if(!found){
 			    	    	Location check = new Location(sign.getWorld(), x, i, z);
 			    	    	if(check.getBlock().getState() instanceof Sign){
 			    	    		Sign theDest = (Sign)check.getBlock().getState();
 			    	    		if(theDest.getLine(1).contains("Lift") && theDest.getLocation().getY() != sign.getLocation().getY() && theDest.getLocation().getY() != y + 1){
 			    	    		found = true;
 			    	    		lift = check.getBlock().getState();
 			    	    		}
 			    	    	}
 			    	    	}
 			    	    }
 			    	    if(!found){
 			    	    	event.getPlayer().sendMessage(plugin.colors.getError() + "Could not find a lift.");
 			    	    	return;
 			    	    }
 			    	    Sign dest = (Sign)lift;
 			    	    double dy = dest.getLocation().getY();
 			    	    Location loc = event.getPlayer().getLocation();
 			    	    loc.setY(dy-1);
 			    	    Block destBlock = loc.getBlock();
 			    	    int id = destBlock.getTypeId();
 			    	    if(id != 0 && id != 70 && id != 72){
 			    	    	event.getPlayer().sendMessage(plugin.colors.getError() + "Destination obstructed");
 			    	    	return;
 			    	    }
 			    	    Location floorLoc = new Location(loc.getWorld(), loc.getX(), loc.getY() -1, loc.getZ());
 			    	    Block floor = floorLoc.getBlock();
 			    	    int FloorId = floor.getTypeId();
 			    	    if(FloorId == 0){
 			    	    	Location RLoc = dest.getLocation();
 			    	    	RLoc.setY(RLoc.getY() - 1);
 			    	    	if(RLoc.getBlock().getTypeId() != 0 && RLoc.getBlock().getTypeId() != 70 && RLoc.getBlock().getTypeId() != 72){
 			    	    		event.getPlayer().sendMessage(plugin.colors.getError() + "Destination obstructed");
 			    	    		return;
 			    	    	}
 			    	    	Block RFloor = new Location(RLoc.getWorld(), RLoc.getX(), RLoc.getY()-1, RLoc.getZ()).getBlock();
 			    	    	if(RFloor.getTypeId() == 0){
 			    	    		event.getPlayer().sendMessage(plugin.colors.getError() + "There is no floor at the destination!");
 			    	    		return;
 			    	    	}
 			    	    	event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Going down a level!");
 				    	    event.getPlayer().teleport(RLoc);
 				    	    event.getPlayer().getWorld().playSound(RLoc, Sound.NOTE_PLING, 1, 1);
 				    	    return;
 			    	    }
 			    	    event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Going down a level!");
 			    	    event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.NOTE_PLING, 1, 1);
 			    	    event.getPlayer().teleport(loc);
 			    		return;
 			    }
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[Gamemode]")){
 			    	if(!useful.config.getBoolean("signs.GamemodeSigns.enable")){
 			    		event.getPlayer().sendMessage(plugin.colors.getError() + "That type of sign is disabled.");
 			    		return;
 			    	}
 			    	    String mode = ChatColor.stripColor(sign.getLine(1));
 			    	    GameMode gm = GameMode.CREATIVE;
 			    	    if(mode.equalsIgnoreCase("creative") || mode.equalsIgnoreCase("1")){
 			    	    	gm = GameMode.CREATIVE;
 			    	    }
 			    	    else if(mode.equalsIgnoreCase("survival") || mode.equalsIgnoreCase("0")){
 			    	    	gm = GameMode.SURVIVAL;
 			    	    }
 			    	    else if(mode.equalsIgnoreCase("adventure") || mode.equalsIgnoreCase("3")){
 			    	    	gm = GameMode.ADVENTURE;
 			    	    }
 			    	    else {
 			    	    	event.getPlayer().sendMessage(plugin.colors.getError() + "Gamemode not recognised.");
 			    	    	return;
 			    	    }
 			    	    event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Setting your Gamemode to " + gm.name().toLowerCase() + " mode!");
 			    	    event.getPlayer().setGameMode(gm);
 			    		return;
 			    	}
 			    else if(ChatColor.stripColor(ChatColor.stripColor(sign.getLine(0))).equalsIgnoreCase("[World]")){
 			    	if(!useful.config.getBoolean("signs.worldSigns.enable")){
 						return;
 					}
 			    	if(!event.getPlayer().hasPermission("useful.world")){
 			    		event.getPlayer().sendMessage(plugin.colors.getError() + "You don't have the useful.world permission");
 			    		return;
 			    	}
 			    	String warpname = sign.getLine(1).toLowerCase();
 			    	warpname = warpname.toString();
 			    	warpname = warpname.toLowerCase();
 			    	warpname = ChatColor.stripColor(warpname);
 			    	if(plugin.getServer().getWorld(warpname) != null){
 			    		event.getPlayer().sendMessage(plugin.colors.getSuccess() + "Teleporting you to " + warpname);
 			    		Location loc = plugin.getServer().getWorld(warpname).getSpawnLocation();
 			    		event.getPlayer().teleport(loc);
 			    		return;
 			    	}
 			    	else{
 			    		event.getPlayer().sendMessage(plugin.colors.getError() + "World not found");
 			    		return;
 			    	}
 			    	
 			    }
 			
 			
 		return;
 	}
 	
 	}
 	
 	
 	
 }
