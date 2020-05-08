 package nl.taico.tekkitrestrict.config;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.github.dreadslicer.tekkitrestrict.Log;
 import com.github.dreadslicer.tekkitrestrict.tekkitrestrict;
 
 public class HackDupeConfig extends TRConfig {
 	public static ArrayList<String> defaultContents(boolean extra){
 		ArrayList<String> tbr = new ArrayList<String>();
 		
 		tbr.add("##############################################################################################");
 		tbr.add("## Configuration file for TekkitRestrict                                                    ##");
 		tbr.add("## Authors: Taeir, DreadEnd (aka DreadSlicer)                                               ##");
 		tbr.add("## BukkitDev: http://dev.bukkit.org/server-mods/tekkit-restrict/                            ##");
 		tbr.add("## Please ask questions/report issues on the BukkitDev page.                                ##");
 		tbr.add("##############################################################################################");
 		tbr.add("");
 		tbr.add("##############################################################################################");
 		tbr.add("################################ Anti-Hack Configuration #####################################");
 		tbr.add("##############################################################################################");
 		tbr.add("# Block hackers from screwing your server up!");
 		tbr.add("# BroadcastString:  The formatting of the BroadcastString.");
 		tbr.add("#                   {PLAYER} will be replaced by the playername.");
 		tbr.add("#                   {TYPE} will be replaced with the hacktype.");
 		tbr.add("#                   Default: \"{PLAYER} tried to {TYPE}-hack!\"");
 		tbr.add("#");
 		tbr.add("# Enabled:          Do you want to enable Anti-Hack for this kind of hack?");
 		tbr.add("#                   Default: All true");
 		tbr.add("#");
 		tbr.add("# Tolerance:        The amount of ticks the player has to hack before he is kicked.");
 		tbr.add("#                   If you set this too low, innocent people might get kicked for connection");
 		tbr.add("#                   problems.");
 		tbr.add("#                   Default:");
 		tbr.add("#                       MoveSpeed: 30");
 		tbr.add("#                       Fly: 40");
 		tbr.add("#                       Forcefield: 20");
 		tbr.add("#");
 		tbr.add("# MaxMoveSpeed:     The maximum speed a player can have (in blocks per second).");
 		tbr.add("#                   Speeds above this are considered hacking.");
 		tbr.add("#                   People with quantum armor will have 3 times this limit.");
 		tbr.add("#                   Default: 2.5");
 		tbr.add("#");
 		tbr.add("# MinHeight:        Minimal Height for the flycheck to kick in.");
 		tbr.add("#                   If you set this too low, people might get kicked for jumping.");
 		tbr.add("#                   Default: 3");
 		tbr.add("#");
 		tbr.add("# Angle:            The maximum angle you are allowed to hit a player with.");
 		tbr.add("#                   Default: 40");
 		tbr.add("#");
 		tbr.add("# Broadcast:        Should a message be broadcast to all players with the ");
 		tbr.add("#                   tekkitrestrict.notify.hack permission");
 		tbr.add("#                   Default: All true");
 		tbr.add("#");
 		tbr.add("# Kick:             Should a player get kicked if he hacks?");
 		tbr.add("# ExecuteCommand:");
 		tbr.add("#    Enable:        Should a command be executed when someone hacks for a certain amount");
 		tbr.add("#                   of times?");
 		tbr.add("#                   Default: All true");
 		tbr.add("#");
 		tbr.add("#    Command:       The command to execute.");
 		tbr.add("#                   Default: \"\"");
 		tbr.add("#");
 		tbr.add("#    TriggerAfter:  Set the amount of times the player has to hack before the command is");
 		tbr.add("#                   executed. (Might implement save feature later. Currently only on");
 		tbr.add("#                   the current server session.)");
 		tbr.add("#                   Default: All 1");
 		tbr.add("Anti-Hacks:");
 		tbr.add("    BroadcastString: \"{PLAYER} tried to {TYPE}-hack!\"");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.BroadcastString HackBroadcastString");
 		tbr.add("    MoveSpeed:");
 		tbr.add("        Enabled: true");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.MoveSpeed.Enabled HackSpeedEnabled");
 		tbr.add("        Tolerance: 30");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.MoveSpeed.Tolerance HackMoveSpeedTolerance");
 		tbr.add("        MaxMoveSpeed: 2.5");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.MoveSpeed.MaxMoveSpeed HackMoveSpeedMax");
 		tbr.add("        Broadcast: true");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.MoveSpeed.Broadcast");
 		tbr.add("        Kick: true");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.MoveSpeed.Kick");
 		tbr.add("        ExecuteCommand:");
 		tbr.add("            Enabled: false");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.MoveSpeed.ExecuteCommand.Enabled");
 		tbr.add("            Command: \"\"");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.MoveSpeed.ExecuteCommand.Command");
 		tbr.add("            TriggerAfter: 1");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.MoveSpeed.ExecuteCommand.TriggerAfter");
 		tbr.add("    Fly:");
 		tbr.add("        Enabled: true");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Fly.Enabled HackFlyEnabled");
 		tbr.add("        Tolerance: 40");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Fly.Tolerance");
 		tbr.add("        MinHeight: 3");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Fly.MinHeight");
 		tbr.add("        Broadcast: true");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Fly.Broadcast");
 		tbr.add("        Kick: true");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Fly.Kick");
 		tbr.add("        ExecuteCommand:");
 		tbr.add("            Enabled: false");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Fly.ExecuteCommand.Enabled");
 		tbr.add("            Command: \"\"");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Fly.ExecuteCommand.Command");
 		tbr.add("            TriggerAfter: 1");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Fly.ExecuteCommand.TriggerAfter");
 		tbr.add("    Forcefield:");
 		tbr.add("        Enabled: true");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Forcefield.Enabled HackForcefieldEnabled");
 		tbr.add("        Tolerance: 20");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Forcefield.Tolerance");
 		tbr.add("        Angle: 40");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Forcefield.Angle");
 		tbr.add("        Broadcast: true");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Forcefield.Broadcast");
 		tbr.add("        Kick: true");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Forcefield.Kick");
 		tbr.add("        ExecuteCommand:");
 		tbr.add("            Enabled: false");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Forcefield.ExecuteCommand.Enabled");
 		tbr.add("            Command: \"\"");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Forcefield.ExecuteCommand.Command");
 		tbr.add("            TriggerAfter: 1");
 		if (extra) tbr.add("#:-;-:# Anti-Hacks.Forcefield.ExecuteCommand.TriggerAfter");
 		tbr.add("");
 		tbr.add("##############################################################################################");
 		tbr.add("################################## Anti-Dupe Configuration ###################################");
 		tbr.add("##############################################################################################");
 		tbr.add("# BroadcastString: The formatting of the BroadcastString for dupes.");
 		tbr.add("# Default: \"{PLAYER} tried to dupe using {TYPE}!\"");
 		tbr.add("#");
 		tbr.add("# Broadcast:    If someone tries to dupe and the dupetype is in this list, it is broadcast");
 		tbr.add("#               to players on the server with the tekkitrestrict.notify.dupe permission.");
 		tbr.add("#               Possible: \"rmfurnace\", \"alc\", \"transmute\", \"tankcart\"");
 		tbr.add("#               Default: [\"rmfurnace\", \"alc\", \"tankcart\"]");
 		tbr.add("#");
 		tbr.add("# Kick:         If a player dupes with a dupetype listed here, he will be kicked.");
 		tbr.add("#               NOTE: It is not recommended to kick players on attempting to dupe. In most");
 		tbr.add("#                     cases it was not the players intention to dupe.");
 		tbr.add("#               Possible: [\"rmfurnace\", \"alc\", \"transmute\", \"tankcart\"]");
 		tbr.add("#               Default: []");
 		tbr.add("Anti-Dupes:");
 		tbr.add("    PreventAlchemyBagDupe: true");
 		if (extra) tbr.add("#:-;-:# Anti-Dupes.PreventAlchemyBagDupe PreventAlcDupe");
 		tbr.add("    PreventRMFurnaceDupe: true");
 		if (extra) tbr.add("#:-;-:# Anti-Dupes.PreventRMFurnaceDupe PreventRMFurnaceDupe");
 		tbr.add("    PreventTransmuteDupe: true");
 		if (extra) tbr.add("#:-;-:# Anti-Dupes.PreventTransmuteDupe PreventTransmuteDupe");
 		tbr.add("    PreventTankCartDupe: true");
 		if (extra) tbr.add("#:-;-:# Anti-Dupes.PreventTankCartDupe PreventTankCartDupe");
 		tbr.add("    PreventTankCartGlitch: true");
 		if (extra) tbr.add("#:-;-:# Anti-Dupes.PreventTankCartGlitch PreventTankCartGlitch");
 		tbr.add("    PreventTeleportDupe: true");
 		if (extra) tbr.add("#:-;-:# Anti-Dupes.PreventTeleportDupe Dupes.PreventTeleportDupe");
 		tbr.add("    PreventPedestalEmcGen: true");
 		if (extra) tbr.add("#:-;-:# Anti-Dupes.PreventPedestalEmcGen PreventPedestalEmcGen");
 		tbr.add("    BroadcastString: \"{PLAYER} tried to dupe using {TYPE}!\"");
 		if (extra) tbr.add("#:-;-:# Anti-Dupes.BroadcastString Dupes.BroadcastString");
 		tbr.add("    Broadcast: [\"rmfurnace\", \"alc\", \"tankcart\"]");
 		if (extra) tbr.add("#:-;-:# Anti-Dupes.Broadcast BroadcastDupes");
 		tbr.add("    Kick: []");
 		if (extra) tbr.add("#:-;-:# Anti-Dupes.Kick Dupes.Kick");
 		tbr.add("");
 		tbr.add("##############################################################################################");
 		
 		return tbr;
 	}
 	
 	public static void upgradeOldHackFile(){
 		upgradeOldHackFile(convertDefaults2(defaultContents(true), true));
 	}
 	
 	public static void upgradeFile(){
 		upgradeFile("HackDupe", convertDefaults2(defaultContents(true), false));
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static ArrayList<String> convertDefaults2(ArrayList<String> defaults, boolean fromold){
 		int j = defaults.size();
 		for (int i = 0;i<j;i++){
 			String str = defaults.get(i);
 			if (str.contains("#:-;-:#")){
 				str = str.replace("#:-;-:# ", "");
 				String oldname = null;
 				if (str.contains(" ")){
 					oldname = str.split(" ")[1];
 					str = str.split(" ")[0];
 				}
 				Object obj = tekkitrestrict.config.get(str, null);
				if (fromold && obj == null){
 					obj = tekkitrestrict.config.get(oldname, null);
 				}
 				if (obj == null){
 					defaults.remove(i);
 					i--; j--;
 					continue;
 				}
 				
 				if (obj instanceof String){
 					String str2 = defaults.get(i-1);//Method: "1"
 					defaults.set(i-1, str2.split(":")[0] + ": \""+obj.toString()+"\"");
 					defaults.remove(i);//Remove posString
 					i--; j--;
 				} else if (obj instanceof Integer){
 					String str2 = defaults.get(i-1);//Method: "1"
 					defaults.set(i-1, str2.split(":")[0] + ": "+toInt(obj));
 					defaults.remove(i);//Remove posString
 					i--; j--;
 				} else if (obj instanceof Double){
 					String str2 = defaults.get(i-1);//Method: "1"
 					defaults.set(i-1, str2.split(":")[0] + ": "+toDouble(obj));
 					defaults.remove(i);//Remove posString
 					i--; j--;
 				} else if (obj instanceof Boolean){
 					String str2 = defaults.get(i-1);//Method: "1"
 					defaults.set(i-1, str2.split(":")[0] + ": "+((Boolean) obj).toString());
 					defaults.remove(i);//Remove posString
 					i--; j--;
 				} else if (obj instanceof List){
 					List<Object> l = (List<Object>) obj;
 					
 					String str2 = defaults.get(i-1);//Method: "1"
 					String toadd = "";
 					for (Object o : l){
 						if (isPrimitive(o) || o instanceof String){
 							toadd += "\""+o.toString()+"\", ";
 						} else {
 							tekkitrestrict.log.severe("Error in Upgrader: invalid config entry, not Primitive or String");
 							continue;
 						}
 					}
 					if (!toadd.equals("")){
 						defaults.set(i-1, str2.split(":")[0] + ": [" + toadd.substring(0, toadd.length()-2) + "]");
 					} else {
 						defaults.set(i-1, str2.split(":")[0] + ": []");
 					}
 					
 					defaults.remove(i);//Remove posString, cursor is at first element of list
 					
 					i--; j--;
 				} else {
 					tekkitrestrict.log.severe("Error in Upgrader: invalid config entry, obj is unknown object! Class: " + obj.getClass().getName());
 					defaults.remove(i);
 					i--; j--;
 					continue;
 				}
 			}
 		}
 		return defaults;
 	}
 	
 	private static void upgradeOldHackFile(ArrayList<String> content){
 		tekkitrestrict.log.info("Upgrading Hack.config.yml file.");
 		File configFile = new File("plugins"+s+"tekkitrestrict"+s+"Hack.config.yml");
 		if (configFile.exists()){
 			File backupfile = new File("plugins"+s+"tekkitrestrict"+s+"Hack.config_backup.yml");
 			if (backupfile.exists()) backupfile.delete();
 			configFile.renameTo(backupfile);
 			configFile = new File("plugins"+s+"tekkitrestrict"+s+"HackDupe.config.yml");
 			try {
 				configFile.createNewFile();
 			} catch (IOException e) {
 				Log.Warning.load("Unable to create file HackDupe.config.yml!");
 				return;
 			}
 		}
 		
 		BufferedWriter output = null;
 		try {
 			output = new BufferedWriter(new FileWriter(configFile));
 			for (int i = 0;i<content.size();i++){
 				if (i != 0) output.newLine();
 				output.append(content.get(i));
 			}
 			output.close();
 		} catch (IOException e) {
 			tekkitrestrict.loadWarning("Unable to write changes to HackDupe.config.yml!");
 			try {if (output != null) output.close();} catch (IOException e1) {}
 			return;
 		}
 		tekkitrestrict.log.info("HackDupe.config.yml file was upgraded successfully!");
 		Log.Warning.loadWarnings.add("HackDupe.config.yml file was upgraded! Please check the new/changed config settings!");
 	}
 }
