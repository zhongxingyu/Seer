 package pl.edu.agh.two.mud.server.command.util;
 
 import java.util.Collection;
 
 import pl.edu.agh.two.mud.common.command.UICommand;
 import pl.edu.agh.two.mud.common.command.provider.CommandProvider;
 import pl.edu.agh.two.mud.server.command.HitUICommand;
 import pl.edu.agh.two.mud.server.command.LogInUICommand;
import pl.edu.agh.two.mud.server.command.LogOutUICommand;
import pl.edu.agh.two.mud.server.command.RefreshUICommand;
 import pl.edu.agh.two.mud.server.command.RegisterUICommand;
 import pl.edu.agh.two.mud.server.command.RunUICommand;
import pl.edu.agh.two.mud.server.command.TalkUICommand;
import pl.edu.agh.two.mud.server.command.WhisperUICommand;
 
 @SuppressWarnings("unchecked")
 public class AvailableCommands {
 
 	private static AvailableCommands instance = null;
 
 	private CommandProvider commandProvider;
 
 	private AvailableCommands() {
 	}
 
 	public static AvailableCommands getInstance() {
 		if (instance == null) {
 			instance = new AvailableCommands();
 		}
 
 		return instance;
 	}
 
 	public void setCommandProvider(CommandProvider commandProvider) {
 		this.commandProvider = commandProvider;
 	}
 
 	/**
 	 * Returns collection of commands that should be available when client
 	 * application has been run and client is not logged.
 	 * 
 	 * @return Collection of commands
 	 */
 	public Collection<UICommand> getUnloggedCommands() {
 		return commandProvider.getUICommands(RegisterUICommand.class, LogInUICommand.class);
 	}
 
 	/**
 	 * Returns collection of commands that should be available after client has
 	 * successfully logged in.
 	 * 
 	 * @return Collection of commands
 	 */
 	public Collection<UICommand> getGameCommands() {
 		return commandProvider.getUICommandsWithout(RegisterUICommand.class, LogInUICommand.class, HitUICommand.class,
 				RunUICommand.class);
 	}
 
 	/**
 	 * Returns collection of commands that should be available when you are in
 	 * fight and it is your turn.
 	 * 
 	 * @return Collection of commands
 	 */
 	public Collection<UICommand> getFightYouTurnCommands() {
		return commandProvider.getUICommands(HitUICommand.class, RunUICommand.class, RefreshUICommand.class,
				TalkUICommand.class, WhisperUICommand.class);
 	}
 
 	/**
 	 * Returns collection of commands that should be available when you are in
 	 * fight and it is your's opponent turn.
 	 * 
 	 * @return Collection of commands
 	 */
 	public Collection<UICommand> getFightOpponentTurnCommands() {
		return commandProvider.getUICommands(RefreshUICommand.class, TalkUICommand.class, WhisperUICommand.class);
 	}

 	/**
 	 * Returns collection of commands that should be available when you are in
 	 * fight and it is your's opponent turn.
 	 * 
 	 * @return Collection of commands
 	 */
 	public Collection<UICommand> getDeadPlayerCommands() {
		return commandProvider.getUICommands(RefreshUICommand.class, TalkUICommand.class, WhisperUICommand.class,
				LogOutUICommand.class);
 	}
 }
