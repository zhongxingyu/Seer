 package com.mcnsa.mcnsachat2.commands;
 
 import java.util.Random;
 
 import org.bukkit.entity.Player;
 
 import com.mcnsa.mcnsachat2.MCNSAChat2;
 import com.mcnsa.mcnsachat2.util.ColourHandler;
 import com.mcnsa.mcnsachat2.util.Command;
 import com.mcnsa.mcnsachat2.util.CommandInfo;
 
 @CommandInfo(alias = "rtd", permission = "rtd", usage = "[-s] [#d#]", description = "Rolls a dice using '2d6' notation. -s means silent, default is 1d6")
 public class CommandRollTheDice implements Command {
 	private static MCNSAChat2 plugin = null;
 	public CommandRollTheDice(MCNSAChat2 instance) {
 		plugin = instance;
 	}
 
 	public Boolean handle(Player player, String sArgs) {
 		// default roll is 1d6
 		boolean silent = false;
 		Integer num = 1;
 		Integer max = 6;
 		
 		
 		if(sArgs.trim().length() > 0) {
 			// parse the options
 			// first split, to see if silent
 			String[] args = sArgs.trim().split("\\s");
 			String parseString = "";
 			
 			if(args.length == 2) {
 				// the first one should be "-s"
 				if(args[0].equalsIgnoreCase("-s")) {
 					silent = true;
 				}
 				else {
 					// error!
 					return false;
 				}
 				parseString = args[1];
 			}
 			else {
 				parseString = args[0];
 			}
 			
 			// now turn the parse string into <#>d<#> format
 			Integer dLoc = parseString.indexOf("d");
 			// make sure we found it
 			if(dLoc < 0) {
 				return false;
 			}
 			
 			try {
 				String numStr = parseString.substring(0, dLoc);
 				String maxStr = parseString.substring(dLoc + 1, parseString.length());
 				num = Integer.parseInt(numStr);
 				max = Integer.parseInt(maxStr);
 			}
 			catch(Exception e) {
 				return false;
 			}
 		}
 		
 		Random generator = new Random();
 		Integer sum = 0;
 		for(int i = 0; i < num; i++) {
 			sum += generator.nextInt(max) + 1;
 		}
 		
 		// and announce!
 		if(silent) {
			ColourHandler.sendMessage(player, "&7You rolled a &e" + sum + " &7on your &f" + num + "&7d" + max + " &7roll!");
 		}
 		else {
			plugin.chatManager.handleChat(player, ColourHandler.processColours("rolled a &e" + sum + " &7on their &f" + num + "&7d" + max + " &7roll!"), true, "", false);
 		}
 		
 		// and we handled it!
 		return true;
 	}
 }
 
