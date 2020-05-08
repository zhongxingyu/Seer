 package yugi;
 
 import java.nio.charset.Charset;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 
 /**
  * Handles the configuration for the application.
  */
 public class Config {
 	
 	/**
 	 * This is used to bust the cache for JS and CSS.  Change this every time
 	 * you deploy to AppEngine.
 	 */
	public static final String VERSION = "28";
 	
 	/**
 	 * The set of servlets and their meta-data.
 	 */
 	public enum Servlet {
 		ADMIN_CARD("/admin/card"),
 		ADMIN_CARD_DELETE("/admin/card/delete"),
 		CARD("/card"),
 		CARD_IMAGE("/card/image"),
 		CREATE_GAME("/game/create"),
 		DECK("/deck"),
 		DECK_COPY("/deck/copy"),
 		DECK_DELETE("/deck/delete"),
 		DECK_EDITOR("/deck/editor"),
 		DECK_MANAGER("/deck/manager"),
 		DECK_VIEWER("/deck/viewer"),
 		DECKS("/decks"),
 		FILE("/file"),
 		JOIN_GAME("/game/join"),
 		JOIN_QUERY("/game/join/query"),
 		LANDING("/landing"),
 		MESSAGE("/m");
 		
 		private String path;
 		
 		private Servlet(String path) {
 			this.path = path;
 		}
 		
 		public String getPath() {
 			return path;
 		}
 	}
 	
 	/**
 	 * The URL parameters to the application.
 	 */
 	public enum UrlParameter {
 		CARD_KEY,     // The key for a card.
 		CARD_NAME,    // The name of a card.
 		DATA,         // Any data that might be a part of a request or post.
 		DECK_KEY,     // The key for a deck.
 		ERROR,        // The parameter used to identify the kind of error.
 		GAME_KEY,     // The game key
 		GAME_NAME,    // The name of the game.
 		IMAGE_FILE,   // The image file (used in card uploading).
 		MODE,         // The mode (such as "dev" for development)
 		PLAYER_NAME,  // The player's name.
 		STRUCTURE     // Distinguishes normal decks from structure decks.
 	}
 	
 	/**
 	 * The various modes of the application.
 	 */
 	public enum Mode {
 		RAW
 	}
 	
 	/**
 	 * The set of parameters to be replaced in HTML before sending it to the client.
 	 */
 	public enum HtmlParam {
 		CARD_KEY,
 		CHANNEL_TOKEN,
 		CSS_FILE_PATH,
 		DECK_KEY,
 		DECK_MANAGER_URL,
 		GAME_KEY,
 		JS_FILE_PATH,
 		PLAYER_NAME,
 		READ_ONLY,
 		SIGN_IN_OUT_URL,
 		UPLOAD_URL,
 		USER_JSON
 	}
 	
 	public enum CookieName {
 		PLAYER_ID
 	}
 	
 	public enum Error {
 		GAME_FULL,
 		GAME_NOT_FOUND
 	}
 	
 	/**
 	 * The UTF_8 charset.
 	 */
 	private static Charset UTF_8 = Charset.forName("UTF-8");
 	
 	/**
 	 * Gets the card name from the request, if there is one.
 	 * @param req The HTTP servlet request.
 	 * @return The card name or null if there was none.
 	 */
 	public static String getCardName(HttpServletRequest req) {
 		return getParam(req, UrlParameter.CARD_NAME);
 	}
 	
 	/**
 	 * Gets the game name from the request, if there is one.
 	 * @param req The request to check.
 	 * @return The game name in the request, or null if there isn't one.
 	 */
 	public static String getGameName(HttpServletRequest req) {
 		return getParam(req, UrlParameter.GAME_NAME);
 	}
 	
 	/**
 	 * Gets the game key from the request, if there is one.
 	 * @param req The request to check.
 	 * @return The game key in the request, or null if there isn't one.
 	 */
 	public static String getGameKey(HttpServletRequest req) {
 		return getParam(req, UrlParameter.GAME_KEY);
 	}
 	
 	/**
 	 * Gets the player name from the request, if there is one.
 	 * @param req The request to check.
 	 * @return The player name in the request, or null if there isn't one.
 	 */
 	public static String getPlayerName(HttpServletRequest req) {
 		return getParam(req, UrlParameter.PLAYER_NAME);
 	}
 	
 	/**
 	 * Gets the card key from the request, if there is one.
 	 * @param req The request to check.
 	 * @return The card key in the request, or null if there isn't one.
 	 */
 	public static String getCardKey(HttpServletRequest req) {
 		return getParam(req, UrlParameter.CARD_KEY);
 	}
 	
 	/**
 	 * Gets the deck key from the request, if there is one.
 	 * @param req The request to check.
 	 * @return The deck key in the request, or null if there isn't one.
 	 */
 	public static String getDeckKey(HttpServletRequest req) {
 		return getParam(req, UrlParameter.DECK_KEY);
 	}
 	
 	/**
 	 * @param req The request to check.
 	 * @return True if this request is a structure request or not.
 	 */
 	public static boolean isStructureRequest(HttpServletRequest req) {
 		String param = getParam(req, UrlParameter.STRUCTURE);
 		if (param != null) {
 			return Boolean.parseBoolean(param);
 		}
 		return false;
 	}
 	
 	/**
 	 * Gets the desired parameter from the request object.
 	 * @param req The http servlet request.
 	 * @param param The parameter to retrieve.
 	 * @return The parameter or null if there was none.
 	 */
 	public static String getParam(HttpServletRequest req, UrlParameter param) {
 
 		// There is a bug when dealing with Firefox (doesn't happen with Chrome
 		// for some reason).  You cannot call both req.getParameter() AND
 		// req.getReader() without getting an exception.  Yugioh code uses this
 		// whenever JSON is posted to a servlet, so to get around this, the
 		// method here will parse the raw query string instead of using
 		// req.getParameter.
 		//
 		// See this for more detail on the bug - there might be another fix:
 		// http://jira.codehaus.org/browse/JETTY-477
 		// and
 		// http://jira.codehaus.org/browse/JETTY-1291
 		
 		List<NameValuePair> pairs = URLEncodedUtils.parse(req.getQueryString(), UTF_8);
 		
 		for (NameValuePair pair : pairs) {
 			if (pair.getName().equalsIgnoreCase(param.toString())) {
 				String value = pair.getValue();
 				if (value != null) {
 					return value.trim();
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Determines if the request is asking for raw mode or not.
 	 * @param req The request to check.
 	 * @return True if in raw mode, false otherwise.
 	 */
 	public static boolean isRawMode(HttpServletRequest req) {
 		return isMode(req, Mode.RAW);
 	}
 	
 	/**
 	 * Checks to see if the mode is matched in the request.
 	 * @param req The request.
 	 * @param mode The mode for which to check.
 	 * @return True if the mode was requested, false otherwise.
 	 */
 	public static boolean isMode(HttpServletRequest req, Mode mode) {
 		return getMode(req) == mode;
 	}
 	
 	/**
 	 * Gets the mode from the request.
 	 * @param req The request.
 	 * @return The mode in the request or null if it couldn't be parsed.
 	 */
 	public static Mode getMode(HttpServletRequest req) {
 		String m = getParam(req, UrlParameter.MODE);
 		if (m != null) {
 			try {
 				return Mode.valueOf(m.toUpperCase());
 			} catch (Exception e) {
 				return null;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Replaces the parameter in the given HTML string with the value.
 	 * @param html The HTML that needs to replace the key with the value.
 	 * @param param The parameter to replace.
 	 * @param value The value with which to replace the parameter.
 	 * @return
 	 */
 	public static String replaceParam(String html, HtmlParam param, String value) {
 		return html.replaceAll("\\{\\{ " + param.toString() + " \\}\\}", value);
 	}
 }
