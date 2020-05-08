 package net.sacredlabyrinth.Phaed.TelePlusPlus.managers;
 
 import net.sacredlabyrinth.Phaed.TelePlusPlus.*;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public final class CommandManager implements CommandExecutor
 {
     private TelePlusPlus plugin;
     private HashMap<String, ChatBlock> chatBlocks = new HashMap<String, ChatBlock>();
 
     public CommandManager(TelePlusPlus plugin)
     {
         this.plugin = plugin;
     }
 
     /**
      * Return a new chat block for a player, overwriting old
      *
      * @param sender
      * @return
      */
     public ChatBlock getNewChatBlock(CommandSender sender)
     {
         ChatBlock cb = new ChatBlock();
 
         if (sender instanceof Player)
         {
             chatBlocks.put(sender.getName(), cb);
         }
         else
         {
             chatBlocks.put("console", cb);
         }
         return cb;
     }
 
     /**
      * @param sender
      * @param command
      * @param label
      * @param args
      * @return
      */
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
     {
         try
         {
             if (command.getName().equals("tp"))
             {
                 Player player = null;
 
                 if (sender instanceof Player)
                 {
                     player = (Player)sender;
                 }
 
                 if (args.length > 0)
                 {
                     if (args[0].equalsIgnoreCase("more"))
                     {
                         ChatBlock cb = chatBlocks.get(sender.getName());
 
                         if (cb.size() > 0)
                         {
                             ChatBlock.sendBlank(sender);
 
                             cb.sendBlock(sender, plugin.sm.getPageSize());
 
                             if (cb.size() > 0)
                             {
                                 ChatBlock.sendBlank(sender);
                                 ChatBlock.sendMessage(sender, ChatColor.DARK_GRAY + "Type /tp more to view next page.");
                             }
                             ChatBlock.sendBlank(sender);
 
                             return true;
                         }
 
                         ChatBlock.sendMessage(sender, ChatColor.GOLD + "Nothing more to see.");
                         return true;
                     }
                     else if (args.length == 3 && Helper.isNumber(args[0]) && Helper.isNumber(args[1]) && Helper.isNumber(args[2]) && plugin.pm.hasPermission(sender, plugin.pm.coords) && !plugin.sm.disableCoords)
                     {
                         World currentWorld = player.getWorld();
                         Location loc = new Location(currentWorld, Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]), player.getLocation().getYaw(), player.getLocation().getPitch());
 
                         if (!plugin.tm.teleport(player, loc))
                         {
                             player.sendMessage(ChatColor.RED + "No free space available for teleport");
                             return true;
                         }
 
                         String msg = player.getName() + " teleported to " + "[" + printWorld(loc.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";
 
                         if (plugin.sm.logCoords)
                         {
                             logTp(player, msg);
                         }
                         if (plugin.sm.notifyCoords)
                         {
                             notifyTp(player, msg);
                         }
                         if (plugin.sm.sayCoords)
                         {
                             player.sendMessage(ChatColor.DARK_PURPLE + "Teleported");
                         }
                         return true;
                     }
                     else if (args[0].equalsIgnoreCase("mass") && plugin.pm.hasPermission(sender, plugin.pm.mass) && !plugin.sm.disableMass)
                     {
                         if (args.length == 1)
                         {
                             ArrayList<Entity> entities = new ArrayList<Entity>();
 
                             Player[] players = plugin.getServer().getOnlinePlayers();
 
                             for (Player teleportee : players)
                             {
                                 if (!canTP(sender, teleportee))
                                 {
                                     sender.sendMessage(ChatColor.RED + "No rights to summon " + teleportee.getName());
                                     continue;
                                 }
 
                                 entities.add(teleportee);
                             }
 
                             if (!plugin.tm.teleport(entities, player))
                             {
                                 player.sendMessage(ChatColor.RED + "No free space available for teleport");
                                 return true;
                             }
 
                             String msg = player.getName() + " mass teleported all players to [" + printWorld(player.getWorld().getName()) + player.getLocation().getBlockX() + " " + player.getLocation().getBlockY() + " " + player.getLocation().getBlockZ() + "]";
 
                             if (plugin.sm.logMass)
                             {
                                 logTp(player, msg);
                             }
                             if (plugin.sm.notifyMass)
                             {
                                 notifyTp(player, msg);
                             }
                             if (plugin.sm.sayMass)
                             {
                                 player.sendMessage(ChatColor.DARK_PURPLE + "Mass teleported all players to your location");
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("here") && plugin.pm.hasPermission(sender, plugin.pm.here) && !plugin.sm.disableHere)
                     {
                         if (args.length >= 2)
                         {
                             Location loc = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
 
                             ArrayList<Entity> entities = new ArrayList<Entity>();
 
                             for (int i = 1; i < args.length; i++)
                             {
                                 Player teleportee = Helper.matchUniquePlayer(plugin, args[i]);
 
                                 if (teleportee != null)
                                 {
                                     if (plugin.sm.isNoCrossWorldTps() && !player.getWorld().equals(teleportee.getWorld()))
                                     {
                                         player.sendMessage(ChatColor.RED + "Sorry cannot to tp " + teleportee.getName() + " across worlds");
                                         continue;
                                     }
 
                                     if (!canTP(player, teleportee))
                                     {
                                         player.sendMessage(ChatColor.RED + "No rights to summon " + teleportee.getName());
                                         continue;
                                     }
 
                                     entities.add(teleportee);
                                 }
                                 else
                                 {
                                     player.sendMessage(ChatColor.RED + args[i] + " did not match a player");
                                 }
                             }
 
                             if (!plugin.tm.teleport(entities, player))
                             {
                                 player.sendMessage(ChatColor.RED + "No free space available for teleport");
                                 return true;
                             }
 
                             String msg = player.getName() + " summoned " + Helper.entityArrayString(entities) + " to [" + printWorld(loc.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";
 
                             if (plugin.sm.logHere)
                             {
                                 logTp(player, msg);
                             }
                             if (plugin.sm.notifyHere)
                             {
                                 notifyTp(player, msg);
                             }
                             if (plugin.sm.sayHere)
                             {
                                 player.sendMessage(ChatColor.DARK_PURPLE + "Summoned " + Helper.entityArrayString(entities));
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("top") && plugin.pm.hasPermission(sender, plugin.pm.top) && !plugin.sm.disableTop)
                     {
                         if (args.length == 1)
                         {
                             int y = player.getWorld().getHighestBlockYAt(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
                             Location loc = new Location(player.getWorld(), player.getLocation().getX(), y, player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
 
                             if (!plugin.tm.teleport(player, loc))
                             {
                                 player.sendMessage(ChatColor.RED + "No free space available for teleport");
                                 return true;
                             }
 
                             String msg = player.getName() + " moved to the top [" + printWorld(loc.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";
 
                             if (plugin.sm.logTop)
                             {
                                 logTp(player, msg);
                             }
                             if (plugin.sm.notifyTop)
                             {
                                 notifyTp(player, msg);
                             }
                             if (plugin.sm.sayTop)
                             {
                                 player.sendMessage(ChatColor.DARK_PURPLE + "Teleported to top");
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("up") && plugin.pm.hasPermission(sender, plugin.pm.up) && !plugin.sm.disableUp)
                     {
                         Location glassloc = player.getLocation();
 
                         int glassHeight = glassloc.getBlockY() + 10;
 
                         if (args.length == 2 && Helper.isInteger(args[1]))
                         {
                             glassHeight = glassloc.getBlockY() + Integer.parseInt(args[1]);
                         }
 
                         Block targetglass = player.getWorld().getBlockAt(glassloc.getBlockX(), glassHeight, glassloc.getBlockZ());
                         Location loc = new Location(player.getWorld(), glassloc.getX(), glassHeight + 1, glassloc.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
 
                         if (!plugin.gm.addGlassed(player, targetglass))
                         {
                             if (plugin.pm.hasPermission(sender, plugin.pm.top) && !plugin.sm.disableTop)
                             {
                                 int y = player.getWorld().getHighestBlockYAt(player.getLocation().getBlockX(), player.getLocation().getBlockZ());
                                 loc = new Location(player.getWorld(), player.getLocation().getX(), y, player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
                                 glassHeight = (int) Math.round(Helper.distance(glassloc, loc));
                             }
                             else
                             {
                                 player.sendMessage(ChatColor.RED + "No free space above you at that height");
                                 return true;
                             }
                         }
 
                         if (!plugin.tm.teleport(player, loc))
                         {
                             player.sendMessage(ChatColor.RED + "No free space available for teleport");
                             return true;
                         }
 
                         String msg = player.getName() + " moved up " + glassHeight + " blocks [" + printWorld(loc.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";
 
                         if (plugin.sm.logUp)
                         {
                             logTp(player, msg);
                         }
                         if (plugin.sm.notifyUp)
                         {
                             notifyTp(player, msg);
                         }
                         if (plugin.sm.sayUp)
                         {
                             player.sendMessage(ChatColor.DARK_PURPLE + "Teleported up");
                         }
                         return true;
                     }
                     else if (args[0].equalsIgnoreCase("above") && plugin.pm.hasPermission(sender, plugin.pm.above) && !plugin.sm.disableAbove)
                     {
                         if (args.length >= 2)
                         {
                             int height = 5;
 
                             if (args.length == 3 && (Helper.isInteger(args[2])))
                             {
                                 height = Integer.parseInt(args[2]);
                             }
 
                             Player target = Helper.matchUniquePlayer(plugin, args[1]);
 
                             if (target != null)
                             {
                                 if (!canTP(player, target))
                                 {
                                     player.sendMessage(ChatColor.RED + "No rights to teleport above " + target.getName());
                                     return true;
                                 }
 
                                 Location targetLoc = target.getLocation();
                                 int glassHeight = targetLoc.getBlockY() + height;
 
                                 Block targetglass = target.getWorld().getBlockAt(targetLoc.getBlockX(), glassHeight, targetLoc.getBlockZ());
 
                                 if (!plugin.gm.addGlassed(player, targetglass))
                                 {
                                     player.sendMessage(ChatColor.RED + "No free space above you at that height");
                                     return true;
                                 }
 
                                 Location loc = new Location(player.getWorld(), targetLoc.getX(), glassHeight + 1, targetLoc.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
 
                                 if (!plugin.tm.teleport(player, loc))
                                 {
                                     player.sendMessage(ChatColor.RED + "No free space available for teleport");
                                     return true;
                                 }
 
                                 String msg = player.getName() + " teleported above " + target.getName() + " [" + printWorld(loc.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";
 
                                 if (plugin.sm.logAbove)
                                 {
                                     logTp(player, msg);
                                 }
                                 if (plugin.sm.notifyAbove)
                                 {
                                     notifyTp(player, msg);
                                 }
                                 if (plugin.sm.sayAbove)
                                 {
                                     player.sendMessage(ChatColor.DARK_PURPLE + "Teleported above " + target.getName());
                                 }
                             }
                             else
                             {
                                 player.sendMessage(ChatColor.RED + args[1] + " did not match a player");
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("jump") && plugin.pm.hasPermission(sender, plugin.pm.jump) && !plugin.sm.disableJump)
                     {
                         if (args.length == 1)
                         {
                             TargetBlock aiming = new TargetBlock(player, 1000, 0.2, plugin.sm.getThroughFieldsSet());
                             Block block = aiming.getTargetBlock();
 
                             if (block == null)
                             {
                                 player.sendMessage(ChatColor.RED + "Not pointing to valid block");
                             }
                             else
                             {
                                 double x = block.getX() + 0.5D;
                                 double y = block.getY() + 1;
                                 double z = block.getZ() + 0.5D;
                                 World world = block.getWorld();
 
                                 Location loc = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
 
                                 if (!plugin.tm.teleport(player, loc))
                                 {
                                     player.sendMessage(ChatColor.RED + "No free space available for teleport");
                                     return true;
                                 }
 
                                 String msg = player.getName() + " jumped to " + "[" + printWorld(loc.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";
 
                                 if (plugin.sm.logJump)
                                 {
                                     logTp(player, msg);
                                 }
                                 if (plugin.sm.notifyJump)
                                 {
                                     notifyTp(player, msg);
                                 }
                                 if (plugin.sm.sayJump)
                                 {
                                     player.sendMessage(ChatColor.DARK_PURPLE + "Jumped");
                                 }
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("toggle") && plugin.pm.hasPermission(sender, plugin.pm.toggle) && !plugin.sm.disableToggle)
                     {
                         if (args.length == 1)
                         {
                             boolean toggled = plugin.tgm.toggle(player);
 
                             String msg = player.getName() + " toggled teleports " + (toggled ? "off" : "on");
 
                             if (plugin.sm.logToggle)
                             {
                                 logTp(player, msg);
                             }
                             if (plugin.sm.notifyToggle)
                             {
                                 notifyTp(player, msg);
                             }
                             if (plugin.sm.sayToggle)
                             {
                                 player.sendMessage(ChatColor.DARK_PURPLE + "Toggled teleports " + (toggled ? "off" : "on"));
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("back") && plugin.pm.hasPermission(sender, plugin.pm.back) && !plugin.sm.disableBack)
                     {
                         if (args.length == 1)
                         {
                             Location location = TeleHistory.popLocation(player);
 
                             if (location == null)
                             {
                                 player.sendMessage(ChatColor.RED + "No locations in your teleport history");
                             }
                             else
                             {
                                 if (plugin.sm.isNoCrossWorldTps() && !player.getWorld().equals(location.getWorld()))
                                 {
                                     player.sendMessage(ChatColor.RED + "Sorry cannot to tp across worlds");
                                     return true;
                                 }
 
                                 player.teleport(location);
 
                                 String msg = player.getName() + " went back to " + "[" + printWorld(location.getWorld().getName()) + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + "]";
 
                                 if (plugin.sm.logBack)
                                 {
                                     logTp(player, msg);
                                 }
                                 if (plugin.sm.notifyBack)
                                 {
                                     notifyTp(player, msg);
                                 }
                                 if (plugin.sm.sayBack)
                                 {
                                     player.sendMessage(ChatColor.DARK_PURPLE + "Teleported back");
                                 }
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("clear") && plugin.pm.hasPermission(sender, plugin.pm.clear) && !plugin.sm.disableClear)
                     {
                         if (args.length == 1)
                         {
                             boolean bonecleared = plugin.mm.clearMovedBlock(player);
                             bonecleared = bonecleared || plugin.mm.clearMovedEntity(player);
 
                             if (bonecleared)
                             {
                                 player.sendMessage(ChatColor.DARK_PURPLE + "Your mover selection has been cleared");
                             }
 
                             if (TeleHistory.clearHistory(player))
                             {
                                 player.sendMessage(ChatColor.DARK_PURPLE + "Your history has been cleared");
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("origin") && plugin.pm.hasPermission(sender, plugin.pm.origin) && !plugin.sm.disableOrigin)
                     {
                         if (args.length == 1)
                         {
                             Location location = TeleHistory.origin(player);
 
                             if (location == null)
                             {
                                 player.sendMessage(ChatColor.RED + "No locations in your teleport history");
                             }
                             else
                             {
                                 if (plugin.sm.isNoCrossWorldTps() && !player.getWorld().equals(location.getWorld()))
                                 {
                                     player.sendMessage(ChatColor.RED + "Sorry cannot to tp across worlds");
                                     return true;
                                 }
 
                                 player.teleport(location);
 
                                 String msg = player.getName() + " returned to his origin location " + "[" + printWorld(location.getWorld().getName()) + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + "]";
 
                                 if (plugin.sm.logOrigin)
                                 {
                                     logTp(player, msg);
                                 }
                                 if (plugin.sm.notifyOrigin)
                                 {
                                     notifyTp(player, msg);
                                 }
                                 if (plugin.sm.sayOrigin)
                                 {
                                     player.sendMessage(ChatColor.DARK_PURPLE + "Teleported to origin");
                                 }
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("mover") && plugin.pm.hasPermission(sender, plugin.pm.mover) && !plugin.sm.disableMover)
                     {
                         if (args.length == 1)
                         {
                             if (plugin.im.PutItemInHand(player, Material.getMaterial(plugin.sm.moverItem)))
                             {
                                 if (plugin.sm.sayMover)
                                 {
                                     player.sendMessage(ChatColor.DARK_PURPLE + "You now have a " + Helper.friendlyBlockType(Material.getMaterial(plugin.sm.moverItem).toString()).toLowerCase());
                                 }
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("tool") && plugin.pm.hasPermission(sender, plugin.pm.tool) && !plugin.sm.disableTool)
                     {
                         if (args.length == 1)
                         {
                             if (plugin.im.PutItemInHand(player, Material.getMaterial(plugin.sm.toolItem)))
                             {
                                 if (plugin.sm.sayTool)
                                 {
                                     player.sendMessage(ChatColor.DARK_PURPLE + "You now have a " + Helper.friendlyBlockType(Material.getMaterial(plugin.sm.toolItem).toString()).toLowerCase());
                                 }
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("request") && plugin.pm.hasPermission(sender, plugin.pm.request) && !plugin.sm.disableRequest)
                     {
                         if (args.length > 4 && Helper.isNumber(args[1]) && Helper.isNumber(args[2]) && Helper.isNumber(args[3]))
                         {
                             if (!plugin.rm.existRequestTakers())
                             {
                                 player.sendMessage(ChatColor.RED + "There is no one around to take your request");
                                 return true;
                             }
 
                             String reason = "";
 
                             for (int i = 4; i < args.length; i++)
                             {
                                 reason += args[i] + " ";
                             }
                             reason = reason.trim();
 
                             plugin.rm.addRequest(player, reason, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
 
                             String msg = player.getName() + " requested to be tpd to " + "[" + printWorld(player.getWorld().getName()) + Integer.parseInt(args[1]) + " " + Integer.parseInt(args[2]) + " " + Integer.parseInt(args[3]) + "]";
 
                             if (plugin.sm.logRequest)
                             {
                                 logTp(player, msg);
                             }
                             if (plugin.sm.notifyRequest)
                             {
                                 notifyTp(player, msg);
                             }
                             if (plugin.sm.sayRequest)
                             {
                                 player.sendMessage(ChatColor.RED + "Your tp request has been sent");
                             }
                             return true;
                         }
 
                         if (args.length > 2)
                         {
                             Player targetplayer = Helper.matchUniquePlayer(plugin, args[1]);
 
                             if (targetplayer == null)
                             {
                                 player.sendMessage(ChatColor.RED + "There is no player with that name");
                                 return true;
                             }
 
                             if (!plugin.rm.existRequestTakers())
                             {
                                 player.sendMessage(ChatColor.RED + "There is no one around to take your request");
                                 return true;
                             }
 
                             String reason = "";
 
                             for (int i = 2; i < args.length; i++)
                             {
                                 reason += args[i] + " ";
                             }
                             reason = reason.trim();
 
                             plugin.rm.addRequest(player, reason, targetplayer);
 
                             String msg = player.getName() + " requested to be tpd to " + targetplayer.getName() + " [" + printWorld(player.getWorld().getName()) + targetplayer.getLocation().getBlockX() + " " + targetplayer.getLocation().getBlockY() + " " + targetplayer.getLocation().getBlockZ() + "]";
 
                             if (plugin.sm.logRequest)
                             {
                                 logTp(player, msg);
                             }
                             if (plugin.sm.notifyRequest)
                             {
                                 notifyTp(player, msg);
                             }
                             if (plugin.sm.sayRequest)
                             {
                                 player.sendMessage(ChatColor.DARK_PURPLE + "Your tp request has been sent");
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("take") && plugin.pm.hasPermission(sender, plugin.pm.request) && !plugin.sm.disableRequest)
                     {
                         if (args.length == 1)
                         {
                             Request req = plugin.rm.takeRequest(sender);
 
                             if (req == null)
                             {
                                 sender.sendMessage(ChatColor.RED + "All tp requests have been taken");
                                 return true;
                             }
 
                             Player playername = Helper.matchUniquePlayer(plugin, req.getPlayerName());
 
                             if (playername == null)
                             {
                                 sender.sendMessage(ChatColor.DARK_PURPLE + "The player is no longer online");
                                 plugin.rm.finishTakenRequest(req);
                                 return true;
                             }
 
                             playername.sendMessage(ChatColor.DARK_PURPLE + "Your request has been taken");
 
                             ChatBlock.sendMessage(sender, ChatColor.DARK_PURPLE + "[tp] " + ChatColor.WHITE + "[" + req.getPlayerName() + "] " + ChatColor.YELLOW + "requests tp to " + ChatColor.WHITE + (req.getLocation() != null ? Helper.formatLocation(req.getLocation()) : "[" + req.getTargetName() + "]"));
                             ChatBlock.sendMessage(sender, ChatColor.DARK_PURPLE + "[tp] " + ChatColor.YELLOW + "Reason: " + req.getReason());
                             ChatBlock.sendMessage(sender, ChatColor.DARK_PURPLE + "[tp] " + ChatColor.GREEN + "/tp accept" + ChatColor.YELLOW + " or " + ChatColor.RED + "/tp deny");
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("accept") && plugin.pm.hasPermission(sender, plugin.pm.request) && !plugin.sm.disableRequest)
                     {
                         if (args.length == 1)
                         {
                             Request req = plugin.rm.retrieveTakenRequest(sender);
 
                             if (req != null)
                             {
                                 Player playername = Helper.matchUniquePlayer(plugin, req.getPlayerName());
 
                                 if (playername == null)
                                 {
                                     sender.sendMessage(ChatColor.DARK_PURPLE + "The player is no longer online");
                                     return true;
                                 }
 
                                 if (req.getLocation() != null)
                                 {
                                     playername.teleport(req.getLocation());
                                     playername.sendMessage(ChatColor.DARK_PURPLE + "Your tp request has been " + ChatColor.GREEN + "accepted");
                                 }
                                 else
                                 {
                                     Player targetplayer = Helper.matchUniquePlayer(plugin, req.getTargetName());
 
                                     if (targetplayer == null)
                                     {
                                         sender.sendMessage(ChatColor.DARK_PURPLE + "The target player is no longer online");
                                         playername.sendMessage(ChatColor.DARK_PURPLE + "The target player is no longer online");
                                         plugin.rm.finishTakenRequest(req);
                                         return true;
                                     }
 
                                     playername.teleport(targetplayer);
                                 }
 
                                 plugin.rm.finishTakenRequest(req);
 
                                 String msg = sender.getName() + " accepted " + playername.getName() + "'s request";
 
                                 if (plugin.sm.logRequest)
                                 {
                                     logTp(sender, msg);
                                 }
                                 if (plugin.sm.notifyRequest)
                                 {
                                     notifyTp(sender, msg);
                                 }
                                 if (plugin.sm.sayRequest)
                                 {
                                     sender.sendMessage(ChatColor.DARK_PURPLE + playername.getName() + " has been teleported.");
                                 }
                             }
                             else
                             {
                                 sender.sendMessage(ChatColor.RED + "You have not taken a request");
                                 return true;
                             }
                             return true;
                         }
                     }
                     else if (args[0].equalsIgnoreCase("deny") && plugin.pm.hasPermission(sender, plugin.pm.request) && !plugin.sm.disableRequest)
                     {
                         if (args.length == 1)
                         {
                             if (args.length == 1)
                             {
                                 Request req = plugin.rm.retrieveTakenRequest(sender);
 
                                 if (req != null)
                                 {
                                     Player playername = Helper.matchUniquePlayer(plugin, req.getPlayerName());
 
                                     if (playername == null)
                                     {
                                         sender.sendMessage(ChatColor.DARK_PURPLE + "The player is no longer online");
                                         plugin.rm.finishTakenRequest(req);
                                         return true;
                                     }
 
                                     plugin.rm.finishTakenRequest(req);
                                     String msg = sender.getName() + " denied " + playername.getName() + "'s request";
 
                                     if (plugin.sm.logRequest)
                                     {
                                         logTp(sender, msg);
                                     }
                                     if (plugin.sm.notifyRequest)
                                     {
                                         notifyTp(sender, msg);
                                     }
                                     if (plugin.sm.sayRequest)
                                     {
                                         sender.sendMessage(ChatColor.DARK_PURPLE + "You have denied " + playername.getName() + "'s request");
                                         playername.sendMessage(ChatColor.DARK_PURPLE + "Your tp request has been " + ChatColor.RED + "denied");
                                     }
                                 }
                                 else
                                 {
                                     sender.sendMessage(ChatColor.RED + "You have not taken a request");
                                     return true;
                                 }
                                 return true;
                             }
                         }
                     }
                     else
                     {
                         if (args.length == 1)
                         {
                             Player target = Helper.matchUniquePlayer(plugin, args[0]);
 
                             if (target != null)
                             {
                                 if (plugin.pm.hasPermission(sender, plugin.pm.player) && !plugin.sm.disablePlayer)
                                 {
                                     if (plugin.sm.isNoCrossWorldTps() && !player.getWorld().equals(target.getWorld()))
                                     {
                                         player.sendMessage(ChatColor.RED + "Sorry cannot to tp across worlds");
                                         return true;
                                     }
 
                                     if (!canTP(player, target))
                                     {
                                         player.sendMessage(ChatColor.RED + "No rights to teleport to " + target.getName());
                                         return true;
                                     }
 
                                     Location loc = new Location(target.getWorld(), target.getLocation().getX(), target.getLocation().getY(), target.getLocation().getZ(), target.getLocation().getYaw(), target.getLocation().getPitch());
 
                                     if (!plugin.tm.teleport(player, target))
                                     {
                                         player.sendMessage(ChatColor.RED + "No free space available for teleport");
                                         return true;
                                     }
 
                                     String msg = player.getName() + " teleported to " + target.getName() + " [" + printWorld(loc.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";
 
                                     if (plugin.sm.logPlayer)
                                     {
                                         logTp(player, msg);
                                     }
                                     if (plugin.sm.notifyPlayer)
                                     {
                                         notifyTp(player, msg);
                                     }
                                     if (plugin.sm.sayPlayer)
                                     {
                                         player.sendMessage(ChatColor.DARK_PURPLE + "Teleported to " + target.getName());
                                     }
                                     return true;
                                 }
                             }
                             else
                             {
                                 if (plugin.pm.hasPermission(sender, plugin.pm.world) && !plugin.sm.disableWorld)
                                 {
                                     World world = plugin.getServer().getWorld(args[0]);
 
                                     if (world == null)
                                     {
                                         player.sendMessage(ChatColor.RED + "Not a valid world or player.");
                                     }
                                     else
                                     {
                                         Location loc = new Location(world, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
 
                                         if (plugin.sm.isNoCrossWorldTps() && !player.getWorld().equals(loc.getWorld()))
                                         {
                                             player.sendMessage(ChatColor.RED + "Sorry cannot to tp across worlds");
                                             return true;
                                         }
 
                                         if (!plugin.tm.teleport(player, loc))
                                         {
                                             player.sendMessage(ChatColor.RED + "No free space available for teleport");
                                             return true;
                                         }
 
                                         String msg = player.getName() + " teleported to " + world.getName() + " [" + printWorld(loc.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";
 
                                         if (plugin.sm.logWorld)
                                         {
                                             logTp(player, msg);
                                         }
                                         if (plugin.sm.notifyWorld)
                                         {
                                             notifyTp(player, msg);
                                         }
                                         if (plugin.sm.sayWorld)
                                         {
                                             player.sendMessage(ChatColor.DARK_PURPLE + "Teleported to " + world.getName());
                                         }
                                         return true;
                                     }
                                 }
                                 return true;
                             }
                         }
 
                         int toLocation = Helper.wordLocation(args, "to");
 
                         if (toLocation > 0)
                         {
                             ArrayList<Entity> sources = new ArrayList<Entity>();
 
                             for (int i = 0; i < toLocation; i++)
                             {
                                 Player target = Helper.matchUniquePlayer(plugin, args[i]);
 
                                 if (target != null)
                                 {
                                     if (!canTP(sender, target))
                                     {
                                         sender.sendMessage(ChatColor.RED + "No rights to teleport to " + target.getName());
                                         continue;
                                     }
 
                                     sources.add(target);
                                 }
                                 else
                                 {
                                     sender.sendMessage(ChatColor.RED + args[i] + " did not match a player");
                                 }
                             }
 
                             if (sources.size() > 0)
                             {
                                 int targetCount = (args.length - 1) - toLocation;
 
                                 if (targetCount == 1 && plugin.pm.hasPermission(sender, plugin.pm.othersPlayer) && !plugin.sm.disableOthersPlayer)
                                 {
                                     if (Helper.matchUniquePlayer(plugin, args[toLocation + 1]) != null)
                                     {
                                         Player target = Helper.matchUniquePlayer(plugin, args[toLocation + 1]);
 
                                         if (player != null)
                                         {
                                             if (plugin.sm.isNoCrossWorldTps() && !player.getWorld().equals(target.getWorld()))
                                             {
                                                 player.sendMessage(ChatColor.RED + "Sorry cannot to tp across worlds");
                                                 return true;
                                             }
                                         }
 
                                         if (!canTP(sender, target))
                                         {
                                             sender.sendMessage(ChatColor.RED + "No rights to teleport " + target.getName());
                                             return true;
                                         }
 
                                         if (!plugin.tm.teleport(sources, target))
                                         {
                                             sender.sendMessage(ChatColor.RED + "No free space available for teleport");
                                             return true;
                                         }
 
                                         String msg = sender.getName() + " teleported " + Helper.entityArrayString(sources) + " to " + target.getName() + " [" + printWorld(target.getWorld().getName()) + target.getLocation().getBlockX() + " " + target.getLocation().getBlockY() + " " + target.getLocation().getBlockZ() + "]";
 
                                         if (plugin.sm.logOthersPlayer)
                                         {
                                             logTp(sender, msg);
                                         }
                                         if (plugin.sm.notifyOthersPlayer)
                                         {
                                             notifyTp(sender, msg);
                                         }
                                         if (plugin.sm.sayOthersPlayer)
                                         {
                                             ChatBlock.sendMessage(sender, ChatColor.DARK_PURPLE + "Teleported " + Helper.entityArrayString(sources) + " to " + target.getName());
                                         }
                                         return true;
                                     }
                                     else
                                     {
                                         sender.sendMessage(ChatColor.RED + "Target did not match any player");
                                     }
                                 }
                                 else if (targetCount == 3 && plugin.pm.hasPermission(sender, plugin.pm.othersCoords) && !plugin.sm.disableOthersCoords)
                                 {
                                     if (Helper.isNumber(args[toLocation + 1]) && Helper.isNumber(args[toLocation + 2]) && Helper.isNumber(args[toLocation + 3]))
                                     {
                                         int x = Integer.parseInt(args[toLocation + 1]);
                                         int y = Integer.parseInt(args[toLocation + 2]);
                                         int z = Integer.parseInt(args[toLocation + 3]);
 
                                         Location loc = new Location(player.getWorld(), x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
 
                                         if (plugin.sm.isNoCrossWorldTps() && !player.getWorld().equals(loc.getWorld()))
                                         {
                                             player.sendMessage(ChatColor.RED + "Sorry cannot to tp across worlds");
                                             return true;
                                         }
 
                                         if (!plugin.tm.teleport(sources, loc))
                                         {
                                             player.sendMessage(ChatColor.RED + "No free space available for teleport");
                                             return true;
                                         }
 
                                         String msg = player.getName() + " teleported " + Helper.entityArrayString(sources) + " to [" + printWorld(player.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";
 
                                         if (plugin.sm.logOthersCoords)
                                         {
                                             logTp(player, msg);
                                         }
                                         if (plugin.sm.notifyOthersCoords)
                                         {
                                             notifyTp(player, msg);
                                         }
                                         if (plugin.sm.sayOthersCoords)
                                         {
                                             ChatBlock.sendMessage(player, ChatColor.DARK_PURPLE + "Teleported " + Helper.entityArrayString(sources) + " to [" + x + " " + y + " " + z + "]");
                                         }
                                         return true;
                                     }
                                     else
                                     {
                                         player.sendMessage(ChatColor.RED + "Target are not valid coordinates");
                                     }
                                 }
                                 else
                                 {
                                     player.sendMessage(ChatColor.RED + "Target did not match any player and are not coordinates");
                                 }
                             }
                             else
                             {
                                 player.sendMessage(ChatColor.RED + "No one to teleport");
                             }
                         }
                         else if (args.length == 4 && Helper.isNumber(args[1]) && Helper.isNumber(args[2]) && Helper.isNumber(args[3]))
                         {
                             if (plugin.pm.hasPermission(sender, plugin.pm.world) && !plugin.sm.disableWorld)
                             {
                                 World world = plugin.getServer().getWorld(args[0]);
 
                                 if (world == null)
                                 {
                                     player.sendMessage(ChatColor.RED + "Not a valid world.");
                                 }
                                 else
                                 {
                                     int x = Integer.parseInt(args[1]);
                                     int y = Integer.parseInt(args[2]);
                                     int z = Integer.parseInt(args[3]);
 
                                     Location loc = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
 
                                     if (plugin.sm.isNoCrossWorldTps() && !player.getWorld().equals(loc.getWorld()))
                                     {
                                         player.sendMessage(ChatColor.RED + "Sorry cannot to tp across worlds");
                                         return true;
                                     }
 
                                     if (!plugin.tm.teleport(player, loc))
                                     {
                                         player.sendMessage(ChatColor.RED + "No free space available for teleport");
                                         return true;
                                     }
 
                                     String msg = player.getName() + " teleported across worlds to coords [" + printWorld(loc.getWorld().getName()) + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";
 
                                     if (plugin.sm.logWorld)
                                     {
                                         logTp(player, msg);
                                     }
                                     if (plugin.sm.notifyWorld)
                                     {
                                         notifyTp(player, msg);
                                     }
                                     if (plugin.sm.sayWorld)
                                     {
                                         player.sendMessage(ChatColor.DARK_PURPLE + "Teleported to " + world.getName() + " to [" + x + " " + y + " " + z + "]");
                                     }
                                     return true;
                                 }
                             }
                         }
                     }
                 }
 
                 if (plugin.pm.hasPermission(sender, plugin.pm.menu))
                 {
                     ChatBlock cb = getNewChatBlock(sender);
 
                     if (player != null)
                     {
                         if (plugin.pm.hasPermission(sender, plugin.pm.player) && !plugin.sm.disablePlayer)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp [player]" + ChatColor.DARK_PURPLE + " - Teleport to another player");
                         }
                     }
                     if (plugin.pm.hasPermission(sender, plugin.pm.othersPlayer) && !plugin.sm.disableOthersPlayer)
                     {
                         cb.addRow(ChatColor.WHITE + "  /tp [player(s)] to [player]" + ChatColor.DARK_PURPLE + " - Teleport players to player");
                     }
                     if (player != null)
                     {
                         if (plugin.pm.hasPermission(sender, plugin.pm.othersCoords) && !plugin.sm.disableOthersCoords)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp [player(s)] to [x] [y] [z]" + ChatColor.DARK_PURPLE + " - Teleport players to coords");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.coords) && !plugin.sm.disableCoords)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp [x] [y] [z]" + ChatColor.DARK_PURPLE + " - Teleport to coordinates");
                         }
 
                         if (plugin.pm.hasPermission(sender, plugin.pm.world) && !plugin.sm.disableWorld)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp [world] <x> <y> <z>" + ChatColor.DARK_PURPLE + " - Teleport to world");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.here) && !plugin.sm.disableHere)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp here [player(s)]" + ChatColor.DARK_PURPLE + " - Teleport players to you");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.mass) && !plugin.sm.disableMass)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp mass" + ChatColor.DARK_PURPLE + " - Teleport all players to you");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.top) && !plugin.sm.disableTop)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp top" + ChatColor.DARK_PURPLE + " - Teleport to the block highest above you");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.up) && !plugin.sm.disableUp)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp up <height>" + ChatColor.DARK_PURPLE + " - Teleport up on a glass block");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.above) && !plugin.sm.disableAbove)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp above [player] <height>" + ChatColor.DARK_PURPLE + " - Teleport above a player");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.jump) && !plugin.sm.disableJump)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp jump" + ChatColor.DARK_PURPLE + " - Teleport to the block you're looking at");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.toggle) && !plugin.sm.disableToggle)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp toggle" + ChatColor.DARK_PURPLE + " - Toggle teleporting to you on/off");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.back) && !plugin.sm.disableBack)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp back" + ChatColor.DARK_PURPLE + " - Teleport back to your previous locations");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.origin) && !plugin.sm.disableOrigin)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp origin" + ChatColor.DARK_PURPLE + " - Go back to where you were before all tps");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.clear) && !plugin.sm.disableClear)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp clear" + ChatColor.DARK_PURPLE + " - Clear your tp history and " + Helper.friendlyBlockType(Material.getMaterial(plugin.sm.moverItem).toString()).toLowerCase() + " selections");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.tool) && !plugin.sm.disableTool)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp tool" + ChatColor.DARK_PURPLE + " - Get a " + Helper.friendlyBlockType(Material.getMaterial(plugin.sm.toolItem).toString()).toLowerCase() + " to tp yourself around");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.mover) && !plugin.sm.disableMover)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp mover" + ChatColor.DARK_PURPLE + " - Get a " + Helper.friendlyBlockType(Material.getMaterial(plugin.sm.moverItem).toString()).toLowerCase() + " to tp others around");
                         }
                         if (plugin.pm.hasPermission(sender, plugin.pm.request) && !plugin.sm.disableRequest)
                         {
                             cb.addRow(ChatColor.WHITE + "  /tp request [player|x y z] [reason]" + ChatColor.DARK_PURPLE + " - Request tp");
                         }
                     }
 
                     if (cb.size() > 0)
                     {
                         ChatBlock.sendBlank(sender);
                         ChatBlock.saySingle(sender, ChatColor.LIGHT_PURPLE + "Tele++ " + plugin.getDescription().getVersion() + ChatColor.DARK_GRAY + " ----------------------------------------------------------------------------------");
                         ChatBlock.sendBlank(sender);
 
                         boolean more = cb.sendBlock(sender, plugin.sm.getPageSize());
 
                         if (more)
                         {
                             ChatBlock.sendBlank(sender);
                             ChatBlock.sendMessage(sender, ChatColor.DARK_GRAY + "Type /tp more to view next page.");
                         }
 
                         ChatBlock.sendBlank(sender);
                     }
                 }
                 return true;
             }
         }
         catch (Exception ex)
         {
             TelePlusPlus.log.severe("Command failure:" + ex.getMessage());
         }
 
         return false;
     }
 
     public void notifyTp(CommandSender sender, String msg)
     {
         if (!plugin.pm.hasPermission(sender, plugin.pm.bypassNotify))
         {
             for (Player player : plugin.getServer().getOnlinePlayers())
             {
                if (plugin.pm.hasPermission(player, plugin.pm.notify))
                 {
                     if (sender.getName().equals(player.getName()))
                     {
                         continue;
                     }
                     ChatBlock.sendMessage(player, ChatColor.DARK_GRAY + "Tele++: " + msg);
                 }
             }
         }
     }
 
     public void logTp(CommandSender sender, String msg)
     {
         if (!plugin.pm.hasPermission(sender, plugin.pm.bypassLog))
         {
             plugin.getServer().getConsoleSender().sendMessage(ChatColor.WHITE + "Tele++" + ChatColor.GRAY + ": " + msg);
         }
     }
 
     public String printWorld(String world)
     {
         if (!plugin.getServer().getWorlds().get(0).getName().equals(world))
         {
             return world + " ";
         }
 
         return "";
     }
 
     private boolean canTP(CommandSender sender, Player target)
     {
         if (plugin.pm.hasPermission(sender, plugin.pm.bypassNoTp))
         {
             return true;
         }
 
         if (plugin.pm.hasPermission(target, plugin.pm.noTp))
         {
             return false;
         }
 
         if (plugin.tgm.isDisabled(target))
         {
             return false;
         }
 
         return true;
     }
 }
