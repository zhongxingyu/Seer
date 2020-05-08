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
 public class Friend
 {
 	private final onStartup plugin;
 
 	public Friend(onStartup plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	public void add(Player player1, String player2)
 	{
 		if(player1.getName().equalsIgnoreCase(player2))
 		{
 			player1.sendMessage(ChatColor.RED + "Cant add yourself to your own friends list");
 			return;
 		}
 		User user1 = this.plugin.getUserManager().getUser(player1);
 		Player p2 = Bukkit.getPlayer(player2);
 		
 		// To fix case isues
 		player2 = p2.getName();
 		
 		User user2 = this.plugin.getUserManager().getUser(p2);
 		if (user1 == null || user2 == null)
 		{
 			player1.sendMessage(ChatColor.RED + "Something went wrong while accessing the friend list of the player you specified");
 			return;
 		}
 		
 		// Remove the enemy binding to fix bugs
 		user1.getEnemies().remove(player2);
 		
 		String playerName = player1.getName();
 		if (user2.getFriendsRequestsPending().contains(playerName))
 		{
 			/*
 			 * Other player has sent you an invite
 			 */
 			user2.getFriendsRequestsPending().remove(playerName);
 			user1.getFriendsRequestsPending().remove(playerName);
 			addToList(user2.getFriends(),playerName);
 			addToList(user1.getFriends(),playerName);
 			player1.sendMessage(ChatColor.GREEN + "Succesfully registered " + p2.getName() + " as your friend.");
 			p2.sendMessage(ChatColor.GREEN + playerName + " has accepted your friend request.");
 		}
 		else if (user1.getFriends().contains(playerName))
 		{
 			/*
 			 * Other player is already on your friend list
 			 */
 			player1.sendMessage(ChatColor.RED + p2.getName() + " is already on your friends list");
 		}
 		else if (user1.getFriendsRequestsPending().contains(playerName))
 		{
 			if (user2.getFriends().contains(playerName))
 			{
 				/*
 				 * There was a save bug, lets try to fix it
 				 */
 				addToList(user2.getFriends(),playerName);
 				addToList(user1.getFriends(),playerName);
 				user1.getFriendsRequestsPending().remove(playerName);
 				user2.getFriendsRequestsPending().remove(playerName);
 				player1.sendMessage(ChatColor.YELLOW + "There was a bug at the friends system, We are trying default protocol to fix it.");
 			}
 			else
 			{
 				/*
 				 * Resend friend request
 				 */
 				p2.sendMessage(ChatColor.GOLD + playerName + " has sent you a friend request, do /friend "+playerName + " to accept it");
 			}
 		}
 		else
 		{
 			/*
 			 * New friend request
 			 */
 			addToList(user1.getFriendsRequestsPending(),player2);
 			p2.sendMessage(ChatColor.GOLD + playerName + " has send you're a friend request, do /friend "+playerName + " to accept it");
 			
 		}
 	}
 
 	private <T> void addToList(List<T> list, T value)
 	{
 		if (!list.contains(value))
 		{
 			list.add(value);
 		}
 	}
 }
