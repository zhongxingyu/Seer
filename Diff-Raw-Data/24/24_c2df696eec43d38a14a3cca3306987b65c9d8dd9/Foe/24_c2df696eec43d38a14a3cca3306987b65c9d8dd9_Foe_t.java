 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.CC.Commands.relation;
 
 import com.CC.General.User;
 import com.CC.General.onStartup;
 import java.util.List;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Fernando
  */
 public class Foe
 {
 	private final onStartup plugin;
 
 	public Foe(onStartup plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	private <T> void addToList(List<T> list, T value)
 	{
 		if (!list.contains(value))
 		{
 			list.add(value);
 		}
 	}
 
 	private void removeFriendBindings(User u1, User u2)
 	{
 		if (u1.getFriends().contains(u2.getPlayer().getName()))
 		{
 			u1.getFriends().remove(u2.getPlayer().getName());
 			u2.getFriends().remove(u1.getPlayer().getName());
 			addToList(u2.getFriendsRequestsPending(), u1.getPlayer().getName());
 			u1.getPlayer().sendMessage(ChatColor.GREEN + u2.getPlayer().getName() + " was been removed from your friends list!");
 		}
 	}
 
 	public void add(Player player1, String player2)
 	{
 		if (player1.getName().equalsIgnoreCase(player2))
 		{
 			player1.sendMessage(ChatColor.RED + "Cant add you're self to your own enemy list");
 			return;
 		}
 		User user1 = this.plugin.getUserManager().getUser(player1);
 		Player p2 = Bukkit.getPlayer(player2);
 
                if(p2 == null)
                {
                    player1.sendMessage(ChatColor.RED + "This player is offline!");
                    return;
                }
                
 		// To fix case isues
 		player2 = p2.getName();
 
 		if (p2 == null)
 		{
 			player1.sendMessage(ChatColor.RED + "Your enemy need to be online");
 			return;
 		}
 		User user2 = this.plugin.getUserManager().getUser(p2);
 		if (user1 == null)
 		{
 			player1.sendMessage(ChatColor.RED + "Something went at accessing the enemy list of the player you specified");
 			return;
 		}
 		if (user2 != null)
 		{
 			/*
 			 * Makes you able to add enemies even if the other player dont have its own User object, without NPE thrown here
 			 */
 			removeFriendBindings(user1, user2);
 		}
 		addToList(user1.getEnemies(), player2);
 		player1.sendMessage(ChatColor.GREEN + "Added enemy: " + player2);
 	}
 }
