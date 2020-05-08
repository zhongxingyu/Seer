 package com.oresomecraft.mapdev;
 
 import com.sk89q.minecraft.util.commands.*;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Commands {
 
     MapDevPlugin plugin;
 
     public Commands(MapDevPlugin pl) {
         plugin = pl;
     }
 
     @Command(aliases = {"loadworld", "createworld"},
             usage = "/loadworld <WorldName>",
             desc = "Loads or creates a world.",
             min = 1, max = 1)
     @CommandPermissions({"mapdev.loadworld"})
     public void loadWorld(CommandContext args, CommandSender sender) throws CommandException {
         WorldUtil.loadOrCreateWorld(args.getString(0).toLowerCase());
         sender.sendMessage(ChatColor.DARK_AQUA + "Created/loaded world " + ChatColor.AQUA + args.getString(0).toLowerCase());
     }
 
     @Command(aliases = {"unloadworld"},
             usage = "/unloadworld <WorldName>",
             desc = "Unloads a world.",
             min = 1, max = 1)
     @CommandPermissions({"mapdev.unloadworld"})
     public void unloadWorld(CommandContext args, CommandSender sender) throws CommandException {
         if (WorldUtil.unloadWorld(args.getString(0).toLowerCase()))
             sender.sendMessage(ChatColor.DARK_AQUA + "Unloaded world " + ChatColor.AQUA + args.getString(0).toLowerCase());
         else
             sender.sendMessage(ChatColor.RED + "Unable to unload world!");
     }
 
    @Command(aliases = {"loadworldfromrepo", "loadmapfromrepo"},
             usage = "/loadworldfromrepo <WorldName>",
             desc = "Loads a world from the maps repo",
             min = 1, max = 1)
     @CommandPermissions({"mapdev.loadworldfromrepo"})
     public void loadWorldFromRepo(CommandContext args, CommandSender sender) throws CommandException {
         if (WorldUtil.loadWorldFromRepo(args.getString(0).toLowerCase())) {
             sender.sendMessage(ChatColor.DARK_AQUA + "Copied and loaded world " + ChatColor.AQUA
                     + args.getString(0).toLowerCase() + ChatColor.DARK_AQUA + " from maps repository!");
         } else {
             sender.sendMessage(ChatColor.RED + "Unable to load map from maps repo!");
             sender.sendMessage(ChatColor.RED + "Are you sure the map exists/is spelt correctly?");
         }
     }
 
    @Command(aliases = {"putworldinrepo", "putmapinrepo"},
             usage = "/putworldinrepo <WorldName>",
             desc = "Puts a world into the maps repository",
             min = 1, max = 1)
    @CommandPermissions({"mapdev.putworldinrepo"})
     public void putWorldInRepo(CommandContext args, CommandSender sender) throws CommandException {
         if (WorldUtil.putMapInRepo(args.getString(0).toLowerCase())) {
             sender.sendMessage(ChatColor.DARK_AQUA + "Copied and put world " + ChatColor.AQUA
                     + args.getString(0).toLowerCase() + ChatColor.DARK_AQUA + " into the maps repository!");
         } else {
             sender.sendMessage(ChatColor.RED + "Unable put world into maps repository!");
         }
     }
 
     @Command(aliases = {"discardworld"},
             usage = "/discardworld <WorldName>",
             desc = "Unloads and deletes a world",
             min = 1, max = 1)
     @CommandPermissions({"mapdev.discardworld"})
     public void discardWorld(CommandContext args, CommandSender sender) throws CommandException {
        if (WorldUtil.discardWorld(args.getString(0).toLowerCase())) {
             sender.sendMessage(ChatColor.DARK_AQUA + "Deleted and unloaded " + ChatColor.AQUA + args.getString(0).toLowerCase());
         } else {
             sender.sendMessage(ChatColor.RED + "Unable to delete worlds");
         }
     }
 
     @Command(aliases = {"worldtp"},
             usage = "/worldtp <WorldName>",
             desc = "Teleports you to a world.")
     @CommandPermissions({"battleutils.worldtp"})
     public void worldtp(CommandContext args, CommandSender sender) throws CommandException {
         Player p = (Player) sender;
         if (args.argsLength() < 1) {
             sender.sendMessage(ChatColor.RED + "Correct usage: /worldtp <WorldName>");
         } else {
             if (Bukkit.getWorld(args.getString(0)) != null) {
                 p.teleport(Bukkit.getWorld(args.getString(0)).getSpawnLocation());
             }
         }
     }
 
     @Command(aliases = {"worldsetspawn"},
             usage = "/worldsetspawn",
             desc = "Sets spawn for a world.")
     @CommandPermissions({"battleutils.worldsetspawn"})
     public void worldsetspawn(CommandContext args, CommandSender sender) throws CommandException {
         Player p = (Player) sender;
         World world = p.getWorld();
         world.setSpawnLocation((int) p.getLocation().getX(), (int) p.getLocation().getY(), (int) p.getLocation().getZ());
         sender.sendMessage(ChatColor.AQUA + "Set spawn point for world '" + p.getWorld().getName() + "'");
     }
 
 }
