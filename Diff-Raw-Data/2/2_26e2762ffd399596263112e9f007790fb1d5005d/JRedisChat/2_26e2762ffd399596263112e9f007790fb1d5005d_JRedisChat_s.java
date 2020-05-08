 import java.util.Map;
 
 /**
  * Super basic multi-user chat client that relies on a Redis backend for
  * PubSub. Each channel listens on their own thread (as JRedis blocks on 
  * polling the channel) and makes a callback to the main thread. UI is
  * run off the main thread (bad.. I know, but who cares?).
  * 
  * Assignment: Complete the Redis commands necessary to have a functioning
  * multi-user chat client! Look for //Assignment blocks scattered throughout
  * the User class (these are the only areas that need to be filled in).
  * 
  * Happy hunting!
  * 
  * @author Stephen Smithbower 2013
  * 
  */
 public class JRedisChat implements IMessageEventListener
 {
 	//////////////////////////////////////////////////////
 	// GLOBAL CONFIG									//
 	//////////////////////////////////////////////////////
 	//Change this to match the Redis server IP.
 	public static String host = "192.168.1.103";
 	
 	/**
 	 * User object - manages our pubsub connections and state.
 	 */
 	private User interactiveUser;
 	
 	/**
 	 * Really simple/crappy Swing UI for chatting.
 	 */
 	private ChatUI ui;
 	
 	
 	/**
 	 * Main entry point in to the program.
 	 */
 	public static void main(String[] args)
 	{
 		JRedisChat chat = new JRedisChat();
 	}
 	
 	
 	/**
 	 * Creates a new Redis PubSub connection pool, and spins up the chat UI.
 	 */
 	public JRedisChat()
 	{
 		//Create a new user.
 		this.interactiveUser = new User(host, this);
 		
 		//Display our silly chat UI.
 		this.ui = new ChatUI(this);
 		//SwingUtilities.invokeLater(ui); //Make sure we run on the even-dispatcher thread.
 		this.ui.run();
 		
 		//Add command listing.
 		this.ui.addMessage("COMMAND LISTING:");
 		this.ui.addMessage("/me [username] [age] [sex] [location]       Logs the user in to the server.");
 		this.ui.addMessage("/join [channel]                             Joins the [channel].");
 		this.ui.addMessage("/tell [user] [message]                      Sends a private [message] to [user].");
 		this.ui.addMessage("/delete                                     Deletes the current user, logging them out.");
 		this.ui.addMessage("/leave [channel]                            Leaves the [channel].");
 		this.ui.addMessage("/whois [user]                               Grabs information on [user].");
 		this.ui.addMessage("/chat [channel] [message]                   Broadcasts [message] to [channel].");
 		this.ui.addMessage("");
 	}
 
 
 	/**
 	 * Callback raised by crappy UI when a user hits enter on the input textbox.
 	 * Parse the user-entered command string to figure out what we're to do next.
 	 * 
 	 * @param command The user-entered command string, that needs to be parsed. 
 	 */
 	public void handleUserCommand(String command)
 	{
 		String[] cmds = command.split(" ");
 		
 		//switch(cmds[0])
 		//{
 		
 			//Identifies the user to the server.
 			//[username] [age] [sex] [location]
 			if (cmds[0].equals("/me"))
 			{
 				this.interactiveUser.me(cmds[1], Integer.parseInt(cmds[2]), cmds[3], cmds[4]);
 				this.ui.addMessage("Hello " + this.interactiveUser.username + "!");
 				return;
 			}
 			
 			//Subscribes the user to a specific channel.
 			//[channel]
 			if (cmds[0].equals("/join"))
 			{
 				this.interactiveUser.join(cmds[1]);
 				return;
 			}
 				
 			//Whispers a private message to a specific user.
 			//[user] [message]
 			if (cmds[0].equals("/tell"))
 			{
 				this.interactiveUser.tell(cmds[1], fixSplits(cmds, 2));
 				this.ui.addMessage("Whisper to " + cmds[1] + ": " + fixSplits(cmds, 2));
 				return;
 			}
 			
 			//Wipes the current user out of the universe.
 			if (cmds[0].equals("/delete"))
 			{
 				this.interactiveUser.delete();
 				return;
 			}
 				
 			//Leaves the specified channel.
 			//[channel]
 			if (cmds[0].equals("/leave"))
 			{
				this.interactiveUser.leave(cmds[2]);
 				return;
 			}
 				
 			//Gets some basic data on a user.
 			//[user]
 			if (cmds[0].equals("/whois"))
 			{
 				Map<String, String> who = this.interactiveUser.whois(cmds[1]);
 				
 				for(String key : who.keySet())
 					this.ui.addMessage(key + ": " + who.get(key));
 				
 				return;
 			}
 				
 			//Broadcasts a message to the specified channel.
 			//Defaults to channel "all".
 			//[channel] [message]
 			if (cmds[0].equals("/chat"))
 			{
 				this.interactiveUser.sendMessage(cmds[1], fixSplits(cmds, 2));
 				return;
 			}
 				
 			//Default.
 			//Broadcasts a message on channel "all".
 			//[message]
 			this.interactiveUser.sendMessage("all", fixSplits(cmds, 0));
 	}
 	
 	
 	/**
 	 * Crappy little method to take a string that has beeb split on
 	 * spaces, and recombine the array back in to a string.
 	 * 
 	 * @param source The source string array.
 	 * @param startIndex Where in the source string array to begin recombining.
 	 * 
 	 * @return A recombined string complete with spaces.
 	 */
 	private String fixSplits(String[] source, int startIndex)
 	{
 		String ret = "";
 		
 		for (int i = startIndex; i < source.length - 1; i++)
 			ret += source[i] + " ";
 		ret += source[source.length - 1];
 		
 		return ret;
 	}
 
 	
 	@Override
 	/**
 	 * Raised by User->Channel when a message has been received.
 	 * 
 	 * @param listener The User that has received this message.
 	 * @param from The name of the user who sent the message.
 	 * @param channel The channel on which the message was received.
 	 * @param message The message contents.
 	 */
 	public void onMessageReceived(User listener, String from, String channel, String message) 
 	{
 		if (!listener.username.equals(from))
 			System.out.println("-" + listener.username + "-[" + channel + "][" + from + "]: " + message);
 		
 		this.ui.addMessage("[" + channel + "] [" + from + "]: " + message);
 	}
 	
 }
