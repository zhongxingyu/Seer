 // Bukkit Plugin "iSpawner" by Siguza
 // The license under which this software is released can be accessed at:
 // http://creativecommons.org/licenses/by-nc-sa/3.0/
 
 package net.drgnome.ispawner;
 
 import java.util.*;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.*;
 import org.bukkit.entity.Player;
 import net.drgnome.nbtlib.*;
 
 public enum Commands
 {
     VERSION(null, true, "version", "/ispawner version - Show the version of iSpawner")
     {
         public void execute(CommandSender sender, String[] args)
         {
             Util.sendMessage(sender, "iSpawner version: " + SpawnPlugin._version, ChatColor.GREEN);
         }
     },
     START_CONSOLE("ispawner.use", true, "start * * * *", "/ispawner start (world) (x) (y) (z) - Start editing in \"world\" at position x/y/z.")
     {
         public void execute(CommandSender sender, String[] args)
         {
             if(SpawnPlugin.instance().hasSession(sender.getName()))
             {
                 Util.sendMessage(sender, "You already have a session running.", ChatColor.RED);
                 return;
             }
             World world = Bukkit.getWorld(args[1]);
             if(world == null)
             {
                 Util.sendMessage(sender, "That world does not exist.", ChatColor.RED);
                 return;
             }
             SpawnPlugin.instance().tryStartSession(sender.getName());
             try
             {
                 SpawnPlugin.instance().startSession(sender, world, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
             }
             catch(NumberFormatException e)
             {
                 Util.sendMessage(sender, "The coordinates have to be numbers.", ChatColor.RED);
             }
         }
     },
     START("ispawner.use", false, "start", "/ispawner start - Click a mob spawner to start editing")
     {
         public void execute(CommandSender sender, String[] args)
         {
             if(SpawnPlugin.instance().hasSession(sender.getName()))
             {
                 Util.sendMessage(sender, "You already have a session running.", ChatColor.RED);
                 return;
             }
             SpawnPlugin.instance().prepareSession(sender.getName());
             Util.sendMessage(sender, "Now click a block.", ChatColor.GREEN);
         }
     },
     CANCEL("ispawner.use", true, "cancel", "/ispawner cancel - Cancel the current session")
     {
         public void execute(CommandSender sender, String[] args)
         {
             if(!checkSession(sender))
             {
                 return;
             }
             String name = sender.getName();
             SpawnPlugin.instance().getSession(name).disable(false);
             SpawnPlugin.instance().endSession(name);
             Util.sendMessage(sender, "iSpawner session cancelled.", ChatColor.YELLOW);
         }
     },
     END("ispawner.use", true, "end", "/ispawner end - End an editing session")
     {
         public void execute(CommandSender sender, String[] args)
         {
             if(!checkSession(sender))
             {
                 return;
             }
             String name = sender.getName();
             if(SpawnPlugin.instance().getSession(name).apply())
             {
                 SpawnPlugin.instance().endSession(name);
                 Util.sendMessage(sender, "Data applied and session ended.", ChatColor.GREEN);
             }
             else
             {
                 Util.sendMessage(sender, "Error applying data (see console). Session continues.", ChatColor.RED);
             }
         }
     },
     SET("ispawner.use.admin", true, "", "/ispawner set (path) (type) [value] - Set an NBT tag")
     {
         public void execute(CommandSender sender, String[] args)
         {
             if(!checkSession(sender))
             {
                 return;
             }
             NBTHelper.parse(sender, SpawnPlugin.instance().getSession(sender.getName()).getData(), Util.implode(1, " ", args));
         }
         
         public boolean matches(String[] args)
         {
             return (args.length >= 1) && args[0].equalsIgnoreCase("set");
         }
     },
     IMPORT("ispawner.import", true, "import *", "/ispawner import (name) - Import the file iSpawner/data/(name).txt")
     {
         public void execute(CommandSender sender, String[] args)
         {
             if(!checkSession(sender))
             {
                 return;
             }
             if(!sender.hasPermission("ispawner.import.admin") && !sender.hasPermission("ispawner.import." + args[1]))
             {
                 Util.sendMessage(sender, "You need one of the following permissions:", ChatColor.RED);
                 Util.sendMessage(sender, "ispawner.import.admin", ChatColor.YELLOW);
                 Util.sendMessage(sender, "ispawner.import." + args[1], ChatColor.YELLOW);
                 return;
             }
             String[] lines = SpawnPlugin.importData(args[1]);
             if(lines == null)
             {
                 Util.sendMessage(sender, "File not found.", ChatColor.RED);
             }
             else if(lines.length == 0)
             {
                 Util.sendMessage(sender, "Importing failed.", ChatColor.RED);
                 return;
             }
             HashMap<String, Tag> map = new HashMap<String, Tag>();
             for(int i = 0; i < lines.length; i++)
             {
                 if(!lines[i].isEmpty())
                 {
                     NBTHelper.parse(sender, map, lines[i], i + 1);
                 }
             }
             SpawnPlugin.instance().getSession(sender.getName()).setData(map);
             Util.sendMessage(sender, "Imported.", ChatColor.GREEN);
         }
     },
    EXPORT_OVERRIDE("ispawner.export.admin", true, "export override *", "/ispawner export (name) - Export and override iSpawner/data/(name).txt")
     {
         public void execute(CommandSender sender, String[] args)
         {
             if(!checkSession(sender))
             {
                 return;
             }
             NBTHelper.export(sender, args);
         }
     },
     EXPORT("ispawner.export", true, "export *", "/ispawner export (name) - Export the current spawner to iSpawner/data/(name).txt")
     {
         public void execute(CommandSender sender, String[] args)
         {
             if(!checkSession(sender))
             {
                 return;
             }
             if(SpawnPlugin.dataFileExists(args[1]))
             {
                 Util.sendMessage(sender, "This file exists already.", ChatColor.RED);
                 if(sender.hasPermission(Commands.EXPORT_OVERRIDE._permission))
                 {
                     Util.sendMessage(sender, "Use " + ChatColor.YELLOW + "/ispawner export override ..." + ChatColor.RED + " instead.", ChatColor.RED);
                 }
                 return;
             }
             NBTHelper.export(sender, args);
         }
     },
     INFO("ispawner.use", true, "info", "/ispawner info - Print the data of the current spawner")
     {
         public void execute(CommandSender sender, String[] args)
         {
             if(!checkSession(sender))
             {
                 return;
             }
             NBTHelper.print(sender, SpawnPlugin.instance().getSession(sender.getName()).getData());
         }
     },
     TYPES("ispawner.use", true, "types", "/ispawner types - Show all possible NBT types")
     {
         public void execute(CommandSender sender, String[] args)
         {
             for(NBT val : NBT.values())
             {
                 Util.sendMessage(sender, (val == NBT.END) ? "-" : val.name().toLowerCase(), ChatColor.GREEN);
             }
         }
     },
     HELP(null, true, "", "/ispawner - Show the help")
     {
         public void execute(CommandSender sender, String[] args)
         {
             Util.sendMessage(sender, "----- ----- ----- iSpawner Help ----- ----- -----", ChatColor.AQUA);
             for(Commands cmd : values())
             {
                 if(cmd.hasPermission(sender))
                 {
                     cmd.printUsage(sender);
                 }
             }
         }
         
         public boolean matches(String[] args)
         {
             return (args.length == 0) || ((args.length == 1) && args[0].equalsIgnoreCase("help"));
         }
     };
     
     private final String _permission;
     private final boolean _allowConsole;
     private final String[] _pattern;
     private final String _usage;
     
     private Commands(String permission, boolean allowConsole, String pattern, String usage)
     {
         _permission = permission;
         _allowConsole = allowConsole;
         _pattern = pattern.isEmpty() ? new String[0] : pattern.split(" ");
         _usage = usage;
     }
     
     public boolean hasPermission(CommandSender sender)
     {
         return (_permission == null) || sender.hasPermission(_permission);
     }
     
     public boolean canExecute(CommandSender sender)
     {
         return _allowConsole || (sender instanceof Player);
     }
     
     public boolean matches(String[] args)
     {
         if(args.length != _pattern.length)
         {
             return false;
         }
         for(int i = 0; i < _pattern.length; i++)
         {
             if(!_pattern[i].equals("*") && !_pattern[i].equalsIgnoreCase(args[i]))
             {
                 return false;
             }
         }
         return true;
     }
     
     public void printUsage(CommandSender sender)
     {
         Util.sendMessage(sender, _usage, ChatColor.AQUA);
     }
     
     public abstract void execute(CommandSender sender, String[] args);
     
     private static boolean checkSession(CommandSender sender)
     {
         if(SpawnPlugin.instance().hasSession(sender.getName()))
         {
             return true;
         }
         Util.sendMessage(sender, "You have to start a session first.", ChatColor.RED);
         return false;
     }
     
     public static void handle(CommandSender sender, String[] args)
     {
         Commands cmd = null;
         for(Commands c : values())
         {
             if(c.matches(args))
             {
                 cmd = c;
                 break;
             }
         }
         if(cmd == null)
         {
             Util.sendMessage(sender, "Unknown argument.", ChatColor.RED);
             return;
         }
         if(!cmd.canExecute(sender))
         {
             Util.sendMessage(sender, "This command cannot be run from the console.", ChatColor.RED);
             return;
         }
         if(!cmd.hasPermission(sender))
         {
             Util.sendMessage(sender, "You don't have the permission to execute this command.", ChatColor.RED);
             return;
         }
         cmd.execute(sender, args);
     }
 }
