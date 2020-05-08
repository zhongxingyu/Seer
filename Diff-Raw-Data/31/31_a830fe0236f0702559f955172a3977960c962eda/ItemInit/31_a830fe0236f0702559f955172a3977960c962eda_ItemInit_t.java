 package assets.tacotek.Init;
 
 import assets.tacotek.Items.*;
 import assets.tacotek.blocks.*;
 import assets.tacotek.common.tacotek;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ItemInit {
 
 	public ItemInit()
 	{
 		super();
 	}
 	
	public static Item Flour;
	public static Item Dough;
 	
 	public static void addItems()
 	{
 		//When using custom items use this class not assets.tacotek.Items
 		
 		//Flour
		Flour=new Flour(4000).setUnlocalizedName("flour");
		LanguageRegistry.addName(Flour,"Flour");
 		
 		//Dough
		Dough=new Dough(3999).setUnlocalizedName("dough");
		LanguageRegistry.addName(Dough,"Dough");
 			
 	}
 	
 }
