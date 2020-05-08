 package infinitealloys.handlers;
 
 import infinitealloys.block.BlockMachine;
 import infinitealloys.core.InfiniteAlloys;
 import infinitealloys.core.WorldData;
 import infinitealloys.tile.TileEntityComputer;
 import infinitealloys.tile.TileEntityMachine;
 import infinitealloys.tile.TileEntityMetalForge;
 import infinitealloys.tile.TileEntityXray;
 import infinitealloys.util.Consts;
 import infinitealloys.util.Funcs;
 import infinitealloys.util.Point;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteStreams;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.Player;
 
 public class PacketHandler implements IPacketHandler {
 
 	private static final byte WORLD_DATA = 0;
 	private static final byte TE_SERVER_TO_CLIENT = 1;
 	private static final byte TE_CLIENT_TO_SERVER = 2;
 	private static final byte TE_JOULES = 3;
 	private static final byte COMPUTER_ADD_MACHINE = 4;
 	private static final byte OPEN_GUI = 5;
 	private static final byte SEARCH = 6;
 
 	@Override
 	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
 		ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
 		int packetIndex = data.readByte();
 		World world = ((EntityPlayer)player).worldObj;
 		switch(packetIndex) {
 			case WORLD_DATA:
 				int[] validAlloys = new int[Consts.VALID_ALLOY_COUNT];
 				for(int i = 0; i < validAlloys.length; i++)
 					validAlloys[i] = data.readInt();
 				InfiniteAlloys.instance.worldData = new WorldData(validAlloys);
 				break;
 			case TE_SERVER_TO_CLIENT:
 				int x = data.readInt();
 				int y = data.readInt();
 				int z = data.readInt();
 				TileEntity te = world.getBlockTileEntity(x, y, z);
 				if(te instanceof TileEntityMachine) {
 					int processProgress = data.readInt();
 					byte orientation = data.readByte();
 					int upgrades = data.readInt();
 					int joules = data.readInt();
 					((TileEntityMachine)te).handlePacketDataFromServer(processProgress, orientation, upgrades, joules);
 					if(te instanceof TileEntityComputer) {
 						TileEntityComputer tec = (TileEntityComputer)te;
 						tec.networkCoords.clear();
 						int networkSize = data.readInt();
 						for(int i = 0; i < networkSize; i++) {
 							int machX = data.readInt();
 							int machY = data.readInt();
 							int machZ = data.readInt();
 							tec.networkCoords.add(new Point(machX, machY, machZ));
 						}
 					}
 					else if(te instanceof TileEntityMetalForge) {
 						byte[] recipeAmts = new byte[Consts.METAL_COUNT];
 						for(int i = 0; i < recipeAmts.length; i++)
 							recipeAmts[i] = data.readByte();
 						((TileEntityMetalForge)te).handlePacketData(recipeAmts);
 					}
 					else if(te instanceof TileEntityXray) {
 						TileEntityXray tex = (TileEntityXray)te;
 						tex.clearDetectedBlocks();
 						short size = data.readShort();
 						for(int i = 0; i < size; i++) {
							System.out.println("Reading " + new Point(data.readInt(), data.readShort(), data.readInt()));
 							tex.addDetectedBlock(new Point(data.readInt(), data.readShort(), data.readInt()));
 						}
 					}
 				}
 				break;
 			case TE_CLIENT_TO_SERVER:
 				x = data.readInt();
 				y = data.readInt();
 				z = data.readInt();
 				te = world.getBlockTileEntity(x, y, z);
 				if(te instanceof TileEntityMetalForge) {
 					byte[] recipeAmts = new byte[Consts.METAL_COUNT];
 					for(int i = 0; i < recipeAmts.length; i++)
 						recipeAmts[i] = data.readByte();
 					((TileEntityMetalForge)te).handlePacketData(recipeAmts);
 				}
 				else if(te instanceof TileEntityXray) {
 					boolean searching = data.readBoolean();
 					String playerName = ((EntityPlayer)player).username;
 					short selectedButton = data.readShort();
 					((TileEntityXray)te).handlePacketDataFromClient(searching, playerName, selectedButton);
 				}
 				break;
 			case TE_JOULES:
 				x = data.readInt();
 				y = data.readInt();
 				z = data.readInt();
 				te = world.getBlockTileEntity(x, y, z);
 				if(te instanceof TileEntityMachine) {
 					int joules = data.readInt();
 					int joulesGained = data.readInt();
 					((TileEntityMachine)te).joules = joules;
 					((TileEntityMachine)te).joulesGained = joulesGained;
 				}
 				break;
 			case COMPUTER_ADD_MACHINE:
 				x = data.readInt();
 				y = data.readInt();
 				z = data.readInt();
 				te = world.getBlockTileEntity(x, y, z);
 				if(te instanceof TileEntityComputer) {
 					int machX = data.readInt();
 					int machY = data.readInt();
 					int machZ = data.readInt();
 					((TileEntityComputer)te).addMachine((EntityPlayer)player, machX, machY, machZ);
 				}
 				break;
 			case OPEN_GUI:
 				x = data.readInt();
 				y = data.readInt();
 				z = data.readInt();
 				boolean fromComputer = data.readBoolean();
 				((BlockMachine)Funcs.getBlock(world, x, y, z)).openGui(world, (EntityPlayer)player, (TileEntityMachine)world.getBlockTileEntity(x, y, z),
 						fromComputer);
 				break;
 			case SEARCH:
 				x = data.readInt();
 				y = data.readInt();
 				z = data.readInt();
 				((TileEntityXray)world.getBlockTileEntity(x, y, z)).search();
 				break;
 		}
 	}
 
 	public static Packet getWorldDataPacket() {
 		return getPacket(WORLD_DATA, InfiniteAlloys.instance.worldData.getValidAlloys());
 	}
 
 	public static Packet getTEPacketToClient(TileEntityMachine tem) {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(bos);
 		try {
 			dos.writeByte(TE_SERVER_TO_CLIENT);
 			dos.writeInt(tem.xCoord);
 			dos.writeInt(tem.yCoord);
 			dos.writeInt(tem.zCoord);
 			dos.writeInt(tem.processProgress);
 			dos.writeByte(tem.front);
 			dos.writeInt(tem.getUpgrades());
 			dos.writeInt(tem.joules);
 			if(tem instanceof TileEntityComputer) {
 				TileEntityComputer tec = (TileEntityComputer)tem;
 				dos.writeInt(tec.networkCoords.size());
 				for(Point coords : tec.networkCoords) {
 					dos.writeInt(coords.x);
 					dos.writeInt(coords.y);
 					dos.writeInt(coords.z);
 				}
 			}
 			else if(tem instanceof TileEntityMetalForge)
 				for(byte amt : ((TileEntityMetalForge)tem).recipeAmts)
 					dos.writeByte(amt);
 			else if(tem instanceof TileEntityXray) {
 				dos.writeShort(((TileEntityXray)tem).getDetectedBlocks().size());
 				for(Point p : ((TileEntityXray)tem).getDetectedBlocks()) {
					System.out.println("Writing " + p);
 					dos.writeInt(p.x);
 					dos.writeShort((short)p.y);
 					dos.writeInt(p.z);
 				}
 			}
 		}
 		catch(IOException e) {
 			e.printStackTrace();
 		}
 		Packet250CustomPayload packet = new Packet250CustomPayload("InfiniteAlloys", bos.toByteArray());
 		packet.length = bos.size();
 		return packet;
 	}
 
 	public static Packet getTEPacketToServer(TileEntityMachine tem) {
 		if(tem instanceof TileEntityMetalForge)
 			return getPacket(TE_CLIENT_TO_SERVER, tem.xCoord, tem.yCoord, tem.zCoord, ((TileEntityMetalForge)tem).recipeAmts);
 		if(tem instanceof TileEntityXray)
 			return getPacket(TE_CLIENT_TO_SERVER, tem.xCoord, tem.yCoord, tem.zCoord, ((TileEntityXray)tem).searching, ((TileEntityXray)tem).selectedButton);
 		return null;
 	}
 
 	public static Packet getTEJoulesPacket(TileEntityMachine tem) {
 		return getPacket(TE_JOULES, tem.xCoord, tem.yCoord, tem.zCoord, tem.joules, tem.joulesGained);
 	}
 
 	public static Packet getComputerPacketAddMachine(int compX, int compY, int compZ, int machX, int machY, int machZ) {
 		return getPacket(COMPUTER_ADD_MACHINE, compX, compY, compZ, machX, machY, machZ);
 	}
 
 	public static Packet getPacketOpenGui(int x, int y, int z, boolean fromComputer) {
 		return getPacket(OPEN_GUI, x, y, z, fromComputer);
 	}
 
 	public static Packet getPacketSearch(int x, int y, int z) {
 		return getPacket(SEARCH, x, y, z);
 	}
 
 	private static Packet getPacket(int id, Object... data) {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(bos);
 		try {
 			dos.writeByte(id);
 			for(Object datum : data) {
 				if(datum instanceof Byte)
 					dos.writeByte((Byte)datum);
 
 				else if(datum instanceof byte[])
 					for(byte datum2 : (byte[])datum)
 						dos.writeByte(datum2);
 
 				else if(datum instanceof Short)
 					dos.writeShort((Short)datum);
 
 				else if(datum instanceof short[])
 					for(short datum2 : (short[])datum)
 						dos.writeShort(datum2);
 
 				else if(datum instanceof Integer)
 					dos.writeInt((Integer)datum);
 
 				else if(datum instanceof int[])
 					for(int datum2 : (int[])datum)
 						dos.writeInt(datum2);
 
 				else if(datum instanceof Double)
 					dos.writeDouble((Double)datum);
 
 				else if(datum instanceof Boolean)
 					dos.writeBoolean((Boolean)datum);
 			}
 		}
 		catch(IOException e) {
 			e.printStackTrace();
 		}
 		Packet250CustomPayload packet = new Packet250CustomPayload("InfiniteAlloys", bos.toByteArray());
 		packet.length = bos.size();
 		return packet;
 	}
 }
