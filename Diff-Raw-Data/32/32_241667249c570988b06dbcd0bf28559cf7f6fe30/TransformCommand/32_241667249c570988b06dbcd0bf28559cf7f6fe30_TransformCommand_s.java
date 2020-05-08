 package com.wolvencraft.prison.cmd;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.CommandManager;
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.region.PrisonSelection;
 import com.wolvencraft.prison.settings.Language;
 import com.wolvencraft.prison.util.Message;
 
 public class TransformCommand implements BaseCommand {
 
 	@Override
 	public boolean run(String[] args) {
 		if(args.length == 1) {
 			getHelp();
 			return false;
 		}
 
 		Player player = (Player) CommandManager.getSender();
 		Language language = PrisonSuite.getLanguage();
 		
 		if(!PrisonSuite.hasSelection(player)) {
 			Message.sendError("Make a selection first");
 			return false;
 		}
 		
 		PrisonSelection sel = PrisonSuite.getSelection(player);
		
		if(args[0].equalsIgnoreCase("expand")) {
 			if(args.length == 3) {
 				try {
 					BlockFace direction = BlockFace.valueOf(args[2].toUpperCase());
 					sel.expand(direction, Integer.parseInt(args[1]));
 					Message.sendSuccess("The selection has been expanded by " + ChatColor.RED + args[1] + ChatColor.WHITE + " block(s) " + ChatColor.RED + direction.toString());
 				}
 				catch (NumberFormatException nfe) { Message.sendError(language.ERROR_ARGUMENTS); return false; }
 				catch (IllegalArgumentException iae) { Message.sendError(language.ERROR_DIRECTION.replace("<DIRECTION>", args[2].toUpperCase())); return false; }
 				return true;
 			}
 			else if(args.length == 4) {
 				try {
 					BlockFace direction = BlockFace.valueOf(args[3].toUpperCase());
 					sel.expand(direction, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
 					Message.sendSuccess("The selection has been expanded by " + ChatColor.RED + args[1] + ChatColor.WHITE + " block(s) " + ChatColor.RED + direction.toString());
 					Message.sendSuccess("The selection has been expanded by " + ChatColor.RED + args[2] + ChatColor.WHITE + " block(s) " + ChatColor.RED + direction.getOppositeFace().toString());
 				}
 				catch (NumberFormatException nfe) { Message.sendError(language.ERROR_ARGUMENTS); return false; }
 				catch (IllegalArgumentException iae) { Message.sendError(language.ERROR_DIRECTION.replace("<DIRECTION>", args[3].toUpperCase())); return false;}
 				return true;
 			}
 			else {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 		} else if(args[0].equalsIgnoreCase("contract")) {
 			if(args.length == 3) {
 				try {
 					BlockFace direction = BlockFace.valueOf(args[2].toUpperCase());
 					sel.expand(direction, 0 - Integer.parseInt(args[1]));
 					Message.sendSuccess("The selection has been contracted by " + ChatColor.RED + args[1] + ChatColor.WHITE + " block(s) " + ChatColor.RED + direction.toString());
 				}
 				catch (NumberFormatException nfe) { Message.sendError(language.ERROR_ARGUMENTS); return false; }
 				catch (IllegalArgumentException iae) { Message.sendError(language.ERROR_DIRECTION.replace("<DIRECTION>", args[2].toUpperCase())); return false; }
 				return true;
 			}
 			else if(args.length == 4) {
 				try {
 					BlockFace direction = BlockFace.valueOf(args[3].toUpperCase());
 					sel.expand(direction, 0 - Integer.parseInt(args[1]), 0 - Integer.parseInt(args[2]));
 					Message.sendSuccess("The selection has been contracted by " + ChatColor.RED + args[1] + ChatColor.WHITE + " block(s) " + ChatColor.RED + direction.toString());
 					Message.sendSuccess("The selection has been contracted by " + ChatColor.RED + args[2] + ChatColor.WHITE + " block(s) " + ChatColor.RED + direction.getOppositeFace().toString());
 				}
 				catch (NumberFormatException nfe) { Message.sendError(language.ERROR_ARGUMENTS); return false; }
 				catch (IllegalArgumentException iae) { Message.sendError(language.ERROR_DIRECTION.replace("<DIRECTION>", args[3].toUpperCase())); return false;}
 				return true;
 			}
 			else {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 		} else if(args[0].equalsIgnoreCase("shift")) {
 			if (args.length == 3) {
 				try {
 					BlockFace direction = BlockFace.valueOf(args[2].toUpperCase());
 					sel.shift(direction, Integer.parseInt(args[1]));
 					Message.sendSuccess("The selection has been shifted by " + ChatColor.RED + args[1] + ChatColor.WHITE + " block(s) " + ChatColor.RED + direction.toString());
 				}
 				catch (NumberFormatException nfe) { Message.sendError(language.ERROR_ARGUMENTS); return false; }
 				catch (IllegalArgumentException iae) { Message.sendError(language.ERROR_DIRECTION.replace("<DIRECTION>", args[3].toUpperCase())); return false;}
 				return true;
 			}
 			else {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 		} else {
 			Message.sendError(language.ERROR_COMMAND);
 			return false;
 		}
 	}
 
 	@Override
 	public void getHelp() {
 		Message.formatHeader(20, "Transformation");
 		Message.formatHelp("expand", "<amount> [amount] <direction>", "Expands the selected region specified amount of blocks");
 		Message.formatHelp("contract", "<amount> [amount] <direction>", "Contracts the selected region specified amount of blocks");
 		Message.send(" The second [amount] argument will be applied in the direction opposite of the one specified");
 		Message.formatHelp("shift", "<amount> <direction>", "Shifts the selected region specified amount of blocks");
 	}
 	
	public void getHelpLine() { Message.formatHelp("transform", "", "Shows the selection transformation help page", "mcprison.select.commands"); }
 }
