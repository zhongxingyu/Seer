 package org.p000ison.dev.commandapi;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * Represents a Command
  */
 public class Command {
 
     private String usage, name;
 
     /**
      * A list of sub-commands, you can change this during runtime
      */
     private final List<Command> subCommands = new ArrayList<Command>();
     /**
      * A array of the identifiers to detect this command, you can NOT change this during runtime
      */
     private String[] identifiers;
     /**
      * A List of the arguments for this command, you can NOT change this during runtime
      */
     private final List<Argument> arguments = new ArrayList<Argument>();
     private int maxArguments, minArguments;
 
     /**
      * A List of the callMethods for this command, you can change this during runtime
      * If this command is being executed, those methods get called, too (Aliases)
      */
     private final List<Command> callMethods = new ArrayList<Command>();
 
     /**
      * A List of the permission to execute this command, you can change this during runtime
      */
     private final List<String> permissions = new ArrayList<String>();
 
     private boolean isSubCommand;
 
 
     /**
      * Constructs a new command based on given information
      *
      * @param name        The name of the command
      * @param usage       The usage of the command
      * @param identifiers The identifiers of the command
      * @param subCommand
      */
     void build(final String name, final String usage, final String[] identifiers, final boolean subCommand) {
         this.identifiers = identifiers;
         this.usage = usage;
         this.name = name;
         isSubCommand = subCommand;
 
         int min = 0;
 
         for (final Argument argument : arguments) {
            if (argument.isRequired()) {
                 min++;
             }
         }
 
         this.minArguments = min;
         this.maxArguments = arguments.size();
     }
 
     Command(final String name, final String usage, final String[] identifiers, final List<Argument> arguments, final boolean subCommand) {
         build(name, usage, identifiers, subCommand);
         this.arguments.addAll(arguments);
     }
 
     public Command() {
     }
 
     public final String getName() {
         return name;
     }
 
     public final String getUsage() {
         return usage;
     }
 
     public final String[] getIdentifiers() {
         return identifiers;
     }
 
     public final boolean isIdentifier(String identifier) {
         for (final String string : identifiers) {
             if (identifier.equals(string)) {
                 return true;
             }
         }
 
         return false;
     }
 
     public final List<Command> getSubCommands() {
         return subCommands;
     }
 
     public final int getMaxArguments() {
         return maxArguments;
     }
 
     public final int getMinArguments() {
         return minArguments;
     }
 
     public final List<Argument> getArguments() {
         return arguments;
     }
 
 //    public final boolean hasArguments
 
     public final List<Command> getCallMethods() {
         return callMethods;
     }
 
     public final List<String> getPermissions() {
         return permissions;
     }
 
 
     /*=============================================================================================================*/
 
 
     public void execute(final CommandSender sender, final CallInformation information) {
         System.out.println("Command executed!\n" + this.toString() + "\n" + information);
     }
 
 
     /*=============================================================================================================*/
 
 
     public final void addPermission(String permission) {
         this.permissions.add(permission);
     }
 
     public final void addAlias(Command alias) {
         this.callMethods.add(alias);
     }
 
     public final void addSubCommand(Command subCommand) {
         this.subCommands.add(subCommand);
     }
 
     public final void addArgument(Argument argument) {
         this.arguments.add(argument);
     }
 
     public final void addPermissions(Collection<String> permissions) {
         this.permissions.addAll(permissions);
     }
 
     public final void addAliases(Collection<Command> aliases) {
         for (Command command : aliases) {
             command.addCallMethod(this);
         }
     }
 
     public final void addCallMethod(Command command) {
         this.callMethods.add(command);
     }
 
     public final boolean hasCallMethods() {
         return !callMethods.isEmpty();
     }
 
     void executeCallMethods(CommandSender sender, CallInformation info) {
         for (Command command : callMethods) {
             command.execute(sender, info);
         }
     }
 
     public final void addSubCommands(Collection<Command> subCommands) {
 
         this.subCommands.addAll(subCommands);
     }
 
     public final void addArguments(Collection<Argument> arguments) {
         this.arguments.addAll(arguments);
     }
 
     @Override
     public String toString() {
         return "Command{" +
                 "name='" + name + '\'' +
                 ", identifiers=" + Arrays.toString(identifiers) +
                 ", maxArguments=" + maxArguments +
                 ", minArguments=" + minArguments +
                 ", isSubCommand=" + isSubCommand +
                 '}';
     }
 }
