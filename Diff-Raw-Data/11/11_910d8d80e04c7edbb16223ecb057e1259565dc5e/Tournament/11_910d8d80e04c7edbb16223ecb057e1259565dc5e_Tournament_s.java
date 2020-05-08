 package helpers;
 
 import java.util.ArrayList;
 
 /**
  * {NAME}
  * Author: Phillip Boksz
  * Date: 4/2/12
  * Time: 11:09 AM
  */
 public class Tournament
 {
    private static Tournament tournament;
    private PlayerPool playerPool;
 
    private int prevRound = 0;
    private int round = 1;
    private int numPlayers;
    private int maxRound;
    private int bestOf;
    private String format;
 
    public Tournament(int numPlayers, int maxRound, int bestOf, String format) {
       this.numPlayers = numPlayers;
       this.maxRound = maxRound;
       this.bestOf = bestOf;
       this.format = format;
       this.playerPool = new PlayerPool();
    }
    
    public static void newTournament(int numPlayers, int maxRound, int bestOf, String format){
       tournament = new Tournament(numPlayers, maxRound, bestOf, format);
    }
 
    public static Tournament getTournament() {
       return tournament;
    }
 
    public PlayerPool getPlayerPool() {
       return playerPool;
    }
 
    public int getPrevRound() {
       return prevRound;
    }
 
    public int getRound() {
       return round;
    }
 
    public int getNumPlayers() {
       return numPlayers;
    }
 
    public int getMaxRound() {
       return maxRound;
    }
 
    public int getBestOf() {
       return bestOf;
    }
 
    public String getFormat() {
       return format;
    }
 
    public int getMaxWins() {
       return (int) Math.ceil(bestOf/2.0);
    }
    
    public void nextRound() {
       this.round++;
    }
 
    public void incPrevRound() {
       this.prevRound++;
    }
 
    public boolean isNextRound() {
       return round > prevRound;
    }
 
    /**
     * sets each players final ranking when called on
     *
     * @return a list of players with final ranks in place
     */
    public ArrayList<PlayerInfo> getCurrentRankings()
    {
       ArrayList<PlayerInfo> listOfPlayers = playerPool.getRankSortedListOfPlayers();
       int rank = 1;
       for (PlayerInfo player : listOfPlayers)
       {
          player.setRank(rank++);
       }
       return listOfPlayers;
    }
 }
