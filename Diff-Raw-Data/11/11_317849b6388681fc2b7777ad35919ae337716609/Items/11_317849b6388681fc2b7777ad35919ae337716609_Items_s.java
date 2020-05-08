 package assets.tacotek.Items;
 
 import assets.tacotek.Items.*;
 import assets.tacotek.blocks.*;
 import assets.tacotek.common.tacotek;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class Items {
 
 	public Items()
 	{
 		super();
 	}
 	
 	public static Item Flour;
 	public static Item Dough;
 	public static Item Salt;
 	public static Item Cheese;
 	public static Item UncookedTortilla;
 	public static Item Tortilla;
 	public static Item Taco;
 	
 	public static void addItems()
 	{
 		//When using custom items use this class not assets.tacotek.Items
 		
 		//Flour
 		Flour=new Flour(4000).setUnlocalizedName("flour");
 		LanguageRegistry.addName(Flour, "Flour");
 		
 		//Dough
 		Dough=new Dough(4001).setUnlocalizedName("dough");
 		LanguageRegistry.addName(Dough, "Dough");
 		
 		//Salt
 		Salt=new Salt(4002).setUnlocalizedName("salt");
 		LanguageRegistry.addName(Salt, "Salt");
 		
 		//Cheese
 		Cheese=new Cheese(4003).setUnlocalizedName("cheese");
 		LanguageRegistry.addName(Cheese, "Cheese");
 		
 		//UncookedTortilla
 		UncookedTortilla=new UncookedTortilla(4004).setUnlocalizedName("uncookedtortilla");
 		LanguageRegistry.addName(UncookedTortilla, "Uncooked Tortilla");
 		
 		//Tortilla
		Tortilla=new Tortilla(4003).setUnlocalizedName("tortilla");
 		LanguageRegistry.addName(Tortilla, "Tortilla");
 		
 		//Taco
		Taco=new Taco(4002).setUnlocalizedName("taco");
 		LanguageRegistry.addName(Taco, "Taco");
 		
 	}
 	
 }
