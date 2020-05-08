 package net.mayateck.ChatChannels;
 
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 @SuppressWarnings("unused")
 public class RunBroadcasts implements Runnable{
 	private ChatChannels plugin;
 	public RunBroadcasts(ChatChannels plugin){
 		this.plugin = plugin;
 	}
 	int pointer = 0;
 	@Override
 	public void run() {
 		boolean rand = ChatChannels.rand;
 		String b = ChatChannels.bTag;
 		String c = ChatChannels.bColor;
 		List<String> msgList = ChatChannels.casts;
 		String msg = "";
 		if (msgList!=null){
 			if (rand==true){
 				Random randomise = new Random();
 				int selection = randomise.nextInt(msgList.size());
 				msg = msgList.get(selection);
 			} else {
 				msg = msgList.get(pointer);
 				pointer++;
 				if (pointer>=msgList.size()){
 					pointer=0;
 				}
 			}
			msg = ChatColor.translateAlternateColorCodes('&', msg);
 			msg = ""+c+b+" "+msg;
 			plugin.getServer().broadcastMessage(msg);
 		} else {
 			plugin.getLogger().info("Var 'msgList' was null. (Unknown reason.)");
 		}
 	}
 }
