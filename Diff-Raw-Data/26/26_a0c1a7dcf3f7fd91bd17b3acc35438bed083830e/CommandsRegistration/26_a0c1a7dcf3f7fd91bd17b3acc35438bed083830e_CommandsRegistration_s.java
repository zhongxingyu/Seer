 package fr.aumgn.bukkitutils.command;
 
 import java.lang.reflect.Method;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import fr.aumgn.bukkitutils.command.executor.MethodCommandExecutor;
 import fr.aumgn.bukkitutils.command.executor.NestedCommandExecutor;
 import fr.aumgn.bukkitutils.command.messages.Messages;
 
 public class CommandsRegistration {
 
     private JavaPlugin plugin;
     private Messages local;
 
     public CommandsRegistration(JavaPlugin plugin, Messages local) {
         this.plugin = plugin;
         this.local = local;
     }
 
     public void register(Commands commands) {
         NestedCommands subCmdsAnnotation =
                 commands.getClass().getAnnotation(NestedCommands.class);
         boolean subCommands;
         if (subCmdsAnnotation != null) {
             subCommands = true;
         } else {
             subCommands = false;
         }
 
         for (Method method : commands.getClass().getMethods()) {
             Command cmdAnnotation = method.getAnnotation(Command.class);
             if (cmdAnnotation == null || !isValidCommand(method)) {
                 continue;
             }
 
             PluginCommand command;
             if (subCommands) {
                command = plugin.getCommand(subCmdsAnnotation.name() + " " + cmdAnnotation.name());
                registerSubCommand(subCmdsAnnotation.name(), cmdAnnotation.name());
             } else {
                 command = plugin.getCommand(cmdAnnotation.name());
             }
 
             if (command != null) {
                 CommandExecutor oldExecutor = command.getExecutor();
                 CommandExecutor executor = new MethodCommandExecutor(
                         local, commands, method, cmdAnnotation);
                 if (oldExecutor instanceof NestedCommandExecutor) {
                     ((NestedCommandExecutor) oldExecutor).setDefaultExecutor(executor);
                 } else {
                     setCommandMessages(command);
                     command.setExecutor(executor);
                 }
             }
         }
     }
 
     private void setCommandMessages(PluginCommand command) {
         command.setUsage(local.usagePrefix() + command.getUsage());
         command.setPermissionMessage(local.permissionMessage());
     }
 
     private boolean isValidCommand(Method method) {
         Class<?>[] params = method.getParameterTypes();
         if (params.length < 2) {
             return false;
         }
         if (!CommandSender.class.isAssignableFrom(params[0])) {
             return false;
         }
         if (!CommandArgs.class.isAssignableFrom(params[1])) {
             return false;
         }
 
         return true;
     }
 
     private void registerSubCommand(String name, String subCmdName) {
         PluginCommand command = plugin.getCommand(name);
         CommandExecutor executor = command.getExecutor();
         if (executor instanceof NestedCommandExecutor) {
             ((NestedCommandExecutor) executor).addSubCommand(subCmdName);
         } else {
             NestedCommandExecutor nestedExecutor = new NestedCommandExecutor(plugin, name);
             nestedExecutor.setDefaultExecutor(executor);
             nestedExecutor.addSubCommand(subCmdName);
             if (!(executor instanceof MethodCommandExecutor)) {
                 setCommandMessages(command);
             }
             command.setExecutor(nestedExecutor);
         }
     }
 }
