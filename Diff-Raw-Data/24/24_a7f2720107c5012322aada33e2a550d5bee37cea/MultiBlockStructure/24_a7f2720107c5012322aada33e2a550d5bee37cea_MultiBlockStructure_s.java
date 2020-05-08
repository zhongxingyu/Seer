 package com.untamedears.rourke750.ExpensiveBeacons;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.block.Beacon;
 import org.bukkit.block.Block;
 
 import com.untamedears.rourke750.ExpensiveBeacons.BeaconTypes.SpeedBeacon;
 import com.untamedears.rourke750.ExpensiveBeacons.BeaconTypes.StrengthBeacon;
 
 public class MultiBlockStructure {
 	private BeaconListener ls = null;
 	private ExpensiveBeaconsPlugin pl = null;
 	private StoredValues sv = null;
 	private StrengthBeacon strb;
 	
 	private StaticBeaconMeta meta = null;
 	
 	public MultiBlockStructure(ExpensiveBeaconsPlugin plugin, BeaconListener lis, StoredValues stored,
 			StaticBeaconMeta m) {
 		pl = plugin;
 		ls = lis;
 		meta = m;
 	}
 
 	Logger logger = Logger.getLogger(ExpensiveBeaconsPlugin.class.getName());
 
 	public void checkBuild(Location loc) {
 		int tier = 0;
 		String type=null;
 		String typeName[] = {"speed", "strength"};
 		Block block=loc.getBlock();
 		
			for(int i=1; i<=meta.getMaxSize();i++){			//Remove if statements for loop statement.	~iebagi
 				if(meta.getStruct(i).matches((Beacon) block.getState())==true){
					tier = i%5;
					type = typeName[(i/5)%4];			//Every 4 loops, typeName[n] turns into typeName[1] ~iebagi
 				} 
 			}
 			
 			sv.setTier(loc, tier);
 			sv.setType(loc, type);
 				
 			if (tier==0){
 			if (sv.getType(loc)!=null){
 				sv.removeTier(loc);
 				sv.removeType(loc);
 				}
 			}
 		}
 	}
 
