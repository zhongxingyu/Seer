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
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.mgenterprises.java.bukkit.gmcfps.Core.GameManagement.Game;
 import org.mgenterprises.java.bukkit.gmcfps.Core.GameManagement.GameManager;
 
 /**
  *
  * @author Manuel Gauto
  */
 public class GameManagementCommands implements CommandExecutor {
 
     private GameManager gameManager;
 
     public GameManagementCommands(GameManager gameManager) {
         this.gameManager = gameManager;
     }
 
     @Override
     public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
         if (string.equalsIgnoreCase(Commands.JOIN.toString())) {
             return processJoinCommand(cs, args);
         }
         else if(string.equalsIgnoreCase(Commands.LEAVE.toString())){
             
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
 
                 return true;
             }
             else{
                 cs.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                 return true;
             }
         } else {
             cs.sendMessage(ChatColor.BLUE + "Command must be executed by a player");
             return true;
         }
     }
 }
