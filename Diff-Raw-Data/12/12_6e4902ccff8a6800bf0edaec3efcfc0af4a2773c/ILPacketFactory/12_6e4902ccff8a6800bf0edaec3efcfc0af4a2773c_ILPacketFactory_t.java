 package jig.ironLegends.oxide.packets;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Collection;
 import java.util.logging.Logger;
 
 import jig.ironLegends.CommandState;
 import jig.ironLegends.oxide.client.ClientInfo;
 import jig.ironLegends.oxide.exceptions.IncompletePacketException;
 import jig.ironLegends.oxide.exceptions.IronOxideException;
 import jig.ironLegends.oxide.exceptions.PacketFormatException;
 import jig.ironLegends.oxide.util.PacketBuffer;
 import jig.ironLegends.oxide.util.PacketList;
 
 
 /**
  * @author Travis Hall
  */
 public class ILPacketFactory {
 	
 	
 	public static ILPacket getPacketFromData(ByteBuffer buffer, String id)
 			throws PacketFormatException, IOException 
 	{	
 		
 		PacketBuffer pb = new PacketBuffer(buffer);
 		
 		int protocolID = buffer.getInt(); // Strip the protocol ID
 		
 		if (protocolID != ILPacket.IL_PROTOCOL_ID) {
 			throw new PacketFormatException("Unknown packet protocol. Header: 0x"
 					+ Integer.toHexString(protocolID));
 		}
 		
 		String identifier = pb.getString();
 		
 		if (id != null) {
 			if (identifier.compareTo(id) != 0) {
 				throw new PacketFormatException("Incorrect identifier");
 			}
 		}
 		
		String senderID = pb.getString(); 
			
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(baos);
 		
		dos.writeBytes(identifier + "\0");
		dos.writeBytes(senderID + "\0");
 		
 		byte[] pd = new byte[9];
 		buffer.get(pd, 0, pd.length);
 		
 		dos.write(pd);
 		dos.flush();
 		dos.close();
 		
 		byte[] protocolData = baos.toByteArray();
 		
 		int header = buffer.get();
 		
 		byte[] contentData = new byte[buffer.remaining()];
 		buffer.get(contentData);
 		buffer.rewind();
 		
 		switch(header) {
 		case ILPacket.IL_SERVER_ADVERTISEMENT_HEADER:
 			return new ILServerAdvertisementPacket(protocolData, contentData);
 		case ILPacket.IL_LOBBY_DATA_HEADER:
 			return new ILLobbyPacket(protocolData, contentData);
 		case ILPacket.IL_EVENT_HEADER:
 			return new ILEventPacket(protocolData, contentData);
 		case ILPacket.IL_READY_HEADER:
 			return new ILReadyPacket(protocolData);
 		case ILPacket.IL_LOBBY_EVENT_HEADER:
 			return new ILLobbyEventPacket(protocolData, contentData);
 		case ILPacket.IL_START_GAME_HEADER:
 			return new ILStartGamePacket(protocolData, contentData);
 		case ILPacket.IL_GAME_DATA_HEADER:
 			return new ILGameStatePacket(protocolData, contentData);
 		
 		default:
 			throw new PacketFormatException("Unknown packet. Header: 0x"
 					+ Integer.toHexString(header));
 		}
 	}
 	
 	public static ILPacket reassemblePacket(PacketList packetList, String identifier)
 			throws IOException, IronOxideException
 	{
 		ILPacket firstPacket = packetList.list[0];
 		
 		if (firstPacket == null) {
 			throw new IncompletePacketException();
 		}
 		
 		for (int i = 1; i < packetList.list.length; i++) {
 			ILPacket splitPacket = packetList.list[i];
 			
 			if (splitPacket == null) {
 				throw new IncompletePacketException();
 			}
 			
 			firstPacket.getContent().concatenatePacketBuffer(splitPacket.getContent());
 		}
 		
 		return ILPacketFactory.getPacketFromData(firstPacket.getByteBuffer(), identifier);
 	}
 
 	/**
 	 * @param packetID
 	 * @param numberOfPlayers
 	 * @param maxPlayers
 	 * @param serverName
 	 * @param map
 	 * @param version
 	 * @return
 	 */
 	public static ILServerAdvertisementPacket newAdvertisementPacket(
 			int packetID, String identifier, String senderID, byte numberOfPlayers, byte maxPlayers,
 			String serverName, String map, String version) {
 	
 		byte[] protocolData = null;
 		try {
 			protocolData = getProtocolData(packetID, identifier, senderID);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		byte[] contentBytes = null;
 		try {
 			contentBytes = ILServerAdvertisementPacket.createContent(numberOfPlayers, maxPlayers, serverName, map, version);
 		} catch (IOException e) {
 //			Logger.getLogger()
 			e.printStackTrace();
 		}
 		
 		return new ILServerAdvertisementPacket(protocolData, contentBytes);
 	}
 
 	public static ILLobbyPacket newLobbyPacket(int packetID, String identifier, String senderID, byte numberOfPlayers, String serverName, String map, Collection<ClientInfo> clients) {
 		byte[] protocolData = null;
 		try {
 			protocolData = getProtocolData(packetID, identifier, senderID);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		try {
 			return new ILLobbyPacket(protocolData, numberOfPlayers, serverName, map, clients);
 		} catch (IOException e) {
 			Logger.getLogger("global").warning(e.toString());
 			return null;
 		}
 	}
 	
 	public static ILLobbyEventPacket newLobbyEventPacket(int packetID, String identifier, String senderID, String name, byte team) {
 		byte[] protocolData = null;
 		try {
 			protocolData = getProtocolData(packetID, identifier, senderID);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		try {
 			return new ILLobbyEventPacket(protocolData, name, team);
 		} catch (IOException e) {
 			Logger.getLogger("global").warning(e.toString());
 			return null;
 		}
 	}
 	
 	public static ILEventPacket newEventPacket(int packetID, String identifier, String senderID, CommandState event) {
 		byte[] protocolData = null;
 		try {
 			protocolData = getProtocolData(packetID, identifier, senderID);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			return new ILEventPacket(protocolData, event);
 		} catch (IOException e) {
 			Logger.getLogger("global").warning(e.toString());
 			return null;
 		}
 	}
 	
 	/**
 	 * @param packetID
 	 * @return
 	 */
 	public static ILReadyPacket newReadyPacket(int packetID, String identifier, String senderID) {
 		byte[] protocolData = null;
 		try {
 			protocolData = getProtocolData(packetID, identifier, senderID);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		return new ILReadyPacket(protocolData);
 	}
 	
 	/**
 	 * @param packetID
 	 * @return
 	 */
 	public static ILStartGamePacket newStartGamePacket(int packetID, String identifier, String senderID) {
 		byte[] protocolData = null;
 		try {
 			protocolData = getProtocolData(packetID, identifier, senderID);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		return new ILStartGamePacket(protocolData);
 	}
 	
 	/**
 	 * @param packetID
 	 * @return
 	 * @throws IOException 
 	 */
 	private static byte[] getProtocolData(int packetID, String identifier, String senderID) throws IOException {
 		return ILPacket.createProtocolData(identifier, senderID, packetID, (byte) 0, (byte) 1, ILPacket.DEFAULT_SPLIT_SIZE);
 	}
 
 }
