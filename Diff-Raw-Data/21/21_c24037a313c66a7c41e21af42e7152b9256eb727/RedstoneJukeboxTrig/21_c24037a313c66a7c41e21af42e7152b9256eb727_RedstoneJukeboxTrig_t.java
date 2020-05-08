 package me.cheddar262.RedstoneJukeboxTrig;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.block.Jukebox;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class RedstoneJukeboxTrig extends JavaPlugin{
 	Logger log = Logger.getLogger("Minecraft");
 	private final RJTBlockListener blockListener = new RJTBlockListener(this);
 	private final RJTPlayerListener playerListener = new RJTPlayerListener(this);
 	public Map<String, Long> posETime = Collections.synchronizedMap(new HashMap<String, Long> ());
 	static long[] recordTimes = { //(Minutes * 60) + seconds
			((2*60)+58), //13
			((3*60)+5), //cat
			((5*60)+45), //blocks
 			((3*60)+5), //chirp
 			((2*60)+54), //far
 			((3*60)+17), //mall
			((1*60)+36), //mellohi
 			((2*60)+30), //stal
			((3*60)+8), //strad
			((4*60)+11), //ward
			((1*60)+11), //11
			((3*60)+51)}; //wait
 	
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		
 		log.info("RedstoneJukeboxTrig has been enabled!");
 		pm.registerEvents(blockListener, this);
 		pm.registerEvents(playerListener, this);
 	}
 	public void onDisable() {
 		log.info("RedstoneJukeboxTrig has been disabled.");
 		
 	}	
 	
 	//public Map<String, Long> hashIt(String pos, long endTime) {
 		//posETime.put(pos, endTime);
 		//return posETime;
 	//}
 
 	public long findEndTime(long stTime, Material record) {
 		long end = 0;
 		long timeAdd = 0;
 		if(record == Material.GOLD_RECORD){ //13
 			timeAdd = (recordTimes[0]);
 		}
 		else if(record == Material.GREEN_RECORD){ //cat
 			timeAdd = (recordTimes[1]);
 		}
 		else if(record == Material.RECORD_3){ //blocks
 			timeAdd = (recordTimes[2]);
 		}
 		else if(record == Material.RECORD_4){ //chirp
 			timeAdd = (recordTimes[3]);
 		}
 		else if(record == Material.RECORD_5){ //far
 			timeAdd = (recordTimes[4]);
 		}
 		else if(record == Material.RECORD_6){ //mall
 			timeAdd = (recordTimes[5]);
 		}
 		else if(record == Material.RECORD_7){ //mellohi
 			timeAdd = (recordTimes[6]);
 		}
 		else if(record == Material.RECORD_8){ //stal
 			timeAdd = (recordTimes[7]);
 		}
 		else if(record == Material.RECORD_9){ //strad
 			timeAdd = (recordTimes[8]);
 		}
 		else if(record == Material.RECORD_10){ //ward
 			timeAdd = (recordTimes[9]);
 		}
 		else if(record == Material.RECORD_11){ //11
 			timeAdd = (recordTimes[10]);
 		}
		else if(record == Material.RECORD_12){ //wait
 			timeAdd = (recordTimes[11]);
 		}
 		
 		timeAdd = timeAdd*1000;
 		end = stTime + timeAdd;
 		return end;
 	}
 	
 	public Material getPlaying(Jukebox jb) {
 	    try {
 	        jb.getPlaying();
 	        return jb.getPlaying();
 	    } catch (Exception e) {
 	        return Material.AIR;
 	    }
 	}
 }
