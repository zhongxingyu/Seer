 package btwmod.tickmonitor;
 
 import java.util.Arrays;
 import java.util.List;
 
 import net.minecraft.src.CommandBase;
 import net.minecraft.src.ICommandSender;
 
 public class MonitorCommand extends CommandBase {
 	
 	private BTWModTickMonitor mod;
 	
 	public MonitorCommand(BTWModTickMonitor mod) {
 		this.mod = mod;
 	}
 
 	@Override
 	public String getCommandName() {
 		return "monitor";
 	}
 	
 	public String getCommandUsage(ICommandSender sender)
     {
         return "/" + getCommandName() + " [<on|off|status>]";
     }
 
 	@Override
 	public List getCommandAliases() {
         return Arrays.asList(new String[] { "tick", "tickmonitor" });
 	}
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length == 0 || args[0].toLowerCase() == "status") {
 			sender.sendChatToPlayer(mod.getName() + " is " + (mod.isRunning() ? "on" : "off") + ".");
 		}
		else if (args[0].toLowerCase().equals("on")) {
 			if (mod.isRunning()) {
 				sender.sendChatToPlayer(mod.getName() + " is already on.");
 			}
 			else {
 				mod.setIsRunning(true);
 				sender.sendChatToPlayer(mod.getName() + " is now on.");
 			}
 		}
		else if (args[0].toLowerCase().equals("off")) {
 			if (mod.isRunning()) {
 				mod.setIsRunning(false);
 				sender.sendChatToPlayer(mod.getName() + " is now off.");
 			}
 			else {
 				sender.sendChatToPlayer(mod.getName() + " is already off.");
 			}
 		}
 	}
 }
