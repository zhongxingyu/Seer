 package au.com.addstar.pandora.modules;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.permissions.Permissible;
 
 import au.com.addstar.pandora.MasterPlugin;
 import au.com.addstar.pandora.Module;
 
 public class KeywordFilter implements Module, Listener
 {
 	private HashMap<Pattern, String> mPatterns = new HashMap<Pattern, String>();
 	private MasterPlugin mPlugin;
 	
 	@Override
 	public void onEnable() 
 	{
 		mPatterns.clear();
 		
 		InputStream input = null;
 		try
 		{
 			File onDisk = new File(mPlugin.getDataFolder(), "keywords.txt"); 
 			if(onDisk.exists())
 				input = new FileInputStream(onDisk);
 			else
 				input = mPlugin.getResource("keywords.txt");
 			
 			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 			int lineNo = 0;
 			while(reader.ready())
 			{
 				++lineNo;
 				String line = reader.readLine();
 				if(line.startsWith("#") || line.trim().isEmpty())
 					continue;
 				
 				String regex, colourString;
 			
 				if(line.contains(">"))
 				{
 					int pos = line.lastIndexOf('>');
 					regex = line.substring(0, pos).trim();
 					colourString = line.substring(pos + 1).trim();
 				}
 				else
 				{
 					regex = line.trim();
 					colourString = ChatColor.GOLD.getChar() + "";
 				}
 
 				Pattern pattern = null;
 				try
 				{
 					pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
 				}
 				catch(PatternSyntaxException e)
 				{
 					mPlugin.getLogger().warning("[KeywordFilter] Invalid regex: \"" + regex + "\" at line " + lineNo);
 					continue;
 				}
 				
 				StringBuilder colour = new StringBuilder(); 
 				for(int i = 0; i < colourString.length(); ++i)
 				{
 					char c = colourString.charAt(i);
 					ChatColor col = ChatColor.getByChar(c);
 					
 					if(col == null)
 					{
 						mPlugin.getLogger().warning("[KeywordFilter] Invalid colour code: \'" + col + "\' at line " + lineNo);
 						continue;
 					}
 					
 					colour.append(col.toString());
 				}
 				
 				mPatterns.put(pattern, colour.toString());
 			}
 			
 			input.close();
 		}
 		catch(FileNotFoundException e) 
 		{ 
 			// Not possible
 		}
 		catch(IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 		
 	}
 
 	@Override
 	public void onDisable() {}
 
 	@Override
 	public void setPandoraInstance( MasterPlugin plugin ) 
 	{
 		mPlugin = plugin;
 	}
 
 	@Override
 	public String getName()
 	{
 		return "Important Keyword Filter";
 	}
 
	// This level handles removing recipients
 	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
 	private void onChat(AsyncPlayerChatEvent event)
 	{
 		String newMessage = highlightKeywords(event.getMessage(), ChatColor.getLastColors(event.getFormat()));
 		if(newMessage == null)
 			return;
 		
 		for(Permissible permissible : Bukkit.getPluginManager().getPermissionSubscriptions("pandora.keyword-filter.listen"))
 		{
 			if(!(permissible instanceof Player))
 				continue;
 			
 			event.getRecipients().remove(permissible);
		}
	}
	
	// This one was added because the format is not always set in lowest priority level depending on the load order of plugins.
	// And since by the agreement of MONITOR priority, it does not modify the event at all, which is why we have both levels
	
	// This level handles sending the fake messages
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	private void onChatResult(AsyncPlayerChatEvent event)
	{
		String newMessage = highlightKeywords(event.getMessage(), ChatColor.getLastColors(event.getFormat()));
		if(newMessage == null)
			return;
		
		for(Permissible permissible : Bukkit.getPluginManager().getPermissionSubscriptions("pandora.keyword-filter.listen"))
		{
			if(!(permissible instanceof Player))
				continue;
			
 			((Player)permissible).sendMessage(String.format(event.getFormat(), event.getPlayer().getDisplayName(), newMessage));
 		}
 	}
 	
 	private String highlightKeywords(String message, String defaultColour)
 	{
 		if(defaultColour.isEmpty())
 			defaultColour = ChatColor.RESET.toString();
 		
 		boolean matched = false;
 		for(Entry<Pattern, String> entry : mPatterns.entrySet())
 		{
 			Matcher m = entry.getKey().matcher(message);
			String modified = message;
 			
 			int offset = 0;
 			
 			while(m.find())
 			{
 				String currentColour = ChatColor.getLastColors(message.substring(0, m.end()));
 				if(currentColour.isEmpty())
 					currentColour = defaultColour;
 				
				modified = modified.substring(0,m.start() + offset) + entry.getValue() + m.group(0) + currentColour + modified.substring(m.end() + offset);
 				offset += entry.getValue().length() + currentColour.length();
 				matched = true;
 			}
			
			message = modified;
 		}
 		
 		if(matched)
 			return message;
 		
 		return null;
 	}
 	
 }
