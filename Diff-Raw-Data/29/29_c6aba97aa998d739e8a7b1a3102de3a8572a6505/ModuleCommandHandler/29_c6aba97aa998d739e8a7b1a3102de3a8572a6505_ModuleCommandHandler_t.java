 package uk.codingbadgers.bFundamentals.commands;
 
import java.util.ArrayList;
 import java.util.Arrays;
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.command.CommandSender;
 
 import uk.codingbadgers.bFundamentals.module.Module;
 
 public class ModuleCommandHandler {
 
	private static Map<Module, List<ModuleCommand>> commands = new HashMap<Module, List<ModuleCommand>>();
 	
 	public static void registerCommand(Module module, ModuleCommand command) {
 		if (commands.containsKey(module)) {
 			commands.get(module).add(command);
 		} else {
			commands.put(module, new ArrayList<ModuleCommand>(Arrays.asList(command)));
 		}
 	}
 	
 	public static boolean handleCommad(CommandSender sender, String label, String[] args) {
 		for (Map.Entry<Module, List<ModuleCommand>> entry : commands.entrySet()) {
 			for (ModuleCommand cmd : entry.getValue()) {
 				if (cmd.getLabel().equalsIgnoreCase(label)) {
 					if (!cmd.onCommand(sender, label, args) && !entry.getKey().onCommand(sender, label, args)) {
 						sender.sendMessage(cmd.getUsage());
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 }
