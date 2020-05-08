 package com.gmail.jameshealey1994.simplepvptoggle.commands;
 
 import com.gmail.jameshealey1994.simplepvptoggle.SimplePVPToggle;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * Command executor for the SimplePVPToggle plugin.
  * @author JamesHealey94 <jameshealey1994.gmail.com>
  */
 public class SimplePVPToggleCommandExecutor implements CommandExecutor {
 
     /**
      * Plugin the commands are executed for.
      */
     private SimplePVPToggle plugin;
     
     /**
      * The default command, executed when no arguments are given.
      * Can be null, in which case no command is executed.
      */
     private SimplePVPToggleCommand defaultCommand;
 
     /**
      * Constructor to set plugin instance variable.
      * @param plugin The plugin used to set internal plugin value
      * @param defaultCommand The default command, executed when no arguments are given.
      */
     public SimplePVPToggleCommandExecutor(SimplePVPToggle plugin, SimplePVPToggleCommand defaultCommand) {
         this.plugin = plugin;
         this.defaultCommand = defaultCommand;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {        
         if (args.length > 0) {
             for (SimplePVPToggleCommand command : plugin.getCommands()) {
                if (command.getAliases().contains(args[0].toLowerCase())) {
                     return command.execute(plugin, sender, label, args);
                 }
             }
         } else { // No args given
             if (defaultCommand == null) {
                 return false;
             } else {
                 return defaultCommand.execute(plugin, sender, label, args);
             }
         }
         return false;
     }
 }
