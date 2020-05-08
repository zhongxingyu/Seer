 package uk.org.cowgill.james.jircd.util;
 
 import java.util.Map;
 
 import uk.org.cowgill.james.jircd.Channel;
 import uk.org.cowgill.james.jircd.ChannelMemberMode;
 import uk.org.cowgill.james.jircd.Client;
 import uk.org.cowgill.james.jircd.Permissions;
 import uk.org.cowgill.james.jircd.ServerISupport;
 import static uk.org.cowgill.james.jircd.util.ChannelCheckError.*;
 																																										
 /**
  * Contains methods for checking whether channel actions are allowed
  * 
  * @author James
  */
 public final class ChannelChecks
 {
 	/**
 	 * Determines whether a client can join a channel
 	 * 
 	 * @param channel channel they are attempting to join
 	 * @param client client who's joining
 	 * @param key the channel key provided by the client or null
 	 * @param allowJoinAny allow opers with joinAnyChannel permission to bypass checks
 	 * @return the error or ChannelCheckError.OK if they can join
 	 */
 	public static ChannelCheckError canJoin(Channel channel, Client client, String key, boolean allowJoinAny)
 	{
 		//Check already joined
 		if(channel.lookupMember(client) != null)
 		{
 			return JoinAlreadyInChannel;
 		}
 		
 		//Check for too many channels joined
 		if(client.getChannels().size() >= ServerISupport.MAXCHANNELS)
 		{
 			return JoinTooManyChannels;
 		}
 		
 		//Check if can join any channel
 		if(allowJoinAny && client.hasPermission(Permissions.joinAnyChannel))
 		{
 			return OK;
 		}
 		
 		//Check oper only channel
 		if(channel.isModeSet('O') && !(client.isModeSet('o') || client.isModeSet('O')))
 		{
 			return JoinOpersOnly;
 		}
 		
 		//Check channel full
 		if(channel.getLimit() > 0 && channel.getMembers().size() >= channel.getLimit())
 		{
 			return JoinChannelFull;
 		}
 		
 		//Check key
 		String chanKey = channel.getKey();
 		if(chanKey != null && (key == null || !chanKey.equals(key)))
 		{
 			return JoinInvalidKey;
 		}
 		
 		//Check invites
		if(channel.isModeSet('i') && (channel.isOnInviteExceptList(client) || channel.isInvited(client)))
 		{
 			return JoinInviteOnly;
 		}
 
 		//Check banned
 		if(channel.isBannedSkipMember(client))
 		{
 			return JoinBanned;
 		}
 
 		//OK
 		return OK;
 	}
 
 	/**
 	 * Determines whether a client can speak in a channel
 	 * 
 	 * @param channel channel they are attempting to speask in
 	 * @param client client who's speaking
 	 * @return the error or ChannelCheckError.OK if they can speak
 	 */
 	public static ChannelCheckError canSpeak(Channel channel, Client client)
 	{
 		//Get mode
 		ChannelMemberMode mode = channel.lookupMember(client);
 		
 		//Check if in channel
 		if(channel.isModeSet('n') && mode == null)
 		{
 			return SpeakNotInChannel;
 		}
 		
 		//Check moderated
 		if(channel.isModeSet('m') && (mode == null || mode.getHighestMode() < ChannelMemberMode.VOICE))
 		{
 			return SpeakModerated;
 		}
 		
 		//Check banned
 		if(channel.isBanned(client))
 		{
 			return SpeakBanned;
 		}
 	
 		return OK;
 	}
 
 	/**
 	 * Determines whether a client can kick someone in a channel
 	 * 
 	 * <p>can return GeneralNotAnOp
 	 * 
 	 * @param channel channel they are attempting to kick in
 	 * @param client client who's kicking
 	 * @param kicked client who's being kicked
 	 * @return the error or ChannelCheckError.OK if they can join
 	 */
 	public static ChannelCheckError canKick(Channel channel, Client client, Client kicked)
 	{
 		//Lookup both clients
 		ChannelMemberMode clientMode = channel.lookupMember(client);
 		ChannelMemberMode kickedMode = channel.lookupMember(kicked);
 		
 		//Check if they exist
 		if(clientMode == null)
 		{
 			return GeneralNotAnOp;
 		}
 		
 		if(kickedMode == null)
 		{
 			return KickOtherNotInChannel;
 		}
 		
 		//Check permissions
 		if(canOwn(clientMode, kickedMode))
 		{
 			return OK;
 		}
 		else
 		{
 			return GeneralNotAnOp;
 		}
 	}
 
 	/**
 	 * Determines whether a client can set the topic in a channel
 	 * 
 	 * <p>can return GeneralNotAnOp and GeneralNotInChannel
 	 * 
 	 * @param channel channel they are attempting to set the topic in
 	 * @param client client who's setting the topic
 	 * @return the error or ChannelCheckError.OK if they can set the topic
 	 */
 	public static ChannelCheckError canSetTopic(Channel channel, Client client)
 	{
 		//Check if in channel
 		ChannelMemberMode mode = channel.lookupMember(client);
 		
 		if(mode == null)
 		{
 			return GeneralNotInChannel;
 		}
 		
 		//Check if oper
 		if(channel.isModeSet('t') && mode.getHighestMode() < ChannelMemberMode.HALFOP)
 		{
 			return GeneralNotAnOp;
 		}
 
 		return OK;
 	}
 
 	/**
 	 * Determines whether a client can invite someone to a channel
 	 * 
 	 * <p>can return GeneralNotAnOp and GeneralNotInChannel
 	 * 
 	 * @param channel channel they are attempting to invite to
 	 * @param client client who's inviting
 	 * @param invited client who's being invited
 	 * @return the error or ChannelCheckError.OK if they can invite that person
 	 */
 	public static ChannelCheckError canInvite(Channel channel, Client client, Client invited)
 	{
 		//Lookup channel permissions
 		ChannelMemberMode clientMode = channel.lookupMember(client);
 		
 		if(clientMode == null)
 		{
 			return GeneralNotInChannel;
 		}
 		
 		//Check other is not in channel
 		if(channel.lookupMember(invited) != null)
 		{
 			return InviteAlreadyInChannel;
 		}
 		
 		//Check operator privs
 		if(channel.isModeSet('i') && clientMode.getHighestMode() < ChannelMemberMode.OP)
 		{
 			return GeneralNotAnOp;
 		}
 		
 		return OK;
 	}
 	
 	/**
 	 * Checks to see if a ban / invite list is full
 	 * 
 	 * @param add true if adding to list
 	 * @param list the list to check
 	 * @return the channel check error to generate
 	 */
 	private static ChannelCheckError handleListFull(boolean add, Map<String, Channel.SetInfo> list)
 	{
 		if(add && list.size() >= ServerISupport.MAXLIST)
 		{
 			return SetModeListFull;
 		}
 		else
 		{
 			return OK;
 		}
 	}
 
 	/**
 	 * Determines whether a client can set a mode in a channel
 	 * 
 	 * <p>can return GeneralNotAnOp
 	 * <p>This does not check for removal of your own modes
 	 * 
 	 * @param channel channel they are attempting to set a mode on
 	 * @param client client who's setting the mode
 	 * @param mode the mode which is being modified
 	 * @return the error or ChannelCheckError.OK if they can set the mode
 	 */
 	public static ChannelCheckError canSetMode(Channel channel, Client client, boolean add, char mode)
 	{
 		//Lookup member
 		ChannelMemberMode clientMode = channel.lookupMember(client);
 		
 		if(clientMode == null)
 		{
 			return GeneralNotAnOp;
 		}
 		
 		//Must be half-op to set anything
 		boolean canSet = true;
 		int modeHigh = clientMode.getHighestMode();
 		
 		if(modeHigh < ChannelMemberMode.HALFOP)
 		{
 			return GeneralNotAnOp;
 		}
 		
 		//Modes by default can be set by half-ops
 		// Check other modes
 		switch(mode)
 		{
 		case 'O':
 			//Must be IRC operator and OP
 			if(!client.isModeSet('o') && !client.isModeSet('O'))
 			{
 				return SetModeNotAnIrcOp;
 			}
 			
 		case 'h':
 		case 'o':
 		case 'p':
 		case 's':
 			//Must be op
 			canSet = clientMode.getHighestMode() >= ChannelMemberMode.OP;
 			break;
 			
 		case 'a':
 		case 'q':
 			//Must be owner
 			canSet = clientMode.getHighestMode() >= ChannelMemberMode.OWNER;
 			break;
 			
 			//Handle lists (half-ops can do all this)
 		case 'b':
 			return handleListFull(add, channel.getBanList());
 			
 		case 'e':
 			return handleListFull(add, channel.getBanExceptList());
 			
 		case 'I':
 			return handleListFull(add, channel.getInviteExceptList());
 			
 		default:
 			//Must be half op = OK
 			return OK;
 		}
 		
 		//Check result
 		if(canSet)
 		{
 			return OK;
 		}
 		else
 		{
 			//Send correct error
 			if(modeHigh == ChannelMemberMode.HALFOP)
 			{
 				return ChannelCheckError.SetModeHalfOpDeny;
 			}
 			else
 			{
 				return ChannelCheckError.SetModeOwnerOnly;
 			}
 		}
 	}
 
 	/**
 	 * Determines whether a client can get the topic of a channel
 	 * 
 	 * @param channel channel they are attempting to get the topic of
 	 * @param client client who's getting the topic
 	 * @return true if the topic can be read
 	 */
 	public static boolean canGetTopic(Channel channel, Client client)
 	{
 		//Must be on the channel or not secret
 		return !channel.isModeSet('s') || channel.lookupMember(client) != null;
 	}
 
 	/**
 	 * Determines whether a client can see the member list of a channel
 	 * 
 	 * @param channel channel they are attempting to see the member list of
 	 * @param client client who's getting the member list
 	 * @return true if the names list can be read
 	 */
 	public static boolean canGetNames(Channel channel, Client client)
 	{
 		//Must be on channel or not secret or private
 		return !(channel.isModeSet('s') || channel.isModeSet('p')) || channel.lookupMember(client) != null;
 	}
 	
 	/**
 	 * Determined whether a client can "own" (obliterate, destroy) another client based on channel modes
 	 * 
 	 * @param client client who is going to do the owning
 	 * @param toBeOwned client to be owned
 	 * @return true if the client can own the other person
 	 */
 	public static boolean canOwn(ChannelMemberMode client, ChannelMemberMode toBeOwned)
 	{
 		//We can own if any of the following are true:
 		// * We are an owner
 		// * We are an admin and other is not owner
 		// * We are an op and other is not an admin (or more)
 		// * We are a half-op and the other is not a half-op (or more)
 		//
 		
 		int clientHigh = client.getHighestMode();
 		int ownedHigh = toBeOwned.getHighestMode();
 		
 		return clientHigh > ownedHigh ||
 				(clientHigh >= ChannelMemberMode.OP && clientHigh == ownedHigh);
 	}
 
 	private ChannelChecks()
 	{
 	}
 }
