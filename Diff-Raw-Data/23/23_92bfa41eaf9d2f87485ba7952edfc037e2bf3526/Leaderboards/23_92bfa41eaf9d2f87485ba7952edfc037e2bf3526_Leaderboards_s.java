 package controllers.api;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import controllers.services.LeaderboardService;
 
 import datatransfer.ScoreDTO;
 
 import models.Game;
 import models.Leaderboard;
 import models.Score;
 import models.User;
 import play.Play;
 import play.db.jpa.GenericModel.JPAQuery;
 import play.db.jpa.JPABase;
 import play.mvc.Controller;
 import utils.Range;
 
 public class Leaderboards extends Controller {
 
 	
 
 	static public void score() {
 		String apiKey = params.get("apiKey");
 		Game game = Game.find("byApiKey", apiKey).first();
 		if (game == null)
			error("No game matching apikey was found");
 
 		String leaderboardName = params.get("leaderboard");
 		if (leaderboardName == null)
			error("You need to provide the leaderboard name");
 
 		Leaderboard leaderboard = Leaderboard.find("byGameAndName", game, leaderboardName).first();
 
 		if (leaderboard == null)
			error("Leaderboard (" + leaderboardName + ") doesn't match game (" + game.name + ")");
 
 		String userId = params.get("userId");
 		if (userId == null)
			error("You need to provide the userId of the user");
 
 		User user = User.find("byUserId", Long.parseLong(userId)).first();
 		if (!user.privatekey.equals(params.get("privatekey")))
			error("User authentication failed");
 
 		Long scoreValue = params.get("score", Long.class);
 
 		DateTime dateTime = new DateTime();
 		String overridendayofyear = params.get("dayofyear");
 		if (Play.runingInTestMode() && overridendayofyear != null) {
 			System.out.println("Overriding Date to " + overridendayofyear);
 			dateTime = dateTime.dayOfYear().setCopy(Integer.parseInt(overridendayofyear));
 		}
 
 		LeaderboardService.submitScore(leaderboard, user, scoreValue, dateTime);
 		renderText("Score submitted succesfully");
 	}
 
 
 	
 
 	public static void scores() {
 		
 		System.out.println(params.allSimple());
 		
 		String apiKey = params.get("apiKey");
 		Game game = Game.find("byApiKey", apiKey).first();
 		if (game == null)
			error("No game matching apikey was found");
 
 		String leaderboardName = params.get("leaderboard");
 		if (leaderboardName == null)
			error("You need to provide the leaderboard name");
 
 		Leaderboard leaderboard = Leaderboard.find("byGameAndName", game, leaderboardName).first();
 
 		if (leaderboard == null)
			error("Leaderboard (" + leaderboardName + ") doesn't match game (" + game.name + ")");
 
 		String rangeKey = params.get("range");
 
 		Range range = Range.getByKey(rangeKey);
 		if (range == null)
 			range = Range.All;
 
 
 		DateTime dateTime = new DateTime();
 		String overridendayofyear = params.get("dayofyear");
 		if (Play.runingInTestMode() && overridendayofyear != null) {
 			System.out.println("Overriding Date to " + overridendayofyear);
 			dateTime = dateTime.dayOfYear().setCopy(Integer.parseInt(overridendayofyear));
 		}	
 
 		int page = 0;
 		int pageSize = 20;
 		
 		String pageParam = params.get("page");
 		if(pageParam!=null && pageParam!="")
 			page = Integer.parseInt(pageParam);
 		
 		String pageSizeParam = params.get("pageSize");
 		if(pageSizeParam!=null && pageSizeParam!="")
 			pageSize = Integer.parseInt(pageSizeParam);
 		
 		
 		List<Score> scores = LeaderboardService.getScores(leaderboard, range, dateTime, page, pageSize);
 
 		List<ScoreDTO> scoreDTOs = ScoreDTO.convert(scores);
 		renderJSON(scoreDTOs);
 
 	}
 }
