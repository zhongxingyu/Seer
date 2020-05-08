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
 		sendMessage("&e====[ &aSwornParkour &e]====");
 		
 		StringBuilder line = new StringBuilder();
 		line.append("&eAuthor: ");
 		for (String author : plugin.getDescription().getAuthors())
 		{
 			line.append("&a" + author + "&e, ");
 		}
 		line.deleteCharAt(line.lastIndexOf(","));
 		sendMessage(line.toString());
 		
 		sendMessage("&eVersion: &a{0}", plugin.getDescription().getFullName());
 		sendMessage("&eUpdate Available: &a{0}", plugin.updateNeeded() ? "true" : "false");
 		sendMessage("&eDownload:&a http://dev.bukkit.org/bukkit-mods/swornparkour/");
 	}
 }
