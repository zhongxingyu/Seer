 package makmods.levelstorage;
 
 import ic2.api.item.Items;
 import ic2.api.recipe.Recipes;
 
 import java.lang.reflect.Field;
 import java.util.Random;
 
 import makmods.levelstorage.item.ItemAdvancedScanner;
 import makmods.levelstorage.item.ItemFrequencyCard;
 import makmods.levelstorage.item.ItemLevelStorageBook;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ModItems {
 	public static final ModItems instance = new ModItems();
 
 	public static ItemLevelStorageBook itemLevelStorageBook;
 	public static ItemAdvancedScanner itemAdvScanner;
 	public static ItemFrequencyCard itemFreqCard;
 
 	private int incr = 0;
 
 	private ModItems() {
 	}
 
 	private void initItems() {
 		itemLevelStorageBook = new ItemLevelStorageBook(
 				BlockItemIds.instance.getIdFor("itemXPBookId"),
 				LevelStorage.itemLevelStorageBookSpace);
 		itemAdvScanner = new ItemAdvancedScanner(
 				BlockItemIds.instance.getIdFor("itemAdvScannerId"));
 		itemFreqCard = new ItemFrequencyCard(
 				BlockItemIds.instance.getIdFor("itemFrequencyCard"));
 	}
 
 	private void addRecipes() {
 		// Book
 		ItemStack stackDepleted = new ItemStack(itemLevelStorageBook, 1, 0);
 		stackDepleted.stackTagCompound = new NBTTagCompound();
 		ItemStack stackBook = new ItemStack(Item.book);
 		ItemStack stackGoldBlock = new ItemStack(Block.blockGold);
 		ItemStack stackEnchTable = new ItemStack(Block.enchantmentTable);
 		GameRegistry.addShapelessRecipe(stackDepleted, stackBook,
 				stackGoldBlock, stackEnchTable);
 		// Scanner
 		ItemStack ovScanner = Items.getItem("ovScanner");
 		ItemStack uum = Items.getItem("matter");
 		ItemStack energyCrystal = Items.getItem("energyCrystal");
 		ItemStack advCircuit = Items.getItem("advancedCircuit");
 		ItemStack glassFiber = Items.getItem("glassFiberCableItem");
 		ItemStack advScanner = new ItemStack(ModItems.itemAdvScanner);
 		Recipes.advRecipes.addRecipe(advScanner, "ucu", "asa", "ggg",
 				Character.valueOf('u'), uum, Character.valueOf('g'),
 				glassFiber, Character.valueOf('a'), advCircuit,
 				Character.valueOf('c'), energyCrystal, Character.valueOf('s'),
 				ovScanner);
 
 		// Frequency card
		ItemStack frequencyTr = Items.getItem("frequencyTransmitter");
		Recipes.advRecipes.addShapelessRecipe(new ItemStack(
				this.itemFreqCard), frequencyTr, new ItemStack(Item.paper));
 		// To get rid of card data
 		Recipes.advRecipes.addShapelessRecipe(new ItemStack(
				ModItems.itemFreqCard), new ItemStack(this.itemFreqCard));
 		CraftingManager.getInstance().getRecipeList()
 				.add(new ExperienceRecipe());
 	}
 
 	private void addCustomFeatures() {
 		for (Field f : ChestGenHooks.class.getDeclaredFields()) {
 			try {
 				String category = (String) f.get(null);
 
 				Random rnd = new Random();
 				rnd.setSeed(rnd.nextInt() + this.incr);
 
 				ItemStack bookStack = new ItemStack(
 						ModItems.itemLevelStorageBook);
 				bookStack.stackTagCompound = new NBTTagCompound();
 				bookStack.stackTagCompound.setInteger(
 						ItemLevelStorageBook.STORED_XP_NBT,
 						rnd.nextInt(LevelStorage.itemLevelStorageBookSpace));
 				bookStack.setItemDamage(ItemLevelStorageBook
 						.calculateDurability(bookStack));
 				ChestGenHooks.addItem(category, new WeightedRandomChestContent(
 						bookStack, 0, 1, 10));
 				this.incr++;
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	private void addItemNames() {
 		LanguageRegistry.addName(itemLevelStorageBook, "XP Tome");
 		LanguageRegistry.addName(itemAdvScanner, "Advanced OV-Scanner");
 		LanguageRegistry.addName(itemFreqCard, "Frequency Card");
 	}
 
 	public void init() {
 		this.initItems();
 		this.addRecipes();
 		this.addItemNames();
 		this.addCustomFeatures();
 	}
 }
