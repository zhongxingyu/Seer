 /*
  * @author Raymond Hammarling
  * @description This module lets you set reminders for a task after a specified time to anyone
  * @basecmd remind
  * @category utility
  */
 package commands;
 
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import lombok.Data;
 import lombok.NonNull;
 
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 import org.pircbotx.hooks.events.NickChangeEvent;
 
 import backend.Bot;
 import backend.Database;
 import backend.TimerThread;
 import backend.Util;
 
 public class ReminderCommand extends Command {
 	private Map<String, Long> wordAmount;
 	
 	private static Map<Reminder, ScheduledFuture<?>> reminders;
 	
 	private static String[] backInTimeLines = {
 		"You think this is *%#@ing Back To The Future?",
 		"What the *%#@ do you think this is? Steins;Gate?",
 		"Flux Capacitors are yet to be mass-produced...",
 		"Yeah, I'll just, y'know, totally send you a message back in time. Not a problem.",
 		"You're supposed to specify sometime in the " + Colors.BOLD + "future" + Colors.NORMAL + ", not the past!",
 		"ERROR: Unable to send D-Mail: Lifter is missing",
 		"Phone Microwave currently out of function, cannot send D-Mail",
 		"If I could time travel, I'd remind %TARGET% to %TASK% %TIME% ago. But I can't!",
 		"Yeah, sure, remind %TARGET% to %TASK% %TIME% ago, because I TOTALLY can send messages back in time!",
 		"This is just awfully silly of you.",
 		"I don't even know what to say.",
 		"I suppose you don't have any device that can send messages back in time? Because I don't."
 	};
 	
 	//^remind(?:\s+(\w{1,32}))?\s*(?:in(\s+(?:(?:AMNTS|(?:\d+(?:\.\d+)?))\s*(?:\w+\s*))+)|to\s+(.+?))*[.!?]*$
 	private static Pattern remindParamPattern;
 	
 	private static Pattern timePattern;
 	
 	private static Pattern forgetParamPattern;
 	
 	@Override
 	protected void initialize() {
 		setName("Reminder");
 		setHelpText("Tell me to remind you (or something) about something in a while!");
 		addAlias("remind");
 		addAlias("nevermind");
 		addAlias("forget");
 		setTableName("remindtasks");
 		
 		if(reminders == null) reminders = new LinkedHashMap<>();
 		loadTasks();
 		
 		System.out.println("Got " + reminders.size() + " reminders!");
 		for(Reminder reminder : reminders.keySet()) {
 			long time = reminder.endTime.getTime() - System.currentTimeMillis();
 			System.out.println("Reminder ID: " + reminder.id + ", set by " + reminder.setter + "@" + reminder.server +
 					" to " + reminder.target + "; Task: to " + reminder.task + ", endtime " +
 					(time > 0 ? "in " + outputTime(time) : outputTime(-time) + " ago"));
 		}
 		
 		if(wordAmount == null) wordAmount = new LinkedHashMap<>();
 		else wordAmount.clear();
 		wordAmount.put("a few", 3L);
 		wordAmount.put("a lot of", -79L);
 		wordAmount.put("alot of", -74L);
 		wordAmount.put("a long", (long)Short.MAX_VALUE);
 		wordAmount.put("a very long", (long)Integer.MAX_VALUE);
 		wordAmount.put("an", 1L);
 		wordAmount.put("a", 1L);
 		wordAmount.put("one", 1L);
 		wordAmount.put("two", 2L);
 		wordAmount.put("three", 3L);
 		wordAmount.put("four", 4L);
 		wordAmount.put("five", 5L);
 		wordAmount.put("many", -25L);
 		wordAmount.put("tons of", -1000L);
 		
 		String wordAmnts = "";
 		for(String amnt : wordAmount.keySet()) {
 			wordAmnts += (wordAmnts.length() > 0 ? "|" : "") + amnt;
 		}
 		
 		remindParamPattern = Pattern.compile(
 				"^remind(?:\\s+(\\w{1,32}))?\\s*(?:" +
 					"in(\\s+(?:(?:"+wordAmnts+"|(?:-?\\d+(?:\\.\\d+)?))\\s*(?:[\\w,-_]+\\s*))+)|" +
 					"to\\s+(.+?)" +
 				"){0,2}[.!?]*$");
 		
 		timePattern = Pattern.compile("(?:((?:"+wordAmnts+")\\s|-?\\d+(?:\\.\\d+)?)\\s*([\\w-_]+)\\s*)");
 		
 		forgetParamPattern = Pattern.compile("^(?:forget|nevermind)\\s+(?:(my|\\w+'s))?\\s*((?:that|last|\\d+(?:st|nd|rd|th))\\s+)reminder[.!?]*$");
 	}
 	
 	public void loadTasks() {
 		try {
 			for(ScheduledFuture<?> future : reminders.values()) {
 				future.cancel(false);
 			}
 			reminders.clear();
 			
 			Database.createTable(getFormattedTableName(),
 					"server varchar(60), target char(32), task varchar(500), endtime timestamp, setter char(32)");
 			
 			List<HashMap<String, Object>> tasks =
 					Database.select("select id, server, target, task, endtime, setter from " + getFormattedTableName());
 			
 			for(HashMap<String, Object> taskRow : tasks) {
 				int id = (Integer) taskRow.get("ID");
 				String server = (String) taskRow.get("SERVER");
 				String target = (String) taskRow.get("TARGET");
 				String task = (String) taskRow.get("TASK");
 				Timestamp endTime = (Timestamp) taskRow.get("ENDTIME");
 				String setter = (String) taskRow.get("SETTER");
 				
 				if(server == null || target == null || task == null || endTime == null || setter == null) {
 					debug("Row " + id + ": INVALID REMINDER ROW FOUND");
 					continue;
 				}
 				
 				Reminder reminder = new Reminder(id, server, target.trim(), task, endTime, setter.trim());
 				
 				long msUntilEnd = endTime.getTime() - System.currentTimeMillis();
 				if(msUntilEnd < 0) msUntilEnd = 0;
 				
 				reminders.put(reminder, Bot.scheduleOneShotTask(new ReminderThread(reminder), msUntilEnd, TimeUnit.MILLISECONDS));
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	protected String format() {
 		return super.format() + " [{target} [to [task] | in [amount] [unit] ...] ...]";
 	}
 
 	@Override
 	public void execute(Bot bot, Channel chan, User user, String message) {
 		String[] args = message.split(" ", 2);
 		
 		switch(args[0]) {
 		case "remind":
 			handleRemindUse(bot, chan, user, message);
 			break;
 		case "nevermind": case "forget":
 			handleForgetUse(bot, chan, user, message);
 			break;
 		}
 	}
 	
 	private void handleRemindUse(Bot bot, Channel chan, User user, String message) {
 		String reminderTarget = user.getNick();
 		String task = "do something apparently quite important";
 		long moment = 0;
 		
 		if(Util.hasArgs(message, 2)) {
 			Matcher matcher = remindParamPattern.matcher(message);
 			
 			if(matcher.find()) {
 				if(matcher.group(1) != null && !matcher.group(1).equalsIgnoreCase("me")) {
 					reminderTarget = matcher.group(1);
 				}
 				if(matcher.group(2) != null) { //time
 					//Slightly special case, so hold this off for a bit
 					Matcher timeMatcher = timePattern.matcher(matcher.group(2).trim());
 					String lastUnit = null;
 					
 					while(timeMatcher.find()) {
 						System.out.println("UNIT: " + timeMatcher.group(2) + ", AMOUNT: " + timeMatcher.group(1));
 						
 						long timeAmount = 0;
 						if(wordAmount.containsKey(timeMatcher.group(1).trim())) {
 							timeAmount = wordAmount.get(timeMatcher.group(1).trim());
 							if(timeAmount < 0) timeAmount = (new Random()).nextLong() % -timeAmount;
 						}
 						else timeAmount = Integer.parseInt(timeMatcher.group(1).trim());
 						
 						String unit = timeMatcher.group(2).trim().toLowerCase();
 						long time = 0;
 						switch(unit) {
 							case "half": case "halves":
 								time = getUnitToMillisecMultiplier(lastUnit) / 2L;
 								break;
 							case "third": case "thirds":
 								time = getUnitToMillisecMultiplier(lastUnit) / 3L;
 								break;
 							case "quarter": case "quarters": case "fourth": case "fourths":
 								time = getUnitToMillisecMultiplier(lastUnit) / 4L;
 								break;
 							default:
 								time = getUnitToMillisecMultiplier(unit);
 								lastUnit = unit;
 								break;
 						}
 						
 						if(time >= 0) moment += timeAmount * time;
 						else {
 							passMessage(bot, chan, user, "I got no clue what kind of unit \"" + timeMatcher.group(2).toLowerCase() + "\" is.");
 							return;
 						}
 					}
 				}
 				if(matcher.group(3) != null) {//task
 					task = matcher.group(3).trim();
 				}
 			}
 			else {
 				invalidFormat(bot, chan, user);
 				return;
 			}
 		}
 		
 		boolean exitTrailRemovingLoop = false;
         while(task.length() > 0 && !exitTrailRemovingLoop) {
                 switch(task.charAt(task.length()-1)) {
                 case '.': case '!': case '?':
                         task = task.substring(0, task.length()-1);
                         break;
                         
                 default:
                         exitTrailRemovingLoop = true;
                         break;
                 }
         }
 		
 		if(task.length() == 0) {
 			invalidFormat(bot, chan, user);
 			return; //NOTHING TO DO HERE
 		}
 		
 		if(moment < 0) {
 			passMessage(bot, chan, user,
 					backInTimeLines[new Random().nextInt(backInTimeLines.length)]
 							.replace("%TIME%", outputTime(-moment))
 							.replace("%TARGET%", reminderTarget)
 							.replace("%SETTER%", user.getNick())
 							.replace("%TASK%", task)
 							);
 			return;
 		}
 		
 		System.out.println("ms: " + moment);
 		passMessage(bot, chan, user, "Alright " + user.getNick() + ", I'll tell "
 				+ (reminderTarget.equals(user.getNick()) ? "you" : reminderTarget) + " to " + task + " " + ((moment > 0) ? "in "
 				+ outputTime(moment) : "right now") + " and stuff.");
 		
 		if(user.getNick().equals(reminderTarget)) {
 			task = task
 					.replaceAll("\\b(?:me|I)\\b", "you")
 					.replaceAll("\\bmy\\b", "your")
 					.replaceAll("\\bmyself\\b", "yourself")
 					.replaceAll("\\byou\\b", bot.getNick())
 					.replaceAll("\\byour\\b", bot.getNick() + "'s")
 					;
 		}
 		else {
 			task = task
 					.replaceAll("\\b(?:me|I)\\b", user.getNick())
 					.replaceAll("\\bmy\\b", user.getNick() + "'s")
 					.replaceAll("\\bmyself\\b", "him|her")
 					.replaceAll("\\byou\\b", bot.getNick())
 					.replaceAll("\\byour\\b", bot.getNick() + "'s")
 					.replaceAll("\\bs?he\\b", "you")
 					.replaceAll("\\bhis\\b", "your")
 					.replaceAll("\\bher\\b", "you(r)")
 					.replaceAll("\\b(?:him|her)\\b", "you")
 					.replaceAll("\\b(?:him|her)self\\b", "yourself")
 					;
 		}
 		
 		Reminder reminder =
 				new Reminder(-1, bot.getServer(), reminderTarget, task, new Timestamp(System.currentTimeMillis() + moment), user.getNick());
 		
 		try {
 			Database.insert(getFormattedTableName(),
 					"server, target, task, endtime, setter",
 					reminder.toDatabaseString());
 			
 			reminder.setId( Database.getLastGeneratedId(getFormattedTableName()) );
 			
 			debug("LastID is " + reminder.getId());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		reminders.put(reminder, Bot.scheduleOneShotTask(new ReminderThread(reminder), moment, TimeUnit.MILLISECONDS));
 	}
 	
 	private void handleForgetUse(Bot bot, Channel chan, User user, String message) {
 		Matcher matcher = forgetParamPattern.matcher(message);
 		
 		String setter = user.getNick();
 		int which = -1;
 		
 		if(matcher.find()) {
 			if(matcher.group(1) != null && !matcher.group(1).equalsIgnoreCase("my")) {
 				setter = matcher.group(1).substring(0, matcher.group(1).indexOf("'s"));
 			}
 			if(matcher.group(2) != null) { //TODO Make this use a map
 				switch(matcher.group(2)) {
 				case "last": case "that":
 					which = -1;
 					break;
 				}
 			}
 			
 			if(!setter.equals(user.getNick())) {
 				passMessage(bot, chan, user, "Silly you, can't tinker around with someone else's reminders!");
 				return;
 			}
 			
 			List<Reminder> reminderList = new ArrayList<>();
 			for(Reminder reminder : reminders.keySet()) {
 				if(reminder.setter.equals(setter)) {
 					reminderList.add(reminder);
 				}
 			}
 			if(reminderList.size() == 0) {
 				passMessage(bot, chan, user, "You haven't set any reminder!");
 				return;
 			}
 			
 			if(which < 0) which += reminderList.size();
 			Reminder reminderData;
 			try {
 				reminderData = reminderList.get(which);
 			}
 			catch(IndexOutOfBoundsException e) {
 				passMessage(bot, chan, user, "You don't have that many reminders!");
 				return;
 			}
 			
 			if(reminderData.getId() != -1) {
 				try {
 					Database.execRaw("delete from " + getFormattedTableName() + " where id=" + reminderData.getId());
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			reminders.get(reminderData).cancel(false);
 			reminders.remove(reminderData);
 			
 			passMessage(bot, chan, user, "Alright, dropped that reminder.");
 		}
 		else {
 			invalidFormat(bot, chan, user);
 			return;
 		}
 	}
 	
 	public long getUnitToMillisecMultiplier(String unit) {
 		switch(unit) {
 		case "ms": case "millisec": case "millisecs": case "millisecond": case "milliseconds":
 			return 1L;
 		case "atom": case "atoms":
 			return 160L; //About 15/94 of a second
 		case "s": case "sec": case "secs": case "second": case "seconds":
 			return 1000L;
 		case "m": case "min": case "mins": case "minute": case "minutes":
 			return 60*1000L;
 		case "h": case "hour": case "hours":
 			return 60*60*1000L;
 		case "pahar": case "pahars": case "paher": case "pahers":
 			return 3*60*60*1000L;
 		case "d": case "day": case "days":
 			return 24*60*60*1000L;
 		case "w": case "week": case "weeks": case "sennight": case "sennights":
 			return 7*24*60*60*1000L;
 		case "fortnight": case "fortnights":
 			return 14*24*60*60*1000L;
 		case "lunarday": case "lunardays":
 			return 2551443000L;
 		case "month": case "months":
 			return 30*24*60*60*1000L;
 		case "y": case "year": case "years":
 			return 365*24*60*60*1000L;
 		case "moment": case "moments":
 			return 90*1000L;
 		case "chelek": case "cheleks":
 			return 3333L;
 		case "rega": case "regas":
 			return 43L;
 		case "jiffy": case "jiffys": case "jiffies":
 			return 17L;
 		case "decade": case "decades":
 			return 10*365*24*60*60*1000L;
 		case "century": case "centuries": case "centurys":
 			return 100L*(long)(365*24*60*60)*1000L;
 		case "millennium": case "millenniums": case "millennia":
 			return 1000L*(long)(365*24*60*60)*1000L;
 		case "microfortnight": case "microfortnights":
 			return 1209L;
 		case "instant": case "instants":
 			return 0;
 			
 		default:
 			if(unit.startsWith("dog-")) return getUnitToMillisecMultiplier(unit.substring(4)) / 7L;
 			if(unit.startsWith("dog")) return getUnitToMillisecMultiplier(unit.substring(3)) / 7L;
 			
 			if(unit.startsWith("half-")) return getUnitToMillisecMultiplier(unit.substring(5)) / 2L;
 			if(unit.startsWith("half")) return getUnitToMillisecMultiplier(unit.substring(4)) / 2L;
 			
 			if(unit.startsWith("quarter-")) return getUnitToMillisecMultiplier(unit.substring(8)) / 4L;
 			if(unit.startsWith("quarter")) return getUnitToMillisecMultiplier(unit.substring(7)) / 4L;
 			
 			return -1;
 		}
 	}
 	
 	public boolean isStartOfDelayDuration(String param) {
 		if(param.startsWith("in")) {
 			for(int i = "in".length(); i < param.length(); i++) {
 				if(Character.isWhitespace(param.charAt(i))) continue;
 				
 				if(Character.isDigit(param.charAt(i))) return true;
 				
 				if(Character.isLetter(param.charAt(i))) return false;
 			}
 		}
 		
 		return false;
 	}
 	
 	public void onNickChange(NickChangeEvent<Bot> event) {
 		for(Reminder reminder : reminders.keySet()) {
 			if(reminder.getTarget().equals(event.getOldNick())) {
 				reminder.setTarget(event.getNewNick());
 				
 				try {
 					Database.execRaw("update " + getFormattedTableName()
 							+ " set target='" + reminder.getTarget() + "' where id=" + reminder.getId());
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			if(reminder.getSetter().equals(event.getOldNick())) {
 				reminder.setSetter(event.getNewNick());
 				
 				try {
 					Database.execRaw("update " + getFormattedTableName()
 							+ " set setter='" + reminder.getSetter() + "' where id=" + reminder.getId());
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	@Data
 	public class Reminder {
 		@NonNull private int id;
 		@NonNull private String server;
 		@NonNull private String target;
 		@NonNull private String task;
 		@NonNull private Timestamp endTime;
 		@NonNull private String setter;
 		
 		public String toDatabaseString() {
 			//			"server,		   target,			 task,		    endtime, 					 setter"
 			return "'" + server + "', '" + target + "', '" + task + "', '" + endTime.toString() + "', '" + setter + "'";
 		}
 	}
 	
 	private class ReminderThread extends TimerThread {
 		private Reminder reminderData = null;
 		
 		public ReminderThread(Reminder data) {
 			super("ReminderThread");
 			
 			reminderData = data;
 		}
 		
 		@Override
 		public void run() {
 			setContext(Bot.getBotByServer(reminderData.getServer()));
 			
 			String messageTarget = reminderData.getTarget();
 			
 			for(Channel channel : getContext().getChannels()) {
 				for(User user : channel.getUsers()) {
 					if(user.getNick().equals(reminderData.getTarget())) messageTarget = channel.getName();
 				}
 			}
 			
 			String message = "'Ey, you, " + reminderData.getTarget() + "! "
 					+ (reminderData.getTarget().equals(reminderData.getSetter()) ? "You" : reminderData.getSetter())
 					+ " wanted me to remind you to " + reminderData.getTask() + "!";
 			
 			getContext().sendMessage(messageTarget, message);
			if(!reminderData.getTarget().equals(messageTarget)) getContext().sendMessage(reminderData.getTarget(), message);
 			getContext().sendNotice(reminderData.getTarget(), message);
 			
 			if(reminderData.getId() != -1) {
 				try {
 					Database.execRaw("delete from " + getFormattedTableName() + " where id=" + reminderData.getId());
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			reminders.remove(reminderData);
 		}
 		
 	}
 	
 	public String outputTime(long time) {
 		long ms = time % 1000;
 		time /= 1000;
 		long seconds = time % 60;
 		long minutes = (time % 3600) / 60;
 		long hours = (time % 86400) / 3600;
 		long days = (time % 31536000) / 86400;
 		long years = time / 31536000;
 		
 		String output = "";
 		if(years != 0) output += (output.length()>0 ? ", " : "") + years + " year" + (years > 1 ? "s" : "");
 		if(days != 0) output += (output.length()>0 ? ", " : "") + days + " day" + (days > 1 ? "s" : "");
 		if(hours != 0) output += (output.length()>0 ? ", " : "") + hours + " hour" + (hours > 1 ? "s" : "");
 		if(minutes != 0) output += (output.length()>0 ? ", " : "") + minutes + " minute" + (minutes > 1 ? "s" : "");
 		if(seconds != 0) output += (output.length()>0 ? ", " : "") + seconds + " second" + (seconds > 1 ? "s" : "");
 		if(ms != 0) output += (output.length()>0 ? ", " : "") + ms + " millisecond" + (ms > 1 ? "s" : "");
 		
 		return output;
 	}
 }
