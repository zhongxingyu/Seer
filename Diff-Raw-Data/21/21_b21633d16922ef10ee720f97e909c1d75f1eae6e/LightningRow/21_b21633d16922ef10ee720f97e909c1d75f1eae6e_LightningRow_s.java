 package net.craftrepo.WeatherControl;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class LightningRow implements Runnable {
 
 	private Integer range;
 	private Integer count=0;
 	public Integer id;
 	public BlockFace face;
 	private Location current;
 	private Location start;
 	private World world;
 	private JavaPlugin plugin;
 	
 	
 	@Override
 	public void run() {
 		if(count<range)
 		{
 			Location loc = world.getBlockAt(current).getFace(face).getLocation();
 			loc.setY(world.getHighestBlockYAt(loc));
 			world.strikeLightning(loc);
 			current = loc;
 			count++;
 		}
 		else
 		{
 			plugin.getServer().getScheduler().cancelTask(id);
 		}
 		
 	}
 	/**
 	 * @param range the range to set
 	 */
 	public void setRange(Integer range) {
 		this.range = range;
 	}
 	/**
 	 * @return the range
 	 */
 	public Integer getRange() {
 		return range;
 	}
 	/**
 	 * @param current the current to set
 	 */
 	public void setCurrent(Location current) {
 		this.current = current;
 	}
 	/**
 	 * @return the current
 	 */
 	public Location getCurrent() {
 		return current;
 	}
 	/**
 	 * @param world the world to set
 	 */
 	public void setWorld(World world) {
 		this.world = world;
 	}
 	/**
 	 * @return the world
 	 */
 	public World getWorld() {
 		return world;
 	}
 	/**
 	 * @param start the start to set
 	 */
 	public void setStart(Location start) {
 		this.start = start;
 	}
 	/**
 	 * @return the start
 	 */
 	public Location getStart() {
 		return start;
 	}
 	/**
 	 * @param plugin the plugin to set
 	 */
 	public void setPlugin(JavaPlugin plugin) {
 		this.plugin = plugin;
 	}
 	/**
 	 * @return the plugin
 	 */
 	public JavaPlugin getPlugin() {
 		return plugin;
 	}
 
 }
