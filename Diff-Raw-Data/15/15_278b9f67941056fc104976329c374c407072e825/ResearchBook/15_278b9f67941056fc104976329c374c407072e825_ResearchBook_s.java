 package mod.industrialscience.modules.research;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 
 public class ResearchBook extends Item {
 
 	public ResearchBook(int id) {
 		super(id);
 		setMaxStackSize(64);
 		setCreativeTab(CreativeTabs.tabMisc);
 		setUnlocalizedName("Research Book");
 		LanguageRegistry.addName(this, "Research Book");
 
 	}
 	public void func_94581_a(IconRegister iconRegister)
 	{
 	         iconIndex = iconRegister.func_94245_a("book");
 	}
 
 }
