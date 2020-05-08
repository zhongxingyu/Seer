 package dbs.project.service;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import dbs.project.entity.Match;
 import dbs.project.entity.Player;
 import dbs.project.entity.Team;
 import dbs.project.service.event.LineUpEventService;
 import dbs.project.util.MatchMinute;
 import dbs.project.util.Substitution;
 import dbs.project.util.Tuple;
 
 public class TeamService {
 
 	/**
 	 * gets all playing players for a team in given match in a specified minute
 	 * 
 	 * @param match
 	 * @param team
 	 * @return
 	 */
 	public static List<Player> getPlayingPlayersInAMatchForTeam(Match match, Team team, MatchMinute minute) {
		List<Player> players = MatchService.getPlayingPlayersForMatch(match, minute);
		for(Player player : players)
			if(!player.getTeams().contains(team))
				players.remove(player);
 		
 		return players;
 	}
 
 	/**
 	 * gets all players which are on the bench
 	 * 
 	 * @param match
 	 * @param team
 	 * @return
 	 */
 	public static List<Player> getPlayersOnTheBench(Match match, Team team, MatchMinute minute) {
		List<Player> allPlayers = MatchService.getLineupByMatch(match);
		allPlayers.removeAll(getPlayingPlayersInAMatchForTeam(match, team , minute));
 		
 		return allPlayers;
 	}
 
 }
