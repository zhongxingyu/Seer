 package net.insomniacraft.codeex.InsomniaDOTA;
 
 import java.util.ArrayList;
 
 import org.bukkit.block.Block;
 
 public class IDBlockSelector {
 	
 	private static ArrayList<Block> selected = new ArrayList<Block>();
 	
 	public static void addBlock(Block b) {
 		if (selected.contains(b)) {
 			return;
 		}
 		selected.add(b);
 	}
 	public static int getSize() {
 		return selected.size();
 	}
 	
 	public static ArrayList<Block> getSelected() {
 		String xyz = "";
 		for (Block b: selected) {
 			int x = b.getX();
 			int y = b.getY();
 			int z = b.getZ();
 			xyz = xyz + x + ":" + y + ":" + z + " ";
 		}
 		System.out.println("[DEBUG] Passing "+xyz);
 		return selected;
 	}
 	
 	public static Block[] getArraySelected() {
 		Block[] blocks = new Block[selected.size()];
 		for (int i = 0; i < selected.size(); i++) {
 			blocks[i] = selected.get(i);
 		}
 		return blocks;
 	}
 	
 	public static void clearBlocks() {
 		selected.clear();
 	}
 
 }
