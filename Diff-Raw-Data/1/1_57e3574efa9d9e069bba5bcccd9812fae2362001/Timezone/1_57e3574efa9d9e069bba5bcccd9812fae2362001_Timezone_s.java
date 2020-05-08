 package de.hydrox.timezone;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Timezone extends JavaPlugin {
 
 	private static final Logger log = Logger.getLogger("Minecraft");
 
 	public void onEnable() {
 		log.info("[Timezone] Timezone loaded");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		Date date = new Date();
 
 		DateFormat serverFormat = new SimpleDateFormat();
 		DateFormat firstFormat = new SimpleDateFormat();
 
 		sender.sendMessage("Servertime: " + serverFormat.format(date));
 		TimeZone zone = null;
 
 
 		if (args.length == 0) {
 			zone = TimeZone.getTimeZone("GMT");
 			firstFormat.setTimeZone(zone);
 			sender.sendMessage("GMT: " + firstFormat.format(date));
 
 			zone = TimeZone.getTimeZone("AET");
 			firstFormat.setTimeZone(zone);
 			sender.sendMessage("Sydney: " + firstFormat.format(date));
 
 			return true;
 		}
 		if (args.length >= 1) {
 			if (args[0].toLowerCase().equals("list")) {
 				listTimezones(sender);
 				return true;
 			}
 			if (args[0].toLowerCase().equals("help")) {
 				displayHelp(sender);
 				return true;
 			}
 			zone = TimeZone.getTimeZone(args[0]);
			sender.sendMessage("Servertime: " + serverFormat.format(date));
 			
 			for (String string : args) {
 				zone = TimeZone.getTimeZone(string);
 				firstFormat.setTimeZone(zone);
 				sender.sendMessage("-->"+string+": " + firstFormat.format(date));
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public void onDisable() {
 		log.info("[Timezone] Timezone unloaded");
 	}
 
 	private void listTimezones(CommandSender sender) {
 		sender.sendMessage("Here is a list of valid Timezones (not complete):");
 		sender.sendMessage("GMT, GMT+1, GMT-5, PST, AET, Australia/Sydney, CST, EST, CET");
 	}
 
 	private void displayHelp(CommandSender sender) {
 		sender.sendMessage("Type '/tz' or '/tz help' to display this help");
 		sender.sendMessage("Type '/tz list' for a (non complete) list of valid timezones");
 		sender.sendMessage("Type '/tz timezone1 [timezone2]' to show the servertime and the times in the given zones");
 		sender.sendMessage("You can display multiple timezones at once");
 	}
 }
