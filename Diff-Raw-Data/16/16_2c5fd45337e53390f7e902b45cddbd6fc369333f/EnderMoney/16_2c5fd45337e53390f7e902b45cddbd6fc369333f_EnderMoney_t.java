 package com.github.soniex2.endermoney.core;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.init.Items;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 
 import com.github.soniex2.endermoney.config.ConfigBoolean;
 import com.github.soniex2.endermoney.core.block.LiquidCoin;
 import com.github.soniex2.endermoney.core.block.Ore;
 import com.github.soniex2.endermoney.core.fluid.FluidEnderCoin;
 import com.github.soniex2.endermoney.core.item.EnderCoin;
 import com.github.soniex2.endermoney.core.item.EnderItem;
 import com.github.soniex2.endermoney.core.item.EnderItem.EnderSubItem;
 import com.github.soniex2.endermoney.core.item.GenericItem;
 import com.google.gson.Gson;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "EnderMoneyCore", name = "EnderMoney Core", version = Version.MOD_VERSION, dependencies = "required-after:Forge")
 public class EnderMoney {
 
 	public static final CreativeTabs tab = new CreativeTabs(
 			"endermoneycore.EnderMoney") {
 		@Override
 		public Item getTabIconItem() {
 			return null;
 		}
 	};
 	public static EnderItem enderItem;
 	public static Item coin;
 	public static Block ore;
 	public static EnderSubItem ender;
 	public static EnderSubItem ironDust;
 	public static EnderSubItem enderIngot;
 
 	@Instance("EnderMoneyCore")
 	public static EnderMoney instance;
 
 	@SidedProxy(clientSide = "com.github.soniex2.endermoney.core.ClientProxy", serverSide = "com.github.soniex2.endermoney.core.CommonProxy")
 	public static CommonProxy proxy;

	// public static FluidEnderCoin fluidEC;
	// public static LiquidCoin blockLiqEC;
 
 	private static class Config {
 		public ConfigBoolean craftableCoins = new ConfigBoolean(
 				"Set to true to enable EnderCoin crafting", true);
 
 		private Config() {
 		}
 	}
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 
 		// WTF AM I EVEN DOING HERE?!?!
 		File configFile = new File(event.getModConfigurationDirectory(),
 				"EnderMoney/Core.json");
 		Config config = new Config();
 		if (!configFile.exists()) {
 			try {
 				FileOutputStream out = new FileOutputStream(configFile);
 				Gson gson = new Gson();
 				String s = gson.toJson(config);
 				out.write(s.getBytes("UTF-8"));
 				out.close();
 			} catch (Exception e) {
 				if (e instanceof RuntimeException)
 					throw (RuntimeException) e;
 				throw new RuntimeException(e);
 			}
 		} else {
 			try {
 				FileInputStream in = new FileInputStream(configFile);
 				Gson gson = new Gson();
 				ByteArrayOutputStream bs = new ByteArrayOutputStream(4096);
 				byte[] b = new byte[4096];
 				int x;
 				while ((x = in.read(b)) > -1) {
 					bs.write(b, 0, x);
 				}
 				String s = new String(bs.toByteArray(), "UTF-8");
 				config = gson.fromJson(s, Config.class);
 				in.close();
 			} catch (Exception e) {
 				if (e instanceof RuntimeException)
 					throw (RuntimeException) e;
 				throw new RuntimeException(e);
 			}
 		}
 		// END
 
 		enderItem = EnderItem.instance = new EnderItem();
 		coin = new EnderCoin();
 		ore = new Ore();
 		ender = new GenericItem(0, "dustEnder", "endermoneycore:dust",
 				0x228866, true);
 		ironDust = new GenericItem(1, "dustIron", "endermoneycore:dust",
 				0xDDDDDD);
 		enderIngot = new GenericItem(2, "ingotEnder", "iron_ingot", 0x228866,
 				true);
		// fluidEC = new FluidEnderCoin();
		// blockLiqEC = new LiquidCoin(0, fluidEC);
 
 		GameRegistry.registerBlock(ore, Ore.Item.class, "endermoneycore.ore");
		// GameRegistry.registerBlock(blockLiqEC, "endermoneycore.liquidMoney");
 
 		OreDictionary.registerOre("dustEnder", ender.getItemStack());
 		OreDictionary.registerOre("dustIron", ironDust.getItemStack());
 		OreDictionary.registerOre("ingotEnder", enderIngot.getItemStack());
 		OreDictionary.registerOre("oreEnderDust", new ItemStack(ore, 1, 1));
 
 		LanguageRegistry langRegistry = LanguageRegistry.instance();
 		langRegistry.addStringLocalization(
 				"item.endermoneycore.endercoin.name", "EnderCoin");
 		LanguageRegistry.addName(ender.getItemStack(), "Ender Dust");
 		LanguageRegistry.addName(ironDust.getItemStack(), "Iron Dust");
 		LanguageRegistry.addName(enderIngot.getItemStack(), "Ender Ingot");
 		LanguageRegistry.addName(new ItemStack(ore, 1, 0), "Dusty Iron Ore");
 		LanguageRegistry.addName(new ItemStack(ore, 1, 1), "Ender Ore");
		// LanguageRegistry.addName(blockLiqEC, "Liquid EnderCoin");
 		langRegistry.addStringLocalization(
 				"itemGroup.endermoneycore.EnderMoney", "EnderMoney");
 
 		GameRegistry.addRecipe(new CoinCrafter());
 
 		if (config.craftableCoins.getValue()) {
 			GameRegistry
 					.addRecipe(new ShapedOreRecipe(((EnderCoin) coin)
 							.getItemStack(1, 64), false, "xyx", "y#y", "xyx",
 							'x', "ingotEnder", 'y', Items.iron_ingot, '#',
 							Items.ender_pearl));
 		}
 
 		GameRegistry.addRecipe(new ShapelessOreRecipe(ender.getItemStack(2),
 				"dustIron", "dustIron", Items.ender_pearl));
 
 		FurnaceRecipes.smelting().func_151394_a(
 				new ItemStack(enderItem, ironDust.idx),
 				new ItemStack(Items.iron_ingot, 1), 0F);
 		FurnaceRecipes.smelting().func_151394_a(
 				new ItemStack(enderItem, ender.idx),
 				new ItemStack(enderItem, 1, enderIngot.idx), 0.5F);
 
 		MinecraftForge.EVENT_BUS.register(new EventListener());
 		MinecraftForge.ORE_GEN_BUS.register(new OreGenListener());
 
 		GameRegistry.registerWorldGenerator(new WorldGenerator(), 1);
 	}
 
 	@EventHandler
 	public void init(FMLInitializationEvent event) {
 		proxy.setCustomRenderers();
 	}
 
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {
 
 	}
 
 }
