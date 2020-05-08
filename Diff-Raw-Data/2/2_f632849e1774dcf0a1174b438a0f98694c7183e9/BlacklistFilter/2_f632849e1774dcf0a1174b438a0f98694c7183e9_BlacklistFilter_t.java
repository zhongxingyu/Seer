 package no.runsafe.nchat.antispam;
 
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.nchat.Core;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class BlacklistFilter implements ISpamFilter, IConfigurationChanged
 {
 	public BlacklistFilter(Core nChat, IOutput output)
 	{
 		this.output = output;
 		this.filePath = String.format("plugins/%s/", nChat.getName());
 	}
 
 	@Override
 	public String processString(RunsafePlayer player, String message)
 	{
 		// Parse the message to lowercase to prevent bypassing that way.
 		message = message.toLowerCase();
 
 		// Check each node, if we find it in the string, cancel the message.
 		for (String blacklisted : this.blacklist)
 			if (message.contains(blacklisted))
 				return null;
 
 		return message;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration iConfiguration)
 	{
 		this.blacklist.clear(); // Clear the current blacklist.
 		String filePath = this.filePath + "blacklist.txt";
 		File folder = new File(this.filePath);
 		File file = new File(filePath);
 
 		// Check if the file exists.
 		if (!file.exists())
 		{
 			// The file does not exist, lets try creating it.
 			try
 			{
 				if (!folder.mkdirs() || !file.createNewFile())
 				{
					this.output.warning("Unable to create blacklist file at: " + filePath);
 					return;
 				}
 			}
 			catch (IOException exception)
 			{
 				this.output.warning("Unable to create blacklist file due to exception:");
 				exception.printStackTrace();
 				return;
 			}
 		}
 
 		try
 		{
 			BufferedReader reader = new BufferedReader(new FileReader(filePath));
 			String line = reader.readLine();
 
 			// Add every line we find to the blacklist array, converting it to lowercase.
 			while (line != null)
 				this.blacklist.add(line.toLowerCase());
 		}
 		catch (Exception exception)
 		{
 			// We should not get here, but we catch it just in-case.
 			this.output.warning("Unexpected exception prevented blacklist loading:");
 			exception.printStackTrace();
 		}
 	}
 
 	private List<String> blacklist = new ArrayList<String>();
 	private String filePath;
 	private IOutput output;
 }
