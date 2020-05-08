 package codeGenerator;
 
 import java.io.BufferedReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import jsonObjects.BoxJson;
 import jsonObjects.PBPJson;
 import jsonObjects.boxScoreObjects.InactiveJson;
 import jsonObjects.boxScoreObjects.PlayerStatsJson;
 
 import nba.play.Play;
 import nba.playType.foul.Foul;
 import nba.playType.freeThrow.FreeThrow;
 import nba.playType.rebound.Rebound;
 import nba.playType.shot.Shot;
 import nba.playType.turnover.Turnover;
 import nbaDownloader.NBADownloader;
 import nba.playType.block.*;
 import nba.playType.steal.*;
 import nba.playType.substitution.Substitution;
 
 import nba.PlayRole;
 import nba.Player;
 
 public class RosterSQLGenerator 
 {
 	private ArrayList<Player> homeStarters, awayStarters,
 								homeBench, awayBench,
 								homeInactive, awayInactive,
 								homeDNP, awayDNP;
 	private int homeID, awayID;
 	private int gameID;
 	private String nbaGameID;
 	private ArrayList<PBPJson> pbp;
 	private ArrayList<Player> subbedPlayers;
 	
 	public RosterSQLGenerator(int homeID, int awayID, int gameID, ArrayList<InactiveJson> inactive,
 								ArrayList<PlayerStatsJson> players, ArrayList<PBPJson> pbp, String nbaGameID)
 	{
 		this.homeStarters = parseStarters(homeID, players);
 		this.homeBench = parseBench(homeID, players);
 		this.homeInactive = parseInactive(homeID, inactive);
 		this.awayStarters = parseStarters(awayID, players);
 		this.awayBench = parseBench(awayID, players);
 		this.awayInactive = parseInactive(awayID, inactive);
 		this.homeDNP = parseDNP(homeID, players);
 		this.awayDNP = parseDNP(awayID, players);
 		this.homeID = homeID;
 		this.awayID = awayID;
 		this.gameID = gameID;
 		this.nbaGameID = nbaGameID;
 		this.pbp = new ArrayList<PBPJson>(pbp);
 		this.subbedPlayers = new ArrayList<Player>();
 		Collections.sort(this.pbp, PBPJson.COMPARE_BY_PLAY_ID);
 	}
 
 	public ArrayList<Player> getHomeStarters() { return homeStarters; }
 	public ArrayList<Player> getAwayStarters() { return awayStarters; }
 	public ArrayList<Player> getHomeBench() { return homeBench; }
 	public ArrayList<Player> getAwayBench() { return awayBench; }
 	public ArrayList<Player> getHomeInactive() { return homeInactive; }
 	public ArrayList<Player> getAwayInactive() { return awayInactive; }
 	public ArrayList<Player> getHomeDNP() { return homeDNP; }
 	public ArrayList<Player> getAwayDNP() { return awayDNP; }
 	public int getHomeID() { return this.homeID; }
 	public int getAwayID() { return this.awayID; }
 	public ArrayList<Player> getSubbedPlayers() { return subbedPlayers; }
 	
 	public void resetSubbedPlayers()
 	{
 		this.subbedPlayers = new ArrayList<Player>();
 	}
 	
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
 	
 	private ArrayList<Player> getMatchingPlayers(ArrayList<Player> possiblePlayers,
 												 Player player)
 	{
 		String[] playerNameArray;
 		Player tempPlayer = new Player("Dummy", -1);
 		ArrayList<Player> matchingPlayers = new ArrayList<Player>();
 		
 		playerNameArray = cleanPlayerName(player.getPlayerName());
 		
 		while(tempPlayer != null)
 		{
 			tempPlayer = searchPlayerText(possiblePlayers, playerNameArray);
 			if(tempPlayer != null)
 			{
 				possiblePlayers.remove(tempPlayer);
 				matchingPlayers.add(tempPlayer);
 			}
 		}
 		
 		return matchingPlayers;
 	}
 	
 	public void setPlayer(Player player, Play currentPlay, PlayRole role,
 			boolean isAssist)
 	{
 		ArrayList<Player> tempPlayers, matchingPlayers;
 		
 		if (role.equals(PlayRole.HOME))
 		{
 			tempPlayers = new ArrayList<Player>(getHomeActive());
 		}
 		else if (role.equals(PlayRole.AWAY))
 		{
 			tempPlayers = new ArrayList<Player>(getAwayActive());
 		}
 		else
 		{
 			tempPlayers = new ArrayList<Player>(getActive());
 		}
 		
 		matchingPlayers = getMatchingPlayers(tempPlayers, player);
 		
 		if (matchingPlayers.size() < 1)
 		{
 			System.out.println("Could not find player: " + player.getPlayerName());
 			player.setPlayerID(-1);
 		}
 		else if (matchingPlayers.size() == 1)
 		{
 			player.setPlayerID(matchingPlayers.get(0).getPlayerID());
 			player.setPlayerName(matchingPlayers.get(0).getPlayerName());
 		}
 		else
 		{
 			duplicateSearch(player, currentPlay, role, isAssist);
 		}
 	}
 	
 	public void setPlayer(Player player, Play currentPlay, PlayRole role,
 							PBPJson nextActivePlay, PBPJson previousActivePlay)
 	{
 		ArrayList<Player> tempPlayers, matchingPlayers;
 		
 		if (role.equals(PlayRole.HOME))
 		{
 			tempPlayers = new ArrayList<Player>(getHomeActive());
 		}
 		else if (role.equals(PlayRole.AWAY))
 		{
 			tempPlayers = new ArrayList<Player>(getAwayActive());
 		}
 		else
 		{
 			tempPlayers = new ArrayList<Player>(getActive());
 		}
 		
 		matchingPlayers = getMatchingPlayers(tempPlayers, player);
 		
 		if (matchingPlayers.size() < 1)
 		{
 			System.out.println("Could not find player: " + player.getPlayerName());
 			player.setPlayerID(-1);
 		}
 		else if (matchingPlayers.size() == 1)
 		{
 			player.setPlayerID(matchingPlayers.get(0).getPlayerID());
 			player.setPlayerName(matchingPlayers.get(0).getPlayerName());
 		}
 		else
 		{
 			duplicateSearch(player, currentPlay, role, nextActivePlay, previousActivePlay);
 		}
 	}
 	
 	public boolean searchHomePlayers(Player player)
 	{
 		return getHomeActive().contains(player);
 	}
 	
 	private void duplicateSearch(Player player, Play currentPlay, PlayRole role, 
 			boolean isAssist)
 	{
 		int index, startTime, endTime;
 		PBPJson relevantPlay = new PBPJson();
 		ArrayList<PlayerStatsJson> pbpData;
 		ArrayList<Player> possiblePlayers, matchingPlayers;
 		
 		relevantPlay.setEventNum(currentPlay.getPlayID());
 		index = Collections.binarySearch(this.pbp, relevantPlay, 
 				PBPJson.COMPARE_BY_PLAY_ID);
 		
 		if (index == -1)
 		{
 			System.out.println("Game: " + this.gameID + " " +
 					"Play: " + currentPlay.getPlayID() + 
 					" Play not found.");
 			System.exit(-1);
 		}
 		else
 		{
 			relevantPlay = this.pbp.get(index);
 		}
 		
 		startTime = convertStringTime(relevantPlay.getGameTime());
 		startTime += addPeriodTime(relevantPlay.getPeriod());
 		startTime -= 10;
 		if (startTime == -10)
 		{
 			startTime = 0;
 		}
 		endTime = convertStringTime(relevantPlay.getGameTime());
 		endTime += addPeriodTime(relevantPlay.getPeriod());
 		endTime += 10;
 		
 		pbpData = downloadCustomBoxScore(startTime, endTime);
 		
 		if (role.equals(PlayRole.HOME))
 		{
 			possiblePlayers = parseTeam(homeID, pbpData);
 		}
 		else if (role.equals(PlayRole.AWAY))
 		{
 			possiblePlayers = parseTeam(awayID, pbpData);
 		}
 		else
 		{
 			possiblePlayers = parseTeam(homeID, pbpData);
 			possiblePlayers.addAll(parseTeam(awayID, pbpData));
 		}
 		
 		matchingPlayers = getMatchingPlayers(possiblePlayers, player);
 		
 		if (matchingPlayers.size() < 1)
 		{
 			System.out.println("Could not find player on 2nd pass: " 
 								+ player.getPlayerName());
 			player.setPlayerID(-1);
 		}
 		else if (matchingPlayers.size() == 1)
 		{
 			player.setPlayerID(matchingPlayers.get(0).getPlayerID());
 			player.setPlayerName(matchingPlayers.get(0).getPlayerName());
 		}
 		else
 		{
 			parsePlayData(player, currentPlay, role, pbpData, isAssist);
 		}
 	}
 	
 	private void duplicateSearch(Player player, Play currentPlay, PlayRole role,
 							PBPJson nextActivePlay, PBPJson previousActivePlay)
 	{
 		int index, startTime, endTime;
 		PBPJson relevantPlay = new PBPJson();
 		ArrayList<PlayerStatsJson> pbpData;
 		ArrayList<Player> playersOnFloor, playersOnBench, matchingPlayers;
 		Substitution sub = (Substitution)currentPlay.getPlayType();
 		
 		playersOnFloor = new ArrayList<Player>();
 		playersOnBench = new ArrayList<Player>();
 		matchingPlayers = new ArrayList<Player>();
 		relevantPlay.setEventNum(currentPlay.getPlayID());
 		index = Collections.binarySearch(this.pbp, relevantPlay, 
 				PBPJson.COMPARE_BY_PLAY_ID);
 		
 		if (index == -1)
 		{
 			System.out.println("Game: " + this.gameID + " " +
 					"Play: " + currentPlay.getPlayID() + 
 					" Play not found.");
 			System.exit(-1);
 		}
 		else
 		{
 			relevantPlay = this.pbp.get(index);
 		}
 		
 		startTime = convertStringTime(relevantPlay.getGameTime());
 		startTime += addPeriodTime(relevantPlay.getPeriod());
 		startTime -= 10;
 		if (startTime == -10)
 		{
 			startTime = 0;
 		}
 		endTime = convertStringTime(relevantPlay.getGameTime());
 		endTime += addPeriodTime(relevantPlay.getPeriod());
 		endTime += 10;
 		
 		pbpData = downloadCustomBoxScore(startTime, endTime);
 		
 		if (role.equals(PlayRole.HOME))
 		{
 			playersOnFloor = parseTeam(homeID, pbpData);
 			playersOnBench = new ArrayList<Player>(getHomeActive());
 			playersOnBench.removeAll(playersOnFloor);
 		}
 		else if (role.equals(PlayRole.AWAY))
 		{
 			playersOnFloor = parseTeam(awayID, pbpData);
 			playersOnBench = new ArrayList<Player>(getAwayActive());
 			playersOnBench.removeAll(playersOnFloor);
 		}
 		else
 		{
 			//TODO error, no neutral sub
 		}
 		
 		if (playersOnFloor.size() == 5)
 		{
 			if (sub.getIn().equals(player))
 			{
 				matchingPlayers = getMatchingPlayers(playersOnBench, player);
 			}
 			else if (sub.getOut().equals(player))
 			{
 				matchingPlayers = getMatchingPlayers(playersOnFloor, player);
 			}
 			else
 			{
 				//TODO error, player not found
 			}
 		}
 		else if (playersOnFloor.size() > 5)
 		{
 			if (sub.getIn().equals(player) | sub.getOut().equals(player))
 			{
 				parsePlayData(player, currentPlay, role, pbpData, nextActivePlay,
 						previousActivePlay);
 				return;
 			}
 			else
 			{
 				//TODO error, player not found
 			}
 		}
 		else
 		{
 			//TODO less than 5 players on floor
 		}
 		
 		if (matchingPlayers.size() < 1)
 		{
 			System.out.println("Could not find player on 2nd pass: " 
 								+ player.getPlayerName());
 			player.setPlayerID(-1);
 		}
 		else if (matchingPlayers.size() == 1)
 		{
 			player.setPlayerID(matchingPlayers.get(0).getPlayerID());
 			player.setPlayerName(matchingPlayers.get(0).getPlayerName());
 		}
 		else
 		{
 			parsePlayData(player, currentPlay, role, pbpData, nextActivePlay,
 					previousActivePlay);
 		}
 	}
 	
 	private void parsePlayData(Player player, Play currentPlay, PlayRole role,
 								ArrayList<PlayerStatsJson> pbpData, boolean isAssist)
 	{
 		ArrayList<Player> possiblePlayers, matchingPlayers;
 		int teamID;
 		
 		if (role.equals(PlayRole.NEUTRAL))
 		{
 			player.setPlayerID(-1);
 			System.out.println("Could not find player on 2nd pass: " 
 					+ player.getPlayerName() + ", Player on both teams");
 			return;
 		}
 		
 		if (role.equals(PlayRole.HOME))
 			teamID = homeID;
 		else
 			teamID = awayID;
 		
 		possiblePlayers = new ArrayList<Player>();
 		
 		if (currentPlay.getPlayType() instanceof Rebound)
 		{
 			possiblePlayers = parseReb(teamID, pbpData);
 		}
 		else if (currentPlay.getPlayType() instanceof Turnover)
 		{
 			possiblePlayers = parseTO(teamID, pbpData);
 		}
 		else if (currentPlay.getPlayType() instanceof Shot)
 		{
 			if (isAssist)
 			{
 				possiblePlayers = parseAst(teamID, pbpData);
 			}
 			else
 			{
 				possiblePlayers = parseShot(teamID, pbpData);
 			}
 		}
 		else if (currentPlay.getPlayType() instanceof Foul)
 		{
 			possiblePlayers = parsePF(teamID, pbpData);
 		}
 		else if (currentPlay.getPlayType() instanceof FreeThrow)
 		{
 			possiblePlayers = parseFT(teamID, pbpData);
 		}
 		else if (currentPlay.getPlayType() instanceof Steal)
 		{
 			possiblePlayers = parseStl(teamID, pbpData);
 		}
 		else if (currentPlay.getPlayType() instanceof Block)
 		{
 			possiblePlayers = parseBlk(teamID, pbpData);
 		}
 		
 		matchingPlayers = getMatchingPlayers(possiblePlayers, player);
 		
 		if (matchingPlayers.size() < 1)
 		{
 			System.out.println("Could not find player on 3rd pass: " 
 								+ player.getPlayerName());
 			player.setPlayerID(-1);
 		}
 		else if (matchingPlayers.size() == 1)
 		{
 			player.setPlayerID(matchingPlayers.get(0).getPlayerID());
 			player.setPlayerName(matchingPlayers.get(0).getPlayerName());
 		}
 		else
 		{
 			System.out.println("Could not narrow results on 3rd pass: " 
 					+ player.getPlayerName());
 			player.setPlayerID(-1);
 		}
 	}
 	
 	private void parsePlayData(Player player, Play currentPlay, PlayRole role,
 			ArrayList<PlayerStatsJson> pbpData, PBPJson nextActivePlay,
 			PBPJson previousActivePlay)
 	{
 		ArrayList<Player> playersOnFloorCurrent, playersOnBenchCurrent,
 			playersOnFloorAfter, playersOnBenchAfter, playersOnBenchPrior,
 			playersOnFloorPrior, matchingPlayers;
 		ArrayList<PlayerStatsJson> pbpDataFollowing, pbpDataPrior;
 		int startTime, endTime;
 		Substitution sub = (Substitution)currentPlay.getPlayType();
 		
 		playersOnFloorCurrent = new ArrayList<Player>();
 		playersOnBenchCurrent = new ArrayList<Player>();
 		playersOnFloorAfter = new ArrayList<Player>();
 		playersOnBenchAfter = new ArrayList<Player>();
 		playersOnFloorPrior = new ArrayList<Player>();
 		playersOnBenchPrior = new ArrayList<Player>();
 		matchingPlayers = new ArrayList<Player>();
 		
 		startTime = convertStringTime(nextActivePlay.getGameTime());
 		startTime += addPeriodTime(nextActivePlay.getPeriod());
 		startTime -= 10;
 		if (startTime == -10)
 		{
 			startTime = 0;
 		}
 		endTime = convertStringTime(nextActivePlay.getGameTime());
 		endTime += addPeriodTime(nextActivePlay.getPeriod());
 		endTime += 10;
 		
 		pbpDataFollowing = downloadCustomBoxScore(startTime, endTime);
 		
 		startTime = convertStringTime(previousActivePlay.getGameTime());
 		startTime += addPeriodTime(previousActivePlay.getPeriod());
 		startTime -= 10;
 		if (startTime == -10)
 		{
 			startTime = 0;
 		}
 		endTime = convertStringTime(previousActivePlay.getGameTime());
 		endTime += addPeriodTime(previousActivePlay.getPeriod());
 		endTime += 10;
 		
 		pbpDataPrior = downloadCustomBoxScore(startTime, endTime);
 		
 		if (role.equals(PlayRole.HOME))
 		{
 			playersOnFloorCurrent = parseTeam(homeID, pbpData);
 			playersOnBenchCurrent = new ArrayList<Player>(getHomeActive());
 			playersOnBenchCurrent.removeAll(playersOnFloorCurrent);
 			playersOnFloorAfter = parseTeam(homeID, pbpDataFollowing);
 			playersOnBenchAfter = new ArrayList<Player>(getHomeActive());
 			playersOnBenchAfter.removeAll(playersOnFloorAfter);
 			playersOnFloorPrior = parseTeam(homeID, pbpDataPrior);
 			playersOnBenchPrior = new ArrayList<Player>(getHomeActive());
 			playersOnBenchPrior.removeAll(playersOnFloorPrior);
 		}
 		else if (role.equals(PlayRole.AWAY))
 		{
 			playersOnFloorCurrent = parseTeam(awayID, pbpData);
 			playersOnBenchCurrent = new ArrayList<Player>(getAwayActive());
 			playersOnBenchCurrent.removeAll(playersOnFloorCurrent);
 			playersOnFloorAfter = parseTeam(awayID, pbpDataFollowing);
 			playersOnBenchAfter = new ArrayList<Player>(getAwayActive());
 			playersOnBenchAfter.removeAll(playersOnFloorAfter);
 			playersOnFloorPrior = parseTeam(awayID, pbpDataPrior);
 			playersOnBenchPrior = new ArrayList<Player>(getAwayActive());
 			playersOnBenchPrior.removeAll(playersOnFloorPrior);
 		}
 		else
 		{
 			//TODO error, no neutral sub
 		}
 		
 		if (previousActivePlay.getPeriod() != nextActivePlay.getPeriod())
 		{
 			//because plays take place over different periods, the lineup is not guaranteed
			playersOnFloorCurrent.addAll(playersOnBenchCurrent);
 			matchingPlayers = getMatchingPlayers(playersOnFloorCurrent, player);
 			player.setPlayerID(matchingPlayers.get(0).getPlayerID());
 			player.setPlayerName(matchingPlayers.get(0).getPlayerName());
 			return;
 		}
 		
 		if (playersOnFloorCurrent.size() == 5)
 		{
 			if (sub.getIn().equals(player))
 			{
 				playersOnFloorAfter.removeAll(playersOnFloorCurrent);
 				matchingPlayers = getMatchingPlayers(playersOnFloorAfter, player);
 			}
 			else if (sub.getOut().equals(player))
 			{
 				playersOnBenchAfter.removeAll(playersOnBenchCurrent);
 				matchingPlayers = getMatchingPlayers(playersOnBenchAfter, player);
 			}
 			else
 			{
 				//TODO error, player not found
 			}
 		}
 		else if (playersOnFloorCurrent.size() > 5)
 		{
 			if (sub.getIn().equals(player))
 			{
 				playersOnFloorAfter.removeAll(playersOnFloorPrior);
 				matchingPlayers = getMatchingPlayers(playersOnFloorAfter, player);
 			}
 			else if (sub.getOut().equals(player))
 			{
 				playersOnBenchAfter.removeAll(playersOnBenchCurrent);
 				matchingPlayers = getMatchingPlayers(playersOnBenchAfter, player);
 			}
 			else
 			{
 				//TODO error, player not found
 			}
 		}
 		else
 		{
 			//TODO, less than 5 players on floor
 		}
 		
 		if (matchingPlayers.size() < 1)
 		{
 			System.out.println("Could not find player on 3rd pass: " 
 						+ player.getPlayerName());
 			player.setPlayerID(-1);
 		}
 		else if (matchingPlayers.size() == 1)
 		{
 			player.setPlayerID(matchingPlayers.get(0).getPlayerID());
 			player.setPlayerName(matchingPlayers.get(0).getPlayerName());
 		}
 		else
 		{
 			matchingPlayers.removeAll(this.subbedPlayers);
 			player.setPlayerID(matchingPlayers.get(0).getPlayerID());
 			player.setPlayerName(matchingPlayers.get(0).getPlayerName());
 			this.subbedPlayers.add(matchingPlayers.get(0));
 		}
 	}
 	
 	public boolean searchAwayPlayers(Player player)
 	{
 		return getAwayActive().contains(player);
 	}
 	
 	public boolean searchPlayers(Player player)
 	{
 		return getActive().contains(player);
 	}
 	
 	private String[] cleanPlayerName(String playerName)
 	{
 		String tempPlayerName;
 		
 		tempPlayerName = playerName.replace('.', ' ');
 		tempPlayerName = tempPlayerName.trim();
 		return tempPlayerName.split(" ");
 	}
 	
 	private Player searchPlayerText(ArrayList<Player> players, String[] playerName)
 	{
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
 	
 	public class CheckReb implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return player.getTeamID() == teamID &&
 					player.getReb() > 0;
 		}
 	}
 	
 	public class CheckTO implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return player.getTeamID() == teamID &&
 					player.getTo() > 0;
 		}
 	}
 	
 	public class CheckShot implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return player.getTeamID() == teamID &&
 					player.getFga() > 0;
 		}
 	}
 	
 	public class CheckPF implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return player.getTeamID() == teamID &&
 					player.getPf() > 0;
 		}
 	}
 	
 	public class CheckFT implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return player.getTeamID() == teamID &&
 					player.getFta() > 0;
 		}
 	}
 	
 	public class CheckStl implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return player.getTeamID() == teamID &&
 					player.getStl() > 0;
 		}
 	}
 	
 	public class CheckBlk implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return player.getTeamID() == teamID &&
 					player.getBlk() > 0;
 		}
 	}
 	
 	public class CheckAst implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return player.getTeamID() == teamID &&
 					player.getAst() > 0;
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
 	
 	private ArrayList<Player> parseTeam(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckTeam());
 	}
 	
 	private ArrayList<Player> parseReb(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckReb());
 	}
 	
 	private ArrayList<Player> parseTO(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckTO());
 	}
 	
 	private ArrayList<Player> parseShot(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckShot());
 	}
 	
 	private ArrayList<Player> parsePF(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckPF());
 	}
 	
 	private ArrayList<Player> parseFT(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckFT());
 	}
 	
 	private ArrayList<Player> parseStl(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckStl());
 	}
 	
 	private ArrayList<Player> parseBlk(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckBlk());
 	}
 	
 	private ArrayList<Player> parseAst(int teamID,
 			ArrayList<PlayerStatsJson> players)
 	{
 		return parsePlayers(teamID, players, new CheckAst());
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
 	
 	private int convertStringTime(String time)
 	{
 		String[] timeParts = time.split(":");
 		String min = timeParts[0];
 		String tens = timeParts[1].substring(0,1);
 		String singles = timeParts[1].substring(1, 2);
 		return 7200 - ((Integer.parseInt(min) * 60) + (Integer.parseInt(tens) * 10) +
 				Integer.parseInt(singles)) * 10;
 	}
 	
 	private int addPeriodTime(int period)
 	{
 		if (period < 5)
 		{
 			return (period - 1) * (12 * 60 * 10);
 		}
 		else
 		{
 			return ((period - 4) * (5 * 60 * 10)) +
 					((period - 1) * (12 * 60 * 10));
 		}
 	}
 	
 	private ArrayList<PlayerStatsJson> downloadCustomBoxScore(int startTime,
 															  int endTime)
 	{
 		try 
 		{
 			Thread.sleep(3000);
 		} 
 		catch (InterruptedException e) 
 		{
 			e.printStackTrace();
 		}
 		BufferedReader br = NBADownloader.downloadCustomBox(
 				nbaGameID, startTime, endTime);
 		return BoxJson.getBoxScore(br).getPlayerStats();
 	}
 }
