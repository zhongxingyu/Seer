 package me.eccentric_nz.plugins.secretary;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 import java.util.logging.Logger;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Villager;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SecretaryExecutor extends JavaPlugin implements CommandExecutor {
 
 	private Secretary plugin;
 	private World world;
 	private Block targetBlock;
 	private Location spawnLoc;
 	private LivingEntity thesecretary;
 	private UUID secID;
 	private double secLocX;
 	private double secLocY;
 	private double secLocZ;
 	private String secWorld;
 	private static Logger log;
 	private boolean fences = false;
 	private boolean plates = false;
 
 	public SecretaryExecutor(Secretary plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		// If the player typed /setprof then do the following...
 		// check there is the right number of arguments
 		if (cmd.getName().equalsIgnoreCase("secretary")) {
 			Player player = null;
 			if (sender instanceof Player) {
 				player = (Player) sender;
 			}
 			if (args.length == 0) {
 				sender.sendMessage(Constants.COMMANDS.split("\n"));
 				return true;
 			}
 			// the command list - first argument MUST appear here!
 			if (!args[0].equals("create") && !args[0].equals("todo") && !args[0].equals("remind") && !args[0].equals("delete") && !args[0].equals("setsound") && !args[0].equals("name") && !args[0].equals("help") && !args[0].equals("admin") && !args[0].equals("repeat")) {
 				sender.sendMessage("Do you want to create, todo, remind, setsound, view name or delete?");
 				return false;
 			}
 			if (args[0].equals("admin")) {
 				if (args.length == 1) {
 					sender.sendMessage(Constants.COMMAND_ADMIN.split("\n"));
 					return true;
 				}
 				if (args.length < 3) {
 					sender.sendMessage("Too few command arguments!");
 					return false;
 				} else {
 					if (args[1].equals("s_limit")) {
 						String a = args[2];
 						int val;
 						try {
 							val = Integer.parseInt(a);
 						} catch (NumberFormatException nfe) {
 							// not a number
 							sender.sendMessage("The last argument must be a number!");
 							return false;
 						}
 						plugin.config.set("secretary_limit", val);
 					}
 					if (args[1].equals("t_limit")) {
 						String a = args[2];
 						int val;
 						try {
 							val = Integer.parseInt(a);
 						} catch (NumberFormatException nfe) {
 							// not a number
 							sender.sendMessage("The last argument must be a number!");
 							return false;
 						}
 						plugin.config.set("todo_limit", val);
 					}
 					if (args[1].equals("r_limit")) {
 						String a = args[2];
 						int val;
 						try {
 							val = Integer.parseInt(a);
 						} catch (NumberFormatException nfe) {
 							// not a number
 							sender.sendMessage("The last argument must be a number!");
 							return false;
 						}
 						plugin.config.set("reminder_limit", val);
 					}
 					if (args[1].equals("use_inv")) {
 						// check they typed true of false
 						String tf = args[2].toLowerCase();
 						if (!tf.equals("true") && !tf.equals("false")) {
 							sender.sendMessage(ChatColor.RED + "The last argument must be true or false!");
 							return false;
 						}
 						plugin.config.set("use_inventory", Boolean.valueOf(tf));
 					}
 					if (args[1].equals("damage")) {
 						String a = args[2];
 						int val;
 						try {
 							val = Integer.parseInt(a);
 						} catch (NumberFormatException nfe) {
 							// not a number
 							sender.sendMessage("The last argument must be a number!");
 							return false;
 						}
 						// convert minutes to seconds (60 per minute) to ticks (20 per second)
 						int ticks = val * 60 * 20;
 						plugin.config.set("no_damage", ticks);
 					}
 					try {
 						plugin.config.save(plugin.myconfigfile);
 						sender.sendMessage("The config was updated!");
 					} catch (IOException e) {
 						sender.sendMessage("There was a problem saving the config file!");
 					}
 				}
 				return true;
 			}
 			if (player == null) {
 				sender.sendMessage("This command can only be run by a player");
 				return false;
 			} else {
 				if (args[0].equals("create")) {
 					if (player.hasPermission("secretary.create")) {
 						// check if secretary limit has been reached
 						String configPath = player.getName();
 						if (plugin.secrets.isSet(configPath)) {
 							Set<String> seclist = plugin.secrets.getConfigurationSection(configPath).getKeys(false);
 							int size = seclist.size();
 							int limit = plugin.config.getInt("secretary_limit");
 							if (size >= limit) {
 								sender.sendMessage("You have reached the maximum allowed number of secretaries! The limit is " + limit + " per player.");
 								return false;
 							}
 						}
 						if (plugin.config.getBoolean("use_inventory") == true && player.getGameMode() == GameMode.SURVIVAL) {
 							if (!player.getInventory().contains(Material.FENCE, 8)) {
 								sender.sendMessage("You do not have enough wood fences, (at least are 8 needed).");
 								return false;
 							}
 							if (!player.getInventory().contains(Material.WOOD_PLATE, 3)) {
 								sender.sendMessage("You do not have enough wood pressure plates, (at least are 3 needed).");
 								return false;
 							}
 							// remove the items from the players inventory
 							player.getInventory().removeItem(new ItemStack(Material.FENCE, 8));
 							player.getInventory().removeItem(new ItemStack(Material.WOOD_PLATE, 3));
 						}
 
 						EntityType et = EntityType.fromName("VILLAGER");
 						if (et == null) {
 							return true;
 						}
 						// correct for negative yaw
 						float pyaw = player.getLocation().getYaw();
 						if (pyaw >= 0) {
 							pyaw = (pyaw % 360);
 						} else {
 							pyaw = (360 + (pyaw % 360));
 						}
 						spawnLoc = player.getTargetBlock(null, 50).getLocation();
 						/*
 						 * need to set the x and z values + 0.5 as block coords
 						 * seem to start from the corner of the block causing
 						 * the spawned villager to take damage from surrounding
 						 * blocks. also need to set the y value + 1 as block
 						 * coords seem to start from bottom of the block and we
 						 * want to spawn the villager on top of and in the
 						 * middle of the block!
 						 */
 						double lowX = spawnLoc.getX();
 						double lowY = spawnLoc.getY();
 						double lowZ = spawnLoc.getZ();
 						spawnLoc.setX(lowX + 0.5);
 						spawnLoc.setY(lowY + 1);
 						spawnLoc.setZ(lowZ + 0.5);
 						// get relative locations
 						int x = (spawnLoc.getBlockX());
 						int plusx = (spawnLoc.getBlockX() + 1);
 						int minusx = (spawnLoc.getBlockX() - 1);
 						int y = (spawnLoc.getBlockY());
 						int plusy = (spawnLoc.getBlockY() + 1);
 						int z = (spawnLoc.getBlockZ());
 						int plusz = (spawnLoc.getBlockZ() + 1);
 						int minusz = (spawnLoc.getBlockZ() - 1);
 						world = spawnLoc.getWorld();
 
 						// setBlock(World w, int x, int y, int z, float yaw, float min, float max, String compare)
 						Constants.setBlock(world, plusx, y, z, pyaw, 45, 135, "AND");
 						Constants.setBlock(world, plusx, y, plusz, pyaw, 45, 225, "AND");
 						Constants.setBlock(world, x, y, plusz, pyaw, 135, 225, "AND");
 						Constants.setBlock(world, minusx, y, plusz, pyaw, 135, 315, "AND");
 						Constants.setBlock(world, minusx, y, z, pyaw, 225, 315, "AND");
 						Constants.setBlock(world, minusx, y, minusz, pyaw, 225, 45, "OR");
 						Constants.setBlock(world, x, y, minusz, pyaw, 315, 45, "OR");
 						Constants.setBlock(world, plusx, y, minusz, pyaw, 315, 135, "OR");
 
 						thesecretary = (LivingEntity) player.getWorld().spawnEntity(spawnLoc, et);
 						// change the villager to a librarian
 						Villager villager = (Villager) thesecretary;
 						villager.setProfession(Villager.Profession.LIBRARIAN);
 						// set the No Damage Ticks value
 						int ndticks = plugin.config.getInt("no_damage");
 						if (ndticks > 0) {
 							thesecretary.setNoDamageTicks(ndticks);
 						}
 						secID = thesecretary.getUniqueId();
 						secLocX = thesecretary.getLocation().getX();
 						secLocY = thesecretary.getLocation().getY();
 						secLocZ = thesecretary.getLocation().getZ();
 						secWorld = world.getName();
 						String new_name = Constants.name();
 						plugin.secrets.set(player.getName() + "." + secID + ".name", new_name);
 						plugin.secrets.set(player.getName() + "." + secID + ".sound", "GHAST_SHRIEK");
 						plugin.secrets.set(player.getName() + "." + secID + ".location.world", secWorld);
 						plugin.secrets.set(player.getName() + "." + secID + ".location.x", secLocX);
 						plugin.secrets.set(player.getName() + "." + secID + ".location.y", secLocY);
 						plugin.secrets.set(player.getName() + "." + secID + ".location.z", secLocZ);
 						plugin.PlayerEntityMap.put(player, secID);
 						try {
 							plugin.secrets.save(plugin.secretariesfile);
 						} catch (IOException e) {
 							sender.sendMessage("There was a problem saving the secretary settings!");
 						}
 						sender.sendMessage("The secretary " + ChatColor.AQUA + "'" + new_name + "'" + ChatColor.RESET + " was created and selected successfully!");
 						sender.sendMessage(Constants.INSTRUCTIONS.split("\n"));
 						return true;
 					} else {
 						sender.sendMessage(Constants.NO_PERMS_MESSAGE);
 						return false;
 					}
 				}
 				if (args[0].equals("todo")) {
 					if (player.hasPermission("secretary.todo")) {
 						if (!plugin.PlayerEntityMap.containsKey(player)) {
 							sender.sendMessage("You need to select the secretary with a " + plugin.config.getString("select_material") + " before you can add a todo!");
 							return false;
 						}
 						UUID entID = plugin.PlayerEntityMap.get(player);
 						String configPath = player.getName() + "." + entID;
 						if (!args[1].equals("add") && !args[1].equals("list") && !args[1].equals("mark") && !args[1].equals("delete")) {
 							sender.sendMessage("Do you want to add, list, mark or delete?");
 							return false;
 						}
 						if (args[1].equals("add")) {
 							// check if todo limit has been reached
 							if (plugin.todos.isSet(configPath)) {
 								Set<String> todolist = plugin.todos.getConfigurationSection(configPath).getKeys(false);
 								int size = todolist.size();
 								int limit = plugin.config.getInt("todo_limit");
 								if (size >= limit) {
 									sender.sendMessage("You have reached the maximum allowed number of todos! The limit is " + limit + " per player.");
 									return false;
 								}
 							}
 							if (args.length < 3) {
 								sender.sendMessage("Too few command arguments!");
 								return false;
 							}
 							int count = args.length;
 							String t = "";
 							for (int i = 2; i < count; i++) {
 								t += args[i] + " ";
 							}
 							t = t.substring(0, t.length() - 1);
 							// need to make there are no periods(.) in the text
 							String nodots = StringUtils.replace(t, ".", "_");
 							plugin.todos.set(configPath + "." + nodots + ".status", 0);
 							try {
 								plugin.todos.save(plugin.todofile);
 							} catch (IOException e) {
 								sender.sendMessage("There was a problem saving the todo item!");
 							}
 							sender.sendMessage("The todo was added successfully!");
 							return true;
 						}
 						if (args[1].equals("list")) {
 							Constants.list(plugin.todos, configPath, player, "todos");
 						}
 						if (args[1].equals("mark")) {
 							if (args.length < 3) {
 								sender.sendMessage("Too few command arguments!");
 								return false;
 							}
 							Set<String> todolist = plugin.todos.getConfigurationSection(configPath).getKeys(false);
 							String a = args[2];
 							int val;
 							try {
 								val = Integer.parseInt(a);
 							} catch (NumberFormatException nfe) {
 								// not a number
 								sender.sendMessage("The last argument must be a number!");
 								return false;
 							}
 							int i = 1;
 							for (String str : todolist) {
 								int num = plugin.todos.getInt(configPath + "." + str + ".status");
 								if (i == val) {
 									if (num == 0) {
 										plugin.todos.set(configPath + "." + str + ".status", 1);
 										sender.sendMessage("Item " + i + " marked as done.");
 									} else {
 										plugin.todos.set(configPath + "." + str + ".status", 0);
 										sender.sendMessage("Item " + i + " unmarked.");
 									}
 									try {
 										plugin.todos.save(plugin.todofile);
 									} catch (IOException e) {
 										sender.sendMessage("There was a problem changing the todo status!");
 									}
 									// show the list of todos again
 									Constants.list(plugin.todos, configPath, player, "todos");
 									break;
 								}
 								i++;
 							}
 							return true;
 						}
 						if (args[1].equals("delete")) {
 							if (args.length < 3) {
 								sender.sendMessage("Too few command arguments!");
 								return false;
 							}
 							Set<String> todolist = plugin.todos.getConfigurationSection(configPath).getKeys(false);
 							String a = args[2];
 							int val;
 							try {
 								val = Integer.parseInt(a);
 							} catch (NumberFormatException nfe) {
 								// not a number
 								sender.sendMessage("The last argument must be a number!");
 								return false;
 							}
 							int i = 1;
 							for (String str : todolist) {
 								if (i == val) {
 									plugin.todos.set(configPath + "." + str, null);
 									sender.sendMessage("Item " + i + " deleted.");
 									try {
 										plugin.todos.save(plugin.todofile);
 									} catch (IOException e) {
 										sender.sendMessage("There was a problem changing the todo status!");
 									}
 									// show the list of todos again
 									Constants.list(plugin.todos, configPath, player, "todos");
 									break;
 								}
 								i++;
 							}
 							return true;
 						}
 					} else {
 						sender.sendMessage(Constants.NO_PERMS_MESSAGE);
 						return false;
 					}
 				}
 				if (args[0].equals("remind") || args[0].equals("repeat")) {
 					if (player.hasPermission("secretary.remind")) {
 						if (!plugin.PlayerEntityMap.containsKey(player)) {
 							sender.sendMessage("You need to select the secretary with a " + plugin.config.getString("select_material") + " before you can add a reminder!");
 							return false;
 						}
 						if (args.length < 2) {
 							sender.sendMessage("Too few command arguments!");
 							return false;
 						}
 						UUID entID = plugin.PlayerEntityMap.get(player);
 						String configPath = player.getName() + "." + entID;
 						if (!args[1].equals("add") && !args[1].equals("list") && !args[1].equals("set")) {
 							sender.sendMessage("Do you want to add, list or set?");
 							return false;
 						}
 						if (args[1].equals("add")) {
 							// check if reminder limit has been reached
 							if (plugin.reminds.isSet(configPath)) {
 								Set<String> remindlist = plugin.reminds.getConfigurationSection(configPath).getKeys(false);
 								int size = remindlist.size();
 								int limit = plugin.config.getInt("reminder_limit");
 								if (size >= limit) {
 									sender.sendMessage("You have reached the maximum allowed number of reminders! The limit is " + limit + " per player.");
 									return false;
 								}
 							}
 							if (args.length < 4) {
 								sender.sendMessage("Too few command arguments!");
 								return false;
 							}
 							int count = args.length - 1;
 							String r = "";
 							for (int i = 2; i < count; i++) {
 								r += args[i] + " ";
 							}
 							r = r.substring(0, r.length() - 1);
 							String nodots = StringUtils.replace(r, ".", "_");
 							long currentTime = System.currentTimeMillis();
 							long l;
 							try {
 								l = Long.parseLong(args[count]);
 							} catch (NumberFormatException nfe) {
 								// not a number
 								sender.sendMessage("The last argument must be a number!");
 								return false;
 							}
 							long alarmTime = currentTime + (l * 60000);
 							plugin.reminds.set(configPath + "." + nodots + ".time-set", currentTime);
 							plugin.reminds.set(configPath + "." + nodots + ".alarm", alarmTime);
 							if (args[0].equals("repeat")) {
 								plugin.reminds.set(configPath + "." + nodots + ".repeat", Boolean.valueOf("true"));
 							}
 							try {
 								plugin.reminds.save(plugin.remindersfile);
 							} catch (IOException e) {
 								sender.sendMessage("There was a problem saving the reminder!");
 							}
 							sender.sendMessage("The reminder was added successfully!");
 							return true;
 						}
 						if (args[1].equals("list")) {
 							Constants.list(plugin.reminds, configPath, player, "reminders");
 						}
 						if (args[1].equals("set")) {
 							String a = args[2];
 							int val;
 							try {
 								val = Integer.parseInt(a);
 							} catch (NumberFormatException nfe) {
 								// not a number
 								sender.sendMessage("The last argument must be a number!");
 								return false;
 							}
 							int i = 1;
 							String tf;
 							Set<String> remindlist = plugin.reminds.getConfigurationSection(configPath).getKeys(false);
 							for (String str : remindlist) {
 								if (i == val) {
 									// toggle repeat status
 									if (plugin.reminds.isSet(configPath + "." + str + ".repeat")) {
 										plugin.reminds.set(configPath + "." + str + ".repeat", null);
 										tf = "FALSE";
 									} else {
 										plugin.reminds.set(configPath + "." + str + ".repeat", Boolean.valueOf("true"));
 										tf = "TRUE";
 									}
 									sender.sendMessage("Reminder " + i + " repeat set to " + tf + ".");
 									try {
 										plugin.reminds.save(plugin.remindersfile);
 									} catch (IOException e) {
 										sender.sendMessage("There was a problem changing the repeat status!");
 									}
 									// show the list of todos again
 									Constants.list(plugin.reminds, configPath, player, "reminders");
 									break;
 								}
 								i++;
 							}
 						}
 					} else {
 						sender.sendMessage(Constants.NO_PERMS_MESSAGE);
 						return false;
 					}
 				}
 				if (args[0].equals("delete")) {
					if (player.hasPermission("secretary.todo")) {
 						if (!plugin.PlayerEntityMap.containsKey(player)) {
 							sender.sendMessage("You need to select the secretary with a " + plugin.config.getString("select_material") + " before you can delete it!");
 							return false;
 						} else {
 							String name = "";
 							UUID EntID = plugin.PlayerEntityMap.get(player);
 							String configPath = player.getName() + "." + EntID;
 							world = player.getWorld();
 							Set<String> seclist = plugin.secrets.getConfigurationSection(player.getName()).getKeys(false);
 							Set<String> todolist = plugin.todos.getConfigurationSection(player.getName()).getKeys(false);
 							Set<String> reminderlist = plugin.reminds.getConfigurationSection(player.getName()).getKeys(false);
 							boolean found = false;
 							for (String u : seclist) {
 								if (u.equals(EntID.toString())) {
 									found = true;
 									name = "'" + plugin.secrets.getString(player.getName() + "." + u + ".name") + "'";
 									plugin.secrets.set(configPath, null);
 									// remove the villager
 									List<LivingEntity> elist = world.getLivingEntities();
 									for (LivingEntity l : elist) {
 										if (l.getUniqueId().equals(EntID)) {
 											l.remove();
 										}
 									}
 								}
 							}
 							if (found == false) {
 								sender.sendMessage("Could not find the secretary's config!");
 								return false;
 							} else {
 								for (String t : todolist) {
 									if (t.equals(EntID.toString())) {
 										plugin.todos.set(configPath, null);
 									}
 								}
 								for (String r : todolist) {
 									if (r.equals(EntID.toString())) {
 										plugin.reminds.set(configPath, null);
 									}
 								}
 								try {
 									plugin.PlayerEntityMap.remove(player);
 									plugin.secrets.save(plugin.secretariesfile);
 									plugin.todos.save(plugin.todofile);
 									plugin.reminds.save(plugin.remindersfile);
 								} catch (IOException io) {
 									System.out.println("Could not save the config files!");
 								}
 								sender.sendMessage("The secretary " + name + " was deleted successfully!");
 								return true;
 							}
 						}
 					} else {
 						sender.sendMessage(Constants.NO_PERMS_MESSAGE);
 						return false;
 					}
 				}
 				if (args[0].equals("name")) {
 					if (player.hasPermission("secretary.name")) {
 						if (!plugin.PlayerEntityMap.containsKey(player)) {
 							sender.sendMessage("You need to select the secretary with a " + plugin.config.getString("select_material") + " before you can delete it!");
 							return false;
 						} else {
 							UUID EntID = plugin.PlayerEntityMap.get(player);
 							String configPath = player.getName() + "." + EntID;
 							String name = plugin.secrets.getString(configPath + ".name");
 							sender.sendMessage("The secretary's name is " + ChatColor.RED + name);
 							return true;
 						}
 					} else {
 						sender.sendMessage(Constants.NO_PERMS_MESSAGE);
 						return false;
 					}
 				}
 				if (args[0].equals("setsound")) {
 					if (player.hasPermission("secretary.sound")) {
 						if (!plugin.PlayerEntityMap.containsKey(player)) {
 							sender.sendMessage("You need to select the secretary with a " + plugin.config.getString("select_material") + " before you can delete it!");
 							return false;
 						} else {
 							if (args.length < 2) {
 								sender.sendMessage("Too few command arguments!");
 								return false;
 							}
 							String effect = args[1].toUpperCase();
 
 							// check they typed a valid villager type
 							if (!Arrays.asList(Constants.EFFECT_TYPES).contains(effect)) {
 								sender.sendMessage("Not a valid sound effect name! Type /secretary help setsound");
 								return false;
 							}
 							UUID EntID = plugin.PlayerEntityMap.get(player);
 							String configPath = player.getName() + "." + EntID;
 							plugin.secrets.set(configPath + ".sound", effect);
 							try {
 								plugin.secrets.save(plugin.secretariesfile);
 							} catch (IOException io) {
 								System.out.println("Could not save the secretary file!");
 							}
 							sender.sendMessage("The reminder alarm sound was changed to " + effect);
 							return true;
 						}
 					} else {
 						sender.sendMessage(Constants.NO_PERMS_MESSAGE);
 						return false;
 					}
 				}
 				if (args[0].equals("help")) {
 					if (args.length == 1) {
 						sender.sendMessage(Constants.COMMANDS.split("\n"));
 						return true;
 					}
 					if (args.length == 2) {
 						switch (Constants.fromString(args[1])) {
 							case CREATE:
 								sender.sendMessage(Constants.COMMAND_CREATE.split("\n"));
 								break;
 							case DELETE:
 								sender.sendMessage(Constants.COMMAND_DELETE.split("\n"));
 								break;
 							case TODO:
 								sender.sendMessage(Constants.COMMAND_TODO.split("\n"));
 								break;
 							case REMIND:
 								sender.sendMessage(Constants.COMMAND_REMIND.split("\n"));
 								break;
 							case REPEAT:
 								sender.sendMessage(Constants.COMMAND_REPEAT.split("\n"));
 								break;
 							case SETSOUND:
 								sender.sendMessage(Constants.COMMAND_SOUND.split("\n"));
 								break;
 							case NAME:
 								sender.sendMessage(Constants.COMMAND_NAME.split("\n"));
 								break;
 							case ADMIN:
 								sender.sendMessage(Constants.COMMAND_ADMIN.split("\n"));
 								break;
 							default:
 								sender.sendMessage(Constants.COMMANDS.split("\n"));
 						}
 					}
 					return true;
 				}
 			}
 		}
 		//If the above has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
 		return false;
 	}
 }
