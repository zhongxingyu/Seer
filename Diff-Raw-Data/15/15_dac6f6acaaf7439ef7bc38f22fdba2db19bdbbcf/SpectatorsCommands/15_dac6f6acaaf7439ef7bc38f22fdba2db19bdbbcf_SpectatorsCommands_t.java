 package fr.aumgn.diamondrush.command;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 import fr.aumgn.bukkitutils.command.Commands;
 import fr.aumgn.diamondrush.DiamondRush;
 import fr.aumgn.diamondrush.exception.PlayerNotInGame;
 import fr.aumgn.diamondrush.game.Game;
 import fr.aumgn.diamondrush.game.Spectators;
 import fr.aumgn.diamondrush.game.Team;
 import fr.aumgn.diamondrush.region.TeamSpawn;
 import fr.aumgn.diamondrush.region.Totem;
 import fr.aumgn.diamondrush.stage.JoinStage;
 
 @NestedCommands(name = "diamondrush")
 public class SpectatorsCommands implements Commands {
 
     @Command(name = "watch")
     public void watchGame(Player player, CommandArgs args) {
         Game game = DiamondRush.getGame();
 
         if (game.contains(player)) {
             throw new CommandError("Vous êtes déjà dans la partie.");
         }
 
         if (game.getStage() instanceof JoinStage) {
             throw new CommandError(
                     "Impossible de passer en spectateur durant une phase de join.");
         }
 
         Spectators spectators = game.getSpectators();
         if (spectators.contains(player)) {
             throw new CommandError("Vous êtes déjà spectateur.");
         }
 
         game.sendMessage(player.getDisplayName() + ChatColor.YELLOW +
                 " est maintenant spectateur.");
         spectators.add(player);
         player.sendMessage(ChatColor.GREEN + "Vous êtes maintenant spectateur.");
     }
 
     private void ensureIsSpectator(Player player) {
         Game game = DiamondRush.getGame();
         if (!game.getSpectators().contains(player)) {
            throw new CommandError("Cette commande n'est utilisable que pour un spectateur.");
         }
     }
 
     private Player matchPlayer(String name) {
         List<Player> players = Bukkit.matchPlayer(name);
         if (players.size() > 1) {
             throw new CommandError("Plus d'un joueur trouvés avec le motif " + name + ".");
         } else if (players.size() < 1) {
             throw new CommandError("Aucun joueur trouvé.");
         }
 
         return players.get(0);
     }
 
     @Command(name = "unwatch", max = 1)
     public void unwatchGame(Player player, CommandArgs args) {
         Player spectator;
         if (args.length() == 0) {
             spectator = player;
         } else {
             spectator = matchPlayer(args.get(0));
         }
 
        ensureIsSpectator(spectator);
        Game game = DiamondRush.getGame();

         game.getSpectators().remove(spectator);
        spectator.sendMessage(ChatColor.GREEN + "Vous n'êtes plus spectateur.");
        game.sendMessage(spectator.getDisplayName() + ChatColor.YELLOW +
                 " n'est plus spectateur.");
     }
 
     @Command(name = "tp-player", min = 1, max = 1)
     public void tpPlayer(Player player, CommandArgs args) {
         ensureIsSpectator(player);
 
         Game game = DiamondRush.getGame();
 
         Player target = matchPlayer(args.get(0));
         if (!game.contains(target) || !game.getSpectators().contains(player)) {
             throw new PlayerNotInGame();
         }
         player.teleport(target);
         player.sendMessage(ChatColor.GREEN + "Poof !");
     }
 
     @Command(name = "tp-team", min = 1, max = 1, flags = "sf")
     public void tpTeam(Player player, CommandArgs args) {
         ensureIsSpectator(player);
 
         Game game = DiamondRush.getGame();
         String arg = args.get(0);
 
         Team team = game.getTeam(arg);
         if (args.hasFlag('f')) {
             Player foreman = team.getForeman();
             if (foreman == null) {
                 throw new CommandError("Cette équipe n'a pas encore de chef d'équipe.");
             }
             player.teleport(foreman);
         } else if (args.hasFlag('s')) {
             TeamSpawn spawn = team.getSpawn();
             if (spawn == null) {
                 throw new CommandError("Cette équipe n'a pas encore de spawn.");
             }
             Location loc = spawn.getTeleportLocation(game.getWorld(), game.getSpawn());
             player.teleport(loc);
         } else {
             Totem totem = team.getTotem();
             if (totem == null) {
                 throw new CommandError("Cette équipe n'a pas encore de totem.");
             }
             Location loc = totem.getTeleportLocation(game.getWorld(), game.getSpawn());
             player.teleport(loc);
         }
         player.sendMessage(ChatColor.GREEN + "Poof !");
     }
 }
