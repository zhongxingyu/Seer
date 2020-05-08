 /*
  * Adrastea IRC Bot
  * Version 2.0.0 (Java)
  *
  * (c) 2011 Sam Thompson <sam@websyntax.net>
  * License: http://www.opensource.org/licenses/mit-license.php The MIT License
  */
 
 package adrastea;
 
 public class Adrastea {
 	/*
 	 * Main
 	 */
 	public static void main(String[] args) {
 		System.out.println("Starting up Adrastea IRC Bot");
 
 		// Declare our IRC environment vars
 		IrcConnection irc = new IrcConnection(); // Connection object
 		IrcMessage message; // Stores the message per tick
 		IrcEventHandler event = new IrcEventHandler();
 		String outBuffer;
 
 		System.out.println("Loading configuration file");
 		// Load all our config stuffs
 		try {
 			IrcConfig.load("config.txt");
 		}
 		catch(Exception e) {
 			System.out.println("[ERROR] Unable to load configuration file.");
 			System.exit(0);
 		}
 		
 		System.out.println("Setting listeners");
 
 		// Add all listeners
 		// PING
 		event.register(new IrcListener("system.ping").handler(new IrcListenerInterface () {
 			public String[] run(IrcMessage m) {
 				return new String[] {"PONG :" + m.message};
 			}
 		}));
 
		// Join Channels after identifing
		event.register(new IrcListener("system.notice").handler(new IrcListenerInterface () {
 			public String[] run(IrcMessage m) {
				return (m.message.startsWith("You are now identified for")) ? new String[] {"JOIN " +  IrcConfig.channels} : null;
 			}
 		}));
 
 		// Send login
 		event.register(new IrcListener("system.notice").handler(new IrcListenerInterface () {
 			public String[] run(IrcMessage m) {
 				return (m.message.startsWith("This nickname is registered")) ? new String[] {"PRIVMSG NickServ :IDENTIFY " + IrcConfig.pass} : null;
 			}
 		}));
 
 		// Respond to VERSION
 		event.register(new IrcListener("user.ctcp.version").handler(new IrcListenerInterface () {
 			public String[] run(IrcMessage m) {
 				return new String[] {"PRIVMSG " +  m.nick + " :\001CLIENT Adrastea v2.0.0 JavaSE_1.6\001"};
 			}
 		}));
 
 		// Exit
 		event.register(new IrcListener("user.message").handler(new IrcListenerInterface () {
 			public String[] run(IrcMessage m) {
 				if(m.vhost.equals(IrcGlobals.commander) && m.message.equals("shutdown")) {
 					IrcGlobals.exit = true;
 				}
 				return null;
 			}
 		}));

		// Identify 
 		event.register(new IrcListener("user.message").handler(new IrcListenerInterface () {
 			public String[] run(IrcMessage m) {
 				if (m.message.toLowerCase().startsWith("identify ")) {
 					if(m.message.substring(m.message.indexOf(" ") + 1).equals(IrcConfig.passphrase)) {
 						IrcGlobals.commander = m.vhost;
 						return new String[] {"PRIVMSG " + m.nick + " :Your wish is now my command"};
 					}
 					else {
 						return new String[] {"PRIVMSG " + m.nick + " :Improper credentials supplied"};
 					}
 				}
 
 				return null;
 			}
 		}));
 
		event.register(new IrcListener("channel.event.invite").handler(new IrcListenerInterface () {
 			public String[] run(IrcMessage m) {
 				if (m.vhost.equals(IrcGlobals.commander)) {
 					return new String[] {"JOIN " + m.message};
 				}
 
 				return null;
 			}
 		}));
 
 		// Connection loop
 		while(true) {
 			try {
 				// let us connect
 				System.out.println("Connecting to " + IrcConfig.host + ":" + IrcConfig.port + "...");
 				irc.connect(IrcConfig.host, IrcConfig.port);
 				
 				// Send our nickname and user
 				irc.sendRaw("NICK " + IrcConfig.nick);
 				irc.sendRaw("USER " + IrcConfig.user + " 8 * :" + IrcConfig.name);
 
 				// Receive message loop
 				while(true) {
 					message = new IrcMessage(irc.recv());
 					event.run(new IrcEvent(message));
 
 					System.out.println(message.raw);
 
 					while((outBuffer = event.queue()) != null) {
 						irc.sendRaw(outBuffer);
 						System.out.println(outBuffer);
 					}
 
 					// This will eventually check a flag to see if an exit
 					// command has been given
 					if (IrcGlobals.exit) {
 						irc.sendRaw("QUIT :" + IrcConfig.quitmsg);
 						System.out.println("QUIT :" + IrcConfig.quitmsg);
 						throw new IrcExitException();
 					}
 				}
 			}
 			catch(IrcNullMessageException e) {
 				System.out.println("\n[ERROR] Network Error, reconnecting...");
 			}
 			catch(IrcFailedConnectException e) {
 				System.out.println("\n[ERROR] Failed to connect, retrying...");
 			}
 			catch(IrcExitException e) {
 				System.out.println("\nSystem Shutdown.");
 				irc.close();
 				System.exit(0);
 			}
 			catch(Exception e) {
 				System.out.println("[ERROR] Unknown Error, reconnecting...");
 			}
 		}
 	}
 }
