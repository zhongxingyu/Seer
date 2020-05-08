 package strimy.bukkit.plugins.localauth;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class PlayerManagement implements CommandExecutor
 {
 	HashMap<String, String> listPlayers = new HashMap<String, String>();
 	LocalAuth plugin;
 	Element root;
 	Document doc;
 	
 	public PlayerManagement(LocalAuth plugin)
 	{
 		this.plugin = plugin;
 		LoadXmlDocument();
 	}
 	
 	private void LoadXmlDocument()
 	{
		String path = plugin.getDataFolder().getParent() + "\\players.xml";
 		if(new File(path).exists())
 		{
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db;
 			try 
 			{
 				db = dbf.newDocumentBuilder();
 				doc = db.parse(path);
 				root = doc.getDocumentElement();
 				NodeList playerNodes = root.getElementsByTagName("player");
 				
 				for (int i = 0; i < playerNodes.getLength(); i++) 
 				{
 					Node item = playerNodes.item(i);
 					NamedNodeMap attribs = item.getAttributes();
 					String name = attribs.getNamedItem("username").getTextContent();
 					String password = attribs.getNamedItem("password").getTextContent();
 					
 					listPlayers.put(name, password);
 				}
 			} 
 			catch (ParserConfigurationException e) 
 			{
 				e.printStackTrace();
 			} 
 			catch (SAXException e) 
 			{
 				e.printStackTrace();
 			} catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		else 
 		{
 			CreateXmlDocument();
 		}
 		SaveXml();
 	}
 	
 	private void CreateXmlDocument()
 	{
 		DocumentBuilderFactory factory   = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder;
 		try 
 		{
 			builder = factory.newDocumentBuilder();
 			DOMImplementation impl = builder.getDOMImplementation();
 			
 			Document doc = impl.createDocument(null, null, null);
 			doc.createElement("players");
 			SaveXml();
 			
 		} 
 		catch (ParserConfigurationException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 
 	public boolean CheckPassword(String username, String password)
 	{
 		byte[] uniqueKey = password.getBytes();
         String hash      = null;
 
 		try
         {
             hash = bytes2Hex(MessageDigest.getInstance("MD5").digest(uniqueKey));
     		if(listPlayers.containsKey(username))
     		{
     			return listPlayers.get(username).equals(hash);
     		}
         }
         catch (NoSuchAlgorithmException e)
         {
             throw new Error("No MD5 support in this VM.");
         }
 
 		return false;
 	}
 	
 	public void createUser(CommandSender sender, String username, String password)
 	{
 		byte[] uniqueKey = password.getBytes();
         String hash      = null;
         
 		try
         {
             hash = bytes2Hex(MessageDigest.getInstance("MD5").digest(uniqueKey));
     		if(listPlayers.containsKey(username) && sender != null)
     		{
     			sender.sendMessage("User already exists");
     		}
     		else
     		{
     			Element playerElem = doc.createElement("player");
     			
     			playerElem.setAttribute("username", username);
     			playerElem.setAttribute("password", hash);
     			
     			root.appendChild(playerElem);
     			listPlayers.put(username, hash);
     			SaveXml();
     			if(sender instanceof Player)
     				sender.sendMessage(ChatColor.GOLD + "User "+ username +" added");
     			
     			plugin.Print("User "+ username +" added");
     		}
         }
         catch (NoSuchAlgorithmException e)
         {
             throw new Error("No MD5 support in this VM.");
         }
 	}
 	
 	public void deleteUser(CommandSender sender, String username)
 	{
 		NodeList playerNodes = root.getElementsByTagName("player");
 		
 		for (int i = 0; i < playerNodes.getLength(); i++) 
 		{
 			Node item = playerNodes.item(i);
 			NamedNodeMap attribs = item.getAttributes();
 			String name = attribs.getNamedItem("username").getTextContent();
 			
 			if(name.equals(username))
 			{
 				root.removeChild(item);
 				listPlayers.remove(name);
 				
 				if(sender instanceof Player)
 					sender.sendMessage(ChatColor.GOLD + "User deleted : " + username);
 				
 				plugin.Print("User deleted : " + username);
 				SaveXml();
 				return;
 			}
 		}
 	}
 	
 	public void changePassword(CommandSender sender, String username, String newPassword)
 	{
 		if(listPlayers.containsKey(username))
 		{
 			String hash = getMD5(newPassword);
 			listPlayers.remove(username);
 			listPlayers.put(username, hash);
 			
 			deleteUser(sender, username);
 			createUser(sender, username, newPassword);
 			if(sender instanceof Player)
 			{
 				sender.sendMessage("Password changed for user" + ((Player)sender).getDisplayName());
 			}
 		}
 	}
 	
 	private String bytes2Hex(byte[] data)
 	{
 		StringBuffer sb = new StringBuffer();
         for (int i = 0; i < data.length; i++) {
           sb.append(Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1));
         }
         
         return sb.toString();
 	}
 	
 	private String getMD5(String str)
 	{
 		try 
 		{
 			return bytes2Hex(MessageDigest.getInstance("MD5").digest(str.getBytes()));
 		} 
 		catch (NoSuchAlgorithmException e) 
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private void SaveXml()
 	{
 		String path = plugin.getDataFolder().getParent() + "\\players.xml";
 		 try {
 		        // Prepare the DOM document for writing
 		        Source source = new DOMSource(doc);
 
 		        // Prepare the output file
 		        File file = new File(path);
 		        Result result = new StreamResult(file);
 
 		        // Write the DOM document to the file
 		        Transformer xformer = TransformerFactory.newInstance().newTransformer();
 		        xformer.transform(source, result);
 		    } catch (TransformerConfigurationException e) {
 		    } catch (TransformerException e) {
 		    }
 
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
 			String[] arg3) {
 		if(sender.isOp())
 		{
 			if(arg3.length == 0)
 			{
 				sender.sendMessage(ChatColor.RED + "Command arguments are : add, del, setpassword");
 			}
 			else if(arg3[0].equals("add"))
 			{
 				if(arg3.length <= 2)
 				{
 					sender.sendMessage("You must enter an username followed by a password");
 				}
 				else
 				{
 					createUser(sender, arg3[1], arg3[2]);
 				}
 			}
 			else if(arg3[0].equals("delete"))
 			{
 				if(arg3.length >= 1)
 				{
 					for (int i = 0; i < (arg3.length - 1); i++) 
 					{
 						deleteUser(sender, arg3[i+1]);
 					}
 					
 				}
 			}
 			else if(arg3[0].equals("list"))
 			{
 				int i = 0;
 				for (String username : listPlayers.keySet()) 
 				{
 					i++;
 					sender.sendMessage(i + " : " + username);
 				}
 			}
 			else if(arg3[0].equals("setpassword"))
 			{
 				
 			}
 			else if(arg3[0].equals("reload"))
 			{
 				sender.sendMessage(ChatColor.GREEN + "Reloading user list...");
 				LoadXmlDocument();
 			}
 		}
 		else
 		{
 			sender.sendMessage("You don't have the permission to use this command");
 		}
 		return true;
 	}
 }
