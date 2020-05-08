 /*
  *  hbIRCS
  *  
  *  Copyright 2005 Boris HUISGEN <bhuisgen@hbis.fr>
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Library General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 package fr.hbis.ircs.commands;
 
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import fr.hbis.ircs.Command;
 import fr.hbis.ircs.IRC;
 import fr.hbis.ircs.Manager;
 import fr.hbis.ircs.Message;
 import fr.hbis.ircs.Source;
 
 /**
  * The class <code>Mode</code> implements the MODE command.
  * 
  * @author bhuisgen
  */
 public class Mode extends Command
 {
 	/**
 	 * Constructs a new <code>Mode</code> object.
 	 * 
 	 * @param manager
 	 *            the manager.
 	 */
 	public Mode (Manager manager)
 	{
 		m_manager = manager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Command#execute(fr.hbis.ircs.Source,
 	 * java.lang.String[])
 	 */
 	public void execute (Source src, String[] params)
 	{
 		if (src == null)
 			throw new IllegalArgumentException ("invalid source");
 
 		// MODE <target> [[<modes>[ <args>]] ...]
 
 		if ((params == null) || (params.length < 1))
 		{
 			Message message = Message.create (IRC.ERR_NEEDMOREPARAMS, src);
 
 			message.addParameter ("MODE");
 			message.addLastParameter (IRC.ERRMSG_NEEDMOREPARAMS);
 
 			src.send (message);
 
 			return;
 		}
 
 		doCommand (src, params);
 	}
 
 	/**
 	 * Processes the MODE command.
 	 * 
 	 * @param src
 	 *            the source.
 	 * @param params
 	 *            the parameters of the command.
 	 */
 	private void doCommand (Source src, String[] params)
 	{
 		if (src instanceof fr.hbis.ircs.User)
 		{
 			String target = params[0];
 
 			fr.hbis.ircs.User user = (fr.hbis.ircs.User) src;
 			fr.hbis.ircs.User dst = src.getServer ().getNetwork ().getUser (
 					target);
 
 			/* user mode */
 			if (dst != null)
 			{
 				if (!hasPermission (user, dst))
 				{
 					Message message = Message.create (IRC.ERR_USERSDONTMATCH,
 							target);
 					message.addParameter (target);
					message.addLastParameter (IRC.ERRMSG_USERSDONTMATCH);
 					src.send (message);
 
 					return;
 				}
 
 				/* set mode */
 				if (params.length > 1)
 				{
 					userMode (user, dst, params);
 				}
 				/* get mode */
 				else
 				{
 					Message message = Message.create (IRC.RPL_UMODEIS, src);
 					message.addParameter (dst.getModesList ());
 					src.send (message);
 				}
 			}
 			/* channel mode */
 			else
 			{
 				fr.hbis.ircs.Channel channel;
 
 				if (IRC.isLocalChannelNameValid (target))
 				{
 					channel = src.getServer ().getLocalChannel (target);
 				}
 				else
 				{
 					channel = src.getServer ().getNetwork ()
 							.getChannel (target);
 				}
 
 				if (channel == null)
 				{
 					Message message = Message.create (IRC.ERR_NOSUCHCHANNEL,
 							src);
 					message.addParameter (target);
 					message.addLastParameter (IRC.ERRMSG_NOSUCHCHANNEL);
 					src.send (message);
 
 					return;
 				}
 
 				if (!channel.isOn (user))
 				{
 					Message message = Message
 							.create (IRC.ERR_NOTONCHANNEL, src);
 					message.addParameter (target);
 					message.addLastParameter (IRC.ERRMSG_NOTONCHANNEL);
 					src.send (message);
 
 					return;
 				}
 
 				/* set mode */
 				if (params.length > 1)
 				{
 					if (channel.isModeless ())
 					{
 						Message message = Message.create (IRC.ERR_NOCHANMODES,
 								src);
 						message.addParameter (target);
 						message.addLastParameter (IRC.ERRMSG_NOCHANMODES);
 						src.send (message);
 
 						return;
 					}
 
 					if (!hasPermission (user, channel))
 					{
 						Message message = Message.create (
 								IRC.ERR_CHANOPRIVSNEEDED, src);
 						message.addParameter (target);
						message.addLastParameter (IRC.ERRMSG_CHANOPRIVSNEEDED);
 						src.send (message);
 
 						return;
 					}
 
 					channelMode (user, channel, params);
 				}
 				/* get mode */
 				else
 				{
 					Message message = Message.create (IRC.RPL_CHANNELMODEIS,
 							src);
 					message.addParameter (target);
 					message.addParameter (channel.getModesList ());
 					src.send (message);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks if a user has the permission to change the mode of an user.
 	 * 
 	 * @param src
 	 *            the source.
 	 * @param user
 	 *            the user to change mode.
 	 * @return <code>true</code> if the user has the permission;
 	 *         <code>false</code> otherwise.
 	 */
 	private boolean hasPermission (fr.hbis.ircs.User src, fr.hbis.ircs.User user)
 	{
 		if ((src == user)
 				|| (src.isModeSet (fr.hbis.ircs.User.USER_MODE_OPERATOR)))
 			return (true);
 
 		return (false);
 	}
 
 	/**
 	 * Checks if a user has the permission to change the mode of an channel.
 	 * 
 	 * @param src
 	 *            the source.
 	 * @param channel
 	 *            the channel to change mode.
 	 * @return <code>true</code> if the user has the permission;
 	 *         <code>false</code> otherwise.
 	 */
 	private boolean hasPermission (fr.hbis.ircs.User src,
 			fr.hbis.ircs.Channel channel)
 	{
 		if ((channel.isOperator (src))
 				|| (src.isModeSet (fr.hbis.ircs.User.USER_MODE_OPERATOR)))
 			return (true);
 
 		return (false);
 	}
 
 	/**
 	 * Changes the modes of a user.
 	 * 
 	 * @param src
 	 *            the source.
 	 * @param user
 	 *            the user.
 	 * @param params
 	 *            the mode parameters.
 	 */
 	private void userMode (fr.hbis.ircs.User src, fr.hbis.ircs.User user,
 			String[] params)
 	{
 		Map<Character, Flag> flags = new Hashtable<Character, Flag> ();
 		boolean state = true;
 
 		for (int i = 1; i < params.length; i++)
 		{
 			for (int j = 0; j < params[i].length (); j++)
 			{
 				char c = params[i].charAt (j);
 
 				switch (c)
 				{
 				case (fr.hbis.ircs.Mode.MODE_ADD):
 				{
 					state = true;
 
 					break;
 				}
 
 				case (fr.hbis.ircs.Mode.MODE_REMOVE):
 				{
 					state = false;
 
 					break;
 				}
 
 				case (fr.hbis.ircs.User.USER_MODE_AWAY):
 				{
 					break;
 				}
 
 				case (fr.hbis.ircs.User.USER_MODE_OPERATOR):
 				case (fr.hbis.ircs.User.USER_MODE_LOCALOPERATOR):
 				{
 					if (state)
 						break;
 
 					flags.put (Character.valueOf (c), new Flag (state));
 
 					break;
 				}
 
 				case (fr.hbis.ircs.User.USER_MODE_INVISIBLE):
 				case (fr.hbis.ircs.User.USER_MODE_WALLOPS):
 				case (fr.hbis.ircs.User.USER_MODE_RESTRICTED):
 				case (fr.hbis.ircs.User.USER_MODE_SERVERNOTICES):
 				{
 					flags.put (Character.valueOf (c), new Flag (state));
 
 					break;
 				}
 
 				default:
 				{
 					Message message = Message.create (IRC.ERR_UMODEUNKNOWNFLAG,
 							src);
 					message.addLastParameter (IRC.ERRMSG_UMODEUNKNOWNFLAG);
 					src.send (message);
 
 					continue;
 				}
 				}
 			}
 		}
 
 		StringBuilder addModesLine = new StringBuilder (String
 				.valueOf (fr.hbis.ircs.Mode.MODE_ADD));
 		StringBuilder removeModesLine = new StringBuilder (String
 				.valueOf (fr.hbis.ircs.Mode.MODE_REMOVE));
 
 		Iterator<Entry<Character, Flag>> iterator = flags.entrySet ()
 				.iterator ();
 
 		while (iterator.hasNext ())
 		{
 			Entry<Character, Flag> entry = iterator.next ();
 			Character mode = entry.getKey ();
 			Flag flag = entry.getValue ();
 
 			if (user.isModeSet (mode) == flag.m_bState)
 				continue;
 
 			user.changeMode (mode, flag.m_bState);
 
 			if (flag.m_bState)
 				addModesLine.append (mode);
 			else
 				removeModesLine.append (mode);
 		}
 
 		if ((addModesLine.length () > 1) || (removeModesLine.length () > 1))
 		{
 			Message message = Message.create (src.getServer (), "MODE", user);
 			if (addModesLine.length () > 1)
 				message.addParameter (addModesLine.toString ());
 			if (removeModesLine.length () > 1)
 				message.addParameter (removeModesLine.toString ());
 			user.send (message);
 		}
 	}
 
 	/**
 	 * Changes the modes of a channel.
 	 * 
 	 * @param src
 	 *            the source.
 	 * @param channel
 	 *            the channel.
 	 * @param params
 	 *            the mode parameters.
 	 */
 	private void channelMode (fr.hbis.ircs.User src,
 			fr.hbis.ircs.Channel channel, String[] params)
 	{
 		Map<Character, Flag> flags = new Hashtable<Character, Flag> ();
 		Set<String> addBans = new HashSet<String> ();
 		Set<String> removeBans = new HashSet<String> ();
 		Set<String> addExcepts = new HashSet<String> ();
 		Set<String> removeExcepts = new HashSet<String> ();
 		Set<String> addInvites = new HashSet<String> ();
 		Set<String> removeInvites = new HashSet<String> ();
 		Set<fr.hbis.ircs.User> addOperators = new HashSet<fr.hbis.ircs.User> ();
 		Set<fr.hbis.ircs.User> removeOperators = new HashSet<fr.hbis.ircs.User> ();
 		Set<fr.hbis.ircs.User> addVoices = new HashSet<fr.hbis.ircs.User> ();
 		Set<fr.hbis.ircs.User> removeVoices = new HashSet<fr.hbis.ircs.User> ();
 		int paramModeCount = 0;
 		boolean state = true;
 
 		for (int i = 1; i < params.length; i++)
 		{
 			String param = params[i];
 
 			for (int j = 0; j < param.length (); j++)
 			{
 				char c = param.charAt (j);
 
 				switch (c)
 				{
 				case (fr.hbis.ircs.Mode.MODE_ADD):
 				{
 					state = true;
 
 					break;
 				}
 
 				case (fr.hbis.ircs.Mode.MODE_REMOVE):
 				{
 					state = false;
 
 					break;
 				}
 
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_CHANNELCREATOR):
 				{
 					break;
 				}
 
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_ANONYMOUS):
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_INVITEONLY):
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_MODERATED):
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_NOEXTERNALMESSAGES):
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_PRIVATE):
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_QUIET):
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_SECRET):
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_TOPICPROTECTED):
 				{
 					flags.put (Character.valueOf (c), new Flag (state));
 
 					break;
 				}
 
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_REOP):
 				{
 					if (channel.isSafe () && !channel.isCreator (src))
 					{
 						Message message = Message.create (
 								IRC.ERR_CHANOPRIVSNEEDED, src);
 						message.addParameter (channel.getName ());
 						message.addParameter (IRC.ERRMSG_CHANOPRIVSNEEDED);
 						src.send (message);
 
 						return;
 					}
 
 					break;
 				}
 
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_KEY):
 				{
 					if (state && (i + 1 > params.length))
 						return;
 
 					String value;
 
 					if (state)
 					{
 						value = params[++i];
 						
 						paramModeCount++;						
 						if (paramModeCount > IRC.MAXCHANNELMODEPARAMS)
 							continue;
 					}
 					else
 						value = "";
 
 					Flag flag = flags.get (c);
 					if (flag != null)
 						continue;
 
 					flags.put (Character.valueOf (c), new Flag (state, value));
 
 					break;
 				}
 
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_LIMIT):
 				{
 					if (state && (i + 1 > params.length))
 					{
 						Message message = Message.create (
 								IRC.ERR_NEEDMOREPARAMS, src);
 						message.addParameter ("MODE");
 						message.addParameter (String
 								.valueOf (fr.hbis.ircs.Mode.MODE_ADD)
 								+ String.valueOf (c));
 						message.addLastParameter (IRC.ERRMSG_NEEDMOREPARAMS);
 
 						return;
 					}
 
 					String value;
 
 					if (state)
 					{
 						value = params[++i];
 						
 						paramModeCount++;						
 						if (paramModeCount > IRC.MAXCHANNELMODEPARAMS)
 							continue;
 					}
 					else
 						value = "";
 
 					Flag flag = flags.get (c);
 					if (flag != null)
 						continue;
 
 					flags.put (Character.valueOf (c), new Flag (state, value));
 
 					break;
 				}
 
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_BAN):
 				{
 					String mask = "";
 
 					if (i + 1 < params.length)
 					{
 						mask = IRC.getMask (params[++i]);
 						if (mask == null)
 							continue;
 
 						paramModeCount++;						
 						if (paramModeCount > IRC.MAXCHANNELMODEPARAMS)
 							continue;
 
 						if (channel.hasBan (mask) == state)
 							continue;
 
 						if (state)
 							addBans.add (mask);
 						else
 							removeBans.add (mask);
 					}
 					else
 					{
 						if (state)
 							addBans.add (mask);
 						else
 							continue;
 					}
 
 					break;
 				}
 
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_EXCEPTION):
 				{
 					String mask = "";
 
 					if (i + 1 < params.length)
 					{
 						mask = IRC.getMask (params[++i]);
 						if (mask == null)
 							continue;
 
 						paramModeCount++;						
 						if (paramModeCount > IRC.MAXCHANNELMODEPARAMS)
 							continue;
 
 						if (channel.hasExcept (mask) == state)
 							continue;
 
 						if (state)
 							addExcepts.add (mask);
 						else
 							removeExcepts.add (mask);
 					}
 					else
 					{
 						if (state)
 							addExcepts.add (mask);
 						else
 							continue;
 					}
 
 					break;
 				}
 
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_INVITATION):
 				{
 					String mask = "";
 
 					if (i + 1 < params.length)
 					{
 						mask = IRC.getMask (params[++i]);
 						if (mask == null)
 							continue;
 
 						paramModeCount++;						
 						if (paramModeCount > IRC.MAXCHANNELMODEPARAMS)
 							continue;
 
 						if (channel.hasInvite (mask) == state)
 							continue;
 
 						if (state)
 							addInvites.add (mask);
 						else
 							removeInvites.add (mask);
 					}
 					else
 					{
 						if (state)
 							addInvites.add (mask);
 						else
 							continue;
 					}
 
 					break;
 				}
 
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_OPERATOR):
 				{
 					if (i + 1 >= params.length)
 					{
 						Message message = Message.create (
 								IRC.ERR_NEEDMOREPARAMS, src);
 						message.addParameter ("MODE");
 						message.addParameter (String
 								.valueOf (fr.hbis.ircs.Mode.MODE_ADD)
 								+ String.valueOf (c));
 						message.addLastParameter (IRC.ERRMSG_NEEDMOREPARAMS);
 
 						return;
 					}
 
 					String nick = params[++i];
 					
 					paramModeCount++;					
 					if (paramModeCount > IRC.MAXCHANNELMODEPARAMS)
 						continue;
 
 					fr.hbis.ircs.User dst = src.getServer ().getNetwork ()
 							.getUser (nick);
 					if (dst == null)
 					{
 						Message message = Message.create (IRC.ERR_NOSUCHNICK,
 								src);
 						message.addParameter (nick);
 						message.addLastParameter (IRC.ERRMSG_NOSUCHNICK);
 						src.send (message);
 
 						continue;
 					}
 
 					if (!channel.isOn (dst))
 					{
 						Message message = Message.create (
 								IRC.ERR_USERNOTINCHANNEL, src);
 						message.addParameter (dst.getNick ());
 						message.addParameter (channel.getName ());
 						message.addLastParameter (IRC.ERRMSG_USERNOTINCHANNEL);
 						src.send (message);
 
 						break;
 					}
 
 					if (state)
 						addOperators.add (dst);
 					else
 						removeOperators.add (dst);
 
 					break;
 				}
 
 				case (fr.hbis.ircs.Channel.CHANNEL_MODE_VOICE):
 				{
 					if (i + 1 >= params.length)
 					{
 						Message message = Message.create (
 								IRC.ERR_NEEDMOREPARAMS, src);
 						message.addParameter ("MODE");
 						message.addParameter (String
 								.valueOf (fr.hbis.ircs.Mode.MODE_ADD)
 								+ String.valueOf (c));
 						message.addLastParameter (IRC.ERRMSG_NEEDMOREPARAMS);
 
 						return;
 					}
 
 					String nick = params[++i];
 					
 					paramModeCount++;					
 					if (paramModeCount > IRC.MAXCHANNELMODEPARAMS)
 						continue;
 
 					fr.hbis.ircs.User dst = src.getServer ().getNetwork ()
 							.getUser (nick);
 					if (dst == null)
 					{
 						Message message = Message.create (IRC.ERR_NOSUCHNICK,
 								src);
 						message.addParameter (nick);
 						message.addLastParameter (IRC.ERRMSG_NOSUCHNICK);
 						src.send (message);
 
 						continue;
 					}
 
 					if (!channel.isOn (dst))
 					{
 						Message message = Message.create (
 								IRC.ERR_USERNOTINCHANNEL, src);
 						message.addParameter (dst.getNick ());
 						message.addParameter (channel.getName ());
 						message.addLastParameter (IRC.ERRMSG_USERNOTINCHANNEL);
 						src.send (message);
 
 						break;
 					}
 
 					if (state)
 						addVoices.add (dst);
 					else
 						removeVoices.add (dst);
 
 					break;
 				}
 
 				default:
 				{
 					Message message = Message.create (IRC.ERR_UNKNOWNMODE, src);
 					message.addParameter (String.valueOf (c));
 					message.addLastParameter (IRC.ERRMSG_UNKNOWNMODE);
 					src.send (message);
 
 					continue;
 				}
 				}
 			}
 		}
 
 		StringBuilder addModesLine = new StringBuilder (String
 				.valueOf (fr.hbis.ircs.Mode.MODE_ADD));
 		StringBuilder addModesParamsLine = new StringBuilder ();
 		StringBuilder removeModesLine = new StringBuilder (String
 				.valueOf (fr.hbis.ircs.Mode.MODE_REMOVE));
 		StringBuilder removeModesParamsLine = new StringBuilder ();
 
 		Iterator<Entry<Character, Flag>> iterator = flags.entrySet ()
 				.iterator ();
 
 		while (iterator.hasNext ())
 		{
 			Entry<Character, Flag> entry = iterator.next ();
 			Character mode = entry.getKey ();
 			Flag flag = entry.getValue ();
 
 			switch (mode)
 			{
 			case (fr.hbis.ircs.Channel.CHANNEL_MODE_ANONYMOUS):
 			{
 				if (!channel.isLocal () && !channel.isSafe ())
 				{
 					Message message = Message.create (IRC.ERR_UNKNOWNMODE, src);
 					message.addParameter (String.valueOf (mode));
 					message.addLastParameter (IRC.ERRMSG_UNKNOWNMODE);
 					src.send (message);
 
 					return;
 				}
 
 				if (channel.isSafe () && flag.m_bState
 						&& (channel.getCreator () != src))
 				{
 					Message message = Message.create (
 							IRC.ERR_UNIQOPPRIVSNEEDED, src);
 					message.addLastParameter (IRC.ERRMSG_UNIQOPPRIVSNEEDED);
 					src.send (message);
 
 					return;
 				}
 
 				channel.changeMode (
 						fr.hbis.ircs.Channel.CHANNEL_MODE_ANONYMOUS,
 						flag.m_bState);
 				if (flag.m_bState)
 					addModesLine.append (mode);
 				else
 					removeModesLine.append (mode);
 
 				break;
 			}
 
 			case (fr.hbis.ircs.Channel.CHANNEL_MODE_INVITEONLY):
 			{
 				channel.removeAllInvitations ();
 
 				channel.changeMode (
 						fr.hbis.ircs.Channel.CHANNEL_MODE_INVITEONLY,
 						flag.m_bState);
 				if (flag.m_bState)
 					addModesLine.append (mode);
 				else
 					removeModesLine.append (mode);
 
 				break;
 			}
 			
 			case (fr.hbis.ircs.Channel.CHANNEL_MODE_MODERATED):
 			{
 				if (flag.m_bState
 						&& channel
 								.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_MODERATED))
 					continue;
 
 				channel.changeMode (fr.hbis.ircs.Channel.CHANNEL_MODE_MODERATED,
 						flag.m_bState);
 				if (flag.m_bState)
 					addModesLine.append (mode);
 				else
 					removeModesLine.append (mode);
 
 				break;
 			}
 			
 			case (fr.hbis.ircs.Channel.CHANNEL_MODE_NOEXTERNALMESSAGES):
 			{
 				if (flag.m_bState
 						&& channel
 								.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_NOEXTERNALMESSAGES))
 					continue;
 
 				channel.changeMode (fr.hbis.ircs.Channel.CHANNEL_MODE_NOEXTERNALMESSAGES,
 						flag.m_bState);
 				if (flag.m_bState)
 					addModesLine.append (mode);
 				else
 					removeModesLine.append (mode);
 
 				break;
 			}
 				
 			case (fr.hbis.ircs.Channel.CHANNEL_MODE_PRIVATE):
 			{
 				if (flag.m_bState
 						&& channel
 								.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_SECRET))
 					continue;
 
 				channel.changeMode (fr.hbis.ircs.Channel.CHANNEL_MODE_PRIVATE,
 						flag.m_bState);
 				if (flag.m_bState)
 					addModesLine.append (mode);
 				else
 					removeModesLine.append (mode);
 
 				break;
 			}
 
 			case (fr.hbis.ircs.Channel.CHANNEL_MODE_QUIET):
 			{
 				Message message = Message.create (IRC.ERR_UNKNOWNMODE, src);
 				message.addParameter (String.valueOf (mode));
 				message.addLastParameter (IRC.ERRMSG_UNKNOWNMODE);
 				src.send (message);
 
 				return;
 			}
 
 			case (fr.hbis.ircs.Channel.CHANNEL_MODE_REOP):
 			{
 				if (!channel.isSafe ())
 				{
 					Message message = Message.create (IRC.ERR_UNKNOWNMODE, src);
 					message.addParameter (String.valueOf (mode));
 					message.addLastParameter (IRC.ERRMSG_UNKNOWNMODE);
 					src.send (message);
 
 					return;
 				}
 
 				channel.changeMode (fr.hbis.ircs.Channel.CHANNEL_MODE_REOP,
 						flag.m_bState);
 				if (flag.m_bState)
 					addModesLine.append (mode);
 				else
 					removeModesLine.append (mode);
 
 				break;
 			}
 
 			case (fr.hbis.ircs.Channel.CHANNEL_MODE_SECRET):
 			{
 				if (flag.m_bState
 						&& channel
 								.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_PRIVATE))
 					continue;
 
 				channel.changeMode (fr.hbis.ircs.Channel.CHANNEL_MODE_SECRET,
 						flag.m_bState);
 				if (flag.m_bState)
 					addModesLine.append (mode);
 				else
 					removeModesLine.append (mode);
 
 				break;
 			}
 			
 			case (fr.hbis.ircs.Channel.CHANNEL_MODE_TOPICPROTECTED):
 			{
 				if (flag.m_bState
 						&& channel
 								.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_TOPICPROTECTED))
 					continue;
 
 				channel.changeMode (fr.hbis.ircs.Channel.CHANNEL_MODE_TOPICPROTECTED,
 						flag.m_bState);
 				if (flag.m_bState)
 					addModesLine.append (mode);
 				else
 					removeModesLine.append (mode);
 
 				break;
 			}
 
 			case (fr.hbis.ircs.Channel.CHANNEL_MODE_KEY):
 			{
 				String key;
 
 				if (flag.m_bState)
 				{
 					if (channel
 							.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_KEY))
 					{
 						Message message = Message.create (src, "MODE", channel);
 						message.addParameter (String
 								.valueOf (fr.hbis.ircs.Mode.MODE_REMOVE)
 								+ String.valueOf (mode));
 						message.addParameter (channel.getKey ());
 						channel.send (message);
 					}
 
 					key = flag.m_strData;
 				}
 				else
 				{
 					key = "";
 				}
 
 				channel.setKey (key);
 
 				channel.changeMode (fr.hbis.ircs.Channel.CHANNEL_MODE_KEY,
 						flag.m_bState);
 				if (flag.m_bState)
 				{
 					addModesLine.append (mode);
 					addModesParamsLine.append (" " + key);
 				}
 				else
 					removeModesLine.append (mode);
 
 				break;
 			}
 
 			case (fr.hbis.ircs.Channel.CHANNEL_MODE_LIMIT):
 			{
 				int limit;
 
 				if (flag.m_bState)
 				{
 					try
 					{
 						limit = Integer.parseInt (flag.m_strData);
 					}
 					catch (NumberFormatException numberFormatException)
 					{
 						continue;
 					}
 
 					if (limit < 1)
 						continue;
 				}
 				else
 				{
 					limit = 0;
 				}
 
 				channel.setLimit (limit);
 
 				channel.changeMode (fr.hbis.ircs.Channel.CHANNEL_MODE_LIMIT,
 						flag.m_bState);
 				if (flag.m_bState)
 				{
 					addModesLine.append (mode);
 					addModesParamsLine.append (" " + limit);
 				}
 				else
 					removeModesLine.append (mode);
 
 				break;
 			}
 
 			default:
 			{
 				break;
 			}
 			}
 		}
 
 		if (addBans.size () > 0)
 		{
 			for (String mask : addBans)
 			{
 				if ("".equals (mask))
 				{
 					String[] bans = channel.getBanList ();
 
 					for (String ban : bans)
 					{
 						Message message = Message.create (IRC.RPL_BANLIST, src);
 						message.addParameter (channel.getName ());
 						message.addParameter (ban);
 						src.send (message);
 					}
 
 					Message message = Message
 							.create (IRC.RPL_ENDOFBANLIST, src);
 					message.addParameter (channel.getName ());
 					message.addLastParameter (IRC.RPLMSG_ENDOFBANLIST);
 					src.send (message);
 
 					continue;
 				}
 
 				channel.addBan (src, mask);
 
 				addModesLine.append (fr.hbis.ircs.Channel.CHANNEL_MODE_BAN);
 				addModesParamsLine.append (" " + mask);
 			}
 		}
 
 		if (removeBans.size () > 0)
 		{
 			for (String mask : removeBans)
 			{
 				channel.removeBan (mask);
 
 				removeModesLine.append (fr.hbis.ircs.Channel.CHANNEL_MODE_BAN);
 				removeModesParamsLine.append (" " + mask);
 			}
 		}
 
 		if (addExcepts.size () > 0)
 		{
 			for (String mask : addExcepts)
 			{
 				if ("".equals (mask))
 				{
 					String[] bans = channel.getExceptList ();
 
 					for (String ban : bans)
 					{
 						Message message = Message.create (IRC.RPL_EXCEPTLIST,
 								src);
 						message.addParameter (channel.getName ());
 						message.addParameter (ban);
 						src.send (message);
 					}
 
 					Message message = Message.create (IRC.RPL_ENDOFEXCEPTLIST,
 							src);
 					message.addParameter (channel.getName ());
 					message.addLastParameter (IRC.RPLMSG_ENDOFEXCEPTLIST);
 					src.send (message);
 
 					continue;
 				}
 
 				channel.addExcept (src, mask);
 
 				addModesLine
 						.append (fr.hbis.ircs.Channel.CHANNEL_MODE_EXCEPTION);
 				addModesParamsLine.append (" " + mask);
 			}
 		}
 
 		if (removeExcepts.size () > 0)
 		{
 			for (String mask : removeExcepts)
 			{
 				channel.removeExcept (mask);
 
 				removeModesLine
 						.append (fr.hbis.ircs.Channel.CHANNEL_MODE_EXCEPTION);
 				removeModesParamsLine.append (" " + mask);
 			}
 		}
 
 		if (addInvites.size () > 0)
 		{
 			for (String mask : addInvites)
 			{
 				if ("".equals (mask))
 				{
 					String[] bans = channel.getInviteList ();
 
 					for (String ban : bans)
 					{
 						Message message = Message.create (IRC.RPL_INVITELIST,
 								src);
 						message.addParameter (channel.getName ());
 						message.addParameter (ban);
 						src.send (message);
 					}
 
 					Message message = Message.create (IRC.RPL_ENDOFINVITELIST,
 							src);
 					message.addParameter (channel.getName ());
 					message.addLastParameter (IRC.RPLMSG_ENDOFINVITELIST);
 					src.send (message);
 
 					continue;
 				}
 
 				channel.addInvite (src, mask);
 
 				addModesLine
 						.append (fr.hbis.ircs.Channel.CHANNEL_MODE_INVITATION);
 				addModesParamsLine.append (" " + mask);
 			}
 		}
 
 		if (removeInvites.size () > 0)
 		{
 			for (String mask : removeInvites)
 			{
 				channel.removeInvite (mask);
 
 				removeModesLine
 						.append (fr.hbis.ircs.Channel.CHANNEL_MODE_INVITATION);
 				removeModesParamsLine.append (" " + mask);
 			}
 		}
 
 		if (addOperators.size () > 0)
 		{
 			for (fr.hbis.ircs.User dst : addOperators)
 			{
 				channel.setOperator (dst, true);
 
 				addModesLine
 						.append (fr.hbis.ircs.Channel.CHANNEL_MODE_OPERATOR);
 				addModesParamsLine.append (" " + dst.getNick ());
 			}
 		}
 
 		if (removeOperators.size () > 0)
 		{
 			for (fr.hbis.ircs.User dst : removeOperators)
 			{
 				channel.setOperator (dst, false);
 
 				removeModesLine
 						.append (fr.hbis.ircs.Channel.CHANNEL_MODE_OPERATOR);
 				removeModesParamsLine.append (" " + dst.getNick ());
 			}
 		}
 
 		if (addVoices.size () > 0)
 		{
 			for (fr.hbis.ircs.User dst : addVoices)
 			{
 				channel.setVoice (dst, true);
 
 				addModesLine.append (fr.hbis.ircs.Channel.CHANNEL_MODE_VOICE);
 				addModesParamsLine.append (" " + dst.getNick ());
 			}
 		}
 
 		if (removeVoices.size () > 0)
 		{
 			for (fr.hbis.ircs.User dst : removeVoices)
 			{
 				channel.setVoice (dst, false);
 
 				removeModesLine
 						.append (fr.hbis.ircs.Channel.CHANNEL_MODE_VOICE);
 				removeModesParamsLine.append (" " + dst.getNick ());
 			}
 		}
 
 		if ((addModesLine.length () > 1) || (removeModesLine.length () > 1))
 		{
 			Message message = Message.create (src, "MODE", channel);
 			if (addModesLine.length () > 1)
 				message.addParameter (addModesLine.toString ());
 			if (addModesParamsLine.length () > 1)
 				message.addParameter (addModesParamsLine.toString ().substring (
 						1));
 			if (removeModesLine.length () > 1)
 				message.addParameter (removeModesLine.toString ());
 			if (removeModesParamsLine.length () > 1)
 				message.addParameter (removeModesParamsLine.toString ()
 						.substring (1));
 			channel.send (message);
 		}
 	}
 
 	/**
 	 * The class <code>Flag</code> implements a user or channel mode flag.
 	 * 
 	 * @author bhuisgen
 	 */
 	private static class Flag
 	{
 		/**
 		 * Constructs a new <code>Flag</code> object.
 		 * 
 		 * @param state
 		 *            the state.
 		 */
 		public Flag (boolean state)
 		{
 			m_bState = state;
 			m_strData = "";
 		}
 
 		/**
 		 * Constructs a new <code>Flag</code> object.
 		 * 
 		 * @param state
 		 *            the state.
 		 * @param data
 		 *            the data associated.
 		 */
 		public Flag (boolean state, String data)
 		{
 			m_bState = state;
 			m_strData = data;
 		}
 
 		private boolean m_bState;
 		private String m_strData;
 	};
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Command#getName()
 	 */
 	public String getName ()
 	{
 		return ("MODE");
 	}
 
 	private Manager m_manager;
 }
