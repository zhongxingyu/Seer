 package no.runsafe.command;
 
 import no.runsafe.framework.command.console.ConsoleCommand;
 import no.runsafe.framework.messaging.IMessagePump;
 import no.runsafe.framework.messaging.Message;
 
 import java.util.HashMap;
 
 public class ReloadConfigCommand extends ConsoleCommand
 {
 	public ReloadConfigCommand(IMessagePump pump)
 	{
 		super("reload", "Reloads configuration of runsafe plugins");
 		messagePump = pump;
 	}
 
 	@Override
	public String OnExecute(HashMap<String, String> stringStringHashMap, String[] strings)
 	{
 		Message reloadConfigMessage = new Message();
 		reloadConfigMessage.setTargetService("Configuration");
 		reloadConfigMessage.setQuestion("config.reload");
 		messagePump.HandleMessageAll(reloadConfigMessage);
 		return "Configuration reloaded";
 	}
 
 	private final IMessagePump messagePump;
 
 }
