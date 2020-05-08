 package no.runsafe.nchat.chat;
 
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.nchat.database.IgnoreDatabase;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
public class IgnoreHandler
 {
 	public IgnoreHandler(IgnoreDatabase database)
 	{
 		this.database = database;
		ignoreList = database.getIgnoreList();
 	}
 
 	/**
 	 * Adds a player to the players ignore list.
 	 *
	 * @param player The player who will ignore.
 	 * @param ignorePlayer The player to be ignored.
 	 */
 	public void ignorePlayer(ICommandExecutor player, ICommandExecutor ignorePlayer)
 	{
 		database.ignorePlayer(player, ignorePlayer);
 		String ignorePlayerName = ignorePlayer.getName();
 
 		if (!ignoreList.containsKey(ignorePlayerName))
 			ignoreList.put(ignorePlayerName, new ArrayList<String>(1));
 
 		ignoreList.get(ignorePlayerName).add(player.getName());
 	}
 
 	/**
 	 * Removes a player from a players ignore list.
 	 *
	 * @param player The player to perform the removal.
 	 * @param ignorePlayer The player to be removed.
 	 */
 	public void removeIgnorePlayer(ICommandExecutor player, ICommandExecutor ignorePlayer)
 	{
 		database.removeIgnorePlayer(player, ignorePlayer);
 		String ignorePlayerName = ignorePlayer.getName();
 
 		if (ignoreList.containsKey(ignorePlayerName))
 		{
 			List<String> playersIgnoring = ignoreList.get(ignorePlayerName);
 			playersIgnoring.remove(player.getName());
 
 			// Check if we still have anyone ignoring the player, no need to keep empty lists.
 			if (playersIgnoring.isEmpty())
 				ignoreList.remove(ignorePlayerName);
 		}
 	}
 
 	/**
 	 * Checks to see if a player is ignoring another player.
 	 *
	 * @param player The player to check for.
 	 * @param ignorePlayer The player who is being ignored.
 	 * @return True if the player is being ignored.
 	 */
 	public boolean playerIsIgnoring(ICommandExecutor player, ICommandExecutor ignorePlayer)
 	{
 		String ignorePlayerName = ignorePlayer.getName();
 		return ignoreList.containsKey(ignorePlayerName) && ignoreList.get(ignorePlayerName).contains(player.getName());
 	}
 
 	/**
 	 * Returns a list of players who are ignoring a player.
 	 *
 	 * @param ignorePlayer The player to query for.
 	 * @return A list of player names.
 	 */
 	public List<String> getPlayersIgnoring(ICommandExecutor ignorePlayer)
 	{
 		String ignoreName = ignorePlayer.getName();
 		if (ignoreList.containsKey(ignoreName))
 			return ignoreList.get(ignoreName);
 
 		return new ArrayList<String>(0);
 	}
 
 	/**
 	 * Returns a list of players being ignored by a player.
 	 *
 	 * @param player The player who's list to return.
 	 * @return A list of players being ignored.
 	 */
 	public List<String> getIgnoredPlayers(ICommandExecutor player)
 	{
 		List<String> ignoredPlayers = new ArrayList<String>(0);
 
 		for (Map.Entry<String, List<String>> node : ignoreList.entrySet())
 			if (node.getValue().contains(player.getName()))
 				ignoredPlayers.add(node.getKey());
 
 		return ignoredPlayers;
 	}
 
 	private final IgnoreDatabase database;
 	private final HashMap<String, List<String>> ignoreList;
 }
