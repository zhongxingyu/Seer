 package no.runsafe.UserControl.command;
 
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 import java.util.HashMap;
 
 public class Kick extends ExecutableCommand implements IConfigurationChanged
 {
 	public Kick()
 	{
 		super("kick", "Kicks a player from the server", "runsafe.usercontrol.kick", "player", "reason");
 		captureTail();
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
 	{
 		RunsafePlayer victim;
 		if (executor instanceof RunsafePlayer)
 			victim = RunsafeServer.Instance.getOnlinePlayer((RunsafePlayer) executor, parameters.get("player"));
 		else
 			victim = RunsafeServer.Instance.getOnlinePlayer(null, parameters.get("player"));
 		if (victim == null)
 			return "Player not found";
 
 		if (victim instanceof RunsafeAmbiguousPlayer)
 			return victim.toString();
 
 		if (victim.hasPermission("runsafe.usercontrol.kick.immune"))
 			return "You cannot kick that player";
 
 		String reason = parameters.get("reason");
 
 		if (executor instanceof RunsafePlayer)
 		{
 			RunsafePlayer executorPlayer = (RunsafePlayer) executor;
 			RunsafeServer.Instance.kickPlayer(executorPlayer, victim, reason);
 			this.sendKickMessage(victim, executorPlayer, reason);
 		}
 		else
 		{
 			RunsafeServer.Instance.kickPlayer(null, victim, reason);
 			this.sendKickMessage(victim, null, reason);
 		}
 
 		return null;
 	}
 
 	public void sendKickMessage(RunsafePlayer victim, RunsafePlayer player, String reason)
 	{
		RunsafeServer.Instance.broadcastMessage(
				(player != null ? this.onKickMessage : this.onServerKickMessage)
				.replaceAll("%target%", victim.getPrettyName())
				.replaceAll("%player%", (player != null ? player.getPrettyName() : ""))
				.replaceAll("%reason%", reason)
		);
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		this.onKickMessage = configuration.getConfigValueAsString("messages.onKick");
 		this.onServerKickMessage = configuration.getConfigValueAsString("messages.onServerKick");
 	}
 
 	private String onKickMessage;
 	private String onServerKickMessage;
 }
