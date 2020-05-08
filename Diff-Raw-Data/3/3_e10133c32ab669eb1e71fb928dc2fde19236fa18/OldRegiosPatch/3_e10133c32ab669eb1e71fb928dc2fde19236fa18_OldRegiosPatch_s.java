 package couk.Adamki11s.Regios.Data;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.Configuration;
 
 import com.alta189.sqlLibrary.SQLite.sqlCore;
 
 import couk.Adamki11s.Extras.Extras.Extras;
 import couk.Adamki11s.Regios.Main.Regios;
 import couk.Adamki11s.Regios.Mutable.MutableModification;
 import couk.Adamki11s.Regios.Regions.GlobalRegionManager;
 import couk.Adamki11s.Regios.Regions.Region;
 
 public class OldRegiosPatch {
 
 	static sqlCore dbManage;
 	static File maindir = new File("plugins" + File.separator + "Regios");
 	static ArrayList<String> toSend = new ArrayList<String>();
 
 	public static void runPatch(Player p) {
 		boolean filefound = false;
 		for (File f : maindir.listFiles()) {
 			if (f.isFile()) {
 				if (f.getName().contains("region")) {
 					filefound = true;
 				}
 			}
 		}
 		if (!filefound) {
 			p.sendMessage(ChatColor.RED + "[Regios] The old database was not found!");
 			return;
 		}
 		patchMessage("Patching all Region Files...", p);
 		patch(p);
 		patchMessage("Regions Patched successfully", p);
 		patchMessage("Removing Directories...", p);
 		deleteOldFiles(p);
 		patchMessage("Directories Removed", p);
 		patchMessage("Patch Complete!", p);
 		patchMessage("Please Reload!", p);
 		for(String s : toSend){
 			p.sendMessage(s);
 		}
 	}
 
 	private static void patchMessage(String msg, Player p) {
 		System.out.println("[Regios][Patch] " + msg);
 		p.sendMessage("[Regios][Patch] " + msg);
 	}
 
 	private static void deleteOldFiles(Player p) {
 		patchMessage("Deleting old files...", p);
 		ArrayList<File> old = new ArrayList<File>();
 		for (File sub : maindir.listFiles()) {
 			if (!sub.getName().equalsIgnoreCase("Database") && !sub.getName().equalsIgnoreCase("Configuration") && !sub.getName().equalsIgnoreCase("Other")
 					&& !sub.getName().equalsIgnoreCase("Backups")) {
 				patchMessage(("Deleting file : " + sub.getName()), p);
 				deleteDir(sub);
 			}
 		}
 	}
 
 	public static boolean deleteDir(File dir) {
 		if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean success = deleteDir(new File(dir, children[i]));
 				if (!success) {
 					return false;
 				}
 			}
 		}
 
 		// The directory is now empty so delete it
 		return dir.delete();
 	}
 
 	private static Location parseLocation(String location, World wn) {
		// if(location.contains("\\@") && location != constructLocation(new
		// Location(Regios.server.getWorld(wn), 0, 0, 0, 0, 0))){

 		String[] parts = location.split("\\@");
 		if (parts.length == 5) {
 			double x = Double.parseDouble(parts[1]);
 			double y = Double.parseDouble(parts[2]);
 			double z = Double.parseDouble(parts[3]);
 			float yaw = Float.parseFloat(parts[4]);
 			float pitch = Float.parseFloat(parts[5]);
 			return new Location(wn, x, y, z, yaw, pitch);
 		} else {
 			return null;
 		}
 	}
 
 	public final static char[] invalidModifiers = { '!', '\'', '', '$', '%', '^', '&', '*', '', '`', '/', '?', '<', '>', '|', '\\' };
 
 	private static void patch(Player p) {
 		try {
 			dbManage = new sqlCore(Logger.getLogger("Minecraft.Regios"), "[Regios]", "regions", maindir.getAbsolutePath());
 			dbManage.initialize();
 			String query = "SELECT COUNT(*) as count FROM regions;";
 			ResultSet result = dbManage.sqlQuery(query);
 
 			String regQuery = "SELECT * FROM regions;";
 			ResultSet regres = dbManage.sqlQuery(regQuery);
 
 			while (regres.next()) {
 				boolean integrity = true;
 				double x1, x2, y1, y2, z1, z2;
 				Location l1, l2;
 				String world;
 
 				x1 = regres.getDouble("x1");
 				x2 = regres.getDouble("x2");
 				y1 = regres.getDouble("y1");
 				y2 = regres.getDouble("y2");
 				z1 = regres.getDouble("z1");
 				z2 = regres.getDouble("z2");
 
 				world = regres.getString("world");
 
 				World w = Bukkit.getServer().getWorld(world);
 
 				if (w == null) {
 					System.out.println("[Regios] World name did not resolve to a world! Defaulting to : " + Bukkit.getServer().getWorlds().get(0).getName());
 					w = Bukkit.getServer().getWorlds().get(0);
 				}
 
 				l1 = new Location(w, x1, y1, z1);
 				l2 = new Location(w, x2, y2, z2);
 				
 				Location warp = parseLocation(regres.getString("warps"), w);
 
 				String welcomeMessage = regres.getString("welcomemsg");
 				String leaveMessage = regres.getString("leavemsg");
 				String owner = regres.getString("owner");
 
 				int healthRegen = regres.getInt("healthregen");
 
 				String name = regres.getString("regionname").toLowerCase();
 
 				if(warp == null){
 					System.out.println("[Regios] Warp couldn't be patched! Defaulting to null.");
 					warp = new Location(w, 0, 0, 0);
 				}
 				
 				if(l1 == null || l2 == null){
 					System.out.println("[Regios] Error parsing region location. Region will not be patched!");
 					toSend.add(ChatColor.RED + "[Regios] Region " + name + " was not patched. Location couldn't be parsed!");
 					integrity = false;
 				}
 				
 				boolean charInteg = true;
 				for (char ch : name.toCharArray()) {
 					for (char inv : invalidModifiers) {
 						if (ch == inv) {
 							integrity = false;
 							charInteg = false;
 						}
 					}
 				}
 				
 				if(!charInteg){
 					toSend.add(ChatColor.RED + "[Regios] Region " + name + " was not patched. name contained invalid characters!");
 					integrity = false;
 				}
 
 				if (integrity) {
 
 					if (GlobalRegionManager.doesExist(name)) {
 						MutableModification.editDeleteRegion(GlobalRegionManager.getRegion(name), false, p);
 						patchMessage("Deleting existing region name : " + name, p);
 					}
 
 					patchMessage(("Patching Region " + name), p);
 
 					int lsps = regres.getInt("lsps");
 
 					
 
 					boolean showWelcome, showLeave, _protected, preventEntry, preventExit, healthEnabled, pvp;
 
 					if (regres.getByte("showwelcome") == 1) {
 						showWelcome = true;
 					} else {
 						showWelcome = false;
 					}
 					if (regres.getByte("showleave") == 1) {
 						showLeave = true;
 					} else {
 						showLeave = false;
 					}
 					if (regres.getByte("protected") == 1) {
 						_protected = true;
 					} else {
 						_protected = false;
 					}
 					if (regres.getByte("prevententry") == 1) {
 						preventEntry = true;
 					} else {
 						preventEntry = false;
 					}
 					if (regres.getByte("preventexit") == 1) {
 						preventExit = true;
 					} else {
 						preventExit = false;
 					}
 					if (regres.getByte("healthenabled") == 1) {
 						healthEnabled = true;
 					} else {
 						healthEnabled = false;
 					}
 					if (regres.getByte("pvp") == 1) {
 						pvp = true;
 					} else {
 						pvp = false;
 					}
 
 					Region r = new Region(owner, name, l1, l2, w, null, true);
 					r.setWelcomeMessage(replaceString(welcomeMessage));
 					r.setLeaveMessage(replaceString(leaveMessage));
 					r.setHealthRegen(healthRegen);
 					r.setLSPS(lsps);
 					r.setWarp(warp);
 					r.setShowWelcomeMessage(showWelcome);
 					r.setShowLeaveMessage(showLeave);
 					r.set_protection(_protected);
 					r.setPreventEntry(preventEntry);
 					r.setPreventExit(preventExit);
 					r.setHealthEnabled(healthEnabled);
 					r.setPvp(pvp);
 					massConvertRegion(r);
 				} else {
 					System.out.println("[Regios] Region " + name + " was not patched!");
 				}
 			}
 
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 		dbManage.close();
 	}
 
 	private static String replaceString(String message) {
 
 		message = message.replaceAll("%BLACK%", "<BLACK>");
 		message = message.replaceAll("\\&0", "<BLACK>");
 		message = message.replaceAll("\\$0", "<BLACK>");
 
 		message = message.replaceAll("%DBLUE%", "<DBLUE>");
 		message = message.replaceAll("\\&1", "<DBLUE>");
 		message = message.replaceAll("\\$1", "<DBLUE>");
 
 		message = message.replaceAll("%DGREEN%", "<DGREEN>");
 		message = message.replaceAll("\\&2", "<DGREEN>");
 		message = message.replaceAll("\\$2", "<DGREEN>");
 
 		message = message.replaceAll("%DTEAL%", "<DTEAL>");
 		message = message.replaceAll("\\&3", "<DTEAL>");
 		message = message.replaceAll("\\$3", "<DTEAL>");
 
 		message = message.replaceAll("%DRED%", "<DRED>");
 		message = message.replaceAll("\\&4", "<DRED>");
 		message = message.replaceAll("\\$4", "<DRED>");
 
 		message = message.replaceAll("%PURPLE%", "<PURPLE>");
 		message = message.replaceAll("\\&5", "<PURPLE>");
 		message = message.replaceAll("\\$5", "<PURPLE>");
 
 		message = message.replaceAll("%GOLD%", "<GOLD>");
 		message = message.replaceAll("\\&6", "<GOLD>");
 		message = message.replaceAll("\\$6", "<GOLD>");
 
 		message = message.replaceAll("%GREY%", "<GREY>");
 		message = message.replaceAll("\\&7", "<GREY>");
 		message = message.replaceAll("\\$7", "<GREY>");
 
 		message = message.replaceAll("%DGREY%", "<DGREY>");
 		message = message.replaceAll("\\&8", "<DGREY>");
 		message = message.replaceAll("\\$8", "<DGREY>");
 
 		message = message.replaceAll("%BLUE%", "<BLUE>");
 		message = message.replaceAll("\\&9", "<BLUE>");
 		message = message.replaceAll("\\$9", "<BLUE>");
 
 		message = message.replaceAll("%BGREEN%", "<BGREEN>");
 		message = message.replaceAll("\\&A", "<BGREEN>");
 		message = message.replaceAll("\\$A", "<BGREEN>");
 
 		message = message.replaceAll("%TEAL%", "<TEAL>");
 		message = message.replaceAll("\\&B", "<TEAL>");
 		message = message.replaceAll("\\$B", "<TEAL>");
 
 		message = message.replaceAll("%RED%", "<RED>");
 		message = message.replaceAll("\\&C", "<RED>");
 		message = message.replaceAll("\\$C", "<RED>");
 
 		message = message.replaceAll("%PINK%", "<PINK>");
 		message = message.replaceAll("\\&D", "<PINK>");
 		message = message.replaceAll("\\$D", "<PINK>");
 
 		message = message.replaceAll("%YELLOW%", "<YELLOW>");
 		message = message.replaceAll("\\&E", "<YELLOW>");
 		message = message.replaceAll("\\$E", "<YELLOW>");
 
 		message = message.replaceAll("%WHITE%", "<WHITE>");
 		message = message.replaceAll("\\&F", "<WHITE>");
 		message = message.replaceAll("\\$F", "<WHITE>");
 
 		return message;
 	}
 
 	private static void massConvertRegion(Region r) {
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		all.remove("Region.Messages.WelcomeMessage");
 		all.remove("Region.Messages.LeaveMessage");
 		all.remove("Region.Other.HealthRegen");
 		all.remove("Region.Other.LSPS");
 		all.remove("Region.Teleportation.Warp.Location");
 		all.remove("Region.Messages.ShowWelcomeMessage");
 		all.remove("Region.Messages.ShowLeaveMessage");
 		all.remove("Region.General.Protected");
 		all.remove("Region.General.PreventEntry");
 		all.remove("Region.General.PreventExit");
 		all.remove("Region.Other.HealthEnabled");
 		all.remove("Region.Other.PvP");
 		for (Entry<String, Object> entry : all.entrySet()) {
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Messages.WelcomeMessage", r.getWelcomeMessage());
 		c.setProperty("Region.Messages.LeaveMessage", r.getLeaveMessage());
 		c.setProperty("Region.Other.HealthRegen", r.getHealthRegen());
 		c.setProperty("Region.Other.LSPS", r.getLSPS());
 		c.setProperty("Region.Teleportation.Warp.Location", r.getWarp().getWorld().getName() + "," + r.getWarp().getBlockX() + "," + r.getWarp().getBlockY() + ","
 				+ r.getWarp().getBlockZ());
 		c.setProperty("Region.Messages.ShowWelcomeMessage", r.isShowWelcomeMessage());
 		c.setProperty("Region.Messages.ShowLeaveMessage", r.isShowLeaveMessage());
 		c.setProperty("Region.General.Protected", r.is_protection());
 		c.setProperty("Region.General.PreventEntry", r.isPreventEntry());
 		c.setProperty("Region.General.PreventExit", r.isPreventExit());
 		c.setProperty("Region.Other.HealthEnabled", r.isHealthEnabled());
 		c.setProperty("Region.Other.PvP", r.isPvp());
 		c.save();
 	}
 
 }
