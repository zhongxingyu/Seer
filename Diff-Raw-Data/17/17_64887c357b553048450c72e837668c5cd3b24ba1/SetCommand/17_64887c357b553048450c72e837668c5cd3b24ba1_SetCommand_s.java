 package me.limebyte.battlenight.core.commands;
 
 import me.limebyte.battlenight.api.battle.Waypoint;
 import me.limebyte.battlenight.api.managers.ArenaManager;
 import me.limebyte.battlenight.api.util.BattleNightCommand;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.Messenger.Message;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SetCommand extends BattleNightCommand {
 
     protected SetCommand() {
         super("Set");
 
         setLabel("set");
         setDescription("Sets a BattleNight waypoint.");
         setUsage("/bn set <waypoint> [x] [y] [z] [world]");
         setPermission(CommandPermission.ADMIN);
     }
 
     @Override
     protected boolean onPerformed(CommandSender sender, String[] args) {
         if (args.length < 1) {
             Messenger.tell(sender, Message.SPECIFY_WAYPOINT);
             Messenger.tell(sender, Message.USAGE, getUsage());
             return false;
         } else {
             Waypoint waypoint = null;
             ArenaManager manager = api.getArenaManager();
 
             if (args[0].equalsIgnoreCase("lounge")) {
                 waypoint = manager.getLounge();
             } else if (args[0].equalsIgnoreCase("exit")) {
                 waypoint = manager.getExit();
             }
 
             if (waypoint != null) {
                 if (args.length == 1) {
                     if (sender instanceof Player) {
                         Player player = (Player) sender;
                         waypoint.setLocation(player.getLocation());
                        Messenger.tell(sender, Message.WAYPOINT_SET_CURRENT_LOC, waypoint);
                     } else {
                         Messenger.tell(sender, Message.SPECIFY_COORDINATE);
                         Messenger.tell(sender, Message.USAGE, getUsage());
                         return false;
                     }
                 } else if (args.length == 4 && sender instanceof Player) {
                     Player player = (Player) sender;
                     Location loc = parseArgsToLocation(args, player.getWorld());
                     waypoint.setLocation(loc);
                    Messenger.tell(sender, Message.WAYPOINT_SET_THIS_WORLD, waypoint, loc);
                 } else if (args.length == 5) {
                     if (Bukkit.getWorld(args[4]) != null) {
                         Location loc = parseArgsToLocation(args);
                         waypoint.setLocation(loc);
                        Messenger.tell(sender, Message.WAYPOINT_SET, waypoint, loc, loc.getWorld());
                     } else {
                         Messenger.tell(sender, Message.CANT_FIND_WORLD, args[4]);
                         return false;
                     }
                 } else {
                     Messenger.tell(sender, Message.INCORRECT_USAGE);
                     Messenger.tell(sender, Message.USAGE, getUsage());
                     return false;
                 }
                 return true;
             } else {
                 Messenger.tell(sender, Message.INVALID_WAYPOINT);
                 return false;
             }
         }
     }
 
     private Location parseArgsToLocation(String[] args) {
         int x = getInteger(args[1], -30000000, 30000000);
         int y = getInteger(args[2], 0, 256);
         int z = getInteger(args[3], -30000000, 30000000);
         World world = Bukkit.getWorld(args[4]);
 
         return new Location(world, x + 0.5, y, z + 0.5);
     }
 
     private Location parseArgsToLocation(String[] args, World world) {
         int x = getInteger(args[1], -30000000, 30000000);
         int y = getInteger(args[2], 0, 256);
         int z = getInteger(args[3], -30000000, 30000000);
 
         return new Location(world, x + 0.5, y, z + 0.5);
     }
 
 }
