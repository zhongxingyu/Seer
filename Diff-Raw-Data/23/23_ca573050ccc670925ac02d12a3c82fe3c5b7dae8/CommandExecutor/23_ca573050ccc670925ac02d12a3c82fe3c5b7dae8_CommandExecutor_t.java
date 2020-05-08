 package org.p000ison.dev.commandlib;
 
 import org.apache.commons.lang.StringUtils;
 
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
         if (!executeAll(sender, identifier, arguments, commands)) {
             onCommandNotFound(sender);
         }
     }
 
     private boolean executeAll(CommandSender sender, String identifier, String[] arguments, List<Command> currentCommands) {
         int argumentsNr = arguments.length;
 
         for (Command command : currentCommands) {
             if (command.isIdentifier(identifier)) {
                 boolean found = true;
                 if (argumentsNr > 0) {
                     found = executeAll(sender, arguments[0], CallInformation.removeUntil(arguments, 1), command.getSubCommands());
                 }
 
                if (argumentsNr < command.getMinArguments() || argumentsNr > command.getMaxArguments()) {
                     if (!found) {
                         onDisplayCommandHelp(sender, command);
                         continue;
                     }
                 } else if (argumentsNr > 0 && arguments[0].equals("?")) {
                     onDisplayCommandHelp(sender, command);
                     continue;
                 }
 
                if (!sender.hasPermission(command)) {
                    onPermissionFailed(sender, command);
                    continue;
                }

                 command.execute(sender, new CallInformation(identifier, arguments, sender));
                 return true;
             }
         }
 
         return false;
     }
 
     public abstract void onDisplayCommandHelp(CommandSender sender, Command command);
 
     public abstract void onCommandNotFound(CommandSender sender);
 
    public abstract void onPermissionFailed(CommandSender sender, Command command);

     //================================================================================
     // Command registration
     //================================================================================
 
     public CommandBuilder build() {
         return new CommandBuilder();
     }
 
     public CommandBuilder build(String name) {
         return new CommandBuilder().setName(name);
     }
 
     public void register(Command command) {
         if (!commands.contains(command)) {
             commands.add(command);
         }
     }
 
     public static double fuzzyEqualsString(String a, String b) {
         return 1.0 - (double) StringUtils.getLevenshteinDistance(a, b) / (a.length() >= b.length() ? a.length() : b.length());
     }
 
     public Command register(Object instance) {
         return register(instance, null, instance.getClass().getDeclaredMethods());
     }
 
     public Command register(Class clazz) {
         return register(null, null, clazz.getDeclaredMethods());
     }
 
     public Command register(Object instance, String name) {
         return register(instance, name, instance.getClass().getDeclaredMethods());
     }
 
     public Command register(Class clazz, String name) {
         return register(null, name, clazz.getDeclaredMethods());
     }
 
 
     private Command register(Object instance, String name, Method... methods) {
         for (Method method : methods) {
             CommandAnnotation annotation = getAnnotation(method);
 
             if (annotation != null) {
                 if (name != null && !annotation.name().equals(name)) {
                     continue;
                 }
 
                 Command cmd = createCommand(method, instance, annotation);
 
                 if (cmd != null) {
                     register(cmd);
 
                     return cmd;
                 }
             }
         }
 
         return null;
     }
 
     public boolean isRegistered(Command command) {
         return commands.contains(command);
     }
 
     public Command buildFromMethod(Method method, Object instance) {
 
         Command cmd = createCommand(method, instance, getAnnotation(method));
 
         if (cmd == null) {
             throw new IllegalArgumentException("The method does not have a CommandAnnotation or does not have the correct parameters!");
         }
 
         return cmd;
     }
 
     private CommandAnnotation getAnnotation(Method method) {
 
         Class<?>[] parameterTypes = method.getParameterTypes();
         if (parameterTypes.length != 2 || parameterTypes[0] != CommandSender.class || parameterTypes[1] != CallInformation.class) {
             return null;
         }
 
         return method.getAnnotation(CommandAnnotation.class);
     }
 
     private Command createCommand(Method method, Object instance, CommandAnnotation annotation) {
         return new AnnotatedCommand(annotation.name(), annotation.usage(),
                 annotation.identifiers(),
                 annotation.minArguments(), annotation.maxArguments(), annotation.arguments(),
                 method, instance);
     }
 }
