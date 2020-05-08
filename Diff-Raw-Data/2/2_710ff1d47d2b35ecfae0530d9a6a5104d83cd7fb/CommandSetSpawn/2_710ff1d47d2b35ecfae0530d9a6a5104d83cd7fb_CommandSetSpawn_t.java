 package dries007.SimpleCore.Commands;
 
 import java.util.Iterator;
 import java.util.List;
 
 import dries007.SimpleCore.*;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.*;
 
 public class CommandSetSpawn extends CommandBase
 {
     public String getCommandName()
     {
         return "setspawn";
     }
 
     public String getCommandUsage(ICommandSender par1ICommandSender)
     {
     	return "/" + getCommandName() + " [rank] [nameOfSpawn]";
     }
 
     public List getCommandAliases()
     {
         return null;
     }
     
     public void processCommand(ICommandSender sender, String[] args)
     {
     	EntityPlayer player = ((EntityPlayer)sender);
     	if (args.length==0)
     	{
     		Double X = player.posX;
     		Double Y = player.posY;
     		Double Z = player.posZ;
     		player.worldObj.getWorldInfo().setSpawnPosition(X.intValue(),Y.intValue(),Z.intValue());
     		player.sendChatToPlayer("Serverspawn set. All ranks with no seperate spawn will spawn here.");
     	}
     	else
     	{
     		String rank = getRank(args[0]);
     		NBTTagCompound rankdata = SimpleCore.rankData.getCompoundTag(rank);
     		NBTTagCompound spawn = new NBTTagCompound();
     		try {spawn.setString("name", args[1]);} catch (Exception e) {spawn.setString("name", "spawn");}
     		spawn.setDouble("X", player.posX); 
			spawn.setDouble("Y", player.posY);
 			spawn.setDouble("Z", player.posZ);
 			spawn.setFloat("yaw", player.rotationYaw);
 			spawn.setFloat("pitch", player.rotationPitch);
 			spawn.setInteger("dim", player.dimension);
 			rankdata.setCompoundTag("Spawn", spawn );
     		SimpleCore.rankData.setCompoundTag(rank, rankdata);
     		player.sendChatToPlayer("Spawn for rank " + rank + " set. Name:" + spawn.getString("name") + ".");
     	}
     }
     
     public boolean canCommandSenderUseCommand(ICommandSender sender)
     {
         return Permissions.hasPermission(sender.getCommandSenderName(), "SC.admin");
     }
     
     /**
      * Adds the strings available in this command to the given list of tab completion options.
      */
     public List addTabCompletionOptions(ICommandSender sender, String[] args)
     {
     	if(args.length == 1)
         {
         	String msg = "";
         	for(String st : Permissions.getPermissions()) msg = msg + st + ", ";
         	sender.sendChatToPlayer("List of permissions: " + msg);
         	return getListOfStringsMatchingLastWord(args, Permissions.getPermissions());
         }
     	return null;
     }
     
     protected String getRank(String input)
     {
     	Iterator ranks = SimpleCore.rankData.getTags().iterator();
     	while (ranks.hasNext())
     	{
     		NBTTagCompound rank = (NBTTagCompound) ranks.next();
     		if (rank.getName().equalsIgnoreCase(input)) return rank.getName();
     	}
     	throw new WrongUsageException("Rank '" + input + "' not found!", new Object[0]);
     }
 }
