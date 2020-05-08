 package net.worldoftomorrow.noitem.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map.Entry;
 
 import org.bukkit.plugin.Plugin;
 
 import net.worldoftomorrow.noitem.NoItem;
 import net.worldoftomorrow.noitem.permissions.VaultHook;
 
 public class DebugDump {
 	
 	private static final String seperator = "----------------------------";
 	
 	public static void dump(Plugin instance) {
 		File dump = new File(instance.getDataFolder(), "debug.txt");
 		NoItem.log(seperator);
 		NoItem.log("Beginning debug dump.");
 		if (dump.exists() && dump.delete())
 			NoItem.log("Deleting old debug file.");
 		try {
 			if (!dump.createNewFile()) {
 				NoItem.log().severe("Could not create debug file!");
 			}
			PrintWriter out = new PrintWriter(dump, "UTF-8");
 			out.println("---- NoItem Debug File ----");
 			out.println("Plugin Version: " + instance.getDescription().getVersion());
 			out.println("CraftBukkit Version: " + instance.getServer().getBukkitVersion());
 			out.println("Minecraft Version: " + instance.getServer().getVersion());
 			out.println("Max Players: " + instance.getServer().getMaxPlayers());
 			out.println("Online Players: " + instance.getServer().getOnlinePlayers().length);
 			out.println("Online Mode: " + instance.getServer().getOnlineMode());
 			out.println("Using Vault:" + VaultHook.isLoaded());
 			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - HH:mm:ss zzzz");
 			out.println("Date/Time: " + sdf.format(new Date(System.currentTimeMillis())));
 			out.println("=================================");
 			out.println("--- Installed Plugins ---");
 			Plugin[] plugins = NoItem.getInstance().getServer().getPluginManager().getPlugins();
 			for (Plugin p : plugins) {
 				out.println("- " + p.getName() + " - " + p.getDescription().getVersion());
 			}
 			out.println("=================================");
 			out.println("--- Config Values ---");
 			for(Entry<String, Object> e : instance.getConfig().getValues(true).entrySet()) {
 				out.println(e.getKey() + ": " + e.getValue());
 			}
 			out.println("--- END ---");
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		NoItem.log("Done.");
 		NoItem.log(seperator);
 	}
 }
