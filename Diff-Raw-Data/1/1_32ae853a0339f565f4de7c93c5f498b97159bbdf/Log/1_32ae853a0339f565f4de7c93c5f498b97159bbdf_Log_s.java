 package persistance;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import core.Query;
 
 /**
  * A log of all requests received. This class follows the Singleton pattern. The
  * log file format is:
  * 
  * <pre>
  * [date] [processing time] phonehash {keyword} "flattened text of message"
  * </pre>
  */
 public class Log {
 
 	/**
 	 * The format log dates are stored in. More precise than the general format
 	 * used for the messages.
 	 */
 	public static final SimpleDateFormat LOG_DATE_FORM = new SimpleDateFormat(
 			"MM/dd/yyyy@kk:mm:ss");
 
 	public static final File LOG_FILE = new File("raz.log");
 
 	/** The total number of text messages processed by this instance of log. */
 	private int total;
 
 	private ArrayList<Query> buffer;
 
 	/** The file this instance of log saves to. */
 	private final File file;
 
 	/** The singleton instance of log. */
 	private static Log instance;
 
 	public static final Log getInstance() {
 		if (instance == null) {
 			instance = new Log();
 		}
 		return instance;
 	}
 
 	protected Log() {
 		this.file = LOG_FILE;
 		buffer = new ArrayList<Query>();
 	}
 
 	public Log(File file) {
 		this.file = file;
 		buffer = new ArrayList<Query>();
 	}
 
 	public static void log(Query query) {
 		getInstance().record(query);
 	}
 
 	/**
 	 * Returns a single line log format of a message.  This <em>is</em> lossy
 	 * because data about the phone number is hashed and the response to the
 	 * message is wiped.  All other data is recorded in a compact format though.
 	 * <p>
 	 * The first three field should all be justified / equally spaced. The phone
 	 * number is hashed because we want to know about user statistics, but
 	 * storing peoples phone number is just creepy. Currently it isn't a secure
 	 * hash, but that could be fixed if security becomes an issue.
 	 * 
 	 * @see {@link Log}
 	 * 
 	 * @param q the query to be flattened into a single log entry
 	 * @return the string representing the query
 	 */
 	static String queryToString(Query q) {
 		String date = LOG_DATE_FORM.format(q.getTimeReceived());
 		String time;
 		if (q.getTimeResponded() == null) {
 			time = "------";
 		} else {
 			long dt = q.getTimeResponded().getTime()
 					- q.getTimeReceived().getTime();
 			time = String.format("%06d", dt);
 		}
 		String keyword = q.getKeyword() == null ? "-" : q.getKeyword();
 
 		String phonehash = String.format("%08x", q.getPhoneNumber().hashCode());
 		String message = flatten(q.getBody());
 		return String.format("[%s] [%5s] %s {%s} \"%s\"", date, time,
 				phonehash, keyword, message);
 	}
 
 	/**
 	 * Returns a version of the string stripped of all newlines, tabs and extra
 	 * whitespace.
 	 * 
 	 * @param s
 	 *            the string to flatten
 	 * @return a version of the string with all extra whitespace removed
 	 */
 	static String flatten(String s) {
 		return s.replaceAll("\\s+", " ").trim();
 	}
 
 
 	/** 
 	* Records a single query in this log and occasionally saves the log to its file.
 	*
 	* @param query
 	 *            the query to be logged
 	 */
 	public void record(Query query) {
	public void record(Query query) {
 		total++;
 		buffer.add(query);
 		if (total % 10 == 0) {
 			try {
 				save(buffer, file);
 				buffer.clear();
 			} catch (IOException e) {
 				// TODO: inform user
 				// for now do nothing but save again in another 10 messages
 			}
 			if (total % 500 == 0) {
 
 			}
 		}
 
 	}
 
 	/** @return the total number of queries this log has recorded */
 	public int getTotal() {
 		return total;
 	}
 
 	/**
 	 * Saves a list of queries to a file. This appends the queries to the file
 	 * instead of overwriting.
 	 * 
 	 * @param queries a list of queries to be saved
 	 * @param file the file to append them to
 	 * @throws IOException if the file cannot be read
 	 */
 	public static void save(List<Query> queries, File file) throws IOException {
 		boolean append = true;
 		FileWriter writer = new FileWriter(file, append);
 		for (Query q : queries) {
 			writer.write(queryToString(q)
 					+ System.getProperty("line.separator"));
 		}
 		writer.flush();
 		writer.close();
 	}
 
 	public static void main(String[] args) throws InterruptedException {
 		Date then = new Date();
 		Thread.sleep(1000);
 		for (int i = 0; i < 20; i++) {
 			Query q = new Query(then, "help", Math.random() * Integer.MAX_VALUE
 					+ "");
 			q.setResponse("no");
 			// q.setTimeResponded(new Date());
 			System.out.println(queryToString(q));
 		}
 	}
 }
