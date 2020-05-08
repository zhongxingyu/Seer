 package slimevoid.tmf.client.proxy;
 
 import net.minecraft.client.Minecraft;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.MinecraftForge;
 import slimevoid.lib.core.SlimevoidCore;
 import slimevoid.tmf.armor.ArmorLib;
 import slimevoid.tmf.client.network.ClientPacketHandler;
 import slimevoid.tmf.client.sounds.TrackerSounds;
 import slimevoid.tmf.client.tickhandlers.MiningHelmetRenderTickHandler;
 import slimevoid.tmf.client.tickhandlers.MotionSensorTickHandler;
 import slimevoid.tmf.core.TMFCore;
 import slimevoid.tmf.core.TMFInit;
 import slimevoid.tmf.network.CommandLib;
 import slimevoid.tmf.network.packets.executors.MotionSensorSweepExecutor;
 import slimevoid.tmf.proxy.CommonProxy;
 import slimevoid.tmf.tickhandlers.MiningHelmetTickHandler;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 public class ClientProxy extends CommonProxy {
 	
 	
 	@Override
 	public void preInit() {
 		super.preInit();
		ClientPacketHandler.init();
 		
 		MinecraftForge.EVENT_BUS.register(new TrackerSounds());
 	}
 
 	@Override
 	public void registerRenderInformation() {
 		SlimevoidCore.console(TMFInit.TMF.getModName(), "Registering Renderers...");
 		MinecraftForgeClient.preloadTexture(
 				ArmorLib.registerArmorTexture(
 						TMFCore.miningHelmetIron,
 						ArmorLib.IRON_MINING_HELMET)
 		);
 		MinecraftForgeClient.preloadTexture(
 				ArmorLib.registerArmorTexture(
 						TMFCore.miningHelmetGold,
 						ArmorLib.GOLD_MINING_HELMET)
 		);
 		MinecraftForgeClient.preloadTexture(
 				ArmorLib.registerArmorTexture(
 						TMFCore.miningHelmetDiamond,
 						ArmorLib.DIAMOND_MINING_HELMET)
 		);
 	}
 	
 	@Override
 	public void registerTickHandler() {
 		super.registerTickHandler();
 		SlimevoidCore.console(TMFInit.TMF.getModName(), "Registering Client tick handlers...");
 		TickRegistry.registerTickHandler(new MotionSensorTickHandler(TMFCore.motionSensorMaxEntityDistance, TMFCore.motionSensorMaxGameTicks, TMFCore.motionSensorDrawRight), Side.CLIENT);
 		TickRegistry.registerTickHandler(new MiningHelmetTickHandler(), Side.CLIENT);
 		TickRegistry.registerTickHandler(new MiningHelmetRenderTickHandler(), Side.CLIENT);
 	}
 	
 	@Override
 	public String getMinecraftDir() {
 		return Minecraft.getMinecraftDir().toString();
 	}
 }
