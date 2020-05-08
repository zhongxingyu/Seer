 package com.mcnsa.instanceportals.commands;
 
 import org.bukkit.entity.Player;
 
 import com.mcnsa.instanceportals.InstancePortals;
 import com.mcnsa.instanceportals.util.ColourHandler;
 import com.mcnsa.instanceportals.util.Command;
 import com.mcnsa.instanceportals.util.CommandInfo;
 
 @CommandInfo(alias = "isave", permission = "instance.create", usage = "", description = "cancels definition of your instance")
 public class InstanceSave implements Command {
 	private static InstancePortals plugin = null;
 	public InstanceSave(InstancePortals instance) {
 		plugin = instance;
 	}
 	
 	
 	@Override
 	public Boolean handle(Player player, String sArgs) {
 		// make sure they have valid args
 		if(sArgs.trim().length() > 0) {
 			return false;
 		}
 		
 		// see if they're defining an instance
 		if(plugin.playerManager.playerDefiningInstance(player)) {
 			// they are! tell them they can't do that!
 			ColourHandler.sendMessage(player, "&cFinish your instance set before you try to save them all!");
 			return true;
 		}
 		
 		// ok, save all the things.
 		plugin.persistanceManager.writePersistance();
 		
 		//Message them
		ColourHandler.sendMessage(player, "&cInstances Saved!");
 		
 		return true;
 	}
 }
