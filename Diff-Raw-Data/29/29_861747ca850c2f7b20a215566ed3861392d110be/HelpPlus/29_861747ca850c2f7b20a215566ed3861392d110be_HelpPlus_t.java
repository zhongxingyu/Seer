 ﻿/*
  * This file is part of HelpPlus. HelpPlus is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any later version. HelpPlus is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received
  * a copy of the GNU General Public License along with HelpPlus. If not, see <http://www.gnu.org/licenses/>.
  */
 package sk.tomsik68.helpplus;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.command.PluginCommandYamlParser;
 import org.bukkit.command.defaults.VanillaCommand;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import sk.tomsik68.permsguru.EPermissions;
 
 /**
  * HelpPlus for Bukkit
  * 
  * @author Tomsik68
  */
 public class HelpPlus extends JavaPlugin {
     public static int commandsPerPage = 7;

     private ChatColor def = ChatColor.BLUE;
    // this color is used on more places...
     public ChatColor def1 = ChatColor.GOLD;
     private ChatColor def2 = ChatColor.GREEN;
     private boolean showPlugin = true;
     private EPermissions perms;
     private FileConfiguration config;
     private boolean permissedHelp;
     public static Logger log;
 
     public HelpPlus() {
         super();
     }
 
     @Override
     public void onEnable() {
         log = getLogger();
         log.info("Enabling...");
         getCommand("help").setExecutor(this);
         getCommand("hp").setExecutor(this);
         getCommand("h+").setExecutor(this);
         getCommand("hplisting").setExecutor(this);
         System.out.println("Checking DB...");
         try {
             getDatabase().find(CommandInfo.class).findRowCount();
             log.info("DB is OK");
         } catch (Exception e) {
             try {
                 log.info("Installing DB due to first time usage...");
                 installDDL();
                 log.info("Database setup successful!");
             } catch (Exception e1) {
                 log.info("Can't install database. Since this is critical error, plugin will now disable itself. I'm sorry for inconvenience.");
                 e1.printStackTrace();
                 getServer().getPluginManager().disablePlugin(this);
             }
         }
         if (new File(getDataFolder(), "config.yml").exists()) {
             config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
             perms = EPermissions.parse(config.getString("perms", "SP"));
             commandsPerPage = config.getInt("cmds-on-page");
             def = ChatColor.valueOf(config.getString("colors.a").toUpperCase());
             def1 = ChatColor.valueOf(config.getString("colors.b").toUpperCase());
             def2 = ChatColor.valueOf(config.getString("colors.c").toUpperCase());
             showPlugin = config.getBoolean("show.plugin");
             permissedHelp = config.getBoolean("help.perm", false);
             log.info("Found config file. Loading overrriden commands...");
             getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
                 public void run() {
                     try {
                         HashSet<String> overridenCommands = new HashSet<String>(config.getConfigurationSection("commands").getKeys(false));
                         for (String name : overridenCommands) {
                             ConfigurationSection cs = config.getConfigurationSection("commands." + name);
                             CommandInfo ci = new CommandInfo(name, cs.getString("usage"), cs.getString("description"), null, cs.getString("permission"), cs.getString("plugin", "<unknown>"));
                             addCommand(ci);
                         }
                         log.info("[HelpPlus] Overriden commands loaded.");
                         log.info("[HelpPlus] Finally enabled!");
                     } catch (NullPointerException n) {
                         log.info("[HelpPlus] No overriden commands were found.");
                     }
                     try {
                         Class.forName("org.bukkit.craftbukkit.CraftServer");
                         Map<String, Command> commands = getField(getField(getServer(), "commandMap"), "knownCommands");
                         Set<VanillaCommand> vanillaCommands = getField(getField(getServer(), "commandMap"), "fallbackCommands");
                         FakePlayer fakie = new FakePlayer();
                         for (Entry<String, Command> entry : commands.entrySet()) {
                             if (commandExists(entry.getKey()))
                                 continue;
                             if (entry.getValue() instanceof PluginCommand) {
                                 addCommand(new CommandInfo((PluginCommand) entry.getValue()));
                             } else {
                                 addCommand(new CommandInfo(entry.getKey(), entry.getValue().getUsage().replaceAll("<command>", entry.getValue().getName()), entry.getValue().getDescription(), entry.getValue().getAliases().toArray(new String[0]), entry.getValue().getPermission(), "<unknown>"));
                             }
                             if (entry.getValue().getPermission() == null || entry.getValue().getPermission().length() == 0) {
                                try {
                                    Bukkit.dispatchCommand(fakie, entry.getValue().getLabel());
                                } catch (CommandException ce) {
                                    // failed to resolve permission for
                                    // entry.getValue()
                                }
                                 if (fakie.getPermissionsUsed().size() > 0) {
                                     entry.getValue().setPermission(fakie.getPermissionsUsed().get(0));
                                     fakie.getPermissionsUsed().clear();
                                 }
                             }

                         }
                         for (VanillaCommand vc : vanillaCommands) {
                             addCommand(new CommandInfo(vc.getName(), vc.getUsage(), vc.getDescription(), vc.getAliases().toArray(new String[0]), vc.getPermission(), "bukkit"));
                         }
                     } catch (Exception e) {
                         System.out.println("[HelpPlus] CommandMap hooking failed. Probably not a craftbukkit server. Falling back to an API function.");
                         System.out.println("[HelpPlus] Error trace: ");
                         e.printStackTrace();
                         for (Plugin plug : getServer().getPluginManager().getPlugins()) {
                             List<Command> plugComms = PluginCommandYamlParser.parse(plug);
                             for (Command c : plugComms) {
                                 CommandInfo ci = new CommandInfo((PluginCommand) c);
                                 addCommand(ci);
                             }
                         }
                     }
                     for (CommandInfo ci : getAllCommands()) {
                         if (config.contains("commands." + ci.getName()))
                             continue;
                         config.set("commands." + ci.getName() + ".description", ci.getDescription());
                         config.set("commands." + ci.getName() + ".usage", ci.usgae);
                         config.set("commands." + ci.getName() + ".aliases", ci.getAliases());
                         config.set("commands." + ci.getName() + ".permission", ci.getPermission());
                         config.set("commands." + ci.getName() + ".plugin", ci.getPlugin());
                     }
                     try {
                         config.save(new File(getDataFolder(), "config.yml"));
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             });
         } else {
             log.info("[HelpPlus] Config file not found. Creating a new one...");
             getDataFolder().mkdir();
             try {
                 new File(getDataFolder(), "config.yml").createNewFile();
                 config = new YamlConfiguration();
                 config.set("perms", "SP");
                 config.set("cmds-on-page", 7);
                 config.set("colors.a", ChatColor.BLUE.name());
                 config.set("colors.b", ChatColor.GOLD.name());
                 config.set("colors.c", ChatColor.GREEN.name());
                 config.set("show.plugin", true);
                 config.save(new File(getDataFolder(), "config.yml"));
                 perms = EPermissions.SP;
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
     }
 
     @Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(CommandInfo.class);
         return list;
     }
 
     @Override
     public void onDisable() {
         log.info("[HelpPlus] Disabled");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] cmd) {
        if (command.getName().equalsIgnoreCase("hplisting")) {
             if (!perms.has(sender, "helpplus.listing")) {
                 sender.sendMessage(command.getPermissionMessage());
                 return true;
             }
            sender.sendMessage(def1 + "[HelpPlus] Creating your listing please wait...");
             getServer().getScheduler().scheduleAsyncDelayedTask(this, new ListingRunnable(sender));
             return true;
         }
         if (!perms.has(sender, "helpplus.help") && permissedHelp) {
             sender.sendMessage(command.getPermissionMessage());
             return true;
         }
         List<CommandInfo> commands = getAllCommands();
         int commandsOnPage = 0;
         if (cmd.length == 0) {
             sender.sendMessage(def1 + "[HelpPlus] Available Commands Page 1 of " + Math.round(commands.size() / HelpPlus.commandsPerPage));
             for (int i = 0; commandsOnPage != HelpPlus.commandsPerPage && i < commands.size(); i++) {
                 if (isCommandDisplayed(sender, commands.get(i))) {
                     CommandInfo ci = commands.get(i);
                     commandsOnPage++;
                     sender.sendMessage(def + " /" + ci.getName() + " - " + def2 + ci.getDescription());
                 }
             }
             return true;
         } else if (cmd.length == 1) {
             try {
                 final int page = Integer.parseInt(cmd[0]) - 1;
                 if (page > Math.floor(commands.size() / HelpPlus.commandsPerPage)) {
                     sender.sendMessage(def1 + "[HelpPlus] Page number " + (page + 1) + "doesn't exist.");
                     return true;
                 }
                 sender.sendMessage(def1 + "[HelpPlus] Available Commands Page " + (page + 1) + " of " + Math.round(commands.size() / HelpPlus.commandsPerPage));
                 int i = page * commandsPerPage;
                 final int displayCommands = i + HelpPlus.commandsPerPage;
                 List<CommandInfo> coms = new ArrayList<CommandInfo>(commands);
                 for (; i < displayCommands;) {
                     CommandInfo ci = coms.get(i);
                     // no more commands to display
                     if (ci == null) {
                         return true;
                     }
                     if (isCommandDisplayed(sender, ci)) {
                         sender.sendMessage(def + " /" + ci.getName() + " - " + def2 + ci.getDescription());
                         i++;
                     }
                 }
             } catch (Exception e) {
                 String cmdName = cmd[0];
                 if (cmdName.startsWith("/"))
                     cmdName.replaceFirst("/", "");
                 CommandInfo comm = getCommandInfo(cmdName);
                 // fix for / in command name
                 if (comm == null)
                     comm = getCommandInfo(cmdName);
                 if (comm != null && comm.getName().equalsIgnoreCase(cmdName) && isCommandDisplayed(sender, comm)) {
                     sender.sendMessage(def + "Command: " + def2 + "/" + comm.getName());
                     sender.sendMessage(def + "Description: " + def2 + comm.getDescription());
                     if (comm.usgae.length() > 20) {
                         sender.sendMessage(def + "Usage: [too long]");
                         sender.sendMessage(def + "Use /help " + comm.getName() + " to display it.]");
                     } else
                         sender.sendMessage(def + "Usage: " + def2 + comm.usgae);
                     sender.sendMessage(def + "Permission needed: " + def2 + comm.getPermission());
                     if (showPlugin)
                         sender.sendMessage(def + "Plugin: " + def2 + comm.getPlugin());
                     sender.sendMessage(def + "Aliases: " + def2 + comm.getAliases());
                     List<CommandInfo> similar = getSimilar(cmdName);
                     if (!similar.isEmpty()) {
                         StringBuilder sb = new StringBuilder(def + "Similar commands: " + def1);
                         for (CommandInfo ci : similar) {
                             sb = sb.append(ci.getName()).append(',');
                         }
                         sb = sb.deleteCharAt(sb.length() - 1);
                         sender.sendMessage(sb.toString());
                     }
                     return true;
                 }
                 // check if we have plugin called cmdName
                 if (getServer().getPluginManager().getPlugin(cmdName) != null) {
                     List<CommandInfo> plugComms = getAllCommands(cmdName);
                     int i = 0;
                     for (CommandInfo ci : plugComms) {
                         if (isCommandDisplayed(sender, ci)) {
                             sender.sendMessage(def + " /" + ci.getName() + " - " + def2 + ci.getDescription());
                             i++;
                         }
                         if (i == commandsPerPage)
                             break;
                     }
                     return true;
                 }
                 sender.sendMessage(ChatColor.RED + "[HelpPlus] Command not found.");
                 List<CommandInfo> similar = getSimilar(cmdName);
                 if (!similar.isEmpty()) {
                     sender.sendMessage(def + "[HelpPlus] You may have thought one of these: ");
                     StringBuilder sb = new StringBuilder();
                     for (CommandInfo ci : similar) {
                         sb = sb.append(def1).append(ci.getName()).append(ChatColor.WHITE).append(',');
                     }
                     sb = sb.deleteCharAt(sb.length() - 2);
                     sender.sendMessage(sb.toString());
                 }
 
             }
         } else if (cmd.length == 2) {
             String pluginName = cmd[0];
             int page = 0;
             try {
                 page = Integer.parseInt(cmd[1]);
             } catch (Exception e) {
                 sender.sendMessage("[HelpPlus] Number expected, '" + cmd[1] + "' given.");
             }
             // used for usage
             if (commandExists(cmd[0])) {
                 String usage = getCommandInfo(cmd[0]).getUsgae();
                 sender.sendMessage(def1 + "[HelpPlus] Usage of " + cmd[0] + ":");
                 sender.sendMessage(usage);
                 return true;
             }
             if (getServer().getPluginManager().getPlugin(pluginName) != null) {
                 List<CommandInfo> plugComms = getAllCommands(pluginName);
                 sender.sendMessage(def1 + "[HelpPlus] Available Commands of " + pluginName + " Page " + page + " of " + Math.round(plugComms.size() / HelpPlus.commandsPerPage));
                 int i = page * commandsPerPage;
                 final int displayCommands = i + HelpPlus.commandsPerPage;
                 for (; i < displayCommands;) {
                     CommandInfo ci = plugComms.get(i);
                     if (isCommandDisplayed(sender, ci)) {
                         sender.sendMessage(def + " /" + ci.getName() + " - " + def2 + ci.getDescription());
                         i++;
                     }
                 }
                 return true;
             }
             sender.sendMessage(ChatColor.RED + "[HelpPlus] Plugin/Command '" + cmd[0] + "' not found");
         } else {
             sender.sendMessage(ChatColor.RED + "/help [page | command]");
         }
         return true;
     }
 
     private boolean isCommandDisplayed(CommandSender sender, CommandInfo ci) {
         if (ci.getPermission() == null || ci.getPermission().equalsIgnoreCase("null") || ci.getPermission().length() == 0)
             return true;
         // looks like more permission nodes are possible
         if (ci.permission.contains(";") && sender instanceof Player) {
             String[] nodes = ci.permission.split(";");
             for (String node : nodes) {
                 if (sender.hasPermission(node))
                     return true;
             }
             return false;
         }
         return !(sender instanceof Player) || perms.has(sender, ci.getPermission());
     }
 
     public CommandInfo getCommandInfo(final String name) {
         return getDatabase().find(CommandInfo.class).where().ieq("name", name).findUnique();
     }
 
     public boolean commandExists(String name) {
         boolean b = !getDatabase().find(CommandInfo.class).where().ieq("name", name).findList().isEmpty();
         return b;
     }
 
     public List<CommandInfo> getSimilar(String name) {
         List<CommandInfo> result = getDatabase().find(CommandInfo.class).where().like("name", name).where().ne("name", name).findList();
         result.addAll(getDatabase().find(CommandInfo.class).where().startsWith("name", name).where().ne("name", name).findList());
         result.addAll(getDatabase().find(CommandInfo.class).where().endsWith("name", name).where().ne("name", name).findList());
         Collections.sort(result);
         return result;
     }
 
     public void addCommand(CommandInfo ci) {
         if (getDatabase().find(CommandInfo.class).where().ieq("name", ci.name) != null) {
             List<CommandInfo> matchingStuff = getDatabase().find(CommandInfo.class).where().ieq("name", ci.getName()).findList();
             getDatabase().delete(matchingStuff);
         }
         getDatabase().save(ci);
     }
 
     public int getCommandsCount() {
         int i = getDatabase().find(CommandInfo.class).findRowCount();
         return i;
     }

     public List<CommandInfo> getAllCommands() {
         List<CommandInfo> result = getDatabase().find(CommandInfo.class).orderBy("name").findList();
         return result;
     }
 
     public List<CommandInfo> getAllCommands(final String plugin) {
         List<CommandInfo> result = getDatabase().find(CommandInfo.class).where().ieq("plugin", plugin).orderBy("name").findList();
         return result;
     }
 
     @SuppressWarnings("unchecked")
     private final <T> T getField(Object obj, String name) {
         try {
             Class<?> clazz = obj.getClass();
             Field field = clazz.getDeclaredField(name);
             field.setAccessible(true);
             return (T) field.get(obj);
         } catch (NoSuchFieldException e) {
             System.out.println("Class '" + obj.getClass().getName() + "' hasn't got field called '" + name + "'");
         } catch (IllegalAccessException e) {
             System.out.println("Access to '" + obj.getClass().getName() + "'s field called '" + name + "' is illegal.");
         }
         return null;
     }

    public static HelpPlus getInstance() {
         return (HelpPlus) Bukkit.getPluginManager().getPlugin("HelpPlus");
     }
 }
