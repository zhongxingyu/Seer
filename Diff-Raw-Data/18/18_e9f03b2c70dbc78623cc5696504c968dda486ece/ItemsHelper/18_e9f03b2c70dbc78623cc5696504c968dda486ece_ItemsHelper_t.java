 package assets.tacotek.Items;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import assets.tacotek.common.IDsHelper;
 import assets.tacotek.common.tacotek;
 import net.minecraft.item.Item;
 
 public class ItemsHelper {
 	//Items
 	public static Item cheese;
 	public static Item dough;
 	public static Item flour;
 	public static Item salt;
 	public static Item taco;
 	public static Item tortilla;
 	public static Item uncookedTortilla;
 	
 	public static void setupItems() {
 		//Item Loading
 		cheese = new Cheese(IDsHelper.cheeseID).setUnlocalizedName("cheese").setCreativeTab(tacotek.tacotekTab);
 		dough = new Dough(IDsHelper.doughID).setUnlocalizedName("dough").setCreativeTab(tacotek.tacotekTab);
 		flour = new Flour(IDsHelper.flourID).setUnlocalizedName("flour").setCreativeTab(tacotek.tacotekTab);
 		salt = new Salt(IDsHelper.saltID).setUnlocalizedName("salt").setCreativeTab(tacotek.tacotekTab);
 		taco = new Taco(IDsHelper.tacoID).setUnlocalizedName("taco").setCreativeTab(tacotek.tacotekTab);
 		tortilla = new Tortilla(IDsHelper.tortillaID).setUnlocalizedName("tortilla").setCreativeTab(tacotek.tacotekTab);
 		uncookedTortilla = new UncookedTortilla(IDsHelper.uncookedTortillaID).setUnlocalizedName("uncookedtortilla").setCreativeTab(tacotek.tacotekTab);
 		
 		gameRegisters();
         languageRegistry();
         oreDictionary();
 	}
 	
 	private static void gameRegisters() {
 		//Item Registry
 		GameRegistry.registerItem(cheese, "Cheese", null);
 		GameRegistry.registerItem(dough, "Dough", null);
 		GameRegistry.registerItem(flour, "Flour", null);
 		GameRegistry.registerItem(salt, "Salt", null);
 		GameRegistry.registerItem(taco, "Taco", null);
 		GameRegistry.registerItem(tortilla, "Tortilla", null);
 		GameRegistry.registerItem(uncookedTortilla, "Uncooked Tortilla", null);
 	}
 
    private static void languageRegistry() {
     	LanguageRegistry.addName(flour, "Flour");
 		LanguageRegistry.addName(dough, "Dough");
 		LanguageRegistry.addName(salt, "Salt");
 		LanguageRegistry.addName(cheese, "Cheese");
 		LanguageRegistry.addName(uncookedTortilla, "Uncooked Tortilla");
 		LanguageRegistry.addName(tortilla, "Tortilla");
 		LanguageRegistry.addName(taco, "Taco");
     }
 	
 	private static void oreDictionary() {
 		//OreDictionary.registerOre("ingotLead", leadIngot);
 	}
 }
