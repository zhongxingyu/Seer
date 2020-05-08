 package com.wolvencraft.prison.cmd;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.wolvencraft.prison.CommandManager;
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.util.Message;
 
 public class WandCommand  implements BaseCommand {
 	public boolean run(String[] args) {
 		
 		if(args.length != 1) {
 			Message.sendError(PrisonSuite.getLanguage().ERROR_ARGUMENTS);
 			return false;
 		}
 		
 		Player player = (Player) CommandManager.getSender();
 		ItemStack wand = new ItemStack(PrisonSuite.getSettings().WAND);
 		
 		if(player.getInventory().contains(wand)) {
 			Message.sendError("You already have a &c" + wand.getType().toString().toLowerCase().replace("_", " ") + "!&f");
 			return false;
 		}
 		
 		player.getInventory().addItem(wand);
 		Message.sendSuccess("Here is your &c" + wand.getType().toString().toLowerCase().replace("_", " ") + "!&f");
 		return true;
 	}
 	
 	public void getHelp() {}
 	
	public void getHelpLine() { Message.formatHelp("wand", "", "Gives you a PrisonSuite selection wand", "prison.mine.wand"); }
 }
