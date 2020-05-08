 /*
  * WikiResponder.java
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
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import com.seanmadden.deepthought.IRCClient;
 import com.seanmadden.deepthought.Message;
 import com.seanmadden.deepthought.MessageHandler;
 
 public class WikiResponder implements MessageHandler {
 
 	@Override
 	public boolean handleMessage(IRCClient irc, Message m) {
 		String message = m.getMessage();
 		if(!message.contains("!wiki")){
 			return false;
 		}
 		String[] args = message.split(" ", 2);
 		if(args.length < 2){
 			return false;
 		}
 		if(!args[0].equals("!wiki")){
 			return false;
 		}
 		
 		String wiki = "https://secure.wikimedia.org/wikipedia/en/wiki/";
 		try {
			args[1] = args[1].replaceAll(" ", "_");
 			wiki += URLEncoder.encode(args[1], "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		
 		Message mesg = new Message(wiki, m.getTarget());
 		irc.sendMessage(mesg);
 		return true;
 	}
 
 }
