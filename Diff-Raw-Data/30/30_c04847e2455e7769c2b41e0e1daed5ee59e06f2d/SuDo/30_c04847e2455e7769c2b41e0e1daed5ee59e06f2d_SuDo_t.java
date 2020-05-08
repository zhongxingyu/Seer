 package no.runsafe.UserControl.command;
 
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.HashMap;
 
 public class SuDo extends ExecutableCommand
 {
 	public SuDo()
 	{
 		super("sudo", "Force a player to run a command", "runsafe.usercontrol.sudo", "player", "command");
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] arguments)
 	{
 		RunsafePlayer target = RunsafeServer.Instance.getPlayer(parameters.get("player"));
 		if (target.hasPermission("runsafe.usercontrol.sudo.immune"))
 			return "You cannot make that user run commands";
 
 		String command = parameters.get("command");
 		if (arguments.length > 0)
			command += " " + StringUtils.join(arguments, " ");
 		target.getRawPlayer().performCommand(command);
 		return String.format("Forced %s to run /%s", target.getName(), command);
 	}
 }
