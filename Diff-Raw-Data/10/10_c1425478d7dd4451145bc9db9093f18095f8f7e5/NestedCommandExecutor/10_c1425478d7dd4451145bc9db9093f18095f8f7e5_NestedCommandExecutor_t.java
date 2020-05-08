 package fr.aumgn.bukkitutils.command.executor;
 
 import org.apache.commons.lang.Validate;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import fr.aumgn.bukkitutils.command.CommandsMessages;
 import fr.aumgn.bukkitutils.command.args.CommandArgsParser;
 
 public class NestedCommandExecutor implements CommandExecutor {
 
     private final JavaPlugin plugin;
     private final CommandsMessages messages;
     private final String defaultTo;
 
     public NestedCommandExecutor(JavaPlugin plugin, CommandsMessages messages,
             String defaultTo) {
         this.plugin = plugin;
         this.messages = messages;
         this.defaultTo = defaultTo;
     }
 
     @Override
     public boolean onCommand(CommandSender sender,
             Command cmd, String label, String[] rawArgs) {
 
         CommandArgsParser parser =  new CommandArgsParser(messages, rawArgs);
         String[] args = parser.linearize();
 
         PluginCommand subCmd;
        String subLabel;
         String[] subCmdArgs;
         if (args.length == 0) {
             if (defaultTo.isEmpty()) {
                 return false;
             }
 
             subCmd = plugin.getCommand(cmd.getName() + " " + defaultTo);
             Validate.notNull(subCmd);
            subLabel = label;
             subCmdArgs = args;
         } else {
             subCmd = plugin.getCommand(cmd.getName() + " " + args[0]);
             if (subCmd == null) {
                 if (defaultTo.isEmpty()) {
                     return false;
                 }
 
                 subCmd = plugin.getCommand(cmd.getName() + " " + defaultTo);
                subLabel = label;
                 Validate.notNull(subCmd);
                 subCmdArgs = args;
             } else {
                subLabel = label + " " + args[0];
                 subCmdArgs = new String[args.length - 1];
                 System.arraycopy(args, 1, subCmdArgs, 0, args.length - 1);
             }
         }
 
        subCmd.execute(sender, subLabel, subCmdArgs);
         return true;
     }
 }
