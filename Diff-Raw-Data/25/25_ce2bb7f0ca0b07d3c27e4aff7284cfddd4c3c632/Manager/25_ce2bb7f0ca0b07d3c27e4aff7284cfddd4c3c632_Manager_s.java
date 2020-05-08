 package wIRC;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Scanner;
 import java.util.TreeMap;
 
import javaext.SortedListModel;
 
 import wIRC.Message.Code;
 import wIRC.Message.TextColor;
 import wIRC.interfaces.MessageParser;
 import wIRC.interfaces.Plugin;
 import wIRC.interfaces.UserInput;
 
 /**
  * Chat structural-object
  * <br><br>
  * This class manages the logical operations which happen between 
  * users IO and remote IO. It also serves as the medium by which 
  * both are allowed to communicate through, ideally. Though there 
  * are currently some hacks in place.
  * <br><br>
  * @author 	see http://code.google.com/p/wirc/wiki/AUTHORS
  */
 public class Manager 
 {
 	public static final String NULL_CHAR = String.valueOf(0);
 	public static final String CTCP_CHAR = String.valueOf(1);
 	public static final String PSLASH = File.separator;
 	
 	private IRCSocket s;
 	
 	protected File profile;
 	
 	protected DateFormat time = new java.text.SimpleDateFormat("HH:mm:ss");
 	
 	protected File homePath = new File(System.getProperty("user.home") + File.separator + ".wIRC");
 	protected File appPath = new File(System.getProperty("user.dir"));
 	
 	protected String nickName = "Nullname" + (int)(Math.random() * 9000 + 999);
 	protected String realName = "Anonymous";
 	protected String userInfo = "No info set.";
 	protected String hostName = "chat.freenode.net";
 
 	protected String[] struct = {"profiles", "scripts", "plugins"};
 	
 	protected TreeMap<String, User> users = new TreeMap<String, User>();
 	protected ArrayList<Plugin> plugins = new ArrayList<Plugin>();
 	protected UserInput window = new DefaultGUI(hostName, this);
 	
 	private boolean debug;
 	
 	private MessageHandler mesgHandler;
 	
 	{	staticInit();	}
 	private void staticInit()
 	{
 		MessageParser unknown = new MessageParser()
 		{
 			@Override
 			public void parseMessage(Message mesg)
 			{
 				window.println("(" + mesg.getCode() + ") " + mesg.getMessage(), mesg.getChannel(), TextColor.GRAY);
 			}
 		};
 		
 		/* * * * * * * * * * * *
 		 *   Message Parsers   *
 		 * * * * * * * * * * * */
 		
 		// notice, ping, join
 
 		MessageParser notice = new MessageParser()
 		{
 			@Override
 			public void parseMessage(Message mesg)
 			{
 				window.println("(NOTICE) " + mesg.getMessage(), mesg.getChannel(), TextColor.ORANGE);
 			}
 		};
 		
 		// more declarations here
 		
 		// add parsers
 
 		this.mesgHandler = new MessageHandler(unknown);
 		this.mesgHandler.addParser(Code.NOTICE, notice);
 	}
 	
 	public Manager(IRCSocket s)
 	{
 		this.s = s;
 		
 		if (!homePath.isDirectory())
 		{
 			if (homePath.mkdir() == false)
 				printDebugMsg("Unable to create user folder.");
 			else
 				printDebugMsg("User folder created.");
 		}
 		else
 		{
 			printDebugMsg("Using existing user folder.");
 		}
 		
 		window.println("(SYSTEM) Home path: " + homePath, TextColor.BLUEGRAY);
 		
 		window.println("(SYSTEM) Run path: " + appPath, TextColor.BLUEGRAY);
 		
 		window.println("(SYSTEM) Requesting login info...", TextColor.GREEN);
 	}
 	
 	protected boolean initialize(boolean askAll)
 	{
 		if (askAll)
 		{	
 			String prfl = window.askQuestion("Enter the new or existing profile to use:", "default");
 			
 			if (prfl == null)
 				profile = null;
 			else
 				profile = new File(homePath + PSLASH + "profiles" + PSLASH + prfl.toLowerCase());
 			
 			try
 			{
 				Scanner in = new Scanner(profile);
 				
 				nickName = in.nextLine().trim();
 				realName = in.nextLine().trim();
 				hostName = in.nextLine().trim();
 			}
 			catch(Exception e)
 			{
 				nickName = window.askQuestion("Enter your nick-name:", nickName);
 				
 				if (nickName == null)
 					return false;
 		
 				realName = window.askQuestion("Enter your user-name:", realName);
 				
 				if (realName == null)
 					return false;
 				
 				hostName = window.askQuestion("Enter the host-name:", hostName);
 				
 				if (hostName == null)
 					return false;
 				
 				writeProfile(profile);
 			}
 		}
 		else
 		{
 			hostName = window.askQuestion("Invalid Host. Re-enter the host-name:", hostName);
 			
 			if (hostName == null)
 				return false;
 			
 			writeProfile(profile);
 		}
 		
 		window.setServerInfo(hostName);
 		
 		return true;
 	}
 	
 	protected boolean writeProfile(File outputProfile)
 	{
 		boolean writeable = checkFolders(struct) && profile != null;
 		
 		if (writeable)
 		{
 			try
 			{
 				if (!outputProfile.isFile() && !outputProfile.createNewFile())
 					printDebugMsg("couldn't create profile");
 				
 				BufferedOutputStream out = 
 					new BufferedOutputStream(new FileOutputStream(outputProfile));
 				
 				out.write((nickName + "\n").getBytes());
 				out.write((realName + "\n").getBytes());
 				out.write((hostName + "\n").getBytes());
 				
 				out.close();
 				
 				return true;
 			}
 			catch(Exception f)
 			{
 				printDebugMsg("couldn't write profile: " + f.getMessage());
 			}
 		}
 		
 		return false;
 	}
 	
 	protected boolean checkFolders(String[] folders)
 	{
 		File temp;
 		
 		for (int i = 0; i < folders.length; ++i)
 		{
 			temp = new File(homePath + PSLASH + folders[i]);
 			
 			if (!temp.isDirectory() && !temp.mkdir())
 				return false;
 		}
 		
 		return true;
 	}
 	
 	protected void notifyConnect()
 	{
 		sendData("NICK " + nickName);
 		sendData("USER " + nickName + " 0 * :" + realName);
 	}
 	
 	protected void sendData(String msg)
 	{
 		if (msg != null)
 			s.sendData(msg);
 	}
 	
 	protected void printSystemMsg(String msg, TextColor style)
 	{
 		window.println("(SYSTEM) " + msg, style);
 	}
 	
 	protected void printDebugMsg(String msg)
 	{
 		if (debug)
 		{
 			String timeStamp = time.format(new java.util.Date());
 	        
 			window.println("(ERROR) [" + timeStamp + "] " + msg, "\002ERROR\003", TextColor.RED);
 			
 			//System.err.println(msg);
 		}
 	}
 	
 	protected void sendMsg(String msg, String chanName)
 	{
 		String chan = chanName.toLowerCase();
 		
 		if (!plugins.isEmpty())
 		{
 			boolean halt = false;
 			
 			String[] output;
 			
 			for (int i = 0; i < plugins.size(); ++i)
 			{
 				output = plugins.get(i).processOutput(msg, chanName);
 				
 				if (output != null)
 				{
 					for (int j = 0; j < output.length; ++j)
 					{
 						if (output[j].equals("\002HALT\003"))
 							halt = true;
 						else
 							sendMsg(output[j], chanName);
 					}
 				}
 			}
 			
 			if (halt)
 				return;
 		}
 		
 		if (chanName.equals("\002ERROR\003"))
 		{
 			if (msg.toUpperCase().startsWith("/PART %ERR"))
 				debug = false;
 			
 			return;
 		}
 		
 		if (msg.charAt(0) == '/')
 		{
 			String command = new String();
 			int spaceIndex = msg.indexOf(' ');
 			
 			if (spaceIndex > -1)
 			{
 				command = msg.substring(1, spaceIndex).toUpperCase();
 			}
 			else
 			{
 				command = msg.substring(1).toUpperCase();
 			}
 			
 			if (command.equals("MSG"))
 			{
 				if (spaceIndex > -1)
 				{
 					int m1 = spaceIndex + 1;
 					int m2 = msg.indexOf(" ", m1 + 1);
 					
 					if (m2 > m1)
 					{
 						String recipient = msg.substring(m1, m2);
 						String message = msg.substring(m2 + 1);
 						
 						s.sendData("PRIVMSG " + recipient + " :" + msg);
 						
 						window.println("<" + nickName + "> ", recipient, TextColor.BLUE_BOLD);
 						window.print(message, recipient, TextColor.BLACK);
 					}
 				}
 			}
 			else if (command.equals("JOIN"))
 			{				
 				if (spaceIndex > -1)
 				{
 					String channel = msg.split(" ")[1];
 					
 					s.sendData("JOIN " + channel);
 					window.addChat(channel);
 					window.setFocusedChat(channel);
 				}
 				else if (!chanName.equals("Console"))
 				{
 					s.sendData("JOIN " + chanName);
 				}
 			}
 			else if (command.equals("SJOIN"))
 			{				
 				if (spaceIndex > -1)
 				{
 					String channel = msg.split(" ")[1];
 					
 					s.sendData("JOIN " + channel);
 					window.addChat(channel);
 				}
 				else if (!chanName.equals("Console"))
 				{
 					s.sendData("JOIN " + chanName);
 				}
 			}
 			else if (command.equals("REJOIN"))
 			{
 				if (spaceIndex > -1)
 				{
 					s.sendData("PART" + msg.substring(7));
 					
 					s.sendData(msg.substring(3));
 				}
 				else if (!chanName.equals("Console"))
 				{
 					s.sendData("JOIN " + chanName);
 				}
 			}
 			else if (command.equals("PART"))
 			{	
 				if (spaceIndex > -1)
 					closeChat(msg.substring(spaceIndex + 1));
 				else if (!chanName.equals("Console"))
 					closeChat(chanName);
 			}
 			else if (command.equals("AUTH"))
 			{
 				s.sendData("PRIVMSG NICKSERV :IDENTIFY " + msg.substring(spaceIndex + 1));
 			}
 			else if (command.equals("RECONNECT"))
 			{
 				window.println("\n(SYSTEM) Reconnecting...", chan, TextColor.ORANGE);
 				s.disconnect("reconnecting");
 			}
 			else if (command.equals("QUIT") || command.equals("DISCONNECT"))
 			{
 				s.disconnect("user termination");
 			}
 			else if (command.equals("SLOAD"))
 			{
 				window.println("(SYSTEM) Loading plugin (strict)...", chan, TextColor.BLUE);
 				
 				String pluginPath = msg.substring(spaceIndex + 1).trim();
 				
 				String pluginName = loadPlugin(pluginPath);
 				
 				if (pluginName != null)
 					window.println("(SYSTEM) " + pluginName + " loaded.", chan, TextColor.BLUE);
 				else
 					window.println("(SYSTEM) Plugin loading failed - path: " + pluginPath, chan, TextColor.BLUE);
 			}
 			else if (command.equals("LOAD"))
 			{
 				window.println("(SYSTEM) Loading plugin...", chan, TextColor.BLUE);
 				
 				String input = msg.substring(spaceIndex + 1).trim();
 				
 				String pluginPath;
 				
 				if (input.startsWith("http://"))
 					pluginPath = input;
 				else
 					pluginPath = homePath + PSLASH + "plugins" + PSLASH + input;
 				
 				if (!pluginPath.endsWith(".class"))
 					pluginPath += ".class";
 				
 				String pluginName = loadPlugin(pluginPath);
 				
 				if (pluginName != null)
 					window.println("(SYSTEM) " + pluginName + " loaded.", chan, TextColor.BLUE);
 				else
 					window.println("(SYSTEM) Plug-in failed to load or does not exist using path: " + pluginPath, chan, TextColor.BLUE);
 			}
 			else if (command.equals("SCRIPT"))
 			{
 				window.println("(SYSTEM) Executing script...", chan, TextColor.BLUE);
 				
 				String input = msg.substring(spaceIndex + 1).trim();
 				
 				String scriptPath;
 				
 				if (input.startsWith("http://"))
 					scriptPath = input;
 				else
 					scriptPath = homePath + PSLASH + "scripts" + PSLASH + input;
 				
 				if (!scriptPath.endsWith(".script"))
 					scriptPath += ".script";
 				
 				String scriptName = executeScript(scriptPath);
 				
 				if (scriptName != null)
 					window.println("(SYSTEM) " + scriptName + " finished.", chan, TextColor.BLUE);
 				else
 					window.println("(SYSTEM) Script failed to load or does not exist using path: " + scriptPath, chan, TextColor.BLUE);
 			}
 			else if (command.equals("DEBUG"))
 			{
 				debug = !debug;
 				
 				return;
 			}
 			else
 				s.sendData(msg.substring(1));
 		}
 		else
 		{
 			if (!chanName.equals("Console"))
 			{
 				s.sendData("PRIVMSG " + chanName + " :" + msg);
 				window.println("<" + nickName + "> ", chan, TextColor.BLUE_BOLD);
 				window.print(msg, chan, TextColor.BLACK);
 			}
 		}
 	}
 	
 	protected String loadPlugin(String path)
 	{
 	    PlugInLoader l = new PlugInLoader(this);
 	    
 	    try
 	    {
 	    	Class<?> p = l.findClass(path);
 	    	
 	    	Object o = p.newInstance();
 	    	
     		Plugin t = (Plugin)o;
     		
 	    	plugins.add(t);
 	    	
 	    	String[] msg = t.onLoad();
 	    	
 	    	if (msg != null)
 	    		for (int i = 0; i < msg.length; ++i)
 	    			sendData(msg[i]);
 	    	
 	    	return t.getVersion();
 	    }
 	    catch (InstantiationException e)
 	    {
 	    	e.printStackTrace();
 	    }
 	    catch (IllegalAccessException e)
 	    {
 	    	e.printStackTrace();
 	    }
 	    catch (Exception e)
 	    {	
 	    	printDebugMsg(e.toString());
 	    }
 	    
 	    return null;
 	}
 	
 	protected String executeScript(String path)
 	{
 	    try
 	    {
 	    	Scanner in = new Scanner(new File(path));
 			
 	    	if (!in.hasNextLine())
 	    		return null;
 	    		
 	    	String header = in.nextLine();
 	    	
 			while (in.hasNextLine())
 				sendMsg(in.nextLine().trim(), "Console");
 			
 			return header;
 	    }
 	    catch (Exception e)
 	    {
 	    	printDebugMsg(e.toString());
 	    }
 	    
 	    return null;
 	}
 	
 	protected void closeChat(String chan)
 	{
 		if (window.removeChat(chan) == true)
 		{
 			if (chan.charAt(0) == '#')
 				s.sendData("PART " + chan);
 		}
 		else
 		{
 			window.println("(ERROR) That chat does not exist.", "Console", TextColor.RED);
 		}
 	}
 	
 	protected void ProcessMessage(String rawIn)
 	{
 		Message x = new Message(rawIn, this);
 		
 		Code code = x.getCode();
 		String msg = x.getMessage();
 		String chan = x.getChannel();
 		
 		if (code == Code.UNKNOWN) System.out.println("\tUnknown command signature: " + msg);
 		
 		if (!plugins.isEmpty())
 		{
 			String[] output;
 			
 			for (int i = 0; i < plugins.size(); ++i)
 			{
 				output = plugins.get(i).processInput(rawIn, chan);
 				
 				if (output != null)
 					for (int j = 0; j < output.length; ++j)
 						s.sendData(output[j]);
 			}
 		}
 
 		if (code.ircCode < 0)
 		{
 			if (code == Code.MESSAGE)
 			{
 				if (x.getChannel().charAt(0) == 0x23)
 				{
 					window.println("<" + x.getNick() + "> ", chan, TextColor.BLUE);
 					
 					int i = msg.indexOf(nickName);
 					
 					if (i > -1)
 					{
 						int j = 0;
 						
 						while (i > -1)
 						{
 							window.print(msg.substring(j, i), chan, TextColor.BLACK);
 							
 							j = i + nickName.length();
 							
 							window.print(msg.substring(i, j), chan, TextColor.BLACK_BOLD);
 							
 							i = msg.indexOf(nickName, j);
 						}
 						
 						window.print(msg.substring(j), chan, TextColor.BLACK);
 					}
 					else
 					{
 						window.print(msg, chan, TextColor.BLACK);
 					}
 				}
 				else
 				{
 					window.println("<" + x.getNick() + "> ", chan, TextColor.VIOLET);
 					window.print(msg, chan, TextColor.BLACK);
 				}
 			}
 			else if (code == Code.NOTICE)
 			{
 				window.println("(NOTICE) " + msg, chan, TextColor.ORANGE);
 			}
 			else if (code == Code.PING)
 			{
 				s.sendData("PONG " + msg);
 				window.println("(PING) " + msg, chan, TextColor.GREEN);
 			}
 			else if (code == Code.JOIN)
 			{
 				window.println("<" + x.getNick() + " has joined>", chan, TextColor.BLUEGRAY);
 				
 				if (!x.getNick().equals(nickName))
 				{
 					window.addNicks(x.getChannel(), x.getNick());
 					users.put(x.getNick(), new User(x.getNick(), null, x.getChannel()));
 				}
 			}
 			else if (code == Code.PART)
 			{	
 				if (!chan.equals("Console"))
 				{
 					if (x.getNick().equals(nickName))
 					{
 						window.println("<You have left " + chan + ">", TextColor.BLUEGRAY);
 					}
 					else
 					{
 						if (msg.length() < 2)
 							window.println("<" + x.getNick() + " has left>", chan, TextColor.BLUEGRAY);
 						else
 							window.println("<" + x.getNick() + " has left - " + msg + ">", chan, TextColor.BLUEGRAY);
 						
 						window.removeNicks(chan, x.getNick());
 						
 						if (!removeUser(x.getNick()))
 							printDebugMsg(x.getNick() + " not found in user-treemap. (PART)");
 					}
 				}
 				else
 				{
 					printDebugMsg(x.getNick() + " cannot be removed from the Console. (PART)");
 					printDebugMsg(x.getMessage() + " | " + rawIn);
 				}
 			}
 			else if (code == Code.QUIT)
 			{
 				if (users.containsKey(x.getNick()))
 				{
 					String[] chans = users.get(x.getNick()).getChans();
 					
 					for (int a = 0; a < chans.length; ++a)
 					{
 						window.removeNicks(chans[a], x.getNick());
 						
 						if (msg.length() < 2)
 							window.println("<" + x.getNick() + " has quit>", chans[a], TextColor.BLUEGRAY);
 						else
 							window.println("<" + x.getNick() + " has quit - " + msg.toLowerCase() + ">", chans[a], TextColor.BLUEGRAY);
 						
 						if (!removeUser(x.getNick()))
 							printDebugMsg(x.getNick() + " not found in user-treemap. (QUIT)");
 					}
 				}
 				else
 				{
 					printDebugMsg(x.getNick() + " not found in user-treemap. (QUIT)");
 				}
 			}
 			else if (code == Code.MODE)
 			{
 				if (x.getNick() != hostName)
 				{
 					window.println("<" + x.getNick() + " is now " + msg + ">", chan, TextColor.BLUEGRAY);
 					window.replaceNick(x.getNick(), msg);
 				}
 				else
 					window.println("<" + x.getChannel() + " is now " + msg + ">", chan, TextColor.BLUEGRAY);
 			}
 			else if (code == Code.NICK)
 			{
 				// TODO: Show this notification on the right channels.
 				if (x.getNick().equals(nickName) == true)
 				{
 					nickName = msg;
 					window.replaceNick(x.getNick(), msg);
 					window.println("<You are now known as " + msg + ">", window.getFocusedChat(), TextColor.BLUE);
 					
 					writeProfile(profile);
 				}
 				else
 				{
 					window.replaceNick(x.getNick(), msg);
 					window.println("<" + x.getNick() + " is now known as " + msg + ">", chan, TextColor.BLUEGRAY);
 				}
 				
 /*		FIXME : keys are not being correctly changed.
 
 				if (users.containsKey(x.getNick()))
                 {
                         String[] chans = users.get(x.getNick()).getChans();
                         
                         for (int a = 0; a < chans.length; ++a)
                         {
                                 window.println("<" + x.getNick() + " is now known as " + msg + ">", chans[a], C.BLUEGREY);
                                 updateUser(x.getNick(), msg);
                         }
                 }
                 else
                 {
                         printDebugMsg(x.getNick() + " not found in user-treemap. (NICK)");
                 }
 */
 			}
 			else if (code == Code.CTCP_MSG)
 			{	
 				String reply = new String();
 				
 				if (msg.indexOf("ACTION") == 0)
 				{
 					window.println("<" + x.getNick() + msg.substring(msg.indexOf(" ")) + ">", chan, TextColor.VIOLET);
 				}
 				else if (msg.indexOf("PING") == 0)
 				{
 					window.println("<" + x.getNick() + " has requested your ping>", chan, TextColor.VIOLET);
 					reply = "PING " + msg.substring(msg.indexOf(" ") + 1);
 					s.sendData("NOTICE " + x.getNick() + " :\1" + reply + "\1");
 				}
 				else if (msg.indexOf("VERSION") == 0)
 				{
 					window.println("<" + x.getNick() + " has requested your version>", chan, TextColor.VIOLET);
 					reply = "VERSION wIRC v0.2 <wisteso@gmail.com>";
 					s.sendData("NOTICE " + x.getNick() + " :\1" + reply + "\1");
 				}
 				else if (msg.indexOf("TIME") == 0)
 				{
 					window.println("<" + x.getNick() + " has requested your local time>", chan, TextColor.VIOLET);
 					
 					Calendar T = Calendar.getInstance();
 					
 					reply = "TIME " + T.get(5) + "/" + T.get(2) + "/" + T.get(1) +
 						" " + T.get(10) + ":" + T.get(12) + ":" + T.get(13);
 						
 					if (T.get(9) == 0) reply += " AM";
 					else reply += " PM";
 					
 					s.sendData("NOTICE " + x.getNick() + " :\1" + reply + "\1");
 				}
 				else if (msg.indexOf("USERINFO") == 0)
 				{
 					window.println("<" + x.getNick() + " has requested your user info>", chan, TextColor.VIOLET);
 					s.sendData("NOTICE " + x.getNick() + " :\1" + reply + "\1");
 				}
 				else
 					printDebugMsg("!!! " + msg);
 			}
 			else if (code == Code.TOPIC)
 			{
 				window.println("(TOPIC) " + msg, chan, TextColor.BLUE);
 			}
 			else if (code == Code.ERROR)
 			{
 				if (x.getMessage().indexOf("Closing Link") > -1)
 					s.disconnect(msg);
 				else
 					window.println("(ERROR) " + msg, chan, TextColor.RED);
 			}
 			else
 			{
 				window.println("(" + code + ") " + msg, chan, TextColor.GRAY);
 			}
 		}
 		else if (code.ircCode > -1)
 		{
 			if (code.ircCode > 0 && code.ircCode < 7)  // Post-registration greeting.
 			{
 				if (code.ircCode < 4)
 					window.println("(GREET) " + msg, chan, TextColor.GREEN);
 				
 				// TODO - Add the rest of the code for the statistic crap here.
 			}
 			else if (code.ircCode > 249 && code.ircCode < 270)  // Misc. information.
 			{
 				window.println("(INFO) " + msg, chan, TextColor.GRAY);
 			}
 			else if (code == Code.CHAN_TOPIC)  // Topic.
 			{
 				window.println("(TOPIC) " + msg, chan, TextColor.BLUE);
 			}
 			else if (code.ircCode == 333 || code == Code.NAMELIST_CONTENT || code == Code.NAMELIST_FOOTER)  // Name list.
 			{
 				if (code == Code.NAMELIST_CONTENT)
 				{
 					SortedListModel l = window.getNickList(x.getChannel());
 					
 					ArrayList<String> tList = new ArrayList<String>();
 					
 					if (l == null) return;
 					
 					String t = new String();
 					
 					for (int c = 0; c < msg.length(); ++c)
 					{
 						if (msg.charAt(c) > 32)
 						{
 							t += msg.charAt(c);
 						}
 						else if (t.length() > 1)
 						{
 							tList.add(t);
 							
 							if (!users.containsKey(t))
 								users.put(t, new User(t, null, x.getChannel()));
 							else
 								users.get(t).addChans(x.getChannel());
 							
 							t = new String();
 						}
 						else
 						{
 							printDebugMsg("Invalid name: " + t);
 							t = new String();
 						}
 					}
 					
 					tList.add(t);
 					
 					Object[] tListObjs = tList.toArray();
 					
 					for (int i = 0; i < tList.size(); ++i)
 					{
 						if (!l.contains(tListObjs[i]))
 							l.addElement(tListObjs[i]);
 					}
 					
 					//l.addElement(tList.toArray(new String[tList.size()]));
 					
 					if (!users.containsKey(t))
 						users.put(t, new User(t, null, x.getChannel()));
 					else
 						users.get(t).addChans(x.getChannel());
 				}
 			}
 			else if (code.ircCode > 371 && code.ircCode < 377)  // Message of the day.
 			{
 				window.println("(MOTD) " + msg, chan, TextColor.GREEN);
 				
 				if (code == Code.MOTD_FOOTER)
 					sendMsg("/SCRIPT default.script", "Console");
 			}
 			else if (code.ircCode == 433)
 			{
 				String tempNick = "Nullname" + (int)(Math.random() * 10000);
 				window.println("(ERROR) Nickname already in use. Nickname set to: " + tempNick, chan, TextColor.RED);
 				
 				s.sendData("NICK " + tempNick);
 				nickName = tempNick;
 			}
 			else
 			{
 				window.println("(" + code + ") " + msg, chan, TextColor.GRAY);
 			}
 		}
 	}
 	
 	protected void disconnect(String reason)
 	{
 		s.disconnect(reason);
 	}
 	
 	protected boolean addUser(String name)
 	{
 		if (users.containsKey(name))
 		{
 			return false;
 		}
 		else
 		{
 			users.put(name, new User(name));
 			return true;
 		}
 	}
 	
 	protected boolean removeUser(String name)
 	{
 		if (users.containsKey(name))
 		{
 			users.remove(name);
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	protected boolean updateUser(String oldName, String newName)
 	{
 		if (users.containsKey(oldName))
 		{
 			users.get(oldName).addNick(newName);
 			users.put(newName, users.get(oldName));
 			users.remove(oldName);
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 }
