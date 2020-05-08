 package net.othercraft.steelsecurity.listeners;
  
 import java.util.List;
 import net.othercraft.steelsecurity.Config;
 import net.othercraft.steelsecurity.Main;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.*;
  
 public class PlayerChatListener implements Listener {
 	
 public Main plugin;
 	
 	public PlayerChatListener(Main instance) {	
 		plugin = instance;
 	}	
 
 	@EventHandler
     public void onPlayerchat(PlayerChatEvent event) {
 		if (new Config(plugin).getConfigurationBoolean("AntiSpam.Censoring.Enabled") && event.getPlayer().hasPermission("steelsecurity.antispam.bypasscensor") == false) {
 		String Listpath = "AntiSpam.Censoring.Block_Words";
 		 List<String> list = new Config(plugin).getConfigurationList(Listpath);
 		 String message = event.getMessage();
 		 int wordcount = list.size();
 		 int wordcounter = 0;
 		 while (wordcounter<wordcount) {
 			 int lettercount = list.get(wordcounter).toCharArray().length;
 			 int lettercounter = 0;
 			 String newword = "";
 			 String badword = list.get(wordcounter).toString();	 	 
 			 while (lettercounter<lettercount) {
 				 newword = (newword + "*");
 				 lettercounter = lettercounter + 1; 
 			 }
			 message = message.replaceAll(badword, newword);
 			 wordcounter = wordcounter + 1; 
 		 }
 		 event.setMessage(message);
 		}
 	}
 }
