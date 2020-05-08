 package me.naithantu.ArenaPVP.Commands;
 
 import me.naithantu.ArenaPVP.ArenaManager;
 import me.naithantu.ArenaPVP.ArenaPVP;
 
 import org.bukkit.command.CommandSender;
 
 public class CommandPVP extends AbstractCommand {
 
 	protected CommandPVP(CommandSender sender, String[] args, ArenaPVP plugin, ArenaManager arenaManager) {
 		super(sender, args, plugin, arenaManager);
 	}
 
 	@Override
 	public boolean handle() {
 		if(!testPermission(sender, "pvp") && !testPermission(sender, "player")){
 			this.noPermission(sender);
 			return true;
 		}
		
 		//TODO Create main pvp help command.
 		return true;
 	}
 }
