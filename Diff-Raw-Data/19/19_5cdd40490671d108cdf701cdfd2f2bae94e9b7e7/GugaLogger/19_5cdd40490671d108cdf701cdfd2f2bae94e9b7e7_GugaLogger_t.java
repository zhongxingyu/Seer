 package me.Guga.Guga_SERVER_MOD;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 public class GugaLogger 
 {	
 	GugaLogger(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 		logBlockBreak = true;
 		logBlockPlace = true;
 		logBlockIgnite = true;
 	}
 	public void PrintBlockData(final Player sender, final Block block)
 	{
 		plugin.scheduler.scheduleAsyncDelayedTask(plugin, new Runnable(){
 			public void run()
 			{
 				ArrayList<String> blockBreakData;
 				ArrayList<String> blockPlaceData;
 				blockBreakData = GetBlockBreakData(block);
 				blockPlaceData = GetBlockPlaceData(block);
 				Iterator<String> i = blockPlaceData.iterator();
 				while (i.hasNext())
 				{
 					String e = i.next();
 					String splitted[] = e.split(";");
 					String msg = "BLOCK_PLACE: name=" + splitted[1] + "  id=" + splitted[2] + " time=" + splitted[0].substring(0, 20);
 					sender.sendMessage(msg);
 				}
 				i = blockBreakData.iterator();
 				while (i.hasNext())
 				{
 					String e = i.next();
 					String splitted[] = e.split(";");
 					String msg = "BLOCK_BREAK: name=" + splitted[1] + "  id=" + splitted[2] + " time=" + splitted[0].substring(0, 20);
 					sender.sendMessage(msg);
 				}
 			}
 		});
 	}
 	public ArrayList<String> GetBlockBreakData(Block block)
 	{
 		ArrayList<String> dataBuffer = new ArrayList<String>();
 		int blockX = block.getX();
 		int blockY = block.getY();
 		int blockZ = block.getZ();
 		File file = new File(blockBreakFile);
 		if (!file.exists())
 		{
 			try 
 			{
 				file.createNewFile();
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			try 
 			{
 				FileInputStream fRead = new FileInputStream(file);
 				DataInputStream inStream = new DataInputStream(fRead);
 				BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));		
 				String line;
 				String []splittedLine;
 				int x;
 				int y;
 				int z;
 				try {
 					while ((line = bReader.readLine()) != null)
 					{
 						splittedLine = line.split(";");
 						x = Integer.parseInt(splittedLine[3]);
 						y = Integer.parseInt(splittedLine[4]);
 						z = Integer.parseInt(splittedLine[5]);
 						if ( (x == blockX) && (y == blockY) && (z == blockZ) )
 						{
 							dataBuffer.add(line);
 						}
 					}
 					bReader.close();
 					inStream.close();
 					fRead.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}			
 			} 
 			catch (FileNotFoundException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		return dataBuffer;
 	}
 	public ArrayList<String> GetBlockPlaceData(Block block)
 	{
 		ArrayList<String> dataBuffer = new ArrayList<String>();
 		int blockX = block.getX();
 		int blockY = block.getY();
 		int blockZ = block.getZ();
 		File file = new File(blockPlaceFile);
 		if (!file.exists())
 		{
 			try 
 			{
 				file.createNewFile();
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			try 
 			{
 				FileInputStream fRead = new FileInputStream(file);
 				DataInputStream inStream = new DataInputStream(fRead);
 				BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));		
 				String line;
 				String []splittedLine;
 				int x;
 				int y;
 				int z;
 				try {
 					while ((line = bReader.readLine()) != null)
 					{
 						splittedLine = line.split(";");
 						x = Integer.parseInt(splittedLine[3]);
 						y = Integer.parseInt(splittedLine[4]);
 						z = Integer.parseInt(splittedLine[5]);
 						if ( (x == blockX) && (y == blockY) && (z == blockZ) )
 						{
 							dataBuffer.add(line);
 						}
 					}
 					bReader.close();
 					inStream.close();
 					fRead.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}			
 			} 
 			catch (FileNotFoundException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		return dataBuffer;
 	}
	public void LogBlockBreak(final BlockBreakEvent e, final int typeID)
 	{
 		if (logBlockBreak)
 		{
 			plugin.scheduler.scheduleAsyncDelayedTask(plugin, new Runnable() {
 				public void run()
 				{
 					File blockBreak = new File(blockBreakFile);
 					if (!blockBreak.exists())
 					{
 						try 
 						{
 							blockBreak.createNewFile();
 							
 						} 
 						catch (IOException ex) 
 						{
 							ex.printStackTrace();
 						}
 					}
 					try 
 					{
 						FileWriter fStream = new FileWriter(blockBreak, true);
 						BufferedWriter bWriter;
 						bWriter = new BufferedWriter(fStream);
 						String line;
						//int typeID = e.getBlock().getTypeId();
 						int x = e.getBlock().getX();
 						int y = e.getBlock().getY();
 						int z = e.getBlock().getZ();
 						String pName = e.getPlayer().getName();
 						Date now = new Date();
 						line = now + ";" + pName + ";" + typeID + ";" + x + ";" + y + ";" + z;
 						bWriter.write(line);
 						bWriter.newLine();
 						bWriter.close();
 						fStream.close();
 					} catch (IOException ex) {
 						ex.printStackTrace();
 					}
 				}
 			}
 			
 			);	
 		}
 	}
 	public void LogBlockPlace(final BlockPlaceEvent e)
 	{
 		if (logBlockPlace)
 		{
 			plugin.scheduler.scheduleAsyncDelayedTask(plugin, new Runnable() {
 				public void run()
 				{
 					File blockPlace = new File(blockPlaceFile);
 					if (!blockPlace.exists())
 					{
 						try 
 						{
 							blockPlace.createNewFile();
 							
 						} 
 						catch (IOException ex) 
 						{
 							ex.printStackTrace();
 						}
 					}
 					try 
 					{
 						FileWriter fStream = new FileWriter(blockPlace, true);
 						BufferedWriter bWriter;
 						bWriter = new BufferedWriter(fStream);
 						String line;
 						int typeID = e.getBlock().getTypeId();
 						int x = e.getBlock().getX();
 						int y = e.getBlock().getY();
 						int z = e.getBlock().getZ();
 						String pName = e.getPlayer().getName();
 						Date now = new Date();
 						line = now + ";" + pName + ";" + typeID + ";" + x + ";" + y + ";" + z;
 						bWriter.write(line);
 						bWriter.newLine();
 						bWriter.close();
 						fStream.close();
 					} catch (IOException ex) {
 						ex.printStackTrace();
 					}
 				}
 			}
 			
 			);	
 		}
 	}
 	public void LogBlockIgnite(final BlockIgniteEvent e)
 	{
 		if (logBlockIgnite)
 		{
 			plugin.scheduler.scheduleAsyncDelayedTask(plugin, new Runnable() {
 				public void run()
 				{
 					File blockIgnite = new File(blockIgniteFile);
 					if (!blockIgnite.exists())
 					{
 						try 
 						{
 							blockIgnite.createNewFile();
 							
 						} 
 						catch (IOException ex) 
 						{
 							ex.printStackTrace();
 						}
 					}
 					try 
 					{
 						FileWriter fStream = new FileWriter(blockIgnite, true);
 						BufferedWriter bWriter;
 						bWriter = new BufferedWriter(fStream);
 						String line;
 						int typeID = e.getBlock().getTypeId();
 						int x = e.getBlock().getX();
 						int y = e.getBlock().getY();
 						int z = e.getBlock().getZ();
 						String pName;
 						
 						if (e.getPlayer() == null)
 						{
 							pName = "null";
 						}
 						else
 						{
 							pName = e.getPlayer().getName();
 						}
 						Date now = new Date();
 						line = now + ";" + pName + ";" + typeID + ";" + x + ";" + y + ";" + z;
 						bWriter.write(line);
 						bWriter.newLine();
 						bWriter.close();
 						fStream.close();
 					} catch (IOException ex) {
 						ex.printStackTrace();
 					}
 				}
 			}
 			
 			);	
 		}
 	}
 	
 	private boolean logBlockBreak;
 	private boolean logBlockPlace;
 	private boolean logBlockIgnite;
 	private String blockBreakFile = "plugins/BlockBreakLog.log";
 	private String blockIgniteFile = "plugins/BlockIgniteLog.log";
 	private String blockPlaceFile = "plugins/BlockPlaceLog.log";
 	private Guga_SERVER_MOD plugin;
 }
