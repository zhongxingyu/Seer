 package com.jjtcomkid.tb.block;
 
 import static net.minecraftforge.common.ForgeDirection.EAST;
 import static net.minecraftforge.common.ForgeDirection.NORTH;
 import static net.minecraftforge.common.ForgeDirection.SOUTH;
 import static net.minecraftforge.common.ForgeDirection.WEST;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockTorch;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
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
         setCreativeTab(CreativeTabs.tabDecorations);
         setUnlocalizedName("torchNew");
     }
 
     public boolean canPlaceTorchOn(World world, int x, int y, int z) {
         if (world.doesBlockHaveSolidTopSurface(x, y, z))
             return true;
         else {
             int Id = world.getBlockId(x, y, z);
             return (Block.blocksList[Id] != null && Block.blocksList[Id].canPlaceTorchOnTop(world, x, y, z));
         }
 
     }
 
     @Override
     public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 vector1, Vec3 vector2) {
         int metadata = world.getBlockMetadata(x, y, z);
         float f = 0.15F;
 
         if (metadata == 1 || metadata == 7)
             setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
         else if (metadata == 2 || metadata == 8)
             setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
         else if (metadata == 3 || metadata == 9)
             setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
         else if (metadata == 4 || metadata == 10)
             setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
         else {
             f = 0.1F;
             setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.6F, 0.5F + f);
         }
 
         setBlockBoundsBasedOnState(world, x, y, z);
         vector1 = vector1.addVector(-x, -y, -z);
         vector2 = vector2.addVector(-x, -y, -z);
         Vec3 vector3 = vector1.getIntermediateWithXValue(vector2, minX);
         Vec3 vector4 = vector1.getIntermediateWithXValue(vector2, maxX);
         Vec3 vector5 = vector1.getIntermediateWithYValue(vector2, minY);
         Vec3 vector6 = vector1.getIntermediateWithYValue(vector2, maxY);
         Vec3 vector7 = vector1.getIntermediateWithZValue(vector2, minZ);
         Vec3 vector8 = vector1.getIntermediateWithZValue(vector2, maxZ);
 
         if (!isVecInsideYZBounds(vector3))
             vector3 = null;
 
         if (!isVecInsideYZBounds(vector4))
             vector4 = null;
 
         if (!isVecInsideXZBounds(vector5))
             vector5 = null;
 
         if (!isVecInsideXZBounds(vector6))
             vector6 = null;
 
         if (!isVecInsideXYBounds(vector7))
             vector7 = null;
 
         if (!isVecInsideXYBounds(vector8))
             vector8 = null;
 
         Vec3 vector9 = null;
 
         if (vector3 != null && (vector9 == null || vector1.squareDistanceTo(vector3) < vector1.squareDistanceTo(vector9)))
             vector9 = vector3;
 
         if (vector4 != null && (vector9 == null || vector1.squareDistanceTo(vector4) < vector1.squareDistanceTo(vector9)))
             vector9 = vector4;
 
         if (vector5 != null && (vector9 == null || vector1.squareDistanceTo(vector5) < vector1.squareDistanceTo(vector9)))
             vector9 = vector5;
 
         if (vector6 != null && (vector9 == null || vector1.squareDistanceTo(vector6) < vector1.squareDistanceTo(vector9)))
             vector9 = vector6;
 
         if (vector7 != null && (vector9 == null || vector1.squareDistanceTo(vector7) < vector1.squareDistanceTo(vector9)))
             vector9 = vector7;
 
         if (vector8 != null && (vector9 == null || vector1.squareDistanceTo(vector8) < vector1.squareDistanceTo(vector9)))
             vector9 = vector8;
 
         if (vector9 == null)
             return null;
         else {
             byte b0 = -1;
 
             if (vector9 == vector3)
                 b0 = 4;
 
             if (vector9 == vector4)
                 b0 = 5;
 
             if (vector9 == vector5)
                 b0 = 0;
 
             if (vector9 == vector6)
                 b0 = 1;
 
             if (vector9 == vector7)
                 b0 = 2;
 
             if (vector9 == vector8)
                 b0 = 3;
 
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
 
     @SideOnly(Side.CLIENT)
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
         return TorchBurnout.renderTorchID;
     }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
     @Override
     @SideOnly(Side.CLIENT)
     public void getSubBlocks(int id, CreativeTabs tabs, List itemList) {
         for (int i = 0; i < 15; ++i)
             itemList.add(new ItemStack(id, 1, i));
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
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i1, float f1, float f2, float f3) {
         TileEntityTorchNew tile = (TileEntityTorchNew) world.getBlockTileEntity(x, y, z);
         if (tile != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().itemID == Item.flintAndSteel.itemID && world.getBlockMetadata(x, y, z) > 5) {
             int metadata = world.getBlockMetadata(x, y, z);
             tile.light = 14;
             world.setBlockMetadataWithNotify(x, y, z, metadata - 6, 3);
             return true;
         } else
             return super.onBlockActivated(world, x, y, z, player, i1, f1, f2, f3);
     }
 
     @Override
     public void onBlockAdded(World world, int x, int y, int z) {
         if (world.getBlockMetadata(x, y, z) == 0) {
             if (world.isBlockSolidOnSide(x - 1, y, z, EAST, true))
                 world.setBlockMetadataWithNotify(x, y, z, 1, 3);
             else if (world.isBlockSolidOnSide(x + 1, y, z, WEST, true))
                 world.setBlockMetadataWithNotify(x, y, z, 2, 3);
             else if (world.isBlockSolidOnSide(x, y, z - 1, SOUTH, true))
                 world.setBlockMetadataWithNotify(x, y, z, 3, 3);
             else if (world.isBlockSolidOnSide(x, y, z + 1, NORTH, true))
                 world.setBlockMetadataWithNotify(x, y, z, 4, 3);
             else if (canPlaceTorchOn(world, x, y - 1, z))
                 world.setBlockMetadataWithNotify(x, y, z, 5, 3);
         } else if (world.getBlockMetadata(x, y, z) == 6)
             if (world.isBlockSolidOnSide(x - 1, y, z, EAST, true))
                 world.setBlockMetadataWithNotify(x, y, z, 7, 3);
             else if (world.isBlockSolidOnSide(x + 1, y, z, WEST, true))
                 world.setBlockMetadataWithNotify(x, y, z, 8, 3);
             else if (world.isBlockSolidOnSide(x, y, z - 1, SOUTH, true))
                 world.setBlockMetadataWithNotify(x, y, z, 9, 3);
             else if (world.isBlockSolidOnSide(x, y, z + 1, NORTH, true))
                 world.setBlockMetadataWithNotify(x, y, z, 10, 3);
             else if (canPlaceTorchOn(world, x, y - 1, z))
                 world.setBlockMetadataWithNotify(x, y, z, 11, 3);
 
         super.dropTorchIfCantStay(world, x, y, z);
     }
 
     @Override
     public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
         int result = super.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, 0);
         if (metadata == 14)
             result += 6;
         return result;
     }
 
     @Override
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entity, ItemStack itemStack) {
         TileEntityTorchNew tile = (TileEntityTorchNew) world.getBlockTileEntity(x, y, z);
         if (tile != null)
             tile.light = 14 - itemStack.getItemDamage();
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void randomDisplayTick(World world, int x, int y, int z, Random random) {
         int metadata = world.getBlockMetadata(x, y, z);
         if (metadata < 6)
             super.randomDisplayTick(world, x, y, z, random);
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
         if (random.nextInt(25) == 0 && metadata < 6) {
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
                 }
             }
         }
         world.updateAllLightTypes(x, y, z);
         world.markBlockForUpdate(x, y, z);
     }
 }
