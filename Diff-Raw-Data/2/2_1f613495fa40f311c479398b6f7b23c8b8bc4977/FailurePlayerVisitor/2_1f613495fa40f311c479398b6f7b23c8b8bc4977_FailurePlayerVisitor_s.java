 package visitors;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import nba.Period;
 import nba.PlayRole;
 import nba.Player;
 import nba.play.Play;
 import nbaDownloader.NBADownloader;
 import jsonObjects.BoxJson;
 import jsonObjects.PBPJson;
 import jsonObjects.boxScoreObjects.PlayerStatsJson;
 import codeGenerator.PlayerParser;
 import codeGenerator.RosterSQLGenerator;
 
 public class FailurePlayerVisitor extends SubstitutionPlayerVisitor 
 {
 	private ArrayList<PBPJson> pbp;
 	private int homeID, awayID;
 	
 	public FailurePlayerVisitor(RosterSQLGenerator rosters, ArrayList<PBPJson> pbp,
 			int homeID, int awayID) 
 	{
 		super(rosters);
 		this.pbp = new ArrayList<PBPJson>(pbp);
 		this.homeID = homeID;
 		this.awayID = awayID;
		Collections.sort(pbp, PBPJson.COMPARE_BY_PLAY_ID);
 	}
 	
 	@Override
 	public void visit(Period period) 
 	{
 		if (this.currentRole.equals(PlayRole.HOME))
 			this.playersOnFloor = GetInitialLineup(period, this.reversed, homeID);
 		else
 			this.playersOnFloor = GetInitialLineup(period, this.reversed, awayID);
 			
 		if (!this.reversed)
 			VisitEach(period.getPlays());
 		else
 			VisitEachInReverse(period.getPlays());
 	}
 	
 	private ArrayList<Player> GetInitialLineup(Period period, boolean reversed, int teamID)
 	{
 		Play playSearched;
 		int index, startTime, endTime;
 		PBPJson relevantPlay = new PBPJson();
 		ArrayList<PlayerStatsJson> pbpData;
 		
 		if (!reversed)
 		{
 			playSearched = period.getPlays().get(0);
 		}
 		else
 		{
 			playSearched = period.getPlays().get(period.getPlays().size() - 1);
 		}
 		
 		relevantPlay.setEventNum(playSearched.getPlayID());
 		
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
 			System.out.println("Play: " + playSearched.getPlayID() + 
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
 		
 		return TalliedDuplicatePlayerVisitor.parsePlayers(teamID, pbpData, new CheckTeam());
 	}
 	
 	public class CheckTeam implements PlayerParser<PlayerStatsJson>
 	{
 		@Override
 		public boolean check(int teamID, PlayerStatsJson player) 
 		{
 			return player.getTeamID() == teamID;
 		}
 	}
 
 }
