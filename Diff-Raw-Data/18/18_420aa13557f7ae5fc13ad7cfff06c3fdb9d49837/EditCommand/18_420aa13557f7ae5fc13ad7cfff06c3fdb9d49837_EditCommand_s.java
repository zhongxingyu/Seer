 /*
 Copyright (C) 2011 Serge Humphrey
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
 
 package com.btbb.figadmin;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class EditCommand implements CommandExecutor {
 
     FigAdmin plugin;
 
     EditBan ban = null;
 
     /**
      * Used to edit ban entities
      * 
      * @author yottabyte
      * @author Serge Humphrey
      */
     EditCommand(FigAdmin plugin) {
         this.plugin = plugin;
     }
 
     private String banType(int num) {
         switch (num) {
         case 0:
             return "Ban   ";
         case 1:
             return "IP-Ban";
         case 2:
             return "Warn  ";
         default:
             return "?";
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 
         try {
             if (sender instanceof Player)
                 if (!Permissions.Security.has((Player) sender, "figadmin.editban"))
                     return true;
 
             if (args.length < 1)
                 return false;
 
             if (args[0].equalsIgnoreCase("list")) {
                 return list(sender, args);
             }
 
             if (args[0].equalsIgnoreCase("load")) {
                 return load(sender, args);
             }
             if (args[0].equalsIgnoreCase("id")) {
                 return id(sender, args);
             }
             if (args[0].equalsIgnoreCase("delete")) {
                 return delete(sender, args);
             }
             if (args[0].equalsIgnoreCase("search")) {
                 return search(sender, args);
             }
             if (ban == null) {
                 sender.sendMessage(ChatColor.RED + "You aren't editing a ban");
                 return true;
             }
 
             if (args[0].equalsIgnoreCase("save")) {
                 return save(sender, args);
             }
 
             if (args[0].equalsIgnoreCase("cancel")) {
                 return cancel(sender, args);
             }
 
            if (args[0].equalsIgnoreCase("show") || args[9].equalsIgnoreCase("view")) {
                 return view(sender, args);
             }
             if (args[0].equalsIgnoreCase("reason")) {
                 return reason(sender, args);
             }
             if (args[0].equalsIgnoreCase("time")) {
                 return time(sender, args);
             }
         } catch (Exception exc) {
             System.out.println("[FigAdmin] Error: EditCommand");
             exc.printStackTrace();
         }
         return false;
 
     }
 
     private void showBanInfo(EditBan eb, CommandSender sender) {
         DateFormat shortTime = DateFormat.getDateTimeInstance();
         sender.sendMessage(ChatColor.GOLD + " | " + ChatColor.WHITE + eb.name + ChatColor.YELLOW + " was banned by "
                 + ChatColor.WHITE + eb.admin + ChatColor.YELLOW + " at " + shortTime.format((new Date(eb.time * 1000))));
         if (eb.endTime > 0)
             sender.sendMessage(ChatColor.GOLD + " | " + ChatColor.YELLOW + "Will be unbanned at "
                     + shortTime.format((new Date(eb.endTime * 1000))));
         sender.sendMessage(ChatColor.GOLD + " | " + ChatColor.YELLOW + "Reason: " + ChatColor.GRAY + eb.reason);
     }
 
     private boolean list(CommandSender sender, String[] args) {
         if (args.length < 2) {
             sender.sendMessage(ChatColor.RED + "Usage: list <player>");
             return true;
         }
         if (!FigAdmin.validName(args[1])) {
             sender.sendMessage(plugin.formatMessage(plugin.getConfig().getString("messages.badPlayerName", "bad player name")));
             return true;
         }
         List<EditBan> bans = plugin.db.listRecords(args[1], true);
         if (bans.isEmpty()) {
             sender.sendMessage(ChatColor.RED + "No records");
             return true;
         }
         sender.sendMessage(ChatColor.GOLD + "Found " + bans.size() + " records for user " + bans.get(0).name + ":");
         for (EditBan ban : bans) {
             sender.sendMessage(ChatColor.AQUA + banType(ban.type) + ChatColor.YELLOW + ban.id + ": " + ChatColor.GREEN
                     + ban.reason + ChatColor.YELLOW + " by " + ban.admin);
         }
         return true;
     }
 
     private boolean search(CommandSender sender, String[] args) {
         if (args.length < 2) {
             sender.sendMessage(ChatColor.RED + "Usage: search <player>");
             return true;
         }
         List<EditBan> bans = plugin.db.listRecords(args[1], false);
         if (bans.isEmpty()) {
             sender.sendMessage(ChatColor.RED + "No records");
             return true;
         }
         sender.sendMessage(ChatColor.GOLD + "Found " + bans.size() + " records for keyword " + bans.get(0).name + ":");
         for (EditBan ban : bans) {
             sender.sendMessage(ChatColor.AQUA + banType(ban.type) + ChatColor.YELLOW + ban.id + 
                     " "+ban.name +
                     ": " + ChatColor.GREEN + ban.reason + ChatColor.YELLOW + " by " + ban.admin);
         }
         return true;
     }
 
     private boolean load(CommandSender sender, String[] args) {
         if (ban != null) {
             sender.sendMessage(ChatColor.RED + "Finish what you're doing first!");
             return true;
         }
 
         if (args.length < 2) {
             sender.sendMessage(ChatColor.RED + "Usage: load <player>");
             return true;
         }
         if (!FigAdmin.validName(args[1])) {
             sender.sendMessage(ChatColor.RED
                     + plugin.formatMessage(plugin.getConfig().getString("messages.badPlayerName", "bad player name")));
             return true;
         }
         EditBan eb = plugin.db.loadFullRecord(args[1]);
         if (eb == null) {
             sender.sendMessage(ChatColor.RED + "Unable to find the last ban/warn of this player");
             return true;
         }
         ban = eb;
         sender.sendMessage(ChatColor.GREEN + "Editing the last ban/warn of player " + eb.name + ": ");
         showBanInfo(eb, sender);
         return true;
     }
 
     private boolean id(CommandSender sender, String[] args) {
 
         if (ban != null) {
             sender.sendMessage(ChatColor.RED + "Finish what you're doing first!");
             return true;
         }
 
         if (args.length < 2) {
             sender.sendMessage(ChatColor.RED + "Usage: load <ban id>");
             return true;
         }
 
         int id;
         try {
             id = Integer.parseInt(args[1]);
         } catch (NumberFormatException exc) {
             sender.sendMessage(ChatColor.RED + "ID has to be a number!");
             return true;
         }
 
         EditBan eb = plugin.db.loadFullRecord(id);
         if (eb == null) {
             sender.sendMessage(ChatColor.RED + "Unable to find a ban of this player");
             return true;
         }
         ban = eb;
         sender.sendMessage(ChatColor.GREEN + "Editing the last ban/warn of player " + eb.name + ": ");
         showBanInfo(eb, sender);
         return true;
     }
 
     private boolean save(CommandSender sender, String[] args) {
         if (plugin.db.saveFullRecord(ban)) {
             for (int i = 0; i < plugin.bannedPlayers.size(); i++) {
                 EditBan eb = plugin.bannedPlayers.get(i);
                 if (eb.name.equals(ban.name) && eb.type == ban.type) {
                     plugin.bannedPlayers.set(i, eb);
                     break;
                 }
             }
             sender.sendMessage(ChatColor.GREEN + "Saved ban!");
         } else {
             sender.sendMessage(ChatColor.RED + "Saving Failed!");
         }
         ban = null;
         return true;
 
     }
 
     private boolean view(CommandSender sender, String[] args) {
         showBanInfo(ban, sender);
         return true;
 
     }
 
     private boolean reason(CommandSender sender, String[] args) {
 
         if (args.length < 2) {
             sender.sendMessage(ChatColor.RED + "Usage: reason <add/set/show> (text)");
             return true;
         }
 
         if (args[1].equalsIgnoreCase("add")) {
             if (args.length < 3) {
                 sender.sendMessage(ChatColor.RED + "Usage: reason add <text>");
                 return true;
             }
             ban.reason += " " + plugin.combineSplit(2, args, " ");
             ban.reason = plugin.formatMessage(ban.reason);
             return true;
 
         }
 
         if (args[1].equalsIgnoreCase("set")) {
             if (args.length < 3) {
                 sender.sendMessage(ChatColor.RED + "Usage: reason set <text>");
                 return true;
             }
             ban.reason = plugin.combineSplit(2, args, " ");
             ban.reason = plugin.formatMessage(ban.reason);
             return true;
         }
         if (args[1].equalsIgnoreCase("show")) {
             sender.sendMessage(ChatColor.YELLOW + "Reason: " + ChatColor.WHITE + ban.reason);
             return true;
         }
         return false;
     }
 
     private boolean time(CommandSender sender, String[] args) {
 
         if (args.length < 4) {
             sender.sendMessage(ChatColor.RED + "Usage: time <add/sub/set> <time> <sec/min/hour/day/week/month>");
             return true;
         }
 
         long time = plugin.parseTimeSpec(args[2], args[3]);
         if (time == 0) {
             sender.sendMessage(ChatColor.RED + "Invalid time format");
             return true;
         }
 
         boolean add = args[1].equalsIgnoreCase("add"), set = args[1].equalsIgnoreCase("set"), sub = args[1]
                 .equalsIgnoreCase("sub");
         if (add || set || sub) {
             if (ban.endTime == 0) {
                 ban.endTime = ban.time;
             }
             if (add) {
                 ban.endTime += time;
             } else if (set) {
 
                 ban.endTime = ban.time + time;
             } else if (sub) {
                 ban.endTime -= time;
             }
             Date date = new Date();
             date.setTime(ban.endTime * 1000);
             sender.sendMessage(ChatColor.YELLOW + "New time: " + ChatColor.WHITE + date.toString());
             return true;
         }
         return false;
     }
 
     private boolean delete(CommandSender sender, String[] args) {
         if (ban != null) {
             sender.sendMessage(ChatColor.RED + "Finish what you're doing first!");
             return true;
         }
         if (args.length < 2) {
             sender.sendMessage(ChatColor.RED + "Usage: delete [id]");
             return true;
         }
         int id;
         try {
             id = Integer.parseInt(args[1]);
         } catch (NumberFormatException exc) {
             sender.sendMessage(ChatColor.RED + "ID has to be a number!");
             return true;
         }
         for (int i = 0; i < plugin.bannedPlayers.size(); i++) {
             if (plugin.bannedPlayers.get(i).id == id) {
                 plugin.bannedPlayers.remove(i);
                 break;
             }
         }
         return plugin.db.deleteFullRecord(id);
     }
 
     private boolean cancel(CommandSender sender, String[] args) {
         ban = null;
         sender.sendMessage(ChatColor.YELLOW + "Cancelled.");
         return true;
     }
 }
