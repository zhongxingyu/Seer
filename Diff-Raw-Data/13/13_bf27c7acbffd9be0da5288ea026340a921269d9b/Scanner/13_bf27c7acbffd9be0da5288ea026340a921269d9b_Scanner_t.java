 package gsingh.learnkirtan.parser;
 
 import gsingh.learnkirtan.Main;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Scanner {
 	/**
 	 * The logger for this class
 	 */
 	private final static Logger LOGGER = Logger.getLogger(Scanner.class
 			.getName());
 
 	static {
 		LOGGER.addHandler(Main.logFile);
 		LOGGER.setLevel(Level.INFO);
 	}
 
 	/**
 	 * The list through which to scan
 	 */
 	private List<String> list;
 
 	/**
 	 * The maximum length of {@code list}
 	 */
 	private int maxlen;
 
 	/**
 	 * The current index in the list
 	 */
 	private int index;
 
 	/**
 	 * The end label
 	 */
 	String end;
 
 	/**
 	 * True if end has been reached
 	 */
 	boolean finished;
 
 	public Scanner(List<String> list, String end) {
 		maxlen = list.size();
 		this.list = list;
 		this.end = end;
 	}
 
 	/**
 	 * Gets the next input, returning special strings if the end is reached
 	 */
 	public String getNext() {
 		// If we've reached the end of the list
 		if (index == maxlen) {
 			String lastInput = list.get(index - 1);
 
 			if (lastInput.charAt(0) == '#')
 				// If the last input was a label
 				lastInput = "LABEL";
 			else if (lastInput.equals("-"))
 				// If the last character was a dash
 				lastInput = "DASH";
 			else
 				// If the last character was a note
 				lastInput = "NOTE";
 
 			LOGGER.info("Reached End");
 			finished = true;
 			return lastInput;
 		}
 
 		return list.get(index++);
 	}
 
 	/**
 	 * Gets the next note or dash, return special strings for reaching the end
 	 */
 	public String getNextNote() {
 		String input = getNext();
 
 		while (input.charAt(0) == '#') {
 			if (input.equals(end)) {
 				LOGGER.info("Reached End");
 				finished = true;
 				return "ENDLABEL";
 			}
 			input = getNext();
 		}
 
 		return input;
 	}
 
 	/**
	 * Resets the scanner index to 0 and finished to false
 	 */
 	public void reset() {
 		index = 0;
		finished = false;
 	}
 
 	/**
 	 * Decrements the index
 	 */
 	public void decrementIndex() {
 		index--;
 	}
 
 	/**
 	 * Returns true if the scanner has scanned all the words
 	 */
 	public boolean isFinished() {
 		return finished;
 	}
 }
