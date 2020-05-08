 package cz.ogion.ultraitems;
 
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Type;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 import org.bukkit.util.config.ConfigurationNode;
 import org.getspout.spoutapi.SpoutManager;
 
 public class UltraItems extends JavaPlugin {
 	public Map<String, ConfigurationNode> config;
 	Logger log = Logger.getLogger("Minecraft");
 	PluginDescriptionFile pdfile;
 	PlayerListener playerListener;
 
 	public void onDisable() {}
 
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pdfile = this.getDescription();
 		playerListener = new PlayerListener(this);
 		pm.registerEvent(Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Monitor, this);
 		loadConfig();
 		log.info(pdfile.getFullName()+" was enabled");
 	}
 
 	public void loadConfig() {
 		getConfiguration().load();
 		config = this.getConfiguration().getNodes("UltraItems");
 		if (config != null) {
 			for(ConfigurationNode item : config.values()) {
 				try {
 					String url = item.getString("url", null);
 					Integer itemid = item.getInt("item", 0);
 					Short itemdata = ((Integer) item.getInt("data", 0)).shortValue();
 					String title = item.getString("title", null);
 					SpoutManager.getFileManager().addToCache(this, url);
					SpoutManager.getItemManager().setItemTexture(new MaterialData(itemid).getItemType(), itemdata, this, url);
					SpoutManager.getItemManager().setItemName(new MaterialData(itemid).getItemType(), itemdata, title);
 					// TODO: add to general
 					// TODO: crafting recipes
 				} catch (NoSuchMethodError e) {
					log.log(Level.SEVERE, "[" + pdfile.getName() + "]" + " NoSuchMethod Error. This is probably because your spout doesn't support required api, please upgrade to dev version. If you have dev version report the error bellow:");
 					e.printStackTrace();
 				} catch (Exception e) {
					e.printStackTrace();
 				}
 			}
 		} else {
 			this.getConfiguration().setProperty("UltraItems", null);
 			getConfiguration().save();
 		}
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("ultraitems")){
 			if(args.length == 1) {
 				if(args[0].equalsIgnoreCase("reload")) {
 					if(sender.hasPermission("ultraitems.reload")) {
 						loadConfig();
 						sender.sendMessage("Config reloaded.");
 					} else {
 						sender.sendMessage("You don't have permission for reloading config.");
 					}
 					return true;
 				} else if(args[0].equalsIgnoreCase("list")) {
 					if(sender.hasPermission("ultraitems.list")) {
 						StringBuilder sb = new StringBuilder();
 						for(String s : config.keySet()) {
 							sb.append(s);
 							sb.append(" ");
 						}
 						sender.sendMessage("UltraItems: " + sb.toString());
 					} else {
 						sender.sendMessage("You don't have permission for list of ultra items.");
 					}
 					return true;
 				} else if(config.containsKey(args[0])) {
 					if (sender instanceof ConsoleCommandSender){
 						sender.sendMessage("This command must be run in-game.");
 					} else {
 						Player who = (Player) sender;
 						if (who.hasPermission("ultraitems.give")){
 							if (who.hasPermission("ultraitems.give.*") || who.hasPermission("ultraitems.give."+args[0])){
 								try {
 									ConfigurationNode item = config.get(args[0]);
 									Integer itemid = item.getInt("item", 0);
 									Short itemdata = ((Integer)item.getInt("data", 0)).shortValue();
 									if(itemid != 0) {
 										ItemStack stack = new ItemStack(itemid, 1, itemdata);
 										int slot = who.getInventory().firstEmpty();
 										if(slot < 0) {
 											who.getWorld().dropItem(who.getLocation(), stack);
 										} else {
 											who.getInventory().addItem(stack);
 										}
 										sender.sendMessage("Here you are!");
 									} else {
 										sender.sendMessage(args[0]+" has incorrectly set data! Please contact server admin.");
 									}
 								} catch (Exception e) {
 									sender.sendMessage("Error:"+e.getMessage());
 								}
 							} else {
 								sender.sendMessage("You don't have permission to get " + args[0] + ".");
 							}
 						} else {
 							who.sendMessage("You don't have permission to get ultra items.");
 						}
 					}
 					return true;
 				} else {
 					sender.sendMessage(args[0] + " isn't neither command or item!");
 				}
 			}
 		}
 		return false;
 	}
 }
