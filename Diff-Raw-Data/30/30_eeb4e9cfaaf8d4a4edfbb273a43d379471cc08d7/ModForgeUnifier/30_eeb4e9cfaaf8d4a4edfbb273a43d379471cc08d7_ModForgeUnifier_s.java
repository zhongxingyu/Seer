 package schmoller.unifier;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 import schmoller.network.ClientPacketManager;
 import schmoller.network.IModPacketHandler;
 import schmoller.network.ModPacket;
 import schmoller.network.PacketManager;
 import schmoller.unifier.gui.GuiUnifierSettings;
 import schmoller.unifier.mods.factorization.*;
 import schmoller.unifier.mods.forestry.*;
 import schmoller.unifier.mods.gregtech.*;
 import schmoller.unifier.mods.ic2.*;
 import schmoller.unifier.mods.mekanism.*;
 import schmoller.unifier.mods.railcraft.*;
 import schmoller.unifier.mods.thaumcraft.*;
 import schmoller.unifier.mods.thermalExpansion.*;
 import schmoller.unifier.mods.tinkersConstruct.*;
 import schmoller.unifier.packets.ModPacketChangeMapping;
 import schmoller.unifier.packets.ModPacketOpenGui;
 import schmoller.unifier.vanilla.*;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.NetLoginHandler;
 import net.minecraft.network.packet.NetHandler;
 import net.minecraft.network.packet.Packet1Login;
 import net.minecraft.server.MinecraftServer;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.DummyModContainer;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.LoadController;
 import cpw.mods.fml.common.ModMetadata;
 import cpw.mods.fml.common.event.FMLConstructionEvent;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.event.FMLServerStoppingEvent;
 import cpw.mods.fml.common.network.IConnectionHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.common.versioning.VersionParser;
 import cpw.mods.fml.common.versioning.VersionRange;
 import cpw.mods.fml.relauncher.Side;
 
 public class ModForgeUnifier extends DummyModContainer implements IModPacketHandler, IConnectionHandler 
 {
 	public static Logger log;
 	public static GlobalMappings globalMappings = null;
 	public static NetworkedMappings mappings = null;
 	public static ProcessorManager manager;
 	
 	public static PacketManager packetHandler;
 	
 	public ModForgeUnifier()
 	{
 		super(new ModMetadata());
 		ModMetadata meta = getMetadata();
 		meta.modId = "ForgeUnifier";
 		meta.name = "Forge Item Unifier";
 		meta.version = "##FUVersion##";
 	}
 	
 	@Override
 	public boolean registerBus( EventBus bus, LoadController controller )
 	{
 		bus.register(this);
 		return true;
 	}
 	
 	@Subscribe
     public void constructMod(FMLConstructionEvent event)
     {
 		if(event.getSide() == Side.CLIENT)
 			packetHandler = new ClientPacketManager();
 		else
 			packetHandler = new PacketManager();
     }
 	
 	
 	@Subscribe
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		log = Logger.getLogger("OreUnifier");
 		log.setUseParentHandlers(true);
 
 		manager = new ProcessorManager();
 	}
 	
 	@Subscribe
 	public void init(FMLInitializationEvent event)
 	{
 		packetHandler.initialize("FrgUni");
 		PacketManager.registerHandler(this);
 		
 		PacketManager.registerPacket(ModPacketOpenGui.class);
 		PacketManager.registerPacket(ModPacketChangeMapping.class);
 		
 		NetworkRegistry.instance().registerConnectionHandler(this);
 		
 		globalMappings = new GlobalMappings(new File("ForgeUnifier.dat"));
 		try
 		{
 			globalMappings.load();
 		}
 		catch(IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 	
 	@Subscribe
 	public void postInit(FMLPostInitializationEvent event)
 	{
 		manager.registerProcessor(new CraftingProcessor());
 		manager.registerProcessor(new SmeltingProcessor());
 		manager.registerProcessor(new VillagerTradeProcessor());
 		
 		// IC2 processors
 		manager.registerModProcessor("IC2", CompressorProcessor.class);
 		manager.registerModProcessor("IC2", ExtractorProcessor.class);
 		manager.registerModProcessor("IC2", MaceratorProcessor.class);
 		manager.registerModProcessor("IC2", ScrapboxProcessor.class);
 
 		// Railcraft processors
 		manager.registerModProcessor("Railcraft", BlastFurnaceProcessor.class);
 		manager.registerModProcessor("Railcraft", CokeOvenProcessor.class);
 		manager.registerModProcessor("Railcraft", RockCrusherProcessor.class);
 		manager.registerModProcessor("Railcraft", RollingMachineProcessor.class);
 		
 		// Thaumcraft processors
 		manager.registerModProcessor("Thaumcraft", ArcaneCraftingProcessor.class);
 		manager.registerModProcessor("Thaumcraft", CrucibleProcessor.class);
 		manager.registerModProcessor("Thaumcraft", SmelterBonusProcessor.class);
 		
 		// Forestry processors
 		manager.registerModProcessor("Forestry", CarpenterProcessor.class);
 		manager.registerModProcessor("Forestry", CentrifugeProcessor.class);
 		manager.registerModProcessor("Forestry", FabricatorProcessor.class);
 		manager.registerModProcessor("Forestry", SqueezerProcessor.class);
 		
 		// Factorization processors
 		manager.registerModProcessor("factorization", CrystallizerProcessor.class);
 		manager.registerModProcessor("factorization", GrinderProcessor.class);
 		manager.registerModProcessor("factorization", SlagFurnaceProcessor.class);
 		
 		// Tinkers construct
 		manager.registerModProcessor("TConstruct", TableCastingProcessor.class);
 		manager.registerModProcessor("TConstruct", BasinCastingProcessor.class);
 		
 		// Applied energistics
 		manager.registerModProcessor("AppliedEnergistics", schmoller.unifier.mods.appeng.GrinderProcessor.class);
 		
 		// Thermal Expansion
 		manager.registerModProcessor("ThermalExpansion", PulverizerProcessor.class);
 		manager.registerModProcessor("ThermalExpansion", SawmillProcessor.class);
 		manager.registerModProcessor("ThermalExpansion", InductionSmelterProcessor.class);
 		
 		// Gregtech
 		manager.registerModProcessor("GregTech_Addon", AlloySmelterProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", AssemblerProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", CannerProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", ChemicalReactorProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", DistillationProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", ElectrolyzerProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", FusionProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", ImplosionCompressorProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", IndustrialBlastFurnaceProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", IndustrialCentrifugeProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", IndustrialGrinderProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", IndustrialSawmillProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", PlateBenderProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", VacuumFreezerProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", WiremillProcessor.class);
 		manager.registerModProcessor("GregTech_Addon", GTUnificatorProcessor.class);
 		
 		// Mekanism
 		manager.registerModProcessor("Mekanism", CombinerProcessor.class);
 		manager.registerModProcessor("Mekanism", CrusherProcessor.class);
 		manager.registerModProcessor("Mekanism", EnrichmentChamberProcessor.class);
 		manager.registerModProcessor("Mekanism", OsmiumCompressorProcessor.class);
 		manager.registerModProcessor("Mekanism", MetallurgicInfuserProcessor.class);
 		manager.registerModProcessor("Mekanism", PurificationChamberProcessor.class);
 	}
 	
 	@Subscribe
 	public void onServerStarting(FMLServerStartingEvent event)
 	{
 		MinecraftServer server = event.getServer();
 		
 		File folder = null;
 		if(server.isDedicatedServer())
 			folder = server.getFile(server.worldServers[0].getSaveHandler().getWorldDirectoryName());
 		else
 			folder = server.getFile("saves/" + server.getFolderName());
 
 		File path = new File(folder, "ForgeUnifier.dat");
 		
 		Mappings.safeGuardOreDict();
 		
 		mappings = new ServerMappings(path);
 		try
 		{
 			mappings.load();
 			if(server.isSinglePlayer())
 				mappings.setParent(globalMappings);
 			
 			manager.execute(mappings);
 		}
 		catch(IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 		
 		event.registerServerCommand(new CommandUnifier());
 	}
 	
 	@Subscribe
 	public void onServerStopping(FMLServerStoppingEvent event)
 	{
 		mappings.restoreOriginals();
 	}
 	
 	@Override
 	public boolean onPacketArrive( ModPacket packet, Player sender )
 	{
 		if(packet instanceof ModPacketOpenGui && FMLCommonHandler.instance().getSide() == Side.CLIENT)
 			openGui(null, ((ModPacketOpenGui)packet).allowEdit);
 		else if(packet instanceof ModPacketChangeMapping)
 		{
 			mappings.applyChanges((ModPacketChangeMapping)packet, sender);
 		}
 		else
 			return false;
 		
 		return true;
 	}
 	
 	public static void openGui(EntityPlayer player, boolean edit)
 	{
 		if(Utilities.isClient() && Utilities.isServer())
 		{
 			if(player.equals(Minecraft.getMinecraft().thePlayer))
 				FMLClientHandler.instance().showGuiScreen(new GuiUnifierSettings(edit, mappings));
 			else
 				packetHandler.sendPacketToClient(new ModPacketOpenGui(true), player);
 		}
 		else
 		{
 			if(player == null)
 				FMLClientHandler.instance().showGuiScreen(new GuiUnifierSettings(edit, mappings));
 			else if(Utilities.isServer())
 				packetHandler.sendPacketToClient(new ModPacketOpenGui(edit), player);
 		}
 	}
 
 	@Override
 	public void playerLoggedIn( Player player, NetHandler netHandler, INetworkManager manager )
 	{
 		ModPacketChangeMapping packet = mappings.asPacket();
 		packetHandler.sendPacketToClient(packet, (EntityPlayer)player);
 	}
 
 	@Override
 	public String connectionReceived( NetLoginHandler netHandler, INetworkManager manager )	{ return null; }
 
 	@Override
 	public void connectionOpened( NetHandler netClientHandler, String server, int port, INetworkManager manager ) 
 	{
 		// Prepare for connection to server
 		Mappings.safeGuardOreDict();
 		mappings = new NetworkedMappings();
 	}
 
 	@Override
 	public void connectionOpened( NetHandler netClientHandler, MinecraftServer server, INetworkManager manager ) {}
 
 	@Override
 	public void connectionClosed( INetworkManager manager ) 
 	{
 		// Disconnect from server, restore defaults
 		if(!Utilities.isServer())
 			mappings.restoreOriginals();
 	}
 
 	@Override
 	public void clientLoggedIn( NetHandler clientHandler, INetworkManager manager, Packet1Login login ) {}
 	
 	public static boolean canPlayerEdit(EntityPlayer player)
 	{
 		return (FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().areCommandsAllowed(player.username));
 	}
 	
 	@Override
 	public VersionRange acceptableMinecraftVersionRange()
 	{
 		return VersionParser.parseRange("1.5.2");
 	}
 }
