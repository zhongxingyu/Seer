 package jack_1197.swag.common;
 
 import net.minecraft.client.entity.EntityClientPlayerMP;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class CustomFood extends ItemFood {
 	private boolean hasNetherFX = false;
 
 	public CustomFood(int par1, int par2, boolean par3) {
 		super(par1, par2, par3);
 	}
 
    public Item setHasNetherFX(boolean par1)
    {
        this.hasNetherFX = par1;
        return this;
    }
 
 	@Override
 	public ItemStack onFoodEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		if (hasNetherFX) {
			((EntityClientPlayerMP) par3EntityPlayer).timeInPortal = 10.0F;
			((EntityClientPlayerMP) par3EntityPlayer).timeUntilPortal = 10;
 		}
 		return super.onFoodEaten(par1ItemStack, par2World, par3EntityPlayer);
 	}
 }
