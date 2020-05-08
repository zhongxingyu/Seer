 package net.dmulloy2.swornparkour.commands;
 
 import net.dmulloy2.swornparkour.SwornParkour;
 
 /**
  * @author dmulloy2
  */
 
 public class CmdVersion extends SwornParkourCommand
 {
 	public CmdVersion(SwornParkour plugin)
 	{
 		super(plugin);
 		this.name = "version";
 		this.aliases.add("v");
 		this.description = "Display " + plugin.getName() + " version";
 		
 		this.mustBePlayer = false;
 	}
 
 	@Override
 	public void perform()
 	{
 		sendMessage("&3====[ &eSwornParkour &3]====");
 		
 		StringBuilder line = new StringBuilder();
 		line.append("&bAuthor: ");
 		for (String author : plugin.getDescription().getAuthors())
 		{
 			line.append("&e" + author + "&b, ");
 		}
 		line.deleteCharAt(line.lastIndexOf(","));
 		sendMessage(line.toString());
 		
 		sendMessage("&bVersion: &e{0}", plugin.getDescription().getFullName());
 		sendMessage("&bUpdate Available: &e{0}", plugin.updateNeeded() ? "true" : "false");
		sendMessage("&bDownload:&e http://dev.bukkit.org/bukkit-plugins/swornparkour/");
 	}
 }
