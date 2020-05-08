 package com.alta189.chavabot;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 
 import com.alta189.chavabot.events.botevents.JoinEvent;
 import com.alta189.chavabot.events.botevents.KickEvent;
 import com.alta189.chavabot.events.botevents.PartEvent;
 import com.alta189.chavabot.events.botevents.SendMessageEvent;
 import com.alta189.chavabot.events.botevents.SendNoticeEvent;
 import com.alta189.chavabot.events.userevents.NickChangeEvent;
 
 public class ChavaBot {
 	private SimplePircBot bot = new SimplePircBot(this);
 	private List<Channel> channels = new ArrayList<Channel>();
 	private String host;
 	private int port = 0;
 	private List<String> ajChannels = new ArrayList<String>();
 
 	public void connect() throws NickAlreadyInUseException, IOException, IrcException {
 		if (host != null && port != 0 && bot.getNick() != null && bot.getLogin() != null) {
 			bot.connect(host, port);
 		}
 	}
 
 	protected List<String> getAjChannels() {
 		return ajChannels;
 	}
 
 	protected void setAjChannels(List<String> ajChannels) {
 		this.ajChannels = ajChannels;
 	}
 
 	public void requestChavaUser(String nick) {
 		bot.sendWhois(nick);
 	}
 
 	public void setVerbose(boolean verbose) {
 		bot.setVerbose(verbose);
 	}
 
 	public String getNick() {
 		return bot.getNick();
 	}
 
 	public void setNick(String nick) {
 		NickChangeEvent event = NickChangeEvent.getInstance(bot.getNick(), nick);
 		ChavaManager.getPluginManager().callEvent(event);
 		if (!event.isCancelled()) {
 			if (bot.isConnected()) {
 				bot.changeNick(event.getNewNick());
 			} else {
 				bot.setName(event.getNewNick());
 			}
 		}
 	}
 
 	public String getLogin() {
 		return bot.getLogin();
 	}
 
 	public void setLogin(String login) {
 		if (!bot.isConnected())
 			bot.setLogin(login);
 	}
 
 	public Channel getChannel(String channel) {
 		for (Channel chan : channels) {
 			if (chan.equals(channel))
 				return chan;
 		}
 		return null;
 	}
 
 	public void updateChannel(String channel) {
 		channels.remove(channel);
 		Channel chan = new Channel(channel);
 		chan.addUsers(bot.getUsers(channel));
 		chan.setMotd(bot.getMotd(channel));
 		channels.add(chan);
 	}
 
 	public List<Channel> getChannels() {
 		return channels;
 	}
 
 	public String getHost() {
 		return host;
 	}
 
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	public void sendMessage(String target, String message) {
 		SendMessageEvent event = SendMessageEvent.getInstance(message, target);
 		if (!event.isCancelled()) {
 			bot.sendMessage(event.getTarget(), event.getMessage());
 		}
 	}
 
 	public void sendAction(String target, String message) {
 		SendMessageEvent event = SendMessageEvent.getInstance(message, target);
 		if (!event.isCancelled()) {
 			bot.sendMessage(event.getTarget(), event.getMessage());
 		}
 	}
 
 	public void sendNotice(String target, String notice) {
 		SendNoticeEvent event = SendNoticeEvent.getInstance(notice, target);
 		if (!event.isCancelled()) {
 			bot.sendNotice(event.getTarget(), event.getNotice());
 		}
 	}
 
 	public void kick(String channel, String nick) {
 		kick(channel, nick, null);
 	}
 
 	public void kick(String channel, String nick, String reason) {
 		KickEvent event = KickEvent.getInstance(channel, nick, reason);
 		if (!event.isCancelled()) {
 			bot.kick(event.getChannel(), event.getRecipient());
 		}
 	}
 
 	public void joinChannel(String channel) {
 		JoinEvent event = JoinEvent.getInstance(channel);
 		if (!event.isCancelled()) {
 			bot.joinChannel(event.getChannel());
 		}
 	}
 
 	public void partChannel(String channel) {
 		partChannel(channel, null);
 	}
 
 	public void partChannel(String channel, String reason) {
 		PartEvent event = PartEvent.getInstance(channel, reason);
 		if (!event.isCancelled()) {
 			bot.partChannel(event.getChannel(), event.getReason());
 		}
 	}
 	
	public void discconect() {
 		bot.quitServer();
 		ChavaManager.getPluginManager().disablePlugins();
 		System.exit(1);
 	}
 	
 	public void disconnect(String reason) {
 		bot.quitServer(reason);
 		ChavaManager.getPluginManager().disablePlugins();
 		System.exit(1);
 	}
 
 }
