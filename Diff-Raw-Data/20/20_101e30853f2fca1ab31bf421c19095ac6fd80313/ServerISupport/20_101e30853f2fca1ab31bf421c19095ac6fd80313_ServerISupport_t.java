 /*
    Copyright 2011 James Cowgill
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package uk.org.cowgill.james.jircd;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 import uk.org.cowgill.james.jircd.util.ModeType;
 
 /**
  * Contains a list of server limits and options sent in ISUPPORT messages
  * 
  * <p>Also contains the string validation routines
  * 
  * @author James
  */
 public class ServerISupport
 {
 	/**
 	 * Avaliable user modes
 	 * 
 	 * <p>You are allowed to add ONOFF modes to this (do not add modes requiring parameters)
 	 * <p>When adding modes, all existing users will not have that mode set
 	 * 
 	 * @see updateISupport
 	 */
 	public final Map<Character, ModeType> modesUser;
 	
 	/**
 	 * Avaliable chanel modes
 	 * 
 	 * <p>You are allowed to add ONOFF modes to this (do not add modes requiring parameters)
 	 * <p>When adding modes, all existing channels will not have that mode set
 	 * 
 	 * @see updateISupport
 	 */
 	public final Map<Character, ModeType> modesChannel;
 	
 	/**
 	 * Map of isupport messages (key=value)
 	 * 
 	 * <p>Mode related messages are not here, they are automatically
 	 * generated from the modesUser and modesChannel fields
 	 * 
 	 * @see updateISupport
 	 */
 	public final Map<String, String> iSupportMsgs;
 	
 	/**
 	 * Maximum number of entries in lists (ban, exception, invite exception)
 	 */
 	public final static int MAXLIST = 60;
 	
 	/**
 	 * Maximum number of modes which can be changed per message
 	 */
 	public final static int MODES = 12;
 	
 	/**
 	 * Maxmimum number of channels you can join
 	 */
 	public final static int MAXCHANNELS = 10;
 	
 	/**
 	 * Maximum length of away message
 	 */
 	public final static int AWAYLEN = 300;
 	
 	/**
 	 * Maximum length of nickname
 	 */
 	public final static int NICKLEN = 30;
 	
 	/**
 	 * Maximum length of username
 	 */
 	public final static int USERLEN = 30;
 	
 	/**
 	 * Maxmimum topic length
 	 */
 	public final static int TOPICLEN = 300;
 	
 	/**
 	 * Maxmimum length of kick message
 	 */
 	public final static int KICKLEN = 300;
 	
 	/**
 	 * Maxmimum channel name length
 	 */
 	public final static int CHANNELLEN = 30;
 
 	//Caching related fields
 	private String iSupportCache004User = null;
 	private String iSupportCache004Channel = null;
 	private List<String> iSupportCache005 = null;
 	
 	private final static int ISUPPORTLEN = 300;
 	
 	/**
 	 * Creates a new ServerISupport object with the default modes and ISupport messages
 	 */
 	ServerISupport()
 	{
 		//Setup built-in user modes
 		modesUser = new HashMap<Character, ModeType>();
 		modesUser.put('o', ModeType.OnOff);
 		modesUser.put('O', ModeType.OnOff);
 		modesUser.put('i', ModeType.OnOff);
 		modesUser.put('B', ModeType.OnOff);
 		modesUser.put('z', ModeType.OnOff);
 		
 		//Setup built-in channel modes
 		modesChannel = new HashMap<Character, ModeType>();
 		modesChannel.put('q', ModeType.MemberList);
 		modesChannel.put('a', ModeType.MemberList);
 		modesChannel.put('o', ModeType.MemberList);
 		modesChannel.put('h', ModeType.MemberList);
 		modesChannel.put('v', ModeType.MemberList);
 		modesChannel.put('b', ModeType.List);
 		modesChannel.put('e', ModeType.List);
 		modesChannel.put('I', ModeType.List);
 		modesChannel.put('k', ModeType.Param);
		modesChannel.put('l', ModeType.Param);
 		modesChannel.put('p', ModeType.OnOff);
 		modesChannel.put('s', ModeType.OnOff);
 		modesChannel.put('t', ModeType.OnOff);
 		modesChannel.put('n', ModeType.OnOff);
 		modesChannel.put('m', ModeType.OnOff);
 		modesChannel.put('i', ModeType.OnOff);
 		modesChannel.put('O', ModeType.OnOff);
 		modesChannel.put('z', ModeType.OnOff);
 		
 		//Setup built-in isupport msgs (CHANMODES and MAXLIST handled later)
 		iSupportMsgs = new HashMap<String, String>();
 		iSupportMsgs.put("AWAYLEN", Integer.toString(AWAYLEN));
 		iSupportMsgs.put("MODES", Integer.toString(MODES));
 		iSupportMsgs.put("CHANLIMIT", "#:" + Integer.toString(MAXCHANNELS));
 		iSupportMsgs.put("NICKLEN", Integer.toString(NICKLEN));
 		iSupportMsgs.put("TOPICLEN", Integer.toString(TOPICLEN));
 		iSupportMsgs.put("KICKLEN", Integer.toString(KICKLEN));
 		iSupportMsgs.put("CHANNELLEN", Integer.toString(CHANNELLEN));
 		
 		iSupportMsgs.put("PREFIX", "(qaohv)~&@%+");
 		iSupportMsgs.put("CHANTYPES", "#");
 		iSupportMsgs.put("CASEMAPPING", "ascii");
 		iSupportMsgs.put("FNC", "");
 		iSupportMsgs.put("EXCEPTS", "");
 		iSupportMsgs.put("INVEX", "");
 		iSupportMsgs.put("NAMESX", "");
 		iSupportMsgs.put("UHNAMES", "");
 	}
 	
 	/**
 	 * Invalidates the ISupport string cache
 	 * 
 	 * <p>Must be called to update ISupport messages after the server has started
 	 */
 	public void updateISupport()
 	{
 		iSupportCache004User = null;
 		iSupportCache004Channel = null;
 		iSupportCache005 = null;
 	}
 	
 	/**
 	 * Sends the isupport messages (004 and 005)
 	 * 
 	 * @param client client to send to
 	 */
 	public void sendISupportMsgs(Client client)
 	{
 		//Check caches
 		if(iSupportCache004User == null)
 		{
 			//Create mode caches
 			StringBuilder builder = new StringBuilder();
 			
 			for(Character c : modesChannel.keySet())
 			{
 				builder.append(c);
 			}
 			
 			iSupportCache004Channel = builder.toString();
 			
 			//User modes
 			builder = new StringBuilder();
 			
 			for(Character c : modesUser.keySet())
 			{
 				builder.append(c);
 			}
 			
 			iSupportCache004User = builder.toString();
 		}
 		
 		if(iSupportCache005 == null)
 		{
 			//Create CHANMODES cache
 			StringBuilder builderA = new StringBuilder();
 			StringBuilder builderC = new StringBuilder();
 			StringBuilder builderD = new StringBuilder();
 			
 			for(Entry<Character, ModeType> c : modesChannel.entrySet())
 			{
 				switch(c.getValue())
 				{
 				case OnOff:
 					builderD.append(c.getKey());
 					break;
 					
 				case Param:
 					if(c.getKey() != 'k')
 					{
 						builderC.append(c.getKey());
 					}
 					break;
 					
 				case List:
 					builderA.append(c.getKey());
 					break;
 					
 				default:
 					//This shuts the compiler up
 					break;
 				}
 			}
 			
 			//Generate isupport messages
 			StringBuilder iBuilder = new StringBuilder("CHANMODES=");
 			iBuilder.append(builderA);
 			iBuilder.append(",k,");
 			iBuilder.append(builderC);
 			iBuilder.append(',');
 			iBuilder.append(builderD);
 			iBuilder.append(" MAXLIST=");
 			iBuilder.append(builderA);
 			iBuilder.append(':');
 			iBuilder.append(Integer.toString(MAXLIST));
 			
 			//Add messages
 			int lenBefore;
 			iSupportCache005 = new ArrayList<String>();
 			
 			for(Entry<String, String> entry : iSupportMsgs.entrySet())
 			{	
 				iBuilder.append(' ');
 				lenBefore = iBuilder.length();
 				
 				iBuilder.append(entry.getKey());
 				if(!entry.getValue().isEmpty())
 				{
 					iBuilder.append('=');
 					iBuilder.append(entry.getValue());
 				}
 				
 				//Check max characters
 				if(iBuilder.length() > ISUPPORTLEN)
 				{
 					//Rollback this change
 					iBuilder.setLength(lenBefore);
 					
 					//Add ending
 					iBuilder.append(":are supported by this server");
 					
 					//Add to list
 					iSupportCache005.add(iBuilder.toString());
 					iBuilder.setLength(0);
 				}
 			}
 			
 			//Add last message
 			if(iBuilder.length() != 0)
 			{
 				//Add ending
 				iBuilder.append(":are supported by this server");
 				
 				//Add to list
 				iSupportCache005.add(iBuilder.toString());
 			}
 		}
 		
 		//Send 004
 		Message msg004 = client.newNickMessage("004");
 		msg004.appendParam(Server.getServer().getConfig().serverName);
 		msg004.appendParam(Server.VERSION);
 		msg004.appendParam(iSupportCache004User);
 		msg004.appendParam(iSupportCache004Channel);
 		client.send(msg004);
 		
 		//Send 005
 		String startMsg = client.newNickMessage("005").toString() + " ";
 		for(String entry : iSupportCache005)
 		{
 			client.send(startMsg + entry);
 		}
 	}
 	
 	//############################
 	// Validation Checks
 	//############################
 	
 	private final static Pattern nameValidate = Pattern.compile("[a-zA-z]([a-zA-Z0-9\\[\\]\\\\\\Q`^{}-\\|\\E])*");
 	
 	/**
 	 * Checks whether a nickname is allowed
 	 * 
 	 * @param nick nickname to check
 	 * @return whether it is allowed
 	 */
 	public static boolean validateNick(String nick)
 	{
 		return nick.length() <= NICKLEN && nameValidate.matcher(nick).matches();			
 	}
 	
 	/**
 	 * Checks whether a username is allowed
 	 * 
 	 * @param user username to check
 	 * @return whether it is allowed
 	 */
 	public static boolean validateUser(String user)
 	{
 		return user.length() <= USERLEN && nameValidate.matcher(user).matches();			
 	}
 	
 	/**
 	 * Checks whether a channel is allowed
 	 * 
 	 * @param user channel to check
 	 * @return whether it is allowed
 	 */
 	public static boolean validateChannel(String channel)
 	{
 		return channel.length() <= CHANNELLEN && nameValidate.matcher(channel).matches();			
 	}
 }
