 package btwmods.stats;
 
 import btwmods.StatsAPI;
 import net.minecraft.src.CommandBase;
 import net.minecraft.src.ICommandSender;
 
 public class StatsCommand extends CommandBase {
 
 	@Override
 	public String getCommandName() {
 		return "stats";
 	}
 	
 	public String getCommandUsage(ICommandSender sender)
     {
         return "/" + getCommandName() + " [<on|off|status>]";
     }
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
 			sender.sendChatToPlayer("StatsAPI detailed measurements are " + (StatsAPI.detailedMeasurementsEnabled ? "on" : "off") + ".");
 		}
 		else if (args[0].toLowerCase().equals("on")) {
 			if (StatsAPI.detailedMeasurementsEnabled) {
 				sender.sendChatToPlayer("StatsAPI detailed measurements are already on.");
 			}
 			else {
 				StatsAPI.detailedMeasurementsEnabled = true;
 				sender.sendChatToPlayer("StatsAPI detailed measurements are now on.");
 			}
 		}
 		else if (args[0].toLowerCase().equals("off")) {
 			if (StatsAPI.detailedMeasurementsEnabled) {
 				StatsAPI.detailedMeasurementsEnabled = false;
 				sender.sendChatToPlayer("StatsAPI detailed measurements are now off.");
 			}
 			else {
 				sender.sendChatToPlayer("StatsAPI detailed measurements are already off.");
 			}
 		}
 	}
 
 }
