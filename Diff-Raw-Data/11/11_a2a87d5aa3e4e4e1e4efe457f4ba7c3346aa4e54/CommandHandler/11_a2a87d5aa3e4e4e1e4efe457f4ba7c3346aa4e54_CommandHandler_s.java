 /**
  * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
  *
  * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
  * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
  * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
  **/
 
 package com.herocraftonline.dev.heroes.command;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.*;
 
 public class CommandHandler {
     protected LinkedHashMap<String, Command> commands;
     protected HashMap<String, Command> identifiers;
     private Heroes plugin;
 
     public CommandHandler(Heroes plugin) {
         this.plugin = plugin;
         commands = new LinkedHashMap<String, Command>();
         identifiers = new HashMap<String, Command>();
     }
 
     public void addCommand(Command command) {
         commands.put(command.getName().toLowerCase(), command);
         for (String ident : command.getIdentifiers()) {
             identifiers.put(ident.toLowerCase(), command);
         }
     }
 
     public boolean dispatch(CommandSender sender, String label, String[] args) {
         for (int argsIncluded = args.length; argsIncluded >= 0; argsIncluded--) {
             StringBuilder identifierBuilder = new StringBuilder(label);
             for (int i = 0; i < argsIncluded; i++) {
                 identifierBuilder.append(' ').append(args[i]);
             }
 
             String identifier = identifierBuilder.toString();
             Command cmd = getCmdFromIdent(identifier, sender);
             if (cmd == null) {
                 continue;
             }
 
             String[] realArgs = Arrays.copyOfRange(args, argsIncluded, args.length);
 
             if (!cmd.isInProgress(sender)) {
                 if (realArgs.length < cmd.getMinArguments() || realArgs.length > cmd.getMaxArguments()) {
                     displayCommandHelp(cmd, sender);
                     return true;
                 } else if (realArgs.length > 0 && realArgs[0].equals("?")) {
                     displayCommandHelp(cmd, sender);
                     return true;
                 }
             }
 
             if (!hasPermission(sender, cmd.getPermission())) {
                 Messaging.send(sender, "Insufficient permission.");
                 return true;
             }
 
             cmd.execute(sender, identifier, realArgs);
             return true;
         }
 
         return true;
     }
 
     private void displayCommandHelp(Command cmd, CommandSender sender) {
         sender.sendMessage("§cCommand:§e " + cmd.getName());
        sender.sendMessage("§cDescription:§e " + cmd.getDescription());
         sender.sendMessage("§cUsage:§e " + cmd.getUsage());
         if (cmd.getNotes() != null) {
             for (String note : cmd.getNotes()) {
                 sender.sendMessage("§e" + note);
             }
         }
     }
 
     public Command getCmdFromIdent(String ident, CommandSender executor) {
         Skill skill = plugin.getSkillManager().getSkillFromIdent(ident, executor);
         if (skill != null) {
             return skill;
         }
 
         if (identifiers.get(ident.toLowerCase()) == null) {
             for (Command cmd : commands.values()) {
                 if (cmd.isIdentifier(executor, ident)) {
                     return cmd;
                 }
             }
         }
         return identifiers.get(ident.toLowerCase());
     }
 
     public Command getCommand(String name) {
         return commands.get(name.toLowerCase());
     }
 
     public List<Command> getCommands() {
         return new ArrayList<Command>(commands.values());
     }
 
     public void removeCommand(Command command) {
         commands.remove(command.getName().toLowerCase());
         for (String ident : command.getIdentifiers()) {
             identifiers.remove(ident.toLowerCase());
         }
     }
 
     public static boolean hasPermission(CommandSender sender, String permission) {
         if (!(sender instanceof Player) || permission == null || permission.isEmpty()) {
             return true;
         }
 
         Player player = (Player) sender;
 
         return player.isOp() || Heroes.perms.has(player, permission);
 
     }
 }
