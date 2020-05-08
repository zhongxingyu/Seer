 package ocelot.mods.utm.common.network;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 
 import ocelot.mods.utm.client.gui.GUIPrototypeSolarFurnace;
 import ocelot.mods.utm.common.entity.TileBase;
 import ocelot.mods.utm.common.entity.TilePrototypeSolarFurnace;
 import ocelot.mods.utm.common.gui.ContainerPrototypeSolarFurnace;
 import ocelot.mods.utm.common.gui.UTMContainer;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.Player;
 
 public class NetworkHandler implements IPacketHandler, IGuiHandler
 {
 	private PacketHandler handler = new PacketHandler();
 
 	@Override
 	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 		if ((tileEntity instanceof TileBase))
 		{
 			TileBase oTE = (TileBase) tileEntity;
 
 			switch (ID)
 			{
 			case 1:
 			{
 				TilePrototypeSolarFurnace PSFTE = (TilePrototypeSolarFurnace)oTE;
 				return new ContainerPrototypeSolarFurnace(PSFTE, player.inventory);
 			}
 			case 2:
 
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
 		System.out.println(ID);
 		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
 		if ((tileEntity instanceof TileBase))
 		{
 			TileBase oTE = (TileBase) tileEntity;
 			switch (ID)
 			{
 			case 1:
 			{
				return new GUIPrototypeSolarFurnace(new UTMContainer(oTE, player.inventory), oTE, "textures/gui/PrototypeSolarfurnace.png");
 			}
 			case 2:
 
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
 	{
 		int handler;
 		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
 		EntityPlayer ep = (EntityPlayer) player;
 		World world = ep.worldObj;
 		try
 		{
 			handler = inputStream.readInt();
 
 			switch (handler)
 			{
 			case 1:
 				this.handler.handelFacing(inputStream, world);
 				break;
 			}
 		}
 		catch (IOException c)
 		{
 			c.printStackTrace();
 		}
 
 	}
 
 }
