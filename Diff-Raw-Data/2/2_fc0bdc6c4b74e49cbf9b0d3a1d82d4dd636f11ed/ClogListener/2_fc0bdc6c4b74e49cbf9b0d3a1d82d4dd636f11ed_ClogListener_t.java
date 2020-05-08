 package org.dyndns.pamelloes.Clog;
 
 import java.lang.reflect.Constructor;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 import org.dyndns.pamelloes.Clog.Clog.Reason;
 import org.dyndns.pamelloes.Clog.permissions.PermissionsHandler;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 import org.getspout.spoutapi.event.spout.SpoutcraftFailedEvent;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class ClogListener implements Listener {
 	private Clog clog;
 	private boolean foundVault;
 
 	public ClogListener(Clog clog) {
 		this.clog=clog;
 	}
 
 	@EventHandler
 	public void onPluginEnable(PluginEnableEvent e) {
 		handleEnable(e.getPlugin());
 	}
 
 	public void handleEnable(Plugin p) {
 		if(foundVault) return;
 		if(p.getDescription().getName().equals("Vault")) {
 			try {
 				Class<? extends Object> clazz = Class.forName("org.dyndns.pamelloes.Clog.permissions.VaultHandler");
 				Constructor<? extends Object> c = clazz.getConstructor(Clog.class, Plugin.class);
 				PermissionsHandler ph = (PermissionsHandler) c.newInstance(clog, p);
 				clog.setHandler(ph);
 			} catch(Exception ex) {
 				//ignore
 			}
 		} else if(p.getDescription().getName().equals("PermissionsBukkit")) {
 			try {
 				Class<? extends Object> clazz = Class.forName("org.dyndns.pamelloes.Clog.permissions.SuperPermsHandler");
 				Constructor<? extends Object> c = clazz.getConstructor(Clog.class, Plugin.class);
 				PermissionsHandler ph = (PermissionsHandler) c.newInstance(clog, p);
 				clog.setHandler(ph);
 			} catch(Exception ex) {
 				//ignore
 			}
 		} else if(p.getDescription().getName().equals("PermissionsEx")) {
 			try {
 				Class<? extends Object> clazz = Class.forName("org.dyndns.pamelloes.Clog.permissions.PEXHandler");
 				Constructor<? extends Object> c = clazz.getConstructor(Clog.class, Plugin.class);
 				PermissionsHandler ph = (PermissionsHandler) c.newInstance(clog, p);
 				clog.setHandler(ph);
 			} catch(Exception ex) {
 				//ignore
 			}
 		} else if(p.getDescription().getName().equals("bPermissions")) {
 			try {
 				Class<? extends Object> clazz = Class.forName("org.dyndns.pamelloes.Clog.permissions.BPermsHandler");
 				Constructor<? extends Object> c = clazz.getConstructor(Clog.class, Plugin.class);
 				PermissionsHandler ph = (PermissionsHandler) c.newInstance(clog, p);
 				clog.setHandler(ph);
 			} catch(Exception ex) {
 				//ignore
 			}
 		}
 
 	}
 
 	@EventHandler
 	public void onPluginDisable(PluginDisableEvent e) {
		if(clog.getHandler()!=null && clog.getHandler().getPlugin().equals(e.getPlugin())) clog.setHandler(null);
 	}
 
 	@EventHandler
 	public void onSpoutCraftEnable(SpoutCraftEnableEvent e) {
 		clog.authenticate(e.getPlayer());
 	}
 
 	@EventHandler
 	public void onSpoutcraftFailed(SpoutcraftFailedEvent e) {
 		SpoutPlayer p = e.getPlayer();
 		if(!clog.hasPermission(p,"clog.ignore.groups")) {
 			clog.saveGroups(p);
 			clog.setGroups(p, Reason.SCFailed, clog.getLowestGroup());
 		}
 		clog.authenticate(e.getPlayer());
 	}
 
 	@EventHandler
 	public void onPlayerKick(PlayerKickEvent e) {
 		clog.restoreGroups(e.getPlayer());
 		clog.authenticate((SpoutPlayer) e.getPlayer());
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent e) {
 		clog.restoreGroups(e.getPlayer());
 		clog.authenticate((SpoutPlayer) e.getPlayer());
 	}
 }
