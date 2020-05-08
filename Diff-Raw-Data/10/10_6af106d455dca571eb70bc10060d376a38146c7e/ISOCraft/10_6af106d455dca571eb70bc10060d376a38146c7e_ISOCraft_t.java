 package com.isocraft;
 
 import java.util.logging.Level;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
 
 import com.isocraft.block.ModBlocks;
 import com.isocraft.core.CreativeTabISOCraft;
 import com.isocraft.core.SetupRecipes;
 import com.isocraft.core.configuration.ConfigurationHandler;
import com.isocraft.core.handlers.PlayerClickHandler;
 import com.isocraft.core.helpers.LogHelper;
 import com.isocraft.core.proxy.CommonProxy;
 import com.isocraft.item.ModItems;
 import com.isocraft.lib.Reference;
 import com.isocraft.network.PacketPipeline;
 import com.isocraft.thesis.ThesisSystem;
 import com.isocraft.thesis.data.ThesisBasics;
 import com.isocraft.thesis.data.ThesisRefine;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkRegistry;
 
 /**
  * ISOCraft
  * 
  * Main mod class for the Minecraft mod ISOCraft
  * 
  * @author Turnermator13
  */
 
 @Mod(name = Reference.MOD_NAME, modid = Reference.MOD_ID, version = Reference.VERSION, dependencies = Reference.DEPENDENCIES, certificateFingerprint = Reference.FINGERPRINT)
 public class ISOCraft {
 
 	public static final String DISCOVERY_XML_LOC = "/assets/isocraft/Thesis.xml";
 
 	@Instance(Reference.MOD_ID)
 	public static ISOCraft instance;
 
 	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS)
 	public static CommonProxy proxy;
 
 	public static CreativeTabs tabsISO = new CreativeTabISOCraft(CreativeTabs.getNextID(), Reference.MOD_ID);
 	
 	public static final PacketPipeline packetPipeline = new PacketPipeline();
 	
 	@EventHandler
 	public void preinitialization(FMLPreInitializationEvent event) {
 		LogHelper.init();
 
 		ConfigurationHandler.init(event.getSuggestedConfigurationFile());
 
 		//TickRegistry.registerTickHandler(new ISOCraftTickHandler(), Side.CLIENT);
 
 		ModBlocks.init();
 		ModItems.init();
 
 		ThesisSystem.instance().loadFromXML(DISCOVERY_XML_LOC);
 	}
 
 	@EventHandler
 	public void initialization(FMLInitializationEvent event) {
 		packetPipeline.initalise();
 		
 		//NetworkRegistry.INSTANCE.registerConnectionHandler(new ConnectionHandler());
 		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
		MinecraftForge.EVENT_BUS.register(new PlayerClickHandler());
		
		
 		//GameRegistry.registerWorldGenerator(new ISOCraftGenerator(), 0);
 		//ISOCraftGenerationChests.init();
 		//VillagerRegistry.instance().registerVillageCreationHandler(new VillageISOTowerHandler());
 		//MapGenStructureIO.func_143031_a(ComponentISOTower.class, "iso_ISOTower");
 
 		//VillagerRegistry.instance().registerVillagerId(EntityInfo.Historian_id);
 		//VillagerRegistry.instance().registerVillageTradeHandler(EntityInfo.Historian_id, new ISOCraftTradeHandler());
 
 		proxy.registerRenderThings();
 		proxy.initTileEntities();
 
 		SetupRecipes.Setup();
 
 		this.registerTheses();
 	}
 
 	@EventHandler
 	public void postinitialization(FMLPostInitializationEvent event) {
 		packetPipeline.postInitialise();
 		LogHelper.log(Level.INFO, "ISOCraft Version " + Reference.VERSION + " has been loaded successfully");
 	}
 
 	private void registerTheses() {
 		ThesisSystem.WhitelistPlayerDataSync.add(Blocks.crafting_table);
 
 		ThesisSystem.addThesis(new ThesisBasics());
 		ThesisSystem.addThesis(new ThesisRefine());
 	}
 }
