 package mcgill.game;
 
 public class Config {
 	
 	// Server config
 	public static final int THREADS = 10;
 	
 	// Redis server info
 	
	public static final String REDIS_HOST = "23.21.64.174";
 	public static final int REDIS_PORT = 6379;
 	
 	// Network commands
 	
 	public static final String SERVER = "server";
 	public static final String CLIENT = "client";
 	public static final String COMMAND = "command";
 	public static final String NOTIFICATIONS = "notifications";
 	
 	public static final String LOGIN = "login";
 	public static final String LOGOUT = "logout";
 	public static final String REGISTER = "register";
 	public static final String MESSAGE = "message";
 	public static final String GET_FRIENDS = "get_friends";
 	public static final String ADD_FRIEND = "add_friend";
 	public static final String GET_CHATS = "get_chats";
 	public static final String GET_CHAT = "get_chat";
 	public static final String CREATE_CHAT = "create_chat";
 	public static final String ADD_CREDITS = "add_credits";
 	public static final String GET_TABLES = "get_tables";
 	public static final String CREATE_TABLE = "create_table";
 	public static final String JOIN_TABLE = "join_table";
 	public static final String START_ROUND = "start_round";
 	
 	public static final String GET_COMMAND = "get_command";
 	public static final String EMIT_HANDS = "emit_hands";
 	public static final String TABLE_USERS = "table_users";
 	public static final String MESSAGE_REC = "message_rec";
 	public static final String POT_STATUS = "pot_status";
 	
 	// Game Configs
 	
 	public static final int REFILL_CREDITS = 1000;
 	public static final int MAX_PLAYERS = 5;
 	public static final int LOW_BET = 20;
 	public static final int MAX_RAISES = 3;
 	public static final int BRING_IN = 10;
 
 }
