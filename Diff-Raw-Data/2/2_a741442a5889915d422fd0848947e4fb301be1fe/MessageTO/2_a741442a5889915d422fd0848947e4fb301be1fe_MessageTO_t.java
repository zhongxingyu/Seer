 package com.jpizarro.th.lib.message.entity;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.ElementList;
 import org.simpleframework.xml.Root;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 
 @XStreamAlias("message")
@Root(name="message")
 public class MessageTO implements Serializable{
 
 	@Element(required=false)
 	private long messageId;
 	@Element(required=false)
 	private long sender;
 //	private long receiverLogin;
 	@ElementList(required=false)
 	Set<UserTO> receivers;
 	@Element(required=false)
 	private String messageBody;
 	@Element(required=false)
 	private boolean readed = false;
 	@Element(required=false)
 	private int type;
 	
 //	public MessageTO(Message message) {
 //		this.messageId = message.getMessageId();
 //		this.type = message.getType();
 //		
 //		if (message.getSender() == null)
 //			this.senderLogin = null;
 //		else
 //			this.senderLogin = message.getSender().getUsername();
 //		if (message.getReceiver() == null)
 //			this.receiverLogin = null;
 //		else
 //			this.receiverLogin = message.getReceiver().getUsername();
 //		
 //		this.messageBody = message.getMessageBody();	
 //	}
 	
 	public MessageTO(long sender, Set<UserTO> receivers, 
 			String messageBody, int type) {
 		super();
 		this.setSender(sender);
 //		this.setReceiverLogin(receiverLogin);
 		this.receivers = receivers;
 		this.messageBody = messageBody;
 		this.type = type;
 	}
 	public MessageTO() {
 		super();
 		receivers = new HashSet<UserTO>();
 	}
 //	public MessageTO(long messageId, long senderLogin, long receiverLogin,
 //			String messageBody, int type) {
 //		super();
 //		this.messageId = messageId;
 //		this.setSenderLogin(senderLogin);
 //		this.setReceiverLogin(receiverLogin);
 //		this.messageBody = messageBody;
 //		this.type = type;
 //	}
 //	public MessageTO(long messageId, String senderLogin, String receiverLogin,
 //			String messageBody, int type) {
 //		super();
 //		this.messageId = messageId;
 //		this.setSenderLogin(Long.parseLong(senderLogin));
 //		this.setReceiverLogin(Long.parseLong(receiverLogin));
 //		this.messageBody = messageBody;
 //		this.type = type;
 //	}
 //	public MessageTO(String senderLogin, String receiverLogin, String messageBody,
 //			int type) {
 //		this.setSenderLogin(Long.parseLong(senderLogin));
 //		this.setReceiverLogin(Long.parseLong(receiverLogin));
 //		this.messageBody = messageBody;
 //		this.type = type;
 //	}
 	public long getMessageId() {
 		return messageId;
 	}
 	public void setMessageId(long messageId) {
 		this.messageId = messageId;
 	}
 	public String getMessageBody() {
 		return messageBody;
 	}
 	public void setMessageBody(String messageBody) {
 		this.messageBody = messageBody;
 	}
 	public int getType() {
 		return type;
 	}
 	public void setType(int type) {
 		this.type = type;
 	}
 	public void setSender(long sender) {
 		this.sender = sender;
 	}
 	public long getSender() {
 		return sender;
 	}
 	public void setReaded(boolean readed) {
 		this.readed = readed;
 	}
 	public boolean isReaded() {
 		return readed;
 	}
 	public Set<UserTO> getReceivers() {
 		return receivers;
 	}
 	public void setReceivers(Set<UserTO> receivers) {
 		this.receivers = receivers;
 	}
 }
