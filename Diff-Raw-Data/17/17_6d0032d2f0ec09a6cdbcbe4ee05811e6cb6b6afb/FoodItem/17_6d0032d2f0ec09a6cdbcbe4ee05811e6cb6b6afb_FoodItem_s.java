 package cooler.items;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.ItemFood;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class FoodItem extends ItemFood {
 
     public FoodItem(int id, int healAmount, float saturationModifier) {
         super(id, healAmount, saturationModifier, true);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister) {
        this.itemIcon = par1IconRegister.registerIcon(this.getUnlocalizedName().substring(
                this.getUnlocalizedName().indexOf(".") + 1));
     }
 }
