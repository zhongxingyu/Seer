 package ${package}.commands;
 
 import org.spout.api.command.CommandContext;
 import org.spout.api.command.CommandSource;
 import org.spout.api.command.annotated.Command;
 import org.spout.api.exception.CommandException;
 import ${package}.${artifactId};
 
 public class PlayerCommands {
 	private final ${artifactId} plugin;
 
 	public PlayerCommands(${artifactId} instance) {
 		this.plugin = instance;
 	}
 
	@Command(aliases = "Command", usage = "No Usage, replace this command", desc = "This is just an Example. Replace it.", min = 0, max = 0)
 	public void aCommand(CommandContext args, CommandSource source) throws CommandException {
 		// Do Some Command
 	}
 }
