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
 import java.util.Iterator;
 
 import org.bukkit.ChatColor;
 
 public abstract class GugaAnnouncement 
 {
 	public static void SetPlugin(Guga_SERVER_MOD gugaSM)
 	{
 		plugin = gugaSM;
 	}
 	public static void StartAnnouncing()
 	{
 		plugin.scheduler.scheduleAsyncRepeatingTask(plugin, new Runnable() {
 			public void run()
 			{
 				if (messages.size() > 0)
 				{
 					String msg;
 					if (!(position < messages.size()))
 					{
 						position = 0;
 					}
 					msg=messages.get(position);
 					plugin.getServer().broadcastMessage(ChatColor.RED + msg);
 					position++;
 				}
 			}
		}, 6000, 6000);
 	}
 	public static void StopAnnouncing()
 	{
 		plugin.scheduler.cancelTask(taskID);
 	}
 	public static void AddAnnouncement(String msg)
 	{
 		messages.add(msg);
 	}
 	public static String GetAnnouncement(int index)
 	{
 		if (index < messages.size())
 		{
 			return messages.get(index);
 		}
 		else
 		{
 			return null;
 		}
 	}
 	public static boolean RemoveAnnouncement(int index)
 	{
 		if (index < messages.size())
 		{
 			messages.remove(index);
 			return true;
 		}
 		return false;
 	}
 	public static void SaveAnnouncements()
 	{
 		plugin.log.info("Saving Announcement Data...");
 		File ann = new File(announcementFile);
 		if (!ann.exists())
 		{
 			try 
 			{
 				ann.createNewFile();
 				
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		try 
 		{
 			FileWriter fStream = new FileWriter(ann, false);
 			BufferedWriter bWriter;
 			bWriter = new BufferedWriter(fStream);
 			String line;
 			Iterator<String> i = messages.iterator();
 			while (i.hasNext())
 			{
 				line = i.next();
 				bWriter.write(line);
 				bWriter.newLine();
 			}
 			
 			bWriter.close();
 			fStream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	public static void LoadAnnouncements()
 	{
 		plugin.log.info("Loading Announcement Data...");
 		File ann = new File(announcementFile);
 		if (!ann.exists())
 		{
 			try 
 			{
 				ann.createNewFile();
 				return;
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 				return;
 			}
 		}
 		else
 		{
 			try 
 			{
 				FileInputStream fRead = new FileInputStream(ann);
 				DataInputStream inStream = new DataInputStream(fRead);
 				BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));		
 				String line;
 				try {
 					while ((line = bReader.readLine()) != null)
 					{
 						AddAnnouncement(line);
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
 	}
 	private static ArrayList<String> messages = new ArrayList<String>();
 	private static int position = 0;
 	private static int taskID;
 	private static String announcementFile = "plugins/Announcements.dat";
 	private static Guga_SERVER_MOD plugin;
 }
