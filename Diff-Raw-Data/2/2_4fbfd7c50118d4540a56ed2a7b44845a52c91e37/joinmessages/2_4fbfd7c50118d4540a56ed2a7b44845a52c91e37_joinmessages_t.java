 package io.winneonsword.joinmessages;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.Plugin;
 
 public final class joinmessages extends JavaPlugin {
 	
 	joinmessages plugin;
 	
 	public joinmessages(){
 		
 	}
 	
 	public final jm_listeners Listener = new jm_listeners(this);
 	public final vanish_listener vanishListener = new vanish_listener(this);
 	
 	private final class updateCheck implements Runnable{
 		private joinmessages plugin;
 		
 		public updateCheck(joinmessages plugin){
 			this.plugin = plugin;
 		}
 		
 		
 		
 		@Override
 		public void run(){
 			
 			String pluginVersion = plugin.getConfig().getString("version");
 			
 			try {
 				// Credit to mbax for this version checker script. :)
 				final URLConnection connection = new URL("http://dl.dropboxusercontent.com/u/62828086/version").openConnection();
 				connection.setConnectTimeout(8000);
 				connection.setReadTimeout(15000);
 				connection.setRequestProperty("User-agent", "Join Messages");
 				final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 				String version = bufferedReader.readLine();
 				if (version != null){
 					if (pluginVersion.contains("-a")){
 						getLogger().info("You are using a ALPHA version!");
 						getLogger().info("There will be bugs, so try to help by reporting them in an issue at:");
 						getLogger().info("https://github.com/WinneonSword/Join-Messages");
 						return;
 					}
 					if (pluginVersion.contains("-b")){
 						getLogger().info("You are using a BETA version!");
 						getLogger().info("This is slightly more stable than an ALPHA version, but still try to report bugs at:");
 						getLogger().info("https://github.com/WinneonSword/Join-Messages");
 						return;
 					}
 					if (!(pluginVersion.equals(version))){
 						getLogger().info("Found a newer version of Join Messages available: " + version);
 						getLogger().info("To download the newest version, go to: http://dev.bukkit.org/bukkit-plugins/join-messages/");
 						return;
 					}
 					return;
 				}
 				bufferedReader.close();
 				connection.getInputStream().close();
 			} catch (final Exception e){
 				
 			}
			getLogger().severe("Could not check if plugin was up to date! Is the file missing, or is the server down?");
 		}
 	}
 	
 	@Override
 	public void onEnable(){
 		
 		getServer().getScheduler().runTaskTimerAsynchronously(this, new updateCheck(this), 40, 432000);
 		
 		Plugin VanishNoPacket = getServer().getPluginManager().getPlugin("VanishNoPacket");
 		
 		if (VanishNoPacket != null){
 			getLogger().info("VanishNoPacket has been found! Hooking with VanishNoPacket...");
 			getServer().getPluginManager().registerEvents(this.vanishListener, this);
 			getLogger().info("Hooked with VanishNoPacket successfully!");
 		}
 		getServer().getPluginManager().registerEvents(this.Listener, this);
 		getCommand("jm").setExecutor(new jm_command(this));
 		getLogger().info("Join Messages has been enabled!");
 		saveDefaultConfig();
 	}
 	
 	@Override
 	public void onDisable(){
 		getLogger().info("Join Messages has been disabled!");
 	}
 }
