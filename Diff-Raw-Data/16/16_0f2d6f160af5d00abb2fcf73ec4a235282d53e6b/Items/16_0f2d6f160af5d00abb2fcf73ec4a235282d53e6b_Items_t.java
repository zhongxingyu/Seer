 package shadowhax.modjam.item;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.item.Item;
 
 public class Items {
 	
 	public static void init() {
 		
 		addItems();
 	}

 	public static Item rawCrystal;
	public static Item warpCrystal;
 	
 	public static void addItems() {
 		
 		rawCrystal = new ItemRawCrystal(5331).setUnlocalizedName("rawCrystal");
		warpCrystal = new ItemLinkingCrystal(5337).setUnlocalizedName("warping");
 	}
 }
