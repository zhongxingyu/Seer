 package com.jkush321.autowalls;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.kitteh.tag.TagAPI;
 
 public class ColorCycler {
 	public static int MAX_COLOR_TIME;
 	
 	public static Map<Player, ChatColor> fakeColors = new HashMap<>();
 	public static Map<Player, Integer> colorTime = new HashMap<>();
 	
 	public static ChatColor getFakeColor(Player p)
 	{
 		if (fakeColors.containsKey(p)) return fakeColors.get(p);
 		return null;
 	}
 	public static void setFakeColor(Player p, ChatColor c)
 	{
 		if (fakeColors.containsKey(p)) fakeColors.remove(p);
 		fakeColors.put(p, c);
 		TagAPI.refreshPlayer(p);
 	}
 	public static void cycle(Player p)
 	{
 		ChatColor nextColor;
 		if (getFakeColor(p) == null)
 		{
 			if (AutoWalls.redTeam.contains(p))
 			{
 				nextColor = ChatColor.DARK_AQUA;
 			}
 			else if (AutoWalls.blueTeam.contains(p))
 			{
 				nextColor = ChatColor.DARK_GREEN;
 			}
 			else if (AutoWalls.greenTeam.contains(p))
 			{
 				nextColor = ChatColor.GOLD;
 			}
 			else
 			{
 				nextColor = ChatColor.DARK_RED;
 			}
 		}
 		else
 		{
 			if (getFakeColor(p)==ChatColor.DARK_RED)
 			{
 				nextColor = ChatColor.DARK_AQUA;
 			}
 			else if (getFakeColor(p)==ChatColor.DARK_AQUA)
 			{
 				nextColor = ChatColor.DARK_GREEN;
 			}
 			else if (getFakeColor(p)==ChatColor.DARK_GREEN)
 			{
 				nextColor = ChatColor.GOLD;
 			}
 			else
 			{
 				nextColor = ChatColor.DARK_RED;
 			}
 		}
 		if ((AutoWalls.redTeam.contains(p) && nextColor == ChatColor.DARK_RED) ||
 		(AutoWalls.blueTeam.contains(p) && nextColor == ChatColor.DARK_AQUA) ||
 		(AutoWalls.greenTeam.contains(p) && nextColor == ChatColor.DARK_GREEN) ||
 		(AutoWalls.orangeTeam.contains(p) && nextColor == ChatColor.GOLD))
 		{
 			setFakeColor(p, null);
 			p.sendMessage(ChatColor.GREEN + "You now have your original nameplate color");
 		}
 		else
 		{
 			if (nextColor == ChatColor.DARK_RED)
 			{
				p.sendMessage(ChatColor.GREEN + "You nameplate now appears as red");
 			}
 			else if (nextColor == ChatColor.DARK_AQUA)
 			{
				p.sendMessage(ChatColor.GREEN + "You nameplate now appears as blue");
 			}
 			else if (nextColor == ChatColor.DARK_GREEN)
 			{
				p.sendMessage(ChatColor.GREEN + "You nameplate now appears as green");
 			}
 			else
 			{
				p.sendMessage(ChatColor.GREEN + "You nameplate now appears as orange");
 			}
 			setFakeColor(p, nextColor);
 		}
 	}
 	public static void tick()
 	{
 		if (fakeColors.size() > 0)
 		{
 			Set<Player> copiedSet = new HashSet<Player>();
 			copiedSet.addAll(fakeColors.keySet());
 			
 			for (Player p : copiedSet)
 			{
 				if (fakeColors.get(p) != null)
 				{
 					if (colorTime.containsKey(p))
 					{
 						int time;
 						if ((time = colorTime.get(p)) > 0)
 						{
 							time = time - 1;
 							colorTime.remove(p);
 							colorTime.put(p, time);
 						}
 						else
 						{
 							fakeColors.remove(p);
 							p.sendMessage(ChatColor.RED + "Your ability to change color has worn off!");
 							TagAPI.refreshPlayer(p);
 						}
 					}
 					else
 					{
 						colorTime.put(p, MAX_COLOR_TIME);
 					}
 				}
 			}
 		}
 	}
 	
 }
