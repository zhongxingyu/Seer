 package bot;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import jerklib.*;
 import jerklib.events.*;
 import jerklib.events.IRCEvent.*;
 import jerklib.listeners.*;
 import bot.Boot;
 
 public class TinkerHelper implements IRCEventListener {
 	
 	/**
 	 * Constants
 	 */
 	private static final String BOT_NAME = Boot.botname;
 	private static final String NS_PASSWORD = "th-bot123";
 	private static final String SERVER = "irc.geekshed.net";
 	private static final String CHANNEL = "#tinkernut_test_room";
 	private static final String BADWORDS = "fuck fucked fucking suck sucked sucking bitch bitched bitching slut cunt shit poo pee penis cock dick pussy vagina ass badword badword1 badword2";
 	private static final String[] GREETINGS = {"Ahoy, %s", "Howdy, %s", "Welcome, %s",
 		"Hello, %s", "Hi, %s", "Sup, %s", "OoOooOo, %s", "%s!!!!"};
 	private static final String FAQ1 = "what screen recording do you use,how do you record your screen,what screen recording software do you use,what kind of screen recording do you use,what kind of screen recording software do you use";
 	private static final String saystr = null;
 	/**
 	 * Globals
 	 */
 	private ConnectionManager con;
 	private Channel channel;
 	private String command_char;
 	private ArrayList<String> adminList;
 	private String[] badwords;
 	private String[] faq1;
 	private HashMap<String, User> users;
 	private Timer announcer;
 	private Timer funtime;
 	private String announcerStr;
 	private long announcerPeriod;
 	private boolean announcerRunning;
 	private ArrayList<String> awayList;
 	private ArrayList<String> idleList;
 	private ArrayList<String> readList;
 	private boolean fun_on;
 	private boolean greet;
 	private Integer readcount;
 	private String reason;
 //	private Channel chtn;
 	
 	/**
 	 * Class constructor
 	 */
 	public TinkerHelper() {
 		// Initialize globals
 		con = new ConnectionManager(new Profile(BOT_NAME));
 		command_char = "+";
 		adminList = new ArrayList<String>();
 		adminList.add("Buzzbyte");
 		badwords = BADWORDS.split(" ");
 		faq1 = FAQ1.split(",");
 		users = new HashMap<String, User>();
 		announcer = null;
 		announcerStr = null;
 		announcerPeriod = -1;
 		announcerRunning = false;
 		fun_on = false;
 		greet = true;
 		awayList = new ArrayList<String>();
 		idleList = new ArrayList<String>();
 		readList = new ArrayList<String>();
 		reason = null;
 //		chtn = channel.;
 		readcount = 0;
 		
 		// Start TinkerBot
 		con.requestConnection(SERVER).addIRCEventListener(this);
 	}
 
 	@Override
 	/**
 	 * Callback whenever the Bot receives data.
 	 */
 	public void receiveEvent(IRCEvent e) {
 		Type type = e.getType();
 		// Connects to server successfully
 		if (type == Type.CONNECT_COMPLETE) {
 			e.getSession().join(CHANNEL);
 			//e.getSession().join("#tinkernut_test_room");
 			//e.getSession().join("#techisized");
 			//e.getSession().join("#willthecomputergeek");
 			//e.getSession().join("#tinkernutwiki");
 		// Connects to channel successfully
 		} else if (type == Type.JOIN_COMPLETE) {
 			MessageEvent me = (MessageEvent) e;
 			channel = ((JoinCompleteEvent) e).getChannel();
 //			me.getChannel().command("PRIVMSG Buzzbyte Identified!");
 		// User joins channel
 		} else if (type == Type.JOIN) {
 			JoinEvent je = (JoinEvent) e;
 			MessageEvent me = (MessageEvent) e;
 			System.out.println(je.getChannelName() + ": "+ je.getNick() + " has joined.");
 			String msg = me.getMessage().toLowerCase();
 //			if (greet = true) {
 //			je.getChannel().say(String.format(GREETINGS[new Random().nextInt(GREETINGS.length)], je.getNick()));
 //			} else if (greet = false) {
 				// Don't do anything!
 //				return;
 //			}
 				if (je.getNick().equals("gigafide")) {
 					je.getChannel().say("Gigafide!!! May I have your autograph?");
 					if (readcount == 0) {
 						je.getChannel().say("You have no messages!");
 					} else if (readcount != 0) {
 						je.getChannel().say("You have " + readcount +" new messages! Type '+askgig viewmsgs' to view them!");
 					}
 				}
 				if (msg.equals("hello?")) {
 					je.getChannel().say("Hello!");
 				}
 		// User leaves channel
 		} else if (type == Type.QUIT) {
 			QuitEvent qe = (QuitEvent) e;
 			MessageEvent me = (MessageEvent) e;
 			System.out.println(qe.getNick() + " has left (" + qe.getQuitMessage() + ").");
 			users.remove(qe.getNick());
 			
 		// Receives message in channel
 		} else if (type == Type.CHANNEL_MESSAGE) {
 			MessageEvent me = (MessageEvent) e;
 			System.out.println(me.getChannel().getName() + ": <" + me.getNick() + ">"+ " " + me.getMessage());
 			String msg = me.getMessage();
 			User user = loadUser(me.getNick());
 			boolean found = true;
 			
 			if (isCommand(msg)) {
 				if (isAdmin(me.getNick())) {
 					String cmd = getCommand(msg);
 					if (cmd.equals("help"))
 						cmdHelp(me);
 					else if (cmd.equals("kick"))
 						cmdKick(me);
 					else if (cmd.equals("admins"))
 						cmdAdmins(me);
 					else if (cmd.equals("add"))
 						cmdAdd(me);
 					else if (cmd.equals("rem"))
 						cmdRem(me);
 					else if (cmd.equals("roll"))
 						cmdRoll(me);
 					else if (cmd.equals("announcer"))
 						cmdAnnounce(me);
 					else if (cmd.equals("say"))
 						cmdsay(me);
 					else if (cmd.equals("saypriv"))
 						cmdsaypriv(me, user, e);
 					else if (cmd.equals("chnick"))
 						cmdchnick(me);
 					else if (cmd.equals("quit"))
 						cmdquit();
 					else if (cmd.equals("fun-on"))
 						fun_on(me);
 					else if (cmd.equals("fun-off"))
 						fun_off(me);
 					else if (cmd.equals("act"))
 						actions(me);
 					else if (cmd.equals("talent"))
 						talent(me);
 					else if (cmd.equals("askgig"))
 						msgs(me, user);
 					else if (cmd.equals("wiki"))
 						wiki(me);
 					else if (cmd.equals("saytn"))
 						saytn(me);
 					else if (cmd.equals("tncycle"))
 						cycle(me);
 					else if (cmd.equals("cmd"))
 						scmd(me);
 					else if (cmd.equals("google"))
 						google(me);
 					else if (cmd.equals("googlefirst"))
 						googlefirst(me);
 					else
 						found = false;
 				}
 				String cmd = getCommand(msg);
 				if (cmd.equals("away"))
 					cmdAway(me);
 				else if (cmd.equals("idle"))
 					cmdIdle(me);
 				else if (!found)
 					if (fun_on = false) {
 					me.getChannel().say("Unknown command.");
 					} else {
 						fun(me);
 					}
 			} else {
 				checkLanguage(me, user);
 				checkAway(me);
 				answerfaq(me, user);
 	//			asktime(me,user);
 				chatrules(me, user);
 				checkIdle(me);
 	//			fun(me);
 				gtgbrb(me, user);
 				greet(me);
 				msgs(me, user);
 			}
 		} else if (type == Type.PRIVATE_MESSAGE) {
 			MessageEvent me = (MessageEvent) e;
 			System.out.println("Private: <" + me.getNick() + ">"+ " " + me.getMessage());
 			String msg = me.getMessage();
 			me.getSession().sayChannel(me.getSession().getChannel("#tinkernut_test_room"), msg);
 //			if (msg == "control") {
 	//			String cmd = msg.substring(8);
 		//		String quote = msg.substring(12);
 			//	if (cmd == "say") {
 				//	me.getSession().getChannel(CHANNEL).say(quote);
 //				} else if (cmd == "act") {
 	//				me.getSession().getChannel(CHANNEL).action(quote); 
 		//		} else {
 			//		me.getSession().sayPrivate(me.getNick(), "Unknown command. Please use the command 'control' to control a channel privately.");
 				//}
 //			} else if (msg == "chnick") {
 	//			String newnick = msg.substring(7);
 		//		me.getSession().changeNick(newnick);
 			//}
 		}
 	}
 	
 	//==========================================================================
 	// Commands
 	//==========================================================================
 	
 	/**
 	 * Help command
 	 */
 	private void cmdHelp(MessageEvent me) {
 //		me.getChannel().say("Command turned off (Reason: )");
 		me.getSession().notice("Buzzbyte", "List of commands:");
 		me.getSession().notice(me.getNick(), " ");
 		me.getSession().notice(me.getNick(), "+kick <nick> <reason> - Kicks a 'nick' with a 'reason'");
 		me.getSession().notice(me.getNick(), "+admins - Views a list of users who can control the bot");
 		me.getSession().notice(me.getNick(), "+add <nick> - Adds a 'nick' to control the bot");
 		me.getSession().notice(me.getNick(), "+rem <nick> - Removes a 'nick' from control the bot");
 		me.getSession().notice(me.getNick(), "+roll - Rolls a random number");
 		me.getSession().notice(me.getNick(), "+announcer <<time text>|'start'|'stop'> - Command used to announce a 'text' every 'time' | Starts announcer | Stops announcer");
 		me.getSession().notice(me.getNick(), "+say <text> - Makes the bot say 'text'");
 		me.getSession().notice(me.getNick(), "+chnick <new nick> - Makes the bot change its nick to a 'new nick'");
 		me.getSession().notice(me.getNick(), "+quit - Makes the bot quit");
 		me.getSession().notice(me.getNick(), "+act <text> - Makes the bot use action (the /me command) for the 'text'");
 		me.getSession().notice(me.getNick(), "+talent - Makes the bot show it's talent");
 		me.getSession().notice(me.getNick(), "+askgig - Coming Soon!");
 		me.getSession().notice(me.getNick(), " ");
 		me.getSession().notice(me.getNick(), "== End of CMD help ==");
 	}
 	
 	/**
 	 * Kick command
 	 */
 	private void cmdKick(MessageEvent me) {
 		String msg = me.getMessage(),
 			nick = null,
 			reason = null;
 		int start = msg.indexOf(' ') + 1;
 		if (start == -1) {
 			me.getChannel().say("A username is required!");
 			return;
 		}
 		int space = msg.indexOf(' ', start);
 		if (space != -1) {
 			nick = msg.substring(start, space);
 			reason = msg.substring(space + 1);
 		} else {
 			nick = msg.substring(start);
 			reason = "";
 		}
 		
 		me.getChannel().kick(nick, reason);
 	}
 	
 	/**
 	 * Admins command
 	 */
 	private void cmdAdmins(MessageEvent me) {
 		StringBuilder builder = new StringBuilder();
 		for (String admin : adminList) {
 			builder.append(admin);
 			builder.append(" ");
 		}
 		me.getChannel().say(builder.toString());
 	}
 	
 	/**
 	 * Add command
 	 */
 	private void cmdAdd(MessageEvent me) {
 		String msg = me.getMessage(),
 			nick = msg.substring(command_char.length() + 4);
 		if (isAdmin(nick))
 			return;
 		nick = getTrueName(me.getChannel(), nick);
 		if (nick == null) {
 			me.getChannel().say("Unknown username!");
 			return;
 		}
 		
 		adminList.add(nick);
 		me.getChannel().say("User " + nick + " was added!");
 	}
 	
 	/**
 	 * Rem command
 	 */
 	private void cmdRem(MessageEvent me) {
 		String msg = me.getMessage(),
 				nick = msg.substring(command_char.length() + 4);
 		if (!isAdmin(nick))
 			return;
 		if (nick == "Buzzbyte") {
 			me.getChannel().say("Cannot remove username!");
 		}
 		nick = getTrueName(me.getChannel(), nick);
 		adminList.remove(nick);
 		me.getChannel().say("User " + nick + " was removed!");
 	}
 	
 	/**
 	 * Roll command
 	 */
 	private void cmdRoll(MessageEvent me) {
 		me.getChannel().say("" + (new Random().nextInt(100) + 1));
 	}
 	
 	/**
 	 * Announce command
 	 */
 	private void cmdAnnounce(MessageEvent me) {
 		String msg = me.getMessage();
 		if (msg.equals(command_char + "announcer start")) {
 			if (announcerRunning) {
 				me.getChannel().say("Announcer is already running!");
 				return;
 			}
 			announcer = new Timer();
 			announcer.scheduleAtFixedRate(new TimerTask() {
 				@Override
 				public void run() {
 					channel.say(announcerStr);
 				}
 			}, 0, announcerPeriod);
 			announcerRunning = true;
 			me.getChannel().say("Announcer started!");
 		} else if (msg.equals(command_char + "announcer stop")) {
 			if (!announcerRunning) {
 				me.getChannel().say("Announcer is not running!");
 				return;
 			}
 			announcer.cancel();
 			announcer.purge();
 			announcerRunning = false;
 			me.getChannel().say("Announcer stopped!");
 		} else {
 			if (announcerRunning) {
 				me.getChannel().say("Announcer is already running!");
 				return;
 			}
 			int start = msg.indexOf(' ') + 1;
 			int end = msg.indexOf(' ', start);
 			try {
 				announcerPeriod = Integer.parseInt(msg.substring(start, end)) * 1000;
 				announcerStr = msg.substring(end + 1);
 				me.getChannel().say("Announcer configured, use \""+ command_char + "announcer start\" to start annoucing!");
 			} catch (Exception e) {
 				me.getChannel().say("Wrong syntax!");
 			}
 		}
 	}
 	
 	/**
 	 * Away command
 	 */
 	private void cmdAway(MessageEvent me) {
 		reason = me.getMessage().substring(6);
 		boolean found = false;
 		for (String nick : awayList) {
 			if (me.getNick().equals(nick)) {
 				found = true;
 			}
 		}
 		if (!found) {
 			awayList.add(me.getNick());
 			me.getChannel().say(me.getNick() + " is now tagged as 'away' (" + reason + ")");
 		}
 	}
 	
 	/**
 	 * Away checker
 	 */
 	private void checkAway(MessageEvent me) {
 		String msg = me.getMessage().toLowerCase();
 		for (int i = 0; i < awayList.size(); i++) {
 			String nick = awayList.get(i);
 			// Check if msg sender is nick
 			if (nick.equals(me.getNick())) {
 				awayList.remove(nick);
 				me.getChannel().say(me.getNick() + " is now back!");
 				reason = null;
 			}
 			// Check if nick is contained in msg
 			if (msg.contains(nick.toLowerCase())) {
 				if (reason.equals(null)) {
 					me.getChannel().say(nick + " is currently away (No reason)");
 				} else {
 					me.getChannel().say(nick + " is currently away (" + reason + ")");
 				}
 			}
 			
 		}
 	}
 	
 	/**
 	 * Language checker
 	 */
 	private void checkLanguage(MessageEvent me, User user) {
 	String msg = me.getMessage().toLowerCase();
 	int count = 0;
 	for (String word : msg.split(" ")) {
 		for (String badword : badwords) {
 			if (word.toLowerCase().equals(badword)) {
 				count++;
 			}
 		}
 	}
 	if (count > 0) {
 		user.warnings++;
 		if (user.warnings == 1) {
 			me.getChannel().say(me.getNick() + ": please watch your language!");
 		} else if (user.warnings >= 2) {
 			me.getChannel().kick(me.getNick(), "Language");
 		}
 	}
 	}
 	
 	private void answerfaq(MessageEvent me, User user) {
 		String msg = me.getMessage().toLowerCase();
 		
 		for (String q1 : faq1) {
 			if (msg.contains(q1)) {
 				me.getChannel().say(me.getNick() + ": " + "Gigafide began using Camstudio for his first videos, then he used Camtasia 6 for later videos. He does not curently use any screen recording software, Instead he uses screen shots and animate them using Adobe After Effects.");
 			}
 		}
 	}
 	
 	//private void asktime(MessageEvent me, User user) {
 	//	String msg = me.getMessage().toLowerCase();
 		
 	//		if (msg.contains("what time is it")) {
 	//			me.getChannel().say(me.getNick() + ": " + time());
 	//		}
 	//}
 	
 	private void chatrules(MessageEvent me, User user) {
 		String msg = me.getMessage().toLowerCase();
 		
 			if (msg.contains("chat rules")) {
 				me.getChannel().say("Here are the chat rules: http://www.tinkernut.com/wiki/page/Chat_rules");
 			}
 	}
 	
 	private void cmdsay(MessageEvent me) {
 		String msg = me.getMessage();
 		String msg2 = msg.replace("+say ", "");
 				me.getChannel().say(msg2);
 	}
 	
 	private void cmdsaypriv(MessageEvent me, User user, IRCEvent e) {
 //		String msg = me.getMessage();
 //		String msg2 = msg.replace("+saypriv ", "");
 //		String nick = me.getMessage().substring(0);
 		e.getSession().sayPrivate("Buzzbyte", "Hello!!!!");
 //		me.getChannel().say("/me is saying private!");
 	}
 	
 	private void cmdchnick(MessageEvent me) {
 		String msg = me.getMessage().substring(8);
 		me.getSession().changeNick(msg);
 	}
 	
 	private void cmdquit() {
 		con.quit("See Ya later!");
 		System.exit(0);
 	}
 	
 	private void cmdIdle(MessageEvent me) {
 		reason = me.getMessage().substring(6);
 		boolean found = false;
 		for (String nick : idleList) {
 			if (me.getNick().equals(nick)) {
 				found = true;
 			}
 		}
 		if (!found) {
 			idleList.add(me.getNick());
 			me.getChannel().say(me.getNick() + " is now tagged as 'Idle' (" + reason + ")");
 		}
 	}
 	
 	/**
 	 * Idle checker
 	 */
 	private void checkIdle(MessageEvent me) {
 		String msg = me.getMessage().toLowerCase();
 		for (int i = 0; i < idleList.size(); i++) {
 			String nick = idleList.get(i);
 			// Check if msg sender is nick
 			if (nick.equals(me.getNick())) {
 				idleList.remove(nick);
 				me.getChannel().say(me.getNick() + " is now back!");
 				reason = null;
 			}
 			// Check if nick is contained in msg
 			if (msg.contains(nick.toLowerCase())) {
 				if (reason.equals("")) {
 					me.getChannel().say(nick + " is currently idle (No reason)");
 				} else {
 					me.getChannel().say(nick + " is currently idle (" + reason + ")");
 				}
 			}
 		}
 	}
 	
 	private void fun(MessageEvent me) {
 		String msg = me.getMessage();
 		String person = me.getMessage().substring(18);
 		if (fun_on = true) {
 			if (msg.equals("+get me some lemonade")) {
 				me.getChannel().say("Yes sir!");
 				me.getChannel().say("Here you go sir!");
 				me.getChannel().action("hands " + me.getNick() + " some lemonade");
 			} else if (msg.equals("+get buzz some lemonade")) {
 				me.getChannel().say("Yes sir!");
 				me.getChannel().say("Here you go Buzzbyte!");
 				me.getChannel().action("hands Buzzbyte some lemonade");
 			} else if (msg.equals("+get buzzbyte some lemonade")) {
 				me.getChannel().say("Yes sir!");
 				me.getChannel().say("Here you go buzzbyte!");
 				me.getChannel().action("hands Buzzbyte some lemonade");
 			} else if (msg.equals("+get lemonade for " + person)) {
 				me.getChannel().say("Yes sir!");
 				me.getChannel().say("Here is your lemonade " + person);
 				me.getChannel().action("hands " + person + " some lemonade");
 			}
 		} else {
 			me.getChannel().say("Fun command unknown!");
 		}
 	}
 	private void fun_on(MessageEvent me) {
 			fun_on = true;
 			me.getChannel().say("Fun commands are now on!");
 	}
 	private void fun_off(MessageEvent me) {
 			fun_on = false;
 			me.getChannel().say("Fun commands are now off!");
 	}
 	private void gtgbrb(MessageEvent me, User user) {
 		String msg = me.getMessage().toLowerCase();
 //		if (msg.contains("brb")) {
 //			me.getChannel().say("See you when you're back!");
 //		}
 //		if (msg.contains("gtg")) {
 //			me.getChannel().say("See you later, alligator!");
 //		}
 //		if (msg.equals("back")) {
 //			me.getChannel().say("Welcome back!");
 //		}
 //		if (msg.equals("back!")) {
 //			me.getChannel().say("Welcome back!");
 //		}
 //		if (msg.equals("back!!")) {
 //			me.getChannel().say("Welcome back!");
 //		}
 //		if (msg.equals("back!!!")) {
 //			me.getChannel().say("Welcome back!");
 //		}
 //		if (msg.contains("bye")) {
 //			me.getChannel().say("Goodbye!");
 //		}
 		
 	}
 	private void greet(MessageEvent me) {
 		String msg = me.getMessage().toLowerCase();
 		if (msg.equals("greetoff")) {
 			if (greet = false) {
 				me.getChannel().say("Greetings are already off!");
 			} else if (greet = true) {
 				greet = false;
 				me.getChannel().say("Greetings are now off!");
 			}
 		} else if (msg.equals("greeton")) {
 			if (greet = true) {
 				me.getChannel().say("Greetings are already oon!");
 			} else if (greet = false) {
 				greet = true;
 				me.getChannel().say("Greetings are now on!");
 			}
 		}
 	}
 
 	private void actions(MessageEvent me) {
 		String msg = me.getMessage().substring(5);
 		me.getChannel().action(msg);
 	}
 	private void talent(MessageEvent me) {
 		me.getChannel().say(">>");
 		me.getChannel().say(">>>>");
 		me.getChannel().say(">>>>>>");
 		me.getChannel().say(">>>>>>>>");
 		me.getChannel().say(">>>>>>>>>>");
 		me.getChannel().say(">>>>>>>>>>>>");
 		me.getChannel().say(">>>>>>>>>>");
 		me.getChannel().say(">>>>>>>>");
 		me.getChannel().say(">>>>>>");
 		me.getChannel().say(">>>>");
 		me.getChannel().say(">>");
 	}
 	private void msgs(MessageEvent me, User user) {
 //			me.getChannel().say("Coming Soon!");
 			String msg = me.getMessage();
 			String msg2 = me.getMessage().substring(8);
 			readcount = 0;
 			for (String badword : badwords) {
 				if (msg.equals("-askgig")){
 					me.getChannel().say("Please enter a question!");
 					return;
 				} else if (msg.equals("-askgig ")){
 					me.getChannel().say("Please enter a question!");
 					return;
 				} else if (msg.equals("-askgig  ")){
 					me.getChannel().say("Please enter a question!");
 					return;
 //				} else if (msg.contains(badword)) {
 //					me.getChannel().say("Failed to complete operation!");
 //					return;
 				} else if (msg.equals("-askgig viewmsgs")) {
 					me.getChannel().say(prettyPrint(readList));
 					return;
 				} else if (msg.equals("-askgig " + msg2)) {
 					readList.add(msg2 + " - " + me.getNick());
 					readcount++;
 					me.getChannel().say("Your question is now added to the list!");
 					return;
 				}
 			}
 	}
 	private void wiki(MessageEvent me) {
 		String msg = me.getMessage().substring(6);
 		me.getChannel().say("Here is the " + msg + " page: http://www.tinkernut.com/wiki/page/" + msg);
 	}
 	private void saytn(MessageEvent me) {
 		String msg = me.getMessage().substring(7);
 		me.getSession().sayChannel(channel.getSession().getChannel(CHANNEL), msg);
 	}
 	
 	private void cycle(MessageEvent me) {
 		me.getChannel().part("");
 		me.getSession().join("#tinkernut");
 	}
 	private void scmd(MessageEvent me) {
 		String msg = me.getMessage().substring(5);
 		me.getChannel().command(msg);
 		me.getChannel().say("Server command applied!");
 	}
 	private void google(MessageEvent me) {
 		String msg = me.getMessage().substring(7);
 		if (msg == "") {
 			me.getChannel().say("What should I Google?");
 		} else {
 			String googlesearch = me.getMessage().substring(8);
 			String googlesearchurl = googlesearch.replace(" ", "+");
 			
 			me.getChannel().say("Google search results for " + googlesearch + ": http://google.com/search?q=" + googlesearchurl);
 		}
 	}
 	private void googlefirst(MessageEvent me) {
 		String googlesearch = me.getMessage().substring(13);
 		String msg = me.getMessage().substring(12);
 		if (msg == "") {
 			me.getChannel().say("What should I Google?");
 		} else {
 			String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
 		    String search = googlesearch;
 		    String charset = "UTF-8";
 
 	//	    URL url = new URL(google + URLEncoder.encode(search, charset));
 //		    Reader reader = new InputStreamReader(url.openStream(), charset);
 //		    GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);
 
 		    // Show title and URL of 1st result.
 		//    me.getChannel().say(results.getResponseData().getResults().get(0).getTitle() + " - " + results.getResponseData().getResults().get(0).getUrl());
 		}
 	}
 	//==========================================================================
 	// Utilities
 	//==========================================================================
 	
 	/**
 	 * Loads user handle
 	 */
 	private User loadUser(String key) {
 		if (users.get(key) == null) {
 			users.put(key, new User());
 		}
 		return users.get(key);
 	}
 	
 	/**
 	 * Checks if the given nick is an admin
 	 */
 	private boolean isAdmin(String nick) {
 		nick = nick.toLowerCase();
 		for (String admin : adminList) {
 			if (nick.equals(admin.toLowerCase()))
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks if the given string is a command
 	 */
 	private boolean isCommand(String msg) {
 		return msg.startsWith(command_char);
 	}
 	
 	/**
 	 * Extracts the command from the message
 	 */
 	private String getCommand(String msg) {
 		int end = (msg.indexOf(' ') == -1 ? msg.length() : msg.indexOf(' '));
 		return msg.substring(command_char.length(), end);
 	}
 	
 	/**
 	 * Returns the exact name of someone, for instance GigaFide would return gigafide
 	 */
 	private String getTrueName(Channel ch, String str) {
 		str = str.toLowerCase();
 		for (String nick : ch.getNicks()) {
 			if (str.equals(nick.toLowerCase()))
 				return nick;
 		}
 		return null;
 	}
 	
 	/**
 	 * Joins a List of string to make it pretty print
 	 */
 	private String prettyPrint(List<String> list) {
 		StringBuilder builder = new StringBuilder();
 		builder.append("[");
 		for (int i = 0; i < list.size(); i++) {
 			builder.append("\"");
 			builder.append(list.get(i));
 			builder.append("\"");
 			if (i < list.size() - 1)
 				builder.append(",");
 		}
 		builder.append("]");
 		return builder.toString();
 	}
 
 }
