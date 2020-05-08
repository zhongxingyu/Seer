 package visitors;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import jsonObjects.BoxJson;
 import jsonObjects.PBPJson;
 import jsonObjects.boxScoreObjects.PlayerStatsJson;
 
 import nba.PlayRole;
 import nba.Player;
 import nba.playType.block.Block;
 import nba.playType.ejection.Ejection;
 import nba.playType.foul.Foul;
 import nba.playType.freeThrow.FreeThrow;
 import nba.playType.jumpBall.JumpBall;
 import nba.playType.rebound.Rebound;
 import nba.playType.shot.Assist;
 import nba.playType.shot.Shot;
 import nba.playType.steal.Steal;
 import nba.playType.substitution.Substitution;
 import nba.playType.technical.DoubleTechnical;
 import nba.playType.technical.TauntingTechnical;
 import nba.playType.technical.Technical;
 import nba.playType.timeout.Timeout;
 import nba.playType.turnover.Turnover;
 import nba.playType.violation.Violation;
 import nbaDownloader.NBADownloader;
 import codeGenerator.PlayerParser;
 import codeGenerator.RosterSQLGenerator;
 
 public class TalliedDuplicatePlayerVisitor extends PlayerVisitor 
 {
 
 	private ArrayList<PBPJson> pbp;
 	private int homeID, awayID;
 	private PlayerParser<PlayerStatsJson> currentFilter;
 	
 	public TalliedDuplicatePlayerVisitor(RosterSQLGenerator rosters) 
 	{
 		super(rosters);
 	}
 	
 	public TalliedDuplicatePlayerVisitor(RosterSQLGenerator rosters, ArrayList<PBPJson> pbp,
 			int homeID, int awayID) 
 	{
 		this(rosters);
 		this.homeID = homeID;
 		this.awayID = awayID;
 		this.pbp = new ArrayList<PBPJson>(pbp);
 		Collections.sort(pbp, PBPJson.COMPARE_BY_PLAY_ID);
 	}
 	
 	@Override
 	public void visit(Rebound rebound)
 	{
 		this.currentFilter = new CheckReb();
 		super.visit(rebound);
 	}
 	
 	@Override
 	public void visit(Turnover turnover)
 	{
 		this.currentFilter = new CheckTO();
 		super.visit(turnover);
 	}
 	
 	@Override
 	public void visit(Shot shot)
 	{
 		this.currentFilter = new CheckShot();
 		super.visit(shot);
 	}
 	
 	@Override
 	public void visit(Assist assist)
 	{
 		this.currentFilter = new CheckAst();
 		super.visit(assist);
 	}
 	
 	@Override
 	public void visit(Foul foul)
 	{
 		this.currentFilter = new CheckPF();
 		super.visit(foul);
 	}
 	
 	@Override
 	public void visit(FreeThrow ft)
 	{
 		this.currentFilter = new CheckFT();
 		super.visit(ft);
 	}
 	
 	@Override
 	public void visit(Steal steal)
 	{
 		this.currentFilter = new CheckStl();
 		super.visit(steal);
 	}
 	
 	@Override
 	public void visit(Block block)
 	{
 		this.currentFilter = new CheckBlk();
 		super.visit(block);
 	}
 	
 	@Override
 	public void visit(Ejection ejection)
 	{
 		//take no action (play type not tallied in box score)
 	}
 	
 	@Override
 	public void visit(JumpBall jumpBall) 
 	{
 		//take no action (play type not tallied in box score)
 	}
 	
 	@Override
 	public void visit(Substitution sub) 
 	{
 		//take no action (play type not tallied in box score)
 	}
 	
 	@Override
 	public void visit(Technical technical) 
 	{
 		//take no action (play type not tallied in box score)
 	}
 	
 	@Override
 	public void visit(DoubleTechnical technical) 
 	{
 		//take no action (play type not tallied in box score)
 	}
 
 	@Override
 	public void visit(TauntingTechnical technical) 
 	{
 		//take no action (play type not tallied in box score)
 	}
 	
 	@Override
 	public void visit(Timeout timeout) 
 	{
 		//take no action (play type not tallied in box score)
 	}
 	
 	@Override
 	public void visit(Violation violation) 
 	{
 		//take no action (play type not tallied in box score)
 	}
 
 	@Override
 	protected void setPlayer(Player player, ArrayList<Player> possiblePlayers) 
 	{
 		if (player.getPlayerID() > 0)
 		{
 			return;
 		}
 		
 		int index, startTime, endTime;
 		PBPJson relevantPlay = new PBPJson();
 		ArrayList<PlayerStatsJson> pbpData;
 		ArrayList<Player> boxScorePlayers, matchingPlayers;
 		
 		boxScorePlayers = new ArrayList<Player>();
 		
 		relevantPlay.setEventNum(currentContext.getPlayID());
 		
 		index = -1;
 		try
 		{
 			index = Collections.binarySearch(this.pbp, relevantPlay, 
 				PBPJson.COMPARE_BY_PLAY_ID);
 		}
 		catch (Exception e)
 		{
 			
 		}
 		
 		if (index < 0)
 		{
 			System.out.println("Play: " + currentContext.getPlayID() + 
 					" Play not found.");
 			System.exit(-1);
 		}
 		else
 		{
 			relevantPlay = this.pbp.get(index);
 		}
 		
 		startTime = PBPJson.convertStringTime(relevantPlay.getGameTime(), relevantPlay);
 		startTime -= 10;
 		if (startTime == -10)
 		{
 			startTime = 0;
 		}
 		endTime = startTime + 20;
 		
 		pbpData = BoxJson.getDownloadedBoxScorePlayers(
 				NBADownloader.downloadCustomBox(pbp.get(0).getGameID(),
 				startTime, endTime));
 		
 		if (currentContext.getPlayRole() == PlayRole.HOME)
 		{
 			boxScorePlayers = parsePlayers(homeID, pbpData, new CheckTeam());
 		}
 		else if (currentContext.getPlayRole() == PlayRole.AWAY)
 		{
 			boxScorePlayers = parsePlayers(awayID, pbpData, new CheckTeam());
 		}
 		else
 		{
 			//TODO error, there should be no neutral plays
 		}
 		
 		matchingPlayers = RosterSQLGenerator.getMatchingPlayers(boxScorePlayers, player);
 		
 		if (matchingPlayers.size() < 1)
 		{
 			System.out.println("Could not find through box score on 2nd pass: " 
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
 			parsePlayData(player, pbpData);
 		}
 	}
 	
 	private void parsePlayData(Player player, ArrayList<PlayerStatsJson> pbpData)
 	{
 		ArrayList<Player> possiblePlayers, matchingPlayers;
 		int teamID;
 		
		if (currentContext.getPlayRole() == (PlayRole.HOME))
 			teamID = homeID;
 		else
 			teamID = awayID;
 		
 		possiblePlayers = new ArrayList<Player>();
 		
 		possiblePlayers = parsePlayers(teamID, pbpData, currentFilter);
 		
 		matchingPlayers = RosterSQLGenerator.getMatchingPlayers(possiblePlayers, player);
 		
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
 			System.out.println("Could not narrow results through box score on 3rd pass: " 
 			+ player.getPlayerName());
 			player.setPlayerID(-1);
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
 
 }
