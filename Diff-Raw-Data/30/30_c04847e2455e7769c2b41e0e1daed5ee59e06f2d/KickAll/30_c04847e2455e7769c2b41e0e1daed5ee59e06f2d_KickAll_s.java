 package no.runsafe.UserControl.command;
 
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.HashMap;
 
 public class KickAll extends ExecutableCommand
 {
 	public KickAll()
 	{
 		super("kickall", "Kicks all players from the server", "runsafe.usercontrol.kickall", "reason");
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] arguments)
 	{
 		String reason = parameters.get("reason");
 		if (arguments.length > 0)
			reason += " " + StringUtils.join(arguments, " ", 1, arguments.length);
 
 		int n = 0;
 
 		RunsafePlayer kicker = null;
 		if (executor instanceof RunsafePlayer)
 			kicker = (RunsafePlayer) executor;
 
 		for (RunsafePlayer victim : RunsafeServer.Instance.getOnlinePlayers())
 			if (kicker == null || (kicker.canSee(victim) && !victim.getName().equals(executor.getName())))
 			{
 				if (!victim.hasPermission("runsafe.usercontrol.kick.immune"))
 				{
 					RunsafeServer.Instance.kickPlayer(kicker, victim, reason);
 					n++;
 				}
 			}
 		return String.format("Kicked %d players from the server.", n);
 	}
 }
