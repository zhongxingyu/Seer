 package org.opendarts.ui.x01.defi.pref;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.opendarts.core.model.player.IPlayer;
 import org.opendarts.core.service.player.IPlayerService;
 import org.opendarts.core.x01.defi.model.GameX01DefiDefinition;
 import org.opendarts.ui.x01.defi.OpenDartsUiX01DefiPlugin;
 
 /**
  * The Class ListUtils.
  */
 public final class PreferencesConverterUtils {
 
 	/** The SEPARATOR. */
 	private static final char SEPARATOR = '\n';
 
 	/** The Constant PLAYER_SERVICE. */
 	private static final IPlayerService PLAYER_SERVICE = OpenDartsUiX01DefiPlugin
 			.getService(IPlayerService.class);
 
 	/**
 	 * Instantiates a new list util.
 	 */
 	private PreferencesConverterUtils() {
 		super();
 	}
 
 	/**
 	 * Gets the list as string.
 	 *
 	 * @param stats the stats
 	 * @return the list as string
 	 */
 	public static String getListAsString(List<String> stats) {
 		StringBuilder result = new StringBuilder();
 		boolean isFirst = true;
 		for (String s : stats) {
 			if (isFirst) {
 				isFirst = false;
 			} else {
 				result.append(SEPARATOR);
 			}
 			result.append(s);
 		}
 		return result.toString();
 	}
 
 	/**
 	 * Gets the string as list.
 	 *
 	 * @param s the s
 	 * @return the string as list
 	 */
 	public static List<String> getStringAsList(String s) {
 		List<String> result = new ArrayList<String>();
 		StringTokenizer st = new StringTokenizer(s, "" + SEPARATOR);
 		String key;
 		while (st.hasMoreTokens()) {
 			key = st.nextToken();
 			result.add(key.trim());
 		}
 		return result;
 	}
 
 	/**
 	 * Gets the game definition as string.
 	 *
 	 * @param gameDef the game def
 	 * @return the game definition as string
 	 */
 	public static String getGameDefinitionAsString(GameX01DefiDefinition gameDef) {
 		String result = null;
 		if (gameDef != null) {
 			StringBuilder sb = new StringBuilder();
 			sb.append(gameDef.getStartScore());
 			sb.append(SEPARATOR);
 			boolean isFirst = true;
 			for (IPlayer player : gameDef.getInitialPlayers()) {
 				if (isFirst) {
 					isFirst = false;
 				} else {
 					sb.append(SEPARATOR);
 				}
 				sb.append(player.getUuid());
 			}
 
 			result = sb.toString();
 		}
 		return result;
 	}
 
 	/**
 	 * Gets the string as game definition.
 	 *
	 * @param s the s 
 	 * @return the string as game definition
 	 */
 	public static GameX01DefiDefinition getStringAsGameDefinition(String s) {
 		if (s != null) {
 			String[] strings = s.split("" + SEPARATOR);
			if (strings.length > 1) {
 				int startScore = Integer.valueOf(strings[0]);
 
 				List<IPlayer> players = new ArrayList<IPlayer>();
				if (strings.length > 2) {
					for (int i = 1; i < strings.length; i++) {
 						players.add(PLAYER_SERVICE.getPlayer(strings[i]));
 					}
 				}
 
 				return new GameX01DefiDefinition(startScore, players);
 			}
 		}
 		return null;
 	}
 }
