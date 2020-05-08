 package nu.nerd.SafeBuckets.database;
 
 import org.bukkit.block.Block;
 
 import com.avaje.ebean.Query;
import java.util.List;
 
 import nu.nerd.SafeBuckets.SafeBuckets;
 
 public class SafeLiquidTable {
 
 	SafeBuckets plugin;
 
 	public SafeLiquidTable(SafeBuckets plugin) {
 		this.plugin = plugin;
 	}
 
 	public SafeLiquid getID(int id) {
 		SafeLiquid retVal = null;
 
 		Query<SafeLiquid> query = plugin.getDatabase().find(SafeLiquid.class).where().eq("id", id).query();
 
 		if (query != null) {
 			retVal = query.findUnique();
 		}
 
 		return retVal;
 	}
 
 	public void removeSafeLiquid(Block block){
 		if (!isSafeLiquid(block)){
 			return;
 		}
 		try {
			List<SafeLiquid> dbbl = null;
 			Query<SafeLiquid> query = plugin.getDatabase().find(SafeLiquid.class).where()
 			.eq("world", block.getWorld().getName())
 			.eq("x",block.getX())
 			.eq("y",block.getY())
 			.eq("z",block.getZ())
 			.query();
 
 			if (query != null) {
				dbbl = query.findList();
                                for( SafeLiquid s : dbbl){
                                    plugin.getDatabase().delete(s);
                                }
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public boolean isSafeLiquid(Block block){
 		try {
 			int retVal = 0;
 			Query<SafeLiquid> query = plugin.getDatabase().find(SafeLiquid.class).where()
 			.eq("world", block.getWorld().getName())
 			.eq("x",block.getX())
 			.eq("y",block.getY())
 			.eq("z",block.getZ())
 			.query();
 			if (query != null) {
 				retVal = query.findRowCount();
 			}
 
 			return retVal > 0;
 		} catch(Exception e) {
 			e.printStackTrace();
 			return true; // Just in case we fuck something up
 		}
 	}
 
 	public void save(SafeLiquid deathstat) {
 		plugin.getDatabase().save(deathstat);
 	}
 
 }
