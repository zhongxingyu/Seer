 package us.twoguys.thedarkness.mechanics.effects;
 
 import java.util.ArrayList;
 
 import net.minecraft.server.Packet4UpdateTime;
 
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 import us.twoguys.thedarkness.TheDarkness;
 import us.twoguys.thedarkness.mechanics.PlayerLevelChangeEvent;
 /**
  * 
  * @author Nick
  *This class is useless unless we can find a way to prevent regular time packets from being received by the player
  */
 /*
  * Params:
  * 		Madatory:
  * 			setting[0]: time in ticks
  * 			setting[1]: transition speed in ticks
  * 		Optional:
  * 			setting[2]: frequency
  */
 public class Time implements Listener{
  
 	
 	TheDarkness plugin;
 	int time1, time2, delay, level, transition;
 	ArrayList<Integer> time1s, time2s;
 	Player player;
 	int c=0;
 	
 	public Time(TheDarkness instance) {
 		plugin = instance;
 		
 	}
 	@EventHandler
 	public void onPlayerLevelChange(PlayerLevelChangeEvent event){
 		
 		plugin.locCheck.cancelTask("setTime");
 		plugin.locCheck.cancelTask("transition");
 		
 		plugin.debug("Activated Time");
 		player = event.getPlayer();
 		level = event.getLevelTo();
 		
 		
 		try{
 			time1s = plugin.config.getEffectsSettings(this.getClass(), event.getLevelFrom());
 			time1 = time1s.get(0);
 		}catch(Exception e){
 			time1 = ((int)player.getWorld().getTime())%24000;
 			c++;
 		}
 		try{
 			time2s = plugin.config.getEffectsSettings(this.getClass(), event.getLevelTo());
 			time2 = time2s.get(0);
 			transition = time2s.get(1);
 			transition();
 			//setTime();
 			plugin.debug("Reached setTime");
 		}catch(Exception e){
 			c=c+2;
 			if(c==3){
 				player.resetPlayerTime();
 				plugin.debug("No time settings.");
 				return;
 			}
 			plugin.debug("began catch 2 in Time");
 			time2 = ((int)player.getWorld().getTime())%24000;
 			plugin.debug("time 1 is "+time1);
 			plugin.debug("time 2 is "+time2);
 			transition = 100;
 			transition();
 			
 		}
 		
 	}
 	
 	public void transition(){
 		
 		plugin.debug("transitioning from "+time1+" to "+time2);
 		
 		final boolean moveForward = time1 < time2 ? true : false;
 		
 		delay = moveForward ? (time2 - time1)/transition : (time1 - time2)/transition;
 		plugin.debug("Delay: "+delay);
 		
 		plugin.locCheck.addTask("transition", plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
 	   
 	    
 			public void run() {
 				
 				if(moveForward){
 					if(time1 < time2){
 						player.setPlayerTime(time1, false);
 						Packet4UpdateTime packet = new Packet4UpdateTime(time1);
 						((CraftPlayer)player).getHandle().netServerHandler.sendPacket(packet);
 						time1 = time1+transition;
 						
 					}else if(time1 >= time2){
 						plugin.debug("Transition forward complete-----------------");
						player.setPlayerTime(time1, hasSettings(level));
 						plugin.locCheck.cancelTask("transition");
 						
 					}
 				}else{
 					if(time1 > time2){
 						player.setPlayerTime(time1, false);
 						Packet4UpdateTime packet = new Packet4UpdateTime(time1);
 						((CraftPlayer)player).getHandle().netServerHandler.sendPacket(packet);
 						time1 = time1 - transition;
 						//plugin.debug("Trans time is "+time1);
 						
 					}else if(time1 <= time2){
 						plugin.debug("Transition backwards complete---------------");
 						if(hasSettings(level)){
 							player.resetPlayerTime();
 						}else{
 							player.setPlayerTime(time1, hasSettings(level));
 							int timeTemp = (int) player.getPlayerTime();
 							plugin.debug("Player time is "+timeTemp%24000);
 						}
 						plugin.locCheck.cancelTask("transition");
 					}
 				}
 			}
 			
 		}, 0L, 1));
 	}
 	
 	public void setTime(){
 		plugin.locCheck.addTask("setTime", plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
 
 			public void run() {
 				if(player.isOnline()==false){
 					plugin.locCheck.cancelTask("setTime");
 				}
 				if(plugin.locCheck.getDarknessLevel(player) != level){
 					plugin.locCheck.cancelTask("setTime");
 					return;
 				}
 					player.setPlayerTime(time1, false);
 					plugin.debug("set time");
 			}
 		}, delay, getFrequency()));
 	}
 	
 	private int getFrequency(){
 		try{
 			return time2s.size() < 3 ? plugin.config.getDefaultEffectCheckFreq(level) : time2s.get(2);
 		}catch(Exception e){
 			return plugin.config.getDefaultEffectCheckFreq(level);
 		}
 	}
 	
 	private boolean hasSettings(int level){
 		if(plugin.config.getLevelEffectClasses(level).contains(this.getClass())){
 			return false;
 		}else{
 			return true;
 		}
 	}
 }
