 package com.jamesst20.jcommandessentials.Methods;
 
 import com.jamesst20.jcommandessentials.JCMDEssentials.JCMDEss;
 
 public class ServerMotd {
 	static JCMDEss plugin = JCMDEss.plugin;
 	private static String defaultMotd = "&f[&aJCMD&2Ess&f] &cMotd not set!";
 	
 	public static void setServerMotd(String message){
 		plugin.getConfig().set("server.motd", message);
 		plugin.saveConfig();
 	}
 	public static String getServerMotd(){
 		String motd = plugin.getConfig().getString("server.motd");
 		if (motd != null){
 			return coloring(plugin.getConfig().getString("server.motd"));
 		}else{
 			return "";
 		}
 	}
 	public static void disableServerMotd(){
 		plugin.getConfig().set("enable.servermotd", Boolean.valueOf(false));
 		plugin.saveConfig();
 	}
 	public static void enableServerMotd(){
 		plugin.getConfig().set("enable.servermotd", Boolean.valueOf(true));
 		plugin.saveConfig();
 	}
 	public static void setDefaultConfig(){
 		if (plugin.getConfig().get("enable.servermotd") == null){
 			plugin.getConfig().set("enable.servermotd", Boolean.valueOf(true));
 		}
 		if (plugin.getConfig().get("server.motd") == null){
 			plugin.getConfig().set("server.motd", defaultMotd);
 		}
 		plugin.saveConfig();
 	}
 	public static boolean isEnabled(){
 		return plugin.getConfig().getBoolean("enable.servermotd");
 	}
 	private static String coloring(String string)
 	  {
 	    if (string == null) return null;
	    return string.replaceAll("&([0-9a-fA-Fk-oK-OrR])", "�$1");
 	  }
 }
