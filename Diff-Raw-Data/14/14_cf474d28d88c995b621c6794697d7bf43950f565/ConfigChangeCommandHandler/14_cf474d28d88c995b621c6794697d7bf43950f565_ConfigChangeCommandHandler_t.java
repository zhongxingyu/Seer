 /*
  * Author: Dabo Ross
  * Website: www.daboross.net
  * Email: daboross@daboross.net
  */
 package net.daboross.bukkitdev.timedblockreplace;
 
 import net.daboross.bukkitdev.timedblockreplace.commandexecutorbase.CommandExecutorBase;
 import net.daboross.bukkitdev.timedblockreplace.commandexecutorbase.SubCommand;
 import net.daboross.bukkitdev.timedblockreplace.subcommandhandlers.AddCommandHandler;
 import net.daboross.bukkitdev.timedblockreplace.subcommandhandlers.ListCommandHandler;
 import net.daboross.bukkitdev.timedblockreplace.subcommandhandlers.RemoveCommandHandler;
 import org.bukkit.command.PluginCommand;
 
 /**
  *
  * @author daboross
  */
 public class ConfigChangeCommandHandler {
 
     private final CommandExecutorBase commandExecutorBase;
 
     public ConfigChangeCommandHandler(TimedBlockReplace main) {
         commandExecutorBase = new CommandExecutorBase("timedblockreplace.config");
        SubCommand addCommand = new SubCommand("add", true, "timedblockreplace.add", new String[]{"FromBlockID", "ToBlock", "TimeTillChange"},
                "Sets the FromBlock to change to the ToBlock after TimeTillChange seconds", new AddCommandHandler(main));
        SubCommand removeCommand = new SubCommand("add", true, "timedblockreplace.remove",
                new String[]{"FromBlockID"}, "Removes a record added with `add`", new RemoveCommandHandler(main));
         SubCommand listCommand = new SubCommand("add", true, "timedblockreplace.add",
                "Lists all currently active blocks set to change", new ListCommandHandler(main));
         commandExecutorBase.addSubCommand(addCommand);
         commandExecutorBase.addSubCommand(removeCommand);
         commandExecutorBase.addSubCommand(listCommand);
     }
 
     protected void registerCommand(PluginCommand command) {
         command.setExecutor(commandExecutorBase);
     }
 }
