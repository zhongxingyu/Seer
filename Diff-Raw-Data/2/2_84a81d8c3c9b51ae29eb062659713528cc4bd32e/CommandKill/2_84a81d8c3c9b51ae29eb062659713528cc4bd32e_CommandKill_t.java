 package com.theisleoffavalon.mcmanager.commands;
 
 import net.minecraft.command.ICommandSender;
 import net.minecraft.command.PlayerNotFoundException;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.DamageSource;
 
 import com.theisleoffavalon.mcmanager.MCManager;
 
 /**
  * An extension of the vanilla kill command. This version allows the
  * user to specify a player name. The player that is selected is then
  * killed.
  * 
  * @author Cadyyan
  *
  */
 public class CommandKill extends Command
 {
 	/**
 	 * A special damage source for doing true damage to the target for
 	 * an insta-kill.
 	 * 
 	 * @author Cadyyan
 	 *
 	 */
 	private static class KillDamageSource extends DamageSource
 	{
 		/**
 		 * A convenient, easy access instance.
 		 */
 		public static final KillDamageSource damage = new KillDamageSource();
 		
 		/**
 		 * Creates a new instance of true damage.
 		 */
 		private KillDamageSource()
 		{
 			super("execution");
 			setDamageBypassesArmor();
 			setDamageAllowedInCreativeMode();
 		}
 		
 		@Override
 		public String getDeathMessage(EntityLiving player)
 		{
 		    
 			return player.getEntityName() + " was executed";
 		}
 	}
 	
 	/**
 	 * Creates the kill command instance.
 	 */
 	public CommandKill()
 	{
 		super("kill");
 	}
 
 	@Override
 	public String getCommandUsage(ICommandSender sender)
 	{
 		return "/" + name + "[player]";
 	}
 
 	@Override
 	public void processCommand(ICommandSender sender, String[] args)
 	{
 		String username = args.length < 1 ? null : args[0];
 		EntityPlayer player = username == null ? getCommandSenderAsPlayer(sender) :
 												 MCManager.instance.getServer().getConfigurationManager().getPlayerForUsername(args[0]);
 		
 		if(player == null)
 			throw new PlayerNotFoundException("Could not find a player " + username);
 		
 		player.attackEntityFrom(KillDamageSource.damage, 32767);
		notifyAdmins(player, "Executing " + player.username);
 	}
 	
 }
