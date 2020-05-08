 package com.gmail.favorlock.bungeeannouncer.task;
 
 import java.util.ArrayList;
 import java.util.TimerTask;
 import com.gmail.favorlock.bungeeannouncer.BungeeAnnouncer;
 import com.gmail.favorlock.bungeeannouncer.utils.FontFormat;
 
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 
 public class AnnounceTask extends TimerTask {
 	private int counter = 0;
 	private String prefix = "";
 	private ArrayList<String> announcements = new ArrayList<String>();
     private BungeeAnnouncer plugin;
 	
 	public AnnounceTask(BungeeAnnouncer plugin) {
         this.plugin = plugin;
 		this.prefix = plugin.getConfigStorage().settings_prefix;
 		this.announcements = plugin.getConfigStorage().announcements_global;
 	}
 	
 	@Override
 	public void run() {
 		if (announcements.size() > 0) {
 			while (!(announcements.get(counter).equals(")"))) {
				for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    String message = FontFormat.translateString(prefix + announcements.get(counter));

                    if (plugin.getConfigStorage().settings_bouncybungeeon) {
                        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "gsend " + message);
                    } else {
                         player.sendMessage(FontFormat.translateString(prefix +
                                 announcements.get(counter)));
                     }
				}
 				next();
 				if (counter == 0) {
 					break;
 				}
 			}
 			if (announcements.get(counter).equals(")")) {
 				next();
 			}
 		}
 	}
 	
 	public void next() {
 		counter++;
 		
 		if (counter == announcements.size()) {
 			counter = 0;
 		}
 	}
 }
