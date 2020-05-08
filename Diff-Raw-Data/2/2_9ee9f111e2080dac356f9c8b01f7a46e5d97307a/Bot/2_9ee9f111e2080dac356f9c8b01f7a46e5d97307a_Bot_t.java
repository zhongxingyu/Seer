 package net.mms_projects.irc.channel_bots;
 
 import net.mms_projects.irc.channel_bots.irc.Command;
 import net.mms_projects.irc.channel_bots.irc.commands.NickIntroduce;
 
 public class Bot {
 	public String nickname;
 	public String hostname;
 	public String username;
 	public String realname;
 	public boolean quit;
 	
 	public Bot (String nickname, String realname, String username, 
 			String hostname) {
 		this.quit = false;
 		this.nickname = nickname;
 		this.hostname = hostname;
 		this.username = username;
 		this.realname = realname;
 	}
 
 	public NickIntroduce getNickIntroduce() {
 		NickIntroduce c = new NickIntroduce();
 		c.nickname = nickname;
 		c.hobs = 1;
 		c.timestamp = (int) (ChannelBots.date.getDate().getTime() / 1000);
 		c.username = username;
 		c.hostname = hostname;
 		c.server = ChannelBots.server.server;
 		c.serviceStamp = 1;
 		c.realname = realname;
		return c;
 	}
 	
 	public void quit () {
 		quit = true;
 	}
 }
