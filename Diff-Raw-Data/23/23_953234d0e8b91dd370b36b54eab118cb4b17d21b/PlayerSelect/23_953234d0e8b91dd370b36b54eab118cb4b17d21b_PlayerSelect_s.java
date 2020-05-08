 package com.bergerkiller.bukkit.rm;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import com.bergerkiller.bukkit.common.BlockLocation;
import com.bergerkiller.bukkit.common.BlockMap;
 import com.bergerkiller.bukkit.common.utils.MaterialUtil;
 
 public class PlayerSelect {
 	private static HashMap<String, PlayerSelect> selections = new HashMap<String, PlayerSelect>();
 
 	public static PlayerSelect get(Player player) {
 		PlayerSelect ps = selections.get(player.getName());
 		if (ps == null) {
 			ps = new PlayerSelect();
 			selections.put(player.getName(), ps);
 		}
 		return ps;
 	}
 
 	private BlockLocation selectedblock;
 	private BlockMap<Integer> delays = new BlockMap<Integer>();
 	private HashMap<String, BlockLocation> portnames = new HashMap<String, BlockLocation>();
 	public int clickdelay = -1;
 
 	/**
 	 * Gets a map of all blocks vs. a delay that is selected
 	 * 
 	 * @return Block delays
 	 */
 	public BlockMap<Integer> getDelays() {
 		return this.delays;
 	}
 
 	/**
 	 * Sets the tick delay for the currently selected block
 	 * 
 	 * @param delay in ticks to set
 	 */
 	public void setDelay(int delay) {
 		this.delays.put(this.selectedblock, delay);
 	}
 
 	/**
 	 * Clears all the block delays set
 	 */
 	public void clearDelays() {
 		this.delays.clear();
 	}
 
 	public Map<String, BlockLocation> getPorts() {
 		return this.portnames;
 	}
 	public void setPort(String name) {
 		this.portnames.put(name, this.selectedblock);
 	}
 	public void clearPorts() {
 		this.portnames.clear();
 	}
 
 	public void set(Location l) {
 		this.set(l.getBlock());
 	}
 	public void set(Block b) {
 		this.selectedblock = new BlockLocation(b);
 	}
 	public boolean setDelay() {
 		if (this.clickdelay >= 0 && this.isDelayable()) {
 			this.setDelay(this.clickdelay);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public Block getBlock() {
 		if (this.selectedblock == null) {
 			return null;
 		}
 		return this.selectedblock.getBlock();
 	}
 	public Material getType() {
 		Block b = getBlock();
 		if (b == null) {
 			return Material.AIR;
 		}
 		return b.getType();
 	}
 	public boolean isDelayable() {
 		Material type = this.getType();
 		return MaterialUtil.ISDIODE.get(type) || MaterialUtil.ISREDSTONETORCH.get(type);
 	}
 }
