 package jig.ironLegends.oxide.packets;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Vector;
 
 import jig.ironLegends.oxide.util.PacketBuffer;
 
 /**
  * @author Travis Hall
  */
 public class ILStartGamePacket extends ILPacket {
 
 	public String map;
 	public int m_countPlayers;
 	public boolean m_bSinglePlayer;
 	
 	class PlayerData
 	{
 		public PlayerData(String name, int entityNumber)
 		{
 			m_name = name;
 			m_entityNumber = entityNumber;
 		}
 		
 		public PlayerData() {
 			// TODO Auto-generated constructor stub
 		}
 
 		String m_name;
 		int m_entityNumber;
 	}
 
 	public void addPlayer(String name, int entityNumber)
 	{
 		m_playerDataCol.add(new PlayerData(name, entityNumber));
 		m_countPlayers = m_playerDataCol.size();
 	}
 	public Vector<PlayerData> m_playerDataCol;
 	
 	/**
 	 * @param protocolData
 	 * @param map
 	 */
 	protected ILStartGamePacket(byte[] protocolData, String map) {
 		super(ILPacket.IL_START_GAME_HEADER, protocolData);
 		this.map = map;
 		this.m_bSinglePlayer = true;
 		this.m_playerDataCol = new Vector<PlayerData>();
 		m_countPlayers = 0;
 		//this.contentData = null;
 		//new PacketBuffer(map.getBytes());
 	}
 	
 	protected ILStartGamePacket(byte[] protocolData, byte[] contentData) {
 		super(ILPacket.IL_START_GAME_HEADER, protocolData);
 		this.contentData = new PacketBuffer(contentData);
 		
 		// unpack
 		unpack(this.contentData);
 		
 		
 		// end unpack
 		this.contentData.rewind();
 	}
 	
 	public byte[] getBytes() throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(baos);
 
 		pack(dos);
 		
 		dos.flush();
 		dos.close();
 		this.contentData = new PacketBuffer(baos.toByteArray());
 		
 		return super.getBytes();
 	}
 	protected void pack(DataOutputStream dos) throws IOException
 	{
 		dos.writeBytes(map);
 		dos.writeByte(this.m_playerDataCol.size());
 
 		for (PlayerData o : this.m_playerDataCol) {
 			dos.writeBytes(o.m_name);
 			dos.write(o.m_entityNumber);
 		}
 	}
 	
 	private void unpack(PacketBuffer contentData) {
 		map = contentData.getString();
 		m_countPlayers = contentData.getByte();
 		
 		for (int i = 0; i < m_countPlayers; ++i)
 		{
 			PlayerData o = new PlayerData();
 			o.m_name = contentData.getString();
 			o.m_entityNumber = contentData.getInt();
 			m_playerDataCol.add(o);
 		}
 		
 		// assert (m_countPlayers == m_playerDataCol.size()) 
 	}
 }
