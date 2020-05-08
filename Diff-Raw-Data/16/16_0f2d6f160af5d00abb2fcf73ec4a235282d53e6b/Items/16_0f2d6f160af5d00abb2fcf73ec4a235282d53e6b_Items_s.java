 package shadowhax.modjam.item;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.item.Item;
 
 public class Items {
 	
 	public static void init() {
 		
 		addItems();
		addNames();
 	}
	
	public static Item unlinkedCrystal;
 	public static Item rawCrystal;
 	
 	public static void addItems() {
 		
 		rawCrystal = new ItemRawCrystal(5331).setUnlocalizedName("rawCrystal");
		unlinkedCrystal = new ItemLinkingCrystal(5337).setUnlocalizedName("warping");
	}
	
	public static void addNames() {
		
 	}

 }
