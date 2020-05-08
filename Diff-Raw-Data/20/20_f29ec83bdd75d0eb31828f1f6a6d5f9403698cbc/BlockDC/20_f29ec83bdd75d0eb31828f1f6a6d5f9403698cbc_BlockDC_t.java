 package mods.minecraft.darth.dc.block;
 
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import mods.minecraft.darth.dc.lib.Reference;
 import mods.minecraft.darth.dc.tileentity.TileDC;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 
 public class BlockDC extends Block
 {
 
     public BlockDC(int id, Material material)
     {
         super(id, material);
     }
     
     
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister)
     {
         blockIcon = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":" + this.getUnlocalizedName2());
     }
 
     
    /*Sets the direction of the block when placed
     @Override
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityLiving, ItemStack itemStack)
     {
         int direction = 0;
         int facing = MathHelper.floor_double(entityLiving.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
 
 
         switch (facing)
         {
             case 0:
                 direction = ForgeDirection.NORTH.ordinal();
                 break;
                 
             case 1:
                 direction = ForgeDirection.EAST.ordinal();
                 break;
                 
             case 2:
                 direction = ForgeDirection.SOUTH.ordinal();
                 break;
                 
             case 3:
                 direction = ForgeDirection.WEST.ordinal();
                 break;
                 
         };
 
 
         world.setBlockMetadataWithNotify(x, y, z, direction, 3);
 
 
         if (itemStack.hasDisplayName())
         {
             ((TileDC) world.getBlockTileEntity(x, y, z)).setCustomName(itemStack.getDisplayName());
         }
 
         ((TileDC) world.getBlockTileEntity(x, y, z)).setOrientation(direction);
    }*/
 
 
 }
