 package ca.wacos;
 
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.SimpleCommandMap;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.SimplePluginManager;
 
 public class Updater {
 	public static PluginVersion getVersion() {
 		String ver = NametagEdit.plugin.getDescription().getVersion();
 		int build = extractSnapshotFile();
 		if (build == -1) {
 			return new PluginVersion(ver);
 		}
 		else {
 			return new PluginVersion(ver, build);
 		}
 	}
 	private static int extractSnapshotFile() {
 		String pluginPath = "plugins/" + NametagEdit.plugin.getPluginFile().getName();
 		FileInputStream file = null;
 		try {
 			file = new FileInputStream(pluginPath);
 		} catch (FileNotFoundException e) {
 			System.out.println("Could not find \"" + pluginPath + "\".");
 			e.printStackTrace();
 			return -1;
 		}
 		try {
 			ZipInputStream zis = new ZipInputStream(file);
 			ZipEntry entry = zis.getNextEntry();
 			while (entry != null) {
 				if (entry.getName().equals("snapshot.txt") && !entry.isDirectory()) {
 					String buildString;
 					int build;
 					Scanner read = new Scanner(zis);
 					if (read.hasNext())
 						read.next();
 					else {
 						System.out.println("Could not read snapshot.txt from " + pluginPath + ", incorrect formatting!");
 						read.close();
 						break;
 					}
 					if (read.hasNext())
 						buildString = read.next();
 					else {
 						System.out.println("Could not read snapshot.txt from " + pluginPath + ", incorrect formatting!");
 						read.close();
 						break;
 					}
 					try {
 						build = Integer.parseInt(buildString);
 					}
 					catch (Exception e) {
 						System.out.println("Could not read snapshot.txt from " + pluginPath + ", could not parse version number: " + buildString);
 						read.close();
 						break;
 					}
 					read.close();
 
 					zis.close();
 					
 					return build;
 				}
 				entry = zis.getNextEntry();
 			}
 			zis.close();
 		}
 		catch (Exception e) {
 			System.out.println("Encountered an error while extracting snapshot file from .jar: ");
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	static boolean checkForUpdates(boolean dev, CommandSender player) {
 		try {
 			String file;
 			String buildString;
 			int build = -1;
 			String base = "http://wacos.ca/plugins/NametagEdit/";
 			if (dev) {
 				file = "snapshot.txt";
 			}
 			else {
 				file = "version.txt";
 			}
 			URL site = new URL(base + file);
 			Scanner read = new Scanner(site.openStream());
 			if (read.hasNext())
 				read.next();
 			else {
 				player.sendMessage("");
 				player.sendMessage("§4Failed to check for updates!");
 				player.sendMessage("§cCheck the console for more information.");
 				System.out.println("Could not read " + file + " from " + base + ", incorrect formatting!");
 				read.close();
 				return false;
 			}
 			if (read.hasNext())
 				buildString = read.next();
 			else {
 				player.sendMessage("");
 				player.sendMessage("§4Failed to check for updates!");
 				player.sendMessage("§cCheck the console for more information.");
 				System.out.println("Could not read " + file + " from " + base + ", incorrect formatting!");
 				read.close();
 				return false;
 			}
 			if (dev) {
 				try {
 					build = Integer.parseInt(buildString);
 				}
 				catch (Exception e) {
 					player.sendMessage("");
 					player.sendMessage("§4Failed to check for updates!");
 					player.sendMessage("§cCheck the console for more information.");
 					System.out.println("Could not read " + file + " from " + base + ", could not parse version number: " + buildString);
 					read.close();
 					return false;
 				}
 			}
 			read.close();
 			PluginVersion v = getVersion();
 			if (dev) {
 				if (v.isSnapshot()) {
 					if (v.getBuild() >= build) {
 						player.sendMessage("");
 						player.sendMessage("§eNo development build updates are availible.");
 						return false;
 					}
 					else {
 						player.sendMessage("");
 						player.sendMessage("§aA new snapshot is availible: §fSNAPSHOT " + build);
 						player.sendMessage("§eYou are running version: §f" + v.getVersion() + " SNAPSHOT " + v.getBuild());
 						player.sendMessage("§aWould you like to download and install it now?");
 						player.sendMessage("§aType §e/ne confirm§a to confirm.");
 						return true;
 					}
 				}
 				else {
 					player.sendMessage("");
 					player.sendMessage("§aA snapshot is availible: §fSNAPSHOT " + build);
 					player.sendMessage("§eYou are running version §f" + v.getVersion() + " (stable)");
 					player.sendMessage("§aWould you like to download and install it now?");
 					player.sendMessage("§aType §e/ne confirm§a to confirm.");
 					return true;
 				}
 			}
 			else {
 				if (NametagUtils.compareVersion(v.getVersion(), buildString)) {
 					player.sendMessage("");
 					player.sendMessage("§aA new update is availible: §fVersion " + buildString);
 					if (v.isSnapshot())
 						player.sendMessage("§eYou are running version: §f" + v.getVersion() + " SNAPSHOT " + v.getBuild());
 					else
 						player.sendMessage("§eYou are running version: §f" + v.getVersion() + " (stable)");
 					player.sendMessage("§aWould you like to download and install it now?");
 					player.sendMessage("§aType §e/ne confirm§a to confirm.");
 					return true;
 				}
 				else if (v.isSnapshot()){
 					player.sendMessage("");
 					player.sendMessage("§aAn update is availible, but it not newer than this version (§f" + buildString + "§a)");
 					player.sendMessage("§eYou are running version: §f" + v.getVersion() + " SNAPSHOT " + v.getBuild());
 					player.sendMessage("§aYou may downgrade from this development snapshot to the latest stable build.");
 					player.sendMessage("§aWould you like to download and install it now?");
 					player.sendMessage("§aType §e/ne confirm§a to confirm.");
 					return true;
 				}
 				else {
 					player.sendMessage("");
 					player.sendMessage("§eNo new updates found, plugin is up to date.");
 					return false;
 				}
 			}
 		} catch (Exception e) {
 			player.sendMessage("");
 			player.sendMessage("§4Failed to check for updates: §c" + e.toString());
 			player.sendMessage("§cCheck the console for more information.");
 			e.printStackTrace();
 		}
 		return false;
 	}
 	static boolean checkForUpdates(CommandSender player) {
 		try {
 			String file = "version.txt";
 			String buildString;
 			String base = "http://wacos.ca/plugins/NametagEdit/";
 			URL site = new URL(base + file);
 			Scanner read = new Scanner(site.openStream());
 			if (read.hasNext())
 				read.next();
 			else {
 				player.sendMessage("");
 				player.sendMessage("§4NametagEdit failed to check for updates!");
 				player.sendMessage("§cCheck the console for more information.");
 				System.out.println("Could not read " + file + " from " + base + ", incorrect formatting!");
 				read.close();
 				return false;
 			}
 			if (read.hasNext())
 				buildString = read.next();
 			else {
 				player.sendMessage("");
 				player.sendMessage("§4NametagEdit failed to check for updates!");
 				player.sendMessage("§cCheck the console for more information.");
 				System.out.println("Could not read " + file + " from " + base + ", incorrect formatting!");
 				read.close();
 				return false;
 			}
 			read.close();
 			if (NametagUtils.compareVersion(NametagEdit.plugin.getDescription().getVersion(), buildString)) {
 				player.sendMessage("");
 				player.sendMessage("§aA new update is availible for NametagEdit: §fVersion " + buildString);
				player.sendMessage("§aType §e/ne update stable.§a to update!");
 				return true;
 			}
 		} catch (Exception e) {
 			player.sendMessage("");
 			player.sendMessage("§4NametagEdit failed to check for updates: §c" + e.toString());
 			player.sendMessage("§cCheck the console for more information.");
 			e.printStackTrace();
 		}
 		return false;
 	}
 	@SuppressWarnings("unchecked")
 	static boolean downloadUpdate(CommandSender player, boolean dev) {
 		boolean success = false;
 		String base = "http://wacos.ca/plugins/NametagEdit/";
 		FileOutputStream fos = null;
 		BufferedInputStream bis = null;
 		String pluginPath = "plugins/" + NametagEdit.plugin.getPluginFile().getName();
 		String file;
 		if (dev) {
 			file = "latest.jar";
 		}
 		else {
 			file = "stable.jar";
 		}
 
 		try {
 			URL site = new URL(base + file);
 			bis = new BufferedInputStream(site.openStream());
 			fos = new FileOutputStream(pluginPath);
 			player.sendMessage("§eDownloading: §7" + base + file);
 			int read;
 			int count = -1;
 			while ((read = bis.read()) != -1) {
 				fos.write((byte) read);
 				count++;
 			}
 			player.sendMessage("§ePlugin downloaded! §7(" + count + " bytes)");
 			player.sendMessage("§eReloading plugin...");
 			
 			SimplePluginManager spm = ((SimplePluginManager) NametagEdit.plugin.getServer().getPluginManager());
 			
 			Field fp = spm.getClass().getDeclaredField("plugins");
 			fp.setAccessible(true);
 			List<Plugin> plugins = (List<Plugin>) fp.get(spm);
 			
 			Field fc = spm.getClass().getDeclaredField("commandMap");
             fc.setAccessible(true);
             SimpleCommandMap commandMap = (SimpleCommandMap) fc.get(spm);
             
             Field fn = spm.getClass().getDeclaredField("lookupNames");
             fn.setAccessible(true);
             Map<String, Plugin> lookupNames = (Map<String, Plugin>) fn.get(spm);
 
             Field fk = commandMap.getClass().getDeclaredField("knownCommands");
             fk.setAccessible(true);
             Map<String, Command> knownCommands = (Map<String, Command>) fk.get(commandMap);
            
             NametagEdit.plugin.getCommand("ne").unregister(commandMap);
             
             knownCommands.remove("ne");
             
             for (String key : lookupNames.keySet().toArray(new String[lookupNames.keySet().size()])) {
             	if (lookupNames.get(key) == NametagEdit.plugin) {
             		lookupNames.remove(key);
             		break;
             	}
             }
             plugins.remove(NametagEdit.plugin);
 			
 			NametagEdit.plugin.getServer().getPluginManager().disablePlugin(NametagEdit.plugin);
 			
 			NametagEdit.plugin.getServer().getPluginManager().enablePlugin(NametagEdit.plugin.getServer().getPluginManager().loadPlugin(new File(pluginPath)));
 			
 			player.sendMessage("§eInstallation complete!");
 			success = true;
 		} catch (Exception e) {
 			player.sendMessage("");
 			player.sendMessage("§4Failed to donwload update: §c" + e.toString());
 			player.sendMessage("§cCheck the console for more information.");
 			e.printStackTrace();
 		}
 		if (fos != null) {
 			try {
 				fos.close();
 			} catch (IOException e) {
 				System.out.println("Couldn't close output stream!");
 				e.printStackTrace();
 			}
 		}
 		if (bis != null) {
 			try {
 				bis.close();
 			} catch (IOException e) {
 				System.out.println("Couldn't close input stream!");
 				e.printStackTrace();
 			}
 		}
 		return success;
 	}
 }
