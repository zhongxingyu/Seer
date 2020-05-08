 package no.runsafe.nchat.command;
 
 import no.runsafe.framework.api.command.ExecutableCommand;
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.command.argument.Player;
 import no.runsafe.framework.api.command.argument.IArgumentList;
 import no.runsafe.framework.api.command.argument.Period;
 import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.nchat.chat.MuteHandler;
 import org.joda.time.Duration;
 
 
 public class MuteCommand extends ExecutableCommand
 {
 	public MuteCommand(IConsole console, MuteHandler muteHandler)
 	{
 		super(
 			"mute", "Suppress chat messages from a player", "runsafe.nchat.mute",
 			new Player.Any().require(), new Period()
 		);
 		this.console = console;
 		this.muteHandler = muteHandler;
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
 	{
 		IPlayer player = executor instanceof IPlayer ? (IPlayer) executor : null;
		String mutePlayerName = parameters.getValue("player");
 		if (mutePlayerName == null)
 			return null;
 		Duration duration = parameters.getValue("duration");
 
 		if (mutePlayerName.equalsIgnoreCase("server"))
 		{
 			if (player != null && !player.hasPermission("runsafe.nchat.mute.server"))
 				return "&cYou do not have permission to do that";
 
 			muteHandler.muteServer();
 			return "&bGlobal chat has been muted, you monster.";
 		}
 		IPlayer mutePlayer = parameters.getValue("player");
 
 		if (mutePlayer == null)
 			return "&cThat player does not exist.";
 
 		if (player != null && (!mutePlayer.isOnline() || player.shouldNotSee(mutePlayer)))
 			return "&cThat player is currently offline.";
 
 		if (player != null && mutePlayer.hasPermission("runsafe.nchat.mute.exempt"))
 			return "&cNice try, but you cannot mute that player."; // Unless you are the console ^w^
 
 		console.logInformation(String.format("%s muted %s", executor.getName(), mutePlayer.getName()));
 		if (duration != null)
 			muteHandler.tempMutePlayer(mutePlayer, duration);
 		else
 			muteHandler.mutePlayer(mutePlayer);
 		return String.format("&bMuted %s.", mutePlayer.getPrettyName());
 	}
 
 	private final IConsole console;
 	private final MuteHandler muteHandler;
 }
