 package com.popodeus.chat;
 
 import java.io.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 /**
  * photodeus
  * Sep 2, 2009
  * 10:01:11 PM
  */
 public final class ChatLogger {
 
 	final Logger logger = Logger.getLogger(ChatLogger.class.getName());
 	
 	final DateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
 	private static final int MAX_SCAN_LINES = 600;
 	private File logdir;
 	private ConcurrentMap<String, ChannelLog> channels;
 
 	public class ChatLine implements Comparable {
 		long timestamp;
 		String nick;
 		String line;
 
 		ChatLine(final String nick, final String line) {
 			this.timestamp = System.currentTimeMillis();
 			this.nick = nick;
 			this.line = line;
 		}
 
 		ChatLine(final long timestamp, final String nick, final String line) {
 			this.timestamp = timestamp;
 			this.nick = nick;
 			this.line = line;
 		}
 
 		public int compareTo(final Object o) {
 			if (o instanceof ChatLine) {
 				ChatLine cl = (ChatLine) o;
 				return (int) Math.signum(this.timestamp - cl.timestamp);
 			}
 			return 0;
 		}
 
 		@Override
 		public String toString() {
 			String localtime = dateformat.format(timestamp);
 			return timestamp + " [" + localtime + "] <" + nick + "> " + line;
 		}
 	}
 
 	class ActionLine extends ChatLine {
 		ActionLine(final String nick, final String line) {
 			super(nick, line);
 		}
 
 		ActionLine(final long timestamp, final String nick, final String line) {
 			super(timestamp, nick, line);
 		}
 
 		@Override
 		public String toString() {
 			String localtime = dateformat.format(timestamp);
 			String prefix = "-!- ";
 			if (line.startsWith("*")) {
 				prefix = "";
 			}
 			return timestamp + " [" + localtime + "] " + prefix + line;
 		}
 	}
 
 	class ChannelLog {
 		private BufferedWriter writer;
 		private BlockingDeque<ChatLine> lines;
 		private ConcurrentMap<String, Long> nicktimes;
 		private long lastflush;
 		private int BACKLINES = 4096;
 		private int lastlogDay;
 
 		ChannelLog(final String channel) throws IOException {
 			this.lines = new LinkedBlockingDeque<ChatLine>(BACKLINES);
 			this.nicktimes = new ConcurrentHashMap<String, Long>(256);
 			this.lastflush = System.currentTimeMillis();
 
 			final File logfile = getLogFile(channel);
 			String line = null;
 			try {
 				if (logfile.exists()) {
 					logger.info("Opening existing log file: " + logfile);
 					ReverseFileReader rfr = new ReverseFileReader(logfile);
 					int readlines = 0;
 					do {
 						line = rfr.readLine();
 						if (line == null) {
 							break;
 						}
 						if (line.startsWith("--- ")) {
 							continue;
 						}
 						String[] p = line.split(" ", 3);
 						if (p.length == 3) {
 							ChatLine chatline = null;
 							long ts = Long.parseLong(p[0]);
 							// p[1] is just [HH:mm:ss], skip it
 							if (p[2].startsWith("<")) {
 								final int nickend = p[2].indexOf(">");
 								String nick = p[2].substring(1, nickend);
 								chatline = new ChatLine(ts, nick, p[2].substring(nickend + 2));
 								touch(nick, ts);
 							} else if (p[2].startsWith("-!- ")) {
 								String nick = p[2].substring(4, p[2].indexOf(" ", 5));
 								chatline = new ActionLine(ts, nick, p[2]);
 								touch(nick, ts);
 							} else if (p[2].startsWith("* ")) {
 								String nick = p[2].substring(2, p[2].indexOf(" ", 3));
 								chatline = new ActionLine(ts, nick, p[2]);
 								touch(nick, ts);
 							}
 							if (lastlogDay < 0) {
 								Calendar cal = new GregorianCalendar();
 								cal.setTimeInMillis(ts);
 								lastlogDay = cal.get(Calendar.DAY_OF_MONTH);
 							}
 							if (chatline != null) {
 								//System.out.println("\""+ chatline + "\" (" + chatline.getClass() + ")");
 								this.lines.offerLast(chatline);
 							}
 						}
 					} while (++readlines < BACKLINES);
 					rfr.close();
 					logger.fine("Read back " + readlines);
 				} else {
 					logger.fine("No previous logfile exists");
 				}
 			} catch (Exception e) {
 				logger.severe((line != null ? line + ": " : "") + e);
 			}
 
 			if (logfile.exists()) {
 				logger.info("Appending to log file: " + logfile);
 				this.writer = new BufferedWriter(new FileWriter(logfile, true), 1024);
 			} else {
 				logger.info("Writing to new log file: " + logfile);
 				this.writer = new BufferedWriter(new FileWriter(logfile), 1024);
 			}
 		}
 
 		private File getLogFile(final String channel) {
 			//return new File(logdir, date + File.separatorChar + channel);
 			return new File(logdir, channel.toLowerCase().replaceAll("[#&]", "") + ".log");
 		}
 
 		public void close() {
 			try {
 				writer.write("--- Log closed " + new Date());
 				writer.newLine();
 				writer.flush();
 				writer.close();
 			} catch (Exception e) {
 				logger.log(Level.SEVERE, e.toString(), e);
 			}
 		}
 
 		public void log(final String nick, final String line) {
 			try {
 				addit(new ChatLine(nick, line));
 				touch(nick);
 			} catch (Exception e) {
 				logger.log(Level.SEVERE, e.toString(), e);
 			}
 		}
 
 		public void logAction(final String nick, final String line) {
 			try {
 				addit(new ActionLine(nick, line));
 			} catch (Exception e) {
 				logger.log(Level.SEVERE, e.toString(), e);
 			}
 		}
 
 		private void addit(final ChatLine line) throws IOException {
 			if (lines.remainingCapacity() == 0) {
 				ChatLine out = lines.pollLast();
 				// right now we don't need the line that is removed from the buffer
 			}
 			Calendar cal = new GregorianCalendar();
 			cal.setTimeInMillis(line.timestamp);
 			int daynow = cal.get(Calendar.DAY_OF_MONTH);
 			if (daynow != lastlogDay) {
 				writer.write("--- Date changed to " + cal.getTime());
 				writer.newLine();
 				lastlogDay = daynow;
 			}
 
 			lines.offerFirst(line);
 			writer.write(line.toString());
 			writer.newLine();
 		}
 
 		public void flush() {
 			try {
 				// Minimum 100 ms between flushes
 				if (System.currentTimeMillis() - lastflush >= 100) {
 					lastflush = System.currentTimeMillis();
 					writer.flush();
 				}
 			} catch (Exception e) {
 				logger.log(Level.SEVERE, e.toString(), e);
 			}
 		}
 
 		public void touch(final String nick) {
 			touch(nick, System.currentTimeMillis());
 		}
 
 		public synchronized void touch(final String nick, final long timestamp) {
 			final String lnick = nick.toLowerCase();
 			if (nicktimes.containsKey(lnick)) {
 				nicktimes.put(lnick, Math.max(nicktimes.get(lnick), timestamp));
 			} else {
 				nicktimes.put(lnick, timestamp);
 			}
 		}
 	}
 
 	protected ChatLogger() { }
 	
 	public ChatLogger(final File logdir) {
 		logger.info("New ChatLogger logging into " + logdir.toString());
 		if (!logdir.exists()) {
 			//throw new IOException("Log output directory " + logdir + " does not exist or is not writable.");
 			logger.severe("Log output directory " + logdir + " does not exist or is not writable.");
 		}
 		this.logdir = logdir;
 		this.channels = new ConcurrentHashMap<String, ChannelLog>(5);
 	}
 
 	public synchronized void joinChannel(final String _channel) throws IllegalStateException {
 		logger.info("Joining channel: " + _channel);
 		String channel = _channel.toLowerCase();
 		if (channels.containsKey(channel)) {
 			ChannelLog tmp = channels.get(channel);
 			logger.info("Closing channel log: " + _channel + " => " + channel);
 			tmp.close();
 		}
 		try {
 			ChannelLog log = new ChannelLog(channel);
 			channels.put(channel.toLowerCase(), log);
 			log.writer.write("--- Log opened " + new Date());
 			log.writer.newLine();
 			log.writer.flush();
 		} catch (IOException e) {
 			logger.log(Level.SEVERE, e.toString(), e);
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	public boolean hasJoined(final String channel) {
 		return channels.containsKey(channel.toLowerCase());
 	}
 
 	public synchronized void leaveChannel(final String _channel) {
 		final String key = _channel.toLowerCase();
 		ChannelLog clog = channels.get(key);
 		if (clog != null) {
 			logger.info("Leaving: " + _channel + " and closing writer.");
 			clog.close();
 			channels.remove(key);
 			logger.info("channels.size(): " + channels.size());
 		}
 	}
 
 	/**
 	 * Logs specified line for channel. If channel is null, writes
 	 * events into all channel logs.
 	 *
 	 * @param channel Channel name or null
 	 * @param nick	Nickname of the one generating the event
 	 * @param line	Message to log
 	 */
 	public void log(final String channel, String nick, String line) {
 		log(channel, nick, line, false);
 	}
 
 	public void log(final String channel, String nick, String line, boolean flush) {
 		if (channel == null) {
 			for (ChannelLog logga : channels.values()) {
 				logga.log(nick, line);
 				if (flush) {
 					logga.flush();
 				}
 			}
 		} else {
 			ChannelLog output = channels.get(channel.toLowerCase());
 			if (output == null) {
 				logger.warning("Failed to log to " + channel + ". Not opened: " + line);
 				logger.warning("Logs that are open: " + channels.keySet());
 			} else {
 				output.log(nick, line);
 				if (flush) {
 					output.flush();
 				}
 			}
 		}
 	}
 
 	public void logAction(final String channel, final String nick, final String line) {
 		logAction(channel, nick, line, true);
 	}
 
 	public void logAction(final String channel, final String nick, final String line, boolean flush) {
 		if (channel == null) {
 			for (ChannelLog log : channels.values()) {
 				log.logAction(nick, line);
 				if (flush) {
 					log.flush();
 				}
 				if (nick != null) {
 					log.touch(nick);
 				}
 			}
 		} else {
 			ChannelLog output = channels.get(channel.toLowerCase());
 			if (output != null) {
 				output.logAction(nick, line);
 				if (flush) {
 					output.flush();
 				}
 				if (nick != null) {
 					output.touch(nick);
 				}
 			} else {
 				logger.warning("Channel " + channel + " log not opened: " + line);
 				logger.finer("Logs that are open: " + channels.keySet());
 			}
 		}
 	}
 
 	/**
 	 * Closes all open logs
 	 */
 	public void closeAll() {
 		logger.info("Closing all channel logs: channels.size(): " + channels.size());
 		for (ChannelLog clog : channels.values()) {
 			clog.close();
 		}
 		channels.clear();
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		closeAll();
 	}
 
 	public ChatLine getLastLine(final String channel) {
 		ChannelLog clog = channels.get(channel.toLowerCase());
 		ChatLine retval = null;
 		if (clog != null) {
 			retval = clog.lines.peek();
 		}
 		return retval;
 	}
 
 	public Long getLastActivity(final String _channel, final String nick) {
 		final String channel = _channel.toLowerCase();
 		if (channels.containsKey(channel)) {
 			return channels.get(channel).nicktimes.get(nick.toLowerCase());
 		}
 		return null;
 	}
 
 	public Iterator<ChatLine> getLines(final String channel) {
 		ChannelLog log = channels.get(channel.toLowerCase());
 		if (log != null) {
 			return log.lines.iterator();
 		}
 		// Return empty iterator if channel isn't being logged
 		return new Iterator<ChatLine>() {
 			public boolean hasNext() {
 				return false;
 			}
 
 			public ChatLine next() {
 				return null;
 			}
 
 			public void remove() {
 			}
 		};
 	}
 
 	public String getStatus() {
 		StringBuilder sb = new StringBuilder(200);
 		sb.append("Channels logs open: ");
 		for (String name : channels.keySet()) {
 			sb.append(name + ", ");
 		}
 		return sb.toString();
 	}
 
 	public void nickChange(final String oldNick, final String newNick) {
 		for (Map.Entry<String, ChannelLog> e : channels.entrySet()) {
 			Map m = e.getValue().nicktimes;
 			m.put(newNick.toLowerCase(), m.get(oldNick.toLowerCase()));
 			//m.remove(oldNick.toLowerCase());
 		}
 	}
 
 	/**
 	 * Tell how many lines logged for the previous n milliseconds
 	 *
 	 * @param channel Which channel. Log must be opened for this to return a meaningful value.
 	 * @param minutes The number of minutes away you want to count back. For multiple times, multiple results are returned.
 	 * @return null if log is closed
 	 */
 	public Iterator<Map.Entry<Integer, Integer>> getLineCounts(final String channel, final int... minutes) {
 		final ChannelLog log = channels.get(channel.toLowerCase());
 		if (log == null) {
 			return new HashMap<Integer, Integer>().entrySet().iterator();
 		}
 
 		final SortedMap<Integer, Integer> linecounts = new TreeMap<Integer, Integer>();
 		long maxdelta = -1;
 		for (int minute : minutes) {
 			linecounts.put(minute, 0);
 			maxdelta = Math.max(minute, maxdelta);
 		}
 		int backstep = 0;
 
 		final long now = System.currentTimeMillis();
 		final Iterator<ChatLine> it = log.lines.iterator();
 		while (it.hasNext()) {
 			ChatLine line = it.next();
 			long linetime = line.timestamp;
 			long delta = (now - linetime) / (60 * 1000);
 			if (delta > maxdelta) {
 				break;
 			}
 			for (Map.Entry<Integer, Integer> min : linecounts.entrySet()) {
 				Integer timelimit = min.getKey();
 				if (delta <= timelimit) {
 					int lines = min.getValue();
 					linecounts.put(timelimit, lines + 1);
 				}
 			}
 			++backstep;
 			if (backstep >= MAX_SCAN_LINES) {
 				break; // enough lines...
 			}
 		}
 		return linecounts.entrySet().iterator();
 	}
 
 
 	class ReverseFileReader {
 		private RandomAccessFile randomfile;
 		private long position;
 
 		public ReverseFileReader(final File filename) throws IOException {
 			// Open up a random access file
 			this.randomfile = new RandomAccessFile(filename, "r");
 			// Set our seek position to the end of the file
 			this.position = this.randomfile.length();
 
 			// Seek to the end of the file
 			this.randomfile.seek(this.position);
 			//Move our pointer to the first valid position at the end of the file.
 			String thisLine = this.randomfile.readLine();
 			while (thisLine == null) {
 				this.position--;
 				this.randomfile.seek(this.position);
 				thisLine = this.randomfile.readLine();
 				this.randomfile.seek(this.position);
 			}
 		}
 
 		public void close() throws IOException {
 			this.randomfile.close();
 		}
 
 		// Read one line from the current position towards the beginning
 		public String readLine() throws IOException {
 			int thisCode;
 			char thisChar;
 
 			// If our position is less than zero already, we are at the beginning
 			// with nothing to return.
 			if (this.position < 0) {
 				return null;
 			}
 
 			StringBuilder line = new StringBuilder(256);
 			for (; ;) {
 				// we've reached the beginning of the file
 				if (this.position < 0) {
 					break;
 				}
 				// Seek to the current position
 				this.randomfile.seek(this.position);
 
 				// Read the data at this position
 				thisCode = this.randomfile.readByte();
 				thisChar = (char) thisCode;
 
 				// If this is a line break or carrige return, stop looking
 				if (thisCode == 13 || thisCode == 10) {
 					// See if the previous character is also a line break character.
 					// this accounts for crlf combinations
 					this.randomfile.seek(this.position - 1);
 					int nextCode = this.randomfile.readByte();
 					if ((thisCode == 10 && nextCode == 13) || (thisCode == 13 && nextCode == 10)) {
 						// If we found another linebreak character, ignore it
 						this.position = this.position - 1;
 					}
 					// Move the pointer for the next readline
 					this.position--;
 					break;
 				} else {
 					// This is a valid character append to the string
 					line.insert(0, thisChar);
 				}
 				// Move to the next char
 				this.position--;
 			}
 			// return the line
 			return line.toString();
 		}
 	}
 
 	public static void main(String[] args) {
 		File logdir = new File("/tmp/chatlog.log");
 		try {
 			ChatLogger c = new ChatLogger(logdir);
 			c.joinChannel("#Popmundo");
 			System.out.println("============================");
 			System.out.println(c.getLastActivity("#Popmundo", "TestUser"));
 
 			Iterator<Map.Entry<Integer,Integer>> it = c.getLineCounts("#Popmundo", 5, 10, 30);
 			while (it.hasNext()) {
 				Map.Entry entry = it.next();
 				System.out.println(entry.getKey() + ": " + entry.getValue());
 			}
 			c.closeAll();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 }
