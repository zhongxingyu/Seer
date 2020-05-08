 package org.p000ison.dev.commandapi;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents a CommandExecutor
  */
 public abstract class CommandExecutor {
 
     private final List<Command> commands = new ArrayList<Command>(1);
 
     public void executeAll(CommandSender sender, String command) {
 
         String[] arguments = command.split(" ");
         if (arguments.length == 0) {
             return;
         }
 
         String identifier = arguments[0];
 
         if (arguments.length >= 1) {
             arguments = CallInformation.removeUntil(arguments, 1);
         }
 
         executeAll(sender, identifier, arguments);
     }
 
     public void executeAll(CommandSender sender, String identifier, String[] arguments) {
         CallInformation info = new CallInformation(identifier, arguments, sender);
         executeAll(sender, info);
     }
 
     public void executeAll(CommandSender sender, CallInformation info) {
         Command command = findCommand(info);
 
         command.execute(sender, info);
 
         if (command.hasCallMethods()) {
             command.executeCallMethods(sender, info);
         }
     }
 
     private Command findCommand(CallInformation info) {
         String identifier = info.getIdentifier();
         int arguments = info.getArguments().length;
         CommandSender sender = info.getSender();
 
         Command failCommand = null;
 
         for (Command command : commands) {
             if (command.isIdentifier(identifier)) {
                 if (!sender.hasPermission(command)) {
                     // no permission
                     continue;
                 } else if (arguments < command.getMinArguments() || arguments > command.getMaxArguments()) {
 
 
                     boolean failed = true;
 
                     //Check if the first argument is a sub-command
                     if (arguments >= 1) {
                         String subCommandArgument = info.getArguments()[0];
                         for (Command subCommand : command.getSubCommands()) {
                             if (subCommand.isIdentifier(subCommandArgument)) {
                                 //remove the sub command from the arguments
                                 info.removeFirstArgument();
                                 info.setIdentifier(subCommandArgument);
 
                                 subCommand.execute(sender, info);
                                 failed = false;
                             }
                         }
                     }
 
                     if (failed) {
                         failCommand = command;
                         continue;
                     }
                 } else if (arguments > 0 && info.getArguments()[0].equals("?")) {
                     displayCommandHelp(sender, command);
                     return command;
                 }
 
 
                 info.setIdentifier(identifier);
                 return command;
             }
         }
 
         if (failCommand != null) {
             displayCommandHelp(sender, failCommand);
         }
 
         return null;
     }
 
     public abstract void displayCommandHelp(CommandSender sender, Command command);
 
     //================================================================================
     // Command registration
     //================================================================================
 
     public CommandBuilder build() {
         return new CommandBuilder(this);
     }
 
     public void register(Command command) {
         commands.add(command);
     }
 
     public void register(Object instance) {
         for (Method method : instance.getClass().getMethods()) {
             Command cmd = buildFromMethod0(method, instance);
 
             if (cmd != null) {
                 register(cmd);
             }
         }
     }
 
     public void register(Class clazz) {
         for (Method method : clazz.getMethods()) {
             Command cmd = buildFromMethod0(method, null);
 
             if (cmd != null) {
                 register(cmd);
             }
         }
     }
 
     public Command buildFromMethod(Method method, Object instance) {
 
         Command cmd = buildFromMethod0(method, instance);
 
         if (cmd == null) {
             throw new IllegalArgumentException("The method does not have a CommandAnnotation or does not have the correct parameters!");
         }
 
         return cmd;
     }
 
     private Command buildFromMethod0(Method method, Object instance) {
 
         Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 2 && parameterTypes[0] == CommandSender.class && parameterTypes[1] == CallInformation.class) {
             return null;
         }
 
         CommandAnnotation annotation = method.getAnnotation(CommandAnnotation.class);
 
         if (annotation == null) {
             return null;
         }
 
         return createCommand(method, instance, annotation);
     }
 
     private Command createCommand(Method method, Object instance, CommandAnnotation annotation) {
         return new AnnotatedCommand(annotation.name(), annotation.usage(),
                 annotation.identifiers(),
                 annotation.minArguments(), annotation.maxArguments(), annotation.arguments(),
                 annotation.subCommand(),
                 method, instance);
     }
 }
