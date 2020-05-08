 /*
  * Author: Dabo Ross
  * Website: www.daboross.net
  * Email: daboross@daboross.net
  */
 package net.daboross.bukkitdev.commandexecutorbase;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 /**
  *
  * @author daboross
  */
 public class SubCommand {
 
     private final Set<CommandExecutorBase> commandExecutorBasesUsingThis;
     final SubCommandHandler commandHandler;
     final String commandName;
     final boolean playerOnly;
     final String permission;
     private final List<String> aliases;
     final List<String> aliasesUnmodifiable;
     final String helpMessage;
     private final List<String> argumentNames;
     final List<String> argumentNamesUnmodifiable;
     private ArgumentHandler argumentHandler;
 
     public SubCommand(final String commandName, final String[] aliases, final boolean canConsoleExecute, final String permission, final String[] argumentNames, String helpMessage, SubCommandHandler subCommandHandler) {
         if (commandName == null) {
             throw new IllegalArgumentException("Null commandName argument");
         } else if (subCommandHandler == null) {
             throw new IllegalArgumentException("Null subCommandHandler argument");
         }
         this.commandName = commandName.toLowerCase(Locale.ENGLISH);
         this.aliases = aliases == null ? new ArrayList<String>() : ArrayHelpers.copyToListLowercase(aliases);
         this.aliasesUnmodifiable = Collections.unmodifiableList(this.aliases);
         this.playerOnly = !canConsoleExecute;
         this.permission = permission;
         this.helpMessage = (helpMessage == null ? "" : helpMessage);
         this.argumentNames = argumentNames == null ? new ArrayList<String>() : ArrayHelpers.copyToList(argumentNames);
         this.argumentNamesUnmodifiable = Collections.unmodifiableList(this.argumentNames);
         this.commandHandler = subCommandHandler;
         this.commandExecutorBasesUsingThis = new HashSet<CommandExecutorBase>();
         this.argumentHandler = null;
     }
 
     public SubCommand(String cmd, String[] aliases, boolean isConsole, String permission, String helpString, SubCommandHandler commandHandler) {
         this(cmd, aliases, isConsole, permission, null, helpString, commandHandler);
     }
 
     public SubCommand(String cmd, boolean isConsole, String permission, String[] arguments, String helpString, SubCommandHandler commandHandler) {
         this(cmd, null, isConsole, permission, arguments, helpString, commandHandler);
     }
 
     public SubCommand(String cmd, boolean isConsole, String permission, String helpString, SubCommandHandler commandHandler) {
         this(cmd, null, isConsole, permission, null, helpString, commandHandler);
     }
 
     public void addAlias(String alias) {
         this.aliases.add(alias);
         for (CommandExecutorBase commandExecutorBase : commandExecutorBasesUsingThis) {
             commandExecutorBase.addAlias(this, alias);
         }
     }
 
     public void setArgumentHandler(ArgumentHandler argumentHandler) {
         this.argumentHandler = argumentHandler;
     }
 
     public String getName() {
         return commandName;
     }
 
     public String getHelpMessage(String baseCommandLabel) {
         return CommandExecutorBase.getHelpMessage(this, baseCommandLabel);
     }
 
     public String getHelpMessage(String baseCommandLabel, String subCommandLabel) {
        return CommandExecutorBase.getHelpMessage(this, subCommandLabel, baseCommandLabel);
     }
 
     void usingCommand(CommandExecutorBase commandExecutorBase) {
         commandExecutorBasesUsingThis.add(commandExecutorBase);
     }
 
     ArgumentHandler getArgumentHandler() {
         return argumentHandler;
     }
 }
