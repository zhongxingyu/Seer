 package com.jjtcomkid.tb.block;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.BlockTorch;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 import com.jjtcomkid.tb.TorchBurnout;
 import com.jjtcomkid.tb.tileentity.TileEntityTorchNew;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * Torch Burnout
  *
  * @author jjtcomkid
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  *
  */
 public class BlockTorchNew extends BlockTorch {
 
 	@SideOnly(Side.CLIENT)
 	public Icon torchBurntIcon;
 	@SideOnly(Side.CLIENT)
 	public Icon torchLitIcon;
 
 	public BlockTorchNew() {
 		super(50);
 		this.setCreativeTab(CreativeTabs.tabDecorations);
 		this.setUnlocalizedName("torchNew");
 	}
 
 	@Override
 	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 vector1, Vec3 vector2) {
 		int metadata = world.getBlockMetadata(x, y, z);
 		float f = 0.15F;
 
 		if (metadata == 1 || metadata == 6) {
 			this.setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
 		} else if (metadata == 2 || metadata == 7) {
 			this.setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
 		} else if (metadata == 3 || metadata == 8) {
 			this.setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
 		} else if (metadata == 4 || metadata == 9) {
 			this.setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
 		} else {
 			f = 0.1F;
 			this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.6F, 0.5F + f);
 		}
 
 		this.setBlockBoundsBasedOnState(world, x, y, z);
 		vector1 = vector1.addVector(-x, -y, -z);
 		vector2 = vector2.addVector(-x, -y, -z);
 		Vec3 vector3 = vector1.getIntermediateWithXValue(vector2, minX);
 		Vec3 vector4 = vector1.getIntermediateWithXValue(vector2, maxX);
 		Vec3 vector5 = vector1.getIntermediateWithYValue(vector2, minY);
 		Vec3 vector6 = vector1.getIntermediateWithYValue(vector2, maxY);
 		Vec3 vector7 = vector1.getIntermediateWithZValue(vector2, minZ);
 		Vec3 vector8 = vector1.getIntermediateWithZValue(vector2, maxZ);
 
 		if (!this.isVecInsideYZBounds(vector3)) {
 			vector3 = null;
 		}
 
 		if (!this.isVecInsideYZBounds(vector4)) {
 			vector4 = null;
 		}
 
 		if (!this.isVecInsideXZBounds(vector5)) {
 			vector5 = null;
 		}
 
 		if (!this.isVecInsideXZBounds(vector6)) {
 			vector6 = null;
 		}
 
 		if (!this.isVecInsideXYBounds(vector7)) {
 			vector7 = null;
 		}
 
 		if (!this.isVecInsideXYBounds(vector8)) {
 			vector8 = null;
 		}
 
 		Vec3 vector9 = null;
 
 		if (vector3 != null && (vector9 == null || vector1.squareDistanceTo(vector3) < vector1.squareDistanceTo(vector9))) {
 			vector9 = vector3;
 		}
 
 		if (vector4 != null && (vector9 == null || vector1.squareDistanceTo(vector4) < vector1.squareDistanceTo(vector9))) {
 			vector9 = vector4;
 		}
 
 		if (vector5 != null && (vector9 == null || vector1.squareDistanceTo(vector5) < vector1.squareDistanceTo(vector9))) {
 			vector9 = vector5;
 		}
 
 		if (vector6 != null && (vector9 == null || vector1.squareDistanceTo(vector6) < vector1.squareDistanceTo(vector9))) {
 			vector9 = vector6;
 		}
 
 		if (vector7 != null && (vector9 == null || vector1.squareDistanceTo(vector7) < vector1.squareDistanceTo(vector9))) {
 			vector9 = vector7;
 		}
 
 		if (vector8 != null && (vector9 == null || vector1.squareDistanceTo(vector8) < vector1.squareDistanceTo(vector9))) {
 			vector9 = vector8;
 		}
 
 		if (vector9 == null)
 			return null;
 		else {
 			byte b0 = -1;
 
 			if (vector9 == vector3) {
 				b0 = 4;
 			}
 
 			if (vector9 == vector4) {
 				b0 = 5;
 			}
 
 			if (vector9 == vector5) {
 				b0 = 0;
 			}
 
 			if (vector9 == vector6) {
 				b0 = 1;
 			}
 
 			if (vector9 == vector7) {
 				b0 = 2;
 			}
 
 			if (vector9 == vector8) {
 				b0 = 3;
 			}
 
 			return new MovingObjectPosition(x, y, z, b0, vector9.addVector(x, y, z));
 		}
 	}
 
 	@Override
 	public TileEntity createTileEntity(World world, int metadata) {
 		return new TileEntityTorchNew();
 	}
 
 	@Override
 	public int damageDropped(int metadata) {
 		return 14;
 	}
 
 	@Override
 	public Icon getIcon(int side, int metadata) {
 		if (metadata < 6)
 			return torchLitIcon;
 		else
 			return torchBurntIcon;
 	}
 
 	@Override
 	public int getLightValue(IBlockAccess world, int x, int y, int z) {
 		TileEntityTorchNew tile = (TileEntityTorchNew) world.getBlockTileEntity(x, y, z);
 		if (tile != null) {
 			int light = tile.light;
 			return light;
 		}
 		return 14;
 	}
 
 	@Override
 	public int getRenderType() {
 		return TorchBurnout.renderID;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int id, CreativeTabs tabs, List itemList) {
		for (int i = 0; i < 16; ++i) {
 			itemList.add(new ItemStack(id, 1, i));
 		}
 	}
 
 	@Override
 	public boolean hasTileEntity(int metadata) {
 		return true;
 	}
 
 	private boolean isVecInsideXYBounds(Vec3 vector) {
 		return vector == null ? false : vector.xCoord >= minX && vector.xCoord <= maxX && vector.yCoord >= minY && vector.yCoord <= maxY;
 	}
 
 	private boolean isVecInsideXZBounds(Vec3 vector) {
 		return vector == null ? false : vector.xCoord >= minX && vector.xCoord <= maxX && vector.zCoord >= minZ && vector.zCoord <= maxZ;
 	}
 
 	private boolean isVecInsideYZBounds(Vec3 vector) {
 		return vector == null ? false : vector.yCoord >= minY && vector.yCoord <= maxY && vector.zCoord >= minZ && vector.zCoord <= maxZ;
 	}
 
 	@Override
 	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entity, ItemStack itemStack) {
 		TileEntityTorchNew tile = (TileEntityTorchNew) world.getBlockTileEntity(x, y, z);
 		if (tile != null) {
 			tile.light = 14 - itemStack.getItemDamage();
 		}
 		if (itemStack.getItemDamage() == 14) {
 			world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) + 5, 3);
 		}
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void randomDisplayTick(World world, int x, int y, int z, Random random) {
 		int metadata = world.getBlockMetadata(x, y, z);
 		if (metadata < 6) {
 			super.randomDisplayTick(world, x, y, z, random);
 		}
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister icon) {
 		torchLitIcon = icon.registerIcon("torch");
 		torchBurntIcon = icon.registerIcon("TorchBurnout:torchBurnt");
 	}
 
 	@Override
 	public void updateTick(World world, int x, int y, int z, Random random) {
 		super.updateTick(world, x, y, z, random);
 		int metadata = world.getBlockMetadata(x, y, z);
 		int light;
 		if (/* random.nextInt(25) == 0 && */metadata < 6) {
 			TileEntityTorchNew tile = (TileEntityTorchNew) world.getBlockTileEntity(x, y, z);
 			if (tile != null) {
 				light = tile.light;
 				if (light > 0) {
 					light -= 1;
 					tile.light = light;
 				}
 				if (light == 0) {
 					world.setBlockMetadataWithNotify(x, y, z, metadata + 5, 3);
 					metadata = world.getBlockMetadata(x, y, z);
 					world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
 					for (int l = 0; l < 8; ++l) {
 						world.spawnParticle("smoke", x + 0.5F + Math.random() / 25, y + 0.7D, z + 0.5 + Math.random() / 25, 0.0D, 0.2D, 0.0D);
 					}
 				}
 			}
 		}
 		world.updateAllLightTypes(x, y, z);
 		world.markBlockForUpdate(x, y, z);
 	}
 }
