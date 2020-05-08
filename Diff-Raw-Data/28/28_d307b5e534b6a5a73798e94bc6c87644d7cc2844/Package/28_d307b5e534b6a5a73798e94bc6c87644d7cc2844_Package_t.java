 package com.asylumsw.bukkit.kits;
 
 import java.util.Calendar;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Map;
import java.util.concurrent.TimeUnit;
 import org.bukkit.ChatColor;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author jonathan
  */
 public class Package {
 	private String name;
 	private EnumMap<Material, Integer> items = new EnumMap<Material, Integer>(Material.class);
 	private int cooldown;
 	private HashMap<String, Long> playerUses = new HashMap<String, Long>();
 
 	public Package() {
 	}
 
 	public Package(String name, int cooldown) {
 		this.name = name;
 		this.cooldown = cooldown;
 	}
 
 	public void update(Package pkg) {
 		if( !name.equalsIgnoreCase(pkg.getName()) ) return;
 
 		cooldown = pkg.getCooldown();
 		items = pkg.items;
 		playerUses.putAll(pkg.playerUses);
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getName() {
 		return name;
 	}
 	public int getNumItems() {
 		return items.size();
 	}
 	public int getCooldown() {
 		return cooldown;
 	}
 
 	public boolean addItem(Material item, int quantity) {
 		quantity = Math.max(quantity, 1) + (items.containsKey(item) ? items.get(item) : 0);
 		items.put(item, quantity);
 		return true;
 	}
 
 	public boolean spawnAtPlayer(Player player) {
 		if( playerUses.containsKey(player.getName()) ) {
 			/**
 			 * Only do cooldown checking if the player has used this kit before,
 			 * (and thus triggered a cooldown)
 			 */
 
 			// Calcualte when this package will be usable by this player again.
 			Calendar pkgUsableTime = Calendar.getInstance();
 			pkgUsableTime.setTimeInMillis(playerUses.get(player.getName()));
 			pkgUsableTime.add(Calendar.SECOND, cooldown);
 
 			if( pkgUsableTime.after(Calendar.getInstance()) ) {
				player.sendMessage(ChatColor.DARK_GRAY+"[kits] "+
								ChatColor.RED+"ERROR: Kit '"+name+"' is unavailable,"+
								getDurationString(TimeUnit.MILLISECONDS.toSeconds(pkgUsableTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()))+
								ChatColor.RED+" remaining.");
 				return false;
 			}
 		}
 
 		playerUses.put(player.getName(), Calendar.getInstance().getTimeInMillis());
 		spawnAtLocation(player.getLocation());
 		return true;
 	}
 	
 	public void spawnAtLocation(Location loc) {
 		for( Map.Entry<Material,Integer> item : items.entrySet()) {
 			loc.getWorld().dropItem(loc, new ItemStack(item.getKey(), item.getValue()));
 		}
 	}
 
 	public String getChatMessage() {
 		String message = ChatColor.BLUE + name + ": ";
 		for( Map.Entry<Material,Integer> item : items.entrySet()) {
 			message += ChatColor.LIGHT_PURPLE + item.getKey().toString() + " "
 							+ ChatColor.GRAY + "("+item.getValue()+"), ";
 		}
		return message.substring(0, message.length()-2)+getDurationString(cooldown);
 	}
 
 	@Override
 	public String toString() {
 		String line = String.format("%s:%d;", name, cooldown);
 		for( Map.Entry<Material,Integer> item : items.entrySet() ) {
 			line += String.format("%s:%d;", item.getKey().toString(), item.getValue());
 		}
 		return line;
 	}
 
	protected String getDurationString(long duration) {
		if( 0 >= duration ) return "";

		return String.format(" %s[%s%02d%sh%s%02d%sm%s%02d%ss]",
			ChatColor.GRAY, ChatColor.DARK_AQUA, TimeUnit.SECONDS.toHours(duration), ChatColor.GRAY,
			ChatColor.DARK_AQUA, TimeUnit.SECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(duration)),
			ChatColor.GRAY, ChatColor.DARK_AQUA,
			duration - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration)),
			ChatColor.GRAY
		);
	}

}
