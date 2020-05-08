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
 
 import fr.hbis.ircs.Channel;
 import fr.hbis.ircs.Command;
 import fr.hbis.ircs.IRC;
 import fr.hbis.ircs.Manager;
 import fr.hbis.ircs.Message;
 import fr.hbis.ircs.Source;
 
 /**
  * The class <code>Privmsg</code> implements the PRIVMSG command.
  * 
  * @author bhuisgen
  * 
  */
 public class Privmsg extends Command
 {
 	/**
 	 * Construct a new <code>Privmsg</code> object.
 	 * 
 	 * @param manager
 	 *            the manager.
 	 */
 	public Privmsg (Manager manager)
 	{
 		m_manager = manager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.hbis.ircs.Command#run(fr.hbis.ircs.Source,
 	 * java.lang.String[])
 	 */
 	public void run (Source src, String[] params)
 	{
 		// PRIVMSG <target> :<msg>
 
 		if ((params == null) || (params.length < 1))
 		{
 			Message message = Message.create (IRC.ERR_NORECIPIENT, src);
 
 			message.addParameter ("USER");
 			message.addLastParameter (IRC.ERRMSG_NORECIPIENT);
 
 			src.send (message);
 
 			return;
 		}
 
 		if (params.length < 2)
 		{
 			Message message = Message.create (IRC.ERR_NOTEXTTOSEND, src);
 
 			message.addParameter ("USER");
 			message.addLastParameter (IRC.ERRMSG_NOTEXTTOSEND);
 
 			src.send (message);
 
 			return;
 		}
 
 		doCommand (src, params);
 	}
 
 	/**
 	 * Process the PRIVMSG command.
 	 * 
 	 * @param src
 	 *            the source.
 	 * @param params
 	 *            the parameters of the command.
 	 */
 	private void doCommand (Source src, String[] params)
 	{
 		String target = params[0];
 		String msg = params[1];
 
 		/* message to channel */
 		if (IRC.isChannelNameValid (target))
 		{
 			if (IRC.isLocalChannelNameValid (target))
 			{
 				Message message = Message.create (IRC.ERR_NOSUCHCHANNEL, src);
 				message.addParameter (target);
 				message.addLastParameter (IRC.ERRMSG_NOSUCHCHANNEL);
 				src.send (message);
 
 				return;
 			}
 
 			Channel channel = src.getServer ().getNetwork ()
 					.getChannel (target);
 			if (channel == null)
 			{
 				Message message = Message.create (IRC.ERR_NOSUCHCHANNEL, src);
 				message.addParameter (target);
 				message.addLastParameter (IRC.ERRMSG_NOSUCHCHANNEL);
 				src.send (message);
 
 				return;
 			}
 
 			fr.hbis.ircs.User user = (fr.hbis.ircs.User) src;
 
 			if (channel.isModeSet (Channel.CHANNEL_MODE_MODERATED)
 					&& (!channel.isOperator (user) || !channel.isVoice (user)))
 			{
 				Message message = Message.create (IRC.ERR_CANNOTSENDTOCHAN,
 						user);
 				message.addParameter (channel.getName ());
 				message.addLastParameter (IRC.ERRMSG_CANNOTSENDTOCHAN);
 
 				return;
 			}
 
 			Message message = Message.create (user, "PRIVMSG", channel);
 			message.addLastParameter (msg);
 			channel.send (message, user);
 
 		}
 		/* message to user */
 		else
 		{
 			fr.hbis.ircs.User dst = src.getServer ().getNetwork ()
 					.getUser (target);
 			if (dst == null)
 			{
 				Message message = Message.create (IRC.ERR_NOSUCHNICK, src);
 				message.addParameter (target);
 				message.addLastParameter (IRC.ERRMSG_NOSUCHNICK);
 				src.send (message);
 
 				return;
 			}
 
			fr.hbis.ircs.User user = (fr.hbis.ircs.User) dst;
 
 			if (dst.isModeSet (fr.hbis.ircs.User.USER_MODE_AWAY))
 			{
				Message message = Message.create (IRC.RPL_AWAY, user);
 				message.addParameter (dst.getNick ());
 				message.addLastParameter (dst.getAwayMessage ());
				user.send (message);
 			}
 
			Message message = Message.create (user, "PRIVMSG", src);
 			message.addLastParameter (msg);
			dst.send (message);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Command#getName()
 	 */
 	public String getName ()
 	{
 		return ("PRIVMSG");
 	}
 
 	private Manager m_manager;
 }
