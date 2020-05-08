 package Fly_Craft;
 
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 
 public class ItemIngotSteel extends Item 
 
 {
 	
 	public ItemIngotSteel(int par1) 
 	{
 		super(par1);
		this.setCreativeTab(CreativeTabs.tabMaterials);
 	}
 	
	@Override
	public void updateIcons(IconRegister iconRegister)
 	{
	iconIndex = iconRegister.registerIcon("JJMACCA_FlyCraft:IngotSteel");
 	}
 
 }
