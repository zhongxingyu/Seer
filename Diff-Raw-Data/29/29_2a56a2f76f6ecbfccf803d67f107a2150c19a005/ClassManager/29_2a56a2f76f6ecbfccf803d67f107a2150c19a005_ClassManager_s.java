 package com.theminequest.MQCoreRPG.Class;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.logging.Level;
 
 import javax.swing.filechooser.FileFilter;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 
 import com.theminequest.MQCoreRPG.MQCoreRPG;
 import com.theminequest.MQCoreRPG.Player.PlayerDetails;
 import com.theminequest.MineQuest.MineQuest;
 
 public class ClassManager implements Listener {
 	
 	public static final String loc = MineQuest.activePlugin.getDataFolder()+File.separator+"classes";
 	private LinkedHashMap<String,ClassDetails> classes;
 
 	public ClassManager(){
 		MineQuest.log("[Class] Starting Manager...");
 		classes = new LinkedHashMap<String,ClassDetails>();
 		initialize();
 		classes.put("default", new ClassDetails());
 	}
 	
 	public boolean hasClassDetail(String name){
 		return classes.containsKey(name);
 	}
 	
 	public ClassDetails getClassDetail(String name){
 		return classes.get(name);
 	}
 	
 	private void initialize(){
 		File f = new File(loc);
 		if (!f.exists())
 			f.mkdirs();
 		File[] cfiles = f.listFiles(new FilenameFilter(){
 
 			@Override
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".cspec");
 			}
 			
 		});
 		for (File c : cfiles){
 			try {
 				ClassDetails d = new ClassDetails(c);
 				classes.put(d.getName(), d);
 			} catch (IOException e) {
 				MineQuest.log(Level.SEVERE, "[Class] Class Specification " + c.getName() + " is invalid.");
 			}
 		}
 	}
 	
 	// FIXME
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent e){
 		double amt = Math.random();
 		if (amt>0.25)
 			amt = 0.25;
 		PlayerDetails d = MQCoreRPG.playerManager.getPlayerDetails(e.getEntity());
 		int rmexp = (int) Math.round(d.getExperience()*amt);
 		if (MQCoreRPG.popupManager!=null)
			MQCoreRPG.popupManager.triggerNotificationPopup(e.getEntity(), e.getDeathMessage() + ". Lost " + rmexp + " exp.");
 		d.modifyExperienceBy(-1*rmexp);
 	}
 	
 	// FIXME
 	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e){
		if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof LivingEntity))
			return;
		if (((LivingEntity)e.getEntity()).getHealth()-e.getDamage()>0)
 			return;
 		double amt = Math.random();
		PlayerDetails d = MQCoreRPG.playerManager.getPlayerDetails((Player) e.getDamager());
 		int addexp = (int) Math.round(50*amt);
		if (MQCoreRPG.popupManager!=null)
			MQCoreRPG.popupManager.triggerNotificationPopup((Player) e.getEntity(), "Defeated " + e.getEntityType().getName() + "! Gained " + addexp + " exp.");
 		d.modifyExperienceBy(addexp);
 	}
 }
