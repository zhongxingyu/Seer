 package com.nuclearw.adminpvptoggle;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class RemoveFromDisallowTask implements Runnable {
 	private AdminPVPToggle plugin;
 
 	public RemoveFromDisallowTask(AdminPVPToggle instance) {
 		this.plugin = instance;
 	}
 
 	@Override
 	public void run() {
		List<PVPPlayer> found = plugin.getDatabase().find(PVPPlayer.class).where()
			.gt("timeExpire", 0L)
			.le("timeExpire", System.currentTimeMillis())
 			.findList();
 		if(found != null) {
 			Iterator<PVPPlayer> i = found.iterator();
 			while(i.hasNext()) {
 				PVPPlayer next = i.next();
 
 				plugin.getLogger().info("PVP disable on " + next.getName() + " expired.");
 			}
 			plugin.getDatabase().delete(found);
 		}
 	}
 }
