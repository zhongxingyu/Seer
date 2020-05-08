 package spazzysmod.item.armor;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 import spazzysmod.item.SpazzysItems;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @SuppressWarnings("deprecation")
 public class TitaniumArmor extends ItemArmor {
 
     public TitaniumArmor(int par1, EnumArmorMaterial par2EnumArmorMaterial,
             int par3, int par4) {
         super(par1, par2EnumArmorMaterial, par3, par4);
     }
 
     @Override
     public String getArmorTexture(ItemStack itemstack, Entity entity, int slot, int layer){
         if (itemstack.itemID == SpazzysItems.titaniumHelmet.itemID
                 || itemstack.itemID == SpazzysItems.titaniumChestplate.itemID
                 || itemstack.itemID == SpazzysItems.titaniumBoots.itemID)
             return "spazzysmod:/textures/armor/TITANIUMARMOR_1.png";
 
         if (itemstack.itemID == SpazzysItems.titaniumLeggings.itemID)
             return "spazzysmod:/textures/armor/TITANIUMARMOR_2.png";
         else
             return null;
 
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister) 
 {
         this.itemIcon = par1IconRegister.registerIcon("spazzysmod:"
                 + (this.getUnlocalizedName().substring(5)));
     }
 }
