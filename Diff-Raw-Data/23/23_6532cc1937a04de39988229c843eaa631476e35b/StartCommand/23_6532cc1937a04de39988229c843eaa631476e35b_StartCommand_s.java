 package vc.pvp.skywars.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import vc.pvp.skywars.controllers.PlayerController;
 import vc.pvp.skywars.game.GameState;
 import vc.pvp.skywars.player.GamePlayer;
 
 @CommandDescription("Starts a SkyWars game")
 @CommandPermissions("skywars.command.start")
 public class StartCommand implements CommandExecutor {
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         GamePlayer gamePlayer = PlayerController.get().get((Player) sender);
 
         if (!gamePlayer.isPlaying()) {
             sender.sendMessage("\247cYou are not in a SkyWars game");
         } else if (gamePlayer.getGame().getState() != GameState.WAITING) {
             sender.sendMessage("\247cGame is already started");
         } else if (gamePlayer.getGame().getPlayerCount() < 2) {
             sender.sendMessage("\247cNot enough players");
        } else if (!gamePlayer.getGame().isBuilt()) {
             sender.sendMessage("\247cArena is still under construction");
         } else {
             gamePlayer.getGame().onGameStart();
         }
 
         return true;
     }
 }
