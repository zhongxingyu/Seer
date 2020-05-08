 package com.wolvencraft.prison.cmd;
  
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.CommandManager;
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.region.PrisonSelection;
 import com.wolvencraft.prison.util.Message;
  
 public class SelectCommand  implements BaseCommand {
     public boolean run(String[] args) {
     	
         if(args.length != 1) {
         	Message.sendError(PrisonSuite.getLanguage().ERROR_ARGUMENTS);
         	return false;
         }
 
         Player player = (Player) CommandManager.getSender();
         Location loc = null;
         String message = "";
         PrisonSelection sel = PrisonSuite.getSelection(player);
          
         if(args[0].equalsIgnoreCase("select")) {
         	getHelp();
         	return true;
         }
         else if(args[0].equalsIgnoreCase("hpos1")) {
             loc = player.getTargetBlock(null, 100).getLocation();
             sel.setFirstCoordinate(loc);
             message = "First position set to (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
         }
         else if(args[0].equalsIgnoreCase("pos1")) {
             loc = player.getLocation();
             sel.setFirstCoordinate(loc);
             message = "First position set to (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
         }
         else if(args[0].equalsIgnoreCase("hpos2")) {
             loc = player.getTargetBlock(null, 100).getLocation();
             sel.setSecondCoordinate(loc);
             message = "Second position set to (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
         }
         else if(args[0].equalsIgnoreCase("pos2")) {
             loc = player.getLocation();
             sel.setSecondCoordinate(loc);
             message = "Second position set to (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
         }
         else {
             Message.sendError(PrisonSuite.getLanguage().ERROR_ARGUMENTS);
             return false;
         }
          
         if(sel.locationsSet()) message = message + " [" + sel.getBlockCount() + "]";
         Message.sendSuccess (message);
          
         return true;
     }
      
     public void getHelp() {
         Message.formatHeader(20, "Selecting");
         Message.formatHelp("hpos1", "", "Creates a reference point 1 at the block you are looking at");
         Message.formatHelp("hpos2", "", "Creates a reference point 2 at the block you are looking at");
         Message.send(" Your field of view is limited to 100 blocks");
         Message.formatHelp("pos1", "", "Creates a reference point 1 at your immediate location");
         Message.formatHelp("pos2", "", "Creates a reference point 2 at your immediate location");
         Message.send(" You can also select a region with your normal World Edit tool");
         return;
     }
 	
	public void getHelpLine() { Message.formatHelp("select", "", "Shows the region selection help page", "prison.select.commands"); }
 }
