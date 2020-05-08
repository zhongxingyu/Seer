 package de.hydrox.timezone;
 
 import java.text.DateFormat;
 import java.text.ParseException;
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
		if (args.length >= 1) {
			Date date = new Date();
 			DateFormat df = DateFormat.getDateInstance();
			df.setTimeZone(TimeZone.getTimeZone(args[0]));

 			DateFormat serverFormat = new SimpleDateFormat();
 			DateFormat firstFormat = new SimpleDateFormat();
 
 			TimeZone zone = TimeZone.getTimeZone(args[0]);
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
 
 }
