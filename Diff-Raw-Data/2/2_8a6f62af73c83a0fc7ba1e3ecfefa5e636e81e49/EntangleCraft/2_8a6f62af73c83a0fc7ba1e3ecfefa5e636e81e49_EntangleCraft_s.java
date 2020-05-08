 package entanglecraft;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.InventoryPlayer;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.KeyBinding;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.NetHandler;
 import net.minecraft.src.NetLoginHandler;
 import net.minecraft.src.INetworkManager;
 import net.minecraft.src.Packet1Login;
 import net.minecraft.src.World;
 import entanglecraft.*;
 import entanglecraft.SoundHandling.LambdaSoundHandler;
 import entanglecraft.blocks.*;
 import entanglecraft.gui.EnumGui;
 import entanglecraft.generation.*;
 import entanglecraft.items.EntangleCraftItems;
 import entanglecraft.items.ItemDevice;
 import entanglecraft.items.ItemLambda;
 import entanglecraft.items.ItemShard;
 import net.minecraft.client.Minecraft;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import cpw.mods.fml.client.registry.KeyBindingRegistry;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.asm.SideOnly;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.IConnectionHandler;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 @Mod( modid = "EntangleCraft", name="EntangleCraft", version="1.0")
 @NetworkMod(
      clientSideRequired = true,
      serverSideRequired = false,
      clientPacketHandlerSpec = @SidedPacketHandler(channels={"EntangleCraft"}, packetHandler=ClientPacketHandler.class),
      serverPacketHandlerSpec = @SidedPacketHandler(channels={"EntangleCraft"}, packetHandler=ServerPacketHandler.class),
      connectionHandler = EntangleCraft.class,
      versionBounds = "[1]"
 	     )
 public class EntangleCraft implements IConnectionHandler
 {
 	@Instance
 	public static EntangleCraft instance;
 	
	@SidedProxy(clientSide = "net.minecraft.src.entanglecraft.ClientProxy", serverSide = "net.minecraft.src.entanglecraft.CommonProxy")
 	public static CommonProxy proxy;
 	
 	private static ArrayList destinations = new ArrayList();
 	private static ArrayList[] channelDests = { new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList() };
 	private static boolean shouldGenerateSkyFortress = true;
 	
 	public static DistanceHandler dhInstance = new DistanceHandler();
   	private WorldGenSkyFortress skyFortressMaker = new WorldGenSkyFortress();
 
   	public static final KeyBinding incrementDeviceChannel = new KeyBinding("incrementDeviceChannel", 34);
   
   	
   	@PreInit
   	public void preLoad(FMLPreInitializationEvent event){
   		proxy.registerClientSide();
   		proxy.registerDistanceSaver(dhInstance);
   		System.out.println("Tried to do dat preload");
   	}
   	
   @Init
   public void load(FMLInitializationEvent event)
   {
 	instance = this;
     MinecraftForgeClient.preloadTexture("/lambdaTextures.png");
     NetworkRegistry.instance().registerGuiHandler(this, proxy);
     
     EntangleCraftBlocks.addBlocks();
     EntangleCraftItems.addItems();
     
 
   } 
   
   /*
   @ForgeSubscribe
   public void generateSurface(World world, Random rand, int chunkX, int chunkZ)
   {	  
 	  int x = chunkX + rand.nextInt(16);
       int y = 192;
       int z = chunkZ + rand.nextInt(16);
 	  skyFortressMaker.generate(world, rand, x, y, z);
 	  new WorldGenOre().generate(world, rand, chunkX, 100, chunkZ);
   }
   */
   
   private static Destination closestDest(EntityPlayer playerEntity, ArrayList dests)
   {
     Destination destination = null;
     double[] playerPoints; double minDistance; Iterator iterator; 
     if (dests.size() != 0) {
       playerPoints = new double[] { playerEntity.posX, playerEntity.posY, playerEntity.posZ };
       destination = (Destination)dests.get(0);
       
       minDistance = getDistance(playerPoints, destination.destinationCoords);
       
       for (iterator = dests.iterator(); iterator.hasNext();) { Object points = iterator.next();
         Destination newPoints = (Destination)points;
         double newDistance = getDistance(playerPoints, newPoints.destinationCoords);
         if (newDistance < minDistance) {
           minDistance = newDistance;
           destination = newPoints;
         } 
         System.out.println("new minimum distance calculated as " + minDistance);
       } 
     } 
     return destination;
   } 
   
   public static void teleport(EntityPlayer par3EntityPlayer, int channel)
   {	
 	// This method is disgusting to read just hide it
 	if (EntangleCraft.proxy.isServer){  
   	ArrayList dests = channelDests[channel];
   	if (dests.size() != 0){
   		Destination dest = closestDest(par3EntityPlayer, dests);
   		double[] destinationPoints = dest.destinationCoords;
   		
   		double[] playerPoints = {par3EntityPlayer.posX, par3EntityPlayer.posY, par3EntityPlayer.posZ};
   		double amount = getDistance(destinationPoints,playerPoints);
   		dhInstance.addToDistance(channel, amount);
   	
   	if (par3EntityPlayer instanceof EntityPlayerMP){
   	EntityPlayerMP thePlayer = (EntityPlayerMP)par3EntityPlayer;
   	// Server-side teleports seem to teleport the player too high so I'm subtracting 1.65 from the usual height
   	try{
       thePlayer.playerNetServerHandler.setPlayerLocation(destinationPoints[0], destinationPoints[1]-1.65, destinationPoints[2], par3EntityPlayer.rotationYaw, par3EntityPlayer.rotationPitch);
       ServerPacketHandler.playTPSoundToClients(thePlayer,destinationPoints,"teleport");
       ServerPacketHandler.spawnParticleToClients(destinationPoints,"largeexplosion");
       ServerPacketHandler.spawnParticleToClients(playerPoints,"largeexplosion");
       
       int[] closBlock = dest.blockCoords;
       World theWorld = par3EntityPlayer.worldObj;
   	TileEntityGenericDestination destEntity = (TileEntityGenericDestination)theWorld.getBlockTileEntity(closBlock[0],closBlock[1],closBlock[2]);
       if(destEntity.teleportsEarned != 0) destEntity.changeTeleportsEarned(-1);
   	}
   	catch (NullPointerException e){
   		thePlayer.addChatMessage("No available teleport Destinations!");
   	}
   	}
   	}
 	}
   }
   
   public static void addDestination(Destination dest)
   {
     int channel = dest.channel;
     
     channelDests[channel].add(dest);
     destinations.add(dest);
   } 
   
 
 
   public static void emptyDestinationList()
   {
     destinations = new ArrayList();
   } 
   
   public static void removeDestination(Destination destToRemove) {
     int channel = destToRemove.channel;
     
 
     channelDests[channel].remove(destToRemove);
     destinations.remove(destToRemove);
   } 
  
 
  public static double getDistance(double[] a, double[] b)
   {
     double x = a[0];double y = a[1];double z = a[2];
     double x0 = b[0];double y0 = b[1];double z0 = b[2];
     return Math.sqrt((x - x0) * (x - x0) + (y - y0) * (y - y0) + (z - z0) * (z - z0));
   } 
   
   public void keyboardEvent(KeyBinding event) {
     if (ModLoader.getMinecraftInstance().theWorld == null)
       return; 
     if (ModLoader.getMinecraftInstance().currentScreen != null)
       return; 
     try {
       ItemStack itemInUse = ModLoader.getMinecraftInstance().thePlayer.getCurrentEquippedItem();
       if (((itemInUse.getItem() instanceof ItemDevice)) && 
         (event == incrementDeviceChannel)) {
         InventoryPlayer inv = ModLoader.getMinecraftInstance().thePlayer.inventory;
         int x = itemInUse.stackSize;
         inv.setInventorySlotContents(inv.currentItem, new ItemStack(((ItemDevice)itemInUse.getItem()).incrementChannel(), x));
         ClientPacketHandler.sendDeviceToggle();
         System.out.println("Changed Channel");
       } 
     }
     catch (NullPointerException e)
     {
       e.printStackTrace();
     } 
   } 
   
 
 @Override
 public void playerLoggedIn(Player player, NetHandler netHandler,
 		INetworkManager manager) {
 	// TODO Auto-generated method stub
 	
 }
 
 @Override
 public String connectionReceived(NetLoginHandler netHandler,
 		INetworkManager manager) {
 	// TODO Auto-generated method stub
 	return null;
 }
 
 @Override
 public void connectionOpened(NetHandler netClientHandler, String server,
 		int port, INetworkManager manager) {
 	// TODO Auto-generated method stub
 	
 }
 
 @Override
 public void connectionOpened(NetHandler netClientHandler,
 		MinecraftServer server, INetworkManager manager) {
 	// TODO Auto-generated method stub
 	
 }
 
 @Override
 public void connectionClosed(INetworkManager manager) {
 	// TODO Auto-generated method stub
 	
 }
 
 @Override
 public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager,
 		Packet1Login login) {
 	// TODO Auto-generated method stub
 	
 } 
   
 } 
