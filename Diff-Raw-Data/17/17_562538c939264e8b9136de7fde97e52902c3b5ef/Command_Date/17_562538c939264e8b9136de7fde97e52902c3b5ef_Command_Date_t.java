 package plugins;
 
 import main.SjBot;
 import java.util.Date;
 import org.pircbotx.*;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.MessageEvent;
 
 
 
 public class Command_Date extends ListenerAdapter {
 	
 	private static boolean isFrozen;
 	private static String cmdName = "date";
 	PircBotX bot = SjBot.bot;
 	
 	public void onMessage(MessageEvent msg) {
		if (msg.getMessage().startsWith(SjBot.pref+cmdName) || msg.getMessage().startsWith(SjBot.pref+" "+cmdName)){
			isFrozen = SjBot.getFrozen();
			if (isFrozen == false ) {
				bot.sendMessage(msg.getChannel(), "The current time is: "+new Date());
				System.out.println("Ran command "+cmdName);
			}else{
				System.out.println("Bot is frozen");
			}
		}	
 	}	
 }
