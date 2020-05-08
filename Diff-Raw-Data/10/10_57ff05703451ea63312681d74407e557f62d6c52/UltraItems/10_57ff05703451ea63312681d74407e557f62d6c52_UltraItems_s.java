 package cz.ogion.ultraitems;
 
 import java.lang.reflect.Field;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Type;
 import org.bukkit.inventory.FurnaceRecipe;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.ConfigurationNode;
 import org.getspout.spoutapi.SpoutManager;
 
 public class UltraItems extends JavaPlugin {
 	public Map<String, ConfigurationNode> config;
 	Logger log = Logger.getLogger("Minecraft");
 	PluginDescriptionFile pdfile;
 	PlayerListener playerListener;
 	EntityListener entityListener;
 
 	public void onDisable() {}
 
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pdfile = this.getDescription();
 		playerListener = new PlayerListener(this);
 		entityListener = new EntityListener(this);
 		pm.registerEvent(Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Normal, this);
 		new SpoutInventoryListener(this);
 		loadConfig();
 		log.info(pdfile.getFullName() + " was enabled");
 	}
 
 	// TODO: reset textures and titles
 	// TODO: value testing
 	// TODO: unit testing
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
 					Material material = new MaterialData(itemid).getItemType();
 					if (url == null) {
 						throw new Exception("You have to specify item texture url", new Throwable("nourl"));
 					} else if (itemid == 0 || material == null) {
 						throw new Exception("You have to specify itemid (don't use 0 or non-existing item id)", new Throwable("wrongitem"));
 					} else if (title == null) {
 						throw new Exception("You have to specify item title", new Throwable("notitle"));
 					}
 					SpoutManager.getFileManager().addToCache(this, url);
 					SpoutManager.getItemManager().setItemTexture(material, itemdata, this, url);
 					SpoutManager.getItemManager().setItemName(material, itemdata, title);
 					try {
 						Field field1 = net.minecraft.server.Item.class.getDeclaredField("bs");
 						if (field1.getType() == boolean.class) {
 							field1.setAccessible(true);
 							field1.setBoolean(net.minecraft.server.Item.byId[itemid], true);
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					try {
 						Integer maxstacksize = item.getInt("maxstacksize", 0);
 						maxstacksize = (maxstacksize < 0 ? 0 : (maxstacksize > 64 ? 64 : maxstacksize));
 						if (maxstacksize != 0) {
 							Field field2 = net.minecraft.server.Item.class.getDeclaredField("maxStackSize");
 							field2.setAccessible(true);
 							field2.setInt(net.minecraft.server.Item.byId[itemid], maxstacksize);
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 
 					List<ConfigurationNode> recipes = item.getNodeList("recipes", null);
 					if (recipes != null) {
 						for (ConfigurationNode recipe : recipes) {
 							String type = recipe.getString("type", "");
 							Integer amount = recipe.getInt("amount", 1);
 							if (type.equalsIgnoreCase("furnace")) {
 								try {
 									Ingredient ingredient = new Ingredient(recipe.getString("ingredients", null));
 									FurnaceRecipe rcp = new FurnaceRecipe(new ItemStack(itemid, amount, itemdata), new MaterialData(ingredient.getMaterial(), ingredient.getDataByte()));
 									Bukkit.getServer().addRecipe(rcp);
 									log.info("[" + pdfile.getName() + "] " + "Added furnace recipe");
 								} catch (Exception e) {
 									log.warning("[" + pdfile.getName() + "] " + e.getMessage());
 								}
 							} else if (type.equalsIgnoreCase("shaped")) {
 								ShapedRecipe rcp = new ShapedRecipe(new ItemStack(itemid, amount, itemdata)).shape("abc", "def", "ghi");
 								String ingredients = recipe.getString("ingredients", "");
 								doRecipe(rcp, ingredients);
 								log.info("[" + pdfile.getName() + "] " + "Added shaped recipe");
 							} else if (type.equalsIgnoreCase("shapeless")) {
 								ShapelessRecipe rcp = new ShapelessRecipe(new ItemStack(itemid, amount, itemdata));
 								String ingredients = recipe.getString("ingredients", "");
 								doRecipe(rcp, ingredients);
 								log.info("[" + pdfile.getName() + "] " + "Added shapeless recipe");
 							} else {
 								log.warning("[" + pdfile.getName() + "] " + "You have to specify valid type of recipe (furnace, shaped, shapeless)");
 							}
 						}
 					}
 					// TODO: add to general
					// TODO: crafting recipes
 				} catch (NoSuchMethodError e) {
 					log.log(Level.SEVERE, "[" + pdfile.getName() + "] " + "NoSuchMethod Error. This is probably because your spout doesn't support required api, please upgrade to dev version. If you have dev version report the error bellow:");
 					e.printStackTrace();
 				} catch (Exception e) {
 					if (e.getCause() != null && (e.getCause().getMessage() == "wrongitem" || e.getCause().getMessage() == "notitle" || e.getCause().getMessage() == "nourl")) {
 						log.warning("[" + pdfile.getName() + "] " + e.getMessage());
 					} else {
 						e.printStackTrace();
 					}
 				}
 			}
 		} else {
 			this.getConfiguration().setProperty("UltraItems", null);
 			getConfiguration().save();
 		}
 	}
 	private void doRecipe(Recipe rcp, String ingredients) throws Exception {
 		Integer curline = 0;
 		Integer curcol = 0;
 		for (String line : ingredients.split("\\r?\\n")){
 			if (curline < 3) {
 				for (String ingredientitem : line.split(" ")){
 					if (curcol < 3) {
 						char a = (char) ('a' + curcol + curline * 3);
 						Ingredient ingredient = new Ingredient(ingredientitem);
 						curcol++;
 						try{
 							if (Integer.decode(ingredientitem)==0) {
 								continue;
 							}
 						} catch (NumberFormatException e) {
 						}
 						if(rcp instanceof ShapedRecipe) {
 							ShapedRecipe rc = (ShapedRecipe) rcp;
 							rc.setIngredient(a, ingredient.getMaterial(), ingredient.getData());
 						} else {
 							ShapelessRecipe rc = (ShapelessRecipe) rcp;
 							rc.addIngredient(ingredient.getMaterial(), ingredient.getData());
 						}
 					}
 				}
 				curcol = 0;
 			}
 			curline++;
 		}
 		Bukkit.getServer().addRecipe(rcp);
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("ultraitems")){
 			if(args.length == 1) {
 				if(args[0].equalsIgnoreCase("reload")) {
 					if(sender.hasPermission("ultraitems.reload")) {
 						loadConfig();
 						sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
 					} else {
 						sender.sendMessage(ChatColor.RED + "You don't have permission for reloading config.");
 					}
 					return true;
 				} else if(args[0].equalsIgnoreCase("list")) {
 					if(sender.hasPermission("ultraitems.list")) {
 						StringBuilder sb = new StringBuilder();
 						for(String s : config.keySet()) {
 							sb.append(s);
 							sb.append(" ");
 						}
 						sender.sendMessage("UltraItems: " + ChatColor.YELLOW + sb.toString());
 					} else {
 						sender.sendMessage(ChatColor.RED + "You don't have permission for list of ultra items.");
 					}
 					return true;
 				} else if(config.containsKey(args[0])) {
 					if (sender instanceof ConsoleCommandSender){
 						sender.sendMessage(ChatColor.RED + "This command must be run in-game.");
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
 										sender.sendMessage(ChatColor.GREEN + "Here you are!");
 									} else {
 										sender.sendMessage(ChatColor.RED + args[0]+" has incorrectly set data! Please contact server admin.");
 									}
 								} catch (Exception e) {
 									sender.sendMessage(ChatColor.RED + "Error:"+e.getMessage());
 								}
 							} else {
 								sender.sendMessage(ChatColor.RED + "You don't have permission to get " + args[0] + ".");
 							}
 						} else {
 							who.sendMessage(ChatColor.RED + "You don't have permission to get ultra items.");
 						}
 					}
 					return true;
 				} else {
 					sender.sendMessage(ChatColor.RED + args[0] + " isn't neither command or item!");
 				}
 			}
 		}
 		return false;
 	}
 }
