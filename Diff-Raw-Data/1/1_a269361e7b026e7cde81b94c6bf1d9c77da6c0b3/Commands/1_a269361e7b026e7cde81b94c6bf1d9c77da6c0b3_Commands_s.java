 package com.oresomecraft.mapdev;
 
 import com.oresomecraft.mapdev.generators.NullChunkGenerator;
 import com.sk89q.minecraft.util.commands.Command;
 import com.sk89q.minecraft.util.commands.CommandContext;
 import com.sk89q.minecraft.util.commands.CommandException;
 import com.sk89q.minecraft.util.commands.CommandPermissions;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 public class Commands {
     MapDevPlugin plugin;
 
     public Commands(MapDevPlugin pl) {
         plugin = pl;
     }
 
     @Command(aliases = {"loadworld", "createworld"},
             usage = "<WorldName>",
             desc = "Loads or creates a world.",
             min = 1,
             max = 1)
     @CommandPermissions({"mapdev.loadworld"})
     public void loadWorld(CommandContext args, CommandSender sender) throws CommandException {
         WorldUtil.loadOrCreateWorld(args.getString(0).toLowerCase());
         sender.sendMessage(ChatColor.DARK_AQUA + "Created/loaded world " + ChatColor.AQUA + args.getString(0).toLowerCase());
     }
 
     @Command(aliases = {"unloadworld"},
             usage = "<WorldName>",
             desc = "Unloads a world.",
             min = 1,
             max = 1)
     @CommandPermissions({"mapdev.unloadworld"})
     public void unloadWorld(CommandContext args, CommandSender sender) throws CommandException {
         if (WorldUtil.unloadWorld(args.getString(0).toLowerCase()))
             sender.sendMessage(ChatColor.DARK_AQUA + "Unloaded world " + ChatColor.AQUA + args.getString(0).toLowerCase());
         else sender.sendMessage(ChatColor.RED + "Unable to unload world!");
     }
 
     @Command(aliases = {"loadworldfromrepo", "loadmapfromrepo"},
             usage = "<WorldName>",
             desc = "Loads a world from the maps repo",
             min = 1,
             max = 1)
     @CommandPermissions({"mapdev.loadworldfromrepo"})
     public void loadWorldFromRepo(CommandContext args, CommandSender sender) throws CommandException {
         if (WorldUtil.loadWorldFromRepo(args.getString(0).toLowerCase()))
             sender.sendMessage(ChatColor.DARK_AQUA + "Copied and loaded world " + ChatColor.AQUA + args.getString(0).toLowerCase() + ChatColor.DARK_AQUA + " from maps repository!");
         else {
             sender.sendMessage(ChatColor.RED + "Unable to load map from maps repo!");
             sender.sendMessage(ChatColor.RED + "Are you sure the map exists/is spelt correctly?");
         }
     }
 
     @Command(aliases = {"putworldinrepo", "putmapinrepo"},
             usage = "<WorldName>",
             desc = "Puts a world into the maps repository",
             min = 1,
             max = 1)
     @CommandPermissions({"mapdev.putworldinrepo"})
     public void putWorldInRepo(CommandContext args, CommandSender sender) throws CommandException {
         if (WorldUtil.putMapInRepo(args.getString(0).toLowerCase()))
             sender.sendMessage(ChatColor.DARK_AQUA + "Copied and put world " + ChatColor.AQUA + args.getString(0).toLowerCase() + ChatColor.DARK_AQUA + " into the maps repository!");
         else sender.sendMessage(ChatColor.RED + "Unable put world into maps repository!");
     }
 
     @Command(aliases = {"discardworld"},
             usage = "<WorldName>",
             desc = "Unloads and deletes a world",
             min = 1,
             max = 1)
     @CommandPermissions({"mapdev.discardworld"})
     public void discardWorld(CommandContext args, CommandSender sender) throws CommandException {
         if (WorldUtil.discardWorld(args.getString(0).toLowerCase())) {
             sender.sendMessage(ChatColor.DARK_AQUA + "Deleted and unloaded " + ChatColor.AQUA + args.getString(0).toLowerCase());
         } else {
             sender.sendMessage(ChatColor.RED + "Unable to delete worlds");
         }
     }
 
     @Command(aliases = {"renameworld"},
             usage = "<OriginalWorldName> <NewName>",
             desc = "Copies, renames & loads a world",
             flags = "d",
             min = 2, max = 2)
     @CommandPermissions({"mapdev.renameworld"})
     public void renameWorld(CommandContext args, CommandSender sender) throws CommandException {
         try {
             WorldUtil.copyFolder(new File(args.getString(0)), new File(args.getString(1)));
             File tar = new File(args.getString(1));
             for (File f : tar.listFiles()) {
                 if (f.getName().equals("uid.dat")) {
                     f.delete();
                 }
             }
             WorldCreator worldc = new WorldCreator(args.getString(1));
             worldc.generator(new NullChunkGenerator());
             Bukkit.createWorld(worldc);
         } catch (IOException e) {
             if (e instanceof FileNotFoundException) {
                 sender.sendMessage(ChatColor.RED + "Something went wrong. Perhaps that world doesn't exist?");
                 return;
             }
             e.printStackTrace();
             //Love, why didn't the world copy?
         }
         if (args.hasFlag('d')) {
             Bukkit.dispatchCommand(sender, "worldtp " + args.getString(1));
             WorldUtil.discardWorld(args.getString(0));
             sender.sendMessage(ChatColor.RED + "WARNING: You used the -d flag and deleted the original map!");
         }
         sender.sendMessage(ChatColor.AQUA + "Copied world '" + args.getString(0) + "' and renamed it to '" + args.getString(1) + "'!");
     }
 
     @Command(aliases = {"listmaps"},
             desc = "Lists all maps in the defined repo")
     @CommandPermissions({"mapdev.listmaps"})
     public void listMaps(CommandContext args, CommandSender sender) throws CommandException {
         sender.sendMessage(ChatColor.DARK_AQUA + "Maps in the defined repo:");
         for (File f : new File(WorldUtil.MAPS_REPO).listFiles()) {
             sender.sendMessage(ChatColor.AQUA + f.getName());
         }
     }
 
     @Command(aliases = {"worldtp"},
             usage = "<WorldName>",
             desc = "Teleports you to a world.")
     @CommandPermissions({"mapdev.worldtp"})
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
             desc = "Sets spawn for a world.")
     @CommandPermissions({"mapdev.worldsetspawn"})
     public void worldsetspawn(CommandContext args, CommandSender sender) throws CommandException {
         Player p = (Player) sender;
         World world = p.getWorld();
         world.setSpawnLocation((int) p.getLocation().getX(), (int) p.getLocation().getY(), (int) p.getLocation().getZ());
         sender.sendMessage(ChatColor.AQUA + "Set spawn point for world '" + p.getWorld().getName() + "'");
     }
 
     @Command(aliases = {"terraform, tf"},
             usage = "/terraform",
             desc = "Adds Terraforming tools to your inventory.")
     @CommandPermissions({"battleutils.terraform"})
     public void terraform(CommandContext args, CommandSender sender) throws CommandException {
         Player p = (Player) sender;
         p.getInventory().clear();
         p.getInventory().setItem(0, new ItemStack(Material.COMPASS));
         p.getInventory().setItem(1, new ItemStack(Material.WOOD_AXE));
         p.getInventory().setItem(2, new ItemStack(Material.ARROW));
         p.getInventory().setItem(3, new ItemStack(Material.DIRT));
         p.getInventory().setItem(4, new ItemStack(Material.STONE));
         p.getInventory().setItem(5, new ItemStack(Material.DIAMOND_PICKAXE));
 
         p.sendMessage(ChatColor.DARK_AQUA + "Inventory replaced with TerraForming tools!");
     }
 
 }
