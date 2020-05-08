 package com.amoebaman.combatmods;
 
 import java.io.File;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.event.vehicle.VehicleExitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginLogger;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CombatMods extends JavaPlugin implements Listener{
 
 	private PluginLogger log;
 	private ConfigurationSection parrying, headshots, lunging, armoredBoats, fastArrows, arrowRetrieval, antispamBows, brokenKnees;
 	private File configFile;
 	
 	public void onEnable(){
 		log = new PluginLogger(this);
 		getDataFolder().mkdirs();
 		configFile = new File(getDataFolder().getPath() + "/config.yml");
 		try{ loadConfig(); }
 		catch(Exception e){ e.printStackTrace(); }
 		Bukkit.getPluginManager().registerEvents(this, this);
 	}
 	
 	public void loadConfig() throws Exception{
 		try{
 			if(!configFile.exists()){
 				configFile.createNewFile();
 			}
 			getConfig().options().copyDefaults(true);
 			getConfig().load(configFile);
 			getConfig().save(configFile);
 			parrying = getConfig().getConfigurationSection("parrying");
 			headshots = getConfig().getConfigurationSection("headshots");
 			lunging = getConfig().getConfigurationSection("lunging");
 			armoredBoats= getConfig().getConfigurationSection("armored-boats");
 			fastArrows = getConfig().getConfigurationSection("fast-arrows");
 			arrowRetrieval = getConfig().getConfigurationSection("arrow-retrieval");
 			antispamBows = getConfig().getConfigurationSection("antispam-bows");
 			brokenKnees = getConfig().getConfigurationSection("broken-knees");
 		}
 		catch(Exception e){
 			e.printStackTrace();
 			throw new Exception();
 		}
 		for(String component : getConfig().getKeys(false))
 			log.fine(component + " " + (getConfig().getConfigurationSection(component).getBoolean("enabled") ? "enabled" : "disabled"));
 	}
 
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
 		if(command.getName().equals("cmreload")){
 			try{
 				loadConfig();
 				sender.sendMessage(ChatColor.GREEN + "CombatMods config reloaded");
 			}
 			catch(Exception e){
 				sender.sendMessage(ChatColor.RED + "Error reloading config");
 				return true;
 			}
 		}
 		return true;
 	}
 	
 	private HashMap<String, Long> lastParryAttempt = new HashMap<String, Long>();
 	@EventHandler
 	public void detectParrying(PlayerInteractEvent event){
 		if(!parrying.getBoolean("enabled"))
 			return;
 		final Player player = event.getPlayer();
 		if(player.getItemInHand() == null)
 			return;
 		Material inHand = player.getItemInHand().getType();
 		if(inHand.name().contains("SWORD") && event.getAction().name().contains("RIGHT_CLICK")){
 			if(!lastParryAttempt.containsKey(player.getName()))
 				lastParryAttempt.put(player.getName(), 0L);
 			if(System.currentTimeMillis() - lastParryAttempt.get(player.getName()) < parrying.getInt("fumble-time") && Math.random() < parrying.getDouble("fumble-chance"))
 				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){ public void run(){
 					player.getWorld().dropItemNaturally(player.getLocation(), player.getItemInHand());
 					player.getInventory().remove(player.getItemInHand());
 					player.sendMessage(ChatColor.translateAlternateColorCodes('&', parrying.getString("fumble-message")));
 				}});
 			else
 				lastParryAttempt.put(player.getName(), System.currentTimeMillis());
 		}
 	}
 	
 	@EventHandler
 	public void skillfulParrying(EntityDamageEvent event){
 		if(!parrying.getBoolean("enabled"))
 			return;
 		if(!(event.getEntity() instanceof Player))
 			return;
 		Player player = (Player) event.getEntity();
 		if(player.isBlocking() && (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.PROJECTILE)){
 			if(!lastParryAttempt.containsKey(player.getName()))
 				lastParryAttempt.put(player.getName(), 0L);
 			if(System.currentTimeMillis() - lastParryAttempt.get(player.getName()) < parrying.getInt("parry-time") && Math.random() < parrying.getDouble("parry-chance")){
 				event.setCancelled(true);
 				player.getWorld().playEffect(player.getLocation(), Effect.ZOMBIE_CHEW_IRON_DOOR, 0);
 				player.sendMessage(ChatColor.translateAlternateColorCodes('&', parrying.getString("parry-message")));
 			}
 			else if(System.currentTimeMillis() - lastParryAttempt.get(player.getName()) > parrying.getInt("disarm-time") && Math.random() < parrying.getDouble("disarm-chance")){
 				player.getWorld().dropItemNaturally(player.getLocation(), player.getItemInHand());
 				player.getInventory().remove(player.getItemInHand());
 				player.sendMessage(ChatColor.translateAlternateColorCodes('&', parrying.getString("disarm-message")));
 			}
 		}
 	}
 	
 	@EventHandler
 	public void headshotBonus(EntityDamageEvent event){
 		if(!headshots.getBoolean("enabled"))
 			return;
 		if(!(event.getEntity() instanceof Player))
 			return;
 		Player player = (Player) event.getEntity();		
 		if(!(event instanceof EntityDamageByEntityEvent))
 			return;
 		EntityDamageByEntityEvent eEvent = (EntityDamageByEntityEvent) event;
 		if(eEvent.getDamager() instanceof Projectile){
 			Projectile proj = (Projectile) eEvent.getDamager();
 			if(proj.getLocation().distance(player.getEyeLocation()) <= 1.2){
 				event.setDamage((int) (event.getDamage() * headshots.getDouble("multiplier")));
 				if(proj.getShooter() instanceof Player)
 					((Player) proj.getShooter()).sendMessage(ChatColor.translateAlternateColorCodes('&', headshots.getString("dealt-message")));
 				player.sendMessage(ChatColor.translateAlternateColorCodes('&', headshots.getString("taken-message")));
 			}
 		}
 	}
 	
 	@EventHandler
 	public void lunging(PlayerMoveEvent event){
 		if(!lunging.getBoolean("enabled"))
 			return;
 		Player player = event.getPlayer();
 		if(player.isSprinting() && event.getFrom().getY() < event.getTo().getY() && event.getTo().getY() - event.getFrom().getY() != 0.5){
 			player.setSprinting(false);
 			player.sendMessage(ChatColor.translateAlternateColorCodes('&', lunging.getString("message")));
 		}
 	}
 	
 	@EventHandler
 	public void armoredBoats(VehicleDestroyEvent event){
 		if(!armoredBoats.getBoolean("enabled"))
 			return;
		if(event.getVehicle() instanceof Boat && !event.getAttacker().equals(event.getVehicle().getPassenger()))
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void cleanDeadBoats(PlayerDeathEvent event){
 		if(!armoredBoats.getBoolean("enabled"))
 			return;
 		if(event.getEntity().getVehicle() instanceof Boat && armoredBoats.getBoolean("prevent-boat-litter"))
 			event.getEntity().getVehicle().remove();
 	}
 	
 	@EventHandler
 	public void cleanVacatedBoats(VehicleExitEvent event){
 		if(!armoredBoats.getBoolean("enabled"))
 			return;
 		if(event.getVehicle() instanceof Boat && armoredBoats.getBoolean("prevent-boat-litter"))
 			event.getVehicle().remove();
 	}
 	
 	@EventHandler
 	public void fastArrows(ProjectileLaunchEvent event){
 		if(!fastArrows.getBoolean("enabled"))
 			return;
 		if(event.getEntity() instanceof Arrow){
 			Arrow arrow = (Arrow) event.getEntity();
 			arrow.setVelocity(arrow.getVelocity().multiply(fastArrows.getDouble("speed")));
 		}
 	}
 	
 	@EventHandler
 	public void fastArrowNerf(EntityDamageEvent event){
 		if(!(fastArrows.getBoolean("enabled") && fastArrows.getBoolean("disable-damage-increase")))
 			return;
 		if(!(event instanceof EntityDamageByEntityEvent))
 			return;
 		EntityDamageByEntityEvent eEvent = (EntityDamageByEntityEvent) event;
 		if(eEvent.getDamager() instanceof Arrow)
 			event.setDamage((int) (event.getDamage() / fastArrows.getDouble("speed"))); 
 	}
 	
 	private HashMap<LivingEntity, Integer> arrowsLodged = new HashMap<LivingEntity, Integer>();
 	@EventHandler
 	public void arrowRetrievalLogger(EntityDamageEvent event){
 		if(!arrowRetrieval.getBoolean("enabled"))
 			return;
 		if(!(event instanceof EntityDamageByEntityEvent))
 			return;
 		EntityDamageByEntityEvent realEvent = (EntityDamageByEntityEvent) event;
 		if(!(realEvent.getDamager() instanceof Arrow && event.getEntity() instanceof LivingEntity))
 			return;
 		Arrow arrow = (Arrow) realEvent.getDamager();
 		LivingEntity victim = (LivingEntity) event.getEntity();
 		if(!(arrow.getShooter() instanceof Player))
 			return;
 		Player shooter = (Player) arrow.getShooter();
 		if(!shooter.getItemInHand().getEnchantments().containsKey(Enchantment.ARROW_INFINITE)){
 			if(!arrowsLodged.containsKey(victim))
 				arrowsLodged.put(victim, 0);
 			if(Math.random() < arrowRetrieval.getDouble("retrieval-rate"))
 				arrowsLodged.put(victim, arrowsLodged.get(victim) + 1);
 		}
 	}
 	
 	@EventHandler
 	public void arrowRetrieval(EntityDeathEvent event){
 		if(!arrowRetrieval.getBoolean("enabled"))
 			return;
 		if(!(event.getEntity() instanceof LivingEntity))
 			return;
 		LivingEntity victim = (LivingEntity) event.getEntity();
 		if(arrowsLodged.containsKey(victim))
 			victim.getWorld().dropItemNaturally(victim.getLocation(), new ItemStack(Material.ARROW, arrowsLodged.get(victim)));
 		arrowsLodged.remove(victim);
 	}
 
 	private HashMap<String, Long> lastBowDraw = new HashMap<String, Long>();
 	@EventHandler
 	public void detectBowDraw(PlayerInteractEvent event){
 		if(!antispamBows.getBoolean("enabled"))
 			return;
 		final Player player = event.getPlayer();
 		if(player.getItemInHand() == null)
 			return;
 		Material inHand = player.getItemInHand().getType();
 		if(inHand == Material.BOW && event.getAction().name().contains("RIGHT_CLICK"))
 			lastBowDraw.put(player.getName(), System.currentTimeMillis());
 	}
 	
 	@EventHandler
 	public void antispamBows(ProjectileLaunchEvent event){
 		if(!antispamBows.getBoolean("enabled"))
 			return;
 		Player shooter = null;
 		if(event.getEntity().getShooter() instanceof Player)
 			shooter = (Player) event.getEntity().getShooter();
 		if(shooter == null)
 			return;
 		if(!lastBowDraw.containsKey(shooter.getName()))
 			lastBowDraw.put(shooter.getName(), 0L);
 		if(System.currentTimeMillis() - lastBowDraw.get(shooter.getName()) < antispamBows.getInt("minimum-draw")){
 			event.setCancelled(true);
			if(!shooter.getItemInHand().getEnchantments().containsKey(Enchantment.ARROW_INFINITE))
 				shooter.getWorld().dropItemNaturally(shooter.getLocation(), new ItemStack(Material.ARROW, 1));
 			shooter.sendMessage(ChatColor.translateAlternateColorCodes('&', antispamBows.getString("message")));
 		}
 	}
 	
 	@EventHandler
 	public void brokenKnees(EntityDamageEvent event){
 		if(!brokenKnees.getBoolean("enabled"))
 			return;
 		if(event.getCause() == DamageCause.FALL)
 			event.setDamage((int) (event.getDamage() * brokenKnees.getDouble("fall-multiplier")));
 	}
 	
 }
