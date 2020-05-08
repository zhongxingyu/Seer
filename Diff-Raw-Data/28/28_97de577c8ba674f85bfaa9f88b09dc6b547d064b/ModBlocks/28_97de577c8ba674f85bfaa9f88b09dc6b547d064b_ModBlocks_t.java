 package makmods.levelstorage;
 
 import ic2.api.item.Items;
import ic2.api.recipe.Recipes;
 import makmods.levelstorage.block.BlockWirelessConductor;
 import makmods.levelstorage.block.BlockXpCharger;
 import makmods.levelstorage.block.BlockXpGenerator;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ModBlocks {
 	public static final ModBlocks instance = new ModBlocks();
 
 	public BlockXpGenerator blockXpGen;
 	public BlockXpCharger blockXpCharger;
 	public BlockWirelessConductor blockWlessConductor;
 
 	private ModBlocks() {
 	}
 
 	private void initBlocks() {
 		this.blockXpGen = new BlockXpGenerator(
 				BlockItemIds.instance.getIdFor("blockXpGenId"));
 		this.blockXpCharger = new BlockXpCharger(
 				BlockItemIds.instance.getIdFor("blockXpChargerId"));
 		this.blockWlessConductor = new BlockWirelessConductor(
 				BlockItemIds.instance.getIdFor("blockWirelessConductor"));
 	}
 
 	private void registerBlocks() {
 		GameRegistry.registerBlock(this.blockXpGen, "tile.blockXpGen");
 		GameRegistry.registerBlock(this.blockXpCharger, "tile.blockXpCharger");
 		GameRegistry.registerBlock(this.blockWlessConductor,
 				"tile.blockWirelessConductor");
 
 	}
 
 	private void addBlockNames() {
 		LanguageRegistry.instance().addName(this.blockXpCharger, "XP Charger");
 		LanguageRegistry.instance().addName(this.blockWlessConductor,
 				"Wireless Conductor");
 		LanguageRegistry.instance().addName(this.blockXpGen, "XP Generator");

 	}
 
 	private void addRecipes() {
 		ItemStack blockXpGenStack = new ItemStack(this.blockXpGen);
 		ItemStack blockXpChargerStack = new ItemStack(this.blockXpCharger);
 		ItemStack advMachine = Items.getItem("advancedMachine");
 		ItemStack machine = Items.getItem("machine");
 		ItemStack generator = Items.getItem("generator");
 		ItemStack refIron = Items.getItem("refinedIronIngot");
 		ItemStack goldIngot = new ItemStack(Item.ingotGold);
 		GameRegistry.addRecipe(blockXpGenStack, "iai", "geg", "imi",
 				Character.valueOf('i'), refIron, Character.valueOf('g'),
 				goldIngot, Character.valueOf('a'), advMachine,
 				Character.valueOf('m'), machine, Character.valueOf('e'),
 				generator);
 		GameRegistry.addRecipe(blockXpChargerStack, "iai", "geg", "imi",
 				Character.valueOf('i'), refIron, Character.valueOf('g'),
 				goldIngot, Character.valueOf('a'), advMachine,
 				Character.valueOf('m'), machine, Character.valueOf('e'),
 				blockXpGenStack);
		ItemStack frequencyTr = Items.getItem("frequencyTransmitter");
		ItemStack transformerHv = Items.getItem("hvTransformer");
		ItemStack enderPearl = new ItemStack(Item.enderPearl);
		ItemStack advCircuit = Items.getItem("advancedCircuit");

		Recipes.advRecipes.addRecipe(new ItemStack(this.blockWlessConductor),
				"tmt", "cec", "chc", Character.valueOf('t'), frequencyTr,
				Character.valueOf('e'), enderPearl, Character.valueOf('c'),
				advCircuit, Character.valueOf('h'), transformerHv,
				Character.valueOf('m'), advMachine);

 	}
 
 	private void setMiningLevels() {
 		MinecraftForge.setBlockHarvestLevel(this.blockXpGen, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(this.blockXpCharger, "pickaxe", 1);
 		MinecraftForge.setBlockHarvestLevel(this.blockWlessConductor,
 				"pickaxe", 1);
 
 	}
 
 	public void init() {
 		this.initBlocks();
 		this.registerBlocks();
 		this.addBlockNames();
 		this.addRecipes();
 		this.setMiningLevels();
 	}
 }
