 package com.drtshock.willie.command.utility;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 
 public class UTimeCommandHandler implements CommandHandler{
 
 	@Override
 	public void handle(Willie bot, Channel channel, User sender, String[] args){
 		if(args.length <= 0){
 			channel.sendMessage("(" + sender.getNick() + ") Usage: !utime <time>[s|ms] [timezone]");
 		}else{
 			String time = args[0];
 			String timezone = "GMT";
 			if(args.length > 1){
 				timezone = args[1];
 			}
 			long realTime = System.currentTimeMillis();
 			String lowerTime = time.toLowerCase();
 			long multiplier = 1000;
 			if(lowerTime.endsWith("ms")){
 				time = time.substring(0, time.length() - 2);
 				multiplier = 1;
 			}else if(lowerTime.endsWith("s")){
 				time = time.substring(0, time.length() - 1);
 			}
 			if(!time.equalsIgnoreCase("now")){
 				try{
 					realTime = Long.parseLong(time);
					multiplier = 1;
 				}catch(NumberFormatException e){
 					channel.sendMessage("(" + sender.getNick() + ") [Invalid time format] Usage: !utime <time>[s|ms] [timezone]");
 					return;
 				}
 			}
 			realTime = realTime * multiplier;
 			Date d = new Date(realTime);
 			String formatted = new String();
 			DateFormat dateFormat = DateFormat.getDateTimeInstance();
 			TimeZone tz = TimeZone.getTimeZone(timezone);
 			dateFormat.setTimeZone(tz);
 			formatted = dateFormat.format(d) + " " + tz.getDisplayName();
 			channel.sendMessage("(" + sender.getNick() + ") " + formatted);
 		}
 	}
 
 }
