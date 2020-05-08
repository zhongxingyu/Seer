 package me.botsko.darmok.channels;
 
 import me.botsko.darmok.Darmok;
 import me.botsko.darmok.exceptions.ChannelPermissionException;
 
 import org.bukkit.entity.Player;
 
 import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
 import com.palmergames.bukkit.towny.object.Resident;
 import com.palmergames.bukkit.towny.object.TownyUniverse;
 
 public class ChannelPermissions {
 	
 	
 	/**
 	 * 
 	 * @param player
 	 * @param channel
 	 * @return
 	 * @throws ChannelPermissionException 
 	 */
 	public static boolean playerCanAutoJoin( Player player, Channel channel ) throws ChannelPermissionException{
 		String permPrefix = "darmok.channel." + channel.getName().toLowerCase() + ".";
 		if( player.hasPermission( permPrefix + "autojoin" ) && player.hasPermission( permPrefix + "read" ) ){
 			return true;
 		}
 		throw new ChannelPermissionException("Insufficient permission to auto-join this channel.");
 	}
 	
 	
 	/**
 	 * 
 	 * @param player
 	 * @param channel
 	 * @return
 	 * @throws ChannelPermissionException 
 	 */
 	public static boolean playerCanBan( Player player, Channel channel ) throws ChannelPermissionException{
 		String permPrefix = "darmok.channel." + channel.getName().toLowerCase() + ".";
 		if( player.hasPermission( permPrefix + "ban" ) || player.hasPermission( "darmok.mod" ) ){
 			return true;
 		}
 		throw new ChannelPermissionException("Insufficient permission to ban a player from this channel.");
 	}
 	
 	
 	/**
 	 * 
 	 * @param player
 	 * @param channel
 	 * @return
 	 * @throws ChannelPermissionException 
 	 */
 	public static boolean playerCanDefaultTo( Player player, Channel channel ) throws ChannelPermissionException{
 		
 		// Perms?
 		String permPrefix = "darmok.channel." + channel.getName().toLowerCase() + ".";
 		if( !player.hasPermission( permPrefix + "read" ) && !player.hasPermission( permPrefix + "speak" ) ){
 			throw new ChannelPermissionException("Insufficient permission to read or speak in this channel.");
 		}
 		
 		// Banned?
 		if( Darmok.getPlayerRegistry().isPlayerBannedFromChannel(player, channel) ){
 			throw new ChannelPermissionException("Player has been banned from this channel.");
 		}
 		
 		// If a town channel, make sure they have a town
 		if( Darmok.getTowny() != null && channel.getContext() != null && channel.getContext().equals("towny-town") ){
 			if( !playerHasTown( player ) ){
 				throw new ChannelPermissionException("Player does not have a town.");
 			}
 		}
 		
 		return true;
 		
 	}
 	
 	
 	/**
 	 * Can the player force another user to change channels
 	 * @param player
 	 * @param channel
 	 * @return
 	 * @throws ChannelPermissionException 
 	 */
 	public static boolean playerCanForce( Player player, Channel channel ) throws ChannelPermissionException {
		if( player.hasPermission( "darmok.mod" ) ){
 			throw new ChannelPermissionException("Insufficient permission to force a player into this channel.");
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * 
 	 * @param player
 	 * @param channel
 	 * @return
 	 * @throws ChannelPermissionException 
 	 */
 	public static boolean playerCanJoin( Player player, Channel channel ) throws ChannelPermissionException{
 		
 		// Perms?
 		String permPrefix = "darmok.channel." + channel.getName().toLowerCase() + ".";
 		if( !player.hasPermission( permPrefix + "read" ) && !player.hasPermission( permPrefix + "speak" ) ){
 			throw new ChannelPermissionException("Insufficient permission to read or speak in this channel.");
 		}
 		
 		// Banned?
 		if( Darmok.getPlayerRegistry().isPlayerBannedFromChannel(player, channel) ){
 			throw new ChannelPermissionException("Player has been banned from this channel.");
 		}
 		
 		// If a town channel, make sure they have a town
 		if( Darmok.getTowny() != null && channel.getContext() != null && channel.getContext().equals("towny-town") ){
 			if( !playerHasTown( player ) ){
 				throw new ChannelPermissionException("Player does not have a town.");
 			}
 		}
 		
 		return true;
 	}
 	
 	
 	/**
 	 * They have to be allowed to join if they have read or speak perms.
 	 * @param player
 	 * @param channel
 	 * @return
 	 * @throws ChannelPermissionException 
 	 */
 	public static boolean playerCanKick( Player player, Channel channel ) throws ChannelPermissionException{
 		String permPrefix = "darmok.channel." + channel.getName().toLowerCase() + ".";
 		if( player.hasPermission( permPrefix + "kick" ) || player.hasPermission( permPrefix + "ban" ) || player.hasPermission( "darmok.mod" ) ){
 			throw new ChannelPermissionException("Insufficient permission to kick a player from this channel.");
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * 
 	 * @param player
 	 * @param channel
 	 * @return
 	 * @throws ChannelPermissionException 
 	 */
 	public static boolean playerCanLeave( Player player, Channel channel ) throws ChannelPermissionException{
 		String permPrefix = "darmok.channel." + channel.getName().toLowerCase() + ".";
 		if( player.hasPermission( permPrefix + "leave" ) ){
 			throw new ChannelPermissionException("Insufficient permission to leave this channel.");
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * Can't speak without reading, so speak grants read perms.
 	 * @param player
 	 * @param channel
 	 * @return
 	 * @throws ChannelPermissionException 
 	 */
 	public static boolean playerCanRead( Player player, Channel channel ) throws ChannelPermissionException{
 		
 		// Perms?
 		String permPrefix = "darmok.channel." + channel.getName().toLowerCase() + ".";
 		if( !player.hasPermission( permPrefix + "read" ) && !player.hasPermission( permPrefix + "speak" ) ){
 			throw new ChannelPermissionException("Insufficient permission to read this channel.");
 		}
 		
 		// Banned?
 		if( Darmok.getPlayerRegistry().isPlayerBannedFromChannel(player, channel) ){
 			throw new ChannelPermissionException("Player has been banned from this channel.");
 		}
 		
 		// If a town channel, make sure they have a town
 		if( Darmok.getTowny() != null && channel.getContext() != null && channel.getContext().equals("towny-town") ){
 			if( !playerHasTown( player ) ){
 				throw new ChannelPermissionException("Player does not have a town.");
 			}
 		}
 		
 		return true;
 		
 	}
 	
 	
 	/**
 	 * 
 	 * @param player
 	 * @param channel
 	 * @return
 	 * @throws ChannelPermissionException 
 	 */
 	public static boolean playerCanSpeak( Player player, Channel channel ) throws ChannelPermissionException{
 		
 		// Perms?
 		String permPrefix = "darmok.channel." + channel.getName().toLowerCase() + ".";
 		if( !player.hasPermission( permPrefix + "speak" ) ){
 			throw new ChannelPermissionException("Insufficient permission to speak this channel.");
 		}
 		
 		// Banned?
 		if( Darmok.getPlayerRegistry().isPlayerBannedFromChannel(player, channel) ){
 			throw new ChannelPermissionException("Player has been banned from this channel.");
 		}
 		
 		// If a town channel, make sure they have a town
 		if( Darmok.getTowny() != null && channel.getContext() != null && channel.getContext().equals("towny-town") ){
 			if( !playerHasTown( player ) ){
 				throw new ChannelPermissionException("Player does not have a town.");
 			}
 		}
 		
 		return true;
 	}
 	
 	
 	/**
 	 * 
 	 * @param player
 	 * @param channel
 	 * @return
 	 */
 	public static boolean playerHasTown( Player player ){
 		try {
 			Resident resident = TownyUniverse.getDataSource().getResident( player.getName() );
 			return resident.hasTown();
 		} catch (NotRegisteredException e) {
 		}
 		return false;
 	}
 }
