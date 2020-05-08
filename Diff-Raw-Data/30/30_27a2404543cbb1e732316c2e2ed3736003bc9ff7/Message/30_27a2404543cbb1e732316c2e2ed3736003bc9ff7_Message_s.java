 package shared;
 
 import org.pircbotx.Channel;
 
 import lombok.Data;
 import connection.Connection;
 import connection.KEllyBot;
 
 @Data 
 public class Message {
 
 	public static final short CONSOLE = 0;
 	public static final short MSG = 1;
 	public static final short NOTICE = 2;
 	public static final short ACTION = 3;
 	public static final short PM = 4;
 
 	private String content, sender, channel;
 	private KEllyBot bot;
 	private short type;
 	
 	public Message(KEllyBot kEllyBot, String message, String nick,
 			String target, short type) {
 		this.setBot(kEllyBot);
		this.setContent(message+(message.contains("\r\n") ? "" : "\r\n"));
 		this.setSender(nick);
 		this.setChannel(target);
 		this.type = type;
 	}
 	//TODO: pass in the actual User object instead of a string
 	public Message(Connection nc, String content, String sender, String channel, short type){
 		this(nc.getBot(), content, sender, channel, type);
 	}
 	public Message(Connection nc, String message, String nick, Channel channel, short type) {
 		this(nc, message, nick, channel == null ? "Console" : channel.getName(), type);
 	}
 }
