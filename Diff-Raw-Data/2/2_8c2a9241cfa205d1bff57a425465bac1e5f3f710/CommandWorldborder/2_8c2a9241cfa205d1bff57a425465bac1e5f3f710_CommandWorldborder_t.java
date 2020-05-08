 package dries007.SimpleCore.Commands;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import dries007.SimpleCore.*;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.*;
 
 public class CommandWorldborder extends CommandBase
 {
     public String getCommandName()
     {
         return "worldborder";
     }
 
     public String getCommandUsage(ICommandSender sender)
     {
    	return "/" + getCommandName() + " <set|on|off> [radius OR minX,minZ,maxX,maxZ]";
     }
 
     public List getCommandAliases()
     {
     	return Arrays.asList(new String[] {"wb"});
     }
     
     public boolean canCommandSenderUseCommand(ICommandSender sender)
     {
     	return Permissions.hasPermission(sender.getCommandSenderName(), "SC.admin");
     }
     
     public List addTabCompletionOptions(ICommandSender sender, String[] args)
     {
         if(args.length == 1)
         {
         	return getListOfStringsMatchingLastWord(args, "set", "on", "off");
         }
         return null;
     }
     
     public void processCommand(ICommandSender sender, String[] args)
     {
     	if(!(args.length==1 || args.length==2)) throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
     	if(args[0].equalsIgnoreCase("on"))
     	{
     		WorldBorder.enable = true;
     		sender.sendChatToPlayer("WorldBorder enabled!");
     	}
     	else if(args[0].equalsIgnoreCase("off"))
     	{
     		WorldBorder.enable = false;
     		sender.sendChatToPlayer("WorldBorder disabled!");
     	}
     	else if(args[0].equalsIgnoreCase("set"))
     	{
     		if(args[1].contains(","))
     		{
     			String[] coords = args[1].split(",");
     			if(coords.length!=4) throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
     			WorldBorder.setWorldBorder(parseInt(sender, coords[0]), parseInt(sender, coords[1]), parseInt(sender, coords[2]), parseInt(sender, coords[3])); 
     		}
     		else
     		{
     			int R = parseInt(sender, args[1]);
     			EntityPlayerMP player = getCommandSenderAsPlayer(sender);
     			int X = ((Double)player.posX).intValue();
     			int Z = ((Double)player.posZ).intValue();
     			WorldBorder.setWorldBorder(X - R, X + R, Z - R, Z + R);
     		}
     		sender.sendChatToPlayer("World border set: minX=" + WorldBorder.minX + ", maxX=" +  + WorldBorder.maxX + ", minZ=" + WorldBorder.minZ + ", maxZ=" + WorldBorder.maxZ);
     	}
     }
 }
