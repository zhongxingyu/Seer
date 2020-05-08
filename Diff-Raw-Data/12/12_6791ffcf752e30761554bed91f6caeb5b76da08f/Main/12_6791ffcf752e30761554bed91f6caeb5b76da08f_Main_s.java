 package com.comze_instancelabs.boatgame;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
 import org.bukkit.event.vehicle.VehicleCreateEvent;
 import org.bukkit.event.vehicle.VehicleDamageEvent;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.event.vehicle.VehicleEnterEvent;
 import org.bukkit.event.vehicle.VehicleExitEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 import org.bukkit.util.Vector;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 /**
  * 
  * @author instancelabs
  *
  */
 
 public final class Main extends JavaPlugin implements Listener{
 	
 	//TODO: Features
 
 	
 	//TODO: BUGS
 	// [LOW] When player holding solid blocks like cobblestone (which can be placed), snowballs won't be added, because interaction with sign fails
 	
 	
 	public static Economy econ = null;
 	public boolean economy = false;
 	
 	static HashMap<Player, String> arenap = new HashMap<Player, String>(); // playername -> arenaname
 	static HashMap<Player, Integer> hp = new HashMap<Player, Integer>(); // playername -> integer of BOATHEALTH
 	static HashMap<Player, Integer> php = new HashMap<Player, Integer>(); // playername -> integer of PLAYERHEALTH
 	static HashMap<String, Boolean> gamestarted = new HashMap<String, Boolean>(); // arenaname -> game started or not
 	HashMap<Player, Integer> canceltask = new HashMap<Player, Integer>(); // player -> task
 	HashMap<String, Integer> secs_ = new HashMap<String, Integer>(); // arena -> cooldown seconds
 	HashMap<String, Player> secs_updater = new HashMap<String, Player>(); // the current seconds updater (player) in the arena
 	HashMap<String, String> tpthem = new HashMap<String, String>();
 	//HashMap<Player, Integer> lock_spawn = new HashMap<Player, Integer>(); // player -> spawn 1 or 2
 	HashMap<Player, Integer> team = new HashMap<Player, Integer>(); // player -> team integer
 	static HashMap<Player, ItemStack[]> pinv = new HashMap<Player, ItemStack[]>(); // player -> Inventory
 	HashMap<Player, Integer> pspawn = new HashMap<Player, Integer>(); // player -> spawn 1 etc.
 	static HashMap<String, Integer> arenaspawn = new HashMap<String, Integer>(); // arena -> current spawn count
 	static HashMap<Player, Integer> usedammo = new HashMap<Player, Integer>(); // player -> Usage count of ammo signs
 	
 	String arenaname = "";
 	
 	@Override
     public void onEnable(){
 		getLogger().info("Initializing BoatGame . . .");
 		getServer().getPluginManager().registerEvents(this, this);
 		
 		getConfig().addDefault("config.alwaysdropboat", true);
 		getConfig().addDefault("config.invincibleboats", false);
 		getConfig().addDefault("config.boatspeed_global", 0.4);
 		getConfig().addDefault("config.boatspeed_arena", 0.4);
 		getConfig().addDefault("config.boatlifes", 5);
 		getConfig().addDefault("config.playerlifes", 1);
 		getConfig().addDefault("config.use_economy", true);
 		getConfig().addDefault("config.entry_money", 10.0);
 		//getConfig().addDefault("config.itemreward_itemid", itemids);
 		getConfig().addDefault("config.itemreward_itemid", new Integer[] {264, 9});
 		getConfig().addDefault("config.itemreward_amount", 2);
 		getConfig().addDefault("config.maxplayers", 10);
 		getConfig().addDefault("config.minplayers", 2);
 		getConfig().addDefault("config.starting_cooldown", 11);
 		getConfig().addDefault("config.teams", false);
 		getConfig().addDefault("config.auto_updating", true);
 		getConfig().addDefault("config.announce_winners", true);
 		getConfig().addDefault("config.lastmanstanding", true);
 		getConfig().addDefault("config.ammo_sign_usage_count", 2);
 		//getConfig().addDefault("config.saveandclearinventory", false);
 		
 		// TODO: new
 		getConfig().addDefault("config.snowballstacks_amount", 3);
 		
 		
 		getConfig().addDefault("strings.ball_name", "2BoatBall");
 		getConfig().addDefault("strings.nopermission", "4You don't have permission!");
 		getConfig().addDefault("strings.createarena", "2Arenaname saved. Now create two spawn points with /sb setspawn <count> <name> and a lobby spawn with /sb setlobby <name>.");
 		getConfig().addDefault("strings.help1", "2Seabattle help:");
 		getConfig().addDefault("strings.help2", "2Use '/sb createarena <name>' to create a new arena");
 		getConfig().addDefault("strings.help3", "2Use '/sb setlobby <name>' to set the lobby for an arena");
 		getConfig().addDefault("strings.help4", "2Use '/sb setspawn <count> <name>' to set a new spawn. <count> can be 1 or 2.");
 		getConfig().addDefault("strings.lobbycreated", "2Lobby successfully created!");
 		getConfig().addDefault("strings.spawn", "2Spawnpoint registered.");
 		//getConfig().addDefault("strings.spawn2", "2Second spawnpoint registered. Now create a join sign at the lobby (create a lobbyspawn if haven't done already) and start playing! :)");
 		getConfig().addDefault("strings.spawnerror", "4An error occured. Did you provide a number (1 or 2)?");
 		getConfig().addDefault("strings.arenaremoved", "4Arena removed.");
 		getConfig().addDefault("strings.notenoughmoney", "4You don't have enough money!");
 		getConfig().addDefault("strings.won", "2[BOATGAME] You won the game!");
 		getConfig().addDefault("strings.lost", "4[BOATGAME] You lost the game!");
 		getConfig().addDefault("strings.lostlife", "4[BOATGAME] You lost one life! Lifes left: ");
 		getConfig().addDefault("strings.reload", "2Boatgame config successfully reloaded.");
 		getConfig().addDefault("strings.nothing", "4This command action was not found.");
 		
 		getConfig().addDefault("tpthem.null", "");
 		getConfig().options().copyDefaults(true);
 		this.saveDefaultConfig();
 		this.saveConfig();
 		
 		if(getConfig().getBoolean("config.use_economy")){
 			economy = true;
 			if (!setupEconomy()) {
 	            getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
 	            economy = false;
 	        }
 		}
 		
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 			// Failed to submit the stats :(
 		}
 		
 		ArrayList<String> keys = new ArrayList<String>();
         keys.addAll(getConfig().getKeys(false));
         for(int i = 0; i < keys.size(); i++){
             gamestarted.put(keys.get(i), false);
         }
         
         if(getConfig().getBoolean("config.auto_updating")){
         	//Updater updater = new Updater(this, "sea-battle", this.getFile(), Updater.UpdateType.DEFAULT, false);
         	Updater updater = new Updater(this, 60894, this.getFile(), Updater.UpdateType.DEFAULT, false);
         }
         
         for(String p_ : getConfig().getConfigurationSection("tpthem.").getKeys(false)){
         	if(Bukkit.getOfflinePlayer(p_).isOnline()){
         		Player p = Bukkit.getPlayer(p_);
         		String arena = getConfig().getString("tpthem." + p_);
         		
         		Double x = getConfig().getDouble(arena + ".lobbyspawn.x");
     	    	Double y = getConfig().getDouble(arena + ".lobbyspawn.y");
     	    	Double z = getConfig().getDouble(arena + ".lobbyspawn.z");
         		World w = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
     	    	Location t = new Location(w, x, y, z);
     	    	
         		BukkitTask task = new futask(p, t, false, getConfig().getInt("config.snowballstacks_amount")).runTaskLater(this, 20);
         	
         		getConfig().set("tpthem." + p_, null);
         		this.saveConfig();
         	}
         }
     }
 
     @Override
     public void onDisable() {
     	for(String arena : arenap.values()){
     		for(Player p2 : this.getKeysByValue(arenap, arena)){
         		//remove vehicle, remove snowballs, tp away
     	    	p2.getVehicle().remove();
     	    	
     	    	p2.updateInventory();
     	    	for(int i_ = 0; i_ < getConfig().getInt("config.snowballstacks_amount") + 1; i_++){
     				p2.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 64));	
     			}
     	    	p2.getInventory().setContents(pinv.get(p2));
     	    	p2.updateInventory();
     	    	
     	    	tpthem.put(p2.getName(), arenap.get(p2));
     	    	getConfig().set("tpthem." + p2.getName(), arenap.get(p2));
     	    	this.saveConfig();
     	    	
     	    	if(p2.isOnline()){
 	    	    	Double x = getConfig().getDouble(arena + ".lobbyspawn.x");
 	    	    	Double y = getConfig().getDouble(arena + ".lobbyspawn.y");
 	    	    	Double z = getConfig().getDouble(arena + ".lobbyspawn.z");
 	        		World w = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
 	    	    	Location t = new Location(w, x, y, z);
 	    	    	
 	    	    	p2.teleport(t);
     	    	}
     	    	arenap.remove(p2);
     	    	
 
     	    	Location b = new Location(Bukkit.getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getDouble(arena + ".sign.x"),getConfig().getDouble(arena + ".sign.y"), getConfig().getDouble(arena + ".sign.z"));
     	    	Sign s = (Sign)Bukkit.getWorld(getConfig().getString(arena + ".sign.world")).getBlockAt(b).getState();
     	    	// update sign: 
                 if(s != null && s.getLine(3) != ""){
                 	String d = s.getLine(3).split("/")[0];
                 	int bef = Integer.parseInt(d);
                 	if(bef > 0){
                 		s.setLine(3, Integer.toString(bef - 1) + "/" + getConfig().getString("config.maxplayers"));
                 		s.setLine(2, "2Join");
                 		s.update();
                 	}
                 }
     		}
     	}
     }
     
     
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
     	if(cmd.getName().equalsIgnoreCase("cleararena-boat")){
     		if(args.length < 1){
     			sender.sendMessage("Usage: /cleararena [Name]");
     		}else{
     			if (sender.hasPermission("boatgame.cleararena"))
                 {
     				while (arenap.values().remove(args[0]));
     				while (hp.values().remove(args[0]));
                 }else{
                 	sender.sendMessage("4You don't have permission!");
                 }
     		}
     		return true;
     	}else if(cmd.getName().equalsIgnoreCase("sb") || cmd.getName().equalsIgnoreCase("seabattle")){
     		if(args.length < 1){
     			sender.sendMessage(getConfig().getString("strings.help1"));
     			sender.sendMessage(getConfig().getString("strings.help2"));
     			sender.sendMessage(getConfig().getString("strings.help3"));
     			sender.sendMessage(getConfig().getString("strings.help4"));
     		}else{
     			if(args.length > 0){
     				String action = args[0];
     				if(action.equalsIgnoreCase("createarena") && args.length > 1){
     					if (sender.hasPermission("boatgame.create"))
     	                {
     						Player temp = (Player)sender;
 	    	    			this.getConfig().set(args[1] + ".name", args[1]);
 	    	    			this.getConfig().set(args[1] + ".world", temp.getWorld().getName());
 	    	    			this.saveConfig();
 	    	    			arenaname = args[1];
 	    	    			sender.sendMessage(getConfig().getString("strings.createarena"));
     	                }else{
     	                	sender.sendMessage(getConfig().getString("strings.nopermission"));
     	                }
     				}else if(action.equalsIgnoreCase("setlobby") && args.length > 1){
     					if (sender.hasPermission("boatgame.setlobby"))
     	                {
 	    	    			String arena = args[1];
 	    		    		Player p = (Player) sender;
 	    		    		Location l = p.getLocation();
 	    		    		getConfig().set(args[1] + ".lobbyspawn.x", (int)l.getX());
 	    		    		getConfig().set(args[1] + ".lobbyspawn.y", (int)l.getY());
 	    		    		getConfig().set(args[1] + ".lobbyspawn.z", (int)l.getZ());
 	    		    		getConfig().set(args[1] + ".lobbyspawn.world", p.getWorld().getName());
 	    		    		this.saveConfig();
 	    		    		sender.sendMessage(getConfig().getString("strings.lobbycreated"));
     	                }else{
     	                	sender.sendMessage(getConfig().getString("strings.nopermission"));
     	                }
     					
     				}else if(action.equalsIgnoreCase("setspawn") && args.length > 2){
     					if (sender.hasPermission("boatgame.setspawn"))
     	                {
     						Player p = (Player) sender;
     						String arena = args[2];
      						String count = args[1];
      			    		Location l = p.getLocation();
      			    		getConfig().set(args[2] + ".spawn" + count + ".x", (int)l.getX());
      			    		getConfig().set(args[2] + ".spawn" + count + ".y", (int)l.getY());
      			    		getConfig().set(args[2] + ".spawn" + count + ".z", (int)l.getZ());
      			    		getConfig().set(args[2] + ".spawn" + count + ".world", p.getWorld().getName());
      			    		this.saveConfig();
      			    		sender.sendMessage(getConfig().getString("strings.spawn"));
     	                }else{
     	                	sender.sendMessage(getConfig().getString("strings.nopermission"));
     	                }
     				}else if(action.equalsIgnoreCase("removearena") && args.length > 1){
     					if (sender.hasPermission("boatgame.remove"))
     	                {
 	    	    			this.getConfig().set(args[1], null);
 	    	    			this.saveConfig();
 	    	    			sender.sendMessage(getConfig().getString("strings.arenaremoved"));
     	                }else{
     	                	sender.sendMessage(getConfig().getString("strings.nopermission"));
     	                }
     				}else if(action.equalsIgnoreCase("reset") && args.length > 0){
     					if(args.length > 1){
 	    					if (sender.hasPermission("boatgame.cleararena"))
 	    	                {
 		    	    			String arena = args[1];
 		    	    			
 		    	    			if(getConfig().contains(arena)){
 			    	    			// tp players out
 			    	    			for(Player p : arenap.keySet()) {
 			    	    				if(arenap.get(p).equalsIgnoreCase(arena)){
 			    	    					Double x = getConfig().getDouble(arena + ".lobbyspawn.x");
 			        				    	Double y = getConfig().getDouble(arena + ".lobbyspawn.y");
 			        				    	Double z = getConfig().getDouble(arena + ".lobbyspawn.z");
 			        			    		World w = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
 			        				    	Location t = new Location(w, x, y, z);
 			        			    		
 			        				    	if(p.isInsideVehicle()){
 			        				    		p.getVehicle().remove();
 			        				    	}
 			        				    	
 			        			    		BukkitTask task = new futask(p, t, false, getConfig().getInt("config.snowballstacks_amount"), pinv.get(p)).runTaskLater(this, 20);
 
 			        			    		p.sendMessage("4The arena just got reset by an operator - leaving game..");
 			    	    				}
 			    	    			}
 			    	    			
 			    	    			while (arenap.values().remove(arena));
 			    	    			gamestarted.put(arena, false);
 		        			    	arenaspawn.remove(arena);
 		        			    	
 		        			    	Location b = new Location(Bukkit.getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getDouble(arena + ".sign.x"),getConfig().getDouble(arena + ".sign.y"), getConfig().getDouble(arena + ".sign.z"));
 		        			    	Sign s = (Sign)Bukkit.getWorld(getConfig().getString(arena + ".sign.world")).getBlockAt(b).getState();
 		        			    	// update sign: 
 		        		            if(s != null && s.getLine(3) != ""){
 		    		            		s.setLine(3, Integer.toString(0) + "/" + getConfig().getString("config.maxplayers"));
 		    		            		s.setLine(2, "2Join");
 		    		            		s.update();
 		    		            		secs_.remove(arena);
 		        		            }
 		        		            sender.sendMessage("2Arena reset.");	
 		    	    			}else{
 		    	    				sender.sendMessage("4This arena couldn't be found.");
 		    	    			}
 	    	                }else{
 	    	                	sender.sendMessage(getConfig().getString("strings.nopermission"));
 	    	                }	
     					}else{
     						sender.sendMessage("4Please provide an arenaname! Usage: /sb reset [name]");
     					}
     				}else if(action.equalsIgnoreCase("leave")){
     					final Player p2 = (Player)sender;
     					//getLogger().info("There are " + Integer.toString(arenap.size() - 1) + " Players in the arena now.");
     			    	if(arenap.containsKey(p2)){
     			    		//remove vehicle, remove snowballs, tp away
     			    		if(p2.getVehicle() != null){
     			    			p2.getVehicle().remove();	
     			    		}else{
     			    			getLogger().severe("[HIGH] -SeaBattle- An Error occured: boat was not found.");
     			    		}
     				    	
     				    	
     				    	p2.updateInventory();
     				    	p2.getInventory().setContents(pinv.get(p2));
     				    	//p2.sendMessage(pinv.get(p2).toString());
     				    	p2.updateInventory();
     				    	for(int i_ = 0; i_ < getConfig().getInt("config.config.snowballstacks_amount") + 1; i_++){
     		    				p2.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 64));	
     		    			}
     				    	p2.getInventory().setContents(pinv.get(p2));
     				    	p2.updateInventory();
     				    	
     				    	String arena = arenap.get(p2);
     			    		
     			    		Double x = getConfig().getDouble(arena + ".lobbyspawn.x");
     				    	Double y = getConfig().getDouble(arena + ".lobbyspawn.y");
     				    	Double z = getConfig().getDouble(arena + ".lobbyspawn.z");
     			    		World w = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
     				    	Location t = new Location(w, x, y, z);
     			    		
     			    		BukkitTask task = new futask(p2, t, false, getConfig().getInt("config.snowballstacks_amount")).runTaskLater(this, 40);
     				    	
     				    	arenap.remove(p2);
     				    	
     				    	Location b = new Location(Bukkit.getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getDouble(arena + ".sign.x"),getConfig().getDouble(arena + ".sign.y"), getConfig().getDouble(arena + ".sign.z"));
     				    	Sign s = (Sign)Bukkit.getWorld(getConfig().getString(arena + ".sign.world")).getBlockAt(b).getState();
     				    	
         			    	getLogger().info(b.toString());
         			    	BlockState bs = b.getBlock().getState();
         			    	Sign s_ = null;
         			    	if(bs instanceof Sign){
         			    		s_ = (Sign)bs;
         			    	}else{
         			    		getLogger().info(bs.getBlock().toString());
         			    	}
     				    	
     				    	// update sign:
                             if(s != null && s.getLine(3) != ""){
                             	String d = s.getLine(3).split("/")[0];
                             	getLogger().info(d);
                             	Integer bef = Integer.parseInt(d);
                             	if(bef > 1){
                             		s.setLine(3, Integer.toString(bef - 1) + "/" + getConfig().getString("config.maxplayers"));
                             		s.update();
                             		getLogger().info(s.getLine(3));
                             	}
                             	if(getConfig().getBoolean("config.lastmanstanding")){
 	                            	if(bef.equals(2)){ // one player left -> gets prize
 	                            		Player last = this.getKeyByValue(arenap, arena);
 	                            		
 	                            		if(last != null){
 	                                		last.sendMessage("3You are the last man standing and got a prize! Leave with /sb leave.");
 	                	            		
 	                                		last.getVehicle().remove();
 	                				    	
 	                                		last.updateInventory();
 	                                		last.getInventory().setContents(pinv.get(last));
 	                                		last.updateInventory();
 	                				    	for(int i_ = 0; i_ < getConfig().getInt("config.config.snowballstacks_amount") + 1; i_++){
 	                				    		last.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 64));	
 	                		    			}
 	                				    	last.getInventory().setContents(pinv.get(last));
 	                				    	last.updateInventory();
 	
 	                				    	Location t_ = new Location(w, x, y, z);
 	                			    		
 	                			    		BukkitTask task_ = new futask(last, t_, false, getConfig().getInt("config.snowballstacks_amount")).runTaskLater(this, 40);
 	                				    	
 	                				    	arenap.remove(last);
 	                                		
 	                				    	s.setLine(2, "2Join");
 	                                		s.setLine(3, "0/" + getConfig().getString("config.maxplayers"));
 	                                		s.update();
 	                                		
 	                                		arenaspawn.remove(arena);
 	                                		try{
 	                                			getServer().getScheduler().cancelTask(canceltask.get(secs_updater.get(arena)));
 	                                		}catch(Exception e){
 	                                			try{
 	                                    			getServer().getScheduler().cancelTask(canceltask.get(last));
 	                                    		}catch(Exception e_){
 	                                    		}
 	                                		}
 	                                		secs_.remove(arena);
 	                				    	
 	                                		if(economy){
 	                	            			EconomyResponse r = econ.depositPlayer(last.getName(), getConfig().getDouble("config.entry_money") * 2);
 	                	            			if(!r.transactionSuccess()) {
 	                	            				last.sendMessage(String.format("An error occured: %s", r.errorMessage));
 	                	                        }
 	                	            		}else{
 	                	            			List<Integer> itemid = getConfig().getIntegerList("config.itemreward_itemid");
 	                	            			
 	                	            			// TODO: Amounts feature
 	                	            			
 	                	            			//List<String> itemid = getConfig().getStringList("config.itemreward_itemid");
 	                	            			/*HashMap<Integer, Integer> item_idamount = new HashMap<Integer, Integer>();
 	                	            			for(String str : itemid){
 	                	            				item_idamount.put(Integer.parseInt(str.substring(str.indexOf(",") + 1, str.length())), Integer.parseInt(str.substring(0, str.substring(str.indexOf(",") - 1))
 	                	            			}*/
 	                	            			
 	                	            			int itemid_amount = getConfig().getInt("config.itemreward_amount");
 	                	            			last.updateInventory();
 	                		            		if(itemid.size() > 0){
 	                		            			for(int id : itemid){
 	                		            				last.getInventory().addItem(new ItemStack(Material.getMaterial(id), itemid_amount));
 	                		            				last.updateInventory();
 	                		            			}
 	                		                    }
 	                	            		}
 	                                		
 	                                		gamestarted.put(arena, false);
 	                            		}
 	                            	}	
                             	}
                             	
                             	if(bef < 2){
                             		s.setLine(2, "2Join");
                             		s.setLine(3, "0/" + getConfig().getString("config.maxplayers"));
                             		s.update(); 
                             		arenaspawn.remove(arena);
                             		try{
                             			getServer().getScheduler().cancelTask(canceltask.get(secs_updater.get(arena)));
                             		}catch(Exception e){
                             			
                             		}
                             		secs_.remove(arena);
                             		gamestarted.put(arena, false);
                             	}
                             }
     				    }
     				}else if(action.equalsIgnoreCase("list")){
     					if(sender.hasPermission("boatgame.list")){
 	    					ArrayList<String> keys = new ArrayList<String>();
 	    			        keys.addAll(getConfig().getKeys(false));
 	    			        try{
 	    			        	keys.remove("config");
 	    			        	keys.remove("strings");
 	    			        	keys.remove("AutoUpdate");
 	    			        	keys.remove("tpthem");
 	    			        }catch(Exception e){
 	    			        	
 	    			        }
 	    			        for(int i = 0; i < keys.size(); i++){
 	    			        	if(!keys.get(i).equalsIgnoreCase("config") && !keys.get(i).equalsIgnoreCase("strings")){
 	    			        		sender.sendMessage("2" + keys.get(i));
 	    			        	}
 	    			        }
     					}else{
     						sender.sendMessage(getConfig().getString("strings.nopermission"));
     					}
     				}else if(action.equalsIgnoreCase("reload")){
     					if(sender.hasPermission("boatgame.reload")){
 	    					this.reloadConfig();
 	    					sender.sendMessage(getConfig().getString("strings.reload"));
     					}else{
     						sender.sendMessage(getConfig().getString("strings.nopermission"));
     					}
     				}else if(action.equalsIgnoreCase("recreateconfig")){
     					if(sender.hasPermission("boatgame.reload")){
     						getConfig().addDefault("config.alwaysdropboat", true);
     						getConfig().addDefault("config.invincibleboats", false);
     						getConfig().addDefault("config.boatspeed_global", 0.4);
     						getConfig().addDefault("config.boatspeed_arena", 0.4);
     						getConfig().addDefault("config.boatlifes", 5);
     						getConfig().addDefault("config.playerlifes", 1);
     						getConfig().addDefault("config.use_economy", true);
     						getConfig().addDefault("config.entry_money", 10.0);
     						//getConfig().addDefault("config.itemreward_itemid", itemids);
     						getConfig().addDefault("config.itemreward_itemid", new Integer[] {364, 9});
     						getConfig().addDefault("config.itemreward_amount", 2);
     						getConfig().addDefault("config.maxplayers", 10);
     						getConfig().addDefault("config.minplayers", 2);
     						getConfig().addDefault("config.starting_cooldown", 11);
     						getConfig().addDefault("config.teams", false);
     						getConfig().addDefault("config.auto_updating", true);
     						getConfig().addDefault("config.announce_winners", true);
     						//getConfig().addDefault("config.saveandclearinventory", false);
     						getConfig().addDefault("config.snowballstacks_amount", 3);
     						
     						getConfig().addDefault("strings.ball_name", "2BoatBall");
     						getConfig().addDefault("strings.nopermission", "4You don't have permission!");
     						getConfig().addDefault("strings.createarena", "2Arenaname saved. Now create two spawn points with /sb setspawn <count> <name> and a lobby spawn with /sb setlobby <name>.");
     						getConfig().addDefault("strings.help1", "2Seabattle help:");
     						getConfig().addDefault("strings.help2", "2Use '/sb createarena <name>' to create a new arena");
     						getConfig().addDefault("strings.help3", "2Use '/sb setlobby <name>' to set the lobby for an arena");
     						getConfig().addDefault("strings.help4", "2Use '/sb setspawn <count> <name>' to set a new spawn. <count> can be 1 or 2.");
     						getConfig().addDefault("strings.lobbycreated", "2Lobby successfully created!");
     						getConfig().addDefault("strings.spawn", "2Spawnpoint registered.");
     						//getConfig().addDefault("strings.spawn2", "2Second spawnpoint registered. Now create a join sign at the lobby (create a lobbyspawn if haven't done already) and start playing! :)");
     						getConfig().addDefault("strings.spawnerror", "4An error occured. Did you provide a number (1 or 2)?");
     						getConfig().addDefault("strings.arenaremoved", "4Arena removed.");
     						getConfig().addDefault("strings.notenoughmoney", "4You don't have enough money!");
     						getConfig().addDefault("strings.won", "2[BOATGAME] You won the game!");
     						getConfig().addDefault("strings.lost", "4[BOATGAME] You lost the game!");
     						getConfig().addDefault("strings.lostlife", "4[BOATGAME] You lost one life! Lifes left: ");
     						getConfig().addDefault("strings.reload", "2Boatgame config successfully reloaded.");
     						getConfig().addDefault("strings.nothing", "4This command action was not found.");
     						
     						getConfig().options().copyDefaults(true);
     						this.saveDefaultConfig();
     						this.saveConfig();
 	    					sender.sendMessage("2Successfully recreated SeaBattle config.");
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
     
 
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onVehicleExit(VehicleExitEvent event){
     	//getLogger().info(Boolean.toString(event.isCancelled()));
     	if(event.getVehicle().getPassenger() instanceof Player){
     		if(arenap.containsKey(event.getVehicle().getPassenger())){
     			//event.getVehicle().getPassenger().eject();
     			event.setCancelled(true);
     		}
     	}
     }
     
     
     boolean whichteam = true;
     public Boat b = null;
     
     @EventHandler
 	public void onSignUse(PlayerInteractEvent event)
 	{
 	    if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
 	    {
 	        if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
 	        {
 	            final Sign s = (Sign) event.getClickedBlock().getState();
 	
 	            for (int i = 0; i < s.getLines().length - 1; i++)
 	            {
 	                if (s.getLine(i).equalsIgnoreCase("2[boat]"))
 	                {
 	                	if((s.getLine(2).equalsIgnoreCase("2Join") || s.getLine(2).equalsIgnoreCase("2Starting"))){
 		                	//add player to hashmap
 		                	final String arena = s.getLine(i + 1);
 		                	final Player p = event.getPlayer();
 
 		                	if(!validArena(arena)){
 		                		p.sendMessage("4This arena is not set up properly! :(");
 		                		return;
 		                	}
 		                	
 		                	usedammo.put(p, getConfig().getInt("config.ammo_sign_usage_count"));
 		                	
 		                	if(!arenaspawn.containsKey(arena)){
 		                		arenaspawn.put(arena, 1);
 		                		pspawn.put(p, 1);
 		                	}else{
 		                		arenaspawn.put(arena, arenaspawn.get(arena) + 1);
 		                		pspawn.put(p, arenaspawn.get(arena));
 		                	}
 	            			boolean cont1 = true;
 	            			// Entry money!
 	            			if(economy){
 	            				if(econ.getBalance(event.getPlayer().getName()) < 10){
 	            					event.getPlayer().sendMessage(getConfig().getString("strings.notenoughmoney"));
 	            					cont1 = false;
 	            				}else{
 	                				EconomyResponse r = econ.withdrawPlayer(event.getPlayer().getName(), getConfig().getDouble("config.entry_money"));
 	                                if(!r.transactionSuccess()) {
 	                                	event.getPlayer().sendMessage(String.format("An error occured: %s", r.errorMessage));
 	                                    //sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
 	                                }
 	            				}
 	            			}
 	            			
 	            			// update sign:
 	                        if(s.getLine(3) != ""){
 	                        	String d = s.getLine(3).split("/")[0];
 	                        	int bef = Integer.parseInt(d);
 	                        	//getLogger().info(Integer.toString(bef) + " " + Integer.toString(getConfig().getInt("config.maxplayers")));
 	                        	if(bef < getConfig().getInt("config.maxplayers")){
 	                        		if(cont1){
 	                            		s.setLine(3, Integer.toString(bef + 1) + "/" + getConfig().getString("config.maxplayers"));
 	                            		s.update();
 	                            		getLogger().info(s.getLine(2));
 	                            		if(bef > (getConfig().getInt("config.minplayers") - 2) && !s.getLine(2).equalsIgnoreCase("2Starting")){ // there was one player in there, bef > 0
 	                            			getLogger().info(s.getLine(2));
 	                            			//start the cooldown for start (10 secs)
 	                            			s.setLine(2, "2Starting");
 		        		                	s.update();
 	                            			int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 	                            				//int secs = 11;
 	                            				
 	                            				@Override
 	            	        		            public void run() {
 	        	        		                	int secs = getConfig().getInt("config.starting_cooldown");
 	                            					if(secs_.containsKey(arena)){
 	                            						secs = secs_.get(arena);
 	                            					}else{
 	                            						secs_.put(arena, secs - 1);
 	                            						secs_updater.put(arena, p);
 	                            					}
 	                            					if(secs_updater.containsValue(p)){
 	                            						secs_.put(arena, secs - 1);
 		                            					for(Player p : arenap.keySet()){
 	            	        		                		p.sendMessage("2Starting in " + Integer.toString(secs));
 	            	        		                	}
 	                            					}
 	            	        		                
 	            	        		                
 	            	        		                
 	            	        		                if(secs < 1){
 	            	        		                	gamestarted.put(arena, true);
 	            	        		                	
 	            	        		                	secs_.remove(arena);
 	            	        		                	secs_updater.remove(arena);
 	            	        		                	
 	            	        		                	getServer().getScheduler().cancelTask(canceltask.get(p));
 	            	        		                	canceltask.remove(p);
 	            	        		                	s.setLine(2, "4Ingame");
 	            	        		                	int count = 0;
 	            	        		                	for(Player p : arenap.keySet()){
 	            	        		                		if(arenap.get(p).equalsIgnoreCase(arena)){
 	            	        		                			count += 1;
 	            	        		                			p.playSound(p.getLocation(), Sound.CAT_MEOW, 1, 0);
 	            	        		                		}
 	            	        		                	}
 	            	        		                	s.setLine(3, Integer.toString(count)  + "/" + getConfig().getString("config.maxplayers"));
 	            	        		                	s.update();
 	            	        		                }
 	                        					
 	                        					}
 	            	        	            }, 20, 20);
 	                            			canceltask.put(event.getPlayer(), id);
 	                            		}
 	                        		}
 	                        		
 	                        	}else{
 	                        		cont1 = false;
 	                        	}
 	                        }
 	                        
 	                        hp.put(event.getPlayer(), getConfig().getInt("config.boatlifes"));
 	            			if(cont1){
 	                			arenap.put(event.getPlayer(), arena);
 	                			hp.put(event.getPlayer(), getConfig().getInt("config.boatlifes"));
 	                			php.put(event.getPlayer(), getConfig().getInt("config.playerlifes"));
 	                			//event.getPlayer().setExp(0.99F);
 	                    		//spawn the player and give him boatballs + boat
 	                            //Location t = new Location(event.getPlayer().getWorld(), getConfig().getDouble(arena + ".spawn2.x"), getConfig().getDouble(arena + ".spawn2.y"), getConfig().getDouble(arena + ".spawn2.z"));
 	                			String count = Integer.toString(arenaspawn.get(arena));
 	    	                	//getLogger().info("this " + count + " " + Integer.toString(arenaspawn.size()));
 	    	                	final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn" + count + ".world")), getConfig().getDouble(arena + ".spawn" + count + ".x"), getConfig().getDouble(arena + ".spawn" + count + ".y"), getConfig().getDouble(arena + ".spawn" + count + ".z"));
 	    	                	event.getPlayer().teleport(t);
 	                            
 	                            ItemStack selectwand = new ItemStack(Material.SNOW_BALL, 64);
 	                            ItemMeta meta = (ItemMeta) selectwand.getItemMeta();
 	                            meta.setDisplayName(getConfig().getString("strings.ball_name"));
 	                            selectwand.setItemMeta(meta);
 	                            pinv.put(p, p.getInventory().getContents());
 	            				p.updateInventory();
 	            				
 	            				p.getInventory().clear();
 	            				for(int i_ = 0; i_ < getConfig().getInt("config.snowballstacks_amount"); i_++){
 	            					event.getPlayer().getInventory().addItem(selectwand);	
 	            				}
 	                            event.getPlayer().updateInventory();
 	                            
 	                            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 	 		                		public void run(){
 	 		                			b = p.getWorld().spawn(t, Boat.class);
 	 		                			b.setPassenger(p);
 	 		                		}
 	 		                	}, 10);
 	                            //b.setWorkOnLand(true);
 	                            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 	                            	public void run(){
 	                            		//final Boat b = p.getWorld().spawn(t, Boat.class);
 	                            		b.setPassenger(p);
 	                            	}
 	                            }, 20);
 	                            //b.setMaxSpeed(getConfig().getDouble("config.boatspeed_arena"));
 	                            
 	                            //lock_spawn.put(p, 2);
 	                            if(getConfig().getBoolean("config.teams")){
 	                            	if(whichteam){
 	                            		team.put(p, 1);
 	                            		p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_BLOCK, 1));
 	                            		p.updateInventory();
 	                            		getLogger().info(p.getName() + " is in team 1.");
 	                            		whichteam = false;
 	                            	}else if(!whichteam){
 	                            		team.put(p, 2);
 	                            		p.getInventory().setHelmet(new ItemStack(Material.GOLD_BLOCK, 1));
 	                            		p.updateInventory();
 	                            		getLogger().info(p.getName() + " is in team 2.");
 	                            		whichteam = true;
 	                            	}
 	                            }
 	                            
 	            			}
 	            			
 	                	}else{
 	                		final String arena = s.getLine(1);
 			                //Auto fix: If player rightclicks on a screwed boatgame sign, it "repairs" itself.
 		        			//getLogger().info("ARENAP COUNT: " + Integer.toString(arenap.values().size()));
 		                	// no players in given arena anymore -> update sign
 		        	    	if(!arenap.values().contains(arena)){
 		        	    		s.setLine(2, "2Join");
 		        	    		s.setLine(3, "0/" + Integer.toString(getConfig().getInt("config.maxplayers")));
 		        	    		s.update();
 		        	    	}
 	                	}// end of if Join or Starting on sign
 	                	
 	                	
 	                		
 	                }else if(s.getLine(0).equalsIgnoreCase("2[boat-ammo]")){ //end of if s.getline .. [BOAT]
 	                	final Player p = event.getPlayer();
 	                	if(arenap.containsKey(p)){
 	                		if(!usedammo.containsKey(p)){
 	                			usedammo.put(p, getConfig().getInt("config.ammo_sign_usage_count"));
 	                		}
 	                		if(usedammo.get(p) > 0){
 		                		int count = 0;
 		                		count = Integer.parseInt(s.getLine(1));
 		                		p.updateInventory();
 		                		p.getInventory().addItem(new ItemStack(Material.SNOW_BALL, count));
 		                		p.updateInventory();
 		                		p.sendMessage("3You just got " + s.getLine(1) + " boatballs!");
 		                		int newcount = usedammo.get(p) - 1;
 		                		usedammo.put(p, newcount);
 		                		return;	
 	                		}else{
 	                			p.sendMessage("4You already used that ammo sign!");
 	                			return;
 	                		}
 	                	}
 	                }
 	              
 	            }
 	        }
 	    }
 	}
     
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onVehicleDestroy(VehicleDestroyEvent event){
     	if(arenap.size() > 0 && event.getVehicle() instanceof Boat){
     		if(arenap.containsKey(event.getVehicle().getPassenger()) && event.getVehicle().getPassenger() != null){
 	    		event.getVehicle().setVelocity(new Vector(0,0,0));
 	            event.setCancelled(true);
     		}
     	}
     }
     
     
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onVehicleDamage(VehicleDamageEvent event){
     	if(arenap.size() > 0 && event.getVehicle() instanceof Boat && event.getVehicle().getPassenger() != null){
     			event.getVehicle().setVelocity(new Vector(0,0,0));
                 event.setCancelled(true);
         		
         		Player th = (Player) event.getVehicle().getPassenger();
         		Player p = (Player) event.getAttacker();
         		
         		if(p != null && arenap.containsKey(p) && th != p && gamestarted.get(arenap.get(p))){
         			if(getConfig().getBoolean("config.teams")){
         				if(team.get(th) == team.get(p)){
         					// same team
         				}else{
         					Integer f = hp.get(th);
     		        		hp.put(th, f - 1);
     		        		//th.setExp(th.getExp() - (1 / getConfig().getInt("config.boatlifes")));
         				}
         			}else{
 	        			Integer f = hp.get(th);
 	        			if(f == null){
 	        				hp.put(th, 1);
 	        			}
 		        		hp.put(th, f - 1);
 		        		//th.setExp(th.getExp() - (1 / getConfig().getInt("config.boatlifes")));
         			}
 	        		
         		}else{
         			// the passenger shoots at himself..
         		}
         		
         		if(hp.get(th) < 1 && php.get(th) > 0){
         			hp.put(th, getConfig().getInt("config.boatlifes"));
         			int now = php.get(th);
         			php.put(th, now - 1);
         			th.sendMessage(getConfig().getString("strings.lostlife") + Integer.toString(now - 1));
         			
         			String arena = arenap.get(th);
             		String spawnstr = ".spawn" + Integer.toString(pspawn.get(th));
             		
             		if(now > 1){
 	            		final Location currentspawn = new Location(Bukkit.getWorld(getConfig().getString(arena + spawnstr + ".world")), getConfig().getDouble(arena + spawnstr + ".x"), getConfig().getDouble(arena + spawnstr + ".y"), getConfig().getDouble(arena + spawnstr + ".z"));
 	        			final Player th_ = th;
 	        			final Boat v = (Boat) th_.getVehicle();
 		    			v.remove();
 		    			p.teleport(currentspawn);
 		    			//v.teleport(t2);
 		    			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 		    				public void run(){
 		    					//p.getVehicle().teleport(t2);
 		    	    			//p.teleport(t2);
 		    					Boat b = th_.getWorld().spawn(currentspawn, Boat.class);
 		                        b.setPassenger(th_);
 		    				}
 		    			}, 10);
             		}
         		}
         		
         		
         		if(hp.get(th) < 1 && php.get(th) < 1){ // boatlifes 0, playerlifes 0
     	    		event.getVehicle().setVelocity(new Vector(0,0,0));
     	            event.setCancelled(true);
     	            
     	            int count = 20;
     		    	
     		    	if(p != null && arenap.containsKey((Player) event.getAttacker())){
     		    		if(p != th){
     		    			if(getConfig().getBoolean("config.announce_winners")){
     		    				getServer().broadcastMessage("3" + p.getName() + " won a sea battle!");
     		    			}
     		    			p.sendMessage(getConfig().getString("strings.won"));
     		    			
     		    			if(p.isInsideVehicle()){
     		    				p.getVehicle().remove();
     		    			}
         			    	
         			    	if(getConfig().getBoolean("config.teams")){
 	        			    	p.getInventory().setHelmet(null);
 	        			    	p.updateInventory();	
         			    	}
 
         			    	if(economy){
     	    			    	EconomyResponse r = econ.depositPlayer(p.getName(), getConfig().getDouble("config.entry_money") * arenap.size());
     	            			if(!r.transactionSuccess()) {
     	                        	p.sendMessage(String.format("An error occured: %s", r.errorMessage));
     	                            //sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
     	                        }
         			    	}
         			    	
         			    	String arena = arenap.get(p);
         			    	arenap.remove(p);
         			    	hp.remove(p);
         			    	gamestarted.put(arena, false);
         			    	arenaspawn.remove(arena);
         			    	
         			    	ArrayList<Player> otherp = new ArrayList<Player>(getKeysByValue(arenap, arena));
         			    	
         			    	Double x = getConfig().getDouble(arena + ".lobbyspawn.x");
         			    	Double y = getConfig().getDouble(arena + ".lobbyspawn.y");
         			    	Double z = getConfig().getDouble(arena + ".lobbyspawn.z");
         			    	
         			    	Location b_ = new Location(Bukkit.getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getDouble(arena + ".sign.x"),getConfig().getDouble(arena + ".sign.y"), getConfig().getDouble(arena + ".sign.z"));
         			    	//getLogger().info(b_.toString());
         			    	BlockState bs = b_.getBlock().getState();
         			    	Sign s_ = null;
         			    	if(bs instanceof Sign){
         			    		s_ = (Sign)bs;
         			    	}else{
         			    		getLogger().info(bs.getBlock().toString());
         			    	}
     				    	// update sign:
                             if(s_ != null && s_.getLine(3) != ""){
                             	String d = s_.getLine(3).split("/")[0];
                             	//getLogger().info(d);
                             	int bef = Integer.parseInt(d);
                             	if(bef > 0){
                             		s_.setLine(3, Integer.toString(bef - 1) + "/" + getConfig().getString("config.maxplayers"));
                             		s_.update();
                             	}
                             	s_.setLine(2, "2Join");
                             	s_.update();
                             }
         			    	
         			    	//getLogger().info(Integer.toString(otherp.size()));
         			    	for(Player otp : otherp){ 
         			    		if(team.get(otp) == team.get(p) && getConfig().getBoolean("config.teams")){
         			    			otp.sendMessage(getConfig().getString("strings.won"));
         			    			if(economy){
             	    			    	EconomyResponse r = econ.depositPlayer(otp.getName(), getConfig().getDouble("config.entry_money") * arenap.size());
             	            			if(!r.transactionSuccess()) {
             	            				otp.sendMessage(String.format("An error occured: %s", r.errorMessage));
             	                            //sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
             	                        }
                 			    	}
         			    		}else{
         			    			otp.sendMessage(getConfig().getString("strings.lost"));
         			    		}
         			    		if(otp.isInsideVehicle()){
         			    			otp.getVehicle().remove();	
         			    		}
         			    		if(getConfig().getBoolean("config.teams")){
         			    			otp.getInventory().setHelmet(null);
         			    			otp.updateInventory();
             			    	}
         			    		
         				    	World w2 = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
         				    	
         				    	Location t2 = new Location(w2, x, y, z);
         				    	//otp.teleport(t2);
         				        BukkitTask task1 = new futask(otp, t2, false, getConfig().getInt("config.snowballstacks_amount")).runTaskLater(this, count);
         				        count += 20;
         				        
         				        otp.updateInventory();
         				        for(int i_ = 0; i_ < getConfig().getInt("config.config.snowballstacks_amount") + 1; i_++){
         		    				otp.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 64));	
         		    			}
         				        otp.getInventory().setContents(pinv.get(otp));
         				        otp.updateInventory();
         				        
 
         				        Location b = new Location(Bukkit.getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getDouble(arena + ".sign.x"),getConfig().getDouble(arena + ".sign.y"), getConfig().getDouble(arena + ".sign.z"));
         				        getLogger().info(b.toString());
         				        BlockState bs_ = b.getBlock().getState();
             			    	Sign s = null;
             			    	if(bs_ instanceof Sign){
             			    		s = (Sign)bs_;
             			    	}else{
             			    		getLogger().info(bs_.getBlock().toString());
             			    	}
         				    	// update sign:
                                 if(s != null && s.getLine(3) != ""){
                                 	String d = s.getLine(3).split("/")[0];
                                 	//getLogger().info(d);
                                 	int bef = Integer.parseInt(d);
                                 	if(bef > 0){
                                 		s.setLine(3, Integer.toString(bef - 1) + "/" + getConfig().getString("config.maxplayers"));
                                 		s.update();
                                 	}
                                 }
         			    	}
         			    	
         			    	World w = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
         			    	Location t = new Location(w, x, y, z);
         			    	//p.teleport(t);
         			    	p.getInventory().setContents(pinv.get(p));
         			    	if(economy){
         			    		BukkitTask task = new futask(p, t, true, getConfig().getInt("config.snowballstacks_amount")).runTaskLater(this, count);
         			    	}else{
         			    		BukkitTask task = new futask(p, t, true, getConfig().getIntegerList("config.itemreward_itemid"), getConfig().getInt("config.itemreward_amount"), getConfig().getInt("config.snowballstacks_amount")).runTaskLater(this, count);
         			    	}
         			    	
         			    	while (arenap.values().remove(arena));
         			    	//hp.clear(); //TODO
     		    		}
     		    	}
         		}
     	}else{
     		if(getConfig().getBoolean("config.invincibleboats") && event.getVehicle() instanceof Boat){
     			event.getVehicle().setVelocity(new Vector(0,0,0));
     	        event.setCancelled(true);
     		}else if(getConfig().getBoolean("config.alwaysdropboat") && event.getVehicle() instanceof Boat){
     			Boat boat = (Boat) event.getVehicle();
     			boat.getLocation().getWorld().dropItemNaturally(boat.getLocation(), new ItemStack(Material.BOAT, 1));
     			if(arenap.size() > 0){
     				boat.remove();
     			}
     		}
     	}
     }
     
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerLeave(PlayerQuitEvent event){
     	Player p2 = event.getPlayer();
     	if(arenap.containsKey(p2)){
     		String arena = arenap.get(p2);
     		//remove vehicle, remove snowballs, tp away
 	    	p2.getVehicle().remove();
 	    	
 	    	p2.updateInventory();
 	    	for(int i_ = 0; i_ < getConfig().getInt("config.config.snowballstacks_amount") + 1; i_++){
 				p2.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 64));	
 			}
 	    	p2.getInventory().setContents(pinv.get(p2));
 	    	p2.updateInventory();
 	    	
 	    	tpthem.put(p2.getName(), arenap.get(p2));
 	    	
 	    	
 	    	arenap.remove(p2);
 	    	
 	    	usedammo.put(p2, getConfig().getInt("config.ammo_sign_usage_count"));
 
 	    	Location b = new Location(Bukkit.getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getDouble(arena + ".sign.x"),getConfig().getDouble(arena + ".sign.y"), getConfig().getDouble(arena + ".sign.z"));
 	    	Sign s = (Sign)Bukkit.getWorld(getConfig().getString(arena + ".sign.world")).getBlockAt(b).getState();
 	    	// update sign: 
             if(s != null && s.getLine(3) != ""){
             	String d = s.getLine(3).split("/")[0];
             	getLogger().info(d);
             	Integer bef = Integer.parseInt(d);
             	if(bef > 1){
             		s.setLine(3, Integer.toString(bef - 1) + "/" + getConfig().getString("config.maxplayers"));
             		s.update();
             	}
             	if(getConfig().getBoolean("config.lastmanstanding")){
 	            	if(bef.equals(2)){ // 1 player left -> gets prize
 	            		s.setLine(3, Integer.toString(0) + "/" + getConfig().getString("config.maxplayers"));
 	            		s.setLine(2, "2Join");
 	            		s.update();
 	            		try{
 	            			getServer().getScheduler().cancelTask(canceltask.get(p2));
 	            		}catch(Exception e){
 	            			
 	            		}
 	            		gamestarted.put(arena, false);
 	            		secs_.remove(arena);
 	            		arenaspawn.remove(arena);
 	            		
 	            		Player last = this.getKeyByValue(arenap, arena);
 	            		
 	            		if(last != null){
 	                		
 	                		if(last != null){
 	                    		last.sendMessage("3You are the last man standing and got a prize! Leave with /sb leave.");
 	    	            		
 	                    		last.getVehicle().remove();
 	    				    	
 	                    		last.updateInventory();
 	                    		last.getInventory().setContents(pinv.get(last));
 	                    		last.updateInventory();
 	    				    	for(int i_ = 0; i_ < getConfig().getInt("config.config.snowballstacks_amount") + 1; i_++){
 	    				    		last.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 64));	
 	    		    			}
 	    				    	last.getInventory().setContents(pinv.get(last));
 	    				    	last.updateInventory();
 	    				    	
 	    				    	Double x = getConfig().getDouble(arena + ".lobbyspawn.x");
 	    				    	Double y = getConfig().getDouble(arena + ".lobbyspawn.y");
 	    				    	Double z = getConfig().getDouble(arena + ".lobbyspawn.z");
 	    			    		World w = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
 	    				    	
 	    				    	Location t_ = new Location(w, x, y, z);
 	    			    		
 	    			    		BukkitTask task_ = new futask(last, t_, false, getConfig().getInt("config.snowballstacks_amount")).runTaskLater(this, 40);
 	    				    	
 	    				    	arenap.remove(last);
 	                    		
 	    				    	s.setLine(2, "2Join");
 	                    		s.setLine(3, "0/" + getConfig().getString("config.maxplayers"));
 	                    		s.update();
 	                    		
 	                    		arenaspawn.remove(arena);
 	                    		try{
 	                    			getServer().getScheduler().cancelTask(canceltask.get(secs_updater.get(arena)));
 	                    		}catch(Exception e){
 	                    			try{
 	                        			getServer().getScheduler().cancelTask(canceltask.get(last));
 	                        		}catch(Exception e_){
 	                        		}
 	                    		}
 	                    		secs_.remove(arena);
 	    				    	
 	                    		if(economy){
 	    	            			EconomyResponse r = econ.depositPlayer(last.getName(), getConfig().getDouble("config.entry_money") * 2);
 	    	            			if(!r.transactionSuccess()) {
 	    	            				last.sendMessage(String.format("An error occured: %s", r.errorMessage));
 	    	                        }
 	    	            		}else{
 	    	            			List<Integer> itemid = getConfig().getIntegerList("config.itemreward_itemid");
 	    	            			int itemid_amount = getConfig().getInt("config.itemreward_amount");
 	    	            			last.updateInventory();
 	    		            		if(itemid.size() > 0){
 	    		            			for(int id : itemid){
 	    		            				last.getInventory().addItem(new ItemStack(Material.getMaterial(id), itemid_amount));
 	    		            				last.updateInventory();
 	    		            			}
 	    		                    }
 	    	            		}
 	                		}
 	            		}
 	            	}	
             	}
             	
             	if(bef < 2){
             		s.setLine(3, Integer.toString(0) + "/" + getConfig().getString("config.maxplayers"));
             		s.setLine(2, "2Join");
             		s.update();
             		try{
             			getServer().getScheduler().cancelTask(canceltask.get(p2));
             		}catch(Exception e){
             			
             		}
             		gamestarted.put(arena, false);
             		secs_.remove(arena);
             		arenaspawn.remove(arena);
             	}
             }
             
 	    }
     }
     
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event){
     	//getLogger().info(tpthem.keySet().toString());
     	if(tpthem.keySet().toString().contains(event.getPlayer().getName())){
     		//getLogger().info("tst");
     		String arena = tpthem.get(event.getPlayer().getName());
     		
     		Double x = getConfig().getDouble(arena + ".lobbyspawn.x");
 	    	Double y = getConfig().getDouble(arena + ".lobbyspawn.y");
 	    	Double z = getConfig().getDouble(arena + ".lobbyspawn.z");
     		World w = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
 	    	Location t = new Location(w, x, y, z);
     		
     		BukkitTask task = new futask(event.getPlayer(), t, false, getConfig().getInt("config.snowballstacks_amount")).runTaskLater(this, 60);
     		//getLogger().info("hoping to tp him away ;)");
     		tpthem.remove(event.getPlayer().getName());
     	}
     }
   
     
     public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
         for (Entry<T, E> entry : map.entrySet()) {
             if (value.equals(entry.getValue())) {
                 return entry.getKey();
             }
         }
         return null;
     }
     
     public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
         Set<T> keys = new HashSet<T>();
         for (Entry<T, E> entry : map.entrySet()) {
             if (value.equals(entry.getValue())) {
                 keys.add(entry.getKey());
             }
         }
         return keys;
    }
     
     @EventHandler
     public void onSignChange(SignChangeEvent event) {
         Player p = event.getPlayer();
         if(event.getLine(0).toLowerCase().contains("[boat]")){
         	if (event.getPlayer().hasPermission("boatgame.sign"))
             {
         		event.setLine(0, "2[Boat]");
         		String name = event.getLine(1);
         		if(getConfig().isSet(name + ".name")){
         			//save sign coords
         			getConfig().set(name + ".sign.world", event.getPlayer().getWorld().getName());
         			getConfig().set(name + ".sign.x", event.getBlock().getX());
         			getConfig().set(name + ".sign.y", event.getBlock().getY());
         			getConfig().set(name + ".sign.z", event.getBlock().getZ());
         			this.saveConfig();
         			event.setLine(2, "2Join");
         			event.setLine(3, "0/" + getConfig().getString("config.maxplayers"));
         			p.sendMessage("2Boatarena successfully created!");
         			
         			gamestarted.put(name, false);
         		}else{
         			p.sendMessage("4This Boatarena was not found. Use /sb createarena [name] to create one.");
         			event.getBlock().breakNaturally();
         		}
             }else{
             	event.setLine(0, "INVALID");
             	p.sendMessage("4You don't have permission to create arenas.");
             }
         }else if(event.getLine(0).toLowerCase().contains("[boat-ammo]")){
         	event.setLine(0, "2[boat-ammo]");
         	if(event.getLine(1).equalsIgnoreCase("") || event.getLine(1) == null){
         		event.getBlock().breakNaturally();
         		event.getPlayer().sendMessage("4You need to provide the number of snowballs to be added, when a player rightclicks the sign.");
         	}
         }
     }
 
     @EventHandler
     public void onblockbreak(BlockBreakEvent event) {
     	if (event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN)
         {
     		//event.setCancelled(true);
             Sign s = (Sign) event.getBlock().getState();
             if(s.getLine(0).equalsIgnoreCase("2[Boat]") && s.getLine(1) != ""){
            	event.setCancelled(true);
	            String name = s.getLine(1);
	            getConfig().set(name + ".sign", null);
	            event.getPlayer().sendMessage("2Don't forget to set a new sign for the boatarena!");
	            event.getBlock().breakNaturally();
             }
         }else{
         	//if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
         		//event.getBlock().breakNaturally();
         	//}
         }
     }
 
     
 	@EventHandler
 	public void onProjectileThrownEvent(ProjectileLaunchEvent event) {
 		if (event.getEntity() instanceof Snowball) {
 			if(event.getEntity().getShooter() instanceof Player){
 				Player p = (Player)event.getEntity().getShooter();
 				if(p != null){
 					if(arenap.containsKey(p)){
 						if(!gamestarted.get(arenap.get(p))){
 							event.setCancelled(true);
 							//p.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 1));
 							//p.updateInventory();
 						}else{
 							p.updateInventory();
 							boolean cont = true;
 							int count = 0;
 							for(ItemStack item : p.getInventory().getContents())
 							{
 							    if(item != null){
 							    	count += 1;
 							    	cont = false;
 							    }
 							}
 							if(count < 2){
 								for(ItemStack item : p.getInventory().getContents())
 								{
 								    if(item != null){
 								    	if(item.getAmount() < 2){
 								    		cont = true;
 								    	}
 								    }
 								}
 							}
 							if(cont){
 								// player doesn't have any snowballs any more -> lost
 								p.sendMessage("3You've lost the boatgame, all your snowballs are spent. :(");
 								
 								String arena = arenap.get(p);
 								
 								Double x = getConfig().getDouble(arena + ".lobbyspawn.x");
 	        			    	Double y = getConfig().getDouble(arena + ".lobbyspawn.y");
 	        			    	Double z = getConfig().getDouble(arena + ".lobbyspawn.z");
 								
 	        			    	if(p.isInsideVehicle()){
 	        			    		p.getVehicle().remove();
 	        			    	}
 								
 	    			    		if(getConfig().getBoolean("config.teams")){
 	    			    			p.getInventory().setHelmet(null);
 	    			    			p.updateInventory();
 	        			    	}
 	    			    		
 	    				    	World w2 = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
 	    				    	
 	    				    	Location t2 = new Location(w2, x, y, z);
 	    				    	//otp.teleport(t2);
 	    				        BukkitTask task1 = new futask(p, t2, false, getConfig().getInt("config.snowballstacks_amount")).runTaskLater(this, 20);
 	    				        
 	    				        p.updateInventory();
 	    				        for(int i_ = 0; i_ < getConfig().getInt("config.config.snowballstacks_amount") + 1; i_++){
 	    		    				p.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 64));	
 	    		    			}
 	    				        p.updateInventory();
 	    				        
 	    				        final Player p_ = p;
 	    				        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 	    		    				public void run(){
 	    		    					p_.getInventory().setContents(pinv.get(p_));
 	    	    				        p_.updateInventory();
 	    		    				}
 	    		    			}, 10);
 	    				        
 	
 	    				        Location b = new Location(Bukkit.getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getDouble(arena + ".sign.x"),getConfig().getDouble(arena + ".sign.y"), getConfig().getDouble(arena + ".sign.z"));
 	    				        getLogger().info(b.toString());
 	    				        BlockState bs_ = b.getBlock().getState();
 	        			    	Sign s = null;
 	        			    	if(bs_ instanceof Sign){
 	        			    		s = (Sign)bs_;
 	        			    	}else{
 	        			    		getLogger().info(bs_.getBlock().toString());
 	        			    	}
 	    				    	// update sign:
 	                            if(s != null && s.getLine(3) != ""){
 	                            	String d = s.getLine(3).split("/")[0];
 	                            	//getLogger().info(d);
 	                            	int bef = Integer.parseInt(d);
 	                            	if(bef > 0){
 	                            		s.setLine(2, "2Join");
 	                            		s.setLine(3, Integer.toString(bef - 1) + "/" + getConfig().getString("config.maxplayers"));
 	                            		s.update();
 	                            	}
 	                            }
 	                            
 	                            arenaspawn.remove(arena);
 	                            arenap.remove(p);
 	                            gamestarted.put(arena, false);
 							}
 						}
 					}
 				}	
 			}
 		}
 	}
     
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event) {
     	final Player p = event.getPlayer();
     	if(p != null && arenap.containsKey(p) && !gamestarted.get(arenap.get(p))){
     	//if(p != null && arenap.containsKey(p)){
     		if(!gamestarted.get(arenap.get(p))){
     			
 	    		String arena = arenap.get(p);
 	    		String spawnstr = ".spawn" + Integer.toString(pspawn.get(p));
 	    		
 	    		
 	    		Location currentspawn = new Location(Bukkit.getWorld(getConfig().getString(arena + spawnstr + ".world")), getConfig().getDouble(arena + spawnstr + ".x"), getConfig().getDouble(arena + spawnstr + ".y"), getConfig().getDouble(arena + spawnstr + ".z"));
 	    		
 	    		Location t1 = p.getLocation();
 	    		final Location t2 = currentspawn;
 	    		
 	    		/*if(!p.isInsideVehicle()){
 	    			final Boat b = event.getPlayer().getWorld().spawn(t2, Boat.class);
 	                b.setWorkOnLand(true);
 	                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 	                	public void run(){
 	                		b.setPassenger(p);
 	                	}
 	                }, 20);
 	    		}*/
 	    		
 	    		//getLogger().info(t1.toString() + " " + t2.toString());
 	    		
 	    		if(t1.getX() - t2.getX() > 2 || t1.getX() - t2.getX() < -2  || t1.getZ() - t2.getZ() > 2 || t1.getZ() - t2.getZ() < -2){
 		    		//getLogger().info("true");
 	    			//event.setCancelled(true);
 		    		//event.getPlayer().setVelocity(new Vector(0, 0, 0));
 	    			//event.getPlayer().teleport(t2);
 		    		if(p.isInsideVehicle()){
 			    		final Boat v = (Boat) p.getVehicle();
 		    			v.remove();
 		    			p.teleport(t2);
 		    			//v.teleport(t2);
 		    			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 		    				public void run(){
 		    					//p.getVehicle().teleport(t2);
 		    	    			//p.teleport(t2);
 		    					Boat b = p.getWorld().spawn(t2, Boat.class);
 		                        b.setPassenger(p);
 		    				}
 		    			}, 10);
 		    		}else{
 		    			/*final Boat b = event.getPlayer().getWorld().spawn(t2, Boat.class);
 		                b.setWorkOnLand(true);
 		                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 		                	public void run(){
 		                		b.setPassenger(p);
 		                	}
 		                }, 20);*/
 		    		}
 	    		}	
     		}else{
     			// game started and player is not in vehicle -> seabattle doesn't work for this server.
 	    		if(!p.isInsideVehicle()){
 	    			getLogger().severe("SeaBattle can't spawn boats! Possible Causes: Wrong bukkit build or other plugin preventing spawning.");
 	    			String arena = arenap.get(p);
 		    		String spawnstr = ".spawn" + Integer.toString(pspawn.get(p));
 		    		
 		    		Location currentspawn = new Location(Bukkit.getWorld(getConfig().getString(arena + spawnstr + ".world")), getConfig().getDouble(arena + spawnstr + ".x"), getConfig().getDouble(arena + spawnstr + ".y"), getConfig().getDouble(arena + spawnstr + ".z"));
 		    		
 	    			final Boat b = event.getPlayer().getWorld().spawn(currentspawn, Boat.class);
 	                b.setWorkOnLand(true);
 	                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 	                	public void run(){
 	                		b.setPassenger(p);
 	                	}
 	                }, 20);
 	    		}
     		}
     		
     		
     		
     	}
     	
     	/*if(p.isInsideVehicle()){
 	    	if (p.getVehicle() instanceof Boat){
 				Boat b = (Boat) p.getVehicle();
 				Float speed = (float) getConfig().getDouble("config.boatspeed_global");
 				Vector v = b.getVelocity();
 				//v.multiply(speed.floatValue());
 				v.multiply(new Vector(speed.floatValue(), 1.0F, speed.floatValue()));
 				b.setVelocity(v);
 				
 			}
     	}*/
 		
     }
     
     /*@EventHandler
     public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
     	if(event.isSneaking() && event.getPlayer().isInsideVehicle()){
     		if(event.getPlayer().getVehicle() instanceof Boat){
     			event.getPlayer().getVehicle().eject();
     		}
     	}
     }*/
     
     
     @EventHandler
     public void onVehicleCreate(VehicleCreateEvent event){
 		if(event.getVehicle() instanceof Boat){
 			Boat v = (Boat) event.getVehicle();
 			v.setMaxSpeed(getConfig().getDouble("config.boatspeed_global"));
 		}
 	}
  
     
     @EventHandler
     public void onInventoryClick(InventoryClickEvent event) {
         Player p = (Player)event.getWhoClicked();
         if(p != null && arenap.containsKey(p)){
         	event.setCancelled(true);
         }
     }
     
     
     @EventHandler
 	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
 		if(arenap.containsKey(event.getPlayer())){
 			if(event.getMessage().equalsIgnoreCase("/sb leave") || event.getMessage().equalsIgnoreCase("/seabattle leave")){
 				// nothing
 			}else{
 				event.setCancelled(true);
 				event.getPlayer().sendMessage("3You're in a SeaBattle game. Please use /sb leave to leave the minigame.");
 			}
 		}
 	}
     
     
     
 	public boolean validArena(String arena) {
 		if (getConfig().isSet(arena + ".sign")
 				&& getConfig().isSet(arena + ".world")
 				&& getConfig().isSet(arena + ".lobbyspawn")
 				&& getConfig().isSet(arena + ".spawn1")) {
 			return true;
 		}
 		return false;
 	}
 
 }
