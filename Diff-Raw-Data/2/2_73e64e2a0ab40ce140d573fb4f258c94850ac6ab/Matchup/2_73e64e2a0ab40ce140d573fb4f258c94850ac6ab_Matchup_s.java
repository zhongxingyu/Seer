 package internal;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author ablaine
  */
 public class Matchup {
 	private final Player player1;
 	private final Player player2;
 	private final List<MatchStatus> winners;
 	private int currentMatchIndex = 0;
 
 	public Matchup(Player player1, Player player2, int totalMatches) {
 		this.player1 = player1;
 		this.player2 = player2;
 		//Fill the list with unplayed match state's.
		winners = new ArrayList(Arrays.asList(new MatchStatus[totalMatches]));
 	}
 
 	public boolean hasMoreMatches() {
 		return currentMatchIndex < winners.size();
 	}
 
 	public void playedMatch(Player winner) {
 		if (player1 == winner) {
 			player1.updateWins();
 			player2.updateLosses();
 		} else if (player2 == winner) {
 			player2.updateWins();
 			player1.updateLosses();
 		} else {
 			player1.updateTies();
 			player2.updateTies();
 		}
 		winners.set(currentMatchIndex, new MatchStatus(winner));
 		currentMatchIndex++;
 		player1.cleanup();
 		player2.cleanup();
 		System.gc();
 	}
 
 	public Player getFirst() {
 		return player1;
 	}
 
 	public Player getSecond() {
 		return player2;
 	}
 
 	public List<MatchStatus> getMatchupResults() {
 		return winners;
 	}
 
 	public MatchStatus getMatchState(int matchIndex) {
 		try {
 			return winners.get(matchIndex);
 		} catch (IndexOutOfBoundsException e) {
 			return MatchStatus.INVALID;
 		}
 	}
 
 	public MatchStatus getPreviousMatchState() {
 		return getMatchState(currentMatchIndex - 1);
 	}
 
 	public Player otherPlayer(Player p) {
 		if (player1 == p) { //Checking memory address
 			return player2;
 		} else if (player2 == p) {
 			return player1;
 		} else {//Hopefully doesn't happen
 			return null;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "[" + player1.toString() + " VS " + player2.toString() + "]";
 	}
 }
