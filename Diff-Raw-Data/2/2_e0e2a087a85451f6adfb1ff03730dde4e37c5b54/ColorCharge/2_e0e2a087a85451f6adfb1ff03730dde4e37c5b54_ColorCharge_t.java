 package FuzzCraft.Items;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ColorCharge extends Item {
  
     public ColorCharge(int id) {
         super(id);
         this.setUnlocalizedName("colorcharge");
         this.setCreativeTab(CreativeTabs.tabMaterials);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void updateIcons(IconRegister icon) {
         for (int i = 0; i < 15; i++) {
             iconIndex = icon.registerIcon("rpworldgen:charge_" + i);
         }
  }
     
     public final static String[] colorChargeNames = {
         "White Charge",
         "Orange Charge",
         "Magenta Charge",
         "Light Blue Charge",
         "Yellow Charge",
         "Lime Charge",
         "Pink Charge",
         "Dark Grey Charge",
         "Cyan Charge",
         "Purple Charge",
         "Blue Charge",
         "Brown Charge", 
         "Green Charge",
         "Red Charge",
         "Black Charge"
     };
    
     
     @Override
     public String getUnlocalizedName(ItemStack itemstack)
     {
             return this.getUnlocalizedName() + colorChargeNames[itemstack.getItemDamage()];
     }
     
     @SideOnly(Side.CLIENT)
     public void getSubItems(int par1, CreativeTabs tab, List subItems) {
         for (int i = 0; i < 15; i++) {
             subItems.add(new ItemStack(this, 1, i));
         }
     }
     
 }
