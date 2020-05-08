 package soofw.trk;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 class Task implements Comparable<Task> {
 	final static Pattern re_tag = Pattern.compile("(^|\\s)([\\@\\#\\+]([\\w\\/]+))");
 	final static Pattern re_at = Pattern.compile("(^|\\s)(\\@([\\w\\/]+))");
 	final static Pattern re_hash = Pattern.compile("(^|\\s)(\\#([\\w\\/]+))");
 	final static Pattern re_plus = Pattern.compile("(^|\\s)(\\+([\\w\\/]+))");
 	final static Pattern re_priority = Pattern.compile("(^|\\s)(\\!(\\d))");
 	final static Pattern re_date = Pattern.compile("((\\d{1,2})/(\\d{1,2})(/(\\d{2}))*([@ ](\\d{1,2})(:(\\d{1,2}))*(am|pm)*)*)");
 	final static Pattern re_done = Pattern.compile("^x\\s+");
 	final static Pattern re_current = Pattern.compile("^/\\s+");
 
 	String source = null;
 	String pretty = null;
 	String sortVal = null;
 	String searchVal = null;
 	boolean done = false;
 	boolean current = false;
 	int priority = 0;
 	Calendar calendar = null;
 	ArrayList<String> tags = new ArrayList<String>();
 
 	Task(String source) {
 		this.source = source.trim();
 		this.pretty = this.source;
 
 		this.pretty = this.pretty.replaceAll(re_current.pattern(), "");
 		this.searchVal = this.pretty = this.pretty.replaceAll(re_done.pattern(), "");
 		this.pretty = this.pretty.replaceAll(re_date.pattern(), "");
 		this.sortVal = this.pretty = this.pretty.replaceAll(re_priority.pattern(), "");
 		this.pretty = this.pretty.replaceAll(re_tag.pattern(), "");
 		this.pretty = this.pretty.replaceAll("\\s+", " ");
 		this.pretty = this.pretty.trim();
 
 		this.sortVal = this.sortVal.toLowerCase();
 
 		// find if it's completed
 		this.done = re_done.matcher(this.source).find();
 		this.current = re_current.matcher(this.source).find();
 
 		// find the priority
 		Matcher m = re_priority.matcher(this.source);
 		if(m.find()) {
 			this.priority = Integer.parseInt(m.group(2).substring(1));
 			if(this.priority == 0) { // !0 is actually -1 priority
 				this.priority  = -1;
 			}
 			this.tags.add(m.group(2));
 		}
 
 		// find the date
 		this.calendar = Task.matcherToCalendar(re_date.matcher(this.source));
 
 		this.addTags(re_date.matcher(this.source), 0);
 		this.addTags(re_tag.matcher(this.source), 2);
 	}
 
 	@Override
 	public String toString() {
 		return this.pretty;
 	}
 
 	@Override
 	public int compareTo(Task other) {
 		if(this.current != other.current) {
 			if(this.current) return -1;
 			else return 1;
 		}
 
 		if(this.priority != other.priority) {
 			return other.priority - this.priority;
 		}
 
 		if(this.calendar != null && other.calendar == null) {
 			return -1;
 		} else if(this.calendar == null && other.calendar != null) {
 			return 1;
 		} else if(this.calendar != null && other.calendar != null) {
 			int cmp = this.calendar.compareTo(other.calendar);
 			if(cmp != 0) {
				return cmp;
 			}
 		}
 
 		return this.sortVal.compareTo(other.sortVal);
 	}
 
 	void setDone(boolean done) {
 		this.done = done;
 		this.source = this.source.replaceAll(re_done.pattern(), "");
 		this.source = this.source.replaceAll(re_current.pattern(), "");
 		if(this.done) {
 			this.source = "x " + this.source;
 		}
 	}
 
 	void setCurrent(boolean current) {
 		this.current = current;
 		this.source = this.source.replaceAll(re_done.pattern(), "");
 		this.source = this.source.replaceAll(re_current.pattern(), "");
 		if(this.current) {
 			this.source = "/ " + this.source;
 		}
 	}
 
 	void addTags(Matcher m, int group) {
 		while(m.find()) {
 			this.tags.add(m.group(group));
 		}
 	}
 	String[] getTags() {
 		String[] temp = new String[this.tags.size()];
 		this.tags.toArray(temp);
 		return temp;
 	}
 
 	boolean contains(String search) {
 		String[] words = search.toLowerCase().split(" ");
 		for(int i = 0; i < words.length; i++) {
 			if(!this.searchVal.toLowerCase().contains(words[i])) {
 				return false;
 			}
 		}
 		return true;
 	}
 	boolean matches(String tag) {
 		tag = tag.toLowerCase();
 		char type = tag.charAt(0);
 		String content = tag.substring(1);
 		String regex = null;
 
 		switch(type) {
 			case '!':
 				regex = "(^|.*\\s)(\\!" + content + ")(\\s.*|$)";
 				break;
 			case '+':
 			case '#':
 			case '@':
 				regex = "(^|.*\\s)(\\" + type + "([\\w\\/]*)(" + content + "))(\\s.*|\\/.*|$)";
 				break;
 			default:
 				regex = "(^|.*\\s)(" + tag + ")(\\s.*|$)";
 		}
 
 		return Pattern.matches(regex, this.source.toLowerCase());
 	}
 
 
 	static Calendar matcherToCalendar(Matcher m) {
 		if(m.find()) {
 			Calendar temp = Calendar.getInstance();
 
 			temp.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1);
 			temp.set(Calendar.DATE, Integer.parseInt(m.group(3)));
 
 			if(m.group(5) != null) {
 				temp.set(Calendar.YEAR, Integer.parseInt(m.group(5)) + 2000);
 			}
 
 			if(m.group(7) != null) {
 				temp.set(Calendar.HOUR, Integer.parseInt(m.group(7)));
 			} else {
 				temp.set(Calendar.HOUR, 11);
 			}
 			if(m.group(9) != null) {
 				temp.set(Calendar.MINUTE, Integer.parseInt(m.group(9)));
 			} else {
 				temp.set(Calendar.MINUTE, 59);
 			}
 			if(m.group(10) != null) {
 				temp.set(Calendar.AM_PM, m.group(10).toLowerCase().equals("pm") ? Calendar.PM : Calendar.AM);
 			} else {
 				temp.set(Calendar.AM_PM, Calendar.PM);
 			}
 
 			return temp;
 		}
 
 		return null;
 	}
 }
