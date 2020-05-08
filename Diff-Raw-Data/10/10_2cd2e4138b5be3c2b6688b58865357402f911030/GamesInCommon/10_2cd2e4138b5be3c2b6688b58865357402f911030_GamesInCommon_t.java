 package gamesincommon;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JTextArea;
 
 import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
 import com.github.koraktor.steamcondenser.steam.community.SteamGame;
 import com.github.koraktor.steamcondenser.steam.community.SteamId;
 
 public class GamesInCommon {

 	JFrame mainFrame;
 
 	public GamesInCommon() {
 		mainFrame = new JFrame();
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainFrame.setLocationRelativeTo(null);
 		MainPanel mainPanel = new MainPanel(this);
 		mainFrame.add(mainPanel);
 		mainFrame.pack();
 		mainFrame.setVisible(true);
 	}
 
 	/**
 	 * Finds common games between an arbitrarily long list of users
 	 * 
 	 * @param users
 	 *            A list of names to find common games for.
 	 * @return A collection of games common to all users
 	 */
 	public Collection<SteamGame> findCommonGames(List<String> users) {
 
 		List<Collection<SteamGame>> userGames = new ArrayList<Collection<SteamGame>>();
 
 		for (String name : users) {
 			try {
 				userGames.add(getGames(SteamId.create(name)));
 			} catch (SteamCondenserException e) {
 				e.printStackTrace();
 			}
 		}
 
 		Collection<SteamGame> commonGames = mergeSets(userGames);
 		return commonGames;
 	}
 
 	/**
 	 * Displays all games from a collection on console output
 	 * 
 	 * @param games
 	 *            The collection to print
 	 */
 	public void displayCommonGames(Collection<SteamGame> games) {
 		// Lists games in common.
 		for (SteamGame i : games) {
 			System.out.println(i.getName());
 		}
 		// Final count
 		System.out.println("Total games in common: " + games.size());
 	}
 
 	/**
 	 * Displays all games from a collection in a new graphical interface frame.
 	 * 
 	 * @param games
 	 *            Games to show.
 	 */
 	public void showCommonGames(Collection<SteamGame> games) {
 		// Create frame object
 		JFrame displayFrame = new JFrame();
 		displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		displayFrame.setLocationRelativeTo(mainFrame);
 		// Display content
 		JTextArea displayArea = new JTextArea();
		displayFrame.add(displayArea);
 		displayArea.setEditable(false);
 		for (SteamGame i : games) {
			displayArea.append(i.getName() + "\n");
 		}
 		// Final count
 		displayArea.append("Total games in common: " + games.size());
		displayFrame.pack();
		displayFrame.setVisible(true);
 
 	}
 
 	/**
 	 * Creates a list of users from user input.
 	 * 
 	 * @return The list of user names.
 	 */
 	public List<String> getUsers() {
 
 		List<String> users = new ArrayList<String>();
 
 		System.out.println("Enter users one by one, typing 'FIN' when complete:");
 
 		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in));) {
 
 			String input;
 			input = br.readLine();
 
 			while (!input.equals("FIN")) {
 
 				users.add(input);
 				input = br.readLine();
 
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return users;
 	}
 
 	/**
 	 * Finds all games from the given steam user.
 	 * 
 	 * @param sId
 	 *            The SteamId of the user to get games from.
 	 * @return A set of all games for the give user.
 	 * @throws SteamCondenserException
 	 */
 	public Collection<SteamGame> getGames(SteamId sId) throws SteamCondenserException {
 		return sId.getGames().values();
 	}
 
 	/**
 	 * Returns games that match one or more of the given filter types
 	 * 
 	 * @param gameList
 	 *            Collection of games to be filtered.
 	 * @param filterList
 	 *            Collection of filters to apply.
 	 * @return A collection of games matching one or more of filters.
 	 */
 	public Collection<SteamGame> filterGames(Collection<SteamGame> gameList, List<FilterType> filterList) {
 
 		Collection<SteamGame> result = new HashSet<SteamGame>();
 
 		for (SteamGame game : gameList) {
 			System.out.println("Checking game '" + game.getName() + "'");
 			try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(
 					"http://store.steampowered.com/api/appdetails/?appids=" + game.getAppId()).openStream()));) {
 				// Read lines in until there are no more to be read, run filter on each line looking for specified package IDs.
 
 				String line;
 
 				while (((line = br.readLine()) != null) && (!result.contains(game))) {
 					for (FilterType filter : filterList) {
 						if (line.contains(filter.getValue())) {
 							result.add(game);
 						}
 					}
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 		return result;
 	}
 
 	/**
 	 * Merges multiple user game sets together to keep all games that are the same.
 	 * 
 	 * @param userGames
 	 *            A list of user game sets. There must be at least one set in this list.
 	 * @return A set containing all common games.
 	 */
 	public Collection<SteamGame> mergeSets(List<Collection<SteamGame>> userGames) {
 
 		Collection<SteamGame> result = userGames.get(0);
 
 		for (int i = 1; i < userGames.size(); i++) {
 			result.retainAll(userGames.get(i));
 		}
 
 		return result;
 
 	}
 
 	public static void main(String[] args) {
 
 		GamesInCommon gamesInCommon = new GamesInCommon();
 		// List<String> users = gamesInCommon.getUsers();
 		// gamesInCommon.displayCommonGames(gamesInCommon.findCommonGames(users));
 
 	}
 }
