 package mods.recipear;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import mods.recipear.api.RecipearEvent;
 import mods.recipear.api.RecipearListener;
 import mods.recipear.modules.RecipearVanilla;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.main.Main;
 import net.minecraft.command.ServerCommandManager;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.logging.LogAgent;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.ModLoader;
 import net.minecraft.util.ChatMessageComponent;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.launcher.FMLTweaker;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.CoreModManager;
 import cpw.mods.fml.relauncher.FMLRelaunchLog;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid = "Recipear2", name = "Recipear2", version = "2.1.0", dependencies="required-after:Forge@[9.10,)")
 @NetworkMod(clientSideRequired = false, serverSideRequired = false, channels = {"recipear"}, packetHandler = PacketManager.class)
 public class Recipear 
 {
 	public static boolean debug = true;
 	public static RecipearListener recipeEvents = new RecipearListener();
 	
 	@SidedProxy(clientSide="mods.recipear.RecipearClientProxy", serverSide="mods.recipear.RecipearCommonProxy")
 	public static RecipearCommonProxy proxy;
 	
 	public static RecipearConfig recipearconfig;
 	
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) 
 	{
 		String date = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
 		
 		BannedRecipes.AddBannedRecipeType("CRAFTING","FURNACE","INVENTORY");
		RecipearLogger.setLogger(new LogAgent("Recipear", "", (new File(event.getModConfigurationDirectory().getParentFile(), "Recipear-" + date + ".log")).getAbsolutePath()));
 		recipearconfig = new RecipearConfig(event);
 	}
 
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) 
 	{
 		// show supported recipe types
 		String supported_types = "Supported Recipe Types are";
 		
 		for(String type : BannedRecipes.getBannedRecipeTypes()) {
 			supported_types += " " + type;
 		}
 		
 		if(debug) RecipearConfig.debug = true;
 		RecipearLogger.info(supported_types);
 		
 		recipeEvents.add(new RecipearVanilla());
 	}
 
 	@EventHandler
 	void ServerStartingEvent(FMLServerStartingEvent event) {
 		if (!proxy.isSinglePlayer()) {
 			recipeEvents.trigger(new RecipearEvent(Side.SERVER, true));
 			
 			ServerCommandManager serverCommand = (ServerCommandManager)MinecraftServer.getServer().getCommandManager();
 	        serverCommand.registerCommand(new RecipearCommand());
 			GameRegistry.registerPlayerTracker(new RecipearPlayerTracker());
 			TickRegistry.registerScheduledTickHandler(new RecipearPlayerTick(), Side.SERVER);
 			NetworkRegistry.instance().registerConnectionHandler(new ConnectionHandler());
 		}
 	}
 }
