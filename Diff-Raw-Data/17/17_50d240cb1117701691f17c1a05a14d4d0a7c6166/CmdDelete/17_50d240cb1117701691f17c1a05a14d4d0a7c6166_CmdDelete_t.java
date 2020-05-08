 package net.dmulloy2.swornparkour.commands;
 
 import net.dmulloy2.swornparkour.SwornParkour;
 import net.dmulloy2.swornparkour.permissions.Permission;
 
 /**
  * @author dmulloy2
  */
 
 public class CmdDelete extends SwornParkourCommand
 {
 	public CmdDelete(SwornParkour plugin)
 	{
 		super(plugin);
 		this.name = "delete";
 		this.aliases.add("d");
 		this.requiredArgs.add("id");
 		this.description = "Deletes an arena";
 		this.permission = Permission.CMD_DELETE;
 		
 		this.mustBePlayer = true;
 	}
 	
 	@Override
 	public void perform()
 	{
 		int id = argAsInt(0, true);
		if (id == -1)
			return;
		
 		if (plugin.loadedArenas.size() < id)
 		{
 			err("&cNo arena by id {0} exists!", id);
 			return;
 		}
 		
 		sendMessage("&eDeleting arena {0}...", id);
 		
 		if (getManager().deleteArena(id))
 		{
 			sendMessage("&eSuccessfully deleted arena!");
 			return;
 		}
 		
 		err("&cCheck console!");
 	}
 }
