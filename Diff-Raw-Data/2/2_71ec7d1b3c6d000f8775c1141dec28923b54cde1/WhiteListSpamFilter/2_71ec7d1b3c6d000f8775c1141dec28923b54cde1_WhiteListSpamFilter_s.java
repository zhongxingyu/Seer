 package com.phdroid.smsb.filter;
 
 import com.phdroid.smsb.SmsPojo;
 import com.phdroid.smsb.storage.dao.Session;
 import com.phdroid.smsb.storage.dao.SmsMessageSenderEntry;
 
 /**
  * Spam Filter taking into consideration only white list of senders.
  */
 public class WhiteListSpamFilter implements ISpamFilter {
 	Session session;
 
 	public WhiteListSpamFilter(Session session) {
 		this.session = session;
 	}
 
 	@Override
 	public boolean isSpam(SmsPojo message) {
 		SmsMessageSenderEntry sender = this.session.getSenderById(message.getSenderId());
		return sender.isInWhiteList();
 	}
 }
