 package main;
 
 /**
  * Connection
  *
  * @author Michael Mrozek
  *         Created Oct 3, 2010.
  */
 public class Connection {
 	public static class DefaultConnection extends Connection {
 		@Override public String getPassword() {
 			return NoiseBot.me.getSecretData("nickserv-pass");
 		}
 	}
 
 	private final String server, nick, password, channel;
 	private final int port;
 	
 	public Connection() {this("rhnoise", null, "#rhnoise");}
	public Connection(String nick, String password, String channel) {this("irc.freenode.net", 6667, nick, password, channel);}
 	public Connection(String nick, String channel) {this(nick, null, channel);}
 	public Connection(String server, int port, String nick, String password, String channel) {
 		this.server = server;
 		this.port = port;
 		this.nick = nick;
 		this.password = password;
 		this.channel = channel;
 	}
 	
 	public String getServer() {return this.server;}
 	public int getPort() {return this.port;}
 	public String getNick() {return this.nick;}
 	public String getPassword() {return this.password;}
 	public String getChannel() {return this.channel;}
 }
