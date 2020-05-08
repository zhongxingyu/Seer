 package me.limebyte.battlenight.core.commands;
 
 import java.util.Arrays;
 
 import me.limebyte.battlenight.api.battle.Arena;
 import me.limebyte.battlenight.api.util.BattleNightCommand;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.Messenger.Message;
 
 import org.bukkit.command.CommandSender;
 
 public class CreateCommand extends BattleNightCommand {
 
     public CreateCommand() {
         super("Create");
 
         setLabel("create");
         setDescription("Creates a BattleNight Arena.");
         setUsage("/bn create <arena>");
         setPermission(CommandPermission.ADMIN);
         setAliases(Arrays.asList("addarena", "createarena"));
     }
 
     @Override
     protected boolean onPerformed(CommandSender sender, String[] args) {
         if (args.length < 1) {
             Messenger.tell(sender, Message.SPECIFY_ARENA);
             Messenger.tell(sender, Message.USAGE, getUsage());
             return false;
         } else {
             for (Arena arena : api.getArenas()) {
                 if (arena.getName().equals(args[0])) {
                     Messenger.tell(sender, "An Arena by that name already exists!");
                     return false;
                 }
             }
            api.registerArena(new Arena(args[0]));
             Messenger.tell(sender, "Arena " + args[0] + " created.");
             return true;
         }
     }
 
 }
