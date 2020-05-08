 package henje.common;
 
 import java.io.File;
 
 import henje.client.GuiHandler;
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import buildcraft.BuildCraftCore;
 import buildcraft.BuildCraftFactory;
 import buildcraft.api.gates.ActionManager;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(modid = "henje_AdvancedCrafting", name="AdvancedCrafting", version="0.4.1")
 @NetworkMod(clientSideRequired=true)
 public class AdvancedCrafting {
 	
 	@Instance("henje_AdvancedCrafting")
 	public static AdvancedCrafting instance;
 	public static Block advancedCraftingBench;
 	public static GuiHandler handler = new GuiHandler();
 	
 	@SidedProxy(clientSide="henje.client.ClientProxy", serverSide="henje.common.CommonProxy")
 	public static CommonProxy proxy;
 	public static Configuration config;
 	
 	@Mod.PreInit
 	public void preLoad(FMLPreInitializationEvent event) {
 		config = new Configuration(new File("config/advancedcrafting.cfg"));
 		config.load();
 	}
 	
 	@Mod.Init
 	public void load(FMLInitializationEvent event) {
 		proxy.loadTexture();
 		advancedCraftingBench = new BlockAdvancedCrafter(config.get("id", "advancedCrafter", 1337).getInt()).setBlockName("block.advancedCrafter");
 		GameRegistry.registerBlock(advancedCraftingBench, "block.advancedCrafter");
 		LanguageRegistry.addName(advancedCraftingBench, "Advanced Autocrafter");
 		GameRegistry.registerTileEntity(TileEntityCrafter.class, "containerCrafter");
 		GameRegistry.addRecipe(new ItemStack(advancedCraftingBench), new Object[] {" # ", "#*#", " # ", '#', BuildCraftCore.goldGearItem, '*', BuildCraftFactory.autoWorkbenchBlock});
 		NetworkRegistry.instance().registerGuiHandler(this, handler);
 	}
 	
 	@Mod.PostInit
 	public void postLoad(FMLPostInitializationEvent event) {
 		ActionManager.registerActionProvider(new ActionProvider());
 		config.save();
 	}
 }
