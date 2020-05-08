 package gr.clog.bot;
 
 import gr.clog.bot.logging.ILogger;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class RequestHandler 
 {
 	private static RequestHandler instance = null;
 	private List<String> authTokens;
 	private List<String> channels;
 	private List<String> channelsToLog;
 	List<ILogger> loggers;
 	
 	protected RequestHandler()
 	{
 		authTokens = new ArrayList<String>();
 		this.channels = new ArrayList<String>();
 		this.channelsToLog = new ArrayList<String>();
 		loadConfig();
 	}
 	
 	public static RequestHandler getInstance()
 	{
 		if(instance==null)
 		{
 			instance = new RequestHandler();
 		}
 		return instance;
 	}
 	
 	public String processRequest(Request request) throws IOException
 	{
 		if(request.isPing())
 		{
 			return "PONG slapmelikeabitch\n";
 		}
 		
 		if(request.isAuth())
 		{
 			String password = request.getLine().split("!auth")[1].trim();
			if(password.equals("yourpassword"))
 			{
 				authTokens.add(getAuthToken(request.getLine()));
 			}
 		}
 		
 		if(request.isNickServRegisterNotice())
 		{           
			return "PRIVMSG NickServ :identify password\n";
 		}
 		
 		if(request.isSystemMsg())
 		{
 
 		}
 		
 		if(request.isChannelMsg())
 		{
 			String temp1 = request.getLine().substring(0, request.getLine().indexOf("PRIVMSG"));
 			String temp2 = request.getLine().substring(request.getLine().indexOf("PRIVMSG")+7, request.getLine().length());
 			String chan = temp2.substring(0,temp2.indexOf(":")).trim();
 			String text = temp2.substring(temp2.indexOf(":")+1, temp2.length());
 			String nick = temp1.substring(1, temp1.indexOf("!"));
			
 			Date d = new Date();
 			
 			if(channelsToLog.contains(chan.toLowerCase().toString()))
 			{
 				for(ILogger logger: loggers)
 				{
 					logger.logMsg(nick, chan, text, d.getTime());
 				}
 			}
 		}
 		
 		if(request.isCommand())
 		{
 			if(isAuthenticated(getAuthToken(request.getLine())))
 			{
 				if(request.isJoin())
 				{
 					String channel = request.getLine().split("!join")[1];
 			        channels.add(channel);
 			        rewriteConfig();
 			        return "JOIN "+channel+"\n";
 				}
 				else if(request.isPart())
 				{
 					String channel = request.getLine().split("!part")[1];
 			        channels.remove(channel);
 			        rewriteConfig();
 			        return "PART "+channel+"\n";
 				}
 				if(request.isLog())
 				{
 					String channel = request.getLine().split("!log")[1].trim();
 					channelsToLog.add(channel.toLowerCase().toString());
 					rewriteConfig();
 				}
 				if(request.isUnlog())
 				{
 					String channel = request.getLine().split("!unlog")[1].trim();
 					channelsToLog.remove(channel);
 					rewriteConfig();
 				}
 				else if(request.isExit())
 				{
 					return "QUIT\n";
 				}
 			}
 		}
 		return "";
 	}
 	
 	private void rewriteConfig()
 	{
 		String fileName = ClassLoader.getSystemResource("gr/clog/bot/channels.conf").getFile();
 		
 		BufferedWriter out;
 		try
 		{
 			out = new BufferedWriter(new FileWriter(fileName));
 			
 			out.write(":channels_to_log\n");
 			for(String chanToLog: channelsToLog)
 			{
 				if(!chanToLog.startsWith(":"))
 				{
 					out.write(chanToLog.trim()+"\n");
 				}
 			}
 			out.write(":channels\n");
 			for(String chan: channels)
 			{
 				if(!chan.startsWith(":"))
 				{
 					out.write(chan.trim()+"\n");
 				}
 			}
 		    
 			out.close();
 		} 
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	    
 
 		
 	}
 	
 	private void loadConfig()
 	{
 		String fileName = ClassLoader.getSystemResource("gr/clog/bot/channels.conf").getFile();
 		try
 		{
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			String line;
 			boolean writeToChans = false;
 			boolean writeToChansToLog = false;
 			
 		    while((line=in.readLine())!=null)
 		    {
 		        if(line.equals(":channels_to_log"))
 		        {
 		        	writeToChans = true;
 		        	writeToChansToLog = false;
 		        }
 		        if(line.equals(":channels"))
 		        {
 		        	writeToChans = false;
 		        	writeToChansToLog = true;
 		        }
 		        
 		        if(writeToChans)
 		        {
 		        	channels.add(line.trim());
 		        }
 		        else if(writeToChansToLog)
 		        {
 		        	channelsToLog.add(line.trim());
 		        }
 		        
 		    }
 		    in.close();
 		} 
 		catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
 		} 
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 	
 	private String getAuthToken(String line)
 	{
 		String temp = line.split("!auth")[0];
 		String nick = temp.substring(1, temp.indexOf("!"));
 		String host = temp.substring(temp.indexOf("!")+1, temp.indexOf(" PRIVMSG"));
 		return nick+"@"+host;
 	}
 	
 	private boolean isAuthenticated(String authToken)
 	{
 		return authTokens.contains(authToken);
 	}
 
 	public void setLoggers(List<ILogger> loggers)
 	{
 		this.loggers = loggers;
 	}
 
 	public List<String> getChannels() 
 	{
 		return channels;
 	}	
 
 }
