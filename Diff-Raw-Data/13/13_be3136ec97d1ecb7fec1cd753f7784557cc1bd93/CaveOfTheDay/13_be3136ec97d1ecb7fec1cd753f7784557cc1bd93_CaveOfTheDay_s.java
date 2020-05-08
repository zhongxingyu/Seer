 package krato.lekro.bukkitplugins.caveoftheday;
 
 import java.io.File;
 import java.util.Calendar;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CaveOfTheDay extends JavaPlugin {
 	Location cotdlocation;
 	File cotdfile;
 	//String timestring;
 	public void onEnable() {
		cotdlocation = new Location(
 				Bukkit.getServer().getWorld(this.getConfig().getString("world-name")),
 				this.getConfig().getDouble("x-coordinate"),
 				this.getConfig().getDouble("y-coordinate"),
 				this.getConfig().getDouble("z-coordinate"),
 				new Float(this.getConfig().getDouble("yaw-coordinate")),
 				new Float(this.getConfig().getDouble("pitch-coordinate"))
				);
 	}
 	public void onDisable() {
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("cotd")) {
 			if (sender instanceof Player) {
 				Player player = (Player) sender;
 				if (cotdlocation != null) {
 					player.sendMessage(ChatColor.GRAY+"Teleporting to the Cave of the Day, set by "+this.getConfig().getString("player-set")+" on "+getTimeString()+"...");
 					player.teleport(cotdlocation);
 				}
 				else {
 					sender.sendMessage(ChatColor.RED+"The Cave of the Day has not been set! Ask an administrator to set it!");
 				}
 			}
 			else {
 				sender.sendMessage("You can't invoke this command through the console :P");
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("cotdset")) {
 			if (sender instanceof Player) {
 				Player player = (Player) sender;
 				
 				cotdlocation = player.getLocation();
 				sender.sendMessage(ChatColor.GREEN+"Cave of the Day set!");
 				Bukkit.getServer().broadcastMessage(ChatColor.BLUE+player.getDisplayName()+" has set the Cave of the Day! Use /cotd to go there!");
 				Calendar now = Calendar.getInstance();
 				this.getConfig().set("x-coordinate", cotdlocation.getX());
 				this.getConfig().set("y-coordinate", cotdlocation.getY());
 				this.getConfig().set("z-coordinate", cotdlocation.getZ());
 				this.getConfig().set("yaw-coordinate", cotdlocation.getYaw());
 				this.getConfig().set("pitch-coordinate", cotdlocation.getPitch());
 				this.getConfig().set("world-name", cotdlocation.getWorld().getName());
 				this.getConfig().set("month-set", now.get(Calendar.MONTH));
 				this.getConfig().set("year-set", now.get(Calendar.YEAR));
 				this.getConfig().set("date-set", now.get(Calendar.DAY_OF_MONTH));
 				this.getConfig().set("hour-set", now.get(Calendar.HOUR_OF_DAY));
 				this.getConfig().set("minute-set", now.get(Calendar.MINUTE));
 				this.getConfig().set("second-set", now.get(Calendar.SECOND));
 				this.getConfig().set("player-set", player.getName());
 				this.saveConfig();
 				
 			}
 			else {
 				sender.sendMessage("You can't invoke this command through the console :P");
 			}
 			return true;
 		}
 		else {
 			return false;
 		}
 		
 		
 	}
 	private String getTimeString() {
 		return this.getConfig().getInt("month-set")+"/"+this.getConfig().getInt("date-set")+"/"+this.getConfig().getInt("year-set")+" "+this.getConfig().getInt("hour-set")+":"+this.getConfig().getInt("minute-set")+":"+this.getConfig().getInt("second-set");
 	}
 }
