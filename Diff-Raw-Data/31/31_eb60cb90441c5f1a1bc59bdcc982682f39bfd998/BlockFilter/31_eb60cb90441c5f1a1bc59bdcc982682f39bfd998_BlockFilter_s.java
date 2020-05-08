 package tk.blackwolf12333.grieflog.utils.filters;
 
 import java.util.ArrayList;
 
 import org.bukkit.Material;
 
import tk.blackwolf12333.grieflog.GriefLog;
 import tk.blackwolf12333.grieflog.PlayerSession;
 import tk.blackwolf12333.grieflog.data.BaseData;
 import tk.blackwolf12333.grieflog.data.block.BaseBlockData;
 import tk.blackwolf12333.grieflog.utils.logging.Events;
 
 public class BlockFilter implements Filter {
 
 	PlayerSession player;
 	ArrayList<Material> allowed;
 	
 	public BlockFilter(PlayerSession player, ArrayList<Material> allowed) {
 		this.player = player;
 		this.allowed = allowed;
 	}
 	
 	public BlockFilter(PlayerSession player, String allowed) {
 		this.player = player;
 		this.allowed = getAllowed(allowed);
 	}
 	
 	private ArrayList<Material> getAllowed(String allowed) {
 		ArrayList<Material> allow = new ArrayList<Material>();
 		
 		if(allowed.contains(",")) {
 			for(String s : allowed.split(",")) {
 				Material m = null;
 				try {
 					m = Material.getMaterial(Integer.parseInt(s));
 				} catch(NumberFormatException e) {
 					m = Material.matchMaterial(s);
 				}
 				if(m != null) {
 					allow.add(m);
 				}
 			}
 		} else {
 			allow.add(Material.matchMaterial(allowed));
 		}
 		
 		return allow;
 	}
 
 	public boolean doFilter(BaseData data) {
 		if(Events.getEvent(data.getEvent()).getCanRollback()) {
			BaseBlockData d = (BaseBlockData) data;
			GriefLog.debug(d.getBlockType());
			return allowed.contains(Material.getMaterial(d.getBlockType()));
 		} else {
 			return false;
 		}
 	}
 
 }
