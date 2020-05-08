 /**
  *  Name: RegisterThread.java
  *  Date: 01:27:47 - 18 aug 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Description:
  *  
  *  
  *  
  * 
  * 
  */
 
 package me.lucasemanuel.register;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class RegisterThread extends Thread {
 	
 	private final String urlString;
 	private final Player player;
 	
 	public RegisterThread(final Player player, final String urlString) {
 		this.urlString = urlString;
 		this.player = player;
 		
 		start();
 	}
 	
 	public void run() {
 		
 		String answer = null;
 		
 		try {
 			
 			URL url = new URL(urlString);
 		    URLConnection connection = url.openConnection();
 		 
 		    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 		    answer = in.readLine();
 		    
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		if(answer != null) {
 			switch(answer) {
 				
 				case "0":
 					player.sendMessage(ChatColor.GREEN + "Grattis " + player.getName() + "! Du har registrerats p forumet med ranken " + ChatColor.LIGHT_PURPLE + "Lrling");
 					player.chat("/sync");
 					break;
 					
 				case "1":
 					player.sendMessage(ChatColor.RED + "E-post redan registrerat!");
 					break;
 					
 				case "2":
 					player.sendMessage(ChatColor.RED + "Anvndarnamnet finns redan!");
 					break;
 					
 				default:
 					player.sendMessage(ChatColor.RED + "Ngot verkar ha gtt snett! Kontakta admin/mod!");
 			}
 		}
 		else {
 			player.sendMessage(ChatColor.RED + "Oops! Ngot gick visst fel! Kontakta admin/mod.");
 		}
 	}
 }
