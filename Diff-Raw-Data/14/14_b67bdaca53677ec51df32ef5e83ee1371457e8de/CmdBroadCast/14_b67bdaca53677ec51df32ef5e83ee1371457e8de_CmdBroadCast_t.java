 package net.shiroumi.central.Command.Server;
 
 import net.shiroumi.central.CentralCore;
 import net.shiroumi.central.Command.BaseCommand;
 import net.shiroumi.central.Command.CommandDescription;
 import net.shiroumi.central.Util.Util;
import net.shiroumi.central.Util.i18n;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 public class CmdBroadCast extends BaseCommand {
 
 	public CmdBroadCast(CentralCore par1Plugin) {
 		super(new CommandDescription("broadcast", "server.broadcast", true));
 	}
 
 	@Override
 	public boolean execute(CommandSender par1Sender, Command par2Command,
 			String par3Args, String[] par4Args) {
 		StringBuilder sb = new StringBuilder();
 		for(String t : par4Args) {
 			sb.append(t);
 			sb.append(" ");
 		}
 		String message = sb.toString();
		Util.broadcastMessage(i18n._("broadcastPrefix"), new String[][]{{"%msg", message}});
 		return true;
 	}
 
 }
