 package no.runsafe.UserControl.command;
 
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.HashMap;
 
 public class Kick extends ExecutableCommand
 {
 	public Kick()
 	{
 		super("kick", "Kicks a player from the server", "runsafe.usercontrol.kick", "player", "reason");
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] arguments)
 	{
 		RunsafePlayer victim;
 		if (executor instanceof RunsafePlayer)
 			victim = RunsafeServer.Instance.getOnlinePlayer((RunsafePlayer) executor, parameters.get("player"));
 		else
 			victim = RunsafeServer.Instance.getOnlinePlayer(null, parameters.get("player"));
 		if (victim == null)
 			return "Player not found";
 
 		if (victim instanceof RunsafeAmbiguousPlayer)
 		{
 			return String.format(
 				"Multiple matches found: %s",
 				StringUtils.join(((RunsafeAmbiguousPlayer) victim).getAmbiguity(), ", ")
 			);
 		}
 
 		if (victim.hasPermission("runsafe.usercontrol.kick.immune"))
 			return "You cannot kick that player";
 
 		String reason = parameters.get("reason");
 		if (arguments.length > 0)
			reason += " " + StringUtils.join(arguments, " ");
 
 		if (executor instanceof RunsafePlayer)
 			RunsafeServer.Instance.kickPlayer((RunsafePlayer) executor, victim, reason);
 		else
 			RunsafeServer.Instance.kickPlayer(null, victim, reason);
 
 		return null;
 	}
 
 }
