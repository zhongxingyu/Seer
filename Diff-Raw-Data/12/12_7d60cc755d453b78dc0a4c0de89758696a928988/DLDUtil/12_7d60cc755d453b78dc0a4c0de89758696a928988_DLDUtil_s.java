 package me.scarfacehd2.DarianLikesDick;
 
 public class DLDUtil {
 
	static DarianLikesDick plugin;
 	
 	/*
 	 *  consoleMSG : Send (String)msg to the console with level (String)level.
 	 */
 	public static void consoleMSG(String level, String msg)
 	{
		if(level == "warning") plugin.getLogger().warning("[DarianLikesDick] " + msg);
		if(level == "severe") plugin.getLogger().severe("[DarianLikesDick] " + msg);
		else plugin.getLogger().info("[DarianLikesDick] " + msg);
 	}
 	
 }
