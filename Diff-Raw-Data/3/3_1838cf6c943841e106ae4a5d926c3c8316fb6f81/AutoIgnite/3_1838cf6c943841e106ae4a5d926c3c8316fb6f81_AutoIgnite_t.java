 package ca.kanoa.request.autoignite;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.World;
 
 public class AutoIgnite extends JavaPlugin implements Listener {
 
 	private Configuration config;
 	private String allowedIn;
 	
 	@Override
 	public void onEnable() {
 		this.getServer().getPluginManager().registerEvents(this, this);
 		this.saveDefaultConfig();
 		this.config = this.getConfig();
 		if (config.getString("allowed_worlds") == null) {
 			List<World> worlds = Bukkit.getWorlds();
 			StringBuilder sb = new StringBuilder(worlds.get(0).getName());
 			for (int i = 1; i < worlds.size(); i++)
 				sb.append(", " + worlds.get(i));
 			config.set("allowed_worlds", sb.toString());
 			allowedIn = sb.toString();
 			this.saveConfig();
 			config = this.getConfig();
 		} else 
 			allowedIn = config.getString("allowed_worlds");
 	}
 	
 	@EventHandler
 	public void onBlockPlaced(BlockPlaceEvent event) {
 		if (event.getPlayer() != null && 
 				event.getPlayer().hasPermission("autoignite") && 
 				allowedIn.contains(event.getPlayer().getWorld().getName()) && 
 				event.getBlock().getType().equals(Material.TNT)) {
 			Location loc = event.getBlock().getLocation();
 			loc.getBlock().setType(Material.AIR);
			loc.setY(loc.getY() + 0.9f);
			loc.setX(loc.getX() + 0.5f);
			loc.setZ(loc.getZ() + 0.5f);
 			loc.getWorld().spawn(loc, TNTPrimed.class);
 		}
 	}
 
 }
