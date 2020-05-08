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
 
 package com.minestar.MineStarWarp.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import com.gemo.utils.UtilPermissions;
 import com.minestar.MineStarWarp.Main;
 import com.minestar.MineStarWarp.localization.LocalizationConstants;
 
 /**
  * Inhertitates from this class to create a new command. Just implement the
  * execute() function and add the command to the CommandList
  * 
  * @author Meldanor
  * 
  */
 public abstract class Command implements LocalizationConstants {
 
     public final static String NO_RIGHT = Main.localization.get(COMMAND_NO_PERMISSIONS);
     public final Server server;
 
     // Add this in every command to add an description
     protected String description = "";
     // Example : /warp create
     private String syntax;
     // Example : <Name>
     private String arguments;
     // Example : minestarwarp.create
     private String permissionNode;
 
     private final int argumentCount;
 
     /**
      * Just call super() in the inhertited classes. <br>
      * 
      * @param syntax
      *            Example : /warp create
      * @param arguments
      *            Example : <Name>
      * @param node
      *            Example : minestarwarp.create
      */
     public Command(String syntax, String arguments, String node, Server server) {
         this.syntax = syntax;
         this.arguments = arguments;
         this.server = server;
         this.permissionNode = node;
         this.argumentCount = countArguments();
     }
 
     /**
      * Call this command to run it functions. It checks at first whether the
      * player has enough rights to use this. Also it checks whether it uses the
      * correct snytax. If both is correct, the real function of the command is
      * called
      * 
      * @param args
      *            The arguments of this command
      * @param player
      *            The command caller
      */
     public void run(String[] args, Player player) {
         if (!hasRights(player)) {
             player.sendMessage(NO_RIGHT);
             return;
         }
 
         if (!hasCorrectSyntax(args)) {
             player.sendMessage(getHelpMessage());
             return;
         }
 
         execute(args, player);
     }
 
     /**
      * Add into this method everything the command should do.
      * 
      * @param args
      *            The arguments of the command
      * @param player
      *            The command caller
      */
     public abstract void execute(String[] args, Player player);
 
     /**
      * @param player
      *            The command caller
      * @return True when the player has enough rights to use the command
      */
     protected boolean hasRights(Player player) {
         return UtilPermissions.playerCanUseCommand(player,
                 "minestarwarp.command." + getPermissionNode());
     }
 
     /**
      * Compares the count of arguments has and the count of arguments the
      * command should have.
      * 
      * @param args
      *            The arguments of the command given by the command caller
      * @return True when both number of arugments are equal
      */
     protected boolean hasCorrectSyntax(String[] args) {
         return args.length == argumentCount;
     }
 
     /**
      * @return The description of the Command, useful for help
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * @return Syntax + Arguments + Description
      */
     public String getHelpMessage() {
        return ChatColor.BLUE + getSyntax() + " " + getArguments() + " "
                 + getDescription();
     }
 
     /**
      * @return The syntax (or label) of the Command
      */
     public String getSyntax() {
         return syntax;
     }
 
 /**
      * @return The arguments in one string. Every argument is labeld in '<' and '>'
      */
     public String getArguments() {
         return arguments;
     }
 
     /**
      * @return The permission node, like minestarwarp.create
      */
     public String getPermissionNode() {
         return permissionNode;
     }
 
     public int getArgumentCount() {
         return argumentCount;
     }
 
 /**
      * @return The number of '<' in the argument String
      */
     private int countArguments() {
 
         if (arguments.isEmpty())
             return 0;
 
         int counter = 0;
         for (int i = 0; i < arguments.length(); ++i)
             if (arguments.charAt(i) == '<')
                 ++counter;
         return counter;
     }
 }
