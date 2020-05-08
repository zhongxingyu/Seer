 package loecraftpack.items;
 
 import java.util.List;
 
 import loecraftpack.LoECraftPack;
 import loecraftpack.blocks.ColoredBedBlock;
 import loecraftpack.blocks.te.ColoredBedTileEntity;
 import loecraftpack.enums.Dye;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 
 public class ColoredBedItem extends Item
 {
 	//TODO  render appearance based on item damage
 	//Associated Block
 	
     public ColoredBedItem(int par1)
     {
         super(par1);
         this.setHasSubtypes(true);
 		this.setMaxDamage(0);
         this.setCreativeTab(LoECraftPack.LoECraftTab);
         this.setUnlocalizedName("coloredBed");
     }
 
     /**
      * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
      * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
      */
     public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int xCoord, int yCoord, int zCoord, int par7, float par8, float par9, float par10)
     {
         if (world.isRemote)
         {
             return true;
         }
         else if (par7 != 1)
         {
             return false;
         }
         else
         {
             ++yCoord;
             int i1 = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
             byte b0 = 0;
             byte b1 = 0;
 
             if (i1 == 0)
             {
                 b1 = 1;
             }
 
             if (i1 == 1)
             {
                 b0 = -1;
             }
 
             if (i1 == 2)
             {
                 b1 = -1;
             }
 
             if (i1 == 3)
             {
                 b0 = 1;
             }
 
             if (player.canPlayerEdit(xCoord, yCoord, zCoord, par7, itemStack) && player.canPlayerEdit(xCoord + b0, yCoord, zCoord + b1, par7, itemStack))
             {
                 if (world.isAirBlock(xCoord, yCoord, zCoord) && world.isAirBlock(xCoord + b0, yCoord, zCoord + b1) && world.doesBlockHaveSolidTopSurface(xCoord, yCoord - 1, zCoord) && world.doesBlockHaveSolidTopSurface(xCoord + b0, yCoord - 1, zCoord + b1))
                 {
                 	System.out.println("place bed");
                     world.setBlock(xCoord, yCoord, zCoord, LoECraftPack.bedBlock.blockID, i1, 3);
                     world.setBlockTileEntity(xCoord, yCoord, zCoord, (new ColoredBedTileEntity(Dye.values()[MathHelper.clamp_int(itemStack.getItemDamage(), 0, ColoredBedBlock.bedTypes-1)])));
 
                     if (world.getBlockId(xCoord, yCoord, zCoord) == LoECraftPack.bedBlock.blockID)
                     {
                         world.setBlock(xCoord + b0, yCoord, zCoord + b1, LoECraftPack.bedBlock.blockID, i1 + 8, 3);
                         world.setBlockTileEntity(xCoord + b0, yCoord, zCoord + b1, (new ColoredBedTileEntity(Dye.values()[MathHelper.clamp_int(itemStack.getItemDamage(), 0, ColoredBedBlock.bedTypes-1)])));
                     }
 
                     --itemStack.stackSize;
                     return true;
                 }
                 else
                 {
                     return false;
                 }
             }
             else
             {
                 return false;
             }
         }
     }
     
     @Override
     public String getUnlocalizedName(ItemStack itemStack)
     {
     	return super.getUnlocalizedName() + "." + Dye.values()[MathHelper.clamp_int(itemStack.getItemDamage(), 0, ColoredBedBlock.bedTypes-1)];
     }
     
     @Override
 	public void getSubItems(int id, CreativeTabs tab, List list)
 	{
		for (int j = 0; j < ColoredBedBlock.bedTypes; ++j)
     	{
     		list.add(new ItemStack(id, 1, j));
     	}
 	}
 }
