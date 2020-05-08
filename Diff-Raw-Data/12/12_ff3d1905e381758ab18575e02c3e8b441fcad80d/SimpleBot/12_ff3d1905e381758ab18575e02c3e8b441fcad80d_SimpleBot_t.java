 package main;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jibble.pircbot.*;
 
 /*
  * This is the main class for the bot.
  */
 
 public class SimpleBot extends PircBot{
 	
 	// Which prefix to use for commands
 	private String commandIdentifier = "-";
 	
 	// The current version of the bot. Only increment this each time there is a release.
 	// Convention: (milestone).(major)[.(minor).[(revision/bugfix)]]
	public static final String version = "1.7.0.2";
 	
 	// More debug output?
 	private static final boolean verbose = false;
 	
 	// UGLY EWW EWW UGLY this list contains all exception messages so they can be read directly from irc,
 	// using the -ed command.
 	public List<String> unreadExceptions = new ArrayList<String>();
 	
 	private CommandHandler ch;
 	
 	// Should generate a getter for this at some point and update my code to use it. CBA to do it now.
 	public static SimpleBot instance;
 	
 	// List of all rems. Currently not persistent. Will eventually be saved in a MySQL database.
 	private Map<String, String> rems = new HashMap<String,String>();
 
 	// When true, enables rems and the $g command
 	public boolean cadburyMode = false;
 	
 	// Initializes some variables, shows login data and connects to the MySQL DB.
 	public SimpleBot(){
 		String name = SettingsManager.getInstance().getSetting("nick");
 		String login = SettingsManager.getInstance().getSetting("ident");
 		String port = SettingsManager.getInstance().getSetting("sqlport");
 		String host = SettingsManager.getInstance().getSetting("sqlhost");
 		String user = SettingsManager.getInstance().getSetting("sqluser");
 		String pass = SettingsManager.getInstance().getSetting("sqlpass");
 		
 		if(name.equals("") || login.equals("") || port.equals("") || host.equals("") || user.equals("")){
 			System.out.println("Please make sure you've entered the bot settings in your settings file. Bot will now shut down.");
 			System.exit(0);
 		}
 		int iPort = 0;
 		try{
 			iPort = Integer.parseInt(port);
 		}catch(NumberFormatException e){
 			System.out.println("Please check your settings file. The 'Port' setting may only contain numbers. Bot will now shut down.");
 			System.exit(0);
 		}
 		
 		System.out.println("\t IRC Server:\tirc.esper.net");
 		System.out.println("\t IRC Nick:\t" + name);
 		System.out.println("\t IRC Login:\t" + login);
 		System.out.println("\t===========================");
 		System.out.println("\t SQL Host:\t" + host);
 		System.out.println("\t SQL Port:\t" + port);
 		System.out.println("\t SQL User:\t" + user);
 		System.out.println("\t SQL DB:\t" + "stats_bot");
 		
 		
 		
 		this.setName(name);
 		this.setLogin(login);
 		
 		SqlConnector.getInstance().connect(host, iPort, user, pass, "stats_bot");
 		
 		ch = new CommandHandler();
 		
 		instance = this;
 	}
 	public void onAction(String sender, String login, String hostname, String target, String action){
 		StatsHandler.getInstance().processAction(sender,login,hostname,target,action);
 	}
 	
 	// Gets executed whenever an IRC message is sent
 	public void onMessage(String channel, String sender, String login, String hostname, String message){
 		// Process the line as a command if it starts with the command identifier or, if cadbury mode is enabled, the $ character.
 		if(message.startsWith(commandIdentifier) || (cadburyMode && message.startsWith("$"))){
 			ch.processCommand(channel, sender, login, hostname, message);
 		}
 		// Only update statistics for lines that are not Cadbury commands
 		else if(!message.startsWith("$")){
 			StatsHandler.getInstance().processMessage(channel, sender, login, hostname, message);
 		}
 	}
 	// Gets executed whenever somebody joins the channel
 	public void onJoin(String channel, String sender, String login, String hostname){
 		// Automatically disable cadbury mode if Cadbury joins the channel
 		if(login.equals("~Cadbury")){
 			cadburyMode = false;
 		}
 	}
 	// THROWS EXCEPTION EWW EWWW UGLY
 	// Intializes the bot by creating an instance of it, having it connect to an IRC server and join a channel.
 	public static void main(String args[]) throws Exception{
 		splash();
 		SimpleBot bot = new SimpleBot();
 		System.out.println("\n==============================================");
 		System.out.println("Connecting to IRC server...");
 		bot.setVerbose(verbose);
 		bot.connect("irc.esper.net");
 		System.out.println("   Done");
 		String channel = SettingsManager.getInstance().getSetting("channel");
 		System.out.println("Joining " + channel);
 		bot.joinChannel(channel);
 		System.out.println("Ready to serve.");
 	}
 	
 	// This was not cheaply copied from Cadbury's source or anything. Nope. Not at all.
 	// In my defense, my ASCII art sucks.
 	private static void splash() {
 		System.out.println("   ###   ##   ##   ## #   # ###   ##  #####");
 		System.out.println("   #  # #  # #    #    # #  #  # #  #   #");
 		System.out.println("   ###  #### #    #     #   ###  #  #   #");
 		System.out.println("   #  # #  # #  # #  #  #   #  # #  #   #");
 		System.out.println("   ###  #  #  ##   ##   #   ###   ##    #");
 		System.out.println("\t===========================");
 		System.out.println("\t Version:\t" + version);
 		System.out.println("\t Author:\tbaggerboot");
 		System.out.println("\t===========================");
 		
 	}
 	public boolean remExists(String rem){
 		return rems.containsKey(rem);
 	}
 	public void addRem(String rem, String definition) {
 		rems.put(rem, definition);
 	}
 	public void removeRem(String rem){
 		rems.remove(rem);
 	}
 	public void removeRemIfExists(String rem){
 		if(remExists(rem)){
 			removeRem(rem);
 		}
 	}
 	public String getRem(String rem){
 		return rems.get(rem);
 	}
 	// This /should/ disconnect the bot cleanly.
 	public void shutdown(){
 		quitServer();
 		dispose();
 		SqlConnector.getInstance().disconnect();
 		System.exit(0);
 	}
 }
