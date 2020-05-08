 package core;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * The different keywords the system recognizes. Each keyword needs a brief
  * definition to be used when the built-in "help" keyword is queried. This class
  * follows the Singleton pattern and and duplicates some
  * Collection&lt;String&gt; functionality.
  *<p>
  * Example keywords could be:
  * 
  * <pre>
  * square
  * lc
  * law
  * fred
  * </pre>
  */
 public class Keywords {
 
 	/** The keyword, definition map that everything is stored in. */
 	private final Map<String, String> words;
 	
 	/** The global instance. */
 	private static Keywords instance;
 	
 	/** Preset keywords */
 	public static final String KEY_HELP = "help", KEY_ALL = "hour";
 	
 	protected Keywords() {
 		words = new TreeMap<String, String>();
 		words.put(KEY_HELP, "how to use the service");
 		words.put(KEY_ALL, "all stops in the next hour");
 	}
 	
 	/**
 	 * Add a keyword to the global list.
 	 * 
 	 * @param keyword
 	 *            the keyword to be added
 	 * @param description
 	 *            a <em>brief</em> description of what the keyword should return
 	 */
 	public void add(String keyword, String description) {
 		words.put(keyword.trim().toLowerCase(), description);
 	}
 
 	/**
 	 * Returns true if the keyword is recognized by the system
 	 * 
 	 * @param word
 	 *            the word in question
 	 * @return true if the parameter is recognized
 	 */
 	public boolean contains(String word) {
 		return words.containsKey(word.toLowerCase());
 	}
 
 	/** @return all the definitions of keywords */
 	public Collection<String> definitions() {
 		return words.values();
 	}
 
 	/**
 	 * Extracts a keyword from any input string if there is one or null if there
 	 * isn't a keyword.
 	 * 
 	 * @param message
 	 *            the raw message to be parsed
 	 * @return the keyword from the message or null
 	 */
 	public String extract(String message) {
 		String first = message.split(" ")[0].trim();
 		for (String k : words()) {
 			if (k.equalsIgnoreCase(first)) {
 				return k;
 			}
 		}
 		return null;
 	}
 
 	/**
	 * Returns the definition associated with the keyword or null if the keyword
 	 * is not one recognized by the system
 	 * 
 	 * @param keyword
 	 * @return
 	 */
 	public String getDefinition(String keyword) {
 		return words.get(keyword);
 	}
 
 	/** @return all keywords the system recognizes */
 	public Collection<String> words() {
 		return words.keySet();
 	}
 
 	public static Keywords instance() {
 		if (instance == null) {
 			instance = new Keywords();
 		}
 		return instance;
 	}
 }
