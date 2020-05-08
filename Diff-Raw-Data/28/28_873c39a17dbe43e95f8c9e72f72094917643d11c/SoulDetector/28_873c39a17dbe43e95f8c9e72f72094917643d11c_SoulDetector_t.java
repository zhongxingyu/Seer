 package NetherStuffs.Blocks;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.src.AxisAlignedBB;
 import net.minecraft.src.BlockContainer;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityBat;
 import net.minecraft.src.EntityBlaze;
 import net.minecraft.src.EntityChicken;
 import net.minecraft.src.EntityCow;
 import net.minecraft.src.EntityCreeper;
 import net.minecraft.src.EntityDragon;
 import net.minecraft.src.EntityEnderman;
 import net.minecraft.src.EntityGhast;
 import net.minecraft.src.EntityIronGolem;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityMagmaCube;
 import net.minecraft.src.EntityMob;
 import net.minecraft.src.EntityMooshroom;
 import net.minecraft.src.EntityOcelot;
 import net.minecraft.src.EntityPig;
 import net.minecraft.src.EntityPigZombie;
 import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.EntitySheep;
 import net.minecraft.src.EntitySilverfish;
 import net.minecraft.src.EntitySkeleton;
 import net.minecraft.src.EntitySlime;
 import net.minecraft.src.EntitySnowman;
 import net.minecraft.src.EntitySpider;
 import net.minecraft.src.EntitySquid;
 import net.minecraft.src.EntityVillager;
 import net.minecraft.src.EntityWitch;
 import net.minecraft.src.EntityWither;
 import net.minecraft.src.EntityWolf;
 import net.minecraft.src.EntityZombie;
 import net.minecraft.src.IBlockAccess;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 import net.minecraftforge.common.ForgeDirection;
 import NetherStuffs.NetherStuffs;
 import NetherStuffs.SoulDetector.TileSoulDetector;
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 import cpw.mods.fml.common.network.FMLNetworkHandler;
 
 public class SoulDetector extends BlockContainer {
 
 	public static final int mk1 = 0;
 	public static final int mk2 = 1;
 	public static final int mk3 = 2;
 	public static final int mk4 = 3;
 
 	public SoulDetector(int par1, int par2) {
 		super(par1, par2, Material.circuits);
 		this.setCreativeTab(CreativeTabs.tabRedstone);
 		this.setRequiresSelfNotify();
 	}
 
 	@Override
 	public int tickRate() {
 		return 20;
 	}
 
 	@Override
 	public void onBlockAdded(World par1World, int par2, int par3, int par4) {
 		if (!par1World.isRemote) {
 			par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate());
 			setEmittingSignal(false, par1World, par2, par3, par4);
 		}
 	}
 
 	@Override
 	public boolean isPoweringTo(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
 		return (par1IBlockAccess.getBlockMetadata(par2, par3, par4) & 8) > 0;
 	}
 
 	@Override
 	public boolean isIndirectlyPoweringTo(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
 		return (par1IBlockAccess.getBlockMetadata(par2, par3, par4) & 8) > 0;
 	}
 
 	@Override
 	public String getTextureFile() {
 		return "/block_detector.png";
 	}
 
 	@Override
 	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
 		if ((meta & 8) > 0)
 			return side + 16;
 		else
 			return side;
 	}
 
 	public int getMetadataSize() {
 		return SoulDetectorItemBlock.blockNames.length;
 	}
 
 	@Override
 	public int damageDropped(int meta) {
 		return meta & 7;
 	}
 
 	@Override
 	public boolean canProvidePower() {
 		return true;
 	}
 
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i, float f, float g, float t) {
 		TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
 
 		if (tile_entity == null || player.isSneaking()) {
 			return false;
 		}
 
 		FMLNetworkHandler.openGui(player, NetherStuffs.instance, 0, world, x, y, z);
 
 		if (!world.isRemote) {
 			((TileSoulDetector) tile_entity).sendToClient(player);
 			((TileSoulDetector) tile_entity).sendDetectToClient(player);
 		}
 		return true;
 	}
 
 	@Override
 	public void updateTick(World par1World, int xCoord, int yCoord, int zCoord, Random par5Random) {
 		if (!par1World.isRemote) {
 			TileEntity tile_entity = par1World.getBlockTileEntity(xCoord, yCoord, zCoord);
 			if (tile_entity instanceof TileSoulDetector) {
 
 				int nRangeUp = ((TileSoulDetector) tile_entity).getRange(ForgeDirection.UP);
 				int nRangeDown = ((TileSoulDetector) tile_entity).getRange(ForgeDirection.DOWN);
 				int nRangeNorth = ((TileSoulDetector) tile_entity).getRange(ForgeDirection.NORTH);
 				int nRangeSouth = ((TileSoulDetector) tile_entity).getRange(ForgeDirection.SOUTH);
 				int nRangeEast = ((TileSoulDetector) tile_entity).getRange(ForgeDirection.EAST);
 				int nRangeWest = ((TileSoulDetector) tile_entity).getRange(ForgeDirection.WEST);
 
 				Integer nLowerX = xCoord - nRangeWest;
 				Integer nLowerY = yCoord - nRangeDown;
 				Integer nLowerZ = zCoord - nRangeNorth;
 				Integer nUpperX = xCoord + nRangeEast + 1;
 				Integer nUpperY = yCoord + nRangeUp + 1;// height has to be 1 more for upwards detection (detects Head Position)
 				Integer nUpperZ = zCoord + nRangeSouth + 1;
 
 				AxisAlignedBB axis = AxisAlignedBB.getAABBPool().addOrModifyAABBInPool(nLowerX, nLowerY, nLowerZ, nUpperX, nUpperY, nUpperZ);
 				List tmp = par1World.getEntitiesWithinAABBExcludingEntity((Entity) null, axis);
 
 				List results = new ArrayList(); //this could be a boolean, but maybe we want a count based detection, like, if 5 Pigs...
 				Iterator it = tmp.iterator();
 				
 				while (it.hasNext()) {
 					Object data = it.next();
					if (data instanceof EntityLiving || data instanceof EntityPlayerMP) {} else {
 						it.remove();
 					}
 				}
 
 				if (((TileSoulDetector) tile_entity).detectEntities[TileSoulDetector.nDetectEverything]) {
 					results.addAll(tmp);
 				}
 
 				if (((TileSoulDetector) tile_entity).detectEntities[TileSoulDetector.nDetectHostile]) {
 					it = tmp.iterator();
 					while (it.hasNext() && results.size() == 0) {
 						Object data = it.next();
 						if (data instanceof EntityMob || data instanceof EntityGhast || data instanceof EntitySlime || data instanceof EntityWolf || data instanceof EntityDragon) {
 							if (data instanceof EntityWolf && ((EntityWolf) data).isTamed() == true) {} else
 								results.add(data);
 						}
 					}
 				}
 
 				if (((TileSoulDetector) tile_entity).detectEntities[TileSoulDetector.nDetectNonHostile]) {
 					it = tmp.iterator();
 					while (it.hasNext() && results.size() == 0) {
 						Object data = it.next();
 						if (data instanceof EntityCow || data instanceof EntityBat || data instanceof EntityVillager || data instanceof EntityOcelot) {
 							if (data instanceof EntityWolf && ((EntityWolf) data).isTamed() == false) {} else
 								results.add(data);
 						}
 					}
 				}
 
 				if (results.size() == 0) {
 					it = tmp.iterator();
 					while (it.hasNext() && results.size() == 0) {
 						Object data = it.next();
 						if (data instanceof EntityPig && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectPig])
 							results.add(data);
 						else if (data instanceof EntitySheep && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectSheep])
 							results.add(data);
 						else if (data instanceof EntityCow && !(data instanceof EntityMooshroom) && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectCow])
 							results.add(data);
 						else if (data instanceof EntityChicken && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectChicken])
 							results.add(data);
 						else if (data instanceof EntitySquid && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectSquid])
 							results.add(data);
 						else if (data instanceof EntityMooshroom && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectMooshroom])
 							results.add(data);
 						else if (data instanceof EntityVillager && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectVillager])
 							results.add(data);
 						else if (data instanceof EntityOcelot && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectOcelot])
 							results.add(data);
 						else if (data instanceof EntityBat && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectBat])
 							results.add(data);
 						else if (data instanceof EntityWolf && ((EntityWolf) data).isAngry() == false && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectWolfTameable])
 							results.add(data);
 						else if (data instanceof EntityIronGolem && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectIronGolem])
 							results.add(data);
 						else if (data instanceof EntitySnowman && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectSnowGolem])
 							results.add(data);
 						else if (data instanceof EntityCreeper && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectCreeper])
 							results.add(data);
 						else if (data instanceof EntityZombie && !(data instanceof EntityPigZombie) && ((EntityZombie) data).isVillager() == false
 								&& ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectZombie])
 							results.add(data);
 						else if (data instanceof EntitySpider && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectSpider]) // this also detects cave spiders
 							results.add(data);
 						else if (data instanceof EntityWither && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectWither])
 							results.add(data);
 						else if (data instanceof EntitySkeleton && ((EntitySkeleton) data).getSkeletonType() != 1 && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectSkeleton])
 							results.add(data);
 						else if (data instanceof EntityWolf && ((EntityWolf) data).isAngry() == true && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectWolfAggressive])
 							results.add(data);
 						else if (data instanceof EntitySilverfish && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectSilverfish])
 							results.add(data);
 						else if (data instanceof EntityEnderman && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectEnderman])
 							results.add(data);
 						else if (data instanceof EntitySlime && !(data instanceof EntityMagmaCube) && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectSlime])
 							results.add(data);
 						else if (data instanceof EntityGhast && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectGhast])
 							results.add(data);
 						else if (data instanceof EntityPigZombie && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectPigZombie])
 							results.add(data);
 						else if (data instanceof EntitySkeleton && ((EntitySkeleton) data).getSkeletonType() == 1
 								&& ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectWitherSkeleton])
 							results.add(data);
 						else if (data instanceof EntityMagmaCube && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectMagmaCube])
 							results.add(data);
 						else if (((Entity) data).ridingEntity instanceof EntitySpider && ((Entity) data).riddenByEntity instanceof EntitySkeleton
 								&& ((EntitySkeleton) ((Entity) data).riddenByEntity).getSkeletonType() == 1
 								&& ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectWitherSkeletonJockey])
 							results.add(data);
 						else if (data instanceof EntityBlaze && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectBlaze])
 							results.add(data);
 						else if (data instanceof EntityWitch && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectWitch])
 							results.add(data);
 						else if (data instanceof EntityZombie && ((EntityZombie) data).isVillager() == true && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectZombieVillager])
 							results.add(data);
 						else if (((Entity) data).ridingEntity instanceof EntitySpider && ((Entity) data).riddenByEntity instanceof EntitySkeleton
 								&& ((EntitySkeleton) ((Entity) data).riddenByEntity).getSkeletonType() != 1
 								&& ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectWitherSkeletonJockey])
 							results.add(data);
 						else if (data instanceof EntityDragon && ((TileSoulDetector) tile_entity).detectEntitiesMobs[TileSoulDetector.nDetectEnderDragon])
 							results.add(data);
 					}
 				}
 
 				// System.out.println(results);
 
 				if (results.size() >= 1) {
 					setEmittingSignal(true, par1World, xCoord, yCoord, zCoord);
 				} else {
 					setEmittingSignal(false, par1World, xCoord, yCoord, zCoord);
 				}
 			}
 
 			par1World.scheduleBlockUpdate(xCoord, yCoord, zCoord, this.blockID, this.tickRate());
 		}
 	}
 
 	private void setEmittingSignal(boolean active, World par1World, int par2, int par3, int par4) {
 		if (!par1World.isRemote) {
 			int nMeta = par1World.getBlockMetadata(par2, par3, par4);
 			if ((nMeta & 8) > 0 && active == false) // it is active, sets to non active
 				par1World.setBlockMetadataWithNotify(par2, par3, par4, (nMeta & 7));
 			else if ((nMeta & 8) == 0 && active == true) // it is not active, sets to active
 				par1World.setBlockMetadataWithNotify(par2, par3, par4, (nMeta | 8));
 		}
 	}
 
 	@Override
 	public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6) {
 		super.breakBlock(par1World, par2, par3, par4, par5, par6);
 		setEmittingSignal(false, par1World, par2, par3, par4);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileSoulDetector();
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void getSubBlocks(int par1, CreativeTabs tab, List list) {
 		for (int metaNumber = 0; metaNumber < getMetadataSize(); metaNumber++) {
 			list.add(new ItemStack(par1, 1, metaNumber));
 		}
 	}
 }
