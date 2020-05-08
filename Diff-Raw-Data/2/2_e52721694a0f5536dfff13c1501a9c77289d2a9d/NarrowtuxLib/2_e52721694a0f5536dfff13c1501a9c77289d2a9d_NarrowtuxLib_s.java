 /*
  * Copyright (C) 2011 Moritz Schmale <narrow.m@gmail.com>
  *
  * NarrowtuxLib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/gpl.html>.
  */
 
 package com.narrowtux.narrowtuxlib;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.InvalidDescriptionException;
 import org.bukkit.plugin.InvalidPluginException;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.UnknownDependencyException;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.FileUtil;
 import org.getspout.spoutapi.SpoutManager;
 
 import com.narrowtux.narrowtuxlib.event.NTScreenListener;
 import com.narrowtux.narrowtuxlib.assistant.Icon;
 import com.narrowtux.narrowtuxlib.event.NTLPlayerListener;
 import com.narrowtux.narrowtuxlib.notification.Notification;
 import com.narrowtux.narrowtuxlib.notification.NotificationManager;
 import com.narrowtux.narrowtuxlib.notification.SimpleNotificationManager;
 import com.narrowtux.narrowtuxlib.utils.NetworkUtils;
 
 import com.nijikokun.register.Register;
 import com.nijikokun.register.payment.Method;
 import com.nijikokun.register.payment.Methods;
 
 public class NarrowtuxLib extends JavaPlugin {
 	private static Logger log = Bukkit.getServer().getLogger();
 	private NTLPlayerListener playerListener = new NTLPlayerListener();
 	private SimpleNotificationManager notificationManager = new SimpleNotificationManager();
 	private Configuration config;
 	private static NarrowtuxLib instance;
 
 	@Override
 	public void onDisable() {
 		//Attempt to auto update if file is available
         try {
             File directory = new File(Bukkit.getServer().getUpdateFolder());
             if (directory.exists()) {
                 File plugin = new File(directory.getPath(), "NarrowtuxLib.jar");
                 if (plugin.exists()) {
                     FileUtil.copy(plugin, this.getFile());
                     plugin.delete();
                 }
             }
         }
         catch (Exception e) {}
         save();
 		sendDescription("disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		instance = this;
 		createDataFolder();
 		config = new Configuration(new File(getDataFolder(), "narrowtuxlib.cfg"));
 		final PluginManager pm = getServer().getPluginManager();
 		load();
 		if(config.isAutoUpdate()){
 			(new Thread() {
 	            public void run() {
 	                update();
 	            }
 	        }).start();
 		}
 		if(isSpoutInstalled()){
 			SpoutManager.getFileManager().addToCache(this, "http://tetragaming.com/narrowtux/pluginres/narrowtuxlib/messageBoxBG.png");
 			for(Icon icon:Icon.values()){
 				SpoutManager.getFileManager().addToCache(this, icon.getUrl());
 			}
 		}
 		checkForRegister();
 		registerEvents();
 		sendDescription("enabled");
 	}
 
 	private void load() {
 		//Load Notifications
 		notificationManager.load();
 	}
 
 	private void save() {
 		//Save Notifications
 		notificationManager.save();
 	}
 
 	private void createDataFolder() {
 		File folder = getDataFolder();
 		if(!folder.exists()){
 			folder.mkdir();
 		}
 	}
 
 	private void registerEvents() {
 		registerEvent(Type.PLAYER_CHAT, playerListener, Priority.Lowest);
 		registerEvent(Type.PLAYER_MOVE, playerListener);
 		registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Highest);
 		if(isSpoutInstalled()){
 			registerEvent(Type.CUSTOM_EVENT, new NTScreenListener());
 		}
 	}
 
 	private void registerEvent(Type type, Listener listener, Priority priority){
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(type, listener, priority, this);
 	}
 
 	private void registerEvent(Type type, Listener listener){
 		registerEvent(type, listener, Priority.Normal);
 	}
 
 	private void sendDescription(String startup){
 		PluginDescriptionFile pdf = getDescription();
 		String authors = "";
 		for(String name: pdf.getAuthors()){
 			if(authors.length()>0){
 				authors+=", ";
 			}
 			authors+=name;
 		}
 		log.log(Level.INFO, "["+pdf.getName()+"] v"+pdf.getVersion()+" by ["+authors+"] "+startup+".");
 	}
 	/**
 	 * @return the logger
 	 */
 	public static Logger getLogger(){
 		return log;
 	}
 
 	protected boolean isUpdateAvailable() {
 		log.info("Version: "+getVersion());
 		try {
 			URL url = new URL("http://tetragaming.com/narrowtux/plugins/NarrowtuxLibVersion.txt");
 			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 			String str;
 			while ((str = in.readLine()) != null) {
 				String[] split = str.split("\\.");
 				int version = Integer.parseInt(split[0]) * 100 + Integer.parseInt(split[1]) * 10 + Integer.parseInt(split[2]);
 				if (version > getVersion()){
 					in.close();
 					return true;
 				}
 			}
 			in.close();
 		}
 		catch (Exception e) {
 		}
 		return false;
 	}
 
 	/**
 	 * @return the version as int
 	 */
 	public int getVersion() {
 		try {
 			String[] split = this.getDescription().getVersion().split("\\.");
 			return Integer.parseInt(split[0]) * 100 + Integer.parseInt(split[1]) * 10 + Integer.parseInt(split[2]);
 		}
 		catch (Exception e) {}
 		return -1;
 	}
 
 	protected void update() {
         if (!isUpdateAvailable()) {
             return;
         }
         try {
             File directory = new File(Bukkit.getServer().getUpdateFolder());
             if (!directory.exists()) {
                 directory.mkdir();
             }
             File plugin = new File(directory.getPath(), "NarrowtuxLib.jar");
             if (!plugin.exists()) {
                 URL bukkitContrib = new URL("http://tetragaming.com/narrowtux/plugins/NarrowtuxLib.jar");
                 HttpURLConnection con = (HttpURLConnection)(bukkitContrib.openConnection());
                 System.setProperty("http.agent", ""); //Spoofing the user agent is required to track stats
                 con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
                 ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
                 FileOutputStream fos = new FileOutputStream(plugin);
                 fos.getChannel().transferFrom(rbc, 0, 1 << 24);
             }
         }
         catch (Exception e) {}
     }
 	/**
 	 *
 	 * @return the notification manager
 	 */
 	public static NotificationManager getNotificationManager(){
 		return instance.notificationManager;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
 		/*
 		 * Commands:
 		 *  nt add Player Title Text
 		 */
 		if(cmd.getName().equals("notification")){
 			if(args.length>=1){
 				if(args[0].equals("add")&&sender.isOp()){
 					if(args.length==4){
 						String player = args[1];
 						String title = args[2];
 						String text = args[3];
 						Notification n = new Notification(player, title, text);
 						n.print();
 						sender.sendMessage("Added notification for "+player);
 						return true;
 					}
 				}
 				if(sender instanceof Player){
 					if(args.length==1){
 						Player p = (Player)sender;
 						int id = 0;
 						try{
 							id = Integer.valueOf(args[0]);
 						} catch(Exception e){
 							id = 0;
 						}
 						Notification n = notificationManager.get(id);
 						if(n == null)
 						{
 							p.sendMessage("This notification does not exist!");
 							return true;
 						}
 						if(n.getReceiver().toLowerCase().equals(p.getName().toLowerCase())){
 							n.print();
 						} else {
 							p.sendMessage("You may not see this notification!");
 						}
 						return true;
 					}
 				}
 			}
 			if(args.length==0){
 				if(sender instanceof Player){
 					Player p = (Player)sender;
 					List<Notification> pending = notificationManager.getPendingNotifications(p);
 					if(pending.size()>0){
 						p.sendMessage("Unread Notifications:");
 						p.sendMessage("---------------------");
 						for(Notification n:pending){
 							p.sendMessage(n.getId()+": "+n.getTitle());
 						}
 						p.sendMessage("Type /nt [number] to view the notification.");
 					} else {
 						p.sendMessage("No new notifications.");
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 * @return the plugin instance
 	 */
 	public static NarrowtuxLib getInstance() {
 		return instance;
 	}
 
 	public static Method getMethod(){
 		return Methods.getMethod();
 	}
 	
 	protected void checkForRegister() {
 		Plugin pl = Bukkit.getPluginManager().getPlugin("Register");
		if (pl == null || !(pl instanceof Register) && config.isInstallRegister()) {
 			try {
 				NetworkUtils.download(getLogger(), new URL("http://ci.getspout.org/view/Economy/job/Register/lastSuccessfulBuild/artifact/register-1.5.jar"), new File("plugins", "Register.jar"));
 				Bukkit.getPluginManager().loadPlugin(new File("plugins", "Register.jar"));
 				Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().getPlugin("Register"));
 			} catch (Exception e) {
 				getLogger().log(Level.WARNING, "Couldn't install register");
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Downloads the spout plugin and installs it.
 	 * @warning this depends on the spout config value in the narrowtux configuration.
 	 * @return if the installation has been successful.
 	 */
 	public boolean installSpout() {
 		if(config.isInstallSpout()) {
 			final PluginManager pm = getServer().getPluginManager();
 		    if (pm.getPlugin("Spout") == null && config.isInstallSpout()){
 		        try {
 		            NetworkUtils.download(log, new URL("http://ci.getspout.org/view/SpoutDev/job/Spout/promotion/latest/Recommended/artifact/target/spout-dev-SNAPSHOT.jar"), new File("plugins/Spout.jar"));
 		            pm.loadPlugin(new File("plugins" + File.separator + "Spout.jar"));
 		            pm.enablePlugin(pm.getPlugin("Spout"));
 		        } catch (final Exception ex) {
 		            log.warning("[NarrowtuxLib] Failed to install Spout, you may have to restart your server or install it manually.");
 		            return false;
 		        }
 		        return true;
 		    }
 		}
 		return false;
 	}
 	
 	public static boolean isSpoutInstalled() {
 		PluginManager pm = instance.getServer().getPluginManager();
 		return pm.getPlugin("Spout")!=null;
 	}
 }
