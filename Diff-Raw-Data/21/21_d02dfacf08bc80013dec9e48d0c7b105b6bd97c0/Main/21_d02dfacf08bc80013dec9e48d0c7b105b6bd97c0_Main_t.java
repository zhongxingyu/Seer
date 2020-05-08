 package com.comze_instancelabs.witherfight;
 
 /**
  * 
  * @author instancelabs
  *
  */
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Skeleton.SkeletonType;
 import org.bukkit.entity.Wither;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scheduler.BukkitTask;
 
 import com.sk89q.worldguard.bukkit.WGBukkit;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.domains.DefaultDomain;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class Main extends JavaPlugin implements Listener{
 	
 	
 	public static Economy econ = null;
 	public boolean economy = false;
 	
 	WorldGuardPlugin worldGuard = null;
 	
 	
 	static HashMap<Player, String> arenap = new HashMap<Player, String>(); // playername -> arenaname
 	static HashMap<Player, String> tpthem = new HashMap<Player, String>(); // playername -> arenaname
 	static HashMap<Player, ItemStack[]> pinv = new HashMap<Player, ItemStack[]>(); // player -> Inventory
 	static HashMap<Player, ItemStack[]> parmor = new HashMap<Player, ItemStack[]>(); // player -> Inventory
 
 	
 	@Override
 	public void onEnable(){
 		getServer().getPluginManager().registerEvents(this, this);
 		worldGuard = (WorldGuardPlugin) getWorldGuard();
 		
 		getConfig().addDefault("config.use_points", true);
 		getConfig().addDefault("config.use_economy", false);
 		getConfig().addDefault("config.moneyreward_amount", 20.0);
 		getConfig().addDefault("config.itemid", 264);
 		getConfig().addDefault("config.itemamount", 1);
 		getConfig().addDefault("config.auto_updating", true);
 		getConfig().addDefault("config.cooldown", 24);
 		
		getConfig().addDefault("config.maxplayers", 3);
 		
 		getConfig().addDefault("strings.nopermission", "4You don't have permission!");
 		getConfig().addDefault("strings.createcourse", "2WitherFight saved. Now create a spawn and a lobby. :)");
 		getConfig().addDefault("strings.help1", "2WitherFight help:");
 		getConfig().addDefault("strings.help2", "2Use '/wither createarena<name>' to create a new WitherFight.");
 		getConfig().addDefault("strings.help3", "2Use '/wither setlobby <name>' to set the lobby for an WitherFight.");
 		getConfig().addDefault("strings.help4", "2Use '/wither setspawn <name>' to set a new WitherFight spawn.");
 		getConfig().addDefault("strings.lobbycreated", "2Lobby successfully created!");
 		getConfig().addDefault("strings.spawn", "2Spawnpoint registered.");
 		getConfig().addDefault("strings.courseremoved", "4WitherFight removed.");
 		getConfig().addDefault("strings.reload", "2WitherFight config successfully reloaded.");
 		getConfig().addDefault("strings.nothing", "4This command action was not found.");
 		getConfig().addDefault("strings.ingame", "eYou are not able to use any commands while in this minigame. You can use /wither leave if you want to leave the minigame.");
 		getConfig().addDefault("strings.left", "eYou left the WitherFight!");
 		
 		
 		if(getConfig().getBoolean("config.use_economy")){
 			economy = true;
 			if (!setupEconomy()) {
 	            getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
 	            //getServer().getPluginManager().disablePlugin(this);
 	            economy = false;
 	        }
 		}
 		
 		getConfig().options().copyDefaults(true);
 		this.saveConfig();
 	}
 	
 	public Plugin getWorldGuard(){
     	return Bukkit.getPluginManager().getPlugin("WorldGuard");
     }
 
 
 	private boolean setupEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         econ = rsp.getProvider();
         return econ != null;
     }
 
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equalsIgnoreCase("wither")){
     		if(args.length < 1){
     			sender.sendMessage(getConfig().getString("strings.help1"));
     			sender.sendMessage(getConfig().getString("strings.help2"));
     			sender.sendMessage(getConfig().getString("strings.help3"));
     			sender.sendMessage(getConfig().getString("strings.help4"));
     			return true;
     		}else{
     			Player p = (Player)sender;
     			if(args.length > 0){
     				String action = args[0];
     				if(action.equalsIgnoreCase("createarena") && args.length > 1){
     					// Create arena
     					if(p.hasPermission("wither.create")){
     						this.getConfig().set(args[1] + ".name", args[1]);
 	    	    			this.getConfig().set(args[1] + ".world", p.getWorld().getName());
 	    	    			this.saveConfig();
 	    	    			String arenaname = args[1];
 	    	    			sender.sendMessage(getConfig().getString("strings.createcourse"));
     					}
     				}else if(action.equalsIgnoreCase("setlobby") && args.length > 1){
     					// setlobby
     					if(p.hasPermission("wither.setlobby")){
     						String arena = args[1];
 	    		    		Location l = p.getLocation();
 	    		    		getConfig().set(args[1] + ".lobbyspawn.x", (int)l.getX());
 	    		    		getConfig().set(args[1] + ".lobbyspawn.y", (int)l.getY());
 	    		    		getConfig().set(args[1] + ".lobbyspawn.z", (int)l.getZ());
 	    		    		getConfig().set(args[1] + ".lobbyspawn.world", p.getWorld().getName());
 	    		    		this.saveConfig();
 	    		    		sender.sendMessage(getConfig().getString("strings.lobbycreated"));
     					}
     				}else if(action.equalsIgnoreCase("setspawn") && args.length > 1){
     					// setspawn
     					if(p.hasPermission("wither.setspawn")){
     						String arena = args[1];
     			    		Location l = p.getLocation();
     			    		getConfig().set(args[1] + ".spawn.x", (int)l.getX());
     			    		getConfig().set(args[1] + ".spawn.y", (int)l.getY());
     			    		getConfig().set(args[1] + ".spawn.z", (int)l.getZ());
     			    		getConfig().set(args[1] + ".spawn.world", p.getWorld().getName());
     			    		this.saveConfig();
     			    		sender.sendMessage(getConfig().getString("strings.spawn"));
     					}
     				}else if(action.equalsIgnoreCase("removearena") && args.length > 1){
     					// removearena
     					if(p.hasPermission("wither.remove")){
     						this.getConfig().set(args[1], null);
 	    	    			this.saveConfig();
 	    	    			sender.sendMessage(getConfig().getString("strings.courseremoved"));
     					}
     				}else if(action.equalsIgnoreCase("leave")){
     					// leave
     					//if(p.hasPermission("wither.leave")){
     					if(arenap.containsKey(p)){
     						String arena = arenap.get(p);
     						final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world")), getConfig().getDouble(arena + ".lobbyspawn.x"), getConfig().getDouble(arena + ".lobbyspawn.y"), getConfig().getDouble(arena + ".lobbyspawn.z"));
                 			p.teleport(t);
                 			arenap.remove(p);
                 			p.sendMessage(getConfig().getString("strings.left"));
                 			
                 			Player p2 = p;
                     		p2.getInventory().clear();
                 	    	p2.updateInventory();
                 	    	p2.getInventory().setContents(pinv.get(p2));
                 	    	p2.getInventory().setArmorContents(parmor.get(p2));
                 	    	p2.updateInventory();
                 	    	
                 	    	// no players in given arena anymore -> update sign
                 	    	Sign s = this.getSignFromArena(arena);
                 	    	if(!arenap.values().contains(arena)){
                 	    		s.setLine(2, "2Join!");
                	    		s.setLine(3, "0/" + Integer.toString(getConfig().getInt("config.maxplayers")));
                 	    		s.update();
                 	    	}
     					}
     					//}
     				}else if(action.equalsIgnoreCase("list")){
     					// list
     					if(p.hasPermission("wither.list")){
     						ArrayList<String> keys = new ArrayList<String>();
 	    			        keys.addAll(getConfig().getKeys(false));
 	    			        try{
 	    			        	keys.remove("config");
 	    			        	keys.remove("strings");
 	    			        }catch(Exception e){
 	    			        	
 	    			        }
 	    			        for(int i = 0; i < keys.size(); i++){
 	    			        	if(!keys.get(i).equalsIgnoreCase("config") && !keys.get(i).equalsIgnoreCase("strings")){
 	    			        		sender.sendMessage("2" + keys.get(i));
 	    			        	}
 	    			        }
     					}
     				}else if(action.equalsIgnoreCase("reload")){
     					if(sender.hasPermission("wither.reload")){
 	    					this.reloadConfig();
 	    					sender.sendMessage(getConfig().getString("strings.reload"));
     					}else{
     						sender.sendMessage(getConfig().getString("strings.nopermission"));
     					}
     				}else{
     					sender.sendMessage(getConfig().getString("strings.nothing"));
     				}
     			}
     		}
     		return true;
     	}
     	return false;
     }
 	
 	
 	@EventHandler
 	public void onPlayerLeave(PlayerQuitEvent event){
 		Player p = event.getPlayer();
 		if(arenap.containsKey(p)){
 			tpthem.put(p, arenap.get(p));
 			String arena = arenap.get(p);
 			final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world")), getConfig().getDouble(arena + ".lobbyspawn.x"), getConfig().getDouble(arena + ".lobbyspawn.y"), getConfig().getDouble(arena + ".lobbyspawn.z"));
 			p.teleport(t);
 			arenap.remove(p);	
 		}
 	}
 	
 	@EventHandler
     public void onPlayerDeath(PlayerDeathEvent event){
 		Player p = event.getEntity();
 		if(arenap.containsKey(p)){
 			tpthem.put(p, arenap.get(p));
 			String arena = arenap.get(p);
 			final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world")), getConfig().getDouble(arena + ".lobbyspawn.x"), getConfig().getDouble(arena + ".lobbyspawn.y"), getConfig().getDouble(arena + ".lobbyspawn.z"));
 			p.teleport(t);
 			arenap.remove(p);
 			
 			Player p2 = p;
 			p2.getInventory().clear();
 	    	p2.updateInventory();
 	    	p2.getInventory().setContents(pinv.get(p2));
 	    	p2.getInventory().setArmorContents(parmor.get(p2));
 	    	p2.updateInventory();	
 		}
 		
 	}
 	
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event){
 		if(tpthem.containsKey(event.getPlayer())){
 			String arena = tpthem.get(event.getPlayer());
 			Player p = event.getPlayer();
 			final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world")), getConfig().getDouble(arena + ".lobbyspawn.x"), getConfig().getDouble(arena + ".lobbyspawn.y"), getConfig().getDouble(arena + ".lobbyspawn.z"));
 			p.teleport(t);
 		}
 	}
 	
 	
 	@EventHandler
 	public void onSignUse(PlayerInteractEvent event)
 	{
 	    if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
 	    {
 	        if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
 	        {
 	            final Sign s = (Sign) event.getClickedBlock().getState();
                 if (s.getLine(0).equalsIgnoreCase("2[witherfight]"))
                 {
                 	boolean update = true;
                 	for(Player p_ : arenap.keySet()){
                 		if(arenap.containsKey(p_)){
                 			update = false;
                 		}
                 	}
                 	if(update){
             			s.setLine(2, "2Join!");
            			s.setLine(3, "0/" + Integer.toString(getConfig().getInt("config.maxplayers")));
             			s.update();
                 	}
                 	
                 	String arena = s.getLine(1);
                	getLogger().info("ARENAP COUNT: " + Integer.toString(arenap.values().size()));
                 	// no players in given arena anymore -> update sign
         	    	if(!arenap.values().contains(arena)){
         	    		s.setLine(2, "2Join!");
        	    		s.setLine(3, "0/" + Integer.toString(getConfig().getInt("config.maxplayers")));
         	    		s.update();
         	    	}
                 	
                 	if(s.getLine(2).equalsIgnoreCase("2Join!")){
                     	arena = arena.substring(2);
                     	Player p = event.getPlayer();
                     	int currentcount = Integer.parseInt(s.getLine(3).substring(0, 1));
                     	
                     	if(currentcount < getConfig().getInt("config.maxplayers") - 1){ // 3
                     		Player p2 = event.getPlayer();
                     		pinv.put(p2, p2.getInventory().getContents());
                     		parmor.put(p2, p2.getInventory().getArmorContents());
                     		p2.getInventory().clear();
                 	    	p2.updateInventory();
                 	    	p2.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                 	    	p2.getInventory().addItem(new ItemStack(Material.BOW));
                 	    	p2.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                 	    	p2.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                 	    	p2.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                 	    	p2.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                 	    	p2.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
                 	    	p2.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                 	    	p2.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                 	    	p2.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
                 	    	p2.updateInventory();
                     		
     	                	arenap.put(event.getPlayer(), arena);
     	                	
     	                	p2.setAllowFlight(false);
     	                	//p2.setGameMode(GameMode.SURVIVAL);
     	                	p2.setFlying(false);
     	                	p2.setHealth(20D);
     	                	
     	                	event.getPlayer().sendMessage("2You have entered the WitherFight minigame!");
     	                	
     	                	final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getDouble(arena + ".spawn.x"), getConfig().getDouble(arena + ".spawn.y"), getConfig().getDouble(arena + ".spawn.z"));
     	        			event.getPlayer().teleport(t);
     	        			
     	        			s.setLine(3, Integer.toString(currentcount) + "/" + getConfig().getString("config.maxplayers"));
     	        			s.update();
                     	}else{
                     		Player p2 = event.getPlayer();
                     		pinv.put(p2, p2.getInventory().getContents());
                     		parmor.put(p2, p2.getInventory().getArmorContents());
                     		p2.getInventory().clear();
                 	    	p2.updateInventory();
                 	    	p2.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                 	    	p2.getInventory().addItem(new ItemStack(Material.BOW));
                 	    	p2.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                 	    	p2.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                 	    	p2.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                 	    	p2.getInventory().addItem(new ItemStack(Material.ARROW, 64));
                 	    	p2.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
                 	    	p2.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                 	    	p2.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                 	    	p2.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
                 	    	p2.updateInventory();
                     		
     	                	arenap.put(event.getPlayer(), arena);
     	                	
     	                	event.getPlayer().sendMessage("2You have entered the WitherFight minigame!");
     	                	
     	                	final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getDouble(arena + ".spawn.x"), getConfig().getDouble(arena + ".spawn.y"), getConfig().getDouble(arena + ".spawn.z"));
     	        			event.getPlayer().teleport(t);
                     		
                     		// start the game! spawn a wither and update the sign.
                     		s.setLine(2, "4Ingame!");
                     		s.setLine(3, Integer.toString(currentcount) + "/" + getConfig().getString("config.maxplayers"));
     	        			s.update();
                     		for(Player p_ : arenap.keySet()){
                     			if(arenap.get(p_).equalsIgnoreCase(arena)){
                     				p_.sendMessage("2The Fight has started! Good luck! 3You can leave with /wither leave.");
                     			}
                     		}
                     		Wither w = p.getWorld().spawn(new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getInt(arena + ".spawn.x"), getConfig().getInt(arena + ".spawn.y"), getConfig().getInt(arena + ".spawn.z")), Wither.class);
                     		w.setMaxHealth(200D);
                     		w.setHealth(200D);
                     		for(int i = 0; i < 20; i++){
                     			Skeleton sk = p.getWorld().spawn(new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getInt(arena + ".spawn.x"), getConfig().getInt(arena + ".spawn.y"), getConfig().getInt(arena + ".spawn.z")), Skeleton.class);	
                     			sk.setSkeletonType(SkeletonType.WITHER);
                     		}
                     	}
                 	}
                 }
 	        }
 	    }
 	}
 	
 	
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event){
         Entity entity = event.getEntity();
         if(entity instanceof Wither){ 
             if(entity.getLastDamageCause() instanceof EntityDamageByEntityEvent){ 
                 EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entity.getLastDamageCause();
                 if(entityDamageByEntityEvent.getDamager() instanceof Player){ 
                     Player killer = (Player)entityDamageByEntityEvent.getDamager();
                     //do stuff to killer
                     if(arenap.containsKey(killer)){
                     	getServer().broadcastMessage("2[WitherFight] 3" + killer.getName() + " just won a WitherFight!");
                         
                         Player p = killer;
                         
                         if(getConfig().getBoolean("config.use_economy")){
                     		EconomyResponse r = econ.depositPlayer(p.getName(), getConfig().getDouble("config.moneyreward_amount"));
                 			if(!r.transactionSuccess()) {
                 				p.sendMessage(String.format("An error occured: %s", r.errorMessage));
                                 //sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
                             }
                     	}else{
                     		getServer().dispatchCommand(getServer().getConsoleSender(), "enjin addpoints " + p.getName() + " 5");
                     	}
                     	String arena = arenap.get(p);
     					final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world")), getConfig().getDouble(arena + ".lobbyspawn.x"), getConfig().getDouble(arena + ".lobbyspawn.y"), getConfig().getDouble(arena + ".lobbyspawn.z"));
             			p.teleport(t);
             			arenap.remove(p);
             			
             			Player p2 = p;
                 		p2.getInventory().clear();
             	    	p2.updateInventory();
             	    	p2.getInventory().setContents(pinv.get(p2));
             	    	p2.getInventory().setArmorContents(parmor.get(p2));
             	    	p2.updateInventory();
             			
             			for(Player p_ : arenap.keySet()){
             				if(arenap.get(p_).equalsIgnoreCase(arena)){
             					Location t_ = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world")), getConfig().getDouble(arena + ".lobbyspawn.x"), getConfig().getDouble(arena + ".lobbyspawn.y"), getConfig().getDouble(arena + ".lobbyspawn.z"));
                     			p_.teleport(t_);
                     			arenap.remove(p_);
                     			
                     			Player p2_ = p_;
                     			p2_.getInventory().clear();
                     			p2_.updateInventory();
                     			p2_.getInventory().setContents(pinv.get(p2_));
                     			p2_.getInventory().setArmorContents(parmor.get(p2_));
                     			p2_.updateInventory();
             				}
             			}
             			
             			Sign s = getSignFromArena(arena);
             			s.setLine(2, "2Join!");
             			s.setLine(3, "0/" + getConfig().getString("config.maxplayers"));
             			s.update();
                     }
                 }
             }
         }
     }
 	
 	
 	
 	@EventHandler
     public void onSignChange(SignChangeEvent event) {
         Player p = event.getPlayer();
         if(event.getLine(0).toLowerCase().contains("[witherfight]")){
         	if(event.getPlayer().hasPermission("wither.sign")){
 	        	event.setLine(0, "2[WitherFight]");
 	        	if(!event.getLine(1).equalsIgnoreCase("")){
 	        		String arena = event.getLine(1);
 	        		event.setLine(1, "5" +  arena);
 	        		event.setLine(2, "2Join!");
 	        		event.setLine(3, "0/" + getConfig().getString("config.maxplayers"));
 	        		
 	        		getConfig().set(arena + ".sign.world", event.getBlock().getWorld().getName());
 	        		getConfig().set(arena + ".sign.x", event.getBlock().getLocation().getBlockX());
 	        		getConfig().set(arena + ".sign.y", event.getBlock().getLocation().getBlockY());
 	        		getConfig().set(arena + ".sign.z", event.getBlock().getLocation().getBlockZ());
 	        		this.saveConfig();
 	        	}
         	}
         }
 	}
 	
 
 	
 	@EventHandler
 	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
 		if(arenap.containsKey(event.getPlayer())){
 			// j leave
 			if(event.getMessage().equalsIgnoreCase("/wither leave")){
 				// nothing
 			}else{
 				event.setCancelled(true);
 				event.getPlayer().sendMessage(getConfig().getString("strings.ingame"));
 			}
 		}
 	}
 	
 	
 	
 	public Sign getSignFromArena(String arena){
 		Location b_ = new Location(Bukkit.getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getDouble(arena + ".sign.x"), getConfig().getDouble(arena + ".sign.y"), getConfig().getDouble(arena + ".sign.z"));
     	BlockState bs = b_.getBlock().getState();
     	Sign s_ = null;
     	if(bs instanceof Sign){
     		s_ = (Sign)bs;
     	}else{
     		getLogger().info("Could not find sign: " + bs.getBlock().toString());
     	}
 		return s_;
 	}
 }
