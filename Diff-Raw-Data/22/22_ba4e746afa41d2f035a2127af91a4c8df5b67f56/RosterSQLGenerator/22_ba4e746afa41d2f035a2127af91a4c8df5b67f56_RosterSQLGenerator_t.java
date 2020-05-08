 package codeGenerator;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import jsonObjects.boxScoreObjects.InactiveJson;
 import jsonObjects.boxScoreObjects.PlayerStatsJson;
 
 
 import nba.Player;
 
 public class RosterSQLGenerator 
 {
 	private ArrayList<Player> homeStarters, awayStarters,
 								homeBench, awayBench,
 								homeInactive, awayInactive,
 								homeDNP, awayDNP,
 								homeMatching, awayMatching,
 								splitMatching;
 	private int homeID, awayID;
 	private int gameID;
 	
 	public RosterSQLGenerator(int homeID, int awayID, int gameID, ArrayList<InactiveJson> inactive,
 								ArrayList<PlayerStatsJson> players)
 	{
 		this.homeStarters = parseStarters(homeID, players);
 		this.homeBench = parseBench(homeID, players);
 		this.homeInactive = parseInactive(homeID, inactive);
 		this.awayStarters = parseStarters(awayID, players);
 		this.awayBench = parseBench(awayID, players);
 		this.awayInactive = parseInactive(awayID, inactive);
 		this.homeDNP = parseDNP(homeID, players);
 		this.awayDNP = parseDNP(awayID, players);
 		this.homeMatching = findMatchingHomePlayers();
 		this.awayMatching = findMatchingAwayPlayers();
 		this.splitMatching = findMatchingPlayers();
 		this.homeID = homeID;
 		this.awayID = awayID;
 		this.gameID = gameID;
 	}
 
 	public ArrayList<Player> getHomeStarters() { return homeStarters; }
 	public ArrayList<Player> getAwayStarters() { return awayStarters; }
 	public ArrayList<Player> getHomeBench() { return homeBench; }
 	public ArrayList<Player> getAwayBench() { return awayBench; }
 	public ArrayList<Player> getHomeInactive() { return homeInactive; }
 	public ArrayList<Player> getAwayInactive() { return awayInactive; }
 	public ArrayList<Player> getHomeDNP() { return homeDNP; }
 	public ArrayList<Player> getAwayDNP() { return awayDNP; }
 	public ArrayList<Player> gethomeMatching() { return homeMatching; }
 	public ArrayList<Player> getawayMatching() { return awayMatching; }
 	public ArrayList<Player> getsplitMatching() { return splitMatching; }
 	public int getHomeID() { return this.homeID; }
 	public int getAwayID() { return this.awayID; }
 	
 	public ArrayList<Player> getHomeTeam()
 	{
 		ArrayList<Player> teamPlayers = new ArrayList<Player>(homeStarters);
 		teamPlayers.addAll(homeBench);
 		teamPlayers.addAll(homeInactive);
 		return teamPlayers;
 	}
 	
 	public ArrayList<Player> getHomeActive()
 	{
 		ArrayList<Player> teamPlayers = new ArrayList<Player>(homeStarters);
 		teamPlayers.addAll(homeBench);
 		teamPlayers.removeAll(homeDNP);
 		return teamPlayers;
 	}
 	
 	public ArrayList<Player> getAwayTeam()
 	{
 		ArrayList<Player> teamPlayers = new ArrayList<Player>(awayStarters);
 		teamPlayers.addAll(awayBench);
 		teamPlayers.addAll(awayInactive);
 		return teamPlayers;
 	}
 	
 	public ArrayList<Player> getAwayActive()
 	{
 		ArrayList<Player> teamPlayers = new ArrayList<Player>(awayStarters);
 		teamPlayers.addAll(awayBench);
 		teamPlayers.removeAll(awayDNP);
 		return teamPlayers;
 	}
 	
 	public ArrayList<Player> getActive()
 	{
 		ArrayList<Player> teamPlayers = new ArrayList<Player>(awayStarters);
 		teamPlayers.addAll(awayBench);
 		teamPlayers.addAll(homeStarters);
 		teamPlayers.addAll(homeBench);
 		teamPlayers.removeAll(homeDNP);
 		teamPlayers.removeAll(awayDNP);
 		return teamPlayers;
 	}
 	
 	private ArrayList<Player> findMatchingHomePlayers()
 	{
 		ArrayList<Player> tempHome = new ArrayList<Player>(this.getHomeActive());
 		ArrayList<Player> matchedPlayers = new ArrayList<Player>();
 		ArrayList<Player> matchingPlayers = new ArrayList<Player>();
 		for (Player player : tempHome)
 		{
 			matchedPlayers = getMatchingPlayers(tempHome, player);
			if(matchedPlayers.size() > 1)
 			{
 				for(Player p : matchedPlayers)
 				{
 					if (!matchingPlayers.contains(p))
 					{
 							matchingPlayers.add(p);
 					}
 						
 				}
 				if (!matchingPlayers.contains(player))
 					matchingPlayers.add(player);
 			}
 		}
 		
 		return matchingPlayers;
 	}
 	
 	private ArrayList<Player> findMatchingAwayPlayers()
 	{
 		ArrayList<Player> tempHome = new ArrayList<Player>(this.getAwayActive());
 		ArrayList<Player> matchedPlayers = new ArrayList<Player>();
 		ArrayList<Player> matchingPlayers = new ArrayList<Player>();
 		for (Player player : tempHome)
 		{
 			matchedPlayers = getMatchingPlayers(tempHome, player);
			if(matchedPlayers.size() > 1)
 			{
 				for(Player p : matchedPlayers)
 				{
 					if (!matchingPlayers.contains(p))
 					{
 							matchingPlayers.add(p);
 					}
 						
 				}
 				if (!matchingPlayers.contains(player))
 					matchingPlayers.add(player);
 			}
 		}
 		
 		return matchingPlayers;
 	}
 	
 	private ArrayList<Player> findMatchingPlayers()
 	{
 		ArrayList<Player> tempHome = new ArrayList<Player>(this.getActive());
 		ArrayList<Player> matchedPlayers = new ArrayList<Player>();
 		ArrayList<Player> matchingPlayers = new ArrayList<Player>();
 		for (Player player : tempHome)
 		{
 			matchedPlayers = getMatchingPlayers(tempHome, player);
			if(matchedPlayers.size() > 1)
 			{
 				for(Player p : matchedPlayers)
 				{
 					if (!matchingPlayers.contains(p))
 					{
 							matchingPlayers.add(p);
 					}
 						
 				}
 				if (!matchingPlayers.contains(player))
 					matchingPlayers.add(player);
 			}
 		}
 		
 		return matchingPlayers;
 	}
 	
 	public static ArrayList<Player> getMatchingPlayers(ArrayList<Player> possiblePlayers,
 												 Player player)
 	{
 		String[] playerNameArray;
 		Player tempPlayer = new Player("Dummy", -1);
 		ArrayList<Player> matchingPlayers = new ArrayList<Player>();
		ArrayList<Player> tempPossiblePlayers = new ArrayList<Player>(possiblePlayers);
 		
 		playerNameArray = cleanPlayerName(player.getPlayerName());
 		
 		while(tempPlayer != null)
 		{
			tempPlayer = searchPlayerText(tempPossiblePlayers, playerNameArray);
 			if(tempPlayer != null)
 			{
				tempPossiblePlayers.remove(tempPlayer);
 				matchingPlayers.add(tempPlayer);
 			}
 		}
 		
 		return matchingPlayers;
 	}
 	
 	public boolean searchHomePlayers(Player player)
 	{
 		return getHomeActive().contains(player);
 	}
 	
 	public boolean searchAwayPlayers(Player player)
 	{
 		return getAwayActive().contains(player);
 	}
 	
 	public boolean searchPlayers(Player player)
 	{
 		return getActive().contains(player);
 	}
 	
 	private static String[] cleanPlayerName(String playerName)
 	{
 		//removes any player names and white space from the player's name
 		//returns a space delimited string array of the parts of the name
 		String tempPlayerName;
 		
 		tempPlayerName = playerName.replace('.', ' ');
 		tempPlayerName = tempPlayerName.trim();
 		return tempPlayerName.split(" ");
 	}
 	
 	private static Player searchPlayerText(ArrayList<Player> players, String[] playerName)
 	{
 		//looks at the last name part of playerName, and each player in players
 		//returns the first matching player from players
 		boolean found = false;
 		String[] reversedName, beingSearched, currentPlayerName;
 		
 		reversedName = new String[playerName.length];
 		for (int i =0; i < playerName.length; i++)
 		{
 			reversedName[i] = playerName[playerName.length - (1 + i)];
 		}
 		
 		for(Player player : players)
 		{
 			currentPlayerName = cleanPlayerName(player.getPlayerName());
 			beingSearched = new String[currentPlayerName.length];
 			for (int i =0; i < currentPlayerName.length; i++)
 			{
 				beingSearched[i] = 
 						currentPlayerName[currentPlayerName.length - (1 + i)];
 			}
 			found = reversedName[0].equals(
 					beingSearched[0]);
 					
 			if (found)
 			{
 				for(String namePart : playerName)
 				{
 					if (player.getPlayerName().contains(namePart))
 					{
 						found = true;
 					}
 					else
 					{
 						found = false;
 						break;
 					}
 				}
 			}
 			if (found)
 			{
 				return player;
 			}
 		}
 		
 		return null;
 	}
 	
 	private ArrayList<Player> parsePlayers(int teamID, 
 			ArrayList<PlayerStatsJson> players, 
 			PlayerParser<PlayerStatsJson> playerParser)
 	{
 		ArrayList<Player> starters = new ArrayList<Player>();
 		
 		for(PlayerStatsJson player : players)
 		{
 			if(playerParser.check(teamID, player))
 				starters.add(new Player(player.getPlayerName(), 
 										player.getPlayerID()));
 		}
 		
 		return starters;
 	}
 	
 	private ArrayList<Player> parseInactivePlayers(int teamID, 
 			ArrayList<InactiveJson> players, 
 			PlayerParser<InactiveJson> playerParser)
 	{
 		ArrayList<Player> inactives = new ArrayList<Player>();
 		
 		for(InactiveJson player : players)
 		{
 			if(playerParser.check(teamID, player))
 				inactives.add(new Player(player.getFirstName() + " " +
 										player.getLastName(),
 										player.getPlayerID()));
 		}
 		
 		return inactives;
 	}
 	
 	public class CheckStarters implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return (player.getTeamID() == teamID) && 
 				(!player.getStartPosition().equals(""));
 		}
 	}
 	
 	public class CheckBench implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return (player.getTeamID() == teamID) &&
 					(player.getStartPosition().equals(""));
 		}
 	}
 	
 	public class CheckDNP implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return (player.getTeamID() == teamID) && 
 					(player.getComment().contains("DNP"));
 		}
 	}
 	
 	public class CheckTeam implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return player.getTeamID() == teamID;
 		}
 	}
 	
 	public class CheckInactive implements PlayerParser<InactiveJson>
 	{
 		@Override
 		public boolean check(int teamID, InactiveJson player) 
 		{
 			return player.getTeamID() == teamID;
 		}
 	}
 	
 	private ArrayList<Player> parseStarters(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckStarters());
 	}
 	
 	private ArrayList<Player> parseBench(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckBench());
 	}
 	
 	private ArrayList<Player> parseDNP(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckDNP());
 	}
 	
 	private ArrayList<Player> parseInactive(int teamID,
 			ArrayList<InactiveJson> players)
 	{
 		return parseInactivePlayers(teamID, players, new CheckInactive());
 	}
 	
 	public void compile(String path,
 			String userName, String password)
 	{
 		Connection conn;
 		PreparedStatement stmt;
 		
 		try 
 		{
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(path,userName,password);
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`game_players` (`game_id`,`team_id`,`player_id`," +
 					"`active`) VALUES (?,?,?,?);");
 			
 			for (Player player : homeStarters)
 			{
 				stmt.setInt(1, this.gameID);
 				stmt.setInt(2, this.homeID);
 				stmt.setInt(3, player.getPlayerID());
 				stmt.setBoolean(4, true);
 				stmt.executeUpdate();
 			}
 			
 			for (Player player : homeBench)
 			{
 				stmt.setInt(1, this.gameID);
 				stmt.setInt(2, this.homeID);
 				stmt.setInt(3, player.getPlayerID());
 				stmt.setBoolean(4, true);
 				stmt.executeUpdate();
 			}
 			
 			for (Player player : homeInactive)
 			{
 				stmt.setInt(1, this.gameID);
 				stmt.setInt(2, this.homeID);
 				stmt.setInt(3, player.getPlayerID());
 				stmt.setBoolean(4, false);
 				stmt.executeUpdate();
 			}
 			
 			for (Player player : awayStarters)
 			{
 				stmt.setInt(1, this.gameID);
 				stmt.setInt(2, this.awayID);
 				stmt.setInt(3, player.getPlayerID());
 				stmt.setBoolean(4, true);
 				stmt.executeUpdate();
 			}
 			
 			for (Player player : awayBench)
 			{
 				stmt.setInt(1, this.gameID);
 				stmt.setInt(2, this.awayID);
 				stmt.setInt(3, player.getPlayerID());
 				stmt.setBoolean(4, true);
 				stmt.executeUpdate();
 			}
 			
 			for (Player player : awayInactive)
 			{
 				stmt.setInt(1, this.gameID);
 				stmt.setInt(2, this.awayID);
 				stmt.setInt(3, player.getPlayerID());
 				stmt.setBoolean(4, false);
 				stmt.executeUpdate();
 			}
 			
 			stmt.close();
 			conn.close();
 			
 		} 
 		catch (ClassNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
