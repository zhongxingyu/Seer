 package de.minestar.cok.command;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import net.minecraft.command.ICommandSender;
 import de.minestar.cok.game.CoKGame;
 import de.minestar.cok.helper.ChatSendHelper;
 import de.minestar.cok.references.Color;
 import de.minestar.cok.references.Reference;
 
 public class CommandCreateTeam extends CoKCommand {
 
 	@Override
 	public String getCommandName() {
 		return Reference.CreateTeamCommand;
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender icommandsender) {
		return getCommandName() + " TeamName Color";
 	}
 
 	@Override
 	public void processCommand(ICommandSender icommandsender, String[] astring) {
 		if(astring.length < 2){
 			ChatSendHelper.sendMessage(icommandsender, "Usage: " + getCommandUsage(icommandsender));
 			return;
 		}
 		char color = Color.getColorFromString(astring[1]);
 		if(color != 'g'){
 			boolean res = false;
 			if(astring.length >= 3){
 				res = CoKGame.addTeam(astring[0], color, astring[2]);
 			} else{
 				res = CoKGame.addTeam(astring[0], color);
 			}
 			if(!res){
 				ChatSendHelper.sendError(icommandsender, "Team \"" + astring[0] + "\" could not be created!");
 			} else{
 				ChatSendHelper.sendMessage(icommandsender, "Team \"" + astring[0] + "\" successfully created!");
 			}
 		} else{
 			ChatSendHelper.sendError(icommandsender, "Invalid colour \"" + astring[0] + "\"!");
 		}
 	}
 
 	@Override
 	public List<?> addTabCompletionOptions(ICommandSender icommandsender,
 			String[] astring) {
 		LinkedList<String> list = new LinkedList<String>();
 		list.add("Red");
 		list.add("Blue");
 		return list;
 	}
 
 }
