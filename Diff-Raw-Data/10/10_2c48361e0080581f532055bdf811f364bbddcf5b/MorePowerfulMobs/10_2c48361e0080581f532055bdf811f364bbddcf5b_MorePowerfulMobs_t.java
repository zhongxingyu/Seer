 package com.ainast.morepowerfulmobsreloaded;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MorePowerfulMobs extends JavaPlugin{
 	Logger log;
 	boolean eventsRegistered = false;
 	GenericBossMob gbm;
 	
 	public void onEnable(){
 		log = this.getLogger();
 		MPMTools m = new MPMTools(this);	
 		
 		//MUST REGISTER BOSS MOBS BEFORE REGISTERING OTHER EVENTS!!!
 		
 		World r1World;
 		Location loc;
 		
 		if (this.getServer().getPlayer("crysillion")==null){
 			r1World = getServer().getWorld("World");
 			loc = new Location(r1World, -126, 99, 461);
 		}else{
 			loc = this.getServer().getPlayer("crysillion").getLocation();
 		}
 		
		ItemStack[] drops = { TieredItems.getTier1DentedPlateMail() };
		gbm = new GenericBossMob("Evil Frank", loc, EntityType.ZOMBIE, TieredItems.getTier1DentedPlateMail(), null, drops, 100, 10000);
 		gbm.setMaxHealth(10000);
 		getServer().getScheduler().runTaskTimer(this, gbm, 0, 400);		
 	
 		getServer().getPluginManager().registerEvents(new MobEvents(), this);
 		getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
 	
 		getServer().getScheduler().runTaskTimer(this, new PerTickBuffs(), 0, 20);
 
 	}
 }
