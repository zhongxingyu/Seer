 package com.qzx.au.extras;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 import net.minecraftforge.common.EnumPlantType;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.IPlantable;
 import static net.minecraftforge.common.EnumPlantType.*;
 
 import java.util.Random;
 
 public class BlockFlowerSeed extends Block implements IPlantable {
 	@SideOnly(Side.CLIENT)
 	private Icon stage1Icon;
 	@SideOnly(Side.CLIENT)
 	private Icon stage0Icon;
 	@SideOnly(Side.CLIENT)
 	private Icon seedsIcon;
 
 	public static final float y_offset = 0.0625F;
 
 	public BlockFlowerSeed(int id, String name){
 		super(id, Material.plants);
 		this.setUnlocalizedName(name);
 		GameRegistry.registerBlock(this, ItemBlockFlowerSeed.class, name);
 
 		this.setTickRandomly(true);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister){
 		this.stage1Icon = iconRegister.registerIcon("au_extras:flowerStage1");
 		this.stage0Icon = iconRegister.registerIcon("au_extras:flowerStage0");
 		this.seedsIcon = iconRegister.registerIcon("au_extras:flowerSeeds");
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int stage){
 		if(stage == 1) return this.stage1Icon;
 		return this.stage0Icon;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public Icon getItemIcon(int stage){
 		return this.seedsIcon;
 	}
 
 	@Override
 	public int damageDropped(int stage){
 		return 0;
 	}
 
 	@Override
 	public boolean isOpaqueCube(){
 		return false;
 	}
 
 	@Override
 	public boolean renderAsNormalBlock(){
 		return false;
 	}
 
 	@Override
 	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side){
 		return false;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public int getRenderType(){
 		return ClientProxy.flowerRenderType;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean canRenderInPass(int pass){
 		return (pass == 0);
 	}
 
 	@Override
 	public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z){
 		int stage = access.getBlockMetadata(x, y, z);
 		if(stage == 1)
 			this.setBlockBounds(0.35F, 0.0F-BlockFlowerSeed.y_offset, 0.35F,   0.65F, 0.45F-BlockFlowerSeed.y_offset, 0.65F);
 		else
 			this.setBlockBounds(0.40F, 0.0F-BlockFlowerSeed.y_offset, 0.40F,   0.60F, 0.15F-BlockFlowerSeed.y_offset, 0.60F);
 	}
 
 	//////////
 
 	@Override
 	public boolean canPlaceBlockAt(World world, int x, int y, int z){
 		return super.canPlaceBlockAt(world, x, y, z) && this.canBlockStay(world, x, y, z);
 	}
 
 	@Override
 	public void onNeighborBlockChange(World world, int x, int y, int z, int side){
 		super.onNeighborBlockChange(world, x, y, z, side);
 		this.checkFlowerChange(world, x, y, z);
 	}
 
 	@Override
 	public void updateTick(World world, int x, int y, int z, Random random){
 		if(this.checkFlowerChange(world, x, y, z)) return;
 		if(!world.isRemote)
 			if(world.getBlockLightValue(x, y, z) >= 9 && random.nextInt(50) == 0)
 				growFlower(world, x, y, z);
 	}
 
 	public void growFlower(World world, int x, int y, int z){
 		int stage = world.getBlockMetadata(x, y, z) + 1;
 		if(stage == 2){
 			// change to a flower block
 			Random random = new Random();
 			int color = (Cfg.enableFlowerSeed100 || random.nextInt(4) == 0
 						? random.nextInt(16)
 						: WorldGenFlowers.getBiomeColor(world, x, z, random)); // 25% chance to produce non-biome specific colors
 			world.setBlock(x, y, z, THIS_MOD.blockFlower.blockID, color, 2);
 		} else {
 			// increment stage
 			world.setBlockMetadataWithNotify(x, y, z, stage, 2);
 		}
 	}
 
 	private boolean checkFlowerChange(World world, int x, int y, int z){
 		if(!this.canBlockStay(world, x, y, z)){
 			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
 			world.setBlockToAir(x, y, z);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean canBlockStay(World world, int x, int y, int z){
 		Block soil = Block.blocksList[world.getBlockId(x, y - 1, z)];
		if(soil == null) return false;
 		return (world.getFullBlockLightValue(x, y, z) >= 8 || world.canBlockSeeTheSky(x, y, z)) && soil.isFertile(world, x, y - 1, z);
 	}
 
 	@Override
 	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z){
 		return null;
 	}
 
 	//////////
 
 	@Override
 	public EnumPlantType getPlantType(World world, int x, int y, int z){
 		return Plains;
 	}
 
 	@Override
 	public int getPlantID(World world, int x, int y, int z){
 		return blockID;
 	}
 
 	@Override
 	public int getPlantMetadata(World world, int x, int y, int z){
 		return world.getBlockMetadata(x, y, z);
 	}
 }
