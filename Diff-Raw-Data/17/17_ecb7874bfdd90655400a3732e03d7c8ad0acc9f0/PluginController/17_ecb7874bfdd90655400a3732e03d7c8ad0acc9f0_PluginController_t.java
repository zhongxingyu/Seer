 package roujo.emily.plugins.basic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import roujo.emily.core.MessageContext;
 import roujo.emily.core.commands.Command;
 import roujo.emily.core.extensibility.Plugin;
 import roujo.emily.core.extensibility.capabilities.CommandManager;
 import roujo.emily.plugins.basic.commands.*;
 
 public class PluginController implements Plugin, CommandManager {
 	private String pluginName = "Basic";

 	private List<Command> commands;

 	public PluginController() {
 		commands = new ArrayList<Command>();
 	}
 
 	@Override
 	public boolean processMessage(MessageContext context) {
		if (context.wasPrefixed()) {
			for (Command command : commands) {
				if (command.getPattern().matcher(context.getMessage())
						.matches()) {
					command.execute(context);
					return true;
				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean load() {
 		commands.add(new EchoCommand());
 		commands.add(new HatsCommand());
 		commands.add(new JoinCommand());
 		commands.add(new LionCommand());
 		commands.add(new PartCommand());
 		commands.add(new PluginCommand());
 		commands.add(new QuitCommand());
 		commands.add(new RegexCommand());
 		commands.add(new RollCommand());
 		commands.add(new SlapCommand());
 		commands.add(new TellCommand());
 		commands.add(new TimeCommand());
 		commands.add(new VoiceCommand());
 		return true;
 	}
 
 	@Override
 	public boolean unload() {
 		commands.clear();
 		return true;
 	}
 
 	@Override
 	public String getName() {
 		return pluginName;
 	}
 
 }
