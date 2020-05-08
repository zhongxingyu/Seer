 package net.othercraft.steelsecurity.listeners;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.othercraft.steelsecurity.Main;
 import net.othercraft.steelsecurity.utils.SSCmdExe;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerChatEvent;
 
 public class ChatFilter extends SSCmdExe {
 
 	public Main plugin;
 
 	Map<String, Long> chattimes = new HashMap<String, Long>();// for tracking the speed of chat
 
 	public ChatFilter(String name, Main instance) {
 		super("ChatFilter", true);//true only if its a listener, false if it isnt
 		this.plugin = instance;
 	}
 
 	@EventHandler
 	public void onPlayerChat(PlayerChatEvent event){
 		try {
 			int speed = plugin.getConfig().getInt("AntiSpam.AntiFlood.Speed");
 			Boolean spam = true;//if this is true at the end cancel the event
 			String name = event.getPlayer().getName();//name of the player for use in the hashmap
 			Long time = System.currentTimeMillis();//current time
 			Long lasttime = chattimes.get(name);//last time the player has chatted
 			if(lasttime == null)lasttime = Long.valueOf(time - (speed + 1));// default if the player hasnt chatted yet
 			int check = (time.intValue() - lasttime.intValue());//used to compare to the configured speed
 			chattimes.put(name, time);//overwrites the old time with the new one
			if (check > speed || plugin.getConfig().getBoolean("AntiSpam.AntiFlood.Enabled") == false || event.getPlayer().hasPermission("steelsecurity.bypass.antiflood") == false) {//checks if the speed of chat is faster than what is configured
 				spam = false;//sets spam to false
 			}
 			String message = event.getMessage();//prepairs message for editing
 			if ((spam == false)) {//only bothers with the anticaps and the censoring if it isnt spam.
 				if (plugin.getConfig().getBoolean("AntiSpam.Censoring.Enabled") && event.getPlayer().hasPermission("steelsecurity.bypass.censor") == false) {//checks if it so scan for configured word
 					@SuppressWarnings("unchecked")
 					List<String> list =  (List<String>) plugin.getConfig().getList("AntiSpam.Censoring.Block_Words");//retreives the list of blocked words
 					int wordcount = list.size();//the length of the list of blocked words
 					int wordcounter = 0;//i like using this instead of for loops becasue these are less restricting
 					while (wordcounter<wordcount) {
 						String newword;
 						String badword = list.get(wordcounter);
 						int lettercount = list.get(wordcounter).toCharArray().length;//word length
 						int lettercounter = 0;
 						newword = "";
 						while (lettercounter < lettercount) {//used to generate a new word considting of *s
 							newword = (newword + "*");
 							++lettercounter;
 						}
 						message = message.replaceAll("(?i)" + badword, newword);
 						++wordcounter;
 					}
 				}
 
 				if (event.getMessage().length()>plugin.getConfig().getInt("AntiSpam.AntiCaps.Minimum_Length")){//
 					if (plugin.getConfig().getBoolean("AntiSpam.AntiCaps.Enabled") && event.getPlayer().hasPermission("steelsecurity.bypass.anticaps") == false) {//checks for if it should do the anticaps check
 						double percent = plugin.getConfig().getInt("AntiSpam.AntiCaps.Percent");//gets the configured percent
 						int capcount = message.length();
 						int capcounter = 0;
 						Double uppercase = 0.0;
 						Double lowercase = 0.0;
 						while (capcounter<capcount) {
 							if (message.toCharArray()[capcounter] == message.toLowerCase().toCharArray()[capcounter]) {//counts both upper case and lower case letters
 								++lowercase;
 							}
 							else {
 								++uppercase;
 							}
 							++capcounter;
 						}
 						double total = uppercase + lowercase;
 						double result = uppercase/total;
 						percent = percent/100;
 						if (percent<result){//converts to lowercase if needed
 							message = message.toLowerCase();
 						}
 					}
 				}
 				event.setMessage(message);//changes message to newly generated one
 			}
 			event.setCancelled(spam);//cancel if it spam from the beginning
 		}
 		catch (Exception e){
 			try {
 				catchListenerException(e, event.getEventName());
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 	}
 
 }
