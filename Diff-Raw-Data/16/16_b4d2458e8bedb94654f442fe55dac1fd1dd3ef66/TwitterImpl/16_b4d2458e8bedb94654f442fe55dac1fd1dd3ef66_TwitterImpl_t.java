 package plugins.impl;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import net.xeoh.plugins.base.annotations.meta.Author;
 
 import org.jibble.pircbot.PircBot;
 
 import plugins.Twitter;
 
 import com.xtouchme.ircbot.BotOptions;
 
 @PluginImplementation
 @Author(name="xTouchMe")
 public class TwitterImpl implements Twitter {
 
 	private ArrayList<String> commandStart = new ArrayList<String>();
 	private PircBot bot = null;
 	
 	@Override
 	public void run(String params, String chan, String sender, BotOptions options) {
 		ArrayList<String> tweets = new ArrayList<String>();
 		
 		String isPM = params.substring(params.length()-2, params.length());
 		params = params.substring(0, params.length()-2);
 		String messages[] = params.split(" ");
 		if(!options.isModerator(sender)) { restrictions(chan, sender, isPM.equals("PM")); return; }
 
 		URL twitter;
 		try {
 			twitter = new URL("http://twitter.com/statuses/user_timeline/"+messages[0]+".rss");
 			BufferedReader in = new BufferedReader(new InputStreamReader(twitter.openStream()));
 			Pattern patternTitle = Pattern.compile("<title>.*?</title>");
 			Matcher matcher;
 
 			String currLine;
 			while((currLine = in.readLine()) != null) {
 				matcher = patternTitle.matcher(currLine);
 				if(matcher.find()) {
					tweets.add("@"+matcher.group().replaceAll("</?title>", "").replaceAll("[.]", "&#46"));
 				}
 			}
 
 			tweets.remove(0); //Removes the starting "Twitter / *"
 			if(messages.length > 1) { sendMessage(chan, sender, tweets.get(Integer.parseInt(messages[1])-1)+isPM); }
 			else { sendMessage(chan, sender, tweets.get(0)+isPM); }
 		} catch (FileNotFoundException e) {
 			sendMessage(chan, sender, "Sorry, this user/account does not exist!"+isPM);
 		} catch(IndexOutOfBoundsException e) {
 			sendMessage(chan, sender,
 					(Integer.parseInt(messages[1])-1 < 0)?"Sorry, I cannot predict the future."+isPM:
 					"Sorry, cannot retrieve more than 20 tweets"+isPM);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
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
 		setCommandStart(new String[] {"twitter", "tw"});
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
