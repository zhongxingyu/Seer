 package slimevoid.tmf.proxy;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.NetHandler;
 import net.minecraft.network.packet.Packet1Login;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import slimevoid.lib.ICommonProxy;
 import slimevoid.lib.IPacketHandling;
import slimevoid.lib.network.PacketIds;
 import slimevoid.tmf.network.CommandLib;
 import slimevoid.tmf.network.CommonPacketHandler;
 import slimevoid.tmf.network.handlers.PacketMotionSensorHandler;
 import slimevoid.tmf.network.packets.executors.MotionSensorSweepExecutor;
 import slimevoid.tmf.tickhandlers.MiningHelmetTickHandler;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 public class CommonProxy implements ICommonProxy {
 
 	@Override
 	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
 			int x, int y, int z) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
 			int x, int y, int z) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void preInit() {
 		CommonPacketHandler.init();
		PacketMotionSensorHandler packetMiningEntityHandler = new PacketMotionSensorHandler();
		packetMiningEntityHandler.registerPacketHandler(CommandLib.PLAY_MOTION_SWEEP, new MotionSensorSweepExecutor());
		CommonPacketHandler.registerPacketHandler(PacketIds.ENTITY, packetMiningEntityHandler);
 	}
 
 	@Override
 	public void registerTickHandler() {
 		TickRegistry.registerTickHandler(new MiningHelmetTickHandler(), Side.SERVER);
 	}
 
 	@Override
 	public void registerRenderInformation() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public String getMinecraftDir() {
 		return "./";
 	}
 
 	@Override
 	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getBlockTextureFromMetadata(int meta) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void onPacketData(INetworkManager manager,
 			Packet250CustomPayload packet, Player player) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public IPacketHandling getPacketHandler() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void registerTileEntitySpecialRenderer(
 			Class<? extends TileEntity> clazz) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void displayTileEntityGui(EntityPlayer entityplayer,
 			TileEntity tileentity) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public World getWorld() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public World getWorld(NetHandler handler) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public EntityPlayer getPlayer() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void login(NetHandler handler, INetworkManager manager,
 			Packet1Login login) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
