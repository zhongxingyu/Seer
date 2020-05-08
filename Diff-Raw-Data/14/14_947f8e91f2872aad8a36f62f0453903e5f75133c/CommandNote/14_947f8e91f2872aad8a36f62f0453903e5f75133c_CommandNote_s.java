 package com.hamaluik.PlayerNotes.commands;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.hamaluik.PlayerNotes.PlayerNotes;
 import com.hamaluik.PlayerNotes.PlayerNotesCommandExecutor;
 
 public class CommandNote implements Command {
 	private PlayerNotes plugin;
 	public CommandNote(PlayerNotes instance) { plugin = instance; }
 	
 	public boolean onCommand(CommandSender sender, String[] args) {
 		// make sure they're providing enough information
 		if(args.length < 2) return false;
 		
 		String typedTarget = args[0];
 		String target = new String("");
 		
 		// ok, we have the page and target
 		// look up the target's actual name
 		if(plugin.getServer().getPlayer(typedTarget) != null) {
 			// the player is online, set their name
 			target = plugin.getServer().getPlayer(typedTarget).getName();
 		}
 		else {
 			target = typedTarget;
 		}
 		
 		// get the note-taker name
 		String noteTaker = new String("*Console*");
 		if(sender instanceof Player) noteTaker = ((Player)sender).getName();
 		
 		// we have the target, now compile a string based on the remaining arguments
 		StringBuilder noteSB = new StringBuilder();
 		for(int i = 1; i < args.length; i++) noteSB.append(args[i] + " ");
 		
 		// and go for it!
 		if(!plugin.dbm.writeNote(noteTaker, target, noteSB.toString())) {
 			PlayerNotesCommandExecutor.returnMessage(sender, "&cAn error occurred!");
 		}
 		else {
			PlayerNotesCommandExecutor.returnMessage(sender, "&6Your note has been recorded successfully!");
 		}
 		
 		return true;
 	}
 	
 	public String requiredPermission() {
 		return "playernotes.note";
 	}
 	
 	public String getCommand() {
 		return "n";
 	}
 	
 	public String getArguments() {
 		return "<player> <your note>";
 	}
 	
 	public String getDescription() {
 		return "writes a note about <player>";
 	}
 
 }
