 package forgetools.common;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.CommandBase;
 import net.minecraft.src.EntityChicken;
 import net.minecraft.src.EntityLightningBolt;
 import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.ICommandSender;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.ServerConfigurationManager;
 import net.minecraft.src.WorldServer;
 import net.minecraft.src.WrongUsageException;
 
 public class SmiteCommand extends CommandBase{
 
 	@Override
 	public String getCommandName() {
 		return "smite";
 	}
 	
 	@Override
 	public String getCommandUsage(ICommandSender par1ICommandSender)
     {
    	return "/smite playername";
     }
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args) {
 		if(!FMLCommonHandler.instance().getEffectiveSide().isServer()) return;
 		
 		ServerConfigurationManager serverConfig = ModLoader.getMinecraftServerInstance().getConfigurationManager();
 		MinecraftServer server = ForgeTools.server;
 		
 		EntityPlayerMP player = getCommandSenderAsPlayer(sender);
 		if (!player.username.equalsIgnoreCase("Server") && !serverConfig.getOps().contains(player.username.trim().toLowerCase()))
 		{
 			sender.sendChatToPlayer("\u00a74You do not have permission to use the /smite command.");
 			return;
 		}
 		
 		if (args.length != 1) throw new WrongUsageException(getCommandUsage(sender));	// Only accept the command with one argument
 		
 		String players[] = serverConfig.getAllUsernames();	// Get an array of all usernames
 		boolean found = false;
 		for (String s: players) {							// Search for the targeted username
 			if (s.equals(args[0]))
 				 found = true;
 		}
 		
 		if (found) {
 			sender.sendChatToPlayer("\u00a77Smiting " + args[0]);
 			
 			EntityPlayerMP target = serverConfig.getPlayerForUsername(args[0]);
 			WorldServer targetWorld = null;
 			
 			for(WorldServer s : server.worldServers) {		// Find the world the player is in
 				if (s.getWorldInfo().equals(target.worldObj.getWorldInfo())) {
 					targetWorld = s;
 					break;
 				}
 			}
 			
 			// Create the lightning bolt targeted at the players position and then spawn it in the world
 			EntityLightningBolt bolt = new EntityLightningBolt(targetWorld, target.posX, target.posY, target.posZ);
 			targetWorld.spawnEntityInWorld(bolt);
 			
 			target.heal(-20);	// Remove 20 half-hearts from the player, this kills the player regardless of armor
 		} else
 			sender.sendChatToPlayer("\u00a7c" + args[0] + " cannot be found");
 	}
 
 }
