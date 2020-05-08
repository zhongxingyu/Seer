 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of MineStarWarp.
  * 
  * MineStarWarp is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * MineStarWarp is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MineStarWarp.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.MineStarWarp.commands.teleport;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import de.minestar.MineStarWarp.Core;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class TeleportToCommand extends AbstractExtendedCommand {
 
     public TeleportToCommand(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
 
         this.description = "Teleportiert dich zu einem Spieler";
     }
 
     @Override
     /**
      * Representing the command <br>
      * /tphere PLAYERNAME (TARGETSNAME) <br>
      * This teleports first player to the other player
      * 
      * @param player
      *            Called the command
      * @param args
      *            args[0] is the player to teleport OR addionatiol <br>
      *            args[1] is the targets name
      * 
      */
     public void execute(String[] args, Player player) {
         // tp <Player>
         if (args.length == 1)
             teleportToPlayer(args, player);
         // tp <Player> <Target>
         else if (args.length == 2)
             teleportPlayerToPlayer(args, player);
         // tp x y z or /tp x y z world
         else if (args.length == 3 || args.length == 4)
             teleportToCoords(args, player);
         else
             player.sendMessage(getHelpMessage());
     }
 
     private void teleportToPlayer(String[] args, Player player) {
 
         if (!checkSpecialPermission(player, "minestarwarp.command.tpTo"))
             return;
 
         String targetName = args[0];
 
         Player target = PlayerUtils.getOnlinePlayer(targetName);
         if (target == null) {
             PlayerUtils.sendError(player, pluginName, "Der Spieler '" + targetName + "' wurde nicht gefunden. Vielleicht ist er offline?");
             return;
         }
 
         player.teleport(target.getLocation());
         PlayerUtils.sendSuccess(player, pluginName, "Du hast dich zum Spieler '" + target.getName() + "' teleportiert!");
     }
 
     private void teleportPlayerToPlayer(String[] args, Player player) {
 
         if (!checkSpecialPermission(player, "minestarwarp.command.tpPlayerTo"))
             return;
 
         String playerName = args[0];
         String targetName = args[1];
 
         Player playerToTeleport = PlayerUtils.getOnlinePlayer(playerName);
         if (playerToTeleport == null) {
             PlayerUtils.sendError(player, pluginName, "Der Spieler '" + playerName + "' wurde nicht gefunden. Vielleicht ist er offline?");
             return;
         }
         Player target = PlayerUtils.getOnlinePlayer(args[1]);
         if (target == null) {
             PlayerUtils.sendError(player, pluginName, "Der Spieler '" + targetName + "' wurde nicht gefunden. Vielleicht ist er offline?");
             return;
         }
         playerToTeleport.teleport(target);
 
         // Information for players
         PlayerUtils.sendSuccess(playerToTeleport, pluginName, "Du wurdest zum Spieler '" + target.getName() + "' teleportiert!");
         PlayerUtils.sendSuccess(target, pluginName, "Der Spieler '" + playerToTeleport.getName() + "' wurde zu dir teleportiert!");
 
         // Information for command executer
        PlayerUtils.sendSuccess(player, pluginName, "Der Spieler '" + target.getName() + "' wurde zu '" + playerToTeleport.getName() + "' teleportiert!");
     }
 
     private void teleportToCoords(String[] args, Player player) {
         if (!checkSpecialPermission(player, "minestarwarp.command.tptpcoords"))
             return;
 
         double x = 0.0;
         double y = 0.0;
         double z = 0.0;
         try {
             x = Double.parseDouble(args[0]);
             y = Double.parseDouble(args[1]);
             z = Double.parseDouble(args[2]);
         } catch (Exception e) {
             player.sendMessage(ChatColor.BLUE + "/tp X Y Z ");
             return;
         }
 
         World targetWorld = null;
         if (args.length == 4) {
             targetWorld = player.getServer().getWorld(args[4]);
             if (targetWorld == null) {
                 PlayerUtils.sendError(player, pluginName, "Die Welt '" + targetWorld + "' existiert nicht!");
                 return;
             }
         } else
             targetWorld = player.getWorld();
 
         player.teleport(new Location(targetWorld, x, y, z));
         PlayerUtils.sendSuccess(player, pluginName, "Du wurdest erfolgreich zur der Position X=" + x + " Y=" + y + " Z=" + z + " in der Welt " + targetWorld.getName());
     }
 }
