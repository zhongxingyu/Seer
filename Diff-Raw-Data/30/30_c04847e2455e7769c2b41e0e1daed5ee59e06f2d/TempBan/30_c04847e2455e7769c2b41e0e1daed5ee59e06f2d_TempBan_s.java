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
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.format.PeriodFormatter;
 import org.joda.time.format.PeriodFormatterBuilder;
 
 import java.util.HashMap;
 
 public class TempBan extends ExecutableCommand implements IConfigurationChanged
 {
 	public TempBan(PlayerDatabase playerDatabase, PlayerKickLog logger)
 	{
 		super("tempban", "Temporarily ban a player from the server", "runsafe.usercontrol.ban.temporary", "player", "time", "reason");
 		this.logger = logger;
 		timeParser = new PeriodFormatterBuilder()
 			.printZeroRarelyFirst().appendYears().appendSuffix("y")
 			.printZeroRarelyFirst().appendWeeks().appendSuffix("w", "weeks")
 			.printZeroRarelyFirst().appendDays().appendSuffix("d")
 			.printZeroRarelyFirst().appendHours().appendSuffix("h")
 			.printZeroRarelyFirst().appendMinutes().appendSuffix("m")
 			.printZeroRarelyFirst().appendSeconds().appendSuffix("s")
 			.toFormatter();
 		playerdb = playerDatabase;
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] arguments)
 	{
 		try
 		{
 			Period duration = timeParser.parsePeriod(parameters.get("time"));
 			DateTime expires = DateTime.now().plus(duration);
 			String reason = parameters.get("reason");
 			if (arguments.length > 0)
				reason += " " + StringUtils.join(arguments, " ", 1, arguments.length);
 
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
 
 			playerdb.setPlayerTemporaryBan(victim, expires);
 
 			RunsafePlayer banner = null;
 			if (executor instanceof RunsafePlayer)
 				banner = (RunsafePlayer) executor;
 
 			if (!victim.isOnline() || (banner != null && !banner.canSee(victim)))
 			{
 				victim.setBanned(true);
 				logger.logKick(banner, victim, reason, true);
 				playerdb.logPlayerBan(victim, banner, reason);
 				return String.format("Temporarily banned offline player %s.", victim.getPrettyName());
 			}
 			if (lightning)
 				victim.strikeWithLightning(fakeLightning);
 			RunsafeServer.Instance.banPlayer(banner, victim, reason);
 			return null;
 		}
 		catch (IllegalArgumentException e)
 		{
 			return "Unrecognized time format, use y/w/d/h/m/s";
 		}
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		lightning = configuration.getConfigValueAsBoolean("ban.lightning.strike");
 		fakeLightning = !configuration.getConfigValueAsBoolean("ban.lightning.real");
 	}
 
 	private final PeriodFormatter timeParser;
 	private final PlayerDatabase playerdb;
 	private final PlayerKickLog logger;
 	private boolean lightning;
 	private boolean fakeLightning;
 }
