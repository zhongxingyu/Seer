 package com.spaceemotion.updater;
 
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 
 import com.spaceemotion.updater.UpdateReader.UpdateMessage;
 
 public class Updater {
 	public final static String URL = "http://dev.catharos.de/update.php?project=%P%&version=%V%";
 
 	private JavaPlugin plugin;
 	private UpdateReader reader;
 
 	private BukkitTask updateTask;
 
 	public Updater( JavaPlugin plugin ) {
 		this.plugin = plugin;
 
 		PluginDescriptionFile descr = this.plugin.getDescription();
 
 		String url = URL;
 		url = url.replace( "%P%", descr.getName() );
 		url = url.replace( "%V%", Integer.toString( getVersion( descr.getVersion() ) ) );
 
 		this.reader = new UpdateReader( url );
 
 		updateTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously( plugin, new Runnable() {
 			public void run() {
 				checkForUpdate();
 			}
 		}, 0, 20 * 60 * plugin.getConfig().getInt( "update-interval", 20 ) );
 	}
 
 	public void checkForUpdate() {
 		try {
 			UpdateMessage msg = reader.read();
 			plugin.getLogger().info( (msg.update ? ChatColor.GOLD : ChatColor.BLUE) + msg.message );
 
 			return;
 		} catch (Exception ex) {
			plugin.getLogger().info( ChatColor.BLUE + "No update found!" );
 		}
 	}
 
 	private int getVersion( String version ) {
 		try {
 			if (version.contains( "-b" )) {
 				return Integer.parseInt( version.substring( version.indexOf( "-b" ) + 1 ) );
 			} else {
 				return Integer.parseInt( version.replaceAll( ".", "" ) );
 			}
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 
 	public BukkitTask getUpdaterTask() {
 		return updateTask;
 	}
 }
