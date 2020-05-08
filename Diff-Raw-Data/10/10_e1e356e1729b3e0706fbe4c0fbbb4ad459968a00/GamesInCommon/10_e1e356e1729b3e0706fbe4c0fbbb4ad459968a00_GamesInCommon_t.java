 package gamesincommon;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
 import com.github.koraktor.steamcondenser.steam.community.SteamGame;
 import com.github.koraktor.steamcondenser.steam.community.SteamId;
 
 public class GamesInCommon {
 
 	public GamesInCommon() {
 
 		try {
 		  
 		  List<Set<Map.Entry<Integer, SteamGame>>> userGames = new ArrayList<Set<Map.Entry<Integer, SteamGame>>>();
 		  
 			userGames.add(getGames(SteamId.create("HSAR")));
 			userGames.add(getGames(SteamId.create("yammy24")));
 			
 			Set<Map.Entry<Integer, SteamGame>> commonGames = mergeSets(userGames);
 			
 			// Lists games in common.
 			for (Entry<Integer, SteamGame> i : commonGames) {
 				System.out.println(i.getValue().getName());
 			}
 			
 		} catch (SteamCondenserException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	/**
 	 * Finds all games from the given steam user.
 	 * @param sId The SteamId of the user to get games from.
 	 * @return A set of all games for the give user.
 	 * @throws SteamCondenserException
 	 */
 	public Set<Map.Entry<Integer, SteamGame>> getGames(SteamId sId) throws SteamCondenserException {
 		return sId.getGames().entrySet();
 	}
 	
 	/**
 	 * Merges multiple user game sets together to keep all games that are the same.
	 * @param userGames A list of user game sets. There must be at least one set in this list.
 	 * @return A set containing all common games.
 	 */
 	public Set<Map.Entry<Integer, SteamGame>> mergeSets(List<Set<Map.Entry<Integer, SteamGame>>> userGames) {
 	  
	  Set<Map.Entry<Integer, SteamGame>> result = userGames.get(0);
 	  
	  for (int i = 1; i < userGames.size(); i++) {
 	    result.retainAll(userGames.get(i));
 	  }
 	  
 	  return result;
 	  
 	}
 
 	public static void main(String[] args) {
 		GamesInCommon gic = new GamesInCommon();
 	}
 
 }
