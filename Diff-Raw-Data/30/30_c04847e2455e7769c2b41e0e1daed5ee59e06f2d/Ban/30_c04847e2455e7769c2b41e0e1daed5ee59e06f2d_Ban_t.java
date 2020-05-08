 package no.runsafe.UserControl.command;
 
 import no.runsafe.UserControl.database.PlayerDatabase;
 import no.runsafe.UserControl.database.PlayerKickLog;
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.HashMap;
 
 public class Ban extends ExecutableCommand implements IConfigurationChanged
 {
 	public Ban(PlayerKickLog log, PlayerDatabase playerDatabase)
 	{
 		super("ban", "Permanently bans a player from the server", "runsafe.usercontrol.ban", "player", "reason");
 		logger = log;
 		playerdb = playerDatabase;
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] arguments)
 	{
 		String reason = parameters.get("reason");
 		if (arguments.length > 0)
			reason += " " + StringUtils.join(arguments, " ");
 
 		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(parameters.get("player"));
 		if (victim == null)
 			return "Player not found";
 
 		if (victim instanceof RunsafeAmbiguousPlayer)
 		{
 			return String.format(
 				"Multiple matches found: %s",
 				StringUtils.join(((RunsafeAmbiguousPlayer) victim).getAmbiguity(), ", ")
 			);
 		}
 
 		if (victim.hasPermission("runsafe.usercontrol.ban.immune"))
 			return "You cannot ban that player";
 
 		RunsafePlayer banningPlayer = null;
 		if (executor instanceof RunsafePlayer)
 			banningPlayer = (RunsafePlayer) executor;
 
 		if (!victim.isOnline() || (banningPlayer != null && !banningPlayer.canSee(victim)))
 		{
 			victim.setBanned(true);
 			playerdb.logPlayerBan(victim, banningPlayer, reason);
 			logger.logKick(banningPlayer, victim, reason, true);
 			return String.format("Banned offline player %s.", victim.getPrettyName());
 		}
 		if (lightning)
 			victim.strikeWithLightning(fakeLightning);
 		RunsafeServer.Instance.banPlayer(banningPlayer, victim, reason);
 		return null;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		lightning = configuration.getConfigValueAsBoolean("ban.lightning.strike");
 		fakeLightning = !configuration.getConfigValueAsBoolean("ban.lightning.real");
 	}
 
 	private final PlayerKickLog logger;
 	private final PlayerDatabase playerdb;
 	private boolean lightning;
 	private boolean fakeLightning;
 }
