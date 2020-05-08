 package shadow.mods.metallurgy.utility;
 
 import java.util.List;
 import java.util.Random;
 
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 
 public class BlockVein extends Block
 {
 	public String texturePath;
 
 	public BlockVein(int i, String s, Material material) {
 		super(i, material.iron);
 		texturePath = s;
 		this.setCreativeTab(CreativeTabs.tabBlock);
 	}
 
 	public BlockVein setHardness(float par1) {
 		return (BlockVein) super.setHardness(par1);
 	}
 
 	public BlockVein setResistance(float par1) {
 		return (BlockVein) super.setResistance(par1);
 	}
 
 	@Override
 	public int idDropped(int metadata, Random random, int par3) {
 		if(metadata == 0)
 			return mod_Phosphorite.phosphorite.shiftedIndex;
 		if(metadata == 1)
 			return mod_Sulfur.sulfur.shiftedIndex;
 		if(metadata == 2)
 			return mod_Saltpeter.saltpeter.shiftedIndex;
 		if(metadata == 3)
 			return mod_Magnesium.magnesium.shiftedIndex;
 		if(metadata == 4)
 			return mod_Bitumen.bitumen.shiftedIndex;
 		if(metadata == 5)
 			return mod_Potash.potash.shiftedIndex;
 		return blockID;
 	}
 
 	@Override
 	protected int damageDropped(int metadata) {
		return metadata;
 	}
 
 	public int quantityDropped(Random random) {
 		return 1 + random.nextInt(4);
 	}
 
 	public int getBlockTextureFromSideAndMetadata(int side, int metadata) {
 		return metadata;
 	}
 
 	public String getTextureFile() {
 		return texturePath;
 	}
 	
 	@Override
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List) {
 		for (int n = 0; n < 5; n++) {
 			par3List.add(new ItemStack(this, 1, n));
 		}
 	}
 }
