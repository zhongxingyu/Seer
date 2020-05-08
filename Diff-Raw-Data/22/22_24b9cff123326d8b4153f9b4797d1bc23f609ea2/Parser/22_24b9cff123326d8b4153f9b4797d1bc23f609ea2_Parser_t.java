 package com.test9.irc.parser;
 
 import java.util.Arrays;
 
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
 
 	public static void main(String[] args)
 	{
 		Parser p = new Parser();
 
 		System.out.println(p.parse(":irc.ecsig.com 005 jared-test CMDS=KNOCK,MAP,DCCALLOW,USERIP " +
 				"UHNAMES NAMESX SAFELIST HCN MAXCHANNELS=50 CHANLIMIT=#:50 MAXLIST=b:60,e:60,I:60 " +
 				"NICKLEN=30 CHANNELLEN=32 TOPICLEN=307 KICKLEN=307 AWAYLEN=307 :are supported " +
 				"by this server"));
		
		p.reset_parser();
 
 		System.out.println(p.parse(":jared-test!jared-test@somehost 255 jared-test :I have 12 clients and 1 servers"));
	
		p.reset_parser();
		
		System.out.println(p.parse("255 jared-test :I have 12 clients and 1 servers"));

		p.reset_parser();
 	}
 
 	public Parser() 
 	{
 		init = true;
 	}
 
 	public String parse(String message)
 	{
 		String message_split[];
 		if(message.startsWith(":"))
 			prefix_present = true;
 
 		if(prefix_present)
 			message_split = message.split(" ", 3);
 		else
 			message_split = message.split(" ", 2);
		
 		System.out.println(Arrays.toString(message_split));
 
 		if(prefix_present)
		{
 			get_prefix(message_split[0]);
			command = message_split[1];
		}
		else
			command = message_split[0];
 
 		System.out.println("nickname:'"+nickname+"'");
 		System.out.println("user:'"+user+"'");
 		System.out.println("host:'"+host+"'");
 		System.out.println("server_name:'"+server_name+"'");
 		System.out.println("command:'"+command+"'");
 		return "";
 	}
 
 	private void get_prefix(String prefix)
 	{
 		prefix = prefix.substring(1);
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
 	public void reset_parser()
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
