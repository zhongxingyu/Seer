 /**
  * This mod is distributed under the terms of the Minecraft Mod Public
  * License 1.0, or MMPL. Please check the contents of the license
  * located in /MMPL-1.0.txt
  */
 
 package extrabiomes.module.summa.block;
 
 import static extrabiomes.module.summa.block.BlockRedRock.BlockType.RED_COBBLE;
 import static extrabiomes.module.summa.block.BlockRedRock.BlockType.RED_ROCK_BRICK;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 import net.minecraft.src.World;
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 import extrabiomes.utility.IDRestrictionAnnotation;
 
 @IDRestrictionAnnotation(maxIDRValue = 255)
 public class BlockRedRock extends Block {
 
 	public enum BlockType {
 		RED_ROCK(0, "Red Rock"),
 		RED_COBBLE(1, "Red Cobblestone"),
 		RED_ROCK_BRICK(2, "Red Rock Brick");
 
 		private final int		value;
 		private final String	itemName;
 
 		BlockType(int value, String itemName) {
 			this.value = value;
 			this.itemName = itemName;
 		}
 
 		public String itemName() {
 			return itemName;
 		}
 
 		public int metadata() {
 			return value;
 		}
 	}
 
 	public BlockRedRock(int id) {
 		super(id, 2, Material.rock);
 		setHardness(1.5F);
 		setResistance(2.0F);
 
 		setTextureFile("/extrabiomes/extrabiomes.png");
 		setCreativeTab(CreativeTabs.tabBlock);
 		setLightOpacity(0);
 	}
 
 	@Override
 	public void addCreativeItems(ArrayList itemList) {
 		for (final BlockType blockType : BlockType.values())
 			itemList.add(new ItemStack(this, 1, blockType.metadata()));
 	}
 
 	@Override
 	protected int damageDropped(int metadata) {
 		return metadata == RED_ROCK_BRICK.metadata() ? RED_ROCK_BRICK
 				.metadata() : RED_COBBLE.metadata();
 	}
 
 	@Override
 	public float getBlockHardness(World world, int x, int y, int z) {
 		final int meta = world.getBlockMetadata(x, y, z);
 		return meta == RED_COBBLE.metadata() ? 2.0F : blockHardness;
 	}
 
 	@Override
 	public int getBlockTextureFromSideAndMetadata(int side, int metadata)
 	{
 		return metadata == RED_ROCK_BRICK.metadata() ? 11
 				: metadata == RED_COBBLE.metadata() ? 12 : super
 						.getBlockTextureFromSideAndMetadata(side,
 								metadata);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int id, CreativeTabs tab, List itemList) {
 		if (tab == CreativeTabs.tabBlock)
 			for (final BlockType blockType : BlockType.values())
 				itemList.add(new ItemStack(this, 1, blockType
 						.metadata()));
 	}
 }
