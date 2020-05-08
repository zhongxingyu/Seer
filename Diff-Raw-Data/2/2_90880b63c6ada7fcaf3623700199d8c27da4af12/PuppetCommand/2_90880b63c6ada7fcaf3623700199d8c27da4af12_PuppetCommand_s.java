 package no.runsafe.nchat.command;
 
 import no.runsafe.framework.api.command.ExecutableCommand;
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.command.argument.IArgumentList;
 import no.runsafe.framework.api.command.argument.Player;
 import no.runsafe.framework.api.command.argument.TrailingArgument;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.nchat.chat.PlayerChatEngine;
 import no.runsafe.nchat.emotes.EmoteHandler;
 
 import javax.annotation.Nullable;
 
 public class PuppetCommand extends ExecutableCommand
 {
 	public PuppetCommand(EmoteHandler emoteHandler, PlayerChatEngine chatEngine)
 	{
		super("puppet", "Make it look like someone said something", "runsafe.nchat.puppet", new Player.Any().require(), new TrailingArgument("message"));
 		this.emoteHandler = emoteHandler;
 		this.chatEngine = chatEngine;
 	}
 
 	@Nullable
 	@Override
 	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
 	{
 		IPlayer targetPlayer = parameters.getValue("player");
 		if (targetPlayer == null)
 			return null;
 
 		String message = parameters.get("message");
 		if (!emoteHandler.executeEmote(targetPlayer, message))
 			chatEngine.broadcastMessageAsPlayer(targetPlayer, message);
 
 		return null;
 	}
 
 	private final EmoteHandler emoteHandler;
 	private final PlayerChatEngine chatEngine;
 }
