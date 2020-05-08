 package com.github.soniex2.endermoney.core.block;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.fluids.BlockFluidClassic;
 import net.minecraftforge.fluids.Fluid;
 
 public class LiquidCoin extends BlockFluidClassic {
 
 	private Icon[] theIcon;
 
 	public LiquidCoin(int id, Fluid fluid) {
 		super(id, fluid, Material.water);
 		fluid.setBlockID(this);
 		this.setQuantaPerBlock(8);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int meta) {
 		return side != 0 && side != 1 ? this.theIcon[1] : this.theIcon[0];
 	}
 
 	@Override
 	public boolean displaceIfPossible(World world, int x, int y, int z) {
 		if (world.isAirBlock(x, y, z)) return true;
 		int bId = world.getBlockId(x, y, z);
		if (bId == blockID || bId == Block.waterStill.blockID || bId == Block.waterMoving.blockID)
			return false;
 		if (displacementIds.containsKey(bId)) {
 			if (displacementIds.get(bId)) {
 				Block.blocksList[bId].dropBlockAsItem(world, x, y, z,
 						world.getBlockMetadata(x, y, z), 0);
 				return true;
 			}
 			return false;
 		}
 		Material material = Block.blocksList[bId].blockMaterial;
 		if (material.blocksMovement() || material == Material.portal) return false;
		if (getDensity(world, x, y, z) == Integer.MAX_VALUE
 				|| this.density > getDensity(world, x, y, z)) {
 			Block.blocksList[bId].dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z),
 					0);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public int colorMultiplier(IBlockAccess iblockaccess, int x, int y, int z) {
 		int meta = iblockaccess.getBlockMetadata(x, y, z);
 		switch (meta) {
 			case 0:
 				return 0xffffff;
 			case 1:
 				return 0xf00000;
 			case 2:
 				return 0xff8000;
 			case 3:
 				return 0xffff00;
 			case 4:
 				return 0x7940;
 			case 5:
 				return 0x4040ff;
 			case 6:
 				return 0xa000c0;
 			case 7:
 				return 0;
 		}
 		return 0;
 	}
 
 	@Override
 	public void registerIcons(IconRegister ir) {
 		String l = "endermoneycore:liquid_";
 		this.theIcon = new Icon[] { ir.registerIcon(l + "still"), ir.registerIcon(l + "flow") };
 	}
 
 	@Override
 	public void onEntityCollidedWithBlock(World world, int x_coord, int y_coord, int z_coord,
 			Entity entity) {
 		if (world.isRemote) return;
 		if (entity instanceof EntityLivingBase) {
 			EntityLivingBase living = (EntityLivingBase) entity;
 			living.setAir(300);
 		}
 	}
 
 }
