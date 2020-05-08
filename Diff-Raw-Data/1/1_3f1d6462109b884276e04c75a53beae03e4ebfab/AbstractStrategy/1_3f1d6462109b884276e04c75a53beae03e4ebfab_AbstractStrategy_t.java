 package com.chitter.bot.strategy;
 
 import java.util.List;
 
 import twitter4j.TwitterException;
 
 import com.chitter.persistence.UserAccount;
 import com.google.appengine.api.xmpp.JID;
 import com.google.appengine.api.xmpp.Message;
 import com.google.appengine.api.xmpp.MessageBuilder;
 import com.google.appengine.api.xmpp.MessageType;
 import com.google.appengine.api.xmpp.XMPPService;
 import com.google.appengine.api.xmpp.XMPPServiceFactory;
 
 public abstract class AbstractStrategy {
 	protected static final XMPPService xmppService =
 		XMPPServiceFactory.getXMPPService();
 	
 	abstract public void handleMessage(UserAccount userAccount, Message message) throws TwitterException;
 	
 	protected void replyToMessage(Message message, String body) {
 		Message reply = new MessageBuilder()
 		.withRecipientJids(message.getFromJid())
 		.withMessageType(MessageType.CHAT)
 		.withBody(body)
 		.build();
 		xmppService.sendMessage(reply);
 	}
 
 	protected void sendMessage(String recipient, String body) {
 		sendMessage(new JID[] {new JID(recipient)}, body);
 	}
 
 	protected void sendMessage(List<String> recipientList, String body) {
 		sendMessage((JID[]) recipientList.toArray(), body);
 	}
 
 	protected void sendMessage(JID[] recipients, String body) {
 		Message message = new MessageBuilder()
 		.withRecipientJids(recipients)
 		.withMessageType(MessageType.NORMAL)
 		.withBody(body)
 		.build();
 
 		xmppService.sendMessage(message);
 	}
 }
