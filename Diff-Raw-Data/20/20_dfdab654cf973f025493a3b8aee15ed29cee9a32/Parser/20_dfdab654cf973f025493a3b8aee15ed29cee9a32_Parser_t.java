 package com.test9.irc.parser;
 
 import java.util.Arrays;
 
 public class Parser {
 	/*
 	 * <message>  ::= [':' <prefix> <SPACE> ] <command> <params> <crlf> 
 	 * <prefix>   ::= <servername> | <nick> [ '!' <username> ] [ '@' <host> ] 
 	 * <command>  ::= <letter> { <letter> } | <number> <number> <number> 
 	 * <SPACE>    ::= ' ' { ' ' }
 	 * <params>   ::= <SPACE> [ ':' <trailing> | <middle> <params> ] 
 	 * <middle>   ::= <Any *non-empty* sequence of octets not including SPACE or NUL or CR or LF, the first of which may not be ':'> 
 	 * <trailing> ::= <Any, possibly *empty*, sequence of octets not including NUL or CR or LF> 
 	 * <crlf>     ::= CR LF
 	 */
 	private static boolean init = false;
 	private static boolean prefix_present = false;
 	private static String prefix = "";
 	private static String command = "";
 	private static String params = "";
 	private static String server_name = "";
 	private static String nickname = "";
 	private static String user = "";
 	private static String host = "";
 
 	public static void main(String[] args)
 	{
 		Parser p = new Parser();
 
 		p.parse(new StringBuffer(":irc.ecsig.com 005 jared-test CMDS=KNOCK,MAP,DCCALLOW,USERIP " +
 				"UHNAMES NAMESX SAFELIST HCN MAXCHANNELS=50 CHANLIMIT=#:50 MAXLIST=b:60,e:60,I:60 " +
 				"NICKLEN=30 CHANNELLEN=32 TOPICLEN=307 KICKLEN=307 AWAYLEN=307 :are supported " +
 				"by this server"));
 
 		p.parse(new StringBuffer(":jared-test!jared-test@somehost 255 jared-test :I have 12 clients and 1 servers"));
 
 		p.parse(new StringBuffer("255 jared-test :I have 12 clients and 1 servers"));
 
 	}
 
 	public Parser() 
 	{
 		init = true;
 	}
 
 	public void parse(StringBuffer message)
 	{
 		reset_parser();
 
 		if(message.substring(0,1).equals(":"))
 		{
 			prefix_present = true;
 			message.delete(0, 1);
 		}
 		System.out.println("after ':' found : '" + message+"'");
 
 		if(prefix_present)
 		{
			prefix = message.substring(0, message.indexOf(" ")+1);
 			message.delete(0, message.indexOf(" ") + 1);
			parse_prefix(prefix);
 		}
 		System.out.println("after prefix deleted: '" + message+"'");
		
		command = message.substring(0, message.indexOf(" "));
		message.delete(0, message.indexOf(" ") + 1);
		System.out.println("after command deleted: '" + message + "'");
		
 		System.out.println("nickname:'"+nickname+"'");
 		System.out.println("user:'"+user+"'");
 		System.out.println("host:'"+host+"'");
 		System.out.println("server_name:'"+server_name+"'");
 		System.out.println("command:'"+command+"'");
 	}
 
 	private void parse_prefix(String prefix)
 	{
 		System.out.println("prefix: '"+prefix+"'");
 		String split_prefix[] = prefix.split("[!@ ]");
 		System.out.println(Arrays.toString(split_prefix));
 
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
 
 	}
 
 	/**
 	 * @return the init
 	 */
 	public static boolean isInit() {
 		return init;
 	}
 	/**
 	 * @param init the init to set
 	 */
 	public static void setInit(boolean init) {
 		Parser.init = init;
 	}
 
 	public static String getPrefix() {
 		return prefix;
 	}
 
 	public static void setPrefix(String prefix) {
 		Parser.prefix = prefix;
 	}
 
 	public static String getCommand() {
 		return command;
 	}
 
 	public static void setCommand(String command) {
 		Parser.command = command;
 	}
 
 	public static String getParams() {
 		return params;
 	}
 
 	public static void setParams(String params) {
 		Parser.params = params;
 	}
 
 	public static String getServer_name() {
 		return server_name;
 	}
 
 	public static void setServer_name(String server_name) {
 		Parser.server_name = server_name;
 	}
 
 	public static String getNickname() {
 		return nickname;
 	}
 
 	public static void setNickname(String nickname) {
 		Parser.nickname = nickname;
 	}
 
 	public static String getUser() {
 		return user;
 	}
 
 	public static void setUser(String user) {
 		Parser.user = user;
 	}
 
 	public static String getHost() {
 		return host;
 	}
 
 	public static void setHost(String host) {
 		Parser.host = host;
 	}
 
 }
