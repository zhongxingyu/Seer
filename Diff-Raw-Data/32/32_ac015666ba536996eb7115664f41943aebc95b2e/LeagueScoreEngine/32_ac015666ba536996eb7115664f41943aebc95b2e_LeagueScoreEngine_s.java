 
 public class LeagueScoreEngine {
 	public Game evaluateGame(Game game) {
 		if (game != null) {
 			if (game.golsLocal > game.golsVisitor) {
 				game.local.score++;
 				game.visitor.score--;
 			} else {
 				game.visitor.score++;
 				game.local.score--;
 			}
 			return game;
 		}
 		
 		return null;
 	}
 }
