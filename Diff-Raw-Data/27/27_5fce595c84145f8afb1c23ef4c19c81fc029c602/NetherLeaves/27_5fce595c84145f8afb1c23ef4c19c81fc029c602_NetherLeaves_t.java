 package bstramke.NetherStuffs.Blocks;
 
 import static net.minecraftforge.common.ForgeDirection.UP;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.particle.EntityFX;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.IShearable;
 import bstramke.NetherStuffs.NetherStuffs;
 import bstramke.NetherStuffs.Client.EntityDropParticleFXNetherStuffs;
 import bstramke.NetherStuffs.Common.BlockNotifyType;
 import bstramke.NetherStuffs.Common.CommonProxy;
 import bstramke.NetherStuffs.Common.NetherLeavesMaterial;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class NetherLeaves extends Block implements IShearable {
 	public static final int hellfire = 0;
 	public static final int acid = 1;
 	public static final int death = 2;
 
 	int[] adjacentTreeBlocks;
 	private Icon icoLeafHellfire;
 	private Icon icoLeafAcid;
 	private Icon icoLeafDeath;
 
 	public NetherLeaves(int par1) {
 		super(par1, NetherLeavesMaterial.netherLeaves);
 		//this.setRequiresSelfNotify();
 		this.setStepSound(Block.soundGrassFootstep);
 		this.setTickRandomly(true);
 		this.setCreativeTab(NetherStuffs.tabNetherStuffs);
 		this.setBurnProperties(this.blockID, 0, 0);
 	}
 
 	@Override
 	public boolean isFireSource(World world, int x, int y, int z, int metadata, ForgeDirection side) {
 		if (side == UP) {
 			return true;
 		}
 		return false;
 	}
 
 	/*
 	 * ejects contained items into the world, and notifies neighbours of an update, as appropriate
 	 */
 	@Override
 	public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6) {
 		byte var7 = 1;
 		int var8 = var7 + 1;
 
 		if (par1World.checkChunksExist(par2 - var8, par3 - var8, par4 - var8, par2 + var8, par3 + var8, par4 + var8)) {
 			for (int var9 = -var7; var9 <= var7; ++var9) {
 				for (int var10 = -var7; var10 <= var7; ++var10) {
 					for (int var11 = -var7; var11 <= var7; ++var11) {
 						int var12 = par1World.getBlockId(par2 + var9, par3 + var10, par4 + var11);
 
 						if (Block.blocksList[var12] != null) {
 							Block.blocksList[var12].beginLeavesDecay(par1World, par2 + var9, par3 + var10, par4 + var11);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Ticks the block if it's been scheduled
 	 */
 	@Override
 	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
 		if (!par1World.isRemote) {
 			int var6 = par1World.getBlockMetadata(par2, par3, par4);
 			if (isDecaying(var6) && !isUserPlaced(var6)) {
 				byte var7 = 4;
 				int var8 = var7 + 1;
 				byte var9 = 32;
 				int var10 = var9 * var9;
 				int var11 = var9 / 2;
 
 				if (this.adjacentTreeBlocks == null) {
 					this.adjacentTreeBlocks = new int[var9 * var9 * var9];
 				}
 
 				int var12;
 
 				if (par1World.checkChunksExist(par2 - var8, par3 - var8, par4 - var8, par2 + var8, par3 + var8, par4 + var8)) {
 					int var13;
 					int var14;
 					int var15;
 
 					for (var12 = -var7; var12 <= var7; ++var12) {
 						for (var13 = -var7; var13 <= var7; ++var13) {
 							for (var14 = -var7; var14 <= var7; ++var14) {
 								var15 = par1World.getBlockId(par2 + var12, par3 + var13, par4 + var14);
 
 								Block block = Block.blocksList[var15];
 
 								if (block != null && block.canSustainLeaves(par1World, par2 + var12, par3 + var13, par4 + var14)) {
 									this.adjacentTreeBlocks[(var12 + var11) * var10 + (var13 + var11) * var9 + var14 + var11] = 0;
 								} else if (block != null && block.isLeaves(par1World, par2 + var12, par3 + var13, par4 + var14)) {
 									this.adjacentTreeBlocks[(var12 + var11) * var10 + (var13 + var11) * var9 + var14 + var11] = -2;
 								} else {
 									this.adjacentTreeBlocks[(var12 + var11) * var10 + (var13 + var11) * var9 + var14 + var11] = -1;
 								}
 							}
 						}
 					}
 
 					for (var12 = 1; var12 <= 4; ++var12) {
 						for (var13 = -var7; var13 <= var7; ++var13) {
 							for (var14 = -var7; var14 <= var7; ++var14) {
 								for (var15 = -var7; var15 <= var7; ++var15) {
 									if (this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11) * var9 + var15 + var11] == var12 - 1) {
 										if (this.adjacentTreeBlocks[(var13 + var11 - 1) * var10 + (var14 + var11) * var9 + var15 + var11] == -2) {
 											this.adjacentTreeBlocks[(var13 + var11 - 1) * var10 + (var14 + var11) * var9 + var15 + var11] = var12;
 										}
 
 										if (this.adjacentTreeBlocks[(var13 + var11 + 1) * var10 + (var14 + var11) * var9 + var15 + var11] == -2) {
 											this.adjacentTreeBlocks[(var13 + var11 + 1) * var10 + (var14 + var11) * var9 + var15 + var11] = var12;
 										}
 
 										if (this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11 - 1) * var9 + var15 + var11] == -2) {
 											this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11 - 1) * var9 + var15 + var11] = var12;
 										}
 
 										if (this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11 + 1) * var9 + var15 + var11] == -2) {
 											this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11 + 1) * var9 + var15 + var11] = var12;
 										}
 
 										if (this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11) * var9 + (var15 + var11 - 1)] == -2) {
 											this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11) * var9 + (var15 + var11 - 1)] = var12;
 										}
 
 										if (this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11) * var9 + var15 + var11 + 1] == -2) {
 											this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11) * var9 + var15 + var11 + 1] = var12;
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 
 				var12 = this.adjacentTreeBlocks[var11 * var10 + var11 * var9 + var11];
 
 				if (var12 >= 0) {
					par1World.setBlockMetadataWithNotify(par2, par3, par4, clearDecayOnMetadata(var6), 4);
 				} else {
 					this.removeLeaves(par1World, par2, par3, par4);
 				}
 			}
 		}
 	}
 
 	@SideOnly(Side.CLIENT)
 	/**
 	 * A randomly called display update to be able to add particles or other items for display
 	 */
 	@Override
 	public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random) {
 		if (par1World.provider.isHellWorld && par5Random.nextInt(15) == 1 && par1World.isAirBlock(par2, par3 - 1, par4)) {
 			double var6 = (double) ((float) par2 + par5Random.nextFloat());
 			double var8 = (double) par3 - 0.05D;
 			double var10 = (double) ((float) par4 + par5Random.nextFloat());
 			int metadata = par1World.getBlockMetadata(par2, par3, par4);
 			EntityFX fx = null;
 			switch (unmarkedMetadata(metadata)) {
 			case hellfire:
 				par1World.spawnParticle("dripLava", var6, var8, var10, 0.0D, 0.0D, 0.0D);
 				break;
 			case acid:
 				fx = new EntityDropParticleFXNetherStuffs(par1World, var6, var8, var10, "acid");
 				break;
 			case death:
 				fx = new EntityDropParticleFXNetherStuffs(par1World, var6, var8, var10, "death");
 				break;
 			default:
 				// par1World.spawnParticle("dripLava", var6, var8, var10, 0.0D, 0.0D, 0.0D);
 				break;
 			}
 
 			if (fx != null) {
 				Minecraft.getMinecraft().effectRenderer.addEffect((EntityFX) fx);
 			}
 		}
 
 	}
 
 	private void removeLeaves(World par1World, int par2, int par3, int par4) {
 		this.dropBlockAsItem(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4), 0);
 		par1World.setBlockToAir(par2, par3, par4);
 	}
 
 	/**
 	 * Returns the quantity of items to drop on block destruction.
 	 */
 	@Override
 	public int quantityDropped(Random par1Random) {
 		return par1Random.nextInt(20) == 0 ? 1 : 0;
 	}
 
 	/**
 	 * Returns the ID of the items to drop on destruction.
 	 */
 	@Override
 	public int idDropped(int par1, Random par2Random, int par3) {
 		return NetherBlocks.netherSapling.blockID;
 	}
 
 	/**
 	 * Called when the player destroys a block with an item that can harvest it. (i, j, k) are the coordinates of the block and l is the block's subtype/damage.
 	 */
 	@Override
 	public void harvestBlock(World par1World, EntityPlayer par2EntityPlayer, int par3, int par4, int par5, int par6) {
 		super.harvestBlock(par1World, par2EntityPlayer, par3, par4, par5, par6);
 	}
 
 	/**
 	 * Determines the damage on the item the block drops. Used in cloth and wood.
 	 */
 	@Override
 	public int damageDropped(int meta) {
 		return unmarkedMetadata(meta);
 	}
 
 	public int getMetadataSize() {
 		return NetherWoodItemBlock.blockNames.length;
 	}
 
 	@Override
 	public boolean isOpaqueCube() {
 		return false;
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister)
 	{
 		icoLeafHellfire = par1IconRegister.registerIcon(CommonProxy.getIconLocation("LeafHellfire"));
 		icoLeafAcid = par1IconRegister.registerIcon(CommonProxy.getIconLocation("LeafAcid"));
 		icoLeafDeath = par1IconRegister.registerIcon(CommonProxy.getIconLocation("LeafDeath"));		 
 	}
 	
 
 	@Override
 	public Icon getIcon(int side, int meta) {
 		switch (unmarkedMetadata(meta)) {
 		case hellfire:
 			return icoLeafHellfire;
 		case acid:
 			return icoLeafAcid;
 		case death:
 			return icoLeafDeath;
 		default:
 			return icoLeafHellfire;
 		}
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void getSubBlocks(int par1, CreativeTabs tab, List list) {
 		for (int metaNumber = 0; metaNumber < getMetadataSize(); metaNumber++) {
 			list.add(new ItemStack(par1, 1, metaNumber));
 		}
 	}
 
 	@Override
 	public boolean isShearable(ItemStack item, World world, int x, int y, int z) {
 		return true;
 	}
 
 	@Override
 	public ArrayList<ItemStack> onSheared(ItemStack item, World world, int x, int y, int z, int fortune) {
 		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
 		ret.add(new ItemStack(this, 1, unmarkedMetadata(world.getBlockMetadata(x, y, z))));
 		return ret;
 	}
 
 	@Override
 	public boolean isLeaves(World world, int x, int y, int z) {
 		return true;
 	}
	
 	@Override
   protected ItemStack createStackedBlock(int par1)
   {
       return new ItemStack(this.blockID, 1, par1 & 3);
   }
 
 	private static final int METADATA_BITMASK = 0x3;
 	private static final int METADATA_USERPLACEDBIT = 0x4;
 	private static final int METADATA_DECAYBIT = 0x8;
 	private static final int METADATA_CLEARDECAYBIT = -METADATA_DECAYBIT - 1;
 
 	private static int clearDecayOnMetadata(int metadata) {
 		return metadata & METADATA_CLEARDECAYBIT;
 	}
 
 	private static boolean isDecaying(int metadata) {
 		return (metadata & METADATA_DECAYBIT) != 0;
 	}
 
 	private static boolean isUserPlaced(int metadata) {
 		return (metadata & METADATA_USERPLACEDBIT) != 0;
 	}
 
 	@Override
 	public void beginLeavesDecay(World world, int x, int y, int z) {
		world.setBlockMetadataWithNotify(x, y, z, setDecayOnMetadata(world.getBlockMetadata(x, y, z)), 4);
 	}
 
 	private static int setDecayOnMetadata(int metadata) {
 		return metadata | METADATA_DECAYBIT;
 	}
 
 	protected static int unmarkedMetadata(int metadata) {
 		return metadata & METADATA_BITMASK;
 	}
 
 }
