 package com.alta189.chavabot;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.internal.Lists;
 
 public class Options {
 	
 	@Parameter
 	private List<String> parameters = Lists.newArrayList();
 	 
 	@Parameter(names = { "-verbose" , "-v" }, description = "Level of verbosity")
 	private boolean verbose = false;
 	 
 	@Parameter(names = {"-host" , "-h"}, description = "IRC Host")
 	private String host = "irc.ytalk.us";
 	
 	@Parameter(names = { "-port" , "-p" }, description = "IRC Port")
 	private int port = 6667;
 
 	@Parameter(names = { "-nick" , "-n" }, description = "IRC Nick")
 	private String nick = "ChavaBot";
 	
 	@Parameter(names = { "-login" , "-l" }, description = "IRC Login")
 	private String login = "ChavaBot";
 	
 	@Parameter(names = { "-pass" , "-ps"}, description = "IRC NickServ Password")
 	private String pass = null;
 	
 	@Parameter(names = { "-channels" , "-c" }, description = "IRC Channels to connect to separated by a comma")
 	private String channels = "#chavabot";
 	
 	public List<String> getChannels() {
 		List<String> result = new ArrayList<String>();
 		for (String chan : channels.split(",")) {
			result.add("#" + chan);
 		}
 		return result;
 	}
 	
 	public List<String> getParameters() {
 		return parameters;
 	}
 
 	public boolean isVerbose() {
 		return verbose;
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public String getNick() {
 		return nick;
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public String getPass() {
 		return pass;
 	}
 }
