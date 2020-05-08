 package mods.mod_nw;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class Recipes {
 	public static void init() {
 		/** Shaped recipes **/
 		basePlankRecipe(NWBlock.customTreePlank);
 		woodenToolsRecipe(NWBlock.customTreePlank, Item.stick, Item.swordWood, Item.pickaxeWood, Item.shovelWood, Item.axeWood, Item.hoeWood);
 		//You can use this simple preset to create new armor/tools sets - (Material, Sword, Pickaxe, Shovel, Axe, Hoe, Helmet, Chestplate, Leggins, Boots).
 		//Simple Example:
 		baseEquipmentRecipe(NordWest.cingotItem, Item.swordSteel, Item.pickaxeSteel, Item.shovelSteel, Item.axeSteel, Item.hoeSteel, null, null, null, null);
 		//Example ends here! This is really easy to understand, isn't it?
 		addRecipe(new ItemStack(NWBlock.blockmithri, 1), new Object[] { "###", "###", "###", '#', NordWest.mingotItem });
 		addRecipe(new ItemStack(NWBlock.coperblock, 1), new Object[] { "###", "###", "###", '#', NordWest.cingotItem });
 		/** Shapeless recipes **/
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.expBottle, 1), NordWest.lexpiItem, Item.glassBottle);
 		GameRegistry.addShapelessRecipe(new ItemStack(NordWest.cingotItem, 9), new ItemStack(NWBlock.coperblock));
 		GameRegistry.addShapelessRecipe(new ItemStack(NordWest.mingotItem, 9), new ItemStack(NWBlock.blockmithri));
 		for (int i = 0; i < 4; i++) {
 			GameRegistry.addShapelessRecipe(new ItemStack(NWBlock.customTreePlank, 4, i), new ItemStack(
 					NWBlock.customTreeWood, 1, i));
 
 		}
 
 	}
 
 	private static void basePlankRecipe(Block plank) {
 
 		addRecipe(new ItemStack(Item.bed, 1), new Object[] { "###", "XXX", '#', Block.cloth, 'X', plank });
 		addRecipe(new ItemStack(Block.fenceGate, 1), new Object[] { "#W#", "#W#", '#', Item.stick, 'W', plank });
 		addRecipe(new ItemStack(Item.doorWood, 1), new Object[] { "##", "##", "##", '#', plank });
 		addRecipe(new ItemStack(Block.trapdoor, 2), new Object[] { "###", "###", '#', plank });
 		addRecipe(new ItemStack(Item.sign, 3), new Object[] { "###", "###", " X ", '#', plank, 'X', Item.stick });
 		addRecipe(new ItemStack(Item.bowlEmpty, 4), new Object[] { "# #", " # ", '#', plank });
 		addRecipe(new ItemStack(Item.boat, 1), new Object[] { "# #", "###", '#', plank });
 		addRecipe(new ItemStack(Block.tripWireSource, 2), new Object[] { "I", "S", "#", '#', plank, 'S', Item.stick,
 				'I', Item.ingotIron });
		addRecipe(new ItemStack(Block.workbench, 1), new Object[] { "##", "##", '#', plank });
 		addRecipe(new ItemStack(Block.woodenButton, 1), new Object[] { "#", '#', plank });
 		addRecipe(new ItemStack(Block.pressurePlatePlanks, 1), new Object[] { "##", '#', plank });
 		addRecipe(new ItemStack(Block.jukebox, 1), new Object[] { "###", "#X#", "###", '#', plank, 'X', Item.diamond });
 		addRecipe(new ItemStack(Block.music, 1), new Object[] { "###", "#X#", "###", '#', plank, 'X', Item.redstone });
 		addRecipe(new ItemStack(Block.bookShelf, 1), new Object[] { "###", "XXX", "###", '#', plank, 'X', Item.book });
 		addRecipe(new ItemStack(Block.chest), new Object[] { "###", "# #", "###", '#', plank });
 		addRecipe(new ItemStack(Item.stick, 4), new Object[] { "#", "#", '#', plank });
 	}
 	
 	private static void baseEquipmentRecipe(Item material, Item sword, Item pickaxe, Item shovel, Item axe, Item hoe, Item helmet, Item chestplate, Item legs, Item boots) {
 		if(pickaxe != null) {
 		addRecipe(new ItemStack(sword, 1), new Object[] { "X", "X", "#", '#', Item.stick, 'X', material });
 		addRecipe(new ItemStack(pickaxe, 1), new Object[] { "XXX", " # ", " # ", '#', Item.stick, 'X', material });
 		addRecipe(new ItemStack(shovel, 1), new Object[] { "X", "#", "#", '#', Item.stick, 'X', material });
 		addRecipe(new ItemStack(axe, 1), new Object[] { "XX", "X#", " #", '#', Item.stick, 'X', material });
 		addRecipe(new ItemStack(hoe, 1), new Object[] { "XX", " #", " #", '#', Item.stick, 'X', material });
 		}
		if(chestplate != null) {
 		addRecipe(new ItemStack(helmet, 1), new Object[] { "XXX", "X X", 'X', material });
 		addRecipe(new ItemStack(chestplate, 1), new Object[] { "X X", "XXX", "XXX", 'X', material });
 		addRecipe(new ItemStack(legs, 1), new Object[] { "XXX", "X X", "X X", 'X', material });
 		addRecipe(new ItemStack(boots, 1), new Object[] { "X X", "X X", 'X', material });
		}
 	}
 	
 	private static void woodenToolsRecipe(Block material, Item stick, Item sword, Item pickaxe, Item shovel, Item axe, Item hoe) {
 		addRecipe(new ItemStack(sword, 1), new Object[] { "X", "X", "#", '#', stick, 'X', material });
 		addRecipe(new ItemStack(pickaxe, 1), new Object[] { "XXX", " # ", " # ", '#', stick, 'X', material });
 		addRecipe(new ItemStack(shovel, 1), new Object[] { "X", "#", "#", '#', stick, 'X', material });
 		addRecipe(new ItemStack(axe, 1), new Object[] { "XX", "X#", " #", '#', stick, 'X', material });
 		addRecipe(new ItemStack(hoe, 1), new Object[] { "XX", " #", " #", '#', stick, 'X', material });
 	}
 
 	private static void addRecipe(ItemStack output, Object... params) {
 		GameRegistry.addRecipe(output, params);
 	}
 	
 
 	
}
