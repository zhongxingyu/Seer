 package com.dustyneuron.bitprivacy.exchanger;
 
 import org.jboss.netty.channel.Channel;
 
 
 public class ContactInfo {
 	private Channel channel;
 	
 	public ContactInfo(Channel c) {
 		channel = c;
 	}
 	
 	public Channel getChannel() {
 		return channel;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj.getClass() == ContactInfo.class) {
			return ((ContactInfo) obj).channel.equals(channel);
 		}
 		return super.equals(obj);
 	}
 	
 	@Override
 	public String toString() {
 		return channel.toString();
 	}
 }
