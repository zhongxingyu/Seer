 
 package net.specialattack.modjam.item;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.specialattack.modjam.tileentity.TileEntityLight;
 
 public class ItemDebug extends Item {
 
     public ItemDebug(int itemId) {
         super(itemId);
         this.setFull3D();
         this.setMaxStackSize(1);
     }
 
     @Override
     public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float posX, float posY, float posZ) {
         if (world.isRemote) {
             return true;
         }
 
         int blockId = world.getBlockId(x, y, z);
         int meta = world.getBlockMetadata(x, y, z);
 
         player.addChatMessage("Block clicked: " + blockId + ":" + meta);
 
         if (Item.itemsList[blockId] != null) {
             ItemStack itemStack = new ItemStack(blockId, 1, meta);
             player.addChatMessage("Block name: " + itemStack.getDisplayName());
         }
         else if (Block.blocksList[blockId] != null) {
             player.addChatMessage("Block name: " + Block.blocksList[blockId].getLocalizedName());
         }
 
         TileEntity tile = world.getBlockTileEntity(x, y, z);
         if (tile != null) {
             player.addChatMessage(tile.toString());
             if (tile instanceof TileEntityLight) {
                 player.addChatMessage("Level: " + ((TileEntityLight) tile).getBrightness(0.0F));
             }
         }
 
         return true;
     }
 
 }
