 /*
  * CommandManager.java
  * 
  * PrisonMine
  * Copyright (C) 2013 bitWolfy <http://www.wolvencraft.com> and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.wolvencraft.prison.mines;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.hooks.CommandHook;
 import com.wolvencraft.prison.mines.cmd.*;
 import com.wolvencraft.prison.mines.util.Message;
 
 /**
  * <b>Command manager</b><br />
  * Handles subcommands and current command sender
  * @author bitWolfy
  *
  */
 public enum CommandManager implements CommandHook {
     BLACKLIST (BlacklistCommand.class, "prison.mine.edit", true, "blacklist", "bl", "whitelist", "wl"),
     DEBUG(DebugCommand.class, "prison.mine.debug", true, "import", "debug", "setregion", "tp", "unload", "locale"),
     EDIT (EditCommand.class, "prison.mine.edit", true, "edit", "add", "+", "remove", "-", "delete", "del", "name", "link", "setparent", "cooldown", "setwarp"),
     FLAG (FlagCommand.class, "prison.mine.edit", true, "flag"),
     HELP (HelpCommand.class, null, true, "help"),
     INFO (InfoCommand.class, "prison.mine.info.time", true, "info"),
     LIST (ListCommand.class, "prison.mine.info.list", true, "list"),
     META (MetaCommand.class, "prison.mine.about", true, "meta", "about"),
     PROTECTION (ProtectionCommand.class, "prison.mine.edit", true, "protection", "prot"),
     RESET (ResetCommand.class, null, true, "reset"),
     SAVE (SaveCommand.class, "prison.mine.edit", false, "save", "create", "new"),
     TIME (TimeCommand.class, "prison.mine.info.time", true, "time"),
     TRIGGER (TriggerCommand.class, "prison.mine.edit", true, "trigger"),
     VARIABLES (VariablesCommand.class, "prison.mine.edit", true, "variables"),
     UTIL (UtilCommand.class, "prison.mine.admin", true, "reload", "saveall"),
     WARNING (WarningCommand.class, "prison.mine.edit", true, "warning");
     
     private static CommandSender sender = null;
     
     private BaseCommand command;
     private String permission;
     private boolean allowConsole;
     private List<String> alias;
     
     CommandManager(Class<?> command, String permission, boolean allowConsole, String... args) {
         try { this.command = (BaseCommand) command.newInstance(); }
         catch (InstantiationException e) { Message.log(Level.SEVERE, "Error while instantiating a command! InstantiationException"); return; }
         catch (IllegalAccessException e) { Message.log(Level.SEVERE, "Error while instantiating a command! IllegalAccessException"); return; }
         
         this.permission = permission;
         this.allowConsole = allowConsole;
         
         alias = new ArrayList<String>();
         for(String arg : args) {
             alias.add(arg);
         }
     }
     
     /**
      * Returns the command instance
      * @return Command instance
      */
     public BaseCommand get() {
         return command;
     }
     
     /**
      * Checks if the specified alias corresponds to the command
      * @param alias Alias to check
      */
     public boolean isCommand(String alias) {
         return this.alias.contains(alias);
     }
     
     /**
      * Returns the verbose help message for the command
      */
     public void getHelp() {
         command.getHelp();
     }
     
     /**
      * Returns the single-line help message for the command
      */
     public void getHelpLine() {
         command.getHelpLine();
     }
     
     /**
      * Returns the full list of aliases for the corresponding command
      * @return List of aliases
      */
     public List<String> getAlias() {
         List<String> temp = new ArrayList<String>();
         for(String str : alias) temp.add(str);
         return temp;
     }
     
     /**
      * Executes the corresponding command with specified arguments.<br />
      * Checks for player's permissions before passing the arguments to the command
      * @param args Arguments to pass on to the command
      */
     public boolean run(String[] args) {
         if(sender != null) {
             if(sender instanceof Player) Message.debug("Command issued by player: " + sender.getName());
             else if(sender instanceof ConsoleCommandSender) Message.debug("Command issued by CONSOLE");
             else Message.debug("Command issued by GHOSTS and WIZARDS");
         }
         if(!allowConsole && !(sender instanceof Player)) { Message.sendFormattedError(PrisonMine.getLanguage().ERROR_SENDERISNOTPLAYER); return false; }
         if(permission != null && (sender instanceof Player) && !sender.hasPermission(permission)) { Message.sendFormattedError(PrisonMine.getLanguage().ERROR_ACCESS); return false; }
         try {
             return command.run(args);
         } catch (Exception e) {
             Message.sendFormattedError("An internal error occurred while running the command", false);
             Message.log(Level.SEVERE, "=== An error occurred while executing command ===");
            Message.log(Level.SEVERE, "Version = " + PrisonMine.getInstance().getDescription().getVersion());
             Message.log(Level.SEVERE, "Exception = " + e.toString());
             Message.log(Level.SEVERE, "CommandSender = " + sender.getName());
             Message.log(Level.SEVERE, "isConsole = " + (sender instanceof ConsoleCommandSender));
             String fullArgs = ""; for(String arg : args) fullArgs += arg + " ";
             Message.log(Level.SEVERE, "Command: /mine " + fullArgs);
             Message.log(Level.SEVERE, "Permission = " + permission);
             if(permission != null)
             Message.log(Level.SEVERE, "hasPermission = " + sender.hasPermission(permission));
             Message.log(Level.SEVERE, "");
             Message.log(Level.SEVERE, "=== === === === === Error log === === === === ===");
             e.printStackTrace();
             Message.log(Level.SEVERE, "=== === === ===  End of error log  === === === ===");
             return false;
         }
     }
     
     /**
      * Executes the corresponding command with specified arguments.<br />
      * Checks for player's permissions before passing the arguments to the command.<br />
      * Wraps around <code>run(String[] arg)</code> with one argument.
      * @param args Arguments to pass on to the command
      */
     public boolean run(String arg) {
         String[] args = {"", arg};
         return run(args);
     }
     
     /**
      * Returns the CommandSender for the command that is currently being processed.
      * @return Command sender, or <b>null</b> if there is no command being processed
      */
     public static CommandSender getSender() {
         return sender;
     }
     
     /**
      * Sets the CommandSender to the one specified
      * @param sender Sender to be set
      */
     public static void setSender(CommandSender sender) {
         CommandManager.sender = sender;
     }
     
     /**
      * Resets the active command sender to null
      */
     public static void resetSender() {
         sender = null;
     }
 }
