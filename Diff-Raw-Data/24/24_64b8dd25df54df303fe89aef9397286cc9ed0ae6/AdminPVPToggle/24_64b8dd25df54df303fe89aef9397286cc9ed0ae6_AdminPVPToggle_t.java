 package com.nuclearw.adminpvptoggle;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 import javax.persistence.PersistenceException;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AdminPVPToggle extends JavaPlugin implements Listener {
 	@Override
 	public void onEnable() {
 		// Register listeners
 		getServer().getPluginManager().registerEvents(this, this);
 
 		// Database
 		initDatabase();
 
 		// CleanupTask
 		RemoveFromDisallowTask task = new RemoveFromDisallowTask(this);
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, task, 1L, 12000L);
 
 		// Hi ma
 		getLogger().info("Finished Loading " + getDescription().getFullName());
 	}
 
 	@Override
 	public void onDisable() {
 		getLogger().info("Finished Unloading "+getDescription().getFullName());
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(label.equalsIgnoreCase("enablepvp")) {
 			// Permissions!
 			if(sender instanceof Player) {
 				if(!sender.hasPermission("adminpvptoggle.enablepvp")) {
 					sender.sendMessage("You do not have permission to perform this command.");
 				}
 			}
 
 			// Args length check
 			if(args.length != 1) return false;
 
 			String targetName = args[0];
 
 			// Check to get a player name from a partial match
 			Player target = getServer().getPlayer(args[0]);
 			if(target != null) targetName = target.getName();
 
 			// Do it
 			removePlayer(targetName);
 
 			sender.sendMessage("Removed PVP disable on "+targetName);
 			getLogger().info(sender.getName() + " removed PVP disable on " + targetName);
 
 			return true;
 		}
 
 		if(label.equalsIgnoreCase("disablepvp")) {
 			// Permissions!
 			if(sender instanceof Player) {
 				if(!sender.hasPermission("adminpvptoggle.disablepvp")) {
 					sender.sendMessage("You do not have permission to perform this command.");
 				}
 			}
 
 			/* Args length check
 			 * Valid forms:
 			 * /disablepvp user					1
 			 * /disablepvp user 1 m				3
 			 * /disablepvp user 1 m 2 d			5
 			 * /disablepvp user 1 m 2 d 3 w		7
 			 * 
 			 * Invalid forms:
 			 * /disablepvp						0
 			 * /disablepvp user 1				2
 			 * /disablepvp user 1 m 2			4
 			 * /disablepvp user 1 m 2 d 3		6
 			 */
 			if(args.length < 1 || args.length > 7 || args.length % 2 == 0) return false;
 
 			String targetName = args[0];
 
 			// Check to get a player name from a partial match
 			Player target = getServer().getPlayer(args[0]);
 			if(target != null) targetName = target.getName();
 
 			// Time till expiration
 			long time = 0;
 			if(args.length > 1) time = System.currentTimeMillis();
 
 			int count = args.length;
 			
 			while(count > 1) {
 				try {
 					int timeMultiplier = Integer.valueOf(args[count-2]);
 					int timeModifier = getTimeModifier(args[count-1]);
 
 					// Check for valid time modifier
 					if(timeModifier == 0) {
 						sender.sendMessage(args[count-1] + " is an invalid time modifier!");
 						return false;
 					}
 
 					time += timeMultiplier * timeModifier;
 				} catch(NumberFormatException ex) {
 					return false;
 				}
 				count -= 2;
 			}
 
 			if(time == 0) {
 				if(!sender.hasPermission("adminpvptoggle.disablepvp.permanent")) {
 					sender.sendMessage("You do not have permission to perform this command.");
 				}
 			}
 
 			// Do it
 			addPlayer(targetName, time);
 
 			String until = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(time));
 			if(time == 0) {		// Lets be a little silly :I
 				switch (new Random().nextInt(4)) {
 					case 0:
 						until = "The heat death of the universe";
 						break;
 					case 1:
 						until = "The time after time";
 						break;
 					case 2:
 						until = "The cows come home";
 						break;
 					case 3:
 						until = "The copyright on Micky Mouse expires";
 						break;
 				}
 			}
 			sender.sendMessage("Disabled PVP on " + targetName + " until " + until);
 			getLogger().info(sender.getName() + " set PVP disable on " + targetName + " until " + until);
 
 			return true;
 		}
 
 		return true;
 	}
 
 	@Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(PVPPlayer.class);
         return list;
     }
 
	@EventHandler(ignoreCancelled = true)
 	public void onEntityDamage(EntityDamageEvent event) {
 		if(!(event instanceof EntityDamageByEntityEvent)) return;
 		EntityDamageByEntityEvent dEvent = (EntityDamageByEntityEvent) event;
		if(!(dEvent.getDamager() instanceof Player || dEvent.getDamager() instanceof Projectile)) return;
 		if(!(dEvent.getEntity() instanceof Player)) return;
 
		Player attacker;
		if(dEvent.getDamager() instanceof Projectile) {
			Projectile projectile = (Projectile) dEvent.getDamager();
			LivingEntity shooter = projectile.getShooter();
			if(!(shooter instanceof Player)) return;

			attacker = (Player) shooter;
		} else {
			attacker = (Player) dEvent.getDamager();
		}

 		Player victim = (Player) dEvent.getEntity();
 
 		if(!canPVP(attacker.getName()) || !canPVP(victim.getName())) {
 			event.setCancelled(true);
 			return;
 		}
 	}
 
 	private void removePlayer(String playerName) {
 		List<PVPPlayer> found = getDatabase().find(PVPPlayer.class).where().ieq("name", playerName).findList();
 		if(found != null) getDatabase().delete(found);
 	}
 
 	private void addPlayer(String playerName, long timeExpire) {
 		PVPPlayer add = new PVPPlayer();
 		add.setName(playerName);
 		add.setTimeExpire(timeExpire);
 
 		getDatabase().save(add);
 	}
 
 	private boolean canPVP(String playerName) {
 		int rowCount = getDatabase().find(PVPPlayer.class).where().ieq("name", playerName).findRowCount();
 		if(rowCount > 0) return false;
 		return true;
 	}
 
 	private int getTimeModifier(String modifier) {
 		if(modifier.equalsIgnoreCase("m")) {
 			return 60000;			// 60 * 1000
 		} else if(modifier.equalsIgnoreCase("d")) {
 			return 86400000;		// 60 * 60 * 24 * 1000
 		} else if(modifier.equalsIgnoreCase("w")) {
 			return 604800000;		// 60 * 60 * 24 * 7 * 1000
 		} else {
 			return 0;
 		}
 	}
 
 	private void initDatabase() {
         try {
             getDatabase().find(PVPPlayer.class).findRowCount();
         } catch (PersistenceException ex) {
             getLogger().info("Initializing database");
             this.installDDL();
         }
 	}
 }
