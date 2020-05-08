 package com.lala.wordrank;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerListener;
 
 import com.lala.wordrank.misc.PermHandle;
import com.lala.wordrank.misc.RedeemType;
 import com.lala.wordrank.sql.SQLWord;
 
 public class ChatListen extends PlayerListener{
 
 	private WordRank plugin;
 	
 	public ChatListen(WordRank plugin){
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public void onPlayerChat(PlayerChatEvent event){
		if (plugin.redeemtype.equals(RedeemType.Command) || plugin.redeemtype.equals(RedeemType.Unknown)) return;
 		Player player = event.getPlayer();
 		String msg = event.getMessage();
 		
 		if (player.hasPermission("WordRank.say") || player.hasPermission("WordRank."+msg)){
 			Word w = new Word(msg, "unknown");
 			SQLWord sw = new SQLWord(plugin, w);
 			ArrayList<String> wordlist = sw.getWords();
 			if (wordlist.contains(msg)){
 				Config config = new Config(plugin);
 				PermHandle ph = new PermHandle(plugin, config.getPerms(), player);
 				String groupname = sw.getWordGroup();
 				
 				if (ph.getPlayerGroups().contains(groupname)) return;
 				w.setGroup(groupname);
 				ph.setGroup(groupname);
 				
 				player.sendMessage(ChatColor.GREEN+"Congrats! You have been promoted to the group "+ChatColor.YELLOW+w.getGroup()+ChatColor.GREEN+"!");
 				event.setCancelled(true);
 				plugin.send(player.getName()+" has been promoted to "+w.getGroup()+" by WordRank.");
 				return;
 			}
 		}else{
 			return;
 		}
 	}
 }
