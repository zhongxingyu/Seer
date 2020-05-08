 package no.runsafe.nchat.command;
 
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.command.argument.AnyPlayerRequired;
 import no.runsafe.framework.api.command.argument.IArgumentList;
 import no.runsafe.framework.api.command.player.PlayerCommand;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.nchat.chat.IgnoreHandler;
 
 public class IgnoreCommand extends PlayerCommand
 {
 	public IgnoreCommand(IgnoreHandler ignoreHandler)
 	{
 		super("ignore", "Toggle the ignoring of a player.", "runsafe.nchat.ignore", new AnyPlayerRequired());
 		this.ignoreHandler = ignoreHandler;
 	}
 
 	/**
 	 * Checks to see if a player can be ignored.
 	 *
 	 * @param ignorePlayer The player to check.
 	 * @return True if the player can be ignored, otherwise false.
 	 */
 	public static boolean isMuteExempt(ICommandExecutor ignorePlayer)
 	{
 		return ignorePlayer.hasPermission("runsafe.nchat.ignore.exempt");
 	}
 
 	@Override
 	public String OnExecute(IPlayer executor, IArgumentList parameters)
 	{
 		IPlayer ignorePlayer = parameters.getPlayer("player");
 		if (ignorePlayer == null)
 			return "&cUnable to ignore that player, server error.";
 
		if (executor.getName().equalsIgnoreCase(ignorePlayer.getName()))
			return "&cWhy would you want to do that?";

 		if (ignoreHandler.playerIsIgnoring(executor, ignorePlayer))
 		{
 			ignoreHandler.removeIgnorePlayer(executor, ignorePlayer);
 			return String.format("&aYou are no longer ignoring %s.", ignorePlayer.getName());
 		}
 		else
 		{
 			if (isMuteExempt(ignorePlayer))
 				return "&cYou cannot ignore that player.";
 
 			ignoreHandler.ignorePlayer(executor, ignorePlayer);
 			return String.format("&aYou are now ignoring %s. Repeat the command again to un-ignore.", ignorePlayer.getName());
 		}
 	}
 
 	private final IgnoreHandler ignoreHandler;
 }
