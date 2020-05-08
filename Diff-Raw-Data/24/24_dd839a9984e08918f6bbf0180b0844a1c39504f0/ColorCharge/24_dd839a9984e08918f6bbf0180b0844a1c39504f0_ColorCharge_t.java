 package FuzzCraft.Items;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ColorCharge extends Item {
  
     public ColorCharge(int id) {
         super(id);
         this.setUnlocalizedName("colorcharge");
         this.setCreativeTab(CreativeTabs.tabMaterials);
         this.setMaxStackSize(16);
     }
 
    protected Icon[] icon = new Icon[15];
 
     @Override
     @SideOnly(Side.CLIENT)
    public void updateIcons(IconRegister par1IconRegister) {
         for (int i = 0; i < 15; i++) {
            icon[i] = par1IconRegister.registerIcon("FuzzCraft:charge_" + i);
         }
  }
     
    public Icon getIconFromDamage(int par1)
    {
        int j = MathHelper.clamp_int(par1, 0, 14);
        return this.icon[j];
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
