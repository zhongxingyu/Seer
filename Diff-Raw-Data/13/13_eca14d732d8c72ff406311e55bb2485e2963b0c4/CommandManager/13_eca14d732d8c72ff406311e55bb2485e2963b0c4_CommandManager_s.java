 package zerothindex.minecraft.clancraft.command;
 
 import java.util.HashSet;
 
 import zerothindex.minecraft.clancraft.Messageable;
 import zerothindex.minecraft.clancraft.bukkit.ClanPlugin;
 
 public class CommandManager {
 
 	private HashSet<CommandBase> commands;
 	private CommandBase commandHelp;
 	
 	public CommandManager() {
 		commands = new HashSet<CommandBase>();
 		commandHelp = new CommandHelp();
 		
 		commands.add(new CommandCreate());
 		commands.add(new CommandList());
 	}
 	
 	
 	public void handle(Messageable sender, String[] args) {
 		
 		if (args.length == 0) {
 			commandHelp.handle(sender, args);
 			return;
 		} 
 		
 		for (CommandBase cmd : commands) {
 			if (cmd.getName().equalsIgnoreCase(args[0])) {
 				if (cmd.handle(sender, args)) {
 					return;
 				} else {
 					sender.message("Usage: "+cmd.getUsage());
 				}
 			}
 		}
 		
 		sender.message("Unkown command \""+args[0]+"\"");
 	}
 }
