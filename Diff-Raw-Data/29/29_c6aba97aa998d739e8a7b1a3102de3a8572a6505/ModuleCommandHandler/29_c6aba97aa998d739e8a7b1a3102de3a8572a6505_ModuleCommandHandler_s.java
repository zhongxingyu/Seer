 package uk.codingbadgers.bFundamentals.commands;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.command.CommandSender;
 
 import uk.codingbadgers.bFundamentals.module.Module;
 
import com.google.common.collect.Maps;

 public class ModuleCommandHandler {
 
	private static Map<Module, List<ModuleCommand>> commands = Maps.newHashMap();
 	
 	public static void registerCommand(Module module, ModuleCommand command) {
 		if (commands.containsKey(module)) {
 			commands.get(module).add(command);
 		} else {
			commands.put(module, Arrays.asList(command));
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
