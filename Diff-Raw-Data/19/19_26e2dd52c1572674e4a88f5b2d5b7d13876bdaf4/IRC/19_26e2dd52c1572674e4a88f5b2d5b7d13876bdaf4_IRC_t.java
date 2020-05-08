 /*
  *  hbIRCS
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
 
 package fr.hbis.ircs;
 
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The class <code>IRC</code> defines all IRC stuff.
  * 
  * @author bhuisgen
  * 
  */
 public class IRC
 {
 	/**
 	 * Private constructor.
 	 */
 	private IRC ()
 	{
 	}
 
 	/*
 	 * Functions
 	 */
 
 	/**
 	 * Tests if a channel name is valid.
 	 * 
 	 * @param channelName
 	 *            the channel name.
 	 * 
 	 * @return <code>true</code> if the channel name is valid;
 	 *         <code>false</code> otherwise.
 	 */
 	public static final boolean isChannelNameValid (String channelName)
 	{
 		if ((channelName == null) || ("".equals (channelName)))
 			throw new IllegalArgumentException ("invalid channel");
 
 		if (channelName.length () > IRC.CHANNELMAXLEN)
 			return (false);
 
 		return (IRC.IRC_VALID_CHANNEL.matcher (channelName).matches ());
 	}
 
 	/**
 	 * Tests if a local channel name is valid.
 	 * 
 	 * @param channelName
 	 *            the channel name.
 	 * @return
 	 *         <code>true<code> if the local channel name is valid; <code>false</code>
 	 *         otherwise.
 	 */
 	public static final boolean isLocalChannelNameValid (String channelName)
 	{
 		if ((channelName == null) || ("".equals (channelName)))
 			throw new IllegalArgumentException ("invalid channel");
 
 		if (channelName.charAt (0) != IRC.CHANNEL_LOCAL_PREFIX)
 			return (false);
 
 		return (IRC.isChannelNameValid (channelName));
 	}
 
 	/**
 	 * Tests if a safe channel name is valid.
 	 * 
 	 * @param channelName
 	 *            the channel name.
 	 * @return
 	 *         <code>true<code> if the safe channel name is valid; <code>false</code>
 	 *         otherwise.
 	 */
 	public static final boolean isSafeChannelNameValid (String channelName)
 	{
 		if ((channelName == null) || ("".equals (channelName)))
 			throw new IllegalArgumentException ("invalid channel");
 
 		if (channelName.charAt (0) != IRC.CHANNEL_SAFE_PREFIX)
 			return (false);
 		
 		return (IRC.isChannelNameValid (channelName));
 	}
 
 	/**
 	 * Tests if a nickname is valid.
 	 * 
 	 * @param nick
 	 *            the nick.
 	 * 
 	 * @return <code>true</code> if the nick is valid; <code>false</code>
 	 *         otherwise.
 	 */
 	public static final boolean isNicknameValid (String nick)
 	{
 		if ((nick == null) || ("".equals (nick)))
 			throw new IllegalArgumentException ("invalid nick");
 
 		if (nick.length () > IRC.NICKMAXLEN)
 			return (false);
 
 		if (nick.equalsIgnoreCase (IRC.ANONYMOUS_NICK))
 			return (false);
 
 		return (IRC.IRC_VALID_NICKNAME.matcher (nick).matches ());
 	}
 
 	/**
 	 * Tests if a full hostmask is valid.
 	 * 
 	 * @param hostmask
 	 *            the hostmask to validate.
 	 * 
 	 * @return <code>true</code> if the hostmask is valid; <code>false</code>
 	 *         otherwise.
 	 */
 	public static final boolean isHostmaskValid (String hostmask)
 	{
 		if ((hostmask == null) || ("".equals (hostmask)))
 			throw new IllegalArgumentException ("invalid hostmask");
 
 		Matcher mMessage = IRC.IRC_VALID_MASK.matcher (hostmask);
 
 		if (!mMessage.matches ())
 			return (false);
 
 		String nick = mMessage.group (IRC.IRC_VALID_MASK_NICK);
 		String user = mMessage.group (IRC.IRC_VALID_MASK_USER);
 		String host = mMessage.group (IRC.IRC_VALID_MASK_HOST);
 
 		if ((nick == null) || !IRC.isNicknameValid (nick))
 			return (false);
 
 		if ((user == null) || !IRC.IRC_VALID_USER.matcher (user).matches ())
 			return (false);
 
 		if ((host == null) || !IRC.IRC_VALID_HOST.matcher (host).matches ())
 			return (false);
 
 		return (true);
 	}
 
 	/**
 	 * Gets the full format of a hostmask from given nickname, username and
 	 * hostname. Each arguments are firstly validated.
 	 * 
 	 * @param nick
 	 *            the nickname.
 	 * @param user
 	 *            the username.
 	 * @param host
 	 *            the hostname.
 	 * 
 	 * @return the hostmask or <code>null</code> if any argument is invalid.
 	 */
 	public static final String getHostmaskFromTokens (String nick, String user,
 			String host)
 	{
 		if ((nick == null) || ("".equals (nick)))
 			throw new IllegalArgumentException ("invalid nickname");
 
 		if ((user == null) || ("".equals (user)))
 			throw new IllegalArgumentException ("invalid username");
 
 		if ((host == null) || ("".equals (host)))
 			throw new IllegalArgumentException ("invalid hostname");
 
 		if (!IRC.isNicknameValid (nick))
 			return (null);
 
 		if (!IRC.IRC_VALID_USER.matcher (user).matches ())
 			return (null);
 
 		if (!IRC.IRC_VALID_HOST.matcher (host).matches ())
 			return (null);
 
 		String hostmask;
 
 		hostmask = nick + String.valueOf (IRC.PREFIX_USERNAME) + user
 				+ String.valueOf (IRC.PREFIX_HOSTNAME) + host;
 
 		return (hostmask);
 	}
 
 	/**
 	 * Tests if a full mask is valid.
 	 * 
 	 * @param mask
 	 *            the mask to validate.
 	 * 
 	 * @return <code>true</code> if the mask is valid; <code>false</code>
 	 *         otherwise.
 	 */
 	public static final boolean isMaskValid (String mask)
 	{
 		if ((mask == null) || ("".equals (mask)))
 			throw new IllegalArgumentException ("invalid mask");
 
 		Matcher mMessage = IRC.IRC_VALID_MASK.matcher (mask);
 
 		if (!mMessage.matches ()
 				&& (mMessage.group (IRC.IRC_VALID_MASK_NICK) != null)
 				&& (mMessage.group (IRC.IRC_VALID_MASK_USER) != null)
 				&& (mMessage.group (IRC.IRC_VALID_MASK_HOST) != null))
 			return (false);
 
 		return (true);
 	}
 
 	/**
 	 * Tests if a a given string matches a mask.
 	 * 
 	 * @param mask
 	 *            the mask.
 	 * @param str
 	 *            the string.
 	 * @return <code>true</code> if and only if the string matches the mask;
 	 *         <code>false</code> otherwise.
 	 */
 	public static final boolean isMaskMatched (String mask, String str)
 	{
 		if ((mask == null) || ("".equals (mask)))
 			throw new IllegalArgumentException ("invalid mask");
 
 		if ((str == null) || ("".equals (str)))
 			throw new IllegalArgumentException ("invalid string");
 
 		int i, j;
 		char c1, c2;
 
 		for (i = 0, j = 0; i < mask.length (); i++, j++)
 		{
 			c1 = Character.toLowerCase (mask.charAt (i));
 			c2 = Character.toLowerCase (str.charAt (j));
 
 			/*
 			 * buffer has multiple characters wildcard
 			 */
 			if (c2 == IRC.WILDCARD_MANY)
 			{
 				return (IRC.isMaskMatched (str.substring (j), mask
 						.substring (i)));
 			}
 			/*
 			 * buffer has one character wildcard
 			 */
 			else if (c2 == IRC.WILDCARD_ONE)
 			{
 				return (IRC.isMaskMatched (str.substring (j), mask
 						.substring (i)));
 			}
 			/*
 			 * mask has multiple characters wildcard
 			 */
 			else if (c1 == IRC.WILDCARD_MANY)
 			{
 				// ignore next mask wildcards
 				do
 				{
 					// end of buffer
 					if (i + 1 >= mask.length ())
 						return (true);
 
 					i++;
 					c1 = Character.toLowerCase (mask.charAt (i));
 				}
 				while ((c1 == IRC.WILDCARD_MANY) || (c1 == IRC.WILDCARD_ONE));
 
 				// search the next mask character
 				for (; c2 != c1; j++)
 				{
 					// end of buffer
 					if (j >= str.length ())
 					{
 						// buffer has too a wildcard
 						if ((c2 == IRC.WILDCARD_MANY)
 								|| (c2 == IRC.WILDCARD_ONE))
 							return (true);
 
 						return (false);
 					}
 
 					c2 = Character.toLowerCase (str.charAt (j));
 
 					/*
 					 * buffer has multiple characters wildcard
 					 */
 					if (c2 == IRC.WILDCARD_MANY)
 					{
 						// ignore next buffer wildcards
 						do
 						{
 							// end of buffer
 							if (j + 1 >= str.length ())
 								return (true);
 
 							j++;
 							c2 = Character.toLowerCase (str.charAt (j));
 						}
 						while ((c2 == IRC.WILDCARD_MANY)
 								|| (c2 == IRC.WILDCARD_ONE));
 
 						break;
 					}
 					/*
 					 * buffer has one character wildcard
 					 */
 					else if (c2 == IRC.WILDCARD_ONE)
 					{
 						// end of buffer
 						if (j + 1 >= str.length ())
 							return (true);
 
 						j++;
 
 						break;
 					}
 				}
 
 				j--;
 			}
 			/*
 			 * mask has one character wildcard
 			 */
 			else if (c1 == IRC.WILDCARD_ONE)
 			{
 				if (c2 != IRC.WILDCARD_MANY)
 				{
 					// end of buffer
 					if (j + 1 >= str.length ())
 						return (true);
 				}
 
 				continue;
 			}
 			/*
 			 * mask has no special character
 			 */
 			else
 			{
 				if (c1 != c2)
 					return (false);
 			}
 		}
 
 		// mask and buffer ends
 		if (j >= str.length ())
 			return (true);
 
 		return (false);
 	}
 
 	/**
 	 * Gets the full format of a mask.
 	 * 
 	 * @param mask
 	 *            the mask in any format.
 	 * 
 	 * @return the full format of the mask or <code>null</code> if invalid.
 	 */
 	public static final String getMask (String mask)
 	{
 		if ((mask == null) || ("".equals (mask)))
 			throw new IllegalArgumentException ("invalid mask");
 
 		String[] tokens;
 		String fullMask;
 
 		tokens = IRC.getMaskTokens (mask);
 		if (tokens == null)
 			return (null);
 
 		fullMask = IRC.getMaskFromTokens (tokens[0], tokens[1], tokens[2]);
 		if (fullMask == null)
 			return (null);
 
 		return (fullMask);
 	}
 
 	/**
 	 * Gets the full format of a mask from given nickname, username and
 	 * hostname. Any missing information is replaced by wildcard.
 	 * 
 	 * @param nick
 	 *            the nickname.
 	 * @param user
 	 *            the username.
 	 * @param host
 	 *            the hostname.
 	 * 
 	 * @return the mask or <code>null</code> if any argument is invalid.
 	 */
 	public static final String getMaskFromTokens (String nick, String user,
 			String host)
 	{
 		if ((nick == null) || ("".equals (nick)))
 			throw new IllegalArgumentException ("invalid nickname");
 
 		if ((user == null) || ("".equals (user)))
 			throw new IllegalArgumentException ("invalid username");
 
 		if ((host == null) || ("".equals (host)))
 			throw new IllegalArgumentException ("invalid hostname");
 
 		String mask;
 		String tmpNick = nick;
 		String tmpUser = user;
 		String tmpHost = host;
 
 		if (nick.charAt (0) == IRC.WILDCARD_MANY)
 			nick = "ab";
 
 		nick = nick.replaceAll ("\\*", "abc");
 		nick = nick.replaceAll ("\\?", "a");
 		user = user.replaceAll ("\\*", "abc");
 		user = user.replaceAll ("\\?", "a");
 		host = host.replaceAll ("\\*", "abc");
 		host = host.replaceAll ("\\?", "a");
 
 		if (!IRC.isNicknameValid (nick))
 			return (null);
 
 		if (!IRC.IRC_VALID_USER.matcher (user).matches ())
 			return (null);
 
 		if (!IRC.IRC_VALID_HOST.matcher (host).matches ())
 			return (null);
 
 		nick = tmpNick;
 		user = tmpUser;
 		host = tmpHost;
 
 		mask = nick + String.valueOf (IRC.PREFIX_USERNAME) + user
 				+ String.valueOf (IRC.PREFIX_HOSTNAME) + host;
 
 		return (mask);
 	}
 
 	/**
 	 * Returns the tokens of a mask.
 	 * <p>
 	 * <blockquote> The tokens are :
 	 * <ul>
 	 * <li>the nickname, at index 0.</li>
 	 * <li>the username, at index 1.</li>
 	 * <li>the hostname, at index 2.</li>.
 	 * </ul>
 	 * </blockquote>
 	 * 
 	 * @param mask
 	 *            the hostmask in any format to tokenize.
 	 * 
 	 * @return a <code>String</code> array which contains the tokens of the mask
 	 *         or <code>null</code> if invalid.
 	 */
 	public final static String[] getMaskTokens (String mask)
 	{
 		if ((mask == null) || ("".equals (mask)))
 			throw new IllegalArgumentException ("invalid mask");
 
 		Matcher mMessage = IRC.IRC_VALID_MASK.matcher (mask);
 
 		if (!mMessage.matches ())
 			return (null);
 
 		String[] tokens = new String[3];
 		String multiWildcard = String.valueOf (IRC.WILDCARD_MANY);
 
 		if ((mMessage.group (IRC.IRC_VALID_MASK_USER) == null)
 				&& (mMessage.group (IRC.IRC_VALID_MASK_HOST) == null))
 		{
 			tokens[0] = mask;
 			tokens[1] = multiWildcard;
 			tokens[2] = multiWildcard;
 		}
 		else if (mMessage.group (IRC.IRC_VALID_MASK_USER) == null)
 		{
 			tokens[0] = mMessage.group (IRC.IRC_VALID_MASK_NICK);
 			tokens[1] = multiWildcard;
 			tokens[2] = (mMessage.group (IRC.IRC_VALID_MASK_HOST) != null ? mMessage
 					.group (IRC.IRC_VALID_MASK_HOST)
 					: multiWildcard);
 		}
 		else
 		{
 			tokens[0] = (mMessage.group (IRC.IRC_VALID_MASK_NICK) != null ? mMessage
 					.group (IRC.IRC_VALID_MASK_NICK)
 					: multiWildcard);
 			tokens[1] = (mMessage.group (IRC.IRC_VALID_MASK_USER) != null ? mMessage
 					.group (IRC.IRC_VALID_MASK_USER)
 					: multiWildcard);
 			tokens[2] = (mMessage.group (IRC.IRC_VALID_MASK_HOST) != null ? mMessage
 					.group (IRC.IRC_VALID_MASK_HOST)
 					: multiWildcard);
 		}
 
 		return (tokens);
 	}
 
 	/**
 	 * Generates a channel safe identifier.
 	 * 
 	 * @return a channel safe ID.
 	 */
 	public final static String generateSafeChannelID ()
 	{
 		// [48;57] [0-9] 10
 		// [58;64] ----- 7
 		// [65;90] [A-Z] 24
 
 		// TODO: calculte ID from time
 		long time = System.currentTimeMillis ();
 
 		StringBuilder buffer = new StringBuilder ();
 		Random r = new Random ();
 		int range = 10 + 24;
 		int i;
 
 		for (int j = 0; j < IRC.CHANNELIDLEN; j++)
 		{
 			i = r.nextInt (range) - 1;
 
 			if (i < 10)
 				i += 48;
 			else
 				i += 65 - 10;
 
 			buffer.append ((char) i);
 		}
 
 		return (buffer.toString ());
 	}
 
 	/*
 	 * Constants
 	 */
 
 	// Limits
 	public static final int MAXCHANNELS = 4096;
 	public static final int MAXCHANNELJOINS = 21;
 	public static final int MAXCHANNELMODEPARAMS = 3;
 	public static final int MAXLIST = 42;
 	public static final int MSGMAXLEN = 512;
 	public static final int HOSTMAXLEN = 63;
 	public static final int NICKMAXLEN = 15;
 	public static final int USERNAMEMAXLEN = 15;
 	public static final int REALNAMEMAXLEN = 128;
 	public static final int MOTDMAXLEN = 80;
 	public static final int INFOMAXLEN = 80;
 	public static final int QUITMSGMAXLEN = 256;
 	public static final int AWAYMAXLEN = 64;
 	public static final int CHANNELMAXLEN = 50;
 	public static final int CHANNELIDLEN = 5;
 	public static final int TOPICMAXLEN = 4096;
 	public static final int KICKMAXLEN = 160;
 	public static final int KEYMAXLEN = 23;
 	public static final int TARGETMAXLEN = CHANNELMAXLEN;
 	public static final int PASSWORDMAXLEN = 52;
 	public static final int SERVERINFOMAXLEN = 64;
 	public static final int PINGMAXLEN = 64;
 	public static final int SERVERMSGMAXLEN = MSGMAXLEN + 48;
 	public static final int TEXTMAXLEN = MSGMAXLEN - TARGETMAXLEN - HOSTMAXLEN
 			- 13;
 
 	// Syntax
 	public static final String MESSAGE_EOL = "\r\n";
 	public static final char PREFIX_USERNAME = '!';
 	public static final char PREFIX_HOSTNAME = '@';
 	public static final char WILDCARD_ONE = '?';
 	public static final char WILDCARD_MANY = '*';
 	public static final String MASK_ANY = "*";
 	public static final String MASK_ALL = "0";
 	public static final char CHANNEL_LOCAL_PREFIX = '&';
 	public static final char CHANNEL_SAFE_PREFIX = '!';
 	public static final char CHANNEL_MODELESS_PREFIX = '+';
 	public static final char CHANNEL_STANDARD_PREFIX = '#';
 	public static final String OPERATOR_PREFIXES = "@%!";
 	public static final char OPERATOR_PREFIX = '@';
 	public static final String VOICE_PREFIXES = "+";
 	public static final char VOICE_PREFIX = '+';
 	public static final String ANONYMOUS_NICK = "anonymous";
 	public static final String ANONYMOUS_HOSTMASK = ANONYMOUS_NICK
 			+ IRC.PREFIX_USERNAME + ANONYMOUS_NICK + IRC.PREFIX_HOSTNAME
 			+ ANONYMOUS_NICK + ".";
 
 	// Command responses
 	public static final String RPL_WELCOME = "001";
 	public static final String RPL_YOURHOST = "002";
 	public static final String RPL_CREATED = "003";
 	public static final String RPL_MYINFO = "004";
 	public static final String RPL_ISUPPORT = "005";
 	public static final String RPL_TRACELINK = "200";
 	public static final String RPL_TRACECONNECTING = "201";
 	public static final String RPL_TRACEHANDSHAKE = "202";
 	public static final String RPL_TRACEUNKNOWN = "203";
 	public static final String RPL_TRACEOPERATOR = "204";
 	public static final String RPL_TRACEUSER = "205";
 	public static final String RPL_TRACESERVER = "206";
 	public static final String RPL_TRACESERVICE = "207";
 	public static final String RPL_TRACENEWTYPE = "208";
 	public static final String RPL_TRACECLASS = "209";
 	public static final String RPL_TRACERECONNECT = "210";
 	public static final String RPL_STATSLINKINFO = "211";
 	public static final String RPL_STATSCOMMANDS = "212";
 	public static final String RPL_ENDOFSTATS = "219";
 	public static final String RPL_UMODEIS = "221";
 	public static final String RPL_SERVLIST = "234";
 	public static final String RPL_SERVLISTEND = "235";
 	public static final String RPL_STATSUPTIME = "242";
 	public static final String RPL_STATSOLINE = "243";
 	public static final String RPL_LUSERCLIENT = "251";
 	public static final String RPL_LUSEROP = "252";
 	public static final String RPL_LUSERUNKNOWN = "253";
 	public static final String RPL_LUSERCHANNELS = "254";
 	public static final String RPL_LUSERME = "255";
 	public static final String RPL_ADMINME = "256";
 	public static final String RPL_ADMINLOC1 = "257";
 	public static final String RPL_ADMINLOC2 = "258";
 	public static final String RPL_ADMINEMAIL = "259";
 	public static final String RPL_TRYAGAIN = "263";
 	public static final String RPL_TRACELOG = "261";
 	public static final String RPL_TRACEEND = "262";
 	public static final String RPL_AWAY = "301";
 	public static final String RPL_USERHOST = "302";
 	public static final String RPL_ISON = "303";
 	public static final String RPL_UNAWAY = "305";
 	public static final String RPL_NOWAWAY = "306";
 	public static final String RPL_WHOISUSER = "311";
 	public static final String RPL_WHOISSERVER = "312";
 	public static final String RPL_WHOISOPERATOR = "313";
 	public static final String RPL_WHOWASUSER = "314";
 	public static final String RPL_ENDOFWHO = "315";
 	public static final String RPL_WHOISIDLE = "317";
 	public static final String RPL_ENDOFWHOIS = "318";
 	public static final String RPL_WHOISCHANNELS = "319";
 	public static final String RPL_LISTSTART = "321";
 	public static final String RPL_LIST = "322";
 	public static final String RPL_LISTEND = "323";
 	public static final String RPL_CHANNELMODEIS = "324";
 	public static final String RPL_UNIQOPIS = "325";
 	public static final String RPL_NOTOPIC = "331";
 	public static final String RPL_TOPIC = "332";
 	public static final String RPL_TOPICINFO = "333";
 	public static final String RPL_INVITING = "341";
 	public static final String RPL_SUMMONING = "342";
 	public static final String RPL_INVITELIST = "346";
 	public static final String RPL_ENDOFINVITELIST = "347";
 	public static final String RPL_EXCEPTLIST = "348";
 	public static final String RPL_ENDOFEXCEPTLIST = "349";
 	public static final String RPL_VERSION = "351";
 	public static final String RPL_WHOREPLY = "352";
 	public static final String RPL_NAMREPLY = "353";
 	public static final String RPL_LINKS = "364";
 	public static final String RPL_ENDOFLINKS = "365";
 	public static final String RPL_ENDOFNAMES = "366";
 	public static final String RPL_BANLIST = "367";
 	public static final String RPL_ENDOFBANLIST = "368";
 	public static final String RPL_ENDOFWHOWAS = "369";
 	public static final String RPL_INFO = "371";
 	public static final String RPL_MOTD = "372";
 	public static final String RPL_ENDOFINFO = "374";
 	public static final String RPL_MOTDSTART = "375";
 	public static final String RPL_ENDOFMOTD = "376";
 	public static final String RPL_YOUREOPER = "381";
 	public static final String RPL_REHASHING = "382";
 	public static final String RPL_YOURESERVICE = "383";
 	public static final String RPL_TIME = "391";
 	public static final String RPL_USERSSTART = "392";
 	public static final String RPL_USERS = "393";
 	public static final String RPL_ENDOFUSERS = "394";
 	public static final String RPL_NOUSERS = "395";
 	public static final String RPLMSG_WELCOME = "Welcome to the Internet Relay Network <nick>!<user>@<host>";
 	public static final String RPLMSG_YOURHOST = "Your host is <servername>, running version <version>";
 	public static final String RPLMSG_CREATED = "This server was created <date>";
 	public static final String RPLMSG_MYINFO = "<servername> <version> <available user modes> <available channel modes>";
 	public static final String RPLMSG_BOUNCE = "Try server <server name>, port <port>";
 	public static final String RPLMSG_TRACELINK = "Link <version & debug level> <destination> <next server> V<protocol version> <link uptime in seconds> <backstream sendq> <upstream sendq>";
 	public static final String RPLMSG_TRACECONNECTING = "Try. <class> <server>";
 	public static final String RPLMSG_TRACEHANDSHAKE = "H.S. <class> <server>";
 	public static final String RPLMSG_TRACEUNKNOWN = "???? <class> [<client IP address in dot form>]";
 	public static final String RPLMSG_TRACEOPERATOR = "Oper <class> <nick>";
 	public static final String RPLMSG_TRACEUSER = "User <class> <nick>";
 	public static final String RPLMSG_TRACESERVER = "Serv <class> <int>S <int>C <server> <nick!user|*!*>@<host|server> V<protocol version>";
 	public static final String RPLMSG_TRACESERVICE = "Service <class> <name> <type> <active type>";
 	public static final String RPLMSG_TRACENEWTYPE = "<newtype> 0 <client name>";
 	public static final String RPLMSG_TRACECLASS = "Class <class> <count>";
 	public static final String RPLMSG_TRACERECONNECT = "unused";
 	public static final String RPLMSG_STATSLINKINFO = "<linkname> <sendq> <sent messages> <sent Kbytes> <received messages> <received Kbytes> <time open>";
 	public static final String RPLMSG_STATSCOMMANDS = "<command> <count> <byte count> <remote count>";
 	public static final String RPLMSG_ENDOFSTATS = "<stats letter> :End of STATS report";
 	public static final String RPLMSG_UMODEIS = "<user mode string>";
 	public static final String RPLMSG_SERVLIST = "<name> <server> <mask> <type> <hopcount> <info>";
 	public static final String RPLMSG_SERVLISTEND = "<mask> <type> :End of service listing";
 	public static final String RPLMSG_STATSLLINE = "L <hostmask> * <servername> <maxdepth>";
 	public static final String RPLMSG_STATSUPTIME = ":Server Up %d days %d:%02d:%02d";
 	public static final String RPLMSG_STATSOLINE = "O <hostmask> * <name>";
 	public static final String RPLMSG_STATSHLINE = "H <hostmask> * <servername>";
 	public static final String RPLMSG_LUSERCLIENT = ":There are <integer> users and <integer> services on <integer> servers";
 	public static final String RPLMSG_LUSEROP = "<integer> :operator(s) online";
 	public static final String RPLMSG_LUSERUNKNOWN = "<integer> :unknown connection(s)";
 	public static final String RPLMSG_LUSERCHANNELS = "<integer> :channels formed";
 	public static final String RPLMSG_LUSERME = ":I have <integer> clients and <integer> servers";
 	public static final String RPLMSG_ADMINME = "Administrative info";
 	public static final String RPLMSG_TRYAGAIN = "<command> :Please wait a while and try again.";
 	public static final String RPLMSG_TRACELOG = "File <logfile> <debug level>";
 	public static final String RPLMSG_TRACEEND = "<server name> <version & debug level> :End of TRACE";
 	public static final String RPLMSG_NONE = "dummy";
 	public static final String RPLMSG_USERHOST = "<nick><*>=<+|-><hostname>";
 	public static final String RPLMSG_ISON = "<nick>";
 	public static final String RPLMSG_AWAY = "<<away message>";
 	public static final String RPLMSG_UNAWAY = "You are no longer marked as being away";
 	public static final String RPLMSG_NOWAWAY = "You have been marked as being away";
 	public static final String RPLMSG_WHOISUSER = "<nick> <user> <host> * :<real name>";
 	public static final String RPLMSG_WHOISSERVER = "<nick> <server> :<server info>";
 	public static final String RPLMSG_WHOISOPERATOR = "is an IRC operator";
 	public static final String RPLMSG_WHOISIDLE = "seconds idle";
 	public static final String RPLMSG_WHOISIDLESIGNON = "seconds idle, signon time";
 	public static final String RPLMSG_ENDOFWHOIS = "End of WHOIS list";
 	public static final String RPLMSG_WHOISCHANNELS = "<nick> :*{[@|+]<channel><space>}";
 	public static final String RPLMSG_WHOWASUSER = "<nick> <user> <host> * :<real name>";
 	public static final String RPLMSG_ENDOFWHO = "End of WHO list";
 	public static final String RPLMSG_LISTSTART = "unused";
 	public static final String RPLMSG_LIST = "<channel> <# visible> :<topic>";
 	public static final String RPLMSG_LISTEND = "End of LIST";
 	public static final String RPLMSG_CHANNELMODEIS = "<channel> <mode> <mode params>";
 	public static final String RPLMSG_UNIQOPIS = "<channel> <nickname>";
 	public static final String RPLMSG_NOTOPIC = "No topic is set";
 	public static final String RPLMSG_TOPICINFO = "<channel> <set by> <unixtime>";
 	public static final String RPLMSG_INVITING = "<channel> <nick>";
 	public static final String RPLMSG_SUMMONING = "Summoning user to IRC";
 	public static final String RPLMSG_INVITELIST = "<channel> <invitemask>";
 	public static final String RPLMSG_ENDOFINVITELIST = "End of channel invite list";
 	public static final String RPLMSG_EXCEPTLIST = "<channel> <exceptionmask>";
 	public static final String RPLMSG_ENDOFEXCEPTLIST = "End of channel exception list";
 	public static final String RPLMSG_VERSION = "<version>.<debuglevel> <server> :<comments>";
 	public static final String RPLMSG_WHOREPLY = "<channel> <user> <host> <server> <nick> <H|G>[*][@|+] :<hopcount> <real name>";
 	public static final String RPLMSG_NAMREPLY = "[=|*|@] <channel> :[[@|+]<nick>]";
 	public static final String RPLMSG_LINKS = "<mask> <server> :<hopcount> <server info>";
 	public static final String RPLMSG_ENDOFLINKS = "End of LINKS list";
 	public static final String RPLMSG_ENDOFNAMES = "End of NAMES list";
 	public static final String RPLMSG_BANLIST = "<channel> <banid>";
 	public static final String RPLMSG_ENDOFBANLIST = "End of channel ban list";
 	public static final String RPLMSG_ENDOFWHOWAS = "End of WHOWAS";
 	public static final String RPLMSG_ENDOFINFO = "End of INFO list";
 	public static final String RPLMSG_MOTDSTART = ":- <server> Message of the day - ";
 	public static final String RPLMSG_ENDOFMOTD = "End of /MOTD command";
 	public static final String RPLMSG_YOUREOPER = "You are now an IRC operator";
 	public static final String RPLMSG_REHASHING = "Rehashing";
 	public static final String RPLMSG_YOURESERVICE = "You are service <servicename>";
 	public static final String RPLMSG_USERSSTART = ":UserID   Terminal  Host";
 	public static final String RPLMSG_USERS = ":<username> <ttyline> <hostname>";
 	public static final String RPLMSG_ENDOFUSERS = "End of users";
 	public static final String RPLMSG_NOUSERS = "Nobody logged in";
 
 	// Error replies
 	public static final String ERR_NOSUCHNICK = "401";
 	public static final String ERR_NOSUCHSERVER = "402";
 	public static final String ERR_NOSUCHCHANNEL = "403";
 	public static final String ERR_CANNOTSENDTOCHAN = "404";
 	public static final String ERR_TOOMANYCHANNELS = "405";
 	public static final String ERR_WASNOSUCHNICK = "406";
 	public static final String ERR_TOOMANYTARGETS = "407";
 	public static final String ERR_NOSUCHSERVICE = "408";
 	public static final String ERR_NOORIGIN = "409";
 	public static final String ERR_NORECIPIENT = "411";
 	public static final String ERR_NOTEXTTOSEND = "412";
 	public static final String ERR_NOTOPLEVEL = "413";
 	public static final String ERR_WILDTOPLEVEL = "414";
 	public static final String ERR_BADMASK = "415";
 	public static final String ERR_UNKNOWNCOMMAND = "421";
 	public static final String ERR_NOMOTD = "422";
 	public static final String ERR_NOADMININFO = "423";
 	public static final String ERR_FILEERROR = "424";
 	public static final String ERR_NONICKNAMEGIVEN = "431";
 	public static final String ERR_ERRONEUSNICKNAME = "432";
 	public static final String ERR_NICKNAMEINUSE = "433";
 	public static final String ERR_NICKCOLLISION = "436";
 	public static final String ERR_UNAVAILRESOURCE = "437";
 	public static final String ERR_USERNOTINCHANNEL = "441";
 	public static final String ERR_NOTONCHANNEL = "442";
 	public static final String ERR_USERONCHANNEL = "443";
 	public static final String ERR_NOLOGIN = "444";
 	public static final String ERR_SUMMONDISABLED = "445";
 	public static final String ERR_USERSDISABLED = "446";
 	public static final String ERR_NOTREGISTERED = "451";
 	public static final String ERR_NEEDMOREPARAMS = "461";
 	public static final String ERR_ALREADYREGISTRED = "462";
 	public static final String ERR_NOPERMFORHOST = "463";
 	public static final String ERR_PASSWDMISMATCH = "464";
 	public static final String ERR_YOUREBANNEDCREEP = "465";
 	public static final String ERR_YOUWILLBEBANNED = "466";
 	public static final String ERR_KEYSET = "467";
 	public static final String ERR_CHANNELISFULL = "471";
 	public static final String ERR_UNKNOWNMODE = "472";
 	public static final String ERR_INVITEONLYCHAN = "473";
 	public static final String ERR_BANNEDFROMCHAN = "474";
 	public static final String ERR_BADCHANNELKEY = "475";
 	public static final String ERR_BADCHANMASK = "476";
 	public static final String ERR_NOCHANMODES = "477";
 	public static final String ERR_BANLISTFULL = "478";
 	public static final String ERR_NOPRIVILEGES = "481";
 	public static final String ERR_CHANOPRIVSNEEDED = "482";
 	public static final String ERR_CANTKILLSERVER = "483";
 	public static final String ERR_RESTRICTED = "484";
 	public static final String ERR_UNIQOPPRIVSNEEDED = "485";
 	public static final String ERR_NOOPERHOST = "491";
 	public static final String ERR_UMODEUNKNOWNFLAG = "501";
 	public static final String ERR_USERSDONTMATCH = "502";
 	public static final String ERRMSG_NOSUCHNICK = "No such nickname";
 	public static final String ERRMSG_NOSUCHSERVER = "No such server";
 	public static final String ERRMSG_NOSUCHCHANNEL = "No such channel";
 	public static final String ERRMSG_CANNOTSENDTOCHAN = "Cannot send to channel";
 	public static final String ERRMSG_TOOMANYCHANNELS = "You have joined too many channels";
 	public static final String ERRMSG_WASNOSUCHNICK = "There was no such nickname";
 	public static final String ERRMSG_TOOMANYTARGETS = "<error code> recipients. <abort message>";
 	public static final String ERRMSG_NOSUCHSERVICE = "No such service";
 	public static final String ERRMSG_NOORIGIN = "No origin specified";
 	public static final String ERRMSG_NORECIPIENT = "No recipient given";
 	public static final String ERRMSG_NOTEXTTOSEND = "No text to send";
 	public static final String ERRMSG_NOTOPLEVEL = "No toplevel domain specified";
 	public static final String ERRMSG_WILDTOPLEVEL = "Wildcard in toplevel domain";
 	public static final String ERRMSG_BADMASK = "Bad Server/host mask";
 	public static final String ERRMSG_UNKNOWNCOMMAND = "Unknown command";
 	public static final String ERRMSG_NOMOTD = "MOTD File is missing";
 	public static final String ERRMSG_NOADMININFO = "No administrative info available";
 	public static final String ERRMSG_FILEERROR = "File error doing <file op> on <file>";
 	public static final String ERRMSG_FILEERROR_OPCREATE = "creating";
 	public static final String ERRMSG_FILEERROR_OPREAD = "reading";
 	public static final String ERRMSG_FILEERROR_OPWRITE = "writing";
 	public static final String ERRMSG_NONICKNAMEGIVEN = "No nickname given";
 	public static final String ERRMSG_ERRONEUSNICKNAME = "Erroneus nickname";
 	public static final String ERRMSG_NICKNAMEINUSE = "Nickname is already in use";
 	public static final String ERRMSG_NICKCOLLISION = "Nickname collision KILL from <user>@<host>";
 	public static final String ERRMSG_UNAVAILRESOURCE = "Nick/channel is temporarily unavailable";
 	public static final String ERRMSG_USERNOTINCHANNEL = "They aren't on that channel";
 	public static final String ERRMSG_NOTONCHANNEL = "You're not on that channel";
 	public static final String ERRMSG_USERONCHANNEL = "is already on channel";
 	public static final String ERRMSG_NOLOGIN = "User not logged in";
 	public static final String ERRMSG_SUMMONDISABLED = "SUMMON has been disabled";
 	public static final String ERRMSG_USERSDISABLED = "USERS has been disabled";
 	public static final String ERRMSG_NOTREGISTERED = "You have not registered";
 	public static final String ERRMSG_NEEDMOREPARAMS = "Not enough parameters";
 	public static final String ERRMSG_ALREADYREGISTRED = "Unauthorized command (already registered)";
 	public static final String ERRMSG_NOPERMFORHOST = "Your host isn't among the privileged";
 	public static final String ERRMSG_PASSWDMISMATCH = "Password incorrect";
 	public static final String ERRMSG_YOUREBANNEDCREEP = "You are banned from this server";
 	public static final String ERRMSG_KEYSET = "Channel key already set";
 	public static final String ERRMSG_CHANNELISFULL = "Cannot join channel (+l)";
 	public static final String ERRMSG_UNKNOWNMODE = "is unknown mode char to me";
 	public static final String ERRMSG_INVITEONLYCHAN = "Cannot join channel (+i)";
 	public static final String ERRMSG_BANNEDFROMCHAN = "Cannot join channel (+b)";
 	public static final String ERRMSG_BADCHANNELKEY = "Cannot join channel (+k)";
 	public static final String ERRMSG_BADCHANMASK = "Bad Channel Mask";
 	public static final String ERRMSG_NOCHANMODES = "Channel doesn't support modes";
 	public static final String ERRMSG_BANLISTFULL = "Channel list is full";
 	public static final String ERRMSG_NOPRIVILEGES = "Permission Denied- You're not an IRC operator";
 	public static final String ERRMSG_CHANOPRIVSNEEDED = "You're not channel operator";
 	public static final String ERRMSG_CANTKILLSERVER = "You cant kill a server!";
 	public static final String ERRMSG_RESTRICTED = "Your connection is restricted!";
 	public static final String ERRMSG_UNIQOPPRIVSNEEDED = "You're not the original channel operator";
 	public static final String ERRMSG_NOOPERHOST = "No O-lines for your host";
 	public static final String ERRMSG_UMODEUNKNOWNFLAG = "Unknown MODE flag";
 	public static final String ERRMSG_USERSDONTMATCH = "Cannot change mode for other users";
 
 	public static final String ERRMSG_READERROR = "Read error to user: EOF from client";
 	public static final String ERRMSG_WRITEERROR = "Write error to user: closing link";
 	public static final String ERRMSG_PINGTIMEOUT = "Ping timeout";
 	public static final String ERRMSG_CLOSINGLINK = "Closing Link: <ip> (<reason>)";
 
 	// Reserved numerics
 	public static final int RPL_STATSCLINE = 213;
 	public static final int RPL_STATSNLINE = 214;
 	public static final int RPL_STATSILINE = 215;
 	public static final int RPL_STATSKLINE = 216;
 	public static final int RPL_STATSQLINE = 217;
 	public static final int RPL_STATSYLINE = 218;
 	public static final int RPL_SERVICEINFO = 231;
 	public static final int RPL_ENDOFSERVICES = 232;
 	public static final int RPL_SERVICE = 233;
 	public static final int RPL_STATSVLINE = 240;
 	public static final int RPL_STATSLLINE = 241;
 	public static final int RPL_STATSHLINE = 244;
 	public static final int RPL_STATSSLINE = 245;
 	public static final int RPL_STATSPING = 246;
 	public static final int RPL_STATSBLINE = 247;
 	public static final int RPL_STATSDLINE = 250;
 	public static final int RPL_NONE = 300;
 	public static final int RPL_WHOISCHANOP = 316;
 	public static final int RPL_KILLDONE = 361;
 	public static final int RPL_CLOSING = 362;
 	public static final int RPL_CLOSEEND = 363;
 	public static final int RPL_INFOSTART = 373;
 	public static final int RPL_MYPORTIS = 384;
 	public static final int ERR_NOSERVICEHOST = 492;
 	public static final String RPLMSG_STATSCLINE = "C <host> * <name> <port> <class>";
 	public static final String RPLMSG_STATSNLINE = "N <host> * <name> <port> <class>";
 	public static final String RPLMSG_STATSILINE = "I <host> * <host> <port> <class>";
 	public static final String RPLMSG_STATSKLINE = "K <host> * <username> <port> <class>";
 	public static final String RPLMSG_STATSYLINE = "Y <class> <ping frequency> <connect frequency> <max sendq>";
 
 	/*
 	 * Patterns
 	 */
 
 	/**
 	 * Pattern for splitting a buffer by message lines.
 	 */
 	private static final String BUFFER_MESSAGE_REGEX = "\r\n|\n|\r";
 	public static final Pattern BUFFER_MESSAGE = Pattern
 			.compile (IRC.BUFFER_MESSAGE_REGEX);
 
 	/**
 	 * Pattern for splitting a buffer by whitespace characters.
 	 */
 	private static final String BUFFER_TOKEN_REGEX = "\\s";
 	public static final Pattern BUFFER_TOKEN = Pattern
 			.compile (IRC.BUFFER_TOKEN_REGEX);
 
 	/**
 	 * Pattern for finding whitespace presence in a buffer.
 	 */
 	private static final String BUFFER_SPACE_REGEX = "(.*\\s.*)++";
 	public static final Pattern BUFFER_SPACE = Pattern
 			.compile (IRC.BUFFER_SPACE_REGEX);
 
 	/**
 	 * Pattern for tokenizing a list of arguments separated by a comma.
 	 */
 	private static final String BUFFER_COMMA_LIST_REGEX = ",";
 	public static final Pattern BUFFER_COMMA_LIST = Pattern
 			.compile (IRC.BUFFER_COMMA_LIST_REGEX);
 
 	/**
 	 * Pattern for matching a IRC message.
 	 * 
 	 * <ul>
 	 * <li>group 1 = prefix without ':' character
 	 * <li>group 2 = command (message or numeric)
 	 * <li>group 3 = params (may be null)
 	 * </ul>
 	 */
 	private static final String IRC_MESSAGE_REGEX = "(\\:(\\S*) )?(\\S*)( (.*))?";
 	public static final int IRC_MESSAGE_PREFIX = 2;
 	public static final int IRC_MESSAGE_COMMAND = 3;
 	public static final int IRC_MESSAGE_PARAMS = 5;
 	public static final Pattern IRC_MESSAGE = Pattern
 			.compile (IRC.IRC_MESSAGE_REGEX);
 
 	/**
 	 * Pattern for tokenizing a IRC message omitting character ':'.
 	 */
 	private static final String IRC_TOKEN_REGEX = "\\s\\:?";
 	public static final Pattern IRC_TOKEN = Pattern
 			.compile (IRC.IRC_TOKEN_REGEX);
 
 	/**
 	 * Pattern for matching a prefix from a IRC message.
 	 * 
 	 * <ul>
 	 * <li>group 1 = hostmask (\<nickname\>!\<user\>@\<host\>), only for client
 	 * message
 	 * <li>group 2 = nickname, only for client message
 	 * <li>group 3 = user, only for client message
 	 * <li>group 4 = host, only for client message
 	 * <li>group 5 = servername, only for server message
 	 * </ul>
 	 */
 	private static final String IRC_PREFIX_REGEX = "((.*)!(.*)@(.*))|(.*)";
 	public static final int IRC_PREFIX_CLIENT_HOSTMASK = 1;
 	public static final int IRC_PREFIX_CLIENT_NICK = 2;
 	public static final int IRC_PREFIX_CLIENT_USER = 3;
 	public static final int IRC_PREFIX_CLIENT_HOST = 4;
 	public static final int IRC_PREFIX_SERVER_SERVERNAME = 5;
 	public static final Pattern IRC_PREFIX = Pattern
 			.compile (IRC.IRC_PREFIX_REGEX);
 
 	/**
 	 * Pattern for matching a command from a IRC message.
 	 * 
 	 * <ul>
 	 * <li>group 1 = message
 	 * <li>group 2 = numeric
 	 * </ul>
 	 */
 	private static final String IRC_COMMAND_REGEX = "(\\S*)|(\\d{3})";
 	public static final int IRC_COMMAND_MESSAGE = 1;
 	public static final int IRC_COMMAND_NUMERIC = 2;
 	public static final Pattern IRC_COMMAND = Pattern
 			.compile (IRC.IRC_COMMAND_REGEX);
 
 	/**
 	 * Pattern for checking validity of a parameter from a IRC message.
 	 */
 	private static final String IRC_VALID_PARAM_REGEX = "[^\\u000A\\u000D\\u0010-\\u0020\\u0040\\:]*";
 	public static final Pattern IRC_VALID_PARAM = Pattern
 			.compile (IRC.IRC_VALID_PARAM_REGEX);
 
 	/**
 	 * Pattern for checking validity of a target.
 	 */
 	private static final String IRC_VALID_TARGET_REGEX = IRC.IRC_VALID_SERVERNAME_REGEX
 			+ IRC.IRC_VALID_NICKNAME_REGEX;
 	public static final Pattern IRC_VALID_TARGET = Pattern
 			.compile (IRC.IRC_VALID_TARGET_REGEX);
 
 	/**
 	 * Pattern for checking validity of a message target.
 	 */
 	private static final String IRC_VALID_MSGTARGET_REGEX = IRC.IRC_VALID_MSGTO_REGEX
 			+ "(," + IRC.IRC_VALID_MSGTO_REGEX + ")*";
 	public static final Pattern IRC_VALID_MSGTARGET = Pattern
 			.compile (IRC_VALID_MSGTARGET_REGEX);
 
 	/**
 	 * Pattern for checking validity of a message to.
 	 */
 	private static final String IRC_VALID_MSGTO_REGEX = IRC.IRC_VALID_CHANNEL_REGEX
 			+ "|"
 			+ IRC.IRC_VALID_USER_REGEX
 			+ "(?:%"
 			+ IRC.IRC_VALID_HOST_REGEX
 			+ ")?@"
 			+ IRC.IRC_VALID_SERVERNAME_REGEX
 			+ "|"
 			+ IRC.IRC_VALID_USER_REGEX
 			+ "%"
 			+ IRC.IRC_VALID_HOST_REGEX
 			+ "|"
 			+ IRC.IRC_VALID_TARGETMASK_REGEX
 			+ "|"
 			+ IRC.IRC_VALID_NICKNAME_REGEX
 			+ "|"
 			+ IRC.IRC_VALID_NICKNAME_REGEX
 			+ "!" + IRC.IRC_VALID_USER_REGEX + "@" + IRC.IRC_VALID_HOST_REGEX;
 	public static final Pattern IRC_VALID_MSGTO = Pattern
 			.compile (IRC_VALID_MSGTO_REGEX);
 
 	/**
 	 * Pattern for checking validity of a channel.
 	 */
 	private static final String IRC_VALID_CHANNEL_REGEX = "(#|\\+|!("
 			+ IRC.IRC_VALID_CHANNELID_REGEX + ")?|&)("
 			+ IRC.IRC_VALID_CHANSTRING_REGEX + ")(\\u003A"
 			+ IRC.IRC_VALID_CHANSTRING_REGEX + ")?";
 	public static final int IRC_VALID_CHANNEL_PREFIX = 1;
 	public static final int IRC_VALID_CHANNEL_ID = 2;
 	public static final int IRC_VALID_CHANNEL_STRING = 3;
 	public static final int IRC_VALID_CHANNEL_TOPIC = 4;
 	public static final Pattern IRC_VALID_CHANNEL = Pattern
 			.compile (IRC_VALID_CHANNEL_REGEX);
 
 	/**
 	 * Pattern for checking validity of a servername.
 	 */
 	private static final String IRC_VALID_SERVERNAME_REGEX = IRC.IRC_VALID_HOSTNAME_REGEX;
 	public static final Pattern IRC_VALID_SERVERNAME = Pattern
 			.compile (IRC.IRC_VALID_SERVERNAME_REGEX);
 
 	/**
 	 * Pattern for checking validity of a host.
 	 */
 	private static final String IRC_VALID_HOST_REGEX = IRC.IRC_VALID_HOSTNAME_REGEX
 			+ "|" + IRC.IRC_VALID_HOSTADDR_REGEX;
 	public static final Pattern IRC_VALID_HOST = Pattern
 			.compile (IRC.IRC_VALID_HOST_REGEX);
 
 	/**
 	 * Pattern for checking validity of a hostname.
 	 */
 	private static final String IRC_VALID_HOSTNAME_REGEX = IRC.IRC_VALID_SHORTNAME_REGEX
 			+ "[\\." + IRC.IRC_VALID_SHORTNAME_REGEX + "]*";
 	public static final Pattern IRC_VALID_HOSTNAME = Pattern
 			.compile (IRC.IRC_VALID_HOSTNAME_REGEX);
 
 	/**
 	 * Pattern for checking validity of a shortname.
 	 */
 	private static final String IRC_VALID_SHORTNAME_REGEX = "[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]";
 	public static final Pattern IRC_VALID_SHORTNAME = Pattern
 			.compile (IRC.IRC_VALID_SHORTNAME_REGEX);
 
 	/**
 	 * Pattern for checking validity of a host address.
 	 */
 	private static final String IRC_VALID_HOSTADDR_REGEX = IRC.IRC_VALID_IP4ADDR_REGEX
 			+ "|" + IRC.IRC_VALID_IP6ADDR_REGEX;
 	public static final Pattern IRC_VALID_HOSTADDR = Pattern
 			.compile (IRC.IRC_VALID_HOSTADDR_REGEX);
 
 	/**
 	 * Pattern for checking validity of a IP address version 4.
 	 */
 	private static final String IRC_VALID_IP4ADDR_REGEX = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
 	public static final Pattern IRC_VALID_IP4ADDR = Pattern
 			.compile (IRC.IRC_VALID_IP4ADDR_REGEX);
 
 	/**
 	 * Pattern for checking validity of a IP address version 6.
 	 */
 	private static final String IRC_VALID_IP6ADDR_REGEX = "[0-9A-F](?:\\u003A[0-9A-F]){7}?"
 			+ "|"
 			+ "(?:0\\:0\\:0\\:0\\:0\\:(?:0|FFFF)\\:"
 			+ IRC.IRC_VALID_IP4ADDR_REGEX + ")";
 	public static final Pattern IRC_VALID_IP6ADDR = Pattern
 			.compile (IRC.IRC_VALID_IP6ADDR_REGEX);
 
 	/**
 	 * Pattern for checking validity of a target mask.
 	 */
 	private static final String IRC_VALID_TARGETMASK_REGEX = "[$#]";
 	public static final Pattern IRC_VALID_TARGETMASK = Pattern
 			.compile (IRC.IRC_VALID_TARGETMASK_REGEX);
 
 	/**
 	 * Pattern for checking validity of a nickname.
 	 */
 	// ( letter / special ) *8( letter / digit / special / "-" )
 	// special = %x5B-60 / %x7B-7D
 	// ; "[", "]", "\", "`", "_", "^", "{", "|", "}"
 	private static final String IRC_VALID_NICKNAME_REGEX = "[A-Za-z\\u005B-\\u0060\\u007B-\\u007D;\\[\\]`_^\\{\\|\\}]{1}[A-Za-z0-9\\u005B-\\u0060\\u007B-\\u007D;\\[\\]`_^\\{\\|\\}-]{0,}";
 	public static final Pattern IRC_VALID_NICKNAME = Pattern
 			.compile (IRC.IRC_VALID_NICKNAME_REGEX);
 
 	/**
 	 * Pattern for checking validity of a channel string.
 	 */
 	private static final String IRC_VALID_CHANSTRING_REGEX = "[^\\u0000\\u0007\\u000A\\u000D\\u0020\\u002C\\u003A]*";
 	public static final Pattern IRC_VALID_CHANSTRING = Pattern
 			.compile (IRC.IRC_VALID_CHANSTRING_REGEX);
 
 	/**
 	 * Pattern for checking validity of a channel ID.
 	 */
 	private static final String IRC_VALID_CHANNELID_REGEX = "[A-Za-z0-9]{"
 			+ IRC.CHANNELIDLEN + "}";
 	public static final Pattern IRC_VALID_CHANNELID = Pattern
 			.compile (IRC.IRC_VALID_CHANNELID_REGEX);
 
 	/**
 	 * Pattern for checking validity of a user.
 	 */
 	private static final String IRC_VALID_USER_REGEX = "[^\\u0000\\u000A\\u000D\\u0020\\u0040]{1,}";
 	public static final Pattern IRC_VALID_USER = Pattern
 			.compile (IRC.IRC_VALID_USER_REGEX);
 
 	/**
 	 * Pattern for checking validity of a key
 	 */
 	private static final String IRC_VALID_KEY_REGEX = "[^\\u0000\\u0006\\u0009-\\u000B\\u000D\\u0020]{1,}";
 	public static final Pattern IRC_VALID_KEY = Pattern
 			.compile (IRC.IRC_VALID_KEY_REGEX);
 
 	/**
 	 * Pattern for checking a mask.
 	 */
 	private static final String IRC_VALID_MASK_REGEX = "([^@!]*)(!([^@]*))?(@(.*))?";
 
 	public static final int IRC_VALID_MASK_NICK = 1;
 	public static final int IRC_VALID_MASK_USER = 3;
 	public static final int IRC_VALID_MASK_HOST = 5;
 	public static final Pattern IRC_VALID_MASK = Pattern
 			.compile (IRC.IRC_VALID_MASK_REGEX);
 
 }
