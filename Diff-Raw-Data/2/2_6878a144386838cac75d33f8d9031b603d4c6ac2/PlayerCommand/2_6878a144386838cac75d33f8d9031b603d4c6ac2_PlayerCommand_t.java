 package civchat.command;
 
 import org.bukkit.command.CommandSender;
 
 public abstract class PlayerCommand implements Command
 {
 	private String name;
 	private String description = "";
 	private String usage = "";
 	private int minArguments = 0;
 	private int maxArguments = 0;
 	private String identifier = "";
 
 	public PlayerCommand(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public String getDescription() {
 		return this.description;
 	}
 
 	public String getUsage() {
 		return this.usage;
 	}
 
	public String getIdentifier() {
 		return this.identifier;
 	}
 
 	public int getMinArguments() {
 		return this.minArguments;
 	}
 
 	public int getMaxArguments() {
 		return this.maxArguments;
 	}
 
 }
