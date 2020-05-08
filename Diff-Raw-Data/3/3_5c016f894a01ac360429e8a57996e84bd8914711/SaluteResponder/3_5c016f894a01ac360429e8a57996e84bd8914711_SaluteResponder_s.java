 /*
  * SaluteResponder.java
  * 
  *  $Id $
  *  Author: smadden
  *
  *    Copyright (C) 2010 Sean Madden
  *
  *    Please see the pertinent documents for licensing information.
  *
  */
 
 package com.seanmadden.deepthought.responders;
 
 import com.seanmadden.deepthought.IRCClient;
 import com.seanmadden.deepthought.Message;
 import com.seanmadden.deepthought.MessageHandler;
 
 /**
  * [Insert class description here]
  *
  * @author Sean P Madden
  */
 public class SaluteResponder implements MessageHandler {
 
 	private static String[] ranks = {
 		"major", "general", "private", "corporal", "chief", "kernel"
 	};
 	
 	/**
 	 * [Place method description here]
 	 *
 	 * @see com.seanmadden.deepthought.MessageHandler#handleMessage(com.seanmadden.deepthought.IRCClient, com.seanmadden.deepthought.Message)
 	 * @param irc
 	 * @param m
 	 * @return
 	 */
 	@Override
 	public boolean handleMessage(IRCClient irc, Message m) {
 		String message = m.getMessage();
 		for(String rank : ranks){
 			if(!message.toLowerCase().contains(rank)){
 				continue;
 			}
 			int startpos = message.toLowerCase().indexOf(rank) + rank.length();
 			int offset = message.indexOf(' ', startpos + 1);
 			if(offset < 0){
 				offset = message.length();
 			}
 			String response = message.substring(startpos, offset);
 			response = "\u0001ACTION salutes " + rank + " " + response + "\u0001";
 			Message msg = new Message(response, m.getTarget());
 			irc.sendMessage(msg);
 			return true;
 		}
 		return false;
 	}
 
 }
