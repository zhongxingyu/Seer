 package se.enji;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 public class MushPoof extends JavaPlugin implements Listener {
 	FileConfiguration config;
 	final double STILL = -0.0784000015258789;
 	boolean canJump = true;
 
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 		config = getConfig();
 		config.options().copyDefaults(true);
 		saveConfig();
 	}
	
 	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
 		if (!(sender instanceof Player)) return false;
 		Player p = (Player) sender;
 		int amount = args.length == 1 && isInt(args[0]) ? Integer.parseInt(args[0]) : 64;
 		p.getInventory().addItem(new ItemStack(Material.HUGE_MUSHROOM_2, amount));
 		return true;
 	}
	
 	@EventHandler
 	public void onEntityDamage(EntityDamageEvent e) {
 		if (!e.getEntityType().equals(EntityType.PLAYER)) return;
 		Player p = (Player) e.getEntity();
 		if (config.getBoolean("goldenBoots") && !goldenBoots(p)) return;
 		if (e.getCause().equals(DamageCause.FALL) && goldenBoots(p)) e.setCancelled(true);
 	}
	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent e) {
   		Player p = e.getPlayer();
   		if (config.getBoolean("goldenBoots") && !goldenBoots(p)) return;
   		World w = p.getWorld();
   		Location loc = p.getLocation();
   		int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
   		Material b = w.getBlockAt(x, y - 1, z).getType(), o = w.getBlockAt(x, y + 3,z).getType();
   		if (isMush(b)) {
   			p.setFallDistance(0);
   			Vector dir = p.getLocation().getDirection();
   	  		double sp = config.getDouble("sidePoof"), hp = config.getDouble("heightPoof") * 1.0D;
   	  	    if (p.isSprinting() && config.getBoolean("fasterWhenSprinting")) {
   	  	    	sp *= 1.5;
   	  	    	hp *= 1.2;
   	  	    }
   	  	    dir = dir.multiply(sp);
   	  	    dir.setY(hp);
   	  		if (p.getVelocity().getY() > STILL) {
   	  			p.setVelocity(dir);
   	  		}
   	  		if (w.getBlockAt(x, y - 3, z).getType().equals(Material.AIR) && p.isSneaking()) {
 				loc.setY(y - 3);
 				p.teleport(loc);
 			}
   		} else if (isMush(o)) {
   			Material oo = w.getBlockAt(x, y + 4, z).getType();
   	  		if (p.getVelocity().getY() > STILL && oo.equals(Material.AIR)) {
   	  			canJump = false;
   	  			loc.setY(y + 4);
   	  			p.teleport(loc);
   	  			p.setVelocity(new Vector(0, 0, 0));
   	  			canJump = true;
   	  		}
   		} else return;
 	}
	
 	private boolean isMush(Material m) {
 		if (m.equals(Material.HUGE_MUSHROOM_1) || m.equals(Material.HUGE_MUSHROOM_2)) return true;
 		return false;
 	}
	
 	private boolean isInt(String s) {
 		try {
			Integer.parseInt(s); 
 		} catch (NumberFormatException e) {
			return false; 
 		}
 	    return true;
 	}
	
 	private boolean goldenBoots(Player p) {
 		return (p.getInventory().getBoots() == null) ? false : p.getInventory().getBoots().getType().equals(Material.GOLD_BOOTS);
 	}
 }
