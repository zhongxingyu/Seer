 package com.teammetallurgy.agriculture;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidRegistry;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 
 import com.teammetallurgy.agriculture.crops.BlockPeanut;
 import com.teammetallurgy.agriculture.crops.BlockSpice;
 import com.teammetallurgy.agriculture.crops.BlockStrawberry;
 import com.teammetallurgy.agriculture.machines.brewer.BlockBrewer;
 import com.teammetallurgy.agriculture.machines.brewer.TileEntityBrewer;
 import com.teammetallurgy.agriculture.machines.counter.BlockCounter;
 import com.teammetallurgy.agriculture.machines.counter.TileEntityCounter;
 import com.teammetallurgy.agriculture.machines.frier.BlockFrier;
 import com.teammetallurgy.agriculture.machines.frier.TileEntityFrier;
 import com.teammetallurgy.agriculture.machines.icebox.BlockIcebox;
 import com.teammetallurgy.agriculture.machines.icebox.TileEntityIcebox;
 import com.teammetallurgy.agriculture.machines.oven.BlockOven;
 import com.teammetallurgy.agriculture.machines.oven.TileEntityOven;
 import com.teammetallurgy.agriculture.machines.processor.BlockProcessor;
 import com.teammetallurgy.agriculture.machines.processor.TileEntityProcessor;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class AgricultureBlocks
 {
 	public static Block oven;
 	public static Block counter;
 	public static Block processor;
 	public static Block brewer;
 	public static Block icebox;
 	public static Block frier;
 
 	public static Block salt;
 	public static Block cinnamon;
 	public static Block vanilla;
 	public static Block peanut;
 	public static Block strawberry;
 
 	public static void addNames()
 	{
 		LanguageRegistry.addName(oven, "Oven");
 		LanguageRegistry.addName(counter, "Counter");
 		LanguageRegistry.addName(processor, "Processor");
 		LanguageRegistry.addName(brewer, "Brewer");
 		LanguageRegistry.addName(cinnamon, "Cinnamon Plant");
 		LanguageRegistry.addName(vanilla, "Vanilla Plant");
 		LanguageRegistry.addName(salt, "Salt Ore");
		LanguageRegistry.addName(icebox, "Ice Box");
		LanguageRegistry.addName(frier, "Frier");
 	}
 
 	public static void init()
 	{
 		oven = new BlockOven(3000, Material.wood).setUnlocalizedName("agriculture:oven").setCreativeTab(Agriculture.tab);
 		counter = new BlockCounter(3001, Material.wood).setUnlocalizedName("agriculture:counter").setCreativeTab(Agriculture.tab);
 		processor = new BlockProcessor(3002, Material.wood).setUnlocalizedName("agriculture:processor").setCreativeTab(Agriculture.tab);
 		salt = new BlockSalt(3003, Material.rock).setUnlocalizedName("agriculture:salt").func_111022_d("agriculture:Salt").setCreativeTab(Agriculture.tab);
 		cinnamon = new BlockSpice(3004).setDrop(AgricultureItems.cinnamon.getItemStack()).setHardness(1.0f).setUnlocalizedName("agriculture:cinnamon").func_111022_d("agriculture:cinnamon").setCreativeTab(Agriculture.tab);
 		vanilla = new BlockSpice(3008).setDrop(AgricultureItems.vanilla.getItemStack()).setHardness(1.0f).setUnlocalizedName("agriculture:vanilla").func_111022_d("agriculture:vanilla").setCreativeTab(Agriculture.tab);
 		peanut = new BlockPeanut(3009).setHardness(0.1f).setUnlocalizedName("agriculture:peanut").func_111022_d("agriculture:peanut").setCreativeTab(Agriculture.tab);
 		strawberry = new BlockStrawberry(3010).setHardness(0.1f).setUnlocalizedName("agriculture:strawberry").func_111022_d("agriculture:strawberry").setCreativeTab(Agriculture.tab);
 
 		brewer = new BlockBrewer(3005, Material.wood).setUnlocalizedName("agriculture:brewer").setCreativeTab(Agriculture.tab);
 		icebox = new BlockIcebox(3006, Material.wood).setUnlocalizedName("agriculture:icebox").setCreativeTab(Agriculture.tab);
 		frier = new BlockFrier(3007, Material.wood).setUnlocalizedName("agriculture:frier").setCreativeTab(Agriculture.tab);
 
 		FluidRegistry.registerFluid(new Fluid("milk"));
 		FluidRegistry.registerFluid(new Fluid("beer"));
 		FluidRegistry.registerFluid(new Fluid("hotcocoa"));
 		FluidRegistry.registerFluid(new Fluid("vinegar"));
 		FluidRegistry.registerFluid(new Fluid("cookingOil"));
 		FluidRegistry.registerFluid(new Fluid("vodka"));
 		FluidRegistry.registerFluid(new Fluid("cider"));
 
 		GameRegistry.registerBlock(oven, "AgricultureOvenBlock");
 		GameRegistry.registerBlock(counter, "AgricultureCounterBlock");
 		GameRegistry.registerBlock(processor, "AgricultureProcessorBlock");
 		GameRegistry.registerBlock(salt, "AgricultureSalt");
 		GameRegistry.registerBlock(cinnamon, "AgricultureCinnamon");
 		GameRegistry.registerBlock(vanilla, "AgricultureVanilla");
 		GameRegistry.registerBlock(brewer, "AgricultureBrewerBlock");
 		GameRegistry.registerBlock(icebox, "AgricultureIcebox");
 		GameRegistry.registerBlock(frier, "AgricultureFrier");
 		GameRegistry.registerTileEntity(TileEntityOven.class, "AgricultureOvenTileEntity");
 		GameRegistry.registerTileEntity(TileEntityCounter.class, "AgricultureCounterTileEntity");
 		GameRegistry.registerTileEntity(TileEntityProcessor.class, "AgricultureProcessorTileEntity");
 		GameRegistry.registerTileEntity(TileEntityBrewer.class, "AgricultureBrewerTileEntity");
 		GameRegistry.registerTileEntity(TileEntityIcebox.class, "AgricultureIceboxTileEntity");
 		GameRegistry.registerTileEntity(TileEntityFrier.class, "AgricultureFrierTileEntity");
 
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(oven), "WWW", "BFB", "BBB", 'W', "plankWood", 'F', Block.furnaceIdle, 'B', Item.brick));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(brewer), "BBB", "BGB", "BIB", 'G', Block.glass, 'I', Item.ingotIron, 'B', Item.brick));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(counter), "WWW", "BCB", "BBB", 'W', "plankWood", 'C', Block.chest, 'B', Item.brick));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(processor), "WIW", "BFB", "BBB", 'W', "plankWood", 'I', Item.ingotIron, 'F', Block.furnaceIdle, 'B', Item.brick));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(icebox), "BIB", "BBB", "BIB", 'I', Item.ingotIron, 'B', Item.brick));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(frier), "BIB", "BGB", "BIB", 'I', Item.ingotIron, 'G', Block.glass, 'B', Item.brick));
 
 		addNames();
 	}
 }
