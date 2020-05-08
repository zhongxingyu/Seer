 package org.nationsatwar.snacktime;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Snacktime extends JavaPlugin implements Listener {
 	private static final Logger log = Logger.getLogger("Minecraft");
 	private boolean snacktime = true;
 	private HashMap<String, Date> cookietime = new HashMap<String, Date>();
 	
 	public String getVersion() {
 		return "0.1";
 	}
 	
 	public void sendToLog(String message) {
 		log.info("["+this.getName()+"]: " + message);
 	}
 	
 	public void onEnable() {		
 		this.sendToLog(this.getVersion()+ " Loaded");
 	}
 	
 	public void onDisable() {
 		this.sendToLog(this.getVersion()+ " Unloaded");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		if(sender instanceof Player) {
 			player = (Player) sender;
 		}
		if(cmd.getName().equalsIgnoreCase("snacktime")) {
			if((player != null && player.hasPermission("nations.cookieadmin")) || player == null) {
				this.clearAllCooldowns();
				this.messageAll("It's snacktime!");
				this.sendToLog("Snacktime initiated.");
				return true;
			} else {
				return false;
			}
		}
 		if(player == null) {
 			sender.sendMessage(ChatColor.RED + "You can't eat snacks!");
 			return false;
 		}
 		if(!this.snacktime) {
 			player.sendMessage(ChatColor.RED + "It's not snacktime.");
 			return false;
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("cookie")) {
 			if(player.hasPermission("nations.cookie")) {
 				int min = 180*1000;
 				int max = 1200*1000;
 				int cooltime = min + (int)(Math.random() * ((max - min) + 1));
 				if(checkCooldown(player.getName(), cooltime)) {
 					player.getInventory().addItem(new ItemStack(Material.COOKIE, 1));
 					if(Math.random() > .85) {
 						player.getInventory().addItem(new ItemStack(Material.MILK_BUCKET, 1));
 					}
 					player.playEffect(player.getLocation(), Effect.POTION_BREAK, 0);
 				} else {
 					player.sendMessage(ChatColor.RED + "You had snacks too recently. You'll ruin your dinner.");
 				}
 			} else {
 				player.sendMessage(ChatColor.RED + "No cookies for you.");
				return false;
 			}
 		}
 
 		/*if(cmd.getName().equalsIgnoreCase("milk")) {
 			if(player.hasPermission("nations.milk")) {
 				player.getInventory().addItem(new ItemStack(Material.MILK_BUCKET, 1));
 				player.playEffect(player.getLocation(), Effect.POTION_BREAK, 0);
 			} else {
 				player.sendMessage("No milk for you.");
 				return false;
 			}
 		}*/
 		return false;
 	}
 	
 	public void messageAll(String message) {
 		this.getServer().broadcastMessage(ChatColor.DARK_RED + "["+this.getName()+"]: " + message);
 	}
 	
 	public boolean checkCooldown(String player, int coolTime) {
 		if(this.cookietime.containsKey(player)) {
 			long checkDate = this.cookietime.get(player).getTime() + coolTime;
 			if( Calendar.getInstance().getTimeInMillis() > checkDate) {
 				this.cookietime.remove(player);
 				this.cookietime.put(player, Calendar.getInstance().getTime());
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			this.cookietime.put(player, Calendar.getInstance().getTime());
 			return true;
 		}
 	}
 	
 	public void clearCooldowns(String player) {
 		Iterator<Map.Entry<String, Date>> it = this.cookietime.entrySet().iterator();
 		while(it.hasNext()) {
 			Map.Entry<String, Date> pairs = it.next();
 			if(pairs.getKey().split(".", 2)[0].equals(player)) {
 				this.cookietime.remove(pairs.getKey());
 			}
 		}
 	}
 	
 	public void clearAllCooldowns() {
 		Iterator<Map.Entry<String, Date>> it = this.cookietime.entrySet().iterator();
 		while(it.hasNext()) {
 			Map.Entry<String, Date> pairs = it.next();
 			this.cookietime.remove(pairs.getKey());
 		}
 	}
 }
