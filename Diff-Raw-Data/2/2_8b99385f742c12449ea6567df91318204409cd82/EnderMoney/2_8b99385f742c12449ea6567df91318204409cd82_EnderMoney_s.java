 package com.github.soniex2.endermoney.core;
 
 import java.io.File;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 
 import com.github.soniex2.endermoney.core.block.LiquidCoin;
 import com.github.soniex2.endermoney.core.block.Ore;
 import com.github.soniex2.endermoney.core.fluid.FluidEnderCoin;
 import com.github.soniex2.endermoney.core.item.EnderCoin;
 import com.github.soniex2.endermoney.core.item.EnderItem;
 import com.github.soniex2.endermoney.core.item.EnderItem.EnderSubItem;
 import com.github.soniex2.endermoney.core.item.GenericItem;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 // @SuppressWarnings("unused")
 @Mod(modid = "EnderMoneyCore", name = "EnderMoney Core", version = Version.MOD_VERSION,
 		dependencies = "required-after:Forge")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class EnderMoney {
 
 	public static final CreativeTabs tab = new CreativeTabs("EnderMoney") {
 		@Override
 		public ItemStack getIconItemStack() {
 			return ((EnderCoin) coin).getItemStack(10000000L);
 		}
 	};
 	public static EnderItem enderItem;
 	public static Item coin;
 	public static Block ore;
 	public static EnderSubItem ender;
 	public static EnderSubItem ironDust;
 
 	@Instance("EnderMoneyCore")
 	public static EnderMoney instance;
 
 	@SidedProxy(clientSide = "com.github.soniex2.endermoney.core.ClientProxy",
 			serverSide = "com.github.soniex2.endermoney.core.CommonProxy")
 	public static CommonProxy proxy;
 	public static FluidEnderCoin fluidEC;
 	public static LiquidCoin blockLiqEC;
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 		File configDir = new File(event.getSuggestedConfigurationFile().getParentFile(),
 				"EnderMoney/Core.cfg");
 		Configuration config = new Configuration(configDir);
 		config.load();
 		Property oreID = config.getBlock("ore", 500, "Ore Block ID");
 		Property liqECID = config.getBlock("coin.liquid", 502, "Liquid Money Block ID");
 		Property coinID = config.getItem("coin", 27000, "EnderCoin Item ID");
 		Property itemID = config.getItem("item", 27001, "EnderItem Item ID");
 		Property craftable = config.get(Configuration.CATEGORY_GENERAL, "coin.craftable", true,
 				"Set to false to disable coin crafting");
 		config.save();
 
 		enderItem = EnderItem.instance = new EnderItem(itemID.getInt(27001));
 		coin = new EnderCoin(coinID.getInt(27000));
 		ore = new Ore(oreID.getInt(500));
 		ender = new GenericItem(0, "dustEnder", "endermoneycore:dust", 0x228866, true);
 		ironDust = new GenericItem(1, "dustIron", "endermoneycore:dust", 0xDDDDDD);
 		fluidEC = new FluidEnderCoin();
 		blockLiqEC = new LiquidCoin(liqECID.getInt(502), fluidEC);
 
 		GameRegistry.registerBlock(ore, Ore.Item.class, "endermoneycore.ore");
		GameRegistry.registerBlock(blockLiqEC, Ore.Item.class, "endermoneycore.liquidMoney");
 
 		OreDictionary.registerOre("dustEnder", ender.getItemStack());
 		OreDictionary.registerOre("dustIron", ironDust.getItemStack());
 		OreDictionary.registerOre("oreEnderDust", new ItemStack(ore, 1, 1));
 
 		LanguageRegistry langRegistry = LanguageRegistry.instance();
 		langRegistry.addStringLocalization("item.endercoin.name", "EnderCoin");
 		LanguageRegistry.addName(ender.getItemStack(), "Ender Dust");
 		LanguageRegistry.addName(ironDust.getItemStack(), "Iron Dust");
 		LanguageRegistry.addName(new ItemStack(ore, 1, 0), "Dusty Iron Ore");
 		LanguageRegistry.addName(new ItemStack(ore, 1, 1), "Ender Ore");
 		LanguageRegistry.addName(blockLiqEC, "Liquid EnderCoin");
 		langRegistry.addStringLocalization("itemGroup.EnderMoney", "EnderMoney");
 
 		GameRegistry.addRecipe(new CoinCrafter());
 
 		if (craftable.getBoolean(true)) {
 			GameRegistry.addRecipe(new ShapedOreRecipe(((EnderCoin) coin).getItemStack(1, 64),
 					false, "xyx", "y#y", "xyx", 'x', "dustEnder", 'y', "dustIron", '#',
 					new ItemStack(Item.enderPearl)));
 		}
 
 		FurnaceRecipes.smelting().addSmelting(ironDust.superID, ironDust.itemID,
 				new ItemStack(Item.ingotIron, 1), 0F);
 
 		MinecraftForge.EVENT_BUS.register(new EventListener());
 		MinecraftForge.ORE_GEN_BUS.register(new OreGenListener());
 
 		GameRegistry.registerWorldGenerator(new WorldGenerator());
 	}
 
 	@EventHandler
 	public void init(FMLInitializationEvent event) {
 		proxy.setCustomRenderers();
 	}
 
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {
 
 	}
 
 }
