 package io.rampant.bukkit.orchard;
 
 import java.util.Map;
 import java.util.Random;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.ConfigurationNode;
 
 /**
  *
  * @author jonathan
  */
 public class Tree {
 
 	public static void pruneLeaf(Block leafBlock, Boolean decay, Player player) {
 		int leafType = (leafBlock.getData() & 3);
 		if( !Orchard.LEAF_MAP.containsKey(leafType) ) {
 			return;
 		}
 
 		boolean wieldingShears = (null != player) && (player.getItemInHand().getType() == Material.SHEARS);
 		Random generator = new Random();
 		double chance = generator.nextDouble() * 100.0;
 		double cumulativeChance = 0.0;
 		String path = Orchard.LEAF_MAP.get(leafType) + (decay ? ".decay" : ".break");
 
 		Map<String, ConfigurationNode> blocks = Orchard.config.getNodes(path);
 		if( null == blocks || blocks.isEmpty() ) {
 			return;
 		}
 
 		for( Map.Entry<String, ConfigurationNode> block : blocks.entrySet() ) {
 			ConfigurationNode node = block.getValue();
 			double thisChance = node.getDouble("chance", 0.0);
 			if( chance > (cumulativeChance + thisChance) ) {
 				cumulativeChance += thisChance;
 				continue;
 			}
 
			if( decay || !node.getBoolean("shears", false) || wieldingShears ) {
 				Material itemType;
 				try {
 					itemType = Material.valueOf(block.getKey());
 					dropItemFromLeaf(leafBlock, itemType, node.getInt("data", -1), node.getInt("amount", 1));
 				}
 				catch( IllegalArgumentException e ) {
 				}
 			}
 			return;
 		}
 	}
 
 	protected static void dropItemFromLeaf(Block block, Material itemType, int metaData, int amount) {
 		block.getWorld().dropItemNaturally(block.getLocation(),
 																			 new ItemStack(itemType, amount, (short) 0, (byte) metaData));
 	}
 }
