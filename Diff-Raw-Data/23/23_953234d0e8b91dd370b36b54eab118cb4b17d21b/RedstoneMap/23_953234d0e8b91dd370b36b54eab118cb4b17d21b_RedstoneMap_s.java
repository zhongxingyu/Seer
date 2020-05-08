 package com.bergerkiller.bukkit.rm;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 import com.bergerkiller.bukkit.common.BlockLocation;
import com.bergerkiller.bukkit.common.BlockMap;
 import com.bergerkiller.bukkit.rm.element.Redstone;
 
 /**
  * Maps Redstone instances to blocks in (possibly) multiple worlds
  */
 public class RedstoneMap {
 	private BlockMap<RedstoneContainer> blocks = new BlockMap<RedstoneContainer>();
 	private HashMap<Redstone, HashSet<RedstoneContainer>> maps = new HashMap<Redstone, HashSet<RedstoneContainer>>();
 
 	public RedstoneContainer get(Block block) {
 		return get(new BlockLocation(block));
 	}
 	public RedstoneContainer get(World world, int x, int y, int z) {
 		return get(new BlockLocation(world, x, y, z));
 	}
 	public RedstoneContainer get(BlockLocation block) {
 		RedstoneContainer m = blocks.get(block);
 		if (m == null) {
 			m = new RedstoneContainer(this);
 			blocks.put(block, m);
 		}
 		return m;
 	}
 
 	public HashSet<RedstoneContainer> getMaps(Redstone redstone) {
 		HashSet<RedstoneContainer> map = maps.get(redstone);
 		if (map == null) {
 			map = new HashSet<RedstoneContainer>();
 			maps.put(redstone, map);
 		}
 		return map;
 	}
 
 	public void merge(Redstone from, Redstone to) {
 		HashSet<RedstoneContainer> rmaps = getMaps(from);
 		for (RedstoneContainer map : rmaps) {
 			setValue(map, to);
 		}
 		maps.remove(from);
 	}
 
 	protected void setValue(RedstoneContainer map, Redstone value) {
 		getMaps(value).add(map);
 	}
 }
