 package com.incraftion.j2ther.angrypigzombies;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 
 public class MobListener implements Listener {
 	private Player target;
 	public static Main plugin;
 	public void Moblistener(Main plugin) {
 		MobListener.plugin = plugin;
 	}
		
	public void EntityTargetEvent(final Entity entity, final Player target) {
		this.target = target;
	}
	
	
 	@EventHandler
 	public void onCreatureSpawn(CreatureSpawnEvent event){
 		
 		if(event.isCancelled()){
 			return;
 		}
 		
 		Entity ent = event.getEntity();
 		if (ent == null){
 			return;
 		}
 		
 		if (!(ent instanceof PigZombie)){
 			return;
 		}
 		
		else if (ent instanceof PigZombie){
			PigZombie pz = (PigZombie)event.getEntity();
			pz.setAngry(true);
					
			if (pz.getTarget() != target){
				pz.setTarget(target);
			}
 		}
 		
 	}
 	
 }	
