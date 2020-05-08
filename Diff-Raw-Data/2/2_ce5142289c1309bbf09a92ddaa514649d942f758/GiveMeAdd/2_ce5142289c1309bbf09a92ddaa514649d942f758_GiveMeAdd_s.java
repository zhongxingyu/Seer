 package com.bukkit.cian1500ww.giveit;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * GiveIt for Bukkit
  *
  * @author cian1500ww cian1500ww@gmail.com
  * @version 1.2
  * This class logs the players actions to GiveIt.log
  * 
  */
 
 public class GiveMeAdd {
 	
 	private ArrayList mods = new ArrayList();
 	public boolean givemeadd(CommandSender sender, String[] trimmedArgs) throws IOException{
 		
 		
 		Player player = (Player)sender;
 		String f = "plugins/GiveIt/mods.txt";
 		BufferedReader in = new BufferedReader(new FileReader(f));
 		
		while(in.readLine()==null){
 			mods.add(in.readLine());
 		}
 		
 		if ((trimmedArgs[0] == null) || (trimmedArgs[1]== null)) {
             return false;
         }
 		
 		if(mods.contains(player)==true && trimmedArgs[2]==null){
 			String itemid = trimmedArgs[0];
 			String amount = trimmedArgs[1];
 				
 			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
 			out.write(itemid+"="+amount);
 			return true;
 		}
 		
 		else if(mods.contains(player)==true && trimmedArgs[2]!=null){
 			String itemid = trimmedArgs[0];
 			String amount = trimmedArgs[1];
 			String chosen_player = trimmedArgs[2];	
 			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
 			out.write(itemid+"="+amount+"."+chosen_player);
 			return true;
 		}
 		
 		else if(mods.contains(player)==false){
 			player.sendMessage("GiveIt: You are not allowed to add items");
 			return true;
 		}
 		else
 			return false;
 	}
 
 	public boolean givemeremove(CommandSender sender, String[] trimmedArgs) throws IOException{
 		
 		Properties prop = new Properties();
 		try {
 			InputStream is = new FileInputStream("plugins/GiveIt/allowed.txt");
 			prop.load(is);
 		} catch (IOException e) {
 			System.out.println("GiveIt: Problem opening allowed.txt file");
 		}
 		
 		Player player = (Player)sender;
 		String f = "plugins/GiveIt/mods.txt";
 		BufferedReader in = new BufferedReader(new FileReader(f));
 		
 		while(in.readLine()!=null){
 			mods.add(in.readLine());
 		}
 		
 		if ((trimmedArgs[0] == null) || (trimmedArgs[0].length() > 3) || (trimmedArgs[0].length() < 3)) {
             return false;
         }
 		
 		if(mods.contains(player)==true){
 			prop.remove(trimmedArgs[0]);
 			player.sendMessage("GiveIt: Successfully removed item number "+ trimmedArgs[0]);
 			return true;
 		}
 		
 		else if(mods.contains(player)==false){
 			player.sendMessage("GiveIt: You are not allowed to add items");
 			return true;
 		}
 		else
 			return false;
 	}
 }
