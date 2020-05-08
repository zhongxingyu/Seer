 package com.vloxlands.net.packet;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Arrays;
 
 import com.vloxlands.game.world.Island;
 import com.vloxlands.util.Compressor;
 import com.vloxlands.util.MapAssistant;
 
 /**
  * @author Dakror
  */
 public class Packet9Island extends Packet
 {
 	byte[] islandData;
 	
 	public Packet9Island(Island island)
 	{
 		super(9);
 		
 		try
 		{
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			MapAssistant.saveIsland(island, baos);
 			islandData = Compressor.compress(baos.toByteArray());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public Packet9Island(byte[] data)
 	{
 		super(9);
 		islandData = Arrays.copyOfRange(data, 1, data.length);
 	}
 	
 	public Island getIsland()
 	{
		return MapAssistant.loadIsland(Compressor.decompress(islandData));
 	}
 	
 	@Override
 	protected byte[] getPacketData()
 	{
 		return islandData;
 	}
 }
