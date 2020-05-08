 package com.syd.expskills;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandManager implements CommandExecutor
 {
     ExpSkills plugin;
     
     public CommandManager(ExpSkills instance)
     {
         plugin = instance;
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
         // Console or Player check - loading player data
         Player p = null;
         
         if (sender instanceof Player)
             p = (Player) sender;
         
         if (p == null || p.hasPermission("expskills.use") || p.hasPermission("expskills.admin"))
         {
             if (cmd.getName().equalsIgnoreCase("exp"))
             {
                 if (args.length == 0)
                     return false;
                 
                 if (args[0].equalsIgnoreCase("help"))
                 {
                     if (args.length == 1)
                     {
                         sender.sendMessage("/exp help [command] - " + ExpSkills.lang.getString("command.help", "get help!"));
                         sender.sendMessage("/exp stats - " + ExpSkills.lang.getString("command.stats", "show's stats"));
                         sender.sendMessage("/exp list <page> <filter> - " + ExpSkills.lang.getString("command.list", "list avaible skills"));
                         sender.sendMessage("/exp info <skill> - " + ExpSkills.lang.getString("command.info", "get information about a skill!"));
                         sender.sendMessage("/exp buy <skill> - " + ExpSkills.lang.getString("command.buy", "buy a skill"));
                         sender.sendMessage("/exp rent <skill> - " + ExpSkills.lang.getString("command.rent", "rent a skill"));
                         sender.sendMessage("/exp current - " + ExpSkills.lang.getString("command.current", "show's your current skills"));
                         if (p == null || p.hasPermission("expskills.admin"))
                         {
                             sender.sendMessage("===== Admin Commands =====");
                             sender.sendMessage("/exp stats <player> - " + ExpSkills.lang.getString("command.stats", "show's stats"));
                             sender.sendMessage("/exp <set/add> <player> <xp/level/skill> <amount> - " + ExpSkills.lang.getString("command.setadd", "modify a player's stats"));
                             sender.sendMessage("/exp <grant/revoke> <player> <skill> - " + ExpSkills.lang.getString("command.grantrevoke", "grant/revoke a skill"));
                             sender.sendMessage("/exp current <player> - " + ExpSkills.lang.getString("command.current", "show's your current skills"));
                             sender.sendMessage("/exp reset <player> - " + ExpSkills.lang.getString("command.reset", "resets all skills of a player"));
                         }
                         return true;
                     }
                     else if (args[1].equalsIgnoreCase("stats"))
                     {
                         sender.sendMessage(ExpSkills.lang.getString("help.stats", "show's your current Level, XP, Skillpoints and playtime"));
                         return true;
                     }
                     else if (args[1].equalsIgnoreCase("list"))
                     {
                         List<String> cats = funcs.getCats();
                         sender.sendMessage(ExpSkills.lang.getString("help.list", "List all avaible skills."));
                         sender.sendMessage(ExpSkills.lang.getString("help.listfilter", "Possible filter:"));
                         for (String cat : cats)
                             sender.sendMessage(cat);
                         
                         return true;
                     }
                     else if (args[1].equalsIgnoreCase("info"))
                     {
                         sender.sendMessage(ExpSkills.lang.getString("help.info", "Shows you more detailed infos about a skill"));
                         return true;
                     }
                     else if (args[1].equalsIgnoreCase("buy"))
                     {
                         sender.sendMessage(ExpSkills.lang.getString("help.buy", "Buy this skill"));
                         return true;
                     }
                     else if (args[1].equalsIgnoreCase("rent"))
                     {
                         sender.sendMessage(ExpSkills.lang.getString("help.rent", "Rent this skill"));
                         return true;
                     }
                     else if (args[1].equalsIgnoreCase("current"))
                     {
                         sender.sendMessage(ExpSkills.lang.getString("help.current", "Show you all bought skills"));
                         return true;
                     }
                     if (p == null || p.hasPermission("expskills.admin"))
                     {
                         if (args[1].equalsIgnoreCase("set"))
                         {
                             sender.sendMessage(ExpSkills.lang.getString("help.set", "Set the stats off a player"));
                             return true;
                         }
                         else if (args[1].equalsIgnoreCase("add"))
                         {
                             sender.sendMessage(ExpSkills.lang.getString("help.add", "Modify stats of a player"));
                             return true;
                         }
                         else if (args[1].equalsIgnoreCase("revoke"))
                         {
                             sender.sendMessage(ExpSkills.lang.getString("help.revoke", "revokes a skill from a player"));
                             return true;
                         }
                         else if (args[1].equalsIgnoreCase("grant"))
                         {
                             sender.sendMessage(ExpSkills.lang.getString("help.grant", "grants a skill to a player"));
                             return true;
                         }
                         else if (args[1].equalsIgnoreCase("reset"))
                         {
                             sender.sendMessage(ExpSkills.lang.getString("help.reset", "resets a player's skills"));
                             return true;
                         }
                     }
                     else
                     {
                         sender.sendMessage("/exp help [command] - " + ExpSkills.lang.getString("command.help", "get help!"));
                         sender.sendMessage("/exp stats - " + ExpSkills.lang.getString("command.stats", "show's your stats"));
                         sender.sendMessage("/exp list <page> <filter> - " + ExpSkills.lang.getString("command.list", "list avaible skills"));
                         sender.sendMessage("/exp info <skill> - " + ExpSkills.lang.getString("command.info", "get information about a skill!"));
                         sender.sendMessage("/exp buy <skill> - " + ExpSkills.lang.getString("command.buy", "buy a skill"));
                         sender.sendMessage("/exp rent <skill> - " + ExpSkills.lang.getString("command.rent", "rent a skill"));
                         sender.sendMessage("/exp current - " + ExpSkills.lang.getString("command.current", "show's your current skills"));
                         if (p == null || p.hasPermission("expskills.admin"))
                         {
                             sender.sendMessage("===== Admin Commands =====");
                             sender.sendMessage("/exp stats - " + ExpSkills.lang.getString("command.stats", "show's your stats"));
                             sender.sendMessage("/exp <set/add> <player> <xp/level/skill> <amount> - " + ExpSkills.lang.getString("command.setadd", "modify a player's stats"));
                             sender.sendMessage("/exp <grant/revoke> <player> <skill> - " + ExpSkills.lang.getString("command.grantrevoke", "grant/revoke a skill"));
                             sender.sendMessage("/exp current <player> - " + ExpSkills.lang.getString("command.current", "show's your current skills"));
                             sender.sendMessage("/exp reset <player> - " + ExpSkills.lang.getString("command.reset", "resets all skills of a player"));
                         }
                     }
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("stats"))
                 {
                     if (args.length == 2)
                     {
                         Player player = funcs.getPlayer(args[1]);
                         if (player == null)
                         {
                             sender.sendMessage(ChatColor.RED + ExpSkills.lang.getString("error.pnotonline", "Player is not online"));
                             return true;
                         }
                         
                         funcs.updatePlaytime(player);
                         sender.sendMessage(player.getName() + "'s Stats:");
                         sender.sendMessage(ExpSkills.lang.getString("general.experience", "Experience") + ": " + player.getTotalExperience() + " (" + funcs.getXpToUp(player) + " " + ExpSkills.lang.getString("general.xptoup", "XP until LevelUp") + ")");
                         sender.sendMessage(ExpSkills.lang.getString("general.level", "Level") + ": " + funcs.getLevel(player));
                         sender.sendMessage(ExpSkills.lang.getString("general.skillpoints", "Skillpoints") + ": " + funcs.getSkillPoints(player));
                         sender.sendMessage(ExpSkills.lang.getString("general.playtime", "Playtime") + ": " + funcs.getPlaytime(player));
                         return true;
                     }
                     else
                     {
                         if (p == null)
                         {
                             sender.sendMessage("Consoles dont have experience!");
                             return true;
                         }
                         
                         funcs.updatePlaytime(p);
                         sender.sendMessage(sender.getName() + "'s Stats:");
                         sender.sendMessage(ExpSkills.lang.getString("general.experience", "Experience") + ": " + p.getTotalExperience() + " (" + funcs.getXpToUp(p) + " " + ExpSkills.lang.getString("general.xptoup", "XP until LevelUp") + ")");
                         sender.sendMessage(ExpSkills.lang.getString("general.level", "Level") + ": " + funcs.getLevel(p));
                         sender.sendMessage(ExpSkills.lang.getString("general.skillpoints", "Skillpoints") + ": " + funcs.getSkillPoints(p));
                         sender.sendMessage(ExpSkills.lang.getString("general.playtime", "Playtime") + ": " + funcs.getPlaytime(p));
                         return true;
                     }
                 }
                 if (args[0].equalsIgnoreCase("list"))
                 {
                     int page;
                     if (p != null)
                         if (args.length == 4)
                         {
                             try
                             {
                                 page = Integer.parseInt(args[1]);
                             }
                             catch (NumberFormatException ex)
                             {
                                 sender.sendMessage(ExpSkills.lang.getString("error.notvalid", "No valid argument"));
                                 return false;
                             }
                             String filter = args[2];
                             
                             if (args[3].equalsIgnoreCase("all"))
                                 funcs.getList(page, filter, p, true);
                             else
                                 funcs.getList(page, filter, p, false);
                             
                             return true;
                         }
                         else if (args.length == 3)
                         {
                             String filter;
                             try
                             {
                                 page = Integer.parseInt(args[1]);
                                 filter = args[2];
                             }
                             catch (NumberFormatException ex)
                             {
                                 page = 1;
                                 filter = args[1];
                             }
                             
                             if (args[2].equalsIgnoreCase("all"))
                                 funcs.getList(page, filter, p, true);
                             else
                                 funcs.getList(page, filter, p, false);
                             return true;
                         }
                         else if (args.length == 2)
                         {
                             try
                             {
                                 page = Integer.parseInt(args[1]);
                             }
                             catch (NumberFormatException ex)
                             {
                                 funcs.getList(1, args[1], p, false);
                                 return true;
                             }
                             
                             funcs.getList(page, null, p, false);
                             return true;
                         }
                         else if (args.length == 1)
                         {
                             funcs.getList(1, null, p, false);
                             return true;
                         }
                         else
                         {
                             sender.sendMessage(ExpSkills.lang.getString("error.toomuchlessarguments", "Too much/less arguments"));
                             return true;
                         }
                     sender.sendMessage("You are not a player!");
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("info"))
                 {
                     if (p == null)
                     {
                         sender.sendMessage("You are not a player!");
                         return true;
                     }
                     else if (args.length == 2)
                     {
                         funcs.getInfo(args[1], p);
                         return true;
                     }
                     else
                     {
                         sender.sendMessage(ExpSkills.lang.getString("error.skillnotfound", "Skill not found!"));
                         return true;
                     }
                 }
                 if (args[0].equalsIgnoreCase("buy"))
                 {
                     if (p != null)
                     {
                         if (args.length == 2)
                         {
                             if (funcs.buySkill(args[1], p))
                             {
                                 funcs.addSkill(p, funcs.getSkillKey(args[1]));
                                 p.sendMessage(ExpSkills.lang.getString("success.skillbought", "Skill successfully bought"));
                             }
                             return true;
                         }
                         else
                         {
                             p.sendMessage(ExpSkills.lang.getString("error.toomuchlessarguments", "Too much/less arguments"));
                             return true;
                         }
                     }
                     else
                     {
                         sender.sendMessage("The Console can't buy skills!");
                         return true;
                     }
                 }
                 if (args[0].equalsIgnoreCase("rent"))
                 {
                     if (p != null)
                     {
                         if (args.length == 3)
                         {
                             if (RentingManager.rentSkill(args[1], p, Integer.parseInt(args[2])))
                                 p.sendMessage(ExpSkills.lang.getString("success.skillrented", "Skill successfully rented"));
                             
                             return true;
                         }
                         if (args.length == 2)
                         {
                             if (RentingManager.rentSkill(args[1], p, -1))
                                 p.sendMessage(ExpSkills.lang.getString("success.skillrented", "Skill successfully rented"));
                             
                             return true;
                         }
                         else
                         {
                             p.sendMessage(ExpSkills.lang.getString("error.toomuchlessarguments", "Too much/less arguments"));
                             return true;
                         }
                     }
                     else
                     {
                         sender.sendMessage("The Console can't rent skills!");
                         return true;
                     }
                 }
                 if (args[0].equalsIgnoreCase("current"))
                 {
                     List<String> current = null;
                     
                     if (p != null && args.length == 1)
                         current = funcs.getCurrent(p);
                     else if (args.length == 2 && sender.hasPermission("expskills.current.others"))
                     {
                         Player player = funcs.getPlayer(args[1]);
                         if (player != null)
                             current = funcs.getCurrent(player);
                         else
                             sender.sendMessage("Player not online");
                     }
                     else
                     {
                         sender.sendMessage("Consoles dont have skills!");
                         return true;
                     }
                     
                     sender.sendMessage(ExpSkills.lang.getString("general.ownedskills", "Owned skills:"));
                     if (current != null)
                     {
                         int a = current.size();
                         for (int i = 0; i < a;)
                         {
                             if (a - i >= 3)
                             {
                                 sender.sendMessage(ExpSkills.config.getString("skills." + current.get(i) + ".name") + " " + ExpSkills.config.getString("skills." + current.get(i + 1) + ".name") + " " + ExpSkills.config.getString("skills." + current.get(i + 2) + ".name"));
                                 i = i + 3;
                             }
                             else if (a - i == 2)
                             {
                                 sender.sendMessage(ExpSkills.config.getString("skills." + current.get(i) + ".name") + " " + ExpSkills.config.getString("skills." + current.get(i + 1) + ".name"));
                                 i = i + 2;
                             }
                             else if (a - i == 1)
                             {
                                 sender.sendMessage(ExpSkills.config.getString("skills." + current.get(i) + ".name"));
                                 i = a;
                             }
                         }
                         return true;
                     }
                     else
                         sender.sendMessage(ExpSkills.lang.getString("error.notanyskillhe", "This player dont own any skill"));
                     
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("rented"))
                 {
                     List<String> rented = new ArrayList<String>();
                     
                     if (p != null && args.length == 1)
                        for (String skill : funcs.getRented(p))
                            rented.add(skill);
                     else if (args.length == 2 && sender.hasPermission("expskills.rented.others"))
                        for (String skill : funcs.getRented((Player) funcs.getOfflinePlayer(args[1])))
                            rented.add(skill);
                    
                     else
                     {
                         sender.sendMessage("Consoles dont have skills!");
                         return true;
                     }
                     
                     sender.sendMessage(ExpSkills.lang.getString("general.rentedskills", "Rented skills:"));
                    if (rented.size() != 0)
                     {
                         int a = rented.size();
                         for (int i = 0; i < a;)
                         {
                             if (a - i >= 3)
                             {
                                 sender.sendMessage(ExpSkills.config.getString("skills." + rented.get(i) + ".name") + " " + ExpSkills.config.getString("skills." + rented.get(i + 1) + ".name") + " " + ExpSkills.config.getString("skills." + rented.get(i + 2) + ".name"));
                                 i = i + 3;
                             }
                             else if (a - i == 2)
                             {
                                 sender.sendMessage(ExpSkills.config.getString("skills." + rented.get(i) + ".name") + " " + ExpSkills.config.getString("skills." + rented.get(i + 1) + ".name"));
                                 i = i + 2;
                             }
                             else if (a - i == 1)
                             {
                                 sender.sendMessage(ExpSkills.config.getString("skills." + rented.get(i) + ".name"));
                                 i = a;
                             }
                             
                         }
                     }
                     else
                         sender.sendMessage(ExpSkills.lang.getString("error.notanyskillhe", "This player dont own any skill"));
                     
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("add") && (sender.hasPermission("expskills.admin") || sender.hasPermission("expskills.admin.add")))
                 {
                     if (args.length == 4)
                     {
                         int amount;
                         Player player = funcs.getPlayer(args[1]);
                         if (player == null)
                         {
                             sender.sendMessage(ChatColor.RED + ExpSkills.lang.getString("error.pnotonline", "Player is not online"));
                             return true;
                         }
                         
                         amount = Integer.parseInt(args[3]);
                         
                         if (args[2].equalsIgnoreCase("xp"))
                         {
                             funcs.setXP(player, player.getTotalExperience() + amount);
                             sender.sendMessage(ExpSkills.lang.getString("success.done", "Done"));
                         }
                         else if (args[2].equalsIgnoreCase("level"))
                         {
                             funcs.setLevel(player, funcs.getLevel(player) + amount);
                             sender.sendMessage(ExpSkills.lang.getString("success.done", "Done"));
                         }
                         else if (args[2].equalsIgnoreCase("skill"))
                         {
                             funcs.addSkillPoints(player, amount);
                             sender.sendMessage(ExpSkills.lang.getString("success.done", "Done"));
                         }
                     }
                     else
                     {
                         sender.sendMessage(ExpSkills.lang.getString("error.toomuchlessarguments", "Too much/less arguments"));
                     }
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("set") && (sender.hasPermission("expskills.admin") || sender.hasPermission("expskills.admin.set")))
                 {
                     if (args.length == 4)
                     {
                         int amount;
                         Player player = funcs.getPlayer(args[1]);
                         
                         amount = Integer.parseInt(args[3]);
                         
                         if (args[2].equalsIgnoreCase("xp"))
                         {
                             funcs.setXP(player, amount);
                             sender.sendMessage(ExpSkills.lang.getString("success.done", "Done"));
                         }
                         else if (args[2].equalsIgnoreCase("level"))
                         {
                             funcs.setLevel(player, amount);
                             sender.sendMessage(ExpSkills.lang.getString("success.done", "Done"));
                         }
                         else if (args[2].equalsIgnoreCase("skill"))
                         {
                             funcs.setSkillPoints(player, amount);
                             sender.sendMessage(ExpSkills.lang.getString("success.done", "Done"));
                         }
                     }
                     else
                         sender.sendMessage(ExpSkills.lang.getString("error.toomuchlessarguments", "Too much/less arguments"));
                     
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("grant") && (sender.hasPermission("expskills.admin") || sender.hasPermission("expskills.admin.grant")))
                 {
                     if (args.length > 2 && args.length < 5)
                     {
                         Player player = funcs.getPlayer(args[1]);
                         
                         if (player != null)
                         {
                             if (funcs.grantSkill(player, args[2]))
                                 sender.sendMessage(ExpSkills.lang.getString("success.granted", "Skill successfully granted"));
                             else
                                 sender.sendMessage(ExpSkills.lang.getString("error.skillnotfound", "Skill is not existing"));
                         }
                         else
                             sender.sendMessage(ChatColor.RED + ExpSkills.lang.getString("error.pnotonline", "Player is not online"));
                     }
                     else
                         sender.sendMessage(ExpSkills.lang.getString("error.toomuchlessarguments", "Too much/less arguments"));
                     
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("revoke") && (sender.hasPermission("expskills.admin") || sender.hasPermission("expskills.admin.revoke")))
                 {
                     if (args.length > 2 && args.length < 5)
                     {
                         Player player = funcs.getPlayer(args[1]);
                         
                         if (player != null)
                         {
                             if (funcs.revokeSkill(player, args[2]))
                                 sender.sendMessage(ExpSkills.lang.getString("success.revoked", "Skill successfully revoked"));
                             else
                                 sender.sendMessage(ExpSkills.lang.getString("error.skillnotfound", "Skill is not existing"));
                         }
                         else
                             sender.sendMessage(ChatColor.RED + ExpSkills.lang.getString("error.pnotonline", "Player is not online"));
                     }
                     else
                         sender.sendMessage(ExpSkills.lang.getString("error.toomuchlessarguments", "Too much/less arguments"));
                     
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("reset") && (sender.hasPermission("expskills.admin") || sender.hasPermission("expskills.admin.reset")))
                 {
                     Player player = ExpSkills.server.getPlayerExact(args[1]);
                     if (player != null)
                     {
                         if (args.length == 2)
                         {
                             funcs.reset(player, "skill");
                             sender.sendMessage(player.getName() + "'s " + ChatColor.RED + ExpSkills.lang.getString("success.reset", "skills were reset"));
                         }
                         else if (args.length == 3)
                         {
                             if (args[2].equalsIgnoreCase("total") || args[2].equalsIgnoreCase("level"))
                                 funcs.reset(player, args[2]);
                             else
                                 sender.sendMessage(ExpSkills.lang.getString("error.notvalid", "No valid argument"));
                         }
                         else
                             sender.sendMessage(ExpSkills.lang.getString("error.toomuchlessarguments", "Too much/less arguments"));
                     }
                     else
                         sender.sendMessage(ChatColor.RED + ExpSkills.lang.getString("error.pnotfoundexact", "Player is not online. You need a exact match."));
                     
                     return true;
                 }
             }
             else
                 return false;
         }
         sender.sendMessage(ExpSkills.lang.getString("error.permissions", "You don't have the needed permissions"));
         return true;
     }
 }
