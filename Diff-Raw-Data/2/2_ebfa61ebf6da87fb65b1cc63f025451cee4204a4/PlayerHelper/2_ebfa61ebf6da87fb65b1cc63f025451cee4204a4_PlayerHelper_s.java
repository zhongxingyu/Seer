 package de.minestar.cok.helper;
 
 import net.minecraft.server.MinecraftServer;
 
 public class PlayerHelper {
 
 
 	
 	public static boolean isOnlineUser(String username){
 		for(String name:MinecraftServer.getServer().getConfigurationManager().getAllUsernames()){
			if(name.equalsIgnoreCase(username)){
 				System.out.println(name);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 }
