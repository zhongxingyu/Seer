 package com.araeosia.ArcherGames.utils;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import java.util.ArrayList;
 import java.util.List;
import java.util.logging.Level;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 import static org.bukkit.enchantments.Enchantment.*;
 
 
 public class Config {
 
 	
 	private ArcherGames plugin;
 	/**
 	 * 
 	 * @param plugin
 	 */
 	public Config(ArcherGames plugin){
 		this.plugin = plugin;
 	}
 	
 	/**
 	 * 
 	 */
 	public void loadConfiguration(){
 		if(!(plugin.getConfig().getDouble("ArcherGames.technical.version") == 0.1)){
 			plugin.getConfig().set("ArcherGames.technical.debug", false);
 			plugin.getConfig().set("ArcherGames.technical.version", 0.1);
 			ArrayList<String> voteSites = new ArrayList<String>();
 			voteSites.add("http://ow.ly/cpQI0");
 			voteSites.add("http://ow.ly/cmwer");
 			voteSites.add("http://ow.ly/cmnPu");
 			voteSites.add("http://ow.ly/cmDIF");
 			voteSites.add("http://ow.ly/cmP18");
 			voteSites.add("http://ow.ly/cmETB");
 			voteSites.add("http://ow.ly/dVcsF");
 			voteSites.add("http://ow.ly/eggLe");
 			plugin.getConfig().set("ArcherGames.vote.sites", voteSites);
 			plugin.getConfig().set("ArcherGames.timers.preGameCountdown", 120); // 2 minutes for everyone to get in game
 			plugin.getConfig().set("ArcherGames.timers.gameInvincibleCountdown", 60); // 1 minute for everyone to get far enough away from each other
 			plugin.getConfig().set("ArcherGames.timers.gameOvertimeCountdown", 600); // 10 minutes to play before we force the round to end
 			plugin.getConfig().set("ArcherGames.timers.shutdownTimer", 30); // 30 seconds until the server reboots.
			plugin.getConfig().set("ArcherGames.game.startPosition.world", plugin.getServer().getWorlds().get(0).getName()); // Fetch the default world
 			plugin.getConfig().set("ArcherGames.game.startPosition.x", 0);
 			plugin.getConfig().set("ArcherGames.game.startPosition.y", 64);
 			plugin.getConfig().set("ArcherGames.game.startPosition.z", 0);
 			plugin.getConfig().set("ArcherGames.kits.ExampleKitName1", "itemid:damage:enchantid:enchantlvl");
 			plugin.getConfig().set("ArcherGames.strings.startnotenoughplayers", "[ArcherGames] Attempted to start, but there weren't enough players.");
 			plugin.getConfig().set("ArcherGames.strings.starting", "[ArcherGames] The Archer Games have started! You have 1 minute of invincibility to get away from enemies.");
 			plugin.getConfig().set("ArcherGames.strings.invincibilityend", "[ArcherGames] Your minute of invincibility is up! Let the games begin...");
 			plugin.getConfig().set("ArcherGames.strings.overtimestart", "[ArcherGames] Overtime has started! Fight to the death!");
 			plugin.getConfig().set("ArcherGames.strings.gameended", "[ArcherGames] The game is over!");
 			plugin.getConfig().set("ArcherGames.strings.serverclosekick", "The server is rebooting.");
 			plugin.getConfig().set("ArcherGames.strings.joinedgame", "[ArcherGames] Welcome %s to %s, make sure to read your book for information!");
 			plugin.getConfig().set("ArcherGames.strings.servername", "ArcherGamesServer");
 			plugin.getConfig().set("ArcherGames.strings.starttimeleft", "[ArcherGames] The game will start in %s!");
 			plugin.saveConfig();
 		}
 		plugin.voteSites = (java.util.List<String>) plugin.getConfig().getList("ArcherGames.vote.sites");
 		plugin.scheduler.preGameCountdown = plugin.getConfig().getInt("ArcherGames.timers.preGameCountdown");
 		plugin.scheduler.gameInvincibleCountdown = plugin.getConfig().getInt("ArcherGames.timers.gameInvincibleCountdown");
 		plugin.scheduler.gameOvertimeCountdown = plugin.getConfig().getInt("ArcherGames.timers.gameOvertimeCountdown");
 		plugin.scheduler.minPlayersToStart = plugin.getConfig().getInt("ArcherGames.game.minPlayersToStart");
 		plugin.scheduler.shutdownTimer = plugin.getConfig().getInt("ArcherGames.game.shutdownTimer");
 		plugin.startPosition = new Location(
 			plugin.getServer().getWorld(plugin.getConfig().getString("ArcherGames.game.startPosition.world")),
 			plugin.getConfig().getInt("ArcherGames.game.startPosition.x"),
 			plugin.getConfig().getInt("ArcherGames.game.startPosition.y"),
 			plugin.getConfig().getInt("ArcherGames.game.startPosition.z")
 		);
 		plugin.strings.put("startnotenoughplayers", plugin.getConfig().getString("ArcherGames.strings.startnotenoughplayers"));
 		plugin.strings.put("starting", plugin.getConfig().getString("ArcherGames.strings.starting"));
 		plugin.strings.put("invincibilityend", plugin.getConfig().getString("ArcherGames.strings.invincibilityend"));
 		plugin.strings.put("overtimestart", plugin.getConfig().getString("ArcherGames.strings.overtimestart"));
 		plugin.strings.put("gameended", plugin.getConfig().getString("ArcherGames.strings.gameended"));
 		plugin.strings.put("serverclosekick", plugin.getConfig().getString("ArcherGames.strings.serverclosekick"));
 		plugin.strings.put("joinedgame", plugin.getConfig().getString("ArcherGames.strings.joinedgame"));
 		plugin.strings.put("starttimeleft", plugin.getConfig().getString("ArcherGames.strings.starttimeleft"));
 		loadKits();
 		}
 		
 		/**
 		 * Load the Kits form the Config
 		 * 
 		 */
 		   public void loadKits(){
 			   ArrayList<ItemStack> temp;
 			   for(String key : plugin.getConfig().getConfigurationSection("ArcherGames.kits").getKeys(false)){
 				   temp = new ArrayList<ItemStack>();
 				   String s = plugin.getConfig().getString("ArcherGames.kits." + key);
 				   for(String str : s.split(",")){
						try{
						ItemStack itemStack = new ItemStack( Material.getMaterial(Integer.parseInt( str.split(":")[0] )), Integer.parseInt(str.split(":")[1]));
						itemStack.addEnchantment(Enchantment.getById(Integer.parseInt(str.split(":")[2])), Integer.parseInt(str.split(":")[3]));
						temp.add( itemStack );
						} catch (NumberFormatException e) {
							plugin.log.log(Level.SEVERE, "Warning: ArcherGames Kit {0} is not configured correctly!", key);
						}
 				   }
 				   plugin.kits.put(key, temp);
 			   }
 		}
 }
