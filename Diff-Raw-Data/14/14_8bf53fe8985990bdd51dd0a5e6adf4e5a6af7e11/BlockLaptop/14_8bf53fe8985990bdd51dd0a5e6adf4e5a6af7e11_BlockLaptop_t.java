 package net.cortexmodders.atomtech.blocks;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.util.ArrayList;
 
 import net.cortexmodders.atomtech.AtomTech;
 import net.cortexmodders.atomtech.CommonProxy;
 import net.cortexmodders.atomtech.item.ModItems;
 import net.cortexmodders.atomtech.lib.RenderIds;
 import net.cortexmodders.atomtech.tileentity.TileEntityLaptop;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 public class BlockLaptop extends BlockContainer
 {
     public static void ejectFlashDrive(final World world, final int x, final int y, final int z)
     {
         TileEntityLaptop tile = (TileEntityLaptop) world.getBlockTileEntity(x, y, z);
         if (tile != null && !world.isRemote && tile.getStackInSlot(0) != null)
         {
             EntityItem item = new EntityItem(world);
             item.setEntityItemStack(tile.getStackInSlot(0));
             switch (world.getBlockMetadata(x, y, z))
             {
                 case 0:
                     item.setPosition(x + 1.1D, y, z + 0.5D);
                     break;
                 case 1:
                     item.setPosition(x + 0.5D, y, z + 1.1D);
                     break;
                 case 2:
                     item.setPosition(x - 0.1D, y, z + 0.5D);
                     break;
                 case 3:
                     item.setPosition(x + 0.5D, y, z - 0.1D);
                     break;
             }
             
             world.spawnEntityInWorld(item);
             tile.setInventorySlotContents(0, null);
         }
     }
     
     /**
      * Gets the hitbox for the flash drive on the block using metadata.
      * 
      * @param meta
      * @return
      */
     public static AxisAlignedBB getSubHitBox(final int meta)
     {
         float f = 0.0625F;
         final float origMinX = 1;
         final float origMinY = 0;
         final float origMinZ = f * 4;
         
         final float origMaxX = 1;
         final float origMaxY = f * 2;
         final float origMaxZ = f * 7;
         
         float minX = origMinX;
         float minY = origMinY;
         float minZ = origMinZ;
         
         float maxX = origMaxX;
         float maxY = origMaxY;
         float maxZ = origMaxZ;
         
         switch (meta)
         {
             case 1:
                 minX = 1 - origMaxZ;
                 minZ = origMinX;
                 maxX = 1 - origMinZ;
                 maxZ = origMaxX;
                 break;
             case 2:
                 minX = 0;
                 minZ = 1 - origMaxZ;
                 maxX = 0;
                 maxZ = 1 - origMinZ;
                 break;
             case 3:
                 minX = origMinZ;
                 minZ = 0;
                 maxX = origMaxZ;
                 maxZ = 0;
                 break;
         }
         
         return AxisAlignedBB.getAABBPool().getAABB(minX, minY, minZ, maxX, maxY, maxZ);
     }
     
     public static void sync(final int x, final int y, final int z, final TileEntityLaptop tile)
     {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         DataOutputStream data = new DataOutputStream(bos);
         
         try
         {
             data.writeByte(0);
             data.writeInt(x);
             data.writeInt(y);
             data.writeInt(z);
             data.writeByte(tile.getData());
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
         
         PacketDispatcher.sendPacketToAllPlayers(PacketDispatcher.getPacket("AtomTech", bos.toByteArray()));
     }
     
     public BlockLaptop(final int id)
     {
         super(id, Material.iron);
         this.setCreativeTab(AtomTech.atomTab);
     }
     
     @Override
     public void breakBlock(final World world, final int x, final int y, final int z, final int blockId, final int meta)
     {
         if (!world.isRemote)
         {
             TileEntityLaptop tile = (TileEntityLaptop) world.getBlockTileEntity(x, y, z);
             if (tile.hasFlashDrive())
                 ejectFlashDrive(world, x, y, z);
         }
     }
     
     @Override
     public TileEntity createNewTileEntity(final World world)
     {
         return new TileEntityLaptop();
     }
     
     @Override
     public ArrayList<ItemStack> getBlockDropped(final World world, final int x, final int y, final int z, final int metadata, final int fortune)
     {
         ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
         
         ItemStack stack = new ItemStack(ModBlocks.laptop);
         NBTTagCompound tag = stack.getTagCompound();
         if (tag == null)
             tag = new NBTTagCompound();
         tag.setFloat("power", ((TileEntityLaptop) world.getBlockTileEntity(x, y, z)).getPower());
         tag.setByte("laptopDamage", ((TileEntityLaptop) world.getBlockTileEntity(x, y, z)).getCondition());
         stack.setTagCompound(tag);
         
         ret.add(stack);
         super.breakBlock(world, x, y, z, ModBlocks.laptop.blockID, metadata);
         return ret;
     }
     
     @Override
     public int getRenderType()
     {
         return RenderIds.LAPTOP_RENDER_ID;
     }
     
     @Override
     public boolean isOpaqueCube()
     {
         return false;
     }
     
     @Override
     public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ)
     {
         if (!world.isRemote && player != null)
         {
             TileEntityLaptop tile = (TileEntityLaptop) world.getBlockTileEntity(x, y, z);
             if (tile != null && !tile.isBroken())
             {
                 ItemStack heldItem = player.getHeldItem();
                 if (CommonProxy.isVecInsideBB(Vec3.createVectorHelper(hitX, hitY, hitZ), getSubHitBox(world.getBlockMetadata(x, y, z))))
                 {
                     if (heldItem != null && heldItem.getItem().equals(ModItems.flashDrive) && !tile.hasFlashDrive())
                     {
                         ((TileEntityLaptop) world.getBlockTileEntity(x, y, z)).setInventorySlotContents(0, heldItem);
                         heldItem.stackSize--;
                     }
                     else if (heldItem == null && tile.hasFlashDrive())
                     {
                         player.setCurrentItemOrArmor(0, tile.getStackInSlot(0));
                         ((TileEntityLaptop) world.getBlockTileEntity(x, y, z)).setInventorySlotContents(0, null);
                     }
                 }
                 else
                 {
                     // player.openGui(AtomTech.instance, 2, world, x, y, z);
                     tile.toggleLid();
                     tile.setState((byte) 7);
                 }
                 sync(x, y, z, tile);
                 return true;
             }
             else if (tile != null && tile.isBroken())
             {
                 ItemStack heldItem = player.getHeldItem();
                 if (heldItem != null && heldItem.getItem().equals(Item.stick))
                 {
                     tile.fix();
                     sync(x, y, z, tile);
                     return true;
                 }
             }
         }
         return false;
     }
     
     @Override
     public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase entity, final ItemStack stack)
     {
         super.onBlockPlacedBy(world, x, y, z, entity, stack);
         int l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
         
         world.setBlockMetadataWithNotify(x, y, z, l, 2);
         
         NBTTagCompound tag = stack.getTagCompound();
         if (tag != null)
         {
             ((TileEntityLaptop) world.getBlockTileEntity(x, y, z)).setPower(tag.getFloat("power"));
             ((TileEntityLaptop) world.getBlockTileEntity(x, y, z)).setCondition(tag.getByte("laptopDamage"));
         }
     }
     
     @Override
     public void onEntityWalking(final World par1World, final int x, final int y, final int z, final Entity entity)
     {
         TileEntityLaptop tile = (TileEntityLaptop) par1World.getBlockTileEntity(x, y, z);
         
         if (!par1World.isRemote)
         {
             if (!(entity instanceof EntityPlayer) && !par1World.getGameRules().getGameRuleBooleanValue("mobGriefing"))
                 return;
             
             tile.degradeCondition();
             sync(x, y, z, tile);
         }
     }
     
     @Override
     public void onFallenUpon(final World par1World, final int x, final int y, final int z, final Entity entity, final float par6)
     {
         TileEntityLaptop tile = (TileEntityLaptop) par1World.getBlockTileEntity(x, y, z);
         
         if (!par1World.isRemote)// && par1World.rand.nextFloat() < par6 - 0.5F -
                                 // (tile.isLidClosed() ? 0.6875F : 0.0F))
         {
             if (!(entity instanceof EntityPlayer) && !par1World.getGameRules().getGameRuleBooleanValue("mobGriefing"))
                 return;
             
             tile.degradeCondition();
             sync(x, y, z, tile);
         }
     }
     
     @Override
     public void registerIcons(final IconRegister register)
     {
         this.blockIcon = register.registerIcon("atomtech:laptop");
     }
     
     @Override
     public boolean renderAsNormalBlock()
     {
         return false;
     }
     
     @Override
     public void setBlockBoundsBasedOnState(final IBlockAccess access, final int x, final int y, final int z)
     {
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
         float minx = (float) this.minX;
         float miny = (float) this.minY;
         float minz = (float) this.minZ;
         float maxx = (float) this.maxX;
         float maxy = (float) this.maxY;
         float maxz = (float) this.maxZ;
         
         if (access.getBlockTileEntity(x, y, z) != null)
             if (!((TileEntityLaptop) access.getBlockTileEntity(x, y, z)).isLidClosed())
                 maxy = 0.8125F;
         
         int meta = access.getBlockMetadata(x, y, z);
         if (meta == 0 || meta == 2)
         {
             minz = 0.125F;
             maxz = 0.875F;
             if (!((TileEntityLaptop) access.getBlockTileEntity(x, y, z)).isLidClosed())
                 if (meta == 0)
                     maxz = 1.0F;
                 else if (meta == 2)
                     minz = 0.0F;
         }
         else if (meta == 1 || meta == 3)
         {
             minx = 0.125F;
             maxx = 0.875F;
             if (!((TileEntityLaptop) access.getBlockTileEntity(x, y, z)).isLidClosed())
                 if (meta == 1)
                     minx = 0.0F;
                 else if (meta == 3)
                     maxx = 1.0F;
         }
         
         this.setBlockBounds(minx, miny, minz, maxx, maxy, maxz);
     }
 }
