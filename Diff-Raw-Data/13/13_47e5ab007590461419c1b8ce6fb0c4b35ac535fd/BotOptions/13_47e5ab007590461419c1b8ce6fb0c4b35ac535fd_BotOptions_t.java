 package com.xtouchme.ircbot;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 public class BotOptions implements java.io.Serializable {
 	
 	private static final long serialVersionUID = 448671760652256083L;
 	
 	ArrayList<String> admin;
 	ArrayList<String> moderator;
 	ArrayList<String> greetChannels;
 	ArrayList<String> peopleToAvoid;
 	ArrayList<String> peopleToIgnore;
 	boolean greet;
 	boolean reconnect;
 	boolean avoid;
 	int month, day, year, hour, minute;
 	Calendar target;
 	String eventName;
 	
 	File optionsFile = null;
 	private static BotOptions options = null;
 	boolean shouldShutUp = false;
 	
 	private BotOptions() {}
 	
 	public static synchronized BotOptions getOptions(String moderator, String filename) {
 		if(options == null) {
 			options = new BotOptions();
 			options.initialize(moderator, filename);
 		}
 		return options;
 	}
 
 	public Object clone() throws CloneNotSupportedException {
 		throw new CloneNotSupportedException();
 	}
 	
 	public void initialize(String moderator, String filename) {
 		admin = new ArrayList<String>();
 		this.moderator = new ArrayList<String>();
 		admin.add(moderator);
 		this.moderator.add(moderator);
 		
 		greetChannels = new ArrayList<String>();
 		peopleToAvoid = new ArrayList<String>();
 		peopleToIgnore = new ArrayList<String>();
 		
 		greet = false;
 		reconnect = false;
 		avoid = false;
 		target = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
 		
 		eventName = "Generic Event Name";
 		
 		setFile(filename);
 		try {
 			load();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void load() throws FileNotFoundException {
 		if(!optionsFile.exists()) save();
 		
 		FileInputStream input = new FileInputStream(optionsFile);		
 		try {
 			ObjectInputStream reader = new ObjectInputStream(input);
 			options = (BotOptions)reader.readObject();
			System.out.println(setTarget(options.year, options.month, options.day, options.hour, options.minute));
 			
 			System.out.println("-- Start of Options --");
 			System.out.println("admin: "+options.admin.toString());
 			System.out.println("moderator: "+options.moderator.toString());
 			System.out.println("greetChannels: "+options.greetChannels.toString());
 			System.out.println("peopleToAvoid: "+options.peopleToAvoid.toString());
 			System.out.println("peopleToIgnore: "+options.peopleToIgnore.toString());
 			System.out.println("greet: "+options.greet);
 			System.out.println("reconnect: "+options.reconnect);
 			System.out.println("avoid: "+options.avoid);
			System.out.printf("target date: %tB %td, %tY - %tH:%tM %tp%n",options.target, options.target, options.target,
																		  options.target, options.target, options.target);
			System.out.printf("date ints: y:%d m:%d d:%d h:%d min:%d %n", options.year, options.month, options.day,
																		  options.hour, options.minute);
			System.out.println("event: "+options.eventName);
 			System.out.println("--   Options End   --");
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void save() {
 		if(!optionsFile.exists()) {
 			try {
 				optionsFile.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		FileOutputStream out = null;
 		ObjectOutputStream writer = null;
 		
 		try {
 			out = new FileOutputStream(optionsFile);
 			writer = new ObjectOutputStream(out);
 			writer.writeObject(this);
 			writer.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void shouldShutUp(boolean shouldShutUp) {
 		this.shouldShutUp = shouldShutUp;
 	}
 	
 	public boolean shouldShutUp() {
 		return shouldShutUp;
 	}
 	
 	public void setFile(String filename) {
 		optionsFile = new File(filename);
 	}
 	
 	public boolean isModerator(String user) {
 		return moderator.contains(user);
 	}
 
 	public boolean isSuperAdmin(String user) {
 		return admin.get(0).equals(user);
 	}
 	
 	public boolean isAdmin(String user) {
 		return admin.contains(user);
 	}
 
 	public boolean shouldAvoid(String user) {
 		return peopleToAvoid.contains(user);
 	}
 	
 	public boolean shouldGreetChannel(String chan) {
 		return greetChannels.contains(chan);
 	}
 	
 	public String isGreet() {
 		return "Greeting is "+((greet)?"on":"off");
 	}
 
 	public String setGreet(boolean greet) {
 		this.greet = greet;
 		return "Greeting is now "+((greet)?"on":"off");
 	}
 
 	public String isReconnect() {
 		return "Reconnection is "+((reconnect)?"on":"off");
 	}
 
 	public String setReconnect(boolean reconnect) {
 		this.reconnect = reconnect;
 		return "Reconnection is now "+((reconnect)?"on":"off");
 	}
 
 	public String isAvoid() {
 		return ((avoid)?"Will":"Will not")+" avoid people when they join";
 	}
 
 	public String setAvoid(boolean avoid) {
 		this.avoid = avoid;
 		return "Now I "+((avoid)?"will":"will not")+" avoid people when they join";
 	}
 
 	public String getChans() {
 		String chans = "";
 		for(String s : greetChannels) {
 			chans += ", "+s;
 		}
 		if(chans.length() < 2) return chans;
 		return chans.substring(2);
 	}
 	
 	public String getModerators() {
 		String moderators = "";
 		for(String s : moderator) {
 			moderators += ", "+s;
 		}
 		System.out.println(moderators.substring(2));
 		return moderators.substring(2);
 	}
 	
 	public String addChan(String channel) {
 		if(!channel.startsWith("#")) return channel+" seems to be a bad channel. Try adding a # before its name";
 		if(greetChannels.contains(channel)) return "Channel is already on the list";
 		greetChannels.add(channel);
 		return "Will greet people on "+channel;
 	}
 	
 	public String removeChan(String channel) {
 		if(!greetChannels.contains(channel)) return "Channel is already off the list";
 		greetChannels.remove(channel);
 		return "Channel successfully removed";
 	}
 
 	public String addModerator(String[] nicks) {
 		String newModerators = "";
 		for(String s : nicks) {
 			if(addModerator(s).equals("Added "+s+" as bot moderator")) newModerators += ", "+s;
 		}
 		if(newModerators.length() == 0) return "Added no one";
 		return "Added "+((newModerators.length() < 2)?newModerators:newModerators.substring(2))+" as bot moderators";
 	}
 	
 	public String addModerator(String nick) {
 		if(moderator.contains(nick)) return "User is already a bot moderator";
 		moderator.add(nick);
 		return "Added "+nick+" as bot moderator";
 	}
 	
 	public String removeModerator(String nick) {
 		if(!moderator.contains(nick)) return "User is already off the list";
 		if(nick.equalsIgnoreCase(admin.get(0))) return "User is running the bot. You cannot remove this user";
 		moderator.remove(nick);
 		return "Removed "+nick+" in the bot moderator list";
 	}
 	
 	public boolean shouldIgnore(String user) {
 		return peopleToIgnore.contains(user);
 	}
 	
 	public String getIgnored() {
 		String ignored = "";
 		for(String s : peopleToIgnore) {
 			ignored += ", "+s;
 		}
 		if(ignored.length() < 2) return ignored;
 		return ignored.substring(2);
 	}
 
 	public String addToIgnore(String[] nicks) {
 		String newIgnores = "";
 		for(String s : nicks) {
 			if(addToIgnore(s).equals("Ignoring "+s)) newIgnores += ", "+s;
 		}
 		if(newIgnores.length() == 0) return "Ignored no one";
 		return "Ignoring "+((newIgnores.length() < 2)?newIgnores:newIgnores.substring(2));
 	}
 	
 	public String addToIgnore(String nick) {
 		if(peopleToIgnore.contains(nick)) return "User is already ignored";
 		if(nick.equalsIgnoreCase(admin.get(0))) return "User is running the bot. You cannot ignore this user";
 		peopleToIgnore.add(nick);
 		return "Ignoring "+nick;
 	}
 	
 	public String removeFromIgnore(String nick) {
 		if(!peopleToIgnore.contains(nick)) return "User is already unignored";
 		peopleToIgnore.remove(nick);
 		return "Unignored "+nick;
 	}
 	
 	public String getAdmins() {
 		String admins = "";
 		for(String s : admin) {
 			admins += ", "+s;
 		}
 		if(admins.length() < 2) return admins;
 		return admins.substring(2);
 	}
 	
 	public String addAdmin(String[] nicks) {
 		String newAdmins = "";
 		for(String s : nicks) {
 			if(addAdmin(s).equals("Added "+s+" as bot admin")) newAdmins += ", "+s;
 		}
 		if(newAdmins.length() == 0) return "Added no one";
 		return "Added "+((newAdmins.length() < 2)?newAdmins:newAdmins.substring(2))+" as bot admins";
 	}
 	
 	public String addAdmin(String nick) {
 		if(admin.contains(nick)) return "User is already an admin";
 		admin.add(nick);
 		addModerator(nick);
 		return "Added "+nick+" as bot admin";
 	}
 	
 	public String removeAdmin(String nick) {
 		if(!admin.contains(nick)) return "User is already off the list";
 		if(nick.equalsIgnoreCase(admin.get(0))) return "User is running the bot. You cannot remove this user";
 		admin.remove(nick);
 		return "Removed "+nick+" in the bot admin list";
 	}
 	
 	public String removeAdmin(String[] nicks) {
 		String removedAdmins = "";
 		for (String s : nicks) {
 			if (removeAdmin(s).equals("Removed "+s+" in the bot admin list")) removedAdmins += ", " + s;
 		}
 		if(removedAdmins.length() == 0) return "Removed no one";
 		return "Removed "+((removedAdmins.length() < 2)?removedAdmins:removedAdmins.substring(2))+" as bot admins";
 	}
 
 	public String removeModerator(String[] nicks) {
 		String removedModerators = "";
 		for (String s : nicks) {
 			if (removeModerator(s).equals("Removed "+s+" in the bot moderator list")) removedModerators += ", " + s;
 		}
 		if(removedModerators.length() == 0) return "Removed no one";
 		return "Removed "+((removedModerators.length() < 2)?removedModerators:removedModerators.substring(2))+
 			   " as bot moderators";
 	}
 	
 	public String removeFromIgnore(String[] nicks) {
 		String removedIgnores = "";
 		for (String s : nicks) {
 			if (removeFromIgnore(s).equals("Unignored "+s)) removedIgnores += ", " + s;
 		}
 		if(removedIgnores.length() == 0) return "Unignored no one";
 		return "Unignored "+((removedIgnores.length() < 2)?removedIgnores:removedIgnores.substring(2));
 	}
 	
 	public String setTarget(int month, int day, int year, int hour, int minute) {
 		this.month = month;
 		this.day = day;
 		this.year = year;
 		this.hour = hour;
 		this.minute = minute;
 		target.set(year, month, day, hour, minute);
 		
 		return String.format("Set target date to: %tB %td, %tY - %tH:%tM %tp"
 							,target, target, target, target, target, target);
 	}
 	
 	public String updateEvent(String newEvent) {
 		if(newEvent.equals(eventName))
 			return "New event name is the same from the last. Event Name unchanged.";
 		
 		eventName = newEvent;
 		return "Event updated to "+newEvent;
 	}
 	
 	public String calculateTimeDifference() {
 		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
 		long milliseconds1 = target.getTimeInMillis();
 		long milliseconds2 = current.getTimeInMillis();
 		long diff = milliseconds1 - milliseconds2;
 		long diffMinutes = diff / (60 * 1000);
 		long diffHours = diff / (60 * 60 * 1000);
 		long diffDays = diff / (24 * 60 * 60 * 1000);
 	
 		long excessHours = diffHours - (diffDays * 24);
 		long excessMinutes = diffMinutes - (diffHours * 60);
 	
 		String result = ((diffDays>0)?diffDays+((diffDays>1)?" days":" day"):"");
 		result += ((excessHours>0)?((diffDays>0&excessHours>0)?", ":"")+excessHours+((excessHours>1)?" hours":" hour"):"");
 		if(excessHours<1 && diffDays>1 && excessMinutes>1) result += ", ";
 		result += ((excessMinutes>0)?((excessHours>0&excessMinutes>0)?", ":"")+excessMinutes
 		 	   +  ((excessMinutes>1)?" minutes":" minute"):"");
 		result += " until: "+eventName;
 		if(result.equals(" until: "+eventName)) {
 			result = String.format(eventName+", last %tB %td, %tY - %tH:%tM %tp, has been passed",
 								   target, target, target, target, target, target);
 		}
 		return result;
 	}
 	
 }
