package agents.test.mock;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * This class should be used by Mock agents to log significant events. For
  * example, you might write a log entry every time a Mock receives a message.
  * The class exposes some helper methods to allow you to easily parse and search
  * these log files.
  * 
  * @author Sean Turner
  * 
  */
 public class EventLog {
 
 	/**
 	 * This is the backing data store for the list of events.
 	 */
 	private LinkedList<LoggedEvent> events = new LinkedList<LoggedEvent>();
 
 	/**
 	 * Add a new event to the log.
 	 * 
 	 * @param e
 	 */
 	public void add(LoggedEvent e) {
 		events.add(e);
 	}
 
 	/**
 	 * Clear the event log
 	 */
 	public void clear() {
 		events.clear();
 	}
 
 	/**
 	 * @return the number of events in the log
 	 */
 	public int size() {
 		return events.size();
 	}
 
 	/**
 	 * Searches all of the messages contained in the log for the specified
 	 * string. If the specified string is contained in any messages in the log
 	 * file, returns true.
 	 * 
 	 * @param message
 	 *            the string to search for
 	 * @return true if string is contained somewhere within the text of a logged
 	 *         event. False otherwise.
 	 */
 	public boolean containsString(String message) {
 		for (LoggedEvent e : events) {
 			if (e.getMessage().contains(message)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Similar to retains string, except returns the LoggedEvent object
 	 * 
 	 * @param message
 	 *            the message to search for
 	 * @return the first LoggedEvent which contains the given
 	 *         string
 	 */
 	public LoggedEvent getFirstEventWhichContainsString(String message) {
 		for (LoggedEvent e : events) {
 			if (e.getMessage().contains(message)) {
 				return e;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @return the most recently LoggedEvent
 	 */
 	public LoggedEvent getLastLoggedEvent() {
 		return events.getLast();
 	}
 
 	public String toString() {
 		StringBuilder text = new StringBuilder();
 		String newLine = System.getProperty("line.separator");
 
 		if (events.size() == 0) {
 			return "Log is empty.";
 		}
 
 		for (LoggedEvent e : events) {
 			text.append(e.toString());
 			text.append(newLine);
 		}
 		return text.toString();
 
 	}
 }
