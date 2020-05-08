 package de.cubenation.plugins.utils.commandapi;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.cubenation.plugins.utils.commandapi.annotation.Command;
 import de.cubenation.plugins.utils.commandapi.exception.CommandException;
 import de.cubenation.plugins.utils.commandapi.exception.CommandWarmUpException;
 
 public class CommandsManager {
     private JavaPlugin plugin;
     private ArrayList<ChatCommand> commands = new ArrayList<ChatCommand>();
     private PermissionInterface permissionInterface = null;
 
     public CommandsManager(JavaPlugin plugin) {
         this.plugin = plugin;
     }
 
     public void add(Class<?> commandClass) throws CommandWarmUpException {
         if (commandClass == null) {
             throw new CommandWarmUpException("command class could not be null");
         }
 
         try {
             Object instance = null;
             try {
                 Constructor<?> ctor = commandClass.getConstructor(JavaPlugin.class);
                 instance = ctor.newInstance(plugin);
             } catch (NoSuchMethodException e) {
                 try {
                     instance = commandClass.newInstance();
                 } catch (InstantiationException e1) {
                    throw new CommandWarmUpException(commandClass, "no matching constructor found");
                 }
             }
             Method[] declaredMethods = instance.getClass().getDeclaredMethods();
             for (Method declaredMethod : declaredMethods) {
                 Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
 
                 if (parameterTypes.length != 2) {
                     throw new CommandWarmUpException(commandClass, "wrong number of paramter in method " + declaredMethod.getName() + ", expected 2 but was "
                             + parameterTypes.length);
                 }
                 if (!parameterTypes[0].equals(Player.class) && !parameterTypes[0].equals(ConsoleCommandSender.class)) {
                     throw new CommandWarmUpException(commandClass, "first parameter in method " + declaredMethod.getName()
                             + " must be Player or ConsoleCommandSender but was " + parameterTypes[0].getName());
                 }
 
                 if (!parameterTypes[1].equals(String[].class)) {
                     throw new CommandWarmUpException(commandClass, "second parameter in method " + declaredMethod.getName() + " must be String[] but was "
                             + parameterTypes[1].getName());
                 }
 
                 boolean annotationPresent = declaredMethod.isAnnotationPresent(Command.class);
                 if (annotationPresent) {
                     ChatCommand newchatCommand = new ChatCommand(instance, declaredMethod);
                     if (permissionInterface != null) {
                         newchatCommand.setPermissionInterface(permissionInterface);
                     }
                     commands.add(newchatCommand);
                 }
             }
         } catch (CommandWarmUpException e) {
             throw e;
         } catch (Exception e) {
             throw new CommandWarmUpException(commandClass, "error on command warmup", e);
         }
     }
 
     public void clear() {
         commands.clear();
     }
 
     public void execute(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) throws CommandException {
         findAndExecuteCommand(sender, commandLabel, args);
     }
 
     private void findAndExecuteCommand(CommandSender sender, String commandLabel, String[] args) throws CommandException {
         String mainCommand = commandLabel;
         String subCommand = "";
         boolean helpCommand = false;
 
         if (mainCommand.isEmpty()) {
             return;
         }
 
         if (args.length > 0) {
             subCommand = args[0];
             if (args.length > 1 && (args[1].equalsIgnoreCase("help") || args[1].equals("?"))) {
                 helpCommand = true;
             }
         }
 
         if (!subCommand.isEmpty() && (subCommand.equalsIgnoreCase("help") || subCommand.equals("?"))) {
             subCommand = "";
             helpCommand = true;
         }
 
         if (helpCommand) {
             // search for defined help command
             for (ChatCommand command : commands) {
                 if (command.isCommand(sender, mainCommand, "help") || command.isCommand(sender, mainCommand, "?")) {
                     Queue<String> argsQueue = new LinkedList<String>(Arrays.asList(args));
 
                     command.execute(sender, argsQueue.toArray(new String[] {}));
 
                     return;
                 }
             }
 
             for (ChatCommand command : commands) {
                 command.sendHelp(mainCommand, subCommand, sender);
             }
 
             return;
         }
 
         if (!subCommand.isEmpty()) {
             for (ChatCommand command : commands) {
                 if (command.isCommand(sender, mainCommand, subCommand)) {
                     Queue<String> argsQueue = new LinkedList<String>(Arrays.asList(args));
                     argsQueue.poll();
                     command.execute(sender, argsQueue.toArray(new String[] {}));
                     return;
                 }
             }
         }
 
         for (ChatCommand command : commands) {
             if (command.isCommand(sender, mainCommand)) {
                 Queue<String> argsQueue = new LinkedList<String>(Arrays.asList(args));
 
                 command.execute(sender, argsQueue.toArray(new String[] {}));
                 return;
             }
         }
 
         if (sender instanceof Player) {
             ((Player) sender).sendMessage(ChatColor.RED + "Befehl nicht gefunden. Versuche /" + mainCommand + " help"
                     + (!subCommand.isEmpty() ? " oder /" + mainCommand + " " + subCommand + " help" : ""));
         } else {
             ((ConsoleCommandSender) sender).sendMessage("Befehl nicht gefunden. Versuche /" + mainCommand + " help"
                     + (!subCommand.isEmpty() ? " oder /" + mainCommand + " " + subCommand + " help" : ""));
         }
     }
 
     public void setPermissionInterface(PermissionInterface permissionInterface) {
         this.permissionInterface = permissionInterface;
 
         for (ChatCommand command : commands) {
             command.setPermissionInterface(permissionInterface);
         }
     }
 }
