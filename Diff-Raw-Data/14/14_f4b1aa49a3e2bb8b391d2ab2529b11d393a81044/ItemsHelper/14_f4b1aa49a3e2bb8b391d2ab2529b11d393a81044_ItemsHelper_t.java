 package assets.tacotek.Items;
 
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.src.ModLoader;
 import net.minecraftforge.common.EnumHelper;
 import assets.tacotek.common.IDsHelper;
 import assets.tacotek.common.tacotek;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ItemsHelper {
 	//Items
 	public static Item cheese;
 	public static Item dough;
 	public static Item flour;
 	public static Item salt;
 	public static Item taco;
 	public static Item toaster;
 	public static Item tortilla;
 	public static Item uncookedTortilla;
 	
 	//Armor
 	public static Item tux_head;
 	public static Item tux_chest;
 	public static Item tux_legs;
 	public static Item tux_boots;
 	
	public static EnumArmorMaterial TuxArmor = EnumHelper.addArmorMaterial("Tux", 66, new int[] {3,3,1,1}, 50);
 	
 	public static void setupItems() {
 		//Item Loading
 		cheese = new Cheese(IDsHelper.cheeseID).setUnlocalizedName("cheese").setCreativeTab(tacotek.tacotekTab);
 		dough = new Dough(IDsHelper.doughID).setUnlocalizedName("dough").setCreativeTab(tacotek.tacotekTab);
 		flour = new Flour(IDsHelper.flourID).setUnlocalizedName("flour").setCreativeTab(tacotek.tacotekTab);
 		salt = new Salt(IDsHelper.saltID).setUnlocalizedName("salt").setCreativeTab(tacotek.tacotekTab);
 		taco = new Taco(IDsHelper.tacoID).setUnlocalizedName("taco").setCreativeTab(tacotek.tacotekTab);
		toaster = new Toaster(IDsHelper.toasterID).setUnlocalizedName("toaster").setCreativeTab(tacotek.tacotekTab);
 		tortilla = new Tortilla(IDsHelper.tortillaID).setUnlocalizedName("tortilla").setCreativeTab(tacotek.tacotekTab);
 		uncookedTortilla = new UncookedTortilla(IDsHelper.uncookedTortillaID).setUnlocalizedName("uncookedtortilla").setCreativeTab(tacotek.tacotekTab);
 		
 		//Armor Loading
		tux_head = new TuxArmor(IDsHelper.tux_headID, TuxArmor, ModLoader.addArmor("Tux"), 0).setUnlocalizedName("tux_head").setCreativeTab(tacotek.tacotekTab);
		tux_chest = new TuxArmor(IDsHelper.tux_chestID, TuxArmor, ModLoader.addArmor("Tux"), 1).setUnlocalizedName("tux_chest").setCreativeTab(tacotek.tacotekTab);
		tux_legs = new TuxArmor(IDsHelper.tux_legsID, TuxArmor, ModLoader.addArmor("Tux"), 2).setUnlocalizedName("tux_legs").setCreativeTab(tacotek.tacotekTab);
		tux_boots = new TuxArmor(IDsHelper.tux_bootsID, TuxArmor, ModLoader.addArmor("Tux"), 3).setUnlocalizedName("tux_boots").setCreativeTab(tacotek.tacotekTab);
 		
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
 		GameRegistry.registerItem(toaster, "Toaster", null);
 		GameRegistry.registerItem(tortilla, "Tortilla", null);
 		GameRegistry.registerItem(uncookedTortilla, "Uncooked Tortilla", null);
 	}
 
     private static void languageRegistry() {
 		LanguageRegistry.addName(cheese, "Cheese");
 		LanguageRegistry.addName(dough, "Dough");
     	LanguageRegistry.addName(flour, "Flour");
 		LanguageRegistry.addName(salt, "Salt");
 		LanguageRegistry.addName(taco, "Taco");
 		LanguageRegistry.addName(toaster, "Toaster");
 		LanguageRegistry.addName(tortilla, "Tortilla");
 		LanguageRegistry.addName(uncookedTortilla, "Uncooked Tortilla");
     }
 	
 	private static void oreDictionary() {
 		//OreDictionary.registerOre("ingotLead", leadIngot);
 	}
 }
