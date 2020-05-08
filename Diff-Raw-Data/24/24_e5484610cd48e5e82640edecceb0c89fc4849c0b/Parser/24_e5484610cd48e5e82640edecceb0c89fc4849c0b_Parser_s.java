 package com.test9.irc.parser;
 
 /**
  * Used to parse messages that come from the server.
  *
  * <message>  ::= [':' <prefix> <SPACE> ] <command> <params> <crlf> 
  * <prefix>   ::= <servername> | <nick> [ '!' <username> ] [ '@' <host> ] 
  * <command>  ::= <letter> { <letter> } | <number> <number> <number> 
  * <SPACE>    ::= ' ' { ' ' }
  * <params>   ::= <SPACE> [ ':' <trailing> | <middle> <params> ] 
  * <middle>   ::= <Any *non-empty* sequence of octets not including SPACE or NUL or CR or LF, the first of which may not be ':'> 
  * <trailing> ::= <Any, possibly *empty*, sequence of octets not including NUL or CR or LF> 
  * <crlf>     ::= CR LF
  * @author Jared Patton
  *
  */
 public class Parser {
 
 	private static boolean init = false;
 	private static boolean prefix_present = false;
 	private static String prefix = "";
 	private static String command = "";
 	private static String params = "";
 	private static String server_name = "";
 	private static String nickname = "";
 	private static String user = "";
 	private static String host = "";
 	private static String content = "";
 	final private static String DIVIDER = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
 
 	public static void main(String[] args)
 	{
 		Parser p = new Parser();
 		p.parse(new StringBuffer(":irc.ecsig.com 005 jared-test CMDS=KNOCK,MAP,DCCALLOW,USERIP " +
 				"UHNAMES NAMESX SAFELIST HCN MAXCHANNELS=50 CHANLIMIT=#:50 MAXLIST=b:60,e:60,I:60 " +
 				"NICKLEN=30 CHANNELLEN=32 TOPICLEN=307 KICKLEN=307 AWAYLEN=307 :are supported " +
 				"by this server"));
 		System.out.println(DIVIDER);
 
 		p.parse(new StringBuffer(":irc.ecsig.com 255 jared-test :I have 12 clients and 1 servers")).toString();
 		System.out.println(DIVIDER);
 
 		p.parse(new StringBuffer(":jared-test!jared-test@ecsig-A1B219D7.ri.ri.cox.net JOIN :#jared"));
 		System.out.println(DIVIDER);
 
 		p.parse(new StringBuffer(":Jared!Jared@ecsig-A1B219D7.ri.ri.cox.net PRIVMSG #jircc :ermahgard"));
 		System.out.println(DIVIDER);
 
 		p.parse(new StringBuffer(":Jared!Jared@ecsig-A1B219D7.ri.ri.cox.net PRIVMSG #jared ::here is a message: with : some :semicolons:::"));
 		System.out.println(DIVIDER);
 
 		p.parse(new StringBuffer("255 jared-test :I have 12 clients and 1 servers"));
 		System.out.println(DIVIDER);
 		
 		p.parse(new StringBuffer(":irc.ecsig.com 333 jared-test #jared Jared 1355349884"));
 		System.out.println(DIVIDER);
 		
 		p.parse(new StringBuffer("PING :irc.ecsig.com"));
 		
 	}
 
 	/**
 	 * Initializes the parser and set's init to true.
 	 * @param init sets to true
 	 */
 	public Parser() 
 	{
 		init = true;
 	}
 
 	/**
 	 * 
 	 * @param message
 	 * @return
 	 */
 	public Message parse(StringBuffer message)
 	{
 		reset_parser();
 
 		if(message.substring(0,1).equals(":"))
 		{
 			prefix_present = true;
 			message.delete(0, 1);
 		}
 
 		if(prefix_present)
 		{
 			prefix = message.substring(0, message.indexOf(" "));
 			message.delete(0, message.indexOf(" ") + 1);
			parse_prefix(prefix);
 		}
 
 		command = message.substring(0, message.indexOf(" "));
 		message.delete(0, message.indexOf(" "));
 
 		if(message.indexOf(" :") >= 0)
 		{
 			params = message.substring(0, message.indexOf(" :")).trim();
 			message.delete(0, message.indexOf(" :") + 2);
 			
 			content = message.substring(0, message.length());
 		}
 		else
 		{
 			params = message.substring(0, message.length()).trim();
 			message.delete(0, message.length());
 			content = "";
 		}
 
 		print_stuff();
 
 		return(new Message(prefix, command, params, server_name, nickname, user, host, content));
 
 	}
 
 	/**
 	 * 
 	 * @param prefix
 	 */
	private void parse_prefix(String prefix)
 	{
 		String split_prefix[] = prefix.split("[!@ ]");
 		if(split_prefix.length == 3) {
 			nickname = split_prefix[0];
 			user = split_prefix[1];
 			host = split_prefix[2];
 		}
 		else if(split_prefix.length == 2)
 		{
 			nickname = split_prefix[0];
 			host = split_prefix[1];
 		}
 		else
 			server_name = split_prefix[0];	
 	}
 
 	/**
 	 * 
 	 */
 	private void reset_parser()
 	{
 		prefix_present = false;
 		prefix = "";
 		command = "";
 		params = "";
 		server_name = "";
 		nickname = "";
 		user = "";
 		host = "";
 		content = "";
 	}
 
 	/**
 	 * 
 	 */
 	private void print_stuff()
 	{
 		System.out.println("Prefix: \t'" + prefix + "'");
 		System.out.println("Command: \t'" + command + "'");
 		System.out.println("Params: \t'"+ params + "'");
 		System.out.println("Server_name: \t'" + server_name + "'");
 		System.out.println("Nickname: \t'" + nickname + "'");
 		System.out.println("User: \t\t'"+ user + "'");
 		System.out.println("Host: \t\t'"+host + "'");
 		System.out.println("Content: \t'"+content+"'");
 	}
 
 	/**
 	 * @return the init
 	 */
 	public static boolean isInit() {
 		return init;
 	}
 }
