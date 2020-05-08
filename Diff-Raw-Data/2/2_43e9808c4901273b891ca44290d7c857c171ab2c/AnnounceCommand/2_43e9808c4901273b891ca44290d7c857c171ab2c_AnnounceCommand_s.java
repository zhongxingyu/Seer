 package me.limebyte.battlenight.core.commands;
 
 import java.util.Arrays;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.util.Messaging;
 import me.limebyte.battlenight.core.util.Messaging.Message;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 public class AnnounceCommand extends BattleNightCommand {
 
     protected AnnounceCommand() {
         super("Announce");
 
         this.setLabel("announce");
        this.setDescription("Announces a message to all player in the Battle.");
         this.setUsage("/bn announce <message>");
         this.setPermission(CommandPermission.MODERATOR);
         this.setAliases(Arrays.asList("tellall"));
     }
 
     @Override
     protected boolean onPerformed(CommandSender sender, String[] args) {
         if (!BattleNight.getBattle().isInProgress() && !BattleNight.playersInLounge) {
             Messaging.tell(sender, Message.BATTLE_NOT_IN_PROGRESS);
             return false;
         }
 
         if (args.length < 1) {
             Messaging.tell(sender, Message.SPECIFY_MESSAGE);
             Messaging.tell(sender, Message.USAGE, getUsage());
             return false;
         }
 
         Messaging.tellEveryone(ChatColor.translateAlternateColorCodes('&', createString(args, 0)));
         return true;
     }
 
 }
