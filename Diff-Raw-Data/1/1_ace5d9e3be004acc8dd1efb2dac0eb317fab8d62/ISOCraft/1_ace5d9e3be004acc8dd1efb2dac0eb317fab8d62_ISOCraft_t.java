 package com.isocraft;
 
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraftforge.common.MinecraftForge;
 
 import com.isocraft.block.ModBlocks;
 import com.isocraft.core.CreativeTabISOCraft;
 import com.isocraft.core.configuration.ConfigurationHandler;
 import com.isocraft.core.generation.ISOCraftGenerator;
 import com.isocraft.core.generation.ISOCraftTradeHandler;
 import com.isocraft.core.generation.structures.ISOCraftGenerationChests;
 import com.isocraft.core.generation.structures.ISOCraftVillagePieces;
 import com.isocraft.core.handlers.ConnectionHandler;
 import com.isocraft.core.handlers.ISOCraftTickHandler;
 import com.isocraft.core.handlers.LocalizationHandler;
 import com.isocraft.core.handlers.PlayerClickHandler;
 import com.isocraft.core.helpers.LogHelper;
 import com.isocraft.core.proxy.CommonProxy;
 import com.isocraft.item.ModItems;
 import com.isocraft.lib.EntityInfo;
 import com.isocraft.lib.Reference;
 import com.isocraft.network.PacketHandler;
 import com.isocraft.thesis.Theorem;
 import com.isocraft.thesis.Thesis;
 import com.isocraft.thesis.ThesisRarity;
 import com.isocraft.thesis.ThesisSystem;
 import com.isocraft.thesis.data.ThesisIntro;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.common.registry.VillagerRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 /**
  * ISOCraft
  * 
  * Main mod class for the Minecraft mod ISOCraft
  * 
  * @author Turnermator13
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 @Mod(name = Reference.MOD_NAME, modid = Reference.MOD_ID, dependencies = Reference.DEPENDENCIES)
 @NetworkMod(channels = { Reference.MOD_CHANNEL }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
 public class ISOCraft {
 
     public static final String DISCOVERY_XML_LOC = "/assets/isocraft/Thesis.xml";
 
     public static final Thesis ELECTRIC = new Thesis("ELECTRIC THESIS", ThesisRarity.WellKnown).setLimiterTimer(190);
     public static final Thesis MOTION = new Thesis("MOTION THESIS", ThesisRarity.Rare).setMerchantStock(false);
 
     @Instance(Reference.MOD_ID)
     public static ISOCraft instance;
 
     @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS)
     public static CommonProxy proxy;
 
     public static CreativeTabs tabsISO = new CreativeTabISOCraft(CreativeTabs.getNextID(), Reference.MOD_ID);
 
     @EventHandler
     public void preinitialization(FMLPreInitializationEvent event) {
    	event.getModMetadata().version = Reference.VERSION;
         LogHelper.init();
         ConfigurationHandler.init(event.getSuggestedConfigurationFile());
         LocalizationHandler.loadLanguages();
         TickRegistry.registerTickHandler(new ISOCraftTickHandler(), Side.SERVER);
         proxy.registerRenderTickHandler();
 
         ModBlocks.init();
         ModItems.init();
 
         ThesisSystem.instance().loadFromXML(DISCOVERY_XML_LOC);
     }
 
     @EventHandler
     public void initialization(FMLInitializationEvent event) {
         NetworkRegistry.instance().registerConnectionHandler(new ConnectionHandler());
         NetworkRegistry.instance().registerGuiHandler(instance, proxy);
         GameRegistry.registerWorldGenerator(new ISOCraftGenerator());
         MinecraftForge.EVENT_BUS.register(new PlayerClickHandler());
         ISOCraftGenerationChests.init();
 
         VillagerRegistry.instance().registerVillageCreationHandler(new ISOCraftVillagePieces());
         VillagerRegistry.instance().registerVillagerId(EntityInfo.Historian_id); 
         VillagerRegistry.instance().registerVillageTradeHandler(EntityInfo.Historian_id, new ISOCraftTradeHandler());
 
         proxy.registerRenderThings();
         proxy.initTileEntities();
 
         this.registerTheses();
     }
 
     @EventHandler
     public void postinitialization(FMLPostInitializationEvent event) {
         LogHelper.log(Level.INFO, "ISOCraft Version " + Reference.VERSION + " has been loaded successfully");
     }
 
     private void registerTheses() {
         ThesisSystem.WhitelistPlayerDataSync.add(Block.workbench.blockID);
 
         ThesisSystem.addThesis(new ThesisIntro());
 
         // Test stuff
         ELECTRIC.addTheorem(new Theorem("TEST RARE", ThesisRarity.Legendary).setMerchantStock(false));
         ELECTRIC.addTheorem(new Theorem("TEST COMMON", ThesisRarity.Common));
         // TEST STUFF - ELECTRIC.addTheorem(new Theorem("TEST COMMON", ThesisRarity.Common));
         
         MOTION.addTheorem(new Theorem("TEST RARE", ThesisRarity.Legendary).setMerchantStock(false));
         MOTION.addTheorem(new Theorem("TEST COMMON", ThesisRarity.Common));
 
         ThesisSystem.addThesis(ELECTRIC);
         ThesisSystem.addThesis(MOTION);
         // End Test Stuff
     }
 }
