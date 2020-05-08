 package dark.comecloser.common;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 
 import net.minecraft.client.renderer.entity.RenderPlayer;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.NetLoginHandler;
 import net.minecraft.network.packet.NetHandler;
 import net.minecraft.network.packet.Packet1Login;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.world.World;
 import net.minecraftforge.common.Configuration;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteStreams;
 
 import cpw.mods.fml.common.DummyModContainer;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.IConnectionHandler;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
@Mod(modid = "ComeCloser ", name = "ComeCloser ", version = "0.1.3")
 @NetworkMod(channels = { ComeCloser.CHANNEL }, clientSideRequired = true, serverSideRequired = false, connectionHandler = ComeCloser.class, packetHandler = ComeCloser.class)
 public class ComeCloser extends DummyModContainer implements IPacketHandler, IConnectionHandler
 {
     public static ComeCloser Instance;
     public static final String CHANNEL = "ComeCloser";
     // Variables //
     static Configuration config = new Configuration(new File(cpw.mods.fml.common.Loader.instance().getConfigDir(), "ComeCloser.cfg"));
    public static float standingRange = 64f;
    public static float sneakRange = 32f;
 
     @PreInit
     public void preInit(FMLPreInitializationEvent event)
     {
         // // Config ////
         if (event.getSide() == Side.SERVER)
         {
             config.load();
            sneakRange = Integer.parseInt(config.get(Configuration.CATEGORY_GENERAL, "SneakRange", 32).value);
            standingRange = Integer.parseInt(config.get(Configuration.CATEGORY_GENERAL, "NormalRange", 64).value);
             config.save();
         }
 
     }
 
     @SideOnly(Side.CLIENT)
     public void changeRangeClient(float high, float low)
     {
         RenderPlayer.NAME_TAG_RANGE = high;
         RenderPlayer.NAME_TAG_RANGE_SNEAK = low;
     }
 
     public void changeRangeServer(float high, float low)
     {
        this.sneakRange = low;
        this.standingRange = high;
         this.sendPacket();
     }
 
     public void sendPacket()
     {
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         DataOutputStream data = new DataOutputStream(bytes);
         try
         {
            data.writeFloat(sneakRange);
 
            data.writeFloat(standingRange);
 
             Packet250CustomPayload packet = new Packet250CustomPayload();
             packet.channel = CHANNEL;
             packet.data = bytes.toByteArray();
             packet.length = packet.data.length;
 
             if (FMLCommonHandler.instance().getMinecraftServerInstance() != null)
             {
                 FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(packet);
             }
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     @Override
     public void playerLoggedIn(Player p, NetHandler netHandler, INetworkManager manager)
     {
         EntityPlayer player = netHandler.getPlayer();
         if (player != null && !player.worldObj.isRemote)
         {
            player.sendChatToPlayer("ComeCloser: TagRange:" + sneakRange + "|" + standingRange);
         }
         sendPacket();
     }
 
     @Override
     public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void connectionClosed(INetworkManager manager)
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
     {
         sendPacket();
 
     }
 
     @Override
     public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
     {
         try
         {
             ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
 
             float f = data.readFloat();
             float f2 = data.readFloat();
            this.changeRangeClient(f2, f);
             if (player instanceof EntityPlayer)
             {
                 World world = ((EntityPlayer) player).worldObj;
                 if (world != null && world.isRemote)
                 {
                     /* TODO remove *///((ICommandSender) player).sendChatToPlayer("ping");
                 }
             }
         }
         catch (Exception e)
         {
 
         }
 
     }
 }
