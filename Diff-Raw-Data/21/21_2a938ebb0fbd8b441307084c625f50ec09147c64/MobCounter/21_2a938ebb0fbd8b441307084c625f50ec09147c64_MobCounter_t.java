 package me.xXLupoXx.NoSpawn;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Giant;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 
 public class MobCounter {
 
 	Server server;
 	ConfigBuffer cb;
 	List<LivingEntity> le = new LinkedList<LivingEntity>();
 	List<Entity> el = new LinkedList<Entity>();
 	int Timer;
 
 	
 	
 	public MobCounter(final Server server, final ConfigBuffer cb){
 		
 		
 		this.server = server;
 		this.cb = cb;
 		Timer = cb.CountTimer;
 
 
 		runSchedueler();
 	}
 	
 	
 	public void runSchedueler(){
 		
		server.getScheduler().scheduleSyncRepeatingTask(cb.plugin, new Runnable() {
 			public void run(){
 
 					  for(World w : server.getWorlds())
 					  {
 							int tmpCountPig = 0;
 							int tmpCountCow = 0;
 							int tmpCountSheep = 0;
 							int tmpCountChicken = 0;
 							int tmpCountWolf = 0;
 							int tmpCountZombie = 0;
 							int tmpCountSkeleton = 0;
 							int tmpCountPigZombie = 0;
 							int tmpCountSpider = 0;
 							int tmpCountGiant = 0;
 							int tmpCountCreeper = 0;
 							int tmpCountGhast = 0;
 							int tmpCountSlime = 0;
 							int tmpCountSquid = 0;
 							int tmpTotalMobCounter = 0;
 						  
 						  le = w.getLivingEntities();
 						  
 						  for(LivingEntity e : le){
 							  if (e instanceof Pig){
 								  
 								  tmpCountPig ++; 
 								  
 								  
 							  } else if (e instanceof Cow){
 								  
 								  tmpCountCow ++;
 								  						  
 							  } else if (e instanceof Chicken){
 								  
 								  tmpCountChicken ++;
 								  
 							  } else if (e instanceof Wolf){
 								  
 								  tmpCountWolf ++;
 								  
 							  } else if (e instanceof Squid){
 								  
 								  tmpCountSquid ++;
 								  
 							  } else if (e instanceof Creeper){
 								  
 								  tmpCountCreeper ++;
 								  
 							  } else if (e instanceof Spider){
 								  
 								  tmpCountSpider ++;
 								  
 							  } else if (e instanceof Giant){
 								  
 								  tmpCountGiant ++;
 								  
 							  } else if (e instanceof Ghast){
 								  
 								  tmpCountGhast ++;
 								  
 							  } else if (e instanceof Slime){
 								  
 								  tmpCountSlime ++;
 								  
 							  } else if (e instanceof PigZombie){
 								  
 								  tmpCountPigZombie ++;
 								  
 							  } else if (e instanceof Zombie){
 								  
 								  tmpCountZombie ++;
 								  
 							  } else if (e instanceof Skeleton){
 								  
 								  tmpCountSkeleton ++;
 								  
 							  } else if (e instanceof Sheep){
 								  
 								  tmpCountSheep ++;
 								  
 							  } 
 							  
 							  if(!(e instanceof Player)){
 							  tmpTotalMobCounter ++;
 							  }
 						  }
 						  
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.SHEEP, tmpCountSheep);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.COW, tmpCountCow);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.CHICKEN, tmpCountChicken);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.WOLF, tmpCountWolf);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.ZOMBIE, tmpCountZombie);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.PIG_ZOMBIE, tmpCountPigZombie);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.PIG, tmpCountPig);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.GHAST, tmpCountGhast);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.SLIME, tmpCountSlime);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.GIANT, tmpCountGiant);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.CREEPER, tmpCountCreeper);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.SKELETON, tmpCountSkeleton);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.SQUID, tmpCountSquid);
 						  cb.worldSpawns.get(w).CurrentMobCount.put(CreatureType.SPIDER, tmpCountSpider);
 						  cb.worldSpawns.get(w).CurrentTotalMobs =  tmpTotalMobCounter;
 						  
 						  //System.out.println(w.getName() + " "+ tmpTotalMobCounter);
 					  }
 					  //System.out.println("Bukkit Schedueler");
 					  //System.out.println("Mobrefresch 20 Sek bis zum nchsten");
 			
 			}  
 					  
 		}, 0, ((cb.CountTimer/1000)*20));
 		
 	}
 	
 	
 }
