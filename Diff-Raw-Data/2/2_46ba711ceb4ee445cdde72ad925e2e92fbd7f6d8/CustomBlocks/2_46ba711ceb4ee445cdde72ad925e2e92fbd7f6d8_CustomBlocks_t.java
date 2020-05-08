 package me.furt.industrial;
 
 import me.furt.industrial.block.BlockCopperOre;
 import me.furt.industrial.block.BlockMiningMachine;
 import me.furt.industrial.block.BlockMiningTube;
 import me.furt.industrial.block.BlockMiningTubeTip;
 import me.furt.industrial.block.BlockTinOre;
 
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.inventory.SpoutShapedRecipe;
 import org.getspout.spoutapi.material.MaterialData;
 
 import com.github.Zarklord1.FurnaceApi.FurnaceRecipes;
 
 public class CustomBlocks {
 	private IndustrialInc plugin;
 	public BlockMiningMachine miningMachine;
 	public BlockMiningTube miningTube;
 	public BlockMiningTubeTip miningTubeTip;
 	public BlockCopperOre copperOre;
 	public BlockTinOre tinOre;
 	
 	public CustomBlocks(IndustrialInc instance) {
 		this.plugin = instance;
 	}
 
 	public void init() {
 		copperOre = new BlockCopperOre(plugin);
 		tinOre = new BlockTinOre(plugin);
 		miningMachine = new BlockMiningMachine(plugin);
 		miningTubeTip = new BlockMiningTubeTip(plugin);
 		miningTube = new BlockMiningTube(plugin);
 		
 		//Crafting Recipes
 		ItemStack mmResult = new SpoutItemStack(miningMachine, 1);
 		SpoutShapedRecipe mmRecipe = new SpoutShapedRecipe(mmResult);
 		mmRecipe.shape("ACA", "BDB", "ADA");
 		mmRecipe.setIngredient('A', plugin.ci.temperedIronIngot);
 		mmRecipe.setIngredient('B', MaterialData.redstone);
 		mmRecipe.setIngredient('C', MaterialData.diamondPickaxe);
 		mmRecipe.setIngredient('D', miningTube);
 		
 		ItemStack mtResult = new SpoutItemStack(miningTube, 6);
 		SpoutShapedRecipe mtRecipe = new SpoutShapedRecipe(mtResult);
 		mtRecipe.shape(" A ", " A ", " A ");
 		mtRecipe.setIngredient('A', plugin.ci.temperedIronIngot);
 		
 		SpoutManager.getMaterialManager().registerSpoutRecipe(mmRecipe);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(mtRecipe);
 		
 		//Furnace Recipes
		FurnaceRecipes.CustomFurnaceRecipe(new SpoutItemStack(plugin.ci.copperIngot), 318, copperOre.getCustomId());
 		
 	}
 
 }
