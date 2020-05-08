 package assets.tacotek.Items;
 
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.EnumHelper;
 import assets.tacotek.common.IDsHelper;
 import assets.tacotek.proxy.ServerProxy;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class ItemsHelper {
 	//Food Items
 	public static Item cheese;
 	public static Item taco;
 	public static Item toast;
 
 	//Tools (usable Items)
 	public static Item exchangeOMatic;
 	public static Item elect_end_chest;
 	public static Item torch_placer;
 	public static Item toaster;
 
 	//Generic Items
 	public static Item flour;
 	public static Item salt;
 	public static Item tortilla;
 	public static Item uncookedTortilla;
 	public static Item dough;
 	public static Item neutDust;
 	public static Item kProjLite;
 	public static Item kProj;
 	public static Item appleSauce;
 	public static Item applePie;
 	public static Item appleCider;
 	public static Item bagel;
 	public static Item bottleofMilk;
 	public static Item cheesyPotato;
 	public static Item chocolateBar;
 	public static Item sugarWater;
 	
 	//Armor Pieces
 	public static Item tux_head;
 	public static Item tux_chest;
 	public static Item tux_legs;
 	public static Item tux_boots;
 	public static Item shield_chest;
 
 	//Armor Types
 	public static EnumArmorMaterial TuxArmor = EnumHelper.addArmorMaterial("Tux", 66, new int[] {3,2,1,1}, 50);
 	public static EnumArmorMaterial ShieldArmor = EnumHelper.addArmorMaterial("Shield", 50, new int[] {0,0,0,0}, 5);
 
 	public static void setupItems(ServerProxy proxy) {
 		//Generic Items
 		salt = new GenericItem(IDsHelper.saltID, "salt");
 		dough = new GenericItem(IDsHelper.doughID, "dough");
 		flour = new GenericItem(IDsHelper.flourID, "flour");
 		tortilla = new GenericItem(IDsHelper.tortillaID, "tortilla");
 		uncookedTortilla = new GenericItem(IDsHelper.uncookedTortillaID, "uncookedtortilla");
		sugarWater = new GenericItem(IDsHelper.sugarWaterID, "sugarwater");
 		
 		//Advanced Armor Components
 		kProjLite = new GenericItemDescription(IDsHelper.kProjLiteID, "kprojlite", "A weak and unrefined shield projector.", 1);
 		kProj = new GenericItemDescription(IDsHelper.kProjID, "kproj", "Place holder until I come up with something cool. ~Sulljason", 1);
 		neutDust = new GenericItemDescription(IDsHelper.neutDustID, "neut", "Creates an electromagnetic field when current is applied.");
 
 		//Foods
 		taco = new GenericEdible(IDsHelper.tacoID, "taco", 8, true, 22, 300, 0, 1.0F);
 		cheese = new GenericEdible(IDsHelper.cheeseID, "cheese", 2, false);
 		toast = new GenericEdible(IDsHelper.toastID, "toast", 6, false);
 		appleSauce = new GenericEdible(IDsHelper.appleSauceID, "appleSauce", 4, false);
 		applePie = new GenericEdible(IDsHelper.applePieID, "applePie", 8, false);
 		appleCider = new GenericEdible(IDsHelper.appleCiderID, "appleCider", 4, false);
 		bagel = new GenericEdible(IDsHelper.bagelID, "bagel", 6, false);
 		bottleofMilk = new GenericEdible(IDsHelper.bottleofMilkID, "bottleofMilk", 4, false);
 		cheesyPotato = new GenericEdible(IDsHelper.cheesyPotatoID, "cheesyPotato", 8, false);
 		chocolateBar = new GenericEdible(IDsHelper.chocolateBarID, "chocolateBar", 3, false);
 		
 		//Tools
 		toaster = new Toaster(IDsHelper.toasterID, "toaster");
 		exchangeOMatic = new ExchangeOMatic(IDsHelper.exchangeOMaticID, "exchangeOMatic");
 		elect_end_chest = new ElectricEnderChest(IDsHelper.electEndChestID, "elect_end_chest");
 		torch_placer=new TorchPlacer(IDsHelper.torch_placerID, "torch_bag", "Uses the magic of enderstorage to hold many torches in one bag.", 1024, 1);
 
 		//Armor
 		int renderTuxArmor = proxy.addArmor("Tux");
 		tux_head = new TuxArmor(IDsHelper.tux_headID, TuxArmor, renderTuxArmor, 0, "tux_head");
 		tux_chest = new TuxArmor(IDsHelper.tux_chestID, TuxArmor, renderTuxArmor, 1, "tux_chest");
 		tux_legs = new TuxArmor(IDsHelper.tux_legsID, TuxArmor, renderTuxArmor, 2, "tux_legs");
 		tux_boots = new TuxArmor(IDsHelper.tux_bootsID, TuxArmor, renderTuxArmor, 3, "tux_boots");
 		int renderShieldArmor = proxy.addArmor("Shield");
 		shield_chest = new ShieldArmor(IDsHelper.shield_chestID, ShieldArmor, renderShieldArmor, 1, 100000, 2, 100, "shield_chest",200);
 
 		gameRegisters();
 		languageRegistry(proxy);
 	}
 
 	private static void gameRegisters() {
 		//Item Registry
 		GameRegistry.registerItem(cheese, "Cheese", null);
 		GameRegistry.registerItem(dough, "Dough", null);
 		GameRegistry.registerItem(exchangeOMatic, "ExchangeOMatic", null);
 		GameRegistry.registerItem(flour, "Flour", null);
 		GameRegistry.registerItem(salt, "Salt", null);
 		GameRegistry.registerItem(taco, "Taco", null);
 		GameRegistry.registerItem(toaster, "Toaster", null);
 		GameRegistry.registerItem(tortilla, "Tortilla", null);
 		GameRegistry.registerItem(uncookedTortilla, "Uncooked Tortilla", null);
 		GameRegistry.registerItem(toast, "Toast", null);
 		GameRegistry.registerItem(torch_placer, "Torch Bag", null);
 		GameRegistry.registerItem(elect_end_chest, "Handheld Enderchest", null);
 		GameRegistry.registerItem(appleSauce, "appleSauce", null);
 		GameRegistry.registerItem(applePie, "applePie", null);
 		GameRegistry.registerItem(appleCider, "appleCider", null);
 		GameRegistry.registerItem(bagel, "bagel", null);
 		GameRegistry.registerItem(bottleofMilk, "bottleofMilk", null);
 		GameRegistry.registerItem(cheesyPotato, "cheesyPotato", null);
 		GameRegistry.registerItem(chocolateBar, "chocolateBar", null);
 		GameRegistry.registerItem(sugarWater, "sugarWater", null);
 		
 		//Advanced Armor Components
 		GameRegistry.registerItem(kProjLite, "Shield Projector Prototype", null);
 		GameRegistry.registerItem(kProj, "Shield Projector", null);
 		GameRegistry.registerItem(neutDust, "Neutronium", null);
 
 		//Armor Items
 		GameRegistry.registerItem(tux_head, "tux_head", null);
 		GameRegistry.registerItem(tux_chest, "tux_chest", null);
 		GameRegistry.registerItem(tux_legs, "tux_legs", null);
 		GameRegistry.registerItem(tux_boots, "tux_boots", null);
 		GameRegistry.registerItem(shield_chest, "shield_chest", null);
 	}
 
 	private static void languageRegistry(ServerProxy proxy) {
 		//Items
 		proxy.addName(cheese, "Cheese");
 		proxy.addName(dough, "Dough");
 		proxy.addName(exchangeOMatic, "Exchange O Matic");
 		proxy.addName(flour, "Flour");
 		proxy.addName(salt, "Salt");
 		proxy.addName(taco, "Taco");
 		proxy.addName(toaster, "Toaster");
 		proxy.addName(tortilla, "Tortilla");
 		proxy.addName(uncookedTortilla, "Uncooked Tortilla");
 		proxy.addName(toast, "Toast");
 		proxy.addName(torch_placer, "Torch Bag");
 		proxy.addName(appleSauce, "Apple Sauce");
 		proxy.addName(applePie, "Apple Pie");
 		proxy.addName(appleCider, "Apple Cider");
 		proxy.addName(bagel, "Bagel");
 		proxy.addName(bottleofMilk, "Bottle of Milk");
 		proxy.addName(cheesyPotato, "Cheesy Potato");
 		proxy.addName(chocolateBar, "Chocolate Bar");
 		proxy.addName(sugarWater, "Sugar Water");
 		
 		//Advanced Armor Components
 		proxy.addName(neutDust, "Neutronium");
 		proxy.addName(kProjLite, "Shield Projector Prototype");
 		proxy.addName(kProj, "Shield Projector");
 
 		//Armor Items
 		proxy.addName(tux_head, "Fedora");
 		proxy.addName(tux_chest, "Tuxedo");
 		proxy.addName(tux_legs, "Dress Pants");
 		proxy.addName(tux_boots, "Dress Shoes");
 		proxy.addName(shield_chest, "Shield Armor");
 	}
 }
