 package chrisclark13.minecraft.artificing.block;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import chrisclark13.minecraft.artificing.Artificing;
 import chrisclark13.minecraft.artificing.item.ItemRune;
 import chrisclark13.minecraft.artificing.item.ModItems;
 import chrisclark13.minecraft.artificing.lib.GuiIds;
 import chrisclark13.minecraft.artificing.lib.Reference;
 import chrisclark13.minecraft.artificing.lib.Strings;
 import chrisclark13.minecraft.artificing.network.packet.PacketATChargeUpdate;
 import chrisclark13.minecraft.artificing.tileentity.TileArtificingTable;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.server.FMLServerHandler;
 
 public class BlockArtificingTable extends BlockArtificingGeneral {
     
     public Icon iconTop;
     public Icon iconBottom;
     
     public BlockArtificingTable(int id) {
         super(id, Material.rock);
         this.setUnlocalizedName(Strings.ARTIFICING_TABLE_NAME);
         this.setCreativeTab(Artificing.tabArtificing);
         this.setHardness(5);
     }
     
     @SuppressWarnings({ "unchecked", "rawtypes" })
     @Override
     @SideOnly(Side.CLIENT)
     public void getSubBlocks(int id, CreativeTabs tabs, List list) {
         for (int i = TileArtificingTable.CHARGE_STATUS_THRESHOLDS.length - 1; i >= 0; i--) {
             list.add(new ItemStack(this, 1, TileArtificingTable.CHARGE_STATUS_THRESHOLDS[i]));
         }
     }
     
     @Override
     public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z) {
         if (!player.capabilities.isCreativeMode && !world.isRemote
                 && canHarvestBlock(player, world.getBlockMetadata(x, y, z))) {
             // TileArtificingTable table =
             // (TileArtificingTable)world.getBlockTileEntity(x, y, z);
             
             float motion = 0.7F;
             double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
             double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
             double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
             
             EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ,
                     getPickBlock(null, world, x, y, z));
             
             world.spawnEntityInWorld(entityItem);
         }
         
         return world.setBlockToAir(x, y, z);
     }
     
     @Override
     public int idDropped(int par1, Random par2Random, int par3) {
         return 0;
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int side, int metadata) {
         return side == ForgeDirection.UP.ordinal() ? iconTop : side == ForgeDirection.DOWN
                 .ordinal() ? iconBottom : this.blockIcon;
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister) {
         this.blockIcon = iconRegister.registerIcon(Reference.MOD_ID + ":"
                 + Strings.ARTIFICING_TABLE_ICON_SIDE);
         iconTop = iconRegister.registerIcon(Reference.MOD_ID + ":"
                 + Strings.ARTIFICING_TABLE_ICON_TOP);
         iconBottom = iconRegister.registerIcon(Reference.MOD_ID + ":"
                 + Strings.ARTIFICING_TABLE_ICON_BOTTOM);
     }
     
     @Override
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityLiving,
             ItemStack itemStack) {
         super.onBlockPlacedBy(world, x, y, z, entityLiving, itemStack);
         
         TileArtificingTable table = (TileArtificingTable) world.getBlockTileEntity(x, y, z);
         table.setCharge(itemStack.getItemDamage());
     }
     
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int idk,
             float what, float these, float are) {
         TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
         
         // Null check
         if (tileEntity == null) {
             return false;
         }
         
         if (player.isSneaking()) {
            if (player.getCurrentEquippedItem().getItem() instanceof ItemRune) {
                 
                 TileArtificingTable table = (TileArtificingTable) tileEntity;
                 if (table.getCharge() > 0) {
                    ItemStack itemStack = player.getCurrentEquippedItem();
                     int levels = ModItems.rune.getEnchantmentData(itemStack).enchantmentLevel;
                     
                     table.addCharge(-levels * TileArtificingTable.CHARGE_RESTORED_PER_RUNE_LEVEL);
                     
                     if (!player.capabilities.isCreativeMode) {
                         player.inventory.decrStackSize(player.inventory.currentItem, 1);
                     }
                     
                     if (!world.isRemote) {
                         PacketDispatcher.sendPacketToAllAround(x, y, z, 128D,
                                 world.provider.dimensionId,
                                 new PacketATChargeUpdate(x, y, z, table.getCharge())
                                         .makePacket250());
                     }
                 }
                 
                 return true;
             } else {
                 return false;
             }
         }
         
         // Open GUI
         player.openGui(Artificing.instance, GuiIds.ARTIFICING_TABLE, world, x, y, z);
         return true;
     }
     
     @Override
     public int getDamageValue(World world, int x, int y, int z) {
         TileArtificingTable table = (TileArtificingTable) world.getBlockTileEntity(x, y, z);
         return table.getCharge();
     }
     
     @Override
     public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata,
             int fortune) {
         // TODO Auto-generated method stub
         return super.getBlockDropped(world, x, y, z, metadata, fortune);
     }
     
     @Override
     public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
         dropItems(world, x, y, z);
         super.breakBlock(world, x, y, z, par5, par6);
     }
     
     private void dropItems(World world, int x, int y, int z) {
         Random rand = new Random();
         
         TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
         if (!(tileEntity instanceof TileArtificingTable)) {
             return;
         }
         TileArtificingTable inventory = (TileArtificingTable) tileEntity;
         
         for (int i = 0; i < inventory.getSizeInventory(); i++) {
             
             if (i == TileArtificingTable.OUTPUT_SLOT_INDEX) {
                 continue;
             }
             
             ItemStack item = inventory.getStackInSlot(i);
             
             if (item != null && item.stackSize > 0) {
                 float rx = rand.nextFloat() * 0.8F + 0.1F;
                 float ry = rand.nextFloat() * 0.8F + 0.1F;
                 float rz = rand.nextFloat() * 0.8F + 0.1F;
                 
                 EntityItem entityItem = new EntityItem(world, x + rx, y + ry, z + rz,
                         new ItemStack(item.itemID, item.stackSize, item.getItemDamage()));
                 
                 if (item.hasTagCompound()) {
                     entityItem.getEntityItem().setTagCompound(
                             (NBTTagCompound) item.getTagCompound().copy());
                 }
                 
                 float factor = 0.05F;
                 entityItem.motionX = rand.nextGaussian() * factor;
                 entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
                 entityItem.motionZ = rand.nextGaussian() * factor;
                 world.spawnEntityInWorld(entityItem);
                 item.stackSize = 0;
             }
         }
     }
     
     @Override
     public TileEntity createNewTileEntity(World world) {
         return new TileArtificingTable();
     }
 }
