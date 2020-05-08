 package xelitez.frostcraft;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import xelitez.frostcraft.client.render.RenderFrostTool;
 import xelitez.frostcraft.effect.EffectTicker;
 import xelitez.frostcraft.effect.FCPotion;
 import xelitez.frostcraft.enchantment.FrostEnchantment;
 import xelitez.frostcraft.energy.EnergyRequestRegistry;
 import xelitez.frostcraft.network.NetworkManager;
 import xelitez.frostcraft.network.PacketManagerClient;
 import xelitez.frostcraft.network.PacketManagerServer;
 import xelitez.frostcraft.plugins.NEIFCLoader;
 import xelitez.frostcraft.plugins.Update;
 import xelitez.frostcraft.registry.CommonProxy;
 import xelitez.frostcraft.registry.IdMap;
 import xelitez.frostcraft.registry.RecipeRegistry;
 import xelitez.frostcraft.registry.Settings;
 import xelitez.frostcraft.world.WorldTicker;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.*;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(	
 		modid = "XEZFrostCraft",
 		name = "FrostCraft", 
 		acceptedMinecraftVersions = "[1.5.1]",
		version = "0.0.2")
 @NetworkMod(
 		clientSideRequired = true,
 		serverSideRequired = false,
 		versionBounds = "[0.0.0,0.1)",
 		clientPacketHandlerSpec = @SidedPacketHandler(channels = {"XFCC"}, packetHandler = PacketManagerClient.class ),
 		serverPacketHandlerSpec = @SidedPacketHandler(channels = {"XFCS"}, packetHandler = PacketManagerServer.class ))
 public class FrostCraft 
 {
 	@Instance(value = "XEZFrostCraft")
 	public static FrostCraft instance;
 	
 	private Configuration C;
 	private IdMap map = new IdMap();
 	
 	@SidedProxy(clientSide = "xelitez.frostcraft.registry.ClientProxy", serverSide = "xelitez.frostcraft.registry.CommonProxy")
 	public static CommonProxy proxy = new CommonProxy();
 	
 	public Version version = new Version();
 	
 	@PreInit
     public void preload(FMLPreInitializationEvent evt)
     {
 		evt.getModMetadata().name = "FrostCraft";
 		evt.getModMetadata().version = Version.getVersion() + "-Alpha for " + Version.MC;
 		
 		if(evt.getSide().isClient())
 		{
 			MinecraftForgeClient.preloadTexture("/mods/FrostCraft/textures/effects.png");
 		}
 		
		C = new Configuration(new File("XEliteZ/FrostCraft.cfg"));
 		try
 		{
 			C.load();
 			
 			C.addCustomCategoryComment(Configuration.CATEGORY_GENERAL, "IDs that are -1 will be given the next free id possible");
 			
 			Settings.MaxEnfrocerItems = C.get(Configuration.CATEGORY_GENERAL, "MaxEnforcerItems", Settings.defaultMaxEnforcedItems).getInt(Settings.defaultMaxEnforcedItems);
 			map.enforcerToolStartId = C.get("Equipment", "EnforcerToolsStartId", map.defaultEnforcerToolStartId).getInt(map.defaultEnforcerToolStartId);
 			
 			// Start getting world configuration
 			Settings.EndlessWinterID = C.get("World", "EndlessWinterId", Settings.EndlessWinterID).getInt();
 			
 			// Start getting potion configuration
 			Settings.potionFreezeId = C.get("Potion", "PotionFreezeId", Settings.potionFreezeId).getInt();
 			Settings.potionFrostburnId = C.get("Potion", "PotionFrostburnId", Settings.potionFrostburnId).getInt();
 			
 			// Start getting enchantments configuration
 			Settings.enchantmentFreezeId = C.get("Enchantments", "EnchantmentFreezeId", Settings.enchantmentFreezeId).getInt();
 			Settings.enchantmentFrostburnId = C.get("Enchantments", "EnchantmentFrostburnId", Settings.enchantmentFrostburnId).getInt();
 
 			// Start getting configuration settings of Item/Block Id's
 			IdMap.IdThermalPipe = C.get("Mechanical", "ThermalPipeId", map.defaultIdThermalPipe).getInt(map.defaultIdThermalPipe);
 			IdMap.IdThermalMachines = C.get("Mechanical", "ThermalPumpId", map.defaultIdThermalMachines).getInt(map.defaultIdThermalMachines);
 			
 			IdMap.IdFrostBow = C.get("Equipment", "FrostBowId", map.defaultIdFrostBow).getInt(map.defaultIdFrostBow);
 			IdMap.IdFrostGun = C.get("Equipment", "FrostGunId", map.defaultIdFrostGun).getInt(map.defaultIdFrostGun);
 			IdMap.IdCompiledFrostBlade = C.get("Equipment", "CompiledFrostBladeId", map.defaultIdCompiledFrostBlade).getInt(map.defaultIdCompiledFrostBlade);
 			IdMap.IdCompiledFrostSpade = C.get("Equipment", "CompiledFrostSpadeId", map.defaultIdCompiledFrostSpade).getInt(map.defaultIdCompiledFrostSpade);
 			IdMap.IdCompiledFrostPickaxe = C.get("Equipment", "CompiledFrostPickaxeId", map.defaultIdCompiledFrostPickaxe).getInt(map.defaultIdCompiledFrostPickaxe);
 			IdMap.IdCompiledFrostAxe = C.get("Equipment", "CompiledFrostAxeId", map.defaultIdCompiledFrostAxe).getInt(map.defaultIdCompiledFrostAxe);
 			IdMap.IdCompiledFrostHoe = C.get("Equipment", "CompiledFrostHoeId", map.defaultIdCompiledFrostHoe).getInt(map.defaultIdCompiledFrostHoe);
 			IdMap.IdFrozenFrostBlade = C.get("Equipment", "FrozenFrostBladeId", map.defaultIdFrozenFrostBlade).getInt(map.defaultIdFrozenFrostBlade);
 			IdMap.IdFrozenFrostSpade = C.get("Equipment", "FrozenFrostSpadeId", map.defaultIdFrozenFrostSpade).getInt(map.defaultIdFrozenFrostSpade);
 			IdMap.IdFrozenFrostPickaxe = C.get("Equipment", "FrozenFrostPickaxeId", map.defaultIdFrozenFrostPickaxe).getInt(map.defaultIdFrozenFrostPickaxe);
 			IdMap.IdFrozenFrostAxe = C.get("Equipment", "FrozenFrostAxeId", map.defaultIdFrozenFrostAxe).getInt(map.defaultIdFrozenFrostAxe);
 			IdMap.IdFrozenFrostHoe = C.get("Equipment", "FrozenFrostHoeId", map.defaultIdFrozenFrostHoe).getInt(map.defaultIdFrozenFrostHoe);
 			IdMap.IdFrostBlade = C.get("Equipment", "FrostBladeId", map.defaultIdFrostBlade).getInt(map.defaultIdFrostBlade);
 			IdMap.IdFrostSpade = C.get("Equipment", "FrostSpadeId", map.defaultIdFrostSpade).getInt(map.defaultIdFrostSpade);
 			IdMap.IdFrostPickaxe = C.get("Equipment", "FrostPickaxeId", map.defaultIdFrostPickaxe).getInt(map.defaultIdFrostPickaxe);
 			IdMap.IdFrostAxe = C.get("Equipment", "FrostAxeId", map.defaultIdFrostAxe).getInt(map.defaultIdFrostAxe);
 			IdMap.IdFrostHoe = C.get("Equipment", "FrostHoeId", map.defaultIdFrostHoe).getInt(map.defaultIdFrostHoe);
 			
 			IdMap.IdCraftingItems = C.get("Misc", "CraftingItemsId", map.defaultIdCraftingItems).getInt(map.defaultIdCraftingItems);
 			IdMap.IdIcicle = C.get("Misc", "IcicleId", map.defaultIdIcicle).getInt(map.defaultIdIcicle);
 			IdMap.IdBlockIcicle = C.get("Misc", "BlockIcicleId", map.defaultIdBlockIcicle).getInt(map.defaultIdBlockIcicle);
 			
 			// Getting configuration about update checker
             Property update = C.get("Updates", "Check for updates", true);
             Property ignoreMinorBuilds = C.get("Updates", "Ignore minor builds", true);
             Property ignoreOtherMCVersions = C.get("Updates", "Ignore other MC versions", false);
             Property notify = C.get("Updates", "Notify about XEZUpdateUtility", true);
             Version.notify = notify.getBoolean(true);
             Settings.checkForUpdates = update.getBoolean(true);
             Settings.ignoremB = ignoreMinorBuilds.getBoolean(true);
             Settings.ignoreMC = ignoreOtherMCVersions.getBoolean(false);
 		}
 		catch(Exception e)
 		{
 			FCLog.log(Level.WARNING, "FrostCraft failed to load the configuration file", e);
 		}
 		finally
 		{
 			C.save();
 		}
     }
 	
     @Init
     public void load(FMLInitializationEvent evt)
     {
     	map.initialiseBlocks();
     	map.initialiseItems();
     	map.initialiseEntities();
     	map.initialiseWorld();
     	if(evt.getSide().isClient())
     	{
     		map.initialiseRenderers();
     	}
     	FCPotion.RegisterPotionEffects();
     	FrostEnchantment.registerFrostEnchantments();
     	NetworkRegistry.instance().registerGuiHandler(instance, proxy);
     	TickRegistry.registerTickHandler(EnergyRequestRegistry.getInstance(), Side.SERVER);
     	TickRegistry.registerTickHandler(EffectTicker.instance(), Side.SERVER);
     	TickRegistry.registerTickHandler(new WorldTicker(), Side.SERVER);
     	NetworkRegistry.instance().registerConnectionHandler(new NetworkManager());
     	RecipeRegistry.registry().registerRecipes();
     	MinecraftForge.EVENT_BUS.register(new EventListener());
         try
         {
             if (Settings.checkForUpdates)
             {
 				Class<? extends Object> clazz = Class.forName("xelitez.updateutility.UpdateRegistry");
                 Method registermod = clazz.getDeclaredMethod("addMod", Object.class, Object.class);
                 registermod.invoke(null, this, new Update());
                 Version.registered = true;
             }
         }
         catch (Exception E)
         {
         	FCLog.log(Level.INFO, "FrostCraft failed to register to the XEZUpdateUtility");
         	FCLog.log(Level.INFO, "It isn't required but you should download it if possible");
             if (Settings.checkForUpdates)
             {
                 version.checkForUpdatesNoUtility();
             }
         }
     }
     
 	@PostInit
     public void postload(FMLPostInitializationEvent evt)
     {
 		try
 		{
 			Class.forName("codechicken.nei.api.API");
 			NEIFCLoader.register();
 		} 
 		catch(Exception e)
 		{
 			
 		}
 		map.initialiseEnfrocerItems();
 		if(evt.getSide().isClient())
 		{
 			for(int i = 0;i < map.EnforcedTools.length;i++)
 			{
 				if(map.EnforcedTools[i] != null)
 				{
 					MinecraftForgeClient.registerItemRenderer(map.EnforcedTools[i].itemID , new RenderFrostTool());
 				}
 			}
 		}
         proxy.registerSidedElements();
     }
 }
