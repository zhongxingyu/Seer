 package com.github.winneonsword.WCS;
 
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Scanner;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 @SuppressWarnings("static-access")
 public class CommandWCS implements CommandExecutor {
 	
 	MainWCS plugin;
 	
 	public CommandWCS(MainWCS plugin){
 		
 		this.plugin = plugin;
 		
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		
 		if (cmd.getName().equalsIgnoreCase("wcs")){
 			
 			String intro = plugin.ConfigWCS.getString("intro");
 			
 			try {
 				
 				URLConnection connect = new URL("http://status.mojang.com/check").openConnection();
 				
 				connect.setConnectTimeout(8000);
 				connect.setReadTimeout(15000);
 				connect.setRequestProperty("User-agent", "WCSession");
 				
 				InputStreamReader is = new InputStreamReader(connect.getInputStream());
 				String jsonTxt = convertStreamToString(is);
 				
 				try {
 					
 					JSONArray array = new JSONArray(jsonTxt);
 					JSONObject websiteA = array.getJSONObject(0);
 					JSONObject loginA = array.getJSONObject(1);
 					JSONObject sessionA = array.getJSONObject(2);
 					JSONObject skinsA = array.getJSONObject(5);
 					String website = websiteA.getString("minecraft.net");
 					String login = loginA.getString("login.minecraft.net");
 					String session = sessionA.getString("session.minecraft.net");
 					String skins = skinsA.getString("skins.minecraft.net");
 					
 					sender.sendMessage(rA(intro + "Minecraft Status:"));
 					
 					if (website.equals("green")){
 						
 						sender.sendMessage(rA("&5- &dWebsite: &aOnline"));
 						
 					} else if (website.equals("yellow")){
 						
 						sender.sendMessage(rA("&5- &dWebsite: &6Slow, but Online."));
 						
 					} else if (website.equals("red")){
 						
 						sender.sendMessage(rA("&5- &dWebsite: &cTimed Out or Offline"));
 						
					} else {
						
						sender.sendMessage(rA("&5- &dWebsite: &cTimed Out or Offline"));
						
 					}
 					
 					if (login.equals("green")){
 						
 						sender.sendMessage(rA("&5- &dLogin: &aOnline"));
 						
 					} else if (login.equals("yellow")){
 						
 						sender.sendMessage(rA("&5- &dLogin: &6Slow, but Online."));
 						
 					} else if (login.equals("red")){
 						
 						sender.sendMessage(rA("&5- &dLogin: &cTimed Out or Offline"));
 						
					} else {
						
						sender.sendMessage(rA("&5- &dWebsite: &cTimed Out or Offline"));
						
 					}
 					
 					if (session.equals("green")){
 						
 						sender.sendMessage(rA("&5- &dSession: &aOnline"));
 						
 					} else if (session.equals("yellow")){
 						
 						sender.sendMessage(rA("&5- &dSession: &6Slow, but Online."));
 						
 					} else if (session.equals("red")){
 						
 						sender.sendMessage(rA("&5- &dSession: &cTimed Out or Offline"));
 						
					} else {
						
						sender.sendMessage(rA("&5- &dWebsite: &cTimed Out or Offline"));
						
 					}
 					
 					if (skins.equals("green")){
 						
 						sender.sendMessage(rA("&5- &dSkins: &aOnline"));
 						
 					} else if (skins.equals("yellow")){
 						
 						sender.sendMessage(rA("&5- &dSkins: &6Slow, but Online."));
 						
 					} else if (skins.equals("red")){
 						
 						sender.sendMessage(rA("&5- &dSkins: &cTimed Out or Offline"));
 						
					} else {
						
						sender.sendMessage(rA("&5- &dWebsite: &cTimed Out or Offline"));
						
 					}
 					
 				} catch(JSONException e) {
 					
 					sender.sendMessage(rA(intro + "Could not parse the JSON! Here is the string:"));
 					sender.sendMessage(jsonTxt);
 					e.printStackTrace();
 					
 				}
 				
 				is.close();
 				
 			} catch (Exception e){
 				
 				sender.sendMessage(rA(intro + "Could not connect to the servers!"));
 				
 			}
 			
 			return true;
 			
 		} else {
 			
 			return false;
 			
 		}
 		
 	}
 	
 	@SuppressWarnings("resource")
 	static String convertStreamToString(InputStreamReader is){
 		
 	    java.util.Scanner s = new Scanner(is).useDelimiter("\\A");
 	    
 	    return s.hasNext() ? s.next() : "";
 	    
 	}
 	
 	public static String rA(String message){
 		message = ChatColor.translateAlternateColorCodes('&', message);
 		return message;
 	}
 	
 }
