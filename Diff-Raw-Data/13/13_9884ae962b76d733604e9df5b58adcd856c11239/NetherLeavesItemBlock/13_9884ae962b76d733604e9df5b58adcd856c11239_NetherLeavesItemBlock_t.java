 package NetherStuffs.Blocks;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.ItemBlock;
 import net.minecraft.src.ItemStack;
 
 public class NetherLeavesItemBlock extends ItemBlock {
 
 	public static String[] blockNames = new String[] { "Hellfire", "Acid", "Death" };
 	public static String[] blockDisplayNames = new String[] { "Hellfire Leaves", "Acid Leaves", "Death Leaves" };
 
 	public NetherLeavesItemBlock(int id, Block block) {
 		super(id);
 		setHasSubtypes(true);
 	}
 
 	public static int getMetadataSize() {
 		return blockNames.length;
 	}
 
 	@Override
 	public String getItemNameIS(ItemStack is) {
 		String name = "";
 		if (is.getItemDamage() < getMetadataSize() && is.getItemDamage() >= 0)
 			name = blockNames[is.getItemDamage()];
 		else
 			name = blockNames[0];
 
 		return getItemName() + "." + name;
 	}
 
 	@Override
 	public int getMetadata(int meta) {
		return meta | 4; // sets user placed bit to prevent decay
 	}
 }
