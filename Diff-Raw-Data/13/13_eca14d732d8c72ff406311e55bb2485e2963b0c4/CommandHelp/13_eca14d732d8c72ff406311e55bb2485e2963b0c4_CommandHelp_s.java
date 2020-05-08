 package zerothindex.minecraft.clancraft.command;
 
import org.bukkit.command.CommandSender;

 import zerothindex.minecraft.clancraft.Messageable;
 
 public class CommandHelp extends CommandBase {
 
 	@Override
 	public String getName() {
		return "c";
 	}
 
 	@Override
 	public String getDescription() {
 		return "view the help page";
 	}
 
 	@Override
 	public String getUsage() {
 		return "/c";
 	}
 
 	@Override
 	public boolean playerOnly() {
 		return false;
 	}
 
 	@Override
 	public boolean adminOnly() {
 		return false;
 	}
 
 	@Override
 	public boolean handle(Messageable sender, String[] args) {
 		sender.message("-ClanCraft- Nothing to see here!");
 		return true;
 		
 	}
 
 	
 
 	
 
 }
