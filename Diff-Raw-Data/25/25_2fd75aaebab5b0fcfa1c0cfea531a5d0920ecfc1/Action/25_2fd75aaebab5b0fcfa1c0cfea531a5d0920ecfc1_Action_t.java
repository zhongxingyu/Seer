 package com.angrykings;
 
 public class Action {
 
 	public class Server {
 		public static final int LOBBY_UPDATE = 1001;
 		public static final int REQUEST = 1002;
 		public static final int START = 1003;
 		public static final int DENIED = 1004;
 		public static final int TURN = 1005;
 		public static final int CONFIRM = 1006;
 		public static final int PARTNER_LEFT = 1007;
 		public static final int UNKNOWN_USER = 1008;
 		public static final int YOU_WIN = 1009;
 		public static final int KNOWN_USER = 1010;
 		public static final int SEND_NAME = 1011;
 		public static final int END_TURN = 1012;
 	}
 
 	public class Client {
 		public static final int SET_NAME = 2001;
 		public static final int GO_TO_LOBBY = 2002;
 		public static final int PAIR = 2003;
 		public static final int ACCEPT = 2004;
 		public static final int DENY = 2005;
 		public static final int TURN = 2006;
 		public static final int SET_ID = 2007;
 		public static final int LOSE = 2008;
 		public static final int GET_NAME = 2009;
 		public static final int END_TURN = 2010;
		public static final int LEAVE_LOBBY = 2011;		
 	}
 }
