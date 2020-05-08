package me.cain.mobspawn;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class Util
 {
 	public void console(String message)
 	{
 		System.out.println(message);
 	}
 	
 	public void success(Player p, String message)
 	{
 		p.sendMessage(ChatColor.GREEN + message);
 	}
 	
 	public void deny(Player p, String message)
 	{
 		p.sendMessage(ChatColor.RED + message);
 	}
 	
 }
