 package aidancbrady.server;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Map;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 public class FileHandler
 {
 	public static void read()
 	{
 		try {
 			File file = new File(getHomeDirectory() + File.separator + "Server.txt");
 			
 			if(!file.exists())
 			{
 				return;
 			}
 			
 			BufferedReader reader = new BufferedReader(new FileReader(file));
 			
 			String readingLine;
 			
 			while((readingLine = reader.readLine()) != null)
 			{
 				String[] lineSplit = readingLine.split(":");
 				
 				String username = lineSplit[0];
 				ArrayList<String> messagesList = new ArrayList<String>();
 				
 				if(lineSplit.length == 2 && lineSplit[1] != "")
 				{
 					String[] messagesSplit = lineSplit[1].split("/");
 					
 					for(String s : messagesSplit)
 					{
 						messagesList.add(s);
 					}
 				}
 				
 				ServerCore.instance().cachedUsers.put(username, new User(username, messagesList));
 			}
 			
 			reader.close();
 		} catch(Exception e) {
 			System.err.println("Error while reading from Server.txt file:");
 			e.printStackTrace();
 		}
 	}
 	
 	public static void write()
 	{
 		try {
 			File file = new File(getHomeDirectory() + File.separator + "Server.txt");
 			
 			if(file.exists())
 			{
 				file.delete();
 			}
 			
 			file.createNewFile();
 			
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 			
 			for(Map.Entry<String, User> entry : ServerCore.instance().cachedUsers.entrySet())
 			{
 				StringBuilder builder = new StringBuilder(entry.getKey() + ":");
 				
 				for(String message : entry.getValue().messages)
 				{
 					builder.append(message + "/");
 				}
 				
 				String toWrite = builder.toString().trim();
 				
 				if(toWrite.charAt(toWrite.length()-1) == '/')
 				{
 					toWrite = toWrite.substring(0, toWrite.length()-1);
 				}
 				
 				writer.append(toWrite);
 				writer.newLine();
 			}
 			
 			writer.flush();
 			writer.close();
 		} catch(Exception e) {
 			System.err.println("Error while writing to Server.txt file:");
 			e.printStackTrace();
 		}
 	}
 	
 	public static String getHomeDirectory()
 	{
 		return System.getProperty("user.home");
 	}
 	
 	public static void saveDiscussion(String discussionName)
 	{
 		try {
 			File dir = new File(getHomeDirectory() + "/Documents/Discussions");
 			
 			if(!dir.exists())
 			{
 				System.out.println("Yes");
 				dir.mkdir();
 			}
 			
 			File file = new File(getHomeDirectory() + "/Documents/Discussions/" + discussionName + ".disc");
 			
 			if(file.exists())
 			{
 				file.delete();
 			}
 			
 			file.createNewFile();
 			
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 			
 			writer.append(ServerCore.instance().theGui.chatBox.getText());
 			
 			writer.flush();
 			writer.close();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void loadDiscussion(File file)
 	{
 		try {
 			if(!file.exists())
 			{
 				return;
 			}
 			
 			if(!file.getAbsolutePath().endsWith(".disc"))
 			{
 				JOptionPane.showMessageDialog(ServerCore.instance().theGui, "Please select a valid '.disc' discussion file to load.", "Warning", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 			
 			StringBuilder builder = new StringBuilder();
 			
 			BufferedReader reader = new BufferedReader(new FileReader(file));
 			
 			String readingLine;
 			
 			while((readingLine = reader.readLine()) != null)
 			{
 				builder.append(readingLine);
 				builder.append("\n");
 			}
 			
 			ServerCore.instance().updateDiscussion(file.getName().replace(".", ":").split(":")[0]);
 			ServerCore.instance().theGui.chatBox.setText(builder.toString());
 			ServerCore.instance().syncChat();
 			
 			reader.close();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
