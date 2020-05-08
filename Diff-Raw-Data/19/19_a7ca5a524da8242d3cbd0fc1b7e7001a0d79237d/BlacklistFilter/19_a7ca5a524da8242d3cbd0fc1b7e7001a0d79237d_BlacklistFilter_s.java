 package no.runsafe.nchat.antispam;
 
 import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IConsole;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.nchat.Core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class BlacklistFilter implements ISpamFilter, IConfigurationChanged
 {
 	public BlacklistFilter(Core nChat, IConsole output)
 	{
 		this.output = output;
 		this.filePath = new File(nChat.getDataFolder(), "blacklist.txt");
 	}
 
 	@Override
 	public String processString(IPlayer player, String message)
 	{
 		// Parse the message to lowercase to prevent bypassing that way.
 		String lowerMessage = message.toLowerCase();
 
 		// Check each node, if we find it in the string, cancel the message.
 		for (String blacklisted : this.blacklist)
 		{
 			if (lowerMessage.contains(blacklisted))
 			{
 				player.sendColouredMessage("&cYour last message was blacklisted and not sent, sorry.");
 				return null;
 			}
 		}
 
 		return message;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration iConfiguration)
 	{
 		this.blacklist.clear(); // Clear the current blacklist.
 
 		// Check if the file exists.
 		if (!filePath.exists())
 		{
 			// The file does not exist, lets try creating it.
 			try
 			{
 				if (!filePath.getParentFile().isDirectory())
 				{
 					this.output.logWarning("Unable to locate plugin data folder at: " + filePath.getParentFile());
 					return;
 				}
 				if (!filePath.createNewFile())
 				{
 					this.output.logWarning("Unable to create blacklist file at: " + filePath);
 					return;
 				}
 			}
 			catch (IOException exception)
 			{
 				this.output.logWarning("Unable to create blacklist file due to exception:");
 				exception.printStackTrace();
 				return;
 			}
 		}
 
 		try
 		{
 			BufferedReader reader = new BufferedReader(new FileReader(filePath));
 			String line;
 			while (true)
 			{
 				line = reader.readLine();
 				if (line == null)
 					break;
 				// Add every line we find to the blacklist array, converting it to lowercase.
 				this.blacklist.add(line.toLowerCase());
 			}
 			output.logInformation("Loaded %s blacklist filters from file.", blacklist.size());
 		}
 		catch (Exception exception)
 		{
 			// We should not get here, but we catch it just in-case.
 			this.output.logWarning("Unexpected exception prevented blacklist loading:");
 			exception.printStackTrace();
 		}
 	}
 
 	private List<String> blacklist = new ArrayList<String>();
 	private File filePath;
 	private IConsole output;
 }
