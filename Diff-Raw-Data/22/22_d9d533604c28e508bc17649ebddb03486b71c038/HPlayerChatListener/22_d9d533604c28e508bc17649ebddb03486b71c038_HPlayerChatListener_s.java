 package com.fsscripts.bentzilla;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerListener;
 public class HPlayerChatListener extends PlayerListener {
 	protected Health plugin;
 
 	public HPlayerChatListener(Health plugin) {
 		this.plugin = plugin;
 	}
 
 	public void onPlayerChat(PlayerChatEvent event) {
 		Player player = event.getPlayer();
 		for (Player P : plugin.getServer().getOnlinePlayers()) {
 			if (Health.hpChatEnabled(P)) {
 				ChatColor c1 = ChatColor.GREEN;
 				ChatColor c2 = ChatColor.GREEN;
 				ChatColor c3 = ChatColor.GREEN;
				System.out.println(Health.chatHealthUsers.values().toString());
 				if (Health.chatHealthUsers.containsValue("off")){
 					if (player.getHealth() <= 10){
 						c1 = ChatColor.RED;
 					} else {
 						c1 = ChatColor.GREEN;
 					}
					P.sendMessage("<" + player.getDisplayName() + "> [" + c1 + player.getHealth() + ChatColor.WHITE + "/" + ChatColor.GREEN + "20" +  ChatColor.WHITE + "]: "  + event.getMessage().toString());
 				} else if(Health.chatHealthUsers.containsValue("on")){
 					String s1 ="", s2 = "", s3 = "";
 					Integer h = player.getHealth();
					System.out.println(h);
 					switch (h){
 					//case 20: 
 					case 19: s1 = "|||||||||";
 							 c2 = ChatColor.YELLOW;
 							 s2 = "|";
 							 break;
 					case 18: s1 = "||||||||";
 			 		 		 c3 = ChatColor.RED;
 			 		 		 s3 = "|";
 			 		 		 break;
 					case 17: s1 = "||||||||";
 					 		 c2 = ChatColor.YELLOW;
 					 		 s2 = "|";
 					 		 c3 = ChatColor.RED;
 					 		 s3 = "|";
 					 		 break;
 					case 16: s1 = "||||||||";
 	 		 		 		 c3 = ChatColor.RED;
 	 		 		 		 s3 = "||";
 	 		 		 		 break;
 					case 15: s1 = "|||||||";
 			 		 		 c2 = ChatColor.YELLOW;
 			 		 		 s2 = "|";
 			 		 		 c3 = ChatColor.RED;
 			 		 		 s3 = "||";
 			 		 		 break;
 					case 14: s1 = "|||||||";
 	 		 		 		 c3 = ChatColor.RED;
 	 		 		 		 s3 = "|||";
 	 		 		 		 break;
 					case 13: s1 = "||||||";
 							 c2 = ChatColor.YELLOW;
 							 s2 = "|";
 							 c3 = ChatColor.RED;
 							 s3 = "|||";
 							  break;
 					case 12: s1 = "||||||";
 	 		 		 		 c3 = ChatColor.RED;
 	 		 		 		 s3 = "||||";
 	 		 		 		 break;
 					case 11: s1 = "|||||";
 			 		 	 	 c2 = ChatColor.YELLOW;
 			 		 	 	 s2 = "||||";
 			 		 	 	 c3 = ChatColor.RED;
 			 		 	 	 s3 = "|";
 			 		 	 	 break;
 					case 10: s1 = "|||||";
 	 		 		 		 c3 = ChatColor.RED;
 	 		 		 		 s3 = "|||||";
 	 		 		 		 break;
 					case 9: s1 = "||||";
 							c2 = ChatColor.YELLOW;
 							s2 = "|||||";
 							c3 = ChatColor.RED;
 							s3 = "|";
 							break;
 					case 8: s1 = "||||";
 	 		 		 		c3 = ChatColor.RED;
 	 		 		 		s3 = "||||||";
 	 		 		 		break;
 					case 7: s1 = "|||";
 							c2 = ChatColor.YELLOW;
 							s2 = "|";
 							c3 = ChatColor.RED;
 							s3 = "||||||";
 							break;
 					case 6: s1 = "|||";
 	 		 		 		c3 = ChatColor.RED;
 	 		 		 		s3 = "|||||||";
 	 		 		 		break;
 					case 5: s1 = "||";
 							c2 = ChatColor.YELLOW;
 							s2 = "|";
 							c3 = ChatColor.RED;
 							s3 = "|||||||";
 							break;
 					case 4: s1 = "||";
 	 		 		 		c3 = ChatColor.RED;
 	 		 		 		s3 = "||||||||";
 	 		 		 		break;
 					case 3: s1 = "|";
 			 		 		c2 = ChatColor.YELLOW;
 			 		 		s2 = "|";
 			 		 		c3 = ChatColor.RED;
 			 		 		s3 = "||||||||";
 			 		 		break;
 					case 2: s1 = "|";
 	 		 		 		c3 = ChatColor.RED;
 	 		 		 		s3 = "|||||||||";
 	 		 		 		break;
 					case 1: s1 = "";
 							c2 = ChatColor.YELLOW;
 							s2 = "|";
 							c3 = ChatColor.RED;
 							s3 = "|||||||||";
 							break;
 					case 0: s1 = "";
 	 		 		 		c3 = ChatColor.RED;
 	 		 		 		s3 = "||||||||||";
 	 		 		 		break;
 					}
					System.out.println(c1 + s1 + c2 + s2 + c3 + s3);
					P.sendMessage("[" + c1 + s1 + c2 + s2 + c3 + s3 + ChatColor.WHITE + "]: <" + player.getDisplayName() + "> "  + event.getMessage().toString());
 				}
 			} else {
 				P.sendMessage("<" + player.getDisplayName() + "> " + event.getMessage().toString());
 			}	
 		}
 		System.out.println("<" + player.getDisplayName() + "> " + event.getMessage().toString());
 		event.setCancelled(true);
 	}
 }
