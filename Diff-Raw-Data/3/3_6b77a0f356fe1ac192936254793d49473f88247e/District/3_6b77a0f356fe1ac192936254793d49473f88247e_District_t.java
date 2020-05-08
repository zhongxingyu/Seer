 package com.md_5.district;
 
 import com.griefcraft.lwc.LWC;
 import com.griefcraft.lwc.LWCPlugin;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandException;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class District extends JavaPlugin {
 
     public static District instance;
     public LWC lwc;
 
     @Override
     public void onEnable() {
         instance = this;
         Config.load();
         Database.init();
         new DistrictListener();
         final LWCPlugin lwcPlugin = (LWCPlugin) getServer().getPluginManager().getPlugin("LWC");
         if (lwcPlugin != null) {
             lwc = lwcPlugin.getLWC();
         }
     }
 
     @Override
     public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
         if (sender instanceof Player) {
             final Player player = (Player) sender;
             if (args.length == 0) {
                 player.sendMessage(ChatColor.GOLD + "District by md_5, the following commands may be used at this time:");
                 player.sendMessage(ChatColor.GOLD + "/district claim <size> <region>");
                 player.sendMessage(ChatColor.GOLD + "/district show <region>");
                 player.sendMessage(ChatColor.GOLD + "/district hide <region>");
                 player.sendMessage(ChatColor.GOLD + "/district remove <region>");
                 player.sendMessage(ChatColor.GOLD + "/district list");
                 player.sendMessage(ChatColor.GOLD + "/district listall [player]");
                 player.sendMessage(ChatColor.GOLD + "/district quota");
                 player.sendMessage(ChatColor.GOLD + "/district setowner <region> <player>");
                 player.sendMessage(ChatColor.GOLD + "/district addmember <region> <player>");
                 player.sendMessage(ChatColor.GOLD + "/district delmember <region> <player>");
                 player.sendMessage(ChatColor.GOLD + "/district listmembers <region>");
                 return true;
             }
             if (args[0].equalsIgnoreCase("claim")) {
                 Commands.claim(player, args, 3);
                 return true;
             }
             if (args[0].equalsIgnoreCase("show")) {
                 Commands.show(player, args, matchRegion(player, args));
                 return true;
             }
             if (args[0].equalsIgnoreCase("hide")) {
                 Commands.hide(player, args, matchRegion(player, args));
                 return true;
             }
             if (args[0].equalsIgnoreCase("remove")) {
                 Commands.remove(player, args, matchRegion(player, args));
                 return true;
             }
             if (args[0].equalsIgnoreCase("list")) {
                 Commands.list(player.getName(), player);
                 return true;
             }
             if (args[0].equalsIgnoreCase("listall")) {
                 Commands.listAll(player, args);
                 return true;
             }
             if (args[0].equalsIgnoreCase("quota")) {
                 Commands.quota(player, args);
                 return true;
             }
             if (args[0].equalsIgnoreCase("setowner")) {
                 Commands.setOwner(player, args, matchRegion(player, args));
                 return true;
             }
             if (args[0].equalsIgnoreCase("addmember")) {
                 Commands.addMember(player, args, matchRegion(player, args));
                 return true;
             }
             if (args[0].equalsIgnoreCase("delmember")) {
                 Commands.delMember(player, args, matchRegion(player, args));
                 return true;
             }
             if (args[0].equalsIgnoreCase("listmembers")) {
                 Commands.listMembers(player, args, matchRegion(player, args));
                 return true;
             }
             player.sendMessage(ChatColor.RED + "District: That is not a valid command");
         }
         return true;
     }
 
     private Region matchRegion(final Player player, final String[] args) {
         if (args.length <= 1) {
            player.sendMessage("You must supply a region name or '-'");
            return null;
         } else if (args[1].trim().equals("-")) {
             Region region = Util.getRegion(player.getLocation());
             if (region == null) {
                 player.sendMessage(ChatColor.RED + "Unable to use '-' operator when not in a region");
             }
             return region;
         } else {
             Region r = Loader.load(args[1]);
             if (r == null) {
                 player.sendMessage(ChatColor.RED + "Region does not exist");
             }
             return r;
         }
     }
 }
