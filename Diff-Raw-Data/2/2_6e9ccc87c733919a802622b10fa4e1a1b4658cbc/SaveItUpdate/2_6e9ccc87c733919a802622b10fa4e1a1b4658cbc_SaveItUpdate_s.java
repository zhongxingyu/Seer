 package me.dretax.SaveIt;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 public class SaveItUpdate
 {
 	/*
 	 * @Author: DreTaX | SaveIt Update Checker File, rewritten from scratch.
 	 */
 	
 	private Main plugin;
 	private String updateVersion;
 	Logger log = Logger.getLogger("Minecraft");
 
 	public SaveItUpdate(Main plugin) {
 		this.plugin = plugin;
 	}
 
 	public Boolean isLatest() {
 		sendConsoleMessage(ChatColor.GREEN + "Checking for updates. Please wait.");
 		try {
 			int updateVer = 0;
 			int curVer = 0;
 			URLConnection yc = new URL("https://raw.github.com/dretax/Saving-it/master/update.txt").openConnection();
 			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
 			PluginDescriptionFile pdf = this.plugin.getDescription();
 			String version = pdf.getVersion();
 			this.updateVersion = in.readLine().replace(".", "");
 			updateVer = Integer.parseInt(this.updateVersion);
 			curVer = Integer.parseInt(version.replace(".", ""));
 			if (updateVer > curVer) {
 				sendConsoleMessage(ChatColor.RED + "A new version of SaveIt is available:  " + ChatColor.GREEN + this.updateVersion);
 				sendConsoleMessage(ChatColor.RED + "Your current version is:  " + ChatColor.GREEN + version);
				sendConsoleMessage(ChatColor.RED + "Get it From: http://goo.gl/yb1ii");
 				return Boolean.valueOf(false);
 			}
 			sendConsoleMessage(ChatColor.GREEN + "No Updates Found...");
 			in.close();
 			return Boolean.valueOf(true);
 		}
 		catch (Exception e) {
 			sendConsoleMessage(ChatColor.GREEN + "Error Occured while check, notify DreTaX!");
 			e.printStackTrace();
 		}return Boolean.valueOf(true);
 	}
 
 	public String getUpdateVersion() {
 		return this.updateVersion;
 	}
   
 	public static void sendConsoleMessage(String msg) {
 		Main._cs.sendMessage(Main._prefix + ChatColor.AQUA + msg);
 	}
   
 }
