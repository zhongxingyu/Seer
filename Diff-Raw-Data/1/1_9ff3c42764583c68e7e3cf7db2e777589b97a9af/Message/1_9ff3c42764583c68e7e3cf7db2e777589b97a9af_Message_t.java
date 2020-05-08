 /* This code is part of a plugin for Freenet. It is distributed 
  * under the GNU General Public License, version 3 (or at your option
  * any later version). See http://www.gnu.org/ for details of the GPL. */
 
 package plugin.frirc;
 /**
  * This should be a proper parser someday, please contribute ;)
  * @author tmarkus
  *
  */
 
 public class Message {
 
 	private String type = "";
 	private String nick = "";
 	private String username = "";
 	private String value = "";
 	private String channel = "";
 	private String raw = "";
 	
 	/**
 	 * Constructor for existing messages
 	 * @param message
 	 */
 	
 	public Message(String message)
 	{
 		//System.out.println(message);
 		this.raw = message;
 		
 		String[] split = message.split(" ");
 		
 		if (message.equals("QUIT"))
 		{
 			this.type = "QUIT";
 		}
 		
 		else if (split[0].equals("NICK"))
 		{
 			this.type = "NICK";
 			this.nick = split[1];
 		}
 		
 		else if (split[0].equals("USER"))
 		{
 			this.type = "USER";
 			this.username = split[1];
 			if (split.length > 1) this.value = "extra_info_we_will_ignore";
 		}
 
 		else if (split[0].equals("MODE"))
 		{
 			this.type = "MODE";
 			this.channel = split[1];
 			if (split.length > 2) this.value = split[2];
 		}
 		
 		else if (split[1].equals("NOTICE"))
 		{
 			this.type = "NOTICE";
 			
 			//FIXME: set other fields too
 		}
 		
 		
 		else if (split[0].equals("WHO"))
 		{
 			this.type = "WHO";
 			this.channel = split[1];
 		}
 		
 		else if (split[0].equals("JOIN"))
 		{
 			this.type = "JOIN";
 			if (split.length > 1) this.channel = split[1];
 		}
 
 		else if (split[0].equals("PART") && split.length == 2)
 		{
 			this.type = "PART";
 			this.channel = split[1];
 		}
 		
 		else if (split[1].equals("PART") && split.length == 3)
 		{
 			this.type = "PART";
 			this.channel = split[2];
 			this.username = split[0];
 		}
 		
 		else if (split[0].equals("TOPIC"))
 		{
 			this.type = "TOPIC";
 			if (split.length > 1) this.channel = split[1];
 			if (split.length > 2) this.value = split[2];
 		}
 		
 		else if (split[0].equals(":" + IRCServer.SERVERNAME))
 		{
 			if (split[1].equals("001") || split[1].equals("375") || split[1].equals("372") || split[1].equals("376"))
 			{
 				this.type = split[1];
 				this.nick = split[2];
 				for(int i=3; i < split.length; i++)
 				{
 					this.value += split[i] + " ";
 				}
 			}
 			else if (split[1].equals("004"))
 			{
 				this.type = split[1];
 				this.nick = split[2];
 				this.value = split[3];
 			}
 			else if (split[1].matches("331") || split[1].matches("353") || split[1].matches("366"))
 			{
 				this.type = split[1];
 				this.raw = message;
 			}
 			else if (split[1].matches("352"))
 			{
 				this.type = split[1];
 				this.raw = message;
 			}
 		
 		}
 		else if (split.length == 3 && split[1].equals("JOIN"))
 		{
 			this.type = split[1];
 			this.channel = split[2];
 			this.value = split[0];
 		}
 		else if (split.length == 3 && split[1].equals("MODE"))
 		{
 			this.type = "MODE";
 			this.raw = message;
 		}
 		else if (split.length > 2 && split[1].equals("PRIVMSG"))
 		{
 			this.type = "PRIVMSG";
 			this.username = split[0];
 			this.channel = split[2];
 			
 			for(int i=3; i < split.length; i++)
 			{
 				this.value += split[i] + " ";
 			}
			this.value = this.value.substring(1);
 		}
 		else if (split.length > 2 && split[0].equals("PRIVMSG"))
 		{
 			this.type = "PRIVMSG";
 			this.channel = split[1];
 			
 			for(int i=2; i < split.length; i++)
 			{
 				this.value += split[i] + " ";
 			}
 		
 			this.value = this.value.substring(1);
 		}
 		
 		
 		else if (split[0].equals("PING"))
 		{
 			this.type = "PING";
 			this.value = split[1];
 		}
 		
 		else
 		{
 			System.out.println("UNABLE TO PARSE MESSAGE, storing as raw: " + message);
 			this.raw = message;
 		}
 		
 	}
 	
 	/**
 	 * Constructor for new messages
 	 */
 	
 	public Message(){
 		
 	}
 
 	
 	public final String getType()
 	{
 		return this.type;
 	}
 	
 	/**
 	 * Convert the message to a string
 	 */
 	
 	public String toString()
 	{
 		/*
 		
 		if (type.equals("NICK"))
 		{
 			return type + " " + nick;
 		}
 		else if (type.equals("JOIN"))
 		{
 			if (channel.equals(""))
 			{
 				return type + " " + value;
 			}
 			else
 			{
 				return value + " " + type + " " + channel;
 			}
 		}	
 		else if (type.equals("TOPIC"))
 		{
 			return type + " " + channel +  " " + value;
 		}	
 		else if (type.equals("001") || type.equals("375") || type.equals("372") || type.equals("376"))
 		{
 			return ":" + IRCServer.SERVERNAME + " " + type + " " + nick + " " + value;
 		}
 		else if (type.equals("004"))
 		{
 			return ":" + IRCServer.SERVERNAME + " " + type + " " + nick + " " + IRCServer.SERVERNAME + " " + value;
 		}
 		else if (type.matches("331") || type.matches("353") || type.matches("366"))
 		{
 			return this.raw;
 		}
 		*/
 		
 		return raw;
 		
 		
 		//fail safe (should never be returned)
 		
 //		return "ERROR: " + type;
 	}
 	
 	public String getValue()
 	{
 		return this.value;
 	}
 	
 	public String getNick()
 	{
 		return this.nick;
 	}
 
 	public String getChannel()
 	{
 		return this.channel;
 	}
 
 	public String getUser()
 	{
 		return this.username;
 	}
 	
 }
