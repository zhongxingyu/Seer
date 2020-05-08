 package com.googlecode.prmf.huabot;
 
 import static com.googlecode.prmf.merapi.net.irc.IrcCommands.privmsg;
 import static com.googlecode.prmf.merapi.util.Iterators.iterator;
 import static com.googlecode.prmf.merapi.util.Iterators.lines;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Random;
 import java.util.ResourceBundle;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.googlecode.prmf.merapi.net.irc.Entity;
 import com.googlecode.prmf.merapi.net.irc.IrcClient;
 import com.googlecode.prmf.merapi.net.irc.cmd.JoinCommand;
 import com.googlecode.prmf.merapi.net.irc.cmd.PrivmsgCommand;
 import com.googlecode.prmf.merapi.net.irc.event.AbstractIrcEventListener;
 import com.googlecode.prmf.merapi.net.irc.event.IrcEvent;
 import com.googlecode.prmf.merapi.net.www.Tinysong;
 import com.googlecode.prmf.merapi.net.www.TinysongResult;
 import com.googlecode.prmf.merapi.util.Pair;
 import com.googlecode.prmf.merapi.util.Strings;
 
 public class Huabot extends AbstractIrcEventListener {
 	private static final String COMMAND_TRIGGER = "~"; // what a message must start with to be considered a command
 	private static final Pattern COMMAND_PATTERN = Pattern.compile("\\s*" + Pattern.quote(COMMAND_TRIGGER) + "\\s*(\\w+)\\s*(.*)", Pattern.DOTALL);
 	
 	private static final String KARMA_FILE = "karma.txt";
 	private static final List<Pair<Pattern,Integer>> KARMA_PATTERNS = new ArrayList<Pair<Pattern,Integer>>();
 	static {
 		String entityRegex = "(\\w+)";
 		String[] modifiers = {"+", "-"};
 		int[] dks = {1, -1};
 		
 		for(int i = 0; i < modifiers.length; ++i) {
 			Integer dk = Integer.valueOf(dks[i]);
 			String modifier = "(?:" + Pattern.quote(modifiers[i]) + "){2,}";
 			
 			KARMA_PATTERNS.add(new Pair<Pattern,Integer>(Pattern.compile("(?<!\\S)" + modifier + entityRegex), dk)); // pre-notation
 			KARMA_PATTERNS.add(new Pair<Pattern,Integer>(Pattern.compile(entityRegex + modifier + "(?!\\S)"), dk)); // post-notation
 		}
 	}
 	
 	private final Map<String,Integer> karma;
 	
 	/**
 	 * Default constructor.
 	 */
 	public Huabot() {
 		this.karma = readMapFromFile(KARMA_FILE);
 	}
 
 	@Override
 	public void joinEvent(IrcEvent<JoinCommand> event) {
 		IrcClient client = event.getClient();
 		JoinCommand cmd = event.getCommand();
 
 		String nick = client.getDesiredNick(); // We're assuming that the desired nick and actual nick are the same... Not good.
 		String channel = cmd.getChannel().toLowerCase(Locale.ENGLISH);
 		String user = event.getOrigin().getNick();
 
 		if(!nick.equalsIgnoreCase(user)) { // Otherwise we'd welcome ourselves!
 			double rnd = new Random().nextDouble();
 			if(rnd <= 0.85) {
 				privmsg(client, channel, String.format("Welcome to %s, %s!", channel, user));
 			}
 			else if(rnd <= 0.95) {
 				privmsg(client, channel, String.format("Welcome, %s, to %s!", user, channel));
 			}
 			else {
				privmsg(client, channel, String.format("WELCOME TO %s %s!!!!!!!!", channel.toUpperCase(Locale.ENGLISH), user.toUpperCase(Locale.ENGLISH)));
 			}
 		}
 	}
 
 	@Override
 	public void privmsgEvent(IrcEvent<PrivmsgCommand> event) {
 		IrcClient client = event.getClient();
 		PrivmsgCommand cmd = event.getCommand();
 
 		String nick = client.getDesiredNick(); // Again, unsafely assuming that the desired nick and actual nick are the same.
 
 		// The "target" of the message is either a channel, or our nickname, in the case of a direct message.
 		String target = cmd.getTarget();
 		if(nick.equalsIgnoreCase(target)) {
 			// direct message
 		}
 		else {
 			// message in a channel
 			reactToMessage(client, target, event.getOrigin(), cmd.getMessage());
 		}
 	}
 
 	private int getKarma(String entity) {
 		Integer k = this.karma.get(entity);
 		return k == null ? 0 : k.intValue();
 	}
 
 	private void setKarma(String entity, int karma) {
 		this.karma.put(entity, Integer.valueOf(karma));
 	}
 
 	private void changeKarma(String entity, int changeInKarma) {
 		setKarma(entity, getKarma(entity) + changeInKarma);
 	}
 	
 	private void reactToMessage(IrcClient client, String channel, Entity sender, String message) {
 		Matcher cmdMatcher = COMMAND_PATTERN.matcher(message);
 
 		if(cmdMatcher.matches()) {
 			// We got ourselves a command!
 			String cmd = cmdMatcher.group(1).toLowerCase(Locale.ENGLISH);
 			String[] arg = cmdMatcher.group(2).trim().split("\\s+");
 
 			if(cmd.equals("help")) {
 				privmsg(client, channel, "My commands are: date, help, karma, time, tinysong, version");
 			}
 			
 			if(cmd.equals("version")) {
 				ResourceBundle rb = ResourceBundle.getBundle(Huabot.class.getPackage().getName() + ".Info");
 				String version = rb.getString("huabot.version");
 				privmsg(client, channel, "This is huabot, version " + version + ".");
 			}
 
 			if(cmd.equals("date")) {
 				privmsg(client, channel, String.format("Today's date is %s.",
 						DateFormat.getDateInstance(DateFormat.LONG).format(new Date())));
 			}
 
 			if(cmd.equals("time")) {
 				privmsg(client, channel, String.format("The current time is %s.",
 						DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date())));
 			}
 
 			if(cmd.equals("tinysong")) {
 				Tinysong ts = new Tinysong();
 
 				String response = null;
 				if(arg.length == 0 || arg[0].length() == 0)
 					response = "Some search terms would be nice.";
 				else {
 					String query = Strings.join(" ", arg);
 					try {
 						TinysongResult result = ts.topResult(query);
 						if(result != null)
 							response = String.format("Top result for \"%s\": %s by %s <%s>",
 									query, result.getSongName(), result.getArtistName(), result.getUrl());
 						else
 							response = String.format("There were no search results for \"%s\" :/", query);
 					}
 					catch(Exception e) {
 						response = "There was an error. " + e.getMessage();
 					}
 				}
 
 				privmsg(client, channel, response);
 			}
 			
 			if(cmd.equals("karma")) {
 				if(arg.length == 0 || arg[0].length() == 0) {
 					Pair<String,String> topAndBottomKarma = topAndBottomKarma();
 					String top = topAndBottomKarma.getFirst();
 					String bottom = topAndBottomKarma.getSecond();
 					if(top == null)
 						privmsg(client, channel, "There is no registered karma!");
 					else
 						privmsg(client, channel, String.format("Karma extremes: top is %s with %d, bottom is %s with %d.",
 								top, Integer.valueOf(getKarma(top)), bottom, Integer.valueOf(getKarma(bottom))));
 				}
 				else {
 					String entity = arg[0];
 					privmsg(client, channel, String.format("%s has %d karma.", entity, Integer.valueOf(getKarma(entity))));
 				}
 			}
 			
 		}
 		else {
 			// This was not a command. Do other kinds of text processing.
 			String nick = sender.getNick();
 
 			boolean responded = false; // whether or not we reacted to the message
 			
 			// Process karma!
 			boolean karmaUpdated = false;
 			boolean reprimandedForSelfPlus = false; // Don't self plus! It's bad karma.
 			for(Pair<Pattern,Integer> pair: KARMA_PATTERNS) {
 				int dk = pair.getSecond().intValue();
 				Matcher karmaMatcher = pair.getFirst().matcher(message);
 				while(karmaMatcher.find()) {
 					String karmaTarget = karmaMatcher.group(1);
 					boolean change = true;
 					
 					// Self-plus is case insensitive, rest isn't anymore.
 					if(dk > 0 && karmaTarget.equalsIgnoreCase(nick)) {
 						if(!reprimandedForSelfPlus) {
 							privmsg(client, channel, nick + ": It's not cool to self-plus.");
 							reprimandedForSelfPlus = true;
 							responded = true;
 						}
 						change = false;
 					}
 
 					if(change) {
 						changeKarma(karmaTarget, dk);
 						karmaUpdated = true;
 					}
 				}
 			}
 			if(karmaUpdated)
 				writeMapToFile(this.karma, KARMA_FILE);
 			
 			if(!responded) { // Well maybe we should!
 				// It was empirically determined that 1% of messages are zing-worthy.
 				if(Math.random() <= 0.01)
 					privmsg(client, channel, "zing~");
 			}
 		}
 	}
 
 	private void writeMapToFile(Map<String,Integer> map, String filename) {
 		try {
 			PrintStream out = new PrintStream(new File(filename));
 			for(Entry<String,Integer> entry: iterator(map)) {
 				String entity = entry.getKey();
 				int karma = entry.getValue().intValue();
 				out.println(entity + " " + karma);
 			}
 		}
 		catch(FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private Map<String,Integer> readMapFromFile(String filename) {
 		Map<String,Integer> ret = new HashMap<String,Integer>();
 
 		try {
 			for(String line: lines(new File(filename))) {
 				String[] arr = line.split("\\s+");
 				ret.put(arr[0], Integer.valueOf(Integer.parseInt(arr[1])));
 			}
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		return ret;
 	}
 	
 	private Pair<String,String> topAndBottomKarma() {
 		String top = null;
 		String bottom = null;
 		int topKarma = 0;
 		int bottomKarma = 0;
 		
 		for(Entry<String,Integer> entry: iterator(this.karma)) {
 			String entity = entry.getKey();
 			int karma = entry.getValue().intValue();
 			if(top == null || karma > topKarma) {
 				top = entity;
 				topKarma = karma;
 			}
 			if(bottom == null || karma < bottomKarma) {
 				bottom = entity;
 				bottomKarma = karma;
 			}
 		}
 		
 		return new Pair<String,String>(top, bottom);
 	}
 }
