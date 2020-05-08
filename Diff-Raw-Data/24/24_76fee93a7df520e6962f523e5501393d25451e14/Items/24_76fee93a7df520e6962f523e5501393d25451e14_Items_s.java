 package shadowhax.modjam.item;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.item.Item;
 
 public class Items {
 	
 	public static void init() {
 		
 		addItems();
 		addNames();
 	}
 	
 	public static Item unlinkedCrystal;
 	
 	public static void addItems() {
 		
 		unlinkedCrystal = new ItemLinkingCrystal(5337).setUnlocalizedName("notlinked");
 	}
 	
 	public static void addNames() {
 		
		LanguageRegistry.addName(unlinkedCrystal, "Unlinked Crystal");
 	}
 
 }
