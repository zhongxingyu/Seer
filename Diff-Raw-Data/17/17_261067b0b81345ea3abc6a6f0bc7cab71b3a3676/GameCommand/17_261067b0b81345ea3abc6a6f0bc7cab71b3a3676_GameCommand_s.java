 package me.cmastudios.ironball.command;
 
 import me.cmastudios.ironball.Arena;
 import me.cmastudios.ironball.Game;
 import me.cmastudios.ironball.IronBall;
 import me.cmastudios.ironball.TeamType;
 import me.cmastudios.ironball.task.GameStartTimer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class GameCommand implements CommandExecutor {
 
     private final IronBall plugin;
 
     public GameCommand(IronBall plugin) {
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (args.length < 1) {
             sender.sendMessage(IronBall.getString("OPT.NOTENOUGH", new Object[] {}));
             return false;
         }
         Player player = plugin.getServer().getPlayerExact(sender.getName());
         String optArg = args[0];
         Option option = Option.valueOf(optArg.toUpperCase());
         Arena arena = null;
         if (args.length >= 2) {
             arena = plugin.getDatabase().find(Arena.class).where().ieq("name", args[1]).findUnique();
         }
         switch (option) {
             case START:
                 if (sender instanceof Player) {
                     Game game = plugin.getGameByPlayer((Player)sender);
                     if (game != null) {
                         game.removePlayer((Player)sender);
                     }
                 }
                 if (arena == null) {
                     sender.sendMessage(IronBall.getString("OPT.NOTENOUGH", new Object[] {}));
                     return false;
                 }
                 for (Game game : plugin.activeGames) {
                     if (game.getArena().equals(arena)) {
                         sender.sendMessage(IronBall.getString("GAME.ALREADYINPROGRESS", new Object[] {}));
                         return true;
                     }
                 }
                 Game startGame = new Game(arena);
                 plugin.activeGames.add(startGame);
                 startGame.startGame(plugin);
                plugin.getServer().broadcastMessage(IronBall.getPrefix() + IronBall.getString("GAME.ANNOUNCE", new Object[]{sender.getName(), arena.getName()}));
                 break;
             case PLAY:
                 if (player == null) {
                     sender.sendMessage("Can't find player " + sender.getName());
                     return true;
                 }
                 if (arena == null) {
                     sender.sendMessage(IronBall.getString("OPT.NOTENOUGH", new Object[] {}));
                     return false;
                 }
                 Game playGame = plugin.getGameByArena(arena);
                 if (playGame == null) {
                     sender.sendMessage(IronBall.getString("GAME.NOTINPROGRESS", new Object[] {arena.getName()}));
                 } else {
                     playGame.addPlayerToTeam(player, playGame.autoAssign());
                 }
                 break;
             case LEAVE:
                 if (player == null) {
                     sender.sendMessage("Can't find player " + sender.getName());
                     return true;
                 }
                 Game leaveGame = plugin.getGameByPlayer(player);
                 if (leaveGame != null) {
                     leaveGame.broadcastMessage(IronBall.getString("GAME.PLAYERQUIT", new Object[] {player.getDisplayName()}));
                     leaveGame.removePlayer(player);
                     if (leaveGame.isEmpty()) {
                         leaveGame.broadcastMessage(IronBall.getPrefix() + IronBall.getString("GAME.END"));
                         leaveGame.endGame();
                         plugin.activeGames.remove(leaveGame);
                     } else if (leaveGame.areTeamsUnbalanced()) {
                         leaveGame.broadcastMessage(IronBall.getPrefix() + IronBall.getString("GAME.REBALANCE"));
                         leaveGame.balanceTeams();
                     }
                 }
                 break;
             default:
                 break;
         }
         return true;
     }
 
     private enum Option {
 
         LIST,
         START,
         PLAY,
         SPECTATE,
         LEAVE
     }
 }
