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
 
 import fr.hbis.ircs.Command;
 import fr.hbis.ircs.IRC;
 import fr.hbis.ircs.Manager;
 import fr.hbis.ircs.Message;
 import fr.hbis.ircs.Source;
 
 /**
  * The class <code>Names</code> implements the NAMES command.
  * 
  * @author bhuisgen
  */
 public class Names extends Command
 {
 	/**
 	 * Constructs a new <code>Names</code> object.
 	 * 
 	 * @param manager
 	 *            the manager.
 	 */
 	public Names (Manager manager)
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
 		// NAMES [<channel1>[,<channel2>,...]]
 
 		doCommand (src, params);
 	}
 
 	/**
 	 * Processes the NAMES command.
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
 			fr.hbis.ircs.User user = (fr.hbis.ircs.User) src;
 
 			fr.hbis.ircs.Channel channels[];
 
 			if ((params == null) || (params.length < 1))
 			{
 				channels = src.getServer ().getNetwork ().getChannels ();
 			}
 			else
 			{
 				String channelsList = params[0];
 				String channelNames[] = channelsList.split (",");
 
 				for (String channelName : channelNames)
 				{
 					fr.hbis.ircs.Channel channel;
 
 					if (IRC.isLocalChannelNameValid (channelName))
 					{
 						channel = src.getServer ().getLocalChannel (channelName);
 					}
 					else
 					{
 						channel = src.getServer ().getNetwork ()
 								.getChannel (channelName);
 					}
 					
 					if (channel == null)
 						continue;
 
 					boolean isOn = channel.isOn (user);
 
					if (isOn
 							&& (channel.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_ANONYMOUS)
 									|| channel.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_PRIVATE) ||
 									channel.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_QUIET) ||
 									channel.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_SECRET)))
 						continue;
 
 					StringBuilder usersList = new StringBuilder ();
 
 					for (fr.hbis.ircs.User dst : channel.getUsers ())
 					{
 						if (!isOn
 								&& dst
 										.isModeSet (fr.hbis.ircs.User.USER_MODE_INVISIBLE))
 							continue;
 
 						if (channel.isOperator (dst))
 							usersList.append (' ').append (IRC.OPERATOR_PREFIX);
 						else if (channel.isVoice (dst))
 							usersList.append (' ').append (IRC.VOICE_PREFIX);
 						else
 							usersList.append (' ');
 
 						usersList.append (dst.getNick ());
 					}
 
 					String namesList;
 
 					if (usersList.length () > 0)
 						namesList = usersList.substring (1);
 					else
 						namesList = "";
 
 					String channelPrefix;
 
 					if (channel
 							.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_SECRET))
 						channelPrefix = "@";
 					else if (channel
 							.isModeSet (fr.hbis.ircs.Channel.CHANNEL_MODE_PRIVATE))
 						channelPrefix = "*";
 					else
 						channelPrefix = "=";
 
 					Message message = Message.create (IRC.RPL_NAMREPLY, user);
 					message.addParameter (channelPrefix);
 					message.addParameter (channel.getName ());
 					message.addLastParameter (namesList);
 					user.send (message);
 
 					message = Message.create (IRC.RPL_ENDOFNAMES, user);
 					message.addParameter (channel.getName ());
 					message.addLastParameter (IRC.RPLMSG_ENDOFNAMES);
 					user.send (message);
 				}
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Command#getName()
 	 */
 	public String getName ()
 	{
 		return ("NAMES");
 	}
 
 	private Manager m_manager;
 }
