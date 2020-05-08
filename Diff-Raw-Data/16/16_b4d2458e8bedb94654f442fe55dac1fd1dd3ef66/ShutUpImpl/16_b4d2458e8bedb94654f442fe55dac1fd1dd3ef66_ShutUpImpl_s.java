 package plugins.impl;
 
 import java.util.ArrayList;
 
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import net.xeoh.plugins.base.annotations.meta.Author;
 
 import org.jibble.pircbot.PircBot;
 
 import com.xtouchme.ircbot.BotOptions;
 
 import plugins.ShutUp;
 
 @PluginImplementation
 @Author(name="xTouchMe")
 public class ShutUpImpl implements ShutUp {
 
 	private ArrayList<String> commandStart = new ArrayList<String>();
 	private PircBot bot = null;
 	
 	@Override
 	public void run(String params, String chan, String sender, BotOptions options) {
 		boolean isPM = params.substring(params.length()-2, params.length()).equals("PM");
 		if(!options.isAdmin(sender)) { restrictions(chan, sender, isPM); return; }
 		if(options.shouldShutUp()) {
			sendMessage(chan, sender, "I am not responding to non-admin command senders already, no action taken.");
 			return;
 		}
 		options.shouldShutUp(true);
 		sendMessage(chan, sender, "Ignoring commands from non-bot admin users"+params);
 	}
 
 	private void restrictions(String channel, String sender, boolean isPM) {
 		sendMessage(channel, sender, "You don't have enough privileges to perform this task"+((isPM)?"PM":"NM"));
 	}
 	
 	private void sendMessage(String channel, String sender, String sendWhat) {
 		//If its a normal message
 		if(sendWhat.substring(sendWhat.length()-2, sendWhat.length()).equals("NM")) {
 			bot.sendMessage(channel, sendWhat.substring(0, sendWhat.length()-2));
 		} else { //Otherwise, its a pm
 			bot.sendNotice(sender, sendWhat.substring(0, sendWhat.length()-2));
 		}
 	}
 
 	@Override
 	public void load(PircBot bot) {
 		setCommandStart(new String[] {"shutup", "quiet", "disable"});
 		this.bot = bot;
 	}
 	
 	@Override
 	public void setCommandStart(String[] commandStart) {
 		for(String s : commandStart) {
 			this.commandStart.add(s);
 		}
 	}
 	
 	@Override
 	public boolean checkForCommand(String commandStart) {
 		return this.commandStart.contains(commandStart);
 	}
 
 	@Override
 	public void unload() {
 		commandStart = null;
 		bot = null;
 	}
 	
 }
