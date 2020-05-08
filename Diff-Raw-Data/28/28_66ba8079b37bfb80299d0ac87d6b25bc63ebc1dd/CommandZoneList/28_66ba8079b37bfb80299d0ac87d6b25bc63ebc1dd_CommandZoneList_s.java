 package btwmod.protectedzones;
 
 import java.util.List;
 
 import btwmods.Util;
 
 import net.minecraft.src.CommandBase;
 import net.minecraft.src.ICommandSender;
 import net.minecraft.src.Packet3Chat;
 import net.minecraft.src.WrongUsageException;
 
 public class CommandZoneList extends CommandBase {
 	
 	private final mod_ProtectedZones mod;
 	
 	public CommandZoneList(mod_ProtectedZones mod) {
 		this.mod = mod;
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender par1iCommandSender) {
 		return "/" + getCommandName() + " [<page>]";
 	}
 
 	@Override
 	public String getCommandName() {
 		return "zonelist";
 	}
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args) {
 		if (args.length > 1)
 			throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
 		
		String headerShort = "Zones: ";
		String headerLong = "Zones (Page XX/YY): ";
		int page = args.length == 1 ? parseIntWithMin(sender, args[0], 1) : 1;
		int maxListSize = Packet3Chat.maxChatLength - Math.max(headerShort.length(), headerLong.length());
 		
		List<String> pages = Util.combineIntoMaxLengthMessages(mod.getZoneNames(), maxListSize, ", ", false);
		
		page = Math.min(page, pages.size());
		
		sender.sendChatToPlayer((pages.size() == 1 ? headerShort : headerLong.replaceAll("XX/YY", page + "/" + pages.size()))
				+ pages.get(Math.min(page, pages.size()) - 1));
 	}
 
 }
