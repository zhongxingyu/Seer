 package com.massivecraft.factions.cmd;
 
 import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.struct.Role;
 
 public class CmdLeave extends FCommand {
 
 	public CmdLeave() {
 		super();
 		this.aliases.add("leave");
 
 		// this.requiredArgs.add("");
 		// this.optionalArgs.put("", "");
 
 		this.permission = Permission.LEAVE.node;
 		this.disableOnLock = true;
 
 		senderMustBePlayer = true;
 		senderMustBeMember = true;
 		senderMustBeModerator = false;
 		senderMustBeAdmin = false;
 	}
 
 	@Override
 	public void perform() {
		
		if(fme.getRole() == Role.ADMIN){
			if(fme.getFaction().isInWar()){
				fme.sendMessage("You cant leave your Faction in war, as Leader!");
				return;
			}
		}
				
		
 		fme.leave(true);
 	}
 
 }
