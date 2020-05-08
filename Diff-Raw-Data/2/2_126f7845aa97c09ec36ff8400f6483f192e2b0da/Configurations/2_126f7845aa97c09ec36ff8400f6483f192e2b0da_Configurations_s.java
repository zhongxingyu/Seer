 package pt.traincompany.utility;
 
 import pt.traincompany.main.DatabaseHelper;
 
 public class Configurations {
 	
	final public static String AUTHORITY = "10.0.2.2:3000";
 	final public static String SCHEME = "http";
 	final public static String FORMAT = "json";
 	
 	final public static String GETSTATIONS = "stops";
 	final public static String GETROUTE = "get_route";
 	final public static String LOGIN = "login";
 	final public static String SIGNUP = "signup";
 	final public static String GETCARDSBYID = "getCardsByUserId";
 	final public static String ADDCARD = "addCardToUser";
 	final public static String REMOVECARD = "removeCard";
 	final public static String VERIFYTICKET = "verifyTicket";
 	final public static String SIMPLE_ROUTE = "SIMPLE_ROUTE";
 	final public static String DUAL_ROUTE_OTHER_DAY = "DUAL_ROUTE_OTHER_DAY";
 	final public static String DUAL_ROUTE_SAME_DAY = "DUAL_ROUTE_SAME_DAY";
 	final public static String GETTICKETSBYID = "getTicketsByUserId";
 	final public static String BUYTICKET = "newTicket";
 	final public static String ADDTICKETROUTES = "addTicketRoutes";
 	final public static String CANCELTICKET = "cancelTicket";
 	final public static String PAYTICKET = "payTicket";
 	final public static String GETPAIDTICKETSBYID = "getPaidTicketsByUserId";
 	
 	public static String username;
 	public static String name;
 	public static int userId = 0;
 	public static boolean cardsLoaded = false;
 	public static DatabaseHelper databaseHelper;
 	
 	
 
 	final public static String QRCODE = "https://chart.googleapis.com/chart?chs=250x250&cht=qr&chl=";
 	final public static String THE_TRAIN_PROJECT_DB = "TheTrainProject.db";
 	
 }
