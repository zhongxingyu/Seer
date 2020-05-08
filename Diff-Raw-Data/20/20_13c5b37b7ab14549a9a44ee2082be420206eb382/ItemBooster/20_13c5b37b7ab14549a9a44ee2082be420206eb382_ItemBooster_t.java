 package Booster;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 
 public class ItemBooster extends ItemArmor {
 
 	public ItemBooster(int par1, EnumArmorMaterial par2EnumArmorMaterial, int par3, int par4) {
 		super(par1, par2EnumArmorMaterial, par3, par4);
 	}
     public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer)
 	{
 		String st;
		if(stack.getItem().itemID == Booster.Booster08.itemID)
		{
			System.out.print("check");
 			st = (layer == 1)? Booster.Armor08_1:Booster.Armor08_2;
		}
 		else
 			st = (layer == 1)? Booster.Armor20_1:Booster.Armor20_2;
     	return st;
 	}
 }
