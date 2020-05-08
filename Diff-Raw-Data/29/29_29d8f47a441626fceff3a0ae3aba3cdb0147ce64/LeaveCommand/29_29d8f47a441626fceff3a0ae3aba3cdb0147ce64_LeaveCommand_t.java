 package me.limebyte.battlenight.core.commands;
 
 import java.util.Arrays;
 
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.battle.Lobby;
 import me.limebyte.battlenight.api.managers.SpectatorManager;
 import me.limebyte.battlenight.api.util.Message;
 import me.limebyte.battlenight.api.util.Messenger;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class LeaveCommand extends BattleNightCommand {
 
     protected LeaveCommand() {
         super("Leave");
 
         setLabel("leave");
         setDescription("Leave the Battle.");
         setUsage("/bn leave");
         setPermission(CommandPermission.USER);
         setAliases(Arrays.asList("l", "quit"));
     }
 
     @Override
     protected boolean onPerformed(CommandSender sender, String[] args) {
         Messenger messenger = api.getMessenger();
 
         if (sender instanceof Player) {
             Battle battle = api.getBattleManager().getBattle();
             Lobby lobby = api.getLobby();
             Player player = (Player) sender;
 
             SpectatorManager spectators = api.getSpectatorManager();
 
             if (battle.containsPlayer(player)) {
                 return battle.removePlayer(player);
             } else if (lobby.getPlayers().contains(player.getName())) {
                 lobby.removePlayer(player);
                 return true;
            } else if (spectators.getSpectators().contains(player.getName())) {
                 spectators.removeSpectator(player);
                 return true;
             } else {
                 messenger.tell(sender, Message.NOT_IN_BATTLE);
                 return false;
             }
         } else {
             messenger.tell(sender, Message.PLAYER_ONLY);
             return false;
         }
     }
 
 }
