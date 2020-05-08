 package com.vloxlands.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 
 import com.vloxlands.game.world.Island;
 import com.vloxlands.game.world.Map;
 
 public class MapAssistant
 {
 	public static void saveMap(Map m, String name)
 	{
 		try
 		{
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			for (Island island : m.getIslands())
 			{
 				saveIsland(island, baos);
 			}
 			Compressor.compressFile(new File("src/test/" + name + ".bin"), baos.toByteArray());
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static Map loadMap(String name)
 	{
 		Map map = new Map();
 		// -- bin file -- //
 		File bin = new File("src/test/" + name + ".bin");
 		byte[] data = Compressor.decompress(Compressor.getFileContentAsByteArray(bin));
 
 		int pos = 0;
 		while (pos < data.length - 1)
 		{
 			ByteBuffer bb = ByteBuffer.wrap(data, pos, 4);
 			pos += 4;
 			long length = bb.getInt();
 			byte[] ids = new byte[(int) length];
 			System.arraycopy(data, pos, ids, 0, (int) length);
 			pos += length;
 			bb = ByteBuffer.wrap(data, pos, 4);
 			pos += 4;
 			length = bb.getInt();
 			byte[] mds = new byte[(int) length];
 			System.arraycopy(data, pos, mds, 0, (int) length);
 			pos += length;
 			map.addIsland(loadIsland(ids, mds));
 		}
 		return map;
 	}
 
 	private static Island loadIsland(byte[] ids, byte[] mds)
 	{
 		Island island = new Island();
 
 		// -- IDs -- //
 		for (int i = 0; i < ids.length; i += 2)
 		{
 			int t = i / 2;
 
 			for (byte j = (byte) 0; j < ids[i]; j++)
 			{
 				short[] pos = get3DIndex(t + j);
 				island.setVoxel(pos[0], pos[1], pos[2], ids[t + 1]);
 			}
 		}
 
 		// -- Metadatas -- //
 		for (int i = 0; i < mds.length; i += 2)
 		{
 			int t = i / 2;
 
 			for (byte j = (byte) 0; j < mds[i]; j++)
 			{
 				short[] pos = get3DIndex(t + j);
 				island.setVoxelMetadata(pos[0], pos[1], pos[2], mds[t + 1]);
 			}
 		}
 		return island;
 	}
 
 	private static short[] get3DIndex(int i)
 	{
 		return new short[] { (short) Math.round(i / (float) Math.pow(Island.MAXSIZE, 2)), (short) (i / (float) Island.MAXSIZE), (short) (i % Island.MAXSIZE) };
 	}
 
 	private static void saveIsland(Island island, OutputStream os) throws Exception
 	{
 		byte[] ids = Compressor.compressRow(island.getVoxels());
 		byte[] mds = Compressor.compressRow(island.getVoxelMetadatas());
 
 		ByteBuffer bb = ByteBuffer.allocate(4);
 		bb.putInt(ids.length);
 		os.write(bb.array());
 
 		os.write(ids);
 
 		bb = ByteBuffer.allocate(4);
 		bb.putInt(mds.length);
 		os.write(bb.array());
 
 		os.write(mds);
 	}
 }
