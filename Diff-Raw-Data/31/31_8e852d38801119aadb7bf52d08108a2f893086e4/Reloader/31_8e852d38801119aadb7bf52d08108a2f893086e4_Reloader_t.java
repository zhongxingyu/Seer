 package bukkit.jmgr2007.Reloader;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.command.SimpleCommandMap;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.InvalidDescriptionException;
 import org.bukkit.plugin.InvalidPluginException;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredListener;
 import org.bukkit.plugin.SimplePluginManager;
 import org.bukkit.plugin.UnknownDependencyException;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import bukkit.jmgr2007.Reloader.Metrics.Graph;
 
 public class Reloader extends JavaPlugin {
 	private int [] stats = {0,0,0,0,0};
 	
 	public void onEnable() {
 		Logger log = this.getServer().getLogger();
 		initialConfigCheck();
 		try {
 			startMetrics();
 		} catch (IOException e1) {
 			log.severe("[Reloader] Metrics was unable to start");
 		}
 		if(this.getConfig().getBoolean("timer.autoreload")) {
 			this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 				@Override
 				public void run() {
 					Bukkit.getServer().broadcastMessage("6[2Reloader6] aRunning automatic reload, Please wait for lag to end.");
 					PluginManager pm = Bukkit.getServer().getPluginManager();
 					Plugin[] plugins = pm.getPlugins();
 					for(int i = 0; i < plugins.length; i++) {
 						if(plugins[i].getName() != "Reloader") {
 							try {
 								unload(plugins[i].getName());
 							} catch (NoSuchFieldException e) {
 								e.printStackTrace();
 							} catch (IllegalAccessException e) {
 								e.printStackTrace();
 							}
 							load(plugins[i].getName());
 						}
 					}
 				}
 				
 			}, (((this.getConfig().getLong("timer.hours")*60*60) + (this.getConfig().getLong("timer.minutes")*60) + this.getConfig().getLong("timer.seconds")) * 20), (((this.getConfig().getLong("timer.hours")*60*60) + (this.getConfig().getLong("timer.minutes")*60) + this.getConfig().getLong("timer.seconds")) * 20));
 		}
 	}
 
 	public void onDisable() {
 
 	}
 	
 	private void addStats(String s, int n) {
 		if(s.equalsIgnoreCase("load")) {
 			stats[0] = stats[0] + n;
 		}else if(s.equalsIgnoreCase("unload")) {
 			stats[1] = stats[1] + n;
 		}else if(s.equalsIgnoreCase("enable")) {
 			stats[2] = stats[2] + n;
 		}else if(s.equalsIgnoreCase("disable")) {
 			stats[3] = stats[3] + n;
 		}else if (s.equalsIgnoreCase("reload")) {
 			stats[4] = stats[4] + n;
 		} else {
 		}
 	}
 	
 	private void initialConfigCheck(){
 		getConfig().options().copyDefaults(true);
 		if(!(new File(this.getDataFolder(),"config.yml").exists())){
 			this.getLogger().info("Saving default configuration file.");
 			this.saveDefaultConfig();
 		}
 	}
 
 	private void startMetrics() throws IOException {
 		Metrics metrics;
 		metrics = new Metrics(this);
 		Graph load = metrics.createGraph("Plugins loaded/unloaded");
 		
 		load.addPlotter(new Metrics.Plotter("Plugins Loaded") {
 			
 			@Override
 			public int getValue() {
 				return stats[0];
 			}
 		});
 		
 		load.addPlotter(new Metrics.Plotter("Plugins Unloaded") {
 			
 			@Override
 			public int getValue() {
 				return stats[1];
 			}
 		});
 		
 		Graph enable = metrics.createGraph("Plugins enabled/disabled");
 		
 		enable.addPlotter(new Metrics.Plotter("Plugins Enabled") {
 			
 			@Override
 			public int getValue() {
 				return stats[2];
 			}
 		});
 		
 		enable.addPlotter(new Metrics.Plotter("Plugins Disabled") {
 			
 			@Override
 			public int getValue() {
 				return stats[3];
 			}
 		});
 		
 		Graph reload = metrics.createGraph("Plugins Reloaded");
 		
 		reload.addPlotter(new Metrics.Plotter("Plugins Reloaded") {
 			
 			@Override
 			public int getValue() {
 				return stats[4];
 			}
 		});
 		
 		metrics.start();
 		stats[0] = 0;
 		stats[1] = 0;
 		stats[2] = 0;
 		stats[3] = 0;
 		stats[4] = 0;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 		PluginManager pm = this.getServer().getPluginManager();
 		Logger log = this.getServer().getLogger();
 		if (sender.hasPermission("reloader.reload")) {
 			if (cmd.getName().equalsIgnoreCase("reloader")) {
 				if (args.length == 0) {
 					if (sender instanceof Player) {
 						sender.sendMessage("6----------- cReloader help 6-----------");
 						sender.sendMessage("4/reloader reload <Plugin|all|*> 6-- cReload <Plugin>");
 						sender.sendMessage("4/reloader disable <Plugin|all|*> 6-- cDisable <Plugin>");
 						sender.sendMessage("4/reloader enable <Plugin|all|*> 6-- cEnable <Plugin>");
 						sender.sendMessage("4/reloader load <File> 6-- cLoad <File>");
 						sender.sendMessage("4/reloader unload <File> 6-- cUn-Load <File>");
 						sender.sendMessage("4/reloader check <Plugin> 6-- cCheck whether or not <Plugin> is enabled");
 						sender.sendMessage("4/reloader info <Plugin> 6-- cGives info on <Plugin>");
 						sender.sendMessage("4/reloader use <Plugin> 6-- cGives info on how to use <Plugin>");
 						sender.sendMessage("4/reloader perm [Player] <Permission> 6-- cTells you if you or [Player] has <Permission>");
 						sender.sendMessage("4/reloader list 6-- cList plugins in alphabetical order and sorts them by enabled or disabled");
 					} else {
 						log.info("----------- Reloader help -----------");
 						log.info("reloader reload <plugin|all|*> -- Reload <Plugin>");
 						log.info("reloader disable <Plugin|all|*> -- Disable <Plugin>");
 						log.info("reloader enable <Plugin|all|*> -- Enable <Plugin>");
 						log.info("reloader load <File> -- Load <File>");
 						log.info("reloader unload <File> -- Un-Load <File>");
 						log.info("reloader check <Plugin> -- check whether or not <Plugin> is enabled");
 						log.info("reloader info <Plugin> -- Gives info on <Plugin>");
 						log.info("reloader use <Plugin> -- Gives info on how to use <plugin>");
 						log.info("reloader perm [Player] <Permission> -- Tells you if you or [Player] has <Permission>");
 						log.info("reloader list -- List plugins in alphabetical order and sorts them by enabled or disabled");
 					}
 				} else if (args[0].equalsIgnoreCase("reload")) {
 					if (args[1].equalsIgnoreCase("all")
 							|| args[1].equalsIgnoreCase("*")) {
 						this.getServer().broadcastMessage(
 								"2[Reloader] 4Reloading ALL plugins");
 						Plugin[] plugins = pm.getPlugins();
 						addStats("reload", plugins.length);
 						for(int i = 0; i < plugins.length; i++) {
 							boolean allow = true;
 							for(String ex : this.getConfig().getStringList("exempt")) {
 								String p = ex;
 								if(plugins[i].getName() == p) {
 									allow = false;
 								}
 							}
 							if(plugins[i].getName() != "Reloader" || allow) {
 								try {
 									unload(plugins[i].getName());
 								} catch (NoSuchFieldException | IllegalAccessException e) {
 									e.printStackTrace();
 								}
 								load(plugins[i].getName());
 							}
 						}
 					} else {
 						addStats("reload", 1);
 						for(String ex : this.getConfig().getStringList("exempt")) {
 							Plugin r = pm.getPlugin(ex);
 							if(r != null) {
 								String p = r.getName();
 								if(p.equalsIgnoreCase(args[1])) {
 									sender.sendMessage(ChatColor.RED + "This plugin may not be reloaded, check the config!");
 									return true;
 								}
 							}
 						}
 						Plugin[] plugins = pm.getPlugins();
 						for(int i = 0; i < plugins.length; i++) {
 							if(plugins[i].getName().equalsIgnoreCase(args[1])) {
 								try {
 									unload(plugins[i].getName());
 								} catch (NoSuchFieldException e) {
 									e.printStackTrace();
 								} catch (IllegalAccessException e) {
 									e.printStackTrace();
 								}
 								load(plugins[i].getName(), this.getServer().getPlayer(sender.getName()));
 								sender.sendMessage(ChatColor.GREEN + "Plugin reloaded");
 							}
 						}
 					}
 				} else if (args[0].equalsIgnoreCase("disable")) {
 					if (args.length == 2) {
 						if (args[1].equalsIgnoreCase("all")
 								|| args[1].equalsIgnoreCase("*")) {
 							sender.sendMessage(ChatColor.RED
 									+ "Disabling ALL plugins(except for Reloader)");
 							Plugin[] plugins = pm.getPlugins();
 							addStats("disable", plugins.length);
 							for(int i = 0; i < plugins.length; i++) {
 								if(plugins[i].getName() != "Reloader") {
 									pm.disablePlugin(plugins[i]);
 								}
 								for(String ex : this.getConfig().getStringList("exempt")) {
 									Plugin r = pm.getPlugin(ex);
 									if(r != null) {
 										pm.enablePlugin(r);
 									}
 								}
 							}
 						} else if (args[1].equalsIgnoreCase("Reloader")) {
 							sender.sendMessage(ChatColor.RED
 									+ "You cant disable this plugin");
 						} else {
 							for(String ex : this.getConfig().getStringList("exempt")) {
 							Plugin r = pm.getPlugin(ex);
 							if(r != null) {
 								String p = r.getName();
 								if(p.equalsIgnoreCase(args[1])) {
 									sender.sendMessage(ChatColor.RED + "This plugin may not be disabled or unloaded, check the config!");
 									return true;
 								}
 							}
 						}
 							Plugin[] plugins = pm.getPlugins();
 							for(int i = 0; i < plugins.length; i++) {
 								if(plugins[i].getName().equalsIgnoreCase(args[1])) {
 									pm.disablePlugin(plugins[i]);
 									addStats("disable", 1);
 									sender.sendMessage(ChatColor.GREEN + "Plugin disabled");
 								}
 							}
 						}
 					}
 				} else if (args[0].equalsIgnoreCase("enable")) {
 					if (args.length == 2) {
 						if (args[1].equalsIgnoreCase("all")
 								|| args[1].equalsIgnoreCase("*")) {
 
 							Plugin[] plugins = pm.getPlugins();
 							addStats("enable", plugins.length);
 							for(int i = 0; i < plugins.length; i++) {
 									if(plugins[i].getName() != "Reloader") {
 										pm.enablePlugin(plugins[i]);
 									}
 							}
 							sender.sendMessage(ChatColor.GREEN
 									+ "Enabling all plugins");
 						} else {
 							Plugin[] plugins = pm.getPlugins();
 							for(int i = 0; i < plugins.length; i++) {
 								if(plugins[i].getName().equalsIgnoreCase(args[1])) {
 									pm.enablePlugin(plugins[i]);
 									sender.sendMessage(ChatColor.GREEN + "Plugin enabled");
 									addStats("enable", 1);
 								}
 							}
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Invalid Args");
 					}
 				} else if (args[0].equalsIgnoreCase("load")) {
 					if (args.length == 2) {
 						load(args[1],
 								this.getServer().getPlayer(sender.getName()));
 					} else {
 						sender.sendMessage(ChatColor.RED + "Invalid Args");
 					}
 					sender.sendMessage(ChatColor.GREEN + "Plugin loaded and enabled");
 				} else if (args[0].equalsIgnoreCase("unload")) {
 					for(String ex : this.getConfig().getStringList("exempt")) {
 						Plugin r = pm.getPlugin(ex);
 						if(r != null) {
 							String p = r.getName();
 							if(p.equalsIgnoreCase(args[1])) {
 								sender.sendMessage(ChatColor.RED + "This plugin may not be disabled or unloaded, check the config!");
 								return true;
 							}
 						}
 					}
 					try {
 						Plugin plugin = null;
 						Plugin[] plugins = pm.getPlugins();
 						for(int i = 0; i < plugins.length; i++) {
 							if(plugins[i].getName().equalsIgnoreCase(args[1])) {
 								plugin = plugins[i];
 							}
 						}
 						unload(plugin.getName());
 					} catch (NoSuchFieldException e) {
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						e.printStackTrace();
 					}
 					sender.sendMessage("2Plugin unloaded and disabled");
 				} else if (args[0].equalsIgnoreCase("check")) {
 				Plugin plugin = null;
 				Plugin[] plugins = pm.getPlugins();
 				for(int i = 0; i < plugins.length; i++) {
 					if(plugins[i].getName().equalsIgnoreCase(args[1])) {
 						plugin = plugins[i];
 					}
 				}
 				if(plugin != null) {
 					if(plugin.isEnabled()) {
 						sender.sendMessage("a" + plugin.getName() + " is enabled");
 					} else {
 						sender.sendMessage("c" + plugin.getName() + " Is disabled");
 					}
 				} else {
 					sender.sendMessage("This is not a plugin loaded on this server");
 				}
 			} else if (args[0].equalsIgnoreCase("info")) {
 				Plugin plugin = null;
 				Plugin[] plugins = pm.getPlugins();
 				for(int i = 0; i < plugins.length; i++) {
 					if(plugins[i].getName().equalsIgnoreCase(args[1])) {
 						plugin = plugins[i];
 					}
 				}
 				if(plugin != null) {
 					sender.sendMessage("cPlugin info: a" + plugin.getName());
 					if(plugin.getDescription().getAuthors() != null) {
 						String author = "";
 						List<String> authors = plugin.getDescription().getAuthors();
 						for(int i = 0; i < authors.size(); i++) {
 							if(i == 0)
 								author = authors.get(i);
 							if(i > 1)
 								author = author + ", " + authors.get(i);
 						}
 						sender.sendMessage("cAuthor(s): a" + author);
 					}
 					if(plugin.getDescription().getDescription() != null)
 						sender.sendMessage("cDescription: a" + plugin.getDescription().getDescription());
 					if(plugin.getDescription().getVersion() != null)
 						sender.sendMessage("cVersion: a" + plugin.getDescription().getVersion());
 					if(plugin.getDescription().getWebsite() != null) 
 						sender.sendMessage("cWebsite: a" + plugin.getDescription().getWebsite());
 					if(plugin.getDescription().getDepend() != null) {
 						sender.sendMessage("cRequired plugins");
 							List<String> depends = plugin.getDescription().getDepend();
 							for(int i = 0; i < depends.size(); i++) {
 									sender.sendMessage("c* a" + depends.get(i));
 							}
 						}
 					if(plugin.getDescription().getSoftDepend() != null) {
 						sender.sendMessage("cRequired plugins");
 							List<String> depends = plugin.getDescription().getSoftDepend();
 							for(int i = 0; i < depends.size(); i++) {
 									sender.sendMessage("c* a" + depends.get(i));
 							}
 						}
 					}
 			} else if (args[0].equalsIgnoreCase("use")) {
 				Plugin plugin = null;
 				Plugin[] plugins = pm.getPlugins();
 				for(int i = 0; i < plugins.length; i++) {
 					if(plugins[i].getName().equalsIgnoreCase(args[1])) {
 						plugin = plugins[i];
 					}
 				}
 				ArrayList<String> out = new ArrayList<String>();
 	            ArrayList<String> parsedCommands = new ArrayList<String>();
 	            Map commands = plugin.getDescription().getCommands();
 	
 	            if (commands != null) {
 	                Iterator commandsIt = commands.entrySet().iterator();
 	                while (commandsIt.hasNext()) {
 	                    Map.Entry thisEntry = (Map.Entry) commandsIt.next();
 	                    if (thisEntry != null) {
 	                        parsedCommands.add((String) thisEntry.getKey());
 	                    }
 	                }
 	            }
 	
 	            if (!parsedCommands.isEmpty()) {
 	
 	                StringBuilder commandsOut = new StringBuilder();
 	                sender.sendMessage("cCommands: ");	
 	                for (int i = 0; i < parsedCommands.size(); i++) {
 	
 	                    String thisCommand = parsedCommands.get(i);
 	
 	                    if (commandsOut.length() + thisCommand.length() > 55) {
 	                        sender.sendMessage(commandsOut.toString());
 	                        commandsOut = new StringBuilder();
 	                    }
 	
 	                    if (parsedCommands.size() > 0) {
 	                        sender.sendMessage("c* a/" + thisCommand);
 	                    } else {
 	                        sender.sendMessage("c* a/" + thisCommand);
 	                    }
 	
 	                }
 	
 	                out.add(commandsOut.toString());
 
 					if(plugin.getDescription().getPermissions() != null) {
 						List<Permission> perms = plugin.getDescription().getPermissions();
 						if(perms.size() != 0)
 							sender.sendMessage("cPermissions:");
 						for(int i = 0; i < perms.size(); i++) {
 							sender.sendMessage("c* a" + perms.get(i).getName());
 						}
 					}
 	            }
 			} else if (args[0].equalsIgnoreCase("perm")) {
 				if(args.length == 3) {
 					if(this.getServer().getPlayer(args [1]) != null) {
 						Player target = this.getServer().getPlayer(args [1]);
 						if(target.hasPermission(args[2])) {
 							sender.sendMessage("a" + target.getName() + " has permission " + args[2]);
 						} else {
 							sender.sendMessage("c" + target.getName() + " doesn't have permission " + args[2]);
 						}
 					}
 				} else if(args.length == 2) {
 					if(sender.hasPermission(args[1])) {
 						sender.sendMessage("aYou have permission " + args[1]);
 					} else {
 						sender.sendMessage("cYou don't have permission " + args[1]);
 					}
 				}
 			} else if (args[0].equalsIgnoreCase("list")) {
 				ArrayList<String> enabled = new ArrayList<String>();
 				ArrayList<String> disabled = new ArrayList<String>();
 				for(int i = 0; i < pm.getPlugins().length; i++) {
 					Plugin[] plugins = pm.getPlugins();
 					if(plugins[i].isEnabled())
 						enabled.add(plugins[i].getName());
 					else
 						disabled.add(plugins[i].getName());
 				}
 				Collections.sort(enabled,  String.CASE_INSENSITIVE_ORDER);
 				Collections.sort(disabled,  String.CASE_INSENSITIVE_ORDER);
 				if(enabled.isEmpty()) {
 					
 				} else {
 					sender.sendMessage("6Enabled:");
 					String enable = "";
 					for(int i = 0; i < enabled.size(); i++) {
 						enable = enable + ", "  + enabled.get(i);
 					}
 					enable = enable.replaceFirst(", ", "");
 					sender.sendMessage("a" + enable);
 				}
 				if(disabled.isEmpty()) {
 					
 				} else {
 					String disable = "";
 					sender.sendMessage("6Disabled:");
 					for(int i = 0; i < disabled.size(); i++) {
 						disable = disable + ", " + disabled.get(i);
 					}
 					disable = disable.replaceFirst(", ", "");
 					sender.sendMessage("c" + disable);
 				}
 			} else {
 					sender.sendMessage(ChatColor.RED + "Invalid Args");
 			}
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED + "Invalid permissions");
 		}
 		return true;
 	}
 
 	private void load(final String pluginName) {
 		PluginManager pm = this.getServer().getPluginManager();
 		String name = "";
 		String pname = "";
 		if (pluginName.endsWith(".jar") || pluginName.endsWith(".Jar")
 				|| pluginName.endsWith(".JAr") || pluginName.endsWith(".JaR")
 				|| pluginName.endsWith(".JAR")) {
 			name = name.replaceAll(".jar", "");
 			name = name.replaceAll(".Jar", "");
 			name = name.replaceAll(".jAr", "");
 			name = name.replaceAll(".jaR", "");
 			name = name.replaceAll(".JAr", "");
 			name = name.replaceAll(".jAR", "");
 			name = name.replaceAll(".JaR", "");
 			name = name.replaceAll(".JAR", "");
 			name = pluginName;
 		} else {
 			name = pluginName;
 		}
 		String path = "./plugins";
 		String files;
 		File folder = new File(path);
 		File[] listOfFiles = folder.listFiles();
 		for (int i = 0; i < listOfFiles.length; i++) {
 			if (listOfFiles[i].isFile()) {
 				files = listOfFiles[i].getName();
 				int num = i;
 				if (files.toLowerCase().startsWith(name.toLowerCase())) {
 					try {
 						pm.loadPlugin(listOfFiles[num]);
 						pname = listOfFiles[num].getName();
 						pname = pname.replaceAll(".jar", "");
 						addStats("load", 1);
 					} catch (UnknownDependencyException
 							| InvalidPluginException
 							| InvalidDescriptionException e) {
 					}
 				}
 			}
 		}
 		Plugin[] plugins = pm.getPlugins();
 		for(int i = 0; i < plugins.length; i++) {
 			if(plugins[i].getName().equalsIgnoreCase(pname)) {
 				pm.enablePlugin(plugins[i]);
 				addStats("enable", 1);
 			}
 		}
 	}
 
 	private void load(final String pluginName, Player sender) {
 		PluginManager pm = this.getServer().getPluginManager();
 		String name = "";
 		String pname = "";
 		if (pluginName.endsWith(".jar") || pluginName.endsWith(".Jar")
 				|| pluginName.endsWith(".JAr") || pluginName.endsWith(".JaR")
 				|| pluginName.endsWith(".JAR")) {
 			name = pluginName;
 		}
 		name = pluginName;
 		String path = "./plugins";
 		String files;
 		File folder = new File(path);
 		File[] listOfFiles = folder.listFiles();
 		for (int i = 0; i < listOfFiles.length; i++) {
 			if (listOfFiles[i].isFile()) {
 				files = listOfFiles[i].getName();
 				int num = i;
 				if (files.toLowerCase().startsWith(name.toLowerCase())) {
 					try {
 						pm.loadPlugin(listOfFiles[num]);
 						pname = listOfFiles[num].getName();
 						pname = pname.replaceAll(".jar", "");
 						addStats("load", 1);
 					} catch (UnknownDependencyException
 							| InvalidPluginException
 							| InvalidDescriptionException e) {
 						sender.sendMessage(ChatColor.RED + "Not a plugin file OR has an underlying problem");
 					}
 				}
 			}
 		}
 		Plugin[] plugins = pm.getPlugins();
 		for(int i = 0; i < plugins.length; i++) {
 			if(plugins[i].getName().equalsIgnoreCase(pname)) {
 				pm.enablePlugin(plugins[i]);
 				addStats("enable", 1);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void unload(final String pluginName)
 			throws NoSuchFieldException, IllegalAccessException {
 		PluginManager manager = getServer().getPluginManager();
 		SimplePluginManager spm = (SimplePluginManager) manager;
 		SimpleCommandMap commandMap = null;
 		List<Plugin> plugins = null;
 		Map<String, Plugin> lookupNames = null;
 		Map<String, Command> knownCommands = null;
 		Map<Event, SortedSet<RegisteredListener>> listeners = null;
 		boolean reloadlisteners = true;
 
 		if (spm != null) {
 			Field pluginsField = spm.getClass().getDeclaredField("plugins");
 			pluginsField.setAccessible(true);
 			plugins = (List<Plugin>) pluginsField.get(spm);
 
 			Field lookupNamesField = spm.getClass().getDeclaredField(
 					"lookupNames");
 			lookupNamesField.setAccessible(true);
 			lookupNames = (Map<String, Plugin>) lookupNamesField.get(spm);
 
 			try {
 				Field listenersField = spm.getClass().getDeclaredField(
 						"listeners");
 				listenersField.setAccessible(true);
 				listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField
 						.get(spm);
 			} catch (Exception e) {
 				reloadlisteners = false;
 			}
 
 			Field commandMapField = spm.getClass().getDeclaredField(
 					"commandMap");
 			commandMapField.setAccessible(true);
 			commandMap = (SimpleCommandMap) commandMapField.get(spm);
 
 			Field knownCommandsField = commandMap.getClass().getDeclaredField(
 					"knownCommands");
 			knownCommandsField.setAccessible(true);
 			knownCommands = (Map<String, Command>) knownCommandsField
 					.get(commandMap);
 		}
 
 		for (Plugin pl : getServer().getPluginManager().getPlugins()) {
 			if (pl.getDescription().getName().equalsIgnoreCase(pluginName)) {
 				manager.disablePlugin(pl);
 				if (plugins != null && plugins.contains(pl)) {
 					plugins.remove(pl);
 					addStats("unload", 1);
 					addStats("disable", 1);
 				}
 
 				if (lookupNames != null && lookupNames.containsKey(pluginName)) {
 					lookupNames.remove(pluginName);
 				}
 
 				if (listeners != null && reloadlisteners) {
 					for (SortedSet<RegisteredListener> set : listeners.values()) {
 						for (Iterator<RegisteredListener> it = set.iterator(); it
 								.hasNext();) {
 							RegisteredListener value = it.next();
 
 							if (value.getPlugin() == pl) {
 								it.remove();
 							}
 						}
 					}
 				}
 
 				if (commandMap != null) {
 					for (Iterator<Map.Entry<String, Command>> it = knownCommands
 							.entrySet().iterator(); it.hasNext();) {
 						Map.Entry<String, Command> entry = it.next();
 						if (entry.getValue() instanceof PluginCommand) {
 							PluginCommand c = (PluginCommand) entry.getValue();
 							if (c.getPlugin() == pl) {
 								c.unregister(commandMap);
 								it.remove();
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 }
