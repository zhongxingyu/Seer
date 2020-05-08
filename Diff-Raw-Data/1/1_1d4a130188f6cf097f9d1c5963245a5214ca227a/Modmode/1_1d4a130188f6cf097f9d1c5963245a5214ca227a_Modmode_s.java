 package iggy.Modmode;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 //import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 //import org.bukkit.potion.PotionEffect;
 //import org.bukkit.potion.PotionEffectType;
 
 
 public class Modmode extends JavaPlugin{
 	Logger logger = Logger.getLogger("Minecraft");
 	
 	String pluginTitle;
 	PluginDescriptionFile pdFile;
 	
 	public Map<Player, Double> lightningDegree = new HashMap<Player, Double>();
 	public double distance = 7;
   //////////////////////////////////////////////////////////////////////////////
  ////////////////////////////// ENABLE / DISABLE //////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	@Override
 	public void onDisable() {
 		info(" Version " + pdFile.getVersion() +" is disabled");
 	}
 	
 	@Override
 	public void onEnable () {
 		pdFile = this.getDescription();
 		String pluginName = pdFile.getName();
 		pluginTitle = "[\033[2;35m"+pluginName+"\033[0m]";
 		
 		
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable () {
 			public void run() {
 				for (Entry<Player,Double> p : lightningDegree.entrySet()) {
 					Player player = p.getKey();
 					
 					info (player.getName() + " is in GODMODE at x=" + player.getLocation().getBlockX() + " z="+player.getLocation().getBlockZ());
 					
 					double deg = (double)p.getValue();
 					if (deg >= 360) deg -= 360;
 					
 					//player.sendMessage("HI! DEG AT "+deg+" "+Math.sin(Math.PI*deg/180)*distance+":"+Math.cos(Math.PI*deg/180)*distance);
 					//World world = player.getWorld();
 					
 					Location location = player.getLocation();
 					//location.add(Math.acos(deg)*distance, 0, Math.asin(deg)*distance);
 					location.add(Math.sin(Math.PI*deg/180)*distance, 0, Math.cos(Math.PI*deg/180)*distance);
 					
 					location.setY(player.getWorld().getHighestBlockYAt(location));
 					
 					p.getKey().getWorld().strikeLightningEffect(location);
 					lightningDegree.put(player, deg+20);
 				}
 			}
 		}, 2L, 2L);
 		
 		info ("Version " + pdFile.getVersion() +" is enabled");
 	}
 	
 
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		if (player == null) {
 			info("This command can only be run by a player");
 			return false;
 		}
 		
 		info (player.getName() + " tried to run the command " + commandLabel);
 		
 		if (!player.isOp()) {
 			player.sendMessage(ChatColor.RED+"This command can only be run by an Admin" + ChatColor.WHITE);
 		}
 		
 		//World world = player.getWorld();
 		
 		if (commandLabel.equalsIgnoreCase("GODMODE")){
 			if (lightningDegree.containsKey(player)){
 				lightningDegree.remove(player);
 				player.sendMessage("Godmode deactivated");
 				info (player.getName() + "Left Godmode at x=" + player.getLocation().getBlockX() + " z="+player.getLocation().getBlockZ());
 			}
 			else {
 				info (player.getName() + "Entered Godmode at x=" + player.getLocation().getBlockX() + " z="+player.getLocation().getBlockZ());
 				player.sendMessage("Godmode activated");
 				lightningDegree.put(player, 0D);
 				if (player.getItemInHand().getType() == Material.DIAMOND_SWORD) {
 					info (player.getName() + " recieved the admin sword");
 					player.getItemInHand().addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 999);
 					player.getItemInHand().addUnsafeEnchantment(Enchantment.DURABILITY, 999);
 					player.getItemInHand().setType(Material.WOOD_SWORD);
 				}
 			}
 			
 		}
 		
 		if (commandLabel.equalsIgnoreCase("RAGE")) {
 			info (player.getName() + " lighnign raged at x=" + player.getLocation().getBlockX() + " z="+player.getLocation().getBlockZ());
 			for (double deg = 0; deg < 360; deg += 10){
 				Location location = player.getLocation();
 				//location.add(Math.acos(deg)*distance, 0, Math.asin(deg)*distance);
 				location.add(Math.sin(Math.PI*deg/180)*distance, 0, Math.cos(Math.PI*deg/180)*distance);
 				location.setY(player.getWorld().getHighestBlockYAt(location));
 				
 				player.getWorld().strikeLightningEffect(location);
 			}
 		}
 		
 		if (commandLabel.equalsIgnoreCase("OPEN")) {
 			if (args.length != 1) {
 				player.sendMessage("Correct usage /open <playername>");
 				return false;
 			}
 			Player target = getServer().getPlayer(args[0]);
 			if (target == null) {
 				player.sendMessage(args[0] + " not found online");
 				return false;
 			}
 			player.openInventory(target.getInventory());
 		}
 		return false;
 	}
   //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// DISPLAY HELPERS //////////////////////////////
 //////////////////////////////////////////////////////////////////////////////
 	/********************************** LOG INFO **********************************\
 	| The log info function is a simple function to display info to the console    |
 	| logger. It also prepends the plugin title (with color) to the message so     |
 	| that the plugin that sent the message can easily be identified               |
 	\******************************************************************************/
 	public void info(String input) {
 		this.logger.info("  " + pluginTitle + " " + input);
 	}
 	
 	/********************************* LOG SEVERE *********************************\
 	| The log severe function is very similar to the log info function in that it  |
 	| displays information to the console, but the severe function sends a SEVERE  |
 	| message instead of an INFO. It also turns the message text red               |
 	\******************************************************************************/
 	public void severe (String input) {
 		this.logger.severe(pluginTitle+" \033[31m"+input+"\033[0m");
 	}
 }
