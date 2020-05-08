 package com.minecraftdimensions.bungeesuite.configs;
 
 import java.io.File;
 
 import com.minecraftdimensions.bungeesuite.configlibrary.Config;
 
 public class ChatConfig {
 
 
 		 private static String configpath = File.separator+"plugins"+File.separator+"BungeeSuite"+File.separator+"chat.yml";
 		 public static Config c = new Config(configpath);
 		 public static boolean logChat = c.getBoolean("Log chat to console", true);
 		 public static boolean stripChat = c.getBoolean("Strip color from log", true);
 		 public static boolean mutePrivateMessages = c.getBoolean("MutePrivateMessages", true);
 		 public static int nickNameLimit = c.getInt("NicknameLengthLimit", 16);
 		 public static String globalChatRegex = c.getString("GlobalChatRegex",
 		 "\\{(factions_.*?)\\}");
 		 public static String defaultChannel = c.getString("DefaultChannel", "Global");		 
		 public static boolean broadcastProxyConnectionMessages = c.getBoolean("RemoveServerConnectionMessages", true);
 		 public static void reload(){
 			 c = new Config(configpath);
 			 logChat = c.getBoolean("Log chat to console", true);
 			 stripChat = c.getBoolean("Strip color from log", true);
 			 mutePrivateMessages = c.getBoolean("MutePrivateMessages", true);
 			 nickNameLimit = c.getInt("NicknameLengthLimit", 16);
 			 globalChatRegex = c.getString("GlobalChatRegex",
 			 "\\{(factions_.*?)\\}");
 			 defaultChannel = c.getString("DefaultChannel", "Global");	
 		 }
 }
