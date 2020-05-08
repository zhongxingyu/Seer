 package btwmod.admincommands;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.CommandBase;
 import net.minecraft.src.ICommandSender;
 import net.minecraft.src.WrongUsageException;
 
 public class CommandMOTD extends CommandBase {
 
 	@Override
 	public String getCommandName() {
 		return "motd";
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender par1iCommandSender) {
 		return "/" + getCommandName() + " [<message>]";
 	}
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args) {
 		if (args.length == 0) {
 			sender.sendChatToPlayer("MOTD: " + MinecraftServer.getServer().getMOTD());
 		}
		else if (args.length == 1) {
			MinecraftServer.getServer().setMOTD(args[0]);
			sender.sendChatToPlayer("MOTD: " + MinecraftServer.getServer().getMOTD());
		}
 		else {
			throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
 		}
 	}
 }
