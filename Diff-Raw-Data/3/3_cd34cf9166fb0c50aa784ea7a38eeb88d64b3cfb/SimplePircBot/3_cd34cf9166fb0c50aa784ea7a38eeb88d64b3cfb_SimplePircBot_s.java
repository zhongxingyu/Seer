 package com.alta189.chavabot;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jibble.pircbot.PircBot;
 
 import com.alta189.chavabot.events.botevents.InvitedEvent;
 import com.alta189.chavabot.events.botevents.PrivateMessageEvent;
 import com.alta189.chavabot.events.botevents.ServerPingEvent;
 import com.alta189.chavabot.events.channelevents.ChannelKickEvent;
 import com.alta189.chavabot.events.channelevents.ChannelOpEvent;
 import com.alta189.chavabot.events.channelevents.ChannelVoiceEvent;
 import com.alta189.chavabot.events.channelevents.ChannelJoinEvent;
 import com.alta189.chavabot.events.channelevents.MessageEvent;
 import com.alta189.chavabot.events.channelevents.ChannelPartEvent;
 import com.alta189.chavabot.events.channelevents.SetChannelBanEvent;
 import com.alta189.chavabot.events.channelevents.SetModeratedEvent;
 import com.alta189.chavabot.events.ircevents.ConnectEvent;
 import com.alta189.chavabot.events.ircevents.DisconnectEvent;
 import com.alta189.chavabot.events.userevents.ActionEvent;
 import com.alta189.chavabot.events.userevents.ChavaUserEvent;
 import com.alta189.chavabot.events.userevents.NickChangeEvent;
 import com.alta189.chavabot.events.userevents.NoticeEvent;
 import com.alta189.chavabot.events.userevents.QuitEvent;
 
 public class SimplePircBot extends PircBot {
 	private final ChavaBot parent;
 	private WhoisResult whoisResult;
 
 	@Override
 	protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
 		ChavaManager.getPluginManager().callEvent(NoticeEvent.getInstance(new ChavaUser(sourceNick, sourceLogin, sourceHostname, null), target, notice));
 	}
 
 	private boolean waitWhois = false;
 	private Map<String, String> motds = new HashMap<String, String>();
 
 	protected String getMotd(String channel) {
 		return motds.get(channel);
 	}
 
 	protected SimplePircBot(ChavaBot parent) {
 		this.parent = parent;
 	}
 
 	@Override
 	protected void onConnect() {
 		ChavaManager.getPluginManager().callEvent(ConnectEvent.getInstance());
 		for (String channel : parent.getAjChannels()) {
 			parent.joinChannel(channel);
 		}
 	}
 
 	@Override
 	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
 		ChavaManager.getPluginManager().callEvent(PrivateMessageEvent.getInstance(message, new ChavaUser(sender, login, hostname, null)));
 	}
 
 	@Override
 	protected void onJoin(String channel, String sender, String login, String hostname) {
 		parent.updateChannel(channel);
 		ChavaManager.getPluginManager().callEvent(ChannelJoinEvent.getInstance(new ChavaUser(sender, login, hostname, null), channel));
 	}
 
 	public synchronized void sendWhois(String nick) {
 		while (waitWhois) {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 			}
 		}
 		waitWhois = true;
 		whoisResult = new WhoisResult(nick);
 		this.sendRawLineViaQueue("WHOIS " + nick);
 	}
 
 	@Override
 	protected void onDisconnect() {
 		ChavaManager.getPluginManager().callEvent(DisconnectEvent.getInstance());
 	}
 
 	@Override
 	protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
 		motds.remove(channel);
 		motds.put(channel, topic);
 		parent.updateChannel(channel);
 	}
 
 	@Override
 	protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
 		Channel chan = parent.getChannel(channel);
 		ChavaUser user = new ChavaUser(sourceNick,sourceLogin,sourceHostname,null);
 		if (chan != null) {		
 		} else {
 			parent.updateChannel(channel);
 			chan  = parent.getChannel(channel);
 		}
 		ChavaManager.getPluginManager().callEvent(ChannelOpEvent.getInstance(user, channel, recipient));
 	}
 
 	@Override
 	protected void onAction(String sender, String login, String hostname, String target, String action) {
 		ChavaManager.getPluginManager().callEvent(ActionEvent.getInstance(new ChavaUser(sender, login, hostname, null), target, action));
 	}
 
 	@Override
 	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
 		ChavaManager.getPluginManager().callEvent(MessageEvent.getInstance(new ChavaUser(sender, login, hostname, null), channel, message));
 	}
 
 	@Override
 	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
 		ChavaManager.getPluginManager().callEvent(QuitEvent.getInstance(new ChavaUser(sourceNick, sourceLogin, sourceHostname, null), reason));
 	}
 
 	@Override
 	protected void onPart(String channel, String sender, String login, String hostname) {
 		parent.updateChannel(channel);
 		ChavaManager.getPluginManager().callEvent(ChannelPartEvent.getInstance(new ChavaUser(sender, login, hostname, null), channel));
 	}
 
 	@Override
 	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
 		ArrayList<Channel> channels = (ArrayList<Channel>) parent.getChannels();
 		for (Channel chan : channels) {
 			if (chan.getUser(oldNick) != null) {
 				parent.updateChannel(chan.toString());
 			}
 		}
 		ChavaManager.getPluginManager().callEvent(NickChangeEvent.getInstance(oldNick, newNick));
 	}
 
 	@Override
 	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
 		Channel chan = parent.getChannel(channel);
 		ChavaUser user = new ChavaUser(kickerNick,kickerLogin,kickerHostname,null);
 		if (chan != null) {		
 		} else {
 			parent.updateChannel(channel);
 			chan  = parent.getChannel(channel);
 		}
 		ChavaManager.getPluginManager().callEvent(ChannelKickEvent.getInstance(user, channel, recipientNick, reason));
 	}
 
 	@Override
 	protected void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
 		Channel chan = parent.getChannel(channel);
 		ChavaUser user = new ChavaUser(sourceNick,sourceLogin,sourceHostname,null);
 		if (chan != null) {		
 		} else {
 			parent.updateChannel(channel);
 			chan  = parent.getChannel(channel);
 		}
 		ChavaManager.getPluginManager().callEvent(ChannelVoiceEvent.getInstance(user, channel, recipient));
 	}
 
 	@Override
 	protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
 		ChavaManager.getPluginManager().callEvent(SetChannelBanEvent.getInstance(new ChavaUser(sourceNick,sourceLogin,sourceHostname, null), channel, hostmask));
 	}
 
 	@Override
 	protected void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
 		ChavaManager.getPluginManager().callEvent(SetModeratedEvent.getInstance(new ChavaUser(sourceNick,sourceLogin,sourceHostname, null), channel));
 	}
 
 	@Override
 	protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
 		ChavaManager.getPluginManager().callEvent(InvitedEvent.getInstance(channel, new ChavaUser(sourceNick, sourceLogin,sourceHostname, null)));
 	}
 
 	@Override
 	protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
 		this.sendMessage(sourceNick, ChavaManager.getVersion());
 	}
 
 	@Override
 	protected void onServerPing(String response) {
 		ChavaManager.getPluginManager().callEvent(ServerPingEvent.getInstance(response));
		this.sendRawLine("PONG" + response);
 	}
 
 	@Override
 	protected void onServerResponse(int code, String response) {
 		switch (code) {
 			case RPL_WHOISUSER:
 				whoisResult.put(code, response);
 				break;
 			case RPL_WHOISCHANNELS:
 				whoisResult.put(code, response);
 				break;
 			case RPL_ENDOFWHOIS:
 				whoisResult.put(code, response);
 				ChavaUser result = whoisResult.build();
 				whoisResult = null;
 				waitWhois = false;
 				ChavaManager.getPluginManager().callEvent(ChavaUserEvent.getInstance(result));
 				break;
 		}
 	}
 
 }
