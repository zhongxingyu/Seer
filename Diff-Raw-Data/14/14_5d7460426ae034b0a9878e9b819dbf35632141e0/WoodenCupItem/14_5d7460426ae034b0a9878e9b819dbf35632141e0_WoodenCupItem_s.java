 package mmm.esm.thirst.items;
 
 import mmm.esm.thirst.ItemDrink;
 import mmm.esm.thirst.ThirstLoader;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumMovingObjectType;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.world.World;
 
 public class WoodenCupItem extends Item {
 
 
 	public WoodenCupItem(int par1) {
 		super(par1);
		setMaxStackSize(1);
 		setCreativeTab(CreativeTabs.tabMisc);
 		setUnlocalizedName("woodenCup");
 		
 	}
 	
 	public void registerIcons(IconRegister iconRegister)
 	{
 		
 	         this.itemIcon = iconRegister.registerIcon("ThirstMod:WoodenCup");
 	}
 	
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
     {
 		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(par2World, par3EntityPlayer, true);
 		if(movingobjectposition == null)
 			return par1ItemStack;
 		
 		
 		if (movingobjectposition.typeOfHit == EnumMovingObjectType.TILE)
         {
             int i = movingobjectposition.blockX;
             int j = movingobjectposition.blockY;
             int k = movingobjectposition.blockZ;
 
             if (!par2World.canMineBlock(par3EntityPlayer, i, j, k))
             {
                 return par1ItemStack;
             }
 
             if (!par3EntityPlayer.canPlayerEdit(i, j, k, movingobjectposition.sideHit, par1ItemStack))
             {
                 return par1ItemStack;
             }
             
             Material m = par2World.getBlockMaterial(i, j, k);
             if (m == Material.water && par2World.getBlockMetadata(i, j, k) == 0)
             {
            	par3EntityPlayer.inventory.addItemStackToInventory(new ItemStack(par1ItemStack.itemID,par1ItemStack.stackSize-1,par1ItemStack.getItemDamage()));
             	
            	return new ItemStack(ThirstLoader.unfilteredWaterCup);
             }
         }
 		
 		return par1ItemStack;
 
     }
 	
 }
