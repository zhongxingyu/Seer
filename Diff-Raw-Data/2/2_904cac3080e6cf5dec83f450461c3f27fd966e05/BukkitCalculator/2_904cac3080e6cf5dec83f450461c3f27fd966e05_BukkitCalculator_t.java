 package com.kwiius.BukkitCalculator;
 
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BukkitCalculator extends JavaPlugin {
 	public static Logger log;
 
 	@Override
 	public void onEnable() {
 		log = getLogger();
 		log.info("onEnable Called");
 	}
 
 	@Override
 	public void onDisable() {
 		log.info("onDisable Called");
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (cmd.getName().equals("c")) {
 			if (args.length != 0) {
 				String joined = Utils.implode(args);
 
 				Pattern p = Pattern.compile("[a-zA-Z]");
 				Matcher m = p.matcher(joined);
 				if (m.find()) {
 					sender.sendMessage(ChatColor.RED
 							+ "Error: No letters allowed!");
 				}
 
 				Pattern p2 = Pattern.compile("[.]");
 				Matcher m2 = p.matcher(joined);
 				if (m2.find()) {
 					sender.sendMessage(ChatColor.RED
 							+ "Error: Non-decimal numbers only!");
 				}
 
 				joined.replace("**", "^");
 
 				try {
					String output = Calculator.calculate(joined);
 					if (output == null) {
 						return false;
 					} else {
 						sender.sendMessage(ChatColor.YELLOW + output);
 					}
 				} catch (NumberFormatException e) {
 					sender.sendMessage(ChatColor.RED + "Error: "
 							+ e.getMessage());
 				} catch (Exception e) {
 					sender.sendMessage(ChatColor.RED + "Error: "
 							+ e.getMessage());
 				}
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 		return true;
 	}
 }
