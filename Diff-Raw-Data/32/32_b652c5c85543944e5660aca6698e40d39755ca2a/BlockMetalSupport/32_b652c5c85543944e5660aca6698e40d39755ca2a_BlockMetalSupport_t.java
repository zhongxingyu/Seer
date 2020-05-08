 package constructionCraft.block;
 
 import java.util.List;
 
 import constructionCraft.ConstructionCraft;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockGlass;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 
 public class BlockMetalSupport extends Block {
 	public static Icon[] textures = new Icon[47];
 	public static int[] textureRefByID = { 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19,
 			15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2,
 			27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1,
 			18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4,
 			5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 16, 16, 20, 20, 16, 16, 28,
 			28, 21, 21, 46, 42, 21, 21, 43, 38, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9,
 			30, 12, 9, 9, 30, 12, 16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45,
 			37, 25, 25, 40, 32, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19,
 			15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0,
 			6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1,
 			13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17,
 			22, 26, 17, 17, 22, 26, 7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41,
 			29, 29, 39, 33, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12,
 			7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11 };
 
 	public BlockMetalSupport(int id) {
 		super(id, Material.rock);
 		setHardness(0.5F);
 	}
 
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess,
		int par2, int par3, int par4, int par5) {
		int i1 = par1IBlockAccess.getBlockId(par2, par3, par4);
		boolean flag = false;
		return !flag && i1 == this.blockID ? false : super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5);
	}

 	public void registerIcons(IconRegister iconRegistry) {
 		for (int i = 0; i < 47; i++)
			textures[i] = iconRegistry.registerIcon(ConstructionCraft.modid
					+ ":/support/metalSupport_" + (i + 1));
 	}
 
 	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z,
 			int side) {
 		boolean[] bitMatrix = new boolean[8];
 
 		if (side == 0 || side == 1) {
 			bitMatrix[0] = world.getBlockId(x - 1, y, z - 1) == this.blockID;
 			bitMatrix[1] = world.getBlockId(x, y, z - 1) == this.blockID;
 			bitMatrix[2] = world.getBlockId(x + 1, y, z - 1) == this.blockID;
 			bitMatrix[3] = world.getBlockId(x - 1, y, z) == this.blockID;
 			bitMatrix[4] = world.getBlockId(x + 1, y, z) == this.blockID;
 			bitMatrix[5] = world.getBlockId(x - 1, y, z + 1) == this.blockID;
 			bitMatrix[6] = world.getBlockId(x, y, z + 1) == this.blockID;
 			bitMatrix[7] = world.getBlockId(x + 1, y, z + 1) == this.blockID;
 		}
 		if (side == 2 || side == 3) {
 			bitMatrix[0] = world.getBlockId(x + (side == 2 ? 1 : -1), y + 1, z) == this.blockID;
 			bitMatrix[1] = world.getBlockId(x, y + 1, z) == this.blockID;
 			bitMatrix[2] = world.getBlockId(x + (side == 3 ? 1 : -1), y + 1, z) == this.blockID;
 			bitMatrix[3] = world.getBlockId(x + (side == 2 ? 1 : -1), y, z) == this.blockID;
 			bitMatrix[4] = world.getBlockId(x + (side == 3 ? 1 : -1), y, z) == this.blockID;
 			bitMatrix[5] = world.getBlockId(x + (side == 2 ? 1 : -1), y - 1, z) == this.blockID;
 			bitMatrix[6] = world.getBlockId(x, y - 1, z) == this.blockID;
 			bitMatrix[7] = world.getBlockId(x + (side == 3 ? 1 : -1), y - 1, z) == this.blockID;
 		}
 		if (side == 4 || side == 5) {
 			bitMatrix[0] = world.getBlockId(x, y + 1, z + (side == 5 ? 1 : -1)) == this.blockID;
 			bitMatrix[1] = world.getBlockId(x, y + 1, z) == this.blockID;
 			bitMatrix[2] = world.getBlockId(x, y + 1, z + (side == 4 ? 1 : -1)) == this.blockID;
 			bitMatrix[3] = world.getBlockId(x, y, z + (side == 5 ? 1 : -1)) == this.blockID;
 			bitMatrix[4] = world.getBlockId(x, y, z + (side == 4 ? 1 : -1)) == this.blockID;
 			bitMatrix[5] = world.getBlockId(x, y - 1, z + (side == 5 ? 1 : -1)) == this.blockID;
 			bitMatrix[6] = world.getBlockId(x, y - 1, z) == this.blockID;
 			bitMatrix[7] = world.getBlockId(x, y - 1, z + (side == 4 ? 1 : -1)) == this.blockID;
 		}
 
 		int idBuilder = 0;
 
 		for (int i = 0; i <= 7; i++) {
			idBuilder = idBuilder
					+ (bitMatrix[i] ? (i == 0 ? 1 : (i == 1 ? 2 : (i == 2 ? 4
							: (i == 3 ? 8 : (i == 4 ? 16 : (i == 5 ? 32
									: (i == 6 ? 64 : 128))))))) : 0);
 		}

		return idBuilder > 255 || idBuilder < 0 ? textures[0]
				: textures[textureRefByID[idBuilder]];
 	}
 
 	public Icon getIcon(int side, int meta) {
 		return textures[0];
 	}
 }
