 package me.limebyte.battlenight.core.commands;
 
 import java.util.Arrays;
 
 import me.limebyte.battlenight.api.battle.Arena;
 import me.limebyte.battlenight.api.battle.Waypoint;
 import me.limebyte.battlenight.api.util.BattleNightCommand;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.Messenger.Message;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class AddCommand extends BattleNightCommand {
 
     public AddCommand() {
         super("Add");
 
         setLabel("add");
         setDescription("Adds a spawnpoint to a Arena.");
         setUsage("/bn add <arena>");
         setPermission(CommandPermission.ADMIN);
         setAliases(Arrays.asList("addspawn", "setspawn"));
     }
 
     @Override
     protected boolean onPerformed(CommandSender sender, String[] args) {
         if (args.length < 1) {
             Messenger.tell(sender, Message.SPECIFY_ARENA);
             Messenger.tell(sender, Message.USAGE, getUsage());
             return false;
         } else {
             Arena arena = null;
 
             for (Arena a : api.getArenas()) {
                if (a.getName().equalsIgnoreCase(args[0])) {
                     arena = a;
                 }
             }
 
             if (arena == null) {
                 Messenger.tell(sender, "An Arena by that name does not exist!");
                 return false;
             }
             Waypoint point = new Waypoint();
             point.setLocation(((Player) sender).getLocation());
             arena.addSpawnPoint(point);
             Messenger.tell(sender, "Spawn point created.");
             return true;
         }
     }
 
 }
