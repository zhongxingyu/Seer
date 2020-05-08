 package me.wildn00b.prekick;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Language {
 
 	public YamlConfiguration file;
 
 	public Language(PreKick prekick) {
 		file = new YamlConfiguration();
 		try {
 			File f = new File(prekick.getDataFolder().getAbsolutePath() + File.separator + "lang" + File.separator + prekick.config.GetString("PreKick.Language") + ".yml");
 			if (f.exists())
 				file.load(f);
 			else {
 				f = new File(prekick.getDataFolder().getAbsolutePath() + File.separator + "lang" + File.separator + "en-US.yml");
 				if (f.exists())
 					file.load(new File(prekick.getDataFolder().getAbsolutePath() + File.separator + "lang" + File.separator + "en-US.yml"));
 				else
 					createFile(f);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void Close() {
 		file = null;
 	}
 
 	private void createFile(File f) {
		file.set("Listener.Message.Base", "'%PLAYER%' from IP '%IP%' was kicked");
 		file.set("Listener.Message.WhiteList", "for not being on the whitelist.");
 		file.set("Listener.Message.IP", "for the IP not being on the IP whitelist.");
 		file.set("Listener.Message.BlackList", "for being on the blacklist.");
 		file.set("Listener.Message.Missbehaving", "for PreKick missbehaving!");
 		file.set("Listener.Message.Console.Missbehaving", "Unknown kick reason. reason: %REASON%");
 		file.set("Listener.EasterEgg", "&6WildN00b, the developer for PreKick is connecting. Say Hi!");
		file.set("Listener.Console.EasterEgg", "You can disable this message, if you want ;(, just turn off 'Easter-egg' in the config");
 
 		file.set("Permissions.NotFound", "Vault not found, fall back on Bukkit Permissions.");
 
 		file.set("PreKick.Initialized", "Initialized successfully.");
 		file.set("PreKick.Disabling", "Disabling successfully.");
 
 		file.set("PreKickCommand.Status.PreKick", "PreKick V%VERSION% status page");
 		file.set("PreKickCommand.Status.Status.PreKick", "PreKick status:");
 		file.set("PreKickCommand.Status.Status.Whitelist", "Whitelist status:");
 		file.set("PreKickCommand.Status.Status.IP", "IP status:");
 		file.set("PreKickCommand.Status.Status.Blacklist", "Blacklist status:");
 		file.set("PreKickCommand.Status.On", "&aon");
 		file.set("PreKickCommand.Status.Off", "&coff");
 		file.set("PreKickCommand.Toggle.Enable", "%NAME% is now &aenabled.");
 		file.set("PreKickCommand.Toggle.AlreadyEnable", "%NAME% is already enabled.");
 		file.set("PreKickCommand.Toggle.Disable", "%NAME% is now &cdisabled.");
 		file.set("PreKickCommand.Toggle.AlreadyEnable", "%NAME% is already disabled.");
 		file.set("PreKickCommand.Help.Help", "help <Number> - Shows <Number> page of help.");
 		file.set("PreKickCommand.Help.Status", "status - Show some status about PreKick.");
 		file.set("PreKickCommand.Help.Switch.On", "on - Turns %CMD% on.");
 		file.set("PreKickCommand.Help.Switch.Off", "off - Turns %CMD% off.");
 		file.set("PreKickCommand.Help.Whitelist.Add", "whitelist add <Player> - Adds player to the whitelist.");
 		file.set("PreKickCommand.Help.Whitelist.Remove", "whitelist remove <Player> - Removes player from the whitelist.");
 		file.set("PreKickCommand.Help.IP.Add", "ip add <Player> <IP> - Adds player to the ip whitelist with the ip or adds ip the player.");
 		file.set("PreKickCommand.Help.IP.Remove", "ip remove <Player> - Removes player from the ip whitelist.");
 		file.set("PreKickCommand.Help.IP.RemoveIP", "ip remove <Player> <IP> - Removes IP from the player from the ip whitelist.");
 		file.set("PreKickCommand.Help.Blacklist.Add", "blacklist add <Group> <Player> - Adds player to the group in the blacklist.");
 		file.set("PreKickCommand.Help.Blacklist.Remove", "blacklist remove <Group> <Player> - Removes player from the group in the blacklist.");
 		file.set("PreKickCommand.Help.Page", "PreKick V%VERSION% Page %PAGE%/%MAXPAGE%");
 
 		file.set("Whitelist.KickMessage.CantFindGroup", "Can't find the blacklist group for %PLAYER%");
 		file.set("Whitelist.KickMessage.UnknownReason", "Unknown kick reason. reason: %REASON%");
 		file.set("Whitelist.KickMessage.Missbehaving", "PreKick missbehaving.");
 		file.set("Whitelist.Whitelist.AlreadyAdded", "%PLAYER% already on the whitelist.");
 		file.set("Whitelist.Whitelist.Add", "Added %PLAYER% to the whitelist successfully.");
 		file.set("Whitelist.Whitelist.NotOnList", "%PLAYER% is not on the whitelist.");
 		file.set("Whitelist.Whitelist.Removed", "Removed %PLAYER% from the whitelist successfully.");
 		file.set("Whitelist.IP.AddIP", "Added IP to %PLAYER% in the IP whitelist successfully.");
 		file.set("Whitelist.IP.Add", "Added %PLAYER% in the IP whitelist successfully.");
 		file.set("Whitelist.IP.RemovedIP", "Removed IP from the %PLAYER% from the IP whitelist successfully.");
 		file.set("Whitelist.IP.Removed", "Removed %PLAYER% from the IP whitelist successfully.");
 		file.set("Whitelist.IP.NotOnList", "Can't find %PLAYER% on the IP whitelist.");
 		file.set("Whitelist.Blacklist.AlreadyAdded", "%PLAYER% already on the blacklist.");
 		file.set("Whitelist.Blacklist.Add", "Added %PLAYER% to the blacklist successfully.");
 		file.set("Whitelist.Blacklist.NotOnList", "%PLAYER% is not on the blacklist.");
 		file.set("Whitelist.Blacklist.Removed", "Removed %PLAYER% from the blacklist successfully.");
 		try {
 			file.save(f);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String processColors(String text) {
 		return text.replaceAll("&", "\u00A7");
 	}
 
 	public String GetText(String path) {
 		return processColors(file.getString(getPath(path), path));
 	}
 
 	public String getPath(String path) {
 		String split[] = path.split("\\.");
 		if (split.length <= 0)
 			return path;
 
 		String newPath = "";
 		boolean set = true;
 
 		ConfigurationSection section = file.getRoot();
 
 		for (int i = 0; i < split.length; i++) {
 			if (!set) {
 				newPath += "." + split[i];
 				continue;
 			}
 
 			set = false;
 			try {
 				Set<String> entries = section.getKeys(false);
 
 				for (String x : entries) {
 					if (x.equalsIgnoreCase(split[i])) {
 						newPath += "." + x;
 						set = true;
 						section = section.getConfigurationSection(x);
 					}
 				}
 			} catch (Exception e) {
 				set = false;
 			}
 			if (!set) {
 				newPath += "." + split[i];
 			}
 		}
 
 		return newPath.substring(1);
 	}
 
 }
