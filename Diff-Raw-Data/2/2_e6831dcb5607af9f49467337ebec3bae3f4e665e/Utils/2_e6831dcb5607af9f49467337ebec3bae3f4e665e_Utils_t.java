 package me.jmgr2007.Reloader;
 
 import java.io.File;
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
 
 public class Utils {
 	private static PluginManager pm = Bukkit.getServer().getPluginManager();
 	private static boolean canceled;
 	
 	public Utils(String name, CommandSender sender) {
 		if(exempt(name))
 			msg(sender, ChatColor.RED + "This plugin is exempt");
 		canceled = exempt(name);
 	}
 
 	public Utils(String name) {
 		canceled = exempt(name);
 	}
 
     public static void load(final String pluginName) {
         PluginManager pm = Bukkit.getServer().getPluginManager();
         
         boolean there = false;
         
         for(Plugin pl : pm.getPlugins())
         	if(pl.getName().toLowerCase().startsWith(pluginName))
         		there = true;
         
         if(there) {
         	System.out.print("Plugin already enabled");
         	return;
         } else {
 	        String name = "";
 	        String path = pm.getPlugin("Reloader").getDataFolder().getParent();
 	        File folder = new File(path);
 	        ArrayList<File> files = new ArrayList<File>();
 	        File[] listOfFiles = folder.listFiles();
 	        for (File compare : listOfFiles) {
 	            if (compare.isFile()) {
 	            	try {
 						name = ReloaderListener.plugin.getPluginLoader().getPluginDescription(compare).getName();
 					} catch (InvalidDescriptionException e) {
 						System.out.print(compare.getName() + "didn't match");
 					}
 	            	if(name.toLowerCase().startsWith(pluginName.toLowerCase())) {
 	            		files.add(compare);
 	            		try {
 							Bukkit.getServer().getPluginManager().loadPlugin(compare);
 						} catch (UnknownDependencyException e) {
 							System.out.print(compare.getName() + "is missing a dependant plugin");
 						} catch (InvalidPluginException e) {
 							System.out.print(compare.getName() + "is not a plugin");
 						} catch (InvalidDescriptionException e) {
 							System.out.print(compare.getName() + "has an incorrect description");
 						}
 	            	}
 	            }
 	        }
 	        
 	        Plugin[] plugins = pm.getPlugins();
 	        for(Plugin pl : plugins) {
 	        	for(File compare : files) {
 	        		try {
 						if(pl.getName().equalsIgnoreCase(ReloaderListener.plugin.getPluginLoader().getPluginDescription(compare).getName())) {
 						    pm.enablePlugin(pl);
 						    Vars.enabled.increment();
 						}
 					} catch (InvalidDescriptionException e) {
 						e.printStackTrace();
 					}
 	        	}
 	        }
         }
         return;
     }
 
     public static void load(final String pluginName, CommandSender sender) {
         PluginManager pm = Bukkit.getServer().getPluginManager();
         
         boolean there = false;
         
         for(Plugin pl : pm.getPlugins())
         	if(pl.getName().toLowerCase().startsWith(pluginName))
         		there = true;
         
         if(there) {
         	System.out.print("Plugin already enabled");
         	return;
         } else {
 	        String name = "";
 	        String path = ReloaderListener.plugin.getDataFolder().getParent();
 	        File folder = new File(path);
 	        ArrayList<File> files = new ArrayList<File>();
 	        File[] listOfFiles = folder.listFiles();
 	        for (File compare : listOfFiles) {
	            if (compare.isFile() && compare.getName().endsWith(".jar")) {
 	            	try {
 						name = ReloaderListener.plugin.getPluginLoader().getPluginDescription(compare).getName();
 					} catch (InvalidDescriptionException e) {
 						System.out.print(compare.getName() + " has an incorect description");
 					}
 	            	if(name.toLowerCase().startsWith(pluginName.toLowerCase())) {
 	            		files.add(compare);
 	            		try {
 							Bukkit.getServer().getPluginManager().loadPlugin(compare);
 						} catch (UnknownDependencyException e) {
 							System.out.print(compare.getName() + "is missing a dependant plugin");
 						} catch (InvalidPluginException e) {
 							System.out.print(compare.getName() + "is not a plugin");
 						} catch (InvalidDescriptionException e) {
 							System.out.print(compare.getName() + " has an incorrect description");
 						}
 	            	}
 	            }
 	        }
 	        
 	        Plugin[] plugins = pm.getPlugins();
 	        for(Plugin pl : plugins) {
 	        	for(File compare : files) {
 	        		try {
 						if(pl.getName().equalsIgnoreCase(ReloaderListener.plugin.getPluginLoader().getPluginDescription(compare).getName())) {
 						    pm.enablePlugin(pl);
 						    Vars.enabled.increment();
 						}
 					} catch (InvalidDescriptionException e) {
 						e.printStackTrace();
 					}
 	        	}
 	        }
         }
         return;
     }
     
     public static boolean fload(final String pluginName, CommandSender sender) {
         PluginManager pm = Bukkit.getServer().getPluginManager();
         String name = "";
         String pname = "";
         if (pluginName.toLowerCase().endsWith(".jar")) {
             name = pluginName.replaceAll(".jar", "");
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
                         Vars.loaded.increment();
                     } catch (UnknownDependencyException e) {
                         sender.sendMessage(ChatColor.RED + "Not a plugin file OR has an underlying problem");
                     } catch (InvalidPluginException e) {
                         sender.sendMessage(ChatColor.RED + "Not a plugin file OR has an underlying problem");
                     } catch (InvalidDescriptionException e) {
                         sender.sendMessage(ChatColor.RED + "Not a plugin file OR has an underlying problem");
                     }
                 }
             }
         }
         Plugin[] plugins = pm.getPlugins();
         for(int i = 0; i < plugins.length; i++) {
             if(plugins[i].getName().equalsIgnoreCase(pname)) {
                 pm.enablePlugin(plugins[i]);
                 Vars.enabled.increment();
             }
         }
         return true;
     }
 
     @SuppressWarnings("unchecked")
     public static void unload(final String pluginName) {
     	
     	if(canceled)
     		return;
     	
         PluginManager manager = Bukkit.getServer().getPluginManager();
         SimplePluginManager spm = (SimplePluginManager) manager;
         SimpleCommandMap commandMap = null;
         List<Plugin> plugins = null;
         Map<String, Plugin> lookupNames = null;
         Map<String, Command> knownCommands = null;
         Map<Event, SortedSet<RegisteredListener>> listeners = null;
         boolean reloadlisteners = true;
         try {
 	        if (spm != null) {
 	            Field pluginsField = spm.getClass().getDeclaredField("plugins");
 	            pluginsField.setAccessible(true);
 	            plugins = (List<Plugin>) pluginsField.get(spm);
 	
 	            Field lookupNamesField = spm.getClass().getDeclaredField("lookupNames");
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
     	} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (NoSuchFieldException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
         boolean in = false;
 
         for (Plugin pl : Bukkit.getServer().getPluginManager().getPlugins()) {
             if (pl.getName().toLowerCase().contains(pluginName.toLowerCase())) {
                 manager.disablePlugin(pl);
                 if (plugins != null && plugins.contains(pl)) {
                     plugins.remove(pl);
                     Vars.unloaded.increment();
                     Vars.disabled.increment();
                 }
 
                 if (lookupNames != null && lookupNames.containsKey(pl.getName())) {
                     lookupNames.remove(pl.getName());
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
 			    for (Plugin plu : Bukkit.getServer().getPluginManager().getPlugins()) {
 			        if(plu.getDescription().getDepend() != null) {
 				        for (String depend : plu.getDescription().getDepend()) {
 				        	if(depend.equalsIgnoreCase(pl.getName())) {
 				        		Utils.unload(plu.getName());
 				        	}
 				        }
 			        }
 			    }
 			    in = true;
 		    }
 	    }
         if(!in) {
         	Bukkit.getLogger().info("Not an existing plugin");
         }
         System.gc();
         return;
     }
 
     public static void disable(String plugin, CommandSender sender) {
     	if(canceled)
     		return;
     	Plugin [] plugins = pm.getPlugins();
     	for(Plugin pl : plugins) {
             if(pl.getName().toLowerCase().startsWith(plugin.toLowerCase())) {
                 pm.disablePlugin(pl);
                 Vars.disabled.increment();
             }
         }
         return;
     }
 
     public static void hReload() {
     	for(Plugin pl : pm.getPlugins()) {
     		Utils.unload(pl.getName());
     		for(File fl : new File(pl.getDataFolder().getParent()).listFiles()) {
     			try {
 					pm.loadPlugin(fl);
 				} catch (InvalidDescriptionException e) {
 				} catch (UnknownDependencyException e) {
 					e.printStackTrace();
 				} catch (InvalidPluginException e) {
 				}
     		}
     	}
     }
 
     public static boolean enable(String plugin, CommandSender sender) {
         Plugin[] plugins = pm.getPlugins();
         for(Plugin pl : plugins) {
             if(pl.getName().toLowerCase().startsWith(plugin.toLowerCase())) {
                 pm.enablePlugin(pl);
                 Vars.enabled.increment();
             }
         }
         return true;
     }
 
     @SuppressWarnings("rawtypes")
 	public static boolean use(String plugin, CommandSender sender) {
         Plugin plug = null;
         Plugin [] plugins = pm.getPlugins();
         for(Plugin pl : plugins) {
             if(pl.getName().toLowerCase().startsWith(plugin.toLowerCase())) {
                 plug = pl;
             }
         }
         ArrayList<String> out = new ArrayList<String>();
         ArrayList<String> parsedCommands = new ArrayList<String>();
         Map commands = plug.getDescription().getCommands();
 
         if (commands != null) {
             Iterator commandsIt = commands.entrySet().iterator();
             while (commandsIt.hasNext()) {
                 Map.Entry pluginEntry = (Map.Entry) commandsIt.next();
                 if (pluginEntry != null) {
                     parsedCommands.add((String) pluginEntry.getKey());
                 }
             }
         }
 
         if (!parsedCommands.isEmpty()) {
 
             StringBuilder commandsOut = new StringBuilder();
             sender.sendMessage("cCommands: ");    
             for (int i = 0; i < parsedCommands.size(); i++) {
 
                 String pluginCommand = parsedCommands.get(i);
 
                 if (commandsOut.length() + pluginCommand.length() > 55) {
                     sender.sendMessage(commandsOut.toString());
                     commandsOut = new StringBuilder();
                 }
 
                 if (parsedCommands.size() > 0) {
                     sender.sendMessage("c* a/" + pluginCommand);
                 } else {
                     sender.sendMessage("c* a/" + pluginCommand);
                 }
 
             }
 
             out.add(commandsOut.toString());
 
             if(plug.getDescription().getPermissions() != null) {
                 List<Permission> perms = plug.getDescription().getPermissions();
                 if(perms.size() != 0)
                     sender.sendMessage("cPermissions:");
                 for(int i = 0; i < perms.size(); i++) {
                     sender.sendMessage("c* a" + perms.get(i).getName());
                 }
             }
         }
         return true;
     }
 
     public static boolean info(String plugin, CommandSender sender) {
     	
     	if(canceled)
     		return canceled;
     	
         Plugin plug = null;
         Plugin [] plugins = pm.getPlugins();
         for(Plugin pl : plugins) {
             if(pl.getName().toLowerCase().startsWith(plugin.toLowerCase())) {
                 plug = pl;
             }
         }
         if(plugin != null) {
             sender.sendMessage("cPlugin info: a" + plug.getName());
             if(plug.getDescription().getAuthors() != null) {
                 String author = "";
                 List<String> authors = plug.getDescription().getAuthors();
                 for(int i = 0; i < authors.size(); i++) {
                     if(i == 0)
                         author = authors.get(i);
                     if(i > 1)
                         author = author + ", " + authors.get(i);
                 }
                 sender.sendMessage("cAuthor(s): a" + author);
             }
             if(plug.getDescription().getDescription() != null)
                 sender.sendMessage("cDescription: a" + plug.getDescription().getDescription());
             if(plug.getDescription().getVersion() != null)
                 sender.sendMessage("cVersion: a" + plug.getDescription().getVersion());
             if(plug.getDescription().getWebsite() != null) 
                 sender.sendMessage("cWebsite: a" + plug.getDescription().getWebsite());
             if(plug.getDescription().getDepend() != null) {
                 sender.sendMessage("cRequired plugins");
                     List<String> depends = plug.getDescription().getDepend();
                     for(int i = 0; i < depends.size(); i++) {
                             sender.sendMessage("c* a" + depends.get(i));
                     }
                 }
             if(plug.getDescription().getSoftDepend() != null) {
                 sender.sendMessage("cPreffered plugins");
                     List<String> depends = plug.getDescription().getSoftDepend();
                     for(int i = 0; i < depends.size(); i++) {
                             sender.sendMessage("c* a" + depends.get(i));
                     }
                 }
             }
         	return true;
     }
 
     public static boolean check(String plugin, CommandSender sender) {
         Plugin plug = null;
         Plugin [] plugins = pm.getPlugins();
         for(Plugin pl : plugins) {
             if(pl.getName().toLowerCase().startsWith(plugin.toLowerCase())) {
                 plug = pl;
             }
         }
         if(plug != null) {
             if(plug.isEnabled()) {
                 sender.sendMessage("a" + plug.getName() + " is enabled");
             } else {
                 sender.sendMessage("c" + plug.getName() + " Is disabled");
             }
             return true;
         } else {
             sender.sendMessage("This is not a plugin loaded on plugin server");
             return true;
         }
     }
 
     public static boolean perm(CommandSender sender, String permission) {
         if(sender.hasPermission(permission)) {
             sender.sendMessage("aYou have permission " + permission);
         } else {
             sender.sendMessage("cYou don't have permission " + permission);
         }
         return true;
     }
     
     public static boolean perm(String player, CommandSender sender, String permission) {
     	if(Bukkit.getServer().getPlayer(player) != null) {
             Player target = Bukkit.getServer().getPlayer(player);
 	        if(target.hasPermission(permission)) {
 	            sender.sendMessage("a" + target.getName() + " has permission " + permission);
 	        } else {
 	            sender.sendMessage("c" + target.getName() + " doesn't have permission " + permission);
 	        }
     	}
         return true;
     }
     
     public static boolean list(CommandSender sender) {
         Plugin[] plugins = pm.getPlugins();
         ArrayList<String> enabled = new ArrayList<String>();
         ArrayList<String> disabled = new ArrayList<String>();
         for(Plugin pl : plugins) {
             if(pl.isEnabled())
                 enabled.add(pl.getName());
             else
                 disabled.add(pl.getName());
         }
         Collections.sort(enabled,  String.CASE_INSENSITIVE_ORDER);
         Collections.sort(disabled,  String.CASE_INSENSITIVE_ORDER);
         if(plugins.length != 0)
         	sender.sendMessage("a" + plugins.length + " plugins loaded");
         if(!enabled.isEmpty()) {
             sender.sendMessage("6Enabled:");
             String enable = "";
             for(int i = 0; i < enabled.size(); i++) {
                 enable = enable + ", "  + enabled.get(i);
             }
             enable = enable.replaceFirst(", ", "");
             sender.sendMessage("a" + enable);
         }
         if(!disabled.isEmpty()) {
             String disable = "";
             sender.sendMessage("6Disabled:");
             for(int i = 0; i < disabled.size(); i++) {
                 disable = disable + ", " + disabled.get(i);
             }
             disable = disable.replaceFirst(", ", "");
             sender.sendMessage("c" + disable);
         }
         return true;
     }
     
     public static void msg(CommandSender sender, String msg) {
     	sender.sendMessage(msg);
     }
     
     public static boolean help(CommandSender sender) {
         Logger log = Bukkit.getServer().getLogger();
         if (sender instanceof Player) {
             sender.sendMessage("6----------- cReloader help 6-----------");
             sender.sendMessage("4/reloader reload <Plugin|all|*|harsh> 6-- cReload <Plugin>/reload all plugins in the server/reload plugins and load new one");
             sender.sendMessage("4/reloader disable <Plugin|all|*> 6-- cDisable <Plugin>");
             sender.sendMessage("4/reloader enable <Plugin|all|*> 6-- cEnable <Plugin>");
             sender.sendMessage("4/reloader load <File> 6-- cLoad <File>");
             sender.sendMessage("4/reloader unload <File> 6-- cUn-Load <File>");
             sender.sendMessage("4/reloader check <Plugin> 6-- cCheck whether or not <Plugin> is enabled");
             sender.sendMessage("4/reloader info <Plugin> 6-- cGives info on <Plugin>");
             sender.sendMessage("4/reloader use <Plugin> 6-- cGives info on how to use <Plugin>");
             sender.sendMessage("4/reloader perm [Player] <Permission> 6-- cTells you if you or [Player] has <Permission>");
             sender.sendMessage("4/reloader list 6-- cList plugins in alphabetical order and sorts them by enabled or disabled");
             sender.sendMessage("4/reloader config [plugin] 6-- cReload [plugin]'s config or leave blank to reload Reloader's config");
         } else {
             log.info("----------- Reloader help -----------");
             log.info("reloader reload <plugin|all|*|harsh> -- Reload <Plugin>/reload all plugins in the server/reload plugins and load new ones");
             log.info("reloader disable <Plugin|all|*> -- Disable <Plugin>");
             log.info("reloader enable <Plugin|all|*> -- Enable <Plugin>");
             log.info("reloader load <File> -- Load <File>");
             log.info("reloader unload <File> -- Un-Load <File>");
             log.info("reloader check <Plugin> -- check whether or not <Plugin> is enabled");
             log.info("reloader info <Plugin> -- Gives info on <Plugin>");
             log.info("reloader use <Plugin> -- Gives info on how to use <plugin>");
             log.info("reloader perm [Player] <Permission> -- Tells you if you or [Player] has <Permission>");
             log.info("reloader list -- List plugins in alphabetical order and sorts them by enabled or disabled");
             log.info("/reloader config [plugin] -- Reload [plugin]'s config or leave blank to reload Reloader's config");
         }
         return true;
     }
     
     public static boolean exempt(String name) {
     	for(String ex : Bukkit.getPluginManager().getPlugin("Reloader").getConfig().getStringList("exempt")) {
     		if(name.equalsIgnoreCase(ex))
     			return true;
         }
     	return false;
     }
     
     public static String join(String [] args) {
     	String l = args[1];
     	for(int i = 2; i < args.length; i++) {
     		l = l + " " + args[i];
     	}
     	l = l.trim();
 		return l;
     }
 }
