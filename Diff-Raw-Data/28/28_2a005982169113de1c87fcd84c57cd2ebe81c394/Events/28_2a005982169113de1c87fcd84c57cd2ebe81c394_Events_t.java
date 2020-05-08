 package com.cyprias.invisibilityviewer;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import com.cyprias.invisibilityviewer.VersionChecker.VersionCheckerEvent;
 
 public class Events implements Listener {
 	private InvisibilityViewer plugin;
 
 	public Events(InvisibilityViewer plugin) {
 		this.plugin = plugin;
 	}
 
 	
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onVersionCheckerEvent(VersionCheckerEvent event) {
 		if (event.getPluginName() == plugin.getName()) {
 			VersionChecker.versionInfo info = event.getVersionInfo(0);
			if (info == null)
				return;
			
 			Object[] args = event.getArgs();
 
 			String curVersion = plugin.getDescription().getVersion();
 
 			if (args.length == 0) {
 
 				int compare = plugin.versionChecker.compareVersions(curVersion, info.getTitle());
 				if (compare < 0) {
 					plugin.info("We're running v" + curVersion + ", v" + info.getTitle() + " is available");
 					plugin.info(info.getLink());
 				}
 
 				return;
 			}
 		}
 	}
 }
