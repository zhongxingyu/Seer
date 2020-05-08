 /*
  * The MIT License
  *
  * Copyright 2013 Manuel Gauto.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.mgenterprises.java.bukkit.gmcfps.Core.BasicCommands;
 
 import java.util.HashMap;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.mgenterprises.java.bukkit.gmcfps.Core.GameManagement.Game;
 import org.mgenterprises.java.bukkit.gmcfps.Core.GameManagement.GameManager;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Teams.Team;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Weapons.Implementations.BasicRocketLauncher;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Weapons.Implementations.BasicSMG;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Weapons.Implementations.BasicShotgun;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Weapons.Implementations.BasicSniper;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Weapons.Implementations.Twa16GodWeapon;
 
 /**
  *
  * @author Manuel Gauto
  */
 public class GameManagementCommands implements CommandExecutor {
 
     private GameManager gameManager;
     private HashMap<String, Game> editing = new HashMap<String, Game>();
     private HashMap<String, String> editingTeam = new HashMap<String, String>();
 
     public GameManagementCommands(GameManager gameManager) {
         this.gameManager = gameManager;
     }
 
     @Override
     public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
         if (string.equalsIgnoreCase(Commands.JOIN.toString())) {
             return processJoinCommand(cs, args);
         } else if (string.equalsIgnoreCase(Commands.LEAVE.toString())) {
             return processLeaveCommand(cs, args);
         } else if (string.equalsIgnoreCase(Commands.GAME.toString())) {
             return processWizard(cs, args);
         }
         return false;
     }
 
     private boolean processJoinCommand(CommandSender cs, String[] args) {
         if (cs instanceof Player) {
             if (((Player) cs).hasPermission("gfps.join") || ((Player) cs).isOp()) {
                 if (args.length >= 1) {
                     Player p = (Player) cs;
                     Game g = gameManager.getGameByName(args[0]);
                     if (g == null) {
                         p.sendRawMessage(ChatColor.BLUE + "Please enter a valid game name!");
                     } else {
                         if (g.registerPlayer(p)) {
                             p.sendRawMessage(ChatColor.BLUE + "You have joined: " + g.getName());
                         } else {
                             p.sendRawMessage(ChatColor.BLUE + g.getName() + " is full!");
                         }
                         return true;
                     }
                 }
 
                 return false;
             } else {
                 cs.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                 return true;
             }
         } else {
             cs.sendMessage(ChatColor.BLUE + "Command must be executed by a player");
             return true;
         }
     }
 
     private boolean processLeaveCommand(CommandSender cs, String[] args) {
         if (cs instanceof Player) {
             if (((Player) cs).hasPermission("gfps.leave") || ((Player) cs).isOp()) {
                 Player p = (Player) cs;
                 Game g = gameManager.getGameByName(args[0]);
                 if (g == null) {
                     p.sendRawMessage(ChatColor.BLUE + "Please enter a valid game name!");
                 } else {
                     g.unregisterPlayer(p);
                     p.sendRawMessage(ChatColor.BLUE + "You have left your game!");
                 }
             }
         }
         return true;
     }
 
     private boolean processWizard(CommandSender cs, String[] args) {
         Player p = (Player) cs;
         if (!p.hasPermission("gfps.create") && !p.isOp()) {
             p.sendRawMessage(ChatColor.RED+"You do not have permission");
             return true;
         }
        if (args.length >= 2 && args[0].equals("create")) {
             Game newGame = new Game(gameManager.getPluginReference(), args[1]);
             this.editing.put(p.getName(), newGame);
             p.sendRawMessage(ChatColor.AQUA + "Game " + newGame.getName() + " was Created!");
             p.sendRawMessage(ChatColor.AQUA + "Next Step: " + " Set the lobby as the location you are at with " + ChatColor.GREEN + "/game setlobby");
         }
         if (args[0].equals("setlobby")) {
             Game newGame = this.editing.get(p.getName());
             newGame.getFPSCore().getSpawnManager().setLobby(p.getLocation());
             p.sendRawMessage(ChatColor.AQUA + "Game lobby set at your current location!");
             p.sendRawMessage(ChatColor.AQUA + "Next Step: " + "Decide if the game will be free for all using " + ChatColor.GREEN + "/game setffa [true/false]");
         }
         if (args.length > 2 && args[0].equals("setffa")) {
             Game newGame = this.editing.get(p.getName());
             if (args[1].contains("t")) {
                 p.sendRawMessage(ChatColor.AQUA + "Free for all was set to " + ChatColor.BLUE + "TRUE");
                 newGame.getFPSCore().getTeamManager().setFreeForAll(true);
                 p.sendRawMessage(ChatColor.AQUA + "Next Step: " + "Add teams using " + ChatColor.GREEN + "/game addteam [name]");
                 return true;
             }
             newGame.getFPSCore().getTeamManager().setFreeForAll(false);
             p.sendRawMessage(ChatColor.AQUA + "Free for all was set to " + ChatColor.RED + "FALSE");
             p.sendRawMessage(ChatColor.AQUA + "Next Step: " + "Add teams using " + ChatColor.GREEN + "/game addteam [name]");
         }
         if (args.length > 2 && args[0].equals("addteam")) {
             Game newGame = this.editing.get(p.getName());
             newGame.getFPSCore().getTeamManager().registerTeam(new Team(args[1]));
             p.sendRawMessage(ChatColor.AQUA + "Team " + args[1] + " was Added!");
             this.editingTeam.put(p.getName(), args[1]);
             p.sendRawMessage(ChatColor.AQUA + "Next Step: " + "Set the team spawn with " + ChatColor.GREEN + "/game setspawn");
         }
         if (args[0].equals("setspawn")) {
             Game newGame = this.editing.get(p.getName());
             Team edit = newGame.getFPSCore().getTeamManager().getTeam(this.editingTeam.get(p.getName()));
             edit.setSpawn(p.getLocation());
             this.editingTeam.remove(p.getName());
             p.sendRawMessage(ChatColor.AQUA + "Team spawn set at your current location!");
             p.sendRawMessage(ChatColor.AQUA + "Next Step: " + "Add more teams or finish the game with " + ChatColor.GREEN + "/game done");
         }
         if (args[0].equals("done")) {
             Game g = this.editing.get(p.getName());
             g.getFPSCore().getWeaponManager().registerWeapon(new BasicSMG(g.getFPSCore().getWeaponManager()));
             g.getFPSCore().getWeaponManager().registerWeapon(new BasicSniper(g.getFPSCore().getWeaponManager()));
             g.getFPSCore().getWeaponManager().registerWeapon(new BasicRocketLauncher(g.getFPSCore().getWeaponManager()));
             g.getFPSCore().getWeaponManager().registerWeapon(new BasicShotgun(g.getFPSCore().getWeaponManager()));
             g.getFPSCore().getWeaponManager().registerWeapon(new Twa16GodWeapon(g.getFPSCore().getWeaponManager()));
             this.editing.remove(p.getName());
             p.sendRawMessage(ChatColor.AQUA + g.getName()+" completed!");
         }
         return true;
     }
 }
